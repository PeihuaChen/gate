/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 13/11/2000
 *
 *  $Id$
 *
 */
package gate.gui;

import gate.*;
import gate.util.*;
import gate.corpora.TestDocument;
import gate.creole.tokeniser.DefaultTokeniser;
import gate.creole.*;
import gate.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.beans.*;
import java.util.*;
import java.net.*;
import java.io.*;


public class AnnotationEditor extends AbstractVisualResource{
  //properties
  private transient PropertyChangeSupport propertyChangeListeners =
                                          new PropertyChangeSupport(this);
  private gate.Document document;
  private java.util.Set annotationSchemas;

  protected ColorGenerator colGenerator = new ColorGenerator();

  //GUI components
  JTextPane textPane;
  JScrollPane textScroll;
  XJTable annotationsTable;
  AnnotationsTableModel annotationsTableModel;
  JScrollPane tableScroll;
  JSplitPane leftSplit;

  JTree stylesTree;
  JScrollPane stylesTreeScroll;
  TreeNode stylesTreeRoot;
  DefaultTreeModel stylesTreeModel;

  TextAttributesChooser styleChooser;
  AnnotationEditDialog annotationEditDialog;

  Box progressBox;
  JProgressBar progressBar;

  Highlighter highlighter;

//data members
  /**
   * holds the data for the table: a list of Annotation objects
   */
  java.util.List data;

  /**
   * a list containing {@link Range} objects.
   */
  java.util.List ranges;

  /**
   * Maps from String to Map to Map.
   * Annotation set name -> Annotation type -> {@link TypeData}
   */
  Map typeDataMap;

  DelayedListener eventHandler;

  Thread guiUpdateThread;
  //misc members

  private boolean tableVisible;
  private boolean textVisible;
  private boolean filtersVisible;
  private boolean editable;

  public AnnotationEditor() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  public static void main(String[] args) {
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      Gate.setLocalWebServer(false);
      Gate.setNetConnected(false);
      Gate.init();
      JFrame frame = new JFrame("Gate Document Editor Test");
      frame.addWindowListener(new WindowAdapter(){
        public void windowClosing(WindowEvent e){
          System.exit(0);
        }
      });

      //get a document
      FeatureMap params = Factory.newFeatureMap();
      params.put("markupAware", new Boolean(true));

      params.put("sourceUrlName",
                 "http://redmires.dcs.shef.ac.uk/admin/index.html");
                 //"http://redmires.dcs.shef.ac.uk/java1.3docs/api/javax/swing/Action.html");
                 //"http://redmires.dcs.shef.ac.uk/java1.3docs/api/java/awt/AWTEventMulticaster.html");
      gate.Document doc = (gate.Document)Factory.createResource("gate.corpora.DocumentImpl", params);
      //create a default tokeniser
     params.clear();
     params.put("rulesResourceName", "creole/tokeniser/DefaultTokeniser.rules");
     DefaultTokeniser tokeniser = (DefaultTokeniser) Factory.createResource(
                            "gate.creole.tokeniser.DefaultTokeniser", params);

      AnnotationSet tokeniserAS = doc.getAnnotations("TokeniserAS");
      tokeniser.setDocument(doc);
      tokeniser.setAnnotationSet(tokeniserAS);
      tokeniser.run();
      //check for exceptions
      tokeniser.check();

      AnnotationEditor editor = new AnnotationEditor();
      frame.getContentPane().add(editor);
      frame.pack();
      frame.setVisible(true);
      editor.setDocument(doc);

      //get the annotation schemas
      params =  Factory.newFeatureMap();
      params.put("xmlFileUrl", new java.net.URL("file:///Z:/gate2/src/gate/resources/creole/schema/PosSchema.xml"));

      AnnotationSchema annotSchema = (AnnotationSchema)
         Factory.createResource("gate.creole.AnnotationSchema", params);
      Set annotationSchemas = new HashSet();
      annotationSchemas.add(annotSchema);
      editor.setAnnotationSchemas(annotationSchemas);

    }catch(Exception e){
      e.printStackTrace(System.err);
    }
  }
/*
  public Resource init(){
    //data initialisation
    if(document != null){
      //set the text
      textPane.setText(document.getContent().toString());

      //the AnnotationsTable and the AnnotationSetsTable
      setsData.clear();
      setsDataMap.clear();
      String name = "<Default>";
      SetData sData = new SetData(name, false);
      setsData.add(sData);
      setsDataMap.put(name, sData);
      Iterator setsIter = document.getNamedAnnotationSets().keySet().iterator();
      while(setsIter.hasNext()){
        name = (String)setsIter.next();
        sData = new SetData(name, false);
        setsData.add(sData);
        setsDataMap.put(name, sData);
      }
      setsTableModel.fireTableDataChanged();


      if(visibleAnnotationSetsNames == null){
        visibleAnnotationSetsNames = new HashSet();
      }
      Iterator vasIter = visibleAnnotationSetsNames.iterator();
      String asName;
      while(vasIter.hasNext()){
        asName = (String)vasIter.next();
        makeSetVisible(asName.equals("<Default>") ?
                       document.getAnnotations() :
                       document.getAnnotations(asName));
      }
    }//if(document != null)

    return this;
  }//public Resource init()
*/
  protected void initListeners(){
    //listen for our own properties change events
    this.addPropertyChangeListener("document", new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
        this_documentChanged();
      }
    });
    this.addPropertyChangeListener("visibleAnnotationSets",
                                   new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    this.addPropertyChangeListener("annotationSchemas",
                                   new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    //listen for component events
    this.addComponentListener(new ComponentAdapter(){
      public void componentResized(ComponentEvent e) {
      }

      public void componentShown(ComponentEvent e){
        //expand all the nodes in the tree
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                  ((DefaultMutableTreeNode)stylesTreeRoot).getFirstChild();
        while(node != null){
          stylesTree.expandPath(new TreePath(node.getPath()));
          node = node.getNextSibling();
        }
        stylesTreeModel.reload();
        //set the slider location
        leftSplit.setDividerLocation(leftSplit.getHeight() / 2);
      }
    });

    stylesTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)){
          //where inside the tree?
          int x = e.getX();
          int y = e.getY();
          TreePath path = stylesTree.getPathForLocation(x, y);
          if(path != null){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.
                                         getLastPathComponent();
            TypeData nData = (TypeData)(node).getUserObject();
            //where inside the cell?
            Rectangle cellRect = stylesTree.getPathBounds(path);
            x -= cellRect.x;
            y -= cellRect.y;
            Component cellComp = stylesTree.getCellRenderer().
                                 getTreeCellRendererComponent(stylesTree,
                                                              node, false,
                                                              false, false,
                                                              0, false);
            cellComp.setSize(cellComp.getPreferredSize());
            Component clickedComp = cellComp.getComponentAt(x, y);

            if(clickedComp instanceof JCheckBox){
              nData.setVisible(! nData.getVisible());
              stylesTreeModel.nodeChanged(node);
            }else if(clickedComp instanceof JLabel && e.getClickCount() == 2){
              styleChooser.setLocationRelativeTo(stylesTree);
              nData.setAttributes(styleChooser.show(nData.getAttributes().copyAttributes()));
              stylesTreeModel.nodeChanged(node);
            }
          }
        }
      }
    });

    stylesTree.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        updateTreeSize();
      }
      public void componentShown(ComponentEvent e) {
        updateTreeSize();
      }
    });

    annotationsTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int row = annotationsTable.rowAtPoint(e.getPoint());
        //for any type of click -> highlight the annotation
        int start = ((Long)annotationsTable.getModel().getValueAt(row, 2)).intValue();
        int end = ((Long)annotationsTable.getModel().getValueAt(row, 3)).intValue();
        try{
          highlighter.removeAllHighlights();
          highlighter.addHighlight(start, end, DefaultHighlighter.DefaultPainter);
          textPane.scrollRectToVisible(textPane.modelToView(start));
        }catch(BadLocationException ble){
          throw new GateRuntimeException(ble.toString());
        }
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2){
          //double left click -> edit the annotation
          Annotation ann = (Annotation)annotationsTable.getModel().
                                                        getValueAt(row, -1);
          //find an appropiate schema
          if(annotationSchemas != null && !annotationSchemas.isEmpty()){
            Iterator schemasIter = annotationSchemas.iterator();
            boolean done = false;
            while(!done && schemasIter.hasNext()){
              AnnotationSchema schema = (AnnotationSchema)schemasIter.next();
              if(schema.getAnnotationName().equalsIgnoreCase(ann.getType())){
                FeatureMap features = ann.getFeatures();
                annotationEditDialog.setLocationRelativeTo(annotationsTable);
                FeatureMap newFeatures = annotationEditDialog.show(features,
                                                                   schema);
                if(newFeatures != null){
                  features.clear();
                  features.putAll(newFeatures);
                  ((AbstractTableModel)annotationsTable.getModel()).
                                        fireTableRowsUpdated(row, row);
                }
                done = true;
              }
            }
          }
        }else if(SwingUtilities.isRightMouseButton(e)){
          //right click
        }
      }
    });

    textPane.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)){
          int position = textPane.viewToModel(e.getPoint());
          if(textPane.getSelectionStart() ==  textPane.getSelectionEnd()){
            //no selection -> select an annotation
            JPopupMenu popup = new JPopupMenu("Select:");
            //find annotations at this position
            Iterator annIter = document.getAnnotations().
                                        get(new Long(position),
                                            new Long(position)
                                        ).iterator();
            if(annIter.hasNext()){
              JMenu menu = new JMenu("<Default>");
              popup.add(menu);
              while(annIter.hasNext()){
                Annotation ann = (Annotation)annIter.next();
                JMenuItem item = new SelectAnnotationPopupItem(ann, "<Default>");
                menu.add(item);
              }
            }
            Map namedASs = document.getNamedAnnotationSets();
            if(namedASs != null){
              Iterator namedASiter = namedASs.values().iterator();
              while(namedASiter.hasNext()){
                //find annotations at this position
                AnnotationSet set = (AnnotationSet)namedASiter.next();
                annIter = set.get(new Long(position), new Long(position)).
                              iterator();
                if(annIter.hasNext()){
                  JMenu menu = new JMenu(set.getName());
                  popup.add(menu);
                  while(annIter.hasNext()){
                    Annotation ann = (Annotation)annIter.next();
                    JMenuItem item = new SelectAnnotationPopupItem(ann, set.getName());
                    menu.add(item);
                  }
                }
              }
            }
            popup.show(textPane, e.getPoint().x, e.getPoint().y);
          }else{
            //there is selected text -> create a new annotation
            int start = textPane.getSelectionStart();
            int end = textPane.getSelectionEnd();
            if(annotationSchemas != null &&
               !annotationSchemas.isEmpty()){
              JPopupMenu popup = new JPopupMenu();
              //Add to the default AnnotationSet
              JMenu menu = new JMenu("Add to <Default>");

              Iterator schemasIter = annotationSchemas.iterator();
              while(schemasIter.hasNext()){
                AnnotationSchema schema = (AnnotationSchema)schemasIter.next();
                menu.add(new NewAnnotationPopupItem(start, end, schema,
                         document.getAnnotations()));
              }
              popup.add(menu);

              //Add to a named AnnotationSet
              Iterator asIter = document.getNamedAnnotationSets().values().iterator();
              while(asIter.hasNext()){
                AnnotationSet as = (AnnotationSet)asIter.next();
                menu = new JMenu("Add to " + as.getName());
                schemasIter = annotationSchemas.iterator();
                while(schemasIter.hasNext()){
                  AnnotationSchema schema = (AnnotationSchema)schemasIter.next();
                  menu.add(new NewAnnotationPopupItem(start, end, schema, as));
                }
                popup.add(menu);
              }

              //Add to a new AnnotationSet
              menu = new JMenu("Add to new annotation set");
              schemasIter = annotationSchemas.iterator();
              while(schemasIter.hasNext()){
                AnnotationSchema schema = (AnnotationSchema)schemasIter.next();
                menu.add(new NewAnnotationPopupItem(start, end, schema, null));
              }
              popup.add(menu);

              //show the popup
              popup.show(textPane, e.getPoint().x, e.getPoint().y);
            }
          }
        }
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }
    });

  }//protected void initListeners()

  protected void initLocalData(){
    //init local vars
    data = Collections.synchronizedList(new ArrayList());
    //dataAsAS = new gate.annotation.AnnotationSetImpl(document);
    ranges = new ArrayList();

    typeDataMap = new HashMap();

    eventHandler = new DelayedListener();
    new Thread(eventHandler).start();

  }//protected void initLocalData()

  protected void initGuiComponents(){
    //initialise GUI components
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    //LEFT SPLIT
    textPane = new JTextPane();
//    textPane.setEditable(false);
    textPane.setEnabled(true);
    textPane.setEditorKit(new CustomStyledEditorKit());
    Style defaultStyle = textPane.getStyle("default");
    StyleConstants.setBackground(defaultStyle, Color.white);
    StyleConstants.setFontFamily(defaultStyle, "Arial Unicode MS");
    textScroll = new JScrollPane(textPane);

    annotationsTableModel = new AnnotationsTableModel();
    annotationsTable = new XJTable(annotationsTableModel);
    annotationsTable.setIntercellSpacing(new Dimension(10, 5));
    //annotationsTable.setRowMargin(10);
    tableScroll = new JScrollPane(annotationsTable);
    tableScroll.setOpaque(true);

    leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               textScroll, tableScroll);
    leftSplit.setOneTouchExpandable(true);
    leftSplit.setOpaque(true);
    leftSplit.setAlignmentY(Component.TOP_ALIGNMENT);
    this.add(leftSplit);

    //RIGHT SIDE - the big tree
    stylesTreeRoot = new DefaultMutableTreeNode(null, true);
    stylesTreeModel = new DefaultTreeModel(stylesTreeRoot, true);
    stylesTree = new JTree(stylesTreeModel);
    stylesTree.setRootVisible(false);
    stylesTree.setCellRenderer(new NodeRenderer());
    //TIP: setting rowHeight to 0 tells the tree to query its renderer for each
    //row's size
    stylesTree.setRowHeight(0);
    stylesTree.setShowsRootHandles(true);
    stylesTree.setToggleClickCount(0);
    stylesTreeScroll = new JScrollPane(stylesTree);
    stylesTreeScroll.setAlignmentY(Component.TOP_ALIGNMENT);
    stylesTreeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    this.add(stylesTreeScroll);

    /*
    //RIGHT SIDE - FILTERS BOX
    filtersPane = new JPanel();
    filtersPane.setLayout(new BoxLayout(filtersPane, BoxLayout.Y_AXIS));

    //Annotation sets table
    setsTable = new XJTable(setsTableModel);
    setsTable.setSortedColumn(1);
    setsTable.setTableHeader(null);
    setsTable.setShowGrid(false);
    setsTable.setDefaultRenderer(Boolean.class,
                                 new PlainRenderer(
                                    setsTable.getDefaultRenderer(Boolean.class)
                                 )
                                 );
    setsTable.setDefaultRenderer(String.class,
                                 new PlainRenderer(
                                    setsTable.getDefaultRenderer(String.class)
                                 )
                                 );

    setsScroll = new JScrollPane(setsTable);
    setsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    Border border = setsScroll.getBorder();
    if(border == null) border = BorderFactory.createTitledBorder("Sets");
    else border = BorderFactory.createTitledBorder(border, "Sets");
    setsScroll.setBorder(border);
    filtersPane.add(setsScroll);

    //Annotation types table
    typesTable = new XJTable(typesTableModel);
    typesTable.setSortedColumn(1);
    typesTable.setTableHeader(null);
    typesTable.setShowGrid(false);
    typesTable.setDefaultRenderer(TypeData.class, new TypeDataRenderer());
    typesTable.setDefaultRenderer(Boolean.class,
                                            new PlainRenderer(typesTable.getDefaultRenderer(Boolean.class)));
    typesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    typesScroll = new JScrollPane(typesTable);
    typesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    typesScroll.setBorder(BorderFactory.createTitledBorder("Types"));
    filtersPane.add(typesScroll);
    filtersPane.setAlignmentY(Component.TOP_ALIGNMENT);
    this.add(filtersPane);
    */

    //Extra Stuff
    styleChooser = new TextAttributesChooser();
    styleChooser.setModal(true);
    annotationEditDialog = new AnnotationEditDialog();

    progressBox = new Box(BoxLayout.X_AXIS);
    progressBox.add(Box.createHorizontalStrut(5));
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
    progressBox.add(progressBar);
    progressBox.add(Box.createHorizontalStrut(5));

    highlighter = textPane.getHighlighter();
  }//protected void initGuiComponents()

  protected void updateTreeSize(){
    int width = stylesTree.getPreferredScrollableViewportSize().width +
                stylesTreeScroll.getInsets().left +
                stylesTreeScroll.getInsets().right;

    JComponent comp = stylesTreeScroll.getVerticalScrollBar();
    if(comp.isVisible()) width += comp.getPreferredSize().width;

    int height = stylesTree.getPreferredScrollableViewportSize().height +
                 stylesTreeScroll.getInsets().top +
                 stylesTreeScroll.getInsets().bottom;

    Dimension dim = new Dimension(width, height);
    stylesTreeScroll.setMinimumSize(dim);
    stylesTreeScroll.setPreferredSize(dim);
    stylesTreeScroll.invalidate();
    validate();
  }

  boolean addRange(String setName, String type){
    return false;
  }

  boolean removeRange(String setName, String type){
    return false;
  }




  public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
    super.removePropertyChangeListener(l);
    propertyChangeListeners.removePropertyChangeListener(l);
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
    super.addPropertyChangeListener(l);
    propertyChangeListeners.addPropertyChangeListener(l);
  }

  public synchronized void addPropertyChangeListener(String propertyName,
                                                     PropertyChangeListener l) {
    super.addPropertyChangeListener(propertyName, l);
    propertyChangeListeners.addPropertyChangeListener(propertyName, l);
  }

  public void setDocument(gate.Document newDocument) {
    gate.Document  oldDocument = document;
    document = newDocument;
    propertyChangeListeners.firePropertyChange("document", oldDocument,
                                               newDocument);
  }

  public gate.Document getDocument() {
    return document;
  }

  public void setAnnotationSchemas(java.util.Set newAnnotationSchemas) {
    java.util.Set  oldAnnotationSchemas = annotationSchemas;
    annotationSchemas = newAnnotationSchemas;
    propertyChangeListeners.firePropertyChange("annotationSchemas",
                                               oldAnnotationSchemas,
                                               newAnnotationSchemas);
  }

  public java.util.Set getAnnotationSchemas() {
    return annotationSchemas;
  }
  public void setTableVisible(boolean newTableVisible) {
    boolean  oldTableVisible = tableVisible;
    tableVisible = newTableVisible;
    propertyChangeListeners.firePropertyChange("tableVisible", new Boolean(oldTableVisible), new Boolean(newTableVisible));
  }
  public boolean isTableVisible() {
    return tableVisible;
  }
  public void setTextVisible(boolean newTextVisible) {
    boolean  oldTextVisible = textVisible;
    textVisible = newTextVisible;
    propertyChangeListeners.firePropertyChange("textVisible", new Boolean(oldTextVisible), new Boolean(newTextVisible));
  }
  public boolean isTextVisible() {
    return textVisible;
  }
  public void setFiltersVisible(boolean newFiltersVisible) {
    boolean  oldFiltersVisible = filtersVisible;
    filtersVisible = newFiltersVisible;
    propertyChangeListeners.firePropertyChange("filtersVisible", new Boolean(oldFiltersVisible), new Boolean(newFiltersVisible));
  }
  public boolean isFiltersVisible() {
    return filtersVisible;
  }
  //event handlers
  public void annotationSetAdded(gate.event.DocumentEvent e){
  }

  public void annotationSetRemoved(gate.event.DocumentEvent e){
  }

  public void annotationAdded(AnnotationSetEvent e){
System.out.println("Annotation added!");

  }

  public void annotationRemoved(AnnotationSetEvent e){
  }

  protected void this_documentChanged(){
    initLocalData();
    ((DefaultMutableTreeNode)stylesTreeRoot).removeAllChildren();
    //speed things up by hiding the text display
    textScroll.getViewport().setView(new JLabel("Updating! Please wait..."));
    //register the for this new document's events
    document.addGateListener(eventHandler);

    textPane.setText(document.getContent().toString());
    //add the default annotation set
    AnnotationSet currentAS = document.getAnnotations();
    if(currentAS != null){
      addAnnotationSet(currentAS);
    }
    //add all the other annotation sets
    Map namedASs = document.getNamedAnnotationSets();
    if(namedASs != null){
      Iterator setsIter = namedASs.values().iterator();
      while(setsIter.hasNext()){
        currentAS = (AnnotationSet)setsIter.next();
        if(currentAS != null){
          addAnnotationSet(currentAS);
        }
      }
    }
    //restore the text display
    textPane.select(0, 0);
    textScroll.getViewport().setView(textPane);
    stylesTreeModel.nodeStructureChanged(stylesTreeRoot);
  }

  /**
   * Used to register with the GUI a new annotation set on the current document.
   */
  protected void addAnnotationSet(AnnotationSet as){
    as.addGateListener(eventHandler);
    String setName = as.getName();
    if(setName == null) setName = "<Default>";
    TypeData setData = new TypeData(setName, null, false);
    setData.setAnnotations(as);
    DefaultMutableTreeNode setNode = new DefaultMutableTreeNode(setData, true);
    ((DefaultMutableTreeNode)stylesTreeRoot).add(setNode);
    ArrayList typesLst = new ArrayList(as.getAllTypes());
    Collections.sort(typesLst);
    Iterator typesIter = typesLst.iterator();
    while(typesIter.hasNext()){
      String type = (String)typesIter.next();
      TypeData typeData = new TypeData(setName, type, false);
      AnnotationSet sameType = as.get(type);
      typeData.setAnnotations(sameType);
      DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(typeData,
                                                                   false);
      setNode.add(typeNode);
    }
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        stylesTreeModel.reload();
      }
    });
  }


  public TypeData getTypeData(String setName, String type){
    Map setMap = (Map)typeDataMap.get(setName);
    if(setMap != null) return (TypeData)setMap.get(type);
    else return null;
  }

  protected void showHighlights(AnnotationSet annotations, AttributeSet style){
    //store the state of the text display
    int selStart = textPane.getSelectionStart();
    int selEnd = textPane.getSelectionEnd();
    final int position = textPane.viewToModel(
                            textScroll.getViewport().getViewPosition());
    //hide the text
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        progressBar.setValue(0);
        //progressBar.setMaximumSize(new Dimension(textScroll.getWidth(),20));
        textScroll.getViewport().setView(progressBox);
        textScroll.paintImmediately(textScroll.getBounds());
      }
    });

    //highlight the annotations
    int size = annotations.size();
    int i = 0;
    int lastValue = 0;
    int value;
    Iterator annIter = annotations.iterator();
    while(annIter.hasNext()){
      Annotation ann = (Annotation)annIter.next();
      textPane.select(ann.getStartNode().getOffset().intValue(),
                      ann.getEndNode().getOffset().intValue());
      textPane.setCharacterAttributes(style, true);
      value = i * 100 / size;
      if(value - lastValue >= 5){
        progressBar.setValue(value);
        progressBar.paintImmediately(progressBar.getBounds());
        lastValue = value;
      }
      i++;
    }
    //restore the state
    textPane.select(selStart, selEnd);
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        //show the text
        textScroll.getViewport().setView(textPane);
        try{
          textScroll.getViewport().setViewPosition(
                                   textPane.modelToView(position).getLocation());
          textScroll.paintImmediately(textScroll.getBounds());
        }catch(BadLocationException ble){
        }
      }
    });
  }//protected void showHighlights()

  protected void restoreText(){
    Iterator rangesIter = ranges.iterator();
    int size = 0;
    Range aRange;
    while(rangesIter.hasNext()){
      aRange = (Range)rangesIter.next();
      size += getTypeData(aRange.setName, aRange.type).getAnnotations().size();
    }

    //store the state of the text display
    int selStart = textPane.getSelectionStart();
    int selEnd = textPane.getSelectionEnd();
    final int position = textPane.viewToModel(
                            textScroll.getViewport().getViewPosition());
    //hide the text
    try{
      SwingUtilities.invokeAndWait(new Runnable(){
        public void run(){
          progressBar.setValue(0);
          //progressBar.setMaximumSize(new Dimension(textScroll.getWidth(),20));
          textScroll.getViewport().setView(progressBox);
          textPane.setText(document.getContent().toString());
        }
      });
    }catch(java.lang.reflect.InvocationTargetException ite){
      throw(new gate.util.GateRuntimeException(ite.toString()));
    }catch(java.lang.InterruptedException ie){
      throw(new gate.util.GateRuntimeException(ie.toString()));
    }

    //highlight the annotations
    int i = 0;
    int lastValue = 0;
    int value;
    rangesIter = ranges.iterator();
    TypeData tData;
    while(rangesIter.hasNext()){
      aRange = (Range)rangesIter.next();
      tData = getTypeData(aRange.setName, aRange.type);
      Iterator annIter = tData.annotations.iterator();
      while(annIter.hasNext()){
        Annotation ann = (Annotation)annIter.next();
        textPane.select(ann.getStartNode().getOffset().intValue(),
                        ann.getEndNode().getOffset().intValue());
        textPane.setCharacterAttributes(tData.getAttributes(), true);
        value = i * 100 / size;
        if(value - lastValue >= 5){
          progressBar.setValue(value);
          progressBar.paintImmediately(progressBar.getBounds());
          lastValue = value;
        }
        i++;
      }
    }
    //restore the state
    textPane.select(selStart, selEnd);
    try{
      SwingUtilities.invokeAndWait(new Runnable(){
        public void run(){
          //show the text
          textScroll.getViewport().setView(textPane);
          try{
            textScroll.getViewport().setViewPosition(
                                     textPane.modelToView(position).getLocation());
          }catch(BadLocationException ble){
          }
        }
      });
    }catch(java.lang.reflect.InvocationTargetException ite){
      throw(new gate.util.GateRuntimeException(ite.toString()));
    }catch(java.lang.InterruptedException ie){
      throw(new gate.util.GateRuntimeException(ie.toString()));
    }
  }//protected void restoreText()


  protected void selectAnnotation(String set, Annotation ann){
    TypeData tData = getTypeData(set, ann.getType());
    if(!tData.getVisible()){
      tData.setVisible(true);
      //sleep a while so the gui updater thread has time to start
      try{
        Thread.sleep(100);
      }catch(InterruptedException ie){}
      synchronized(Thread.class){
        while(guiUpdateThread != null && guiUpdateThread.isAlive()){
          try{
            Thread.sleep(100);
          }catch(InterruptedException ie){}
        }
        //refresh the display for the type (the checkbox has to be shown selected)
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                      ((DefaultMutableTreeNode)stylesTreeRoot).
                                      getFirstChild();
        while(node != null &&
              !((TypeData)node.getUserObject()).getSet().equals(set))
          node = node.getNextSibling();
        if(node != null){
          node = (DefaultMutableTreeNode)node.getFirstChild();
          String type = ann.getType();
          while(node != null &&
                !((TypeData)node.getUserObject()).getType().equals(type))
            node = node.getNextSibling();
          if(node != null) stylesTreeModel.nodeChanged(node);
        }
      }
    }
    int position = -1;
    synchronized(data){
      position = data.indexOf(ann);
      data.notifyAll();
    };
    if(position != -1){
      position = annotationsTable.getTableRow(position);
      if(position != -1){
        annotationsTable.clearSelection();
        annotationsTable.addRowSelectionInterval(position, position);
        annotationsTable.scrollRectToVisible(
              annotationsTable.getCellRect(position, 0, true));
      }
    }
  }
  public void setEditable(boolean newEditable) {
    editable = newEditable;
  }
  public boolean isEditable() {
    return editable;
  }
  //inner classes
  /**
   * A custom table model used to render a table containing the annotations from
   * a set of annotation sets.
   * The columns will be: Type, Set, Start, End, Features
   */
  protected class AnnotationsTableModel extends AbstractTableModel{
    public AnnotationsTableModel(){
    }

    public int getRowCount(){
      synchronized(data){
        return data.size();
      }
    }

    public int getColumnCount(){
      return 5;
    }

    public String getColumnName(int column){
      switch(column){
        case 0: return "Type";
        case 1: return "Set";
        case 2: return "Start";
        case 3: return "End";
        case 4: return "Features";
        default:return "?";
      }
    }

    public Class getColumnClass(int column){
      switch(column){
        case 0: return String.class;
        case 1: return String.class;
        case 2: return Long.class;
        case 3: return Long.class;
        case 4: return String.class;
        default:return Object.class;
      }
    }

    public Object getValueAt(int row, int column){
      Annotation ann;
      synchronized(data){
        ann = (Annotation)data.get(row);
        switch(column){
          case -1:{//The actual annotation
            return ann;
          }
          case 0:{//Type
            return ann.getType();
          }
          case 1:{//Set
            Iterator rangesIter = ranges.iterator();
            while(rangesIter.hasNext()){
              Range range = (Range)rangesIter.next();
              if(range.start <= row && row < range.end) return range.setName;
            }
            return "?";
          }
          case 2:{//Start
            return ann.getStartNode().getOffset();
          }
          case 3:{//End
            return ann.getEndNode().getOffset();
          }
          case 4:{//Features
            return ann.getFeatures().toString();
          }
          default:{
          }
        }
      }//synchronized(data)
      return null;
    }
  }//class AnnotationsTableModel extends AbstractTableModel


  /**
   * Displays an entry in the right hand side tree.
   * <strong><a name="override">Implementation Note:</a></strong>
   * This class overrides
   * <code>revalidate</code>,
   * <code>repaint</code>,
   * and
   * <code>firePropertyChange</code>
   * solely to improve performance.
   * If not overridden, these frequently called methods would execute code paths
   * that are unnecessary for a tree cell renderer.
   */
  class NodeRenderer extends JPanel implements TreeCellRenderer{

    public NodeRenderer(){
      icon = new ImageIcon();
      visibleChk = new JCheckBox("",false);
      visibleChk.setOpaque(false);
      label = new JLabel(icon);
      textComponent = new JTextPane();
      selectedBorder = BorderFactory.createLineBorder(Color.blue);
      FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 1, 1);
      setLayout(layout);
      setOpaque(false);
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                              Object value,
                                              boolean selected,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus){
      if(value instanceof DefaultMutableTreeNode){
        TypeData nData = (TypeData)((DefaultMutableTreeNode)value).getUserObject();
        if(nData != null){
          textComponent.selectAll();
          textComponent.replaceSelection(nData.getTitle());
          textComponent.selectAll();
          textComponent.setCharacterAttributes(nData.getAttributes(), true);
          //needs to be sized in order for modelToView() to work properly
          textComponent.setSize(textComponent.getPreferredSize());
          try{
            Rectangle rect = textComponent.modelToView(nData.getTitle().length());
            int width  = rect.x + rect.width -
                         textComponent.modelToView(0).x + 2;
            int height = textComponent.modelToView(0).height + 2;
            BufferedImage image = (BufferedImage)tree.createImage(width, height);
            if(image != null){
              Graphics graphics = image.getGraphics();
              if(graphics != null) {
                textComponent.printAll(graphics);
              }
              icon.setImage(image);
            }
          }catch(BadLocationException ble){
            ble.printStackTrace(System.err);
          }

          removeAll();
          if(nData.getType() != null){
            visibleChk.setSelected(nData.getVisible());
            add(visibleChk);
          }
          add(label);
          if(selected) setBorder(selectedBorder);
          else setBorder(null);
        }
      }else{
        label.setIcon(null);
        label.setText(value.toString());
        removeAll();
        add(label);
      }
      return this;
    }

    /**
     * Overrides <code>JComponent.getPreferredSize</code> to
     * return slightly wider preferred size value.
     */

    public Dimension getPreferredSize() {
      Dimension retDimension = super.getPreferredSize();
      Insets borderInsets = selectedBorder.getBorderInsets(this);
      if(retDimension != null){
          retDimension = new Dimension(retDimension.width + 3 +
                                       borderInsets.left +
                                       borderInsets.right,
                                       retDimension.height +
                                       borderInsets.top +
                                       borderInsets.bottom);
      }
      return retDimension;
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void revalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(long tm, int x, int y, int width, int height) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(Rectangle r) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}

    Border selectedBorder;
    ImageIcon icon;
    JLabel label;
    JCheckBox visibleChk;
    JTextPane textComponent;
  }

  public class TypeData{
    public TypeData(String set, String type, boolean visible){
      this.set = set;
      this.type = type;
      this.visible = visible;
      Map setMap = (Map)typeDataMap.get(set);
      if(setMap == null){
        setMap = new HashMap();
        typeDataMap.put(set, setMap);
      }
      if(type == null){
        //this node represents a Set
        style = textPane.addStyle(set, textPane.getStyle("default"));
      }else{
        style = textPane.addStyle(set + "." + type, textPane.getStyle(set));
        StyleConstants.setForeground(style, colGenerator.getNextColor());http://online.amerada.co.uk
        //add an intermediary style that will be used for the actual display
        textPane.addStyle("_" + set + "." + type, style);
        //add the style that will be used for the actual display
        textPane.addStyle("_" + set + "." + type + "_",
                          textPane.getStyle("_" + set + "." + type));
        setMap.put(type, this);
      }
    }

    public String getSet(){ return set;}
    public void setSet(String set){this.set = set;}

    public String getType(){return type;}

    public String getTitle(){return (type == null) ? set : type;}
    public boolean getVisible(){return visible;}

    public void setVisible(boolean isVisible){
      if(this.visible == isVisible) return;
      this.visible = isVisible;
      //this is most likely called from the SWING thread so we want to get
      //out of here as quickly as possible. We'll start a new thread that will
      //do all that needs doing
      Runnable runnable = new Runnable(){
        public void run(){
          //define the runnable for the guiUpdateThread
          Runnable guiUpdater = new Runnable(){
            public void run(){
              if(visible){
                  //make the corresponding range visible
                  //update the annotations table
                  synchronized(data){
                    range = new Range(set, type, data.size(),
                                      data.size() + annotations.size());
                    ranges.add(range);
                    data.addAll(annotations);
                    SwingUtilities.invokeLater(new Runnable(){
                      public void run(){
                        annotationsTableModel.fireTableDataChanged();
                      }
                    });
                  }

                  //update the text display
                  Style actualStyle = textPane.getStyle("_" + set + "." + type);
                  actualStyle.setResolveParent(style);
                  showHighlights(annotations, textPane.getStyle("_" + set + "." + type + "_"));
                }else{
                  //hide the corresponding range
                  //update the annotations table
                  synchronized(data){
                    Collections.sort(ranges);
                    Iterator rangesIter = ranges.iterator();
                    while(rangesIter.hasNext()){
                      //find my range
                      Range aRange = (Range)rangesIter.next();
                      if(aRange == range){
                        rangesIter.remove();
                        int size = range.end - range.start;
                        //remove the elements from Data
                        data.subList(range.start, range.end).clear();
                        //shift back all the remaining ranges
                        while(rangesIter.hasNext()){
                          aRange = (Range)rangesIter.next();
                          aRange.start -= size;
                          aRange.end -= size;
                        }
                      }
                    }
                    SwingUtilities.invokeLater(new Runnable(){
                      public void run(){
                        annotationsTableModel.fireTableDataChanged();
                      }
                    });
                  }//synchronized(data)
                  //update the text display
                  Style actualStyle = textPane.getStyle("_" + set + "." + type);
                  actualStyle.setResolveParent(textPane.getStyle("default"));
                  //restoreText();
                }
            }
          };

          //wait for the guiUpdateThread to finish if it's doing anything
          //and then start it with the new runnable
          synchronized(Thread.class){
            while(guiUpdateThread != null && guiUpdateThread.isAlive()){
              try{
                Thread.sleep(100);
              }catch(InterruptedException ie){}
            }
            guiUpdateThread = new Thread(guiUpdater);
            guiUpdateThread.setPriority(Thread.MIN_PRIORITY);
            guiUpdateThread.start();
          }//synchronized(Thread.class)
        }//runnable.run()
      };//Runnable runnable = new Runnable()
      Thread thread = new Thread(runnable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }//public void setVisible(boolean isVisible)

    public AttributeSet getAttributes(){ return style;}

    public void setAttributes(AttributeSet newAttributes){
      style.removeAttributes(style.copyAttributes());
      style.addAttributes(newAttributes);
    }


    javax.swing.text.DefaultStyledDocument doc;
    public void setAnnotations(AnnotationSet as){
      this.annotations = as;
    }

    public AnnotationSet getAnnotations(){
      return annotations;
    }

    public String toString(){return getTitle();}

    private String set;
    private String type;
    private boolean visible;
    private Style style;
    private AnnotationSet annotations = null;
    private Range range = null;
  }//class TypeData


  /**
   * Describes a range in the {@link data} structure.
   */
  class Range implements Comparable{
    public Range(String setName, String type, int start, int end){
      this.setName = setName;
      this.type = type;
      this.start = start;
      this.end = end;
    }

    public String toString(){
      return setName +  ", " + type + " (" + start + ", " + end + ")";
    }

    public int compareTo(Object other){
      if(other instanceof Range) return start - ((Range)other).start;
      else throw new ClassCastException("Can't compare a " +
                                         other.getClass() + " to a " +
                                         getClass() + "!");
    }

    String setName;
    String type;
    int start;
    int end;
  }//class Range

  /**
   * Used to process a bunch of events after they happened. E.g. when a
   * processing resource runs over a document it is likely to generate a set of
   * new annotations. We don't want the interface to be updated for each of
   * them but rather to be updated from time to time with a bunch of
   * annnotations at a time.
   * This listener runs in its own thread that mostly sleeps and only wakes up
   * when there are unprocessed events after a given time interval
   * ({@link sleepInterval}) has passed from the last event occured. When the
   * thread wakes it will process <strong>all</strong> the pending events and
   * then will go back to sleep.
   */
  class DelayedListener implements GateListener, Runnable{
    public DelayedListener(){
      eventQueue = Collections.synchronizedList(new ArrayList());
    }

    public void processGateEvent(GateEvent e){
      synchronized(eventQueue){
        eventQueue.add(e);
        lastEvent = System.currentTimeMillis();
      }
    }

    public void run(){
      while(!stop){
        synchronized(eventQueue){
          if((System.currentTimeMillis() - lastEvent) > sleepInterval){
            GateEvent currentEvent;
            while(! eventQueue.isEmpty()){
              currentEvent = (GateEvent)eventQueue.remove(0);
              //process the current event
              if(currentEvent instanceof gate.event.DocumentEvent){
                //document event
                gate.event.DocumentEvent docEvt =
                  (gate.event.DocumentEvent)currentEvent;
                if(docEvt.getType() == docEvt.ANNOTATION_SET_REMOVED){
throw new UnsupportedOperationException("DocumentEditor -> Annotation set removed");
                }else if(docEvt.getType() == docEvt.ANNOTATION_SET_ADDED){
                  addAnnotationSet(document.getAnnotations(docEvt.getAnnotationSetName()));
                }
              }else if(currentEvent instanceof AnnotationSetEvent){
                //annotation set event
                AnnotationSetEvent asEvt = (AnnotationSetEvent)currentEvent;
                if(asEvt.getType() == asEvt.ANNOTATION_ADDED){
                  AnnotationSet set = (AnnotationSet)asEvt.getSource();
                  String setName = set.getName();
                  if(setName == null) setName = "<Default>";
                  Annotation ann = asEvt.getAnnotation();
                  String type = ann.getType();
                  TypeData tData = getTypeData(setName, type);
                  if(tData != null){
                    tData.annotations.add(ann);
                    if(tData.getVisible()){
                      textPane.select(ann.getStartNode().getOffset().intValue(),
                                      ann.getEndNode().getOffset().intValue());
                      textPane.setCharacterAttributes(
                                  textPane.getStyle("_" + setName + "." +
                                                    type + "_"), true);
                    }
                  }else{
                    //new type
                    Map setMap = (Map)typeDataMap.get(setName);
                    if(setMap == null){
                      setMap = new HashMap();
                      typeDataMap.put(setName, setMap);
                    }
                    tData = new TypeData(setName, type, false);
                    tData.setAnnotations(set.get(type));
                    setMap.put(type, tData);
                    DefaultMutableTreeNode typeNode =
                              new DefaultMutableTreeNode(tData, false);

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                      ((DefaultMutableTreeNode)stylesTreeRoot).getFirstChild();
                    while(node != null &&
                          !((TypeData)node.getUserObject()).getSet().equals(setName))
                      node = node.getNextSibling();
if(node == null) System.out.println("Could not find set: " + setName);
                    node.add(typeNode);
                    stylesTreeModel.nodeStructureChanged(node);
                  }
                }else if(asEvt.getType() == asEvt.ANNOTATION_REMOVED){
throw new UnsupportedOperationException("DocumentEditor -> Annotation removed");
                }
              }else{
                //unknown event type
              }
            }//while(! eventQueue.isEmpty())
          }
        }//synchronized(eventQueue)
        try{
          Thread.sleep(sleepInterval);
        }catch(InterruptedException ie){
        }
      }
    }

    int sleepInterval = 200;
    boolean stop = false;
    protected java.util.List eventQueue;
    protected long lastEvent = 0;
  }/////class DelayedListener

  public class CustomLabelView extends javax.swing.text.LabelView{
    public CustomLabelView(Element elem){
      super(elem);
    }

    public Color getBackground() {
      AttributeSet attr = getAttributes();
      if (attr != null) {
        javax.swing.text.Document d = super.getDocument();
        if (d instanceof StyledDocument){
          StyledDocument doc = (StyledDocument) d;
          return doc.getBackground(attr);
        }else{
          return null;
        }
      }
      return null;
    }
  }

  protected class SelectAnnotationPopupItem extends JMenuItem{
    public SelectAnnotationPopupItem(Annotation ann, String setName){
      super(ann.getType());
      setToolTipText("<html><b>Features:</b><br>" +
                     ann.getFeatures().toString() + "</html>");
      annotation = ann;
      start = ann.getStartNode().getOffset().intValue();
      end = ann.getEndNode().getOffset().intValue();
      set = setName;
      this.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
          try{
            highlighter.removeAllHighlights();
            highlighter.addHighlight(start, end, DefaultHighlighter.DefaultPainter);
          }catch(BadLocationException ble){
            throw new GateRuntimeException(ble.toString());
          }
        }

        public void mouseExited(MouseEvent e) {
          highlighter.removeAllHighlights();
        }
      });

      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Runnable runnable = new Runnable(){
            public void run(){
             selectAnnotation(set, annotation);
            }
          };
          Thread thread = new Thread(runnable);
          thread.start();
        }
      });
    }

    int start;
    int end;
    String set;
    Annotation annotation;
  }

  protected class NewAnnotationPopupItem extends JMenuItem{
    public NewAnnotationPopupItem(int aStart, int anEnd,
                                  AnnotationSchema aSchema,
                                  AnnotationSet aTargetAS){
      super(aSchema.getAnnotationName());
      this.start = aStart;
      this.end = anEnd;
      this.schema = aSchema;
      this.targetAS = aTargetAS;

      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String newASName = "foo";
          if(targetAS == null){
            Object answer = JOptionPane.showInputDialog(
                              textPane,
                              "Please provide a name for the new annotation set:",
                              "Gate",
                              JOptionPane.QUESTION_MESSAGE);
            if(answer == null) return;
            newASName = (String)answer;
          }
          FeatureMap features = annotationEditDialog.show(schema);
          if(features != null){
            if(targetAS == null){
              targetAS = document.getAnnotations(newASName);
            }
            try{
              targetAS.add(new Long(start), new Long(end),
                           schema.getAnnotationName(), features);
            }catch(InvalidOffsetException ioe){
              JOptionPane.showMessageDialog(textPane,
                                            "Invalid input!\n" + ioe.toString(),
                                            "Gate", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      });
    }

    int start;
    int end;
    AnnotationSchema schema;
    AnnotationSet targetAS;
  }


  public class CustomStyledEditorKit extends StyledEditorKit{
    private final ViewFactory defaultFactory = new CustomStyledViewFactory();
    public ViewFactory getViewFactory() {
      return defaultFactory;
    }

    /**
      * Inserts content from the given stream, which will be
      * treated as plain text.
      * This insertion is done without checking \r or \r \n sequence.
      * It takes the text from the Reader and place it into Document at position
      * pos
      */
    public void read(Reader in, javax.swing.text.Document doc, int pos)
                throws IOException, BadLocationException {

      char[] buff = new char[65536];
      int charsRead = 0;
      while ((charsRead = in.read(buff, 0, buff.length)) != -1) {
            doc.insertString(pos, new String(buff, 0, charsRead), null);
            pos += charsRead;
      }// while
    }// read
  }

  public class CustomStyledViewFactory implements ViewFactory{
    public View create(Element elem) {
      String kind = elem.getName();
      if (kind != null) {
        if (kind.equals(AbstractDocument.ContentElementName)) {
          return new CustomLabelView(elem);
        }else if (kind.equals(AbstractDocument.ParagraphElementName)) {
          return new ParagraphView(elem);
        }else if (kind.equals(AbstractDocument.SectionElementName)) {
          return new BoxView(elem, View.Y_AXIS);
        }else if (kind.equals(StyleConstants.ComponentElementName)) {
          return new ComponentView(elem);
        }else if (kind.equals(StyleConstants.IconElementName)) {
          return new IconView(elem);
        }
      }
      // default to text display
      return new CustomLabelView(elem);
    }
  }

  }//class AnnotationEditor