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
public interface Document extends FeatureBearer
{
  /** Documents are identified by URLs */
  public URL getUrl();

  /** The annotation graphs for this document in a map indexed by id*/
  public Map getAnnotationGraphs();

  public AnnotationGraph getAnnotationGraph(Long id);

  /** The length of the underlying document, e.g. the number of bytes for
    * textual documents
    */
  public double getLength();

  /** Creates a new empty annotation graph associated with this document
    * and returns it.
    */
  public AnnotationGraph newAnnotationGraph(Long id);

  public Long getId();

  public Object getContent();

  //public Object getContent(long startOffset, long endOffset)
  //throws InvalidOffsetException;

} // interface Document
