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

import gate.*;
import gate.gui.*;



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
  private boolean invokedStandalone = false;
  JMenuBar jMenuBar1 = new JMenuBar();
  JToolBar toolBar = new JToolBar();
  BorderLayout borderLayout1 = new BorderLayout();
  Box southBox;
  JLabel statusBar = new JLabel();
  JProgressBar progressBar = new JProgressBar();
  JButton collectionAddBtn = new JButton();
  JButton japeLoadBtn = new JButton();
  JButton runBtn = new JButton();
  JEditorPane text = new JEditorPane();

  private void jbInit() throws Exception {
    southBox = Box.createHorizontalBox();
    westBox = Box.createVerticalBox();
    this.getContentPane().setLayout(borderLayout1);
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
    this.getContentPane().add(toolBar, BorderLayout.NORTH);
    toolBar.add(collectionAddBtn, null);
    toolBar.add(japeLoadBtn, null);
    toolBar.add(runBtn, null);
    this.getContentPane().add(southBox, BorderLayout.SOUTH);
    southBox.add(statusBar, null);
    southBox.add(progressBar, null);
    this.getContentPane().add(text, BorderLayout.CENTER);
    this.getContentPane().add(westBox, BorderLayout.WEST);
    corpusList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//    String[] data={"...", "...", "...", "..."};
//    corpusList.setListData(data);
    westBox.add(jLabel1, null);
    westBox.add(corpusList, null);
    westBox.add(jLabel2, null);
    westBox.add(grammarLbl, null);
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
      corpusListModel.clear();
      for(int i=0; i< selectedFiles.length; i++){
        corpusListModel.addElement(selectedFiles[i].getName());
      }
      corpusList.repaint();
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
    if(selectedFiles == null || grammarFile == null) return;
    corpus = Transients.newCorpus("Jape 2.0");
    try{
      for(int i=0; i< selectedFiles.length; i++){
        corpus.add(Transients.newDocument(selectedFiles[i].toURL()));
      }
    }catch(java.net.MalformedURLException mue){
      mue.printStackTrace(System.err);
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
    }
    //tokenize all documents
    //do the jape stuff
  }

  void corpusList_mouseClicked(MouseEvent e) {
    //display the selected document
  }
  
  Corpus corpus = null;
  JFileChooser filer;
  ExtensionFileFilter japeFilter;
  File[] selectedFiles;
  File grammarFile;

  Box westBox;
  JLabel jLabel1 = new JLabel();
  DefaultListModel corpusListModel = new DefaultListModel();
  JList corpusList = new JList();
  JLabel jLabel2 = new JLabel();
  JLabel grammarLbl = new JLabel();


}
