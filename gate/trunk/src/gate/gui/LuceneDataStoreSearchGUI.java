/*
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  LuceneDataStoreSearchGUI.java
 *
 *  Thomas Heitz, Dec 11, 2007
 *  based on Niraj GUI
 *
 *  $Id: $
 */


package gate.gui;

import gate.*;
import gate.corpora.SerialCorpusImpl;
import gate.creole.AbstractVisualResource;
import gate.event.DatastoreEvent;
import gate.event.DatastoreListener;
import gate.gui.MainFrame;
import gate.gui.docview.TextualDocumentView;
import gate.persist.LuceneDataStoreImpl;
import gate.persist.PersistenceException;
import gate.swing.XJTable;
import gate.util.GateRuntimeException;
import gate.util.OptionsMap;

import gate.creole.annic.Constants;
import gate.creole.annic.Hit;
import gate.creole.annic.PatternAnnotation;
import gate.creole.annic.Pattern;
import gate.creole.annic.SearchException;
import gate.creole.annic.Searcher;
import gate.creole.annic.lucene.QueryParser;

import java.util.*;
import java.util.List;
import java.util.regex.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.table.*;
import javax.swing.text.BadLocationException;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;


/**
 * GUI allowing to write a query with a JAPE derived syntax for querying
 * a Lucene Datastore and display the results with a stacked view of the
 * annotations and their values.
 *
 * This VR is associated to {@link gate.creole.ir.SearchPR}.
 * You have to set the target with setTarget().
 * 
 * Features: query auto-completion, syntactic error checker,
 * display of very big values, export of results in a file,
 * 16 different type of statistics.
 * 
 * TODO:
 *
 * - when someone invokes a search gui, in which case, entire datastore and
 *   "all sets" options are selected, it should also check if there are any
 *   annotations available to search in - if none available, and show an
 *   information message, the same when the corpus / annotation set change
 * - could be interesting to have statistics per document
 *   it is just adding one condition to the query that retrieves data out of
 *   the index
 * - add shortcut to autocompletion ?
 * - where to store the configuration for shortcut: user config or datastore ?
 * - plus other todos in this file
 */
public class LuceneDataStoreSearchGUI extends AbstractVisualResource
               implements DatastoreListener {

  private static final long serialVersionUID = 3256720688877285686L;

  /**
   * The GUI is associated with the AnnicSearchPR
   */
  private Object target;

  /**
   * URL or file path of the index location of the searcher.
   */
//  private String indexLocation;
  
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

  /**
   * Table that lists the patterns found by the query
   */
  private XJTable patternTable;

  /**
   * Model of the patternTable.
   */
  private PatternsTableModel patternsTableModel;

  /**
   * Display the annotation rows manager.
   */
  private JButton annotationRowsManagerButton;

  /**
   * Show / Hide the statistics panel.
   */
  private JToggleButton displayStatistics;

  /**
   * Contains statistics for the corpus and the annotation set selected.
   * This statistics are always displayed.
   */
  private XJTable globalStatisticsTable;

  /**
   * Contains statistics of one row each.
   */
  private XJTable oneRowStatisticsTable;

  /**
   *  Comparator for Integer in statistics tables.
   */
  private Comparator<Integer> integerComparator;

  /**
   *  Comparator for Integer in statistics tables.
   */
  private Comparator<String> lastWordComparator;

  /**
   * Collator for String with insensitive case.
   */
  private java.text.Collator stringCollator;

  /**
   * Horizontal split between the results pane and statistics pane.
   */
  private JSplitPane bottomSplitPane;
  
  /**
   * Display statistics on the datastore.
   */
  private JTabbedPane statisticsTabbedPane;
  
  /**
   * Export all pattern to html
   */
  private JRadioButton allPatterns;

  /**
   * Export only selectedPatterns to html
   */
  private JRadioButton selectedPatterns;

  /**
   * Text Area that contains the query
   */
  private QueryTextArea queryTextArea;

  /**
   * Which corpus to use when searching in
   */
  private JComboBox corpusToSearchIn;

  /**
   * Which annotation set to search in.
   */
  private JComboBox annotationSetsToSearchIn;

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
  private JSpinner numberOfResultsSpinner;

  /**
   * Allow to show all results.
   */
  private JCheckBox showAllResultsCheckBox;

  /**
   * No Of tokens to be shown in context window
   */
  private JSpinner contextSizeSpinner;

  /**
   * Gives the page number displayed in the results.
   */
  private JLabel titleResults;

  /**
   * Number of the page of results.
   */
  private int pageOfResults;

  /**
   * Number of row to show in the results.
   */
  int noOfPatterns;

  /**
   * JPanel that contains the central panel of annotation rows.
   */
  private JPanel centerPanel;

  /** Instance of ExecuteQueryAction */
  private ExecuteQueryAction executeQueryAction;

  /**
   * Instance of NextResultAction
   */
  private NextResultsAction nextResultsAction;

  /** Instance of ClearQueryAction */
//  private ClearQueryAction clearQueryAction;

  /** Instance of ExportResultsAction */
  private ExportResultsAction exportResultsAction;

  /**
   * Instance of this class.
   */
  LuceneDataStoreSearchGUI thisInstance;

  /**
   * Current instance of the annotation rows manager.
   */
  private AnnotationRowsManagerFrame annotationRowsManager;

  /** Names of the columns for annotationRows data. */
  String[] columnNames =
    {"Display", "Shortcut", "Annotation type", "Feature", "Crop"};

  /** Column (second dimension) of annotationRows double array. */
  static private final int DISPLAY = 0;
  /** Column (second dimension) of annotationRows double array. */
  static private final int SHORTCUT = 1;
  /** Column (second dimension) of annotationRows double array. */
  static private final int ANNOTATION_TYPE = 2;
  /** Column (second dimension) of annotationRows double array. */
  static private final int FEATURE = 3;
  /** Column (second dimension) of annotationRows double array. */
  static private final int CROP = 4;

  /** Maximum number of annotationRow */
  static private final int maxAnnotationRows = 100;

  /** Number of annotationRows. */
  private int numAnnotationRows = 0;

  /**
   * Double array that contains [row, column] of the annotationRows
   * data.
   */
  private String[][] annotationRows =
    new String[maxAnnotationRows+1][columnNames.length];

  private AnnotationRowsManagerTableModel annotationRowsManagerTableModel;

  /**
   * Contains the tooltips of the first column.
   */
  private Vector<String> oneRowStatisticsTableToolTips;

  /**
   * Searcher object obtained from the datastore
   */
  private Searcher searcher;

  /**
   * true if there was an error on the last query.
   */
  private boolean errorOnLastQuery;

  private OptionsMap userConfig;

  /**
   * Animation when waiting for the results.
   */
//  protected Animation animation;


  /**
   * Called when a View is loaded in GATE.
   */
  public Resource init() {

    patterns = new ArrayList<Hit>();
    allAnnotTypesAndFeaturesFromDatastore = new HashMap<String, List<String>>();
    thisInstance = this;
    corpusIds = new ArrayList<Object>();
    populatedAnnotationTypesAndFeatures = new HashMap<String, Set<String>>();
    noOfPatterns = 0;
    for (int row = 0; row <= maxAnnotationRows; row++) {
      annotationRows[row][DISPLAY] = "true";
      annotationRows[row][SHORTCUT] = "";
      annotationRows[row][ANNOTATION_TYPE] = "";
      annotationRows[row][FEATURE] = "";
      annotationRows[row][CROP] = "Crop end";
    }
    userConfig = Gate.getUserConfig();

    // read the user config data
    if (userConfig.get("Annotation_rows") != null) {
      // saved as a string: "[[true, Cat, Token, category, Crop end], [...]]"
      String annotationRowsString =
        (String)userConfig.get("Annotation_rows");
      annotationRowsString = annotationRowsString.replaceAll("^\\[\\[", "");
      annotationRowsString = annotationRowsString.replaceAll("\\]\\]$", "");
      String[] rows = annotationRowsString.split("\\], \\[");
      numAnnotationRows = 0;
      if (annotationRowsString.length() > 0) {
        for (int row = 0; row < rows.length
             && row < maxAnnotationRows; row++) {
          String[] cols = rows[row].split(", ");
          if (cols.length == columnNames.length) {
            // skip the data if incorrect format
            for (int col = 0; col < cols.length; col++) {
              annotationRows[row][col] = cols[col];
            }
            numAnnotationRows++;
          }
        }
      }
    }

    // initialize GUI
    initGui();

    // called when Gate is exited, in case the user doesn't close the datastore
    MainFrame.getInstance().addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent e) {
        // no parent so need to be disposed explicitly
        annotationRowsManager.dispose();
      }
    });

//    // unless the AnnicSerachPR is initialized, we don't have any data
//    // to show
//    if(target != null) {
//      if(target instanceof Searcher) {
//        searcher = (Searcher)target;
//      } else if(target instanceof LuceneDataStoreImpl) {
//        searcher = ((LuceneDataStoreImpl)target).getSearcher();
//      } else {
//        throw new GateRuntimeException("Invalid target specified for the GUI");
//      }
    updateDisplay();
//    }
    validate();

//    LogArea log = new LogArea();
//    JFrame logFrame = new JFrame();
//    logFrame.add(new JScrollPane(log));
//    logFrame.setSize(400, 400);
//    logFrame.setVisible(true);

    return this;
  }

  /**
   * Called when the user close the datastore.
   */
  public void cleanup() {
    // no parent so need to be disposed explicitly
    annotationRowsManager.dispose();
  }

  /**
   * Initialize the GUI.
   */
  protected void initGui() {

    // see the global layout schema at the end
    setLayout(new BorderLayout());

    stringCollator = java.text.Collator.getInstance();
    stringCollator.setStrength(java.text.Collator.TERTIARY);

    lastWordComparator = new Comparator<String>() {
      public int compare(String o1, String o2) {
        if (o1 == null || o2 == null) { return 0; }
        return stringCollator.compare(
          ((String)o1).substring(((String)o1).trim().lastIndexOf(' ')+1),
          ((String)o2).substring(((String)o2).trim().lastIndexOf(' ')+1));
      }
    };

    integerComparator = new Comparator<Integer>() {
      public int compare(Integer o1, Integer o2) {
        if (o1 == null || o2 == null) { return 0; }
        return ((Integer)o1).compareTo((Integer)o2);
      }
    };

    /***************************
     * Annotation rows manager *
     ***************************/

    annotationRowsManager =
      new AnnotationRowsManagerFrame("Annotation Rows Manager");
    annotationRowsManager.setIconImage(
            ((ImageIcon)MainFrame.getIcon("add.gif")).getImage());
    annotationRowsManager.setLocationRelativeTo(thisInstance);
    annotationRowsManager.validate();
    annotationRowsManager.setSize(200, 300);
    annotationRowsManager.pack();

    /*************
     * Top panel *
     *************/

    JPanel topPanel = new JPanel(new GridBagLayout());
    topPanel.setOpaque(false);
    topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    GridBagConstraints gbc = new GridBagConstraints();

    // first column, three rows span
    queryTextArea = new QueryTextArea();
    queryTextArea.setToolTipText(
      "<html>Please enter a query to search in the datastore."
      +"<br>Type '{' to activate auto-completion."
      +"<br>Use [Control]+[Enter] to add a new line.</html>");
    queryTextArea.setEnabled(true);
    queryTextArea.setLineWrap(true);
    gbc.gridheight = 3;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0, 0, 0, 4);
    topPanel.add(new JScrollPane(queryTextArea), gbc);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridheight = 1;
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.insets = new Insets(0, 0, 0, 0);

    // second column, first row
    gbc.gridx = GridBagConstraints.RELATIVE;
    topPanel.add(new JLabel("Corpus: "), gbc);
    corpusToSearchIn = new JComboBox();
    corpusToSearchIn.addItem(Constants.ENTIRE_DATASTORE);
    corpusToSearchIn.setPrototypeDisplayValue(Constants.ENTIRE_DATASTORE);
    corpusToSearchIn.setToolTipText("Select the corpus to search in.");
    if(target == null || target instanceof Searcher) {
      corpusToSearchIn.setEnabled(false);
    }
    corpusToSearchIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ie) {
        updateAnnotationSetsToSearchInBox();
      }
    });
    gbc.gridwidth = 2;
    topPanel.add(corpusToSearchIn, gbc);
    gbc.gridwidth = 1;
    topPanel.add(Box.createHorizontalStrut(4), gbc);
    topPanel.add(new JLabel("Annotation set: "), gbc);
    annotationSetsToSearchIn = new JComboBox();
    annotationSetsToSearchIn.setPrototypeDisplayValue(Constants.COMBINED_SET);
    annotationSetsToSearchIn.setToolTipText("Select the annotation set to search in.");
    annotationSetsToSearchIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ie) {
        updateAnnotationTypeBox();
      }
    });
    topPanel.add(annotationSetsToSearchIn, gbc);

    // second column, second row
    gbc.gridy = 1;
    JLabel noOfPatternsLabel = new JLabel("Retrieve: ");
    topPanel.add(noOfPatternsLabel, gbc);
    numberOfResultsSpinner =
      new JSpinner(new SpinnerNumberModel(50, 1, 10000, 5));
//    numberOfResultsSpinner.setPreferredSize(new Dimension(50,
//      numberOfResultsSpinner.getPreferredSize().height));
    numberOfResultsSpinner.setToolTipText("Number of results per page.");
    numberOfResultsSpinner.setEnabled(true);
    topPanel.add(numberOfResultsSpinner, gbc);
    showAllResultsCheckBox = new JCheckBox("All results");
    showAllResultsCheckBox.setToolTipText("Retrieve all results.");
    showAllResultsCheckBox.setSelected(false);
    showAllResultsCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ie) {
        if (showAllResultsCheckBox.isSelected()) {
          nextResultsAction.setEnabled(false);
          numberOfResultsSpinner.setEnabled(false);
        } else {
          if (searcher.getHits().length == noOfPatterns) {
            nextResultsAction.setEnabled(true);
          }
          numberOfResultsSpinner.setEnabled(true);
        }
       }
    });
    topPanel.add(showAllResultsCheckBox, gbc);
    topPanel.add(Box.createHorizontalStrut(4), gbc);
    JLabel contextWindowLabel = new JLabel("Context size: ");
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;
    topPanel.add(contextWindowLabel, gbc);
    contextSizeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
    contextSizeSpinner.setToolTipText(
      "Number of Tokens to be displayed in the context.");
    contextSizeSpinner.setEnabled(true);
    gbc.anchor = GridBagConstraints.WEST;
    topPanel.add(contextSizeSpinner, gbc);
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // second column, third row
    gbc.gridy = 2;
    executeQueryAction = new ExecuteQueryAction();
    executeQueryAction.setEnabled(true);
    JButton executeQuery = new JButton();
    executeQuery.setBorderPainted(false);
    executeQuery.setContentAreaFilled(false);
    executeQuery.setAction(executeQueryAction);
    executeQuery.setMargin(new Insets(0, 0, 0, 0));
    topPanel.add(executeQuery, gbc);
    ClearQueryAction clearQueryAction = new ClearQueryAction();
    clearQueryAction.setEnabled(true);
    JButton clearQueryTF = new JButton();
    clearQueryTF.setBorderPainted(false);
    clearQueryTF.setContentAreaFilled(false);
    clearQueryTF.setAction(clearQueryAction);
    clearQueryTF.setMargin(new Insets(0, 0, 0, 0));
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    topPanel.add(clearQueryTF, gbc);

    // will be added to the GUI via a split panel

    /****************
     * Center panel *
     ****************/

    // just initialized the components, they will be added in tableValueChanged()
    centerPanel = new JPanel();
    centerPanel.setLayout(new GridBagLayout());
    centerPanel.setOpaque(true);
    centerPanel.setBackground(Color.WHITE);

    annotationRowsManagerButton = new JButton();
    annotationRowsManagerButton.setAction(new DisplayAnnotationRowsManagerAction());

//    animation = new Animation();
//    Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
//            animation, "Lucene Data Store GUI");
//    thread.setDaemon(true);
//    thread.setPriority(Thread.MIN_PRIORITY);
//    thread.start();

    // will be added to the GUI via a split panel

    /*********************
     * Bottom left panel *
     *********************/

    JPanel bottomLeftPanel = new JPanel(new GridBagLayout());
    bottomLeftPanel.setOpaque(false);
    gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 0, 0, 3);
    gbc.gridwidth = 1;

    // title of the table, results options, export and next results button
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    nextResultsAction = new NextResultsAction();
    nextResultsAction.setEnabled(false);
    JButton nextResults = new JButton();
    nextResults.setAction(nextResultsAction);
    bottomLeftPanel.add(nextResults, gbc);
    titleResults = new JLabel("Results");
    gbc.insets = new Insets(0, 10, 0, 10);
    bottomLeftPanel.add(titleResults, gbc);
    gbc.insets = new Insets(0, 0, 0, 3);
    bottomLeftPanel.add(Box.createHorizontalStrut(4), gbc);
    exportResultsAction = new ExportResultsAction();
    exportResultsAction.setEnabled(false);
    JButton exportToHTML = new JButton();
    exportToHTML.setAction(exportResultsAction);
    bottomLeftPanel.add(exportToHTML, gbc);
    allPatterns = new JRadioButton("All");
    allPatterns.setToolTipText("Exports all the rows in the table.");
    allPatterns.setSelected(true);
    allPatterns.setEnabled(true);
    bottomLeftPanel.add(allPatterns, gbc);
    selectedPatterns = new JRadioButton("Selected");
    selectedPatterns.setToolTipText("Exports selected row(s) in the table.");
    selectedPatterns.setSelected(false);
    selectedPatterns.setEnabled(true);
    bottomLeftPanel.add(selectedPatterns, gbc);
    ButtonGroup patternExportButtonsGroup = new ButtonGroup();
    patternExportButtonsGroup.add(allPatterns);
    patternExportButtonsGroup.add(selectedPatterns);
    displayStatistics = new JToggleButton("Statistics");
    displayStatistics.setSelected(false);
    displayStatistics.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ie) {
        if (bottomSplitPane.getComponentCount() == 2) {
          bottomSplitPane.add(statisticsTabbedPane);
          bottomSplitPane.setDividerLocation(0.66);
          displayStatistics.setSelected(true);
        } else if (bottomSplitPane.getComponentCount() == 3) {
          bottomSplitPane.remove(2);
          bottomSplitPane.setDividerLocation(1.0);
          displayStatistics.setSelected(false);
        }
      }
    });
    displayStatistics.setEnabled(true);
    displayStatistics.setToolTipText(
      "Display statistics on the datastore and the result of the query.");
    bottomLeftPanel.add(displayStatistics, gbc);

    // table of results
    patternsTableModel = new PatternsTableModel();
    patternTable = new XJTable(patternsTableModel);
    patternTable.addMouseMotionListener(new MouseMotionListener() {
      public void mouseMoved(MouseEvent me) {
        int row = patternTable.rowAtPoint(me.getPoint());
        row = patternTable.rowViewToModel(row);
        Pattern pattern = null;
        if(row > -1) {
          pattern = (Pattern)patterns.get(row);
          patternTable.setToolTipText("The query that matched this result was: "
          +pattern.getQueryString()+".");
        }
      }
      public void mouseDragged(MouseEvent me) {
      }
    });

    patternTable.addMouseListener(new MouseAdapter() {
      private JPopupMenu mousePopup;
      JMenuItem menuItem;

      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          createPopup();
          mousePopup.show(e.getComponent(), e.getX(), e.getY());
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          createPopup();
          mousePopup.show(e.getComponent(), e.getX(), e.getY());
        }
      }

      private void createPopup() {
          mousePopup = new JPopupMenu();
          menuItem = new JMenuItem("Remove the selected result");
          if(patternTable.getSelectedRowCount() > 1) {
            menuItem.setText(menuItem.getText()+"s");
          }
          mousePopup.add(menuItem);
          menuItem.addActionListener(new ActionListener() {
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
              mousePopup.setVisible(false);
            }
          });

          if(patternTable.getSelectedRowCount() == 1
          && target instanceof LuceneDataStoreImpl) {
          menuItem = new JMenuItem("Open the selected document");
          menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              int row = patternTable.rowViewToModel(
                patternTable.getSelectedRow());
              final Pattern result = (Pattern)patterns.get(row);
              FeatureMap features = Factory.newFeatureMap();
              features.put(DataStore.DATASTORE_FEATURE_NAME,
                (LuceneDataStoreImpl)target);
              features.put(DataStore.LR_ID_FEATURE_NAME,
                result.getDocumentID());
              final Document doc;
              try {
              doc = (Document)Factory
                .createResource("gate.corpora.DocumentImpl", features);
              gate.Main.getMainFrame().select(doc);
              } catch (gate.util.GateException e) {
                e.printStackTrace();
                return;
              }

              // dirty little trick for waiting until the document is
              // displayed and then select the expression
              int numberOfMillisecondsInTheFuture = 1000; // 1 sec
              Date timeToRun = new Date(System.currentTimeMillis()
                +numberOfMillisecondsInTheFuture);
              java.util.Timer timer = new java.util.Timer();
              timer.schedule(new TimerTask() {
                public void run() {
                try {
//                // find the DocumentEditor then the AnnotationSetsView
//                // associated with the document
//                gate.gui.docview.AnnotationSetsView asv;
//                for (Resource r : Gate.getCreoleRegister().getAllInstances(
//                    "gate.gui.docview.DocumentEditor")) {
//                  gate.gui.docview.DocumentEditor de =
//                    (gate.gui.docview.DocumentEditor)r;
//                  
//                  if (de.getCentralViews().get(0).getDocument().getName().equals(doc.getName())) {
//                    asv = (gate.gui.docview.AnnotationSetsView)
//                      de.getHorizontalViews();
//                    break;
//                  }
//                }
                for (Resource r : Gate.getCreoleRegister().getAllInstances(
                    "gate.gui.docview.TextualDocumentView")) {
                  TextualDocumentView t = (TextualDocumentView)r;
                  // find the document opened in DocumentEditor
                  if (t.getDocument().getName().equals(doc.getName())) {
                    // scroll then select the expression that matches
                    // the query result
                    try {
                    t.getTextView().scrollRectToVisible(
                      t.getTextView().modelToView(
                      result.getRightContextEndOffset()));
                    } catch (BadLocationException e) {
                      e.printStackTrace();
                      return;
                    }
                    t.getTextView().select(
                      result.getLeftContextStartOffset(),
                      result.getRightContextEndOffset());
                    t.getTextView().requestFocus();
                    // TODO: display the same annotation types as in Annic
//                    for (int row = 0; row < numAnnotationRows; row++) {
//                      if (annotationRows[row][DISPLAY].equals("false")) {
//                        continue;
//                      }
//                      // for each annotation set in the document
//                      for (Object asn : doc.getAnnotationSetNames()) {
//                        String type = annotationRows[row][ANNOTATION_TYPE];
//                        AnnotationSet as = doc.getAnnotations((String)asn);
//                        // look if there is the type displayed in Annic
//                        AnnotationSet ast = as.get(type);
//                        if (!ast.isEmpty()) {
////                          t.addHighlights(ast, as,
////                            getAnnotationTypeColor(type));
//                          asv.setTypeSelected((String)asn, type, true);
//                        }
//                      }
//                    }
                    break;
                  }
                }
                } catch (gate.util.GateException e) {
                  e.printStackTrace();
                  return;
                }
                }
              }, timeToRun);
            }
          });
          mousePopup.add(menuItem);
          }

      }
    });
    // when user changes his/her selection in the rows,
    // the graphical panel should change its ouput to reflect the new
    // selection. incase where multiple rows are selected
    // the annotations of the first row will be highlighted
    patternTable.getSelectionModel().addListSelectionListener(
      new javax.swing.event.ListSelectionListener() {
        public void valueChanged(javax.swing.event.ListSelectionEvent e) {
          switch (patternTable.getSelectedRows().length) {
            case 0:
              selectedPatterns.setEnabled(false);
              allPatterns.setSelected(true);
              break;
            case 1:
              if (!e.getValueIsAdjusting()) { updateCentralView(); }
              selectedPatterns.setEnabled(true);
              break;
            default:
              selectedPatterns.setEnabled(true);
              selectedPatterns.setSelected(true);
              break;
          }
        }
      });
    // user should be allowed to select multiple rows
    patternTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    patternTable.setColumnSelectionAllowed(false);
    patternTable.setRowSelectionAllowed(true);
    patternTable.setSortable(true);
    patternTable.setComparator(
      PatternsTableModel.LEFT_CONTEXT_COLUMN, lastWordComparator);
    patternTable.setComparator(
      PatternsTableModel.PATTERN_COLUMN, stringCollator);
    patternTable.setComparator(
      PatternsTableModel.RIGHT_CONTEXT_COLUMN, stringCollator);
    // right-alignment of the column
    patternTable.getColumnModel()
      .getColumn(PatternsTableModel.LEFT_CONTEXT_COLUMN)
      .setCellRenderer(new DefaultTableCellRenderer() {
        private static final long serialVersionUID = 1L;
        public Component getTableCellRendererComponent(
                JTable table, Object color, boolean isSelected,
                boolean hasFocus, int row, int col) {
          Component component = super.getTableCellRendererComponent(
            table, color, isSelected, hasFocus, row, col);
          if (component instanceof JLabel) {
            ((JLabel)component).setHorizontalAlignment(SwingConstants.RIGHT);
          }
          return component;
        }
      });
    patternTable.getColumnModel()
      .getColumn(PatternsTableModel.PATTERN_COLUMN)
      .setCellRenderer(new DefaultTableCellRenderer() {
        private static final long serialVersionUID = 1L;
        public Component getTableCellRendererComponent(
                JTable table, Object color, boolean isSelected,
                boolean hasFocus, int row, int col) {
          Component component = super.getTableCellRendererComponent(
            table, color, isSelected, hasFocus, row, col);
          if (component instanceof JLabel) {
            ((JLabel)component).setHorizontalAlignment(SwingConstants.CENTER);
          }
          return component;
        }
      });

    JScrollPane tableScrollPane = new JScrollPane(patternTable,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.gridy = 1;
    gbc.gridx= 0;
    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 2;
    gbc.weighty = 2;
    bottomLeftPanel.add(tableScrollPane, gbc);

    /**************************
     * Statistics tabbed pane *
     **************************/

    statisticsTabbedPane = new JTabbedPane();

    globalStatisticsTable = new XJTable() {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int rowIndex, int vColIndex) {
        return false;
      }
    };
    statisticsTabbedPane.addTab("Global", null,
      new JScrollPane(globalStatisticsTable),
      "Global statistics on the Corpus and Annotation Set selected.");

    oneRowStatisticsTable = new XJTable() {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int rowIndex, int vColIndex) {
        return false;
      }
      public Component prepareRenderer(TableCellRenderer renderer,
              int row, int col) {
        Component c = super.prepareRenderer(renderer, row, col);
        if (c instanceof JComponent) {
          // display a custom tooltip saved when adding statistics
          ((JComponent)c).setToolTipText("<html>"
            +oneRowStatisticsTableToolTips.get(rowViewToModel(row))+"</html>");
        }
        return c;
      }
    };
    statisticsTabbedPane.addTab("One item", null,
      new JScrollPane(oneRowStatisticsTable),
      "One item statistics.");
    oneRowStatisticsTableToolTips = new Vector<String>();

    // will be added to the GUI via a split panel

    /**************************************************************
     * Vertical splits between top, center panel and bottom panel *
     **************************************************************/

    /** ________________________________________
     * |               topPanel                 |
     * |__________________3_____________________|
     * |                                        |
     * |             centerPanel                |
     * |________2________ __________2___________|
     * |                 |                      |
     * | bottomLeftPanel 1 statisticsTabbedPane |
     * |_________________|______________________|
     * 
     * 1 bottomSplitPane
     * 2 centerBottomSplitPane
     * 3 topBottomSplitPane
     */

    bottomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    bottomSplitPane.add(bottomLeftPanel);
    bottomSplitPane.setDividerLocation(1.0);

    JSplitPane centerBottomSplitPane =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    centerBottomSplitPane.add(new JScrollPane(centerPanel,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    centerBottomSplitPane.add(bottomSplitPane);
    centerBottomSplitPane.setResizeWeight(0.5);

    JSplitPane topBottomSplitPane =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    topBottomSplitPane.add(topPanel);
    topBottomSplitPane.add(centerBottomSplitPane);
    topBottomSplitPane.setDividerLocation(80);

    add(topBottomSplitPane, BorderLayout.CENTER);

  }

  /**
   * Initialize the local data (i.e. Pattern data etc.) and then update
   * the GUI
   */
  protected void updateDisplay() {
    // initialize results of the query
    patterns.clear();

    // if target itself is null, we don't want to update anything
    if (target == null) { return; }

    Collections.addAll(patterns, searcher.getHits());

    // update the table of results
    patternsTableModel.fireTableDataChanged();

    if(patterns.size() > 0) {
      String query = queryTextArea.getText().trim();
      if(query.length() > 0 && !patterns.isEmpty()) {
        int row;
        do { // delete previous temporary annotation rows
          row = findAnnotationRow(DISPLAY, "one time");
          deleteAnnotationRow(row);
        } while (row >= 0);
        // from the query display all the existing annotationRows
        // that are not already displayed
        Matcher matcher = java.util.regex.Pattern.compile(
          "\\{" // first condition
          +"([^\\{\\}=,.]+)" // annotation type or shortcut (1)
          +"(?:(?:\\.([^=]+)==\"([^\\}\"]+)\")" // feature (2), value (3)
          +"|(?:==([^\\}]+)))?" // value of a shortcut (4)
          +"(?:, ?" // second condition
          +"([^\\{\\}=,.]+)" // annotation type or shortcut (5)
          +"(?:(?:\\.([^=]+)==\"([^\\}\"]+)\")" // feature (6), value (7)
          +"|(?:==([^\\}]+)))?)?" // value of a shortcut (8)
          +"\\}").matcher(query);
        while (matcher.find()) {
          for (int i = 0; i <= 4; i += 4) { // first then second condition
          String type = null, feature = null, shortcut = null; row = -1;
          if (matcher.group(1+i) != null
           && matcher.group(2+i) == null
           && matcher.group(3+i) == null
           && matcher.group(4+i) == null) {
            type = matcher.group(1+i);
            feature = "";
            row = findAnnotationRow(ANNOTATION_TYPE, type, FEATURE, feature);
          } else if (matcher.group(1+i) != null
                  && matcher.group(2+i) == null
                  && matcher.group(3+i) == null) {
            shortcut = matcher.group(1+i);
            row = findAnnotationRow(SHORTCUT, shortcut);
          } else if (matcher.group(1+i) != null
                  && matcher.group(2+i) != null
                  && matcher.group(4+i) == null) {
            type = matcher.group(1+i);
            feature = matcher.group(2+i);
            row = findAnnotationRow(ANNOTATION_TYPE, type, FEATURE, feature);
          }
          if (row >= 0) {
            annotationRows[row][DISPLAY] = "true";
          } else if (type != null && feature != null
                  && numAnnotationRows < maxAnnotationRows) {
            annotationRows[numAnnotationRows][DISPLAY] = "one time";
            annotationRows[numAnnotationRows][SHORTCUT] = "";
            annotationRows[numAnnotationRows][ANNOTATION_TYPE] = type;
            annotationRows[numAnnotationRows][FEATURE] = feature;
            annotationRows[numAnnotationRows][CROP] = "Crop end";
            numAnnotationRows++;
          }
          }
        }
        annotationRowsManagerTableModel.fireTableDataChanged();
      }
//      updateCentralView();
      exportResultsAction.setEnabled(true);
      if (!showAllResultsCheckBox.isSelected()) {
        nextResultsAction.setEnabled(true);
      }
      if (searcher.getHits().length < noOfPatterns) {
        nextResultsAction.setEnabled(false);
      }
      patternTable.setRowSelectionInterval(0, 0);
      patternTable.scrollRectToVisible(patternTable.getCellRect(0, 0, true));

    } else if (queryTextArea.getText().trim().length() < 1) {
      centerPanel.removeAll();
      centerPanel.add(new JLabel("Please, enter a query in the query text field."),
              new GridBagConstraints());
      centerPanel.updateUI();
      nextResultsAction.setEnabled(false);
      selectedPatterns.setEnabled(false);
      exportResultsAction.setEnabled(false);
      allPatterns.setSelected(true);
      queryTextArea.requestFocusInWindow();

    } else {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.gridy = GridBagConstraints.RELATIVE;
      if (errorOnLastQuery) {
        errorOnLastQuery = false;
      } else {
        centerPanel.removeAll();
        centerPanel.add(new JTextArea("No result found for your query."), gbc);
        if (!corpusToSearchIn.getSelectedItem().equals(Constants.ENTIRE_DATASTORE)
         || !annotationSetsToSearchIn.getSelectedItem().equals(Constants.ALL_SETS)) {
          gbc.insets = new Insets(20, 0, 0, 0);
          centerPanel.add(new JTextArea(
            "Consider increasing the number of documents to search "
            +"in selecting \""+Constants.ENTIRE_DATASTORE+"\" as corpus\n"
            +" and \""+Constants.ALL_SETS+"\" as annotation set "
            +"in the drop-down lists."), gbc);
        }
      }
      gbc.insets = new Insets(20, 0, 0, 0);
      centerPanel.add(new JTextArea(
        "Here are the different type of queries you can use:\n"+
        "1. String, ex. the\n"+
        "2. {AnnotationType}, ex. {Person}\n"+
        "3. {AnnotationType==\"String\"}, ex. {Person==\"Smith\"}\n"+
        "4. {AnnotationType.feature==\"featureValue\"}, ex. {Person.gender==\"male\"}\n"+
        "5. {AnnotationType1, AnnotationType2.feature==\"featureValue\"}\n"+
        "6. {AnnotationType1.feature==\"featureValue\", AnnotationType2.feature==\"featureValue\"}\n\n"+

        "ANNIC also supports the ∣ (OR) operator.\n"+
        "For instance, {A} ({B}∣{C}) is a query of two annotations where the ﬁrst is an annotation of type A followed by the annotation of type either B or C.\n"+
        "ANNIC supports two operators, + and *, to specify the number of times a particular annotation or a sub query should appear in the main query.\n"+
        "Here, ({A})+n means one and up to n occurrences of annotation {A} and ({A})*n means zero or up to n occurrences of annotation {A}.\n"),
        gbc);
      centerPanel.updateUI();
      exportResultsAction.setEnabled(false);
      nextResultsAction.setEnabled(false);
    }
  }

  /**
   * Updates the central view of annotation rows.
   */
//  int numberOfCall = 0;
  protected void updateCentralView() {
//    numberOfCall++;
//    System.out.println("numberOfCall = "+numberOfCall);
    // maximum number of columns to display, i.e. maximum number of characters
    int maxColumns = 150;
    // maximum length of a feature value displayed
    int maxValueLength = 30;

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;

    if (patternTable.getSelectedRow() == -1) {
      // no pattern is selected in the pattern table
      if (patternTable.getRowCount() > 0) {
        centerPanel.removeAll();
        centerPanel.add(new JLabel(
          "Please select a row in the table of results."), gbc);
        centerPanel.validate();
        centerPanel.repaint();
      }
      return;
    }

    // clear the central view
    centerPanel.removeAll();

    Pattern pattern = (Pattern)patterns
      .get(patternTable.rowViewToModel(patternTable.getSelectedRow()));

    // display on the first line the text matching the pattern and its context
    gbc.gridwidth = 1;
    gbc.insets = new java.awt.Insets(10, 10, 10, 10);
    JLabel labelTitle = new JLabel("Pattern Text");
    labelTitle.setOpaque(true);
    labelTitle.setBackground(new Color(240, 240, 240));
    labelTitle.setToolTipText("Text matched by last query and its context.");
    centerPanel.add(labelTitle, gbc);
    gbc.insets = new java.awt.Insets(10, 0, 10, 0);

    int startOffsetPattern =
      pattern.getStartOffset() - pattern.getLeftContextStartOffset();
    int endOffsetPattern =
      pattern.getEndOffset() - pattern.getLeftContextStartOffset();
    boolean textTooLong = pattern.getPatternText().length() > maxColumns;
    int upperBound = pattern.getPatternText().length() - (maxColumns/2);
//    System.out.println("upperBound = "+upperBound);

//    String message = "";
    // for each character in the line of text
    for (int charNum = 0; charNum < pattern.getPatternText().length();
         charNum++) {

      gbc.gridx = charNum + 1;
      if (textTooLong) {
        if (charNum == maxColumns/2) {
          // add ellipsis dots in case of a too long text displayed
          centerPanel.add(new JLabel("..."), gbc);
          // skip the middle part of the text if too long
          charNum = upperBound + 1;
          continue;
        } else if (charNum > upperBound) {
          gbc.gridx -= upperBound - (maxColumns/2) + 1;
        }
      }
//      message += pattern.getPatternText().substring(charNum, charNum+1)+"["+charNum+","+gbc.gridx+"] ";

      // set the text and color of the feature value
      JLabel label = 
        new JLabel(pattern.getPatternText().substring(charNum, charNum+1));
      if (charNum >= startOffsetPattern && charNum < endOffsetPattern) {
        // this part is matched by the pattern, color it
        label.setBackground(new Color(240, 201, 184));
        label.setToolTipText("The query that matched this expression was: "
          +pattern.getQueryString()+".");
      } else {
        // this part is the context, no color
        label.setBackground(Color.WHITE);
      }
      label.setOpaque(true);

      // get the word from which belongs the current character charNum
      int start = pattern.getPatternText().lastIndexOf(" ", charNum);
      int end = pattern.getPatternText().indexOf(" ", charNum);
      String word = pattern.getPatternText().substring(
        (start==-1)?0:start,
        (end==-1)?pattern.getPatternText().length():end);
      // add a mouse listener that modify the query
      label.addMouseListener(new TextRowMouseListener(word));
      centerPanel.add(label, gbc);
    }
//    System.out.println(message+"\n");

    // for each annotation type to display
    for (int row = 0; row < numAnnotationRows; row++) {
      if (annotationRows[row][DISPLAY].equals("false")) { continue; }
      String type = annotationRows[row][ANNOTATION_TYPE];
      String feature = annotationRows[row][FEATURE];

      gbc.gridy++;
      gbc.gridx = 0;
      gbc.gridwidth = 1;
      gbc.insets = new Insets(0, 0, 3, 0);

      // add the annotation type / feature header
      JLabel annotationTypeAndFeature = new JLabel();
      int row2 = findAnnotationRow(ANNOTATION_TYPE, type, FEATURE, feature);
      String shortcut = (row2 >= 0)? annotationRows[row2][SHORTCUT] : "";
      annotationTypeAndFeature.setText((!shortcut.equals(""))?shortcut
              :type+((feature.equals(""))?"":"."+feature));
      annotationTypeAndFeature.setToolTipText(
        "<html>Values of annotation type: "+type+" and "
        +((feature.equals(""))?"all its features":"feature: "+feature)+".</html>");
      annotationTypeAndFeature.setOpaque(true);
      annotationTypeAndFeature.setBackground(new Color(240, 240, 240));
      if (feature.equals("")) {
        annotationTypeAndFeature.addMouseListener(
          new AnnotationRowHeaderMouseListener(type));
      } else {
        annotationTypeAndFeature.addMouseListener(
          new AnnotationRowHeaderMouseListener(type, feature));
      }
      gbc.insets = new java.awt.Insets(0, 10, 3, 10);
      centerPanel.add(annotationTypeAndFeature, gbc);
      gbc.insets = new java.awt.Insets(0, 0, 3, 0);

      // get the annotation type / feature to display
      PatternAnnotation[] annots = (!feature.equals(""))?
          pattern.getPatternAnnotations(type, feature):
          pattern.getPatternAnnotations(type);
      if(annots == null || annots.length == 0) {
        annots = new PatternAnnotation[0];
      }

//      message = "";
      // add a JLabel in the gridbag layout for each feature value
      // of the current annotation type
      HashMap<Integer,Integer> gridSet = new HashMap<Integer,Integer>();
      for (int k = 0; k < annots.length; k++) {
        PatternAnnotation ann = (PatternAnnotation)annots[k];
//        System.out.println(type+"."+feature+"["+k+"] = "+ann.getFeatures().toString()+", ["+ann.getStartOffset()+", "+ann.getEndOffset()+"]");
        gbc.gridx = ann.getStartOffset()
          - pattern.getLeftContextStartOffset() + 1;
        gbc.gridwidth = ann.getEndOffset()
          - pattern.getLeftContextStartOffset() - gbc.gridx + 1;
//        message += "[("+gbc.gridx+","+gbc.gridwidth+")";
        if (textTooLong) {
          if (gbc.gridx > (upperBound+1)) {
            // x starts after the hidden middle part
            gbc.gridx -= upperBound - (maxColumns/2) + 1;
          } else if (gbc.gridx > (maxColumns/2)) {
            // x starts in the hidden middle part
            if (gbc.gridx + gbc.gridwidth <= (upperBound+3)) {
              // x ends in the hidden middle part
              continue; // skip the middle part of the text
            } else {
              // x ends after the hidden middle part
              gbc.gridwidth -= upperBound - gbc.gridx + 2;
              gbc.gridx = (maxColumns/2) + 2;
            }
          } else {
            // x starts before the hidden middle part
            if (gbc.gridx + gbc.gridwidth < (maxColumns/2)) {
              // x ends before the hidden middle part
              // do nothing
            } else if (gbc.gridx + gbc.gridwidth < upperBound) {
              // x ends in the hidden middle part
              gbc.gridwidth = (maxColumns/2) - gbc.gridx + 1;
            } else {
              // x ends after the hidden middle part
              gbc.gridwidth -= upperBound - (maxColumns/2);
            }
          }
        }
        if (gbc.gridwidth == 0) { gbc.gridwidth = 1; }

        JLabel label = new JLabel();
        String value = (feature.equals(""))?
          " ":(String)ann.getFeatures().get(feature);
//        message += value;
        if (value.length() > maxValueLength) {
          label.setToolTipText((value.length()>500)?
            "<html><textarea rows=\"30\" cols=\"40\" readonly=\"readonly\">"
            +value.replaceAll("(.{60})", "$1\n")+"</textarea></html>":
            ((value.length()>100)?
            "<html><table width=\"500\" border=\"0\" cellspacing=\"0\">"
            +"<tr><td>"+value.replaceAll("\n", "<br>")
            +"</td></tr></table></html>":
            value));
          if (annotationRows[row][CROP].equals("Crop start")) {
            value = "..."+value.substring(value.length()-maxValueLength-1);
          } else if (annotationRows[row][CROP].equals("Crop end")) {
            value = value.substring(0, maxValueLength-2)+"...";
          } else { // cut in the middle
            value = value.substring(0, maxValueLength/2)+"..."
                   +value.substring(value.length()-(maxValueLength/2));
          }
        }
        label.setText(value);
        label.setBackground(getAnnotationTypeColor(ann.getType()));
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        label.setOpaque(true);
        if (feature.equals("")) {
          label.addMouseListener(new AnnotationRowValueMouseListener(type));
          String width = (ann.getFeatures().toString().length()>100)?"500":"100%";
          String toolTip = "<html><table width=\""+width
            +"\" border=\"0\" cellspacing=\"1\">";
          for (Map.Entry<String,String> map : ann.getFeatures().entrySet()) {
            toolTip += "<tr align=\"left\"><td bgcolor=\"silver\">"
                      +map.getKey()+"</td><td>"
                      +((map.getValue().length()>500)?
                       "<textarea rows=\"20\" cols=\"40\" readonly=\"readonly\">"
                      +map.getValue().replaceAll("(.{60})", "$1\n")+"</textarea>":
                       map.getValue().replaceAll("\n", "<br>"))
                      +"</td></tr>";
          }
          label.setToolTipText(toolTip+"</table></html>");
          // make the tooltip indefinitely shown when the mouse is over
          label.addMouseListener(new MouseAdapter() {
            int dismissDelay;
            public void mouseEntered(MouseEvent e) {
              dismissDelay =
                ToolTipManager.sharedInstance().getDismissDelay();
              ToolTipManager.sharedInstance()
                .setDismissDelay(Integer.MAX_VALUE);
            }
            public void mouseExited(MouseEvent e) {
              ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);
            }
          });
        } else {
          label.addMouseListener(new AnnotationRowValueMouseListener(
            type, feature, (String)ann.getFeatures().get(feature)));
        }
        if (gridSet.containsKey(gbc.gridx)) {
          // two values for the same row and column
          int oldGridy = gbc.gridy;
          gbc.gridy = gridSet.get(gbc.gridx)+1;
          centerPanel.add(label, gbc);
          for (int w = 0; w < gbc.gridwidth; w++) {
            gridSet.put(gbc.gridx+w, gbc.gridy);
          }
          gbc.gridy = oldGridy;
        } else {
          centerPanel.add(label, gbc);
          // save the row x and column y locations of the current value
          for (int w = 0; w < gbc.gridwidth; w++) {
            gridSet.put(gbc.gridx+w, gbc.gridy);
          }
        }
//        message += ",("+gbc.gridx+","+gbc.gridwidth+")] ";
      }
//      System.out.println(message+"\n");

      // add a remove button at the end of the row
      JButton removePattern = new JButton(MainFrame.getIcon("delete.gif"));
      removePattern.setToolTipText("Hide this annotation row.");
      final String typeFinal = type;
      final String featureFinal = feature;
      removePattern.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          int row = findAnnotationRow(
                  ANNOTATION_TYPE, typeFinal, FEATURE, featureFinal);
          if (row >= 0) {
            annotationRows[row][DISPLAY] = "false";
            saveConfiguration();
          }
          updateCentralView();
        }
      });
      removePattern.setBorderPainted(true);
      removePattern.setMargin(new Insets(0, 0, 0, 0));
      gbc.gridwidth = 1;
      // last cell of the row
      gbc.gridx = Math.min(pattern.getPatternText().length()+1, maxColumns+1);
      gbc.insets = new Insets(0, 10, 3, 0);
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.WEST;
      centerPanel.add(removePattern, gbc);
      gbc.insets = new Insets(0, 0, 3, 0);
      gbc.fill = GridBagConstraints.BOTH;
      gbc.anchor = GridBagConstraints.CENTER;

      // set the new gridy to the maximum row we put a value
      if (gridSet.size() > 0) {
        gbc.gridy = Collections.max(gridSet.values()).intValue();
      }
    }

    // add a annotation rows manager button on the last row
    gbc.insets = new java.awt.Insets(0, 10, 0, 10);
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    centerPanel.add(annotationRowsManagerButton, gbc);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;

    // add an empty cell that takes all remaining space to
    // align the visible cells at the top-left corner
    gbc.gridy++;
    gbc.gridx = maxColumns+1;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weightx = 10;
    gbc.weighty = 10;
    centerPanel.add(new JLabel(""), gbc);

    validate();
    updateUI();
  }

  protected void updateAnnotationSetsToSearchInBox() {
    String corpusName = 
      (corpusToSearchIn.getSelectedItem().equals(Constants.ENTIRE_DATASTORE))?
          null:(String)corpusIds.get(corpusToSearchIn.getSelectedIndex() - 1);
    TreeSet<String> ts = new TreeSet<String>(stringCollator);
    ts.addAll(getAnnotationSetNames(corpusName));
    DefaultComboBoxModel dcbm = new DefaultComboBoxModel(ts.toArray());
    dcbm.insertElementAt(Constants.ALL_SETS, 0);
    annotationSetsToSearchIn.setModel(dcbm);
    annotationSetsToSearchIn.setSelectedItem(Constants.ALL_SETS);

    // used in the AnnotationRowsManager as Annotation type column cell editor
    TreeSet<String> types = new TreeSet<String>(stringCollator);
    types.addAll(getAnnotTypesFeatures(null, null).keySet());
    // put all annotation types from the datastore
    // combobox used as cell editor
    JComboBox annotTypesBox = new JComboBox();
    annotTypesBox.setMaximumRowCount(10);
    annotTypesBox.setModel(new DefaultComboBoxModel(types.toArray()));
    DefaultCellEditor cellEditor = new DefaultCellEditor(annotTypesBox);
    cellEditor.setClickCountToStart(0);
    annotationRowsManager.getTable().getColumnModel()
      .getColumn(ANNOTATION_TYPE)
      .setCellEditor(cellEditor);
  }

  protected void updateAnnotationTypeBox() {
    String corpusName = 
      (corpusToSearchIn.getSelectedItem().equals(Constants.ENTIRE_DATASTORE))?
          null:(String)corpusIds.get(corpusToSearchIn.getSelectedIndex() - 1);
    String annotationSetName =
      (annotationSetsToSearchIn.getSelectedItem().equals(Constants.ALL_SETS))?
              null:(String)annotationSetsToSearchIn.getSelectedItem();
    populatedAnnotationTypesAndFeatures =
      getAnnotTypesFeatures(corpusName, annotationSetName);

    int countTotal = 0;
    try {
      int count = 0;
      DefaultTableModel model = new DefaultTableModel();
      model.addColumn("Annotation Type");
      model.addColumn("Count");
      TreeSet<String> ts = new TreeSet<String>(stringCollator);
      ts.addAll(populatedAnnotationTypesAndFeatures.keySet());
      for (String annotationType : ts) {
        // retrieves the number of occurrences for each Annotation Type
        // of the choosen Annotation Set
        count = searcher.freq(corpusName, annotationSetName, annotationType);
        model.addRow(new Object[]{annotationType, new Integer(count)});
        countTotal += count;
      }
      globalStatisticsTable.setModel(model);
      globalStatisticsTable.setComparator(0, stringCollator);
      globalStatisticsTable.setComparator(1, integerComparator);
    } catch(SearchException se) {
      se.printStackTrace();
      return;
    }
    if (countTotal == 0) {
      centerPanel.removeAll();
      centerPanel.add(new JLabel("<html>There is no annotation for the moment "
        +"for the selected corpus and annotation set.<br><br>"
        +"Please select another corpus or annotation set or wait for the "
        +"end of the automatic indexation."),
        new GridBagConstraints());
    }
  }

  /**
   * Sort use for the pattern table.
   * 
   * @param rows table of rows to sort
   * @return rows sorted 
   */
  protected int[] sortRows(int[] rows) {
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
  protected Color getAnnotationTypeColor(String annotationType) {
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
      gate.swing.ColorGenerator colorGenerator = new gate.swing.ColorGenerator();
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

  protected Set<String> getAnnotationSetNames(String corpusName) {
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

  protected Map<String, Set<String>> getAnnotTypesFeatures(String corpusName,
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
   * Find the first annotation row satisfying all the parameters.
   * @param parameters couples of int*String that stands for column*value
   * @return -2 if there is an error in parameters, -1 if not found,
   * row satisfying the parameters otherwise
   * @see DISPLAY, SHORTCUT, ANNOTATION_TYPE, FEATURE, CROP for column parameter
   */
  protected int findAnnotationRow(Object... parameters) {
    //test the number of parameters
    if ((parameters.length % 2) != 0) { return -2; }
    // test the type and value of the parameters
    for (int num = 0; num < parameters.length; num += 2) {
      if (parameters[num] == null || parameters[num+1] == null) { return -2; }
      try {
        if (Integer.parseInt(parameters[num].toString()) < 0
         || Integer.parseInt(parameters[num].toString()) > (columnNames.length-1)) {
          return -2;
        }
      } catch (NumberFormatException nfe) { return -2; }
      if (!(parameters[num+1] instanceof String)) { return -2; }
    }

    // look for the first row satisfying all the parameters 
    for (int row = 0; row < numAnnotationRows; row++) {
      int numParametersSatisfied = 0;
      for(int num = 0; num < parameters.length; num += 2) {
        if (annotationRows[row][Integer.parseInt(parameters[num].toString())]
           .equals((String)parameters[num+1])) {
          numParametersSatisfied++;
        }
      }
      if (numParametersSatisfied == (parameters.length/2)) { return row; }
    }
    return -1;
  }

  /**
   * Delete a row in the annotationRows array by shifting the following
   * rows to avoid empty row.
   * @param row row to delete in the annotationRows array
   * @return true if deleted, false otherwise
   */
  protected boolean deleteAnnotationRow(int row) {
    if (row < 0 || row > numAnnotationRows) { return false; }
    // shift the rows in the array
    for(int row2 = row; row2 < numAnnotationRows; row2++) {
      for(int col2 = 0; col2 < columnNames.length; col2++) {
        annotationRows[row2][col2] = annotationRows[row2+1][col2];
      }
    }
    annotationRows[numAnnotationRows][DISPLAY] = "true";
    annotationRows[numAnnotationRows][SHORTCUT] = "";
    annotationRows[numAnnotationRows][ANNOTATION_TYPE] = "";
    annotationRows[numAnnotationRows][FEATURE] = "";
    annotationRows[numAnnotationRows][CROP] = "Crop end";
    numAnnotationRows--;
    return true;
  }

  /**
   * Save the user config data.
   */
  protected void saveConfiguration() {
    String annotationRowsString = "[";
    for (int row = 0; row < numAnnotationRows; row++) {
      annotationRowsString += (row==0)?"[":", [";
      for (int col = 0; col < columnNames.length; col++) {
        annotationRowsString +=
          (col==0)?annotationRows[row][col]:", "+annotationRows[row][col];
      }
      annotationRowsString += "]";
    }
    annotationRowsString += "]";
    userConfig.put("Annotation_rows", annotationRowsString);
  }
  
  /**
   * Exports results and statistics to a HTML File.
   */
  protected class ExportResultsAction extends AbstractAction {

    private static final long serialVersionUID = 3257286928859412277L;

    public ExportResultsAction() {
      super("Export");
//      super("", MainFrame.getIcon("annic-export"));
      super.putValue(SHORT_DESCRIPTION,
        "Export results and statistics to a HTML file.");
//      super.putValue(MNEMONIC_KEY, KeyEvent.VK_E);
    }

    public void actionPerformed(ActionEvent ae) {

      Map<String, Object> parameters = searcher.getParameters();

      // no results
      if(patterns == null || patterns.isEmpty()) {
        try {
          JOptionPane.showMessageDialog(thisInstance,
                  "No patterns found to export");
          return;
        }
        catch(Exception e) {
          e.printStackTrace();
          return;
        }
      }

      try {
        // ask a file location where to store results
        JFileChooser fileDialog = new JFileChooser();

        String fileDialogTitle = "HTML";
        fileDialog.setDialogTitle(fileDialogTitle
                + " File to export results and statistics to...");
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.setSelectedFile(new java.io.File("annic.html"));
        fileDialog.showSaveDialog(thisInstance);
        java.io.File file = fileDialog.getSelectedFile();

        // user canceled
        if(file == null) return;

        java.io.FileWriter fileWriter = new java.io.FileWriter(file);

        ArrayList<Hit> patternsToExport = new ArrayList<Hit>();

        if(selectedPatterns.isSelected()) {
          // export selected patterns
          int[] rows = patternTable.getSelectedRows();
          for(int i = 0; i < rows.length; i++) {
            int num = patternTable.rowViewToModel(rows[i]);
            patternsToExport.add(patterns.get(num));
          }

        }
        else {
          // export all patterns
          for(int i = 0; i < patternTable.getRowCount(); i++) {
            int num = patternTable.rowViewToModel(i);
            patternsToExport.add(patterns.get(num));
          }
        }

        // Format:
        // Issued Corpus Query
        // Pattern
        // Table
        // 1. Document it belongs to, 2. Left context, 3. Actual
        // Pattern Text, 4. Right context
        java.io.BufferedWriter bw = new java.io.BufferedWriter(fileWriter);
        // write header
        bw.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"");
        bw.newLine();
        bw.write("\"http://www.w3.org/TR/html4/loose.dtd\">");
        bw.newLine();
        bw.write("<HTML><HEAD><TITLE>Annic Results and Statistics</TITLE>");
        bw.newLine();
        bw.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
        bw.newLine();
        bw.write("</HEAD><BODY>");
        bw.newLine();
        bw.write("<H1 align=\"center\">Annic Results and Statistics</H1>");
        bw.newLine();
        bw.write("<H2>Parameters</H2>");
        bw.newLine();
        bw.write("<UL><LI>Corpus: <B>"+(String)corpusToSearchIn.getSelectedItem()+"</B></LI>");
        bw.newLine();
        bw.write("<LI>Annotation set: <B>"+(String)annotationSetsToSearchIn.getSelectedItem()+"</B></LI>");
        bw.newLine();
        bw.write("<LI>Query Issued: <B>"+searcher.getQuery()+"</B>");
        bw.write("<LI>Context Window: <B>"+((Integer)parameters.get(Constants.CONTEXT_WINDOW))
                .intValue()+"</B></LI>");
        bw.newLine();
        bw.write("<LI>Queries:<UL>");
        String queryString = "";
        for(int i = 0; i < patternsToExport.size(); i++) {
          Pattern ap = (Pattern)patternsToExport.get(i);
          if(!ap.getQueryString().equals(queryString)) {
            bw.write("<LI><a href=\"#" + ap.getQueryString() + "\">"
                    + ap.getQueryString() + "</a></LI>");
            queryString = ap.getQueryString();
            bw.newLine();
          }
        }
        bw.write("</UL></LI></UL>");

        bw.write("<H2>Results</H2>");
        bw.newLine();

        queryString = "";
        for(int i = 0; i < patternsToExport.size(); i++) {
          Pattern ap = (Pattern)patternsToExport.get(i);
          if(!ap.getQueryString().equals(queryString)) {
//            if(!queryString.equals("")) {
//              bw.write("</TABLE>");
//              bw.newLine();
//            }
            queryString = ap.getQueryString();

            bw.newLine();
            bw.write("<P><a name=\"" + ap.getQueryString()
                    + "\">Query Pattern:</a> <B>" + ap.getQueryString()
                    + "</B></P>");
            bw.newLine();
            bw.write("<TABLE border=\"1\"><TR>");
            bw.write("<TH>No.</TH>");
            bw.write("<TH>Document ID</TH>");
            bw.write("<TH>Left Context</TH>");
            bw.write("<TH>Pattern</TH>");
            bw.write("<TH>Right Context</TH>");
            bw.write("</TR>");
            bw.newLine();
          }

          bw.write("<TR><TD>" + (i + 1) + "</TD>");
          bw.write("<TD>" + ap.getDocumentID() + "</TD>");
          bw.write("<TD align=\"right\">"
                  + ap.getPatternText(ap.getLeftContextStartOffset(), ap
                          .getStartOffset()) + "</TD>");
          bw.write("<TD align=\"center\">"
                  + ap.getPatternText(ap.getStartOffset(), ap.getEndOffset())
                  + "</TD>");
          bw.write("<TD align=\"left\">"
                  + ap.getPatternText(ap.getEndOffset(), ap
                          .getRightContextEndOffset()) + "</TD>");
          bw.write("</TR>");
          bw.newLine();
        }
        bw.write("</TABLE>");
        bw.newLine();

        bw.write("<H2>Global Statistics</H2>");
        bw.newLine();

        bw.write("<TABLE border=\"1\">");
        bw.newLine();
        bw.write("<TR>");
        for (int col = 0; col < globalStatisticsTable.getColumnCount(); col++) {
          bw.write("<TH>"+globalStatisticsTable.getColumnName(col)+"</TH>");
          bw.newLine();
        }
        bw.write("</TR>");
        bw.newLine();
        for (int row = 0; row < globalStatisticsTable.getRowCount(); row++) {
          bw.write("<TR>");
          for (int col = 0; col < globalStatisticsTable.getColumnCount(); col++) {
            bw.write("<TD>"+globalStatisticsTable.getValueAt(row, col)+"</TD>");
            bw.newLine();
          }
          bw.write("</TR>");
          bw.newLine();
        }
        bw.write("</TABLE>");
        bw.newLine();

        bw.write("<P><BR></P><HR>");
        bw.newLine();

        bw.write("</BODY>");
        bw.newLine();
        bw.write("</HTML>");

        bw.flush();
        bw.close();
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Clear the queryTextArea text box.
   */
  protected class ClearQueryAction extends AbstractAction {

    private static final long serialVersionUID = 3257569516199228209L;

    public ClearQueryAction() {
      super("", MainFrame.getIcon("annic-clean"));
      super.putValue(SHORT_DESCRIPTION, "Clear the query text box.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_BACK_SPACE);
    }

    public void actionPerformed(ActionEvent ae) {
      queryTextArea.setText("");
      queryTextArea.requestFocus();
    }
  }

  /** 
   * Finds out the newly created query and execute it.
   */
  protected class ExecuteQueryAction extends AbstractAction {

    private static final long serialVersionUID = 3258128055204917812L;

    public ExecuteQueryAction() {
      super("", MainFrame.getIcon("annic-search"));
      super.putValue(SHORT_DESCRIPTION, "Execute the query.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_ENTER);
    }

    public void actionPerformed(ActionEvent ae) {
      // TODO: animation
//      if (!animation.isActive()) animation.activate();

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          thisInstance.setEnabled(false);
          Map<String, Object> parameters = searcher.getParameters();
          if(parameters == null)
            parameters = new HashMap<String, Object>();

          if(target instanceof LuceneDataStoreImpl) {
//            ArrayList<String> indexLocations = new ArrayList<String>();
//            indexLocations.add(indexLocation);
//            parameters.put(Constants.INDEX_LOCATIONS, indexLocations);
//
            String corpus2SearchIn = 
              (corpusToSearchIn.getSelectedItem().equals(Constants.ENTIRE_DATASTORE))?
                      null:(String)corpusIds.get(corpusToSearchIn.getSelectedIndex() - 1);
            parameters.put(Constants.CORPUS_ID, corpus2SearchIn);
          }

          noOfPatterns = (showAllResultsCheckBox.isSelected())?
            -1:((Number)numberOfResultsSpinner.getValue()).intValue();
          int contextWindow =
            ((Number)contextSizeSpinner.getValue()).intValue();
          String query = queryTextArea.getText().trim();
          java.util.regex.Pattern pattern =
            java.util.regex.Pattern.compile("[\\{, ]([^\\{=]+)==");
          Matcher matcher = pattern.matcher(query);
          int start = 0;
          while (matcher.find(start)) {
            start = matcher.end(1); // avoid infinite loop
            int row = findAnnotationRow(SHORTCUT, matcher.group(1));
            if (row >= 0) {
              // rewrite the query to put the long form of the
              // shortcut found
              query = query.substring(0, matcher.start(1))
                +annotationRows[row][ANNOTATION_TYPE]+"."
                +annotationRows[row][FEATURE]
                +query.substring(matcher.end(1));
              matcher = pattern.matcher(query);
            }
          }

          parameters.put(Constants.CONTEXT_WINDOW, new Integer(contextWindow));
          if (annotationSetsToSearchIn.getSelectedItem()
                  .equals(Constants.ALL_SETS)) {
            parameters.remove(Constants.ANNOTATION_SET_ID);
          } else {
            String annotationSet =
              (String)annotationSetsToSearchIn.getSelectedItem();
            parameters.put(Constants.ANNOTATION_SET_ID, annotationSet);
          }

          try {
            if(searcher.search(query, parameters)) {
              searcher.next(noOfPatterns);
            }

            processFinished();
            queryTextArea.requestFocus();
            thisInstance.setEnabled(true);

          } catch (SearchException se) {
            errorOnLastQuery = true;
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            centerPanel.removeAll();
            String[] message = se.getMessage().split("\\n");
            if (message.length == 1) {
              // some errors to fix into QueryParser
              se.printStackTrace();
              return;
            }
            // message[0] contains the Java error
            JTextArea jta = new JTextArea(message[1]);
            jta.setForeground(Color.RED);
            centerPanel.add(jta, gbc);
            jta = new JTextArea(message[2]);
            if (message.length > 3) {
              jta.setText(message[2]+"\n"+message[3]);
            }
            jta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            centerPanel.add(jta, gbc);

          } catch(Exception e) {
            e.printStackTrace();
          }
          pageOfResults = 1;
          titleResults.setText("Page "+pageOfResults+" ("+searcher.getHits().length+" results)");
        }
      });
    }
  }

  /**
   * Finds out the next few results.
   */
  protected class NextResultsAction extends AbstractAction {

    private static final long serialVersionUID = 3257005436719871288L;

    public NextResultsAction() {
      super("Next Page of Results");
//      super("", MainFrame.getIcon("annic-next"));
      super.putValue(SHORT_DESCRIPTION, "Show next page of results.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_RIGHT);
    }

    public void actionPerformed(ActionEvent ae) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {

          thisInstance.setEnabled(false);
          noOfPatterns = ((Number)numberOfResultsSpinner.getValue()).intValue();
          try {
            searcher.next(noOfPatterns);
          }
          catch(Exception e) {
            e.printStackTrace();
            thisInstance.setEnabled(true);
          }
          processFinished();
          pageOfResults++;
          titleResults.setText("Page "+pageOfResults+" ("+searcher.getHits().length+" results)");
          thisInstance.setEnabled(true);
          if (searcher.getHits().length < noOfPatterns) {
            nextResultsAction.setEnabled(false);
          }
        }
      });
    }
  }

  /**
   * Manage the annotation rows to display in the central view of the GUI.
   */
  protected class DisplayAnnotationRowsManagerAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public DisplayAnnotationRowsManagerAction() {
      super("Modify Rows");
//      super("", MainFrame.getIcon("add.gif"));
      super.putValue(SHORT_DESCRIPTION, "Display the annotation rows manager.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_LEFT);
    }

    public void actionPerformed(ActionEvent e) {
      annotationRowsManager.setVisible(false);
      annotationRowsManager.setVisible(true);
    }
  }

  /**
   * Add at the caret position or replace the selection in the query
   * according to the text row value left clicked.
   */
  protected class TextRowMouseListener
    extends javax.swing.event.MouseInputAdapter {

    private String text;
    
    public TextRowMouseListener(String text) {
      this.text = text;
    }

    public void mousePressed(MouseEvent me) {
      if (!me.isPopupTrigger()) {
        int caretPosition = queryTextArea.getCaretPosition();
        String query = queryTextArea.getText();
        String queryMiddle = text;
        String queryLeft =
          (queryTextArea.getSelectionStart() == queryTextArea.getSelectionEnd())?
          query.substring(0, caretPosition):
          query.substring(0, queryTextArea.getSelectionStart());
        String queryRight =
          (queryTextArea.getSelectionStart() == queryTextArea.getSelectionEnd())?
          query.substring(caretPosition, query.length()):
          query.substring(queryTextArea.getSelectionEnd(), query.length());
        queryTextArea.setText(queryLeft+queryMiddle+queryRight);
      }
    }
  }

  /**
   * Modifies the query or displays statistics according to the
   * annotation row value left or right clicked.
   */
  protected class AnnotationRowValueMouseListener
    extends javax.swing.event.MouseInputAdapter {

    private String type;
    private String feature;
    private String text;
    private JPopupMenu mousePopup;
    JMenuItem menuItem;
    final String corpusID =
      (corpusToSearchIn.getSelectedItem().equals(Constants.ENTIRE_DATASTORE))?
      null:(String)corpusIds.get(corpusToSearchIn.getSelectedIndex() - 1);
    final String annotationSetID =
      (annotationSetsToSearchIn.getSelectedItem().equals(Constants.ALL_SETS))?
      null:(String)annotationSetsToSearchIn.getSelectedItem();
    final String corpusName = (String)corpusToSearchIn.getSelectedItem();
    final String annotationSetName = (String)annotationSetsToSearchIn.getSelectedItem();

    public AnnotationRowValueMouseListener(
            String type, String feature, String text) {
      this.type = type;
      this.feature = feature;
      this.text = text;
    }

    public AnnotationRowValueMouseListener(String type) {
      this.type = type;
      this.feature = null;
      this.text = null;
    }

    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger() && type != null && feature != null) {
        createPopup();
        mousePopup.show(e.getComponent(), e.getX(), e.getY());
      } else {
        updateQuery();
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger() && type != null && feature != null) {
        createPopup();
        mousePopup.show(e.getComponent(), e.getX(), e.getY());
      }
    }

    private void updateQuery() {
      int caretPosition = queryTextArea.getCaretPosition();
      String query = queryTextArea.getText();
      String queryMiddle;

      if (type != null && feature != null) {
        int row = findAnnotationRow(ANNOTATION_TYPE, type, FEATURE, feature);
        if (row >= 0 && !annotationRows[row][SHORTCUT].equals("")) {
          queryMiddle = "{"+annotationRows[row][SHORTCUT]+"==\""+text+"\"}";
        } else {
          queryMiddle = "{"+type+"."+feature+"==\""+text+"\"}";
        }
      } else if (type != null) {
        queryMiddle = "{"+type+"}";
        
      } else {
        queryMiddle = text;
      }
        String queryLeft =
          (queryTextArea.getSelectionStart() == queryTextArea.getSelectionEnd())?
            query.substring(0, caretPosition):
            query.substring(0, queryTextArea.getSelectionStart());
        String queryRight =
          (queryTextArea.getSelectionStart() == queryTextArea.getSelectionEnd())?
            query.substring(caretPosition, query.length()):
            query.substring(queryTextArea.getSelectionEnd(), query.length());
        queryTextArea.setText(queryLeft+queryMiddle+queryRight);
      }
    
    private void createPopup() {
        final String value;
        if (text.replace("\\s", "").length() > 20) {
          value = text.replace("\\s", "").substring(0, 20)+("...");
        } else {
          value = text.replace("\\s", "");
        }

        mousePopup = new JPopupMenu();

        menuItem = new JMenuItem("Occurrences in data store");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(corpusID, annotationSetID,
                                    type, feature, text);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+"."+feature+"==\""+value+"\""
              +" (datastore)", new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add("Statistics in data store"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(patterns, type,
                feature, text, true, false);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+"."+feature+"==\""+value+"\""
              +" (matches)", new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add("Statistics in matches"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(patterns, type,
                      feature, text, false, true);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+"."+feature+"==\""+value+"\""
              +" (contexts)", new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add("Statistics in contexts"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches+contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(patterns, type,
                      feature, text, true, true);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+"."+feature+"==\""+value+"\""
              +" (mch+ctxt)", new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add(
              "Statistics in matches+contexts"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);
    }
  }

  /**
   * Displays statistics according to the annotation row header right-clicked.
   */
  protected class AnnotationRowHeaderMouseListener
    extends javax.swing.event.MouseInputAdapter {

    private String type;
    private String feature;
    private JPopupMenu mousePopup;
    JMenuItem menuItem;
    XJTable table;
    final String corpusID =
      (corpusToSearchIn.getSelectedItem().equals(Constants.ENTIRE_DATASTORE))?
      null:(String)corpusIds.get(corpusToSearchIn.getSelectedIndex() - 1);
    final String annotationSetID =
      (annotationSetsToSearchIn.getSelectedItem().equals(Constants.ALL_SETS))?
      null:(String)annotationSetsToSearchIn.getSelectedItem();
    final String corpusName = (String)corpusToSearchIn.getSelectedItem();
    final String annotationSetName = (String)annotationSetsToSearchIn.getSelectedItem();
    
    public AnnotationRowHeaderMouseListener(String type, String feature) {
      this.type = type;
      this.feature = feature;
    }

    public AnnotationRowHeaderMouseListener(String type) {
      this.type = type;
      this.feature = null;
    }

    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) {
        createPopup();
        mousePopup.show(e.getComponent(), e.getX(), e.getY());
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        createPopup();
        mousePopup.show(e.getComponent(), e.getX(), e.getY());
      }
    }

    private void createPopup() {
      mousePopup = new JPopupMenu();

      if (type != null && feature != null) {

        // count values for one Feature of an Annotation type

        menuItem = new JMenuItem("Occurrences in data store");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              // TODO: gives always zero
              count = searcher.freq(corpusID, annotationSetID, type, feature);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+"."+feature
              +" (datastore)", new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add("Statistics in data store"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(patterns, type,
                feature, null, true, false);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+"."+feature
              +" (matches)", new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add("Statistics in matches"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(patterns, type,
                      feature, null, false, true);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+"."+feature
              +" (contexts)", new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add("Statistics in contexts"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches+contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(patterns, type,
                      feature, null, true, true);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+"."+feature
              +" (mch+ctxt)", new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add(
              "Statistics in matches+contexts"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        // count values for all Features of an Annotation Type

        mousePopup.addSeparator();

        menuItem = new JMenuItem("All values from matches");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            Map<String, Integer> freqs;
            try { // retrieves the number of occurrences
              freqs = searcher.freqForAllValues(
                      patterns, type, feature, true, false);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn(type+'.'+feature+" (matches)");
            model.addColumn("Count");
            for (Map.Entry<String,Integer> map : freqs.entrySet()) {
              model.addRow(new Object[]{map.getKey(), map.getValue()});
            }
            table = new XJTable() {
              private static final long serialVersionUID = 1L;
              public boolean isCellEditable(int rowIndex, int vColIndex) {
                return false;
              }
            };
            table.setModel(model);
            table.setComparator(0, stringCollator);
            table.setComparator(1, integerComparator);
            JScrollPane scrollPaneTable = new JScrollPane(table);
            statisticsTabbedPane.addTab(
              String.valueOf(statisticsTabbedPane.getTabCount()-1),
              null, scrollPaneTable,
              "<html>Statistics in matches"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString()
              +"</html>");
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(
              statisticsTabbedPane.getTabCount()-1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("All values from contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            Map<String, Integer> freqs;
            try { // retrieves the number of occurrences
              freqs = searcher.freqForAllValues(
                      patterns, type, feature, false, true);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn(type+'.'+feature+" (contexts)");
            model.addColumn("Count");
            for (Map.Entry<String,Integer> map : freqs.entrySet()) {
              model.addRow(new Object[]{map.getKey(), map.getValue()});
            }
            table = new XJTable() {
              private static final long serialVersionUID = 1L;
              public boolean isCellEditable(int rowIndex, int vColIndex) {
                return false;
              }
            };
            table.setModel(model);
            table.setComparator(0, stringCollator);
            table.setComparator(1, integerComparator);
            statisticsTabbedPane.addTab(
              String.valueOf(statisticsTabbedPane.getTabCount()-1),
              null, new JScrollPane(table),
              "<html>Statistics in contexts"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString()
              +"</html>");
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(
              statisticsTabbedPane.getTabCount()-1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("All values from matches+contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            Map<String, Integer> freqs;
            try { // retrieves the number of occurrences
              freqs = searcher.freqForAllValues(
                      patterns, type, feature, true, true);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn(type+'.'+feature+" (mch+ctxt)");
            model.addColumn("Count");
            for (Map.Entry<String,Integer> map : freqs.entrySet()) {
              model.addRow(new Object[]{map.getKey(), map.getValue()});
            }
            table = new XJTable() {
              private static final long serialVersionUID = 1L;
              public boolean isCellEditable(int rowIndex, int vColIndex) {
                return false;
              }
            };
            table.setModel(model);
            table.setComparator(0, stringCollator);
            table.setComparator(1, integerComparator);
            statisticsTabbedPane.addTab(
              String.valueOf(statisticsTabbedPane.getTabCount()-1),
              null, new JScrollPane(table),
              "<html>Statistics in matches+contexts"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString()
              +"</html>");
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(
              statisticsTabbedPane.getTabCount()-1);
          }
        });
        mousePopup.add(menuItem);

      } else {
        // count values of one Annotation type

        menuItem = new JMenuItem("Occurrences in data store");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(corpusID, annotationSetID, type);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+" (datastore)",
              new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add("Statistics in data store"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(patterns, type, true, false);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+" (matches)",
              new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add("Statistics in matches"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(patterns, type, false, true);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+" (contexts)",
              new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add("Statistics in contexts"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
            statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches+contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            int count;
            try { // retrieves the number of occurrences
              count = searcher.freq(patterns, type, true, true);
            } catch(SearchException se) {
              se.printStackTrace();
              return;
            }
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Annotation Type/Feature");
            model.addColumn("Count");
            for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
              model.addRow(new Object[]{
                oneRowStatisticsTable.getValueAt(row,0),
                oneRowStatisticsTable.getValueAt(row,1)});
            }
            model.addRow(new Object[]{type+" (mch+ctxt)",
              new Integer(count)});
            oneRowStatisticsTable.setModel(model);
            oneRowStatisticsTable.setComparator(0, stringCollator);
            oneRowStatisticsTable.setComparator(1, integerComparator);
            oneRowStatisticsTableToolTips.add(
              "Statistics in matches+contexts"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString());
            if (!displayStatistics.isSelected()) {
              displayStatistics.doClick();
            }
             statisticsTabbedPane.setSelectedIndex(1);
          }
        });
        mousePopup.add(menuItem);
      }
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
          return "Annotation set";
        case LEFT_CONTEXT_COLUMN:
          return "Left context";
        case PATTERN_COLUMN:
          return "Match";
        case RIGHT_CONTEXT_COLUMN:
          return "Right context";
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
   * Panel that shows a table of shortcut, annotation type and feature
   * to display in the central view of the GUI.
   */
  protected class AnnotationRowsManagerFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final int REMOVE = columnNames.length;
    private JTable annotationRowsJTable;
    private JComboBox cropBox;

    public AnnotationRowsManagerFrame(String title) {
      super(title);

      setLayout(new BorderLayout());

      JScrollPane scrollPane = new JScrollPane(
              JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
              JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.getViewport().setOpaque(true);

      annotationRowsManagerTableModel = new AnnotationRowsManagerTableModel();
      annotationRowsJTable = new XJTable(annotationRowsManagerTableModel);
      ((XJTable)annotationRowsJTable).setSortable(false);

      // combobox used as cell editor

      String[] s = {"Crop middle", "Crop start", "Crop end"};
      cropBox = new JComboBox(s);

      // set the cell renderer and/or editor for each column

      annotationRowsJTable.getColumnModel().getColumn(DISPLAY)
        .setCellRenderer(new DefaultTableCellRenderer() {
          private static final long serialVersionUID = 1L;
          public Component getTableCellRendererComponent(
            JTable table, Object color, boolean isSelected,
            boolean hasFocus, int row, int col) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            checkBox.setToolTipText(
                    "Tick to display this row in central section.");
            checkBox.setSelected(
                    (table.getValueAt(row, col).equals("false"))?false:true);
            return checkBox;
          }});

      final class DisplayCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {
        private static final long serialVersionUID = 1L;
        JCheckBox checkBox;
        public DisplayCellEditor() {
          checkBox = new JCheckBox();
          checkBox.setHorizontalAlignment(SwingConstants.CENTER);
          checkBox.addActionListener(this);
        }
        public boolean shouldSelectCell(EventObject anEvent) {
          return false;
        }
        public void actionPerformed(ActionEvent e) {
          fireEditingStopped();
        }
        public Object getCellEditorValue() {
          return (checkBox.isSelected())?"true":"false";
        }
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int col) {
          checkBox.setSelected(
                  (table.getValueAt(row, col).equals("false"))?false:true);
          return checkBox;
        }
      }
      annotationRowsJTable.getColumnModel().getColumn(DISPLAY)
        .setCellEditor(new DisplayCellEditor());

      annotationRowsJTable.getColumnModel().getColumn(SHORTCUT)
        .setCellRenderer(new DefaultTableCellRenderer() {
          private static final long serialVersionUID = 1L;
          public Component getTableCellRendererComponent(
            JTable table, Object color, boolean isSelected,
            boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(
              table, color, isSelected, hasFocus, row, col);
            if (c instanceof JComponent) {
              ((JComponent)c).setToolTipText("Shortcut can be used in queries "
                +"instead of \"AnnotationType.Feature\".");
            }
            return c;
          }});

      DefaultCellEditor cellEditor = new DefaultCellEditor(new JTextField());
      cellEditor.setClickCountToStart(0);
      annotationRowsJTable.getColumnModel()
        .getColumn(SHORTCUT)
        .setCellEditor(cellEditor);

      annotationRowsJTable.getColumnModel()
      .getColumn(ANNOTATION_TYPE)
      .setCellRenderer(new DefaultTableCellRenderer() {
        private static final long serialVersionUID = 1L;
        public Component getTableCellRendererComponent(
          JTable table, Object color, boolean isSelected,
          boolean hasFocus, int row, int col) {
          String[] s = {annotationRows[row][ANNOTATION_TYPE]};
          JComboBox comboBox = new JComboBox(s);
          return comboBox;
        }
      });

      final class featureCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {
        private static final long serialVersionUID = 1L;
        private JComboBox featuresBox;
        public featureCellEditor() {
          featuresBox = new JComboBox();
          featuresBox.setMaximumRowCount(10);
          featuresBox.addActionListener(this);
        }
        public void actionPerformed(ActionEvent e) {
          fireEditingStopped();
        }
        public Object getCellEditorValue() {
          return (featuresBox.getSelectedItem() == null)?
                   "":featuresBox.getSelectedItem();
        }
        public Component getTableCellEditorComponent(JTable table,
          Object value, boolean isSelected, int row, int col) {
          TreeSet<String> ts = new TreeSet<String>(stringCollator);
          if (populatedAnnotationTypesAndFeatures.containsKey((String)
               annotationRowsJTable.getValueAt(row, ANNOTATION_TYPE))) {
            // this annotation type still exists in the datastore
            ts.addAll(populatedAnnotationTypesAndFeatures.get((String)
              annotationRowsJTable.getValueAt(row, ANNOTATION_TYPE)));
          }
          DefaultComboBoxModel dcbm = new DefaultComboBoxModel(ts.toArray());
          dcbm.insertElementAt("", 0);
          featuresBox.setModel(dcbm);
          featuresBox.setSelectedItem(
            (ts.contains(annotationRowsJTable.getValueAt(row, col)))?
              annotationRowsJTable.getValueAt(row, col):"");
          return featuresBox;
        }
      }
      annotationRowsJTable.getColumnModel().getColumn(FEATURE)
        .setCellEditor(new featureCellEditor());

      annotationRowsJTable.getColumnModel()
      .getColumn(FEATURE)
      .setCellRenderer(new DefaultTableCellRenderer() {
        private static final long serialVersionUID = 1L;
        public Component getTableCellRendererComponent(
          JTable table, Object color, boolean isSelected,
          boolean hasFocus, int row, int col) {
          String[] s = {annotationRows[row][FEATURE]};
          JComboBox comboBox = new JComboBox(s);
          return comboBox;
        }
      });

      cellEditor = new DefaultCellEditor(cropBox);
      cellEditor.setClickCountToStart(0);
      annotationRowsJTable.getColumnModel()
        .getColumn(CROP)
        .setCellEditor(cellEditor);
      annotationRowsJTable.getColumnModel()
      .getColumn(CROP)
      .setCellRenderer(new DefaultTableCellRenderer() {
        private static final long serialVersionUID = 1L;
        public Component getTableCellRendererComponent(
          JTable table, Object color, boolean isSelected,
          boolean hasFocus, int row, int col) {
          String[] s = {annotationRows[row][CROP]};
          JComboBox comboBox = new JComboBox(s);
          return comboBox;
        }
      });

      annotationRowsJTable.getColumnModel().getColumn(REMOVE)
        .setCellRenderer(new DefaultTableCellRenderer() {
          private static final long serialVersionUID = 1L;
          public Component getTableCellRendererComponent(
            JTable table, Object color, boolean isSelected,
            boolean hasFocus, int row, int col) {
            JButton button = new JButton();
            button.setHorizontalAlignment(SwingConstants.CENTER);
            if (row == numAnnotationRows) {
              // add button if it's the last row of the table
              button.setIcon(MainFrame.getIcon("add.gif"));
              button.setToolTipText("Click to add this line.");
            } else {
              // remove button otherwise
              button.setIcon(MainFrame.getIcon("delete.gif"));
              button.setToolTipText("Click to remove this line.");
            }
            return button;
          }
        });

      final class AddRemoveCellEditor extends AbstractCellEditor
      implements TableCellEditor, ActionListener {
        private static final long serialVersionUID = 1L;
        private JButton button;
        private int row;
        private boolean addButton;
        public AddRemoveCellEditor() {
          button = new JButton();
          button.setHorizontalAlignment(SwingConstants.CENTER);
          button.addActionListener(this);
        }
        public boolean shouldSelectCell(EventObject anEvent) {
          return false;
        }
        public void actionPerformed(ActionEvent e) {
          if (addButton) {
            if (annotationRows[row][ANNOTATION_TYPE] != null
             && !annotationRows[row][ANNOTATION_TYPE].equals("")) {
              if (numAnnotationRows == maxAnnotationRows) {
                JOptionPane.showMessageDialog(annotationRowsManager,
                  "The number of rows is limited to "+maxAnnotationRows+".",
                  "Alert", JOptionPane.ERROR_MESSAGE);
              } else {
                // add a new row
                numAnnotationRows++;
                annotationRowsManagerTableModel
                  .fireTableRowsInserted(row, row+1);
                updateCentralView();
                saveConfiguration();
              }
            } else {
              JOptionPane.showMessageDialog(annotationRowsManager,
                "Please, fill the Annotation type column at least.",
                "Alert", JOptionPane.ERROR_MESSAGE);
            }
          } else {
            // delete a row
            deleteAnnotationRow(row);
            annotationRowsManagerTableModel.fireTableDataChanged();
            updateCentralView();
            saveConfiguration();
          }
          fireEditingStopped();
        }
        public Object getCellEditorValue() {
          return null;
        }
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int col) {
          this.row = row;
          this.addButton = (row == numAnnotationRows);
          return button;
        }
      }
      annotationRowsJTable.getColumnModel().getColumn(REMOVE)
        .setCellEditor(new AddRemoveCellEditor());

      scrollPane.setViewportView(annotationRowsJTable);

      add(scrollPane, BorderLayout.CENTER);
    }
    
    public JTable getTable() {
      return annotationRowsJTable;
    }
  }

  /**
   * Table model for the annotation rows manager.
   */
  protected class AnnotationRowsManagerTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 3977012959534854193L;
    private final int REMOVE = columnNames.length;

    // plus one to let the user adding a new row
    public int getRowCount() {
      return Math.min(numAnnotationRows+1, maxAnnotationRows+1);
    }

    // plus one for the add/remove column
    public int getColumnCount() {
      return columnNames.length+1;
    }

    public String getColumnName(int col) {
      return (col == REMOVE)?"Add/Remove":columnNames[col];
    }

    public boolean isCellEditable(int row, int col) {
      return true;
    }

    public Class<?> getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }

    public Object getValueAt(int row, int col) {
      if (col == REMOVE) { return null; }
      return annotationRows[row][col];
    }

    public void setValueAt(Object value, int row, int col) {
      if (col == REMOVE) { return; }

      if (col == SHORTCUT && !value.equals("")) {
        if (getAnnotTypesFeatures(null, null).keySet().contains(value)) {
          JOptionPane.showMessageDialog(annotationRowsManager,
            "A Shortcut cannot have the same name as an Annotation type.",
            "Alert", JOptionPane.ERROR_MESSAGE);
          return;
        } else {
          int row2 = findAnnotationRow(SHORTCUT, value);
          if (row2 >= 0 && row2 != row) {
            JOptionPane.showMessageDialog(annotationRowsManager,
              "A Shortcut with the same name already exists.",
              "Alert", JOptionPane.ERROR_MESSAGE);
            return;
          }
        }
      }

      String previousValue = value.toString();
      annotationRows[row][col] = value.toString();

      if (!annotationRows[row][SHORTCUT].equals("")) {
        if (annotationRows[row][ANNOTATION_TYPE].equals("")
         || annotationRows[row][FEATURE].equals("")) {
          // TODO table should be update
          annotationRows[row][col] = previousValue;
          fireTableCellUpdated(row, col);
          annotationRowsManager.getTable().getColumnModel().getColumn(col)
            .getCellEditor().cancelCellEditing();
          JOptionPane.showMessageDialog(annotationRowsManager,
            "A Shortcut need to have a Feature.\n"
            +"Please choose a Feature or delete the Shortcut value.",
            "Alert", JOptionPane.ERROR_MESSAGE);
          return;
        } else {
          int row2 = findAnnotationRow(
                  ANNOTATION_TYPE, annotationRows[row][ANNOTATION_TYPE],
                  FEATURE,         annotationRows[row][FEATURE]);
          if (row2 >= 0 && row2 != row
           && !annotationRows[row2][SHORTCUT].equals("")) {
            annotationRows[row][col] = previousValue;
            annotationRowsManager.getTable().getColumnModel().getColumn(col)
              .getCellEditor().cancelCellEditing();
            JOptionPane.showMessageDialog(annotationRowsManager,
              "You can only have one Shortcut for a couple (Annotation "
              +"type, Feature).", "Alert", JOptionPane.ERROR_MESSAGE);
            return;
          }
        }
      }

      if (annotationRows[row][DISPLAY].equals("one time")) {
        // make a temporary row permanent if the user changes it
        annotationRows[row][DISPLAY] = "true";
      }

      annotationRows[row][col] = value.toString();
      fireTableRowsUpdated(row, row);
      updateCentralView();
      saveConfiguration();
    }
  }

  /**
   * JtextArea with key autocompletion for the annotation types and
   * features, mouse autocompletion for the "number of times" operators
   * and undo/redo.
   */
  protected class QueryTextArea extends JTextArea
    implements DocumentListener, MouseListener {

    private static final long serialVersionUID = 1L;
    private static final String ENTER_ACTION = "enter";
    private static final String NEW_LINE = "new line";
    private static final String PERIOD_ACTION = "period";
    private static final String CANCEL_ACTION = "cancel";
    private static final String DOWN_ACTION = "down";
    private static final String UP_ACTION = "up";
    private static final String UNDO_ACTION = "undo";
    private static final String REDO_ACTION = "redo";
    private static final String NEXT_RESULT = "next result";
    private static final String PREVIOUS_RESULT = "previous result";
    private DefaultListModel queryListModel;
    private JList queryList;
    private JWindow queryPopupWindow;
    private JPopupMenu mousePopup;
    private javax.swing.undo.UndoManager undo;
    private UndoAction undoAction;
    private RedoAction redoAction;
    private int initialPosition;
    private int finalPosition;
    private int mode;
    private static final int INSERT = 0;
    private static final int COMPLETION = 1;
    private static final int POPUP_TYPES = 2;
    private static final int POPUP_FEATURES = 3;
    private static final int PROGRAMMATIC_INSERT = 4;

    public QueryTextArea() {
      super();

      getDocument().addDocumentListener(this);
      addMouseListener(this);
      addAncestorListener(new AncestorListener() {
        public void ancestorMoved(AncestorEvent event) {}
        public void ancestorAdded(AncestorEvent event) {}
        public void ancestorRemoved(AncestorEvent event) {
          // no parent so need to be disposed explicitly
          queryPopupWindow.dispose();
        }
      });

      InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
      InputMap imw = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      ActionMap am = getActionMap();
      // bind keys to actions
      im.put(KeyStroke.getKeyStroke("ENTER"), ENTER_ACTION);
      am.put(ENTER_ACTION, new EnterAction());
      im.put(KeyStroke.getKeyStroke("control ENTER"), NEW_LINE);
      am.put(NEW_LINE, new NewLineAction());
      im.put(KeyStroke.getKeyStroke("PERIOD"), PERIOD_ACTION);
      am.put(PERIOD_ACTION, new PeriodActionWhenPopupTypesMode());
      imw.put(KeyStroke.getKeyStroke("ESCAPE"), CANCEL_ACTION);
      am.put(CANCEL_ACTION, new CancelAction());
      im.put(KeyStroke.getKeyStroke("DOWN"), DOWN_ACTION);
      am.put(DOWN_ACTION, new DownAction());
      im.put(KeyStroke.getKeyStroke("UP"), UP_ACTION);
      am.put(UP_ACTION, new UpAction());
      undoAction = new UndoAction();
      im.put(KeyStroke.getKeyStroke("control Z"), UNDO_ACTION);
      am.put(UNDO_ACTION, undoAction);
      redoAction = new RedoAction();
      im.put(KeyStroke.getKeyStroke("control Y"), REDO_ACTION);
      am.put(REDO_ACTION, redoAction);
      im.put(KeyStroke.getKeyStroke("alt DOWN"), NEXT_RESULT);
      am.put(NEXT_RESULT, new NextResultAction());
      im.put(KeyStroke.getKeyStroke("alt UP"), PREVIOUS_RESULT);
      am.put(PREVIOUS_RESULT, new PreviousResultAction());

      // list for autocompletion
      queryListModel = new DefaultListModel();
      queryList = new JList(queryListModel);
      queryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      queryList.setBackground(Color.WHITE);
      queryList.setVisibleRowCount(10);
      queryList.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) {
            int index = queryList.locationToIndex(e.getPoint());
            mode = INSERT;
            try {
              if (getText().charAt(finalPosition) == '}') { finalPosition--; }
              getDocument().remove(initialPosition+1,
                finalPosition-initialPosition);
              getDocument().insertString(initialPosition+1,
                (String)queryList.getModel().getElementAt(index), null);
              setCaretPosition(getCaretPosition()+1);
            } catch (javax.swing.text.BadLocationException ble) {
              ble.printStackTrace();
            }
            cleanup();
          }
        }
      });

      queryPopupWindow = new JWindow();
      queryPopupWindow.add(new JScrollPane(queryList));

      mousePopup = new JPopupMenu();

      JMenuItem menuItem = new JMenuItem("0 to 1 time");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          try {
          getDocument().insertString(getSelectionStart(), "(", null);
          getDocument().insertString(getSelectionEnd(), ")*1", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
        }
      });
      mousePopup.add(menuItem);
      menuItem = new JMenuItem("0 to 2 times");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          try {
          getDocument().insertString(getSelectionStart(), "(", null);
          getDocument().insertString(getSelectionEnd(), ")*2", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
        }
      });
      mousePopup.add(menuItem);
      menuItem = new JMenuItem("0 to 3 times");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          try {
          getDocument().insertString(getSelectionStart(), "(", null);
          getDocument().insertString( getSelectionEnd(), ")*3", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
        }
      });
      mousePopup.add(menuItem);
      menuItem = new JMenuItem("0 to 4 times");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          try {
          getDocument().insertString(getSelectionStart(), "(", null);
          getDocument().insertString(getSelectionEnd(), ")*4", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
        }
      });
      mousePopup.add(menuItem);
      menuItem = new JMenuItem("0 to 5 times");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          try {
          getDocument().insertString(getSelectionStart(), "(", null);
          getDocument().insertString(getSelectionEnd(), ")*5", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
        }
      });
      mousePopup.add(menuItem);
      menuItem = new JMenuItem("1 to 2 times");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          try {
          getDocument().insertString(getSelectionStart(), "(", null);
          getDocument().insertString(getSelectionEnd(), ")+2", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
        }
      });
      mousePopup.add(menuItem);
      menuItem = new JMenuItem("1 to 3 times");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          try {
          getDocument().insertString(getSelectionStart(), "(", null);
          getDocument().insertString(getSelectionEnd(), ")+3", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
        }
      });
      mousePopup.add(menuItem);
      menuItem = new JMenuItem("1 to 4 times");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          try {
          getDocument().insertString(getSelectionStart(), "(", null);
          getDocument().insertString(getSelectionEnd(), ")+4", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
        }
      });
      mousePopup.add(menuItem);
      menuItem = new JMenuItem("1 to 5 times");
      menuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ie) {
          try {
          getDocument().insertString(getSelectionStart(), "(", null);
          getDocument().insertString(getSelectionEnd(), ")+5", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
        }
      });
      mousePopup.add(menuItem);

      undo = new javax.swing.undo.UndoManager();
      getDocument().addUndoableEditListener(
              new javax.swing.event.UndoableEditListener() {
        public void undoableEditHappened(
                javax.swing.event.UndoableEditEvent e) {
          //Remember the edit and update the menus
          undo.addEdit(e.getEdit());
          undoAction.updateUndoState();
          redoAction.updateRedoState();
        }
      });

      initialPosition = 0;
      finalPosition = 0;
      mode = INSERT;
      
      addCaretListener(new CaretListener() {
        public void caretUpdate(CaretEvent e) {
          if ( (mode == POPUP_TYPES || mode == POPUP_FEATURES)
            && (getCaretPosition() > finalPosition
             || getCaretPosition() <= initialPosition) ) {
            // cancel any autocompletion if the user put the caret
            // outside brackets when in POPUP mode
            cleanup();
            return;
          }
        }
      });
    }

    public void changedUpdate(DocumentEvent ev) {
    }

    public void removeUpdate(DocumentEvent ev) {
      int pos = ev.getOffset()-1;

      if (ev.getLength() != 1
      || ( (pos+1 >= finalPosition || pos < initialPosition)
        && (mode == POPUP_TYPES || mode == POPUP_FEATURES) )) {
        // cancel any autocompletion if the user cut some text
        // or delete outside brackets when in POPUP mode
        cleanup();
        return;
      }

      if (mode == POPUP_TYPES) {
        finalPosition = pos+1;
        String type = getText().substring(initialPosition+1, pos+1);
        if (!type.matches("[a-zA-Z0-9]+")) { return; }
        for (int i = 0; i < queryList.getModel().getSize(); i++) {
          if (startsWithIgnoreCase(((String)queryList.getModel()
              .getElementAt(i)), type)) {
            queryList.setSelectedIndex((i));
            queryList.ensureIndexIsVisible(i);
            break;
          }
        }
      } else if (mode == POPUP_FEATURES) {
        finalPosition = pos+1;
        String feature = getText().substring(initialPosition+1, pos+1);
        if (!feature.matches("[a-zA-Z0-9]+")) { return; }
        for (int i = 0; i < queryList.getModel().getSize(); i++) {
          if (startsWithIgnoreCase(((String)queryList.getModel()
              .getElementAt(i)), feature)) {
            queryList.setSelectedIndex((i));
            queryList.ensureIndexIsVisible(i);
            break;
          }
        }
      }
    }

    public void insertUpdate(DocumentEvent ev) {
      if (mode == PROGRAMMATIC_INSERT) { return; }

      int pos = ev.getOffset();

      if (ev.getLength() != 1) {
        // cancel any autocompletion if the user paste some text
        cleanup();
        return;
      }

      String typedChar = Character.toString(getText().charAt(pos));
      String previousChar = (pos > 0)?
        Character.toString(getText().charAt(pos-1)):"";
      String nextChar = ((pos+1) < getText().length())?
        Character.toString(getText().charAt(pos+1)):"";

      // switch accordinly to the key pressed and the context
      if (typedChar.equals("\"")
      && !previousChar.equals("\\")
      && mode == INSERT) {
        mode = COMPLETION;
        SwingUtilities.invokeLater(new CompletionTask("\"", pos+1));

      } else if (typedChar.equals("{")
             && !previousChar.equals("\\")
             && mode == INSERT) {
        mode = POPUP_TYPES;
        initialPosition = pos;
        finalPosition = pos+1;
        SwingUtilities.invokeLater(new PopupTypeTask("}", pos+1));

      } else if (typedChar.equals(",")
             && nextChar.equals("}")
             && mode == INSERT) {
        mode = POPUP_TYPES;
        initialPosition = pos;
        finalPosition = pos+1;
        SwingUtilities.invokeLater(new PopupTypeTask("", pos+1));

      } else if (typedChar.equals(".")
              && mode == INSERT) {
        mode = POPUP_FEATURES;
        initialPosition = pos;
        finalPosition = pos+1;
        SwingUtilities.invokeLater(new PopupFeatureTask("==\"\"", pos+1));

      } else if (typedChar.matches("[a-zA-Z0-9]")
              && mode == POPUP_TYPES) {
        finalPosition = pos+1;
        String type = getText().substring(initialPosition+1, pos+1);
        if (!type.matches("[a-zA-Z0-9]+")) { return; }
        for (int i = 0; i < queryList.getModel().getSize(); i++) {
          if (startsWithIgnoreCase(((String)queryList.getModel()
              .getElementAt(i)), type)) {
            queryList.setSelectedIndex(i);
            queryList.ensureIndexIsVisible(i);
            break;
          }
        }

      } else if (typedChar.matches("[a-zA-Z0-9]")
              && mode == POPUP_FEATURES) {
        finalPosition = pos+1;
        String feature = getText().substring(initialPosition+1, pos+1);
        if (!feature.matches("[a-zA-Z0-9]+")) { return; }
        for (int i = 0; i < queryList.getModel().getSize(); i++) {
          if (startsWithIgnoreCase(((String)queryList.getModel()
              .getElementAt(i)), feature)) {
            queryList.setSelectedIndex(i);
            queryList.ensureIndexIsVisible(i);
            break;
          }
        }
      }
    }

    private boolean startsWithIgnoreCase(String str1, String str2) {
      return str1.toUpperCase().startsWith(str2.toUpperCase());
    }

    private void cleanup() {
      mode = INSERT;
      queryPopupWindow.setVisible(false);
    }

    private class CompletionTask implements Runnable {
      String completion;
      int position;

      CompletionTask(String completion, int position) {
        this.completion = completion;
        this.position = position;
      }

      public void run() {
        try {
          getDocument().insertString(position, completion, null);
          setCaretPosition(position + completion.length() - 1);
          cleanup();

        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
      }
    }

    private class PopupTypeTask implements Runnable {
      String completion;
      int position;

      PopupTypeTask(String completion, int position) {
        this.completion = completion;
        this.position = position;
      }

      public void run() {
        try {
        mode = PROGRAMMATIC_INSERT;
        getDocument().insertString(position, completion, null);
        mode = POPUP_TYPES;
        setCaretPosition(position);
        TreeSet<String> types = new TreeSet<String>(stringCollator);
        types.addAll(populatedAnnotationTypesAndFeatures.keySet());
        if (types.isEmpty()) {
          types.add("No annotation type found !");
        }
        queryListModel.clear();
        Iterator it = types.iterator();
        while (it.hasNext()) {
          queryListModel.addElement(it.next());
        }
        Rectangle dotRect = modelToView(getCaret().getDot());
        queryPopupWindow.setLocation(
          getLocationOnScreen().x // x location of top-left text field
        + new Double(dotRect.getMaxX()).intValue(), // caret X relative position
          getLocationOnScreen().y // y location of top-left text field
        + new Double(dotRect.getMaxY()).intValue()); // caret Y relative position
        queryPopupWindow.pack();
        queryPopupWindow.setVisible(true);

        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
      }
    }

    private class PopupFeatureTask implements Runnable {
      String completion;
      int position;

      PopupFeatureTask(String completion, int position) {
          this.completion = completion;
          this.position = position;
      }

      public void run() {
        // return the annotation type
        int index = Math.max(getText().substring(0, position).lastIndexOf("{"),
                    Math.max(getText().substring(0, position).lastIndexOf(","),
                             getText().substring(0, position).lastIndexOf(", ")+1));
        String type = getText().substring(index+1, position-1);
        if (!populatedAnnotationTypesAndFeatures.containsKey(type)) {
          mode = INSERT;
          return;
        }
        try {
        mode = PROGRAMMATIC_INSERT;
        getDocument().insertString(position, completion, null);
        mode = POPUP_FEATURES;
        setCaretPosition(position);
        TreeSet<String> features = new TreeSet<String>(stringCollator);
        features.addAll(populatedAnnotationTypesAndFeatures.get(type));
        if (features.size() == 1) {
          // if there is only one choice, write it directly without popup
          mode = PROGRAMMATIC_INSERT;
          try {
          getDocument().insertString(initialPosition+1, features.first(), null);
          setCaretPosition(getCaretPosition()+3);
          } catch (javax.swing.text.BadLocationException e) {
            e.printStackTrace();
          }
          mode = INSERT;
          return;
        }
        queryListModel.clear();
        Iterator it = features.iterator();
        while (it.hasNext()) {
          queryListModel.addElement(it.next());
        }
        Rectangle dotRect = modelToView(getCaret().getDot());
        queryPopupWindow.setLocation(
          getLocationOnScreen().x // x location of top-left text field
          + new Double(dotRect.getMaxX()).intValue(), // caret relative position
            getLocationOnScreen().y // y location of top-left text field
          + new Double(dotRect.getMaxY()).intValue()); // caret Y relative position
        queryPopupWindow.pack();
        queryPopupWindow.setVisible(true);

        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
      }
    }

    private class EnterAction extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent ev) {
        if (mode == POPUP_TYPES) {
          mode = INSERT;
          try {
          if (getText().charAt(finalPosition) == '}') { finalPosition--; }
          getDocument().remove(initialPosition+1,
            finalPosition-initialPosition);
          getDocument().insertString(initialPosition+1,
            (String)queryList.getSelectedValue(), null);
          setCaretPosition(getCaretPosition()+1);
          } catch (javax.swing.text.BadLocationException e) {
            e.printStackTrace();
          }
          cleanup();

        } else if (mode == POPUP_FEATURES) {
          mode = INSERT;
          try {
          if (getText().charAt(finalPosition) == '=') { finalPosition--; }
          getDocument().remove(initialPosition+1,
            finalPosition-initialPosition);
          getDocument().insertString(initialPosition+1,
            (String)queryList.getSelectedValue(), null);
          setCaretPosition(getCaretPosition()+3);
          } catch (javax.swing.text.BadLocationException e) {
            e.printStackTrace();
          }
          cleanup();

        } else {
          mode = INSERT;
          new ExecuteQueryAction().actionPerformed(null); 
        }
      }
    }

    private class PeriodActionWhenPopupTypesMode extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent ev) {
        if (mode == POPUP_TYPES) {
          mode = PROGRAMMATIC_INSERT;
          try {
          if (getText().charAt(finalPosition) == '}') { finalPosition--; }
          getDocument().remove(initialPosition+1,
            finalPosition-initialPosition);
          getDocument().insertString(initialPosition+1,
            (String)queryList.getSelectedValue(), null);
          queryPopupWindow.setVisible(false);
          mode = POPUP_FEATURES;
          initialPosition = getCaretPosition();
          finalPosition = initialPosition+1;
          SwingUtilities.invokeLater(new PopupFeatureTask("==\"\"", finalPosition));
          } catch (javax.swing.text.BadLocationException e) {
            e.printStackTrace();
          }
        }
      }
    }

    private class CancelAction extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent ev) {
        if (mode == POPUP_TYPES) {
          cleanup();
          try {
            if (getText().charAt(initialPosition) == ',') { finalPosition--; }
            getDocument().remove(initialPosition,
              finalPosition-initialPosition+1);
          } catch (javax.swing.text.BadLocationException e) {
            e.printStackTrace();
          }
        } else if (mode == POPUP_FEATURES) {
          cleanup();
            try {
              getDocument().remove(initialPosition,
                finalPosition-initialPosition+4);
            } catch (javax.swing.text.BadLocationException e) {
              e.printStackTrace();
            }
        } else {
          cleanup();
        }
      }
    }

    private class DownAction extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent ev) {
        if (mode == POPUP_TYPES) {
          int index = queryList.getSelectedIndex();
          if ((index+1) < queryList.getModel().getSize()) {
            queryList.setSelectedIndex(index+1);
            queryList.ensureIndexIsVisible(index+1);
          }
        } else if (mode == POPUP_FEATURES) {
          int index = queryList.getSelectedIndex();
          if ((index+1) < queryList.getModel().getSize()) {
            queryList.setSelectedIndex(index+1);
            queryList.ensureIndexIsVisible(index+1);
          }
        }
      }
    }

    private class UpAction extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent ev) {
        if (mode == POPUP_TYPES) {
          int index = queryList.getSelectedIndex();
          if (index > 0) {
            queryList.setSelectedIndex(index-1);
            queryList.ensureIndexIsVisible(index-1);
          }
        } else if (mode == POPUP_FEATURES) {
          int index = queryList.getSelectedIndex();
          if (index > 0) {
            queryList.setSelectedIndex(index-1);
            queryList.ensureIndexIsVisible(index-1);
          }
        }
      }
    }

    private class PreviousResultAction extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent ev) {
        if (patternTable.getSelectedRow() > 0) {
          patternTable.setRowSelectionInterval(patternTable.getSelectedRow()-1,
            patternTable.getSelectedRow()-1);
          patternTable.scrollRectToVisible(patternTable.getCellRect(
            patternTable.getSelectedRow()-1, 0, true));
        }
      }
    }

    private class NextResultAction extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent ev) {
        if (patternTable.getSelectedRow()+1 < patternTable.getRowCount()) {
          patternTable.setRowSelectionInterval(patternTable.getSelectedRow()+1,
            patternTable.getSelectedRow()+1);
          patternTable.scrollRectToVisible(patternTable.getCellRect(
            patternTable.getSelectedRow()+1, 0, true));
        }
      }
    }

    private class NewLineAction extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent ev) {
        try {
          getDocument().insertString(getCaretPosition(), "\n", null);
        } catch (javax.swing.text.BadLocationException e) {
          e.printStackTrace();
        }
      }
    }

    private class UndoAction extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public UndoAction() {
        super("Undo");
        setEnabled(false);
      }

      public void actionPerformed(ActionEvent e) {
        try {
          undo.undo();
        } catch (javax.swing.undo.CannotUndoException ex) {
          System.out.println("Unable to undo: " + ex);
          ex.printStackTrace();
        }
        updateUndoState();
        redoAction.updateRedoState();
      }

      protected void updateUndoState() {
        if (undo.canUndo()) {
          setEnabled(true);
          putValue(Action.NAME, undo.getUndoPresentationName());
        } else {
          setEnabled(false);
          putValue(Action.NAME, "Undo");
        }
      }
    }

    private class RedoAction extends AbstractAction {
      private static final long serialVersionUID = 1L;
      public RedoAction() {
        super("Redo");
        setEnabled(false);
      }

      public void actionPerformed(ActionEvent e) {
        try {
          undo.redo();
        } catch (javax.swing.undo.CannotRedoException ex) {
          System.out.println("Unable to redo: " + ex);
          ex.printStackTrace();
        }
        updateRedoState();
        undoAction.updateUndoState();
      }

      protected void updateRedoState() {
        if (undo.canRedo()) {
          setEnabled(true);
          putValue(Action.NAME, undo.getRedoPresentationName());
        } else {
          setEnabled(false);
          putValue(Action.NAME, "Redo");
        }
      }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) {
        createPopup(e);
        mousePopup.show(e.getComponent(), e.getX(), e.getY());
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        createPopup(e);
        mousePopup.show(e.getComponent(), e.getX(), e.getY());
      }
    }

    private void createPopup(MouseEvent e) {
      if (getSelectedText() != null
       && QueryParser.isValidQuery(getSelectedText())) {
          // if the selected text is a valid expression then shows a popup menu

        } else if (getDocument().getLength() > 3) {
          int positionclicked = viewToModel(e.getPoint());
          if (positionclicked >= getDocument().getLength()) {
            positionclicked = getDocument().getLength()-1;
          }
          int start = getText()
            .substring(0, positionclicked+1).lastIndexOf("{");
          int end = getText().substring(positionclicked, getDocument()
             .getLength()).indexOf("}") + positionclicked;
          if (start != -1 && end != -1
           && QueryParser.isValidQuery(getText().substring(start, end+1))) {
            // select the shortest valid enclosing braced expression
            // and shows a popup menu
            setSelectionStart(start);
            setSelectionEnd(end+1);
          }
        }
    }

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

    // standalone Java application
    if(target instanceof LuceneDataStoreImpl) {

      ((LuceneDataStoreImpl)target).addDatastoreListener(this);
      corpusToSearchIn.setEnabled(true);
      annotationSetsToSearchIn.setEnabled(true);
      searcher = ((LuceneDataStoreImpl)target).getSearcher();

//      try {
//        indexLocation = new File(((URL)((LuceneDataStoreImpl)target)
//          .getIndexer().getParameters().get(Constants.INDEX_LOCATION_URL))
//          .toURI()).getAbsolutePath();
//
//      } catch(URISyntaxException use) {
//        indexLocation = new File(((URL)((LuceneDataStoreImpl)target)
//          .getIndexer().getParameters().get(Constants.INDEX_LOCATION_URL))
//          .getFile()).getAbsolutePath();
//      }

      updateAnnotationSetsTypesFeatures();

      try {
        // get the corpus names from the datastore
        java.util.List corpusPIds = ((LuceneDataStoreImpl)target)
                .getLrIds(SerialCorpusImpl.class.getName());
        if(corpusIds != null) {
          for(int i = 0; i < corpusPIds.size(); i++) {
            String name =
              ((LuceneDataStoreImpl)target).getLrName(corpusPIds.get(i));
            this.corpusIds.add(corpusPIds.get(i));
            // add the corpus name to combobox
            ((DefaultComboBoxModel)corpusToSearchIn.getModel())
              .addElement(name);
          }
        }
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            corpusToSearchIn.updateUI();
            corpusToSearchIn.setSelectedItem(Constants.ENTIRE_DATASTORE);
          }
        });
      }
      catch(PersistenceException e) {
        System.out.println("Couldn't find any available corpusIds.");
        throw new GateRuntimeException(e);
      }
    }
    // Java Web Start application
    else {
      searcher = (Searcher)target;
      corpusToSearchIn.setEnabled(false);
      
      // find out all annotation sets that are indexed
      try {
        annotationSetIDsFromDataStore = searcher
                .getIndexedAnnotationSetNames();
        allAnnotTypesAndFeaturesFromDatastore = searcher
                .getAnnotationTypesMap();

        // lets fire the update event on combobox
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            updateAnnotationSetsToSearchInBox();
          }
        });
      }
      catch(SearchException e) {
        throw new GateRuntimeException(e);
      }
    }

    annotationSetsToSearchIn.setEnabled(true);
    executeQueryAction.setEnabled(true);
    queryTextArea.setEnabled(true);
    numberOfResultsSpinner.setEnabled(true);
    contextSizeSpinner.setEnabled(true);
//    clearQueryAction.setEnabled(true);
  }

  /**
   * Called when the process is finished, fires a refresh for this VR.
   */
  public void processFinished() {

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        executeQueryAction.setEnabled(true);
        queryTextArea.setEnabled(true);
        if(target instanceof LuceneDataStoreImpl) {
          corpusToSearchIn.setEnabled(true);
          annotationSetsToSearchIn.setEnabled(true);
        }
        else {
          corpusToSearchIn.setEnabled(false);
        }
        if (!showAllResultsCheckBox.isSelected()) {
          numberOfResultsSpinner.setEnabled(true);
        }
        contextSizeSpinner.setEnabled(true);
//        clearQueryAction.setEnabled(true);
//        animation.deactivate();
        updateDisplay();
      }
    });
  }

  /**
   * Animation when processing the query.
   * Stolen from gate.gui.MainFrame.
   */
  protected class Animation implements Runnable {
    JLabel progressLabel;

    public Animation() {
      active = false;
      dying = false;
      progressLabel = new JLabel(MainFrame.getIcon("working"));
      progressLabel.setOpaque(false);
      progressLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
//      progressLabel.setEnabled(true);
    }

    public boolean isActive() {
      boolean res;
      synchronized(lock) {
        res = active;
      }
      return res;
    }

    public void activate() {
      // add the label in the panel
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
//          centerPanel.setEnabled(true);
          centerPanel.removeAll();
//          centerPanel.repaint();
//          centerPanel.updateUI();
          centerPanel.add(progressLabel, new GridBagConstraints());
        }
      });
      // wake the dorment thread
      synchronized(lock) {
        active = true;
      }
    }

    public void deactivate() {
      // send the thread to sleep
      synchronized(lock) {
        active = false;
      }
      // clear the panel
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          centerPanel.removeAll();
//          centerPanel.repaint();
        }
      });
    }

    public void dispose() {
      synchronized(lock) {
        dying = true;
      }
    }

    public void run() {
      boolean isDying;
      synchronized(lock) {
        isDying = dying;
      }
      while(!isDying) {
        boolean isActive;
        synchronized(lock) {
          isActive = active;
        }
        if(isActive && centerPanel.isVisible()) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              centerPanel.invalidate();
              centerPanel.repaint();
              centerPanel.updateUI();
//              System.out.println("coucou");
            }
          });
        }
        // sleep
        try {
          Thread.sleep(100);
        }
        catch(InterruptedException ie) {
        }

        synchronized(lock) {
          isDying = dying;
        }
      }// while(!isDying)
    }

    boolean dying;
    boolean active;
    String lock = "lock";
  }


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

      // skip the first element in combo box that is "EntireDataStore"
      index++;

      // now lets remove it from the comboBox as well
      ((DefaultComboBoxModel)corpusToSearchIn.getModel())
              .removeElementAt(index);
    }
    updateAnnotationSetsTypesFeatures();
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
        ((DefaultComboBoxModel)corpusToSearchIn.getModel())
          .addElement(resource.getName());
      }
    }
    updateAnnotationSetsTypesFeatures();
  }

  protected void updateAnnotationSetsTypesFeatures() {

    try {
      annotationSetIDsFromDataStore = searcher.getIndexedAnnotationSetNames();
      allAnnotTypesAndFeaturesFromDatastore = searcher.getAnnotationTypesMap();
      updateAnnotationSetsToSearchInBox();

    } catch(SearchException se) {
      throw new GateRuntimeException(se);
    }
  }

}
