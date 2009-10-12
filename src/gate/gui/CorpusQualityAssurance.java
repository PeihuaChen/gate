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
import java.util.Timer;
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
import gate.swing.XJFileChooser;

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
    annotationTypes = new TreeSet<String>(collator);
    docsSetsTypesFeatures = new LinkedHashMap<String, TreeMap<String,
      TreeMap<String, TreeSet<String>>>>();
    corpusChanged = false;
    doubleComparator = new Comparator<String>() {
      public int compare(String s1, String s2) {
        if (s1 == null || s2 == null) {
          return 0;
        } else {
          return Double.valueOf(s1).compareTo(Double.valueOf(s2));
        }
      }
    };
    totalComparator = new Comparator<String>() {
      public int compare(String s1, String s2) {
        if (s1 == null || s2 == null) {
          return 0;
        } else if (s1.equals("Total")) {
          return 1;
        } else if (s2.equals("Total")) {
          return -1;
        } else {
          return s1.compareTo(s2);
        }
      }
    };
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
    toolbar.add(new ExportToHtmlAction(ExportToHtmlAction.ANNOTATION_TABLE));
    reloadCacheAction = new ReloadCacheAction();
    toolbar.add(reloadCacheAction);
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
        updateSets(UPDATE_ANNOTATION_SETS);
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
    annotationSidePanel.add(Box.createVerticalStrut(2), gbc);
    annotationFeatureCheck = new JCheckBox("present in every type", false);
    annotationFeatureCheck.addActionListener(new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        annotationSetList.getListSelectionListeners()[0].valueChanged(null);
      }
    });
    annotationSidePanel.add(annotationFeatureCheck, gbc);
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
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    JButton button = new JButton(compareAnnotationAction =
      new CompareAnnotationAction());
    compareAnnotationAction.setEnabled(false);
    annotationSidePanel.add(button, gbc);
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    annotationProgressBar = new JProgressBar();
    annotationProgressBar.setStringPainted(true);
    annotationProgressBar.setString("");
    annotationSidePanel.add(annotationProgressBar, gbc);
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weighty = 1;
    annotationSidePanel.add(Box.createVerticalStrut(5), gbc);
    annotationSplitPane.setRightComponent(annotationSidePanel);

    annotationTable = new XJTable(annotationTableModel) {
      // table header tool tips
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
    annotationTable.setSortable(false);
    annotationTable.setEnableHidingColumns(true);
    annotationTable.setAutoResizeMode(XJTable.AUTO_RESIZE_ALL_COLUMNS);
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
    toolbar.add(new ExportToHtmlAction(ExportToHtmlAction.DOCUMENT_TABLE));
    toolbar.add(reloadCacheAction);
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
        updateSets(UPDATE_DOCUMENT_SETS);
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
    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
    documentTypeCheck = new JCheckBox("present in every selected set", false);
    documentTypeCheck.addActionListener(new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        documentSetList.getListSelectionListeners()[0].valueChanged(null);
      }
    });
    documentSidePanel.add(documentTypeCheck, gbc);
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
    documentSidePanel.add(Box.createVerticalStrut(2), gbc);
    documentFeatureCheck = new JCheckBox("present in every selected type", false);
    documentFeatureCheck.addActionListener(new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        documentTypeList.getListSelectionListeners()[0].valueChanged(null);
      }
    });
    documentSidePanel.add(documentFeatureCheck, gbc);
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
    documentProgressBar.setStringPainted(true);
    documentProgressBar.setString("");
    documentSidePanel.add(documentProgressBar, gbc);
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weighty = 1;
    documentSidePanel.add(Box.createVerticalStrut(5), gbc);
    documentSplitPane.setRightComponent(documentSidePanel);

    documentTable = new XJTable(documentTableModel) {
      // table header tool tips
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
    documentTable.setSortable(false);
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
        if (timerTask != null) { timerTask.cancel(); }
        Date timeToRun = new Date(System.currentTimeMillis() + 1000);
        timerTask = new TimerTask() { public void run() {
          updateSets(UPDATE_BOTH_SETS);
        }};
        timer.schedule(timerTask, timeToRun); // add a delay before updating
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
        annotationTypes.clear();
        annotationSetList.setEnabled(false);
        annotationSetCheck.setEnabled(false);
        // update annotation features and annotation types UI lists
        TreeMap<String, TreeSet<String>> types;
        TreeSet<String> annotationFeatures = new TreeSet<String>(collator);
        boolean firstLoop = true; // needed for retainAll to work
        for (TreeMap<String, TreeMap<String, TreeSet<String>>> sets :
             docsSetsTypesFeatures.values()) {
          types = sets.get(annotationKeySetName.equals("[Default set]") ?
            "" : annotationKeySetName);
          if (types != null) {
            annotationTypes.addAll(types.keySet());
            for (TreeSet<String> features : types.values()) {
              if (annotationFeatureCheck.isSelected() && !firstLoop) {
                annotationFeatures.retainAll(features);
              } else {
                annotationFeatures.addAll(features);
              }
            }
          }
          types = sets.get(annotationResponseSetName.equals("[Default set]") ?
            "" : annotationResponseSetName);
          if (types != null) {
            annotationTypes.addAll(types.keySet());
            for (TreeSet<String> features : types.values()) {
              if (annotationFeatureCheck.isSelected() && !firstLoop) {
                annotationFeatures.retainAll(features);
              } else {
                annotationFeatures.addAll(features);
              }
            }
          }
          firstLoop = false;
        }
        annotationFeatureList.setVisibleRowCount(
          Math.min(10, annotationFeatures.size()));
        annotationFeatureList.setModel(
          new ExtendedListModel(annotationFeatures.toArray()));
        compareAnnotationAction.setEnabled(true);
        annotationSetList.setEnabled(true);
        annotationSetCheck.setEnabled(true);
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
        documentSetList.setEnabled(false);
        documentSetCheck.setEnabled(false);
        // update document types UI list
        TreeSet<String> documentTypes = new TreeSet<String>(collator);
        TreeMap<String, TreeSet<String>> types;
        boolean firstLoop = true; // needed for retainAll to work
        for (TreeMap<String, TreeMap<String, TreeSet<String>>> sets :
             docsSetsTypesFeatures.values()) {
          types = sets.get(documentKeySetName.equals("[Default set]") ?
            "" : documentKeySetName);
          if (types != null) {
            if (documentTypeCheck.isSelected() && !firstLoop) {
              documentTypes.retainAll(types.keySet());
            } else {
              documentTypes.addAll(types.keySet());
            }
          }
          types = sets.get(documentResponseSetName.equals("[Default set]") ?
            "" : documentResponseSetName);
          if (types != null) {
            if (documentTypeCheck.isSelected() && !firstLoop) {
              documentTypes.retainAll(types.keySet());
            } else {
              documentTypes.addAll(types.keySet());
            }
          }
          firstLoop = false;
        }
        documentTypeList.setVisibleRowCount(
          Math.min(4, documentTypes.size()));
        documentTypeList.setModel(
          new ExtendedListModel(documentTypes.toArray()));
        documentSetList.setEnabled(true);
        documentSetCheck.setEnabled(true);
      }
    });

    // when document types list selection change
    documentTypeList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        // update document features UI list
        documentFeatureList.setModel(new ExtendedListModel());
        if (documentTypeList.getSelectedValues().length == 0
         || documentTypeList.getSelectionModel().getValueIsAdjusting()) {
          compareDocumentAction.setEnabled(false);
          return;
        }
        final Set<String> typeNames = new HashSet<String>();
        for (Object type : documentTypeList.getSelectedValues()) {
          typeNames.add((String) type);
        }
        documentTypeList.setEnabled(false);
        TreeSet<String> documentFeatures = new TreeSet<String>(collator);
        TreeMap<String, TreeSet<String>> types;
        boolean firstLoop = true; // needed for retainAll to work
        for (TreeMap<String, TreeMap<String, TreeSet<String>>> sets :
             docsSetsTypesFeatures.values()) {
          types = sets.get(documentKeySetName.equals("[Default set]") ?
            "" : documentKeySetName);
          if (types != null) {
            for (String typeName : types.keySet()) {
              if (typeNames.contains(typeName)) {
                if (documentFeatureCheck.isSelected() && !firstLoop) {
                  documentFeatures.retainAll(types.get(typeName));
                } else {
                  documentFeatures.addAll(types.get(typeName));
                }
              }
            }
          }
          types = sets.get(documentResponseSetName.equals("[Default set]") ?
            "" : documentResponseSetName);
          if (types != null) {
            for (String typeName : types.keySet()) {
              if (typeNames.contains(typeName)) {
                if (documentFeatureCheck.isSelected() && !firstLoop) {
                  documentFeatures.retainAll(types.get(typeName));
                } else {
                  documentFeatures.addAll(types.get(typeName));
                }
              }
            }
          }
          firstLoop = false;
        }
        documentFeatureList.setVisibleRowCount(
          Math.min(4, documentFeatures.size()));
        documentFeatureList.setModel(
          new ExtendedListModel(documentFeatures.toArray()));
        documentTypeList.setEnabled(true);
        compareDocumentAction.setEnabled(true);
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
    if (timerTask != null) { timerTask.cancel(); }
    Date timeToRun = new Date(System.currentTimeMillis() + 2000);
    timerTask = new TimerTask() { public void run() {
      updateSets(UPDATE_BOTH_SETS);
    }};
    timer.schedule(timerTask, timeToRun); // add a delay before updating
  }

  public void documentAdded(final CorpusEvent e) {
    corpusChanged = true;
    if (!isShowing()) { return; }
    if (timerTask != null) { timerTask.cancel(); }
    Date timeToRun = new Date(System.currentTimeMillis() + 2000);
    timerTask = new TimerTask() { public void run() {
      updateSets(UPDATE_BOTH_SETS);
    }};
    timer.schedule(timerTask, timeToRun); // add a delay before updating
  }

  public void documentRemoved(final CorpusEvent e) {
    corpusChanged = true;
    if (!isShowing()) { return; }
    if (timerTask != null) { timerTask.cancel(); }
    Date timeToRun = new Date(System.currentTimeMillis() + 2000);
    timerTask = new TimerTask() { public void run() {
      updateSets(UPDATE_BOTH_SETS);
    }};
    timer.schedule(timerTask, timeToRun); // add a delay before updating
  }

  protected static int UPDATE_ANNOTATION_SETS = 0;
  protected static int UPDATE_DOCUMENT_SETS = 1;
  protected static int UPDATE_BOTH_SETS = 2;

  /**
   * Update set lists.
   * @param type type of the update, may be one of UPDATE_ANNOTATION_SETS,
   *  UPDATE_DOCUMENT_SETS or UPDATE_BOTH_SETS.
   */
  protected void updateSets(final int type) {
    corpusChanged = false;
    SwingUtilities.invokeLater(new Runnable(){ public void run() {
      if (type == UPDATE_ANNOTATION_SETS || type == UPDATE_BOTH_SETS) {
        annotationSetList.clearSelection();
        compareAnnotationAction.setEnabled(false);
        annotationProgressBar.setMaximum(corpus.size() - 1);
        annotationProgressBar.setString("Read sets, types, features");
      }
      if (type == UPDATE_DOCUMENT_SETS || type == UPDATE_BOTH_SETS) {
        documentSetList.clearSelection();
        compareDocumentAction.setEnabled(false);
        documentProgressBar.setMaximum(corpus.size() - 1);
        documentProgressBar.setString("Read sets, types, features");
      }
      reloadCacheAction.setEnabled(false);
    }});
    CorpusQualityAssurance.this.setCursor(
      Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    Runnable runnable = new Runnable() { public void run() {
    if (docsSetsTypesFeatures.size() != corpus.getDocumentNames().size()
    || !docsSetsTypesFeatures.keySet().containsAll(corpus.getDocumentNames())) {
    docsSetsTypesFeatures.clear();
    TreeMap<String, TreeMap<String, TreeSet<String>>> sets;
    TreeMap<String, TreeSet<String>> types;
    TreeSet<String> features;
    for (int i = 0; i < corpus.size(); i++) {
      // fill in the lists of document, set, type and feature names
      boolean documentWasLoaded = corpus.isDocumentLoaded(i);
      Document document = (Document) corpus.get(i);
      if (document != null && document.getAnnotationSetNames() != null) {
        sets = new TreeMap<String, TreeMap<String, TreeSet<String>>>(collator);
        HashSet<String> setNames =
          new HashSet<String>(document.getAnnotationSetNames());
        setNames.add("");
        for (String set : setNames) {
          types = new TreeMap<String, TreeSet<String>>(collator);
          AnnotationSet annotations = document.getAnnotations(set);
          for (String type : annotations.getAllTypes()) {
            features = new TreeSet<String>(collator);
            for (Annotation annotation : annotations.get(type)) {
              for (Object featureKey : annotation.getFeatures().keySet()) {
                features.add((String) featureKey);
              }
            }
            types.put(type, features);
          }
          sets.put(set, types);
        }
        docsSetsTypesFeatures.put(document.getName(), sets);
      }
      if (!documentWasLoaded) {
        corpus.unloadDocument(document);
        Factory.deleteResource(document);
      }
      final int progressValue = i + 1;
      SwingUtilities.invokeLater(new Runnable(){ public void run() {
        if (type == UPDATE_ANNOTATION_SETS || type == UPDATE_BOTH_SETS
         && annotationProgressBar.isShowing()) {
            annotationProgressBar.setValue(progressValue);
        }
        if (type == UPDATE_DOCUMENT_SETS || type == UPDATE_BOTH_SETS
         && documentProgressBar.isShowing()) {
            documentProgressBar.setValue(progressValue);
        }
      }});
    }
    }
    final TreeSet<String> annotationSetsNames = new TreeSet<String>(collator);
    final TreeSet<String> documentSetsNames = new TreeSet<String>(collator);
    Set<String> sets;
    boolean firstLoop = true; // needed for retainAll to work
    for (String document : docsSetsTypesFeatures.keySet()) {
      // get the list of set names
      sets = docsSetsTypesFeatures.get(document).keySet();
      if (type == UPDATE_ANNOTATION_SETS || type == UPDATE_BOTH_SETS) {
        if (annotationSetCheck.isSelected() && !firstLoop) {
          annotationSetsNames.retainAll(sets);
        } else {
          annotationSetsNames.addAll(sets);
        }
      }
      if (type == UPDATE_DOCUMENT_SETS || type == UPDATE_BOTH_SETS) {
        if (documentSetCheck.isSelected() && !firstLoop) {
          documentSetsNames.retainAll(sets);
        } else {
          documentSetsNames.addAll(sets);
        }
      }
      firstLoop = false;
    }
    SwingUtilities.invokeLater(new Runnable(){ public void run(){
      // update the UI lists of sets
      if (type == UPDATE_ANNOTATION_SETS || type == UPDATE_BOTH_SETS) {
        annotationSetsNames.remove("");
        annotationSetsNames.add("[Default set]");
        annotationSetList.setVisibleRowCount(
          Math.min(6, annotationSetsNames.size()));
        annotationSetList.setModel(
          new ExtendedListModel(annotationSetsNames.toArray()));
        annotationProgressBar.setValue(annotationProgressBar.getMinimum());
        annotationProgressBar.setString("");
      }
      if (type == UPDATE_DOCUMENT_SETS || type == UPDATE_BOTH_SETS) {
        documentSetsNames.remove("");
        documentSetsNames.add("[Default set]");
        documentSetList.setVisibleRowCount(
          Math.min(4, documentSetsNames.size()));
        documentSetList.setModel(
          new ExtendedListModel(documentSetsNames.toArray()));
        documentProgressBar.setValue(documentProgressBar.getMinimum());
        documentProgressBar.setString("");
      }
      CorpusQualityAssurance.this.setCursor(
        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      reloadCacheAction.setEnabled(true);
    }});
    }};
    Thread thread = new Thread(runnable, "updateSets");
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  protected class AnnotationTableModel extends AbstractTableModel{

    public AnnotationTableModel() {
      f.setMaximumFractionDigits(2);
      f.setMinimumFractionDigits(2);
    }

    public void updateRows() {
      final float c1 = (corpus.getDataStore() == null) ? 0.25f : 1;
      final float c2 = corpus.size()/5 + 1;
      final int progressMaximum =
        Math.round(c1*corpus.size() + c2*annotationTypes.size());
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        annotationProgressBar.setMaximum(progressMaximum);
        annotationProgressBar.setString("Read all annotations");
      }});
      differRows.clear();
      Set<Annotation> keys = new HashSet<Annotation>();
      Set<Annotation> responses = new HashSet<Annotation>();
      // for each document in the corpus
      for (int i = 0; i < corpus.size(); i++) {
        boolean documentWasLoaded = corpus.isDocumentLoaded(i);
        Document document = (Document) corpus.get(i);
        // get annotations from selected annotation sets for all documents
        if (annotationKeySetName.equals("[Default set]")) {
          keys.addAll(document.getAnnotations());
        } else if (document.getAnnotationSetNames() != null
        && document.getAnnotationSetNames().contains(annotationKeySetName)) {
          keys.addAll(document.getAnnotations(annotationKeySetName));
        }
        if (annotationResponseSetName.equals("[Default set]")) {
          responses.addAll(document.getAnnotations());
        } else if (document.getAnnotationSetNames() != null
        && document.getAnnotationSetNames()
          .contains(annotationResponseSetName)) {
          responses.addAll(document.getAnnotations(annotationResponseSetName));
        }
        if (!documentWasLoaded) {
          corpus.unloadDocument(document);
          Factory.deleteResource(document);
          final int progressValue = Math.round(c1 * (i + 1));
          SwingUtilities.invokeLater(new Runnable(){ public void run(){
            annotationProgressBar.setValue(progressValue);
          }});
        }
      }
      Set<String> featureSet = new HashSet<String>();
      for (Object feature : annotationFeatureList.getSelectedValues()) {
        featureSet.add((String) feature);
      }
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        annotationProgressBar.setString("Compare each annotation type");
      }});
      // for each annotation type row
      Set<Annotation> keysIter;
      Set<Annotation> responsesIter;
      for (int row = 0; row < annotationTypes.size(); row++) {
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
        differRows.add(differ);
        final int progressValue = Math.round(c1*corpus.size() + c2*(row + 1));
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          annotationProgressBar.setValue(progressValue);
        }});
      }
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        annotationProgressBar.setValue(annotationProgressBar.getMinimum());
        annotationProgressBar.setString("");
      }});
      columnNames = new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
      headerTooltips = new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));
      for (Object measure : annotationMeasureList.getSelectedValues()) {
        if (measure.equals("F1-score strict")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-strict"));
        } else if (measure.equals("F1-score lenient")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-lenient"));
        } else if (measure.equals("F1-score average")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-average"));
        }
        headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
          "Precision for B relative to A",
          "Combine precision and recall with the same weight for each"));
      }
    }

    public int getColumnCount() {
      return COLUMN_COUNT
        + (annotationMeasureList.getSelectedValues().length * 3);
    }

    public int getRowCount() {
      return (differRows.size() == 0) ? 0 : differRows.size() + 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < differRows.size()) {
        differ = differRows.get(rowIndex);
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
      } else if (rowIndex == differRows.size()) {
        switch(columnIndex) {
          case COL_ANNOTATION:
            return "Total";
          case COL_CORRECT:
          case COL_MISSING:
          case COL_SPURIOUS:
          case COL_PARTIAL:
            int sumInt = 0;
            for (int row = 0; row < differRows.size(); row++) {
              sumInt += Integer.valueOf((String) getValueAt(row, columnIndex));
            }
            return Integer.toString(sumInt);
          default:
            double sumDbl= 0;
            for (int row = 0; row < differRows.size(); row++) {
              sumDbl += Double.valueOf((String) getValueAt(row, columnIndex));
            }
            return f.format(sumDbl / differRows.size());
        }
      } else {
        return "";
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
    private NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);

    private final String[] COLUMN_NAMES = {
      "Annotation", "Match", "Only A", "Only B", "Overlap"};
    private final String[] HEADER_TOOLTIPS = {null,
      "aka 'Correct'", "aka 'Missing'", "aka 'Spurious'", "aka 'Partial'"};
    private ArrayList<String> columnNames =
      new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
    public ArrayList<String> headerTooltips =
      new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));

    public static final int COL_ANNOTATION = 0;
    public static final int COL_CORRECT = 1;
    public static final int COL_MISSING = 2;
    public static final int COL_SPURIOUS = 3;
    public static final int COL_PARTIAL = 4;
    public static final int COLUMN_COUNT = 5;
    public static final int COL_RECALL = 0;
    public static final int COL_PRECISION = 1;
    public static final int COL_FMEASURE = 2;
  }

  protected class DocumentTableModel extends AbstractTableModel{

    public DocumentTableModel() {
      f.setMaximumFractionDigits(2);
      f.setMinimumFractionDigits(2);
    }

    public void updateRows() {
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        documentProgressBar.setMaximum(corpus.size() - 1);
        documentProgressBar.setString("Compare each document annotations");
      }});
      differRows.clear();
      documentNames.clear();
      // for each document
      for (int row = 0; row < corpus.size(); row++) {
        boolean documentWasLoaded = corpus.isDocumentLoaded(row);
        Document document = (Document) corpus.get(row);
        documentNames.add(document.getName());
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
        if (documentMicroAverageButton.isSelected()) {
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
        } else { // macro average
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
        differRows.add(differs);
        final int progressValue = row + 1;
        SwingUtilities.invokeLater(new Runnable(){ public void run(){
          documentProgressBar.setValue(progressValue);
        }});
      }
      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        documentProgressBar.setValue(documentProgressBar.getMinimum());
        documentProgressBar.setString("");
      }});
      columnNames = new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
      headerTooltips = new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));
      for (Object measure : documentMeasureList.getSelectedValues()) {
        if (measure.equals("F1-score strict")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-strict"));
        } else if (measure.equals("F1-score lenient")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-lenient"));
        } else if (measure.equals("F1-score average")) {
          columnNames.addAll(Arrays.asList("Rec.B/A", "Prec.B/A", "F1-average"));
        }
        headerTooltips.addAll(Arrays.asList("Recall for B relative to A",
          "Precision for B relative to A",
          "Combine precision and recall with the same weight for each"));
      }
    }

    public int getColumnCount() {
      return COLUMN_COUNT
        + (documentMeasureList.getSelectedValues().length * 3);
    }

    public int getRowCount() {
      return (differRows.size() == 0) ? 0 : differRows.size() + 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex < differRows.size()) {
        differs = differRows.get(rowIndex);
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
      } else if (rowIndex == differRows.size()) {
        switch(columnIndex) {
          case COL_DOCUMENT:
            return "Total";
          case COL_CORRECT:
          case COL_MISSING:
          case COL_SPURIOUS:
          case COL_PARTIAL:
            int sumInt = 0;
            for (int row = 0; row < differRows.size(); row++) {
              sumInt += Integer.valueOf((String) getValueAt(row, columnIndex));
            }
            return Integer.toString(sumInt);
          default:
            double sumDbl= 0;
            for (int row = 0; row < differRows.size(); row++) {
              sumDbl += Double.valueOf((String) getValueAt(row, columnIndex));
            }
            return f.format(sumDbl / differRows.size());
        }
      } else {
        return "";
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
    private NumberFormat f = NumberFormat.getInstance(Locale.ENGLISH);

    private final String[] COLUMN_NAMES = {
      "Document", "Match", "Only A", "Only B", "Overlap"};
    private final String[] HEADER_TOOLTIPS = {null,
      "aka 'Correct'", "aka 'Missing'", "aka 'Spurious'", "aka 'Partial'"};
    private ArrayList<String> columnNames =
      new ArrayList<String>(Arrays.asList(COLUMN_NAMES));
    public ArrayList<String> headerTooltips =
      new ArrayList<String>(Arrays.asList(HEADER_TOOLTIPS));

    public static final int COL_DOCUMENT = 0;
    public static final int COL_CORRECT = 1;
    public static final int COL_MISSING = 2;
    public static final int COL_SPURIOUS = 3;
    public static final int COL_PARTIAL = 4;
    public static final int COLUMN_COUNT = 5;
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
      annotationTableModel.updateRows();

      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        // redraw tables
        annotationTable.setSortable(false);
        annotationTableModel.fireTableStructureChanged();
        for (int col = 0; col < annotationTable.getColumnCount(); col++) {
          annotationTable.setComparator(col, doubleComparator);
        }
        annotationTable.setComparator(
          AnnotationTableModel.COL_ANNOTATION, totalComparator);
        annotationTable.setSortedColumn(AnnotationTableModel.COL_ANNOTATION);
        annotationTable.setSortable(true);
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
      documentTableModel.updateRows();

      SwingUtilities.invokeLater(new Runnable(){ public void run(){
        // redraw tables
        documentTable.setSortable(false);
        documentTableModel.fireTableStructureChanged();
        for (int col = 0; col < documentTable.getColumnCount(); col++) {
          documentTable.setComparator(col, doubleComparator);
        }
        documentTable.setComparator(
          DocumentTableModel.COL_DOCUMENT, totalComparator);
        documentTable.setSortedColumn(DocumentTableModel.COL_DOCUMENT);
        documentTable.setSortable(true);
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
      String annotationType = (String) documentTypeList.getSelectedValue();
      Set<String> featureSet = new HashSet<String>();
      for (Object feature : documentFeatureList.getSelectedValues()) {
        featureSet.add((String) feature);
      }
      AnnotationDiffGUI frame = new AnnotationDiffGUI("Annotation Difference",
        documentName, documentName, documentKeySetName,
        documentResponseSetName, annotationType, featureSet);
      frame.pack();
      frame.setLocationRelativeTo(MainFrame.getInstance());
      frame.setVisible(true);
    }
  }

  protected class ExportToHtmlAction extends AbstractAction{
    /** @param type ANNOTATION_TABLE or DOCUMENT_TABLE */
    public ExportToHtmlAction(int type){
      super("Export to HTML");
      putValue(SHORT_DESCRIPTION, "Export the results to HTML");
      putValue(SMALL_ICON,
        MainFrame.getIcon("crystal-clear-app-download-manager"));
      this.type = type;
    }
    public void actionPerformed(ActionEvent evt){
      XJFileChooser fileChooser = MainFrame.getFileChooser();
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.setDialogTitle("Choose a file to export the results");
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      ExtensionFileFilter filter = new ExtensionFileFilter("HTML files","html");
      fileChooser.addChoosableFileFilter(filter);
      String title = corpus.getName();
      if (type == ANNOTATION_TABLE) {
        title += "_" + annotationKeySetName;
        title += "-" + annotationResponseSetName;
      } else {
        title += "_" + documentKeySetName;
        title += "-" + documentResponseSetName;
      }
      fileChooser.setFileName(title + ".html");
      int res = fileChooser.showSaveDialog(CorpusQualityAssurance.this,
        CorpusQualityAssurance.class.getName());
      if (res != JFileChooser.APPROVE_OPTION) { return; }

      File saveFile = fileChooser.getSelectedFile();
      try{
      Writer fw = new BufferedWriter(new FileWriter(saveFile));
      // Header, Title
      fw.write(BEGINHTML + nl);
      fw.write(BEGINHEAD);
      fw.write(title);
      fw.write(ENDHEAD + nl);
      fw.write("<H1>Corpus Quality Assurance</H1>" + nl);
      fw.write("<P>Corpus: " + corpus.getName() + "<BR>" + nl);
      if (type == ANNOTATION_TABLE) {
        // annotation table
        fw.write("Key set: " + annotationKeySetName + "<BR>" + nl);
        fw.write("Response set: " + annotationResponseSetName + "</P>" + nl);
        fw.write(BEGINTABLE + nl + "<TR>" + nl);
        for(int col = 0; col < annotationTable.getColumnCount(); col++){
          fw.write("<TH align=\"left\">"
            + annotationTable.getColumnName(col) + "</TH>" + nl);
        }
        fw.write("</TR>" + nl);
        for(int row = 0; row < annotationTableModel.getRowCount(); row ++){
          fw.write("<TR>" + nl);
          for(int col = 0; col < annotationTable.getColumnCount(); col++){
            fw.write("<TD>"
              + annotationTable.getValueAt(row, col) + "</TD>" + nl);
          }
          fw.write("</TR>" + nl);
        }
        fw.write(ENDTABLE + nl);
        fw.write("<P>&nbsp;</P>" + nl);
      } else {
        // document table
        fw.write("Key set: " + documentKeySetName + "<BR>" + nl);
        fw.write("Response set: " + documentResponseSetName + "</P>" + nl);
        fw.write(BEGINTABLE + nl + "<TR>" + nl);
        for(int col = 0; col < documentTable.getColumnCount(); col++){
          fw.write("<TH align=\"left\">"
            + documentTable.getColumnName(col) + "</TH>" + nl);
        }
        fw.write("</TR>" + nl);
        for(int row = 0; row < documentTable.getRowCount(); row ++){
          fw.write("<TR>" + nl);
          for(int col = 0; col < documentTable.getColumnCount(); col++){
            fw.write("<TD>"
              + documentTable.getValueAt(row, col) + "</TD>" + nl);
          }
          fw.write("</TR>" + nl);
        }
        fw.write(ENDTABLE + nl);
      }
      fw.write(ENDHTML + nl);
      fw.flush();
      fw.close();

      } catch(IOException ioe){
        JOptionPane.showMessageDialog(CorpusQualityAssurance.this,
          ioe.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
        ioe.printStackTrace();
      }
    }

    int type;
    final static int ANNOTATION_TABLE = 0;
    final static int DOCUMENT_TABLE = 1;
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

  class ReloadCacheAction extends AbstractAction{
    public ReloadCacheAction(){
      super("Reload cache", MainFrame.getIcon("crystal-clear-action-reload"));
      putValue(SHORT_DESCRIPTION,
        "Reload cache for set, type and feature names list");
    }
    public void actionPerformed(ActionEvent e){
      docsSetsTypesFeatures.clear();
      updateSets(UPDATE_BOTH_SETS);
    }
  }

  protected XJTable documentTable;
  protected DocumentTableModel documentTableModel;
  protected XJTable annotationTable;
  protected AnnotationTableModel annotationTableModel;
  protected Corpus corpus;
  protected TreeSet<String> annotationTypes;
  /** cache for document*set*type*feature names */
  protected LinkedHashMap<String, TreeMap<String, TreeMap<String,
    TreeSet<String>>>> docsSetsTypesFeatures;
  protected String annotationKeySetName;
  protected String annotationResponseSetName;
  protected String documentKeySetName;
  protected String documentResponseSetName;
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
  protected JRadioButton documentMicroAverageButton;
  protected JRadioButton documentMacroAverageButton;
  protected Collator collator;
  protected Comparator<String> doubleComparator;
  protected Comparator<String> totalComparator;
  protected boolean corpusChanged;
  protected OpenDocumentAction openDocumentAction;
  protected OpenAnnotationDiffAction openAnnotationDiffAction;
  protected CompareAnnotationAction compareAnnotationAction;
  protected CompareDocumentAction compareDocumentAction;
  protected ReloadCacheAction reloadCacheAction;
  protected JProgressBar annotationProgressBar;
  protected JProgressBar documentProgressBar;
  protected Timer timer = new Timer("CorpusQualityAssurance", true);
  protected TimerTask timerTask;
}