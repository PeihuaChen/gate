package gate.persist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageResource;
import gate.corpora.SerialCorpusImpl;
import gate.creole.ResourceInstantiationException;
import gate.event.CorpusEvent;
import gate.event.CorpusListener;
import gate.security.SecurityException;
import gate.util.GateRuntimeException;
import gate.util.Strings;
import gate.creole.annic.Constants;
import gate.creole.annic.Hit;
import gate.creole.annic.IndexException;
import gate.creole.annic.Indexer;
import gate.creole.annic.SearchException;
import gate.creole.annic.SearchableDataStore;
import gate.creole.annic.Searcher;
import gate.creole.annic.lucene.LuceneIndexer;
import gate.creole.annic.lucene.LuceneSearcher;

public class LuceneDataStoreImpl extends SerialDataStore implements
                                                        SearchableDataStore,
                                                        CorpusListener {

  /**
   * serial version UID
   */
  private static final long serialVersionUID = 3618696392336421680L;

  /**
   * To store documents to be indexed
   */
  protected Set<Object> documentsToIndex = Collections
          .synchronizedSet(new HashSet<Object>());

  /**
   * To store canonical IDs of the documents being synchronized and
   * indexed
   */
  protected Map<Object, Object> canonicalLrIDs = Collections
          .synchronizedMap(new WeakHashMap<Object, Object>());

  /**
   * Indicates if the datastore is being closed.
   */
  protected boolean dataStoreClosing = false;

  /**
   * Thread that looks after indexing
   */
  protected Thread indexerThread = null;

  /**
   * Indexer to be used for indexing documents
   */
  protected Indexer indexer;

  /**
   * Index Parameters
   */
  protected Map indexParameters;

  /**
   * URL of the index
   */
  protected URL indexURL;

  /**
   * Searcher to be used for searching the indexed documents
   */
  protected Searcher searcher;

  /**
   * This is where we store the search parameters
   */
  protected Map searchParameters;

  private DataStore thisInstance;
  
  private IndexingStatus status = new IndexingStatus();

    
  /** Close the data store. */
  public void close() throws PersistenceException {
    synchronized(documentsToIndex) {
      dataStoreClosing = true;
      documentsToIndex.notify();
    }
    
    synchronized(status) {
      while(!status.finished) {
        try {
          System.out.println("Indexing is not over yet...");
          status.wait();
        } catch(InterruptedException ie) {
          throw new GateRuntimeException(ie);
        }
      }
      System.out.println("Indexing is over... Closing the DataStore");
    }
    
    super.close();     
  } // close()
  
  
  /** Open a connection to the data store. */
  public void open() throws PersistenceException {
    thisInstance = this;
    super.open();

    /*
     * check if the storage directory is a valid serial datastore if we
     * want to support old style: String versionInVersionFile = "1.0";
     * (but this means it will open *any* directory)
     */
    try {
      FileReader fis = new FileReader(getVersionFile());
      BufferedReader isr = new BufferedReader(fis);
      currentProtocolVersion = isr.readLine();
      String url = isr.readLine();
      if(url != null && url.trim().length() > 1) {
        indexURL = new URL(url);
        this.indexer = new LuceneIndexer(indexURL);
        this.searcher = new LuceneSearcher();
        ((LuceneSearcher)this.searcher).setLuceneDatastore(this);
      }
      isr.close();
    }
    catch(IOException e) {
      throw new PersistenceException("Invalid storage directory: " + e);
    }
    if(!isValidProtocolVersion(currentProtocolVersion))
      throw new PersistenceException("Invalid protocol version number: "
              + currentProtocolVersion);

    // Lets create a separate indexer thread which keeps running in the
    // background
    indexerThread = new Thread("ANNIC indexer") {

      // actual stuff for indexing
      public void run() {

        // keep running this thread until we know that the datastore is
        // closing
        while(!dataStoreClosing || documentsToIndex.size() > 0) {

          // lets obtain the documents that need to be indexed
          Object[] lrs = null;
          synchronized(documentsToIndex) {
            if(documentsToIndex.size() == 0) {
              try {
                documentsToIndex.wait();
              } catch(InterruptedException ie) {
                throw new GateRuntimeException(ie);
              }
            }
            
            lrs = documentsToIndex.toArray(new Object[documentsToIndex.size()]);
            documentsToIndex.clear();
          }

          
          
          if(lrs.length > 0) {
            synchronized(status) {
              status.finished = false;
            }
          }
          
          // lets iterate through collected documents to be indexed
          for(Object lrID : lrs) {

            // remove the ID from the waiting set if it's been queued
            // up again in the meantime
            documentsToIndex.remove(lrID);

            Document doc = null;
            Object canonID = canonicalID(lrID);
            synchronized(canonID) {
              // read the document from datastore
              FeatureMap features = Factory.newFeatureMap();
              features.put(DataStore.LR_ID_FEATURE_NAME, lrID);
              features.put(DataStore.DATASTORE_FEATURE_NAME, thisInstance);
              FeatureMap hidefeatures = Factory.newFeatureMap();
              Gate.setHiddenAttribute(hidefeatures, true);
              try {
                doc = (Document)Factory.createResource(
                        "gate.corpora.DocumentImpl", features, hidefeatures);
              }
              catch(ResourceInstantiationException rie) {
                // this means the canonicalID was null
                doc = null;
              }
              // as soon as we have a document to be indexed, we relase
              // the lock by ending the synchronized block
            }
            canonID = null;

            // if the document is not null,
            // proceed to indexing it
            if(doc != null) {

              /*
               * we need to reindex this document in order to
               * synchronize it lets first remove it from the index
               */
              ArrayList<Object> removed = new ArrayList<Object>();
              removed.add(lrID);
              try {
                synchronized(indexer) {
                  indexer.remove(removed);
                }
              }
              catch(IndexException ie) {
                throw new GateRuntimeException(ie);
              }

              // and add it back
              ArrayList<Document> added = new ArrayList<Document>();
              added.add(doc);

              try {
                String corpusPID = null;

                /*
                 * we need to find out the corpus which this document
                 * belongs to one easy way is to check all instances of
                 * serial corpus loaded in memory
                 */
                List scs = Gate.getCreoleRegister().getLrInstances(
                        SerialCorpusImpl.class.getName());
                if(scs != null) {
                  /*
                   * we need to check which corpus the deleted class
                   * belonged to
                   */
                  Iterator iter = scs.iterator();
                  while(iter.hasNext()) {
                    SerialCorpusImpl sci = (SerialCorpusImpl)iter.next();
                    if(sci != null) {
                      if(sci.contains(doc)) {
                        corpusPID = sci.getLRPersistenceId().toString();
                        break;
                      }
                    }
                  }
                }

                /*
                 * it is also possible that the document is loaded from
                 * datastore without being loaded from the corpus (e.g.
                 * using getLR(...) method of datastore) in this case
                 * the relevant corpus won't exist in memory
                 */
                if(corpusPID == null) {
                  List corpusPIDs = getLrIds(SerialCorpusImpl.class.getName());
                  if(corpusPIDs != null) {
                    for(int i = 0; i < corpusPIDs.size(); i++) {
                      Object corpusID = corpusPIDs.get(i);

                      SerialCorpusImpl corpusLR = null;
                      canonID = canonicalID(corpusID);
                      synchronized(canonID) {
                        // we will have to load this corpus
                        FeatureMap params = Factory.newFeatureMap();
                        params.put(DataStore.DATASTORE_FEATURE_NAME, thisInstance);
                        params.put(DataStore.LR_ID_FEATURE_NAME, corpusID);
                        FeatureMap hidefeatures = Factory.newFeatureMap();
                        Gate.setHiddenAttribute(hidefeatures, true);
                        corpusLR = (SerialCorpusImpl)Factory.createResource(
                                SerialCorpusImpl.class.getCanonicalName(),
                                params, hidefeatures);
                        canonID.notify();
                      }
                      canonID = null;

                      if(corpusLR != null) {
                        if(corpusLR.contains(doc)) {
                          corpusPID = corpusLR.getLRPersistenceId().toString();
                        }
                        Factory.deleteResource(corpusLR);
                        if(corpusPID != null) break;
                      }
                    }
                  }
                }

                synchronized(indexer) {
                  indexer.add(corpusPID, added);
                }
                
                Factory.deleteResource(doc);
              }
              catch(Exception ie) {
                ie.printStackTrace();
              }
            }
          }
        }
        synchronized(status) {
          status.finished = true;
          status.notify();
        }
      }
    };

    indexerThread.setPriority(Thread.MIN_PRIORITY);
    indexerThread.start();
  }

  /**
   * Obtain the synchrnozed canonicalID for the given persisitance ID
   * 
   * @param id
   * @return
   */
  private synchronized Object canonicalID(Object id) {
    if(!canonicalLrIDs.containsKey(id)) {
      canonicalLrIDs.put(id, id);
    }
    return canonicalLrIDs.get(id);
  }

  /**
   * Delete a resource from the data store.
   */
  public void delete(String lrClassName, Object lrPersistenceId)
          throws PersistenceException {

    synchronized(documentsToIndex) {
      // we check if the lrPersistenceId appears in this
      // documentsToIndex
      // if so we need to remove it from there as well
      documentsToIndex.remove(lrPersistenceId);
    }

    // and we delete it from the datastore
    // we obtained the lock on this - in order to avoid clashing between
    // the object being loaded by the indexer thread and the thread that
    // deletes it
    Object canonID = canonicalID(lrPersistenceId);
    synchronized(canonID) {
      super.delete(lrClassName, lrPersistenceId);
    }
    canonID = null;

    /*
     * lets first find out if the deleted resource is a corpus. Deleting
     * a corpus does not require deleting all its member documents but
     * we need to remove the reference of corpus from all its underlying
     * documents in index
     */
    try {
      if(Corpus.class.isAssignableFrom(Class.forName(lrClassName, true, Gate
              .getClassLoader()))) {
        /*
         * we would issue a search query to obtain all documents which
         * belong to his corpus and set them as referring to null
         * instead of refering to the given corpus
         */
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(Constants.INDEX_LOCATION_URL, indexURL);
        parameters.put(Constants.CORPUS_ID, lrPersistenceId.toString());
        try {
          boolean success = getSearcher().search("nothing", parameters);
          if(!success) return;

          Hit[] hits = getSearcher().next(-1);
          if(hits == null || hits.length == 0) {
            // do nothing
            return;
          }

          synchronized(documentsToIndex) {
            for(int i = 0; i < hits.length; i++) {
              String docID = hits[i].getDocumentID();
              documentsToIndex.add(docID);
            }

            // we've added enough docs in this so let the indexer thread know about this
            documentsToIndex.notify();
          }
        }
        catch(SearchException se) {
          throw new PersistenceException(se);
        }
        return;
      }
    }
    catch(ClassNotFoundException cnfe) {
      // don't do anything
    }

    // we want to delete this document from the Index as well
    ArrayList<Object> removed = new ArrayList<Object>();
    removed.add(lrPersistenceId);
    try {
      synchronized(indexer) {
        this.indexer.remove(removed);
      }
    }
    catch(IndexException ie) {
      throw new PersistenceException(ie);
    }
  }

  /**
   * Get a resource from the persistent store. <B>Don't use this method -
   * use Factory.createResource with DataStore and DataStoreInstanceId
   * parameters set instead.</B> (Sometimes I wish Java had "friend"
   * declarations...)
   */
  public LanguageResource getLr(String lrClassName, Object lrPersistenceId)
          throws PersistenceException, SecurityException {
    LanguageResource lr = super.getLr(lrClassName, lrPersistenceId);
    if(lr instanceof Corpus) {
      ((Corpus)lr).addCorpusListener(this);
    }
    return lr;
  }

  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  public void sync(LanguageResource lr) throws PersistenceException {
    if(lr.getLRPersistenceId() != null) {
      Object canonID = canonicalID(lr.getLRPersistenceId());
      synchronized(canonID) {
        super.sync(lr);
      }
      canonID = null;
    } else {
      super.sync(lr);
    }

    if(lr instanceof Document) {
      synchronized(documentsToIndex) {
        documentsToIndex.add(lr.getLRPersistenceId());
        // we've added a doc in this so let the indexer thread know about this
        documentsToIndex.notify();
      }
    }
  }

  /**
   * Sets the Indexer to be used for indexing Datastore
   */
  public void setIndexer(Indexer indexer, Map indexParameters)
          throws IndexException {

    this.indexer = indexer;
    this.indexParameters = indexParameters;
    this.indexURL = (URL)this.indexParameters.get(Constants.INDEX_LOCATION_URL);
    this.indexer.createIndex(this.indexParameters);

    // dump the version file
    try {
      File versionFile = getVersionFile();
      OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(
              versionFile));
      osw.write(versionNumber + Strings.getNl());
      osw.write(indexURL.toString());
      osw.close();
    }
    catch(IOException e) {
      throw new IndexException("couldn't write version file: " + e);
    }
  }

  public Indexer getIndexer() {
    return this.indexer;
  }

  public void setSearcher(Searcher searcher) throws SearchException {
    this.searcher = searcher;
    if(this.searcher instanceof LuceneSearcher) {
      ((LuceneSearcher)this.searcher).setLuceneDatastore(this);
    }
  }

  public Searcher getSearcher() {
    return this.searcher;
  }

  /**
   * Search the datastore
   */
  public boolean search(String query, Map searchParameters)
          throws SearchException {
    return this.searcher.search(query, searchParameters);
  }

  /**
   * Returns the next numberOfPatterns
   * 
   * @param numberOfPatterns
   * @return null if no patterns found
   */
  public Hit[] next(int numberOfPatterns) throws SearchException {
    return this.searcher.next(numberOfPatterns);
  }

  // Corpus Events
  /**
   * This method is invoked whenever a document is removed from a corpus
   */
  public void documentRemoved(CorpusEvent ce) {
    Document doc = ce.getDocument();

    /*
     * we need to reindex this document in order to synchronize it lets
     * first remove it from the index
     */
    ArrayList removed = new ArrayList();
    if(doc.getLRPersistenceId() != null) {
      synchronized(documentsToIndex) {
        documentsToIndex.add(doc.getLRPersistenceId());
      }
    }
  }

  /**
   * This method is invoked whenever a document is added to a particular
   * corpus
   */
  public void documentAdded(CorpusEvent ce) {
    /*
     * we don't want to do anything here, because the sync is
     * automatically called when a document is added to a corpus which
     * is part of the the datastore
     */
  }
  
  private class IndexingStatus {
    boolean finished = false;
  }
}
