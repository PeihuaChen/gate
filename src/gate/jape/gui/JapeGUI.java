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
import gate.jape.*;



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
    textViewBox = Box.createVerticalBox();
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
    text.setEditorKit(new RawEditorKit());
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
//    corpusListModel.addElement("      ");
    corpusList.setModel(corpusListModel);
    typesPanel.setLayout(flowLayout1);
    textViewScroll.setPreferredSize(new Dimension(32767, 32767));
    typesPanel.setPreferredSize(null);
    this.getContentPane().add(southBox, BorderLayout.SOUTH);
    southBox.add(statusBar, null);
    southBox.add(progressBar, null);
    this.getContentPane().add(westBox, BorderLayout.WEST);
    corpusList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    westBox.add(jLabel1, null);
    westBox.add(corpusList, null);
    westBox.add(jLabel2, null);
    westBox.add(grammarLbl, null);
    this.getContentPane().add(northBox, BorderLayout.NORTH);
    northBox.add(collectionAddBtn, null);
    northBox.add(japeLoadBtn, null);
    northBox.add(runBtn, null);
    this.getContentPane().add(centerTabPane, BorderLayout.CENTER);
    centerTabPane.add(textViewBox, "Text View");
    textViewBox.add(textViewScroll, null);
    textViewBox.add(typesPanel, null);
    centerTabPane.add(tableViewScroll, "Table View");
    textViewScroll.getViewport().add(text, null);
    setSize(800,600);

    japeFilter = new ExtensionFileFilter();
    japeFilter.addExtension("jape");
    japeFilter.setDescription(".jape Files");

    filer = new JFileChooser();
    filer.addChoosableFileFilter(japeFilter);

  }

  void collectionAddBtn_actionPerformed(ActionEvent e) {
    //multi file selection enabled only in JDK 1.3
    if(System.getProperty("java.version").compareTo("1.3") >=0 ){
      //java 1.3
      filer.setMultiSelectionEnabled(true);
      filer.setDialogTitle("Select document(s) to add...");
      filer.setSelectedFiles(null);
      filer.setFileFilter(filer.getAcceptAllFileFilter());
      int res = filer.showDialog(this, "Open");
      File[] selectedFiles;
      if(res == JFileChooser.APPROVE_OPTION){
        selectedFiles = filer.getSelectedFiles();
        if(selectedFiles != null){
          if(corpus == null) corpus = Transients.newCorpus("Jape 2.0");
          try{
            for(int i=0; i< selectedFiles.length; i++){
              corpus.add(Transients.newDocument(selectedFiles[i].toURL()));
              corpusFiles.add(selectedFiles[i]);
            }
          }catch(java.net.MalformedURLException mue){
            mue.printStackTrace(System.err);
          }catch(IOException ioe){
            ioe.printStackTrace(System.err);
          }
        }//if(selectedFiles != null)
      }

    }else{
      //java 1.2
      filer.setMultiSelectionEnabled(false);
      filer.setDialogTitle("Select ONE document to add...");
      filer.setSelectedFile(null);
      filer.setFileFilter(filer.getAcceptAllFileFilter());
      int res = filer.showDialog(this, "Open");
      File selectedFile;
      if(res == JFileChooser.APPROVE_OPTION){
        selectedFile = filer.getSelectedFile();
        if(selectedFile != null){
          if(corpus == null) corpus = Transients.newCorpus("Jape 2.0");
          try{
              corpus.add(Transients.newDocument(selectedFile.toURL()));
              corpusFiles.add(selectedFile);
          }catch(java.net.MalformedURLException mue){
            mue.printStackTrace(System.err);
          }catch(IOException ioe){
            ioe.printStackTrace(System.err);
          }
        }//if(selectedFile != null)
      }
    }//java 1.2
    corpusListModel.clear();
    Iterator docsIter = corpus.iterator();
    while(docsIter.hasNext()){
      currentDoc = (Document) docsIter.next();
      corpusListModel.addElement(currentDoc.getSourceURL().getFile());
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
    if(corpus.isEmpty() || grammarFile == null) return;

    if(corpusIsDirty){
      corpus.clear();
      Iterator filesIter = corpusFiles.iterator();
      try{
        while(filesIter.hasNext()){
              corpus.add(Transients.newDocument(
                                    ((File)filesIter.next()).toURL()));
            }
      }catch(java.net.MalformedURLException mue){
        mue.printStackTrace(System.err);
      }catch(IOException ioe){
        ioe.printStackTrace(System.err);
      }

    }
    //tokenize all documents
    Iterator docIter = corpus.iterator();
    while(docIter.hasNext()){
      currentDoc = (Document)docIter.next();
      tokenize(currentDoc);
    }
    //do the jape stuff
    try{
      InputStream japeFileStream = new FileInputStream(grammarFile);
      if(japeFileStream == null)
        throw new JapeException("couldn't open " + grammarFile.getName());
      Batch batch = new Batch(japeFileStream);
      batch.transduce(corpus);
    }catch(FileNotFoundException fnfe){
      fnfe.printStackTrace(System.err);
    }catch(JapeException je){
      je.printStackTrace(System.err);
    }

    //select the first document
    docIter = corpus.iterator();
    if(docIter.hasNext()){
      currentDoc = (Document)docIter.next();
      corpusList.setSelectedIndex(0);
    }
    //repaint what needs to be repainted
    updateAll();
    corpusIsDirty = true;
  }

  public void tokenize(Document doc){
    String content = doc.getContent().getString();
    BreakIterator bi = BreakIterator.getWordInstance();
    bi.setText(content);
    int start = bi.first();
    FeatureMap fm;
    try{
      for (int end = bi.next();
           end != BreakIterator.DONE;
           start = end, end = bi.next())
      {
        if(!Character.isWhitespace(content.charAt(start))){
          fm = Transients.newFeatureMap();
          fm.put("string", content.substring(start, end));
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
    text.getHighlighter().removeAllHighlights();
    text.setText(currentDoc.getContent().getString());
    //get all the annotation types and display the buttons
    typesPanel.removeAll();
    Iterator typesIter = currentDoc.getAnnotations().getAllTypes().iterator();
    String currentType;

    JButton typeButton = new JButton();
    JLabel typeLabel = new JLabel("Clear all");
    typeLabel.setBackground(Color.black);
    typeLabel.setForeground(Color.white);
    typeButton.add(typeLabel);
    typeButton.setBackground(Color.black);
    typeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        typeButtonPressed("", null);
      }
    });
    typesPanel.add(typeButton);
    while(typesIter.hasNext()){
      currentType = (String) typesIter.next();
      typeButton = new JButton();
      typeLabel = new JLabel(currentType);
      typeLabel.setBackground(Color.black);
      typeLabel.setForeground(Color.white);
      typeButton.add(typeLabel);
      typeButton.setBackground(new Color(randomGen.nextInt()));
      typeButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(e.getSource() instanceof Container){
            Container cont = (Container) e.getSource();
            if(cont.getComponent(0) instanceof JLabel){
              Color col = cont.getBackground();
              Color highlightCol = new Color(col.getRed(),
                                             col.getGreen(),
                                             col.getBlue(),
                                             128);
              typeButtonPressed(((JLabel)cont.getComponent(0)).getText(),
                                highlightCol);
            }
          }
        }
      });
      typesPanel.add(typeButton);
    }
    //create the table
    tableView = new JTable(new AnnotationSetTableModel(currentDoc.getAnnotations()));
    tableViewScroll.getViewport().add(tableView, null);    
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

  void typeButtonPressed(String type, Color col){
    if(type.equals("")){
      text.getHighlighter().removeAllHighlights();
    }else{
      AnnotationSet as = currentDoc.getAnnotations().get(type);
      Iterator annIter = as.iterator();
      gate.Annotation currentAnn;
      int start, end;
      try{
        while(annIter.hasNext()){
          currentAnn = (gate.Annotation)annIter.next();
          start = currentAnn.getStartNode().getOffset().intValue();
          end = currentAnn.getEndNode().getOffset().intValue();
          text.getHighlighter().addHighlight(start, end,
            new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(col));
//                javax.swing.text.DefaultHighlighter.DefaultPainter);
        }
      }catch(javax.swing.text.BadLocationException ble){
        ble.printStackTrace(System.err);
      }
    }

//System.out.println(type);
  }

  class AnnotationSetTableModel extends javax.swing.table.AbstractTableModel{
    public AnnotationSetTableModel(AnnotationSet as){
      annotations = as.toArray();
    }

    public int getRowCount(){
      return annotations.length;
    }

    public int getColumnCount(){
      return 5;
    }

    public String getColumnName(int column){
      switch(column){
        case 0:{
          return "Start";
        }
        case 1:{
          return "End";
        }
        case 2:{
          return "Type";
        }
        case 3:{
          return "Features";
        }
        case 4:{
          return "Text";
        }
      }
      return null;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return false;
    }
    
    public Object getValueAt(int row, int column){
      gate.Annotation currentAnn = (gate.Annotation)annotations[row];
      switch(column){
        case 0:{
          return currentAnn.getStartNode().getOffset();
        }
        case 1:{
          return currentAnn.getEndNode().getOffset();
        }
        case 2:{
          return currentAnn.getType();
        }
        case 3:{
          return currentAnn.getFeatures();
        }
        case 4:{
          return currentDoc.getContent().getString().substring(
              currentAnn.getStartNode().getOffset().intValue(),
              currentAnn.getEndNode().getOffset().intValue());
        }
      }
      return null;
    }
    Object[] annotations;
  }

  //Gui members
  JMenuBar jMenuBar1 = new JMenuBar();

  Box westBox;
  Box textViewBox;
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
  /** A set of objects of type File containing all the files that should go in
    * the corpus.
    */
  Set corpusFiles = new HashSet();
  File grammarFile;

  private boolean invokedStandalone = false;
  Document currentDoc;
  JPanel typesPanel = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();
  JScrollPane textViewScroll = new JScrollPane();
  Random randomGen = new Random();
  boolean corpusIsDirty = false;
  JTabbedPane centerTabPane = new JTabbedPane();
  JTable tableView;
  JScrollPane tableViewScroll = new JScrollPane();
  javax.swing.table.TableModel tm;
}
