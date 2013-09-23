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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.*;


@CreoleResource(name = "Twitter Corpus Populator", tool = true, autoinstances = @AutoInstance)
public class Population extends ResourceHelper  {

  private static final long serialVersionUID = 1443073039199794668L;

  public static final String DEFAULT_CONTENT_KEYS = "text;created_at";
  public static final String DEFAULT_FEATURE_KEYS = "user:screen_name;user:location;id";
  public static final String KEY_SEPARATOR = ";";

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
    String code = StringUtils.rightPad(Integer.toString(counter), digits, '0');
    String name = StringUtils.stripToEmpty(StringUtils.substring(url.getPath(), 1)) + "_" + code;
    document.setName(name);
    document.setSourceUrl(url);
    document.getFeatures().put(Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, TweetUtils.MIME_TYPE);
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

        // if no file was selected then just stop
        try {
          final URL fileUrl = dialog.getFileUrl();
          if(fileUrl == null)
            return;
          
          // Run the population in a separate thread so we don't lock up the GUI
          Thread thread =
              new Thread(Thread.currentThread().getThreadGroup(),
                  "Twitter JSON Corpus Populator") {
                public void run() {
                  try {
                    populateCorpus((Corpus) handle.getTarget(), fileUrl, dialog.getEncoding(), 
                        dialog.getContentKeys(), dialog.getFeatureKeys(), dialog.getTweetsPerDoc());
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
  private String encoding;
  private int tweetsPerDoc;
  private List<String> contentKeys, featureKeys;
  private JTextField encodingField, contentKeysField, featureKeysField;
  private JCheckBox checkbox;
  private JFileChooser chooser;
  private URL fileUrl;

  
  public PopulationDialogWrapper() {
    dialog = new JDialog(MainFrame.getInstance(), "Populate from Twitter JSON", true);
    MainFrame.getGuiRoots().add(dialog);
    dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
    
    JPanel encodingPanel = new JPanel();
    JLabel encodingLabel = new JLabel("Encoding:");
    encodingField = new JTextField(15);
    encodingPanel.add(encodingLabel);
    encodingPanel.add(encodingField);
    dialog.add(encodingPanel);

    JPanel checkboxPanel = new JPanel();
    JLabel checkboxLabel = new JLabel("One document per tweet");
    checkbox = new JCheckBox();
    checkboxPanel.add(checkboxLabel);
    checkboxPanel.add(checkbox);
    dialog.add(checkboxPanel);
    
    JPanel contentKeysPanel = new JPanel();
    JLabel contentKeysLabel = new JLabel("Content keys:");
    contentKeysField = new JTextField();
    contentKeysField.setText(Population.DEFAULT_CONTENT_KEYS);
    contentKeysPanel.add(contentKeysLabel);
    contentKeysPanel.add(contentKeysField);
    dialog.add(contentKeysPanel);
    
    JPanel featureKeysPanel = new JPanel();
    JLabel featureKeysLabel = new JLabel("Feature keys:");
    featureKeysField = new JTextField();
    featureKeysField.setText(Population.DEFAULT_FEATURE_KEYS);
    featureKeysPanel.add(featureKeysLabel);
    featureKeysPanel.add(featureKeysField);
    dialog.add(featureKeysPanel);

    chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
    return this.encoding;
  }
  
  public URL getFileUrl() throws MalformedURLException {
    return this.fileUrl;
  }

  public int getTweetsPerDoc() {
    return this.tweetsPerDoc;
  }
  
  public List<String> getContentKeys() {
    return this.contentKeys;
  }
  
  public List<String> getFeatureKeys() {
    return this.featureKeys;
  }
  
  protected void load()  {
    this.tweetsPerDoc = this.checkbox.isSelected() ? 1 : 0;

    this.contentKeys = splitField(contentKeysField);
    this.featureKeys = splitField(featureKeysField);
    
    this.encoding = this.encodingField.getText();
    if ( (this.encoding == null) || this.encoding.isEmpty() ) {
      this.encoding = Charset.defaultCharset().name();
    }

    try {
      this.fileUrl = this.chooser.getSelectedFile().toURI().toURL();
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
  
  
  private static List<String> splitField(JTextField field) {
    String [] array = StringUtils.split(field.getText(), Population.KEY_SEPARATOR);
    return Arrays.asList(array);
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
      this.dialog.load();
    }
    else {
      this.dialog.cancel();
    }
  }
  
  
  
  
}
