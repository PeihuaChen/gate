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
  private JTable freqTable;
  private JTabbedPane tabbedPane;
  private DFTableModel freqTableModel;
  private JTextField docsField;
  
  @Override
  public Resource init() {
    initGuiComponents();
    return this;
  }


  private void initGuiComponents() {
    setLayout(new BorderLayout());
    tabbedPane = new JTabbedPane();
    JPanel tableTab = new JPanel(new BorderLayout());
    tabbedPane.addTab("Document Frequency", tableTab);
    
    docsField = new JTextField("...");
    tableTab.add(docsField, BorderLayout.NORTH);

    freqTableModel = new DFTableModel();
    freqTable = new JTable(freqTableModel);
    freqTable.setAutoCreateRowSorter(true);
    freqScrollPane = new JScrollPane(freqTable, 
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    tableTab.add(freqScrollPane, BorderLayout.CENTER);
    
    this.add(tabbedPane, BorderLayout.CENTER);
    tabbedPane.validate();
    tabbedPane.repaint();
  }
  
  
  private void setDocsField() {
    docsField.setText("Doc count = " + this.dfb.getTotalDocs());
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
    setDocsField();
    freqTableModel.setBank(this.dfb);
  }
}


class DFTableModel extends AbstractTableModel {
  private static final long serialVersionUID = -7654670667296912991L;
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
