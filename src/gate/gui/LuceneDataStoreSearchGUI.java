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
 *  $id:$
 */


package gate.gui;

import gate.*;
import gate.corpora.SerialCorpusImpl;
import gate.creole.AbstractVisualResource;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.event.DatastoreEvent;
import gate.event.DatastoreListener;
import gate.gui.docview.*;
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
import java.util.Timer;
import java.util.regex.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.table.*;
import javax.swing.text.BadLocationException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.event.*;


/**
 * GUI allowing to write a query with a JAPE derived syntax for querying
 * a Lucene Datastore and display the results with a stacked view of the
 * annotations and their values.
 * <br>
 * This VR is associated to {@link gate.creole.ir.SearchPR}.
 * You have to set the target with setTarget().
 * <br>
 * Features: query auto-completion, syntactic error checker,
 * display of very big values, export of results in a file,
 * 16 different type of statistics.
 * <br>
 * TODO:
 * <ul>
 * <li>could be interesting to have statistics per document,
 * it is just adding one condition to the query that retrieves data out of
 *   the index</li>
 * <li>add shortcut to autocompletion ?</li>
 * <li>where to store the configuration for shortcut: user config or datastore ?</li>
 * <li>plus other todos in this file</li>
 * </ul>
 */
@CreoleResource(name = "Lucene Datastore Searcher", guiType = GuiType.LARGE,
    resourceDisplayed = "gate.creole.annic.SearchableDataStore",
    comment = "GUI allowing to write a query with a JAPE derived syntax for querying\n" +
        " a Lucene Datastore and display the results with a stacked view of the\n" +
        " annotations and their values.",
        helpURL = "http://gate.ac.uk/cgi-bin/userguide/sec:misc-creole:annic")
public class LuceneDataStoreSearchGUI extends AbstractVisualResource
               implements DatastoreListener {

  private static final long serialVersionUID = 3256720688877285686L;

  /**
   * The GUI is associated with the AnnicSearchPR
   */
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

  /**
   * Table that lists the patterns found by the query
   */
  private XJTable patternTable;

  /**
   * Model of the patternTable.
   */
  private PatternTableModel patternTableModel;

  /**
   * Display the annotation rows manager.
   */
  private JButton annotationRowsManagerButton;

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
  private JSlider numberOfResultsSlider;

  /**
   * No Of tokens to be shown in context window
   */
  private JSlider contextSizeSlider;

  /**
   * Gives the page number displayed in the results.
   */
  private JLabel titleResults;

  /**
   * Show the next page of results.
   */
  private JButton nextResults;

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
   * Instance of NextResultAction.
   */
  private NextResultsAction nextResultsAction;

  /**
  * Instance of ExportResultsAction.
  */
  private ExportResultsAction exportResultsAction;

  /**
   * Button for export results from the results table.
   */
  private JButton exportToHTML;
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


  private DefaultTableModel oneRowStatisticsTableModel;

  private DefaultTableModel globalStatisticsTableModel;

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
    if (userConfig.get(GateConstants.ANNIC_ANNOTATION_ROWS) != null) {
      // saved as a string: "[[true, Cat, Token, category, Crop end], [...]]"
      String annotationRowsString =
        (String)userConfig.get(GateConstants.ANNIC_ANNOTATION_ROWS);
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
            System.arraycopy(cols, 0, annotationRows[row], 0, cols.length);
            numAnnotationRows++;
          }
        }
      }
    }

    // initialize GUI
    initGui();
    updateDisplay();
    validate();
    queryTextArea.requestFocusInWindow();

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

    Comparator<String> lastWordComparator = new Comparator<String>() {
      public int compare(String o1, String o2) {
        if(o1 == null || o2 == null) {
          return 0;
        }
        return stringCollator.compare(
          o1.substring(o1.trim().lastIndexOf(' ') + 1),
          o2.substring(o2.trim().lastIndexOf(' ') + 1));
      }
    };

    integerComparator = new Comparator<Integer>() {
      public int compare(Integer o1, Integer o2) {
        if (o1 == null || o2 == null) { return 0; }
        return o1.compareTo(o2);
      }
    };

    /***************************
     * Annotation rows manager *
     ***************************/

    annotationRowsManager =
      new AnnotationRowsManagerFrame("Annotation Rows Manager");
    annotationRowsManager.setIconImage(((ImageIcon)MainFrame
      .getIcon("crystal-clear-action-window-new")).getImage());
    annotationRowsManager.setLocationRelativeTo(thisInstance);
    annotationRowsManager.getRootPane().getInputMap(
      JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
      .put(KeyStroke.getKeyStroke("ESCAPE"), "close row manager");
    annotationRowsManager.getRootPane().getActionMap()
      .put("close row manager", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        annotationRowsManager.setVisible(false);
      }
    });
    annotationRowsManager.validate();
    annotationRowsManager.setSize(200, 300);
    annotationRowsManager.pack();

    // called when Gate is exited, in case the user doesn't close the datastore
    MainFrame.getInstance().addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent e) {
        // no parent so need to be disposed explicitly
        annotationRowsManager.dispose();
      }
    });


    /*************
     * Top panel *
     *************/

    JPanel topPanel = new JPanel(new GridBagLayout());
    topPanel.setOpaque(false);
    topPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));
    GridBagConstraints gbc = new GridBagConstraints();

    // first column, three rows span
    queryTextArea = new QueryTextArea();
    queryTextArea.setToolTipText(
      "<html>Please enter a query to search in the datastore."
      +"<br>Type '{' to activate auto-completion."
      +"<br>Use [Control]+[Enter] to add a new line.</html>");
    queryTextArea.setLineWrap(true);
    gbc.gridheight = 3;
    gbc.weightx = 2;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0, 0, 0, 4);
    topPanel.add(new JScrollPane(queryTextArea), gbc);
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
    topPanel.add(corpusToSearchIn, gbc);
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
    JLabel noOfPatternsLabel = new JLabel("Results: ");
    topPanel.add(noOfPatternsLabel, gbc);
    numberOfResultsSlider = new JSlider(1, 1100, 50);
    numberOfResultsSlider.setToolTipText("50 results per page");
    numberOfResultsSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (numberOfResultsSlider.getValue()
          > (numberOfResultsSlider.getMaximum() - 100)) {
          numberOfResultsSlider.setToolTipText("Retrieve all results.");
          nextResults.setText("Retrieve all results.");
          nextResultsAction.setEnabled(false);
        } else {
          numberOfResultsSlider.setToolTipText("Retrieve "
            + numberOfResultsSlider.getValue() + " results per page.");
          nextResults.setText(
            "Next page of " + numberOfResultsSlider.getValue() + " results");
          if (searcher.getHits().length == noOfPatterns) {
            nextResultsAction.setEnabled(true);
          }
        }
        // show the tooltip each time the value change
        ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(
          numberOfResultsSlider, MouseEvent.MOUSE_MOVED, 0, 0, 0, 0, 0, false));
      }
    });
    // always show the tooltip for this component
    numberOfResultsSlider.addMouseListener(new MouseAdapter() {
      ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
      int initialDelay = toolTipManager.getInitialDelay();
      int reshowDelay = toolTipManager.getReshowDelay();
      int dismissDelay = toolTipManager.getDismissDelay();
      boolean enabled = toolTipManager.isEnabled();
      public void mouseEntered(MouseEvent e) {
        toolTipManager.setInitialDelay(0);
        toolTipManager.setReshowDelay(0);
        toolTipManager.setDismissDelay(Integer.MAX_VALUE);
        toolTipManager.setEnabled(true);
      }
      public void mouseExited(MouseEvent e) {
        toolTipManager.setInitialDelay(initialDelay);
        toolTipManager.setReshowDelay(reshowDelay);
        toolTipManager.setDismissDelay(dismissDelay);
        toolTipManager.setEnabled(enabled);
      }
    });
    gbc.insets = new Insets(5, 0, 0, 0);
    topPanel.add(numberOfResultsSlider, gbc);
    gbc.insets = new Insets(0, 0, 0, 0);
    topPanel.add(Box.createHorizontalStrut(4), gbc);
    JLabel contextWindowLabel = new JLabel("Context size: ");
    topPanel.add(contextWindowLabel, gbc);
    contextSizeSlider = new JSlider(1, 50, 5);
    contextSizeSlider.setToolTipText(
      "Display 5 tokens of context in the results.");
    contextSizeSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        contextSizeSlider.setToolTipText("Display "
          + contextSizeSlider.getValue()
          + " tokens of context in the results.");
        ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(
          contextSizeSlider, MouseEvent.MOUSE_MOVED, 0, 0, 0, 0, 0, false));
      }
    });
    // always show the tooltip for this component
    contextSizeSlider.addMouseListener(new MouseAdapter() {
      ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
      int initialDelay = toolTipManager.getInitialDelay();
      int reshowDelay = toolTipManager.getReshowDelay();
      int dismissDelay = toolTipManager.getDismissDelay();
      boolean enabled = toolTipManager.isEnabled();
      public void mouseEntered(MouseEvent e) {
        toolTipManager.setInitialDelay(0);
        toolTipManager.setReshowDelay(0);
        toolTipManager.setDismissDelay(Integer.MAX_VALUE);
        toolTipManager.setEnabled(true);
      }
      public void mouseExited(MouseEvent e) {
        toolTipManager.setInitialDelay(initialDelay);
        toolTipManager.setReshowDelay(reshowDelay);
        toolTipManager.setDismissDelay(dismissDelay);
        toolTipManager.setEnabled(enabled);
      }
    });
    gbc.insets = new Insets(5, 0, 0, 0);
    topPanel.add(contextSizeSlider, gbc);
    gbc.insets = new Insets(0, 0, 0, 0);

    // second column, third row
    gbc.gridy = 2;
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
    executeQueryAction = new ExecuteQueryAction();
    JButton executeQuery =
      new ButtonBorder(new Color(240, 240, 240), new Insets(0, 2, 0, 3), false);
    executeQuery.setAction(executeQueryAction);
    panel.add(executeQuery);
    ClearQueryAction clearQueryAction = new ClearQueryAction();
    JButton clearQueryTF =
      new ButtonBorder(new Color(240,240,240), new Insets(4,2,4,3), false);
    clearQueryTF.setAction(clearQueryAction);
    panel.add(Box.createHorizontalStrut(5));
    panel.add(clearQueryTF);
    nextResultsAction = new NextResultsAction();
    nextResultsAction.setEnabled(false);
    nextResults =
      new ButtonBorder(new Color(240,240,240), new Insets(0,0,0,3), false);
    nextResults.setAction(nextResultsAction);
    panel.add(Box.createHorizontalStrut(5));
    panel.add(nextResults);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    topPanel.add(panel, gbc);

    // will be added to the GUI via a split panel

    /****************
     * Center panel *
     ****************/

    // just initialized the components, they will be added in tableValueChanged()
    centerPanel = new JPanel();
    centerPanel.setLayout(new GridBagLayout());
    centerPanel.setOpaque(true);
    centerPanel.setBackground(Color.WHITE);

    annotationRowsManagerButton =
      new ButtonBorder(new Color(250,250,250), new Insets(0,3,0,3), true);
    annotationRowsManagerButton
      .setAction(new DisplayAnnotationRowsManagerAction());

    // will be added to the GUI via a split panel

    /*********************
     * Bottom left panel *
     *********************/

    JPanel bottomLeftPanel = new JPanel(new GridBagLayout());
    bottomLeftPanel.setOpaque(false);
    gbc = new GridBagConstraints();

    // title of the table, results options, export and next results button
    gbc.gridy = 0;
    panel = new JPanel();
    panel.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
    titleResults = new JLabel("Results");
    titleResults.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
    panel.add(titleResults);
    panel.add(Box.createHorizontalStrut(5), gbc);
    exportResultsAction = new ExportResultsAction();
    exportResultsAction.setEnabled(false);
    exportToHTML =
      new ButtonBorder(new Color(240,240,240), new Insets(0,0,0,3), false);
    exportToHTML.setAction(exportResultsAction);
    panel.add(exportToHTML, gbc);
    bottomLeftPanel.add(panel, gbc);

    // table of results
    patternTableModel = new PatternTableModel();
    patternTable = new XJTable(patternTableModel);
    patternTable.setEnableHiddingColumns(true);
    patternTable.addMouseMotionListener(new MouseMotionListener() {
      public void mouseMoved(MouseEvent me) {
        int row = patternTable.rowAtPoint(me.getPoint());
        row = patternTable.rowViewToModel(row);
        Pattern pattern;
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
              Arrays.sort(rows);
              for(int i = rows.length - 1; i >= 0; i--) {
                patterns.remove(rows[i]);
              }
              patternTable.clearSelection();
              patternTableModel.fireTableDataChanged();
              mousePopup.setVisible(false);
            }
          });

          if(patternTable.getSelectedRowCount() == 1
          && target instanceof LuceneDataStoreImpl) {
          menuItem = new JMenuItem("Open the selected document");
          menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

              // create and display the document for this result
              int row = patternTable.rowViewToModel(
                patternTable.getSelectedRow());
              final Pattern result = (Pattern)patterns.get(row);
              FeatureMap features = Factory.newFeatureMap();
              features.put(DataStore.DATASTORE_FEATURE_NAME, target);
              features.put(DataStore.LR_ID_FEATURE_NAME, result.getDocumentID());
              final Document doc;
              try {
              doc = (Document)Factory
                .createResource("gate.corpora.DocumentImpl", features);
              gate.Main.getMainFrame().select(doc);
              } catch (gate.util.GateException e) {
                e.printStackTrace();
                return;
              }

              // wait 1 second until the document is displayed and then select
              // the same expression as in the result
              Date timeToRun = new Date(System.currentTimeMillis() + 1000);
              Timer timer = new Timer();
              timer.schedule(new TimerTask() {
                public void run() {
                try {

                // find the document view associated with the document
                TextualDocumentView t = null;
                for (Resource r : Gate.getCreoleRegister().getAllInstances(
                    "gate.gui.docview.TextualDocumentView")) {
                  if (((TextualDocumentView)r).getDocument().getName()
                    .equals(doc.getName())) {
                    t = (TextualDocumentView)r;
                    break;
                  }
                }

                if (t != null && t.getOwner() != null) {
                  // display the annotation sets view
                  t.getOwner().setRightView(0);
                  try {
                    // scroll to the expression that matches the query result
                    t.getTextView().scrollRectToVisible(
                      t.getTextView().modelToView(
                      result.getRightContextEndOffset()));
                  } catch (BadLocationException e) {
                    e.printStackTrace();
                    return;
                  }
                  // select the expression that matches the query result
                  t.getTextView().select(
                    result.getLeftContextStartOffset(),
                    result.getRightContextEndOffset());
                  t.getTextView().requestFocus();
                }

                // find the annotation sets view associated with the document
                for (Resource r : Gate.getCreoleRegister().getAllInstances(
                    "gate.gui.docview.AnnotationSetsView")) {
                  AnnotationSetsView asv = (AnnotationSetsView)r;
                  if (asv.isActive()
                  && asv.getDocument().getName().equals(doc.getName())) {
                    // display the same annotation types as in Annic
                    for (int row = 0; row < numAnnotationRows; row++) {
                      if (annotationRows[row][DISPLAY].equals("false")) {
                        continue;
                      }
                      String type = annotationRows[row][ANNOTATION_TYPE];
                      if (type.equals(Constants.ANNIC_TOKEN)) {
                        // not interesting to display them
                        continue;
                      }
                      // look if there is the type displayed in Annic
                      String asn = result.getAnnotationSetName();
                      if (asn.equals(Constants.DEFAULT_ANNOTATION_SET_NAME)
                      && doc.getAnnotations().getAllTypes().contains(type)) {
                        asv.setTypeSelected(null, type, true);
                      } else if (doc.getAnnotationSetNames().contains(asn)
                      && doc.getAnnotations(asn).getAllTypes().contains(type)) {
                        asv.setTypeSelected(asn, type, true);
                      }
                    }
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
    // the graphical panel should change its output to reflect the new
    // selection. in case where multiple rows are selected
    // the annotations of the first row will be highlighted
    patternTable.getSelectionModel().addListSelectionListener(
      new javax.swing.event.ListSelectionListener() {
        public void valueChanged(javax.swing.event.ListSelectionEvent e) {
          if (!e.getValueIsAdjusting()) { updateCentralView(); }
          switch (patternTable.getSelectedRows().length) {
            case 0:
              exportToHTML.setText("Export all...");
              exportToHTML.setToolTipText("Exports all the rows in the table.");
              break;
            case 1:
              exportToHTML.setText("Export selection...");
              exportToHTML.setToolTipText("Exports selected row in the table.");
              break;
            default:
              exportToHTML.setText("Export selection...");
              exportToHTML.setToolTipText(
                "Exports selected rows in the table.");
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
      PatternTableModel.LEFT_CONTEXT_COLUMN, lastWordComparator);
    patternTable.setComparator(
      PatternTableModel.PATTERN_COLUMN, stringCollator);
    patternTable.setComparator(
      PatternTableModel.RIGHT_CONTEXT_COLUMN, stringCollator);
    // right-alignment of the column
    patternTable.getColumnModel()
      .getColumn(PatternTableModel.LEFT_CONTEXT_COLUMN)
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
      .getColumn(PatternTableModel.PATTERN_COLUMN)
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
    globalStatisticsTableModel = new DefaultTableModel(
      new Object[]{"Annotation Type", "Count"},0);
    globalStatisticsTable.setModel(globalStatisticsTableModel);
    globalStatisticsTable.setComparator(0, stringCollator);
    globalStatisticsTable.setComparator(1, integerComparator);
    globalStatisticsTable.setSortedColumn(1);
    globalStatisticsTable.setAscending(false);

    statisticsTabbedPane.addTab("Global", null,
      new JScrollPane(globalStatisticsTable),
      "Global statistics on the Corpus and Annotation Set selected.");

    statisticsTabbedPane.addMouseListener(new MouseAdapter() {
      private JPopupMenu mousePopup;
      JMenuItem menuItem;
      public void mousePressed(MouseEvent e) {
        int tabIndex = statisticsTabbedPane.indexAtLocation(e.getX(), e.getY());
        if (e.isPopupTrigger()
        && tabIndex > 0) {
          createPopup(tabIndex);
          mousePopup.show(e.getComponent(), e.getX(), e.getY());
        }
      }
      public void mouseReleased(MouseEvent e) {
        int tabIndex = statisticsTabbedPane.indexAtLocation(e.getX(), e.getY());
        if (e.isPopupTrigger()
        && tabIndex > 0) {
          createPopup(tabIndex);
          mousePopup.show(e.getComponent(), e.getX(), e.getY());
        }
      }
      private void createPopup(final int tabIndex) {
        mousePopup = new JPopupMenu();
        if (tabIndex == 1) {
          menuItem = new JMenuItem("Clear table");
          menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ie) {
              oneRowStatisticsTableModel.setRowCount(0);
            }
          });
        } else {
          menuItem = new JMenuItem("Close tab");
          menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ie) {
              statisticsTabbedPane.remove(tabIndex);
            }
          });
        }
        mousePopup.add(menuItem);
      }
    });

    class RemoveCellEditorRenderer extends AbstractCellEditor
      implements TableCellRenderer, TableCellEditor, ActionListener {
      private static final long serialVersionUID = 1L;
      private int row;
      private JButton button;
      public RemoveCellEditorRenderer() {
        button = new JButton();
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setIcon(MainFrame.getIcon("crystal-clear-action-button-cancel"));
        button.setToolTipText("Remove this line.");
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.addActionListener(this);
      }
      public Component getTableCellRendererComponent(
        JTable table, Object color, boolean isSelected,
        boolean hasFocus, int row, int col) {
        button.setSelected(isSelected);
        return button;
      }
      public boolean shouldSelectCell(EventObject anEvent) {
        return false;
      }
      public void actionPerformed(ActionEvent e) {
        oneRowStatisticsTableModel.removeRow(
          oneRowStatisticsTable.rowViewToModel(row));
      }
      public Object getCellEditorValue() {
        return null;
      }
      public Component getTableCellEditorComponent(JTable table,
              Object value, boolean isSelected, int row, int col) {
        this.row = row;
        return button;
      }
    }

    oneRowStatisticsTable = new XJTable() {
      private static final long serialVersionUID = 1L;
      public boolean isCellEditable(int rowIndex, int vColIndex) {
        return vColIndex == 2;
      }
      public Component prepareRenderer(TableCellRenderer renderer,
              int row, int col) {
        Component c = super.prepareRenderer(renderer, row, col);
        if (c instanceof JComponent && col != 2) {
          // display a custom tooltip saved when adding statistics
          ((JComponent)c).setToolTipText("<html>"
            +oneRowStatisticsTableToolTips.get(rowViewToModel(row))+"</html>");
        }
        return c;
      }
    };

    oneRowStatisticsTableModel = new DefaultTableModel(
      new Object[]{"Annotation Type/Feature","Count",""}, 0);
    oneRowStatisticsTable.setModel(oneRowStatisticsTableModel);
    oneRowStatisticsTable.getColumnModel().getColumn(2).setMaxWidth(
      MainFrame.getIcon("crystal-clear-action-edit-remove").getIconWidth()+6);
    oneRowStatisticsTable.getColumnModel().getColumn(2)
      .setCellEditor(new RemoveCellEditorRenderer());
    oneRowStatisticsTable.getColumnModel().getColumn(2)
      .setCellRenderer(new RemoveCellEditorRenderer());
    oneRowStatisticsTable.setComparator(0, stringCollator);
    oneRowStatisticsTable.setComparator(1, integerComparator);

    statisticsTabbedPane.addTab("One item", null,
      new JScrollPane(oneRowStatisticsTable),
      "<html>One item statistics.<br>"+
      "Right-click on an annotation<br>"+
      "to add statistics here.");
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
    Dimension minimumSize = new Dimension(0, 0);
    bottomLeftPanel.setMinimumSize(minimumSize);
    statisticsTabbedPane.setMinimumSize(minimumSize);
    bottomSplitPane.add(bottomLeftPanel);
    bottomSplitPane.add(statisticsTabbedPane);
    bottomSplitPane.setOneTouchExpandable(true);
    bottomSplitPane.setResizeWeight(0.75);
    bottomSplitPane.setContinuousLayout(true);

    JSplitPane centerBottomSplitPane =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    centerBottomSplitPane.add(new JScrollPane(centerPanel,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    centerBottomSplitPane.add(bottomSplitPane);
    centerBottomSplitPane.setResizeWeight(0.5);
    centerBottomSplitPane.setContinuousLayout(true);

    JSplitPane topBottomSplitPane =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    topBottomSplitPane.add(topPanel);
    topBottomSplitPane.add(centerBottomSplitPane);
    topBottomSplitPane.setContinuousLayout(true);

    add(topBottomSplitPane, BorderLayout.CENTER);

  }

  /**
   * Update the result table and center view according to the result of
   * the search contained in <code>searcher</code>.
   */
  protected void updateDisplay() {

    if (searcher != null) {
      Collections.addAll(patterns, searcher.getHits());
      // update the table of results
      patternTableModel.fireTableDataChanged();
    }

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
      exportResultsAction.setEnabled(true);
      if (numberOfResultsSlider.getValue()
      <= (numberOfResultsSlider.getMaximum() - 100)) {
        nextResultsAction.setEnabled(true);
      }
      if (searcher.getHits().length < noOfPatterns) {
        nextResultsAction.setEnabled(false);
      }
      patternTable.setRowSelectionInterval(0, 0);
      patternTable.scrollRectToVisible(patternTable.getCellRect(0, 0, true));

    } else if (queryTextArea.getText().trim().length() < 1) {
      centerPanel.removeAll();
      centerPanel.add(new JTextArea(
        "First have a look at the annotation statistics table at the bottom right.\n"+
        "Use the most frequent annotations for your query.\n\n"+
        "Then enter a query in the text area at the top-left\n"+
        "and hit Enter. For example: {Person} to retrieve Person annotations."
        ),
        new GridBagConstraints());
      centerPanel.updateUI();
      nextResultsAction.setEnabled(false);
      exportResultsAction.setEnabled(false);

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
  protected void updateCentralView() {

    // maximum number of columns to display in the match,
    // i.e. maximum number of characters
    int maxColumns = 150;
    // maximum length of a feature value displayed
    int maxValueLength = 30;

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;

    if (patternTable.getSelectedRow() == -1) {
      // no pattern is selected in the pattern table
      centerPanel.removeAll();
      if (patternTable.getRowCount() > 0) {
        centerPanel.add(new JLabel(
          "Please select a row in the table of results."), gbc);
      } else {
        if (numberOfResultsSlider.getValue()
          > (numberOfResultsSlider.getMaximum() - 100)) {
          centerPanel.add(new JLabel("Retrieving all results..."), gbc);
        } else {
          centerPanel.add(new JLabel("Retrieving " +
            numberOfResultsSlider.getValue() + " results..."), gbc);
        }
      }
      centerPanel.validate();
      centerPanel.repaint();
      return;
    }

    // clear the central view
    centerPanel.removeAll();

    Pattern pattern = (Pattern) patterns.get(patternTable.rowViewToModel(
      patternTable.getSelectionModel().getLeadSelectionIndex()));

    /*********************************************************
     * Display on the first line the text matching the query *
     *********************************************************/
    
    gbc.gridwidth = 1;
    gbc.insets = new java.awt.Insets(10, 10, 10, 10);
    JLabel labelTitle = new JLabel("Match+Context");
    labelTitle.setOpaque(true);
    labelTitle.setBackground(Color.WHITE);
    labelTitle.setBorder(new CompoundBorder(
      new EtchedBorder(EtchedBorder.LOWERED,
        new Color(250, 250, 250), new Color(250, 250, 250).darker()),
      new EmptyBorder(new Insets(0, 2, 0, 2))));
    labelTitle.setToolTipText("Text matched by last query and its context.");
    centerPanel.add(labelTitle, gbc);
    gbc.insets = new java.awt.Insets(10, 0, 10, 0);

    int startOffsetPattern =
      pattern.getStartOffset() - pattern.getLeftContextStartOffset();
    int endOffsetPattern =
      pattern.getEndOffset() - pattern.getLeftContextStartOffset();
    boolean textTooLong = (endOffsetPattern - startOffsetPattern) > maxColumns;
    int upperBound = pattern.getPatternText().length() - (maxColumns/2);

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

      /**************************************************
       * Display on the next lines each annotation type *
       **************************************************/

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
        "<html>Annotations <b>"+type+"</b>"
        +((feature.equals(""))?"":" and only feature <b>"+feature+"</b>")
        +".<br><em>Right-click for statistics</em>.</html>");
      annotationTypeAndFeature.setOpaque(true);
      annotationTypeAndFeature.setBackground(Color.WHITE);
      annotationTypeAndFeature.setBorder(new CompoundBorder(
        new EtchedBorder(EtchedBorder.LOWERED,
          new Color(250, 250, 250), new Color(250, 250, 250).darker()),
        new EmptyBorder(new Insets(0, 2, 0, 2))));
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

      // add a JLabel in the gridbag layout for each feature value
      // of the current annotation type
      HashMap<Integer,TreeSet<Integer>> gridSet =
        new HashMap<Integer,TreeSet<Integer>>();
      int gridyMax = gbc.gridy;
      for(PatternAnnotation ann : annots) {
        gbc.gridx = ann.getStartOffset()
          - pattern.getLeftContextStartOffset() + 1;
        gbc.gridwidth = ann.getEndOffset()
          - pattern.getLeftContextStartOffset() - gbc.gridx + 1;
        if(textTooLong) {
          if(gbc.gridx > (upperBound + 1)) {
            // x starts after the hidden middle part
            gbc.gridx -= upperBound - (maxColumns / 2) + 1;
          }
          else if(gbc.gridx > (maxColumns / 2)) {
            // x starts in the hidden middle part
            if(gbc.gridx + gbc.gridwidth <= (upperBound + 3)) {
              // x ends in the hidden middle part
              continue; // skip the middle part of the text
            }
            else {
              // x ends after the hidden middle part
              gbc.gridwidth -= upperBound - gbc.gridx + 2;
              gbc.gridx = (maxColumns / 2) + 2;
            }
          }
          else {
            // x starts before the hidden middle part
            if(gbc.gridx + gbc.gridwidth < (maxColumns / 2)) {
              // x ends before the hidden middle part
              // do nothing
            }
            else if(gbc.gridx + gbc.gridwidth < upperBound) {
              // x ends in the hidden middle part
              gbc.gridwidth = (maxColumns / 2) - gbc.gridx + 1;
            }
            else {
              // x ends after the hidden middle part
              gbc.gridwidth -= upperBound - (maxColumns / 2);
            }
          }
        }
        if(gbc.gridwidth == 0) {
          gbc.gridwidth = 1;
        }

        JLabel label = new JLabel();
        String value = (feature.equals("")) ?
          " " : ann.getFeatures().get(feature);
        if(value.length() > maxValueLength) {
          // show the full text in the tooltip
          label.setToolTipText((value.length() > 500) ?
            "<html><textarea rows=\"30\" cols=\"40\" readonly=\"readonly\">"
            + value.replaceAll("(.{50,60})\\b", "$1\n") + "</textarea></html>" :
            ((value.length() > 100) ?
              "<html><table width=\"500\" border=\"0\" cellspacing=\"0\">"
                + "<tr><td>" + value.replaceAll("\n", "<br>")
                + "</td></tr></table></html>" :
              value));
          if(annotationRows[row][CROP].equals("Crop start")) {
            value = "..." + value.substring(value.length() - maxValueLength - 1);
          }
          else if(annotationRows[row][CROP].equals("Crop end")) {
            value = value.substring(0, maxValueLength - 2) + "...";
          }
          else { // cut in the middle
            value = value.substring(0, maxValueLength / 2) + "..."
              + value.substring(value.length() - (maxValueLength / 2));
          }
        }
        label.setText(value);
        label.setBackground(getAnnotationTypeColor(ann.getType()));
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        label.setOpaque(true);
        if(feature.equals("")) {
          label.addMouseListener(new AnnotationRowValueMouseListener(type));
          // show the feature values in the tooltip
          String width = (ann.getFeatures().toString().length() > 100) ?
            "500" : "100%";
          String toolTip = "<html><table width=\"" + width
            + "\" border=\"0\" cellspacing=\"1\">";
          for(Map.Entry<String, String> map : ann.getFeatures().entrySet()) {
            toolTip += "<tr align=\"left\"><td bgcolor=\"silver\">"
              + map.getKey() + "</td><td>"
              + ((map.getValue().length() > 500) ?
              "<textarea rows=\"20\" cols=\"40\" readonly=\"readonly\">"
                + map.getValue().replaceAll("(.{50,60})\\b", "$1\n")
                + "</textarea>" :
              map.getValue().replaceAll("\n", "<br>"))
              + "</td></tr>";
          }
          label.setToolTipText(toolTip + "</table></html>");

        } else {
          label.addMouseListener(new AnnotationRowValueMouseListener(
            type, feature, ann.getFeatures().get(feature)));
          String toolTip = label.getToolTipText() == null ?
            "" : label.getToolTipText();
          toolTip = toolTip.replaceAll("</?html>", "");
          toolTip = "<html>" + (toolTip.length() == 0 ? "" : toolTip + "<br>")
            + "<em>Right-click for statistics.</em></html>";
          label.setToolTipText(toolTip);
        }
        // find the first empty row span for this annotation
        int oldGridy = gbc.gridy;
        for(int y = oldGridy; y <= (gridyMax + 1); y++) {
          // for each cell of this row where spans the annotation
          boolean xSpanIsEmpty = true;
          for(int x = gbc.gridx;
              (x < (gbc.gridx + gbc.gridwidth)) && xSpanIsEmpty; x++) {
            xSpanIsEmpty = !(gridSet.containsKey(x)
              && gridSet.get(x).contains(y));
          }
          if(xSpanIsEmpty) {
            gbc.gridy = y;
            break;
          }
        }
        // save the column x and row y of the current value
        TreeSet<Integer> ts;
        for(int x = gbc.gridx; x < (gbc.gridx + gbc.gridwidth); x++) {
          ts = gridSet.get(x);
          if(ts == null) {
            ts = new TreeSet<Integer>();
          }
          ts.add(gbc.gridy);
          gridSet.put(x, ts);
        }
        centerPanel.add(label, gbc);
        gridyMax = Math.max(gridyMax, gbc.gridy);
        gbc.gridy = oldGridy;
      }

      // add a remove button at the end of the row
      JButton removePattern =
        new ButtonBorder(new Color(250,250,250), new Insets(0,3,0,3), true);
      removePattern.setIcon(
        MainFrame.getIcon("crystal-clear-action-edit-remove"));
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
      gbc.gridwidth = 1;
      // last cell of the row
      gbc.gridx = Math.min(pattern.getPatternText().length(), maxColumns) + 1;
      gbc.insets = new Insets(0, 10, 3, 0);
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.WEST;
      centerPanel.add(removePattern, gbc);
      gbc.insets = new Insets(0, 0, 3, 0);
      gbc.fill = GridBagConstraints.BOTH;
      gbc.anchor = GridBagConstraints.CENTER;

      // set the new gridy to the maximum row we put a value
      gbc.gridy = gridyMax;
    }

    // add an annotation rows manager button on the last row
    gbc.insets = new java.awt.Insets(0, 10, 10, 0);
    gbc.gridx = Math.min(pattern.getPatternText().length(), maxColumns) + 1;
    gbc.gridy++;
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
      TreeSet<String> ts = new TreeSet<String>(stringCollator);
      ts.addAll(populatedAnnotationTypesAndFeatures.keySet());
      globalStatisticsTableModel.setRowCount(0);
      for (String annotationType : ts) {
        // retrieves the number of occurrences for each Annotation Type
        // of the choosen Annotation Set
        count = searcher.freq(corpusName, annotationSetName, annotationType);
        globalStatisticsTableModel.addRow(new Object[]{annotationType, count});
        countTotal += count;
      }
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
   * This method uses the java.util.prefs.Preferences and get the color
   * for particular annotationType.. This color could have been saved by
   * the AnnotationSetsView
   * 
   * @param annotationType name of the annotation type
   * @return the color saved in the GATE preferences
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
    int rgba = (annotationType != null)?prefRoot.getInt(annotationType, -1):-1;
    Color colour;
    if(rgba == -1) {
      // initialise and save
      gate.swing.ColorGenerator colorGenerator = new gate.swing.ColorGenerator();
      float components[] = colorGenerator.getNextColor().getComponents(null);
      colour = new Color(components[0], components[1], components[2], 0.5f);
      int rgb = colour.getRGB();
      int alpha = colour.getAlpha();
      rgba = rgb | (alpha << 24);
      if (annotationType != null) {
        prefRoot.putInt(annotationType, rgba);
      }
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
   * @see #DISPLAY DISPLAY column parameter
   * @see #SHORTCUT SHORTCUT column parameter
   * @see #ANNOTATION_TYPE ANNOTATION_TYPE column parameter
   * @see #FEATURE FEATURE column parameter
   * @see #CROP CROP column parameter
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
           .equals(parameters[num+1])) {
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
      System.arraycopy(annotationRows[row2 + 1], 0, annotationRows[row2], 0,
        columnNames.length);
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
    userConfig.put(GateConstants.ANNIC_ANNOTATION_ROWS,
            annotationRowsString);
  }
  
  /**
   * Exports results and statistics to a HTML File.
   */
  protected class ExportResultsAction extends AbstractAction {

    private static final long serialVersionUID = 3257286928859412277L;

    public ExportResultsAction() {
      super("Export...",
        MainFrame.getIcon("crystal-clear-app-download-manager"));
      super.putValue(SHORT_DESCRIPTION,
        "Export results and statistics to a HTML file.");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_E);
    }

    public void actionPerformed(ActionEvent ae) {

      Map<String, Object> parameters = searcher.getParameters();

      // no results
      if(patterns == null || patterns.isEmpty()) {
          JOptionPane.showMessageDialog(thisInstance,
                  "No patterns found to export");
          return;
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

        if(patternTable.getSelectedRows().length > 0) {
          // export selected patterns
          for(int row : patternTable.getSelectedRows()) {
            int num = patternTable.rowViewToModel(row);
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
        bw.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        bw.newLine();
        bw.write("</HEAD><BODY>");
        bw.newLine();

        bw.write("<H1 align=\"center\">Annic Results and Statistics</H1>");
        bw.newLine();

        bw.write("<H2>Parameters</H2>");
        bw.newLine();

        bw.write("<UL><LI>Corpus: <B>"+corpusToSearchIn.getSelectedItem()+"</B></LI>");
        bw.newLine();
        bw.write("<LI>Annotation set: <B>"+annotationSetsToSearchIn.getSelectedItem()+"</B></LI>");
        bw.newLine();
        bw.write("<LI>Query Issued: <B>"+searcher.getQuery()+"</B>");
        bw.write("<LI>Context Window: <B>"+parameters.get(Constants.CONTEXT_WINDOW)
                +"</B></LI>");
        bw.newLine();
        bw.write("<LI>Queries:<UL>");
        Collections.sort(patternsToExport, new Comparator<Hit>() {
          public int compare(Hit a, Hit b) {
            Pattern p1 = (Pattern)a;
            Pattern p2 = (Pattern)b;
            return p1.getQueryString().compareTo(p2.getQueryString());
          }
        });
        String queryString = "";
        for(Hit patternToExport : patternsToExport) {
          if(!patternToExport.getQueryString().equals(queryString)) {
            bw.write("<LI><a href=\"#" + patternToExport.getQueryString() + "\">"
              + patternToExport.getQueryString() + "</a></LI>");
            queryString = patternToExport.getQueryString();
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
            queryString = ap.getQueryString();

            bw.newLine();
            if(i != 0) {
              bw.write("</TABLE>");
            }
            bw.write("<P><a name=\"" + ap.getQueryString()
                    + "\">Query Pattern:</a> <B>" + ap.getQueryString()
                    + "</B></P>");
            bw.newLine();
            bw.write("<TABLE border=\"1\"><TR>");
            bw.write("<TH>No.</TH>");
            bw.write("<TH>Document ID</TH>");
            bw.write("<TH>Annotation Set</TH>");
            bw.write("<TH>Left Context</TH>");
            bw.write("<TH>Pattern</TH>");
            bw.write("<TH>Right Context</TH>");
            bw.write("</TR>");
            bw.newLine();
          }

          bw.write("<TR><TD>" + (i + 1) + "</TD>");
          bw.write("<TD>" + ap.getDocumentID() + "</TD>");
          bw.write("<TD>" + ap.getAnnotationSetName() + "</TD>");
          bw.write("<TD align=\"right\">"
                  + ap.getPatternText(ap.getLeftContextStartOffset(),
                    ap.getStartOffset()).replaceAll("&", "&amp;")
                    .replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                    .replaceAll("\"", "&quot;")
                  +"</TD>");
          bw.write("<TD align=\"center\">"
                  + ap.getPatternText(ap.getStartOffset(), ap.getEndOffset())
                  .replaceAll("&", "&amp;").replaceAll("<", "&lt;")
                  .replaceAll(">", "&gt;").replaceAll("\"", "&quot;")
                  + "</TD>");
          bw.write("<TD align=\"left\">"
                  + ap.getPatternText(ap.getEndOffset(),
                    ap.getRightContextEndOffset()).replaceAll("&", "&amp;")
                    .replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                    .replaceAll("\"", "&quot;")
                  + "</TD>");
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

        bw.write("<H2>One item Statistics</H2>");
        bw.newLine();

        bw.write("<TABLE border=\"1\">");
        bw.newLine();
        bw.write("<TR>");
        for (int col = 0; col < (oneRowStatisticsTable.getColumnCount()-1); col++) {
          bw.write("<TH>"+oneRowStatisticsTable.getColumnName(col)+"</TH>");
          bw.newLine();
        }
        bw.write("</TR>");
        bw.newLine();
        for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
          bw.write("<TR>");
          for (int col = 0; col < (oneRowStatisticsTable.getColumnCount()-1); col++) {
            bw.write("<TD>"+oneRowStatisticsTable.getValueAt(row, col)+"</TD>");
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
      super("Clear", MainFrame.getIcon("crystal-clear-action-button-cancel"));
      super.putValue(SHORT_DESCRIPTION, "<html>Clear the query text box."
      +"&nbsp;&nbsp;<font color=#667799><small>Alt+Backspace"
      +"&nbsp;&nbsp;</small></font></html>");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_BACK_SPACE);
    }

    public void actionPerformed(ActionEvent ae) {
      queryTextArea.setText("");
      queryTextArea.requestFocusInWindow();
    }
  }

  /** 
   * Finds out the newly created query and execute it.
   */
  protected class ExecuteQueryAction extends AbstractAction {

    private static final long serialVersionUID = 3258128055204917812L;

    public ExecuteQueryAction() {
      super("Search", MainFrame.getIcon("crystal-clear-app-xmag"));
      super.putValue(SHORT_DESCRIPTION, "<html>Execute the query."
      +"&nbsp;&nbsp;<font color=#667799><small>Enter"
      +"&nbsp;&nbsp;</small></font></html>");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_ENTER);
    }

    public void actionPerformed(ActionEvent ae) {

      // clear the result table and center view
      if (patterns.size() > 0) {
        patterns.clear();
        patternTableModel.fireTableDataChanged();
      } else {
        updateCentralView();
      }

      // set the search parameters
      Map<String, Object> parameters = searcher.getParameters();
      if(parameters == null)
        parameters = new HashMap<String, Object>();

      if(target instanceof LuceneDataStoreImpl) {
        String corpus2SearchIn = corpusToSearchIn.getSelectedItem()
          .equals(Constants.ENTIRE_DATASTORE) ?
          null : (String)corpusIds.get(corpusToSearchIn.getSelectedIndex() - 1);
        parameters.put(Constants.CORPUS_ID, corpus2SearchIn);
      }

      noOfPatterns = (numberOfResultsSlider.getValue()
        > (numberOfResultsSlider.getMaximum() - 100))?
        -1:((Number)numberOfResultsSlider.getValue()).intValue();
      int contextWindow = ((Number)contextSizeSlider.getValue()).intValue();
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

      parameters.put(Constants.CONTEXT_WINDOW, contextWindow);
      if (annotationSetsToSearchIn.getSelectedItem()
              .equals(Constants.ALL_SETS)) {
        parameters.remove(Constants.ANNOTATION_SET_ID);
      } else {
        String annotationSet =
          (String)annotationSetsToSearchIn.getSelectedItem();
        parameters.put(Constants.ANNOTATION_SET_ID, annotationSet);
      }

      final String queryF = query;
      final Map<String, Object> parametersF = parameters;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          thisInstance.setEnabled(false);

          try {
            if(searcher.search(queryF, parametersF)) {
              searcher.next(noOfPatterns);
            }

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

          } finally {
            thisInstance.setEnabled(true);
          }

          updateDisplay();
          pageOfResults = 1;
          titleResults.setText(
            "Page "+pageOfResults+" ("+searcher.getHits().length+" results)");
          queryTextArea.requestFocusInWindow();
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
      super("Next page of " + numberOfResultsSlider.getValue() + " results",
        MainFrame.getIcon("crystal-clear-action-loopnone"));
      super.putValue(SHORT_DESCRIPTION, "<html>Show next page of results."
      +"&nbsp;&nbsp;<font color=#667799><small>Alt+Right"
      +"&nbsp;&nbsp;</small></font></html>");
      super.putValue(MNEMONIC_KEY, KeyEvent.VK_RIGHT);
    }

    public void actionPerformed(ActionEvent ae) {

      // clear the results table and center view
      if (patterns.size() > 0) {
        patterns.clear();
        patternTableModel.fireTableDataChanged();
      } else {
        updateCentralView();
      }

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          thisInstance.setEnabled(false);
          noOfPatterns = ((Number)numberOfResultsSlider.getValue()).intValue();
          try {
            searcher.next(noOfPatterns);

          } catch(Exception e) {
            e.printStackTrace();

          } finally {
            thisInstance.setEnabled(true);
          }

          updateDisplay();
          pageOfResults++;
          titleResults.setText(
            "Page "+pageOfResults+" ("+searcher.getHits().length+" results)");
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
      super("", MainFrame.getIcon("crystal-clear-action-edit-add"));
      super.putValue(SHORT_DESCRIPTION,
        "<html>Display the annotation rows manager."
      +"&nbsp;&nbsp;<font color=#667799><small>Alt+Left"
      +"&nbsp;&nbsp;</small></font></html>");
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

    public void mouseClicked(MouseEvent me) {
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
    private String description;
    private String toolTip;
    private String descriptionTemplate;
    private String toolTipTemplate;

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

    ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
    int dismissDelay = toolTipManager.getDismissDelay();
    int initialDelay = toolTipManager.getInitialDelay();
    int reshowDelay = toolTipManager.getReshowDelay();
    boolean enabled = toolTipManager.isEnabled();

    public AnnotationRowValueMouseListener(
            String type, String feature, String text) {
      this.type = type;
      this.feature = feature;
      this.text = text;
      String value;
      if (text.replace("\\s", "").length() > 20) {
        value = text.replace("\\s", "").substring(0, 20)+("...");
      } else {
        value = text.replace("\\s", "");
      }
      this.descriptionTemplate =
        type + "." + feature + "==\"" + value + "\" (kind)";
      this.toolTipTemplate = "Statistics in kind"
        +"<br>on Corpus: "+corpusName
        +"<br>and Annotation Set: "+annotationSetName
        +"<br>for the query: "+patterns.get(0).getQueryString();
    }

    public AnnotationRowValueMouseListener(String type) {
      this.type = type;
    }

    public void mouseEntered(MouseEvent e) {
      // make the tooltip indefinitely shown when the mouse is over
      toolTipManager.setDismissDelay(Integer.MAX_VALUE);
      toolTipManager.setInitialDelay(0);
      toolTipManager.setReshowDelay(0);
      toolTipManager.setEnabled(true);
    }

    public void mouseExited(MouseEvent e) {
      toolTipManager.setDismissDelay(dismissDelay);
      toolTipManager.setInitialDelay(initialDelay);
      toolTipManager.setReshowDelay(reshowDelay);
      toolTipManager.setEnabled(enabled);
    }

    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger() && type != null && feature != null) {
        createPopup(e);
        mousePopup.show(e.getComponent(), e.getX(), e.getY());
      } else {
        updateQuery();
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger() && type != null && feature != null) {
        createPopup(e);
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

    private int checkStatistics() {
      boolean found = false;
      int numRow = 0;
      // check if this statistics doesn't already exist in the table
      for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
        String oldDescription = (String)
          oneRowStatisticsTable.getValueAt(row, 0);
        String oldToolTip = oneRowStatisticsTableToolTips
          .get(oneRowStatisticsTable.rowViewToModel(numRow));
        if (oldDescription.equals(description)
         && oldToolTip.equals(toolTip)) {
          found = true;
          break;
        }
        numRow++;
      }
      return found ? numRow : -1;
    }

    private void addStatistics(String kind, int count, int numRow,
                               final MouseEvent e) {
      JLabel label = (JLabel) e.getComponent();
      if (!label.getToolTipText().contains(kind)) {
        // add the statistics to the tooltip
        String toolTip = label.getToolTipText();
        toolTip = toolTip.replaceAll("</?html>", "");
        toolTip = kind + " = " + count + "<br>" + toolTip;
        toolTip = "<html>" +  toolTip + "</html>";
        label.setToolTipText(toolTip);
      }
      if (bottomSplitPane.getDividerLocation()
        / bottomSplitPane.getSize().getWidth() < 0.90) {
        // select the row in the statistics table
        statisticsTabbedPane.setSelectedIndex(1);
        oneRowStatisticsTable.setRowSelectionInterval(numRow, numRow);
        oneRowStatisticsTable.scrollRectToVisible(
          oneRowStatisticsTable.getCellRect(numRow, 0, true));
      } else {
        // display a tooltip
        JToolTip tip = label.createToolTip();
        tip.setTipText(kind + " = " + count);
        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        final Popup tipWindow = popupFactory.getPopup(label, tip,
          e.getX()+e.getComponent().getLocationOnScreen().x,
          e.getY()+e.getComponent().getLocationOnScreen().y);
        tipWindow.show();
        Date timeToRun = new Date(System.currentTimeMillis() + 2000);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
          public void run() {
            // hide the tooltip after 2 seconds
            tipWindow.hide();
          }
        }, timeToRun);
      }
    }

    private void createPopup(final MouseEvent e) {
        mousePopup = new JPopupMenu();

        menuItem = new JMenuItem("Occurrences in datastore");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "datastore");
            toolTip = toolTipTemplate.replaceFirst("kind", "datastore");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(corpusID, annotationSetID,
                                      type, feature, text);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("datastore", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "matches");
            toolTip = toolTipTemplate.replaceFirst("kind", "matches");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                  count = searcher.freq(patterns, type,
                    feature, text, true, false);
                } catch(SearchException se) {
                  se.printStackTrace();
                  return;
                }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("matches", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "contexts");
            toolTip = toolTipTemplate.replaceFirst("kind", "contexts");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(patterns, type,
                        feature, text, false, true);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("contexts", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches+contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "mch+ctxt");
            toolTip = toolTipTemplate.replaceFirst("kind", "matches+contexts");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(patterns, type,
                        feature, text, true, true);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("matches+contexts", count, numRow, e);
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
    private String description;
    private String toolTip;
    private String descriptionTemplate;
    private String toolTipTemplate;

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
      this.descriptionTemplate = type + "." + feature + " (kind)";
      this.toolTipTemplate = "Statistics in kind"
        +"<br>on Corpus: "+corpusName
        +"<br>and Annotation Set: "+annotationSetName
        +"<br>for the query: "+patterns.get(0).getQueryString();
    }

    public AnnotationRowHeaderMouseListener(String type) {
      this.type = type;
      this.descriptionTemplate = type + " (kind)";
      this.toolTipTemplate = "Statistics in kind"
        +"<br>on Corpus: "+corpusName
        +"<br>and Annotation Set: "+annotationSetName
        +"<br>for the query: "+patterns.get(0).getQueryString();
    }

    public void mouseClicked(MouseEvent e) {
      annotationRowsManagerButton.doClick();
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

    private int checkStatistics() {
      boolean found = false;
      int numRow = 0;
      // check if this statistics doesn't already exist in the table
      for (int row = 0; row < oneRowStatisticsTable.getRowCount(); row++) {
        String oldDescription = (String)
          oneRowStatisticsTable.getValueAt(row, 0);
        String oldToolTip = oneRowStatisticsTableToolTips
          .get(oneRowStatisticsTable.rowViewToModel(numRow));
        if (oldDescription.equals(description)
         && oldToolTip.equals(toolTip)) {
          found = true;
          break;
        }
        numRow++;
      }
      return found ? numRow : -1;
    }

    private void addStatistics(String kind, int count, int numRow,
                               final MouseEvent e) {
      JLabel label = (JLabel) e.getComponent();
      if (!label.getToolTipText().contains(kind)) {
        // add the statistics to the tooltip
        String toolTip = label.getToolTipText();
        toolTip = toolTip.replaceAll("</?html>", "");
        toolTip = kind + " = " + count + "<br>" + toolTip;
        toolTip = "<html>" +  toolTip + "</html>";
        label.setToolTipText(toolTip);
      }
      if (bottomSplitPane.getDividerLocation()
        / bottomSplitPane.getSize().getWidth() < 0.90) {
        // select the row in the statistics table
        statisticsTabbedPane.setSelectedIndex(1);
        oneRowStatisticsTable.setRowSelectionInterval(numRow, numRow);
        oneRowStatisticsTable.scrollRectToVisible(
          oneRowStatisticsTable.getCellRect(numRow, 0, true));
      } else {
        // display a tooltip
        JToolTip tip = label.createToolTip();
        tip.setTipText(kind + " = " + count);
        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        final Popup tipWindow = popupFactory.getPopup(label, tip,
          e.getX()+e.getComponent().getLocationOnScreen().x,
          e.getY()+e.getComponent().getLocationOnScreen().y);
        tipWindow.show();
        Date timeToRun = new Date(System.currentTimeMillis() + 2000);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
          public void run() {
            // hide the tooltip after 2 seconds
            tipWindow.hide();
          }
        }, timeToRun);
      }
    }

    private void createPopup(final MouseEvent e) {
      mousePopup = new JPopupMenu();

      if (type != null && feature != null) {

        // count values for one Feature of an Annotation type

        menuItem = new JMenuItem("Occurrences in datastore");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "datastore");
            toolTip = toolTipTemplate.replaceFirst("kind", "datastore");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(corpusID, annotationSetID, type, feature);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("datastore", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "matches");
            toolTip = toolTipTemplate.replaceFirst("kind", "matches");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(patterns, type,
                  feature, null, true, false);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("matches", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "contexts");
            toolTip = toolTipTemplate.replaceFirst("kind", "contexts");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(patterns, type,
                        feature, null, false, true);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("contexts", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches+contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "mch+ctxt");
            toolTip = toolTipTemplate.replaceFirst("kind", "matches+contexts");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(patterns, type,
                        feature, null, true, true);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("matches+contexts", count, numRow, e);
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
            statisticsTabbedPane.addTab(
              String.valueOf(statisticsTabbedPane.getTabCount()-1),
              null, new JScrollPane(table),
              "<html>Statistics in matches"
              +"<br>on Corpus: "+corpusName
              +"<br>and Annotation Set: "+annotationSetName
              +"<br>for the query: "+patterns.get(0).getQueryString()
              +"</html>");
            if (bottomSplitPane.getDividerLocation()
              / bottomSplitPane.getSize().getWidth() > 0.75) {
               bottomSplitPane.setDividerLocation(0.66);
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
            if (bottomSplitPane.getDividerLocation()
              / bottomSplitPane.getSize().getWidth() > 0.75) {
               bottomSplitPane.setDividerLocation(0.66);
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
            if (bottomSplitPane.getDividerLocation()
              / bottomSplitPane.getSize().getWidth() > 0.75) {
               bottomSplitPane.setDividerLocation(0.66);
            }
            statisticsTabbedPane.setSelectedIndex(
              statisticsTabbedPane.getTabCount()-1);
          }
        });
        mousePopup.add(menuItem);

      } else {
        // count values of one Annotation type

        menuItem = new JMenuItem("Occurrences in datastore");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "datastore");
            toolTip = toolTipTemplate.replaceFirst("kind", "datastore");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(corpusID, annotationSetID, type);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("datastore", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "matches");
            toolTip = toolTipTemplate.replaceFirst("kind", "matches");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(patterns, type, true, false);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("matches", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "contexts");
            toolTip = toolTipTemplate.replaceFirst("kind", "contexts");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(patterns, type, false, true);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("contexts", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);

        menuItem = new JMenuItem("Occurrences in matches+contexts");
        menuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ie) {
            description = descriptionTemplate.replaceFirst("kind", "mch+ctxt");
            toolTip = toolTipTemplate.replaceFirst("kind", "matches+contexts");
            int count;
            int numRow = checkStatistics();
            if (numRow == -1) {
              try { // retrieves the number of occurrences
                count = searcher.freq(patterns, type, true, true);
              } catch(SearchException se) {
                se.printStackTrace();
                return;
              }
              oneRowStatisticsTableModel.addRow(
                new Object[]{description, count, ""});
              oneRowStatisticsTableToolTips.add(toolTip);
              numRow = oneRowStatisticsTable.rowModelToView(
                oneRowStatisticsTable.getRowCount() - 1);
            } else {
              count = (Integer) oneRowStatisticsTable.getValueAt(numRow, 1);
            }
            addStatistics("matches+contexts", count, numRow, e);
          }
        });
        mousePopup.add(menuItem);
      }
    }
  }

  /**
   * Table model for the Pattern Tables.
   */
  protected class PatternTableModel extends AbstractTableModel {

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
                  aResult.getStartOffset()).replaceAll("[\n ]+", " ");
        case PATTERN_COLUMN:
          return aResult.getPatternText(aResult.getStartOffset(), aResult
                  .getEndOffset()).replaceAll("[\n ]+", " ");
        case RIGHT_CONTEXT_COLUMN:
          return aResult.getPatternText(aResult.getEndOffset(), aResult
                  .getRightContextEndOffset()).replaceAll("[\n ]+", " ");
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
      JComboBox cropBox = new JComboBox(s);

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
            checkBox.setSelected((!table.getValueAt(row, col).equals("false")));
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
          checkBox.setSelected((!table.getValueAt(row, col).equals("false")));
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
            c.setBackground(UIManager.getColor("CheckBox.background"));
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
          return new JComboBox(s);
        }
      });

      final class FeatureCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {
        private static final long serialVersionUID = 1L;
        private JComboBox featuresBox;
        public FeatureCellEditor() {
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
            (ts.contains((String)annotationRowsJTable.getValueAt(row, col)))?
              annotationRowsJTable.getValueAt(row, col):"");
          return featuresBox;
        }
      }
      annotationRowsJTable.getColumnModel().getColumn(FEATURE)
        .setCellEditor(new FeatureCellEditor());

      annotationRowsJTable.getColumnModel()
      .getColumn(FEATURE)
      .setCellRenderer(new DefaultTableCellRenderer() {
        private static final long serialVersionUID = 1L;
        public Component getTableCellRendererComponent(
          JTable table, Object color, boolean isSelected,
          boolean hasFocus, int row, int col) {
          String[] s = {annotationRows[row][FEATURE]};
          return new JComboBox(s);
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
          return new JComboBox(s);
        }
      });

      final class AddRemoveCellEditorRenderer extends AbstractCellEditor
      implements TableCellRenderer, TableCellEditor, ActionListener {
        private static final long serialVersionUID = 1L;
        private JButton button;
        private int row;
        private boolean addButton;
        public AddRemoveCellEditorRenderer() {
          button = new JButton();
          button.setHorizontalAlignment(SwingConstants.CENTER);
          button.addActionListener(this);
        }
        public Component getTableCellRendererComponent(
                JTable table, Object color, boolean isSelected,
                boolean hasFocus, int row, int col) {
          if (row == numAnnotationRows) {
            // add button if it's the last row of the table
            button.setIcon(MainFrame.getIcon("crystal-clear-action-edit-add"));
            button.setToolTipText("Click to add this line.");
          } else {
            // remove button otherwise
            button.setIcon(
              MainFrame.getIcon("crystal-clear-action-button-cancel"));
            button.setToolTipText("Click to remove this line.");
          }
          button.setSelected(isSelected);
          return button;
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
        }
        public Object getCellEditorValue() {
          return null;
        }
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int col) {
          this.addButton = (row == numAnnotationRows);
          this.row = row;
          button.setIcon(MainFrame.getIcon((addButton) ?
            "crystal-clear-action-edit-add"
          : "crystal-clear-action-button-cancel"));
          return button;
        }
      }
      annotationRowsJTable.getColumnModel().getColumn(REMOVE)
        .setCellEditor(new AddRemoveCellEditorRenderer());
      annotationRowsJTable.getColumnModel().getColumn(REMOVE)
        .setCellRenderer(new AddRemoveCellEditorRenderer());

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

      String valueString;
      if (value instanceof String) {
        valueString = (String) value;
      } else {
        valueString = "value should be a String";
      }

      if (col == SHORTCUT && !valueString.equals("")) {
        if (getAnnotTypesFeatures(null, null).keySet().contains(valueString)) {
          JOptionPane.showMessageDialog(annotationRowsManager,
            "A Shortcut cannot have the same name as an Annotation type.",
            "Alert", JOptionPane.ERROR_MESSAGE);
          return;
        } else {
          int row2 = findAnnotationRow(SHORTCUT, valueString);
          if (row2 >= 0 && row2 != row) {
            JOptionPane.showMessageDialog(annotationRowsManager,
              "A Shortcut with the same name already exists.",
              "Alert", JOptionPane.ERROR_MESSAGE);
            return;
          }
        }
      }

      String previousValue = valueString;
      annotationRows[row][col] = valueString;

      if (!annotationRows[row][SHORTCUT].equals("")) {
        if (annotationRows[row][ANNOTATION_TYPE].equals("")
         || annotationRows[row][FEATURE].equals("")) {
          // TODO table should be updated
          annotationRowsManager.getTable().getColumnModel().getColumn(col)
            .getCellEditor().cancelCellEditing();
          fireTableCellUpdated(row, col);
          annotationRows[row][col] = previousValue;
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
            annotationRowsManager.getTable().getColumnModel().getColumn(col)
              .getCellEditor().cancelCellEditing();
            annotationRows[row][col] = previousValue;
            fireTableCellUpdated(row, col);
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

      annotationRows[row][col] = valueString;
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
    private Timer timer;
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
      timer = new Timer();
      
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

      timer.cancel();

      int pos = ev.getOffset() - 1;

      if (ev.getLength() != 1
      || ( (pos+1 >= finalPosition || pos < initialPosition)
        && (mode == POPUP_TYPES || mode == POPUP_FEATURES) )) {
        // cancel any autocompletion if the user cut some text
        // or delete outside brackets when in POPUP mode
        cleanup();
        return;
      }

      if (mode == POPUP_TYPES || mode == POPUP_FEATURES) {
        removeUpdateCompletion(ev);

      }  else {
        final DocumentEvent evF = ev;
        // schedule autocompletion in half a second
        Date timeToRun = new Date(System.currentTimeMillis() + 500);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
              removeUpdateCompletion(evF);
            }
          }, timeToRun);
      }
    }

    public void removeUpdateCompletion(DocumentEvent ev) {

      int pos = ev.getOffset() - 1;

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

      timer.cancel();

      if (mode == PROGRAMMATIC_INSERT) { return; }

      if (ev.getLength() != 1) {
        // cancel any autocompletion if the user paste some text
        cleanup();
        return;
      }

      if (mode == POPUP_TYPES || mode == POPUP_FEATURES) {
        insertUpdateCompletion(ev);

      }  else {
        final DocumentEvent evF = ev;
        // schedule autocompletion in half a second
        Date timeToRun = new Date(System.currentTimeMillis() + 500);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
              insertUpdateCompletion(evF);
            }
          }, timeToRun);
      }
    }
    
    public void insertUpdateCompletion(DocumentEvent ev) {
      int pos = ev.getOffset();

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
        for (String type : types) {
          queryListModel.addElement(type);
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
        for (String feature : features) {
          queryListModel.addElement(feature);
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
          executeQueryAction.actionPerformed(null);
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
      searcher = ((LuceneDataStoreImpl)target).getSearcher();

      updateAnnotationSetsTypesFeatures();

      try {
        // get the corpus names from the datastore
        java.util.List corpusPIds = ((LuceneDataStoreImpl)target)
                .getLrIds(SerialCorpusImpl.class.getName());
        if(corpusIds != null) {
          for(Object corpusPId : corpusPIds) {
            String name = ((LuceneDataStoreImpl)target).getLrName(corpusPId);
            this.corpusIds.add(corpusPId);
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

  /**
   * A button with a nice etched border that changed when mouse overed,
   * selected or pressed.
   */
  protected class ButtonBorder extends JButton {
    /**
     * Create a button.
     * @param highlight color of the hightlight
     * @param insets margin between content and border
     * @param showBorderWhenInactive true if there should always be a border
     */
    public ButtonBorder(final Color highlight,
                        final Insets insets,
                        final boolean showBorderWhenInactive) {
      final CompoundBorder borderDarker = new CompoundBorder(
        new EtchedBorder(EtchedBorder.LOWERED,
          highlight, highlight.darker()),
        new EmptyBorder(insets));
      final CompoundBorder borderDarkerDarker = new CompoundBorder(
        new EtchedBorder(EtchedBorder.LOWERED,
          highlight, highlight.darker().darker()),
        new EmptyBorder(insets));
      this.setBorder(borderDarker);
      this.setBorderPainted(showBorderWhenInactive);
      this.setContentAreaFilled(false);
      this.setFocusPainted(false);
      this.addMouseListener(new MouseAdapter(){
        public void mouseEntered(MouseEvent e) {
          JButton button = ((JButton)e.getComponent());
          button.setBorder(borderDarkerDarker);
          button.setBorderPainted(true);
        }
        public void mouseExited(MouseEvent e) {
          JButton button = ((JButton)e.getComponent());
          button.setBorder(borderDarker);
          button.setBorderPainted(showBorderWhenInactive);
        }
        public void mousePressed(MouseEvent e) {
          JButton button = ((JButton)e.getComponent());
          button.setContentAreaFilled(true);
        }
        public void mouseReleased(MouseEvent e) {
          JButton button = ((JButton)e.getComponent());
          button.setContentAreaFilled(false);
        }
      });
      this.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) {
          JButton button = ((JButton)e.getComponent());
          button.setBorder(borderDarkerDarker);
          button.setBorderPainted(true);
        }
        public void focusLost(FocusEvent e) {
          JButton button = ((JButton)e.getComponent());
          button.setBorder(borderDarker);
          button.setBorderPainted(showBorderWhenInactive);
        }
      });
    }
  }

}
