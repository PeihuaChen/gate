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
import java.net.*;
import java.util.*;
import java.text.*;

import gate.*;
import gate.gui.*;
import gate.util.*;
import gate.jape.*;
import gate.creole.tokeniser.*;
import gate.creole.gazeteer.*;



/** A small toy inteface for Jape testing and tweaking */
public class JapeGUI extends JFrame implements ProgressListener,
                                               StatusListener,
                                               Runnable {

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

  public void setCorpus(Corpus corpus){
    this.corpus = corpus;
    corpusListModel.clear();
    Iterator docsIter = corpus.iterator();
    while(docsIter.hasNext()){
      currentDoc = (Document) docsIter.next();
      corpusListModel.addElement(currentDoc.getSourceURL().getFile());
    }
    //select the first document
    Iterator docIter = corpus.iterator();
    if(docIter.hasNext()){
      currentDoc = (Document)docIter.next();
      corpusList.setSelectedIndex(0);
    }
    //repaint what needs to be repainted
    updateAll();
  }

  private void jbInit() throws Exception {
    southBox = Box.createHorizontalBox();
    westBox = Box.createVerticalBox();
    northBox = Box.createHorizontalBox();
    this.getContentPane().setLayout(borderLayout1);
    statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
    statusBar.setMaximumSize(new Dimension(30000, 17));
    statusBar.setMinimumSize(new Dimension(200, 17));
    statusBar.setPreferredSize(new Dimension(30000, 17));
    statusBar.setToolTipText("Status bar");
    progressBar.setMaximumSize(new Dimension(300, 16));
    progressBar.setMinimumSize(new Dimension(300, 16));
    progressBar.setPreferredSize(new Dimension(300, 16));
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
    this.addWindowListener(new WindowListener(this));
    runBtn.setText("Run!");
    runBtn.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        runBtn_actionPerformed(e);
      }
    });
    text.setEditorKit(new RawEditorKit());
    text.setBorder(BorderFactory.createLoweredBevelBorder());
    text.setEditable(false);
    text.setEnabled(true);
    text.setPreferredSize(new Dimension(textViewScroll.getSize().width - 10,
                                        32000));
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
//    textViewScroll.setPreferredSize(new Dimension(32767, 32767));
//    textViewScroll.setPreferredSize(null);
//    typesPanel.setPreferredSize(null);
    logTextArea.setDisabledTextColor(Color.lightGray);
    logTextArea.setEditable(false);
    tokRulesBtn.setText("Load Tokeniser Rules");
    tokRulesBtn.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        tokRulesBtn_actionPerformed(e);
      }
    });
    gazeteerBtn.setText("Load Gazeteer Lists");
    gazeteerBtn.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        gazeteerBtn_actionPerformed(e);
      }
    });
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
    northBox.add(tokRulesBtn, null);
    northBox.add(gazeteerBtn, null);
    northBox.add(runBtn, null);
    this.getContentPane().add(centerTabPane, BorderLayout.CENTER);
    textViewPane.setDividerLocation(400);
    centerTabPane.add(textViewPane, "Text View");
    textViewPane.add(typesPanel, JSplitPane.BOTTOM);
    textViewPane.add(textViewScroll, JSplitPane.TOP);
    textViewScroll.getViewport().add(text, null);
    centerTabPane.add(tableViewScroll, "Table View");
    centerTabPane.add(logScrollPane, "Log");
    logScrollPane.getViewport().add(logTextArea, null);
    setSize(800,600);
    validate();
//    textViewScroll.setPreferredSize(new Dimension(textViewPane.getSize().width,
//                                    textViewPane.getSize().height - 30));
    japeFilter = new ExtensionFileFilter();
    japeFilter.addExtension("jape");
    japeFilter.setDescription("Jape grammars");

    tokFileFilter = new ExtensionFileFilter();
    tokFileFilter.addExtension("rules");
    tokFileFilter.setDescription("Tokeniser rules files");

    gazFileFilter = new ExtensionFileFilter();
    gazFileFilter.addExtension("def");
    gazFileFilter.setDescription("Gazeteer list definition files");

    filer = new JFileChooser("d:/tmp");
    filer.addChoosableFileFilter(japeFilter);
    filer.addChoosableFileFilter(tokFileFilter);

  }

  void collectionAddBtn_actionPerformed(ActionEvent e) {
    Document currDoc = null;
    //multi file selection enabled only in JDK 1.3
    if(System.getProperty("java.version").compareTo("1.3") >=0 ){
      //java 1.3 or better
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
              currDoc = Transients.newDocument(selectedFiles[i].toURL());
              corpus.add(currDoc);
              corpusFiles.add(selectedFiles[i].toURL().toExternalForm());
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
              currDoc = Transients.newDocument(selectedFile.toURL());
              corpus.add(currDoc);
              corpusFiles.add(selectedFile.toURL().toExternalForm());
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
    if(currDoc != null) currentDoc = currDoc;
    updateAll();
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

  public void run() {
    startCorpusLoad = 0;
    startCorpusTokenization = 0;
    startLookupLoad = 0;
    startLookup = 0;
    startJapeFileOpen = 0;
    startCorpusTransduce = 0;
    endProcess = 0;
    if(corpus.isEmpty() || grammarFile == null|| tokeniserRulesFile == null){
      statusBar.setText("Missing corpus, grammar or tokeniser rules!");
      return;
    }
    logTextArea.append("Started at: " + (new Date()) + "\n");
    startCorpusLoad = System.currentTimeMillis();

    if(corpusIsDirty){
      statusBar.setText("Reloading the corpus...");
      corpus.clear();
      int progress = 0;
      int fileCnt = corpusFiles.size();
      Iterator filesIter = corpusFiles.iterator();
      try{
        while(filesIter.hasNext()){
              progressBar.setValue(progress++/fileCnt);
              corpus.add(Transients.newDocument(
                new URL((String)filesIter.next()))
              );
              progressBar.setValue(progress/fileCnt);
            }
      }catch(java.net.MalformedURLException mue){
        progressBar.setValue(0);
        statusBar.setText(mue.toString());
        mue.printStackTrace(System.err);
      }catch(IOException ioe){
        progressBar.setValue(0);
        statusBar.setText(ioe.toString());
        ioe.printStackTrace(System.err);
      }
      progressBar.setValue(0);
    }
    //tokenize all documents
    startCorpusTokenization = System.currentTimeMillis();
    logTextArea.append("corpus loading time: " +
                       (startCorpusTokenization - startCorpusLoad) +
                       "ms\n");

    int docCnt = corpus.size();
    statusBar.setText("Tokenizing all the documents...");
    try{
      tokeniser =new DefaultTokeniser(tokeniserRulesFile.getAbsolutePath());
    }catch(IOException ioe){
      System.err.println("Cannot read the tokeniser rules!" +
                         "\nAre the Gate resources in place?");
    }catch(TokeniserException te){
      te.printStackTrace(System.err);
    }
    tokeniser.addProcessProgressListener(this);
    tokeniser.addStatusListener(this);

    Iterator docIter = corpus.iterator();
    while(docIter.hasNext()){
      currentDoc = (Document)docIter.next();
      tokeniser.tokenise(currentDoc, false);
    }
    startLookupLoad = System.currentTimeMillis();
    logTextArea.append("corpus tokenization time: " +
                       (startLookupLoad - startCorpusTokenization) +
                       "ms\n");

    //gazeteer lookup
    docCnt = corpus.size();
    statusBar.setText("Doing gazeteer lookup...");
    startLookup = startLookupLoad;
    try{
      if(null != gazeteerListDefFile){
        gazeteer =new DefaultGazeteer(gazeteerListDefFile.getAbsolutePath());
        gazeteer.addProcessProgressListener(this);
        gazeteer.addStatusListener(this);
        gazeteer.init();
        startLookup = System.currentTimeMillis();
        logTextArea.append("gazeteer lists load time: " +
                           (startLookup - startLookupLoad) +
                           "ms\n");
        docIter = corpus.iterator();
        while(docIter.hasNext()){
          currentDoc = (Document)docIter.next();
          gazeteer.doLookup(currentDoc, false);
        }
      }
    }catch(IOException ioe){
      System.err.println("Cannot read the gazeteer lists!" +
                         "\nAre the Gate resources in place?");
    }catch(GazeteerException ge){
      ge.printStackTrace(System.err);
    }
//============================


    //do the jape stuff
    try { Gate.init(); } catch(Exception e) { System.err.println(e); }
    progressBar.setValue(0);
    startJapeFileOpen = System.currentTimeMillis();
    logTextArea.append("gazeteer lookup time: " +
                       (startJapeFileOpen - startLookup) +
                       "ms\n");
    try{
      statusBar.setText("Opening Jape grammar...");
      InputStream japeFileStream = new FileInputStream(grammarFile);
      if(japeFileStream == null)
        throw new JapeException("couldn't open " + grammarFile.getName());
      Batch batch = new Batch(grammarFile.getAbsolutePath());
      startCorpusTransduce = System.currentTimeMillis();
      logTextArea.append("JAPE structures build time: " +
                         (startCorpusTransduce - startJapeFileOpen) +
                         "ms\n");
      batch.addProcessProgressListener(this);
      batch.addStatusListener(this);
      batch.transduce(corpus);
      endProcess = System.currentTimeMillis();
      logTextArea.append("transducing time: " +
                         (endProcess - startCorpusTransduce) + "ms\n");
    }catch(FileNotFoundException fnfe){
      fnfe.printStackTrace(System.err);
    }catch(JapeException je){
      je.printStackTrace(System.err);
    }
    statusBar.setText("");
    //select the first document
    docIter = corpus.iterator();
    if(docIter.hasNext()){
      currentDoc = (Document)docIter.next();
      corpusList.setSelectedIndex(0);
    }
    logTextArea.append("Processing ended at: " + (new Date()) + "\n" +
                       "===============================================\n");
    //repaint what needs to be repainted
    updateAll();
    corpusIsDirty = true;
  }
  void runBtn_actionPerformed(ActionEvent e) {
    //We need to run all the actions in a different thread so the interface
    //doesn't freeze
    Thread thread = new Thread(this);
    thread.start();
  }

  public void tokenize(Document doc){
    String content = doc.getContent().toString();
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
    if(currentDoc == null){
      text.setText("");
    }else{
      //display the current document
      text.getHighlighter().removeAllHighlights();
      text.setText(currentDoc.getContent().toString());
      //get all the annotation types and display the buttons
      typesPanel.removeAll();
      Iterator typesIter = currentDoc.getAnnotations().getAllTypes().iterator();
      String currentType;
      ColorGenerator colGen = new ColorGenerator();
      JButton typeButton = new JButton();
      JLabel typeLabel = new JLabel("Clear all");
      typeLabel.setBackground(Color.black);
      typeLabel.setForeground(Color.white);
      typeLabel.setOpaque(true);
      typeButton.add(typeLabel);
      typeButton.setBackground(colGen.getNextColor());
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
        typeLabel.setBackground(Color.white);
        typeLabel.setForeground(Color.black);
        typeLabel.setOpaque(true);
        typeButton.add(typeLabel);
        typeButton.setToolTipText(currentType);
        typeButton.setBackground(colGen.getNextColor());
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
      typesPanel.repaint();
      //create the table
      tableView = new SortedTable();
      tableView.setTableModel(new AnnotationSetTableModel(currentDoc,currentDoc.getAnnotations()));
      tableViewScroll.getViewport().add(tableView, null);

    }
    validate();
  }

  void corpusList_mouseClicked(MouseEvent e) {
    //find out what document we're talking about
    int docIdx = corpusList.locationToIndex(e.getPoint());
    corpusList.setSelectedIndex(docIdx);
    docIdx++;//just a trick to use the same variable
    Iterator docIter = corpus.iterator();
    while(docIter.hasNext() && docIdx >0 ){
      currentDoc = (Document)docIter.next();
      docIdx--;
    }
    if(docIdx != 0){
      throw(new RuntimeException(
                "The user has selected an unexistant document! :)"));
    }
    if((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0){
      JPopupMenu docListPopup = new JPopupMenu();
      JMenuItem delPopup = new JMenuItem("Drop document");
      docListPopup.add(delPopup);
      delPopup.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          //delete the document from the list
          corpusListModel.removeElement(currentDoc.getSourceURL().getFile());
          corpus.remove(currentDoc);
          corpusFiles.remove(currentDoc.getSourceURL().toExternalForm());
          if(corpus.isEmpty()) currentDoc = null;
          else currentDoc = (Document)corpus.first();
          updateAll();
        }
      });
      docListPopup.show(corpusList, e.getPoint().x, e.getPoint().y);
    }else{
      updateAll();
    }
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


  //Gui members
  JMenuBar jMenuBar1 = new JMenuBar();

  Box westBox;
  JSplitPane textViewPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
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
  ExtensionFileFilter tokFileFilter;
  ExtensionFileFilter gazFileFilter;

  /**A set of String objects containing all the names of the files that should
    *go in the corpus.
    */
  Set corpusFiles = new HashSet();
  File grammarFile;
  File tokeniserRulesFile = null;
  File gazeteerListDefFile = null;
  DefaultTokeniser tokeniser =null;
  DefaultGazeteer gazeteer = null;


  private boolean invokedStandalone = false;
  Document currentDoc;
  JPanel typesPanel = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();
  JScrollPane textViewScroll = new JScrollPane();
  Random randomGen = new Random();
  boolean corpusIsDirty = false;
  JTabbedPane centerTabPane = new JTabbedPane();
  SortedTable tableView;
  JScrollPane tableViewScroll = new JScrollPane();
  javax.swing.table.TableModel tm;
  long startCorpusLoad = 0, startCorpusTokenization = 0,
       startJapeFileOpen = 0, startCorpusTransduce = 0,
       endProcess = 0, startLookup = 0, startLookupLoad = 0;
  FlowLayout flowLayout2 = new FlowLayout();
  JTextArea logTextArea = new JTextArea();
  JScrollPane logScrollPane = new JScrollPane();
  JButton tokRulesBtn = new JButton();

  void this_windowClosing(WindowEvent e) {
    System.exit(0);
  }

  void tokRulesBtn_actionPerformed(ActionEvent e) {
    filer.setFileFilter(tokFileFilter);
    filer.setDialogTitle("Open Tokeniser rules file");
    filer.setMultiSelectionEnabled(false);
    filer.setSelectedFile(null);
    filer.setSelectedFiles(null);
    int res = filer.showOpenDialog(this);
    if(res == JFileChooser.APPROVE_OPTION){
      tokeniserRulesFile = filer.getSelectedFile();
    }
  }

  //ProgressListener implementation
  public void progressChanged(int i){
    if(lastProgress != i){
//System.out.println(i);
      progressBar.setValue(i);
      if(System.currentTimeMillis() - lastProgressUpdate > 300){
        progressBar.paintImmediately(progressBar.getVisibleRect());
        lastProgressUpdate = System.currentTimeMillis();
      }
      lastProgress = i;
    }
  }

  public void processFinished(){
    progressBar.setValue(0);
    progressBar.paintImmediately(progressBar.getVisibleRect());
  }

  //StatusListener implementation
  public void statusChanged(String text){
    statusBar.setText(text);
//    if(System.currentTimeMillis() - lastStatusUpdate > 300){
      statusBar.paintImmediately(statusBar.getVisibleRect());
//      lastStatusUpdate = System.currentTimeMillis();
//    }
  }
  long lastStatusUpdate = 0;
  long lastProgressUpdate = 0;
  int lastProgress = 0;
  JButton gazeteerBtn = new JButton();

  static{
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }catch(Exception e){}
  }

  void gazeteerBtn_actionPerformed(ActionEvent e) {
    filer.setFileFilter(gazFileFilter);
    filer.setDialogTitle("Select the Gazeteer list definition");
    filer.setMultiSelectionEnabled(false);
    filer.setSelectedFile(null);
    filer.setSelectedFiles(null);
    int res = filer.showOpenDialog(this);
    if(res == JFileChooser.APPROVE_OPTION){
      gazeteerListDefFile = filer.getSelectedFile();
    }
  }

}



class WindowListener extends java.awt.event.WindowAdapter {
  JapeGUI adaptee;

  WindowListener(JapeGUI adaptee) {
    this.adaptee = adaptee;
  }

  public void windowClosing(WindowEvent e) {
    adaptee.this_windowClosing(e);
  }
}
