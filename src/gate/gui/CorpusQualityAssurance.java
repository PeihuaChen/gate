/*
 *  Copyright (c) 1998-2009, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Thomas Heitz - 10 June 2009
 *
 *  $Id$
 */

package gate.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.NumberFormat;
import java.text.Collator;
import java.io.*;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;

import gate.*;
import gate.util.AnnotationDiffer;
import gate.util.Strings;
import gate.util.ExtensionFileFilter;
import gate.creole.AbstractVisualResource;
import gate.event.CorpusEvent;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.event.CorpusListener;
import gate.swing.XJTable;

/**
 * Quality assurance corpus view
 * <ul>
 * <li>two drop down boxes for key and response annotation sets</li>
 * <li>a table with F-scores per annotation for all the corpus (corpus
 * annotation view)</li>
 * <li>a table with F-scores per document (document view)<ul>
 *  <li>including F-scores per document annotations<ul>
 *    <li>annotation diff can be displayed for each annotation</li>
 * </li></li></ul></ul></ul>
 */
@CreoleResource(name = "Corpus Quality Assurance", guiType = GuiType.LARGE,
    resourceDisplayed = "gate.Corpus", mainViewer = false)
public class CorpusQualityAssurance extends AbstractVisualResource
  implements CorpusListener {

  public Resource init(){
    initLocalData();
    initGuiComponents();
    initListeners();
    return this;
  }

  protected void initLocalData(){
    collator = Collator.getInstance(Locale.ENGLISH);
    collator.setStrength(Collator.TERTIARY);
    documentTableModel = new DocumentTableModel();
    annotationTableModel = new AnnotationTableModel();
    annotationSetsNames = new TreeSet<String>(collator);
    annotationTypes = new TreeSet<String>(collator);
    featuresNames = new TreeSet<String>(collator);
    corpusChanged = false;
  }

  protected void initGuiComponents(){
    setLayout(new BorderLayout());

    topPanel = new JPanel();
    topPanel.setBorder(BorderFactory.createEmptyBorder());
    JToolBar toolbar = new JToolBar();
    toolbar.setBorder(BorderFactory.createEmptyBorder());
    toolbar.setMargin(new Insets(0, 0, 0, 0));
    toolbar.setFloatable(false);
    toolbar.add(openDocumentAction = new OpenDocumentAction());
    openDocumentAction.setEnabled(false);
    toolbar.add(openAnnotationDiffAction = new OpenAnnotationDiffAction());
    openAnnotationDiffAction.setEnabled(false);
    toolbar.add(exportToHtmlAction = new ExportToHtmlAction());
    topPanel.add(toolbar);
    topPanel.add(Box.createHorizontalStrut(5));
    JLabel setALabel = new JLabel("Set A");
    setALabel.setToolTipText("aka 'Key set'");
    topPanel.add(setALabel);
    topPanel.add(Box.createHorizontalStrut(2));
    keyAnnotationSetCombo = new JComboBox();
    keyAnnotationSetCombo.setPrototypeDisplayValue("annotation set");
    topPanel.add(keyAnnotationSetCombo);
    topPanel.add(Box.createHorizontalStrut(5));
    JLabel setBLabel = new JLabel("Set B");
    setBLabel.setToolTipText("aka 'Response set'");
    topPanel.add(setBLabel);
    topPanel.add(Box.createHorizontalStrut(2));
    responseAnnotationSetCombo = new JComboBox();
    responseAnnotationSetCombo.setPrototypeDisplayValue("annotation set");
    topPanel.add(responseAnnotationSetCombo);
    topPanel.add(Box.createHorizontalStrut(5));
    JButton button = new JButton(compareAction = new CompareAction());
    compareAction.setEnabled(false);
    topPanel.add(button);
    add(topPanel, BorderLayout.NORTH);

    Comparator<String> integerComparator = new Comparator<String>() {
      public int compare(String o1, String o2) {
        if (o1 == null || o2 == null) { return 0; }
        Integer i1 = Integer.valueOf(o1);
        Integer i2 = Integer.valueOf(o2);
        return i1.compareTo(i2);
      }
    };

    annotationTable = new XJTable(annotationTableModel) {
      // table header tool tips.
      protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
          public String getToolTipText(MouseEvent e) {
            java.awt.Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            if (index == -1) { return null; }
            int realIndex = columnModel.getColumn(index).getModelIndex();
            return annotationTableModel.HEADER_TOOLTIPS[realIndex];
          }
        };
      }
    };
    annotationTable.setSortable(true);
    annotationTable.setSortedColumn(AnnotationTableModel.COL_ANNOTATION);
    annotationTable.setComparator(AnnotationTableModel.COL_ANNOTATION,collator);
    annotationTable.setComparator(
      AnnotationTableModel.COL_CORRECT, integerComparator);
    annotationTable.setComparator(
      AnnotationTableModel.COL_PARTIAL, integerComparator);
    annotationTable.setComparator(
      AnnotationTableModel.COL_SPURIOUS, integerComparator);
    annotationTable.setComparator(
      AnnotationTableModel.COL_MISSING, integerComparator);
    annotationTable.setAutoResizeMode(XJTable.AUTO_RESIZE_ALL_COLUMNS);
    annotationTable.setEnableHidingColumns(true);
    JScrollPane annotationScroller = new JScrollPane(annotationTable);
    annotationScroller.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    annotationScroller.getViewport().setBackground(
      annotationTable.getBackground());

    documentTable = new XJTable(documentTableModel) {
      // table header tool tips.
      protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
          public String getToolTipText(MouseEvent e) {
            java.awt.Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            if (index == -1) { return null; }
            int realIndex = columnModel.getColumn(index).getModelIndex();
            return documentTableModel.HEADER_TOOLTIPS[realIndex];
          }
        };
      }
    };
    documentTable.setSortable(true);
    documentTable.setSortedColumn(DocumentTableModel.COL_DOCUMENT);
    documentTable.setComparator(DocumentTableModel.COL_DOCUMENT, collator);
    documentTable.setComparator(
      DocumentTableModel.COL_CORRECT, integerComparator);
    documentTable.setComparator(
      DocumentTableModel.COL_PARTIAL, integerComparator);
    documentTable.setComparator(
      DocumentTableModel.COL_SPURIOUS, integerComparator);
    documentTable.setComparator(
      DocumentTableModel.COL_MISSING, integerComparator);
    documentTable.setEnableHidingColumns(true);
    documentTable.setAutoResizeMode(XJTable.AUTO_RESIZE_ALL_COLUMNS);
    JScrollPane docScroller = new JScrollPane(documentTable);
    docScroller.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    docScroller.getViewport().setBackground(documentTable.getBackground());

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT , true,
      annotationScroller, docScroller);
    splitPane.setOneTouchExpandable(true);
    add(splitPane, BorderLayout.CENTER);
    splitPane.setResizeWeight(0.5);
    splitPane.setDividerLocation(0.5);
  }

  protected void initListeners(){

    // when the view is shown update the tables
    addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {
        if (!isShowing() || !corpusChanged) { return; }
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            updateAnnotationSets(null);
          }
        });
      }
      public void ancestorRemoved(AncestorEvent event) { /* do nothing */ }
      public void ancestorMoved(AncestorEvent event) { /* do nothing */ }
    });

    // when annotation set lists selection change
    // enabled/disabled the 'Compare' button
    ActionListener setComboActionListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        compareAction.setEnabled(
            keyAnnotationSetCombo.getSelectedItem() != null
         && responseAnnotationSetCombo.getSelectedItem() != null);
      }
    };
    keyAnnotationSetCombo.addActionListener(setComboActionListener);
    responseAnnotationSetCombo.addActionListener(setComboActionListener);

    // enable/disable toolbar icons according to the document table selection
    documentTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting()) { return; }
          openDocumentAction.setEnabled(documentTable.getSelectedRow() != -1);
          openAnnotationDiffAction.setEnabled(
            documentTable.getSelectedRow() != -1);
        }
      }
    );
  }

  public void cleanup(){
    super.cleanup();
    corpus = null;
  }

  public void setTarget(Object target){
    if(corpus != null && corpus != target){
      //we already had a different corpus
      corpus.removeCorpusListener(this);
    }
    if(!(target instanceof Corpus)){
      throw new IllegalArgumentException(
        "This view can only be used with a GATE corpus!\n" +
        target.getClass().toString() + " is not a GATE corpus!");
    }
    this.corpus = (Corpus) target;
    corpus.addCorpusListener(this);

    corpusChanged = true;
    if (!isShowing()) { return; }
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        updateAnnotationSets(null);
      }
    });
  }

  public void documentAdded(final CorpusEvent e) {
    corpusChanged = true;
    if (!isShowing()) { return; }
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        updateAnnotationSets(e.getDocument());
      }
    });
  }

  public void documentRemoved(final CorpusEvent e) {
    corpusChanged = true;
    if (!isShowing()) { return; }
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        updateAnnotationSets(null);
      }
    });
  }

  protected void updateAnnotationSets(Document documentAdded) {

    if (corpus.size() > 20 && !warningAlreadyShown) {
      final JPanel warningPanel = new JPanel();
      warningPanel.add(new JLabel(
        "<html>This corpus contains more than 20 documents.<br>" +
          "The comparison of all theirs annotations can be very slow.</html>"));
      warningPanel.add(Box.createVerticalStrut(10));
      JButton button  = new JButton("I will be patient!");
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          remove(warningPanel);
          add(topPanel, BorderLayout.NORTH);
          warningAlreadyShown = true;
          topPanel.updateUI();
        }
      });
      warningPanel.add(button);
      remove(topPanel);
      add(warningPanel, BorderLayout.NORTH);
      warningPanel.updateUI();
    }

    corpusChanged = false;
    if (documentAdded == null) {
      annotationSetsNames.clear();
      for (Object resource : corpus) {
        if (resource != null
         && resource instanceof Document
         && ((Document)resource).getAnnotationSetNames() != null) {
          Document document = (Document) resource;
          annotationSetsNames.addAll(document.getAnnotationSetNames());
        }
      }
    } else if (documentAdded.getAnnotationSetNames() != null) {
      annotationSetsNames.addAll(documentAdded.getAnnotationSetNames());
    }
    annotationSetsNames.add("[Default set]");
    Object selectedItem = keyAnnotationSetCombo.getSelectedItem();
    keyAnnotationSetCombo.setModel(
      new DefaultComboBoxModel(annotationSetsNames.toArray()));
    if(!annotationSetsNames.isEmpty()){
      keyAnnotationSetCombo.setSelectedItem(selectedItem);
    }
    selectedItem = responseAnnotationSetCombo.getSelectedItem();
    responseAnnotationSetCombo.setModel(
      new DefaultComboBoxModel(annotationSetsNames.toArray()));
    if(!annotationSetsNames.isEmpty()){
      responseAnnotationSetCombo.setSelectedItem(selectedItem);
    }
  }

  protected class AnnotationTableModel extends AbstractTableModel{
    private ArrayList<String> annotationColValues = new ArrayList<String>();
    private ArrayList<String> featureColValues = new ArrayList<String>();
    private AnnotationDiffer differ;
    private ArrayList<AnnotationDiffer> annotationDifferRows =
      new ArrayList<AnnotationDiffer>();

    public void initialise() {
      annotationColValues.clear();
      annotationColValues.addAll(annotationTypes);
      featureColValues.clear();
      featureColValues.addAll(Collections.nCopies(getRowCount(), "None"));
      annotationDifferRows.clear();
      for (int i = 0; i < getRowCount(); i++) {
        annotationDifferRows.add(null);
      }
    }

    public int getColumnCount() {
      return COLUMN_COUNT;
    }

    public int getRowCount() {
      return annotationColValues.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (annotationColValues.size() <= rowIndex) { return ""; }
      if (annotationDifferRows.get(rowIndex) == null) {
        String keyAnnotationSetName =
          (String) keyAnnotationSetCombo.getSelectedItem();
        String responseAnnotationSetName =
          (String) responseAnnotationSetCombo.getSelectedItem();
        Set<Annotation> keys = new HashSet<Annotation>();
        Set<Annotation> responses = new HashSet<Annotation>();
        for (Object resource : corpus) {
          Document document = (Document) resource;
          Set<Annotation> keyAS = new HashSet<Annotation>();
          Set<Annotation> responseAS = new HashSet<Annotation>();
          if (keyAnnotationSetName.equals("[Default set]")) {
            keyAS = document.getAnnotations();
          } else if (document.getAnnotationSetNames() != null
          && document.getAnnotationSetNames().contains(keyAnnotationSetName)) {
            keyAS = document.getAnnotations(keyAnnotationSetName);
          }
          if (responseAnnotationSetName.equals("[Default set]")) {
            responseAS = document.getAnnotations();
          } else if (document.getAnnotationSetNames() != null
          && document.getAnnotationSetNames()
            .contains(responseAnnotationSetName)) {
            responseAS = document.getAnnotations(responseAnnotationSetName);
          }
          if (annotationColValues.get(rowIndex).equals("All")
           || annotationColValues.get(rowIndex).equals("Expand All")) {
            keys.addAll(keyAS);
            responses.addAll(responseAS);
          } else {
            if (keyAS instanceof AnnotationSet) {
             keys.addAll(((AnnotationSet)keyAS)
               .get(annotationColValues.get(rowIndex)));
           }
           if (responseAS instanceof AnnotationSet) {
            responses.addAll(((AnnotationSet)responseAS)
              .get(annotationColValues.get(rowIndex)));
           }
         }
        }
        differ = new AnnotationDiffer();
        if(featureColValues.get(rowIndex).equals("All")
        || featureColValues.get(rowIndex).equals("Expand All")) {
          differ.setSignificantFeaturesSet(null);
        } else if (featureColValues.get(rowIndex).equals("None")) {
          differ.setSignificantFeaturesSet(new HashSet());
        } else {
          differ.setSignificantFeaturesSet(
            Collections.singleton(featureColValues.get(rowIndex)));
        }
        differ.calculateDiff(keys, responses);
        annotationDifferRows.set(rowIndex, differ);
      } else {
        differ = annotationDifferRows.get(rowIndex);
      }
      NumberFormat f = NumberFormat.getInstance();
      f.setMaximumFractionDigits(2);
      f.setMinimumFractionDigits(2);
      switch(columnIndex) {
        case COL_ANNOTATION:
          return annotationColValues.get(rowIndex);
        case COL_FEATURE:
          return featureColValues.get(rowIndex);
        case COL_CORRECT:
          return Integer.toString(differ.getCorrectMatches());
        case COL_MISSING:
          return Integer.toString(differ.getMissing());
        case COL_SPURIOUS:
          return Integer.toString(differ.getSpurious());
        case COL_PARTIAL:
        return Integer.toString(differ.getPartiallyCorrectMatches());
        case COL_RECALL:
          return f.format(differ.getRecallStrict());
        case COL_PRECISION:
          return f.format(differ.getPrecisionStrict());
        case COL_FMEASURE:
          return f.format(differ.getFMeasureStrict(1.0));
        default:
          return "";
      }
    }

    public Class<?> getColumnClass(int columnIndex) {
      switch(columnIndex) {
        case COL_FEATURE:
          return JComboBox.class;
        default:
          return String.class;
      }
    }

    public String getColumnName(int column) {
      return COLUMN_NAMES[column];
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      switch(columnIndex) {
        case COL_FEATURE:
          return true;
        default:
          return false;
      }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (aValue.equals("--------------")) { return; }
      final int firstRow = rowIndex;
      if (aValue.equals("Use As Default")) {
        String feature = featureColValues.get(rowIndex);
        for (int row = 0; row < featureColValues.size(); row++) {
          featureColValues.set(row, feature);
          annotationDifferRows.set(row, null);
        }
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          fireTableDataChanged();
        }});
        return;
      }
      if (aValue.equals("Expand All")) {
        int row = rowIndex + 1;
        for (String feature : featuresNames) {
          annotationColValues.add(row, annotationColValues.get(rowIndex));
          featureColValues.add(row, feature);
          annotationDifferRows.add(row, null);
          row++;
        }
        final int lastRow = row;
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          fireTableRowsInserted(firstRow+1, lastRow);
        }});
      } else if (featureColValues.get(rowIndex).equals("Expand All")) {
        int row;
        for (row = rowIndex + featuresNames.size(); row > rowIndex;  row--) {
          annotationColValues.remove(row);
          featureColValues.remove(row);
          annotationDifferRows.remove(row);
        }
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          fireTableRowsDeleted(firstRow+1, firstRow+featuresNames.size());
        }});
      }
      featureColValues.set(rowIndex, (String) aValue);
      annotationDifferRows.set(rowIndex, null);
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        fireTableRowsUpdated(firstRow, firstRow);
      }});
    }

    private final String[] COLUMN_NAMES = {"Annotation", "Feature",
      "Match", "Only A", "Only B", "Overlap", "Rec.B/A", "Prec.B/A", "F-Score"};
    public final String[] HEADER_TOOLTIPS = {null, null,
      "aka 'Correct'", "aka 'Missing'", "aka 'Spurious'", "aka 'Partial'",
      "Recall for B relative to A", "Precision for B relative to A",
      "Combine precision and recall with the same weight for each"};
    public static final int COL_ANNOTATION = 0;
    public static final int COL_FEATURE = 1;
    public static final int COL_CORRECT = 2;
    public static final int COL_MISSING = 3;
    public static final int COL_SPURIOUS = 4;
    public static final int COL_PARTIAL = 5;
    public static final int COL_RECALL = 6;
    public static final int COL_PRECISION = 7;
    public static final int COL_FMEASURE = 8;
    private static final int COLUMN_COUNT = 9;
  }

  protected class DocumentTableModel extends AbstractTableModel{
    private ArrayList<Document> documentColValues = new ArrayList<Document>();
    private ArrayList<String> annotationColValues = new ArrayList<String>();
    private ArrayList<String> featureColValues = new ArrayList<String>();
    private AnnotationDiffer differ;
    private ArrayList<AnnotationDiffer> annotationDifferRows =
      new ArrayList<AnnotationDiffer>();

    public void initialise() {
      documentColValues.clear();
      for (Object resource : corpus) {
        documentColValues.add((Document) resource);
      }
      annotationColValues.clear();
      annotationColValues.addAll(Collections.nCopies(getRowCount(), "All"));
      featureColValues.clear();
      featureColValues.addAll(Collections.nCopies(getRowCount(), "None"));
      annotationDifferRows.clear();
      for (int i = 0; i < getRowCount(); i++) {
        annotationDifferRows.add(null);
      }
    }

    public int getColumnCount() {
      return COLUMN_COUNT;
    }

    public int getRowCount() {
      return documentColValues.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (documentColValues.size() <= rowIndex) { return ""; }
      Document document = documentColValues.get(rowIndex);
      if (annotationDifferRows.get(rowIndex) == null) {
        String keyAnnotationSetName =
          (String) keyAnnotationSetCombo.getSelectedItem();
        String responseAnnotationSetName =
          (String) responseAnnotationSetCombo.getSelectedItem();
        Set<Annotation> keys = new HashSet<Annotation>();
        Set<Annotation> responses = new HashSet<Annotation>();
        if (keyAnnotationSetName.equals("[Default set]")) {
          keys = document.getAnnotations();
        } else if (document.getAnnotationSetNames() != null
        && document.getAnnotationSetNames().contains(keyAnnotationSetName)) {
          keys = document.getAnnotations(keyAnnotationSetName);
        }
        if (responseAnnotationSetName.equals("[Default set]")) {
          responses = document.getAnnotations();
        } else if (document.getAnnotationSetNames() != null
        && document.getAnnotationSetNames()
          .contains(responseAnnotationSetName)) {
          responses = document.getAnnotations(responseAnnotationSetName);
        }
        if (!annotationColValues.get(rowIndex).equals("All")
         && !annotationColValues.get(rowIndex).equals("Expand All")) {
          if (keys instanceof AnnotationSet) {
           keys = ((AnnotationSet)keys)
             .get(annotationColValues.get(rowIndex));
         }
         if (responses instanceof AnnotationSet) {
          responses = ((AnnotationSet)responses)
            .get(annotationColValues.get(rowIndex));
         }
       }
        AnnotationDiffer differ = new AnnotationDiffer();
        if(featureColValues.get(rowIndex).equals("All")
        || featureColValues.get(rowIndex).equals("Expand All")) {
          differ.setSignificantFeaturesSet(null);
        } else if (featureColValues.get(rowIndex).equals("None")) {
          differ.setSignificantFeaturesSet(new HashSet());
        } else {
          differ.setSignificantFeaturesSet(
            Collections.singleton(featureColValues.get(rowIndex)));
        }
        differ.calculateDiff(keys, responses);
        annotationDifferRows.set(rowIndex, differ);
      } else {
        differ = annotationDifferRows.get(rowIndex);
      }
      NumberFormat f = NumberFormat.getInstance();
      f.setMaximumFractionDigits(2);
      f.setMinimumFractionDigits(2);
      switch(columnIndex) {
        case COL_DOCUMENT:
          return document.getName();
        case COL_ANNOTATION:
          return annotationColValues.get(rowIndex);
        case COL_FEATURE:
          return featureColValues.get(rowIndex);
        case COL_CORRECT:
          return Integer.toString(differ.getCorrectMatches());
        case COL_MISSING:
          return Integer.toString(differ.getMissing());
        case COL_SPURIOUS:
          return Integer.toString(differ.getSpurious());
        case COL_PARTIAL:
        return Integer.toString(differ.getPartiallyCorrectMatches());
        case COL_RECALL:
          return f.format(differ.getRecallStrict());
        case COL_PRECISION:
          return f.format(differ.getPrecisionStrict());
        case COL_FMEASURE:
          return f.format(differ.getFMeasureStrict(1.0));
        default:
          return "";
      }
    }

    public Class<?> getColumnClass(int columnIndex) {
      switch(columnIndex) {
        case COL_ANNOTATION:
          return JComboBox.class;
        case COL_FEATURE:
          return JComboBox.class;
        default:
          return String.class;
      }
    }

    public String getColumnName(int column) {
      return COLUMN_NAMES[column];
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      switch(columnIndex) {
        case COL_ANNOTATION:
          return true;
        case COL_FEATURE:
          return true;
        default:
          return false;
      }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (aValue.equals("--------------")) { return; }
      final int firstRow = rowIndex;
      ArrayList<String> list = (columnIndex == COL_ANNOTATION) ?
        annotationColValues : featureColValues;
      ArrayList<String> list2 = (columnIndex == COL_ANNOTATION) ?
        featureColValues : annotationColValues;
      Set<String> set = (columnIndex == COL_ANNOTATION) ?
        annotationTypes : featuresNames;

      if (aValue.equals("Use As Default")) {
        String value = list.get(rowIndex);
        for (int row = 0; row < list.size(); row++) {
          list.set(row, value);
          annotationDifferRows.set(row, null);
        }
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          fireTableDataChanged();
        }});
        return;
      }
      if (aValue.equals("Expand All")) {
        int row = rowIndex + 1;
        for (String value : set) {
          documentColValues.add(row, documentColValues.get(rowIndex));
          list.add(row, value);
          list2.add(row, list2.get(rowIndex));
          annotationDifferRows.add(row, null);
          row++;
        }
        final int lastRow = row;
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          fireTableRowsInserted(firstRow+1, lastRow);
        }});
      } else if (list.get(rowIndex).equals("Expand All")) {
        int row;
        for (row = rowIndex + set.size(); row > rowIndex;  row--) {
          documentColValues.remove(row);
          list.remove(row);
          list2.remove(row);
          annotationDifferRows.remove(row);
        }
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          fireTableRowsDeleted(firstRow+1, firstRow+featuresNames.size());
        }});
      }
      list.set(rowIndex, (String) aValue);
      annotationDifferRows.set(rowIndex, null);
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        fireTableRowsUpdated(firstRow, firstRow);
      }});
    }

    private final String[] COLUMN_NAMES = {"Document", "Annotation", "Feature",
      "Match", "Only A", "Only B", "Overlap", "Rec.B/A", "Prec.B/A", "F-Score"};
    public final String[] HEADER_TOOLTIPS = {null, null, null,
      "aka 'Correct'", "aka 'Missing'", "aka 'Spurious'", "aka 'Partial'",
      "Recall for B relative to A", "Precision for B relative to A",
      "Combine precision and recall with the same weight for each"};
    public static final int COL_DOCUMENT = 0;
    public static final int COL_ANNOTATION = 1;
    public static final int COL_FEATURE = 2;
    public static final int COL_CORRECT = 3;
    public static final int COL_MISSING = 4;
    public static final int COL_SPURIOUS = 5;
    public static final int COL_PARTIAL = 6;
    public static final int COL_RECALL = 7;
    public static final int COL_PRECISION = 8;
    public static final int COL_FMEASURE = 9;
    private static final int COLUMN_COUNT = 10;
  }

  /**
   * Update <code>annotationTypes</code> and <code>featuresNames</code>.
   * Update annotation and document tables.
   */
  protected class CompareAction extends AbstractAction{
    public CompareAction(){
      super("Compare");
      putValue(SHORT_DESCRIPTION,
        "Compare annotations between sets A and B");
      putValue(MNEMONIC_KEY, KeyEvent.VK_ENTER);
      putValue(SMALL_ICON, MainFrame.getIcon("crystal-clear-action-run"));
    }
    public void actionPerformed(ActionEvent evt){
      String keyAnnotationSetName =
        (String) keyAnnotationSetCombo.getSelectedItem();
      String responseAnnotationSetName =
        (String) responseAnnotationSetCombo.getSelectedItem();
      if (keyAnnotationSetName == null
       || responseAnnotationSetName == null) {
        return;
      }
      CorpusQualityAssurance.this.setCursor(
        Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      // update features names and annotation types lists
      featuresNames.clear();
      annotationTypes.clear();
      for (Object object : corpus) {
        Document document = (Document) object;
        Set<Annotation> annotations = new HashSet<Annotation>();
        if (keyAnnotationSetName.equals("[Default set]")) {
          annotations = document.getAnnotations();
        } else if (document.getAnnotationSetNames() != null
        && document.getAnnotationSetNames().contains(keyAnnotationSetName)) {
          annotations = document.getAnnotations(keyAnnotationSetName);
        }
        annotationTypes.addAll(((AnnotationSet)annotations).getAllTypes());
        for (Annotation annotation : annotations) {
          for (Object featureKey : annotation.getFeatures().keySet()) {
            featuresNames.add((String) featureKey);
          }
        }
        if (responseAnnotationSetName.equals("[Default set]")) {
          annotations = document.getAnnotations();
        } else if (document.getAnnotationSetNames() != null
                && document.getAnnotationSetNames()
                  .contains(responseAnnotationSetName)) {
          annotations = document.getAnnotations(responseAnnotationSetName);
        }
        annotationTypes.addAll(((AnnotationSet)annotations).getAllTypes());
        for (Annotation annotation : annotations) {
          for (Object featureKey : annotation.getFeatures().keySet()) {
            featuresNames.add((String) featureKey);
          }
        }
      }

      // update cell renderers for annotation and feature columns
      TableColumn featureColumn = annotationTable.getColumnModel()
        .getColumn(AnnotationTableModel.COL_FEATURE);
      JComboBox featureCombo =
        new JComboBox(new Vector<String>(featuresNames));
      featureCombo.setMaximumRowCount(15);
      featureCombo.insertItemAt("None", 0);
      featureCombo.insertItemAt("All", 1);
      featureCombo.insertItemAt("Expand All", 2);
      featureCombo.insertItemAt("Use As Default", 3);
      featureCombo.insertItemAt("--------------", 4);
      featureColumn.setCellEditor(new DefaultCellEditor(featureCombo));
      int maxLength = "Use As Default".length();
      for (String item : featuresNames) {
        if (maxLength < item.length()) { maxLength = item.length(); }
      }
      maxLength += maxLength * 0.2;
      String colName = annotationTableModel
        .getColumnName(AnnotationTableModel.COL_FEATURE);
      featureColumn.setHeaderValue(colName +
        Strings.addPadding(" ", maxLength-colName.length()));
      featureColumn = documentTable.getColumnModel()
        .getColumn(DocumentTableModel.COL_FEATURE);
      featureColumn.setCellEditor(new DefaultCellEditor(featureCombo));
      colName = documentTableModel
        .getColumnName(DocumentTableModel.COL_FEATURE);
      featureColumn.setHeaderValue(colName +
        Strings.addPadding(" ", maxLength-colName.length()));
      TableColumn annotationColumn = documentTable.getColumnModel()
        .getColumn(DocumentTableModel.COL_ANNOTATION);
      JComboBox annotationCombo =
        new JComboBox(new Vector<String>(annotationTypes));
      annotationCombo.setMaximumRowCount(15);
      annotationCombo.insertItemAt("All", 0);
      annotationCombo.insertItemAt("Expand All", 1);
      annotationCombo.insertItemAt("Use As Default", 2);
      annotationCombo.insertItemAt("--------------", 3);
      annotationColumn.setCellEditor(new DefaultCellEditor(annotationCombo));
      maxLength = "Use As Default".length();
      for (String item : annotationTypes) {
        if (maxLength < item.length()) { maxLength = item.length(); }
      }
      maxLength += maxLength * 0.2;
      colName = documentTableModel
       .getColumnName(DocumentTableModel.COL_ANNOTATION);
      annotationColumn.setHeaderValue(colName +
        Strings.addPadding(" ", maxLength-colName.length()));

      // clear internal model of tables
      annotationTableModel.initialise();
      documentTableModel.initialise();

      // update tables
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        annotationTableModel.fireTableDataChanged();
        documentTableModel.fireTableDataChanged();
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          CorpusQualityAssurance.this.setCursor(
            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }});
      }});
    }
  }

  class OpenDocumentAction extends AbstractAction{
    public OpenDocumentAction(){
      super("Open documents", MainFrame.getIcon("document"));
      putValue(SHORT_DESCRIPTION,
        "Opens document for the selected row in a document editor");
      putValue(MNEMONIC_KEY, KeyEvent.VK_UP);
    }
    public void actionPerformed(ActionEvent e){
      Document doc = (Document) corpus.get(
        documentTable.rowViewToModel(documentTable.getSelectedRow()));
      MainFrame.getInstance().select(doc);
    }
  }

  class OpenAnnotationDiffAction extends AbstractAction{
    public OpenAnnotationDiffAction(){
      super("Open annotation diff", MainFrame.getIcon("annDiff"));
      putValue(SHORT_DESCRIPTION,
        "Opens annotation diff for the selected row in the document table");
      putValue(MNEMONIC_KEY, KeyEvent.VK_RIGHT);
    }
    public void actionPerformed(ActionEvent e){
      String documentName = (String) documentTable.getValueAt(
        documentTable.getSelectedRow(), DocumentTableModel.COL_DOCUMENT);
      String annotationType = (String) documentTable.getValueAt(
        documentTable.getSelectedRow(), DocumentTableModel.COL_ANNOTATION);
      String featureName = (String) documentTable.getValueAt(
        documentTable.getSelectedRow(), DocumentTableModel.COL_FEATURE);
      String keyAnnotationSetName =
        (String) keyAnnotationSetCombo.getSelectedItem();
      String responseAnnotationSetName =
        (String) responseAnnotationSetCombo.getSelectedItem();
      AnnotationDiffGUI frame = new AnnotationDiffGUI("Annotation Difference",
        documentName, documentName, keyAnnotationSetName,
        responseAnnotationSetName, annotationType, featureName);
      frame.pack();
      frame.setLocationRelativeTo(MainFrame.getInstance());
      frame.setVisible(true);
    }
  }

  protected class ExportToHtmlAction extends AbstractAction{
    public ExportToHtmlAction(){
      super("Export to HTML");
      putValue(SHORT_DESCRIPTION, "Export the results to HTML");
      putValue(SMALL_ICON,
        MainFrame.getIcon("crystal-clear-app-download-manager"));
    }
    public void actionPerformed(ActionEvent evt){
      JFileChooser fileChooser = (MainFrame.getFileChooser() == null) ?
        new JFileChooser() : MainFrame.getFileChooser();
      File currentFile = fileChooser.getSelectedFile();
      String parent = (currentFile != null) ? currentFile.getParent() :
        System.getProperty("user.home");
      String fileName = corpus.getName();
      fileName += "_" + keyAnnotationSetCombo.getSelectedItem();
      fileName += "-" + responseAnnotationSetCombo.getSelectedItem();
      fileName += ".html";
      fileChooser.setSelectedFile(new File(parent, fileName));
      ExtensionFileFilter fileFilter = new ExtensionFileFilter();
      fileFilter.addExtension(".html");
      fileChooser.setFileFilter(fileFilter);
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int res = fileChooser.showSaveDialog(CorpusQualityAssurance.this);
      if(res == JFileChooser.APPROVE_OPTION){
        File saveFile = fileChooser.getSelectedFile();
        try{
        Writer fw = new BufferedWriter(new FileWriter(saveFile));
        // Header, Title
        fw.write(BEGINHTML + nl);
        fw.write(BEGINHEAD);
        fw.write(corpus.getName() + " - "
          + keyAnnotationSetCombo.getSelectedItem() + " - "
          + responseAnnotationSetCombo.getSelectedItem());
        fw.write(ENDHEAD + nl);
        fw.write("<H1>Corpus Quality Assurance</H1>" + nl);
        fw.write("<P>Corpus: " + corpus.getName() + "<BR>" + nl);
        fw.write("Key annotation set: "
          + keyAnnotationSetCombo.getSelectedItem() + "<BR>" + nl);
        fw.write("Response annotation set: " +
          responseAnnotationSetCombo.getSelectedItem() + "</P>" + nl);
        // annotation table
        fw.write(BEGINTABLE + nl + "<TR>" + nl);
        int maxColIdx = annotationTable.getColumnCount() - 1;
        for(int col = 0; col <= maxColIdx; col++){
          fw.write("<TH align=\"left\">"
            + annotationTable.getColumnName(col) + "</TH>" + nl);
        }
        fw.write("</TR>" + nl);
        for(int row = 0; row < annotationTableModel.getRowCount(); row ++){
          fw.write("<TR>" + nl);
          for(int col = 0; col <= maxColIdx; col++){
            fw.write("<TD>"
              + annotationTable.getValueAt(row, col) + "</TD>" + nl);
          }
          fw.write("</TR>" + nl);
        }
        fw.write(ENDTABLE + nl);
        fw.write("<P>&nbsp;</P>" + nl);
        // document table
        fw.write(BEGINTABLE + nl + "<TR>" + nl);
        maxColIdx = documentTable.getColumnCount() - 1;
        for(int col = 0; col <= maxColIdx; col++){
          fw.write("<TH align=\"left\">"
            + documentTable.getColumnName(col) + "</TH>" + nl);
        }
        fw.write("</TR>" + nl);
        for(int row = 0; row < documentTableModel.getRowCount(); row ++){
          fw.write("<TR>" + nl);
          for(int col = 0; col <= maxColIdx; col++){
            fw.write("<TD>"
              + documentTable.getValueAt(row, col) + "</TD>" + nl);
          }
          fw.write("</TR>" + nl);
        }
        fw.write(ENDTABLE + nl);
        fw.write(ENDHTML + nl);
        fw.flush();
        fw.close();

        }catch(IOException ioe){
          JOptionPane.showMessageDialog(CorpusQualityAssurance.this,
            ioe.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
          ioe.printStackTrace();
        }
      }
    }
    final String nl = Strings.getNl();
    static final String BEGINHTML =
      "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">" +
      "<html>";
    static final String ENDHTML = "</body></html>";
    static final String BEGINHEAD = "<head>" +
      "<meta content=\"text/html; charset=utf-8\" http-equiv=\"content-type\">"
      + "<title>";
    static final String ENDHEAD = "</title></head><body>";
    static final String BEGINTABLE = "<table cellpadding=\"0\" border=\"1\">";
    static final String ENDTABLE = "</table>";
  }

  protected JPanel topPanel;
  protected XJTable documentTable;
  protected DocumentTableModel documentTableModel;
  protected XJTable annotationTable;
  protected AnnotationTableModel annotationTableModel;
  protected Corpus corpus;
  protected TreeSet<String> annotationSetsNames;
  protected TreeSet<String> annotationTypes;
  protected TreeSet<String> featuresNames;
  protected JComboBox keyAnnotationSetCombo;
  protected JComboBox responseAnnotationSetCombo;
  protected Collator collator;
  protected boolean corpusChanged;
  protected OpenDocumentAction openDocumentAction;
  protected OpenAnnotationDiffAction openAnnotationDiffAction;
  protected ExportToHtmlAction exportToHtmlAction;
  protected CompareAction compareAction;
  protected boolean warningAlreadyShown = false;
}