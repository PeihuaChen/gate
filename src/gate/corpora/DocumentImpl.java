/*
	DocumentImpl.java 

	Hamish Cunningham, 11/Feb/2000

	$Id$
*/

package gate.corpora;

import java.util.*;
import java.net.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;

/** Represents the commonalities between all sorts of documents.
  */
public class DocumentImpl implements Document
{
  /** Documents are identified by URLs */
  public URL getSourceURL() { return null; }

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document.
    */
  public Long[] getSourceURLOffsets() { return null; }

  /** Get the data store the document lives in. */
  public DataStore getDataStore() { return null; }

  /** The content of the document: a String for text; MPEG for video; etc. */
  public Object getContent() { return null; }

  /** The portion of content falling between two offsets. */
  public Object getContent(Long start, Long end) throws InvalidOffsetException
  { return null; }

  /** The size of the set of valid offsets in this document's content.
    * For texts this will be the length of the string. For audiovisual
    * materials this will be a measure of time.
    */
  public Long size() { return null; }

  /** Get the default set of annotations. The set is created if it
    * doesn't exist yet.
    */
  public AnnotationSet getAnnotations() { return null; }

  /** Get a named set of annotations. Creates a new set if one with this
    * name doesn't exist yet.
    */
  public AnnotationSet getAnnotations(String name) { return null; }


  /** The features associated with this document. */
  FeatureMap features;

  /** Get the features associated with this document. */
  public FeatureMap getFeatures() { return features; }

} // class DocumentImpl
