/*
 *  LuceneIndexer.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: LuceneIndexer.html,v 1.0 2007/03/19 16:22:01 niraj Exp $
 */
package gate.creole.annic.lucene;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gate.creole.annic.Constants;
import gate.creole.annic.IndexException;
import gate.creole.annic.Indexer;
import gate.creole.annic.apache.lucene.index.IndexReader;
import gate.creole.annic.apache.lucene.index.IndexWriter;
import gate.Corpus;

/**
 * This class provides a Lucene based implementation for the Indexer
 * interface. It asks users to provide various required parameters and
 * creates the Lucene Index.
 * 
 * @author niraj
 * 
 */
public class LuceneIndexer implements Indexer {

  /** An corpus for indexing */
  protected Corpus corpus;

  /**
   * Various parameters such as location of the Index etc.
   */
  protected Map parameters;

  /**
   * For each document, we obtain the indexing units.
   */
  protected Map noOfIndexUnitsPerDocument;

  /**
   * Constructor
   * 
   * @param indexLocationUrl
   * @throws IOException
   */
  public LuceneIndexer(URL indexLocationUrl) throws IOException {
    if(indexLocationUrl != null) readParametersFromDisk(indexLocationUrl);
  }

  /**
   * Checks the Index Parameters to see if they are all compatible
   */
  protected void checkIndexParameters(Map parameters) throws IndexException {
    this.parameters = parameters;
    noOfIndexUnitsPerDocument = new HashMap();

    if(parameters == null) {
      throw new IndexException("No parameters provided!");
    }

    URL indexLocation = (URL)parameters.get(Constants.INDEX_LOCATION_URL);
    if(indexLocation == null)
      throw new IndexException("You must provide a URL for INDEX_LOCATION");

    if(!indexLocation.getProtocol().equalsIgnoreCase("file")) {
      throw new IndexException(
              "Index Output Directory must be set to the empty directory on the file system");
    }

    File file = new File(indexLocation.getFile());
    if(file.exists()) {
      if(!file.isDirectory()) {
        throw new IndexException("Path doesn't exist");
      }
    }

    String baseTokenAnnotationType = (String)parameters
            .get(Constants.BASE_TOKEN_ANNOTATION_TYPE);
    String indexUnitAnnotationType = (String)parameters
            .get(Constants.INDEX_UNIT_ANNOTATION_TYPE);

    if(baseTokenAnnotationType == null
            || baseTokenAnnotationType.trim().length() == 0) {
      throw new IndexException("Base Token Annotation Type not set properly");
    }
  }

  /**
   * Returns the indexing parameters
   * 
   * @return
   */
  protected Map getIndexParameters() {
    return this.parameters;
  }

  /**
   * Creates index directory and indexing all documents in the corpus.
   * 
   * @param indexParameters This is a map containing various values
   *          required to create an index In case of LuceneIndexManager
   *          following are the values required
   *          <P>
   *          INDEX_LOCATION_URL - this is a URL where the Index be
   *          created
   *          <P>
   *          BASE_TOKEN_ANNOTATION_TYPE
   *          <P>
   *          INDEX_UNIT_ANNOTATION_TYPE
   *          <P>
   *          FEATURES_TO_EXCLUDE
   */
  public void createIndex(Map indexParameters) throws IndexException {
    checkIndexParameters(indexParameters);
    URL indexLocation = (URL)parameters.get(Constants.INDEX_LOCATION_URL);

    try {
      File file = new File(indexLocation.getFile());

      // create an instance of Index Writer
      IndexWriter writer = new IndexWriter(file.getAbsolutePath(),
              new LuceneAnalyzer(), true);

      if(corpus != null) {
        // load documents and add them one by one
        for(int i = 0; i < corpus.size(); i++) {
          gate.Document gateDoc = (gate.Document)corpus.get(i);
          String idToUse = gateDoc.getLRPersistenceId() == null ? gateDoc
                  .getName() : gateDoc.getLRPersistenceId().toString();
          String corpusName = corpus.getLRPersistenceId() == null ? corpus
                  .getName() : corpus.getLRPersistenceId().toString();
          gate.creole.annic.apache.lucene.document.Document[] luceneDocs = getLuceneDoc(
                  corpusName, gateDoc, indexLocation.toString());

          if(luceneDocs == null) {
            String indexUnitAnnotationType = (String)parameters
                    .get(Constants.INDEX_UNIT_ANNOTATION_TYPE);
            System.err
                    .println("Ignoring Document : There are no annotations of type :"
                            + indexUnitAnnotationType);
          }
          else {
            for(int j = 0; j < luceneDocs.length; j++) {
              if(luceneDocs[j] != null) {
                writer.addDocument(luceneDocs[j]);
              }
            }
            noOfIndexUnitsPerDocument.put(idToUse, new Integer(
                    luceneDocs.length));
          }
          if(gateDoc.getLRPersistenceId() != null) {
            gate.Factory.deleteResource(gateDoc);
          }
        }
      }// for (all documents)
      writer.close();
      writeParametersToDisk();
    }
    catch(java.io.IOException ioe) {
      throw new IndexException(ioe);
    }
  }

  /** Optimize existing index. */
  public void optimizeIndex() throws IndexException {
    try {
      String location = ((URL)parameters.get(Constants.INDEX_LOCATION_URL))
              .toString();
      IndexWriter writer = new IndexWriter(location,
              new gate.creole.annic.lucene.LuceneAnalyzer(), false);
      writer.optimize();
      writer.close();
    }
    catch(java.io.IOException ioe) {
      throw new IndexException(ioe.getMessage());
    }
  }

  /** Deletes the index. */
  public void deleteIndex() throws IndexException {
    boolean isDeleted = true;
    if(parameters == null) return;
    File dir = new File(((URL)parameters.get(Constants.INDEX_LOCATION_URL))
            .getFile());
    if(dir.exists() && dir.isDirectory()) {
      File[] files = dir.listFiles();
      for(int i = 0; i < files.length; i++) {
        File f = files[i];
        if(f.isDirectory()) {
          File[] subFiles = f.listFiles();
          for(int j = 0; j < subFiles.length; j++) {
            File sf = subFiles[j];
            sf.delete();
          }
        }
        f.delete();
      }
    }
    noOfIndexUnitsPerDocument = new HashMap();
    isDeleted = dir.delete();
    if(!isDeleted) {
      throw new IndexException("Can't delete directory" + dir.getAbsolutePath());
    }
  }

  /**
   * Add new documents to Index
   * 
   * @param corpusPersistenceID
   * @param addedDocuments
   * @throws IndexException
   */
  public void add(String corpusPersistenceID, List<gate.Document> added)
          throws IndexException {
    String location = new File(((URL)parameters
            .get(Constants.INDEX_LOCATION_URL)).getFile()).getAbsolutePath();
    IndexWriter writer = null;
    try {
      writer = new IndexWriter(location, new LuceneAnalyzer(), false);

      if(added != null) {
        for(int i = 0; i < added.size(); i++) {

          gate.Document gateDoc = added.get(i);
          String idToUse = gateDoc.getLRPersistenceId() == null ? gateDoc
                  .getName() : gateDoc.getLRPersistenceId().toString();

          gate.creole.annic.apache.lucene.document.Document[] docs = getLuceneDoc(
                  corpusPersistenceID, gateDoc, location);
          if(docs == null) continue;
          for(int j = 0; j < docs.length; j++) {
            writer.addDocument(docs[j]);
          }
          noOfIndexUnitsPerDocument.put(idToUse, new Integer(docs.length));
        }// for (add all added documents)
      }

    }
    catch(java.io.IOException ioe) {
      throw new IndexException(ioe.getMessage());
    }
    finally {
      // whatever happens we need to try to close the writer
      try {
        writer.close();
        writeParametersToDisk();
      }
      catch(java.io.IOException ioe) {
        throw new IndexException(ioe.getMessage());
      }
    }
  }

  /**
   * remove documents from the Index
   * 
   * @param removedDocumentPersistenceIds - when documents are not
   *          peristed, Persistence IDs will not be available In that
   *          case provide the document Names instead of their IDs
   * @throws Exception
   */
  public void remove(List removedIDs) throws IndexException {
    String location = new File(((URL)parameters
            .get(Constants.INDEX_LOCATION_URL)).getFile()).getAbsolutePath();

    try {

      IndexReader reader = IndexReader.open(location);

      // let us first remove the documents which need to be removed
      if(removedIDs != null) {
        for(int i = 0; i < removedIDs.size(); i++) {
          String id = removedIDs.get(i).toString();

          // for this ID we need to find out noOfUnits
          Integer size = (Integer)noOfIndexUnitsPerDocument.get(id);
          if(size == null) {
            continue;
          }
          // System.out.print("Removing => " + id + "...");

          for(int j = 0; j < size.intValue(); j++) {
            String tempID = id + "-" + j;
            gate.creole.annic.apache.lucene.index.Term term = new gate.creole.annic.apache.lucene.index.Term(
                    Constants.DOCUMENT_ID, tempID);
            reader.delete(term);
            // deleting them from the disk as well
            File file = new File(new File(location,
                    Constants.SERIALIZED_FOLDER_NAME), tempID + ".annic");
            if(file.exists()) file.delete();
          }
          noOfIndexUnitsPerDocument.remove(id);
        }// for (remove all removed documents)
      }

      reader.close();
      writeParametersToDisk();
    }
    catch(java.io.IOException ioe) {
      throw new IndexException(ioe.getMessage());
    }

  }

  /**
   * We create a separate Lucene document for each index unit available
   * in the gate document. An array of Lucene document is returned as a
   * call to this method. It uses various indexing parameters set
   * earlier.
   * 
   * @param corpusPersistenceID
   * @param gateDoc
   * @param location
   * @return
   * @throws IndexException
   */
  private gate.creole.annic.apache.lucene.document.Document[] getLuceneDoc(
          String corpusPersistenceID, gate.Document gateDoc, String location)
          throws IndexException {
    String set = (String)parameters.get(Constants.ANNOTATION_SET_NAME);
    String baseTokenAnnotationType = (String)parameters
            .get(Constants.BASE_TOKEN_ANNOTATION_TYPE);
    ArrayList featuresToExclude = new ArrayList((List)parameters
            .get(Constants.FEATURES_TO_EXCLUDE));
    String indexUnitAnnotationType = (String)parameters
            .get(Constants.INDEX_UNIT_ANNOTATION_TYPE);

    String idToUse = gateDoc.getLRPersistenceId() == null
            ? gateDoc.getName()
            : gateDoc.getLRPersistenceId().toString();
    return new gate.creole.annic.lucene.LuceneDocument().createDocument(
            corpusPersistenceID, gateDoc, idToUse, set, featuresToExclude,
            location, baseTokenAnnotationType, indexUnitAnnotationType);
  }

  /**
   * Returns the corpus.
   */
  public Corpus getCorpus() {
    return corpus;
  }

  /**
   * Sets the corpus.
   */
  public void setCorpus(Corpus corpus) throws IndexException {
    this.corpus = corpus;
    if(corpus == null) {
      throw new IndexException("Corpus is not initialized");
    }

    // we would add a feature to the corpus
    // which will tell us if this corpus was index by the ANNIC
    corpus.getFeatures().put(Constants.CORPUS_INDEX_FEATURE,
            Constants.CORPUS_INDEX_FEATURE_VALUE);
  }

  /**
   * This method, searchers for the LuceneIndexDefinition.xml file at
   * the provided location. The file is supposed to contain all the
   * required parameters which are used to create an index.
   * 
   * @param indexLocationUrl
   * @throws IOException
   */
  private void readParametersFromDisk(URL indexLocationUrl) throws IOException {
    // we create a hashmap to store index definition in the index
    // directory
    String location = indexLocationUrl.toString();
    String newIndexLocation = new URL(location).getFile();

    java.io.File file = new java.io.File(newIndexLocation,
            "LuceneIndexDefinition.xml");
    if(!file.exists()) return;

    java.io.FileReader fileReader = new java.io.FileReader(file);

    // other wise read this and
    com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream(
            new com.thoughtworks.xstream.io.xml.StaxDriver());

    // Saving is accomplished just using XML serialization of the map.
    this.parameters = (HashMap)xstream.fromXML(fileReader);
    this.noOfIndexUnitsPerDocument = (HashMap)this.parameters
            .get(Constants.NO_OF_INDEX_UNITS_PER_DOCUMENT);
    fileReader.close();
  }

  /**
   * All Index parameters are stored on a disc at the
   * index_location_url/LuceneIndexDefinition.xml file.
   * 
   * @throws IOException
   */
  private void writeParametersToDisk() throws IOException {
    // we create a hashmap to store index definition in the index
    // directory
    String location = ((URL)parameters.get(Constants.INDEX_LOCATION_URL))
            .toString();
    String newIndexLocation = new URL(location).getFile();

    if(!newIndexLocation.endsWith("/") && !newIndexLocation.endsWith("\\")) {
      newIndexLocation += "/";
    }

    java.io.File file = new java.io.File(newIndexLocation
            + "LuceneIndexDefinition.xml");
    java.io.FileWriter fileWriter = new java.io.FileWriter(file);
    HashMap indexInformation = new HashMap();
    Iterator iter = parameters.keySet().iterator();
    while(iter.hasNext()) {
      Object key = iter.next();
      indexInformation.put(key, parameters.get(key));
    }

    indexInformation.put(Constants.CORPUS_INDEX_FEATURE,
            Constants.CORPUS_INDEX_FEATURE_VALUE);
    if(corpus != null)
      indexInformation.put(Constants.CORPUS_SIZE, new Integer(corpus
              .getDocumentNames().size()));
    indexInformation.put(Constants.NO_OF_INDEX_UNITS_PER_DOCUMENT,
            noOfIndexUnitsPerDocument);

    // we would use XStream library to store annic patterns
    com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream();

    // Saving is accomplished just using XML serialization of
    // the map.
    xstream.toXML(indexInformation, fileWriter);
  }

  /**
   * Returns the set parameters
   */
  public Map getParameters() {
    return this.parameters;
  }
}
