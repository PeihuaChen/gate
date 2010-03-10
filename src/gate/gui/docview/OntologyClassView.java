/**
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Thomas Heitz - 14/12/2009
 *
 *  $Id$
 */

package gate.gui.docview;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Resource;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.annedit.AnnotationData;
import gate.gui.annedit.AnnotationDataImpl;
import gate.gui.ontology.OntologyItemComparator;
import gate.gui.ontology.OResourceNode;
import gate.creole.ontology.*;
import gate.LanguageResource;
import gate.util.LuckyException;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Document view that displays an ontology class tree to annotate a document.
 *
 * You can tick a class to show the highlight in the text for the class
 * instances.
 */
public class OntologyClassView extends AbstractDocumentView
    implements CreoleListener {

  public OntologyClassView() {

    classColorMap = new HashMap<OClass, Color>();
    selectedClasses = new HashSet<OClass>();
    classHighlightsDataMap = new HashMap<OClass, List>();
    itemComparator = new OntologyItemComparator();
  }

  protected void initGUI() {

    // get a pointer to the text view used to display
    // the selected annotations
    Iterator centralViewsIter = owner.getCentralViews().iterator();
    while(textView == null && centralViewsIter.hasNext()){
      DocumentView aView = (DocumentView) centralViewsIter.next();
      if(aView instanceof TextualDocumentView)
        textView = (TextualDocumentView) aView;
    }
    textArea = textView.getTextView();
    // get a pointer to the instance view
    Iterator horizontalViewsIter = owner.getHorizontalViews().iterator();
    while(instanceView == null && horizontalViewsIter.hasNext()){
      DocumentView aView = (DocumentView)horizontalViewsIter.next();
      if(aView instanceof OntologyInstanceView)
        instanceView = (OntologyInstanceView) aView;
    }
    instanceView.setOwner(owner);
    // find the first ontology loaded in the system
    List<LanguageResource> resources =
      gate.Gate.getCreoleRegister().getPublicLrInstances();
    for (LanguageResource resource : resources) {
      if(resource instanceof Ontology) {
        selectedOntology = (Ontology) resource;
        break;
      }
    }

    mainPanel = new JPanel(new BorderLayout());
    rootNode = new DefaultMutableTreeNode(null, true);
    treeModel = new DefaultTreeModel(rootNode);
    tree = new JTree(treeModel);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new ClassTreeCellRenderer());
    tree.setCellEditor(new ClassTreeCellEditor(tree));
    tree.setEditable(true);
    tree.getSelectionModel().setSelectionMode(
      TreeSelectionModel.SINGLE_TREE_SELECTION);
    scrollPane = new JScrollPane(tree);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    setComboBox = new JComboBox();
    setComboBox.setEditable(true);
    setComboBox.setToolTipText(
      "Annotation set where to load/save the annotations");
    mainPanel.add(setComboBox, BorderLayout.SOUTH);

    initListeners();

    // show the instance view at the bottom
    instanceView.setClassView(this);
    instanceView.setActive(true);
    textView.getOwner().setBottomView(instanceView);

    if (selectedOntology == null) {
      messageLabel = new JLabel(
        "<html><p><font color=red>Load at least one ontology.");
      messageLabel.setBackground(
        UIManager.getColor("Tree.selectionBackground"));
      mainPanel.add(messageLabel, BorderLayout.NORTH);
    } else {
      // find the first set that contains annotations used before by this view
      selectedSet = "";
      List<String> annotationSets = new ArrayList<String>();
      annotationSets.add("");
      annotationSets.addAll(document.getAnnotationSetNames());
      Collections.sort(annotationSets);
      for (String setName : annotationSets) {
        if (setColorTreeNodesWhenInstancesFound(setName)) {
          selectedSet = setName;
          break;
        }
      }
      setComboBox.setModel(new DefaultComboBoxModel(
        new Vector<String>(annotationSets)));
      setComboBox.setSelectedItem(selectedSet);
      updateClassTree();
    }
  }

  protected void initListeners() {

    // when a class is selected in the tree update the instance table
    tree.getSelectionModel().addTreeSelectionListener(
      new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
          if (e.getNewLeadSelectionPath() == null) {
            selectedClass = null;
          } else { // a class is selected
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
              e.getNewLeadSelectionPath().getLastPathComponent();
            selectedClass = (OClass)
              ((OResourceNode) node.getUserObject()).getResource();
          }
          instanceView.updateInstanceTable(selectedClass);
        }
      }
    );

    setComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // TODO: highlights doesn't get removed in the text
        selectedSet = (String) setComboBox.getSelectedItem();
        classHighlightsDataMap.clear();
        selectedClasses.clear();
        setColorTreeNodesWhenInstancesFound(selectedSet);
        updateClassTree();
      }
    });

    // a listener that stops or restarts a timer which calls an action
    mouseStoppedMovingAction = new MouseStoppedMovingAction();
    mouseMovementTimer = new javax.swing.Timer(
      MOUSE_MOVEMENT_TIMER_DELAY, mouseStoppedMovingAction);
    mouseMovementTimer.setRepeats(false);
    textMouseListener = new TextMouseListener();
  }

  protected void registerHooks() {
    textArea.addMouseListener(textMouseListener);
    textArea.addMouseMotionListener(textMouseListener);
    // reselect annotations
    SwingUtilities.invokeLater(new Runnable() { public void run() {
      for (OClass oClass : new HashSet<OClass>(selectedClasses)) {
        if (classHighlightsDataMap.containsKey(oClass)) {
          textView.addHighlights(classHighlightsDataMap.get(oClass));
        }
      }
    }});
  }

  protected void unregisterHooks() {
    textArea.removeMouseListener(textMouseListener);
    textArea.removeMouseMotionListener(textMouseListener);
    // unselect annotations
    SwingUtilities.invokeLater(new Runnable() { public void run() {
      for (OClass oClass : selectedClasses) {
        if (classHighlightsDataMap.containsKey(oClass)) {
          textView.removeHighlights(classHighlightsDataMap.get(oClass));
        }
      }
    }});
  }

  public void cleanup() {
    super.cleanup();
    document = null;
  }

  public Component getGUI() {
    return mainPanel;
  }

  public int getType() {
    return VERTICAL;
  }

  public void resourceLoaded(CreoleEvent e) {
    // TODO: loading an ontology doesn't call this method!
    if (e.getResource() instanceof Ontology) {
      mainPanel.remove(messageLabel);
      selectedOntology = (Ontology) e.getResource();
      selectedSet = "";
      List<String> annotationSets = new ArrayList<String>();
      annotationSets.add("");
      annotationSets.addAll(document.getAnnotationSetNames());
      Collections.sort(annotationSets);
      for (String setName : annotationSets) {
        if (setColorTreeNodesWhenInstancesFound(setName)) {
          selectedSet = setName;
          break;
        }
      }
      setComboBox.setModel(new DefaultComboBoxModel(
        new Vector<String>(annotationSets)));
      setComboBox.setSelectedItem(selectedSet);
      updateClassTree();
    }
  }

  public void resourceUnloaded(CreoleEvent e) { /* do nothing */ }

  public void datastoreOpened(CreoleEvent e) { /* do nothing */ }

  public void datastoreCreated(CreoleEvent e) { /* do nothing */ }

  public void datastoreClosed(CreoleEvent e) { /* do nothing */ }

  public void resourceRenamed(Resource resource, String oldName,
                              String newName) { /* do nothing */ }

  /**
   * Extract annotations that have been created by this view and
   * colored the corresponding tree class node if found.
   * @param setName the annotation set name to search
   * @return true if and only if one annotation has been found
   */
  protected boolean setColorTreeNodesWhenInstancesFound(String setName) {
    boolean returnValue = false;
    for (Annotation annotation :
        document.getAnnotations(setName).get("Mention")) {
      if (annotation.getFeatures().containsKey("ontology")
      && annotation.getFeatures().containsKey("class")
      && annotation.getFeatures().containsKey("inst")) {
        // choose a background color for the annotation type node in the tree
        OClass oClass = selectedOntology.getOClass(selectedOntology
          .createOURI((String)annotation.getFeatures().get("class")));
        if (oClass != null) {
          classColorMap.put(oClass,
            AnnotationSetsView.getColor(setName,oClass.getName()));
          returnValue = true;
        }
      }
    }
    return returnValue;
  }

  /**
   * Update the class tree from the ontology.
   * Based on {@link gate.gui.ontology.OntologyEditor#rebuildModel()}.
   */
  protected void updateClassTree() {
    rootNode.removeAllChildren();
    List<OResource> rootClasses =
      new ArrayList<OResource>(selectedOntology.getOClasses(true));
    Collections.sort(rootClasses, itemComparator);
    addChildrenRec(rootNode, rootClasses, itemComparator);
    SwingUtilities.invokeLater(new Runnable() { public void run() {
      treeModel.nodeStructureChanged(rootNode);
      // expand the root
      tree.expandPath(new TreePath(rootNode));
      // expand the entire tree
      for (int i = 0; i < tree.getRowCount(); i++) { tree.expandRow(i); }
    }});
  }

  /**
   * Adds the children nodes to a node using values from a list of
   * classes and instances.
   * Based on {@link gate.gui.ontology.OntologyEditor#addChidrenRec(
     javax.swing.tree.DefaultMutableTreeNode, java.util.List,
     java.util.Comparator)}.
   *
   * @param parent the parent node.
   * @param children the list of children objects.
   * @param comparator the Comparator used to sort the children.
   */
  protected void addChildrenRec(DefaultMutableTreeNode parent,
          List<OResource> children, Comparator<OResource> comparator) {
    for(OResource aChild : children) {
      DefaultMutableTreeNode childNode =
        new DefaultMutableTreeNode(new OResourceNode(aChild));
      parent.add(childNode);

      if(aChild instanceof OClass) {
        childNode.setAllowsChildren(true);
        // add all the subclasses
        OClass aClass = (OClass)aChild;
        List<OResource> childList = new ArrayList<OResource>(aClass
                .getSubClasses(OClass.Closure.DIRECT_CLOSURE));
        Collections.sort(childList, comparator);
        addChildrenRec(childNode, childList, comparator);
      }
      else if(aChild instanceof OInstance) {
        childNode.setAllowsChildren(false);
      }
      tree.expandPath(new TreePath(childNode.getPath()));
    }
  }

  /**
   * A mouse listener used for events in the text view.
   * Stop or restart the timer that will call {@link MouseStoppedMovingAction}.
   * Based from {@link AnnotationSetsView.TextMouseListener}.
   */
  protected class TextMouseListener extends MouseInputAdapter {
    public void mouseDragged(MouseEvent e){
      //do not create annotations while dragging
      mouseMovementTimer.stop();
    }
    public void mouseMoved(MouseEvent e){
      //this triggers select annotation leading to edit annotation or new
      //annotation actions
      //ignore movement if CTRL pressed or dragging
      int modEx = e.getModifiersEx();
      if((modEx & MouseEvent.CTRL_DOWN_MASK) != 0){
        mouseMovementTimer.stop();
        return;
      }
      if((modEx & MouseEvent.BUTTON1_DOWN_MASK) != 0){
        mouseMovementTimer.stop();
        return;
      }
      //check the text location is real
      int textLocation = textArea.viewToModel(e.getPoint());
      try {
        Rectangle viewLocation = textArea.modelToView(textLocation);
        //expand the rectangle a bit
        int error = 10;
        viewLocation = new Rectangle(viewLocation.x - error,
                                     viewLocation.y - error,
                                     viewLocation.width + 2*error,
                                     viewLocation.height + 2*error);
        if(viewLocation.contains(e.getPoint())){
          mouseStoppedMovingAction.setTextLocation(textLocation);
        }else{
          mouseStoppedMovingAction.setTextLocation(-1);
        }
      }
      catch(BadLocationException ble) {
        throw new LuckyException(ble);
      }finally{
        mouseMovementTimer.restart();
      }
    }
    public void mouseExited(MouseEvent e){
      mouseMovementTimer.stop();
    }
  }

  /**
   * Add the text selection to the filter instance table to enable creating
   * a new instance from the selection or adding it as a new label to an
   * existing instance.
   * Based on {@link AnnotationSetsView.MouseStoppedMovingAction}.
   */
  protected class MouseStoppedMovingAction extends AbstractAction {
    public void actionPerformed(ActionEvent evt) {
      int start = textArea.getSelectionStart();
      int end   = textArea.getSelectionEnd();
      String selectedText = textArea.getSelectedText();
      if (textLocation == -1
       || selectedClass == null
       || selectedText == null
       || start > textLocation
       || end < textLocation
       || start == end) {
        return;
      }
      // remove selection to avoid calling again this method
      textArea.setSelectionStart(start);
      textArea.setSelectionEnd(start);
      instanceView.addSelectionToFilter(selectedSet, selectedText, start, end);
    }

    public void setTextLocation(int textLocation){
      this.textLocation = textLocation;
    }
    int textLocation;
  }

  public void setClassSelected(final OClass oClass, boolean isSelected) {
    if (isSelected) {
      // find all annotations for the selected class
      final List<AnnotationData> annotationsData =
        new ArrayList<AnnotationData>();
      AnnotationSet annotationSet = document.getAnnotations(selectedSet);
      for (Annotation annotation : annotationSet.get("Mention")) {
        if (annotation.getFeatures().containsKey("ontology")
        && annotation.getFeatures().get("ontology")
          .equals(selectedOntology.getOntologyURI())
        && annotation.getFeatures().containsKey("class")
        && annotation.getFeatures().get("class")
          .equals(selectedClass.getONodeID().toString())
        && annotation.getFeatures().containsKey("inst")) {
          annotationsData.add(new AnnotationDataImpl(annotationSet,annotation));
        }
      }
      selectedClasses.add(oClass);
      if (annotationsData.isEmpty()) {
        // no instance annotation for this class
        classColorMap.remove(oClass);
        SwingUtilities.invokeLater(new Runnable() { public void run() {
          if (classHighlightsDataMap.containsKey(oClass)) {
            textView.removeHighlights(classHighlightsDataMap.get(oClass));
          }
          classHighlightsDataMap.remove(oClass);
          tree.repaint();
        }});
      } else {
        final Color color;
        if (classColorMap.containsKey(oClass)) {
          color = classColorMap.get(oClass);
        } else {
          color = AnnotationSetsView.getColor(selectedSet,oClass.getName());
          classColorMap.put(oClass, color);
        }
        SwingUtilities.invokeLater(new Runnable() { public void run() {
          classHighlightsDataMap.put(oClass,
            textView.addHighlights(annotationsData, color));
          tree.repaint();
        }});
      }
    } else {
      selectedClasses.remove(oClass);
        SwingUtilities.invokeLater(new Runnable() { public void run() {
          if (classHighlightsDataMap.containsKey(oClass)) {
            textView.removeHighlights(classHighlightsDataMap.get(oClass));
          }
          tree.repaint();
        }});
    }
  }

  /**
   * To see if it's worth using it to optimise highlights display.
   * @param set
   * @param annotation
   * @param oClass
   */
  public void selectInstance(AnnotationSet set, Annotation annotation,
                             final OClass oClass) {
    final AnnotationData annotationData = new AnnotationDataImpl(set, annotation);
    final List highlightsData = classHighlightsDataMap.containsKey(oClass) ?
      classHighlightsDataMap.get(oClass) : new ArrayList();
    selectedClasses.add(oClass);
    final Color color;
    if (classColorMap.containsKey(oClass)) {
      color = classColorMap.get(oClass);
    } else {
      color = AnnotationSetsView.getColor(set.getName(),oClass.getName());
      classColorMap.put(oClass, color);
    }
    SwingUtilities.invokeLater(new Runnable() { public void run() {
      highlightsData.add(textView.addHighlight(annotationData, color));
      classHighlightsDataMap.put(oClass, highlightsData);
      tree.repaint();
    }});
  }

  protected class ClassTreeCellRenderer extends JPanel
      implements TreeCellRenderer {

    protected Object userObject;
    protected JCheckBox checkBox;
    protected JLabel label;
    private Color selectionColor =
      UIManager.getColor("Tree.selectionBackground");
    private Color backgroundColor = UIManager.getColor("Tree.textBackground");
    private Border normalBorder =
      BorderFactory.createLineBorder(backgroundColor, 1);
    private Border selectionBorder =
      BorderFactory.createLineBorder(selectionColor, 1);

    protected Object getUserObject() {
      return userObject;
    }

    protected JCheckBox getCheckBox() {
      return checkBox;
    }

    public ClassTreeCellRenderer() {
      setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
      setBorder(normalBorder);
      setOpaque(true);
      setBackground(backgroundColor);
      checkBox = new JCheckBox();
      checkBox.setMargin(new Insets(0, 0, 0, 0));
      checkBox.setOpaque(true);
      checkBox.setBackground(backgroundColor);
      add(checkBox);
      label = new JLabel();
      label.setOpaque(true);
      label.setBackground(backgroundColor);
      add(label);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                               boolean isSelected, boolean isExpanded,
                               boolean isLeaf, int row, boolean hasFocus) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
      userObject = node.getUserObject();
      if (node.getUserObject() == null) { return this; }
      OClass oClass = (OClass)
        ((OResourceNode) node.getUserObject()).getResource();
      checkBox.setSelected(selectedClasses.contains(oClass));
      checkBox.setBackground(isSelected ? selectionColor : backgroundColor);
//      checkBox.setBorder(isSelected ? selectionBorder : normalBorder);
      label.setText(oClass.getName());
      label.setBackground(classColorMap.containsKey(oClass) ?
        classColorMap.get(oClass) : isSelected ?
          selectionColor : backgroundColor);
//      label.setBorder(isSelected ? selectionBorder : normalBorder);
//      if (!classColorMap.containsKey(oClass)) {
        setBackground(isSelected ? selectionColor : backgroundColor);
//      }
      setBorder(isSelected ? selectionBorder : normalBorder);

      return this;
    }
  }

  protected class ClassTreeCellEditor extends AbstractCellEditor
      implements TreeCellEditor {

    ClassTreeCellRenderer renderer = new ClassTreeCellRenderer();
    JTree tree;

    public ClassTreeCellEditor(JTree tree) {
      this.tree = tree;
    }

    public Object getCellEditorValue() {
      boolean isSelected = renderer.getCheckBox().isSelected();
      Object userObject = renderer.getUserObject();
      OClass oClass = (OClass) ((OResourceNode) userObject).getResource();
      // show/hide highlights according to the checkbox state
      setClassSelected(oClass, isSelected);
      return userObject;
    }

    public boolean isCellEditable(EventObject event) {
      boolean returnValue = false;
      if (event instanceof MouseEvent) {
        MouseEvent mouseEvent = (MouseEvent) event;
        TreePath path = tree.getPathForLocation(mouseEvent.getX(),
                                                mouseEvent.getY());
        if (path != null) {
          Object node = path.getLastPathComponent();
          if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
            Rectangle r = tree.getPathBounds(path);
            int x = mouseEvent.getX() - r.x;
            JCheckBox checkbox = renderer.getCheckBox();
            // checks if the mouse click was on the checkbox not the label
            returnValue = x > 0 && x < checkbox.getPreferredSize().width;
          }
        }
      }
      return returnValue;
    }

    public Component getTreeCellEditorComponent(final JTree tree, Object value,
        boolean selected, boolean expanded, boolean leaf, int row) {

      // reuse renderer as an editor
      Component editor = renderer.getTreeCellRendererComponent(tree, value,
          true, expanded, leaf, row, true);

      // stop editing when checkbox has state changed
      renderer.getCheckBox().addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent itemEvent) {
          stopCellEditing();
        }
     });

      return editor;
    }
  }

  public String getSelectedSet() {
    return selectedSet;
  }

  // external resources
  protected Ontology selectedOntology;
  protected TextualDocumentView textView;
  protected JTextArea textArea;
  protected OntologyInstanceView instanceView;

  // UI components
  protected JPanel mainPanel;
  protected JLabel messageLabel;
  protected JScrollPane scrollPane;
  protected JTree tree;
  protected DefaultTreeModel treeModel;
  protected DefaultMutableTreeNode rootNode;
  protected JComboBox setComboBox;

  // local objects
  protected OClass selectedClass;
  /** Classes selected in the class tree. */
  protected Set<OClass> selectedClasses;
  /** Colors for class and their instances only if the latter exist. */
  protected Map<OClass, Color> classColorMap;
  /** HighlightData list for each class. */
  protected Map<OClass, List> classHighlightsDataMap;
  protected String selectedSet;
  protected OntologyItemComparator itemComparator;
  protected MouseStoppedMovingAction mouseStoppedMovingAction;
  protected TextMouseListener textMouseListener;
  protected Timer mouseMovementTimer;
  protected static final int MOUSE_MOVEMENT_TIMER_DELAY = 500;
}
