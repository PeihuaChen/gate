package gate.persist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
		SearchableDataStore, CorpusListener {

	/**
	 * serial version UID
	 */
	private static final long serialVersionUID = 3618696392336421680L;

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

	/** Open a connection to the data store. */
	public void open() throws PersistenceException {
		super.open();

		/*
		 * check if the storage directory is a valid serial datastore if we want
		 * to support old style: String versionInVersionFile = "1.0"; (but this
		 * means it will open *any* directory)
		 */
		try {
			FileReader fis = new FileReader(getVersionFile());
			BufferedReader isr = new BufferedReader(fis);
			currentProtocolVersion = isr.readLine();
			String url = isr.readLine();
			if (url != null && url.trim().length() > 1) {
				indexURL = new URL(url);
				this.indexer = new LuceneIndexer(indexURL);
				this.searcher = new LuceneSearcher();
			}
			isr.close();
		} catch (IOException e) {
			throw new PersistenceException("Invalid storage directory: " + e);
		}
		if (!isValidProtocolVersion(currentProtocolVersion))
			throw new PersistenceException("Invalid protocol version number: "
					+ currentProtocolVersion);
	}

	/**
	 * Delete a resource from the data store.
	 */
	public void delete(String lrClassName, Object lrPersistenceId)
			throws PersistenceException {

		super.delete(lrClassName, lrPersistenceId);

		/*
		 * lets first find out if the deleted resource is a corpus. Deleting a
		 * corpus does not require deleting all its member documents but we need
		 * to remove the reference of corpus from all its underlying documents
		 * in index
		 */
		try {
			if (Corpus.class.isAssignableFrom(Class.forName(lrClassName, true,
					Gate.getClassLoader()))) {
				/*
				 * we would issue a search query to obtain all documents which
				 * belong to his corpus and set them as referring to null
				 * instead of refering to the given corpus
				 */
				Map parameters = new HashMap();
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
					
					for(int i=0;i<hits.length;i++) {
						String docID = hits[i].getDocumentID();
				        FeatureMap features = Factory.newFeatureMap();
				        features.put(DataStore.DATASTORE_FEATURE_NAME, this);
 			            features.put(DataStore.LR_ID_FEATURE_NAME, docID);
						Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", features);

						/*
						 * we need to reindex this document in order to synchronize it lets
						 * first remove it from the index
						 */
						ArrayList removed = new ArrayList();
						removed.add(docID);
						this.indexer.remove(removed);

						// and add it back
						ArrayList added = new ArrayList();
						added.add(doc);
						this.indexer.add(null, added);
						Factory.deleteResource(doc);
					}
					
				} catch(SearchException se) {
					throw new PersistenceException(se);
				} catch(ResourceInstantiationException rie) {
					throw new PersistenceException(rie);
				} catch(IndexException ie) {
					throw new PersistenceException(ie);
				}
				return;
			}
		} catch (ClassNotFoundException cnfe) {
			// don't do anything
		}

		// we want to delete this document from the Index as well
		ArrayList removed = new ArrayList();
		removed.add(lrPersistenceId);
		try {
			this.indexer.remove(removed);
		} catch (IndexException ie) {
			throw new PersistenceException(ie);
		}
	}

	/**
	 * Get a resource from the persistent store. <B>Don't use this method - use
	 * Factory.createResource with DataStore and DataStoreInstanceId parameters
	 * set instead.</B> (Sometimes I wish Java had "friend" declarations...)
	 */
	public LanguageResource getLr(String lrClassName, Object lrPersistenceId)
			throws PersistenceException, SecurityException {
		LanguageResource lr = super.getLr(lrClassName, lrPersistenceId);
		if (lr instanceof Corpus) {
			((Corpus) lr).addCorpusListener(this);
		}
		return lr;
	}

	/**
	 * Save: synchonise the in-memory image of the LR with the persistent image.
	 */
	public void sync(LanguageResource lr) throws PersistenceException {
		super.sync(lr);
		if (lr instanceof Document) {

			/*
			 * we need to reindex this document in order to synchronize it lets
			 * first remove it from the index
			 */
			ArrayList removed = new ArrayList();
			if (lr.getLRPersistenceId() != null) {
				removed.add(lr.getLRPersistenceId());
				try {
					this.indexer.remove(removed);
				} catch (IndexException ie) {
					throw new PersistenceException(ie);
				}
			}

			// and add it back
			ArrayList added = new ArrayList();
			added.add(lr);

			try {
				String corpusPID = null;
				if (lr.getLRPersistenceId() != null) {

					/*
					 * we need to find out the corpus which this document
					 * belongs to one easy way is to check all instances of
					 * serial corpus loaded in memory
					 */
					List scs = Gate.getCreoleRegister().getLrInstances(
							SerialCorpusImpl.class.getName());
					if (scs != null) {
						/*
						 * we need to check which corpus the deleted class
						 * belonged to
						 */
						Iterator iter = scs.iterator();
						while (iter.hasNext()) {
							SerialCorpusImpl sci = (SerialCorpusImpl) iter
									.next();
							if (sci != null) {
								if (sci.contains(lr)) {
									corpusPID = sci.getLRPersistenceId()
											.toString();
									break;
								}
							}
						}
					}
					/*
					 * it is also possible that the document is loaded from
					 * datastore without being loaded from the corpus (e.g.
					 * using getLR(...) method of datastore) in this case the
					 * relevant corpus won't exist in memory
					 */
					if (corpusPID == null) {
						List corpusPIDs = this.getLrIds(SerialCorpusImpl.class
								.getName());
						if (corpusPIDs != null) {
							for (int i = 0; i < corpusPIDs.size(); i++) {
								Object corpusID = corpusPIDs.get(i);
								// we will have to load this corpus
								SerialCorpusImpl corpusLR = (SerialCorpusImpl) this
										.getLr(
												SerialCorpusImpl.class
														.getName(), corpusID);
								if (corpusLR != null) {
									if (corpusLR.contains(lr)) {
										corpusPID = corpusLR
												.getLRPersistenceId()
												.toString();
										break;
									}
								}
							}
						}
					}
				}
				this.indexer.add(corpusPID, added);
			} catch (Exception ie) {
				ie.printStackTrace();
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
		this.indexURL = (URL) this.indexParameters
				.get(Constants.INDEX_LOCATION_URL);
		this.indexer.createIndex(this.indexParameters);

		// dump the version file
		try {
			File versionFile = getVersionFile();
			OutputStreamWriter osw = new OutputStreamWriter(
					new FileOutputStream(versionFile));
			osw.write(versionNumber + Strings.getNl());
			osw.write(indexURL.toString());
			osw.close();
		} catch (IOException e) {
			throw new IndexException("couldn't write version file: " + e);
		}
	}

	public Indexer getIndexer() {
		return this.indexer;
	}

	public void setSearcher(Searcher searcher) throws SearchException {
		this.searcher = searcher;
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
		if (doc.getLRPersistenceId() != null) {
			removed.add(doc.getLRPersistenceId());
			try {
				this.indexer.remove(removed);
			} catch (IndexException ie) {
				throw new GateRuntimeException(ie);
			}
		}

		// and add it back
		ArrayList added = new ArrayList();
		added.add(doc);

		try {
			this.indexer.add(null, added);
		} catch (Exception ie) {
			throw new GateRuntimeException(ie);
		}
	}

	/**
	 * This method is invoked whenever a document is added to a particular
	 * corpus
	 */
	public void documentAdded(CorpusEvent ce) {
		/*
		 * we don't want to do anything here, because the sync is automatically
		 * called when a document is added to a corpus which is part of the the
		 * datastore
		 */
	}
}