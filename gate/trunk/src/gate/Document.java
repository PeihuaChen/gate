/*
	Document.java 

	Hamish Cunningham, 19/Jan/2000

	$Id$
*/

package gate;
import java.util.*;
import java.net.*;
import gate.util.*;

/** 
  * Represents the commonalities between all sorts of documents.
  */
public interface Document
{
  /** Documents are identified by URLs */
  public URL getUrl();

  /** The annotation graphs for this document */
  public Set getAnnotationGraphs();

  /** The features of this document */
  public FeatureSet getFeatureSet();

} // interface Document
