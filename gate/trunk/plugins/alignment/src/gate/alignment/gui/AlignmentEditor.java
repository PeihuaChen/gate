package gate.alignment.gui;

import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import gate.*;
import gate.alignment.*;
import gate.composite.CompositeDocument;
import gate.compound.CompoundDocument;
import gate.creole.*;
import gate.swing.ColorGenerator;
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

  // mainPanel is a container for all, paramPanel is a container for
  // user. parameters and waPanel contains JEditorPanes for each
  // document
  private JPanel mainPanel, paramPanel, waPanel;

  // TextFields for users to provide parameter
  private JTextField inputASName, parentOfUnitOfAlignment, unitOfAlignment;

  private JButton populate, next, previous;

  private JEditorPane[] tas;

  private CompoundDocument document;

  private List<String> documentIDs;

  private AlignmentFactory alignFactory;

  private Alignment alignment;

  private HashMap<JEditorPane, HashMap<Annotation, AnnotationHighlight>> highlights;

  private HashMap<JEditorPane, List<Annotation>> latestAnnotationsSelection;

  private Color color, unitColor;

  private ColorGenerator colorGenerator = new ColorGenerator();

  public static final int TEXT_SIZE = 20;

  private Map<JMenuItem, AlignmentAction> actions;

  private AnnotationHighlight currentAnnotationHightlight = null;

  /*
   * (non-Javadoc)
   * 
   * @see gate.Resource#init()
   */
  public Resource init() throws ResourceInstantiationException {
    highlights = new HashMap<JEditorPane, HashMap<Annotation, AnnotationHighlight>>();
    latestAnnotationsSelection = new HashMap<JEditorPane, List<Annotation>>();
    actions = new HashMap<JMenuItem, AlignmentAction>();
    readActions();
    return this;
  }

  /**
   * Initialize the GUI
   */
  private void initGui() {
    mainPanel = new JPanel(new BorderLayout());
    paramPanel = new JPanel(new GridLayout(4, 1));

    waPanel = new JPanel(new GridLayout(documentIDs.size(), 1));

    inputASName = new JTextField(40);
    parentOfUnitOfAlignment = new JTextField(40);
    unitOfAlignment = new JTextField(40);
    populate = new JButton("Populate");
    JPanel temp1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel temp2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel temp3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel temp6 = new JPanel(new FlowLayout(FlowLayout.LEFT));

    temp1.add(new JLabel("Input Annotation Set Name:"));
    temp1.add(inputASName);
    temp2.add(new JLabel("Parent of Unit of Alignment (e.g. Sentence):"));
    temp2.add(parentOfUnitOfAlignment);
    parentOfUnitOfAlignment.setText("Sentence");
    temp3.add(new JLabel("Unit of Alignment (e.g. Token):"));
    temp3.add(unitOfAlignment);
    unitOfAlignment.setText("Token");
    temp6.add(populate);
    paramPanel.add(temp1);
    paramPanel.add(temp2);
    paramPanel.add(temp3);
    paramPanel.add(temp6);

    mainPanel.add(paramPanel, BorderLayout.NORTH);
    populate.addActionListener(this);

    previous = new JButton("< Previous");
    previous.addActionListener(this);
    next = new JButton("Next >");
    next.addActionListener(this);
    temp6.add(previous);
    temp6.add(next);

    tas = new JEditorPane[documentIDs.size()];
    // now we need to create TextArea for each language
    for(int i = 0; i < documentIDs.size(); i++) {
      tas[i] = new JEditorPane();
      tas[i].setBorder(new TitledBorder((String)documentIDs.get(i)));
      waPanel.add(new JScrollPane(tas[i]));
      tas[i]
              .setFont(new Font(tas[i].getFont().getName(), Font.PLAIN,
                      TEXT_SIZE));
      tas[i].addMouseListener(new MouseActionListener());
      tas[i].addMouseMotionListener(new MouseActionListener());
      tas[i].setEditable(false);
    }
    mainPanel.add(waPanel);
    this.setLayout(new BorderLayout());
    this.add(mainPanel, BorderLayout.CENTER);
    unitColor = getColor(new Color(234, 245, 246));
    color = getColor(null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.VisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    this.document = (CompoundDocument)target;
    this.documentIDs = new ArrayList<String>(this.document.getDocumentIDs());
    this.documentIDs.remove(CompositeDocument.COMPOSITE_DOC_NAME);
    alignment = this.document.getAlignmentInformation();
    alignment.addAlignmentListener(this);
    initGui();
  }

  /**
   * This method clears up the latest annotation selection
   */
  public void clearLatestAnnotationsSelection() {
    for(int i = 0; i < this.documentIDs.size(); i++) {
      List<Annotation> srcAnnotations = latestAnnotationsSelection.get(tas[i]);
      if(srcAnnotations == null || srcAnnotations.isEmpty()) continue;

      HashMap<Annotation, AnnotationHighlight> annotHighlights = this.highlights
              .get(tas[i]);
      for(Annotation annotation : srcAnnotations) {
        AnnotationHighlight ah = annotHighlights.get(annotation);
        ah.highlighted = false;
        if(ah.getAlignedHighlight() != null)
          tas[i].getHighlighter().removeHighlight(ah.getAlignedHighlight());
        ah.setAlignedHighlight(null, null);
      }
    }
    latestAnnotationsSelection.clear();
  }

  protected void executeAction(AlignmentAction aa) {
    Map<Document, Set<Annotation>> latestSelectedAnnotations = new HashMap<Document, Set<Annotation>>();
    boolean clearCurrentSelection = true;
    if(currentAnnotationHightlight == null) {
      clearCurrentSelection = false;
    }

    for(int i = 0; i < this.documentIDs.size(); i++) {
      Document aDocument = this.document.getDocument(documentIDs.get(i));
      List<Annotation> srcAnnotations = latestAnnotationsSelection.get(tas[i]);
      if(srcAnnotations == null) {
        srcAnnotations = new ArrayList<Annotation>();
        latestAnnotationsSelection.put(tas[i], srcAnnotations);
      }

      if(clearCurrentSelection
              && srcAnnotations
                      .contains(currentAnnotationHightlight.annotation)) {
        clearCurrentSelection = false;
      }
      latestSelectedAnnotations.put(aDocument, new HashSet<Annotation>(
              srcAnnotations));
    }

    if(clearCurrentSelection) {
      clearLatestAnnotationsSelection();
      // we need to find out the set of aligned annotations and put them
      // under the lastestSelectedAnnotations
      latestAnnotationsSelection = new HashMap<JEditorPane, List<Annotation>>();
      Set<Annotation> alignedAnnotations = alignment
              .getAlignedAnnotations(currentAnnotationHightlight.annotation);
      color = currentAnnotationHightlight.color;
      Set<Annotation> otherAnnotations = new HashSet<Annotation>();
      for(Annotation anAnnotation : alignedAnnotations) {
        otherAnnotations.addAll(alignment.getAlignedAnnotations(anAnnotation));
      }
      alignedAnnotations.addAll(otherAnnotations);
      otherAnnotations = null;

      for(Annotation anAnnotation : alignedAnnotations) {
        String docId = alignment.getDocument(anAnnotation).getName();
        JEditorPane docPane = tas[documentIDs.indexOf(docId)];
        List<Annotation> annots = latestAnnotationsSelection.get(docPane);
        if(annots == null) {
          annots = new ArrayList<Annotation>();
          latestAnnotationsSelection.put(docPane, annots);
        }

        if(!annots.contains(anAnnotation)) {
          annots.add(anAnnotation);
        }
      }

      latestSelectedAnnotations.clear();

      for(int i = 0; i < this.documentIDs.size(); i++) {
        Document aDocument = this.document.getDocument(documentIDs.get(i));
        List<Annotation> srcAnnotations = latestAnnotationsSelection
                .get(tas[i]);
        if(srcAnnotations == null)
          srcAnnotations = new ArrayList<Annotation>();
        if(clearCurrentSelection
                && srcAnnotations
                        .contains(currentAnnotationHightlight.annotation)) {
          clearCurrentSelection = false;
        }
        latestSelectedAnnotations.put(aDocument, new HashSet<Annotation>(
                srcAnnotations));
      }
    }

    try {
      aa.execute(this, this.document, latestSelectedAnnotations);
    }
    catch(AlignmentException ae) {
      throw new GateRuntimeException(ae);
    }
  }

  public void actionPerformed(ActionEvent ae) {
    if(ae.getSource() == populate) {
      try {
        AlignmentFactory af = new AlignmentFactory(document, inputASName
                .getText(), unitOfAlignment.getText(), parentOfUnitOfAlignment
                .getText(), "gate.util.OffsetComparator");
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

  /**
   * This method updates the GUI.
   * 
   * @param docIDsAndAnnots
   */
  private void updateGUI(HashMap<String, Annotation> docIDsAndAnnots) {
    // before refreshing, we remove all the highlights
    latestAnnotationsSelection.clear();
    this.highlights.clear();

    // first we show all the annotations and then highlight each unit
    // using a default highlight color.
    // docIDsAndAnnots has a docId (e.g. en or hi) as key and the
    // parent of the unit of alignment (e.g. Sentence) as the value.
    for(String docId : docIDsAndAnnots.keySet()) {
      Annotation annot = docIDsAndAnnots.get(docId);

      // find out the index of JEditorPane (each doc has a different
      // editor pane associated to it)
      int index = documentIDs.indexOf(docId);

      // gets the annotation's underlying text (e.g. full sentence)
      String text = alignFactory.getText(annot, docId);

      // remove all the highlights
      tas[index].getHighlighter().removeAllHighlights();

      // set the text
      tas[index].setEditable(true);
      tas[index].setText(text);
      tas[index].setEditable(false);
      tas[index].updateUI();

      // we need to highlight the unit type
      AnnotationSet underlyingUnitAnnotationsSet = alignFactory
              .getUnderlyingAnnotations(annot, docId);

      // if there are not underlying annotations, just return
      if(underlyingUnitAnnotationsSet == null) {
        return;
      }

      // for each underlying unit of alignment, we create a default
      // annotation highlight. highlight object contains the offsets,
      // the source annotation and the colour object used for
      // highlighting the annotation
      HashMap<Annotation, AnnotationHighlight> annotationHighlightsMap = new HashMap<Annotation, AnnotationHighlight>();
      for(Annotation underlyingUnitAnnotation : underlyingUnitAnnotationsSet) {
        int start = underlyingUnitAnnotation.getStartNode().getOffset()
                .intValue();
        int end = underlyingUnitAnnotation.getEndNode().getOffset().intValue();

        // read just the offset with respect to the offsets of the
        // parent of unit of alignment annotation
        start -= annot.getStartNode().getOffset().intValue();
        end -= annot.getStartNode().getOffset().intValue();

        // and finally add a new highlight
        try {
          AnnotationHighlight ah = new AnnotationHighlight(unitColor, start,
                  end, underlyingUnitAnnotation);
          annotationHighlightsMap.put(underlyingUnitAnnotation, ah);
          tas[index].getHighlighter().addHighlight(start, end, ah);
        }
        catch(BadLocationException ble) {
          throw new GateRuntimeException(ble);
        }
      }

      this.highlights.put(tas[index], annotationHighlightsMap);
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

      // find out the language/id of the document
      String docId = alignment.getDocument(srcAnnotation).getName();

      // find out the editor pane
      JEditorPane pane = tas[this.documentIDs.indexOf(docId)];

      // and find out the default annotation highlight
      AnnotationHighlight ah = getHighlight(pane, srcAnnotation);
      if(ah == null) continue;

      // ok we need to generate a new color
      Color newColor = getColor(null);

      // and we add the highlight
      try {
        Object obj = pane.getHighlighter().addHighlight(ah.start, ah.end,
                new DefaultHighlighter.DefaultHighlightPainter(newColor));
        ah.setAlignedHighlight(obj, newColor);
        ah.highlighted = true;
        highlightedAnnotations.add(srcAnnotation);
      }
      catch(Exception e) {
        e.printStackTrace();
      }

      // find out the annotations that are aligned to the srcAnnotation
      Set<Annotation> alignedAnnotations = alignment
              .getAlignedAnnotations(srcAnnotation);

      for(Annotation targetAnnotation : alignedAnnotations) {

        // already highlighted??
        if(highlightedAnnotations.contains(targetAnnotation)) continue;

        String targetAnnLang = alignment.getDocument(targetAnnotation)
                .getName();

        // find out text editor
        pane = tas[this.documentIDs.indexOf(targetAnnLang)];
        ah = getHighlight(pane, targetAnnotation);
        if(ah == null || ah.highlighted) continue;

        try {
          Object obj = pane.getHighlighter().addHighlight(ah.start, ah.end,
                  new DefaultHighlighter.DefaultHighlightPainter(newColor));
          ah.setAlignedHighlight(obj, newColor);
          highlightedAnnotations.add(targetAnnotation);
          ah.highlighted = true;
        }
        catch(Exception e) {
          e.printStackTrace();
        }

        // the following loop is necessary
        // e.g. h1, h2, h3 are aligned with e1, e2 and e3
        // h1 is the srcAnnotation and under consideration
        // in the inner loop we are iterating over e1, e2 and e3
        // say we are looking at the e1
        // so the following loop will find out the e1 is aligned with h2
        // and h3 and will highlight them.
        // This helps as we want to use the
        // same color for all h1, h2, h3, e1, e2 and e3
        Set<Annotation> alignedAlignedAnnotations = alignment
                .getAlignedAnnotations(targetAnnotation);

        for(Annotation targetTargetAnnotation : alignedAlignedAnnotations) {
          if(highlightedAnnotations.contains(targetTargetAnnotation)) continue;

          String targetTargetAnnLang = alignment.getDocument(
                  targetTargetAnnotation).getName();

          // find out text editor
          pane = tas[this.documentIDs.indexOf(targetTargetAnnLang)];
          ah = getHighlight(pane, targetTargetAnnotation);
          if(ah == null || ah.highlighted) continue;

          try {
            Object obj = pane.getHighlighter().addHighlight(ah.start, ah.end,
                    new DefaultHighlighter.DefaultHighlightPainter(newColor));
            ah.setAlignedHighlight(obj, newColor);
            highlightedAnnotations.add(targetTargetAnnotation);
            ah.highlighted = true;
          }
          catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  /**
   * Find out the annotation highlight for the given annotation
   * 
   * @param pane
   * @param annot
   * @return
   */
  private AnnotationHighlight getHighlight(JEditorPane pane, Annotation annot) {
    HashMap<Annotation, AnnotationHighlight> highlights = this.highlights
            .get(pane);
    if(highlights == null || highlights.isEmpty()) return null;
    return highlights.get(annot);
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

  protected class AnnotationHighlight
                                     extends
                                       DefaultHighlighter.DefaultHighlightPainter {
    int start;

    int end;

    boolean highlighted = false;

    Object alignedHighlight;

    Color color;

    Annotation annotation;

    public AnnotationHighlight(Color color, int start, int end, Annotation annot) {
      super(color);
      this.start = start;
      this.end = end;
      this.annotation = annot;
    }

    public void setHighlighted(boolean val) {
      this.highlighted = val;
    }

    public void setAlignedHighlight(Object ah, Color color) {
      this.alignedHighlight = ah;
      this.color = color;
    }

    public Object getAlignedHighlight() {
      return this.alignedHighlight;
    }

    public boolean isWithinBoundaries(int location) {
      return location >= start && location <= end;
    }
  }

  protected class MouseActionListener extends MouseInputAdapter {

    public void mouseClicked(MouseEvent me) {
      JEditorPane ta = (JEditorPane)me.getSource();

      // firstly obtain all highlights for this editor pane
      // it contains an annotationHighlight for each annotation
      HashMap<Annotation, AnnotationHighlight> hilights = highlights.get(ta);

      // and iterate through each annotation highlight
      // and find out where did the user click
      ArrayList<AnnotationHighlight> hilites = new ArrayList<AnnotationHighlight>(
              hilights.values());
      boolean found = false;
      for(AnnotationHighlight ah : hilites) {
        if(found) break;
        Point pt = me.getPoint();
        int location = ta.viewToModel(pt);

        // did user click on this annotation highlight?
        if(ah.isWithinBoundaries(location)) {
          found = true;
          currentAnnotationHightlight = ah;

          if(SwingUtilities.isRightMouseButton(me)) {
            // we should show the option menu here
            JPopupMenu optionsMenu = new JPopupMenu();
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

            optionsMenu.show(ta, (int)pt.getX(), (int)pt.getY());
            optionsMenu.setVisible(true);
            return;
          }

          // was this annotation highlighted?
          // if yes, unhighlight it
          if(ah.highlighted) {
            // try to obtain the highlight
            if(ah.color != null && ah.color.equals(color)) {

              // we need to check if the ah is aligned
              // if so, we should prompt user to first reset the
              // alignment
              if(alignment.isAnnotationAligned(ah.annotation)) {
                JOptionPane
                        .showMessageDialog(
                                gate.gui.MainFrame.getInstance(),
                                "To remove this annotation from the current aligment, please use the 'Remove Alignment' from the options menu on right click");
                return;
              }

              Object ah1 = ah.getAlignedHighlight();
              if(ah1 != null) {
                ta.getHighlighter().removeHighlight(ah1);
              }
              ah.highlighted = false;
              ah.color = null;
              ah.setAlignedHighlight(null, null);

              // we stored the recent clicks in
              // latestAnnotationsSelection
              // sch that when user clicks on the align button
              // we align the annotations from this hashmap
              if(latestAnnotationsSelection == null)
                latestAnnotationsSelection = new HashMap<JEditorPane, List<Annotation>>();

              List<Annotation> annotations = latestAnnotationsSelection.get(ta);
              if(annotations == null) {
                annotations = new ArrayList<Annotation>();
              }

              annotations.remove(ah.annotation);
              latestAnnotationsSelection.put(ta, annotations);
            }
          }
          else {
            try {
              Color toUse = color;
              if(ah.color != null) {
                toUse = ah.color;
              }
              Object obj = ta.getHighlighter().addHighlight(ah.start, ah.end,
                      new DefaultHighlighter.DefaultHighlightPainter(toUse));
              ah.setAlignedHighlight(obj, toUse);
              ah.highlighted = true;
              if(latestAnnotationsSelection == null)
                latestAnnotationsSelection = new HashMap<JEditorPane, List<Annotation>>();

              List<Annotation> annotations = latestAnnotationsSelection.get(ta);
              if(annotations == null) {
                annotations = new ArrayList<Annotation>();
              }

              if(!annotations.contains(ah.annotation)) {
                annotations.add(ah.annotation);
              }

              latestAnnotationsSelection.put(ta, annotations);
            }
            catch(Exception e) {
              e.printStackTrace();
            }
          }
          return;
        }
      }
    }

    public void mouseMoved(MouseEvent me) {
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

    int indexOfSrcPane = documentIDs.indexOf(srcDocument.getName());
    if(indexOfSrcPane < 0) {
      System.err.println("Invalid document for alignment "
              + srcDocument.getName());
      return;
    }

    int indexOfTgtPane = documentIDs.indexOf(tgtDocument.getName());
    if(indexOfTgtPane < 0) {
      System.err.println("Invalid document for alignment "
              + tgtDocument.getName());
      return;
    }

    JEditorPane srcPane = tas[indexOfSrcPane];
    JEditorPane tgtPane = tas[indexOfTgtPane];

    Map<Annotation, AnnotationHighlight> srcDocHighlights = this.highlights
            .get(srcPane);
    Map<Annotation, AnnotationHighlight> tgtDocHighlights = this.highlights
            .get(tgtPane);

    AnnotationHighlight srcAH = srcDocHighlights.get(srcAnnotation);
    AnnotationHighlight tgtAH = tgtDocHighlights.get(tgtAnnotation);
    if(srcAH == null || tgtAH == null) return;

    // if one of the two is already aligned
    Color toUse = null;
    if(srcAH.highlighted)
      toUse = srcAH.color;
    else if(tgtAH.highlighted)
      toUse = tgtAH.color;
    else toUse = getColor(null);

    try {

      if(!srcAH.highlighted) {
        Object obj = srcPane.getHighlighter().addHighlight(srcAH.start,
                srcAH.end,
                new DefaultHighlighter.DefaultHighlightPainter(toUse));
        srcAH.setAlignedHighlight(obj, toUse);
        srcAH.highlighted = true;
      }

      if(!tgtAH.highlighted) {
        Object obj = tgtPane.getHighlighter().addHighlight(tgtAH.start,
                tgtAH.end,
                new DefaultHighlighter.DefaultHighlightPainter(toUse));
        tgtAH.setAlignedHighlight(obj, toUse);
        tgtAH.highlighted = true;
      }
    }
    catch(BadLocationException e) {
      throw new GateRuntimeException("Error while adding highlights ", e);
    }
  }

  public void annotationsUnaligned(Annotation srcAnnotation,
          Document srcDocument, Annotation tgtAnnotation, Document tgtDocument) {

    if(srcAnnotation == null || tgtAnnotation == null || srcDocument == null
            || tgtDocument == null) {
      System.err.println("One of the src/tgt annotation/document is null");
      return;
    }

    int indexOfSrcPane = documentIDs.indexOf(srcDocument.getName());
    if(indexOfSrcPane < 0) {
      System.err.println("Invalid document for alignment "
              + srcDocument.getName());
      return;
    }

    int indexOfTgtPane = documentIDs.indexOf(tgtDocument.getName());
    if(indexOfTgtPane < 0) {
      System.err.println("Invalid document for alignment "
              + tgtDocument.getName());
      return;
    }

    JEditorPane srcPane = tas[indexOfSrcPane];
    JEditorPane tgtPane = tas[indexOfTgtPane];

    Map<Annotation, AnnotationHighlight> srcDocHighlights = this.highlights
            .get(srcPane);
    Map<Annotation, AnnotationHighlight> tgtDocHighlights = this.highlights
            .get(tgtPane);

    AnnotationHighlight srcAH = srcDocHighlights.get(srcAnnotation);
    AnnotationHighlight tgtAH = tgtDocHighlights.get(tgtAnnotation);
    if(srcAH == null || tgtAH == null) return;

    if(srcAH.highlighted) {
      srcPane.getHighlighter().removeHighlight(srcAH.getAlignedHighlight());
      srcAH.setAlignedHighlight(null, null);
      srcAH.highlighted = false;
      srcAH.color = null;
    }

    if(tgtAH.highlighted) {
      tgtPane.getHighlighter().removeHighlight(tgtAH.getAlignedHighlight());
      tgtAH.setAlignedHighlight(null, null);
      tgtAH.highlighted = false;
      tgtAH.color = null;
    }
  }

  private void readActions() {
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
            Icon icon = aa.getIcon();

            final JMenuItem menuItem;
            if(caption == null && icon == null) {
              menuItem = new JMenuItem(aa.getClass().getName());
            }
            else if(caption == null) {
              menuItem = new JMenuItem(icon);
            }
            else {
              menuItem = new JMenuItem(caption);
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
}
