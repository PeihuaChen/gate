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
               implements ProgressListener, ActionListener, DatastoreListener {

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
  private Map<String, List<String>> annotTypes;

  /** Table that lists the patterns found by the query */
  private XJTable patternTable;

  /**
   * Model of the patternTable.
   */
  private PatternsTableModel patternsTableModel;

  /** Comboboxes to list the types and respected features */
  private JComboBox annotTypesBox = new JComboBox(),
          featuresBox = new JComboBox();

  /**
   * This is to remember the previously selected annotation type in the
   * drop-down combobox
   */
  private String previousChoice = "";

  /** Button to allow addition of annotTypes and features */
  private JButton addAnnotTypeButton;

  /**
   * Button that allows retrieving next number of results
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

  /**
   * When exportAll patterns option is used, this is made true in order
   * not to update the GUI
   */
  private boolean explicitCall;

  /** Text Field that holds the query */
  private JTextField newQuery;

  /**
   * Options box that can be shown for the advanced user.
   */
  private Box optionsBox;

  /**
   * Options pane that is contained in the option box.
   */
  private JPanel optionsPane;

  /**
   * Checkbox for showing the options UI.
   */
  private JCheckBox optionsEnabledCheck;

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
   * User will specify the noOfPatternsToSearch here
   */
  private JTextField noOfPatternsField;

  /**
   * No Of tokens to be shown in context window
   */
  private JTextField contextWindowField;

  /** Label */
  private JLabel queryToExecute;

  /**
   * Label for total number of found patterns
   */
  private JLabel totalFoundPatterns;

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
  
  /** Added Types and features */
  private Map<String, List<String>> addedAnnotTypes;

  /** Added Annotation Types */
  private ArrayList<String> addedAnnotTypesInGUI;

  /** Added Annotation features */
  private ArrayList<String> addedAnnotFeatureInGUI;

  /** Gridbagconstraints for the guiPanel */
  private GridBagConstraints guiCons;

  /** Color Generator */
  gate.swing.ColorGenerator colorGenerator = new gate.swing.ColorGenerator();

  /** Instance of ExecuteQueryAction */
  ExecuteQueryAction execQueryAction;

  /**
   * Instance of NextResultAction
   */
  NextResultAction nextResultAction;

  /** Instance of ClearQueryAction */
  ClearQueryAction clearQueryAction;

  /** Instance of ExportResultsAction */
  ExportResultsAction exportResultsAction;

  /** Pattern Text displayed in guiPanel */
  JTextField patText;

  JLabel progressLabel;

  LuceneDataStoreSearchAlternativeGUI thisInstance;

  /**
   * Searcher object obtained from the datastore
   */
  private Searcher searcher;


  /**
   * Updates the features box according to the selected annotation type.
   */
  public void actionPerformed(ActionEvent ae) {

    // action for annotTypesBox
    if(ae.getSource() == annotTypesBox) {
      String choice = (String)annotTypesBox.getSelectedItem();
      if(choice != null && !choice.equals(previousChoice)) {
        previousChoice = choice;
        // yes we need to update the featuresBox
        List<String> featuresToAdd = annotTypes.get(choice);

        // and finally update the featuresBox
        featuresBox.removeAllItems();
        for(int i = 0; i < featuresToAdd.size(); i++) {
          featuresBox.addItem(featuresToAdd.get(i));
        }
        featuresBox.updateUI();
      }
    }
  }

  /**
   * Called when a View is loaded in GATE.
   */
  public Resource init() {
    // initialize maps
    patterns = new ArrayList<Hit>();
    annotTypes = new HashMap<String, List<String>>();
    addedAnnotTypes = new HashMap<String, List<String>>();
    addedAnnotTypesInGUI = new ArrayList<String>();
    addedAnnotFeatureInGUI = new ArrayList<String>();
    thisInstance = this;
    corpusIds = new ArrayList<Object>();

    // initialize GUI
    initGui();

    // initially optionsBox is collapsed
    optionsBox.remove(optionsPane);

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

    topPanel = new JPanel(new BorderLayout());
    topPanel.setOpaque(false);

    // first line of top panel
    JPanel queryPanel = new JPanel(new BorderLayout());

    queryToExecute = new JLabel("Query: ");
    queryPanel.add(queryToExecute, BorderLayout.WEST);
    newQuery = new JTextField(30);
    newQuery.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    newQuery.setEnabled(true);
    newQuery.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // pressing enter in the query text field execute the query
        executeQuery.doClick();        
      }
    });
    queryPanel.add(newQuery, BorderLayout.CENTER);
    JPanel queryButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    execQueryAction = new ExecuteQueryAction();
    Icon annicSearchIcon = MainFrame.getIcon("annic-search");
    executeQuery = new JButton();
    executeQuery.setPreferredSize(new Dimension(annicSearchIcon.getIconWidth()+8, annicSearchIcon.getIconHeight()+4));
    executeQuery.setBorderPainted(false);
    executeQuery.setContentAreaFilled(false);
    executeQuery.setAction(execQueryAction);
    executeQuery.setEnabled(true);
    queryButtonsPanel.add(executeQuery);
    clearQueryAction = new ClearQueryAction();
    clearQueryTF = new JButton();
    clearQueryTF.setPreferredSize(new Dimension(annicSearchIcon.getIconWidth()+8, annicSearchIcon.getIconHeight()+4));
    clearQueryTF.setBorderPainted(false);
    clearQueryTF.setContentAreaFilled(false);
    clearQueryTF.setAction(clearQueryAction);
    clearQueryTF.setEnabled(true);
    queryButtonsPanel.add(clearQueryTF);
    queryPanel.add(queryButtonsPanel, BorderLayout.EAST);

    topPanel.add(queryPanel, BorderLayout.NORTH);

    // options Box
    optionsBox = Box.createVerticalBox();
    String aTitle = "Options";
    JLabel aLabel = new JLabel(aTitle);
    optionsBox.setMinimumSize(
      new Dimension(aLabel.getPreferredSize().width, 0));    
    optionsBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      optionsEnabledCheck = new JCheckBox(aTitle, MainFrame.getIcon("closed"), false);
      optionsEnabledCheck.setSelectedIcon(MainFrame.getIcon("expanded"));
      optionsEnabledCheck.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      optionsEnabledCheck.setAlignmentY(JComponent.TOP_ALIGNMENT);
      optionsEnabledCheck.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (optionsEnabledCheck.isSelected()) {
            if (!optionsBox.isAncestorOf(optionsPane)) {
              // add the options box if not already there
              optionsBox.add(optionsPane);
              thisInstance.validate();
//              topPanel.validate();
            }
          } else {
            if(optionsBox.isAncestorOf(optionsPane)){
              optionsBox.remove(optionsPane);
              thisInstance.validate();
//              topPanel.validate();
            }
          }
        }
      });

    optionsBox.add(optionsEnabledCheck);
    optionsBox.add(Box.createHorizontalGlue());

    // Options pane
    optionsPane = new JPanel();
    optionsPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    optionsPane.setAlignmentY(Component.TOP_ALIGNMENT);
    optionsPane.setLayout(new BoxLayout(optionsPane, BoxLayout.Y_AXIS));

    // first options line of top panel
    JPanel newQueryOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JLabel noOfPatternsLabel = new JLabel("Number of results: ");
    newQueryOptionsPanel.add(noOfPatternsLabel);
    noOfPatternsField = new JTextField("50", 3);
    noOfPatternsField.setToolTipText("Number of Patterns to retrieve");
    noOfPatternsField.setEnabled(true);
    newQueryOptionsPanel.add(noOfPatternsField);
    JLabel contextWindowLabel = new JLabel("Context size: ");
    newQueryOptionsPanel.add(contextWindowLabel);
    contextWindowField = new JTextField("5", 2);
    contextWindowField
            .setToolTipText("Number of Tokens to be displayed in context");
    contextWindowField.setEnabled(true);
    newQueryOptionsPanel.add(contextWindowField);

    optionsPane.add(newQueryOptionsPanel);

    // second options line of top panel
    JPanel toSearchInPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    toSearchInPanel.add(new JLabel("Corpus :"));
    DefaultComboBoxModel corpusToSearchInModel = new DefaultComboBoxModel();
    corpusToSearchInModel.addElement("Entire DataStore");
    corpusToSearchIn = new JComboBox(corpusToSearchInModel);
    corpusToSearchIn.setPrototypeDisplayValue("Entire DataStore   ");
    corpusToSearchIn.setToolTipText("Corpus Name");
    if(target == null || target instanceof Searcher) {
      corpusToSearchIn.setEnabled(false);
    }
    toSearchInPanel.add(corpusToSearchIn);
    toSearchInPanel.add(new JLabel("AnnotationSet :"));
    DefaultComboBoxModel annotationSetToSearchInModel = new DefaultComboBoxModel();
    annotationSetToSearchInModel.addElement(Constants.ALL_SETS);
    annotationSetToSearchIn = new JComboBox(annotationSetToSearchInModel);
    annotationSetToSearchIn
            .setPrototypeDisplayValue("Results from combined Sets");
    annotationSetToSearchIn.setToolTipText("AnnotationSet Name");
    toSearchInPanel.add(annotationSetToSearchIn);
    totalFoundPatterns = new JLabel("Total Found Patterns : 0        ");
    toSearchInPanel.add(totalFoundPatterns);
    nextResultAction = new NextResultAction();
    nextResults = new JButton();
    nextResults.setPreferredSize(new Dimension(annicSearchIcon.getIconWidth()+8, annicSearchIcon.getIconHeight()+4));
    nextResults.setBorderPainted(false);
    nextResults.setContentAreaFilled(false);
    nextResults.setAction(nextResultAction);
    nextResults.setEnabled(true);
    toSearchInPanel.add(nextResults);

    optionsPane.add(toSearchInPanel);

    //third options line of top panel
    comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    comboPanel.add(new JLabel("Annotation Types : "));
    annotTypesBox = new JComboBox();
    annotTypesBox.addActionListener(this);
    comboPanel.add(annotTypesBox);
    comboPanel.add(new JLabel("Features : "));
    featuresBox = new JComboBox();
    comboPanel.add(featuresBox);
    addAnnotTypeButton = new JButton();
    addAnnotTypeButton.setPreferredSize(new Dimension(annicSearchIcon.getIconWidth()+8, annicSearchIcon.getIconHeight()+4));
    addAnnotTypeButton.setBorderPainted(false);
    addAnnotTypeButton.setContentAreaFilled(false);
    addAnnotTypeButton.setAction(new AddAnnotTypeAction());
    comboPanel.add(addAnnotTypeButton);
    comboPanel.add(new JLabel("Export patterns:"));
    allPatterns = new JRadioButton("All");
    allPatterns.setToolTipText("exports all the patterns on this screen");
    allPatterns.setSelected(true);
    allPatterns.setEnabled(true);
    comboPanel.add(allPatterns);
    selectedPatterns = new JRadioButton("Selected");
    selectedPatterns.setToolTipText("exports only the selected patterns");
    selectedPatterns.setSelected(false);
    selectedPatterns.setEnabled(true);
    comboPanel.add(selectedPatterns);
    patternExportButtonsGroup = new ButtonGroup();
    patternExportButtonsGroup.add(allPatterns);
    patternExportButtonsGroup.add(selectedPatterns);
    exportResultsAction = new ExportResultsAction();
    exportToHTML = new JButton();
    exportToHTML.setPreferredSize(new Dimension(annicSearchIcon.getIconWidth()+8, annicSearchIcon.getIconHeight()+4));
    exportToHTML.setBorderPainted(false);
    exportToHTML.setContentAreaFilled(false);
    exportToHTML.setAction(exportResultsAction);
    exportToHTML.setEnabled(true);
    comboPanel.add(exportToHTML);

    optionsPane.add(comboPanel);

    optionsBox.add(optionsPane);

    topPanel.add(optionsBox, BorderLayout.CENTER);

    // right side of the top panel
    progressLabel = new JLabel(MainFrame.getIcon("working"));
    progressLabel.setEnabled(false);

    topPanel.add(progressLabel, BorderLayout.EAST);
//    topTopPanel.add(progressLabel, BorderLayout.EAST);
//    JPanel topTopPanel = new JPanel(new BorderLayout());

    JScrollPane scrollPane = new JScrollPane(topPanel);
    scrollPane.setOpaque(false);
//    topTopPanel.add(scrollPane, BorderLayout.CENTER);
//    topTopPanel.add(topPanel, BorderLayout.CENTER);

//    topTopPanel.validate();
//    topTopPanel.setMinimumSize(new Dimension(
//            topPanel.getPreferredSize().width
//           +progressLabel.getPreferredSize().width,
//            (topPanel.getPreferredSize().height
//           > progressLabel.getPreferredSize().height)?
//               topPanel.getPreferredSize().height
//              :progressLabel.getPreferredSize().height));

//    add(topTopPanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.NORTH);

    /****************
     * Center panel *
     ****************/

    guiCons = new GridBagConstraints();
//    guiCons.fill = GridBagConstraints.HORIZONTAL;
//    guiCons.anchor = GridBagConstraints.FIRST_LINE_START;

    guiPanel = new JPanel();
    guiPanel.setLayout(new GridBagLayout());
    guiPanel.setOpaque(true);
    guiPanel.setBackground(Color.WHITE);

    // will be added to the GUI via a split panel

    /****************
     * Bottom panel *
     ****************/

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
              totalFoundPatterns.setText("Total Found Patterns : "
                      + patternTable.getRowCount());
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

    // when user changes his/her selection in the rows
    // the graphical panel should change its ouput to reflect the new
    // selection incase where multiple rows are selected
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

    // will be added to the GUI via a split panel

    /***********************************************
     * Split between center panel and bottom panel *
     ***********************************************/

    JSplitPane sPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sPane.setDividerLocation(300);
    sPane.add(new JScrollPane(guiPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    sPane.add(new JScrollPane(patternTable,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

//    JPanel tempPanel = new JPanel(new BorderLayout());
//    tempPanel.add(sPane, BorderLayout.CENTER);

//    add(tempPanel, BorderLayout.CENTER);
    add(sPane, BorderLayout.CENTER);

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
    annotTypes = searcher.getAnnotationTypesMap();
  }

  /**
   * Initializes the comboboxes for annotation Types and their features.
   */
  public void updateGui() {
    guiPanel.removeAll();
    guiPanel.validate();
    guiPanel.updateUI();
    annotTypesBox.removeAllItems();
    ArrayList<String> annotTypesKeyList = new ArrayList<String>(annotTypes
            .keySet());
    for(int i = 0; i < annotTypesKeyList.size(); i++) {
      annotTypesBox.addItem(annotTypesKeyList.get(i));
    }
    annotTypesBox.updateUI();

    featuresBox.removeAllItems();
    if(annotTypesBox.getItemCount() > 0) {
      List<String> featuresList = annotTypes.get(annotTypesBox.getItemAt(0));
      for(int i = 0; i < featuresList.size(); i++) {
        featuresBox.addItem(featuresList.get(i));
      }
    }
    featuresBox.updateUI();
    newQuery.setText(searcher.getQuery());
  }

  /**
   * Updates the pattern row view when the user changes
   * his/her selection of pattern in the patternTable.
   */
  public void tableValueChanged() {

    guiCons.gridx = 0;
    guiCons.gridy = 0;
    guiCons.gridwidth = 1;
    guiCons.gridheight = 1;
    guiCons.fill = GridBagConstraints.BOTH;
    guiCons.insets = new java.awt.Insets(0, 0, 0, 0);
    guiCons.anchor = GridBagConstraints.CENTER;
    guiCons.weighty = 0.0;
    guiCons.weightx = 1.0;
    guiPanel.removeAll();

    // get the row selected in the pattern table
    int row = patternTable.getSelectedRow();

    if(row == -1) { // no pattern is selected in the pattern table
      guiPanel.add(new JLabel(
              "Please select a row in the pattern table."), guiCons);
      guiPanel.validate();
      return;
    }

    addAnnotTypeButton.setEnabled(true);
    annotTypesBox.setEnabled(true);
    featuresBox.setEnabled(true);

    // in case the user has sorted a column in the pattern table
    row = patternTable.rowViewToModel(row);

    Pattern pattern = (Pattern)patterns.get(row);

    // display on the first line the text matching the pattern and its context
    guiCons.gridx = 0;
    guiPanel.add(new JLabel("Text"), guiCons);
    for (int charNum = 0;
         charNum < pattern.getPatternText().length(); charNum++) {
      guiCons.gridx = charNum + 1;
      JLabel label = new JLabel(String.valueOf(
                (pattern.getPatternText().charAt(charNum))));
      if (charNum >= pattern.getStartOffset()
                   - pattern.getLeftContextStartOffset()
       && charNum < pattern.getEndOffset()
                  - pattern.getLeftContextStartOffset()) {
        label.setBackground(new Color(240, 201, 184));
      } else {
        label.setBackground(Color.WHITE);
      }
      label.setOpaque(true);
      label.addMouseListener(
              new AddPatternRowInQueryMouseInputListener(label.getText()));
      guiPanel.add(label, guiCons);
    }

    // for each annotation type / feature to display
    for(int i = 0; i < addedAnnotTypesInGUI.size(); i++) {
      String type = (String)addedAnnotTypesInGUI.get(i);
      String feature = (String)addedAnnotFeatureInGUI.get(i);

      guiCons.gridy++;
      guiCons.gridx = 0;
      guiPanel.add(new JLabel(type+"."+feature), guiCons);

        // get the annotations / features to display
        PatternAnnotation[] annots =
          pattern.getPatternAnnotations(type, feature);
        if(annots == null || annots.length == 0) {
          annots = new PatternAnnotation[0];
        }

        // add a JPanel in the gridbag layout for each feature
        // of the current annotation type
        for(int k = 0, j = 0; k < annots.length; k++, j += 2) {
          gate.creole.annic.PatternAnnotation ann =
            (gate.creole.annic.PatternAnnotation)annots[k];
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

        // add a delete button at the end of the row
        JButton removePattern;
        removePattern = new JButton();//MainFrame.getIcon("delete.gif"));
        removePattern.setAction(new DeletePatternRowAction(type, feature));
        removePattern.setBorderPainted(true);
        removePattern.setMargin(new Insets(0, 0, 0, 0));
        guiCons.anchor = GridBagConstraints.LINE_END;
        guiCons.gridwidth = 1;
        guiCons.gridx = GridBagConstraints.RELATIVE;
        guiPanel.add(removePattern, guiCons);
        guiCons.anchor = GridBagConstraints.CENTER;
    }

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
    annotTypes.clear();
    addedAnnotTypes.clear();
    addedAnnotTypesInGUI.clear();
    addedAnnotFeatureInGUI.clear();

    // unless we have AnnicSearchPR initialized, we donot have any data
    // to
    // show
    if(target != null) {
      initLocalData();
      updateGui();
      // change the totalFoundLabel
      patternsTableModel.fireTableDataChanged();
      if(patternTable.getRowCount() > 0) {
        patternTable.setRowSelectionInterval(0, 0);
        tableValueChanged();
      }
      totalFoundPatterns.setText("Patterns: listed "
              + patternTable.getRowCount());
      totalFoundPatterns.updateUI();
      
      String query = newQuery.getText();
      ArrayList<String> toAdd = new ArrayList<String>();

      if(query.length() > 0 && !patterns.isEmpty()) {
        String[] posStart = new String[]{"{",","," "};
        String[] posEnd = new String[]{"}",".",","," ","="};

        // lets go through annotations in the addedAnnotTypes
        outer:for(String annotType : annotTypes.keySet()) {
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
        // the user has not selected a feature
        featureType = "nothing";
      }

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

      // update the gui
      patternTable.setRowSelectionInterval(patternTable.getSelectedRow(),
              patternTable.getSelectedRow());
      tableValueChanged();
    }
  }

  /**
   * Exports all patterns to the XML File.
   */
  private class ExportResultsAction extends AbstractAction {

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
          progressLabel.setEnabled(true);
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

                index = annotationSetToSearchIn.getSelectedIndex();
                if(index > 0) {
                  String annotationSet = (String)annotationSetToSearchIn
                          .getSelectedItem();
                  parameters.put(Constants.ANNOTATION_SET_ID, annotationSet);
                }
                else {
                  parameters.remove(Constants.ANNOTATION_SET_ID);
                }
              }

              Integer noOfPatterns = new Integer(noOfPatternsField.getText()
                      .trim());
              Integer contextWindow = new Integer(contextWindowField.getText()
                      .trim());
              String query = newQuery.getText().trim();
              parameters.put(Constants.CONTEXT_WINDOW, contextWindow);

              try {
                if(searcher.search(query, parameters)) {
                  searcher.next(noOfPatterns.intValue());
                }
              }
              catch(Exception e) {
                e.printStackTrace();
                thisInstance.setEnabled(true);
                progressLabel.setEnabled(false);
              }
              processFinished();
              thisInstance.setEnabled(true);
              progressLabel.setEnabled(false);
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
            searcher.next(Integer.parseInt(noOfPatternsField.getText()));
          }
          catch(Exception e) {
            e.printStackTrace();
            thisInstance.setEnabled(true);
          }
          processFinished();
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
//    currentPatternRows.remove(this);
    tableValueChanged();
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
          }
        });

        // TODO obtain all available annotation sets may be through
        // searcher object
      }
      catch(PersistenceException pe) {
        // couldn't find any available corpusIds
      }

      // here we need to find out all annotation sets that are indexed
      try {
        DefaultComboBoxModel annotationSetToSearchInModel = new DefaultComboBoxModel();
        annotationSetToSearchInModel.addElement(Constants.ALL_SETS);
        URL indexLocationURL = (URL)((LuceneDataStoreImpl)target).getIndexer().getParameters().get(Constants.INDEX_LOCATION_URL);
        String location = new File(indexLocationURL.toURI()).getAbsolutePath();
        String[] annotSets = ((LuceneDataStoreImpl)this.target).getSearcher()
                .getIndexedAnnotationSetNames(location);

        for(String aSetName : annotSets) {
          // and we need to add the name to the combobox
          annotationSetToSearchInModel.addElement(aSetName);
        }

        annotationSetToSearchIn.setModel(annotationSetToSearchInModel);

        // lets fire the update event on combobox
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            annotationSetToSearchIn.updateUI();
          }
        });
      }
      catch(SearchException pe) {
        throw new GateRuntimeException(pe);
      } catch(URISyntaxException use) {
        throw new GateRuntimeException(use);
      }
    }
    else {
      this.searcher = (Searcher)this.target;
      corpusToSearchIn.setEnabled(false);

      // here we need to find out all annotation sets that are indexed
      try {
        DefaultComboBoxModel annotationSetToSearchInModel = new DefaultComboBoxModel();
        annotationSetToSearchInModel.addElement(Constants.ALL_SETS);
        String[] annotSets = this.searcher.getIndexedAnnotationSetNames(null);

        for(String aSetName : annotSets) {
          // and we need to add the name to the combobox
          annotationSetToSearchInModel.addElement(aSetName);
        }

        annotationSetToSearchIn.setModel(annotationSetToSearchInModel);

        // lets fire the update event on combobox
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            annotationSetToSearchIn.updateUI();
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
    noOfPatternsField.setEnabled(true);
    contextWindowField.setEnabled(true);
    clearQueryTF.setEnabled(true);
    updateDisplay();
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
    if(!explicitCall) {
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
          noOfPatternsField.setEnabled(true);
          contextWindowField.setEnabled(true);
          clearQueryTF.setEnabled(true);
          updateDisplay();
        }
      });
    }
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
    }
  }

}
