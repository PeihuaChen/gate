/*
 *  Copyright (c) 1995-2013, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  $Id$
 */
package gate.corpora.twitter;

import gate.*;
import gate.corpora.DocumentContentImpl;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleResource;
import gate.gui.*;
import gate.util.*;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.*;


@CreoleResource(name = "Twitter Corpus Populator", tool = true, autoinstances = @AutoInstance)
public class Population extends ResourceHelper  {

  private static final long serialVersionUID = 1443073039199794668L;
  
  public static final String[] DEFAULT_CONTENT_KEYS = {"text", "created_at", "user:name"};
  public static final String[] DEFAULT_FEATURE_KEYS = {"user:screen_name", "user:location", 
    "id", "source", "truncated", "retweeted_status:id"};

  
  public static void populateCorpus(final Corpus corpus, URL inputUrl, PopulationConfig config) 
      throws ResourceInstantiationException {
    populateCorpus(corpus, inputUrl, config.getEncoding(), config.getContentKeys(), 
        config.getFeatureKeys(), config.getTweetsPerDoc());
  }
  
  /**
   * 
   * @param corpus
   * @param inputUrl
   * @param encoding
   * @param contentKeys
   * @param featureKeys
   * @param tweetsPerDoc 0 = put them all in one document; otherwise the number per document
   * @throws ResourceInstantiationException
   */
  public static void populateCorpus(final Corpus corpus, URL inputUrl, String encoding, List<String> contentKeys,
      List<String> featureKeys, int tweetsPerDoc) throws ResourceInstantiationException {
    try {
      InputStream input = inputUrl.openStream();
      List<String> lines = IOUtils.readLines(input, encoding);
      IOUtils.closeQuietly(input);
      
      // TODO: sort this out so it processes one at a time instead of reading the
      // whole hog into memory
      
      // For now, we assume the streaming API format (concatenated maps, not in a list)
      List<Tweet> tweets = TweetUtils.readTweetStrings(lines, contentKeys, featureKeys);
      
      int digits = (int) Math.ceil(Math.log10((double) tweets.size()));
      int tweetCounter = 0;
      Document document = newDocument(inputUrl, tweetCounter, digits);
      StringBuilder content = new StringBuilder();
      Map<PreAnnotation, Integer> annotanda = new HashMap<PreAnnotation, Integer>();
      
      for (Tweet tweet : tweets) {
        if ( (tweetsPerDoc > 0) && (tweetCounter > 0) && ((tweetCounter % tweetsPerDoc) == 0) ) {
          closeDocument(document, content, annotanda, corpus);
          document = newDocument(inputUrl, tweetCounter, digits);
          content = new StringBuilder();
          annotanda = new HashMap<PreAnnotation, Integer>();
        }

        int startOffset = content.length();
        content.append(tweet.getString());
        for (PreAnnotation preAnn : tweet.getAnnotations()) {
          annotanda.put(preAnn, startOffset);
        }

        content.append('\n');
        tweetCounter++;
      } // end of Tweet loop
      
      if (content.length() > 0) {
        closeDocument(document, content, annotanda, corpus);
      }
      else {
        Factory.deleteResource(document);
      }
      
      if(corpus.getDataStore() != null) {
        corpus.getDataStore().sync(corpus);
      }
      
    }
    catch (Exception e) {
      throw new ResourceInstantiationException(e);
    }
  }


  private static Document newDocument(URL url, int counter, int digits) throws ResourceInstantiationException {
    Document document = Factory.newDocument("");
    String code = StringUtils.leftPad(Integer.toString(counter), digits, '0');
    String name = StringUtils.stripToEmpty(StringUtils.substring(url.getPath(), 1)) + "_" + code;
    document.setName(name);
    document.setSourceUrl(url);
    document.getFeatures().put(Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, TweetUtils.MIME_TYPE);
    document.getFeatures().put("gate.SourceURL", url.toString());
    return document;
  }
  
  
  private static void closeDocument(Document document, StringBuilder content, Map<PreAnnotation, Integer> annotanda, Corpus corpus) throws InvalidOffsetException {
    DocumentContent contentImpl = new DocumentContentImpl(content.toString());
    document.setContent(contentImpl);
    AnnotationSet originalMarkups = document.getAnnotations(Gate.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
    for (PreAnnotation preAnn : annotanda.keySet()) {
      preAnn.toAnnotation(originalMarkups, annotanda.get(preAnn));
    }
    corpus.add(document);
    
    if (corpus.getLRPersistenceId() != null) {
      corpus.unloadDocument(document);
      Factory.deleteResource(document);
    }
  }

  
  @Override
  protected List<Action> buildActions(final NameBearerHandle handle) {
    List<Action> actions = new ArrayList<Action>();

    if(!(handle.getTarget() instanceof Corpus)) return actions;

    actions.add(new AbstractAction("Populate from Twitter JSON file") {
      private static final long serialVersionUID = -8511779592856786327L;

      @Override
      public void actionPerformed(ActionEvent e)  {
        final PopulationDialogWrapper dialog = new PopulationDialogWrapper();

        // If no files were selected then just stop
        try {
          final List<URL> fileUrls = dialog.getFileUrls();
          if ( (fileUrls == null) || fileUrls.isEmpty() ) {
            return;
          }
          
          // Run the population in a separate thread so we don't lock up the GUI
          Thread thread =
              new Thread(Thread.currentThread().getThreadGroup(),
                  "Twitter JSON Corpus Populator") {
                public void run() {
                  try {
                    for (URL fileUrl : fileUrls) {
                      populateCorpus((Corpus) handle.getTarget(), fileUrl, dialog.getEncoding(), 
                          dialog.getContentKeys(), dialog.getFeatureKeys(), dialog.getTweetsPerDoc());
                    } 
                  }
                  catch(ResourceInstantiationException e) {
                    e.printStackTrace();
                  }
                }
              };
          thread.setPriority(Thread.MIN_PRIORITY);
          thread.start();
        }
        catch(MalformedURLException e0) {
          e0.printStackTrace();
        }
      }
    });

    return actions;
  }

  
  

}


class PopulationDialogWrapper  {
  private JDialog dialog;
  private PopulationConfig config;
  private JTextField encodingField;
  private JCheckBox checkbox;
  private JFileChooser chooser;
  private List<URL> fileUrls;
  private ListEditor featureKeysEditor, contentKeysEditor;

  
  public PopulationDialogWrapper() {
    config = new PopulationConfig();
    
    dialog = new JDialog(MainFrame.getInstance(), "Populate from Twitter JSON", true);
    MainFrame.getGuiRoots().add(dialog);
    dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
    dialog.add(Box.createVerticalStrut(3));
    
    Box encodingBox = Box.createHorizontalBox();
    JLabel encodingLabel = new JLabel("Encoding:");
    encodingField = new JTextField(config.getEncoding());
    encodingBox.add(encodingLabel);
    encodingBox.add(encodingField);
    dialog.add(encodingBox);
    dialog.add(Box.createVerticalStrut(4));

    Box checkboxBox = Box.createHorizontalBox();
    checkboxBox.setToolTipText("If unchecked, one document per file");
    JLabel checkboxLabel = new JLabel("One document per tweet");
    checkbox = new JCheckBox();
    checkbox.setSelected(config.getOneDocCheckbox());
    checkboxBox.add(checkboxLabel);
    checkboxBox.add(Box.createHorizontalGlue());
    checkboxBox.add(checkbox);
    dialog.add(checkboxBox);
    dialog.add(Box.createVerticalStrut(4));
    
    contentKeysEditor = new ListEditor("Content keys: ", config.getContentKeys());
    contentKeysEditor.setToolTipText("JSON key paths to be turned into DocumentContent");
    dialog.add(contentKeysEditor);
    dialog.add(Box.createVerticalStrut(4));
    
    featureKeysEditor = new ListEditor("Feature keys: ", config.getFeatureKeys());
    featureKeysEditor.setToolTipText("JSON key paths to be turned into Tweet annotation features");
    dialog.add(featureKeysEditor);
    dialog.add(Box.createVerticalStrut(6));
    
    Box configPersistenceBox = Box.createHorizontalBox();
    configPersistenceBox.add(Box.createHorizontalGlue());
    JButton loadConfigButton = new JButton("Load configuration");
    loadConfigButton.setToolTipText("Replace the configuration above with a previously saved one");
    configPersistenceBox.add(loadConfigButton);
    configPersistenceBox.add(Box.createHorizontalGlue());
    JButton saveConfigButton = new JButton("Save configuration");
    saveConfigButton.setToolTipText("Save the configuration above for re-use");
    configPersistenceBox.add(saveConfigButton);
    configPersistenceBox.add(Box.createHorizontalGlue());
    //dialog.add(configPersistenceBox);
    //dialog.add(Box.createVerticalStrut(5));
    
    dialog.add(new JSeparator(SwingConstants.HORIZONTAL));
    dialog.add(Box.createVerticalStrut(2));
    
    chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
    chooser.setDialogTitle("Select a Twitter JSON file");
    chooser.resetChoosableFileFilters();
    chooser.setAcceptAllFileFilterUsed(false);
    ExtensionFileFilter filter = new ExtensionFileFilter("Twitter JSON files (*.json)", "json");
    chooser.addChoosableFileFilter(filter);
    chooser.setFileFilter(filter);
    chooser.setApproveButtonText("Populate");
    chooser.addActionListener(new PopulationDialogListener(this));

    dialog.add(chooser);
    dialog.pack();
    dialog.setLocationRelativeTo(dialog.getOwner());
    dialog.setVisible(true);
  }
  
  
  public String getEncoding() {
    return this.config.getEncoding();
  }
  
  public List<URL> getFileUrls() throws MalformedURLException {
    return this.fileUrls;
  }

  public int getTweetsPerDoc() {
    return this.config.getTweetsPerDoc();
  }
  
  public List<String> getContentKeys() {
    return this.config.getContentKeys();
  }
  
  public List<String> getFeatureKeys() {
    return this.config.getFeatureKeys();
  }
  
  
  protected void updateConfig() {
    this.config.setTweetsPerDoc(this.checkbox.isSelected() ? 1 : 0);
    this.config.setContentKeys(this.contentKeysEditor.getValues());
    this.config.setFeatureKeys(this.featureKeysEditor.getValues());
    this.config.setEncoding(this.encodingField.getText());
  }
  
  
  protected void loadFile()  {
    updateConfig();

    try {
      this.fileUrls = new ArrayList<URL>();
      for (File file : this.chooser.getSelectedFiles()) {
        this.fileUrls.add(file.toURI().toURL());
      }
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }
    finally {
      this.dialog.dispose();
    }
  }

  
  protected void cancel() {
    this.dialog.dispose();
  }
  
}


class PopulationDialogListener implements ActionListener {

  private PopulationDialogWrapper dialog;
  
  public PopulationDialogListener(PopulationDialogWrapper dialog) {
    this.dialog = dialog;
  }

  
  @Override
  public void actionPerformed(ActionEvent event) {
    if (event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)){
      this.dialog.loadFile();
    }
    else {
      this.dialog.cancel();
    }
  }
  
}


class ListEditor extends JPanel {
  private static final long serialVersionUID = -1578463259277343578L;

  private JButton listButton;
  private ListEditorDialog listEditor;
  private List<String> values;
  private JLabel label;
  private JTextField field;
  
  @Override
  public void setToolTipText(String text) {
    super.setToolTipText(text);
    label.setToolTipText(text);
    field.setToolTipText(text);
  }
  
  
  public ListEditor(String labelString, List<String> initialValues) {
    label = new JLabel(labelString);
    field = new JTextField();
    values = initialValues;
    field.setText(Strings.toString(initialValues));
    field.setEditable(false);
        
    listEditor = new ListEditorDialog(SwingUtilities.getAncestorOfClass(
        Window.class, this), values, List.class, String.class.getName());

    listButton = new JButton(MainFrame.getIcon("edit-list"));
    listButton.setToolTipText("Edit the list");
    
    listButton.addActionListener(new ActionListener() {
      @SuppressWarnings("unchecked")
      public void actionPerformed(ActionEvent e) {
        List<?> returnedList = listEditor.showDialog();
        if(returnedList != null) {
          values = (List<String>) returnedList;
          field.setText(Strings.toString(returnedList));
        }
      }
    });
    
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(label);
    this.add(field);
    this.add(listButton);
  }
  
  
  public List<String> getValues() {
    return this.values;
  }
  
}
