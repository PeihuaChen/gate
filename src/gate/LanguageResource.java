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
//  public boolean canLiveIn(DataStore ds);
    //moved this in setupDS

  /**This method allows the persistence capable language resources
    *to check the datastore intended for them to be stored in and to create
    *the appropriate structures.
    *@param ds The datastore object which holds the information regarding the
    *persistence mechanism intended for storing this language resource.
    *@return true if the structure of the database is valid for storage or
    *if the structure of the database has been successfuly altered so it is now
    *ready to store this kind of language resource;
    *false if the database could not be altered such that it complies with the
    *request (e.g. one of the tables exists and has a different structure).
    */
//it seems I cannot implement a method in an interface with a static method.
//On the other hand I cannot declare a method as being static in an interface.
//I need setupDS to be static so I implemented this method in all
//LanguageResource implementers but I had to delete it from here :-(
//  public boolean setupDS(DataStore ds);

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
