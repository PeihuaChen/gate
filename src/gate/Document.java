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
public interface Document extends LanguageResource
{
  /** Documents are identified by URLs */
  public URL getSourceURL();

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document.
    */
  public Long[] getSourceURLOffsets();

  /** The content of the document: a String for text; MPEG for video; etc. */
  public Object getContent();

  /** The portion of content falling between two offsets. */
  public Object getContent(Long start, Long end) throws InvalidOffsetException;

  /** The size of the set of valid offsets in this document's content.
    * For texts this will be the length of the string. For audiovisual
    * materials this will be a measure of time.
    */
  public Long size();

  /** Get the default set of annotations. The set is created if it
    * doesn't exist yet.
    */
  public AnnotationSet getAnnotations();

  /** Get a named set of annotations. Creates a new set if one with this
    * name doesn't exist yet.
    */
  public AnnotationSet getAnnotations(String name);

} // interface Document
