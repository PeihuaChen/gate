/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
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

import gate.creole.AbstractVisualResource;
import gate.*;
import gate.util.*;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import gate.event.*;

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
    docListModel = new DefaultListModel();
  }

  protected void initGuiComponents(){
    setLayout(new BorderLayout());

    documentsList = new JList(docListModel);
    documentsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    listRenderer = new DocumentListCellRenderer();
    documentsList.setCellRenderer(listRenderer);
    JScrollPane listScroll = new JScrollPane(documentsList);

//    featuresEditor = new FeaturesEditor();
//    JScrollPane fEdScroll = new JScrollPane(featuresEditor);
//
//    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
//                                          listScroll, fEdScroll);
//    mainSplit.setDividerLocation(0.30);
//    add(mainSplit, BorderLayout.CENTER);

    add(listScroll, BorderLayout.CENTER);

    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.add(new NewDocumentAction());
    toolbar.add(new RemoveDocumentsAction());

    add(toolbar, BorderLayout.NORTH);
  }

  protected void initListeners(){
/*
//kalina: I commented it, because we want the corpus viewer to show only the
//document names and not add the documents to memory
    documentsList.addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e){
        featuresEditor.setTarget(
          (docListModel.isEmpty() || documentsList.getSelectedIndex() == -1) ?
          null : docListModel.get(documentsList.getSelectedIndex())
        );
      }
    });
*/
    documentsList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2){
          int row = documentsList.locationToIndex(e.getPoint());
          if(row != -1){
            Document doc = (Document) corpus.get(row);
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

  public void setTarget(Object target){
    if(!(target instanceof Corpus)){
      throw new IllegalArgumentException(
        "The GATE corpus editor can only be used with a GATE corpus!\n" +
        target.getClass().toString() + " is not a GATE corpus!");
    }
    this.corpus = (Corpus)target;
    corpus.addCorpusListener(this);

    docListModel.clear();
    java.util.List docNamesList = corpus.getDocumentNames();
    Iterator namesIter = docNamesList.iterator();
    while(namesIter.hasNext()){
      String docName = (String) namesIter.next();
      docListModel.addElement(docName);
    }

    if(!docListModel.isEmpty())
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          documentsList.setSelectedIndex(0);
        }
      });
  }

  public void documentAdded(final CorpusEvent e) {
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        //a new document has been added to the corpus
        Document doc = e.getDocument();
        //let's find where it should go
        int docPosition = 0;
        Iterator iter = corpus.getDocumentNames().iterator();
        while(iter.hasNext() && ! iter.next().equals(doc.getName()))
          docPosition++;
        int[] selIdxs = documentsList.getSelectedIndices();
        docListModel.insertElementAt(doc.getName(), docPosition);
        //restore selection
        for(int i = 0; i < selIdxs.length; i++){
          if(selIdxs[i] >= docPosition) selIdxs[i]++;
        }//for(int i = 0; i < selIdxs.length; i++)
        documentsList.setSelectedIndices(selIdxs);
      }
    });
  }

  public void documentRemoved(final CorpusEvent e) {
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        docListModel.removeElementAt(e.getDocumentIndex());
      }
    });
  }


  class DocumentListCellRenderer extends DefaultListCellRenderer{
    public Component getListCellRendererComponent(JList list,
                                              Object value,
                                              int index,
                                              boolean isSelected,
                                              boolean cellHasFocus){
      //prepare the renderer
      String docName = (String)value;
      super.getListCellRendererComponent(list, docName, index,
                                         isSelected, cellHasFocus);
      setIcon(MainFrame.getIcon("lr.gif"));
      return this;
    }
  }


  class NewDocumentAction extends AbstractAction{
    public NewDocumentAction(){
      super("Add document", MainFrame.getIcon("add.gif"));
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
              "Gate", JOptionPane.ERROR_MESSAGE);
          return;
        }

        Vector docNames = new Vector(loadedDocuments.size());
        for (int i = 0; i< loadedDocuments.size(); i++) {
          docNames.add(((Document)loadedDocuments.get(i)).getName());
        }
        JList docList = new JList(docNames);
        docList.setCellRenderer(listRenderer);

        JOptionPane dialog = new JOptionPane(new JScrollPane(docList),
                                             JOptionPane.QUESTION_MESSAGE,
                                             JOptionPane.OK_CANCEL_OPTION);
        dialog.createDialog(CorpusEditor.this,
                            "Add document(s) to corpus").show();

        if(((Integer)dialog.getValue()).intValue() == dialog.OK_OPTION){
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
      super("Rmove documents", MainFrame.getIcon("remove.gif"));
      putValue(SHORT_DESCRIPTION, "Removes selected documents from this corpus");
    }

    public void actionPerformed(ActionEvent e){
      int[] selectedIndexes = documentsList.getSelectedIndices();
      for(int i = selectedIndexes.length-1; i >= 0; i--){
        corpus.remove(selectedIndexes[i]);
      }
      documentsList.clearSelection();
    }
  }//class RemoveDocumentsAction extends AbstractAction


  JList documentsList;
  DocumentListCellRenderer listRenderer;
  FeaturesEditor featuresEditor;
  JToolBar toolbar;
  Corpus corpus;
  DefaultListModel docListModel;
}
