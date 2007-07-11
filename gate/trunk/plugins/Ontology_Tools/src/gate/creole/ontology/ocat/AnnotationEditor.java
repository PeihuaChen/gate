package gate.creole.ontology.ocat;

import gate.Annotation;
import gate.Factory;
import gate.FeatureMap;
import gate.Main;
import gate.creole.ANNIEConstants;
import gate.creole.AnnotationSchema;
import gate.creole.FeatureSchema;
import gate.creole.ontology.AnnotationProperty;
import gate.creole.ontology.DatatypeProperty;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.OntologyUtilities;
import gate.creole.ontology.RDFProperty;
import gate.gui.MainFrame;
import gate.util.GateRuntimeException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import com.ontotext.gate.vr.ClassNode;
import com.ontotext.gate.vr.IFolder;
import com.ontotext.gate.vr.OntoTreeModel;

/**
 * @author niraj
 * 
 */
public class AnnotationEditor extends AbstractAction {

  protected JWindow annotationWindow;

  protected JComboBox typeCombo;

  protected JCheckBox applyToAll;

  protected JCheckBox createInstance;

  protected JScrollPane scroller;

  protected FeaturesEditor featuresEditor;

  protected JPanel scrollerPanel;

  protected OntologyTreePanel ontologyTreePanel;

  protected DeleteAnnotationAction deleteAnnotationAction;

  protected StartOffsetExtendLeftAction soelAction;

  protected StartOffsetExtendRightAction soerAction;

  protected EndOffsetExtendLeftAction eoelAction;

  protected EndOffsetExtendRightAction eoerAction;

  protected CancelAction dissmissAction;

  protected JButton deleteBtn, soelBtn, soerBtn, eoelBtn, eoerBtn, dissmissBtn;

  private int textLocation;

  private Point mousePoint;

  private boolean newAnnotationMode = false;

  private AddChangeAnnotationAction addChangeAnnotationAction;

  private int selectedAnnotationIndex = 0;

  private int iconWidth = 0;

  /**
   * Constructor
   * 
   * @param ontoTreePanel
   */
  public AnnotationEditor(OntologyTreePanel ontoTreePanel) {
    this.ontologyTreePanel = ontoTreePanel;

    initGUI();
  }

  private void initGUI() {
    annotationWindow = new JWindow(
            SwingUtilities
                    .getWindowAncestor(ontologyTreePanel.ontoViewer.documentTextualDocumentView
                            .getGUI()));
    JPanel pane = new JPanel();
    pane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    pane.setLayout(new GridBagLayout());
    pane.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    annotationWindow.setContentPane(pane);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    Insets insets0 = new Insets(0, 0, 0, 0);
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridwidth = 1;
    constraints.gridy = 0;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.insets = insets0;
    pane.add(buttonPanel, constraints);

    soelAction = new StartOffsetExtendLeftAction(MainFrame
            .getIcon("extend-left"));
    soelBtn = new JButton(soelAction);
    soelBtn.setBorderPainted(false);
    soelBtn.setContentAreaFilled(false);
    soelBtn.setMargin(new Insets(0, 0, 0, 0));
    soelBtn.setToolTipText("Extend StartOffset");
    buttonPanel.add(soelBtn);

    soerAction = new StartOffsetExtendRightAction(MainFrame
            .getIcon("extend-right"));
    soerBtn = new JButton(soerAction);
    soerBtn.setBorderPainted(false);
    soerBtn.setContentAreaFilled(false);
    soerBtn.setMargin(new Insets(0, 0, 0, 0));
    soerBtn.setToolTipText("Shrink StartOffset");
    buttonPanel.add(soerBtn);

    deleteAnnotationAction = new DeleteAnnotationAction(MainFrame
            .getIcon("remove-annotation"));
    deleteBtn = new JButton(deleteAnnotationAction);
    deleteBtn.setBorderPainted(false);
    deleteBtn.setContentAreaFilled(false);
    deleteBtn.setMargin(new Insets(0, 0, 0, 0));
    deleteBtn.setToolTipText("Delete Annotation");
    buttonPanel.add(deleteBtn);

    eoelAction = new EndOffsetExtendLeftAction(MainFrame.getIcon("extend-left"));
    eoelBtn = new JButton(eoelAction);
    eoelBtn.setBorderPainted(false);
    eoelBtn.setContentAreaFilled(false);
    eoelBtn.setMargin(new Insets(0, 0, 0, 0));
    eoelBtn.setToolTipText("Shrink EndOffset");
    buttonPanel.add(eoelBtn);

    eoerAction = new EndOffsetExtendRightAction(MainFrame
            .getIcon("extend-right"));
    eoerBtn = new JButton(eoerAction);
    eoerBtn.setBorderPainted(false);
    eoerBtn.setContentAreaFilled(false);
    eoerBtn.setMargin(new Insets(0, 0, 0, 0));
    eoerBtn.setToolTipText("Extend EndOffset");
    buttonPanel.add(eoerBtn);

    applyToAll = new JCheckBox("Apply To All");
    applyToAll.setBorderPainted(false);
    applyToAll.setContentAreaFilled(false);
    applyToAll.setMargin(new Insets(0, 0, 0, 0));
    applyToAll.setToolTipText("Apply to All");
    buttonPanel.add(applyToAll);

    createInstance = new JCheckBox("Create Instance");
    createInstance.setBorderPainted(false);
    createInstance.setContentAreaFilled(false);
    createInstance.setMargin(new Insets(0, 0, 0, 0));
    createInstance.setToolTipText("Create Instance");
    buttonPanel.add(createInstance);

    Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
    if(icon == null) icon = MainFrame.getIcon("exit");
    dissmissAction = new CancelAction(icon);
    dissmissBtn = new JButton(dissmissAction);
    constraints.insets = new Insets(0, 10, 0, 0);
    constraints.anchor = GridBagConstraints.NORTHEAST;
    constraints.weightx = 1;
    dissmissBtn.setBorder(null);
    pane.add(dissmissBtn, constraints);

    constraints.anchor = GridBagConstraints.CENTER;
    constraints.insets = insets0;

    typeCombo = new JComboBox();
    addChangeAnnotationAction = new AddChangeAnnotationAction();
    typeCombo.addActionListener(addChangeAnnotationAction);
    typeCombo.setRenderer(new ComboRenderer(ontologyTreePanel));
    typeCombo.setEditable(true);
    typeCombo.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));

    typeCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent keyevent) {
        String s = ((JTextComponent)typeCombo.getEditor().getEditorComponent())
                .getText();
        if(s != null) {
          if(keyevent.getKeyCode() != KeyEvent.VK_ENTER
                  || keyevent.getKeyCode() != KeyEvent.VK_UP
                  || keyevent.getKeyCode() != KeyEvent.VK_DOWN) {
            IFolder rootNode = (ClassNode)((OntoTreeModel)ontologyTreePanel.currentOntologyTree
                    .getModel()).getRoot();
            // ok we first need to iterate through nodes and obtain all
            // the class and instances
            ArrayList<ClassNode> items = getClassesAndInstances(rootNode, s
                    .toLowerCase());
            ClassNode[] nodes = new ClassNode[items.size()];
            for(int i = 0; i < items.size(); i++) {
              nodes[i] = items.get(i);
            }
            DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(
                    nodes);
            typeCombo.setModel(defaultcomboboxmodel);

            try {
              if(!items.isEmpty()) typeCombo.showPopup();
            }
            catch(Exception exception) {
            }
          }
          ((JTextComponent)typeCombo.getEditor().getEditorComponent())
                  .setText(s);
        }
      }
    });

    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridy = 1;
    constraints.gridwidth = 6;
    constraints.weightx = 1;
    constraints.insets = new Insets(3, 2, 2, 2);
    pane.add(typeCombo, constraints);

    featuresEditor = new FeaturesEditor();
    featuresEditor.setBackground(UIManager.getLookAndFeelDefaults().getColor(
            "ToolTip.background"));
    featuresEditor.init();

    scrollerPanel = new JPanel(new BorderLayout());
    scrollerPanel.add(featuresEditor.getTable(), BorderLayout.CENTER);
    scroller = new CustomScroller(scrollerPanel);
    iconWidth = MainFrame.getIcon("delete").getIconWidth();
    constraints.gridy = 2;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;
    pane.add(scroller, constraints);
  }

  protected class CustomScroller extends JScrollPane {
    public CustomScroller(Component component) {
      super(component);
    }

    public Dimension getPreferredSize() {
      return new Dimension((int)scrollerPanel.getPreferredSize().getWidth()
              + iconWidth * 2, 130);
    }
  }

  private void enableDisableComponents(boolean isNewAnnotationMode) {
    if(isNewAnnotationMode) {
      deleteBtn.setEnabled(false);
      soelBtn.setEnabled(false);
      soerBtn.setEnabled(false);
      eoelBtn.setEnabled(false);
      eoerBtn.setEnabled(false);
      scroller.setEnabled(false);
    }
    else {
      deleteBtn.setEnabled(true);
      soelBtn.setEnabled(true);
      soerBtn.setEnabled(true);
      eoelBtn.setEnabled(true);
      eoerBtn.setEnabled(true);
      scroller.setEnabled(true);
    }
  }

  public void actionPerformed(ActionEvent ae) {

    int[] range = ontologyTreePanel.ontoTreeListener.annotationRange;
    int index1 = -1;
    ArrayList<Integer> indexes = new ArrayList<Integer>();
    if(range != null) {
      for(int i = 0; i < range.length; i += 2) {
        if(textLocation >= range[i] && textLocation <= range[i + 1]) {
          index1 = (i == 0) ? i : i / 2;
          indexes.add(new Integer(index1));
        }
      }
    }

    final ArrayList<Integer> indexes1 = indexes;

    // yes it is put on the highlighted annotation so show the
    // annotation window
    if(range != null && indexes.size() > 0) {
      newAnnotationMode = false;
      if(ontologyTreePanel.showingAnnotationWindow) {
        gate.Annotation annotation = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(indexes.get(0).intValue());
        try {
          JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
          Rectangle startRect = textPane.modelToView(annotation.getStartNode()
                  .getOffset().intValue());
          Rectangle endRect = textPane.modelToView(annotation.getEndNode()
                  .getOffset().intValue());
          Point topLeft = textPane.getLocationOnScreen();
          JTextArea textComp = ontologyTreePanel.ontoViewer.documentTextArea;
          FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
          int charHeight = fm.getAscent() + fm.getDescent();

          int x = topLeft.x + startRect.x;
          int y = topLeft.y + startRect.y + charHeight; // endRect.y +

          if(annotationWindow.getX() == x && annotationWindow.getY() == y) {
            // do nothing
            return;
          }

          enableDisableComponents(newAnnotationMode);
        }
        catch(BadLocationException e1) {
          throw new GateRuntimeException("Can't show the popup window", e1);
        }
      }

      gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
              .get(indexes.get(0).intValue());
      // ok we need to find out classes
      final ArrayList<String> classValues = new ArrayList<String>();
      final ArrayList<Boolean> isClass = new ArrayList<Boolean>();
      for(int i = 0; i < indexes.size(); i++) {
        gate.Annotation tempAnnot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(indexes.get(i).intValue());
        if(tempAnnot.getFeatures().containsKey(
                ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME)) {
          classValues.add(Utils.getClassFeatureValue(tempAnnot));
          isClass.add(new Boolean(true));
        }
        else {
          classValues.add(Utils.getInstanceFeatureValue(tempAnnot));
          isClass.add(new Boolean(false));
        }
      }

      if(classValues.size() == 1) {
        selectedAnnotationIndex = indexes.get(0).intValue();
        showWindow();
        return;
      }

      // so before showing window we need to list all the available
      // classes/instances
      selectedAnnotationIndex = 0;
      final JPopupMenu classLists = new JPopupMenu();
      classLists.setLayout(new GridLayout(classValues.size(), 1));
      for(int i = 0; i < classValues.size(); i++) {
        Icon icon = isClass.get(i).booleanValue() ? MainFrame
                .getIcon("ontology-class") : MainFrame
                .getIcon("ontology-instance");
        JMenuItem button = new JMenuItem(classValues.get(i), icon);
        classLists.add(button);
        button.setActionCommand("" + i);
        button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            final int tempIndex = Integer.parseInt(ae.getActionCommand());
            selectedAnnotationIndex = indexes1.get(tempIndex).intValue();
            classLists.setVisible(false);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                showWindow();
              }
            });
          }
        });
      }

      // and finally show it
      classLists.show(ontologyTreePanel.ontoViewer.documentTextArea,
              (int)mousePoint.getX(), (int)mousePoint.getY());

      // else lets make it visible see below
    }
    else {
      // we check here if there is any text selected
      JTextArea textPane = ontologyTreePanel.ontoViewer.documentTextArea;
      String selectedText = textPane.getSelectedText();
      if(selectedText != null && selectedText.length() > 0) {
        newAnnotationMode = true;
        showWindow();
      }
    }
  }

  ComboBoxModel model;

  boolean explicitCall = false;

  private void showWindow() {
    IFolder rootNode = (ClassNode)((OntoTreeModel)ontologyTreePanel.currentOntologyTree
            .getModel()).getRoot();
    // ok we first need to iterate through nodes and obtain all the
    // class and instances
    ArrayList<ClassNode> items = getClassesAndInstances(rootNode, "");
    if(items.isEmpty()) return;

    // lets populate the typeCombo
    ClassNode[] nodes = new ClassNode[items.size()];
    for(int i = 0; i < items.size(); i++) {
      nodes[i] = items.get(i);
    }
    model = new DefaultComboBoxModel(nodes);
    typeCombo.setModel(model);

    // and lets show it
    JTextArea textComp = ontologyTreePanel.ontoViewer.documentTextArea;

    int x1 = textComp.getSelectionStart();
    int y1 = textComp.getSelectionEnd();

    enableDisableComponents(newAnnotationMode);

    if(!newAnnotationMode) {
      gate.Annotation tempAnnot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
              .get(selectedAnnotationIndex);
      if(tempAnnot != null) {
        x1 = tempAnnot.getStartNode().getOffset().intValue();
        y1 = tempAnnot.getEndNode().getOffset().intValue();

        String aValue = (String)tempAnnot.getFeatures().get(
                ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME);
        boolean isClassAnnotation = true;
        if(aValue == null) {
          aValue = (String)tempAnnot.getFeatures().get(
                  ANNIEConstants.LOOKUP_INSTANCE_FEATURE_NAME);
          isClassAnnotation = false;
        }
        aValue = OntologyUtilities.getResourceName(aValue);
        ClassNode aNode = ontologyTreePanel.getNode(aValue);
        OResource resource = (OResource)aNode.getSource();
        explicitCall = true;
        typeCombo.setSelectedItem(aNode);
        explicitCall = false;

        AnnotationSchema annSchema = new AnnotationSchema();
        Set<FeatureSchema> fsSet = new HashSet<FeatureSchema>();
        for(RDFProperty aProp : ontologyTreePanel.currentProperties) {
          if(aProp instanceof AnnotationProperty) {
            FeatureSchema fs = new FeatureSchema(aProp.getName(), List.class
                    .getName(), "", "custom", null);
            fsSet.add(fs);
          }
          else if(resource instanceof OInstance) {
            if(aProp instanceof DatatypeProperty
                    && aProp.isValidDomain((OInstance)resource)) {
              FeatureSchema fs = new FeatureSchema(aProp.getName(), List.class
                      .getName(), "", "custom", null);
              fsSet.add(fs);
            }
            else if(aProp instanceof ObjectProperty
                    && aProp.isValidDomain((OInstance)resource)) {
              // here we need to obtain all instances which are valid as
              // range for this instance
              Set<OInstance> instances = ontologyTreePanel.getCurrentOntology()
                      .getOInstances();
              Set<String> instSet = new HashSet<String>();
              Iterator<OInstance> instIter = instances.iterator();
              while(instIter.hasNext()) {
                OInstance inst = instIter.next();
                if(aProp.isValidRange(inst)) {
                  instSet.add(inst.getURI().toString());
                }
              }
              FeatureSchema fs = new FeatureSchema(aProp.getName(), List.class
                      .getName(), "", "fixed", instSet);
              fsSet.add(fs);
            }
          }
        }
        annSchema.setFeatureSchemaSet(fsSet);
        featuresEditor.setSchema(annSchema);
        featuresEditor.setTargetFeatures(tempAnnot.getFeatures());
      }
    }
    else {
      AnnotationSchema annSchema = new AnnotationSchema();
      featuresEditor.setSchema(annSchema);
      featuresEditor.setTargetFeatures(Factory.newFeatureMap());
    }

    try {
      Rectangle startRect = textComp.modelToView(x1);
      Rectangle endRect = textComp.modelToView(y1);
      Point topLeft = textComp.getLocationOnScreen();

      FontMetrics fm = textComp.getFontMetrics(textComp.getFont());
      int charHeight = fm.getAscent() + fm.getDescent();

      int x = topLeft.x + startRect.x;
      int y = topLeft.y + startRect.y + charHeight;

      ontologyTreePanel.showingAnnotationWindow = true;
      annotationWindow.setLocation(x, y);
      annotationWindow.pack();
      annotationWindow.setVisible(true);
    }
    catch(BadLocationException ble) {
      throw new GateRuntimeException("Can't show the window ", ble);
    }
  }

  private ArrayList<ClassNode> getClassesAndInstances(IFolder rootNode,
          String startWith) {

    ArrayList<ClassNode> toReturn = new ArrayList<ClassNode>();

    if(rootNode instanceof ClassNode
            && ((ClassNode)rootNode).getSource() instanceof OResource) {
      if(!ontologyTreePanel.ontologyViewerOptions.isFilterOn()
              || !ontologyTreePanel.ontologyViewerOptions.ontologyClassesToFilterOut
                      .contains(((OResource)((ClassNode)rootNode).getSource())
                              .getName())) {

        if(startWith.length() > 0) {
          if(((OResource)((ClassNode)rootNode).getSource()).getName()
                  .toLowerCase().startsWith(startWith)) {
            toReturn.add((ClassNode)rootNode);
          }
        }
        else {
          toReturn.add((ClassNode)rootNode);
        }
      }
    }

    // we also need to obtain all its children and iterate through all
    // of them
    Iterator childrenIterator = rootNode.getChildren();
    while(childrenIterator.hasNext()) {
      ClassNode aNode = (ClassNode)childrenIterator.next();
      toReturn.addAll(getClassesAndInstances(aNode, startWith));
    }
    return toReturn;
  }

  // what to do when user selects to remove the annotation
  protected class DeleteAnnotationAction extends AbstractAction {

    public DeleteAnnotationAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent e) {
      try {
        if(ontologyTreePanel.ontologyViewerOptions.getDeleteConfirmation()) {
          Object[] options = new Object[] {"YES", "NO"};
          int confirm = JOptionPane.showOptionDialog(Main.getMainFrame(),
                  "Delete Annotation : Are you sure?",
                  "Delete Annotation Confirmation",
                  JOptionPane.YES_NO_CANCEL_OPTION,
                  JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
          if(confirm == JOptionPane.YES_OPTION) {
            gate.Annotation annot = (gate.Annotation)ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                    .get(selectedAnnotationIndex);

            if(annot != null) {
              if(applyToAll.isSelected()) {
                ArrayList<Annotation> annotations = getSimilarAnnotations(annot);
                for(int i = 0; i < annotations.size(); i++) {
                  ontologyTreePanel.deleteAnnotation(annotations.get(i));
                }
              }
              else {
                ontologyTreePanel.deleteAnnotation(annot);
              }
            }
          }
          hideWindow();
        }
        else {
          gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                  .get(selectedAnnotationIndex);

          if(annot != null) {
            if(applyToAll.isSelected()) {
              ArrayList<Annotation> annotations = getSimilarAnnotations(annot);
              for(int i = 0; i < annotations.size(); i++) {
                ontologyTreePanel.deleteAnnotation(annotations.get(i));
              }
            }
            else {
              ontologyTreePanel.deleteAnnotation(annot);
            }
          }
          hideWindow();
        }
      }
      catch(Exception e1) {
        e1.printStackTrace();
      }
    }
  }

  // extend the annotation by one character on left
  protected class StartOffsetExtendLeftAction extends AbstractAction {

    public StartOffsetExtendLeftAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent e) {
      try {
        gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(selectedAnnotationIndex);
        int startOffset = annot.getStartNode().getOffset().intValue();
        int endOffset = annot.getEndNode().getOffset().intValue();
        FeatureMap features = annot.getFeatures();
        if(startOffset == 0) return;
        startOffset--;
        String value = (String)features
                .get(ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME);
        boolean isClassFeature = true;
        if(value == null) {
          isClassFeature = false;
          value = (String)features
                  .get(ANNIEConstants.LOOKUP_INSTANCE_FEATURE_NAME);
        }

        ontologyTreePanel.deleteAnnotation(annot);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionStart(startOffset);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionEnd(endOffset);
        Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
                .addNewAnnotation(ontologyTreePanel.getNode(value), false,
                        features, isClassFeature, false).get(0);
        selectedAnnotationIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .indexOf(addedAnnotation);
      }
      catch(Exception e1) {
        throw new GateRuntimeException(e1);
      }
    }
  }

  // extend the annotation by one character on left
  protected class StartOffsetExtendRightAction extends AbstractAction {
    public StartOffsetExtendRightAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent e) {

      gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
              .get(selectedAnnotationIndex);
      int startOffset = annot.getStartNode().getOffset().intValue();
      int endOffset = annot.getEndNode().getOffset().intValue();
      FeatureMap features = annot.getFeatures();
      startOffset++;
      if(startOffset == endOffset) return;
      String value = (String)features
              .get(ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME);
      boolean isClassFeature = true;
      if(value == null) {
        isClassFeature = false;
        value = (String)features
                .get(ANNIEConstants.LOOKUP_INSTANCE_FEATURE_NAME);
      }

      ontologyTreePanel.deleteAnnotation(annot);
      ontologyTreePanel.ontoViewer.documentTextArea
              .setSelectionStart(startOffset);
      ontologyTreePanel.ontoViewer.documentTextArea.setSelectionEnd(endOffset);
      Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
              .addNewAnnotation(ontologyTreePanel.getNode(value), false,
                      features, isClassFeature, false).get(0);
      selectedAnnotationIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
              .indexOf(addedAnnotation);
    }
  }

  // extend the annotation by one character on left
  protected class EndOffsetExtendLeftAction extends AbstractAction {

    public EndOffsetExtendLeftAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent e) {
      try {
        gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(selectedAnnotationIndex);
        int startOffset = annot.getStartNode().getOffset().intValue();
        int endOffset = annot.getEndNode().getOffset().intValue();
        FeatureMap features = annot.getFeatures();
        endOffset--;
        if(endOffset == startOffset) return;
        String value = (String)features
                .get(ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME);
        boolean isClassFeature = true;
        if(value == null) {
          isClassFeature = false;
          value = (String)features
                  .get(ANNIEConstants.LOOKUP_INSTANCE_FEATURE_NAME);
        }

        ontologyTreePanel.deleteAnnotation(annot);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionStart(startOffset);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionEnd(endOffset);
        Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
                .addNewAnnotation(ontologyTreePanel.getNode(value), false,
                        features, isClassFeature, false).get(0);
        selectedAnnotationIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .indexOf(addedAnnotation);
      }
      catch(Exception e1) {
        throw new GateRuntimeException(e1);
      }
    }
  }

  // extend the annotation by one character on left
  protected class EndOffsetExtendRightAction extends AbstractAction {
    public EndOffsetExtendRightAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent e) {
      gate.Annotation annot = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
              .get(selectedAnnotationIndex);
      int startOffset = annot.getStartNode().getOffset().intValue();
      int endOffset = annot.getEndNode().getOffset().intValue();
      FeatureMap features = annot.getFeatures();
      if(ontologyTreePanel.ontoViewer.getDocument().getContent().size()
              .longValue() == endOffset) return;
      endOffset++;
      String value = (String)features
              .get(ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME);
      boolean isClassFeature = true;
      if(value == null) {
        isClassFeature = false;
        value = (String)features
                .get(ANNIEConstants.LOOKUP_INSTANCE_FEATURE_NAME);
      }

      ontologyTreePanel.deleteAnnotation(annot);
      ontologyTreePanel.ontoViewer.documentTextArea
              .setSelectionStart(startOffset);
      ontologyTreePanel.ontoViewer.documentTextArea.setSelectionEnd(endOffset);
      Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
              .addNewAnnotation(ontologyTreePanel.getNode(value), false,
                      features, isClassFeature, false).get(0);
      selectedAnnotationIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
              .indexOf(addedAnnotation);
    }
  }

  protected class CancelAction extends AbstractAction {
    public CancelAction(Icon icon) {
      super("", icon);
    }

    public void actionPerformed(ActionEvent ae) {
      annotationWindow.setVisible(false);
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
      ontologyTreePanel.ontoViewer.documentTextArea
              .setSelectionStart(ontologyTreePanel.ontoViewer.documentTextArea
                      .getSelectionStart());
      ontologyTreePanel.ontoViewer.documentTextArea
              .setSelectionEnd(ontologyTreePanel.ontoViewer.documentTextArea
                      .getSelectionStart());
      ontologyTreePanel.ontoViewer.documentTextArea.requestFocus();
    }
  }

  protected class AddChangeAnnotationAction implements ActionListener {
    public void actionPerformed(ActionEvent ie) {
      // if(ie.getStateChange() != 1) return;
      if(explicitCall) return;
      if(newAnnotationMode) {
        Object selectedItem = typeCombo.getSelectedItem();
        ClassNode item = null;
        if(selectedItem instanceof String) {
          item = ontologyTreePanel.getNode((String)selectedItem);
        }
        else {
          item = (ClassNode)selectedItem;
        }

        if(item == null) {
          JOptionPane.showMessageDialog(MainFrame.getInstance(),
                  "No resource found with value : " + selectedItem.toString());
          newAnnotationMode = false;
          annotationWindow.setVisible(false);
          return;
        }
        else if(ontologyTreePanel.ontologyViewerOptions.isFilterOn()
                && ontologyTreePanel.ontologyViewerOptions.ontologyClassesToFilterOut
                        .contains(((OResource)item.getSource()).getName())) {
          // JOptionPane.showMessageDialog(MainFrame.getInstance(),
          // "This resource has been filtered out : "
          // + selectedItem.toString());
          newAnnotationMode = false;
          annotationWindow.setVisible(false);
          return;
        }

        boolean isClassAnnotation = item.getSource() instanceof OClass;
        boolean shouldCreateInstance = isClassAnnotation ? (createInstance
                .isSelected() ? true : false) : false;
        Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
                .addNewAnnotation(item, applyToAll.isSelected(), null,
                        isClassAnnotation, shouldCreateInstance).get(0);

        selectedAnnotationIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .indexOf(addedAnnotation);

        newAnnotationMode = false;
        annotationWindow.setVisible(false);
        return;
      }
      else {

        Object selectedItem = typeCombo.getSelectedItem();
        ClassNode item = null;
        if(selectedItem instanceof String) {
          item = ontologyTreePanel.getNode((String)selectedItem);
        }
        else {
          item = (ClassNode)selectedItem;
        }

        if(item == null) {
          JOptionPane.showMessageDialog(MainFrame.getInstance(),
                  "No resource found with value : " + selectedItem.toString());
          newAnnotationMode = false;
          annotationWindow.setVisible(false);
          return;
        }
        else if(ontologyTreePanel.ontologyViewerOptions.isFilterOn()
                && ontologyTreePanel.ontologyViewerOptions.ontologyClassesToFilterOut
                        .contains(((OResource)item.getSource()).getName())) {
          // JOptionPane.showMessageDialog(MainFrame.getInstance(),
          // "This resource has been filtered out : "
          // + selectedItem.toString());
          newAnnotationMode = false;
          annotationWindow.setVisible(false);
          return;
        }

        gate.Annotation annot1 = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                .get(selectedAnnotationIndex);
        int cStartOffset = annot1.getStartNode().getOffset().intValue();
        int cEndOffset = annot1.getEndNode().getOffset().intValue();

        ArrayList<Annotation> annotations = new ArrayList<Annotation>();
        if(applyToAll.isSelected()) {
          annotations = getSimilarAnnotations(annot1);
        }
        else {
          annotations.add(annot1);
        }

        for(int i = 0; i < annotations.size(); i++) {

          boolean updateIndex = false;
          Annotation annot = annotations.get(i);

          if(annot == annot1) {
            updateIndex = true;
          }

          int startOffset = annot.getStartNode().getOffset().intValue();
          int endOffset = annot.getEndNode().getOffset().intValue();

          ontologyTreePanel.deleteAnnotation(annot);

          FeatureMap features = annot.getFeatures();
          String value = (String)features
                  .get(ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME);
          boolean isClassFeature = true;
          if(value == null) {
            isClassFeature = false;
          }

          ontologyTreePanel.ontoViewer.documentTextArea
                  .setSelectionStart(startOffset);
          ontologyTreePanel.ontoViewer.documentTextArea
                  .setSelectionEnd(endOffset);
          boolean isClassAnnotation = item.getSource() instanceof OClass;
          boolean shouldCreateInstance = isClassAnnotation ? (createInstance
                  .isSelected() ? true : false) : false;
          Annotation addedAnnotation = ontologyTreePanel.ontoTreeListener
                  .addNewAnnotation(item, false, features, isClassAnnotation,
                          shouldCreateInstance).get(0);

          if(updateIndex) {
            selectedAnnotationIndex = ontologyTreePanel.ontoTreeListener.highlightedAnnotations
                    .indexOf(addedAnnotation);
            updateIndex = false;
          }
        }
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionStart(cStartOffset);
        ontologyTreePanel.ontoViewer.documentTextArea
                .setSelectionEnd(cEndOffset);
      }
    }
  }

  public void setTextLocation(int textLocation) {
    this.textLocation = textLocation;
  }

  /**
   * Sets the mouse point
   * 
   * @param point
   */
  public void setMousePoint(Point point) {
    this.mousePoint = point;
  }

  public void hideWindow() {
    if(annotationWindow != null) annotationWindow.setVisible(false);
    ontologyTreePanel.showingAnnotationWindow = false;
  }

  /**
   * Given the annotation, this method returns the annotation with same
   * text and same class feature.
   * 
   * @param annot
   * @return
   */
  private ArrayList<Annotation> getSimilarAnnotations(gate.Annotation annot) {
    ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    String classValue = Utils.getClassFeatureValue(annot);
    if(classValue == null) classValue = Utils.getInstanceFeatureValue(annot);
    String annotString = getString(annot);
    ArrayList<Annotation> highlightedAnnotations = ontologyTreePanel.ontoTreeListener.highlightedAnnotations;
    for(int i = 0; i < highlightedAnnotations.size(); i++) {
      gate.Annotation temp = highlightedAnnotations.get(i);
      String tempClass = Utils.getClassFeatureValue(temp);
      if(tempClass == null) tempClass = Utils.getInstanceFeatureValue(temp);
      String tempString = getString(temp);
      if(ontologyTreePanel.ontologyViewerOptions.isAddAllOptionCaseSensitive()) {
        if(classValue.equals(tempClass) && annotString.equals(tempString)) {
          annotations.add(temp);
        }
      }
      else {
        if(classValue.equalsIgnoreCase(tempClass)
                && annotString.equalsIgnoreCase(tempString)) {
          annotations.add(temp);
        }
      }
    }
    return annotations;
  }

  /**
   * Retrieves the underlying text of the annotation.
   * 
   * @param annot
   * @return
   */
  private String getString(gate.Annotation annot) {
    return ontologyTreePanel.ontoViewer.getDocument().getContent().toString()
            .substring(annot.getStartNode().getOffset().intValue(),
                    annot.getEndNode().getOffset().intValue());
  }

}
