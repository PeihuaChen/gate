/*
	LanguageResource.java 

	Hamish Cunningham, 11/Feb/2000

	$Id$
*/

package gate;

import java.util.*;
import java.io.*;

import gate.util.*;

/** Models all sorts of language resources.
  */
public interface LanguageResource extends Resource, Serializable
{

  /** Get the data store that this LR lives in. Null for transient LRs. */
  public DataStore getDataStore();

  /**This method is intended for persistence capable language resources.
    *When this method is called on a language resource, it should check whether
    *it can be stored in the provided datastore (check the structure of the
    *database and so on) and return true if yes and false otherwise.
    *This call will normally be forwarded to a static method on the class that
    *knows how to store this kind of objects, depending on the type of the
    *datastore.
    *@param ds The datastore object which holds the information regarding the
    *persistence mechanism intended for storing this language resource.
  */
  public boolean canLiveIn(DataStore ds);

  /**This method is intended for the persistence capable language resources
    *that are not happy with the structure of the database intended for them
    *to be stored in. It allows for the language resource to build the necessary
    *structures that it requires in the provided datastore.
    *This call will normally be forwarded to a static method on the class that
    *knows how to store this kind of objects, depending on the type of the
    *datastore.
    *If the execution of this method raises no exceptions, a subsequent call to
    *"canLiveIn" for the same datastore object should return "true".
    *@param ds The datastore object which holds the information regarding the
    *persistence mechanism intended for storing this language resource.
    */
  public void setupDS(DataStore ds);

  /**If the last execution of "canLiveIn" returned false, this method can be
    *used to find out out details about the failure.
    */
  public String getErrorMessage();

  /**Specifies whether this language resource knows any way of becoming
    *persistent.
    *Non persistence capable language resources should always return false.
    */
  public boolean isPersistenceCapable();

  /**This method returns a persistent copy of this language resource.
    */
  public LRDBWrapper getDBWrapper(DataStore ds);

  /**This method can be used to check whether a given language resource is
    *persistent or transient.
    *For persistence capable language resources that are not persistent, a call
    *to <b>getDBWrapper</b> should return a persistent copy of themselfs.
    */
  public boolean isPersistent();
} // interface LanguageResource
