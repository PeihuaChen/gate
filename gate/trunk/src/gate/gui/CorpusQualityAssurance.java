/*
 *  Copyright (c) 2009, Ontotext AD.
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
 * Quality assurance corpus view.
 * Compare two sets of annotations with optionally their features
 * globally for each annotation and for each document inside a corpus
 * with different measures notably precision, recall and F1-score.
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
    annotationFeatures = new TreeSet<String>(collator);
    documentSetsNames = new TreeSet<String>(collator);
    documentTypes = new TreeSet<String>(collator);
    documentFeatures = new TreeSet<String>(collator);
    corpusChanged = false;
  }

  protected void initGuiComponents(){
    setLayout(new BorderLayout());

    JTabbedPane tabbedPane = new JTabbedPane();
    JSplitPane annotationSplitPane =
      new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    annotationSplitPane.setContinuousLayout(true);
    annotationSplitPane.setOneTouchExpandable(true);
    annotationSplitPane.setResizeWeight(0.80);
    JSplitPane documentSplitPane =
      new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    documentSplitPane.setContinuousLayout(true);
    documentSplitPane.setOneTouchExpandable(true);
    documentSplitPane.setResizeWeight(0.80);
    tabbedPane.addTab("Corpus statistics", null, annotationSplitPane,
      "Compare each annotation type for the whole corpus");
    tabbedPane.addTab("Document statistics", null, documentSplitPane,
      "Compare each documents in the corpus with theirs annotations");
    add(tabbedPane);

    JPanel annotationSidePanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.add(exportToHtmlAction = new ExportToHtmlAction());
    annotationSidePanel.add(toolbar, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    JLabel label = new JLabel("Annotation Sets A & B");
    label.setToolTipText("aka 'Key & Response sets'");
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.fill = GridBagConstraints.BOTH;
    annotationSidePanel.add(label, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(2), gbc);
    annotationSetList = new JList();
    annotationSetList.setSelectionModel(
      new ToggleSelectionABModel(annotationSetList));
    annotationSetList.setPrototypeCellValue("present in every document");
    annotationSetList.setVisibleRowCount(6);
    annotationSidePanel.add(new JScrollPane(annotationSetList), gbc);
    annotationSidePanel.add(Box.createVerticalStrut(2), gbc);
    annotationSetCheck = new JCheckBox("present in every document", false);
    annotationSetCheck.addActionListener(new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        updateSets(null, UPDATE_ANNOTATION_SETS);
      }
    });
    annotationSidePanel.add(annotationSetCheck, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    label = new JLabel("Annotation Features");
    label.setToolTipText("Annotation features to compare");
    annotationSidePanel.add(label, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(2), gbc);
    annotationFeatureList = new JList();
    annotationFeatureList.setSelectionModel(new ToggleSelectionModel());
    annotationFeatureList.setPrototypeCellValue("present in every document");
    annotationFeatureList.setVisibleRowCount(10);
    annotationSidePanel.add(new JScrollPane(annotationFeatureList), gbc);
//    annotationSidePanel.add(Box.createVerticalStrut(2), gbc);
//    annotationFeatureCheck = new JCheckBox("present in every type", false);
//    annotationSidePanel.add(annotationFeatureCheck, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    label = new JLabel("Measures");
    label.setToolTipText("Measures used to compare annotations");
    annotationSidePanel.add(label, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(2), gbc);
    annotationMeasureList = new JList();
    annotationMeasureList.setSelectionModel(new ToggleSelectionModel());
    annotationMeasureList.setModel(new ExtendedListModel(
      new String[]{"F1-score strict","F1-score lenient", "F1-score average"}));
    annotationMeasureList.setPrototypeCellValue("present in every document");
    annotationMeasureList.setVisibleRowCount(3);
    annotationSidePanel.add(new JScrollPane(annotationMeasureList), gbc);
//    annotationSidePanel.add(Box.createVerticalStrut(2), gbc);
//    annotationMicroAverageButton = new JRadioButton("Micro", true);
//    annotationMacroAverageButton = new JRadioButton("Macro average");
//    ButtonGroup group = new ButtonGroup();
//    group.add(annotationMicroAverageButton);
//    group.add(annotationMacroAverageButton);
//    Box horizontalBox = Box.createHorizontalBox();
//    horizontalBox.add(annotationMicroAverageButton);
//    horizontalBox.add(annotationMacroAverageButton);
//    annotationSidePanel.add(horizontalBox, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    JButton button = new JButton(compareAnnotationAction =
      new CompareAnnotationAction());
    compareAnnotationAction.setEnabled(false);
    annotationSidePanel.add(button, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    annotationProgressBar = new JProgressBar();
    annotationSidePanel.add(annotationProgressBar, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    annotationSplitPane.setRightComponent(annotationSidePanel);

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
            Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            if (index == -1) { return null; }
            int realIndex = columnModel.getColumn(index).getModelIndex();
            return annotationTableModel.headerTooltips.get(realIndex);
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
    annotationSplitPane.setLeftComponent(annotationScroller);

    JPanel documentSidePanel = new JPanel(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    documentSidePanel.add(Box.createVerticalStrut(5), gbc);
    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.add(openDocumentAction = new OpenDocumentAction());
    openDocumentAction.setEnabled(false);
    toolbar.add(openAnnotationDiffAction = new OpenAnnotationDiffAction());
    openAnnotationDiffAction.setEnabled(false);
    toolbar.add(exportToHtmlAction = new ExportToHtmlAction());
    documentSidePanel.add(toolbar, gbc);
    documentSidePanel.add(Box.createVerticalStrut(5), gbc);
    label = new JLabel("Annotation Sets A & B");
    label.setToolTipText("aka 'Key & Response sets'");
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.fill = GridBagConstraints.BOTH;
    documentSidePanel.add(label, gbc);
    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
    documentSetList = new JList();
    documentSetList.setSelectionModel(
      new ToggleSelectionABModel(documentSetList));
    documentSetList.setPrototypeCellValue("present in every document");
    documentSetList.setVisibleRowCount(4);
    documentSidePanel.add(new JScrollPane(documentSetList), gbc);
    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
    documentSetCheck = new JCheckBox("present in every document", false);
    documentSetCheck.addActionListener(new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        updateSets(null, UPDATE_DOCUMENT_SETS);
      }
    });
    documentSidePanel.add(documentSetCheck, gbc);
    documentSidePanel.add(Box.createVerticalStrut(5), gbc);
    label = new JLabel("Annotation Types");
    label.setToolTipText("Annotation types to compare");
    documentSidePanel.add(label, gbc);
    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
    documentTypeList = new JList();
    documentTypeList.setSelectionModel(new ToggleSelectionModel());
    documentTypeList.setPrototypeCellValue("present in every document");
    documentTypeList.setVisibleRowCount(4);
    documentSidePanel.add(new JScrollPane(documentTypeList), gbc);
//    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
//    documentTypeCheck = new JCheckBox("present in every set", false);
//    documentSidePanel.add(documentTypeCheck, gbc);
    documentSidePanel.add(Box.createVerticalStrut(5), gbc);
    label = new JLabel("Annotation Features");
    label.setToolTipText("Annotation features to compare");
    documentSidePanel.add(label, gbc);
    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
    documentFeatureList = new JList();
    documentFeatureList.setSelectionModel(new ToggleSelectionModel());
    documentFeatureList.setPrototypeCellValue("present in every document");
    documentFeatureList.setVisibleRowCount(4);
    documentSidePanel.add(new JScrollPane(documentFeatureList), gbc);
//    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
//    documentFeatureCheck = new JCheckBox("present in every type", false);
//    documentSidePanel.add(documentFeatureCheck, gbc);
    documentSidePanel.add(Box.createVerticalStrut(5), gbc);
    label = new JLabel("Measures");
    label.setToolTipText("Measures used to compare annotations");
    documentSidePanel.add(label, gbc);
    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
    documentMeasureList = new JList();
    documentMeasureList.setSelectionModel(new ToggleSelectionModel());
    documentMeasureList.setModel(new ExtendedListModel(
      new String[]{"F1-score strict","F1-score lenient", "F1-score average"}));
    documentMeasureList.setPrototypeCellValue("present in every document");
    documentMeasureList.setVisibleRowCount(3);
    documentSidePanel.add(new JScrollPane(documentMeasureList), gbc);
    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
    documentMicroAverageButton = new JRadioButton("Micro", true);
    documentMacroAverageButton = new JRadioButton("Macro average");
    ButtonGroup group = new ButtonGroup();
    group.add(documentMicroAverageButton);
    group.add(documentMacroAverageButton);
    Box horizontalBox = Box.createHorizontalBox();
    horizontalBox.add(documentMicroAverageButton);
    horizontalBox.add(documentMacroAverageButton);
    documentSidePanel.add(horizontalBox, gbc);
    documentSidePanel.add(Box.createVerticalStrut(5), gbc);
    button = new JButton(compareDocumentAction = new CompareDocumentAction());
    compareDocumentAction.setEnabled(false);
    documentSidePanel.add(button, gbc);
    documentSidePanel.add(Box.createVerticalStrut(5), gbc);
    documentProgressBar = new JProgressBar();
    documentSidePanel.add(documentProgressBar, gbc);
    documentSidePanel.add(Box.createVerticalStrut(5), gbc);
    documentSplitPane.setRightComponent(documentSidePanel);

    documentTable = new XJTable(documentTableModel) {
      // table header tool tips.
      protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
          public String getToolTipText(MouseEvent e) {
            Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            if (index == -1) { return null; }
            int realIndex = columnModel.getColumn(index).getModelIndex();
            return documentTableModel.headerTooltips.get(realIndex);
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
    JScrollPane documentScroller = new JScrollPane(documentTable);
    documentScroller.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    documentScroller.getViewport().setBackground(documentTable.getBackground());
    documentSplitPane.setLeftComponent(documentScroller);
  }

  protected void initListeners(){

    // when the view is shown update the tables if the corpus has changed
    addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {
        if (!isShowing() || !corpusChanged) { return; }
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            updateSets(null, UPDATE_BOTH_SETS);
          }
        });
      }
      public void ancestorRemoved(AncestorEvent event) { /* do nothing */ }
      public void ancestorMoved(AncestorEvent event) { /* do nothing */ }
    });

    // when annotation sets list selection change
    annotationSetList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        annotationFeatureList.setModel(new ExtendedListModel());
        annotationKeySetName = ((ToggleSelectionABModel)
          annotationSetList.getSelectionModel()).getSelectedValueA();
        annotationResponseSetName = ((ToggleSelectionABModel)
          annotationSetList.getSelectionModel()).getSelectedValueB();
        if (annotationKeySetName == null
         || annotationResponseSetName == null
         || annotationSetList.getSelectionModel().getValueIsAdjusting()) {
          compareAnnotationAction.setEnabled(false);
          return;
        }
        annotationFeatures.clear();
        annotationTypes.clear();
        annotationSetList.setEnabled(false);
        annotationSetCheck.setEnabled(false);
        CorpusQualityAssurance.this.setCursor(
          Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Runnable runnable = new Runnable() { public void run() {
        // update annotation features and annotation types lists
        for (int i = 0; i < corpus.size(); i++) {
          boolean documentWasLoaded = corpus.isDocumentLoaded(i);
          Document document = (Document) corpus.get(i);
          Set<Annotation> annotations = new HashSet<Annotation>();
          if (annotationKeySetName.equals("[Default set]")) {
            annotations = document.getAnnotations();
          } else if (document.getAnnotationSetNames() != null
          && document.getAnnotationSetNames().contains(annotationKeySetName)) {
            annotations = document.getAnnotations(annotationKeySetName);
          }
          if (annotations instanceof AnnotationSet) {
            annotationTypes.addAll(((AnnotationSet)annotations).getAllTypes());
            for (Annotation annotation : annotations) {
              for (Object featureKey : annotation.getFeatures().keySet()) {
                annotationFeatures.add((String) featureKey);
              }
            }
          }
          if (annotationResponseSetName.equals("[Default set]")) {
            annotations = document.getAnnotations();
          } else if (document.getAnnotationSetNames() != null
                  && document.getAnnotationSetNames()
                    .contains(annotationResponseSetName)) {
            annotations = document.getAnnotations(annotationResponseSetName);
          }
          if (annotations instanceof AnnotationSet) {
            annotationTypes.addAll(((AnnotationSet)annotations).getAllTypes());
            for (Annotation annotation : annotations) {
              for (Object featureKey : annotation.getFeatures().keySet()) {
                annotationFeatures.add((String) featureKey);
              }
            }
          }
          if (!documentWasLoaded) {
            corpus.unloadDocument(document);
            Factory.deleteResource(document);
          }
        }
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          annotationFeatureList.setVisibleRowCount(
            Math.min(10, annotationFeatures.size()));
          annotationFeatureList.setModel(
            new ExtendedListModel(annotationFeatures.toArray()));
          CorpusQualityAssurance.this.setCursor(
            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          compareAnnotationAction.setEnabled(true);
          annotationSetList.setEnabled(true);
          annotationSetCheck.setEnabled(true);
        }});
        }};
        Thread thread = new Thread(runnable,
          "annotationSetList.addListSelectionListener");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
    });

    // when document sets list selection change
    documentSetList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        documentTypeList.setModel(new ExtendedListModel());
        documentKeySetName = ((ToggleSelectionABModel)
          documentSetList.getSelectionModel()).getSelectedValueA();
        documentResponseSetName = ((ToggleSelectionABModel)
          documentSetList.getSelectionModel()).getSelectedValueB();
        if (documentKeySetName == null
         || documentResponseSetName == null
         || documentSetList.getSelectionModel().getValueIsAdjusting()) {
          return;
        }
        documentFeatures.clear();
        documentSetList.setEnabled(false);
        documentSetCheck.setEnabled(false);
        CorpusQualityAssurance.this.setCursor(
          Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Runnable runnable = new Runnable() { public void run() {
        // update document types list
        for (int i = 0; i < corpus.size(); i++) {
          boolean documentWasLoaded = corpus.isDocumentLoaded(i);
          Document document = (Document) corpus.get(i);
          Set<Annotation> annotations = new HashSet<Annotation>();
          if (documentKeySetName.equals("[Default set]")) {
            annotations = document.getAnnotations();
          } else if (document.getAnnotationSetNames() != null
          && document.getAnnotationSetNames().contains(documentKeySetName)) {
            annotations = document.getAnnotations(documentKeySetName);
          }
          if (annotations instanceof AnnotationSet) {
            documentTypes.addAll(((AnnotationSet)annotations).getAllTypes());
          }
          if (documentResponseSetName.equals("[Default set]")) {
            annotations = document.getAnnotations();
          } else if (document.getAnnotationSetNames() != null
                  && document.getAnnotationSetNames()
                    .contains(documentResponseSetName)) {
            annotations = document.getAnnotations(documentResponseSetName);
          }
          if (annotations instanceof AnnotationSet) {
            documentTypes.addAll(((AnnotationSet)annotations).getAllTypes());
          }
          if (!documentWasLoaded) {
            corpus.unloadDocument(document);
            Factory.deleteResource(document);
          }
        }
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          documentTypeList.setVisibleRowCount(
            Math.min(4, documentTypes.size()));
          documentTypeList.setModel(
            new ExtendedListModel(documentTypes.toArray()));
          CorpusQualityAssurance.this.setCursor(
            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          documentSetList.setEnabled(true);
          documentSetCheck.setEnabled(true);
        }});
        }};
        Thread thread = new Thread(runnable,
          "documentSetList.addListSelectionListener");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
    });

    // when document types list selection change
    documentTypeList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        // update document features list
        documentFeatureList.setModel(new ExtendedListModel());
        if (documentTypeList.getSelectedValues().length == 0) {
          compareDocumentAction.setEnabled(false);
          return;
        }
        final Set<String> types = new HashSet<String>();
        for (Object type : documentTypeList.getSelectedValues()) {
          types.add((String) type);
        }
        documentFeatures.clear();
        documentTypeList.setEnabled(false);
        CorpusQualityAssurance.this.setCursor(
          Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Runnable runnable = new Runnable() { public void run() {
        for (int i = 0; i < corpus.size(); i++) {
          boolean documentWasLoaded = corpus.isDocumentLoaded(i);
          Document document = (Document) corpus.get(i);
          Set<Annotation> annotations = new HashSet<Annotation>();
          if (documentKeySetName.equals("[Default set]")) {
            annotations = document.getAnnotations().get(types);
          } else if (document.getAnnotationSetNames() != null
          && document.getAnnotationSetNames().contains(documentKeySetName)) {
            annotations = document.getAnnotations(documentKeySetName).get(types);
          }
          if (annotations instanceof AnnotationSet) {
            for (Annotation annotation : annotations) {
              for (Object featureKey : annotation.getFeatures().keySet()) {
                documentFeatures.add((String) featureKey);
              }
            }
          }
          if (documentResponseSetName.equals("[Default set]")) {
            annotations = document.getAnnotations().get(types);
          } else if (document.getAnnotationSetNames() != null
                  && document.getAnnotationSetNames()
                    .contains(documentResponseSetName)) {
            annotations = document.getAnnotations(
              documentResponseSetName).get(types);
          }
          if (annotations instanceof AnnotationSet) {
            for (Annotation annotation : annotations) {
              for (Object featureKey : annotation.getFeatures().keySet()) {
                documentFeatures.add((String) featureKey);
              }
            }
          }
          if (!documentWasLoaded) {
            corpus.unloadDocument(document);
            Factory.deleteResource(document);
          }
        }
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          documentFeatureList.setVisibleRowCount(
            Math.min(4, documentFeatures.size()));
          documentFeatureList.setModel(
            new ExtendedListModel(documentFeatures.toArray()));
          CorpusQualityAssurance.this.setCursor(
            Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          documentTypeList.setEnabled(true);
          compareDocumentAction.setEnabled(true);
        }});
        }};
        Thread thread = new Thread(runnable,
          "documentTypeList.addListSelectionListener");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
    });

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

  protected class ExtendedListModel extends DefaultListModel {
    public ExtendedListModel() {
      super();
    }
    public ExtendedListModel(Object[] elements) {
      super();
      for (Object element : elements) {
        super.addElement(element);
      }
    }
  }

  protected class ToggleSelectionModel extends DefaultListSelectionModel {
    boolean gestureStarted = false;
    public void setSelectionInterval(int index0, int index1) {
      if (isSelectedIndex(index0) && !gestureStarted) {
        super.removeSelectionInterval(index0, index1);
      } else {
        super.addSelectionInterval(index0, index1);
      }
      gestureStarted = true;
    }
    public void setValueIsAdjusting(boolean isAdjusting) {
      if (!isAdjusting) {
        gestureStarted = false;
      }
    }
  }

  /**
   * Add a suffix A and B for the first and second selected item.
   * Allows only 2 items to be selected.
   */
  protected class ToggleSelectionABModel extends DefaultListSelectionModel {
    public ToggleSelectionABModel(JList list) {
      this.list = list;
    }
    public void setSelectionInterval(int index0, int index1) {
      ExtendedListModel model = (ExtendedListModel) list.getModel();
      String value = (String) model.getElementAt(index0);
      if (value.endsWith(" (A)") || value.endsWith(" (B)")) {
        // if ends with ' (A)' or ' (B)' then remove the suffix
        model.removeElementAt(index0);
        model.insertElementAt(value.substring(0,
          value.length() - " (A)".length()), index0);
        if (value.endsWith(" (A)")) {
          selectedValueA = null;
        } else {
          selectedValueB = null;
        }
        removeSelectionInterval(index0, index1);
      } else {
        // suffix with ' (A)' or ' (B)' if not already existing
        if (selectedValueA == null) {
          model.removeElementAt(index0);
          model.insertElementAt(value + " (A)", index0);
          selectedValueA = value;
          addSelectionInterval(index0, index1);
        } else if (selectedValueB == null) {
          model.removeElementAt(index0);
          model.insertElementAt(value + " (B)", index0);
          selectedValueB = value;
          addSelectionInterval(index0, index1);
        }
      }
    }
    public void clearSelection() {
      selectedValueA = null;
      selectedValueB = null;
      super.clearSelection();
    }
    public String getSelectedValueA() {
      return selectedValueA;
    }
    public String getSelectedValueB() {
      return selectedValueB;
    }
    JList list;
    String selectedValueA, selectedValueB;
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
        updateSets(null, UPDATE_BOTH_SETS);
      }
    });
  }

  public void documentAdded(final CorpusEvent e) {
    corpusChanged = true;
    if (!isShowing()) { return; }
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        updateSets(null, UPDATE_BOTH_SETS);
      }
    });
  }

  public void documentRemoved(final CorpusEvent e) {
    corpusChanged = true;
    if (!isShowing()) { return; }
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        updateSets(null, UPDATE_BOTH_SETS);
      }
    });
  }

  protected static int UPDATE_ANNOTATION_SETS = 0;
  protected static int UPDATE_DOCUMENT_SETS = 1;
  protected static int UPDATE_BOTH_SETS = 2;

  /**
   * Update set lists.
   * @param documentAdded faster if only one document is added, may be null
   * @param type type of the update, may be one of UPDATE_ANNOTATION_SETS,
   *  UPDATE_DOCUMENT_SETS or UPDATE_BOTH_SETS.
   */
  protected void updateSets(final Document documentAdded, final int type) {
    corpusChanged = false;
    if (type == UPDATE_ANNOTATION_SETS || type == UPDATE_BOTH_SETS) {
      annotationSetList.clearSelection();
    }
    if (type == UPDATE_DOCUMENT_SETS || type == UPDATE_BOTH_SETS) {
      documentSetList.clearSelection();
    }
    CorpusQualityAssurance.this.setCursor(
      Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    Runnable runnable = new Runnable() { public void run() {
    if (documentAdded == null) {
      if (type == UPDATE_ANNOTATION_SETS || type == UPDATE_BOTH_SETS) {
        annotationSetsNames.clear();
      }
      if (type == UPDATE_DOCUMENT_SETS || type == UPDATE_BOTH_SETS) {
        documentSetsNames.clear();
      }
      for (int i = 0; i < corpus.size(); i++) {
        boolean documentWasLoaded = corpus.isDocumentLoaded(i);
        Document document = (Document) corpus.get(i);
        if (document != null && document.getAnnotationSetNames() != null) {
          if (type == UPDATE_ANNOTATION_SETS || type == UPDATE_BOTH_SETS) {
            if (annotationSetCheck.isSelected()
            && !document.equals(corpus.get(0))) {
              annotationSetsNames.retainAll(document.getAnnotationSetNames());
            } else {
              annotationSetsNames.addAll(document.getAnnotationSetNames());
            }
          }
          if (type == UPDATE_DOCUMENT_SETS || type == UPDATE_BOTH_SETS) {
            if (documentSetCheck.isSelected()
            && !document.equals(corpus.get(0))) {
              documentSetsNames.retainAll(document.getAnnotationSetNames());
            } else {
              documentSetsNames.addAll(document.getAnnotationSetNames());
            }
          }
        }
        if (!documentWasLoaded) {
          corpus.unloadDocument(document);
          Factory.deleteResource(document);
        }
      }
    } else if (documentAdded.getAnnotationSetNames() != null) {
      if (type == UPDATE_ANNOTATION_SETS || type == UPDATE_BOTH_SETS) {
        if (annotationSetCheck.isSelected()) {
          annotationSetsNames.retainAll(documentAdded.getAnnotationSetNames());
        } else {
          annotationSetsNames.addAll(documentAdded.getAnnotationSetNames());
        }
      }
      if (type == UPDATE_DOCUMENT_SETS || type == UPDATE_BOTH_SETS) {
        if (documentSetCheck.isSelected()) {
          documentSetsNames.retainAll(documentAdded.getAnnotationSetNames());
        } else {
          documentSetsNames.addAll(documentAdded.getAnnotationSetNames());
        }
      }
    }
    SwingUtilities.invokeLater(new Runnable(){ public void run(){
      if (type == UPDATE_ANNOTATION_SETS || type == UPDATE_BOTH_SETS) {
        annotationSetsNames.add("[Default set]");
        annotationSetList.setVisibleRowCount(
          Math.min(6, annotationSetsNames.size()));
        annotationSetList.setModel(
          new ExtendedListModel(annotationSetsNames.toArray()));
      }
      if (type == UPDATE_DOCUMENT_SETS || type == UPDATE_BOTH_SETS) {
        documentSetsNames.add("[Default set]");
        documentSetList.setVisibleRowCount(
          Math.min(4, documentSetsNames.size()));
        documentSetList.setModel(
          new ExtendedListModel(documentSetsNames.toArray()));
      }
      CorpusQualityAssurance.this.setCursor(
        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }});
    }};
    Thread thread = new Thread(runnable, "updateSets");
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  protected class AnnotationTableModel extends AbstractTableModel{

    public void initialise() {
      differRows.clear();
      for (int i = 0; i < getRowCount(); i++) {
        differRows.add(null);
      }
    }

    public void updateRows(int rowFirst, int rowLast) {
      final int pCoef = (corpus.getDataStore() == null) ? 0 : 1;
      annotationProgressBar.setMaximum(pCoef*corpus.size() + rowLast + 1);
      Set<Annotation> keys = new HashSet<Annotation>();
      Set<Annotation> responses = new HashSet<Annotation>();
      Set<Annotation> keysIter;
      Set<Annotation> responsesIter;
      // for each document in the corpus
      for (int i = 0; i < corpus.size(); i++) {
        boolean documentWasLoaded = corpus.isDocumentLoaded(i);
        Document document = (Document) corpus.get(i);
        keysIter = new HashSet<Annotation>();
        responsesIter = new HashSet<Annotation>();
        // get annotations from selected annotation sets
        if (annotationKeySetName.equals("[Default set]")) {
          keysIter = document.getAnnotations();
        } else if (document.getAnnotationSetNames() != null
        && document.getAnnotationSetNames().contains(annotationKeySetName)) {
          keysIter = document.getAnnotations(annotationKeySetName);
        }
        if (annotationResponseSetName.equals("[Default set]")) {
          responsesIter = document.getAnnotations();
        } else if (document.getAnnotationSetNames() != null
        && document.getAnnotationSetNames()
          .contains(annotationResponseSetName)) {
          responsesIter = document.getAnnotations(annotationResponseSetName);
        }
        if (!documentWasLoaded) {
          corpus.unloadDocument(document);
          Factory.deleteResource(document);
        }
        keys.addAll(keysIter);
        responses.addAll(responsesIter);
        final int iF = i;
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          annotationProgressBar.setValue(pCoef*(iF + 1));
        }});
      }
      Set<String> featureSet = new HashSet<String>();
      for (Object feature : annotationFeatureList.getSelectedValues()) {
        featureSet.add((String) feature);
      }
      // for each annotation type row
      for (int row = rowFirst; row <= rowLast; row++) {
        keysIter = new HashSet<Annotation>();
        responsesIter = new HashSet<Annotation>();
        String type = (String) annotationTypes.toArray()[row];
        // keep only annotations from the current annotation type
        for (Annotation annotation : keys) {
          if (annotation.getType().equals(type)) {
            keysIter.add(annotation);
          }
        }
        for (Annotation annotation : responses) {
          if (annotation.getType().equals(type)) {
            responsesIter.add(annotation);
          }
        }
        differ = new AnnotationDiffer();
        differ.setSignificantFeaturesSet(featureSet);
        differ.calculateDiff(keysIter, responsesIter); // compare
        differRows.set(row, differ);
        final int rowF = row;
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          annotationProgressBar.setValue(pCoef*corpus.size() + rowF + 1);
        }});
      }
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        annotationProgressBar.setValue(annotationProgressBar.getMinimum());
      }});
      columnNames = new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
      headerTooltips = new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));
      for (Object measure : annotationMeasureList.getSelectedValues()) {
        if (measure.equals("F1-score strict")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-strict"));
          headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
            "Precision for B relative to A",
            "Combine precision and recall with the same weight for each"));
        } else if (measure.equals("F1-score lenient")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-lenient"));
          headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
            "Precision for B relative to A",
            "Combine precision and recall with the same weight for each"));
        } else if (measure.equals("F1-score average")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-average"));
          headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
            "Precision for B relative to A",
            "Combine precision and recall with the same weight for each"));
        }
      }
    }

    public int getColumnCount() {
      return COLUMN_COUNT
        + (annotationMeasureList.getSelectedValues().length * 3);
    }

    public int getRowCount() {
      return annotationTypes.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (annotationTypes.size() <= rowIndex) { return ""; }
      differ = differRows.get(rowIndex);
      NumberFormat f = NumberFormat.getInstance();
      f.setMaximumFractionDigits(2);
      f.setMinimumFractionDigits(2);
      switch(columnIndex) {
        case COL_ANNOTATION:
          return annotationTypes.toArray()[rowIndex];
        case COL_CORRECT:
          return Integer.toString(differ.getCorrectMatches());
        case COL_MISSING:
          return Integer.toString(differ.getMissing());
        case COL_SPURIOUS:
          return Integer.toString(differ.getSpurious());
        case COL_PARTIAL:
        return Integer.toString(differ.getPartiallyCorrectMatches());
        default:
          int colMeasure = (columnIndex - COLUMN_COUNT) % 3;
          int measureIndex = (columnIndex  - COLUMN_COUNT) / 3;
          String measure = (String)
            annotationMeasureList.getSelectedValues()[measureIndex];
          switch (colMeasure) {
            case COL_RECALL:
              if (measure.equals("F1-score strict")) {
                return f.format(differ.getRecallStrict());
              } else if (measure.equals("F1-score lenient")) {
                return f.format(differ.getRecallLenient());
              } else if (measure.equals("F1-score average")) {
                return f.format(differ.getRecallAverage());
              }
            case COL_PRECISION:
              if (measure.equals("F1-score strict")) {
                return f.format(differ.getPrecisionStrict());
              } else if (measure.equals("F1-score lenient")) {
                return f.format(differ.getPrecisionLenient());
              } else if (measure.equals("F1-score average")) {
                return f.format(differ.getPrecisionAverage());
              }
            case COL_FMEASURE:
              if (measure.equals("F1-score strict")) {
                return f.format(differ.getFMeasureStrict(1.0));
              } else if (measure.equals("F1-score lenient")) {
                return f.format(differ.getFMeasureLenient(1.0));
              } else if (measure.equals("F1-score average")) {
                return f.format(differ.getFMeasureAverage(1.0));
              }
            default:
              return "";
          }
      }
    }

    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    public String getColumnName(int column) {
      return columnNames.get(column);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    private AnnotationDiffer differ;
    private ArrayList<AnnotationDiffer> differRows =
      new ArrayList<AnnotationDiffer>();

    private final String[] COLUMN_NAMES = {
      "Annotation", "Match", "Only A", "Only B", "Overlap"};
    public final String[] HEADER_TOOLTIPS = {null,
      "aka 'Correct'", "aka 'Missing'", "aka 'Spurious'", "aka 'Partial'"};
    private ArrayList<String> columnNames =
      new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
    private ArrayList<String> headerTooltips =
      new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));

    public static final int COL_ANNOTATION = 0;
    public static final int COL_CORRECT = 1;
    public static final int COL_MISSING = 2;
    public static final int COL_SPURIOUS = 3;
    public static final int COL_PARTIAL = 4;
    private static final int COLUMN_COUNT = 5;
    public static final int COL_RECALL = 0;
    public static final int COL_PRECISION = 1;
    public static final int COL_FMEASURE = 2;
  }

  protected class DocumentTableModel extends AbstractTableModel{

    public void initialise() {
      differRows.clear();
      documentNames.clear();
      for (int i = 0; i < getRowCount(); i++) {
        differRows.add(null);
        documentNames.add("");
      }
    }

    public void updateRows(int rowFirst, int rowLast) {
      documentProgressBar.setMaximum(rowLast + 1);
      // for each document
      for (int row = rowFirst; row <= rowLast; row++) {
        boolean documentWasLoaded = corpus.isDocumentLoaded(row);
        Document document = (Document) corpus.get(row);
        documentNames.set(row, document.getName());
        Set<Annotation> keys = new HashSet<Annotation>();
        Set<Annotation> responses = new HashSet<Annotation>();
        // get annotations from selected annotation sets
        if (documentKeySetName.equals("[Default set]")) {
          keys = document.getAnnotations();
        } else if (document.getAnnotationSetNames() != null
        && document.getAnnotationSetNames().contains(documentKeySetName)) {
          keys = document.getAnnotations(documentKeySetName);
        }
        if (documentResponseSetName.equals("[Default set]")) {
          responses = document.getAnnotations();
        } else if (document.getAnnotationSetNames() != null
        && document.getAnnotationSetNames()
          .contains(documentResponseSetName)) {
          responses = document.getAnnotations(documentResponseSetName);
        }
        if (!documentWasLoaded) {
          corpus.unloadDocument(document);
          Factory.deleteResource(document);
        }
        Set<String> types = new HashSet<String>();
        for (Object type : documentTypeList.getSelectedValues()) {
          types.add((String) type);
        }
        Set<String> featureSet = new HashSet<String>();
        for (Object feature : documentFeatureList.getSelectedValues()) {
          featureSet.add((String) feature);
        }
        differs = new ArrayList<AnnotationDiffer>();
        AnnotationDiffer differ;
        // keep only annotations from selected annotation types
        if (documentMacroAverageButton.isSelected()) {
          if (keys instanceof AnnotationSet && !types.isEmpty()) {
            keys = ((AnnotationSet)keys).get(types);
          }
          if (responses instanceof AnnotationSet && !types.isEmpty()) {
            responses = ((AnnotationSet)responses).get(types);
          }
          differ = new AnnotationDiffer();
          differ.setSignificantFeaturesSet(featureSet);
          differ.calculateDiff(keys, responses); // compare
          differs.add(differ);
        } else { // micro average
          Set<Annotation> keysIter = new HashSet<Annotation>();
          Set<Annotation> responsesIter = new HashSet<Annotation>();
          for (String type : types) {
            if (keys instanceof AnnotationSet && !types.isEmpty()) {
              keysIter = ((AnnotationSet)keys).get(type);
            }
            if (responses instanceof AnnotationSet && !types.isEmpty()) {
              responsesIter = ((AnnotationSet)responses).get(type);
            }
            differ = new AnnotationDiffer();
            differ.setSignificantFeaturesSet(featureSet);
            differ.calculateDiff(keysIter, responsesIter); // compare
            differs.add(differ);
          }
        }
        differRows.set(row, differs);
        final int rowF = row;
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          documentProgressBar.setValue(rowF + 1);
        }});
      }
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        documentProgressBar.setValue(documentProgressBar.getMinimum());
      }});
      columnNames = new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
      headerTooltips = new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));
      for (Object measure : documentMeasureList.getSelectedValues()) {
        if (measure.equals("F1-score strict")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-strict"));
          headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
            "Precision for B relative to A",
            "Combine precision and recall with the same weight for each"));
        } else if (measure.equals("F1-score lenient")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-lenient"));
          headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
            "Precision for B relative to A",
            "Combine precision and recall with the same weight for each"));
        } else if (measure.equals("F1-score average")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-average"));
          headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
            "Precision for B relative to A",
            "Combine precision and recall with the same weight for each"));
        }
      }
    }

    public int getColumnCount() {
      return COLUMN_COUNT
        + (documentMeasureList.getSelectedValues().length * 3);
    }

    public int getRowCount() {
      return corpus == null ? 0 : corpus.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (corpus.size() <= rowIndex) { return ""; }
      differs = differRows.get(rowIndex);
      NumberFormat f = NumberFormat.getInstance();
      f.setMaximumFractionDigits(2);
      f.setMinimumFractionDigits(2);
      int sumInt = 0;
      switch(columnIndex) {
        case COL_DOCUMENT:
          return documentNames.get(rowIndex);
        case COL_CORRECT:
          for (AnnotationDiffer differ : differs)
          { sumInt += differ.getCorrectMatches(); }
          return Integer.toString(sumInt);
        case COL_MISSING:
          for (AnnotationDiffer differ : differs)
          { sumInt += differ.getMissing(); }
          return Integer.toString(sumInt);
        case COL_SPURIOUS:
          for (AnnotationDiffer differ : differs)
          { sumInt += differ.getSpurious(); }
          return Integer.toString(sumInt);
        case COL_PARTIAL:
          for (AnnotationDiffer differ : differs)
          { sumInt += differ.getPartiallyCorrectMatches(); }
          return Integer.toString(sumInt);
        default:
          double sumDbl = 0;
          int colMeasure = (columnIndex - COLUMN_COUNT) % 3;
          int measureIndex = (columnIndex  - COLUMN_COUNT) / 3;
          String measure = (String)
            documentMeasureList.getSelectedValues()[measureIndex];
          switch (colMeasure) {
            case COL_RECALL:
              if (measure.equals("F1-score strict")) {
                for (AnnotationDiffer differ : differs)
                { sumDbl += differ.getRecallStrict(); }
                return f.format(sumDbl/differs.size());
              } else if (measure.equals("F1-score lenient")) {
                for (AnnotationDiffer differ : differs)
                { sumDbl += differ.getRecallLenient(); }
                return f.format(sumDbl/differs.size());
              } else if (measure.equals("F1-score average")) {
                for (AnnotationDiffer differ : differs)
                { sumDbl += differ.getRecallAverage(); }
                return f.format(sumDbl/differs.size());
              }
            case COL_PRECISION:
              if (measure.equals("F1-score strict")) {
                for (AnnotationDiffer differ : differs)
                { sumDbl += differ.getPrecisionStrict(); }
                return f.format(sumDbl/differs.size());
              } else if (measure.equals("F1-score lenient")) {
                for (AnnotationDiffer differ : differs)
                { sumDbl += differ.getPrecisionLenient(); }
                return f.format(sumDbl/differs.size());
              } else if (measure.equals("F1-score average")) {
                for (AnnotationDiffer differ : differs)
                { sumDbl += differ.getPrecisionAverage(); }
                return f.format(sumDbl/differs.size());
              }
            case COL_FMEASURE:
              if (measure.equals("F1-score strict")) {
                for (AnnotationDiffer differ : differs)
                { sumDbl += differ.getFMeasureStrict(1.0); }
                return f.format(sumDbl/differs.size());
              } else if (measure.equals("F1-score lenient")) {
                for (AnnotationDiffer differ : differs)
                { sumDbl += differ.getFMeasureLenient(1.0); }
                return f.format(sumDbl/differs.size());
              } else if (measure.equals("F1-score average")) {
                for (AnnotationDiffer differ : differs)
                { sumDbl += differ.getFMeasureAverage(1.0); }
                return f.format(sumDbl/differs.size());
              }
            default:
              return "";
          }
      }
    }

    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    public String getColumnName(int column) {
      return columnNames.get(column);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    private ArrayList<AnnotationDiffer> differs;
    private ArrayList<ArrayList<AnnotationDiffer>> differRows =
      new ArrayList<ArrayList<AnnotationDiffer>>();
    private ArrayList<String> documentNames = new ArrayList<String>();

    private final String[] COLUMN_NAMES = {
      "Document", "Match", "Only A", "Only B", "Overlap"};
    public final String[] HEADER_TOOLTIPS = {null,
      "aka 'Correct'", "aka 'Missing'", "aka 'Spurious'", "aka 'Partial'"};
    private ArrayList<String> columnNames =
      new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
    private ArrayList<String> headerTooltips =
      new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));

    public static final int COL_DOCUMENT = 0;
    public static final int COL_CORRECT = 1;
    public static final int COL_MISSING = 2;
    public static final int COL_SPURIOUS = 3;
    public static final int COL_PARTIAL = 4;
    private static final int COLUMN_COUNT = 5;
    public static final int COL_RECALL = 0;
    public static final int COL_PRECISION = 1;
    public static final int COL_FMEASURE = 2;
  }

  /**
   * Update annotation table.
   */
  protected class CompareAnnotationAction extends AbstractAction{
    public CompareAnnotationAction(){
      super("Compare");
      putValue(SHORT_DESCRIPTION,
        "Compare annotations between sets A and B");
      putValue(MNEMONIC_KEY, KeyEvent.VK_ENTER);
      putValue(SMALL_ICON, MainFrame.getIcon("crystal-clear-action-run"));
    }
    public void actionPerformed(ActionEvent evt){
      CorpusQualityAssurance.this.setCursor(
        Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      Runnable runnable = new Runnable() { public void run() {
      // update tables
      annotationTableModel.initialise();
      annotationTableModel.updateRows(0, annotationTableModel.getRowCount()-1);

      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        // redraw tables
        annotationTableModel.fireTableStructureChanged();
        CorpusQualityAssurance.this.setCursor(
          Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }});
      }};
      Thread thread = new Thread(runnable,  "CompareAnnotationAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  /**
   * Update document table.
   */
  protected class CompareDocumentAction extends AbstractAction{
    public CompareDocumentAction(){
      super("Compare");
      putValue(SHORT_DESCRIPTION,
        "Compare annotations between sets A and B");
      putValue(MNEMONIC_KEY, KeyEvent.VK_ENTER);
      putValue(SMALL_ICON, MainFrame.getIcon("crystal-clear-action-run"));
    }
    public void actionPerformed(ActionEvent evt){
      CorpusQualityAssurance.this.setCursor(
        Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      Runnable runnable = new Runnable() { public void run() {
      // update tables
      documentTableModel.initialise();
      documentTableModel.updateRows(0, documentTableModel.getRowCount()-1);

      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        // redraw tables
        documentTableModel.fireTableStructureChanged();
        CorpusQualityAssurance.this.setCursor(
          Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }});
      }};
      Thread thread = new Thread(runnable,  "CompareDocumentAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
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
      String keyAnnotationSetName =
        (String) documentSetList.getSelectedValues()[0];
      String responseAnnotationSetName =
        (String) documentSetList.getSelectedValues()[1];
      String annotationType = (String) documentTypeList.getSelectedValue();
      Set<String> featureSet = new HashSet<String>();
      for (Object feature : documentFeatureList.getSelectedValues()) {
        featureSet.add((String) feature);
      }
      AnnotationDiffGUI frame = new AnnotationDiffGUI("Annotation Difference",
        documentName, documentName, keyAnnotationSetName,
        responseAnnotationSetName, annotationType, featureSet);
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
      fileName += "_" + annotationKeySetName;
      fileName += "-" + annotationResponseSetName;
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
          + annotationKeySetName + " - "
          + annotationResponseSetName);
        fw.write(ENDHEAD + nl);
        fw.write("<H1>Corpus Quality Assurance</H1>" + nl);
        fw.write("<P>Corpus: " + corpus.getName() + "<BR>" + nl);
        fw.write("Key annotation set: "
          + annotationKeySetName + "<BR>" + nl);
        fw.write("Response annotation set: " +
          annotationResponseSetName + "</P>" + nl);
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

  protected XJTable documentTable;
  protected DocumentTableModel documentTableModel;
  protected XJTable annotationTable;
  protected AnnotationTableModel annotationTableModel;
  protected Corpus corpus;
  protected String annotationKeySetName;
  protected String annotationResponseSetName;
  protected String documentKeySetName;
  protected String documentResponseSetName;
  protected TreeSet<String> annotationSetsNames;
  protected TreeSet<String> annotationTypes;
  protected TreeSet<String> annotationFeatures;
  protected TreeSet<String> documentSetsNames;
  protected TreeSet<String> documentTypes;
  protected TreeSet<String> documentFeatures;
  protected JList annotationSetList;
  protected JList annotationFeatureList;
  protected JList annotationMeasureList;
  protected JList documentSetList;
  protected JList documentTypeList;
  protected JList documentFeatureList;
  protected JList documentMeasureList;
  protected JCheckBox annotationSetCheck;
  protected JCheckBox annotationFeatureCheck;
  protected JCheckBox documentSetCheck;
  protected JCheckBox documentTypeCheck;
  protected JCheckBox documentFeatureCheck;
  protected JRadioButton annotationMicroAverageButton;
  protected JRadioButton annotationMacroAverageButton;
  protected JRadioButton documentMicroAverageButton;
  protected JRadioButton documentMacroAverageButton;
  protected Collator collator;
  protected boolean corpusChanged;
  protected OpenDocumentAction openDocumentAction;
  protected OpenAnnotationDiffAction openAnnotationDiffAction;
  protected ExportToHtmlAction exportToHtmlAction;
  protected CompareAnnotationAction compareAnnotationAction;
  protected CompareDocumentAction compareDocumentAction;
  protected JProgressBar annotationProgressBar;
  protected JProgressBar documentProgressBar;
}