/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  LuceneDataStoreSearchAlternativeGUI.java
 *
 *  Thomas Heitz, Dec 11, 2007
 *
 *  $Id: $
 */


package gate.gui;

import gate.creole.annic.Constants;
import gate.creole.annic.Hit;
import gate.creole.annic.PatternAnnotation;
import gate.creole.annic.Pattern;
import gate.creole.annic.SearchException;
import gate.creole.annic.Searcher;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.*;

import gate.*;
import gate.corpora.SerialCorpusImpl;
import gate.creole.AbstractVisualResource;
import gate.event.DatastoreEvent;
import gate.event.DatastoreListener;
import gate.event.ProgressListener;
import gate.gui.MainFrame;
import gate.persist.LuceneDataStoreImpl;
import gate.persist.PersistenceException;
import gate.swing.XJTable;
import gate.util.GateRuntimeException;

/**
 * Shows the results of a IR query. This VR is associated to
 * {@link gate.creole.ir.SearchPR}.
 */
public class LuceneDataStoreSearchAlternativeGUI extends AbstractVisualResource
               implements ProgressListener, DatastoreListener {

  private static final long serialVersionUID = 3256720688877285686L;

  /** The GUI is associated with the AnnicSearchPR */
  private Object target;

  /**
   * arraylist consist of instances of patterns associated found in the
   * document
   */
  private List<Hit> patterns;

  /**
   * A Map that contains annotation types as the key values and the
   * corresponding arraylist consists of features
   */
  private Map<String, List<String>> allAnnotTypesAndFeaturesFromDatastore;

  /**
   * Populated annotationTypesAndFeatures
   */
  private Map<String, Set<String>> populatedAnnotationTypesAndFeatures;

  /** Table that lists the patterns found by the query */
  private XJTable patternTable;

  /**
   * Model of the patternTable.
   */
  private PatternsTableModel patternsTableModel;

  /**
   * Contains the shortcuts for annotation type / feature saved by the user.
   */
  private JComboBox shortcutsBox;

  /** Combobox to list the types */
  private JComboBox annotTypesBox;

  private JComboBox featuresBox;

  /**
   * This is to remember the previously selected annotation type in the
   * drop-down combobox
   */
  private String previousChoice = "";

  /** Button to allow addition of annotations and features */
  private JButton addAnnotTypeButton;

  /**
   * Button that allows retrieving next n number of results
   */
  private JButton nextResults;

  /** Button to execute a new Query */
  private JButton executeQuery;

  /** Button to clear the newQueryQuery Text Field */
  private JButton clearQueryTF;

  /** Button to export results into an HTML file */
  private JButton exportToHTML;

  /**
   * group for allPatterns and selectedPatterns, only one of them should
   * be selected at a time
   */
  private ButtonGroup patternExportButtonsGroup;

  /** Export all pattern to html */
  private JRadioButton allPatterns;

  /** Export only selectedPatterns to html */
  private JRadioButton selectedPatterns;

  /** Text Field that holds the query */
  private JTextField newQuery;

  /**
   * Which corpus to use when searching in
   */
  private JComboBox corpusToSearchIn;

  /**
   * Which annotation set to search in.
   */
  private JComboBox annotationSetToSearchIn;

  /**
   * We maintain a list of IDs available in datastore
   */
  private List<Object> corpusIds;

  /**
   * AnnotationSet IDS the structure is: CorpusID;annotationSetName
   */
  private String[] annotationSetIDsFromDataStore;

  /**
   * User will specify the noOfPatternsToSearch here
   */
  private JTextField noOfPatternsTF;

  /**
   * No Of tokens to be shown in context window
   */
  private JTextField contextWindowTF;

  /** Label */
  private JLabel queryToExecute;

  /**
   * Gives the page number displayed in the results.
   */
  private JLabel titleResults;

  /**
   * Number of the page of results.
   */
  private int pageOfResults;

  /** JPanel that contains the center panel of pattern rows. */
  private JPanel guiPanel;

  /** JPanel that contains the drop down lists for choosing annotation
   * type and feature, buttons to add and export the patterns. */
  private JPanel comboPanel;

  /**
   * JPanel that contains the top panel of drop down lists and buttons for
   * writing a query, executing it, select a corpus/annotation set,
   * select/export a pattern. 
   */
  private JPanel topPanel;
  
  /** Added Annotation Types */
  private ArrayList<String> addedAnnotTypesInGUI;

  /** Added Annotation features */
  private ArrayList<String> addedAnnotFeatureInGUI;

  /** Gridbagconstraints for the guiPanel */
  private GridBagConstraints guiCons;

  /** Color Generator */
  private gate.swing.ColorGenerator colorGenerator = new gate.swing.ColorGenerator();

  /** Instance of ExecuteQueryAction */
  private ExecuteQueryAction execQueryAction;

  /**
   * Instance of NextResultAction
   */
  private NextResultAction nextResultAction;

  /** Instance of ClearQueryAction */
  private ClearQueryAction clearQueryAction;

  /** Instance of ExportResultsAction */
  private ExportResultsAction exportResultsAction;

  /**
   * A working wheel to indicate search is going on.
   */
  private JLabel progressLabel;

  /**
   * Instance of this class.
   */
  LuceneDataStoreSearchAlternativeGUI thisInstance;

  /**
   * Current instance of the feature manager.
   */
  private FeaturesManager featuresManager;

  /**
   * Hashtable that contains the annotation type + feature as key and
   * the shortcut as value.
   */
  private HashMap<String, String> featuresShortcuts;

  /**
   * Searcher object obtained from the datastore
   */
  private Searcher searcher;


  /**
   * Called when a View is loaded in GATE.
   */
  public Resource init() {
    // initialize maps
    patterns = new ArrayList<Hit>();
    allAnnotTypesAndFeaturesFromDatastore = new HashMap<String, List<String>>();
    addedAnnotTypesInGUI = new ArrayList<String>();
    addedAnnotFeatureInGUI = new ArrayList<String>();
    thisInstance = this;
    corpusIds = new ArrayList<Object>();
    populatedAnnotationTypesAndFeatures = new HashMap<String, Set<String>>();
    featuresShortcuts = new HashMap<String, String>();

    // initialize GUI
    initGui();

    // unless the AnnicSerachPR is initialized, we don't have any data
    // to
    // show
    if(target != null) {
      if(target instanceof Searcher) {
        searcher = (Searcher)target;
      }
      else if(target instanceof LuceneDataStoreImpl) {
        searcher = ((LuceneDataStoreImpl)target).getSearcher();
      }
      else {
        throw new GateRuntimeException("Invalid target specified for the GUI");
      }

      initLocalData();
      updateGui();
      patternsTableModel.fireTableDataChanged();
      if(patternTable.getRowCount() > 0) {
        patternTable.setRowSelectionInterval(0, 0);
        tableValueChanged();
      }
    }
    validate();
    return this;
  }

  /**
   * Initialize the GUI.
   */
  protected void initGui() {

    setLayout(new BorderLayout());

    /*************
     * Top panel *
     *************/

    topPanel = new JPanel(new GridBagLayout());
    topPanel.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridwidth = 1;

    // first line of top panel
    gbc.gridy = 0;
    topPanel.add(new JLabel("Corpus :"), gbc);
    DefaultComboBoxModel corpusToSearchInModel = new DefaultComboBoxModel();
    corpusToSearchInModel.addElement("Entire DataStore");
    corpusToSearchIn = new JComboBox(corpusToSearchInModel);
    corpusToSearchIn.setPrototypeDisplayValue("Entire DataStore   ");
    corpusToSearchIn.setToolTipText("Corpus Name");
    if(target == null || target instanceof Searcher) {
      corpusToSearchIn.setEnabled(false);
    }
    corpusToSearchIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ie) {
        updateAnnotationSetsToSearchInBox();
      }
    });
    topPanel.add(corpusToSearchIn, gbc);
    topPanel.add(new JLabel("AnnotationSet :"), gbc);
    DefaultComboBoxModel annotationSetToSearchInModel =
      new DefaultComboBoxModel();
    annotationSetToSearchInModel.addElement(Constants.ALL_SETS);
    annotationSetToSearchIn = new JComboBox(annotationSetToSearchInModel);
    annotationSetToSearchIn
            .setPrototypeDisplayValue("Results from combined Sets");
    annotationSetToSearchIn.setToolTipText("AnnotationSet Name");
    annotationSetToSearchIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ie) {
        updateAnnotationTypeBox();
      }
    });
    topPanel.add(annotationSetToSearchIn, gbc);
    JLabel contextWindowLabel = new JLabel("Context size: ");
    topPanel.add(contextWindowLabel, gbc);
    contextWindowTF = new JTextField("5", 2);
    contextWindowTF
            .setToolTipText("Number of Tokens to be displayed in context");
    contextWindowTF.setEnabled(true);
    topPanel.add(contextWindowTF, gbc);

    // second line of top panel
    gbc.gridy = 1;
    queryToExecute = new JLabel("Query: ");
    topPanel.add(queryToExecute, gbc);
    newQuery = new JTextField(10);
    newQuery.setEnabled(true);
    newQuery.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // pressing enter in the query text field execute the query
        executeQuery.doClick();        
      }
    });
    gbc.gridwidth = 3;
    topPanel.add(newQuery, gbc);
    execQueryAction = new ExecuteQueryAction();
    executeQuery = new JButton();
    executeQuery.setBorderPainted(false);
    executeQuery.setContentAreaFilled(false);
    executeQuery.setAction(execQueryAction);
    executeQuery.setEnabled(true);
    topPanel.add(executeQuery, gbc);
    clearQueryAction = new ClearQueryAction();
    clearQueryTF = new JButton();
    clearQueryTF.setBorderPainted(false);
    clearQueryTF.setContentAreaFilled(false);
    clearQueryTF.setAction(clearQueryAction);
    clearQueryTF.setEnabled(true);
    topPanel.add(clearQueryTF, gbc);

//    JScrollPane topScrollPane = new JScrollPane(topPanel);
//    topScrollPane.setOpaque(false);
//
//    add(topScrollPane, BorderLayout.NORTH);
    add(topPanel, BorderLayout.NORTH);

    /****************
     * Center panel *
     ****************/

    guiCons = new GridBagConstraints();

    guiPanel = new JPanel();
    guiPanel.setLayout(new GridBagLayout());
    guiPanel.setOpaque(true);
    guiPanel.setBackground(Color.WHITE);

    shortcutsBox = new JComboBox();
    shortcutsBox.setBackground(Color.WHITE);
    if (featuresShortcuts.size() > 0) {
      DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(
            featuresShortcuts.values().toArray());
      shortcutsBox.setModel(comboBoxModel);
    }
    annotTypesBox = new JComboBox();
    annotTypesBox.setBackground(Color.WHITE);
    annotTypesBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        updateFeaturesBox();
      }
    });
    featuresBox = new JComboBox();
    featuresBox.setBackground(Color.WHITE);
    addAnnotTypeButton = new JButton();
    addAnnotTypeButton.setBorderPainted(false);
    addAnnotTypeButton.setContentAreaFilled(false);
    addAnnotTypeButton.setAction(new AddAnnotTypeAction());
    addAnnotTypeButton.setBackground(Color.WHITE);

    progressLabel = new JLabel(MainFrame.getIcon("working"));
    progressLabel.setOpaque(false);
    progressLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
//    progressLabel.setEnabled(false);
    progressLabel.setEnabled(true);

    // will be added to the GUI via a split panel

    /****************
     * Bottom panel *
     ****************/

    JPanel bottomPanel = new JPanel(new GridBagLayout());
    bottomPanel.setOpaque(false);
    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridwidth = 1;

    // title of the table, results options, export and next results button
    gbc.gridy = 0;
    titleResults = new JLabel("Results");
    bottomPanel.add(titleResults, gbc);
    nextResultAction = new NextResultAction();
    nextResults = new JButton();
    nextResults.setBorderPainted(false);
    nextResults.setContentAreaFilled(false);
    nextResults.setAction(nextResultAction);
    nextResults.setEnabled(true);
    bottomPanel.add(nextResults, gbc);
    JLabel noOfPatternsLabel = new JLabel("Number of results: ");
    bottomPanel.add(noOfPatternsLabel, gbc);
    noOfPatternsTF = new JTextField("50", 3);
    noOfPatternsTF.setToolTipText("Number of Patterns to retrieve");
    noOfPatternsTF.setEnabled(true);
    bottomPanel.add(noOfPatternsTF, gbc);
    bottomPanel.add(new JLabel("Export patterns:"), gbc);
    allPatterns = new JRadioButton("All");
    allPatterns.setToolTipText("exports all the patterns on this screen");
    allPatterns.setSelected(true);
    allPatterns.setEnabled(true);
    bottomPanel.add(allPatterns, gbc);
    selectedPatterns = new JRadioButton("Selected");
    selectedPatterns.setToolTipText("exports only the selected patterns");
    selectedPatterns.setSelected(false);
    selectedPatterns.setEnabled(true);
    bottomPanel.add(selectedPatterns, gbc);
    patternExportButtonsGroup = new ButtonGroup();
    patternExportButtonsGroup.add(allPatterns);
    patternExportButtonsGroup.add(selectedPatterns);
    exportResultsAction = new ExportResultsAction();
    exportToHTML = new JButton();
    exportToHTML.setBorderPainted(false);
    exportToHTML.setContentAreaFilled(false);
    exportToHTML.setAction(exportResultsAction);
    exportToHTML.setEnabled(true);
    bottomPanel.add(exportToHTML, gbc);

    // table of results
    patternsTableModel = new PatternsTableModel();

    patternTable = new XJTable(patternsTableModel);
    // user should see the respective pattern query for the underlying
    // row
    patternTable.addMouseMotionListener(new MouseMotionListener() {
      public void mouseMoved(MouseEvent me) {
        int row = patternTable.rowAtPoint(me.getPoint());
        row = patternTable.rowViewToModel(row);
        Pattern pattern = null;
        if(row > -1) {
          pattern = (Pattern)patterns.get(row);
          patternTable.setToolTipText(pattern.getQueryString());
        }
      }

      public void mouseDragged(MouseEvent me) {
      }
    });

    patternTable.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent me) {
        if(SwingUtilities.isRightMouseButton(me)) {
          // if yes show the option to delete
          final JPopupMenu popup = new JPopupMenu();
          JButton delete = new JButton("Delete");
          popup.add(delete);
          delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              int[] rows = patternTable.getSelectedRows();
              for(int i = 0; i < rows.length; i++) {
                rows[i] = patternTable.rowViewToModel(rows[i]);
              }

              rows = sortRows(rows);
              // here all rows are in ascending order
              for(int i = rows.length - 1; i >= 0; i--) {
                patterns.remove(rows[i]);
              }
              patternsTableModel.fireTableDataChanged();
              // and finally update the table
              tableValueChanged();
              popup.setVisible(false);
            }
          });
          popup.show(patternTable, me.getX(), me.getY() - 10);
        }
      }

      public void mousePressed(MouseEvent me) {
      }

      public void mouseReleased(MouseEvent me) {
      }

      public void mouseEntered(MouseEvent me) {
      }

      public void mouseExited(MouseEvent me) {
      }
    });

    // when user changes his/her selection in the rows,
    // the graphical panel should change its ouput to reflect the new
    // selection. incase where multiple rows are selected
    // the annotations of the first row will be highlighted
    patternTable.getSelectionModel().addListSelectionListener(
            new javax.swing.event.ListSelectionListener() {
              public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                tableValueChanged();
              }
            });

    // user should be allowed to select multiple rows
    patternTable
            .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    patternTable.setSortable(true);

    JScrollPane tableScrollPane = new JScrollPane(patternTable,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    gbc.gridy = 1;
    gbc.gridx= 0;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    bottomPanel.add(tableScrollPane, gbc);

    // will be added to the GUI via a split panel

    /***********************************************
     * Split between center panel and bottom panel *
     ***********************************************/

    JSplitPane centerBottomSplitPane =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    centerBottomSplitPane.setDividerLocation(350);
    centerBottomSplitPane.add(new JScrollPane(guiPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    centerBottomSplitPane.add(bottomPanel);

    add(centerBottomSplitPane, BorderLayout.CENTER);

  }

  // initialize the local data
  protected void initLocalData() {
    Hit[] pats = searcher.getHits();
    if(patterns == null) patterns = new ArrayList<Hit>();
    patterns.clear();
    for(int m = 0; m < pats.length; m++) {
      patterns.add(pats[m]);
    }
    pats = null;
  }

  /**
   * Initializes the comboboxes for annotation Types and their features.
   */
  public void updateGui() {
    guiPanel.removeAll();
    guiPanel.validate();
    guiPanel.updateUI();
    newQuery.setText(searcher.getQuery());
  }

  /**
   * Updates the pattern row view when the user changes
   * his/her selection of pattern in the patternTable.
   */
  public void tableValueChanged() {

    // clear the pattern row
    guiPanel.removeAll();

    guiCons.gridx = 0;
    guiCons.gridy = 0;
    guiCons.gridwidth = 1;
    guiCons.gridheight = 1;
    guiCons.fill = GridBagConstraints.BOTH;
    guiCons.insets = new java.awt.Insets(0, 0, 0, 0);
    guiCons.anchor = GridBagConstraints.CENTER;
    guiCons.weighty = 0.0;
    guiCons.weightx = 0.0;

    // get the row selected in the pattern table
    int row = patternTable.getSelectedRow();

    if (row == -1) { // no pattern is selected in the pattern table
      guiPanel.add(new JLabel(
              "Please select a row in the pattern table."), guiCons);
      guiPanel.validate();
      return;
    }

    shortcutsBox.setEnabled(true);
    annotTypesBox.setEnabled(true);
    featuresBox.setEnabled(true);
    addAnnotTypeButton.setEnabled(true);

    // in case the user has sorted a column in the pattern table
    row = patternTable.rowViewToModel(row);

    Pattern pattern = (Pattern)patterns.get(row);

    // display on the first line the text matching the pattern and its context
    // we display one character per cell
    guiCons.gridx = 0;
    guiCons.insets = new java.awt.Insets(0, 0, 0, 4);
    guiPanel.add(new JLabel("Text"), guiCons);
    guiCons.insets = new java.awt.Insets(0, 0, 0, 0);
    PatternAnnotation[] baseUnitPatternAnnotations =
      pattern.getPatternAnnotations("Token", "string");
    //TODO: why Constants.BASE_TOKEN_ANNOTATION_TYPE is null/empty ?
//    pattern.getPatternAnnotations(((String)searcher.getParameters()
//            .get(Constants.BASE_TOKEN_ANNOTATION_TYPE)), "string");
//    System.out.println("BASE_TOKEN_ANNOTATION_TYPE = "+searcher.getParameters()
//          .get(Constants.BASE_TOKEN_ANNOTATION_TYPE));
    int BaseUnitNum = 0;
    String baseUnit =
      ((PatternAnnotation)baseUnitPatternAnnotations[BaseUnitNum]).getText();

    for (int charNum = 0;
         charNum < pattern.getPatternText().length(); charNum++) {

      guiCons.gridx = charNum + 1;
      JLabel label = new JLabel(String.valueOf(
                (pattern.getPatternText().charAt(charNum))));
      if (charNum >= pattern.getStartOffset()
                   - pattern.getLeftContextStartOffset()
       && charNum < pattern.getEndOffset()
                  - pattern.getLeftContextStartOffset()) {
        // this part is matched by the pattern, color it
        label.setBackground(new Color(240, 201, 184));
      } else {
        // this part is the context, no color
        label.setBackground(Color.WHITE);
      }
      label.setOpaque(true);

      if (((PatternAnnotation)baseUnitPatternAnnotations[BaseUnitNum])
           .getEndOffset() - pattern.getLeftContextStartOffset() < charNum) {
        BaseUnitNum++;
        // get the next base token unit
        baseUnit = ((PatternAnnotation)
                baseUnitPatternAnnotations[BaseUnitNum]).getText();
      }
      label.addMouseListener(
              new AddPatternRowInQueryMouseInputListener(baseUnit));
      guiPanel.add(label, guiCons);
    }

    // for each annotation type / feature to display
    for(int i = 0; i < addedAnnotTypesInGUI.size(); i++) {
      String type = (String)addedAnnotTypesInGUI.get(i);
      String feature = (String)addedAnnotFeatureInGUI.get(i);

      guiCons.gridy++;
      guiCons.gridx = 0;

      // add the annotation type / feature header
      JLabel annotationTypeAndFeature = new JLabel();
      annotationTypeAndFeature.setText(
        (featuresShortcuts.containsKey(type+"."+feature))?
        featuresShortcuts.get(type+"."+feature):type+"."+feature);
      annotationTypeAndFeature.setToolTipText(
              "Click for replacing with a shortcut.");
      annotationTypeAndFeature.setName(type+"."+feature);
      annotationTypeAndFeature.addMouseListener(
              new javax.swing.event.MouseInputAdapter() {
        public void mouseClicked(MouseEvent me) {
          String inputValue = JOptionPane.showInputDialog(
            "Please, give a shortcut for \""
            +((JLabel)me.getSource()).getName()+"\".", 
            ((JLabel)me.getSource()).getText());
          if (inputValue != null && !inputValue.trim().equals("")) {
            featuresShortcuts.put(
                    ((JLabel)me.getSource()).getName(), inputValue);
            DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(
                    featuresShortcuts.values().toArray());
            shortcutsBox.setModel(comboBoxModel);
            tableValueChanged();
          }
        }
      });
      guiCons.insets = new java.awt.Insets(0, 0, 0, 4);
      guiPanel.add(annotationTypeAndFeature, guiCons);
      guiCons.insets = new java.awt.Insets(0, 0, 0, 0);

        // get the annotation type / feature to display
        PatternAnnotation[] annots =
          pattern.getPatternAnnotations(type, feature);
        if(annots == null || annots.length == 0) {
          annots = new PatternAnnotation[0];
        }

        // add a JLabel in the gridbag layout for each feature
        // of the current annotation type
        for(int k = 0, j = 0; k < annots.length; k++, j += 2) {
          PatternAnnotation ann = (PatternAnnotation)annots[k];
          guiCons.gridx =
            ann.getStartOffset() - pattern.getLeftContextStartOffset() + 1;
          guiCons.gridwidth =
            ann.getEndOffset() - pattern.getLeftContextStartOffset()
            - guiCons.gridx + 1;
          JLabel label = new JLabel((String)ann.getFeatures().get(feature));
          label.setBackground(getAnnotationTypeColor(ann.getType()));
          label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
          label.setOpaque(true);
          label.addMouseListener(new AddPatternRowInQueryMouseInputListener(
                  type, feature, (String)ann.getFeatures().get(feature)));
          guiPanel.add(label, guiCons);
        }

        // add a remove button at the end of the row
        JButton removePattern;
        removePattern = new JButton();
        removePattern.setAction(new DeletePatternRowAction(type, feature));
        removePattern.setBorderPainted(true);
        removePattern.setMargin(new Insets(0, 0, 0, 0));
        guiCons.gridwidth = 1;
        // last cell of the row
        guiCons.gridx = pattern.getPatternText().length() + 1;
        guiPanel.add(removePattern, guiCons);
    }

    guiCons.gridy++;
    guiCons.gridwidth = GridBagConstraints.REMAINDER;
    guiCons.gridx = 0;

    // add a features manager button on the last row
//    JButton featureManagerButton;
//    featureManagerButton = new JButton();
//    featureManagerButton.setAction(new ManageFeaturesToDisplayAction());
//    featureManagerButton.setBorderPainted(true);
//    featureManagerButton.setMargin(new Insets(0, 0, 0, 0));
//    guiPanel.add(featureManagerButton, guiCons);

    // add drop down boxes to add a new annotation type / feature row
    JPanel annotationTypeFeaturePanel =
      new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
    annotationTypeFeaturePanel.add(new JLabel("Shortcut : "));
    annotationTypeFeaturePanel.add(shortcutsBox);
    annotationTypeFeaturePanel.add(new JLabel("Annotation Types : "));
    annotationTypeFeaturePanel.add(annotTypesBox);
    annotationTypeFeaturePanel.add(new JLabel("Features : "));
    annotationTypeFeaturePanel.add(featuresBox);
    annotationTypeFeaturePanel.add(addAnnotTypeButton);
    annotationTypeFeaturePanel.setBackground(Color.WHITE);
    annotationTypeFeaturePanel.setOpaque(true);
    guiPanel.add(annotationTypeFeaturePanel, guiCons);
    guiPanel.setOpaque(true);

    validate();
    updateUI();
  }

  /**
   * Initialize the local data (i.e. Pattern data etc.) and then update
   * the GUI
   */
  protected void updateDisplay() {
    // initialize maps
    patterns.clear();
//    addedAnnotTypesInGUI.clear();
//    addedAnnotFeatureInGUI.clear();

    // if target itself is null, we don't want to update anything
    if(target != null) {
      initLocalData();
      updateGui();
      // change the totalFoundLabel
      patternsTableModel.fireTableDataChanged();
      if(patternTable.getRowCount() > 0) {
        patternTable.setRowSelectionInterval(0, 0);
        tableValueChanged();
      }

      String query = newQuery.getText();
      ArrayList<String> toAdd = new ArrayList<String>();

      if(query.length() > 0 && !patterns.isEmpty()) {
        String[] posStart = new String[] {"{", ",", " "};
        String[] posEnd = new String[] {"}", ".", ",", " ", "="};

        // lets go through annotations in the addedAnnotTypes
        outer:for(String annotType :
                    populatedAnnotationTypesAndFeatures.keySet()) {
          for(String start : posStart) {
            for(String end : posEnd) {
              String toSearch = start + annotType + end;
              if(query.indexOf(toSearch) > -1) {
                toAdd.add(annotType);
                continue outer;
              }
            }
          }
        }

        if(!toAdd.isEmpty()) {
          String featureType = "nothing";
          for(String at : toAdd) {

            boolean add = true;
            int index = addedAnnotTypesInGUI.indexOf(at);
            if(index > -1) {
              if(addedAnnotFeatureInGUI.get(index).equals(featureType)) {
                add = false;
              }
            }

            if(add) {
              addedAnnotTypesInGUI.add(at);
              addedAnnotFeatureInGUI.add(featureType);
            }
            else {
              continue;
            }
          }

          // update the gui
          patternTable.setRowSelectionInterval(patternTable.getSelectedRow(),
                  patternTable.getSelectedRow());
          tableValueChanged();
        }
      }
    }
  }

  private void updateAnnotationSetsToSearchInBox() {
    String corpusName = (String)(corpusToSearchIn.getSelectedIndex() == 0
            ? null
            : corpusIds.get(corpusToSearchIn.getSelectedIndex() - 1));

    // obtain annotationsetnames available for this corpus
    final Set<String> annotSetNames = getAnnotationSetNames(corpusName);
    // populate them in the default combobox model
    DefaultComboBoxModel aNewModel = new DefaultComboBoxModel(annotSetNames
            .toArray());
    // add the all sets element at the top
    aNewModel.insertElementAt(Constants.ALL_SETS, 0);
    annotationSetToSearchIn.setModel(aNewModel);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        annotationSetToSearchIn.updateUI();
        // if there are more than one annotation sets why not
        // refresh the annotationTypesBox as well
        if(annotSetNames.size() > 0) {
          updateAnnotationTypeBox();
        }
      }
    });
  }

  private void updateAnnotationTypeBox() {
    String corpusName = (String)(corpusToSearchIn.getSelectedIndex() == 0
            ? null
            : corpusIds.get(corpusToSearchIn.getSelectedIndex() - 1));
    int selectedAnnotationSetIndex = annotationSetToSearchIn.getSelectedIndex();
    String annotationSetName = (String)(selectedAnnotationSetIndex == 0
            ? null
            : annotationSetToSearchIn.getSelectedItem());
    populatedAnnotationTypesAndFeatures = getAnnotTypesFeatures(corpusName,
            annotationSetName);

    // we need to update the annotTypesBox
    List<String> annotTypes = new ArrayList<String>();
    if(populatedAnnotationTypesAndFeatures != null
    && populatedAnnotationTypesAndFeatures.keySet() != null) {
      annotTypes.addAll(populatedAnnotationTypesAndFeatures.keySet());
    }
    Collections.sort(annotTypes);
    DefaultComboBoxModel aNewModel = new DefaultComboBoxModel(
            annotTypes.toArray());
    annotTypesBox.setModel(aNewModel);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        annotTypesBox.updateUI();
        if(annotTypesBox.getItemCount() > 0)
          updateFeaturesBox();
        else {
          ((DefaultComboBoxModel)featuresBox.getModel()).removeAllElements();
          featuresBox.updateUI();
        }
      }
    });
  }

  private void updateFeaturesBox() {
    String choice = (String)annotTypesBox.getSelectedItem();
    if(choice != null && !choice.equals(previousChoice)) {
      previousChoice = choice;
      // yes we need to update the featuresBox
      List<String> features = new ArrayList<String>();
      Set<String> featuresToAdd = populatedAnnotationTypesAndFeatures
              .get(choice);
      if(featuresToAdd != null) {
        features.addAll(featuresToAdd);
      }
      Collections.sort(features);
      // and finally update the featuresBox
      featuresBox.removeAllItems();
      featuresBox.addItem("All");
      for(String aFeat : features) {
        featuresBox.addItem(aFeat);
      }
      featuresBox.updateUI();
    }
  }

  /**
   * Sort use for the pattern table.
   * 
   * @param rows table of rows to sort
   * @return rows sorted 
   */
  private int[] sortRows(int[] rows) {
    for(int i = 0; i < rows.length; i++) {
      for(int j = 0; j < rows.length - 1; j++) {
        if(rows[j] > rows[j + 1]) {
          int temp = rows[j];
          rows[j] = rows[j + 1];
          rows[j + 1] = temp;
        }
      }
    }
    return rows;
  }

  /**
   * This method uses the java.util.prefs.Preferences and get the color
   * for particular annotationType.. This color could have been saved by
   * the AnnotationSetsView
   * 
   * @param annotationType
   * @return
   */
  private Color getAnnotationTypeColor(String annotationType) {
    java.util.prefs.Preferences prefRoot = null;
    try {
      prefRoot = java.util.prefs.Preferences.userNodeForPackage(Class
              .forName("gate.gui.docview.AnnotationSetsView"));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    int rgba = prefRoot.getInt(annotationType, -1);
    Color colour;
    if(rgba == -1) {
      // initialise and save
      float components[] = colorGenerator.getNextColor().getComponents(null);
      colour = new Color(components[0], components[1], components[2], 0.5f);
      int rgb = colour.getRGB();
      int alpha = colour.getAlpha();
      rgba = rgb | (alpha << 24);
      prefRoot.putInt(annotationType, rgba);

    }
    else {
      colour = new Color(rgba, true);
    }
    return colour;
  }

  private Set<String> getAnnotationSetNames(String corpusName) {
    Set<String> toReturn = new HashSet<String>();

    if(corpusName == null) {
      for(String aSet : annotationSetIDsFromDataStore) {
        aSet = aSet.substring(aSet.indexOf(';') + 1);
        toReturn.add(aSet);
      }
    }
    else {
      for(String aSet : annotationSetIDsFromDataStore) {
        if(aSet.startsWith(corpusName + ";")) {
          aSet = aSet.substring(aSet.indexOf(';') + 1);
          toReturn.add(aSet);
        }
      }
    }
    return toReturn;
  }

  private Map<String, Set<String>> getAnnotTypesFeatures(String corpusName,
          String annotationSetName) {
    HashMap<String, Set<String>> toReturn = new HashMap<String, Set<String>>();
    if(corpusName == null && annotationSetName == null) {
      // we simply go through all the annotTyes
      // remove corpusID;annotationSetID; from it
      for(String type : allAnnotTypesAndFeaturesFromDatastore.keySet()) {
        String annotation = type.substring(type.lastIndexOf(';') + 1);
        Set<String> features = toReturn.get(annotation);
        if(features == null) {
          features = new HashSet<String>();
          toReturn.put(annotation, features);
        }
        features.addAll(allAnnotTypesAndFeaturesFromDatastore.get(type));
      }
    }
    else if(corpusName == null && annotationSetName != null) {
      // we simply go through all the annotTyes
      // remove corpusID;annotationSetID; from it
      for(String type : allAnnotTypesAndFeaturesFromDatastore.keySet()) {
        String annotation = type.substring(type.indexOf(';') + 1);
        if(annotation.startsWith(annotationSetName + ";")) {
          annotation = annotation.substring(annotation.indexOf(';') + 1);
          Set<String> features = toReturn.get(annotation);
          if(features == null) {
            features = new HashSet<String>();
            toReturn.put(annotation, features);
          }
          features.addAll(allAnnotTypesAndFeaturesFromDatastore.get(type));
        }
      }
    }
    else if(corpusName != null && annotationSetName == null) {
      // we simply go through all the annotTyes
      // remove corpusID;annotationSetID; from it
      for(String type : allAnnotTypesAndFeaturesFromDatastore.keySet()) {
        if(type.startsWith(corpusName + ";")) {
          String annotation = type.substring(type.lastIndexOf(';') + 1);
          Set<String> features = toReturn.get(annotation);
          if(features == null) {
            features = new HashSet<String>();
            toReturn.put(annotation, features);
          }
          features.addAll(allAnnotTypesAndFeaturesFromDatastore.get(type));
        }
      }
    }
    else {
      // we simply go through all the annotTyes
      // remove corpusID;annotationSetID; from it
      for(String type : allAnnotTypesAndFeaturesFromDatastore.keySet()) {
        if(type.startsWith(corpusName + ";" + annotationSetName + ";")) {
          String annotation = type.substring(type.lastIndexOf(';') + 1);
          Set<String> features = toReturn.get(annotation);
          if(features == null) {
            features = new HashSet<String>();
            toReturn.put(annotation, features);
          }
          features.addAll(allAnnotTypesAndFeaturesFromDatastore.get(type));
        }
      }
    }
    return toReturn;
  }

  /**
   * Adds the pattern Row gui for newly selected annotation type and
   * feature.
   */
  protected class AddAnnotTypeAction extends AbstractAction {

    private static final long serialVersionUID = 3256438118801225013L;

    public AddAnnotTypeAction() {
      super("", MainFrame.getIcon("annic-down"));
      super.putValue(SHORT_DESCRIPTION,
        "Add selected annotation type and feature to the visualization below.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_DOWN);
    }

    public void actionPerformed(ActionEvent ae) {
      // find out the selected annotation type and feature
      int index = annotTypesBox.getSelectedIndex();
      if(index < 0 && annotTypesBox.getItemCount() > 0) {
        index = 0;
      }
      String annotType = (String)annotTypesBox.getItemAt(index);

      index = featuresBox.getSelectedIndex();
      if(index < 0 && featuresBox.getItemCount() > 0) {
        index = 0;
      }

      String featureType = (String)featuresBox.getItemAt(index);
      if(featureType.equals("All")) {
        // add all the features
        for (int i = 1; i < featuresBox.getItemCount(); i++) {
          addedAnnotTypesInGUI.add(annotType);
          addedAnnotFeatureInGUI.add((String)featuresBox.getItemAt(i));
        }

      } else {
        // add only one feature

      boolean add = true;
        // is the feature already displayed ?
        for(int i = 0; i < addedAnnotTypesInGUI.size(); i++) {
          if(((String)addedAnnotTypesInGUI.get(i)).equals(annotType)) {
            if(((String)addedAnnotFeatureInGUI.get(i)).equals(featureType)) {
              add = false;
              break;
            }
          }
        }
        if(add) {
          addedAnnotTypesInGUI.add(annotType);
          addedAnnotFeatureInGUI.add(featureType);
        }
        else {
          JOptionPane.showMessageDialog(null,
                  "This feature is already displayed.");
          return;
        }
      }

      // update the gui
      patternTable.setRowSelectionInterval(patternTable.getSelectedRow(),
              patternTable.getSelectedRow());
      tableValueChanged();
    }
  }

  /**
   * Exports all patterns to the XML File.
   */
  protected class ExportResultsAction extends AbstractAction {

    private static final long serialVersionUID = 3257286928859412277L;

    public ExportResultsAction() {
      super("", MainFrame.getIcon("annic-export"));
      super.putValue(SHORT_DESCRIPTION, "Export Results to HTML.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_E);
    }

    public void actionPerformed(ActionEvent ae) {

      Map<Object, Object> parameters = searcher.getParameters();

      // if there are no pattern say so
      if(patterns == null || patterns.isEmpty()) {
        try {
          JOptionPane.showMessageDialog(gate.Main.getMainFrame(),
                  "No patterns found to export");
          return;
        }
        catch(Exception e) {
          e.printStackTrace();
          return;
        }
      }

      try {
        // otherwise we need to ask user a location for the file where
        // he wants to store results
        JFileChooser fileDialog = new JFileChooser();

        String fileDialogTitle = "HTML";
        fileDialog.setDialogTitle(fileDialogTitle
                + " File to export pattern results to...");

        JFrame frame = target instanceof Searcher ? null : gate.Main
                .getMainFrame();
        fileDialog.showSaveDialog(frame);
        java.io.File file = fileDialog.getSelectedFile();

        // if user pressed the cancel button
        if(file == null) return;

        java.io.FileWriter fileWriter = new java.io.FileWriter(file);

        // we need to output patterns to the HTML File
        // check if allPatterns is selected we need to reissue the
        // query when we re-issue our query, we do not update GUI
        // but use the same PR and everything to get results from
        // Annic Search hence, all the variables are modified
        // so take backup for the current patterns

        // here we have all patterns that we need to export
        // we store them in a temporary storage
        ArrayList<Hit> patternsToExport = new ArrayList<Hit>();

        // check if selectedPatterns is selected
        if(selectedPatterns.isSelected()) {
          // in this case we only export those patterns which are
          // selected by the user
          int[] rows = patternTable.getSelectedRows();
          for(int i = 0; i < rows.length; i++) {
            int num = patternTable.rowViewToModel(rows[i]);
            patternsToExport.add(patterns.get(num));
          }

        }
        else {
          // in this case we only export those patterns which are
          // selected by the user
          for(int i = 0; i < patternTable.getRowCount(); i++) {
            int num = patternTable.rowViewToModel(i);
            patternsToExport.add(patterns.get(num));
          }
        }

        // what we need to output is the
        // Issued Corpus Query
        // Pattern
        // Table
        // 1. Document it belongs to, 2. Left context, 3. Actual
        // Pattern Text, 4. Right context
        java.io.BufferedWriter bw = new java.io.BufferedWriter(fileWriter);
        // write header
        bw.write("<HTML><TITLE>ANNIC Output</TITLE><BODY>");
        bw.write("<BR><B>Query Issued: "
                + searcher.getQuery()
                + "<BR>Context Window :"
                + ((Integer)parameters.get(Constants.CONTEXT_WINDOW))
                        .intValue() + "</B><BR><BR>");
        bw.write("<BR><B>Queries:</B>");
        String queryString = "";
        for(int i = 0; i < patternsToExport.size(); i++) {
          Pattern ap = (Pattern)patternsToExport.get(i);
          if(!ap.getQueryString().equals(queryString)) {
            bw.write("<BR><a href=\"#" + ap.getQueryString() + "\">"
                    + ap.getQueryString() + "</a>");
            queryString = ap.getQueryString();
          }
        }

        bw.write("<BR><BR>");
        queryString = "";
        for(int i = 0; i < patternsToExport.size(); i++) {
          Pattern ap = (Pattern)patternsToExport.get(i);
          if(!ap.getQueryString().equals(queryString)) {
            if(!queryString.equals("")) {
              bw.write("</TABLE><BR><BR>");
            }
            queryString = ap.getQueryString();

            bw.write("<BR><B> <a name=\"" + ap.getQueryString()
                    + "\">Query Pattern : " + ap.getQueryString()
                    + "</a></B><BR>");
            bw.write("<BR><TABLE border=\"1\">");
            bw.write("<TR><TD><B> No. </B></TD>");
            bw.write("<TD><B> Document ID </B></TD>");
            bw.write("<TD><B> Left Context </B></TD>");
            bw.write("<TD><B> Pattern Text </B></TD>");
            bw.write("<TD><B> Right Context </B></TD>");
            bw.write("</TR>");
          }

          bw.write("<TR><TD>" + (i + 1) + "</TD>");
          bw.write("<TD>" + ap.getDocumentID() + "</TD>");
          bw.write("<TD>"
                  + ap.getPatternText(ap.getLeftContextStartOffset(), ap
                          .getStartOffset()) + "</TD>");
          bw.write("<TD>"
                  + ap.getPatternText(ap.getStartOffset(), ap.getEndOffset())
                  + "</TD>");
          bw.write("<TD>"
                  + ap.getPatternText(ap.getEndOffset(), ap
                          .getRightContextEndOffset()) + "</TD>");
          bw.write("</TR>");
        }
        bw.write("</TABLE></BODY></HTML>");
        bw.flush();
        bw.close();
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Clear the newQuery text box.
   */
  protected class ClearQueryAction extends AbstractAction {

    private static final long serialVersionUID = 3257569516199228209L;

    public ClearQueryAction() {
      super("", MainFrame.getIcon("annic-clean"));
      super.putValue(SHORT_DESCRIPTION, "Clear Query Text Box.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_BACK_SPACE);
    }

    public void actionPerformed(ActionEvent ae) {
      newQuery.setText("");
    }
  }

  /** 
   * Finds out the newly created query and execute it.
   */
  protected class ExecuteQueryAction extends AbstractAction {

    private static final long serialVersionUID = 3258128055204917812L;

    public ExecuteQueryAction() {
      super("", MainFrame.getIcon("annic-search"));
      super.putValue(SHORT_DESCRIPTION, "Execute Query.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_ENTER);
    }

    public void actionPerformed(ActionEvent ae) {

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // TODO: the progress icon doesn't work
          progressLabel.setEnabled(true);
          guiPanel.removeAll();
          guiCons = new GridBagConstraints();
//          guiCons.gridx = 0;
//          guiCons.gridy = 0;
//          guiCons.gridwidth = 1;
//          guiCons.gridheight = 1;
//          guiCons.fill = GridBagConstraints.BOTH;
//          guiCons.insets = new java.awt.Insets(0, 0, 0, 0);
//          guiCons.anchor = GridBagConstraints.CENTER;
//          guiCons.weighty = 1.0;
//          guiCons.weightx = 1.0;
          guiPanel.add(progressLabel, guiCons);
//          guiPanel.add(new JLabel("Query in progress..."), guiCons);

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              progressLabel.updateUI();

              thisInstance.setEnabled(false);
              Map<Object, Object> parameters = searcher.getParameters();
              if(parameters == null)
                parameters = new HashMap<Object, Object>();

              if(target instanceof LuceneDataStoreImpl) {
                URL indexLocationURL = (URL)((LuceneDataStoreImpl)target)
                        .getIndexer().getParameters().get(
                                Constants.INDEX_LOCATION_URL);
                String indexLocation = null;
                try {
                  indexLocation = new File(indexLocationURL.toURI())
                          .getAbsolutePath();
                } catch(URISyntaxException use) {
                  indexLocation = new File(indexLocationURL.getFile()).getAbsolutePath();
                }
                ArrayList<String> indexLocations = new ArrayList<String>();
                indexLocations.add(indexLocation);
                parameters.put(Constants.INDEX_LOCATIONS, indexLocations);

                int index = corpusToSearchIn.getSelectedIndex();
                String corpus2SearchIn = index == 0 ? null : (String)corpusIds
                        .get(index - 1);
                parameters.put(Constants.CORPUS_ID, corpus2SearchIn);
              }

              Integer noOfPatterns = new Integer(noOfPatternsTF.getText()
                      .trim());
              Integer contextWindow = new Integer(contextWindowTF.getText()
                      .trim());
              String query = newQuery.getText().trim();
              parameters.put(Constants.CONTEXT_WINDOW, contextWindow);
              int index = annotationSetToSearchIn.getSelectedIndex();
              if(index > 0) {
                String annotationSet = (String)annotationSetToSearchIn
                        .getSelectedItem();
                parameters.put(Constants.ANNOTATION_SET_ID, annotationSet);
              } else {
                parameters.remove(Constants.ANNOTATION_SET_ID);
              }

              try {
                if(searcher.search(query, parameters)) {
                  searcher.next(noOfPatterns.intValue());
                }
              } catch(Exception e) {
                e.printStackTrace();
                thisInstance.setEnabled(true);
                progressLabel.setEnabled(false);
//                guiPanel.removeAll();
//                progressLabel.updateUI();
              }
              processFinished();
              pageOfResults = 1;
              titleResults.setText("Results - Page "+pageOfResults);
              thisInstance.setEnabled(true);
              progressLabel.setEnabled(false);
//              guiPanel.removeAll();
//              progressLabel.updateUI();
            }
          });

        }
      });
    }
  }

  /**
   * Finds out the next few results.
   */
  protected class NextResultAction extends AbstractAction {

    private static final long serialVersionUID = 3257005436719871288L;

    public NextResultAction() {
      super("", MainFrame.getIcon("annic-forward"));
      super.putValue(SHORT_DESCRIPTION, "Show Next Patterns.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_RIGHT);
    }

    public void actionPerformed(ActionEvent ae) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {

          thisInstance.setEnabled(false);
          try {
            searcher.next(Integer.parseInt(noOfPatternsTF.getText()));
          }
          catch(Exception e) {
            e.printStackTrace();
            thisInstance.setEnabled(true);
          }
          processFinished();
          pageOfResults++;
          titleResults.setText("Results - Page "+pageOfResults);
          thisInstance.setEnabled(true);
        }
      });
    }
  }

  /**
   * Delete the current pattern row.
   */
  protected class DeletePatternRowAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private String type;
    private String feature;

    public DeletePatternRowAction(String type, String feature) {
      super("", MainFrame.getIcon("delete.gif"));
      super.putValue(SHORT_DESCRIPTION, "Delete this pattern row.");
      this.type = type;
      this.feature= feature;
    }

    public void actionPerformed(ActionEvent ae) {
    // we need to remove these things from the added stuff
    forLoop: for(int i = 0; i < addedAnnotTypesInGUI.size(); i++) {
      String type1 = (String)addedAnnotTypesInGUI.get(i);
      if(type1.equals(type)) {
        String f1 = (String)addedAnnotFeatureInGUI.get(i);
        if(feature.equals(f1)) {
          addedAnnotTypesInGUI.remove(i);
          addedAnnotFeatureInGUI.remove(i);
          break forLoop;
        }
      }
    }
    tableValueChanged();
  }
  }

  /**
   * Manage the features to display in the pattern row.
   */
  protected class ManageFeaturesToDisplayAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public ManageFeaturesToDisplayAction() {
      super("", MainFrame.getIcon("add.gif"));
      super.putValue(SHORT_DESCRIPTION, "Display Features Manager.");
    }

    public void actionPerformed(ActionEvent e) {
      featuresManager = new FeaturesManager("Features Manager");
      featuresManager.pack();
      featuresManager.setIconImage(
              ((ImageIcon)MainFrame.getIcon("add.gif")).getImage());
      featuresManager.setLocationRelativeTo(thisInstance);
      featuresManager.setVisible(true);
    }
  }

  /**
   * Modify the query with the current clicked pattern row.
   */
  protected class AddPatternRowInQueryMouseInputListener
    extends javax.swing.event.MouseInputAdapter {

    private String type;
    private String feature;
    private String text;

    public AddPatternRowInQueryMouseInputListener(
            String type, String feature, String text) {
      this.type = type;
      this.feature= feature;
      this.text = text;
    }

    public AddPatternRowInQueryMouseInputListener(String text) {
      this.type = null;
      this.feature= null;
      this.text = text;
    }

    public void mouseClicked(MouseEvent me) {
      int caretPosition = newQuery.getCaretPosition();
      if(caretPosition < 0) {
        caretPosition = newQuery.getText().length();
      }
      String query = newQuery.getText();
      if (type != null && feature != null) {
        // add {feature.type == value} in the query
        query = query.substring(0, caretPosition) + "{" + type
              + (feature.equals("nothing") ? "" : "." + feature) + "==\""
              + text + "\"}" + query.substring(caretPosition, query.length());
      } else {
        query = query.substring(0, caretPosition)
        + text
        + query.substring(caretPosition, query.length());
      }
      newQuery.setText(query);
    }
  }

  /**
   * Table model for the Pattern Tables.
   */
  protected class PatternsTableModel extends AbstractTableModel {

    /**
     * serial version id
     */
    private static final long serialVersionUID = 3977012959534854193L;

    /** Returns the number of rows (patterns) in the table */
    public int getRowCount() {
      return patterns.size();
    }

    /** Number of columns in table */
    public int getColumnCount() {
      return 5;
    }

    /** Column headings */
    public String getColumnName(int columnIndex) {
      switch(columnIndex) {
        case DOC_NAME_COLUMN:
          return "Document";
        case ANNOTATION_SET_NAME_COLUMN:
          return "AnnotationSet";
        case LEFT_CONTEXT_COLUMN:
          return "Left Context";
        case PATTERN_COLUMN:
          return "Pattern";
        case RIGHT_CONTEXT_COLUMN:
          return "Right Context";
        default:
          return "?";
      }
    }

    /** Returns the class of the column object */
    public Class<?> getColumnClass(int columnIndex) {
      switch(columnIndex) {
        case DOC_NAME_COLUMN:
          return String.class;
        case ANNOTATION_SET_NAME_COLUMN:
          return String.class;
        case LEFT_CONTEXT_COLUMN:
          return String.class;
        case PATTERN_COLUMN:
          return String.class;
        case RIGHT_CONTEXT_COLUMN:
          return String.class;
        default:
          return Object.class;
      }
    }

    /** None of the cells in the table are editable */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    /** Finally return an object for the corresponding cell in the table */
    public Object getValueAt(int rowIndex, int columnIndex) {
      Pattern aResult = (Pattern)patterns.get(rowIndex);
      switch(columnIndex) {
        case DOC_NAME_COLUMN:
          return aResult.getDocumentID();
        case ANNOTATION_SET_NAME_COLUMN:
          return aResult.getAnnotationSetName();
        case LEFT_CONTEXT_COLUMN:
          return aResult.getPatternText(aResult.getLeftContextStartOffset(),
                  aResult.getStartOffset());
        case PATTERN_COLUMN:
          return aResult.getPatternText(aResult.getStartOffset(), aResult
                  .getEndOffset());
        case RIGHT_CONTEXT_COLUMN:
          return aResult.getPatternText(aResult.getEndOffset(), aResult
                  .getRightContextEndOffset());
        default:
          return Object.class;
      }
    }

    static private final int DOC_NAME_COLUMN = 3;

    static private final int ANNOTATION_SET_NAME_COLUMN = 4;

    static private final int LEFT_CONTEXT_COLUMN = 0;

    static private final int PATTERN_COLUMN = 1;

    static private final int RIGHT_CONTEXT_COLUMN = 2;

  }

  /**
   * Panel that show a table of shortcut, annotation type and feature
   * to display in the pattern row.
   */
  protected class FeaturesManager extends JFrame {

    private static final long serialVersionUID = 1L;

    public FeaturesManager(String title) {
      super(title);

      setLayout(new BorderLayout());

      JScrollPane scrollPane = new JScrollPane();

      XJTable featuresXJTable;
      String[] columnNames =
        {"Display", "Shortcut", "Annotation type", "Feature"};
      javax.swing.table.DefaultTableModel tableModel =
        new javax.swing.table.DefaultTableModel(columnNames, 10);
      featuresXJTable = new XJTable(tableModel);
      scrollPane.add(featuresXJTable);

      add(scrollPane, BorderLayout.CENTER);

    }
//    featuresShortcuts
  }

  /**
   * Called by the GUI when this viewer/editor has to initialise itself
   * for a specific object.
   * 
   * @param target the object (be it a {@link gate.Resource},
   *          {@link gate.DataStore}or whatever) this viewer has to
   *          display
   */
  public void setTarget(Object target) {
    if(!(target instanceof LuceneDataStoreImpl)
            && !(target instanceof Searcher)) {
      throw new IllegalArgumentException(
              "The GATE LuceneDataStoreSearchGUI can only be used with a GATE LuceneDataStores!\n"
                      + target.getClass().toString()
                      + " is not a GATE LuceneDataStore or an object of Searcher!");
    }

    this.target = target;
    if(this.target instanceof LuceneDataStoreImpl) {
      ((LuceneDataStoreImpl)this.target).addDatastoreListener(this);
      corpusToSearchIn.setEnabled(true);
      annotationSetToSearchIn.setEnabled(true);
      this.searcher = ((LuceneDataStoreImpl)this.target).getSearcher();
      exportToHTML.setEnabled(true);
      allPatterns.setEnabled(true);
      selectedPatterns.setEnabled(true);
      URL indexLocationURL = (URL)((LuceneDataStoreImpl)target).getIndexer()
              .getParameters().get(Constants.INDEX_LOCATION_URL);
      String location = null;
      try {
        location = new File(indexLocationURL.toURI()).getAbsolutePath();
      }
      catch(URISyntaxException use) {
        location = new File(indexLocationURL.getFile()).getAbsolutePath();
      }

      try {
        annotationSetIDsFromDataStore = ((LuceneDataStoreImpl)this.target)
                .getSearcher().getIndexedAnnotationSetNames(location);
        allAnnotTypesAndFeaturesFromDatastore = ((LuceneDataStoreImpl)this.target)
                .getSearcher().getAnnotationTypesMap();
      }
      catch(SearchException se) {
        throw new GateRuntimeException(se);
      }

      // here we need to find out all corpus resources from the
      // datastore
      try {
        java.util.List corpusPIds = ((LuceneDataStoreImpl)this.target)
                .getLrIds(SerialCorpusImpl.class.getName());
        if(corpusIds != null) {
          for(int i = 0; i < corpusPIds.size(); i++) {
            // in order to obtain their names, we'll have to get them
            String name = ((LuceneDataStoreImpl)this.target)
                    .getLrName(corpusPIds.get(i));
            // so first lets add this ID to corpusIds
            this.corpusIds.add(corpusPIds.get(i));
            // and we need to add the name to the combobox
            ((DefaultComboBoxModel)corpusToSearchIn.getModel())
                    .addElement(name);
          }
        }

        // lets fire the update event on combobox
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            corpusToSearchIn.updateUI();
            if(corpusIds.size() > 0) corpusToSearchIn.setSelectedIndex(0);
          }
        });
      }
      catch(PersistenceException pe) {
        // couldn't find any available corpusIds
      }
    }
    else {
      this.searcher = (Searcher)this.target;
      corpusToSearchIn.setEnabled(false);

      // here we need to find out all annotation sets that are indexed
      try {
        DefaultComboBoxModel annotationSetToSearchInModel = new DefaultComboBoxModel();
        annotationSetToSearchInModel.addElement(Constants.ALL_SETS);
        annotationSetIDsFromDataStore = this.searcher
                .getIndexedAnnotationSetNames(null);
        allAnnotTypesAndFeaturesFromDatastore = this.searcher
                .getAnnotationTypesMap();

        // each ID has the corpusName;annotationsetname
        for(String aSetName : annotationSetIDsFromDataStore) {
          // and we need to add the name to the combobox
          annotationSetToSearchInModel.addElement(aSetName.substring(aSetName
                  .indexOf(";") + 1));
        }

        annotationSetToSearchIn.setModel(annotationSetToSearchInModel);

        // lets fire the update event on combobox
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            annotationSetToSearchIn.updateUI();
            if(annotationSetIDsFromDataStore.length > 0) {
              annotationSetToSearchIn.setSelectedIndex(0);
            }
          }
        });
      }
      catch(SearchException pe) {
        throw new GateRuntimeException(pe);
      }
    }

    annotationSetToSearchIn.setEnabled(true);
    executeQuery.setEnabled(true);
    nextResults.setEnabled(true);
    newQuery.setToolTipText("Enter your new query here...");
    newQuery.setEnabled(true);
    noOfPatternsTF.setEnabled(true);
    contextWindowTF.setEnabled(true);
    clearQueryTF.setEnabled(true);
    updateDisplay();

    // lets refresh annotation sets view

  }

  /**
   * Does nothing.
   * 
   * @param i
   */
  public void progressChanged(int i) {
  }

  /**
   * Called when the process is finished, fires a refresh for this VR.
   */
  public void processFinished() {

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        executeQuery.setEnabled(true);
        nextResults.setEnabled(true);
        newQuery.setToolTipText("Enter your new query here...");
        newQuery.setEnabled(true);
        if(target instanceof LuceneDataStoreImpl) {
          corpusToSearchIn.setEnabled(true);
          exportToHTML.setEnabled(true);
          allPatterns.setEnabled(true);
          selectedPatterns.setEnabled(true);
          annotationSetToSearchIn.setEnabled(true);
        }
        else {
          corpusToSearchIn.setEnabled(false);
        }
        noOfPatternsTF.setEnabled(true);
        contextWindowTF.setEnabled(true);
        clearQueryTF.setEnabled(true);
        updateDisplay();
      }
    });
  }

  // Listening to datastore events

  /**
   * This method is called by datastore when a new resource is adopted
   */
  public void resourceAdopted(DatastoreEvent de) {
    // don't want to do anything here
  }

  /**
   * This method is called by datastore when an existing resource is
   * deleted
   */
  public void resourceDeleted(DatastoreEvent de) {
    Resource resource = de.getResource();
    if(resource instanceof Corpus) {
      // lets check if it is already available in our list
      Object id = de.getResourceID();
      int index = corpusIds.indexOf(id);
      if(index < 0) {
        return;
      }

      // we add 1 to index
      // this is because the first element in combo box is "Entire
      // DataStore"
      index++;

      // now lets remove it from the comboBox as well
      ((DefaultComboBoxModel)corpusToSearchIn.getModel())
              .removeElementAt(index);

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          corpusToSearchIn.updateUI();
        }
      });
    }

    // we must update our local resources
    URL indexLocationURL = (URL)((LuceneDataStoreImpl)target).getIndexer()
            .getParameters().get(Constants.INDEX_LOCATION_URL);
    String location = null;
    try {
      location = new File(indexLocationURL.toURI()).getAbsolutePath();
    }
    catch(URISyntaxException use) {
      location = new File(indexLocationURL.getFile()).getAbsolutePath();
    }

    try {
      annotationSetIDsFromDataStore = ((LuceneDataStoreImpl)this.target)
              .getSearcher().getIndexedAnnotationSetNames(location);
      allAnnotTypesAndFeaturesFromDatastore = ((LuceneDataStoreImpl)this.target)
              .getSearcher().getAnnotationTypesMap();
      updateAnnotationSetsToSearchInBox();
    }
    catch(SearchException se) {
      throw new GateRuntimeException(se);
    }

  }

  /**
   * This method is called when a resource is written into the datastore
   */
  public void resourceWritten(DatastoreEvent de) {
    Resource resource = de.getResource();
    if(resource instanceof Corpus) {
      // lets check if it is already available in our list
      Object id = de.getResourceID();
      if(!corpusIds.contains(id)) {
        // we need to add its name to the combobox
        corpusIds.add(id);
        ((DefaultComboBoxModel)corpusToSearchIn.getModel()).addElement(resource
                .getName());
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            corpusToSearchIn.updateUI();
          }
        });
      }

      URL indexLocationURL = (URL)((LuceneDataStoreImpl)target).getIndexer()
              .getParameters().get(Constants.INDEX_LOCATION_URL);
      String location = null;
      try {
        location = new File(indexLocationURL.toURI()).getAbsolutePath();
      }
      catch(URISyntaxException use) {
        location = new File(indexLocationURL.getFile()).getAbsolutePath();
      }

      try {
        annotationSetIDsFromDataStore = ((LuceneDataStoreImpl)this.target)
                .getSearcher().getIndexedAnnotationSetNames(location);
        allAnnotTypesAndFeaturesFromDatastore = ((LuceneDataStoreImpl)this.target)
                .getSearcher().getAnnotationTypesMap();
        updateAnnotationSetsToSearchInBox();
      }
      catch(SearchException se) {
        throw new GateRuntimeException(se);
      }

    }
  }

}
