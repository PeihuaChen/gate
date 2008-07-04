package gate.alignment.gui;

import java.util.*;
import java.util.List;
import java.util.Timer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import gate.*;
import gate.alignment.*;
import gate.alignment.gui.actions.impl.AlignAction;
import gate.alignment.gui.actions.impl.RemoveAlignmentAction;
import gate.alignment.gui.actions.impl.ResetAction;
import gate.compound.CompoundDocument;
import gate.creole.*;
import gate.gui.MainFrame;
import gate.swing.ColorGenerator;
import gate.util.GateException;
import gate.util.GateRuntimeException;

/**
 * This class is the extension of the GATE Document viewer/editor. It
 * provides a editor for aligning texts in a compound document.
 */
public class AlignmentEditor extends AbstractVisualResource implements
                                                           ActionListener,
                                                           AlignmentListener {

  private static final long serialVersionUID = -2867467022258265114L;

  private static final String ACTIONS_CONFIG_FILE = "actions.conf";

  private JPanel mainPanel, paramPanel, waPanel;

  private JPanel propertiesPanel;

  private JComboBox sourceDocumentId;

  private JComboBox targetDocumentId;

  private JComboBox sourceASName;

  private JComboBox targetASName;

  private JComboBox sourceUnitOfAlignment;

  private JComboBox targetUnitOfAlignment;

  private JComboBox sourceParentOfUnitOfAlignment;

  private JComboBox targetParentOfUnitOfAlignment;

  private JComboBox alignmentFeatureNames;

  private JCheckBox showLinks;

  private JLabel alignmentComplete = new JLabel("false");

  private JButton next, previous, loadActions;

  private JToggleButton populate;

  private JPanel sourcePanel;

  private JPanel targetPanel;

  private MappingsPanel linesCanvas;

  private CompoundDocument document;

  private AlignmentFactory alignFactory;

  private Alignment alignment;

  private HashMap<Annotation, AnnotationHighlight> sourceHighlights;

  private HashMap<Annotation, AnnotationHighlight> targetHighlights;

  private List<Annotation> sourceLatestAnnotationsSelection;

  private List<Annotation> targetLatestAnnotationsSelection;

  private List<AlignmentAction> allActions;

  private Color color;

  private ColorGenerator colorGenerator = new ColorGenerator();

  public static final int TEXT_SIZE = 20;

  private Map<JMenuItem, AlignmentAction> actions;

  private Map<String, JMenuItem> actionsMenuItemByCaption;

  private AnnotationHighlight currentAnnotationHightlight = null;

  private AlignmentEditor thisInstance = null;

  private AlignAction alignAction = null;

  private HashMap<AlignmentAction, PropertyActionCB> actionsCBMap = null;

  private RemoveAlignmentAction removeAlignmentAction = null;

  private List<PreDisplayAction> preDisplayActions = null;

  private List<FinishedAlignmentAction> finishedAlignmentActions = null;

  /*
   * (non-Javadoc)
   * 
   * @see gate.Resource#init()
   */
  public Resource init() throws ResourceInstantiationException {
    sourceHighlights = new HashMap<Annotation, AnnotationHighlight>();
    targetHighlights = new HashMap<Annotation, AnnotationHighlight>();
    actionsCBMap = new HashMap<AlignmentAction, PropertyActionCB>();
    sourceLatestAnnotationsSelection = new ArrayList<Annotation>();
    targetLatestAnnotationsSelection = new ArrayList<Annotation>();

    actions = new HashMap<JMenuItem, AlignmentAction>();
    actionsMenuItemByCaption = new HashMap<String, JMenuItem>();
    allActions = new ArrayList<AlignmentAction>();
    preDisplayActions = new ArrayList<PreDisplayAction>();
    finishedAlignmentActions = new ArrayList<FinishedAlignmentAction>();

    ResourceData myResourceData = (ResourceData)Gate.getCreoleRegister().get(
            this.getClass().getName());
    URL creoleXml = myResourceData.getXmlFileUrl();
    URL alignmentHomeURL = null;
    File actionsConfFile = null;
    try {
      alignmentHomeURL = new URL(creoleXml, ".");
      actionsConfFile = new File(new File(new File(alignmentHomeURL.toURI()),
              "resources"), ACTIONS_CONFIG_FILE);
    }
    catch(MalformedURLException mue) {
      throw new GateRuntimeException(mue);
    }
    catch(URISyntaxException use) {
      throw new GateRuntimeException(use);
    }

    readAction(new ResetAction());
    alignAction = new AlignAction();
    readAction(alignAction);
    removeAlignmentAction = new RemoveAlignmentAction();
    readAction(removeAlignmentAction);
    readActions(actionsConfFile);
    thisInstance = this;
    return this;
  }

  /**
   * Initialize the GUI
   */
  private void initGui() {
    mainPanel = new JPanel(new BorderLayout());
    paramPanel = new JPanel(new GridLayout(3, 1));

    waPanel = new JPanel(new BorderLayout());

    sourceDocumentId = new JComboBox(new DefaultComboBoxModel());
    sourceDocumentId.setEditable(false);

    targetDocumentId = new JComboBox(new DefaultComboBoxModel());
    targetDocumentId.setEditable(false);

    sourceDocumentId.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ie) {
        populateAS((String)sourceDocumentId.getSelectedItem(), sourceASName);
      }
    });

    targetDocumentId.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ie) {
        populateAS((String)targetDocumentId.getSelectedItem(), targetASName);
      }
    });

    sourceASName = new JComboBox(new DefaultComboBoxModel());
    sourceASName.setPrototypeDisplayValue("AnnotationSetName");
    sourceASName.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        populateParentOfUnitOfAlignment((String)sourceDocumentId
                .getSelectedItem(), sourceParentOfUnitOfAlignment);
      }
    });

    targetASName = new JComboBox(new DefaultComboBoxModel());
    targetASName.setPrototypeDisplayValue("AnnotationSetName");
    targetASName.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        populateParentOfUnitOfAlignment((String)targetDocumentId
                .getSelectedItem(), targetParentOfUnitOfAlignment);
      }
    });

    sourceParentOfUnitOfAlignment = new JComboBox(new DefaultComboBoxModel());
    sourceParentOfUnitOfAlignment.setPrototypeDisplayValue("AnnotationSetName");
    sourceParentOfUnitOfAlignment.setEditable(false);
    sourceParentOfUnitOfAlignment.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        populateUnitOfAlignment((String)sourceDocumentId.getSelectedItem(),
                sourceUnitOfAlignment);
      }
    });

    targetParentOfUnitOfAlignment = new JComboBox(new DefaultComboBoxModel());
    targetParentOfUnitOfAlignment.setPrototypeDisplayValue("AnnotationSetName");
    targetParentOfUnitOfAlignment.setEditable(false);
    targetParentOfUnitOfAlignment.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        populateUnitOfAlignment((String)targetDocumentId.getSelectedItem(),
                targetUnitOfAlignment);
      }
    });

    sourceUnitOfAlignment = new JComboBox(new DefaultComboBoxModel());
    sourceUnitOfAlignment.setPrototypeDisplayValue("AnnotationSetName");
    sourceUnitOfAlignment.setEditable(false);

    targetUnitOfAlignment = new JComboBox(new DefaultComboBoxModel());
    targetUnitOfAlignment.setPrototypeDisplayValue("AnnotationSetName");
    targetUnitOfAlignment.setEditable(false);

    alignmentFeatureNames = new JComboBox(new DefaultComboBoxModel());
    ((DefaultComboBoxModel)alignmentFeatureNames.getModel())
            .addElement(AlignmentFactory.ALIGNMENT_FEATURE_NAME);
    alignmentFeatureNames.setPrototypeDisplayValue("AnnotationSetName");

    JPanel temp1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel temp2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel temp3 = new JPanel(new FlowLayout(FlowLayout.LEFT));

    temp1.add(new JLabel("sourceDoc:"));
    temp1.add(sourceDocumentId);
    temp1.add(new JLabel("annotationSet:"));
    temp1.add(sourceASName);
    temp1.add(new JLabel("parentOfAlignmentUnit:"));
    temp1.add(sourceParentOfUnitOfAlignment);
    temp1.add(new JLabel("unitOfAlignment:"));
    temp1.add(sourceUnitOfAlignment);

    temp2.add(new JLabel("targetDoc:"));
    temp2.add(targetDocumentId);
    temp2.add(new JLabel("annotationSet:"));
    temp2.add(targetASName);
    temp2.add(new JLabel("parentOfAlignmentUnit:"));
    temp2.add(targetParentOfUnitOfAlignment);
    temp2.add(new JLabel("unitOfAlignment:"));
    temp2.add(targetUnitOfAlignment);

    alignmentFeatureNames
            .setSelectedItem(AlignmentFactory.ALIGNMENT_FEATURE_NAME);
    alignmentFeatureNames.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        populateAlignmentFeatureNames();
      }
    });

    populate = new JToggleButton("Populate");
    populate.addActionListener(this);

    previous = new JButton("< Previous");
    previous.addActionListener(this);

    next = new JButton("Next >");
    next.addActionListener(this);

    showLinks = new JCheckBox("Links");
    showLinks.setSelected(true);
    showLinks.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if(linesCanvas != null) {
          if(showLinks.isSelected()) {
            waPanel.add(linesCanvas, BorderLayout.CENTER);
          }
          else {
            waPanel.remove(linesCanvas);
          }
          waPanel.revalidate();
          waPanel.updateUI();
        }
      }
    });

    loadActions = new JButton("Load Actions");
    loadActions.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          JFileChooser fileChooser = Main.getMainFrame().getFileChooser();
          int answer = fileChooser.showOpenDialog(MainFrame.getInstance());
          if(answer == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if(selectedFile == null) {
              return;
            }
            else {
              readActions(selectedFile);
            }
          }
        }
        catch(GateException ge) {
          throw new GateRuntimeException(ge);
        }
      }
    });

    temp3.add(new JLabel("alignmentFeatureName:"));
    temp3.add(alignmentFeatureNames);
    temp3.add(populate);
    temp3.add(previous);
    temp3.add(next);
    temp3.add(showLinks);
    temp3.add(new JLabel("Status:"));
    temp3.add(alignmentComplete);
    temp3.add(loadActions);

    paramPanel.add(temp1);
    paramPanel.add(temp2);
    paramPanel.add(temp3);

    temp1.setBorder(new TitledBorder("Source Document"));
    temp2.setBorder(new TitledBorder("Target Document"));
    temp3.setBorder(new TitledBorder("Alignment"));

    mainPanel.add(new JScrollPane(paramPanel), BorderLayout.NORTH);

    sourcePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    sourcePanel.setBackground(Color.WHITE);
    targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    targetPanel.setBackground(Color.WHITE);
    linesCanvas = new MappingsPanel();
    linesCanvas.setBackground(Color.WHITE);
    linesCanvas.setLayout(null);
    linesCanvas.setPreferredSize(new Dimension(200, 50));
    linesCanvas.setOpaque(true);

    propertiesPanel = new JPanel();
    propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
    JScrollPane pane = new JScrollPane(propertiesPanel);
    propertiesPanel.add(new JLabel("Options"));
    propertiesPanel.add(Box.createGlue());

    waPanel.add(sourcePanel, BorderLayout.NORTH);
    waPanel.add(targetPanel, BorderLayout.SOUTH);
    waPanel.add(linesCanvas, BorderLayout.CENTER);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    JPanel waParentPanel = new JPanel(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(waPanel);
    scrollPane.setPreferredSize(new Dimension(800, 200));

    waParentPanel.add(scrollPane, BorderLayout.CENTER);
    splitPane.add(waParentPanel);
    splitPane.add(pane);
    mainPanel.add(splitPane, BorderLayout.CENTER);
    this.setLayout(new BorderLayout());
    this.add(mainPanel, BorderLayout.CENTER);
    color = getColor(null);
    splitPane.setDividerLocation(0.8);
    splitPane.revalidate();
    splitPane.updateUI();
    waPanel.setVisible(false);
  }

  public String getSourceParentOfUnitOfAlignment() {
    return sourceParentOfUnitOfAlignment.getSelectedItem().toString();
  }

  public String getTargetParentOfUnitOfAlignment() {
    return targetParentOfUnitOfAlignment.getSelectedItem().toString();
  }

  public String getSourceUnitOfAlignment() {
    return sourceUnitOfAlignment.getSelectedItem().toString();
  }

  public String getTargetUnitOfAlignment() {
    return targetUnitOfAlignment.getSelectedItem().toString();
  }

  public String getSourceDocumentId() {
    return sourceDocumentId.getSelectedItem().toString();
  }

  public String getTargetDocumentId() {
    return targetDocumentId.getSelectedItem().toString();
  }

  public String getSourceAnnotationSetName() {
    String as = sourceASName.getSelectedItem().toString();
    return as.equals("<null>") ? null : as;
  }

  public String getTargetAnnotationSetName() {
    String as = targetASName.getSelectedItem().toString();
    return as.equals("<null>") ? null : as;
  }

  /**
   * populates the annotation set combobox
   * 
   * @param documentID
   * @param boxToPopulate
   */
  private void populateAS(String documentID, JComboBox boxToPopulate) {
    Document doc = document.getDocument(documentID);
    Map<String, AnnotationSet> annotSets = doc.getNamedAnnotationSets();
    if(annotSets == null) {
      annotSets = new HashMap<String, AnnotationSet>();
    }

    HashSet<String> setNames = new HashSet<String>(annotSets.keySet());
    setNames.add("<null>");
    DefaultComboBoxModel dcbm = new DefaultComboBoxModel(setNames
            .toArray(new String[0]));
    boxToPopulate.setModel(dcbm);
    if(!setNames.isEmpty()) {
      boxToPopulate.setSelectedIndex(0);
    }
  }

  /**
   * populates the documentIds combobox
   * 
   * @param documentID
   * @param boxToPopulate
   */
  private void populateDocumentIds(JComboBox boxToPopulate, String[] documentIds) {
    if(documentIds == null) documentIds = new String[0];
    DefaultComboBoxModel dcbm = new DefaultComboBoxModel(documentIds);
    boxToPopulate.setModel(dcbm);
    if(documentIds.length == 0) {
      boxToPopulate.setEnabled(false);
    }
  }

  /**
   * populates the documentIds combobox
   * 
   * @param documentID
   * @param boxToPopulate
   */
  private void populateAlignmentFeatureNames() {
    this.document
            .getAlignmentInformation(AlignmentFactory.ALIGNMENT_FEATURE_NAME);
    Set<String> alignmentFeatureNames = this.document
            .getAllAlignmentFeatureNames();
    DefaultComboBoxModel dcbm = new DefaultComboBoxModel(alignmentFeatureNames
            .toArray(new String[0]));
    Object selectedItem = this.alignmentFeatureNames.getSelectedItem();
    this.alignmentFeatureNames.setModel(dcbm);
    if(selectedItem != null) {
      this.alignmentFeatureNames.setSelectedItem(selectedItem);
    }

    selectedItem = this.alignmentFeatureNames.getSelectedItem();
    alignment = this.document.getAlignmentInformation((String)selectedItem);
    if(alignment != null) {
      alignment.removeAlignmentListener(thisInstance);
      alignment.addAlignmentListener(thisInstance);
      refresh();
    }
  }

  /**
   * populates the annotation set combobox
   * 
   * @param documentID
   * @param boxToPopulate
   */
  private void populateParentOfUnitOfAlignment(String documentID,
          JComboBox boxToPopulate) {
    Document doc = document.getDocument(documentID);
    String asName = null;
    if(boxToPopulate == sourceParentOfUnitOfAlignment) {
      asName = (String)sourceASName.getSelectedItem();
    }
    else {
      asName = (String)targetASName.getSelectedItem();
    }

    AnnotationSet srcAnnotSet = asName.equals("<null>")
            ? doc.getAnnotations()
            : doc.getAnnotations(asName);
    Set<String> annotTypes = srcAnnotSet.getAllTypes();
    if(annotTypes == null) {
      annotTypes = new HashSet<String>();
    }

    DefaultComboBoxModel dcbm = new DefaultComboBoxModel(annotTypes
            .toArray(new String[0]));
    boxToPopulate.setModel(dcbm);
    if(!annotTypes.isEmpty()) {
      if(dcbm.getIndexOf("Sentence") >= 0) {
        boxToPopulate.setSelectedIndex(dcbm.getIndexOf("Sentence"));
      }
      else {
        boxToPopulate.setSelectedIndex(0);
      }
    }
  }

  /**
   * populates the annotation set combobox
   * 
   * @param documentID
   * @param boxToPopulate
   */
  private void populateUnitOfAlignment(String documentID,
          JComboBox boxToPopulate) {
    Document doc = document.getDocument(documentID);
    String asName = null;
    if(boxToPopulate == sourceUnitOfAlignment) {
      asName = (String)sourceASName.getSelectedItem();
    }
    else {
      asName = (String)targetASName.getSelectedItem();
    }

    AnnotationSet srcAnnotSet = asName.equals("<null>")
            ? doc.getAnnotations()
            : doc.getAnnotations(asName);
    Set<String> annotTypes = srcAnnotSet.getAllTypes();
    if(annotTypes == null) {
      annotTypes = new HashSet<String>();
    }

    DefaultComboBoxModel dcbm = new DefaultComboBoxModel(annotTypes
            .toArray(new String[0]));
    boxToPopulate.setModel(dcbm);
    if(annotTypes.size() > 0) {
      if(dcbm.getIndexOf("Token") >= 0) {
        boxToPopulate.setSelectedIndex(dcbm.getIndexOf("Token"));
      }
      else {
        boxToPopulate.setSelectedIndex(0);
      }

    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.VisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    this.document = (CompoundDocument)target;
    thisInstance = this;
    List<String> documentIDs = new ArrayList<String>(this.document
            .getDocumentIDs());
    initGui();
    populateDocumentIds(sourceDocumentId, documentIDs.toArray(new String[0]));
    if(documentIDs.size() > 0) {
      sourceDocumentId.setSelectedIndex(0);
    }

    populateDocumentIds(targetDocumentId, documentIDs.toArray(new String[0]));
    if(documentIDs.size() > 1) {
      targetDocumentId.setSelectedIndex(1);
    }
    else {
      targetDocumentId.setSelectedIndex(0);
    }

    populateAlignmentFeatureNames();
  }

  /**
   * This method clears up the latest annotation selection
   */
  public void clearLatestAnnotationsSelection() {

    if(sourceLatestAnnotationsSelection != null
            && !sourceLatestAnnotationsSelection.isEmpty()) {

      for(Annotation annotation : sourceLatestAnnotationsSelection) {
        AnnotationHighlight ah = sourceHighlights.get(annotation);
        ah.setHighlighted(false, Color.WHITE);
      }
      sourceLatestAnnotationsSelection.clear();

    }

    if(targetLatestAnnotationsSelection != null
            && !targetLatestAnnotationsSelection.isEmpty()) {

      for(Annotation annotation : targetLatestAnnotationsSelection) {
        AnnotationHighlight ah = targetHighlights.get(annotation);
        ah.setHighlighted(false, Color.WHITE);
      }
      targetLatestAnnotationsSelection.clear();
    }

  }

  protected void executeAction(AlignmentAction aa) {

    Document srcDocument = document.getDocument(sourceDocumentId
            .getSelectedItem().toString());
    Document tgtDocument = document.getDocument(targetDocumentId
            .getSelectedItem().toString());

    Set<Annotation> srcSelectedAnnots = new HashSet<Annotation>(
            sourceLatestAnnotationsSelection);
    Set<Annotation> tgtSelectedAnnots = new HashSet<Annotation>(
            targetLatestAnnotationsSelection);

    if(currentAnnotationHightlight != null) {

      Set<Annotation> alignedAnnots = alignment
              .getAlignedAnnotations(currentAnnotationHightlight.annotation);
      if(alignedAnnots == null) alignedAnnots = new HashSet<Annotation>();
      alignedAnnots.add(currentAnnotationHightlight.annotation);

      for(Annotation annot : alignedAnnots) {
        Document tempDoc = alignment.getDocument(annot);
        if(tempDoc == srcDocument) {
          srcSelectedAnnots.add(annot);
        }
        else if(tempDoc == tgtDocument) {
          tgtSelectedAnnots.add(annot);
        }
      }
    }

    try {
      color = Color.WHITE;
      aa.execute(this, this.document, srcDocument,
              getSourceAnnotationSetName(), srcSelectedAnnots, tgtDocument,
              getTargetAnnotationSetName(), tgtSelectedAnnots,
              currentAnnotationHightlight.annotation);

      if(aa == alignAction) {
        for(AlignmentAction a : allActions) {
          if(a.invokeWithAlignAction()) {
            JCheckBox cb = actionsCBMap.get(a);
            if((cb != null && cb.isSelected()) || cb == null)
              a
                      .execute(this, this.document, srcDocument,
                              getSourceAnnotationSetName(), srcSelectedAnnots,
                              tgtDocument, getTargetAnnotationSetName(),
                              tgtSelectedAnnots,
                              currentAnnotationHightlight.annotation);

          }

        }
      }
      else if(aa == removeAlignmentAction) {
        for(AlignmentAction a : allActions) {
          if(a.invokeWithRemoveAction()) {
            JCheckBox cb = actionsCBMap.get(a);
            if((cb != null && cb.isSelected()) || cb == null)

              a
                      .execute(this, this.document, srcDocument,
                              getSourceAnnotationSetName(), srcSelectedAnnots,
                              tgtDocument, getTargetAnnotationSetName(),
                              tgtSelectedAnnots,
                              currentAnnotationHightlight.annotation);
          }
        }
      }
    }
    catch(AlignmentException ae) {
      throw new GateRuntimeException(ae);
    }
  }

  /**
   * Get the alignment feature name
   * 
   * @return
   */
  public String getAlignmentFeatureName() {
    return this.alignmentFeatureNames.getSelectedItem().toString();
  }

  public void populate() {
    if(!populate.isSelected()) {
      if(!disableUserSelections) {
        // we need to disable the alignment gui
        sourceUnitOfAlignment.setEnabled(true);
        targetUnitOfAlignment.setEnabled(true);
        sourceParentOfUnitOfAlignment.setEnabled(true);
        targetParentOfUnitOfAlignment.setEnabled(true);
        sourceASName.setEnabled(true);
        targetASName.setEnabled(true);
        sourceDocumentId.setEnabled(true);
        targetDocumentId.setEnabled(true);
        next.setEnabled(false);
        previous.setEnabled(false);
        showLinks.setEnabled(false);
        waPanel.setVisible(false);
      }
    }
    else {
      if(!disableUserSelections) {
        sourceUnitOfAlignment.setEnabled(false);
        targetUnitOfAlignment.setEnabled(false);
        sourceParentOfUnitOfAlignment.setEnabled(false);
        targetParentOfUnitOfAlignment.setEnabled(false);
        sourceASName.setEnabled(false);
        targetASName.setEnabled(false);
        sourceDocumentId.setEnabled(false);
        targetDocumentId.setEnabled(false);
        next.setEnabled(true);
        previous.setEnabled(true);
        showLinks.setEnabled(true);
        waPanel.setVisible(true);
      }

      try {
        if(sourceUnitOfAlignment.getSelectedItem() == null) return;
        if(targetUnitOfAlignment.getSelectedItem() == null) return;
        if(sourceParentOfUnitOfAlignment.getSelectedItem() == null) return;
        if(targetParentOfUnitOfAlignment.getSelectedItem() == null) return;

        AlignmentFactory af = new AlignmentFactory(document, sourceDocumentId
                .getSelectedItem().toString(), targetDocumentId
                .getSelectedItem().toString(), sourceASName.getSelectedItem()
                .toString(), targetASName.getSelectedItem().toString(),
                sourceUnitOfAlignment.getSelectedItem().toString(),
                targetUnitOfAlignment.getSelectedItem().toString(),
                sourceParentOfUnitOfAlignment.getSelectedItem().toString(),
                targetParentOfUnitOfAlignment.getSelectedItem().toString(),
                "gate.util.OffsetComparator");

        // if there were no errors
        alignFactory = af;
        nextAction();
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }

  }

  public void actionPerformed(ActionEvent ae) {
    if(ae.getSource() == populate) {
      populate();
    }
    else if(ae.getSource() == next) {
      if(alignFactory != null && !alignFactory.isCompleted()) {
        int answer = JOptionPane.showConfirmDialog(mainPanel,
                "Is alignment complete for this pair?");
        if(answer == JOptionPane.YES_OPTION) {
          alignFactory.setCompleted(true);
          callFinishedAlignmentActions();

        }
        else {
          alignFactory.setCompleted(false);
        }
      }
      nextAction();
    }
    else if(ae.getSource() == previous) {
      if(alignFactory != null && !alignFactory.isCompleted()) {
        int answer = JOptionPane.showConfirmDialog(mainPanel,
                "Is alignment complete for this pair?");
        if(answer == JOptionPane.YES_OPTION) {
          alignFactory.setCompleted(true);
          callFinishedAlignmentActions();
        }
        else {
          alignFactory.setCompleted(false);
        }
      }
      previousAction();
    }
  }

  private void callFinishedAlignmentActions() {

    HashMap<String, Annotation> docIDsAndAnnots = alignFactory.current();
    Set<Annotation> srcAnnotations = null;
    Set<Annotation> tgtAnnotations = null;
    Document srcDocument = null;
    Document tgtDocument = null;

    for(String docId : docIDsAndAnnots.keySet()) {

      boolean isSourceDocument = true;
      if(docId.equals(targetDocumentId.getSelectedItem().toString())) {
        isSourceDocument = false;
        tgtDocument = document.getDocument(docId);
      }
      else {
        srcDocument = document.getDocument(docId);
      }

      // sentence annotation
      Annotation annot = docIDsAndAnnots.get(docId);
      if(isSourceDocument) {
        srcAnnotations = alignFactory.getUnderlyingAnnotations(annot, docId,
                sourceUnitOfAlignment.getSelectedItem().toString());
      }
      else {
        tgtAnnotations = alignFactory.getUnderlyingAnnotations(annot, docId,
                targetUnitOfAlignment.getSelectedItem().toString());
      }
    }

    for(FinishedAlignmentAction faa : finishedAlignmentActions) {
      try {
        faa.execute(this, document, srcDocument, (String)sourceASName
                .getSelectedItem(), srcAnnotations, tgtDocument,
                (String)targetASName.getSelectedItem(), tgtAnnotations);
      }
      catch(AlignmentException ae) {
        throw new GateRuntimeException(ae);
      }
    }

  }

  private void nextAction() {
    if(alignFactory != null && alignFactory.hasNext()) {

      HashMap<String, Annotation> next = alignFactory.next();
      Annotation srcAnnotation = next.get(alignFactory.getSrcDocumentID());
      Annotation tgtAnnotation = next.get(alignFactory.getTgtDocumentID());
      Document srcDocument = document.getDocument(alignFactory
              .getSrcDocumentID());
      Document tgtDocument = document.getDocument(alignFactory
              .getTgtDocumentID());
      for(PreDisplayAction pda : preDisplayActions) {
        try {
          pda.execute(this, document, srcDocument,
                  getSourceAnnotationSetName(), srcAnnotation, tgtDocument,
                  getTargetAnnotationSetName(), tgtAnnotation);
        }
        catch(AlignmentException ae) {
          ae.printStackTrace();
        }
      }
      updateGUI(next);
    }
  }

  private void refresh() {
    if(alignFactory != null && alignFactory.current() != null) {
      updateGUI(alignFactory.current());
    }
  }

  /**
   * This method updates the GUI.
   * 
   * @param docIDsAndAnnots
   */
  private void updateGUI(HashMap<String, Annotation> docIDsAndAnnots) {
    // before refreshing, we remove all the highlights
    clearLatestAnnotationsSelection();
    alignmentComplete.setText(alignFactory.isCompleted() + "");
    sourcePanel.removeAll();
    sourcePanel.updateUI();

    targetPanel.removeAll();
    targetPanel.updateUI();

    linesCanvas.removeAllEdges();
    linesCanvas.repaint();

    // first we show all the annotations and then highlight each unit
    // using a default highlight color.
    // docIDsAndAnnots has a docId (e.g. en or hi) as key and the
    // parent of the unit of alignment (e.g. Sentence) as the value.
    for(String docId : docIDsAndAnnots.keySet()) {
      JPanel panelToUse = sourcePanel;
      boolean isSourceDocument = true;
      if(docId.equals(targetDocumentId.getSelectedItem().toString())) {
        panelToUse = targetPanel;
        isSourceDocument = false;
      }

      // sentence annotation
      Annotation annot = docIDsAndAnnots.get(docId);

      // we need to highlight the unit type
      AnnotationSet underlyingUnitAnnotationsSet = alignFactory
              .getUnderlyingAnnotations(annot, docId, isSourceDocument
                      ? sourceUnitOfAlignment.getSelectedItem().toString()
                      : targetUnitOfAlignment.getSelectedItem().toString());
      // if there are not underlying annotations, just return
      if(underlyingUnitAnnotationsSet == null) {
        return;
      }

      ArrayList<Annotation> units = new ArrayList<Annotation>(
              underlyingUnitAnnotationsSet);
      Collections.sort(units, new gate.util.OffsetComparator());

      // for each underlying unit of alignment, we create a default
      // annotation highlight.
      HashMap<Annotation, AnnotationHighlight> annotationHighlightsMap = new HashMap<Annotation, AnnotationHighlight>();
      for(Annotation underlyingUnitAnnotation : units) {
        String text = alignFactory.getText(underlyingUnitAnnotation, docId);
        AnnotationHighlight ah = new AnnotationHighlight(text, Color.WHITE,
                underlyingUnitAnnotation, isSourceDocument);
        annotationHighlightsMap.put(underlyingUnitAnnotation, ah);
        panelToUse.add(ah);
      }

      if(isSourceDocument) {
        this.sourceHighlights = annotationHighlightsMap;
      }
      else {
        this.targetHighlights = annotationHighlightsMap;
      }
      panelToUse.revalidate();
      panelToUse.updateUI();
    }

    // now we need to highlight the aligned annotations if there are any
    Set<Annotation> setOfAlignedAnnotations = alignment.getAlignedAnnotations();

    // we keep record of which annotations are already highlighted in
    // order to not highlight them again
    Set<Annotation> highlightedAnnotations = new HashSet<Annotation>();

    // one annotation at a time
    for(Annotation srcAnnotation : setOfAlignedAnnotations) {

      // if already highlighted, don't do it again
      if(highlightedAnnotations.contains(srcAnnotation)) continue;

      // if the annotation doesn't belong to one of the source
      // or target annotations, just skip it
      if(!sourceHighlights.containsKey(srcAnnotation)
              && !targetHighlights.containsKey(srcAnnotation)) {
        continue;
      }

      // find out the language/id of the document
      String docId = alignment.getDocument(srcAnnotation).getName();

      JPanel pane = null;
      boolean isSrcDocument = false;

      if(docId.equals(sourceDocumentId.getSelectedItem().toString())) {
        pane = sourcePanel;
        isSrcDocument = true;
      }
      else if(docId.equals(sourceDocumentId.getSelectedItem().toString())) {
        pane = targetPanel;
        isSrcDocument = false;
      }

      if(pane == null) continue;

      Set<Annotation> sourceAnnots = new HashSet<Annotation>();
      Set<Annotation> targetAnnots = new HashSet<Annotation>();

      if(isSrcDocument) {
        targetAnnots = alignment.getAlignedAnnotations(srcAnnotation);
        for(Annotation tgtAnnot : targetAnnots) {
          sourceAnnots.addAll(alignment.getAlignedAnnotations(tgtAnnot));
        }
      }
      else {
        sourceAnnots = alignment.getAlignedAnnotations(srcAnnotation);
        for(Annotation srcAnnot : sourceAnnots) {
          targetAnnots.addAll(alignment.getAlignedAnnotations(srcAnnot));
        }
      }

      Color newColor = getColor(null);
      boolean firstTime = true;
      for(Annotation srcAnnot : sourceAnnots) {
        AnnotationHighlight sAh = sourceHighlights.get(srcAnnot);
        sAh.setHighlighted(true, newColor);
        for(Annotation tgtAnnot : targetAnnots) {
          AnnotationHighlight ah = targetHighlights.get(tgtAnnot);

          if(firstTime) {
            ah.setHighlighted(true, newColor);
          }

          Edge edge = new Edge();
          edge.srcAH = sAh;
          edge.tgtAH = ah;
          linesCanvas.addEdge(edge);
          linesCanvas.repaint();
        }
        firstTime = false;
      }
    }
  }

  public void cleanup() {
    for(JMenuItem item : actions.keySet()) {
      actions.get(item).cleanup();
    }

    for(PreDisplayAction pda : preDisplayActions) {
      pda.cleanup();
    }

    for(FinishedAlignmentAction faa : finishedAlignmentActions) {
      faa.cleanup();
    }
  }

  private void previousAction() {
    if(alignFactory.hasPrevious()) {
      updateGUI(alignFactory.previous());
    }
  }

  public void processFinished() {
    this.document.setCurrentDocument("null");
  }

  public void progressChanged(int prgress) {
  }

  protected class AnnotationHighlight extends JLabel {
    boolean highlighted = false;

    boolean sourceDocument = false;

    Color colorToUse = Color.WHITE;

    Annotation annotation;

    public AnnotationHighlight(String text, Color color, Annotation annot,
            boolean sourceDocument) {
      super(text);
      this.setOpaque(true);
      this.annotation = annot;
      this.sourceDocument = sourceDocument;
      this.colorToUse = color;
      setBackground(this.colorToUse);
      this.addMouseListener(new MouseActionListener());
      setFont(new Font(getFont().getName(), Font.PLAIN, TEXT_SIZE));
    }

    public void setHighlighted(boolean val, Color color) {
      this.highlighted = val;
      this.colorToUse = color;
      this.setBackground(color);
      this.updateUI();
    }

    public boolean isHighlighted() {
      return this.highlighted;
    }

    public void setHighlightColor(Color color) {
      this.colorToUse = color;
      this.setBackground(color);
      this.updateUI();
    }

    public Color getHighlightColor() {
      return this.colorToUse;
    }

    protected class MouseActionListener extends MouseInputAdapter {

      public void mouseClicked(MouseEvent me) {
        mouseExited(me);
        AnnotationHighlight ah = (AnnotationHighlight)me.getSource();
        Point pt = me.getPoint();
        currentAnnotationHightlight = ah;

        if(SwingUtilities.isRightMouseButton(me)) {

          if(alignment.isAnnotationAligned(ah.annotation)) {
            // lets clear the latest selection
            clearLatestAnnotationsSelection();
          }

          // we should show the option menu here
          JPopupMenu optionsMenu = new JPopupMenu();
          optionsMenu.setOpaque(true);
          optionsMenu.add(new JLabel("Options"));
          optionsMenu.addSeparator();
          for(JMenuItem item : actions.keySet()) {
            AlignmentAction aa = actions.get(item);
            if(alignment.isAnnotationAligned(ah.annotation)) {
              if(aa.invokeForAlignedAnnotation()) {
                optionsMenu.add(item);
              }
            }
            else if(ah.highlighted) {
              if(aa.invokeForHighlightedUnalignedAnnotation()) {
                optionsMenu.add(item);
              }
            }
            else {
              if(aa.invokeForUnhighlightedUnalignedAnnotation()) {
                optionsMenu.add(item);
              }
            }
          }

          optionsMenu.show(ah, (int)pt.getX(), (int)pt.getY());
          optionsMenu.setVisible(true);
          return;
        }

        // was this annotation highlighted?
        // if yes, remove the highlight
        if(ah.highlighted) {

          // we need to check if the ah is aligned
          // if so, we should prompt user to first reset the
          // alignment
          if(alignment.isAnnotationAligned(ah.annotation)) {
            JOptionPane.showMessageDialog(gate.gui.MainFrame.getInstance(),
                    "To remove this annotation from the current"
                            + " aligment, please use the 'Remove Alignment'"
                            + " from the options menu on right click");
            return;
          }

          // the annotation is not aligned but recently highlighted
          // so remove the highlight
          ah.setHighlighted(false, Color.WHITE);

          if(ah.isSourceDocument()) {
            if(sourceLatestAnnotationsSelection == null) {
              sourceLatestAnnotationsSelection = new ArrayList<Annotation>();
            }

            sourceLatestAnnotationsSelection.remove(ah.annotation);
          }
          else {
            if(targetLatestAnnotationsSelection == null) {
              targetLatestAnnotationsSelection = new ArrayList<Annotation>();
            }

            targetLatestAnnotationsSelection.remove(ah.annotation);
          }
        }
        else {
          if(color == Color.WHITE) color = getColor(null);
          ah.setHighlighted(true, color);
          if(ah.isSourceDocument()) {
            if(sourceLatestAnnotationsSelection == null) {
              sourceLatestAnnotationsSelection = new ArrayList<Annotation>();
            }

            if(!sourceLatestAnnotationsSelection.contains(ah.annotation))
              sourceLatestAnnotationsSelection.add(ah.annotation);
          }
          else {
            if(targetLatestAnnotationsSelection == null) {
              targetLatestAnnotationsSelection = new ArrayList<Annotation>();
            }
            if(!targetLatestAnnotationsSelection.contains(ah.annotation))
              targetLatestAnnotationsSelection.add(ah.annotation);
          }
        }

      }

      JPopupMenu menu = new JPopupMenu();

      FeaturesModel model = new FeaturesModel();

      JTable featuresTable = new JTable(model);

      Timer timer = new Timer();

      TimerTask task;

      public void mouseEntered(final MouseEvent me) {
        final AnnotationHighlight ah = (AnnotationHighlight)me.getSource();
        model.setAnnotation(ah.annotation);
        task = new TimerTask() {
          public void run() {
            menu.add(featuresTable);
            menu.show(ah, me.getX(), me.getY() + 10);
            menu.revalidate();
            menu.updateUI();
          }
        };
        timer.schedule(task, 2000);
      }

      public void mouseExited(MouseEvent me) {
        if(task != null) {
          task.cancel();
        }
        if(menu != null && menu.isVisible()) {
          menu.setVisible(false);
        }
      }
    }

    public boolean isSourceDocument() {
      return sourceDocument;
    }

  }

  private Color getColor(Color c) {
    float components[] = null;
    if(c == null)
      components = colorGenerator.getNextColor().getComponents(null);
    else components = c.getComponents(null);

    Color colour = new Color(components[0], components[1], components[2], 0.5f);
    int rgb = colour.getRGB();
    int alpha = colour.getAlpha();
    int rgba = rgb | (alpha << 24);
    colour = new Color(rgba, true);
    return colour;
  }

  public void annotationsAligned(Annotation srcAnnotation, String srcAS,
          Document srcDocument, Annotation tgtAnnotation, String tgtAS,
          Document tgtDocument) {

    if(srcAnnotation == null || tgtAnnotation == null || srcDocument == null
            || tgtDocument == null) {
      System.err.println("One of the src/tgt annotation/document is null");
      return;
    }

    if(!srcDocument.getName().equals(getSourceDocumentId())) {
      return;
    }
    if(!tgtDocument.getName().equals(getTargetDocumentId())) {
      return;
    }

    AnnotationHighlight srcAH = sourceHighlights.get(srcAnnotation);
    AnnotationHighlight tgtAH = targetHighlights.get(tgtAnnotation);
    if(srcAH == null || tgtAH == null) return;

    // if one of the two is already aligned
    Color toUse = null;
    if(srcAH.highlighted)
      toUse = srcAH.colorToUse;
    else if(tgtAH.highlighted)
      toUse = tgtAH.colorToUse;
    else toUse = getColor(null);
    if(!srcAH.highlighted) {
      srcAH.setHighlighted(true, toUse);
    }

    if(!tgtAH.highlighted) {
      tgtAH.setHighlighted(true, toUse);
    }
    refresh();
  }

  public void annotationsUnaligned(Annotation srcAnnotation, String srcAS,
          Document srcDocument, Annotation tgtAnnotation, String tgtAS,
          Document tgtDocument) {

    if(srcAnnotation == null || tgtAnnotation == null || srcDocument == null
            || tgtDocument == null) {
      System.err.println("One of the src/tgt annotation/document is null");
      return;
    }

    if(!srcDocument.getName().equals(
            sourceDocumentId.getSelectedItem().toString())) return;

    if(!tgtDocument.getName().equals(
            targetDocumentId.getSelectedItem().toString())) return;

    AnnotationHighlight srcAH = sourceHighlights.get(srcAnnotation);
    AnnotationHighlight tgtAH = targetHighlights.get(tgtAnnotation);
    if(srcAH == null || tgtAH == null) return;

    if(srcAH.highlighted) {
      srcAH.setHighlighted(false, Color.WHITE);
    }

    if(tgtAH.highlighted) {
      tgtAH.setHighlighted(false, Color.WHITE);
    }
    refresh();
  }

  private void readAction(AlignmentAction action) {


    boolean addToMenu = true;

    if(action.invokeWithAlignAction()) {
      allActions.add(action);
      addToMenu = false;
    }

    if(action.invokeWithRemoveAction()) {
      if(!allActions.contains(action)) {
        allActions.add(action);
      }
      addToMenu = false;
    }
    
    String caption = action.getCaption();
    Icon icon = action.getIcon();

   
    if(addToMenu) {

      final JMenuItem menuItem;
      if(icon != null) {
        menuItem = new JMenuItem(caption, icon);
        JMenuItem actionItem = actionsMenuItemByCaption.get(action
                .getIconPath());
        if(actionItem != null) {
          actions.remove(actionItem);
          actionsMenuItemByCaption.remove(action.getIconPath());
        }
        actionsMenuItemByCaption.put(action.getIconPath(), menuItem);
      }
      else {
        menuItem = new JMenuItem(caption);
        JMenuItem actionItem = actionsMenuItemByCaption.get(caption);
        if(actionItem != null) {
          actions.remove(actionItem);
          actionsMenuItemByCaption.remove(caption);
        }
        actionsMenuItemByCaption.put(caption, menuItem);
      }
      if(menuItem != null) {
        menuItem.setToolTipText(action.getToolTip());
        actions.put(menuItem, action);
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            executeAction(actions.get(menuItem));
          }
        });
      }
    }
  }

  private void readActions(File actionsConfFile) {

    if(actionsConfFile != null && actionsConfFile.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(actionsConfFile));
        String line = br.readLine();
        String cName = "";
        while(line != null) {
          // each line will have a class name
          try {
            if(line.trim().startsWith("#") || line.trim().length() == 0) {
              line = br.readLine();
              continue;
            }

            int index = line.indexOf(",");
            cName = index < 0 ? line.trim() : line.substring(0, index);
            line = index < 0 ? "" : line.substring(index + 1);

            Class actionClass = Class.forName(cName, true, Gate
                    .getClassLoader());

            Object action = actionClass.newInstance();
            String[] args = line.split("[,]");
            if(action instanceof AlignmentAction) {
              AlignmentAction aa = (AlignmentAction)actionClass.newInstance();
              loadAlignmentAction(aa, args);
            }

            if(action instanceof PreDisplayAction) {
              loadPreDisplayAction((PreDisplayAction)action, args);
            }

            if(action instanceof FinishedAlignmentAction) {
              loadFinishedAlignmentAction((FinishedAlignmentAction)action, args);
            }
          }
          catch(ClassNotFoundException cnfe) {
            System.err.println("class " + cName + " not found!");
            continue;
          }
          catch(IllegalAccessException ilae) {
            System.err.println("class " + cName
                    + " threw the illegal access exception!");
            continue;
          }
          catch(InstantiationException ie) {
            System.err.println("class " + cName + " could not be instantiated");
            continue;
          }
          finally {
            line = br.readLine();
          }
        }
      }
      catch(IOException ioe) {
        throw new GateRuntimeException(ioe);
      }
    }
  }

  private void loadAlignmentAction(AlignmentAction aa, String[] args) {
    try {
      aa.init(args);
    }
    catch(AlignmentActionInitializationException aaie) {
      throw new GateRuntimeException(aaie);
    }

    readAction(aa);
    if(aa.invokeWithAlignAction() || aa.invokeWithRemoveAction()) {
      String title = aa.getCaption();
      if(title == null || title.trim().length() == 0) return;
      PropertyActionCB pab = new PropertyActionCB(title, false, aa);
      pab.setToolTipText(aa.getToolTip());
      actionsCBMap.put(aa, pab);
      int count = propertiesPanel.getComponentCount();
      propertiesPanel.add(pab, count - 1);
      propertiesPanel.validate();
      propertiesPanel.updateUI();
    }
  }

  private void loadFinishedAlignmentAction(FinishedAlignmentAction faa,
          String[] args) {
    try {
      faa.init(args);
      finishedAlignmentActions.add(faa);
    }
    catch(AlignmentActionInitializationException aaie) {
      throw new GateRuntimeException(aaie);
    }
  }

  private void loadPreDisplayAction(PreDisplayAction pda, String[] args) {
    try {
      pda.init(args);
      preDisplayActions.add(pda);
    }
    catch(AlignmentActionInitializationException aaie) {
      throw new GateRuntimeException(aaie);
    }
  }

  private class Edge {
    AnnotationHighlight srcAH;

    AnnotationHighlight tgtAH;
  }

  private class PropertyActionCB extends JCheckBox {
    AlignmentAction aa;

    JCheckBox thisInstance;

    String key;

    public PropertyActionCB(String propKey, boolean value,
            AlignmentAction action) {
      super(propKey);
      setSelected(value);
      this.aa = action;
      thisInstance = this;
      key = propKey;
    }
  }

  private class MappingsPanel extends JPanel {

    private Set<Edge> edges = new HashSet<Edge>();

    public MappingsPanel() {
      // do nothing
      setOpaque(true);
      setBackground(Color.WHITE);
    }

    public void removeAllEdges() {
      edges.clear();
    }

    public void addEdge(Edge edge) {
      if(edge != null) edges.add(edge);
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D)g;
      g2d.setBackground(Color.WHITE);
      g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

      if(showLinks.isSelected()) {
        for(Edge e : edges) {
          int x = (int)(e.srcAH.getBounds().x + (double)((double)e.srcAH
                  .getBounds().width / 2));
          int y = 0;
          int x1 = (int)(e.tgtAH.getBounds().x + (double)((double)e.tgtAH
                  .getBounds().width / 2));
          int y1 = this.getBounds().height;
          Line2D line = new Line2D.Double(new Point((int)x, (int)y), new Point(
                  (int)x1, (int)y1));
          Stroke stroke = new BasicStroke(2.0f);
          g2d.setStroke(stroke);
          Color c = g2d.getColor();
          g2d.setColor(e.srcAH.getBackground());
          g2d.draw(line);
          g2d.setColor(c);
        }
      }
    }
  }

  public class FeaturesModel extends DefaultTableModel {
    Annotation toShow;

    ArrayList<String> features;

    ArrayList<String> values;

    public FeaturesModel() {
      super(new String[] {"Feature", "Value"}, 0);
    }

    public void setAnnotation(Annotation annot) {
      features = new ArrayList<String>();
      values = new ArrayList<String>();
      for(Object key : annot.getFeatures().keySet()) {
        features.add(key.toString());
        values.add(annot.getFeatures().get(key).toString());
      }
      super.fireTableDataChanged();
    }

    public Class getColumnClass(int column) {
      return String.class;
    }

    public int getRowCount() {
      return values == null ? 0 : values.size();
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch(column) {
        case 0:
          return "Feature";
        default:
          return "Value";
      }
    }

    public Object getValueAt(int row, int column) {
      switch(column) {
        case 0:
          return features.get(row);
        default:
          return values.get(row);
      }
    }

  }

  public void setAlignmentFeatureName(String alignmentFeatureName) {
    document.getAlignmentInformation(alignmentFeatureName);
    alignmentFeatureNames.setSelectedItem(alignmentFeatureName);
    if(!alignmentFeatureNames.getSelectedItem().equals(alignmentFeatureName)) {
      alignmentFeatureNames.setEditable(true);
      alignmentFeatureNames.addItem(alignmentFeatureNames);
      alignmentFeatureNames.setSelectedItem(alignmentFeatureName);
      alignmentFeatureNames.setEditable(false);
    }
  }

  public void setSourceDocumentId(String docId) {
    sourceDocumentId.setSelectedItem(docId);
  }

  public void setTargetDocumentId(String docId) {
    targetDocumentId.setSelectedItem(docId);
  }

  public void setSourceAnnotationSetName(String annotSet) {
    if(annotSet == null) {
      sourceASName.setSelectedItem("<null>");
    }
    else {
      sourceASName.setSelectedItem(annotSet);
    }
  }

  public void setTargetAnnotationSetName(String annotSet) {
    if(annotSet == null) {
      targetASName.setSelectedItem("<null>");
    }
    else {
      targetASName.setSelectedItem(annotSet);
    }
  }

  public void setSourceUnitOfAlignment(String unit) {
    sourceUnitOfAlignment.setSelectedItem(unit);
  }

  public void setTargetUnitOfAlignment(String unit) {
    targetUnitOfAlignment.setSelectedItem(unit);
  }

  public void setSourceParentOfUnitOfAlignment(String unit) {
    sourceParentOfUnitOfAlignment.setSelectedItem(unit);
  }

  public void setTargetParentOfUnitOfAlignment(String unit) {
    targetParentOfUnitOfAlignment.setSelectedItem(unit);
  }

  public void disableUserSelections(boolean disableUserSelections) {
    this.disableUserSelections = disableUserSelections;
    if(!this.disableUserSelections) {
      populate.setEnabled(false);
      loadActions.setEnabled(false);
      sourceUnitOfAlignment.setEnabled(false);
      targetUnitOfAlignment.setEnabled(false);
      sourceParentOfUnitOfAlignment.setEnabled(false);
      targetParentOfUnitOfAlignment.setEnabled(false);
      sourceASName.setEnabled(false);
      targetASName.setEnabled(false);
      sourceDocumentId.setEnabled(false);
      targetDocumentId.setEnabled(false);
      alignmentFeatureNames.setEnabled(false);
    }
    else {
      populate.setEnabled(true);
      loadActions.setEnabled(true);
      alignmentFeatureNames.setEnabled(true);
      populate();
    }
  }

  boolean disableUserSelections = false;
}
