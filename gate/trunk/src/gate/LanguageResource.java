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
   
} // interface LanguageResource
