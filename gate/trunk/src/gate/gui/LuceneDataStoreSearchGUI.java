package gate.gui;

import gate.creole.annic.Constants;
import gate.creole.annic.Hit;
import gate.creole.annic.PatternAnnotation;
import gate.creole.annic.Pattern;
import gate.creole.annic.Searcher;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.util.*;

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
public class LuceneDataStoreSearchGUI extends AbstractVisualResource implements
		ProgressListener, ActionListener, DatastoreListener {

	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 3256720688877285686L;

	private int firstColumnWidth = 0;

	/** The GUI is associated with the AnnicSearchPR */
	private Object target;

	/**
	 * arraylist consist of instances of patterns associated found in the
	 * document
	 */
	private ArrayList<Hit> patterns;

	/**
	 * A Map that contains annotation types as the key values and the
	 * corresponding arraylist consists of features
	 */
	private Map<String, ArrayList<String>> annotTypes;

	/** Table that lists the patterns */
	private XJTable patternTable;

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
	 * group for exportToXML and exportToHTML, only one of them should be
	 * selected at a time
	 */
	private ButtonGroup exportButtonsGroup;

	/**
	 * group for allPatterns and selectedPatterns, only one of them should be
	 * selected at a time
	 */
	private ButtonGroup patternExportButtonsGroup;

	/** Export all pattern to html */
	private JRadioButton allPatterns;

	/** Export only selectedPatterns to html */
	private JRadioButton selectedPatterns;

	/**
	 * When exportAll patterns option is used, this is made true in order not to
	 * update the GUI
	 */
	private boolean explicitCall;

	/** Text Field that holds the query */
	private JTextField newQuery;

	/**
	 * Which corpus to use when searching in
	 */
	private JComboBox corpusToSearchIn;

	/**
	 * We maintain a list of IDs available in datastore
	 */
	private ArrayList corpusIds;

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

	/** Panels */
	private JPanel comboPanel, guiPanel;

	/** Added Types and features */
	private HashMap addedAnnotTypes;

	/** Added Annotation Types and features */
	private ArrayList<String> addedAnnotTypesInGUI;

	private ArrayList<String> addedAnnotFeatureInGUI;

	/** Gridbagconstraints for the guiPanel */
	private GridBagConstraints guiCons;

	/**
	 * We need to store all the GraphicalPatternRows that shows the annotation
	 * labels somewhere in order to facitiliate direct removal something we can
	 * straight way remove that pattern from this and redraw
	 */
	private ArrayList<PatternRow> currentPatternRows;

	/** A panel for the Pattern Text */
	private JPanel titleTextPanel;

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

	LuceneDataStoreSearchGUI thisInstance;

	/**
	 * Searcher object obtained from the datastore
	 */
	private Searcher searcher;
	
	/** A method gets called when a View in GATE is loaded */
	public Resource init() {
		// initialize maps
		patterns = new ArrayList<Hit>();
		annotTypes = new HashMap<String, ArrayList<String>>();
		addedAnnotTypes = new HashMap();
		addedAnnotTypesInGUI = new ArrayList<String>();
		addedAnnotFeatureInGUI = new ArrayList<String>();
		thisInstance = this;
		corpusIds = new ArrayList();

		// initialize GUI
		initGui();

		// unless the AnnicSerachPR is initialized, we don't have any data to
		// show
		if (target != null) {
			if(target instanceof Searcher) {
			  searcher = (Searcher) target;
			} else if(target instanceof LuceneDataStoreImpl) {
			  searcher = ((LuceneDataStoreImpl) target).getSearcher();
			} else {
			  throw new GateRuntimeException("Invalid target specified for the GUI");
			}

      initLocalData();
			updateGui();
			patternsTableModel.fireTableDataChanged();
			if (patternTable.getRowCount() > 0) {
				patternTable.setRowSelectionInterval(0, 0);
				tableValueChanged();
			}
		}
		validate();
		return this;
	}

	/**
	 * Initialize the local data (i.e. Pattern data etc.) and then update the
	 * GUI
	 */
	protected void updateDisplay() {
		// initialize maps
		patterns.clear();
		annotTypes.clear();
		addedAnnotTypes.clear();
		addedAnnotTypesInGUI.clear();
		addedAnnotFeatureInGUI.clear();

		// unless we have AnnicSearchPR initialized, we donot have any data to
		// show
		if (target != null) {
			initLocalData();
			updateGui();
			// change the totalFoundLabel
			patternsTableModel.fireTableDataChanged();
			if (patternTable.getRowCount() > 0) {
				patternTable.setRowSelectionInterval(0, 0);
				tableValueChanged();
			}
      totalFoundPatterns.setText("Total Found Patterns : "
                    + patternTable.getRowCount());
      totalFoundPatterns.updateUI();
            
		}
	}

	/** Initialize the GUI */
	protected void initGui() {
		guiCons = new GridBagConstraints();
		guiCons.fill = GridBagConstraints.HORIZONTAL;
		guiCons.anchor = GridBagConstraints.FIRST_LINE_START;

		comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		guiPanel = new JPanel();
		guiPanel.setLayout(new GridBagLayout());
		guiPanel.setOpaque(true);
		guiPanel.setBackground(Color.WHITE);

		execQueryAction = new ExecuteQueryAction();
		executeQuery = new JButton(execQueryAction);
		nextResultAction = new NextResultAction();
		nextResults = new JButton(nextResultAction);
		clearQueryAction = new ClearQueryAction();
		clearQueryTF = new JButton(clearQueryAction);
		newQuery = new JTextField(20);

		DefaultComboBoxModel corpusToSearchInModel = new DefaultComboBoxModel();
		corpusToSearchInModel.addElement("Entire DataStore");
		corpusToSearchIn = new JComboBox(corpusToSearchInModel);
		corpusToSearchIn.setPrototypeDisplayValue("Entire DataStore   ");
		corpusToSearchIn.setToolTipText("Corpus Name");
		
		executeQuery.setEnabled(true);
		nextResults.setEnabled(true);
		clearQueryTF.setEnabled(true);
		newQuery.setEnabled(true);
		noOfPatternsField = new JTextField("50", 3);
		noOfPatternsField.setToolTipText("Number of Patterns to retrieve");
		contextWindowField = new JTextField("5", 2);
		contextWindowField
				.setToolTipText("Number of Tokens to be displayed in context");
		noOfPatternsField.setEnabled(true);
		contextWindowField.setEnabled(true);

		queryToExecute = new JLabel("New Query : ");

		totalFoundPatterns = new JLabel("Total Found Patterns : 0        ");
		exportResultsAction = new ExportResultsAction();
		exportToHTML = new JButton(exportResultsAction);
		exportButtonsGroup = new ButtonGroup();
		patternExportButtonsGroup = new ButtonGroup();
		exportButtonsGroup.add(exportToHTML);
		exportToHTML.setEnabled(true);
		allPatterns = new JRadioButton("All Patterns");
		selectedPatterns = new JRadioButton("Selected Patterns");
		patternExportButtonsGroup.add(allPatterns);
		patternExportButtonsGroup.add(selectedPatterns);
		allPatterns
				.setToolTipText("exports all the patterns on this screen");
		selectedPatterns.setToolTipText("exports only the selected patterns");
		allPatterns.setSelected(true);
		allPatterns.setEnabled(true);
		selectedPatterns.setSelected(false);
		selectedPatterns.setEnabled(true);

    if(target == null || target instanceof Searcher) {
      corpusToSearchIn.setEnabled(false);
    }
		
		annotTypesBox = new JComboBox();
		annotTypesBox.addActionListener(this);

		featuresBox = new JComboBox();
		addAnnotTypeButton = new JButton(new AddAnnotTypeAction());

		comboPanel.add(new JLabel("Annotation Types : "));
		comboPanel.add(annotTypesBox);
		comboPanel.add(new JLabel("Features : "));
		comboPanel.add(featuresBox);
		comboPanel.add(addAnnotTypeButton);

		patternsTableModel = new PatternsTableModel();

		patternTable = new XJTable(patternsTableModel);
		// user should see the respective pattern query for the underlying row
		patternTable.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent me) {
				int row = patternTable.rowAtPoint(me.getPoint());
				row = patternTable.rowViewToModel(row);
				Pattern pattern = null;
				if (row > -1) {
					pattern = (Pattern) patterns.get(row);
					patternTable.setToolTipText(pattern.getQueryString());
				}
			}

			public void mouseDragged(MouseEvent me) {
			}
		});

		patternTable.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent me) {
				if (SwingUtilities.isRightMouseButton(me)) {
					// if yes show the option to delete
					final JPopupMenu popup = new JPopupMenu();
					JButton delete = new JButton("Delete");
					popup.add(delete);
					delete.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							int[] rows = patternTable.getSelectedRows();
							for (int i = 0; i < rows.length; i++) {
								rows[i] = patternTable.rowViewToModel(rows[i]);
							}

							rows = sort(rows);
							// here all rows are in ascending order
							for (int i = rows.length - 1; i >= 0; i--) {
								patterns.remove(rows[i]);
							}
							patternsTableModel.fireTableDataChanged();
							totalFoundPatterns
									.setText("Total Found Patterns : "
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
		// selection
		// incase where multiple rows are selected
		// the annotations of the first row will be highlighted
		patternTable.getSelectionModel().addListSelectionListener(
				new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent e) {
						tableValueChanged();
					}
				});

		// user should be allowed to select multiple rows
		patternTable
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		patternTable.setSortable(true);

		// and now add these stuffs into the main Panel
		setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(3, 1));

		JPanel newQueryPanel = new JPanel();
		newQueryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		newQueryPanel.add(queryToExecute);
		newQueryPanel.add(newQuery);
		newQueryPanel.add(corpusToSearchIn);
		newQueryPanel.add(noOfPatternsField);
		newQueryPanel.add(contextWindowField);
		newQueryPanel.add(clearQueryTF);
		newQueryPanel.add(executeQuery);

		JPanel exportResultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		exportResultPanel.add(totalFoundPatterns);
		exportResultPanel.add(nextResults);
		exportResultPanel.add(exportToHTML);
		exportResultPanel.add(allPatterns);
		exportResultPanel.add(selectedPatterns);

		topPanel.add(newQueryPanel);
		topPanel.add(exportResultPanel);
		topPanel.add(comboPanel);

		add(topPanel, BorderLayout.NORTH);
		titleTextPanel = new JPanel(new BorderLayout());
		titleTextPanel.setOpaque(true);
		titleTextPanel.setBackground(Color.WHITE);
		JSplitPane sPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sPane.setDividerLocation(300);
		sPane.add(new JScrollPane(guiPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		sPane.add(new JScrollPane(patternTable));

		JPanel tempPanel = new JPanel(new BorderLayout());
		tempPanel.add(sPane, BorderLayout.CENTER);
		add(tempPanel, BorderLayout.CENTER);
	}

	private int[] sort(int[] rows) {
		// we need to sort rows
		for (int i = 0; i < rows.length; i++) {
			for (int j = 0; j < rows.length - 1; j++) {
				if (rows[j] > rows[j + 1]) {
					int temp = rows[j];
					rows[j] = rows[j + 1];
					rows[j + 1] = temp;
				}
			}
		}
		return rows;
	}

	/**
	 * This method changes the GUI when user changes his/her selection in the
	 * patternTable
	 */
	public void tableValueChanged() {
		firstColumnWidth = 0;
		// get the selected row
		int row = patternTable.getSelectedRow();
		if (row == -1) {
			// now we need to update the GUI
			// so we would first create the JPanel
			VariableWidthJLabel patternLabel = new VariableWidthJLabel(
					"Pattern Text : ");
			int preferredWidth = patternLabel.getOriginalPreferredSize().width;
			if (preferredWidth > firstColumnWidth)
				firstColumnWidth = preferredWidth;
			titleTextPanel.removeAll();
			titleTextPanel.add(patternLabel, BorderLayout.WEST);

			patText = new JTextField("<No Pattern Found>", 15);
			patText.setEditable(false);
			patText.setOpaque(false);
			patText.setBorder(null);
			JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			tempPanel.setOpaque(false);
			tempPanel.add(patText);
			titleTextPanel.add(tempPanel, BorderLayout.CENTER);
			// clear the previous graphics available
			titleTextPanel.validate();
			addAnnotTypeButton.setEnabled(false);
			annotTypesBox.setEnabled(false);
			featuresBox.setEnabled(false);
			guiCons.gridheight = 1;
			guiCons.gridx = 0;
			guiCons.gridy = 0;
			guiCons.weighty = 0.0;
			guiCons.weightx = 1.0;
			guiCons.fill = GridBagConstraints.HORIZONTAL;
			guiCons.insets = new java.awt.Insets(0, 0, 0, 0);

			if (guiPanel.getComponentCount() > 0) {
				guiPanel.removeAll();
				guiPanel.add(titleTextPanel, guiCons);
				guiCons.weighty = 1.0;
				guiCons.fill = GridBagConstraints.BOTH;
				guiPanel.add(Box.createVerticalGlue(), guiCons);
				guiPanel.validate();
			}
			return;
		} else {
			addAnnotTypeButton.setEnabled(true);
			annotTypesBox.setEnabled(true);
			featuresBox.setEnabled(true);
		}

		// user might have sort the data, so get the actual row in our patterns
		// arraylist
		row = patternTable.rowViewToModel(row);

		Pattern pattern = null;
		if (row > -1)
			pattern = (Pattern) patterns.get(row);

		// now we need to update the GUI
		// so we would first create the JPanel
		VariableWidthJLabel patternLabel = new VariableWidthJLabel(
				"Pattern Text : ");
		int preferredWidth = patternLabel.getOriginalPreferredSize().width;
		if (preferredWidth > firstColumnWidth)
			firstColumnWidth = preferredWidth;
		titleTextPanel.removeAll();
		titleTextPanel.add(patternLabel, BorderLayout.WEST);

		patText = new JTextField(pattern == null ? "<No Pattern Found>"
				: pattern.getPatternText());
		patText.setBorder(null);
		patText.setEditable(false);
		patText.setOpaque(false);
		if (pattern != null)
			patText.setToolTipText(pattern.getQueryString());

		JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.setOpaque(false);
		tempPanel.add(patText);
		titleTextPanel.add(tempPanel, BorderLayout.CENTER);

		javax.swing.text.Highlighter hilite = patText.getHighlighter();

		// clear the previous graphics available
		titleTextPanel.validate();
		if (pattern == null) {
			return;
		}

		// highlight the pattern part in the pattern text
		try {
			hilite
					.addHighlight(
							pattern.getStartOffset()
									- pattern.getLeftContextStartOffset(),
							pattern.getEndOffset()
									- pattern.getLeftContextStartOffset(),
							new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(
									new Color(240, 201, 184)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// and now we need to add the current row data
		// reinitialize current Pattern Rows
		currentPatternRows = new ArrayList<PatternRow>();

		// we need to search for the added Annot Types
		for (int i = 0; i < addedAnnotTypesInGUI.size(); i++) {
			String type = (String) addedAnnotTypesInGUI.get(i);
			String feature = (String) addedAnnotFeatureInGUI.get(i);

			PatternRow pRow = null;
			if (!feature.equals("nothing")) {
				if (pRow == null) {
					pRow = new PatternRow(type, feature, patText.getWidth());
				}
				// for this we need to create a rectangle

				// we need to search for offsets
				PatternAnnotation[] annots = pattern.getPatternAnnotations(
						type, feature);
				if (annots == null || annots.length == 0) {
					// do nothing
					annots = new PatternAnnotation[0];
				}

				// we need to find the relative offsets
				int[] offsets = new int[annots.length * 2];
				for (int k = 0, j = 0; k < annots.length; k++, j += 2) {
					gate.creole.annic.PatternAnnotation ann = (gate.creole.annic.PatternAnnotation) annots[k];
					offsets[j] = ann.getStartOffset()
							- pattern.getLeftContextStartOffset();
					offsets[j + 1] = ann.getEndOffset()
							- pattern.getLeftContextStartOffset();
				}

				// we have relative offsets
				// we need to find the relative rectangles
				for (int k = 0, j = 0; j < annots.length; k += 2, j++) {
					try {
						java.awt.Rectangle rect1 = patText
								.modelToView(offsets[k]);
						int x = (int) rect1.getX();
						int y = (int) rect1.getY();
						rect1 = patText.modelToView(offsets[k + 1]);
						int x1 = (int) rect1.getX();
						PatternAnnotation ann = annots[j];
						String featureValue = (String) ann.getFeatures().get(
								feature);
						if (featureValue == null) {
							continue;
						} else {
							int heightOne = patternLabel.getPreferredSize().height;
							// we have found the locations
							pRow.addPattern(x + 1, 0, x1 - x, heightOne,
									featureValue, ann.getText(), getColor(ann
											.getType()));
						}
					} catch (javax.swing.text.BadLocationException ble) {
						ble.printStackTrace();
					}
				}
			} else {
				if (pRow == null) {
					pRow = new PatternRow(type, "nothing", patText.getWidth());
				}
				// for this we need to create a combobox
				// we need to search for annotations
				PatternAnnotation[] annots = pattern
						.getPatternAnnotations(type);
				if (annots == null || annots.length == 0) {
					// do nothing
					annots = new PatternAnnotation[0];
				}

				// we need to find the relative offsets
				int[] offsets = new int[annots.length * 2];
				for (int k = 0, j = 0; k < annots.length; k++, j += 2) {
					gate.creole.annic.PatternAnnotation ann = (gate.creole.annic.PatternAnnotation) annots[k];
					offsets[j] = ann.getStartOffset()
							- pattern.getLeftContextStartOffset();
					offsets[j + 1] = ann.getEndOffset()
							- pattern.getLeftContextStartOffset();
				}

				// we have relative offsets
				// we need to find the relative rectangles
				int heightOne = patternLabel.getPreferredSize().height;
				for (int k = 0, j = 0; j < annots.length; k += 2, j++) {
					try {
						java.awt.Rectangle rect1 = patText
								.modelToView(offsets[k]);
						int x = (int) rect1.getX();
						int y = (int) rect1.getY();
						rect1 = patText.modelToView(offsets[k + 1]);
						int x1 = (int) rect1.getX();
						// we have found the locations
						PatternAnnotation ann = annots[j];
						pRow.addPattern(x, 0, x1 - x, heightOne, ann
								.getFeatures(), ann.getText(), getColor(ann
								.getType()));
					} catch (javax.swing.text.BadLocationException ble) {
						ble.printStackTrace();
					}
				}
			}

			// and now add this pattern Row in the guiPanel
			pRow.repaintComps();
			currentPatternRows.add(pRow);
		}

		// and finally show all pattern rows in the guiPanel
		guiCons.gridheight = 1;
		guiCons.gridx = 0;
		guiCons.gridy = 0;
		guiCons.weighty = 0.0;
		guiCons.weightx = 1.0;
		guiCons.fill = GridBagConstraints.HORIZONTAL;
		guiCons.insets = new java.awt.Insets(0, 0, 0, 0);

		if (guiPanel.getComponentCount() > 0) {
			guiPanel.removeAll();
			guiPanel.add(titleTextPanel, guiCons);
		}

		// we have totalHeight required by the guiPanel
		int rowToAdd = 0;
		for (int i = 1; i <= currentPatternRows.size(); i++) {
			PatternRow pr = ((PatternRow) currentPatternRows.get(i - 1));
			if (pr.yGrids == 0) {
				pr.yGrids = 1;
			}

			guiCons.gridy = i + rowToAdd;
			guiCons.weighty = 0.0;
			guiCons.gridheight = pr.yGrids;
			guiPanel.add(pr, guiCons);
			rowToAdd += pr.yGrids - 1;
		}

		guiCons.weighty = 1.0;
		guiCons.fill = GridBagConstraints.BOTH;
		guiPanel.add(Box.createVerticalGlue(), guiCons);
		guiPanel.validate();

		for (int i = 0; i < currentPatternRows.size(); i++) {
			PatternRow pr = ((PatternRow) currentPatternRows.get(i));
			pr.updateDisplay();
		}

		validate();
		updateUI();
	}

	/**
	 * This method uses the java.util.prefs.Preferences and get the color for
	 * particular annotationType.. This color could have been saved by the
	 * AnnotationSetsView
	 * 
	 * @param annotationType
	 * @return
	 */
	private Color getColor(String annotationType) {
		java.util.prefs.Preferences prefRoot = null;
		try {
			prefRoot = java.util.prefs.Preferences.userNodeForPackage(Class
					.forName("gate.gui.docview.AnnotationSetsView"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		int rgba = prefRoot.getInt(annotationType, -1);
		Color colour;
		if (rgba == -1) {
			// initialise and save
			float components[] = colorGenerator.getNextColor().getComponents(
					null);
			colour = new Color(components[0], components[1], components[2],
					0.5f);
			int rgb = colour.getRGB();
			int alpha = colour.getAlpha();
			rgba = rgb | (alpha << 24);
			prefRoot.putInt(annotationType, rgba);

		} else {
			colour = new Color(rgba, true);
		}
		return colour;
	}

	/** Initializes the comboboxes for annotation Types and their features */
	public void updateGui() {
		guiPanel.removeAll();
		guiPanel.validate();
		guiPanel.updateUI();
		annotTypesBox.removeAllItems();
		ArrayList<String> annotTypesKeyList = new ArrayList<String>(annotTypes.keySet());
		for (int i = 0; i < annotTypesKeyList.size(); i++) {
			annotTypesBox.addItem(annotTypesKeyList.get(i));
		}
		annotTypesBox.updateUI();

		featuresBox.removeAllItems();
		if (annotTypesBox.getItemCount() > 0) {
			ArrayList<String> featuresList = annotTypes.get(annotTypesBox.getItemAt(0));
			for (int i = 0; i < featuresList.size(); i++) {
				featuresBox.addItem(featuresList.get(i));
			}
		}
		featuresBox.updateUI();
		newQuery.setText(searcher.getQuery());
	}

	/** Updates the features box according to the selected annotation type */
	public void actionPerformed(ActionEvent ae) {

		// action for annotTypesBox
		if (ae.getSource() == annotTypesBox) {
			String choice = (String) annotTypesBox.getSelectedItem();
			if (choice != null && !choice.equals(previousChoice)) {
				previousChoice = choice;
				// yes we need to update the featuresBox
				ArrayList<String> featuresToAdd = annotTypes.get(choice);

				// and finally update the featuresBox
				featuresBox.removeAllItems();
				for (int i = 0; i < featuresToAdd.size(); i++) {
					featuresBox.addItem(featuresToAdd.get(i));
				}
				featuresBox.updateUI();
			}
		}
	}

	/** Adds the pattern Row gui for newly selected annotation type and feature */
	protected class AddAnnotTypeAction extends AbstractAction {

		/**
		 * serial version id
		 */
		private static final long serialVersionUID = 3256438118801225013L;

		AddAnnotTypeAction() {
			super("Add Annotation Type");
		}

		public void actionPerformed(ActionEvent ae) {
			// we would first find out the type of annotation selected and the
			// feature
			int index = annotTypesBox.getSelectedIndex();
			if (index < 0 && annotTypesBox.getItemCount() > 0) {
				index = 0;
			}
			String annotType = (String) annotTypesBox.getItemAt(index);

			index = featuresBox.getSelectedIndex();
			if (index < 0 && featuresBox.getItemCount() > 0) {
				index = 0;
			}

			String featureType = (String) featuresBox.getItemAt(index);
			if (featureType.equals("All")) {
				// nothing indiates the user has not selected any feature
				featureType = "nothing";
			}

			boolean add = true;

			// check if this is already available
			for (int i = 0; i < addedAnnotTypesInGUI.size(); i++) {
				if (((String) addedAnnotTypesInGUI.get(i)).equals(annotType)) {
					if (((String) addedAnnotFeatureInGUI.get(i))
							.equals(featureType)) {
						add = false;
						break;
					}
				}
			}

			if (add) {
				addedAnnotTypesInGUI.add(annotType);
				addedAnnotFeatureInGUI.add(featureType);
			} else {
				JOptionPane.showMessageDialog(null, "Already available in GUI");
				return;
			}

			// update the gui
			patternTable.setRowSelectionInterval(patternTable.getSelectedRow(),
					patternTable.getSelectedRow());
			tableValueChanged();
		}
	}

	
	
	/**
	 * Exports all patterns to the XML File
	 */
	private class ExportResultsAction extends AbstractAction {
		/**
		 * serial version id
		 */
		private static final long serialVersionUID = 3257286928859412277L;

		ExportResultsAction() {
			super("Export Patterns");
		}

		public void actionPerformed(ActionEvent ae) {

			Map parameters = searcher.getParameters();

			// if there are no pattern say so
			if (patterns == null || patterns.isEmpty()) {
				try {
					JOptionPane.showMessageDialog(gate.Main.getMainFrame(),
							"No patterns found to export");
					return;
				} catch (Exception e) {
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
				
				JFrame frame = target instanceof Searcher ? null : gate.Main.getMainFrame(); 
				fileDialog.showSaveDialog(frame);
				java.io.File file = fileDialog.getSelectedFile();

				// if user pressed the cancel button
				if (file == null)
					return;

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
				if (selectedPatterns.isSelected()) {
					// in this case we only export those patterns which are
					// selected by the user
					int[] rows = patternTable.getSelectedRows();
					for (int i = 0; i < rows.length; i++) {
						int num = patternTable.rowViewToModel(rows[i]);
						patternsToExport.add(patterns.get(num));
					}

				} else {
          // in this case we only export those patterns which are
          // selected by the user
          for (int i = 0; i < patternTable.getRowCount(); i++) {
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
				java.io.BufferedWriter bw = new java.io.BufferedWriter(
						fileWriter);
				// write header
				bw.write("<HTML><TITLE>ANNIC Output</TITLE><BODY>");
				bw.write("<BR><B>Query Issued: " + searcher.getQuery()
						+ "<BR>Context Window :"
						+ ((Integer) parameters.get(Constants.CONTEXT_WINDOW)).intValue()
						+ "</B><BR><BR>");
				bw.write("<BR><B>Queries:</B>");
				String queryString = "";
				for (int i = 0; i < patternsToExport.size(); i++) {
					Pattern ap = (Pattern) patternsToExport.get(i);
					if (!ap.getQueryString().equals(queryString)) {
						bw.write("<BR><a href=\"#" + ap.getQueryString()
								+ "\">" + ap.getQueryString() + "</a>");
						queryString = ap.getQueryString();
					}
				}

				bw.write("<BR><BR>");
				queryString = "";
				for (int i = 0; i < patternsToExport.size(); i++) {
					Pattern ap = (Pattern) patternsToExport.get(i);
					if (!ap.getQueryString().equals(queryString)) {
						if (!queryString.equals("")) {
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
							+ ap.getPatternText(ap.getLeftContextStartOffset(),
									ap.getStartOffset()) + "</TD>");
					bw.write("<TD>"
							+ ap.getPatternText(ap.getStartOffset(), ap
									.getEndOffset()) + "</TD>");
					bw.write("<TD>"
							+ ap.getPatternText(ap.getEndOffset(), ap
									.getRightContextEndOffset()) + "</TD>");
					bw.write("</TR>");
				}
				bw.write("</TABLE></BODY></HTML>");
				bw.flush();
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** Action to clear the newQuery text box */
	protected class ClearQueryAction extends AbstractAction {
		/**
		 * serial version id
		 */
		private static final long serialVersionUID = 3257569516199228209L;

		ClearQueryAction() {
			super("Clear");
		}

		public void actionPerformed(ActionEvent ae) {
			newQuery.setText("");
		}
	}

	/** finds out the newly created query and execute it */
	protected class ExecuteQueryAction extends AbstractAction {

		/**
		 * serial version id
		 */
		private static final long serialVersionUID = 3258128055204917812L;

		ExecuteQueryAction() {
			super("Execute");
		}

		public void actionPerformed(ActionEvent ae) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					thisInstance.setEnabled(false);
					Map parameters = searcher.getParameters();
					if (parameters == null)
						parameters = new HashMap();
					
					if(target instanceof LuceneDataStoreImpl) {
					  String indexLocation = new File(((URL) ((LuceneDataStoreImpl)target).getIndexer()
							.getParameters().get(Constants.INDEX_LOCATION_URL))
							.getFile()).getAbsolutePath();
					  ArrayList<String> indexLocations = new ArrayList<String>();
					  indexLocations.add(indexLocation);
					  parameters.put(Constants.INDEX_LOCATIONS, indexLocations);

					  int index = corpusToSearchIn.getSelectedIndex();
	          String corpus2SearchIn = index == 0 ? null
	              : (String) corpusIds.get(index - 1);
	          parameters.put(Constants.CORPUS_ID, corpus2SearchIn);
					}

					Integer noOfPatterns = new Integer(noOfPatternsField
							.getText().trim());
					Integer contextWindow = new Integer(contextWindowField
							.getText().trim());
					String query = newQuery.getText().trim();
					parameters.put(Constants.CONTEXT_WINDOW, contextWindow);
					
					try {
						if (searcher.search(query, parameters)) {
							searcher.next(noOfPatterns.intValue());
						}
					} catch (Exception e) {
						e.printStackTrace();
						thisInstance.setEnabled(true);
					}
					processFinished();
					thisInstance.setEnabled(true);
				}
			});
		}
	}

	/** finds out the next few results */
	protected class NextResultAction extends AbstractAction {

		/**
		 * Serial version ID
		 */
		private static final long serialVersionUID = 3257005436719871288L;

		NextResultAction() {
			super("Next Page");
		}

		public void actionPerformed(ActionEvent ae) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					thisInstance.setEnabled(false);
					try {
						searcher.next(Integer.parseInt(noOfPatternsField
								.getText()));
					} catch (Exception e) {
						e.printStackTrace();
						thisInstance.setEnabled(true);
					}
					processFinished();
					thisInstance.setEnabled(true);
				}
			});
		}
	}

	/** Table model for the Pattern Tables */
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
			return 4;
		}

		/** Column headings */
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case DOC_NAME_COLUMN:
				return "Document";
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
		public Class getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case DOC_NAME_COLUMN:
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
			Pattern aResult = (Pattern) patterns.get(rowIndex);
			switch (columnIndex) {
			case DOC_NAME_COLUMN:
				return aResult.getDocumentID();
			case LEFT_CONTEXT_COLUMN:
				return aResult.getPatternText(aResult
						.getLeftContextStartOffset(), aResult.getStartOffset());
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

		static private final int DOC_NAME_COLUMN = 0;

		static private final int LEFT_CONTEXT_COLUMN = 1;

		static private final int PATTERN_COLUMN = 2;

		static private final int RIGHT_CONTEXT_COLUMN = 3;

	}

	/** the row in the ANNIC Pattern GUI */
	protected class PatternRow extends JPanel implements ActionListener {
		/**
		 * serial version id
		 */
		private static final long serialVersionUID = 3616730456989380917L;

		VariableWidthJLabel patternLabel;

		JButton removePattern;

		ArrayList subComponents;

		JPanel subGuiPanel;

		int maxY = 0;

		int width = 0;

		int yGrids = 0;

		String type, feature;

		// updates all GUI
		public void updateDisplay() {
			patternLabel.updateUI();
			removePattern.updateUI();
			// we need to calculate the height
			// if it is only 1
			// it will be 20
			// otherwise
			int heightOne = patternLabel.getPreferredSize().height;
			subGuiPanel.setPreferredSize(new Dimension(subGuiPanel
					.getPreferredSize().width, (heightOne * yGrids + yGrids)));
			subGuiPanel.updateUI();
		}

		public PatternRow(String type, String feature, int width) {
			this.type = type;
			this.feature = feature;
			String patL = type;
			if (!feature.equals("nothing")) {
				patL += "." + feature;
			}
			this.width = width;
			setLayout(new BorderLayout());
			setOpaque(false);
			// setBorder(LineBorder.createGrayLineBorder());

			patternLabel = new VariableWidthJLabel(patL + ": ");
			patternLabel.setAlignmentY(Component.TOP_ALIGNMENT);
			int preferredWidth = patternLabel.getOriginalPreferredSize().width;
			if (preferredWidth > firstColumnWidth)
				firstColumnWidth = preferredWidth;
			Box labelBox = Box.createVerticalBox();
			labelBox.add(patternLabel);
			labelBox.add(Box.createVerticalGlue());
			add(labelBox, BorderLayout.WEST);

			removePattern = new JButton(MainFrame.getIcon("delete.gif"));
			removePattern.addActionListener(this);
			removePattern.setBorderPainted(true);
			removePattern.setMargin(new Insets(0, 0, 0, 0));
			removePattern.setAlignmentY(Component.TOP_ALIGNMENT);
			Box buttonBox = Box.createVerticalBox();
			buttonBox.add(removePattern);
			buttonBox.add(Box.createVerticalGlue());
			add(buttonBox, BorderLayout.EAST);

			subGuiPanel = new JPanel();
			subGuiPanel.setLayout(null);
			subGuiPanel.setOpaque(false);
			subGuiPanel.setBorder(null);
			subGuiPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			add(subGuiPanel, BorderLayout.CENTER);
			subComponents = new ArrayList();
		}

		public void actionPerformed(ActionEvent ae) {
			// we need to remove these things from the added stuff
			forLoop: for (int i = 0; i < addedAnnotTypesInGUI.size(); i++) {
				String type1 = (String) addedAnnotTypesInGUI.get(i);
				if (type1.equals(type)) {
					String f1 = (String) addedAnnotFeatureInGUI.get(i);
					if (feature.equals(f1)) {
						addedAnnotTypesInGUI.remove(i);
						addedAnnotFeatureInGUI.remove(i);
						break forLoop;
					}
				}
			}

			currentPatternRows.remove(this);
			tableValueChanged();
		}

		// this method is useful to draw the the subComponents
		public void repaintComps() {

			// before drawing we clear all the components
			subGuiPanel.removeAll();

			// we need to draw each subComponent one at a time
			for (int i = 0; i < subComponents.size(); i++) {

				// if we need to draw rectangles for the Type.feature values
				if (subComponents.get(i) instanceof AnnotType) {

					// so lets find out the pattern
					final AnnotType patG = (AnnotType) subComponents.get(i);

					final JLabel tLabel = new JLabel(patG.featureVal);
					tLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
					tLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
					tLabel.setBorder(BorderFactory.createLineBorder(
							Color.BLACK, 1));
					tLabel.setBackground(patG.color);
					tLabel.setOpaque(true);
					tLabel
							.setMaximumSize(new Dimension(patG.width,
									patG.height));
					tLabel.setPreferredSize(new Dimension(patG.width,
							patG.height));
					tLabel.addMouseListener(new MouseListener() {
						JPopupMenu popup = null;

						boolean visible = false;

						public void mouseEntered(MouseEvent me) {
							visible = true;
							popup = new JPopupMenu();
							popup.setBackground(new Color(249, 253, 213));
							popup.setOpaque(true);
							popup.setLayout(new GridLayout(2, 1));
							popup.add(new JLabel("Text : " + patG.text));
							popup.add(new JLabel("Value : " + patG.featureVal));
							popup.show(subGuiPanel, patG.x, patG.y
									+ patG.height);
						}

						public void mouseClicked(MouseEvent me) {
							// here we need to add the
							int caretPosition = newQuery.getCaretPosition();
							if (caretPosition < 0) {
								caretPosition = newQuery.getText().length();
							}
							String text = newQuery.getText();
							text = text.substring(0, caretPosition)
									+ "{"
									+ type
									+ (feature.equals("nothing") ? "" : "."
											+ feature)
									+ "==\""
									+ tLabel.getText()
									+ "\"}"
									+ text.substring(caretPosition, text
											.length());
							newQuery.setText(text);
						}

						public void mousePressed(MouseEvent me) {
						}

						public void mouseReleased(MouseEvent me) {
						}

						public void mouseExited(MouseEvent me) {
							if (popup == null) {
								// do nothing
							} else if (visible) {
								popup.setVisible(false);
							}
						}
					});
					subGuiPanel.add(tLabel);

					// we find out the different between the location of the
					// subGuiPanel and the
					// and we need to place it at the proper place
					tLabel.setBounds(patG.x + 4, patG.y, patG.width,
							patG.height);

				} else {

					final AnnotTypeFeatures atf = (AnnotTypeFeatures) subComponents
							.get(i);
					ArrayList keys;
					ArrayList values = new ArrayList();
					keys = new ArrayList(atf.features.keySet());
					for (int j = 0; j < keys.size(); j++) {
						values.add(keys.get(j).toString() + "="
								+ atf.features.get(keys.get(j)));
					}

					JLabel tLabel = new JLabel("");
					tLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
					tLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
					tLabel.setBorder(BorderFactory.createLineBorder(
							Color.BLACK, 1));
					tLabel.setOpaque(true);
					tLabel.setBackground(atf.color);
					tLabel.setMaximumSize(new Dimension(atf.width, atf.height));
					tLabel
							.setPreferredSize(new Dimension(atf.width,
									atf.height));
					tLabel.addMouseListener(new MouseListener() {
						JPopupMenu popup = null;

						boolean visible = false;

						public void mouseEntered(MouseEvent me) {
							visible = true;
							popup = new JPopupMenu();

							ArrayList features = atf.getFeatures();
							popup.setLayout(new GridLayout(features.size() + 2,
									1));
							popup.setBackground(new Color(249, 253, 213));
							popup.setOpaque(true);
							popup.add(new JLabel("Text : " + atf.text));
							popup.add(new JLabel("Features : "));
							for (int i = 0; i < features.size(); i++) {
								popup.add(new JLabel((String) features.get(i)));
							}
							popup.show(subGuiPanel, atf.x, atf.y + atf.height);
						}

						public void mouseClicked(MouseEvent me) {
							// here we need to add the
							int caretPosition = newQuery.getCaretPosition();
							if (caretPosition < 0) {
								caretPosition = newQuery.getText().length();
							}
							String text = newQuery.getText();
							text = text.substring(0, caretPosition)
									+ "{"
									+ type
									+ (feature.equals("nothing") ? "" : "."
											+ feature)
									+ "}"
									+ text.substring(caretPosition, text
											.length());
							newQuery.setText(text);
						}

						public void mousePressed(MouseEvent me) {
						}

						public void mouseReleased(MouseEvent me) {
						}

						public void mouseExited(MouseEvent me) {
							if (popup == null) {
								// do nothing
							} else if (visible) {
								popup.setVisible(false);
							}
						}
					});
					subGuiPanel.add(tLabel);
					tLabel.setBounds(atf.x + 4, atf.y, atf.width, atf.height);
				}
			}
			subGuiPanel.validate();
		}

		// this is used to add Type.feature
		public void addPattern(int x, int y, int w, int h, String featureVal,
				String text, Color color) {
			y = findOutY(x, y, w, h);
			if (y == 0) {
				// adding one pixel at top and bottom
				y = y + 2;
			}
			AnnotType rect = new AnnotType();
			rect.x = x;
			rect.y = y;
			rect.width = w;
			rect.height = h;
			rect.featureVal = featureVal;
			rect.text = text;
			rect.color = color;
			subComponents.add(rect);
		}

		public void addPattern(int x, int y, int w, int h, HashMap features,
				String text, Color color) {
			y = findOutY(x, y, w, h);
			if (y == 0) {
				// adding one pixel at top and bottom
				y = y + 2;
			}
			AnnotTypeFeatures atf = new AnnotTypeFeatures();
			atf.x = x;
			atf.y = y;
			atf.width = w;
			atf.height = h;
			atf.features = features;
			atf.text = text;
			atf.color = color;
			subComponents.add(atf);
		}

		// this tells where to put the next rectangle
		private int findOutY(int x, int y, int w, int h) {
			// we need to see if any other component in this row panel overlaps
			// with this
			java.awt.Rectangle rc = new java.awt.Rectangle(x, y, w, h);
			for (int i = 0; i < subComponents.size(); i++) {
				java.awt.Rectangle rc1;
				if (subComponents.get(i) instanceof AnnotType) {
					AnnotType t = (AnnotType) subComponents.get(i);
					rc1 = new java.awt.Rectangle(t.x, t.y, t.width, t.height);
				} else {
					AnnotTypeFeatures atf = (AnnotTypeFeatures) subComponents
							.get(i);
					rc1 = new java.awt.Rectangle(atf.x, atf.y, atf.width,
							atf.height);
				}
				if (rc.intersects(rc1)) {
					// well they intersect.. so we need to find out the new
					// location of y
					rc.y = rc1.y + rc1.height;
				}
			}
			if (rc.y + rc.height > maxY) {
				yGrids++;
				maxY = rc.y + rc.height;
			}
			return rc.y;
		}

	}

	// this is a part of PatternRow
	protected class AnnotType {
		int x;

		int y;

		int width;

		int height;

		String featureVal;

		String text;

		Color color;
	}

	// this is a part of PatternRow
	protected class AnnotTypeFeatures {
		int x, y, width, height;

		HashMap features;

		String text;

		Color color;

		public ArrayList getFeatures() {
			ArrayList feat = new ArrayList();
			if (features == null) {
				feat.add("No Feature Available");
				return feat;
			}

			Set set = features.keySet();
			if (set == null || set.size() == 0) {
				feat.add("No Feature Available");
				return feat;
			}

			ArrayList keys = new ArrayList(set);
			for (int i = 0; i < keys.size(); i++) {
				Object val1 = features.get(keys.get(i));
				if (val1 == null) {
					continue;
				}
				feat.add(((String) keys.get(i)) + "=" + val1.toString());
			}
			return feat;
		}
	}

	// initialize the local data
	protected void initLocalData() {
		Hit[] pats = searcher.getHits();
		if (patterns == null)
			patterns = new ArrayList<Hit>();
		patterns.clear();
		for (int m = 0; m < pats.length; m++) {
			patterns.add(pats[m]);
		}
		pats = null;
		annotTypes = searcher.getAnnotationTypesMap();
	}

	/**
	 * Called by the GUI when this viewer/editor has to initialise itself for a
	 * specific object.
	 * 
	 * @param target
	 *            the object (be it a {@link gate.Resource},
	 *            {@link gate.DataStore}or whatever) this viewer has to display
	 */
	public void setTarget(Object target) {
		if (!(target instanceof LuceneDataStoreImpl) && !(target instanceof Searcher)) {
			throw new IllegalArgumentException(
					"The GATE LuceneDataStoreSearchGUI can only be used with a GATE LuceneDataStores!\n"
							+ target.getClass().toString()
							+ " is not a GATE LuceneDataStore or an object of Searcher!");
		}

		this.target = target;
		if(this.target instanceof LuceneDataStoreImpl) {
		  ((LuceneDataStoreImpl) this.target).addDatastoreListener(this);
		  corpusToSearchIn.setEnabled(true);
		  this.searcher = ((LuceneDataStoreImpl) this.target).getSearcher();
	    exportToHTML.setEnabled(true);
	    allPatterns.setEnabled(true);
	    selectedPatterns.setEnabled(true);
		  
	    // here we need to find out all corpus resources from the datastore
	    try {
	      java.util.List corpusPIds = ((LuceneDataStoreImpl) this.target)
	          .getLrIds(SerialCorpusImpl.class.getName());
	      if (corpusIds != null) {
	        for (int i = 0; i < corpusPIds.size(); i++) {
	          // in order to obtain their names, we'll have to get them
	          String name = ((LuceneDataStoreImpl) this.target).getLrName(corpusPIds.get(i));
	          // so first lets add this ID to corpusIds
	          this.corpusIds.add(corpusPIds.get(i));
	          // and we need to add the name to the combobox
	          ((DefaultComboBoxModel) corpusToSearchIn.getModel())
	              .addElement(name);
	        }
	      }
	      // lets fire the update event on combobox
	      SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	          corpusToSearchIn.updateUI();
	        }
	      });
	    } catch (PersistenceException pe) {
	      // couldn't find any available corpusIds
	    }
		} else {
		  this.searcher = (Searcher) this.target;
		  corpusToSearchIn.setEnabled(false);
		}
		
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
		if (!explicitCall) {
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
					} else {
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
	 * This method is called by datastore when an existing resource is deleted
	 */
	public void resourceDeleted(DatastoreEvent de) {
		Resource resource = de.getResource();
		if (resource instanceof Corpus) {
			// lets check if it is already available in our list
			Object id = de.getResourceID();
			int index = corpusIds.indexOf(id);
			if (index < 0) {
				return;
			}

			// we add 1 to index
			// this is because the first element in combo box is "Entire
			// DataStore"
			index++;

			// now lets remove it from the comboBox as well
			((DefaultComboBoxModel) corpusToSearchIn.getModel())
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
		if (resource instanceof Corpus) {
			// lets check if it is already available in our list
			Object id = de.getResourceID();
			if (!corpusIds.contains(id)) {
				// we need to add its name to the combobox
				corpusIds.add(id);
				((DefaultComboBoxModel) corpusToSearchIn.getModel())
						.addElement(resource.getName());
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						corpusToSearchIn.updateUI();
					}
				});
			}
		}
	}

	protected class VariableWidthJLabel extends JLabel {

		public VariableWidthJLabel() {
			super();
		}

		public VariableWidthJLabel(Icon image, int horizontalAlignment) {
			super(image, horizontalAlignment);
		}

		public VariableWidthJLabel(Icon image) {
			super(image);
		}

		public VariableWidthJLabel(String text, Icon icon,
				int horizontalAlignment) {
			super(text, icon, horizontalAlignment);
		}

		public VariableWidthJLabel(String text, int horizontalAlignment) {
			super(text, horizontalAlignment);
		}

		public VariableWidthJLabel(String text) {
			super(text);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension dim = super.getPreferredSize();
			return new Dimension(firstColumnWidth, dim.height);
		}

		public Dimension getOriginalPreferredSize() {
			return super.getPreferredSize();
		}
	}
}