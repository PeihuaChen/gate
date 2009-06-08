/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AnnotationDiffGUI.java
 *
 *  Valentin Tablan, 24-Jun-2004
 *
 *  $Id$
 */

package gate.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import gate.*;
import gate.event.CreoleEvent;
import gate.swing.XJTable;
import gate.util.*;
import gate.event.CreoleListener;

/**
 */
public class AnnotationDiffGUI extends JFrame{

  public AnnotationDiffGUI(String title){
    super(title);
    MainFrame.getGuiRoots().add(this);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    initLocalData();
    initGUI();
    initListeners();
    populateGUI();
  }

  protected void initLocalData(){
    differ = new AnnotationDiffer();
    pairings = new ArrayList<AnnotationDiffer.Pairing>();
    keyMoveValueRows = new ArrayList<Boolean>();
    resMoveValueRows = new ArrayList<Boolean>();
    significantFeatures = new HashSet();
    keyDoc = null;
    resDoc = null;
  }


  protected void initGUI(){
    getContentPane().setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    //changes from defaults
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(2,4,2,4);

    //ROW 0
    constraints.gridx = 1;
    getContentPane().add(new JLabel("Document"), constraints);
    constraints.gridx = GridBagConstraints.RELATIVE;
    getContentPane().add(new JLabel("Annotation Set"), constraints);
    constraints.gridwidth = 2;
    getContentPane().add(new JLabel("F-Measure Weight"), constraints);
    constraints.gridwidth = 1;
    weightTxt = new JTextField("1.00");
    getContentPane().add(weightTxt, constraints);
    diffAction = new DiffAction();
    diffAction.setEnabled(false);
    doDiffBtn = new JButton(diffAction);
    doDiffBtn.setToolTipText("Choose two annotation sets "
            +"that have at least one annotation type in common.");
    constraints.gridx = 7;
    constraints.gridheight = 3;
    constraints.fill = GridBagConstraints.NONE;
    getContentPane().add(doDiffBtn, constraints);
    constraints.gridheight = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;

    //ROW 1
    constraints.gridx = 0;
    constraints.gridy = 1;
    getContentPane().add(new JLabel("Key:"), constraints);
    constraints.gridx = GridBagConstraints.RELATIVE;
    keyDocCombo = new JComboBox();
    getContentPane().add(keyDocCombo, constraints);
    keySetCombo = new JComboBox();
    getContentPane().add(keySetCombo, constraints);
    constraints.gridwidth = 2;
    getContentPane().add(new JLabel("Annotation Type:"), constraints);
    annTypeCombo = new JComboBox();
    getContentPane().add(annTypeCombo, constraints);
    constraints.gridwidth = 1;

    //ROW 2
    constraints.gridy = 2;
    constraints.gridx = 0;
    getContentPane().add(new JLabel("Response:"), constraints);
    constraints.gridx = GridBagConstraints.RELATIVE;
    resDocCombo = new JComboBox();
    getContentPane().add(resDocCombo, constraints);
    resSetCombo = new JComboBox();
    getContentPane().add(resSetCombo, constraints);
    getContentPane().add(new JLabel("Features:"), constraints);
    ButtonGroup btnGrp = new ButtonGroup();
    allFeaturesBtn = new JRadioButton("All");
    allFeaturesBtn.setOpaque(false);
    btnGrp.add(allFeaturesBtn);
    getContentPane().add(allFeaturesBtn, constraints);
    someFeaturesBtn = new JRadioButton("Some");
    someFeaturesBtn.setOpaque(false);
    btnGrp.add(someFeaturesBtn);
    getContentPane().add(someFeaturesBtn, constraints);
    noFeaturesBtn = new JRadioButton("None");
    noFeaturesBtn.setOpaque(false);
    btnGrp.add(noFeaturesBtn);
    getContentPane().add(noFeaturesBtn, constraints);
    noFeaturesBtn.setSelected(true);

    //ROW 3 -> the table
    constraints.gridy = 3;
    constraints.gridx = 0;
    constraints.gridwidth = 10;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;
    diffTableModel = new DiffTableModel();
    diffTable = new XJTable(diffTableModel);
    diffTable.setDefaultRenderer(String.class, new DiffTableCellRenderer());
    diffTable.setDefaultRenderer(Boolean.class, new DiffTableCellRenderer());
    diffTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    diffTable.setComparator(DiffTableModel.COL_MATCH, new Comparator(){
      public int compare(Object o1, Object o2){
        String label1 = (String)o1;
        String label2 = (String)o2;
        int match1 = 0;
        while(!label1.equals(matchLabel[match1])) match1++;
        int match2 = 0;
        while(!label2.equals(matchLabel[match2])) match2++;

        return match1 - match2;
      }
    });
    diffTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    diffTable.setRowSelectionAllowed(true);
    diffTable.setColumnSelectionAllowed(true);
    diffTable.setEnableHidingColumns(true);

    Comparator startEndComparator = new Comparator() {
      public int compare(Object o1, Object o2) {
        String no1 = (String) o1;
        String no2 = (String) o2;
        if (no1.trim().equals("") && no2.trim().equals("")) {
          return 0;
        }
        else if (no1.trim().equals("")) {
          return -1;
        }
        else if (no2.trim().equals("")) {
          return 1;
        }
        int n1 = Integer.parseInt(no1);
        int n2 = Integer.parseInt(no2);
        if(n1 == n2)
          return 0;
        else if(n1 > n2)
          return 1;
        else
          return -1;
      }
    };

    diffTable.setComparator(DiffTableModel.COL_KEY_START, startEndComparator);
    diffTable.setComparator(DiffTableModel.COL_KEY_END, startEndComparator);
    diffTable.setComparator(DiffTableModel.COL_RES_START, startEndComparator);
    diffTable.setComparator(DiffTableModel.COL_RES_END, startEndComparator);

    diffTable.setSortable(true);
    diffTable.setSortedColumn(DiffTableModel.COL_MATCH);
    diffTable.setAscending(false);
    scroller = new JScrollPane(diffTable);
    getContentPane().add(scroller, constraints);

    //build the results pane
    resultsPane = new JPanel();
    resultsPane.setLayout(new GridBagLayout());
    //COLUMN 0
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.gridx = 0;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    JLabel lbl = new JLabel("Correct:");
    lbl.setBackground(diffTable.getBackground());
    resultsPane.add(lbl, constraints);
    lbl = new JLabel("Partially Correct:");
    lbl.setBackground(PARTIALLY_CORRECT_BG);
    lbl.setOpaque(true);
    resultsPane.add(lbl, constraints);
    lbl = new JLabel("Missing:");
    lbl.setBackground(MISSING_BG);
    lbl.setOpaque(true);
    resultsPane.add(lbl, constraints);
    lbl = new JLabel("False Positives:");
    lbl.setBackground(FALSE_POSITIVE_BG);
    lbl.setOpaque(true);
    resultsPane.add(lbl, constraints);

    //COLUMN 1
    constraints.gridx = 1;
    correctLbl = new JLabel("0");
    resultsPane.add(correctLbl, constraints);
    partiallyCorrectLbl = new JLabel("0");
    resultsPane.add(partiallyCorrectLbl, constraints);
    missingLbl = new JLabel("0");
    resultsPane.add(missingLbl, constraints);
    falsePozLbl = new JLabel("0");
    resultsPane.add(falsePozLbl, constraints);

    //COLMUN 2
    constraints.gridx = 2;
    constraints.insets = new Insets(4, 30, 4, 4);
    resultsPane.add(Box.createGlue());
    lbl = new JLabel("Strict:");
    resultsPane.add(lbl, constraints);
    lbl = new JLabel("Lenient:");
    resultsPane.add(lbl, constraints);
    lbl = new JLabel("Average:");
    resultsPane.add(lbl, constraints);

    //COLMUN 3
    constraints.gridx = 3;
    constraints.insets = new Insets(4, 4, 4, 4);
    lbl = new JLabel("Recall");
    resultsPane.add(lbl, constraints);
    recallStrictLbl = new JLabel("0.0000");
    resultsPane.add(recallStrictLbl, constraints);
    recallLenientLbl = new JLabel("0.0000");
    resultsPane.add(recallLenientLbl, constraints);
    recallAveLbl = new JLabel("0.0000");
    resultsPane.add(recallAveLbl, constraints);

    //COLMUN 4
    constraints.gridx = 4;
    lbl = new JLabel("Precision");
    resultsPane.add(lbl, constraints);
    precisionStrictLbl = new JLabel("0.0000");
    resultsPane.add(precisionStrictLbl, constraints);
    precisionLenientLbl = new JLabel("0.0000");
    resultsPane.add(precisionLenientLbl, constraints);
    precisionAveLbl = new JLabel("0.0000");
    resultsPane.add(precisionAveLbl, constraints);

    //COLMUN 5
    constraints.gridx = 5;
    lbl = new JLabel("F-Measure");
    resultsPane.add(lbl, constraints);
    fmeasureStrictLbl = new JLabel("0.0000");
    resultsPane.add(fmeasureStrictLbl, constraints);
    fmeasureLenientLbl = new JLabel("0.0000");
    resultsPane.add(fmeasureLenientLbl, constraints);
    fmeasureAveLbl = new JLabel("0.0000");
    resultsPane.add(fmeasureAveLbl, constraints);

    //COLMUN 6
    constraints.gridx = 6;
    htmlExportAction = new HTMLExportAction();
    htmlExportAction.setEnabled(false);
    htmlExportBtn = new JButton(htmlExportAction);
    htmlExportBtn.setToolTipText(
      "Use first the \"Compute Differences\" button.");
    constraints.gridwidth = 2;
    resultsPane.add(htmlExportBtn, constraints);
    constraints.gridwidth = 1;
    constraints.gridy = 2;
    resultsPane.add(new JLabel("Destination:"), constraints);
    constraints.gridy = GridBagConstraints.RELATIVE;
    moveToConsensusASAction = new MoveToConsensusASAction();
    moveToConsensusASAction.setEnabled(false);
    moveToConsensusBtn = new JButton(moveToConsensusASAction);
    constraints.gridwidth = 2;
    resultsPane.add(moveToConsensusBtn, constraints);
    constraints.gridwidth = 1;

    //COLMUN 7
    constraints.gridx = 7;
    constraints.gridy = 2;
    consensusASTextField = new JTextField("consensus", 10);
    consensusASTextField.setToolTipText(
      "Annotation set name where to move the selected annotations");
    constraints.fill = GridBagConstraints.HORIZONTAL;
    resultsPane.add(consensusASTextField, constraints);
    constraints.fill = GridBagConstraints.NONE;

    //Finished building the results pane
    //Add it to the dialog

    resultsPane.setBackground(Color.red);

    //ROW 4 - the results
    constraints.gridy = 4;
    constraints.gridx = 0;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.gridwidth = 8;
    constraints.gridheight = 1;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    getContentPane().add(resultsPane, constraints);

    // ROW 5
    constraints.gridy = 5;
    // status bar
    statusLabel = new JLabel("Choose two annotation sets to compare");
    constraints.gridx = 0;
    constraints.gridwidth = 7;
    constraints.anchor = GridBagConstraints.SOUTHWEST;
    getContentPane().add(statusLabel, constraints);

    // the progress bar
    progressBar = new JProgressBar();
    constraints.gridx = 7;
    constraints.gridwidth = 1;
    constraints.anchor = GridBagConstraints.SOUTHEAST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    getContentPane().add(progressBar, constraints);

    //set the colours
    Color background = diffTable.getBackground();
    getContentPane().setBackground(background);
    scroller.setBackground(background);
    scroller.getViewport().setBackground(background);
    resultsPane.setBackground(background);

    featureslistModel = new DefaultListModel();
    featuresList = new JList(featureslistModel);
    featuresList.
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    keySetCombo.requestFocusInWindow();
  }

  protected void initListeners(){

    addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
        new CloseAction().actionPerformed(null);
      }
    });

    keyDocCombo.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        int keyDocSelectedIndex = keyDocCombo.getSelectedIndex();
        if (keyDocSelectedIndex == -1) { return; }
        Document newDoc = (Document)documents.get(keyDocSelectedIndex);
        if(keyDoc != newDoc){
          pairings.clear();
          diffTableModel.fireTableDataChanged();
          moveToConsensusASAction.setEnabled(false);
          keyDoc = newDoc;
          keySets = new ArrayList<AnnotationSet>();
          List<String> keySetNames = new ArrayList<String>();
          keySets.add(keyDoc.getAnnotations());
          keySetNames.add("[Default set]");

          if(keyDoc.getNamedAnnotationSets() != null) {
            for (Object o : keyDoc.getNamedAnnotationSets().keySet()) {
              String name = (String) o;
              keySetNames.add(name);
              keySets.add(keyDoc.getAnnotations(name));
            }
          }
          keySetCombo.setModel(new DefaultComboBoxModel(keySetNames.toArray()));
          if(!keySetNames.isEmpty())keySetCombo.setSelectedIndex(0);
        }
      }
    });

    resDocCombo.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        int resDocSelectedIndex = resDocCombo.getSelectedIndex();
        if (resDocSelectedIndex == -1) { return; }
        Document newDoc = (Document)documents.get(resDocSelectedIndex);
        if(resDoc != newDoc){
          resDoc = newDoc;
          pairings.clear();
          diffTableModel.fireTableDataChanged();
          moveToConsensusASAction.setEnabled(false);
          resSets = new ArrayList<AnnotationSet>();
          List<String> resSetNames = new ArrayList<String>();
          resSets.add(resDoc.getAnnotations());
          resSetNames.add("[Default set]");
          if(resDoc.getNamedAnnotationSets() != null) {
            for (Object o : resDoc.getNamedAnnotationSets().keySet()) {
              String name = (String) o;
              resSetNames.add(name);
              resSets.add(resDoc.getAnnotations(name));
            }
          }
          resSetCombo.setModel(new DefaultComboBoxModel(resSetNames.toArray()));
          if(!resSetNames.isEmpty())resSetCombo.setSelectedIndex(0);
        }
      }
    });

    /**
     * This populates the types combo when set selection changes
     */
    ActionListener setComboActionListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        keySet = keySets == null || keySets.isEmpty()?
                 null : keySets.get(keySetCombo.getSelectedIndex());
        resSet = resSets == null || resSets.isEmpty()?
                null : resSets.get(resSetCombo.getSelectedIndex());
        Set<String> keyTypes = (keySet == null || keySet.isEmpty()) ?
                new HashSet<String>() : keySet.getAllTypes();
        Set<String> resTypes = (resSet == null || resSet.isEmpty()) ?
                new HashSet<String>() : resSet.getAllTypes();
        Set<String> types = new HashSet<String>(keyTypes);
        types.retainAll(resTypes);
        List<String> typesList = new ArrayList<String>(types);
        Collections.sort(typesList);
        annTypeCombo.setModel(new DefaultComboBoxModel(typesList.toArray()));
        if(typesList.size() > 0) {
          annTypeCombo.setSelectedIndex(0);
          diffAction.setEnabled(true);
          doDiffBtn.setToolTipText(
                  (String)diffAction.getValue(Action.SHORT_DESCRIPTION));
        } else {
          diffAction.setEnabled(false);
          doDiffBtn.setToolTipText("Choose two annotation sets "
                  +"that have at least one annotation type in common.");
        }
      }
    };
    keySetCombo.addActionListener(setComboActionListener);

    resSetCombo.addActionListener(setComboActionListener);

    someFeaturesBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if(someFeaturesBtn.isSelected()){
          if(keySet == null || keySet.isEmpty() ||
                  annTypeCombo.getSelectedItem() == null) return;
          Iterator<Annotation> annIter = keySet.
              get((String)annTypeCombo.getSelectedItem()).iterator();
          Set featureSet = new HashSet();
          while(annIter.hasNext()){
            Annotation ann = annIter.next();
            Map someFeatures = ann.getFeatures();
            if(someFeatures != null) featureSet.addAll(someFeatures.keySet());
          }
          List featureLst = new ArrayList(featureSet);
          Collections.sort(featureLst);
          featureslistModel.clear();
          Iterator featIter = featureLst.iterator();
          int index = 0;
          while(featIter.hasNext()){
            String aFeature = (String)featIter.next();
            featureslistModel.addElement(aFeature);
            if(significantFeatures.contains(aFeature))
              featuresList.addSelectionInterval(index, index);
            index ++;
          }
           int ret = JOptionPane.showConfirmDialog(AnnotationDiffGUI.this,
                  new JScrollPane(featuresList),
                  "Select features",
                  JOptionPane.OK_CANCEL_OPTION,
                  JOptionPane.QUESTION_MESSAGE);
           if(ret == JOptionPane.OK_OPTION){
             significantFeatures.clear();
             int[] selIdxs = featuresList.getSelectedIndices();
             for(int i = 0; i < selIdxs.length; i++){
               significantFeatures.add(featureslistModel.get(selIdxs[i]));
             }
           }
        }
      }
    });

    diffTableModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(javax.swing.event.TableModelEvent e) {
        if (diffTableModel.getRowCount() > 0) {
          htmlExportAction.setEnabled(true);
          htmlExportBtn.setToolTipText(
                  (String)htmlExportAction.getValue(Action.SHORT_DESCRIPTION));
        } else {
          htmlExportAction.setEnabled(false);
          htmlExportBtn.setToolTipText(
            "Use first the \"Compute Differences\" button.");
        }
      }
    });

    // inverse state of selected checkboxes when Space key is pressed
    diffTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_SPACE
          || !(diffTable.isColumnSelected(DiffTableModel.COL_KEY_MOVE)
            || diffTable.isColumnSelected(DiffTableModel.COL_RES_MOVE))) {
          return;
        }
        e.consume(); // disable normal behavior of Space key in a table
        int[] cols = {DiffTableModel.COL_KEY_MOVE, DiffTableModel.COL_RES_MOVE};
        for (int col : cols) {
          for (int row : diffTable.getSelectedRows()) {
            if (diffTable.isCellSelected(row, col)
             && diffTable.isCellEditable(row, col)) {
              diffTable.setValueAt(
                !(Boolean)diffTable.getValueAt(row, col), row, col);
              diffTableModel.fireTableCellUpdated(row, col);
            }
          }
        }
      }
    });

    // revert to default name if the field is empty and lost focus
    consensusASTextField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        String destination = consensusASTextField.getText().trim();
        if (destination.length() == 0) {
          consensusASTextField.setText("consensus");
        }
        if (keyDoc.getAnnotationSetNames().contains(destination)) {
          statusLabel.setText("Be careful, the annotation set " + destination
            + " already exists.");
        }
      }
    });

    // update the document lists when adding/removing documents in GATE
    Gate.getCreoleRegister().addCreoleListener(new CreoleListener() {
      public void resourceLoaded(CreoleEvent e) {
        if (e.getResource() instanceof Document) {
          populateGUI();
        }
      }
      public void resourceUnloaded(CreoleEvent e) {
        if (e.getResource() instanceof Document) {
          populateGUI();
        }
      }
      public void datastoreOpened(CreoleEvent e) {
      }
      public void datastoreCreated(CreoleEvent e) {
      }
      public void datastoreClosed(CreoleEvent e) {
      }
      public void resourceRenamed(Resource resource, String oldName, String newName) {
        if (resource instanceof Document) {
          populateGUI();
        }
      }
    });
  }

  public void pack(){
    super.pack();
    // add some space
    setSize(getWidth() + 200, getHeight() + 200);
  }

  protected void populateGUI(){
    try{
      documents = Gate.getCreoleRegister().getAllInstances("gate.Document");
    }catch(GateException ge){
      throw new GateRuntimeException(ge);
    }
    List<String> documentNames = new ArrayList<String>(documents.size());
    for(Resource document : documents) {
      documentNames.add(document.getName());
    }
    Object keyDocSelectedItem = keyDocCombo.getSelectedItem();
    Object resDocSelectedItem = resDocCombo.getSelectedItem();
    keyDocCombo.setModel(new DefaultComboBoxModel(documentNames.toArray()));
    resDocCombo.setModel(new DefaultComboBoxModel(documentNames.toArray()));
    if(!documents.isEmpty()){
      keyDocCombo.setSelectedItem(keyDocSelectedItem);
      if (keyDocCombo.getSelectedIndex() == -1) {
        keyDocCombo.setSelectedIndex(0);
      }
      resDocCombo.setSelectedItem(resDocSelectedItem);
      if (resDocCombo.getSelectedIndex() == -1) {
        resDocCombo.setSelectedIndex(0);
      }
    } else {
      statusLabel.setText("You must load at least one document in GATE");
    }
  }

  protected class DiffAction extends AbstractAction{
    public DiffAction(){
      super("<html>Compute<br>Differences</html>");
      putValue(SHORT_DESCRIPTION, "Performs comparisons between annotations");
      putValue(MNEMONIC_KEY, KeyEvent.VK_ENTER);
    }

    public void actionPerformed(ActionEvent evt){
      final int rowView = diffTable.getSelectedRow();
      final int colView = diffTable.getSelectedColumn();
      final int id = evt.getID();
      final String command = evt.getActionCommand();

      // animate the progress bar
      progressBar.setIndeterminate(true);

      // compute the differences
      SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        Set<Annotation> keys = new HashSet<Annotation>(
          keySet.get((String)annTypeCombo.getSelectedItem()));
        Set<Annotation> responses = new HashSet<Annotation>(
          resSet.get((String)annTypeCombo.getSelectedItem()));
        for (Annotation annotation : new ArrayList<Annotation>(keys)) {
          if (annotation.getFeatures().containsKey("anndiffstep")) {
            keys.remove(annotation); // previously processed by the user
          }
        }
        for (Annotation annotation : new ArrayList<Annotation>(responses)) {
          if (annotation.getFeatures().containsKey("anndiffstep")) {
            responses.remove(annotation); // previously processed by the user
          }
        }
        if(someFeaturesBtn.isSelected())
          differ.setSignificantFeaturesSet(significantFeatures);
        else if(allFeaturesBtn.isSelected())
          differ.setSignificantFeaturesSet(null);
        else differ.setSignificantFeaturesSet(new HashSet());
        pairings.clear();
        pairings.addAll(differ.calculateDiff(keys, responses));
        keyMoveValueRows.clear();
        keyMoveValueRows.addAll(Collections.nCopies(pairings.size(), false));
        resMoveValueRows.clear();
        resMoveValueRows.addAll(Collections.nCopies(pairings.size(), false));

        // update the GUI
        diffTableModel.fireTableDataChanged();
        correctLbl.setText(Integer.toString(differ.getCorrectMatches()));
        partiallyCorrectLbl.setText(
                Integer.toString(differ.getPartiallyCorrectMatches()));
        missingLbl.setText(Integer.toString(differ.getMissing()));
        falsePozLbl.setText(Integer.toString(differ.getSpurious()));
        NumberFormat f = NumberFormat.getInstance();
        f.setMaximumFractionDigits(4);
        f.setMinimumFractionDigits(2);
        recallStrictLbl.setText(f.format(differ.getRecallStrict()));
        recallLenientLbl.setText(f.format(differ.getRecallLenient()));
        recallAveLbl.setText(f.format(differ.getRecallAverage()));
        precisionStrictLbl.setText(f.format(differ.getPrecisionStrict()));
        precisionLenientLbl.setText(f.format(differ.getPrecisionLenient()));
        precisionAveLbl.setText(f.format(differ.getPrecisionAverage()));
        double weight = Double.parseDouble(weightTxt.getText());
        fmeasureStrictLbl.setText(f.format(differ.getFMeasureStrict(weight)));
        fmeasureLenientLbl.setText(f.format(differ.getFMeasureLenient(weight)));
        fmeasureAveLbl.setText(f.format(differ.getFMeasureAverage(weight)));
        moveToConsensusASAction.setEnabled(keyDoc.equals(resDoc));
        if (keyDoc.equals(resDoc)) {
          moveToConsensusBtn.setToolTipText((String)
            moveToConsensusASAction.getValue(Action.SHORT_DESCRIPTION));
        } else {
          moveToConsensusBtn.setToolTipText(
            "Key and response document must be the same");
        }
        if (!command.equals("setvalue") && !command.equals("move")) {
          statusLabel.setText(pairings.size() + " pairings have been found");
        }
        diffTable.requestFocusInWindow();
        //stop the progress bar
        progressBar.setIndeterminate(false);

        if (!command.equals("setvalue") && !command.equals("move")) { return; }

        SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          if (command.equals("setvalue")) {
          // select the cell containing the previously selected annotation
          for (int row = 0; row < diffTable.getRowCount(); row++) {
            AnnotationDiffer.Pairing pairing =
              pairings.get(diffTable.rowViewToModel(row));
            if ((pairing.getKey() != null
               && pairing.getKey().getId() == id)
             || (pairing.getResponse() != null
               && pairing.getResponse().getId() == id)) {
              diffTable.changeSelection(row, colView, false, false);
              break;
            }
          }
          } else if (command.equals("move")) { // select the previously selected cell
             diffTable.changeSelection(rowView, colView, false, false);
          }
          SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            diffTable.scrollRectToVisible(diffTable.getCellRect(
              diffTable.getSelectedRow(), diffTable.getSelectedColumn(), true));
          }});
        }});
      }});
    }
  }

  /**
   * Move selected annotations to the destination annotation set.
   */
  protected class MoveToConsensusASAction extends AbstractAction {
    public MoveToConsensusASAction(){
      super("Move selected annotations");
      putValue(SHORT_DESCRIPTION,
        "<html>Move selected annotations to the destination annotation set" +
          "<br>and hide their paired annotations if not moved.</html>");
      putValue(MNEMONIC_KEY, KeyEvent.VK_RIGHT);
    }
    public void actionPerformed(ActionEvent evt){
      String step = (String) keyDoc.getFeatures().get("anndiffsteps");
      if (step == null) { step = "0"; }
      AnnotationSet destination =
        keyDoc.getAnnotations(consensusASTextField.getText().trim());
      AnnotationSet keyAS =
        keyDoc.getAnnotations((String)keySetCombo.getSelectedItem());
      AnnotationSet responseAS =
        resDoc.getAnnotations((String)resSetCombo.getSelectedItem());
      int countMoved = 0, countMarked = 0;
      for (int row = 0; row < pairings.size(); row++) {
        if (keyMoveValueRows.get(row)) {
          Annotation key = pairings.get(row).getKey();
          key.getFeatures().put("anndiffsource", keySetCombo.getSelectedItem());
          key.getFeatures().put("anndiffstep", step);
          destination.add(key); // move the key annotation
          keyAS.remove(key);
          countMoved++;
          if (pairings.get(row).getResponse() != null
           && !resMoveValueRows.get(row)) { // mark the response annotation
            pairings.get(row).getResponse().getFeatures().put("anndiffstep", step);
            countMarked++;
          }
        }
        if (resMoveValueRows.get(row)) {
          Annotation response = pairings.get(row).getResponse();
          response.getFeatures().put("anndiffsource", resSetCombo.getSelectedItem());
          response.getFeatures().put("anndiffstep", step);
          destination.add(response); // move the response annotation
          responseAS.remove(response);
          countMoved++;
          if (pairings.get(row).getKey() != null
         && !keyMoveValueRows.get(row)) { // mark the key annotation
            pairings.get(row).getKey().getFeatures().put("anndiffstep", step);
            countMarked++;
          }
        }
      }
      if (countMoved > 0) {
        step = String.valueOf(Integer.valueOf(step) + 1);
      }
      keyDoc.getFeatures().put("anndiffsteps", step);
      diffAction.actionPerformed(new ActionEvent(this, -1, "move"));
      statusLabel.setText(countMoved +
        " annotations moved to " + consensusASTextField.getText().trim() +
        " and " + countMarked + " hidden");
    }
  }

  /**
   * @return a new file chooser if Gate is not used as a standalone application
   * otherwise the filechooser of the standalone application.
   */
  protected JFileChooser getFileChooser() {
    if(MainFrame.getFileChooser() != null) {
      return MainFrame.getFileChooser();
    } else {
      return new JFileChooser();
    }
  }

  protected class HTMLExportAction extends AbstractAction{
    public HTMLExportAction(){
      super("Export to HTML");
      putValue(SHORT_DESCRIPTION, "Export the results to HTML");
    }
    public void actionPerformed(ActionEvent evt){
      JFileChooser fileChooser = getFileChooser();
      File currentFile = fileChooser.getSelectedFile();
      String nl = Strings.getNl();
      String parent = (currentFile != null) ? currentFile.getParent() :
        System.getProperty("user.home");
      String fileName = (resDoc.getSourceUrl() != null) ?
              resDoc.getSourceUrl().getFile() :
              resDoc.getName();
      fileName += "_" + annTypeCombo.getSelectedItem().toString();
      fileName += ".html";
      fileChooser.setSelectedFile(new File(parent, fileName));
      ExtensionFileFilter fileFilter = new ExtensionFileFilter();
      fileFilter.addExtension(".html");
      fileChooser.setFileFilter(fileFilter);
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int res = fileChooser.showSaveDialog(AnnotationDiffGUI.this);
      if(res == JFileChooser.APPROVE_OPTION){
        File saveFile = fileChooser.getSelectedFile();
        try{
          Writer fw = new BufferedWriter(new FileWriter(saveFile));
          //write the header
          fw.write(HEADER_1);
          fw.write(resDoc.getName() + " " +
                  annTypeCombo.getSelectedItem().toString() +
                  " annotations");
          fw.write(HEADER_2 + nl);
          fw.write("<H2>Annotation Diff - comparing " +
                  annTypeCombo.getSelectedItem().toString() +
                  " annotations" + "</H2>");
          fw.write("<TABLE cellpadding=\"5\" border=\"0\"");
          fw.write(nl);
          fw.write("<TR>" + nl);
          fw.write("\t<TH align=\"left\">&nbsp;</TH>" + nl);
          fw.write("\t<TH align=\"left\">Document</TH>" + nl);
          fw.write("\t<TH align=\"left\">Annotation Set</TH>" + nl);
          fw.write("</TR>" + nl);

          fw.write("<TR>" + nl);
          fw.write("\t<TH align=\"left\">Key</TH>" + nl);
          fw.write("\t<TD align=\"left\">" + keyDoc.getName() + "</TD>" + nl);
          fw.write("\t<TD align=\"left\">" + keySet.getName() + "</TD>" + nl);
          fw.write("</TR>" + nl);
          fw.write("<TR>" + nl);
          fw.write("\t<TH align=\"left\">Response</TH>" + nl);
          fw.write("\t<TD align=\"left\">" + resDoc.getName() + "</TD>" + nl);
          fw.write("\t<TD align=\"left\">" + resSet.getName() + "</TD>" + nl);
          fw.write("</TR>" + nl);
          fw.write("</TABLE>" + nl);
          fw.write("<BR><BR><BR>" + nl);
          //write the results
          java.text.NumberFormat format = java.text.NumberFormat.getInstance();
          format.setMaximumFractionDigits(4);
          fw.write("Recall: " + format.format(differ.getRecallStrict()) + "<br>" + nl);
          fw.write("Precision: " + format.format(differ.getPrecisionStrict()) + "<br>" + nl);
          fw.write("F-measure: " + format.format(differ.getFMeasureStrict(1)) + "<br>" + nl);
          fw.write("<br>");
          fw.write("Correct matches: " + differ.getCorrectMatches() + "<br>" + nl);
          fw.write("Partially Correct matches: " +
              differ.getPartiallyCorrectMatches() + "<br>" + nl);
          fw.write("Missing: " + differ.getMissing() + "<br>" + nl);
          fw.write("False positives: " + differ.getSpurious() + "<br>" + nl);
//          fw.write("<hr>" + nl);
          fw.write(HEADER_3 + nl + "<TR>" + nl);
          int maxColIdx = diffTable.getColumnCount() - 1;
          for(int col = 0; col <= maxColIdx; col++){
            fw.write("\t<TH align=\"left\">" + diffTable.getColumnName(col) +
                    "</TH>" + nl);
          }
          fw.write("</TR>");
          int rowCnt = diffTableModel.getRowCount();
          for(int row = 0; row < rowCnt; row ++){
            fw.write("<TR>");
            for(int col = 0; col <= maxColIdx; col++){
              Color bgCol = diffTableModel.getBackgroundAt(
                      diffTable.rowViewToModel(row),
                      diffTable.convertColumnIndexToModel(col));
              fw.write("\t<TD bgcolor=\"#" +
                      Integer.toHexString(bgCol.getRGB()).substring(2) +
                      "\">" +
                      diffTable.getValueAt(row, col) +
                      "</TD>" + nl);
            }
            fw.write("</TR>");
          }
          fw.write(FOOTER);
          fw.flush();
          fw.close();

        }catch(IOException ioe){
          JOptionPane.showMessageDialog(AnnotationDiffGUI.this, ioe.toString(),
                  "GATE", JOptionPane.ERROR_MESSAGE);
          ioe.printStackTrace();
        }
      }
    }

    static final String HEADER_1 = "<html><head><title>";
    static final String HEADER_2 = "</title></head><body>";
    static final String HEADER_3 = "<table cellpadding=\"0\" border=\"1\">";
    static final String FOOTER = "</table></body></html>";
  }

  protected class CloseAction extends AbstractAction {
    public CloseAction(){
      super("Close");
    }
    public void actionPerformed(ActionEvent evt){
      MainFrame.getGuiRoots().remove(AnnotationDiffGUI.this);
      dispose();
    }
  }

  protected class DiffTableCellRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column){
      int rowModel = diffTable.rowViewToModel(row);
      int colModel = diffTable.convertColumnIndexToModel(column);
      Component component;
      if (value instanceof Boolean) {
        component = new JCheckBox();
      } else {
        component = super.getTableCellRendererComponent(table, value,
          false, hasFocus, row, column);
      }
      if (pairings.size() == 0) { return component; }
      AnnotationDiffer.Pairing pairing = pairings.get(rowModel);
      component.setBackground(isSelected ? table.getSelectionBackground() :
        diffTableModel.getBackgroundAt(rowModel, column));
      component.setForeground(isSelected ? table.getSelectionForeground() :
        table.getForeground());
      // add tooltips for each cell, disable some checkboxes
      if (component instanceof JComponent) {
        String tip;
        try {
        switch (colModel){
          case DiffTableModel.COL_KEY_MOVE:
            tip = "Select this key annotation to move";
            if (pairing.getKey() == null) {
              tip = "There is no key annotation";
            }
            component.setEnabled(pairing.getKey() != null);
            ((JCheckBox)component).setSelected(keyMoveValueRows.get(rowModel));
            break;
          case DiffTableModel.COL_RES_MOVE:
            tip = "Select this response annotation to move";
            if (pairing.getResponse() == null) {
              tip = "There is no response annotation";
            }
            component.setEnabled(pairing.getResponse() != null);
            ((JCheckBox)component).setSelected(resMoveValueRows.get(rowModel));
             break;
          case DiffTableModel.COL_KEY_STRING:
            Annotation key = pairing.getKey();
            if (key == null) {
              tip = null;
            } else { // reformat the text
              tip = keyDoc.getContent().getContent(
                  key.getStartNode().getOffset(),
                  key.getEndNode().getOffset()).toString();
              if (tip.length() > 1000) {
                tip = tip.substring(0, 1000 / 2) + "<br>...<br>"
                  + tip.substring(tip.length() - (1000 / 2));
              }
              tip = keyDoc.getContent().getContent(
                Math.max(0, key.getStartNode().getOffset()-30),
                Math.max(0, key.getStartNode().getOffset())).toString() +
                "<strong>" + tip + "</strong>" +
                keyDoc.getContent().getContent(
                Math.min(keyDoc.getContent().size(),
                  key.getEndNode().getOffset()),
                Math.min(keyDoc.getContent().size(),
                  key.getEndNode().getOffset()+30)).toString();
              tip = tip.replaceAll("\\s*\n\\s*", "<br>");
              tip = tip.replaceAll("\\s+", " ");
              tip = (tip.length() > 100) ?
                "<html><table width=\"500\" border=\"0\" cellspacing=\"0\">"
                    + "<tr><td>" + tip + "</td></tr></table></html>"
                : "<html>" + tip + "</html>";
            }
            break;
          case DiffTableModel.COL_MATCH: tip =
            "correct =, partial ~, mismatch <>, spurious ?-, missing -?";
            break;
          case DiffTableModel.COL_RES_STRING:
            Annotation response = pairing.getResponse();
            if (response == null) {
              tip = null;
            } else { // reformat the text
              tip = resDoc.getContent().getContent(
                  response.getStartNode().getOffset(),
                  response.getEndNode().getOffset()).toString();
              if (tip.length() > 1000) {
                tip = tip.substring(0, 1000 / 2) + "<br>...<br>"
                  + tip.substring(tip.length() - (1000 / 2));
              }
              tip = resDoc.getContent().getContent(
                Math.max(0, response.getStartNode().getOffset()-30),
                Math.max(0, response.getStartNode().getOffset())).toString() +
                "<strong>" + tip + "</strong>" +
                resDoc.getContent().getContent(
                Math.min(resDoc.getContent().size(),
                  response.getEndNode().getOffset()),
                Math.min(resDoc.getContent().size(),
                  response.getEndNode().getOffset()+30)).toString();
              tip = tip.replaceAll("\\s*\n\\s*", "<br>");
              tip = tip.replaceAll("\\s+", " ");
              tip = (tip.length() > 100) ?
                "<html><table width=\"500\" border=\"0\" cellspacing=\"0\">"
                    + "<tr><td>" + tip + "</td></tr></table></html>"
                : "<html>" + tip + "</html>";
            }
            break;
          default: tip = null;
        }
        } catch(InvalidOffsetException ioe){
          //this should never happen
          throw new GateRuntimeException(ioe);
        }
        ((JComponent)component).setToolTipText(tip);
      }
      return component;
    }
  }

  protected class DiffTableModel extends AbstractTableModel{

    public int getRowCount(){
      return pairings.size();
    }

    public Class getColumnClass(int columnIndex){
      switch (columnIndex){
        case COL_KEY_MOVE: return Boolean.class;
        case COL_RES_MOVE: return Boolean.class;
        default: return String.class;
      }
    }

    public int getColumnCount(){
      return COL_CNT;
    }

    public String getColumnName(int column){
      switch(column){
        case COL_KEY_MOVE: return "K";
        case COL_RES_MOVE: return "R";
        case COL_KEY_START: return "Start";
        case COL_KEY_END: return "End";
        case COL_KEY_STRING: return "Key";
        case COL_KEY_FEATURES: return "Features";
        case COL_MATCH: return "=?";
        case COL_RES_START: return "Start";
        case COL_RES_END: return "End";
        case COL_RES_STRING: return "Response";
        case COL_RES_FEATURES: return "Features";
        default: return "?";
      }
    }

    public Color getBackgroundAt(int row, int column){
      AnnotationDiffer.Pairing pairing = pairings.get(row);
      switch(pairing.getType()){
        case(AnnotationDiffer.CORRECT_TYPE): return diffTable.getBackground();
        case(AnnotationDiffer.PARTIALLY_CORRECT_TYPE): return PARTIALLY_CORRECT_BG;
        case(AnnotationDiffer.MISMATCH_TYPE):
          if(column < COL_MATCH) return MISSING_BG;
          else if(column > COL_MATCH) return FALSE_POSITIVE_BG;
          else return diffTable.getBackground();
        case(AnnotationDiffer.MISSING_TYPE): return MISSING_BG;
        case(AnnotationDiffer.SPURIOUS_TYPE): return FALSE_POSITIVE_BG;
        default: return diffTable.getBackground();
      }
//      
//      Color colKey = pairing.getType() == 
//          AnnotationDiffer.CORRECT_TYPE ?
//          diffTable.getBackground() :
//          (pairing.getType() == AnnotationDiffer.PARTIALLY_CORRECT_TYPE ?
//           PARTIALLY_CORRECT_BG : MISSING_BG);
//      if(pairing.getKey() == null) colKey = diffTable.getBackground();
//      
//      Color colRes = pairing.getType() == AnnotationDiffer.CORRECT_TYPE ?
//                     diffTable.getBackground() :
//                       (pairing.getType() == AnnotationDiffer.PARTIALLY_CORRECT_TYPE ?
//                       PARTIALLY_CORRECT_BG :
//                         FALSE_POSITIVE_BG);
//      if(pairing.getResponse() == null) colRes = diffTable.getBackground();
//      switch(column){
//        case COL_KEY_START: return colKey;
//        case COL_KEY_END: return colKey;
//        case COL_KEY_STRING: return colKey;
//        case COL_KEY_FEATURES: return colKey;
//        case COL_MATCH: return diffTable.getBackground();
//        case COL_RES_START: return colRes;
//        case COL_RES_END: return colRes;
//        case COL_RES_STRING: return colRes;
//        case COL_RES_FEATURES: return colRes;
//        default: return diffTable.getBackground();
//      }
    }

    public Object getValueAt(int row, int column){
      final int maxValueLength = 50;
      AnnotationDiffer.Pairing pairing = pairings.get(row);
      Annotation key = pairing.getKey();
      Annotation res = pairing.getResponse();

      switch(column){
        case COL_KEY_MOVE: return keyMoveValueRows.get(row);
        case COL_RES_MOVE: return resMoveValueRows.get(row);
        case COL_KEY_START: return key == null ? "" :
          key.getStartNode().getOffset().toString();
        case COL_KEY_END: return key == null ? "" :
          key.getEndNode().getOffset().toString();
        case COL_KEY_STRING:
          String keyStr = "";
          try{
            if(key != null && keyDoc != null){
              keyStr = keyDoc.getContent().getContent(
                key.getStartNode().getOffset(),
                key.getEndNode().getOffset()).toString();
            }
          }catch(InvalidOffsetException ioe){
            //this should never happen
            throw new GateRuntimeException(ioe);
          }
          // cut annotated text in the middle if too long
          if (keyStr.length() > maxValueLength) {
            keyStr = keyStr.substring(0, maxValueLength / 2) + "..."
              + keyStr.substring(keyStr.length() - (maxValueLength / 2));
          }
          return keyStr;
        case COL_KEY_FEATURES: return key == null ? "" :
          key.getFeatures().toString();
        case COL_MATCH: return matchLabel[pairing.getType()];
        case COL_RES_START: return res == null ? "" :
          res.getStartNode().getOffset().toString();
        case COL_RES_END: return res == null ? "" :
          res.getEndNode().getOffset().toString();
        case COL_RES_STRING:
          String resStr = "";
          try{
            if(res != null && resDoc != null){
              resStr = resDoc.getContent().getContent(
                res.getStartNode().getOffset(),
                res.getEndNode().getOffset()).toString();
            }
          }catch(InvalidOffsetException ioe){
            //this should never happen
            throw new GateRuntimeException(ioe);
          }
          if (resStr.length() > maxValueLength) {
            resStr = resStr.substring(0, maxValueLength / 2) + "..."
              + resStr.substring(resStr.length() - (maxValueLength / 2));
          }
          return resStr;
        case COL_RES_FEATURES: return res == null ? "" :
          res.getFeatures().toString();
        default: return "?";
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      if (pairings.size() == 0) { return false; }
      AnnotationDiffer.Pairing pairing = pairings.get(rowIndex);
      return (columnIndex != COL_KEY_MOVE || pairing.getKey() != null)
          && (columnIndex != COL_RES_MOVE || pairing.getResponse() != null)
          && (columnIndex != COL_KEY_START || pairing.getKey() != null)
          && (columnIndex != COL_KEY_END || pairing.getKey() != null)
          &&  columnIndex != COL_KEY_STRING
          && (columnIndex != COL_KEY_FEATURES || pairing.getKey() != null)
          &&  columnIndex != COL_MATCH
          && (columnIndex != COL_RES_START || pairing.getResponse() != null)
          && (columnIndex != COL_RES_END || pairing.getResponse() != null)
          &&  columnIndex != COL_RES_STRING
          && (columnIndex != COL_RES_FEATURES || pairing.getResponse() != null);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
      AnnotationDiffer.Pairing pairing = pairings.get(rowIndex);
      AnnotationSet keyAS =
        keyDoc.getAnnotations((String)keySetCombo.getSelectedItem());
      AnnotationSet responseAS =
        resDoc.getAnnotations((String)resSetCombo.getSelectedItem());
      Annotation key = pairing.getKey();
      Annotation res = pairing.getResponse();
      int id = -1;
      String keysValues;
      FeatureMap features;
      try {
      switch(columnIndex){
        case COL_KEY_MOVE:
          keyMoveValueRows.set(rowIndex, (Boolean)aValue);
          break;
        case COL_RES_MOVE:
          resMoveValueRows.set(rowIndex, (Boolean)aValue);
          break;
        case COL_KEY_START:
          if (Long.valueOf((String) aValue)
            .equals(key.getStartNode().getOffset())) { break; }
          id = keyAS.add(Long.valueOf((String)aValue),
            key.getEndNode().getOffset(), key.getType(), key.getFeatures());
          keyAS.remove(key);
          break;
        case COL_KEY_END:
          if (Long.valueOf((String) aValue)
            .equals(key.getEndNode().getOffset())) { break; }
          id = keyAS.add(key.getStartNode().getOffset(),
            Long.valueOf((String)aValue), key.getType(), key.getFeatures());
          keyAS.remove(key);
          break;
        case COL_KEY_FEATURES:
          keysValues = (String) aValue;
          keysValues = keysValues.replaceAll("\\s+", " ").replaceAll("[}{]", "");
          features = Factory.newFeatureMap();
          for (String keyValue : keysValues.split(",")) {
            String[] keyOrValue = keyValue.split("=");
            if (keyOrValue.length != 2) { throw new NumberFormatException(); }
              features.put(keyOrValue[0].trim(), keyOrValue[1].trim());
          }
          key.setFeatures(features);
          break;
        case COL_RES_START:
          if (Long.valueOf((String) aValue)
            .equals(res.getStartNode().getOffset())) { break; }
          id = responseAS.add(Long.valueOf((String)aValue),
            res.getEndNode().getOffset(), res.getType(), res.getFeatures());
          responseAS.remove(res);
          break;
        case COL_RES_END:
          if (Long.valueOf((String) aValue)
            .equals(res.getEndNode().getOffset())) { break; }
          id = responseAS.add(res.getStartNode().getOffset(),
            Long.valueOf((String)aValue), res.getType(), res.getFeatures());
          responseAS.remove(res);
          break;
        case COL_RES_FEATURES:
          keysValues = (String) aValue;
          keysValues = keysValues.replaceAll("\\s+", " ").replaceAll("[}{]", "");
          features = Factory.newFeatureMap();
          for (String keyValue : keysValues.split(",")) {
            String[] keyOrValue = keyValue.split("=");
            if (keyOrValue.length != 2) { throw new NumberFormatException(); }
            features.put(keyOrValue[0].trim(), keyOrValue[1].trim());
          }
          res.setFeatures(features);
          break;
      }
      }catch(InvalidOffsetException e){
        statusLabel.setText(
          "This offset is incorrect. No changes have been made.");
        return;
      }catch(NumberFormatException e) {
        statusLabel.setText(
          "The format is incorrect. No changes have been made.");
        return;
      }
      if (id != -1) {
        // compute again the differences
        diffAction.actionPerformed(new ActionEvent(this, id, "setvalue"));
      }
    }

    protected static final int COL_CNT = 11;
    protected static final int COL_KEY_MOVE = 0;
    protected static final int COL_RES_MOVE = 1;
    protected static final int COL_KEY_START = 2;
    protected static final int COL_KEY_END = 3;
    protected static final int COL_KEY_STRING = 4;
    protected static final int COL_KEY_FEATURES = 5;
    protected static final int COL_MATCH = 6;
    protected static final int COL_RES_START = 7;
    protected static final int COL_RES_END = 8;
    protected static final int COL_RES_STRING = 9;
    protected static final int COL_RES_FEATURES = 10;
  } // protected class DiffTableModel extends AbstractTableModel

  protected AnnotationDiffer differ;
  protected List<AnnotationDiffer.Pairing> pairings;
  protected List<Boolean> keyMoveValueRows;
  protected List<Boolean> resMoveValueRows;
  protected Document keyDoc;
  protected Document resDoc;
  protected Set significantFeatures;
  protected List<Resource> documents;
  protected List<AnnotationSet> keySets;
  protected List<AnnotationSet> resSets;
  protected AnnotationSet keySet;
  protected AnnotationSet resSet;
  protected HTMLExportAction htmlExportAction;
  protected DiffAction diffAction;
  protected MoveToConsensusASAction moveToConsensusASAction;

  protected JList featuresList;
  protected DefaultListModel featureslistModel;
  protected DiffTableModel diffTableModel;
  protected XJTable diffTable;
  protected JScrollPane scroller;
  protected JComboBox keyDocCombo;
  protected JComboBox keySetCombo;
  protected JComboBox annTypeCombo;
  protected JComboBox resDocCombo;
  protected JComboBox resSetCombo;
  protected JLabel statusLabel;
  protected JProgressBar progressBar;

  protected JRadioButton allFeaturesBtn;
  protected JRadioButton someFeaturesBtn;
  protected JRadioButton noFeaturesBtn;
  protected JTextField weightTxt;
  protected JButton doDiffBtn;
  protected JTextField consensusASTextField;
  protected JButton moveToConsensusBtn;
  protected JButton htmlExportBtn;

  protected JPanel resultsPane;
  protected JLabel correctLbl;
  protected JLabel partiallyCorrectLbl;
  protected JLabel missingLbl;
  protected JLabel falsePozLbl;
  protected JLabel recallStrictLbl;
  protected JLabel precisionStrictLbl;
  protected JLabel fmeasureStrictLbl;
  protected JLabel recallLenientLbl;
  protected JLabel precisionLenientLbl;
  protected JLabel fmeasureLenientLbl;
  protected JLabel recallAveLbl;
  protected JLabel precisionAveLbl;
  protected JLabel fmeasureAveLbl;

  protected static final Color PARTIALLY_CORRECT_BG = new Color(173,215,255);
  protected static final Color MISSING_BG = new Color(255,173,181);
  protected static final Color FALSE_POSITIVE_BG = new Color(255,231,173);
  protected static final String[] matchLabel;
  static{
    matchLabel = new String[5];
    matchLabel[AnnotationDiffer.CORRECT_TYPE] = "=";
    matchLabel[AnnotationDiffer.PARTIALLY_CORRECT_TYPE] = "~";
    matchLabel[AnnotationDiffer.MISMATCH_TYPE] = "<>";
    matchLabel[AnnotationDiffer.SPURIOUS_TYPE] = "?-";
    matchLabel[AnnotationDiffer.MISSING_TYPE] = "-?";
  }
}
