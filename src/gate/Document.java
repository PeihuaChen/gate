/*
	Document.java 

	Hamish Cunningham, 19/Jan/2000

	$Id$
*/

package gate;
import java.util.*;
import java.net.*;
import gate.util.*;

/** Represents the commonalities between all sorts of documents.
  */
public interface Document
{
  /** Documents are identified by URLs */
  public URL getUrl();

  /** The annotation graphs for this document in a map indexed by id*/
  public Map getAnnotationGraphs();

  public AnnotationGraph getAnnotationGraph(String id);
  /** The features of this document */
  public FeatureSet getFeatureSet();

  /** The length of the underlying document, e.g. the number of bytes for
  *textual documents*/
  public double getLength();
  /**Creates a new empty annotation graph associated with this document and returns it.*/
  public AnnotationGraph newAnnotationGraph(String id);

  public String getId();

  public Object getContent();

  public Object getContent(double startIndex, double endIndex)throws gate.util.InvalidOffsetException;

} // interface Document
