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
package gate.termraider.util;

import gate.*;
import gate.creole.ANNIEConstants;
import gate.persist.PersistenceException;
import gate.security.SecurityException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import java.io.*;
import java.lang.Character.UnicodeBlock;
import java.net.*;
import java.text.DateFormat;
import java.util.*;
import gate.termraider.bank.*;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class Utilities implements ANNIEConstants {

  public static final String CREATE_IDF_TABLE_CLASS = "sumgate.resources.frequency.OnTheFlyInvertedTable";
  public static final String LOAD_IDF_TABLE_CLASS   = "sumgate.resources.frequency.InvertedTable";
  public static final String SERIAL_DATASTORE_CLASS = "gate.persist.SerialDataStore";
  public static final String LUCENE_DATASTORE_CLASS = "gate.persist.LuceneDataStoreImpl";
  public static final String CORPUS_CLASS           = "gate.corpora.SerialCorpusImpl";
  public static final String DOCUMENT_CLASS         = "gate.corpora.DocumentImpl";

  public static final String TOKEN_STATISTICS_PR_CLASS    = "sumgate.resources.frequency.NEFrequency";
  public static final String MWORD_STATISTICS_PR_CLASS    = "sumgate.resources.frequency.NEFrequency";

  public static final String EXTENSION_CSV = "csv";
  public static final String EXTENSION_RDF = "rdf";
  
  public static final String LANGUAGE_ERROR_CODE = "___";

  
  private static long startTime;
  private static DateFormat terseDateTimeFormat;
  
  private static Set<UnicodeBlock> greekBlocks, latinBlocks;
  private static double log10of2;

  
  static {
    terseDateTimeFormat = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss");
    greekBlocks = new HashSet<Character.UnicodeBlock>();
    greekBlocks.add(UnicodeBlock.GREEK);
    greekBlocks.add(UnicodeBlock.GREEK_EXTENDED);
    latinBlocks = new HashSet<Character.UnicodeBlock>();
    latinBlocks.add(UnicodeBlock.BASIC_LATIN);
    latinBlocks.add(UnicodeBlock.LATIN_1_SUPPLEMENT);
    latinBlocks.add(UnicodeBlock.LATIN_EXTENDED_A);
    latinBlocks.add(UnicodeBlock.LATIN_EXTENDED_B);
    latinBlocks.add(UnicodeBlock.LATIN_EXTENDED_ADDITIONAL);
    log10of2 = Math.log10(2.0);
  }

  
  public static void setGateHome() throws GateException {
    String gateHomePath = "../../gate";

    File propertiesFile = new File("build.properties");
    String sysGateProp = System.getProperties().getProperty("gate.home");

    if (propertiesFile.exists() && propertiesFile.isFile()) {
      Properties properties = new Properties();
      try {
        properties.load(new FileInputStream(propertiesFile));
      } 
      catch(IOException e) {
        throw new GateException(e);
      }
      gateHomePath = properties.getProperty("gate.home");
    }
    else if (sysGateProp != null) {
      gateHomePath = sysGateProp;
    }

    File gateHomePathFile = new File(gateHomePath);
    System.out.println("GATE home = " + gateHomePathFile.getAbsolutePath());
    Gate.setGateHome(gateHomePathFile);
  }

  public static double meanDoubleList(List<Double> list) {
    if (list.isEmpty()) {
      return 0.0;
    }
    // implied else
    double total = 0.0;
    for (Double item : list) {
      total += item;
    }
    return total / ((double) list.size());
  }
  
  
  public static double normalizeScore(double score) {
    double norm = 1.0 - 1.0 / (1.0 + Math.log10(1.0 + score));
    return (double) (100.0F * norm);
  }

  /**
   * Returns a negative value if the string is mostly Latin,
   * positive if mostly Greek, 0.0 if equally or neither. 
   * @param string
   * @return -1.0 to 1.0
   */
  public static double greekness(String string) {
    int greek = 0;
    int latin = 0;
    
    if ( (string == null) || string.isEmpty()) {
      return 0.0;
    }

    // implied else
    
    int length = string.length();
    for (int i = 0 ; i < length ; i++)  {
      UnicodeBlock block = Character.UnicodeBlock.of(string.charAt(i));
      if (greekBlocks.contains(block)) {
        greek++;
      }
      else if (latinBlocks.contains(block)) {
        latin++;
      }
    }

    return ( (double) (greek - latin) ) / ((double) length);
  }
  
  
  public static double greekness(Annotation anno, String feature) {
    if (anno.getFeatures().containsKey(feature)) {
      return greekness(anno.getFeatures().get(feature).toString());
    }
    
    // implied else: feature missing, so nothing to measure
    return 0.0;
  }
  
  
  public static CorpusController getPipeline(String pipelineFilename)
    throws GateException {

    File pipelineFile = new File(pipelineFilename);
    System.out.format("Loading pipeline: %s\n", pipelineFile.getPath());

    CorpusController pipeline = null;

    try {
      pipeline = (CorpusController) PersistenceManager.loadObjectFromFile(new File(pipelineFilename));
    }
    catch(IOException ioe) {
      throw new GateException(ioe);
    } // end of try-catch

    return pipeline;
  }
  
  
  public static DataStore openDatastore(String datastoreDirname, boolean lucene)
    throws GateException {
    File datastoreDir = new File(datastoreDirname);
    return openDatastore(datastoreDir, lucene);
  }

  
  public static DataStore openDatastore(File datastoreDir, boolean lucene)
  throws GateException {
  String datastoreUrlString;
  try {
    datastoreUrlString = datastoreDir.toURI().toURL().toString();
  }
  catch(MalformedURLException e) {
    e.printStackTrace();
    throw new GateException(e);
  }
  String dsClass = lucene ? LUCENE_DATASTORE_CLASS : SERIAL_DATASTORE_CLASS;
  DataStore ds = Factory.openDataStore(dsClass, datastoreUrlString);
  System.out.format("Opened %s at %s\n", dsClass, ds.getStorageUrl());
  return ds;
}

  
  
  public static List<Corpus> getCorpora(DataStore datastore) {
    List<Corpus> result = new ArrayList<Corpus>();
    Corpus corpus;
    Object corpusId;
    
    try {
      Iterator<?> corpusIdIter = datastore.getLrIds(CORPUS_CLASS).iterator();
      while (corpusIdIter.hasNext()) {
        corpusId = corpusIdIter.next();
        try {
          corpus = (Corpus) datastore.getLr(CORPUS_CLASS, corpusId);
          result.add(corpus);
        }
        catch(SecurityException e) {
          e.printStackTrace();
        }
      }
    }
    catch(PersistenceException e) {
      e.printStackTrace();
    }
    
    return result;
  }
  
  
  public static int getNbrOfDocuments(DataStore ds) 
      throws PersistenceException {
    return ds.getLrIds(DOCUMENT_CLASS).size();
  }
  
  
    
  
  
  
  public static ProcessingResource getUniquePRByClass(String classname)
  throws GateException {
    List<Resource> prs = Gate.getCreoleRegister().getAllInstances(classname);

    if (prs.size() == 1) {
      return (ProcessingResource) prs.get(0);
    }
    else {
      throw new GateException("Oops!  There are  "
        + prs.size() + "  instances of  " + classname);
    }
  }  
  
  
  public static ProcessingResource getPRbyName(CorpusController pipeline,
      String name) {
    ProcessingResource result = null;
    ProcessingResource temp;
    Iterator<?> prIter = pipeline.getPRs().iterator();
    while (prIter.hasNext()) {
      temp = (ProcessingResource) prIter.next();
      if (temp.getName().equals(name)) {
        result = temp;
        break;
      }
    }
    return result;
  }



  public static Double convertToDouble(Object x) {
    if (x instanceof Number) {
      return ((Number) x).doubleValue();
    }
    
    return Double.parseDouble(x.toString()) ;
  }


  
  /**
   * Read an XML file into a JDOM document.
   * 
   * @param url
   *          an XML file containing the mappings
   * @return a JDOM representation of the XML file
   */
  public static org.jdom.Document loadXml(URL url, boolean debugMode) {
    org.jdom.Document xmlDoc = null;
    if(debugMode) {
      System.out.println("Loading XML from " + url.toString());
    }
    try {
      SAXBuilder sb = new SAXBuilder();
      xmlDoc = sb.build(url);
    }
    catch(JDOMException e) {
      e.printStackTrace();
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    return xmlDoc;
  }

  
  public static boolean loadPlugin(String pluginPath) {
    boolean success = false;
    URL pluginUrl = null;
    try {
      pluginUrl = (new File(pluginPath)).toURI().toURL();
      Gate.getCreoleRegister().registerDirectories(pluginUrl);
      success = true;
    }
    catch(MalformedURLException e) {
      e.printStackTrace();
    }
    catch(GateException e) {
      e.printStackTrace();
    }
    
    return success;
  }
  
  
  public static int nbrOfLiveLanguageResources() {
    return Gate.getCreoleRegister().getLrInstances().size();
  }

  
  public static void startClock() {
    startTime = System.currentTimeMillis();
    printNeatly("Starting!", (new Date(startTime)).toString());
  }
  
  public static long elapsedSeconds() {
    return (System.currentTimeMillis() - startTime) / 1000L;
  }

  public static void printProgress(int nbrDone, int nbrTotal) {
    String eta = "(Error calculating ETA!)";
    
    if ((nbrDone) > 0) {
      long projectedFinish = (System.currentTimeMillis() - startTime) * (nbrTotal / nbrDone)
      + startTime;
      eta = (new Date(projectedFinish)).toString();
    }
    printNeatly("Progress + ETA:", (100 * nbrDone / nbrTotal) + "%    " + eta);
    System.out.println();
  }
  

  public static void printNeatly(String left, Object right) {
    System.out.format("%-40s %s\n", left, right.toString());
  }
  
  public static void printElapsedTime() {
    long e = elapsedSeconds();
    Utilities.printNeatly("Elapsed time:", e + " sec  (" + e/60L + " min)");
  }

  
  public static DataStore createTempDatastore()  {
    String path = "/tmp/tr_ds_" + System.currentTimeMillis(); 
    
    File dir = new File(path);
    dir.mkdirs();
    DataStore ds = null;
    try {
      String urlString = dir.toURI().toURL().toString();
      ds = Factory.createDataStore(Utilities.SERIAL_DATASTORE_CLASS, urlString);
    }
    catch(PersistenceException e) {
      e.printStackTrace();
    }
    catch(UnsupportedOperationException e) {
      e.printStackTrace();
    }
    catch(MalformedURLException e) {
      e.printStackTrace();
    }
    return ds;
  }
  
  
  
  public static File getTempFile(String stuff, boolean cleanup)
      throws IOException {
    File tempFile = File.createTempFile(stuff + "_" + System.currentTimeMillis(), ".txt");
    if (cleanup) {
      tempFile.deleteOnExit();
    }
    return tempFile;
  }
  
  
  public static String flattenString(String input) {
    return input.replaceAll("\\s+", "_").replaceAll("\\|+", "_");
  }

  
  /**
   * Suitable for embedding in URIs.
   */
  public static String veryCleanString(String input) {
    String clean = input.trim();
    return clean.replaceAll("[^\\p{Alnum}\\p{Lu}\\p{Ll}]+", "_");
  }

  
  public static int deleteDatastore(DataStore ds)  {
    int deleted = 0;
    try {
      deleted = deleteAllFiles(new File(new URL(ds.getStorageUrl()).toURI()));
    }
    catch(MalformedURLException e) {
      e.printStackTrace();
    }
    catch(URISyntaxException e) {
      e.printStackTrace();
    }
    return deleted;
  }
  
  
  private static int deleteAllFiles(File file) {
    int deleted = 0;
    
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      for (File x : files) {
        deleted = deleted + deleteAllFiles(x);
      }
    }

    file.delete();
    deleted++;
    
    return deleted;
  }

  
  public static String generateID(String prefix, String suffix) {
    return prefix + java.util.UUID.randomUUID().toString() + suffix;
  }

  
  public static URL getUrlInJar(AbstractTermbank termbank, String filename) {
    ClassLoader cl = termbank.getClass().getClassLoader();
    return cl.getResource(filename);
  }
  
  public static List<String> keysAsStrings(FeatureMap fm) {
    List<String> result = new ArrayList<String>();
    if (fm != null) {
      Set<?> keys = fm.keySet();
      for (Object key : keys) {
        result.add(key.toString());
      }
    }
    return result;
  }


  public static List<String> valuesAsStrings(FeatureMap fm) {
    List<String> result = new ArrayList<String>();
    if (fm != null) {
      for (Object key : fm.keySet()) {
        result.add(fm.get(key).toString());
      }
    }
    return result;
  }
  
  
  public static void setCanonicalFromLemma(Annotation token, Document doc, String lemmaFeatureName) {
    String canonical = getCanonicalFromLemma(token, doc, lemmaFeatureName);
    token.getFeatures().put("canonical", canonical);
  }

  
  public static String getCanonicalFromLemma(Annotation token, Document doc, String lemmaFeatureName) {
    FeatureMap fm = token.getFeatures();
    String canonical = "";
    if (fm.containsKey(lemmaFeatureName)) {
      canonical = fm.get(lemmaFeatureName).toString().toLowerCase();
    }

    if (canonical.equals("") || canonical.equals("<unknown>")) {
      if (fm.containsKey(TOKEN_STRING_FEATURE_NAME)) {
        canonical = fm.get(TOKEN_STRING_FEATURE_NAME).toString().toLowerCase();
      }
      else {
        canonical = gate.Utils.stringFor(doc, token).toLowerCase();
      }
    }
    
    return canonical;
  }


  public static void setCanonicalFromString(Annotation token, Document doc) {
    String canonical = getCanonicalFromString(token, doc);
    token.getFeatures().put("canonical", canonical);
  }

  
  public static String getCanonicalFromString(Annotation token, Document doc) {
    FeatureMap fm = token.getFeatures();
    String canonical = "";
    if (fm.containsKey(TOKEN_STRING_FEATURE_NAME)) {
      canonical = fm.get(TOKEN_STRING_FEATURE_NAME).toString().toLowerCase();
    }
    else {
      canonical = gate.Utils.stringFor(doc, token).toLowerCase();
    }
    
    return canonical;
  }

  
  public static String sourceOrName(Document document) {
    URL url = document.getSourceUrl();
    if (url == null) {
      return document.getName();
    }
    
    //implied else
    return url.toString();
  }
  
  
  public static String getFeatureOrString(Document document, Annotation annotation, String key) {
    FeatureMap fm = annotation.getFeatures();
    if (fm.containsKey(key)) {
      return fm.get(key).toString();
    }
    // implied else
    return gate.Utils.cleanStringFor(document, annotation);
  }
  
  
  public static String getLanguage(Annotation annotation, String languageFeature) {
    String language = LANGUAGE_ERROR_CODE;
    if (annotation.getFeatures().containsKey(languageFeature)) {
      language = annotation.getFeatures().get(languageFeature).toString();
    }
    return language;
  }
  
  
  public static String getTimestamp() {
    synchronized(terseDateTimeFormat) {
      return terseDateTimeFormat.format(new Date());
    }
  }

  
  public static File addExtensionIfNotExtended(File file, String extension) {
    String name = file.getName();
    if (name.contains(".")) {
      return file;
    }

    // implied else: add extension
    File parentDir = file.getParentFile();
    if (extension.startsWith(".")) {
      name = name + extension;
    }
    else {
      name = name + "." + extension;
    }

    return new File(parentDir, name);
  }

  
  public static String integerToString(Integer i) {
    if (i == null) {
      return "<null>";
    }
    // implied else
    return Integer.toString(i);
  }
  
  
  public static double log2(double input) {
    /*  log_a x = log_b x * log_a b
     * 
     *  log_b x = log_a x / log_a b
     */
    return Math.log10(input) / log10of2;
  }


}
