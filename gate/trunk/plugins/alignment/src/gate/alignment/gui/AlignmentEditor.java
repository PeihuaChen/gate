package gate.alignment.gui;

import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

  private JButton populate, next, previous, loadActions;

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

  private Color color, unitColor;

  private ColorGenerator colorGenerator = new ColorGenerator();

  public static final int TEXT_SIZE = 20;

  private Map<JMenuItem, AlignmentAction> actions;

  private Map<String, JMenuItem> actionsMenuItemByCaption;

  private AnnotationHighlight currentAnnotationHightlight = null;

  private AlignmentEditor thisInstance = null;

  /*
   * (non-Javadoc)
   * 
   * @see gate.Resource#init()
   */
  public Resource init() throws ResourceInstantiationException {
    sourceHighlights = new HashMap<Annotation, AnnotationHighlight>();
    targetHighlights = new HashMap<Annotation, AnnotationHighlight>();

    sourceLatestAnnotationsSelection = new ArrayList<Annotation>();
    targetLatestAnnotationsSelection = new ArrayList<Annotation>();

    actions = new HashMap<JMenuItem, AlignmentAction>();
    actionsMenuItemByCaption = new HashMap<String, JMenuItem>();

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

    sourceDocumentId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        populateAS((String)sourceDocumentId.getSelectedItem(), sourceASName);
      }
    });

    targetDocumentId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
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

    populate = new JButton("Populate");
    populate.addActionListener(this);

    previous = new JButton("< Previous");
    previous.addActionListener(this);

    next = new JButton("Next >");
    next.addActionListener(this);

    showLinks = new JCheckBox("Show Links");
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

    waPanel.add(sourcePanel, BorderLayout.NORTH);
    waPanel.add(targetPanel, BorderLayout.SOUTH);
    waPanel.add(linesCanvas, BorderLayout.CENTER);

    mainPanel.add(new JScrollPane(waPanel), BorderLayout.CENTER);
    this.setLayout(new BorderLayout());
    this.add(mainPanel, BorderLayout.CENTER);
    color = getColor(null);
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
      boxToPopulate.setSelectedIndex(0);
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

    Map<Document, Set<Annotation>> latestSelectedAnnotations = new HashMap<Document, Set<Annotation>>();
    latestSelectedAnnotations.put(srcDocument, srcSelectedAnnots);
    latestSelectedAnnotations.put(tgtDocument, tgtSelectedAnnots);

    try {
      color = Color.WHITE;
      aa.execute(this, this.document, latestSelectedAnnotations, currentAnnotationHightlight.annotation);
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

  public void actionPerformed(ActionEvent ae) {
    if(ae.getSource() == populate) {
      try {
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
    else if(ae.getSource() == next) {
      nextAction();
    }
    else if(ae.getSource() == previous) {
      previousAction();
    }
  }

  private void nextAction() {
    if(alignFactory != null && alignFactory.hasNext()) {

      updateGUI(alignFactory.next());
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

      public void mouseMoved(MouseEvent me) {
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

  public void annotationsAligned(Annotation srcAnnotation,
          Document srcDocument, Annotation tgtAnnotation, Document tgtDocument) {

    if(srcAnnotation == null || tgtAnnotation == null || srcDocument == null
            || tgtDocument == null) {
      System.err.println("One of the src/tgt annotation/document is null");
      return;
    }

    if(!srcDocument.getName().equals(getSourceDocumentId())) return;
    if(!tgtDocument.getName().equals(getTargetDocumentId())) return;

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

  public void annotationsUnaligned(Annotation srcAnnotation,
          Document srcDocument, Annotation tgtAnnotation, Document tgtDocument) {

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

  private void readActions(File actionsConfFile) {

    if(actionsConfFile != null && actionsConfFile.exists()) {
      try {
        BufferedReader br = new BufferedReader(new FileReader(actionsConfFile));
        String line = br.readLine();
        while(line != null) {
          // each line will have a class name
          try {
            Class actionClass = Class.forName(line.trim(), true, Gate
                    .getClassLoader());
            AlignmentAction aa = (AlignmentAction)actionClass.newInstance();
            String caption = aa.getCaption();

            // lets check if we already have an action with this name

            Icon icon = aa.getIcon();

            final JMenuItem menuItem;
            if(caption == null && icon == null) {
              menuItem = new JMenuItem(aa.getClass().getName());
              JMenuItem actionItem = actionsMenuItemByCaption.get(aa.getClass()
                      .getName());
              if(actionItem != null) {
                actions.remove(actionItem);
                actionsMenuItemByCaption.remove(aa.getClass().getName());
              }
              actionsMenuItemByCaption.put(aa.getClass().getName(), menuItem);
            }
            else if(caption == null) {
              menuItem = new JMenuItem(icon);
              JMenuItem actionItem = actionsMenuItemByCaption.get(aa
                      .getIconPath());
              if(actionItem != null) {
                actions.remove(actionItem);
                actionsMenuItemByCaption.remove(aa.getIconPath());
              }
              actionsMenuItemByCaption.put(aa.getIconPath(), menuItem);
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
            actions.put(menuItem, aa);
            menuItem.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ae) {
                executeAction(actions.get(menuItem));
              }
            });
          }
          catch(ClassNotFoundException cnfe) {
            System.err.println("class " + line.trim() + " not found!");
            continue;
          }
          catch(IllegalAccessException ilae) {
            System.err.println("class " + line.trim()
                    + " threw the illegal access exception!");
            continue;
          }
          catch(InstantiationException ie) {
            System.err.println("class " + line.trim()
                    + " could not be instantiated");
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

  private class Edge {
    AnnotationHighlight srcAH;

    AnnotationHighlight tgtAH;
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
}
