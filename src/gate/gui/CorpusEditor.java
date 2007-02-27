/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 12/07/2001
 *
 *  $Id$
 *
 */
package gate.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import gate.*;
import gate.creole.AbstractVisualResource;
import gate.event.CorpusEvent;
import gate.event.CorpusListener;
import gate.swing.XJTable;
import gate.util.GateException;
import gate.util.GateRuntimeException;

/**
 * A simple viewer/editor for corpora. It will allow the visualisation of the
 * list of documents inside a corpus along withe their features.
 * It will also allow addition and removal of documents.
 */
public class CorpusEditor extends AbstractVisualResource implements CorpusListener {

  public Resource init(){
    initLocalData();
    initGuiComponents();
    initListeners();
    return this;
  }


  protected void initLocalData(){
    docTableModel = new DocumentTableModel();
  }

  protected void initGuiComponents(){
    setLayout(new BorderLayout());
    renderer = new DocumentNameRenderer();
    
    docTable = new XJTable(docTableModel);
    docTable.setSortable(true);
    docTable.setSortedColumn(DocumentTableModel.COL_INDEX);
    docTable.setAutoResizeMode(XJTable.AUTO_RESIZE_OFF);
    docTable.getColumnModel().getColumn(DocumentTableModel.COL_NAME).
        setCellRenderer(renderer);
//    docTable.setShowGrid(false);

    JScrollPane scroller = new JScrollPane(docTable);
    scroller.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scroller.getViewport().setBackground(docTable.getBackground());
    add(scroller);

    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.add(new NewDocumentAction());
    toolbar.add(new RemoveDocumentsAction());
    toolbar.addSeparator();
    toolbar.add(new MoveUpAction());
    toolbar.add(new MoveDownAction());

    add(toolbar, BorderLayout.NORTH);
  }

  protected void initListeners(){
    docTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2){
          int row = docTable.rowAtPoint(e.getPoint());
          if(row != -1){
            row = docTable.rowViewToModel(row);
            Document doc = (Document) corpus.get(row);
            //try to select the document in the main frame
            Component root = SwingUtilities.getRoot(CorpusEditor.this);
            if(root instanceof MainFrame){
              MainFrame mainFrame = (MainFrame)root;
              mainFrame.select(doc);
            }
          }
        }
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }
    });
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
        "The GATE corpus editor can only be used with a GATE corpus!\n" +
        target.getClass().toString() + " is not a GATE corpus!");
    }
    this.corpus = (Corpus)target;
    corpus.addCorpusListener(this);
    docTableModel.dataChanged();
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        docTableModel.fireTableDataChanged();
      }
    });
  }

  public void documentAdded(final CorpusEvent e) {
    docTableModel.dataChanged();
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        docTableModel.fireTableRowsInserted(e.getDocumentIndex(), 
                e.getDocumentIndex());
      }
    });
  }

  public void documentRemoved(final CorpusEvent e) {
    docTableModel.dataChanged();
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        docTableModel.fireTableRowsDeleted(e.getDocumentIndex(), 
                e.getDocumentIndex());
      }
    });
  }

  class DocumentTableModel extends AbstractTableModel{
    public DocumentTableModel(){
      documentNames = new ArrayList<String>();
    }
    
    /**
     * Called externally when the underlying corpus has changed.
     */
    private void dataChanged(){
      List<String> newDocs = new ArrayList<String>();
      if(corpus != null){
        newDocs.addAll((List<String>)corpus.getDocumentNames());
     }
      List<String> oldDocs = documentNames;
      documentNames = newDocs;
      oldDocs.clear();
    }
    
    public int getColumnCount() {
      return COLUMN_COUNT;
    }

    public int getRowCount() {
      return documentNames.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      //invalid indexes might appear when update events are slow to act 
      if(rowIndex < 0 || rowIndex >= documentNames.size() || 
         columnIndex < 0 || columnIndex > COLUMN_COUNT) return null;
      switch(columnIndex) {
        case COL_INDEX:
          return new Integer(rowIndex);
        case COL_NAME:
          return documentNames.get(rowIndex);
        default:
          return null;
      }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      switch(columnIndex) {
        case COL_INDEX:
          return Integer.class;
        case COL_NAME:
          return String.class;
        default:
          return String.class;
      }
    }

    @Override
    public String getColumnName(int column) {
      return COLUMN_NAMES[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }
    
    private List<String> documentNames;
    private final String[] COLUMN_NAMES = {"Index", "Name"}; 
    private static final int COL_INDEX = 0;
    private static final int COL_NAME = 1;
    private static final int COLUMN_COUNT = 2;
  }

  class DocumentNameRenderer extends DefaultTableCellRenderer implements 
      ListCellRenderer{
    public DocumentNameRenderer(){
      super();
      setIcon(MainFrame.getIcon("document"));
    }
    
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
      // prepare the renderer

      return getTableCellRendererComponent(docTable, value, isSelected, 
              cellHasFocus, index, docTableModel.COL_NAME);
    }
  }
  
  class MoveUpAction extends AbstractAction{
    public MoveUpAction(){
      super("Move up", MainFrame.getIcon("up"));
      putValue(SHORT_DESCRIPTION, "Moves selected document(s) up.");
    }

    public void actionPerformed(ActionEvent e) {
      int[] rowsTable = docTable.getSelectedRows();
      int[] rowsCorpus = new int[rowsTable.length];
      for(int i = 0; i < rowsTable.length; i++)
        rowsCorpus[i] = docTable.rowViewToModel(rowsTable[i]);
      Arrays.sort(rowsCorpus);
      //starting from the smallest one, move each element up
      for(int i = 0; i < rowsCorpus.length; i++){
        if(rowsCorpus[i] > 0){
          //swap the doc with the one before
          Object doc = corpus.remove(rowsCorpus[i]);
          rowsCorpus[i] = rowsCorpus[i] - 1;
          corpus.add(rowsCorpus[i], doc);
        }
      }
      //restore selection
      //the remove / add events will cause the table to be updated
      //we need to only restore the selection after that happened
      final int[] selectedRowsCorpus = new int[rowsCorpus.length];
      System.arraycopy(rowsCorpus, 0, selectedRowsCorpus, 0, rowsCorpus.length);
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          docTable.clearSelection();
          for(int i = 0; i < selectedRowsCorpus.length; i++){
            int rowTable = docTable.rowModelToView(selectedRowsCorpus[i]);
            docTable.getSelectionModel().addSelectionInterval(rowTable, 
                    rowTable);
          }                
        }
      });
    }
    
  }

  class MoveDownAction extends AbstractAction{
    public MoveDownAction(){
      super("Move down", MainFrame.getIcon("down"));
      putValue(SHORT_DESCRIPTION, "Moves selected document(s) down.");
    }

    public void actionPerformed(ActionEvent e) {
      int[] rowsTable = docTable.getSelectedRows();
      int[] rowsCorpus = new int[rowsTable.length];
      for(int i = 0; i < rowsTable.length; i++)
        rowsCorpus[i] = docTable.rowViewToModel(rowsTable[i]);
      Arrays.sort(rowsCorpus);
      //starting from the largest one, move each element down
      for(int i = rowsCorpus.length -1; i >=0; i--){
        if(rowsCorpus[i] < corpus.size() -1){
          //swap the doc with the one before
          Object doc = corpus.remove(rowsCorpus[i]);
          rowsCorpus[i]++;
          corpus.add(rowsCorpus[i], doc);
        }
      }
      //restore selection
      //the remove / add events will cause the table to be updated
      //we need to only restore the selection after that happened
      final int[] selectedRowsCorpus = new int[rowsCorpus.length];
      System.arraycopy(rowsCorpus, 0, selectedRowsCorpus, 0, rowsCorpus.length);
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          docTable.clearSelection();
          for(int i = 0; i < selectedRowsCorpus.length; i++){
            int rowTable = docTable.rowModelToView(selectedRowsCorpus[i]);
            docTable.getSelectionModel().addSelectionInterval(rowTable, 
                    rowTable);
          }                
        }
      });
    }
    
  }
  class NewDocumentAction extends AbstractAction{
    public NewDocumentAction(){
      super("Add document", MainFrame.getIcon("add-document"));
      putValue(SHORT_DESCRIPTION, "Add a new document to this corpus");
    }

    public void actionPerformed(ActionEvent e){
      try{
        //get all the documents loaded in the system
        java.util.List loadedDocuments = Gate.getCreoleRegister().
                               getAllInstances("gate.Document");
        if(loadedDocuments == null || loadedDocuments.isEmpty()){
          JOptionPane.showMessageDialog(
              CorpusEditor.this,
              "There are no documents available in the system!\n" +
              "Please load some and try again!" ,
              "GATE", JOptionPane.ERROR_MESSAGE);
          return;
        }

        Vector docNames = new Vector(loadedDocuments.size());
        for (int i = 0; i< loadedDocuments.size(); i++) {
          docNames.add(((Document)loadedDocuments.get(i)).getName());
        }
        JList docList = new JList(docNames);
        docList.setCellRenderer(renderer);

        JOptionPane dialog = new JOptionPane(new JScrollPane(docList),
                                             JOptionPane.QUESTION_MESSAGE,
                                             JOptionPane.OK_CANCEL_OPTION);
        dialog.createDialog(CorpusEditor.this,
                            "Add document(s) to corpus").setVisible(true);

        if(((Integer)dialog.getValue()).intValue() == JOptionPane.OK_OPTION){
          int[] selection = docList.getSelectedIndices();
          for (int i = 0; i< selection.length ; i++) {
            corpus.add(loadedDocuments.get(selection[i]));
          }
        }
      }catch(GateException ge){
        //gate.Document is not registered in creole.xml....what is!?
        throw new GateRuntimeException(
          "gate.Document is not registered in the creole register!\n" +
          "Something must be terribly wrong...take a vacation!");
      }
    }
  }//class NewDocumentAction extends AbstractAction

  class RemoveDocumentsAction extends AbstractAction{
    public RemoveDocumentsAction(){
      super("Remove documents", MainFrame.getIcon("remove-document"));
      putValue(SHORT_DESCRIPTION, "Removes selected documents from this corpus");
    }

    public void actionPerformed(ActionEvent e){
      int[] selectedIndexes = docTable.getSelectedRows();
      int[] corpusIndexes = new int[selectedIndexes.length];
      for(int i = 0; i < selectedIndexes.length; i++)
        corpusIndexes[i] = docTable.rowViewToModel(selectedIndexes[i]);
      Arrays.sort(corpusIndexes);
      //remove the document starting with the one with the highest index
      for(int i = corpusIndexes.length-1; i >= 0; i--){
        corpus.remove(corpusIndexes[i]);
      }
//      documentsList.clearSelection();
    }
  }//class RemoveDocumentsAction extends AbstractAction


//  protected JList documentsList;
  protected XJTable docTable;
  protected DocumentTableModel docTableModel;
  protected DocumentNameRenderer renderer;
  protected JToolBar toolbar;
  protected Corpus corpus;
}
