/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  CorefEditor.java
 *
 *  Niraj Aswani, 24-Jun-2004
 *
 *  $Id$
 */

package gate.gui.docview;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.*;
import gate.*;
import gate.creole.*;
import java.io.*;
import java.awt.event.*;
import gate.swing.*;
import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter;

public class CorefEditor extends AbstractDocumentView implements ActionListener {

  // default AnnotationSet Name
  private final static String DEFAULT_ANNOTSET_NAME = "Default";

  private JPanel mainPanel, topPanel, subPanel;
  private JToggleButton showAnnotations;
  private JComboBox annotSets, annotTypes;
  private DefaultComboBoxModel annotSetsModel, annotTypesModel;
  private JTree corefTree;
  private CorefTreeNode rootNode;

  // top level hashMap (corefChains)
  // AnnotationSet(CorefTreeNode) --> (CorefTreeNode type ChainNode --> ArrayList AnnotationIds)
  private HashMap corefChains;

  // This is used to store the annotationSet name and its respective corefTreeNode
  // annotationSetName --> CorefTreeNode of type (AnnotationSet)
  private HashMap corefAnnotationSetNodesMap;

  // annotationSetName --> (chainNodeString --> Boolean)
  private HashMap selectionChainsMap;

  // chainString --> Boolean
  private HashMap currentSelections;

  // annotationSetName --> (chainNodeString --> Color)
  private HashMap colorChainsMap;

  // chainNodeString --> Color
  private HashMap currentColors;

  private ColorGenerator colorGenerator;
  private Highlighter highlighter;
  private TextualDocumentView textView;
  private JEditorPane textPane;

  /* ChainNode --> (HighlightedTags) */
  private HashMap highlightedTags;

  /* This arraylist stores the highlighted tags for the specific selected annotation type */
  private ArrayList typeSpecificHighlightedTags;
  private TextPaneMouseListener textPaneMouseListener;

  /* This stores Ids of the highlighted Chain Annotations*/
  private ArrayList highlightedChainAnnots = new ArrayList();
  /* This stores start and end offsets of the highlightedChainAnnotations */
  private int [] highlightedChainAnnotsOffsets;

  /* This stores Ids of the highlighted Annotations of particular type */
  private ArrayList highlightedTypeAnnots = new ArrayList();
  /* This stores start and end offsets of highlightedTypeAnnots */
  private int [] highlightedTypeAnnotsOffsets;

  private ChainToolTipAction chainToolTipAction = new ChainToolTipAction();
  private javax.swing.Timer chainToolTipTimer = new javax.swing.Timer(500, chainToolTipAction);
  private NewCorefAction newCorefAction = new NewCorefAction();
  private javax.swing.Timer newCorefActionTimer = new javax.swing.Timer(500, newCorefAction);
  private Annotation annotToConsiderForChain = null;

  /**
   * This method intiates the GUI for co-reference editor
   */
  protected void initGUI(){

    // set the view to Java Look and Feel
    try {
      UIManager.setLookAndFeel(
          UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) { }


    colorGenerator = new ColorGenerator();

    // main Panel
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    // topPanel
    topPanel = new JPanel();
    topPanel.setLayout(new BorderLayout());

    // subPanel
    subPanel = new JPanel();
    subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    // showAnnotations Button
    showAnnotations = new JToggleButton("show");
    showAnnotations.addActionListener(this);

    // get all the annotationSets
    Map annotSetsMap = document.getNamedAnnotationSets();
    annotSetsModel = new DefaultComboBoxModel();
    if(annotSets != null) {
      annotSetsModel = new DefaultComboBoxModel(annotSetsMap.keySet().toArray());
    }
    annotSetsModel.insertElementAt(DEFAULT_ANNOTSET_NAME,0);
    annotSetsModel.setSelectedItem(annotSetsModel.getElementAt(0));

    // get all the types of the currently Selected AnnotationSet
    Set types = document.getAnnotations().getAllTypes();
    annotTypesModel = new DefaultComboBoxModel();
    if(types != null) {
      annotTypesModel = new DefaultComboBoxModel(types.toArray());
    }

    // annotSets
    annotSets = new JComboBox(annotSetsModel);
    annotSets.addActionListener(this);

    // annotTypes
    annotTypes = new JComboBox(annotTypesModel);
    subPanel.add(annotSets);
    subPanel.add(annotTypes);

    // intialises the Data
    initData();

    // and now tree
    rootNode.add((CorefTreeNode) corefAnnotationSetNodesMap.get(DEFAULT_ANNOTSET_NAME));
    currentSelections = (HashMap) selectionChainsMap.get(DEFAULT_ANNOTSET_NAME);
    currentColors = (HashMap) colorChainsMap.get(DEFAULT_ANNOTSET_NAME);

    // and creating the tree
    corefTree = new JTree(rootNode);
    corefTree.addMouseListener(new CorefTreeMouseListener());
    corefTree.setCellRenderer(new CorefTreeCellRenderer());

    mainPanel.add(topPanel, BorderLayout.NORTH);
    mainPanel.add(new JScrollPane(corefTree), BorderLayout.CENTER);
    JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    tempPanel.add(showAnnotations);
    topPanel.add(tempPanel, BorderLayout.NORTH);
    topPanel.add(subPanel, BorderLayout.CENTER);

    //get a pointer to the textual view used for highlights
    Iterator centralViewsIter = owner.getCentralViews().iterator();
    while(textView == null && centralViewsIter.hasNext()){
      DocumentView aView = (DocumentView)centralViewsIter.next();
      if(aView instanceof TextualDocumentView)
        textView = (TextualDocumentView)aView;
    }
    textPane = (JEditorPane)((JScrollPane)textView.getGUI()).getViewport().getView();
    // get the highlighter
    highlighter = ((JEditorPane)((JScrollPane)textView.getGUI()).getViewport().getView()).getHighlighter();
    textPaneMouseListener = new TextPaneMouseListener();
  }


  /**
   * ActionPerformed Activity
   * @param ae
   */
  public void actionPerformed(ActionEvent ae) {
    // when annotationSet value changes
    if(ae.getSource() == annotSets) {
      annotSetSelectionChanged();
    } else if(ae.getSource() == showAnnotations) {
      showTypeWiseAnnotations();
    }
  }

  /**
   * When user preses the show Toggle button, this will show up annotations
   * of selected Type from selected AnnotationSet
   */
  private void showTypeWiseAnnotations() {
    if(typeSpecificHighlightedTags == null) {
      typeSpecificHighlightedTags = new ArrayList();
      highlightedTypeAnnots = new ArrayList();
      typeSpecificHighlightedTags = new ArrayList();
    }

    if(showAnnotations.isSelected()) {
      // get the annotationsSet and its type
      AnnotationSet set = getAnnotationSet((String) annotSets.getSelectedItem());
      String type = (String) annotTypes.getSelectedItem();
      if(type == null) {
        try {
          JOptionPane.showMessageDialog(Main.getMainFrame(),
                                        "No annotation type found to display");
        }catch(Exception e) {
          e.printStackTrace();
        }
        showAnnotations.setSelected(false);
        return;
      }


      Color color = getColor(type);
      if(type != null) {
        AnnotationSet typeSet = set.get(type);
        Iterator iter = typeSet.iterator();
        while (iter.hasNext()) {
          Annotation ann = (Annotation) iter.next();
          highlightedTypeAnnots.add(ann);
          try {
            typeSpecificHighlightedTags.add(highlighter.addHighlight(ann.
                getStartNode().getOffset().intValue(),
                ann.getEndNode().getOffset().intValue(),
                new DefaultHighlighter.DefaultHighlightPainter(color)));
          } catch(javax.swing.text.BadLocationException e) {
              e.printStackTrace();
          }
        }
      }
    } else {
      for(int i=0;i<typeSpecificHighlightedTags.size();i++) {
        highlighter.removeHighlight(typeSpecificHighlightedTags.get(i));
      }
      typeSpecificHighlightedTags = new ArrayList();
      highlightedTypeAnnots = new ArrayList();
      highlightedTypeAnnotsOffsets = null;
    }

    // This is to make process faster.. instead of accessing each annotation and
    // its offset, we create an array with its annotation offsets to search faster
    Collections.sort(highlightedTypeAnnots,new gate.util.OffsetComparator());
    highlightedTypeAnnotsOffsets = new int[highlightedTypeAnnots.size() * 2];
    for(int i=0,j=0;j<highlightedTypeAnnots.size();i+=2,j++) {
      Annotation ann1 = (Annotation) highlightedTypeAnnots.get(j);
      highlightedTypeAnnotsOffsets[i] = ann1.getStartNode().getOffset().intValue();
      highlightedTypeAnnotsOffsets[i+1] = ann1.getEndNode().getOffset().intValue();
    }

  }

  /**
   * Returns annotation Set
   * @param annotSet
   * @return
   */
  private AnnotationSet getAnnotationSet(String annotSet) {
    return (annotSet.equals(DEFAULT_ANNOTSET_NAME)) ? document.getAnnotations() :
        document.getAnnotations(annotSet);
  }


  /**
   * When annotationSet selection changes
   */
  private void annotSetSelectionChanged() {
    String currentAnnotSet = (String) annotSets.getSelectedItem();

    // get all the types of the currently Selected AnnotationSet
    AnnotationSet temp = getAnnotationSet(currentAnnotSet);
    Set types = temp.getAllTypes();
    annotTypesModel = new DefaultComboBoxModel();
    if(types != null) {
      annotTypesModel = new DefaultComboBoxModel(types.toArray());
    }
    annotTypes.setModel(annotTypesModel);
    annotTypes.updateUI();

    // and redraw the CorefTree
    rootNode.removeAllChildren();
    rootNode.add( (CorefTreeNode) corefAnnotationSetNodesMap.get(
        currentAnnotSet));
    currentSelections = (HashMap) selectionChainsMap.get(currentAnnotSet);
    currentColors = (HashMap) colorChainsMap.get(currentAnnotSet);

    corefTree.repaint();
    corefTree.updateUI();

  }


  /**
   * This will initialise the data
   */
  private void initData() {


    //************************************************************************
    // Internal Data structure
    // top level hashMap (corefChains)
    // AnnotationSet(CorefTreeNode) --> (CorefTreeNode type ChainNode --> ArrayList AnnotationIds)
    //
    // another toplevel hashMap (corefAnnotationSetsNodesMap)
    // annotationSetName --> annotationSetNode(CorefChainTreeNode)
    //************************************************************************

    rootNode = new CorefTreeNode("Coreference Data", true, CorefTreeNode.ROOT_NODE);
    corefChains = new HashMap();
    selectionChainsMap = new HashMap();
    currentSelections = new HashMap();
    colorChainsMap = new HashMap();
    currentColors = new HashMap();
    corefAnnotationSetNodesMap = new HashMap();
    // now we need to findout the chains
    // for the defaultAnnotationSet
    corefAnnotationSetNodesMap.put(DEFAULT_ANNOTSET_NAME, createChain(document.getAnnotations(), true));
    // and for the rest AnnotationSets
    Map annotSets = document.getNamedAnnotationSets();
    if(annotSets != null) {
      Iterator annotSetsIter = annotSets.keySet().iterator();
      while (annotSetsIter.hasNext()) {
        String annotSetName = (String) annotSetsIter.next();
        corefAnnotationSetNodesMap.put(annotSetName, createChain(document.getAnnotations(annotSetName) , false));
      }
    }
  }



  /**
   * Creates the internal data structure
   * @param set
   */
  private CorefTreeNode createChain(AnnotationSet set, boolean isDefaultSet) {

    //************************************************************************
    // Internal Data structure
    // top level hashMap (corefChains)
    // AnnotationSet(CorefTreeNode) --> (CorefTreeNode type ChainNode --> ArrayList AnnotationIds)
    //
    // another toplevel hashMap (corefAnnotationSetsNodesMap)
    // annotationSetName --> annotationSetNode(CorefChainTreeNode)
    //************************************************************************


    // create the node for setName
    String setName = isDefaultSet ? DEFAULT_ANNOTSET_NAME : set.getName();
    CorefTreeNode annotSetNode = new CorefTreeNode(setName, true, CorefTreeNode.ANNOTSET_NODE);

    // create the map for all the annotations with matches feature in it
    ArrayList annotations = new ArrayList();
    Iterator iter = set.iterator();
    while(iter.hasNext()) {
      Annotation ann = (Annotation) iter.next();
      if(ann.getFeatures().get(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME) != null)
        annotations.add(ann);
    }

    // and now create the internal datastructure
    HashMap chainLinks = new HashMap();
    HashMap selectionMap = new HashMap();
    HashMap colorMap = new HashMap();

    // and take one group at a time, find out the longest string and create the chain
    ArrayList tempAnnotations = new ArrayList();
    Iterator tempIter = annotations.iterator();
    while(tempIter.hasNext()) {
      Annotation ann = (Annotation) tempIter.next();
      if(tempAnnotations.contains(ann)) {
        continue;
      }
      ArrayList matches = (ArrayList) ann.getFeatures().get(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME);
      int length = 0;
      int index = 0;
      matches.add(ann.getId());
      for(int i=0;i<matches.size();i++) {
        Annotation currAnn = (Annotation) set.get((Integer) matches.get(i));
        int start = currAnn.getStartNode().getOffset().intValue();
        int end = currAnn.getEndNode().getOffset().intValue();
        if((end - start) > length) {
          length = end - start;
          index = i;
        }
        tempAnnotations.add(currAnn);
      }

      // so now we now have the longest String annotations at index
      Annotation temp = (Annotation) set.get( (Integer) matches.get(index));
      String longestString = getString(temp);
      // so this should become one of the tree node
      CorefTreeNode chainNode = new CorefTreeNode(longestString, false, CorefTreeNode.CHAIN_NODE);
      // and add it under the topNode
      annotSetNode.add(chainNode);

      // chainNode --> All related annotIds
      chainLinks.put(chainNode, matches);
      selectionMap.put(chainNode.toString(), new Boolean(false));
      // and generate the color for this chainNode
      float components[] = colorGenerator.getNextColor().getComponents(null);
      Color color = new Color(components[0],
                         components[1],
                         components[2],
                         0.5f);
      colorMap.put(chainNode.toString(), color);
    }
    corefChains.put(annotSetNode, chainLinks);
    selectionChainsMap.put(setName, selectionMap);
    colorChainsMap.put(setName, colorMap);
    return annotSetNode;
  }

  public String getString(Annotation ann) {
    return document.getContent().toString().substring(ann.
          getStartNode().getOffset().intValue(),
                             ann.getEndNode().getOffset().intValue());
  }

  /**
   * This method removes the reference of this annotatation from the current chain
   * @param ann
   */
  public void removeChainReference(Annotation annot, CorefTreeNode chainHead) {
    // from where we need to remove this entry
    // 1. colorChainMaps
    // 2. corefChains
    // 3. corefTree
    // 4. corefTreeModel
    // 5. currentColors
    // 6. currentSelections
    // 7. highlightedChainAnnots
    // 8. highlightedChainAnnotsOffsets
    // 9. selectionChainMaps

    // so we would find out the matches
    CorefTreeNode currentNode = chainHead;
    // so currentNode is the head of the selected annotations
    // now first see if the selected annotation itself is the head
    if (getString(annot).equals(currentNode.toString())) {
        // yes the head itself required to be deleted

        // 1. We need to remove its reference from all its corrosponding annotations matching List
        HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
        ArrayList matches = (ArrayList) (annot.getFeatures().get(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME));
        AnnotationSet set = getAnnotationSet((String) annotSets.getSelectedItem());
        for(int i=0;matches != null && i<matches.size();i++) {
          Annotation ann = (Annotation) set.get((Integer) matches.get(i));
          ((ArrayList) ann.getFeatures().get(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME)).remove(annot.getId());
        }

        // 2. We need to remove it from the ArrayList Ids from the corefChains
        ArrayList chainIds = (ArrayList) chains.get(currentNode);
        chainIds.remove(annot.getId());

        // 3. Now we need to change the value of currentNode
        if(!chainIds.isEmpty()) {
          // so let's find out the longestString
          int length = 0;
          int index = 0;
          for (int i = 0; i < chainIds.size(); i++) {
            Annotation currAnn = (Annotation) set.get( (Integer) chainIds.get(i));
            int start = currAnn.getStartNode().getOffset().intValue();
            int end = currAnn.getEndNode().getOffset().intValue();
            if ( (end - start) > length) {
              length = end - start;
              index = i;
            }
          }
          Annotation currAnn = (Annotation) set.get( (Integer) chainIds.get(index));
          String longestString = getString(currAnn);
          String previousString = currentNode.toString();
          currentNode.setUserObject(longestString);

          // 4. we need to make changes in the following instances
          // colorChainsMap
          // currentColors
          // selectionChainsMap
          // currentSelections
          HashMap temp = (HashMap) colorChainsMap.get((String) annotSets.getSelectedItem());
          Color color = (Color) temp.get(previousString);
          temp.remove(previousString);
          temp.put(longestString, color);
          currentColors = temp;
          colorChainsMap.put((String) annotSets.getSelectedItem(), temp);

          temp = (HashMap) selectionChainsMap.get((String) annotSets.getSelectedItem());
          Boolean value = (Boolean) temp.get(previousString);
          temp.remove(previousString);
          temp.put(longestString, value);
          currentSelections = temp;
          selectionChainsMap.put((String) annotSets.getSelectedItem(), temp);

          // and finally make arrangements for highlighting
          highlightedTags = null;

          // redraw the tree
          corefTree.repaint();
          corefTree.updateUI();

        } else {
          // the chainIds is empty.. so we need to remove this from the
          // 1. remove it from
          // colorChainsMap
          // currentColors
          // selectionChainsMap
          // currentSelections
          String previousString = currentNode.toString();
          HashMap temp = (HashMap) colorChainsMap.get((String) annotSets.getSelectedItem());
          temp.remove(previousString);
          currentColors = temp;
          colorChainsMap.put((String) annotSets.getSelectedItem(), temp);

          temp = (HashMap) selectionChainsMap.get((String) annotSets.getSelectedItem());
          Boolean value = (Boolean) temp.get(previousString);
          temp.remove(previousString);
          currentSelections = temp;
          selectionChainsMap.put((String) annotSets.getSelectedItem(), temp);

          // 2. remove the currentNode from the tree
          CorefTreeNode annotSetNode = (CorefTreeNode) corefAnnotationSetNodesMap.get(annotSets.getSelectedItem());
          annotSetNode.remove(currentNode);

          chains.remove(currentNode);
          corefChains.put(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()), chains);

          // and finally make arrangements for highlighting
          highlightedTags = null;

          // redraw the tree
          corefTree.repaint();
          corefTree.updateUI();

        }

    } else {
      // this is not the chain head
      // 1. We need to remove its reference from all its corrosponding annotations matching List
      HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
      ArrayList matches = (ArrayList) (annot.getFeatures().get(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME));
      AnnotationSet set = getAnnotationSet((String) annotSets.getSelectedItem());
      for(int i=0;i<matches.size();i++) {
        Annotation ann = (Annotation) set.get((Integer) matches.get(i));
        ((ArrayList) ann.getFeatures().get(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME)).remove(annot.getId());
      }
      // 2. We need to remove it from the ArrayList Ids from the corefChains
      ArrayList chainIds = (ArrayList) chains.get(currentNode);
      chainIds.remove(annot.getId());
      chains.put(currentNode, chainIds);
      highlightedTags = null;
    }
  }


  private CorefTreeNode findOutTheChainHead(Annotation ann) {
    HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
    Iterator iter = chains.keySet().iterator();
    while(iter.hasNext()) {
      CorefTreeNode head = (CorefTreeNode) iter.next();
      if(((ArrayList) chains.get(head)).contains(ann.getId())) {
        return head;
      }
    }
    return null;
  }

  /**
   * This methods highlights the annotations
   */
  public void highlightAnnotations() {

    if(highlightedTags == null) {
      highlightedTags = new HashMap();
      highlighter.removeAllHighlights();
      highlightedChainAnnots = new ArrayList();
    }

    CorefTreeNode parent = (CorefTreeNode) corefAnnotationSetNodesMap.get((String) annotSets.getSelectedItem());
    AnnotationSet annotSet = getAnnotationSet(parent.toString());
    HashMap chainMap = (HashMap) corefChains.get(parent);
    Iterator iter = chainMap.keySet().iterator();

    while(iter.hasNext()) {
      CorefTreeNode currentNode = (CorefTreeNode) iter.next();
      if(((Boolean)currentSelections.get(currentNode.toString())).booleanValue()) {
        if(!highlightedTags.containsKey(currentNode)) {
          // find out the arrayList
          ArrayList ids = (ArrayList) chainMap.get(currentNode);
          ArrayList highlighTag = new ArrayList();
          if (ids != null) {
            for (int i = 0; i < ids.size(); i++) {
              Annotation ann = annotSet.get( (Integer) ids.get(i));
              highlightedChainAnnots.add(ann);
              try {
                Color color = (Color) currentColors.get(currentNode.toString());
                highlighTag.add(highlighter.addHighlight(
                    ann.getStartNode().getOffset().intValue(),
                    ann.getEndNode().getOffset().intValue(),
                    new DefaultHighlighter.DefaultHighlightPainter(color)));
              }
              catch (javax.swing.text.BadLocationException e) {
                e.printStackTrace();
              }
            }
            highlightedTags.put(currentNode, highlighTag);
          }
        }
      } else {
        if(highlightedTags.containsKey(currentNode)) {
          ArrayList highlights = (ArrayList) highlightedTags.get(currentNode);
          for(int i=0;i<highlights.size();i++) {
            highlighter.removeHighlight(highlights.get(i));
          }
          highlightedTags.remove(currentNode);
          ArrayList ids = (ArrayList) chainMap.get(currentNode);
          if (ids != null) {
            for (int i = 0; i < ids.size(); i++) {
              Annotation ann = annotSet.get( (Integer) ids.get(i));
              highlightedChainAnnots.remove(ann);
            }
          }
        }
      }
    }

    // This is to make process faster.. instead of accessing each annotation and
    // its offset, we create an array with its annotation offsets to search faster
    Collections.sort(highlightedChainAnnots, new gate.util.OffsetComparator());
    highlightedChainAnnotsOffsets = new int[highlightedChainAnnots.size() * 2];
    for (int i = 0, j = 0; j < highlightedChainAnnots.size(); i += 2, j++) {
      Annotation ann1 = (Annotation) highlightedChainAnnots.get(j);
      highlightedChainAnnotsOffsets[i] = ann1.getStartNode().getOffset().intValue();
      highlightedChainAnnotsOffsets[i + 1] = ann1.getEndNode().getOffset().intValue();
    }
  }


  protected void registerHooks(){
    textPane.addMouseListener(textPaneMouseListener);
    textPane.addMouseMotionListener(textPaneMouseListener);

  }

  protected void unregisterHooks(){
    textPane.removeMouseListener(textPaneMouseListener);
    textPane.removeMouseMotionListener(textPaneMouseListener);
  }

  public Component getGUI(){
    return mainPanel;
  }

  public int getType(){
    return VERTICAL;
  }

  //**********************************************
  // MouseListener and MouseMotionListener Methods
  //***********************************************

  protected class TextPaneMouseListener extends MouseInputAdapter {

    public TextPaneMouseListener() {
      chainToolTipTimer.setRepeats(false);
      newCorefActionTimer.setRepeats(false);
    }

    public void mouseMoved(MouseEvent me) {
      int textLocation = textPane.viewToModel(me.getPoint());
      chainToolTipAction.setTextLocation(textLocation);
      chainToolTipAction.setMousePointer(me.getPoint());
      chainToolTipTimer.restart();

      newCorefAction.setTextLocation(textLocation);
      newCorefAction.setMousePointer(me.getPoint());
      newCorefActionTimer.restart();
   }
  }

  public CorefTreeNode findOutChainNode(String chainNodeString) {
    if(corefChains == null || corefAnnotationSetNodesMap == null) {
      return null;
    }
    HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
    if(chains == null) {
      return null;
    }
    Iterator iter = chains.keySet().iterator();
    while(iter.hasNext()) {
      CorefTreeNode currentNode = (CorefTreeNode) iter.next();
      if(currentNode.toString().equals(chainNodeString))
        return currentNode;
    }
    return null;
  }

/**
 * When user hovers over the annotations which have been highlighted by
 * show button
 */
 protected class NewCorefAction extends KeyAdapter implements ActionListener, ListSelectionListener {

   int textLocation;
   Point mousePoint;
   JPopupMenu popup = new JPopupMenu();
   String defaultLabelText = "New Coreference Window";
   JLabel label = new JLabel();
   JPanel panel = new JPanel();
   JPanel subPanel = new JPanel();
   JTextField field = new JTextField(20);
   JButton add = new JButton("Add");
   JButton newChain = new JButton("New Chain");
   JList list = new JList();

   public NewCorefAction() {
     popup.setBackground(UIManager.getLookAndFeelDefaults().
         getColor("ToolTip.background"));
     popup.setLayout(new BorderLayout());

     panel.setLayout(new BorderLayout());
     panel.add(subPanel, BorderLayout.NORTH);
     panel.add(new JScrollPane(list), BorderLayout.CENTER);
     subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
     subPanel.add(field);
     subPanel.add(add);
     panel.add(newChain, BorderLayout.SOUTH);
     popup.add(label, BorderLayout.NORTH);
     popup.add(panel,BorderLayout.CENTER);

     // and finally load the data for the list
     add.addActionListener(new AddAction());
     newChain.addActionListener(new AddAction());
     list.setVisibleRowCount(5);
     list.addListSelectionListener(this);
     field.addKeyListener(this);
   }

   public void valueChanged(ListSelectionEvent lse) {
     field.setText((String) list.getSelectedValue());
     field.updateUI();
   }

   public void actionPerformed(ActionEvent ae) {
     int index = -1;
     if (highlightedChainAnnotsOffsets != null) {
       for (int i = 0; i < highlightedChainAnnotsOffsets.length; i += 2) {
         if (textLocation >= highlightedChainAnnotsOffsets[i] &&
             textLocation <= highlightedChainAnnotsOffsets[i + 1]) {
           index = (i == 0) ? i : i / 2;
           break;
         }
       }
     }

     // yes it is put on highlighted so show the annotationType
     if (highlightedChainAnnotsOffsets != null && index < highlightedChainAnnotsOffsets.length && index >= 0) {
       return;
     }

     if (highlightedTypeAnnotsOffsets != null) {
       for (int i = 0; i < highlightedTypeAnnotsOffsets.length; i += 2) {
         if (textLocation >= highlightedTypeAnnotsOffsets[i] &&
             textLocation <= highlightedTypeAnnotsOffsets[i + 1]) {
           index = (i == 0) ? i : i / 2;
           break;
         }
       }
     }

     // yes it is put on highlighted so show the annotationType
     if (highlightedTypeAnnotsOffsets != null &&
         index < highlightedTypeAnnotsOffsets.length && index >= 0) {
       annotToConsiderForChain = (Annotation) highlightedTypeAnnots.get(index);
       // now check if this annotation is already linked with something
       CorefTreeNode headNode = findOutTheChainHead(annotToConsiderForChain);
       if(headNode != null) {
         JPopupMenu popup1 = new JPopupMenu();
         JLabel label1 = new JLabel("Annotation co-referenced with : \""+headNode.toString()+"\"");
         popup1.setBackground(UIManager.getLookAndFeelDefaults().
             getColor("ToolTip.background"));
        label1.setBackground(UIManager.getLookAndFeelDefaults().
             getColor("ToolTip.background"));
         popup1.setLayout(new FlowLayout());
         popup1.add(label1);
         popup1.setVisible(true);
         popup1.show(textPane, (int) mousePoint.getX(), (int) mousePoint.getY());
       } else {

         list.setListData(currentSelections.keySet().toArray());
         list.updateUI();
         popup.setVisible(false);
         label.setText(defaultLabelText + "for : \"" +
                       getString(annotToConsiderForChain) + "\"");
         popup.setVisible(true);
         popup.show(textPane, (int) mousePoint.getX(),
                    (int) mousePoint.getY());
       }
     }
   }

   public void keyReleased(KeyEvent ke) {
     if(ke.getKeyChar()== KeyEvent.VK_UP) {
       int index1 = list.getSelectedIndex();
       if(index1 < list.getModel().getSize() - 1) {
         list.setSelectedIndex(index1 + 1);
         field.setText((String) list.getSelectedValue());
         field.updateUI();
       }
       return;
     } else if(ke.getKeyChar() == KeyEvent.VK_DOWN) {
       int index1 = list.getSelectedIndex();
       if(index1 > 0) {
         list.setSelectedIndex(index1 - 1);
         field.setText((String) list.getSelectedValue());
         field.updateUI();
       }
       return;
     } else if(ke.getKeyChar() == KeyEvent.VK_ENTER) {
       actionPerformed(new ActionEvent(add,ActionEvent.ACTION_PERFORMED,"add"));
       return;
     }

     String startWith = field.getText();
     Vector myList = new Vector();
     Iterator iter = currentSelections.keySet().iterator();
     while(iter.hasNext()) {
       String currString = (String) iter.next();
       if(currString.startsWith(startWith)) {
         myList.add(currString);
       }
     }
     list.setListData(myList);
     list.updateUI();
   }

   public void setTextLocation(int textLocation) {
     this.textLocation = textLocation;
   }

   public void setMousePointer(Point point) {
     this.mousePoint = point;
   }

   private class AddAction extends AbstractAction {
     public void actionPerformed(ActionEvent ae) {
       if(ae.getSource() == add) {
         if(field.getText() == null || field.getText().length() == 0) {
           try {
             JOptionPane.showMessageDialog(Main.getMainFrame(),
                                           "No Chain Selected",
                                           "New Chain - Error",
                                           JOptionPane.ERROR_MESSAGE);
           } catch(Exception e) {
             e.printStackTrace();
           }
           return;
         } else {
           // we want to add this
           // now first find out the annotation
           Annotation ann = annotToConsiderForChain;
           if(ann == null) return;
           // yes it is available
           // find out the CorefTreeNode for the chain under which it is to be inserted
           CorefTreeNode chainNode = findOutChainNode(field.getText());
           HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
           if(chainNode == null) {
             try {
               JOptionPane.showMessageDialog(Main.getMainFrame(), "Incorrect Chain Title",
                                             "New Chain - Error",
                                             JOptionPane.ERROR_MESSAGE);
             } catch(Exception e) {
               e.printStackTrace();
             }
             return;
           }
           popup.setVisible(false);
           ArrayList ids = (ArrayList) chains.get(chainNode);
           AnnotationSet set = getAnnotationSet( (String) annotSets.
                                                getSelectedItem());
           // make the entry of current annotation in all its corefering annotations
           for (int i = 0; i < ids.size(); i++) {
             Annotation tempAnnot = (Annotation) set.get( (Integer) ids.get(i));
             ArrayList matches = (ArrayList) (tempAnnot.getFeatures().get(
                 ANNIEConstants.
                 ANNOTATION_COREF_FEATURE_NAME));
             if (matches == null)
               matches = new ArrayList();

             if (!matches.contains(ann.getId())) {
               matches.add(ann.getId());
               tempAnnot.getFeatures().put(ANNIEConstants.
                                           ANNOTATION_COREF_FEATURE_NAME,
                                           matches);
             }

           }
           // inserting all ids to the currentAnnotations
           FeatureMap features = ann.getFeatures();
           if (features.containsKey(ANNIEConstants.
                                    ANNOTATION_COREF_FEATURE_NAME)) {
             ArrayList matches = (ArrayList) features.get(ANNIEConstants.
                 ANNOTATION_COREF_FEATURE_NAME);
             for (int j = 0; j < ids.size(); j++) {
               if (!matches.contains(ids.get(j))) {
                 matches.add(ids.get(j));
               }
             }
           }
           else {
             features.put(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME, ids);
           }

           ids.add(ann.getId());
           chains.put(chainNode, ids);
           corefChains.put(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()), chains);

           // now see if the string of new annotation is longer than the current chainNode
           if (chainNode.toString().length() >= getString(ann).length()) {
             currentSelections.put(chainNode.toString(), new Boolean(true));
             // and rehighlight the chains
             highlightedTags = null;
             highlightAnnotations();
             typeSpecificHighlightedTags = null;
             showTypeWiseAnnotations();
             return;
           }
           else {
             // the new added annotation has a longer value
             // 1. Now we need to change the value of currentNode
             String longestString = getString(ann);
             String previousString = chainNode.toString();
             chainNode.setUserObject(longestString);

             // 2. we need to make changes in the following instances
             // colorChainsMap
             // currentColors
             // selectionChainsMap
             // currentSelections
             HashMap temp = (HashMap) colorChainsMap.get( (String) annotSets.
                 getSelectedItem());
             Color color = (Color) temp.get(previousString);
             temp.remove(previousString);
             temp.put(longestString, color);
             currentColors = temp;
             colorChainsMap.put( (String) annotSets.getSelectedItem(), temp);

             temp = (HashMap) selectionChainsMap.get( (String) annotSets.
                 getSelectedItem());
             temp.remove(previousString);
             temp.put(longestString, new Boolean(true));
             currentSelections = temp;
             selectionChainsMap.put( (String) annotSets.getSelectedItem(),
                                    temp);

             // and finally make arrangements for highlighting
             highlightedTags = null;
             highlightAnnotations();
             typeSpecificHighlightedTags = null;
             showTypeWiseAnnotations();

             // redraw the tree
             corefTree.repaint();
             corefTree.updateUI();
             return;
           }
         }
       } else if(ae.getSource() == newChain) {

         // we want to add this
         // now first find out the annotation
         Annotation ann = annotToConsiderForChain;
         if(ann == null) return;
         HashMap chains = (HashMap) corefChains.get(corefAnnotationSetNodesMap.get(annotSets.getSelectedItem()));
         CorefTreeNode chainNode = findOutChainNode(getString(ann));
         if(chainNode != null) {
           try {
             JOptionPane.showMessageDialog(Main.getMainFrame(),
                                           "Chain with " + getString(ann) +
                                           " title already exists",
                                           "New Chain - Error",
                                           JOptionPane.ERROR_MESSAGE);
           }catch(Exception e) {
             e.printStackTrace();
           }
           return;
         }

         popup.setVisible(false);

         // this is the new chain
         // get the current annotSetNode
         CorefTreeNode annotSetNode = (CorefTreeNode)
                                      corefAnnotationSetNodesMap.get(annotSets.
             getSelectedItem());
         // create the new chainNode
         chainNode = new CorefTreeNode(getString(ann), false,
                                       CorefTreeNode.CHAIN_NODE);
         // add this to tree
         annotSetNode.add(chainNode);
         // ArrayList matches
         HashMap newHashMap = (HashMap) corefChains.get(annotSetNode);
         ArrayList newChainList = new ArrayList();
         newChainList.add(ann.getId());
         newHashMap.put(chainNode, newChainList);
         corefChains.put(annotSetNode, newHashMap);
         // entry into the selection
         currentSelections.put(chainNode.toString(), new Boolean(true));
         selectionChainsMap.put(annotSets.getSelectedItem(), currentSelections);
         // entry into the colors
         float components[] = colorGenerator.getNextColor().getComponents(null);
         Color color = new Color(components[0],
                                 components[1],
                                 components[2],
                                 0.5f);
         currentColors.put(chainNode.toString(), color);
         colorChainsMap.put(annotSets.getSelectedItem(), currentColors);
         // and finally make arrangements for highlighting
         highlightedTags = null;
         highlightAnnotations();
         typeSpecificHighlightedTags = null;
         showTypeWiseAnnotations();

         // redraw the tree
         corefTree.repaint();
         corefTree.updateUI();
         return;
       }
     }
   }
 }

  /** When user hovers over the chainnodes */
  protected class ChainToolTipAction extends AbstractAction {

    int textLocation;
    Point mousePoint;
    JPopupMenu popup = new JPopupMenu();

    public ChainToolTipAction() {
      popup.setBackground(UIManager.getLookAndFeelDefaults().
          getColor("ToolTip.background"));
    }



    public void actionPerformed(ActionEvent ae) {

      int index = -1;
      if (highlightedChainAnnotsOffsets != null) {
        for (int i = 0; i < highlightedChainAnnotsOffsets.length; i += 2) {
          if (textLocation >= highlightedChainAnnotsOffsets[i] &&
              textLocation <= highlightedChainAnnotsOffsets[i + 1]) {
            index = (i == 0) ? i : i / 2;
            break;
          }
        }
      }

      // yes it is put on highlighted so show the annotationType
      if (highlightedChainAnnotsOffsets != null && index < highlightedChainAnnotsOffsets.length && index >= 0) {
        popup.setVisible(false);
        popup.removeAll();
        final int tempIndex = index;
        CorefTreeNode chainHead = findOutTheChainHead((Annotation) highlightedChainAnnots.get(index));
        final HashMap tempMap = new HashMap();
        popup.setLayout(new FlowLayout(FlowLayout.LEFT));
        if(chainHead != null) {
          JPanel tempPanel = new JPanel();
          tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
          tempPanel.add(new JLabel(chainHead.toString()));
          final JButton deleteButton = new JButton("Delete");
          tempPanel.add(deleteButton);
          popup.add(tempPanel);
          deleteButton.setActionCommand(chainHead.toString());
          tempMap.put(chainHead.toString(), chainHead);
          deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              try {
                int confirm = JOptionPane.showConfirmDialog(Main.getMainFrame(),
                    "Remove Chain Reference : Are you sure?");
                if (confirm == JOptionPane.YES_OPTION) {
                  popup.setVisible(false);
                  // remove it
                  removeChainReference( (Annotation) highlightedChainAnnots.get(
                      tempIndex), (CorefTreeNode) tempMap.get(deleteButton.getActionCommand()));
                  // again highlightCoreferences
                  highlightedTags = null;
                  highlightAnnotations();
                  if (showAnnotations.isSelected()) {
                    typeSpecificHighlightedTags = null;
                    showTypeWiseAnnotations();
                  }
                }
              }
              catch (Exception e1) {
                e1.printStackTrace();
              }
            }
          });
        }
        //label.setText("Remove \""+getString((Annotation) highlightedChainAnnots.get(index)) + "\" from \""+ findOutTheChainHead((Annotation) highlightedChainAnnots.get(index)).toString()+"\"");
        popup.revalidate();
        popup.setVisible(true);
        popup.show(textPane,(int) mousePoint.getX() ,(int) mousePoint.getY() + 10);
      }
    }

    public void setTextLocation(int textLocation) {
      this.textLocation = textLocation;
    }

    public void setMousePointer(Point point) {
      this.mousePoint = point;
    }

  }



  // Class that represents each individual tree node in the corefTree
  protected class CorefTreeNode extends DefaultMutableTreeNode {
    public final static int ROOT_NODE = 0;
    public final static int ANNOTSET_NODE = 1;
    public final static int CHAIN_NODE = 2;

    private int type;

    public CorefTreeNode(Object value, boolean allowsChildren, int type) {
      super(value, allowsChildren);
      this.type = type;
    }

    public int getType() {
      return this.type;
    }

  }



  /**
   * Action for mouseClick on the Tree
   */
  protected class CorefTreeMouseListener extends MouseAdapter {

    public void mouseClicked(MouseEvent me) {
      // ok now find out the currently selected node
      int x = me.getX();
      int y = me.getY();
      int row = corefTree.getRowForLocation(x, y);
      TreePath path = corefTree.getPathForRow(row);

      // let us expand it if the sibling feature is on
      if (path != null) {
        CorefTreeNode node = (CorefTreeNode) path.
                                  getLastPathComponent();

        // if it only chainNode
        if (node.getType() != CorefTreeNode.CHAIN_NODE)
          return;

        boolean isSelected = ! ( (Boolean) currentSelections.get(node.toString())).
                             booleanValue();
        currentSelections.put(node.toString(), new Boolean(isSelected));

        // so now we need to highlight all the stuff
        highlightAnnotations();
        corefTree.repaint();
        corefTree.updateUI();
      }
    }
  }

  /**
   * This method uses the java.util.prefs.Preferences and get the color
   * for particular annotationType.. This color could have been saved
   * by the AnnotationSetsView
   * @param annotationType
   * @return
   */
  private Color getColor(String annotationType){
    java.util.prefs.Preferences prefRoot = null;
    try {
      prefRoot = java.util.prefs.Preferences.userNodeForPackage(Class.forName(
          "gate.gui.docview.AnnotationSetsView"));
    }catch(Exception e) {
      e.printStackTrace();
    }
    int rgba = prefRoot.getInt(annotationType, -1);
    Color colour;
    if(rgba == -1){
      //initialise and save
      float components[] = colorGenerator.getNextColor().getComponents(null);
      colour = new Color(components[0],
                         components[1],
                         components[2],
                         0.5f);
      int rgb = colour.getRGB();
      int alpha = colour.getAlpha();
      rgba = rgb | (alpha << 24);
      prefRoot.putInt(annotationType, rgba);

    }else{
      colour = new Color(rgba, true);
    }
    return colour;
  }

  /**
   * Cell renderer to add the checkbox in the tree
   */
  protected class CorefTreeCellRenderer extends JPanel implements TreeCellRenderer {

    private JCheckBox check;
    private JLabel label;

    /**
     * Constructor
     * @param owner
     */
    public CorefTreeCellRenderer() {
      check = new JCheckBox();
      check.setBackground(Color.white);
      label = new JLabel();
      setLayout(new FlowLayout());
      add(check);
      add(label);
    }


    /**
     * Renderer class
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean isSelected,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {


      CorefTreeNode userObject = (CorefTreeNode) value;
      label.setText(userObject.toString());
      label.setFont(tree.getFont());
      if (userObject.getType() == CorefTreeNode.ROOT_NODE || userObject.getType() == CorefTreeNode.ANNOTSET_NODE) {
        this.setBackground(Color.white);
        this.check.setVisible(false);
        return this;
      } else {
        this.setBackground((Color) currentColors.get(userObject.toString()));
        this.setMaximumSize(new Dimension(this.getWidth(),check.getHeight()));
        check.setVisible(true);
        check.setBackground(Color.white);
      }

      // if node should be selected
      boolean selected = ( (Boolean) currentSelections.get(userObject.toString())).booleanValue();
      check.setSelected(selected);

      return this;
    }


    /**
     * This method tells that what should be the preferred size for the node in the tree
     * @return
     */
    public Dimension getPreferredSize() {
      Dimension d_check = check.getPreferredSize();
      Dimension d_label = label.getPreferredSize();
      return new Dimension(d_check.width + d_label.width,
                           (d_check.height < d_label.height ?
                            d_label.height : d_check.height));
    }

    public void doLayout() {
      Dimension d_check = check.getPreferredSize();
      Dimension d_label = label.getPreferredSize();
      int y_check = 0;
      int y_label = 0;
      if (d_check.height < d_label.height) {
        y_check = (d_label.height - d_check.height) / 2;
      }
      else {
        y_label = (d_check.height - d_label.height) / 2;
      }
      check.setLocation(0, y_check);
      check.setBounds(0, y_check, d_check.width, d_check.height);
      label.setLocation(d_check.width, y_label);
      label.setBounds(d_check.width, y_label, d_label.width, d_label.height);
    }

  }
}