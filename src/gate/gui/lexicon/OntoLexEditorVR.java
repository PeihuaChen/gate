/*
 *  OntoLexEditorVR.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 20/02/2003
 *
 *  $Id$
 */

package gate.gui.lexicon;

import gate.creole.AbstractVisualResource;
import com.ontotext.gate.vr.*;
import gate.lexicon.*;
import gate.util.*;
import javax.swing.*;
import gate.creole.ontology.*;
import java.util.*;
import gate.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import gate.util.GateRuntimeException;
import java.net.URL;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Dimension;
import gate.gui.MainFrame;

public class OntoLexEditorVR extends AbstractVisualResource
    implements ListSelectionListener, TreeSelectionListener {

  public OntoLexEditorVR() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  public void valueChanged(ListSelectionEvent e) {
    Object source = ((JList) e.getSource()).getSelectedValue();
    if (source == null || ! (source instanceof LexKBSynset)) {
      ontoEditor.setSelectionRow(0);
      selectedSynset = null;
      return;
    }
    LexKBSynset theSynset = (LexKBSynset) source;
    this.selectedSynset = theSynset;
    updateOntologySelection();
  }

  public void valueChanged(TreeSelectionEvent e) {
    TreePath[] paths = e.getPaths();

  }

  protected void updateOntologySelection() {
    if (selectedSynset == null) {
      ontoEditor.setSelectionRow(0);
      return;
    }

    List conceptIDs = ontoLex.getConceptIds(selectedSynset.getId());
    if (conceptIDs == null || conceptIDs.isEmpty()) {
      //select the top of the ontology, since there is no corresponding concept
      ontoEditor.setSelectionRow(0);
      return;
    }

    if (theOntology == null)
      return;

    //select the given conceptIDs in the ontology editor
    for (int i=0; i < conceptIDs.size(); i++) {
      Object conceptID = conceptIDs.get(i);
      OClass theClass = theOntology.getClassByName((String) conceptID);
      TreePath thePath = treePath4Class(theClass);
      if (thePath == null)
        ontoEditor.setSelectionRow(0);
      else if (i==0)
        ontoEditor.getSelectionModel().setSelectionPath(thePath);
      else
        ontoEditor.getSelectionModel().addSelectionPath(thePath);
    }//for

  }

  protected TreePath treePath4Class(OClass theClass) {
    List thePathList = new ArrayList();
    thePathList.add(ontoModel.getRoot());
    TreePath thePath = null;
    Iterator theTopsIter = ((ClassNode)ontoModel.getRoot()).getChildren();
    boolean found = false;
    while( !found && theTopsIter.hasNext()) {
      ClassNode theTopNode = (ClassNode) theTopsIter.next();
      OClass theTop = (OClass) theTopNode.getSource();
      try {
        //check if our class is a subtype of this top
        if (theTop.getSubClasses(OClass.TRANSITIVE_CLOSURE).contains(theClass)) {
          //if yes, let's find the full path
          thePathList.add(theTopNode);
          getRemainingPath(theTopNode, theClass, thePathList);
          found = true;
        }
      } catch (NoSuchClosureTypeException ex) {
        throw new GateRuntimeException(ex.getMessage());
      }
    }//while loop through the top concepts

    thePath = new TreePath(thePathList.toArray());
    return thePath;
  }

  protected void getRemainingPath(
      ClassNode theParent, OClass theTarget, List thePath)
      throws NoSuchClosureTypeException
  {
    Iterator theChildrenIter = theParent.getChildren();
    while (theChildrenIter.hasNext()) {
      ClassNode childNode = (ClassNode) theChildrenIter.next();
      OClass theChild = (OClass) childNode.getSource();
      if (theChild.equals(theTarget)) {
        thePath.add(childNode);
        break;
      }
      if (!theChild.getSubClasses(OClass.TRANSITIVE_CLOSURE).contains(theTarget))
        continue;
      thePath.add(childNode);
      getRemainingPath(childNode, theTarget, thePath);
    }
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    mainBox = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    leftBox = Box.createVerticalBox();
    rightBox = Box.createVerticalBox();

    this.setLayout(gridLayout1);

    this.add(mainBox, null);
    mainBox.add(leftBox);
    mainBox.add(rightBox);

    JScrollPane ontoScroller = new JScrollPane(ontoEditor);
//    ontoScroller.setMinimumSize(new Dimension(300, 400));
    rightBox.add(ontoScroller, null);
    ontoEditor.setVisible(false);
    ontoEditor.addTreeSelectionListener(this);
    ontoEditor.getSelectionModel().setSelectionMode(
        javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

    KnowledgeBaseTreeCellRenderer kbTreeCellRenderer =
                              new KnowledgeBaseTreeCellRenderer();
    ontoEditor.setCellRenderer(kbTreeCellRenderer);


    synsetScroller = new JScrollPane();
    leftBox.add(synsetScroller, null);
    synsetScroller.setVisible(false);

    addMappingButton = new JButton(new AddMappingAction());
    addMappingButton.setText("Add Mapping");

    removeMappingButton = new JButton(new RemoveMappingAction());
    removeMappingButton.setText("Remove Mapping");

    Box buttonBox = Box.createHorizontalBox();
    buttonBox.add(addMappingButton);
    buttonBox.add(Box.createHorizontalStrut(20));
    buttonBox.add(removeMappingButton);
    this.add(buttonBox);

  }

  protected void initListeners(){
  }

 /**
 * Called by the GUI when this viewer/editor has to initialise itself for a
 * specific object.
 * @param target the object (be it a {@link gate.Resource},
 * {@link gate.DataStore} or whatever) this viewer has to display
 */
  public void setTarget(Object target) {
    if(target == null) return;
    if(!(target instanceof gate.lexicon.OntoLexLR)){
      throw new GateRuntimeException(this.getClass().getName() +
                                     " can only be used to display " +
                                     gate.lexicon.OntoLexLR.class.getName() +
                                     "\n" + target.getClass().getName() +
                                     " is not a " +
                                     gate.lexicon.OntoLexLR.class.getName() + "!");
    }
    this.ontoLex = (gate.lexicon.OntoLexLR) target;

    updateGUI();
  }

  protected void updateGUI(){
    Ontology ontology = loadOntology(ontoLex.getOntologyIdentifier());
    ClassNode root = ClassNode.createRootNode(ontology, true);
    this.ontoModel = new OntoTreeModel(root);
    this.ontoEditor.setModel(ontoModel);
    ontoEditor.setVisible(true);

    //remove myself from the listeners first, so there are no memory leaks
    if (chooseSynsetPanel != null)
      chooseSynsetPanel.removeSynsetSelectionListener(this);

    LexicalKnowledgeBase lexKB = loadLexicon(ontoLex.getLexKBIdentifier());
    if (lexKB == null)
      return;
    chooseSynsetPanel = new ChooseSynsetPanel(lexKB, false);
    chooseSynsetPanel.addSynsetSelectionListener(this);
    selectedSynset = chooseSynsetPanel.getSelectedSynset();
    synsetScroller.getViewport().add(chooseSynsetPanel);
    synsetScroller.setVisible(true);
  }

  private Ontology loadOntology(Object ontoId) {

    //first if the ontoId is a URL with a gate:// location, this needs to be
    //converted to an absolute URL, coz otherwise this ontology's URL will
    //not be found among the already loaded ones, because they are converted
    if ((ontoId instanceof URL) &&
        (((URL)ontoId).getProtocol().indexOf("gate")>=0) )
      ontoId = gate.util.protocols.gate.Handler.class.getResource(
                    Files.getResourcePath() + ((URL)ontoId).getPath());

    List lrs = Gate.getCreoleRegister().getPublicLrInstances();
    Iterator iter1 = lrs.iterator();
    while (iter1.hasNext()) {
      gate.LanguageResource lr = (LanguageResource) iter1.next();
      if (! (lr instanceof Ontology))
        continue;
      Ontology currentOntology = (Ontology) lr;
      if (currentOntology.getURL().equals(ontoId)) {
        theOntology = currentOntology;
        break;
      }
    }//while

    //the ontology is not loaded, we must do that
    if (theOntology == null) {
      try {
        FeatureMap fm = Factory.newFeatureMap();
        fm.put("URL", ontoId);

        theOntology = (Ontology)Factory.createResource(
            "com.ontotext.gate.ontology.DAMLKnowledgeBaseImpl",
            fm
          );
      } catch (gate.creole.ResourceInstantiationException ex) {
        throw new GateRuntimeException(
            "Cannot load the ontology used in this OntoLex mapping!");
      }
    }//if

    return theOntology;
  }

  private LexicalKnowledgeBase loadLexicon(Object lexId) {
    LexicalKnowledgeBase theLexicon = null;

    List lrs = Gate.getCreoleRegister().getPublicLrInstances();
    Iterator iter1 = lrs.iterator();
    while (iter1.hasNext()) {
      gate.LanguageResource lr = (LanguageResource) iter1.next();
      if (! (lr instanceof LexicalKnowledgeBase))
        continue;
      LexicalKnowledgeBase currentLexicon = (LexicalKnowledgeBase) lr;
      if (currentLexicon.getLexiconId().equals(lexId)) {
        theLexicon = currentLexicon;
        break;
      }
    }//while

    //the ontology is not loaded, we must do that
    if (theLexicon == null) {
      throw new GateRuntimeException(
        "Please load the lexicon first before trying to use/define a mapping for it!");
    }//if

    return theLexicon;
  }

  protected JTree ontoEditor = new JTree();
  protected OntoTreeModel ontoModel;
  protected ChooseSynsetPanel synsetEditor;
  protected OntoLexLR ontoLex;
  protected Ontology theOntology;
  protected GridLayout gridLayout1 = new GridLayout(2,1);
  protected JSplitPane mainBox;
  protected Box leftBox;
  protected Box rightBox;
  protected ChooseSynsetPanel chooseSynsetPanel;
  protected JScrollPane synsetScroller;
  protected LexKBSynset selectedSynset = null;
  protected JButton addMappingButton;
  protected JButton removeMappingButton;

  /**
   */
  protected class AddMappingAction extends AbstractAction{
    AddMappingAction(){
      super("AddMapping");
      putValue(SHORT_DESCRIPTION, "Add a new mapping");
    }
    public void actionPerformed(java.awt.event.ActionEvent e){
      if (selectedSynset == null) {
        JOptionPane.showMessageDialog(
        OntoLexEditorVR.this,
        "Please choose a synset and a corresponding concept first");
        return;
      }
      TreePath[] selectedPaths = ontoEditor.getSelectionPaths();
      if (selectedPaths == null || selectedPaths.length == 0) {
        JOptionPane.showMessageDialog(
        OntoLexEditorVR.this,
        "Please choose a synset and a corresponding concept first");
        return;
      }

      for (int i=0; i< selectedPaths.length; i++) {
        ClassNode selectedNode = (ClassNode) selectedPaths[i].getLastPathComponent();
        if (! (selectedNode.getSource() instanceof OClass))
          continue;
        ontoLex.add(((OClass)selectedNode.getSource()).getName(),
                    selectedSynset.getId());
      }//for loop
      updateOntologySelection();

    }//actionPerformed
  }

  /**
   */
  protected class RemoveMappingAction extends AbstractAction{
    RemoveMappingAction(){
      super("RemoveMapping");
      putValue(SHORT_DESCRIPTION, "Remove the selected mapping");
    }
    public void actionPerformed(java.awt.event.ActionEvent e){
      if (selectedSynset == null) {
        Out.prln("Select a synset first");
        return;
      }
      TreePath[] selectedPaths = ontoEditor.getSelectionPaths();
      if (selectedPaths == null || selectedPaths.length == 0) {
        Out.prln("Select an ontology concept first");
        return;
      }

      for (int i=0; i< selectedPaths.length; i++) {
        ClassNode selectedNode = (ClassNode) selectedPaths[i].getLastPathComponent();
        if (! (selectedNode.getSource() instanceof OClass))
          continue;
        ontoLex.remove(((OClass)selectedNode.getSource()).getName(),
                       selectedSynset.getId());
        ontoEditor.getSelectionModel().removeSelectionPath(selectedPaths[i]);
      }//for loop

      updateOntologySelection();
    }//actionPerformed
  }

  protected class KnowledgeBaseTreeCellRenderer extends DefaultTreeCellRenderer {
    public KnowledgeBaseTreeCellRenderer() {
    }
    public Component getTreeCellRendererComponent(JTree tree,
                                              Object value,
                                              boolean sel,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus){
      super.getTreeCellRendererComponent(tree, value, sel, expanded,
                                         leaf, row, hasFocus);
      if (! (value instanceof ClassNode))
        return this;
      ClassNode theNode = (ClassNode) value;
      if(theNode.getSource() instanceof OClass) {
        setIcon(MainFrame.getIcon("Class.gif"));
      } else if(theNode.getSource() instanceof OInstance) {
        setIcon(MainFrame.getIcon("Instance.gif"));
      }
      return this;
    }
  }


}