/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
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
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FileChooserUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import gate.*;
import gate.Annotation;
import gate.Document;
import gate.swing.XJTable;
import gate.util.*;

/**
 */
public class AnnotationDiffGUI extends JFrame{

  public AnnotationDiffGUI(String title){
    super(title);
    initLocalData();
    initGUI();
    initListeners();
    populateGUI();
  }

  protected void initLocalData(){
    differ = new AnnotationDiffer();
    pairings = new ArrayList();
    significantFeatures = new HashSet();
    keyDoc = null;
    resDoc = null;
  }


  protected void initGUI(){
    getContentPane().setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    //defaults
    constraints.gridy = 0;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(2,4,2,4);
    //ROW 0
    constraints.gridx = 1;
    getContentPane().add(new JLabel("Document"), constraints);
    constraints.gridx = GridBagConstraints.RELATIVE;
    getContentPane().add(new JLabel("Annotation Set"), constraints);
    //ROW 1
    constraints.gridy = 1;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridwidth = 1;
    getContentPane().add(new JLabel("Key:"), constraints);
    keyDocCombo = new JComboBox();
    getContentPane().add(keyDocCombo, constraints);
    keySetCombo = new JComboBox();
    getContentPane().add(keySetCombo, constraints);
    getContentPane().add(new JLabel("Annotation Type:"), constraints);
    annTypeCombo = new JComboBox();
    constraints.gridwidth = 3;
    getContentPane().add(annTypeCombo, constraints);
    constraints.gridwidth = 1;
    getContentPane().add(new JLabel("F-Measure Weight"), constraints);
    constraints.gridheight = 2;
    doDiffBtn = new JButton(new DiffAction());
    getContentPane().add(doDiffBtn, constraints);
    constraints.weightx = 1;
    getContentPane().add(Box.createHorizontalGlue(), constraints);
    //ROW 2
    constraints.gridy = 2;
    constraints.gridx = 0;
    constraints.gridheight = 1;
    constraints.weightx = 0;
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
    weightTxt = new JTextField("1.00");
    getContentPane().add(weightTxt, constraints);
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

    /* Niraj */
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

    diffTable.setComparator(diffTableModel.COL_KEY_START, startEndComparator);
    diffTable.setComparator(diffTableModel.COL_KEY_END, startEndComparator);
    diffTable.setComparator(diffTableModel.COL_RES_START, startEndComparator);
    diffTable.setComparator(diffTableModel.COL_RES_END, startEndComparator);
    /* End */

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
    lbl.setBackground(FALSE_POZITIVE_BG);
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
    resultsPane.add(new JButton(new HTMLExportAction()), constraints);

    //Finished building the results pane
    //Add it to the dialog


    //ROW 4 - the results
    constraints.gridy = 4;
    constraints.gridx = 0;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.gridwidth = 9;
    constraints.gridheight = 1;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    getContentPane().add(resultsPane, constraints);


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
  }

  protected void initListeners(){
    keyDocCombo.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        Document newDoc = (Document)documents.get(keyDocCombo.getSelectedIndex());
        if(keyDoc != newDoc){
          pairings.clear();
          diffTableModel.fireTableDataChanged();
          keyDoc = newDoc;
          keySets = new ArrayList();
          List keySetNames = new ArrayList();
          keySets.add(keyDoc.getAnnotations());
          keySetNames.add("[Default set]");
          Iterator setIter = keyDoc.getNamedAnnotationSets().keySet().iterator();
          while(setIter.hasNext()){
            String name = (String)setIter.next();
            keySetNames.add(name);
            keySets.add(keyDoc.getAnnotations(name));
          }
          keySetCombo.setModel(new DefaultComboBoxModel(keySetNames.toArray()));
          if(!keySetNames.isEmpty())keySetCombo.setSelectedIndex(0);

        }
      }
    });

    resDocCombo.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        Document newDoc = (Document)documents.get(resDocCombo.getSelectedIndex());
        if(resDoc != newDoc){
          resDoc = newDoc;
          pairings.clear();
          diffTableModel.fireTableDataChanged();
          resSets = new ArrayList();
          List resSetNames = new ArrayList();
          resSets.add(resDoc.getAnnotations());
          resSetNames.add("[Default set]");
          Iterator setIter = resDoc.getNamedAnnotationSets().keySet().iterator();
          while(setIter.hasNext()){
            String name = (String)setIter.next();
            resSetNames.add(name);
            resSets.add(resDoc.getAnnotations(name));
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
                 null :
                (AnnotationSet)keySets.get(keySetCombo.getSelectedIndex());
        resSet = resSets == null || resSets.isEmpty()?
                null :
               (AnnotationSet)resSets.get(resSetCombo.getSelectedIndex());
        Set keyTypes = (keySet == null || keySet.isEmpty()) ?
                new HashSet() : keySet.getAllTypes();
        Set resTypes = (resSet == null || resSet.isEmpty()) ?
                new HashSet() : resSet.getAllTypes();
        Set types = new HashSet(keyTypes);
        types.retainAll(resTypes);
        List typesList = new ArrayList(types);
        Collections.sort(typesList);
        annTypeCombo.setModel(new DefaultComboBoxModel(typesList.toArray()));
        if(typesList.size() > 0) annTypeCombo.setSelectedIndex(0);
      }
    };
    keySetCombo.addActionListener(setComboActionListener);

    resSetCombo.addActionListener(setComboActionListener);

    someFeaturesBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        if(someFeaturesBtn.isSelected()){
          if(keySet == null || keySet.isEmpty() ||
                  annTypeCombo.getSelectedItem() == null) return;
          Iterator annIter = keySet.
              get((String)annTypeCombo.getSelectedItem()).iterator();
          Set featureSet = new HashSet();
          while(annIter.hasNext()){
            Annotation ann = (Annotation)annIter.next();
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
  }


  public void pack(){
    super.pack();

    setSize(getWidth(), getHeight() + 100);
  }
  protected void populateGUI(){
    try{
      documents = Gate.getCreoleRegister().getAllInstances("gate.Document");
    }catch(GateException ge){
      throw new GateRuntimeException(ge);
    }
    List documentNames = new ArrayList(documents.size());
    for(int i =0; i < documents.size(); i++){
      Document doc = (Document)documents.get(i);
      documentNames.add(doc.getName());
    }
    keyDocCombo.setModel(new DefaultComboBoxModel(documentNames.toArray()));
    resDocCombo.setModel(new DefaultComboBoxModel(documentNames.toArray()));
    if(!documents.isEmpty()){
      keyDocCombo.setSelectedIndex(0);
      resDocCombo.setSelectedIndex(0);
    }
  }


  protected class DiffAction extends AbstractAction{
    public DiffAction(){
      super("Do Diff");
      putValue(SHORT_DESCRIPTION, "Performs the diff");
    }

    public void actionPerformed(ActionEvent evt){
      Set keys = keySet.get((String)annTypeCombo.getSelectedItem());
      Set responses = resSet.get((String)annTypeCombo.getSelectedItem());
      if(keys == null) keys = new HashSet();
      if(responses == null) responses = new HashSet();
      if(someFeaturesBtn.isSelected())
        differ.setSignificantFeaturesSet(significantFeatures);
      else if(allFeaturesBtn.isSelected())
        differ.setSignificantFeaturesSet(null);
      else differ.setSignificantFeaturesSet(new HashSet());
      pairings.clear();
      pairings.addAll(differ.calculateDiff(keys, responses));
      diffTableModel.fireTableDataChanged();
      correctLbl.setText(Integer.toString(differ.getCorrectMatches()));
      partiallyCorrectLbl.setText(
              Integer.toString(differ.getPartiallyCorrectMatches()));
      missingLbl.setText(Integer.toString(differ.getMissing()));
      falsePozLbl.setText(Integer.toString(differ.getSpurious()));

      NumberFormat formatter = NumberFormat.getInstance();
      formatter.setMaximumFractionDigits(4);
      formatter.setMinimumFractionDigits(2);
      recallStrictLbl.setText(formatter.format(differ.getRecallStrict()));
      recallLenientLbl.setText(formatter.format(differ.getRecallLenient()));
      recallAveLbl.setText(formatter.format(differ.getRecallAverage()));
      precisionStrictLbl.setText(formatter.format(differ.getPrecisionStrict()));
      precisionLenientLbl.setText(formatter.format(differ.getPrecisionLenient()));
      precisionAveLbl.setText(formatter.format(differ.getPrecisionAverage()));

      double weight = Double.parseDouble(weightTxt.getText());
      fmeasureStrictLbl.setText(formatter.format(differ.getFMeasureStrict(weight)));
      fmeasureLenientLbl.setText(formatter.format(differ.getFMeasureLenient(weight)));
      fmeasureAveLbl.setText(formatter.format(differ.getFMeasureAverage(weight)));
    }
  }

  protected class HTMLExportAction extends AbstractAction{
    public HTMLExportAction(){
      super("Export to HTML");
    }
    public void actionPerformed(ActionEvent evt){
      JFileChooser fileChooser = MainFrame.getFileChooser();
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
          //get a list of columns that need to be displayed
          int[] cols = new int[diffTableModel.getColumnCount()];
          int maxColIdx = -1;
          for(int i = 0; i < cols.length; i++){
            if(!diffTable.isColumnHidden(i)){
              maxColIdx ++;
              cols[maxColIdx] = i;
            }
          }
          fw.write(HEADER_3 + nl + "<TR>" + nl);
          for(int col = 0; col <= maxColIdx; col++){
            fw.write("\t<TH align=\"left\">" + diffTable.getColumnName(cols[col]) +
                    "</TH>" + nl);
          }
          fw.write("</TR>");
          int rowCnt = diffTableModel.getRowCount();
          for(int row = 0; row < rowCnt; row ++){
            fw.write("<TR>");
            for(int col = 0; col <= maxColIdx; col++){
              Color bgCol = diffTableModel.getBackgroundAt(
                      diffTable.rowViewToModel(row),
                      diffTable.convertColumnIndexToModel(cols[col]));
              fw.write("\t<TD bgcolor=\"#" +
                      Integer.toHexString(bgCol.getRGB()).substring(2) +
                      "\">" +
                      diffTable.getValueAt(row, cols[col]) +
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

  protected class DiffTableCellRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column){
      Component res = super.getTableCellRendererComponent(table,
              value, false, hasFocus, row, column);
      res.setBackground(isSelected ? table.getSelectionBackground() :
              diffTableModel.getBackgroundAt(diffTable.rowViewToModel(row),
                      column));
      res.setForeground(isSelected ? table.getSelectionForeground() :
        table.getForeground());
      return res;
    }
  }

  protected class DiffTableModel extends AbstractTableModel{
    public int getRowCount(){
      return pairings.size();
    }

    public Class getColumnClass(int columnIndex){
      return String.class;
    }

    public int getColumnCount(){
      return COL_CNT;
    }

    public String getColumnName(int column){
      switch(column){
        case COL_KEY_START: return "Start";
        case COL_KEY_END: return "End";
        case COL_KEY_STRING: return "Key";
        case COL_KEY_FEATURES: return "Features";
        case COL_MATCH: return "";
        case COL_RES_START: return "Start";
        case COL_RES_END: return "End";
        case COL_RES_STRING: return "Response";
        case COL_RES_FEATURES: return "Features";
        default: return "?";
      }
    }

    public Color getBackgroundAt(int row, int column){
      AnnotationDiffer.Pairing pairing =
        (AnnotationDiffer.Pairing)pairings.get(row);
      Color colKey = pairing.getType() == AnnotationDiffer.CORRECT ?
                     diffTable.getBackground() :
                       (pairing.getType() == AnnotationDiffer.PARTIALLY_CORRECT ?
                       PARTIALLY_CORRECT_BG :
                         MISSING_BG);
      if(pairing.getKey() == null) colKey = diffTable.getBackground();
      Color colRes = pairing.getType() == AnnotationDiffer.CORRECT ?
                     diffTable.getBackground() :
                       (pairing.getType() == AnnotationDiffer.PARTIALLY_CORRECT ?
                       PARTIALLY_CORRECT_BG :
                         FALSE_POZITIVE_BG);
      if(pairing.getResponse() == null) colRes = diffTable.getBackground();
      switch(column){
        case COL_KEY_START: return colKey;
        case COL_KEY_END: return colKey;
        case COL_KEY_STRING: return colKey;
        case COL_KEY_FEATURES: return colKey;
        case COL_MATCH: return diffTable.getBackground();
        case COL_RES_START: return colRes;
        case COL_RES_END: return colRes;
        case COL_RES_STRING: return colRes;
        case COL_RES_FEATURES: return colRes;
        default: return diffTable.getBackground();
      }
    }

    public Object getValueAt(int row, int column){
      AnnotationDiffer.Pairing pairing =
        (AnnotationDiffer.Pairing)pairings.get(row);
      Annotation key = pairing.getKey();
      String keyStr = "";
      try{
        if(key != null && keyDoc != null){
          keyStr = keyDoc.getContent().getContent(key.getStartNode().getOffset(),
                  key.getEndNode().getOffset()).toString();
        }
      }catch(InvalidOffsetException ioe){
        //this should never happen
        throw new GateRuntimeException(ioe);
      }
      Annotation res = pairing.getResponse();
      String resStr = "";
      try{
        if(res != null && resDoc != null){
          resStr = resDoc.getContent().getContent(res.getStartNode().getOffset(),
                  res.getEndNode().getOffset()).toString();
        }
      }catch(InvalidOffsetException ioe){
        //this should never happen
        throw new GateRuntimeException(ioe);
      }

      switch(column){
        case COL_KEY_START: return key == null ? "" :
          key.getStartNode().getOffset().toString();
        case COL_KEY_END: return key == null ? "" :
          key.getEndNode().getOffset().toString();
        case COL_KEY_STRING: return keyStr;
        case COL_KEY_FEATURES: return key == null ? "" :
          key.getFeatures().toString();
        case COL_MATCH: return matchLabel[pairing.getType()];
        case COL_RES_START: return res == null ? "" :
          res.getStartNode().getOffset().toString();
        case COL_RES_END: return res == null ? "" :
          res.getEndNode().getOffset().toString();
        case COL_RES_STRING: return resStr;
        case COL_RES_FEATURES: return res == null ? "" :
          res.getFeatures().toString();
        default: return "?";
      }
    }

    protected static final int COL_CNT = 9;
    protected static final int COL_KEY_START = 0;
    protected static final int COL_KEY_END = 1;
    protected static final int COL_KEY_STRING = 2;
    protected static final int COL_KEY_FEATURES = 3;
    protected static final int COL_MATCH = 4;
    protected static final int COL_RES_START = 5;
    protected static final int COL_RES_END = 6;
    protected static final int COL_RES_STRING = 7;
    protected static final int COL_RES_FEATURES = 8;
  }

  protected AnnotationDiffer differ;
  protected List pairings;
  protected Document keyDoc;
  protected Document resDoc;
  protected Set significantFeatures;
  protected List documents;
  protected List keySets;
  protected List resSets;
  protected AnnotationSet keySet;
  protected AnnotationSet resSet;

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

  protected JRadioButton allFeaturesBtn;
  protected JRadioButton someFeaturesBtn;
  protected JRadioButton noFeaturesBtn;
  protected JTextField weightTxt;
  protected JButton doDiffBtn;

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
  protected static final Color MISSING_BG = new Color(255,173,181);;
  protected static final Color FALSE_POZITIVE_BG = new Color(255,231,173);
  protected static final String[] matchLabel;
  static{
    matchLabel = new String[3];
    matchLabel[AnnotationDiffer.CORRECT] = "=";
    matchLabel[AnnotationDiffer.PARTIALLY_CORRECT] = "~";
    matchLabel[AnnotationDiffer.WRONG] = "!=";
  }
}
