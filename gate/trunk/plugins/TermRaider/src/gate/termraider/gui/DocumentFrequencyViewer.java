/*
 *  Copyright (c) 2008--2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */
package gate.termraider.gui;

import gate.Resource;
import gate.creole.ANNIEConstants;
import gate.creole.AbstractVisualResource;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.event.ProgressListener;
import gate.termraider.bank.*;
import gate.termraider.util.*;
import java.awt.BorderLayout;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;


@CreoleResource(name = "Document Frequency Viewer",
        comment = "viewer for the TermRaider DocumentFrequencyBank",
        guiType = GuiType.LARGE,
        mainViewer = true,
        resourceDisplayed = "gate.termraider.bank.DocumentFrequencyBank")
public class DocumentFrequencyViewer 
  extends AbstractVisualResource 
  implements ANNIEConstants, ProgressListener {

  private static final long serialVersionUID = 5632849477601995493L;
  
  private JScrollPane freqScrollPane;
  private DocumentFrequencyBank dfb;
  private JTable freqTable, typeTable, langTable;
  private JTabbedPane tabbedPane;
  private DFTableModel freqTableModel;
  private ListTableModel typeTableModel, langTableModel;
  private JTextField docsField;
  
  @Override
  public Resource init() {
    initGuiComponents();
    return this;
  }


  private void initGuiComponents() {
    setLayout(new BorderLayout());
    tabbedPane = new JTabbedPane();
    JPanel dfTab = new JPanel(new BorderLayout());
    tabbedPane.addTab("Document frequencies", dfTab);
    
    docsField = new JTextField("...");
    dfTab.add(docsField, BorderLayout.NORTH);

    freqTableModel = new DFTableModel();
    freqTable = new JTable(freqTableModel);
    freqTable.setAutoCreateRowSorter(true);
    freqScrollPane = new JScrollPane(freqTable, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    dfTab.add(freqScrollPane, BorderLayout.CENTER);
    
    JSplitPane listsTab = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    typeTableModel = new ListTableModel("Term annotation types");
    typeTable = new JTable(typeTableModel);
    langTableModel = new ListTableModel("Language codes");
    langTable = new JTable(langTableModel);
    listsTab.setLeftComponent(typeTable);
    listsTab.setRightComponent(langTable);
    tabbedPane.addTab("Types and languages", listsTab);
    
    // TODO
    // wrap each table in a pane with optional scrolling
    /*
         termTable.setAutoCreateRowSorter(true);
    pairTable.setAutoCreateRowSorter(true);
    termPane = new JScrollPane(termTable, 
    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pairPane = new JScrollPane(pairTable, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    splitPane.setLeftComponent(termPane);
    splitPane.setRightComponent(pairPane);

     */
    
    this.add(tabbedPane, BorderLayout.CENTER);
    tabbedPane.validate();
    tabbedPane.repaint();
  }
  
  
  public void processFinished() {
    setTarget(dfb);
  }

  public void progressChanged(int i) {
    // nothing?
  }  

  public void setTarget(Object target) {
    if(target == null || ! (target instanceof DocumentFrequencyBank)) {
      throw new IllegalArgumentException("This Viewer cannot show a "
              + (target == null ? "null" : target.getClass().toString()));
    }
    
    dfb = (DocumentFrequencyBank) target;
    docsField.setText("Doc count = " + this.dfb.getTotalDocs());
    freqTableModel.setBank(this.dfb);
    typeTableModel.setList(dfb.getTypes());
    langTableModel.setList(dfb.getLanguages());
  }
}


class DFTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 7536874522584055763L;

  private List<Term> terms;
  private String[] columnNames = {"term", "doc frequency"};
  private Map<Term, Integer> docFrequencies; 

  public DFTableModel() {
    this.docFrequencies = new HashMap<Term, Integer>();
    this.terms = new ArrayList<Term>();
  }
  
  public void setBank(DocumentFrequencyBank termbank) {
    this.docFrequencies = termbank.getDocFrequencies();
    this.terms = new ArrayList<Term>(docFrequencies.keySet());
    Collections.sort(this.terms, new TermComparator());
  }
  
  public int getColumnCount() {
    return 2;
  }

  public int getRowCount() {
    return this.terms.size();
  }

  public Object getValueAt(int row, int col) {
    Term term = this.terms.get(row); 
    if (col == 0) {
      return term.toString();
    }
    // implied else
    if (this.docFrequencies.containsKey(term)) {
      return this.docFrequencies.get(term);
    }
    return 0;
  }
  
  public Class<?> getColumnClass(int col) {
    if (col == 0) {
      return String.class;
    }
    // implied else
    return Integer.class;
  }
  
  public String getColumnName(int col) {
    return columnNames[col];
  }

}



class ListTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 8277085631925984080L;
  
  private List<String> strings;
  private String heading;

  public ListTableModel(String heading) {
    this.heading = heading;
  }
  
  public void setList(Collection<String> strings) {
    this.strings = new ArrayList<String>(strings);
    Collections.sort(this.strings);
  }
  
  public int getColumnCount() {
    return 1;
  }

  public int getRowCount() {
    return this.strings.size();
  }

  public Object getValueAt(int row, int col) {
    return this.strings.get(row);
  }
  
  public Class<?> getColumnClass(int col) {
    return String.class;
  }
  
  public String getColumnName(int col) {
    return heading;
  }

}
