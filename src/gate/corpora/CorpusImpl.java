/*
	CorpusImpl.java 

	Hamish Cunningham, 11/Feb/2000

	$Id$
*/

package gate.corpora;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.annotation.*;

/** Corpora are sets of Document. They are ordered by lexicographic collation
  * on Url.
  */
public class CorpusImpl extends TreeSet implements Corpus
{
  /** Construction from name */
  public CorpusImpl(String name) {
    this(name, null);
  } // Construction from name

  /** Construction from name and features */
  public CorpusImpl(String name, FeatureMap features) {
    this.name = name;
    this.features = features;
  } // Construction from name and features

  /** Construction from name features and documents map.
    *This is actually a copy-constructor.
    */
  public CorpusImpl(String name, FeatureMap features, Map docsById) {
    this.name = name;
    this.features = features;
    this.docsById = docsById;
  } // Construction from name features and documents

  /** Get the name of the corpus. */
  public String getName() { return name; }

  /** Get the data store the document lives in. */
  public DataStore getDataStore() {
    //this is the transient version of corpus, hence return null.
    return null;
  }

  /**Get the documents. Only available to derived classes*/
  public Map getDocsById(){
    return docsById;
  }
  /** Get the features associated with this corpus. */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; }

  public Document getDocument(long id){
    return (DocumentImpl)docsById.get(new Long(id));
  }


//Persistence stuff
  public boolean isPersistent(){
  //This class does not define persistent objects.
    return false;
  }

  public boolean isPersistenceCapable(){
    //There is a persistent implementation for corpora so return true
    return true;
  }
/*
  public boolean canLiveIn(DataStore ds){
    //We only have one type of persistent corpora so we don't need to check
    //the datastore type here.
    return gate.db.CorpusWrapper.checkDS(ds);
  }
*/
  public String getErrorMessage(){
    return gate.db.Checker.errMsg;
  };

  public static boolean setupDS(DataStore ds){
    //We only have one type of persistent corpora so we don't need to check
    //the datastore type here.
    return gate.db.CorpusWrapper.setupDatabase(ds);
  }

  public LRDBWrapper getDBWrapper(DataStore ds){
    //We only have one type of persistent corpora so we don't need to check
    //the datastore type here.
    return new gate.db.CorpusWrapper(ds, this);
  }
//END persistence stuff

  /** The name of the corpus */
  protected String name;

  /** The features associated with this corpus. */
  protected FeatureMap features;

  /**The documents contained by this corpus*/
  protected Map docsById;

} // class CorpusImpl
