/*
*	JapeGUI.java
*
*	Valentin Tablan, 22/May/2000
*
*	$Id$
*/
package gate.jape.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;

import gate.*;
import gate.gui.*;
import gate.util.*;



public class JapeGUI extends JFrame {

  public JapeGUI() {
    try  {
      jbInit();
      setVisible(true);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    corpus = Transients.newCorpus("JapeGUI");
    
  }

  public static void main(String[] args) {
    JapeGUI japeGUI = new JapeGUI();
    japeGUI.invokedStandalone = true;
  }

  private void jbInit() throws Exception {
    southBox = Box.createHorizontalBox();
    westBox = Box.createVerticalBox();
    centerBox = Box.createVerticalBox();
    northBox = Box.createHorizontalBox();
    this.getContentPane().setLayout(borderLayout1);
    statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
    statusBar.setMaximumSize(new Dimension(400, 17));
    statusBar.setMinimumSize(new Dimension(200, 17));
    statusBar.setPreferredSize(new Dimension(200, 17));
    statusBar.setToolTipText("Status bar");
    progressBar.setMaximumSize(new Dimension(400, 16));
    progressBar.setMinimumSize(new Dimension(200, 16));
    progressBar.setPreferredSize(new Dimension(200, 16));
    collectionAddBtn.setText("Add Document(s)");
    collectionAddBtn.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        collectionAddBtn_actionPerformed(e);
      }
    });
    japeLoadBtn.setText("Open grammar");
    japeLoadBtn.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        japeLoadBtn_actionPerformed(e);
      }
    });
    this.setTitle("Jape 2.0");
    runBtn.setText("Run!");
    runBtn.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        runBtn_actionPerformed(e);
      }
    });
    text.setBorder(BorderFactory.createLoweredBevelBorder());
    text.setEnabled(false);
    jLabel1.setHorizontalTextPosition(SwingConstants.LEFT);
    jLabel1.setText("Collection:");
    jLabel2.setHorizontalTextPosition(SwingConstants.LEFT);
    jLabel2.setText("Grammar:");
    grammarLbl.setHorizontalTextPosition(SwingConstants.LEFT);
    grammarLbl.setText("...");
    corpusList.addMouseListener(new java.awt.event.MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        corpusList_mouseClicked(e);
      }
    });
    corpusListModel.addElement("      ");
    corpusList.setModel(corpusListModel);
    typesPanel.setLayout(flowLayout1);
    jScrollPane1.setPreferredSize(new Dimension(32767, 32767));
    this.getContentPane().add(southBox, BorderLayout.SOUTH);
    southBox.add(statusBar, null);
    southBox.add(progressBar, null);
    this.getContentPane().add(westBox, BorderLayout.WEST);
    corpusList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//    String[] data={"...", "...", "...", "..."};
//    corpusList.setListData(data);
    westBox.add(jLabel1, null);
    westBox.add(corpusList, null);
    westBox.add(jLabel2, null);
    westBox.add(grammarLbl, null);
    this.getContentPane().add(centerBox, BorderLayout.CENTER);
    this.getContentPane().add(northBox, BorderLayout.NORTH);
    northBox.add(collectionAddBtn, null);
    northBox.add(japeLoadBtn, null);
    northBox.add(runBtn, null);
    centerBox.add(jScrollPane1, null);
    jScrollPane1.getViewport().add(text, null);
    centerBox.add(typesPanel, null);
    setSize(800,600);

    japeFilter = new ExtensionFileFilter();
    japeFilter.addExtension("jape");
    japeFilter.setDescription(".jape Files");

    filer = new JFileChooser();
    filer.addChoosableFileFilter(japeFilter);
  }

  void collectionAddBtn_actionPerformed(ActionEvent e) {
    filer.setSelectedFile(null);
    filer.setSelectedFiles(null);
    filer.setFileFilter(filer.getAcceptAllFileFilter());
    filer.setDialogTitle("Select document(s) to open...");
    filer.setMultiSelectionEnabled(true);
    filer.setSelectedFile(null);
    filer.setSelectedFiles(null);
    int res = filer.showOpenDialog(this);
    if(res == JFileChooser.APPROVE_OPTION){
      selectedFiles = filer.getSelectedFiles();
      if(selectedFiles != null){
        if(corpus == null) corpus = Transients.newCorpus("Jape 2.0");
        try{
          for(int i=0; i< selectedFiles.length; i++){
            corpus.add(Transients.newDocument(selectedFiles[i].toURL()));
          }
        }catch(java.net.MalformedURLException mue){
          mue.printStackTrace(System.err);
        }catch(IOException ioe){
          ioe.printStackTrace(System.err);
        }
      }//if(selectedFiles != null){

      corpusListModel.clear();
      Iterator docsIter = corpus.iterator();
      while(docsIter.hasNext()){
        currentDoc = (Document) docsIter.next();
        corpusListModel.addElement(currentDoc.getSourceURL().getFile());
      }
    }
  }

  void japeLoadBtn_actionPerformed(ActionEvent e) {
    filer.setFileFilter(japeFilter);
    filer.setDialogTitle("Open JAPE grammar");
    filer.setMultiSelectionEnabled(false);
    filer.setSelectedFile(null);
    filer.setSelectedFiles(null);
    int res = filer.showOpenDialog(this);
    if(res == JFileChooser.APPROVE_OPTION){
      grammarFile = filer.getSelectedFile();
      grammarLbl.setText(grammarFile.getName());
    }
  }

  void runBtn_actionPerformed(ActionEvent e) {
    //tokenize all documents
    Iterator docIter = corpus.iterator();
    while(docIter.hasNext()){
      currentDoc = (Document)docIter.next();
      tokenize(currentDoc);
    }
    //do the jape stuff
    updateAll();
  }

  public void tokenize(Document doc){
    String content = doc.getContent().getString();
    BreakIterator bi = BreakIterator.getWordInstance();
    bi.setText(content);
    int start = bi.first();
    FeatureMap fm = Transients.newFeatureMap();
    try{
      for (int end = bi.next();
           end != BreakIterator.DONE;
           start = end, end = bi.next())
      {
        if(!Character.isWhitespace(content.charAt(start))){
          doc.getAnnotations().add(new Long(start),
                                   new Long(end),
                                   "Token", fm);
//System.out.println("Token: " + content.substring(start, end));
        }
      }//for
    }catch(InvalidOffsetException ioe){
    }
  }

  void updateAll(){
    //display the current document
    text.setText(currentDoc.getContent().getString());
    //get all the annotation types and display the buttons
    typesPanel.removeAll();
    Iterator typesIter = currentDoc.getAnnotations().getAllTypes().iterator();
    String currentType;
    while(typesIter.hasNext()){
      currentType = (String) typesIter.next();
      JButton typeButton = new JButton();
      JLabel typeLabel = new JLabel(currentType);
      typeLabel.setBackground(Color.black);
      typeLabel.setForeground(Color.white);
      typeButton.add(typeLabel);
      typeButton.setBackground(new Color((int)Math.random()));
      typesPanel.add(typeButton);
    }
    validate();
  }

  void corpusList_mouseClicked(MouseEvent e) {
    //display the selected document
    int docIdx = corpusList.locationToIndex(e.getPoint());
    corpusList.setSelectedIndex(docIdx);
    docIdx++;
    Iterator docIter = corpus.iterator();
    while(docIter.hasNext() && docIdx >0 ){
      currentDoc = (Document)docIter.next();
      docIdx--;
    }
    if(docIdx != 0){
      throw(new RuntimeException(
                "The user has selected an unexistant document! :)"));
    }
    updateAll();
  }

  //Gui members
  JMenuBar jMenuBar1 = new JMenuBar();

  Box westBox;
  Box centerBox;
  Box northBox;
  Box southBox;

  BorderLayout borderLayout1 = new BorderLayout();
  JLabel statusBar = new JLabel();

  JButton collectionAddBtn = new JButton();
  JButton japeLoadBtn = new JButton();
  JButton runBtn = new JButton();

  JProgressBar progressBar = new JProgressBar();
  JEditorPane text = new JEditorPane();
  JLabel jLabel1 = new JLabel();
  DefaultListModel corpusListModel = new DefaultListModel();
  JList corpusList = new JList();
  JLabel jLabel2 = new JLabel();
  JLabel grammarLbl = new JLabel();

  Corpus corpus = null;
  JFileChooser filer;
  ExtensionFileFilter japeFilter;
  File[] selectedFiles;
  File grammarFile;

  private boolean invokedStandalone = false;
  Document currentDoc;
  JPanel typesPanel = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();
  JScrollPane jScrollPane1 = new JScrollPane();

}
