/*
	DocumentImpl.java 

	Hamish Cunningham, 11/Feb/2000

	$Id$
*/

package gate.corpora;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;

/** Represents the commonalities between all sorts of documents.
  * 
  * <H2>Editing</H2>
  * 
  * <P>
  * The DocumentImpl class implements the Document interface.
  * The DocumentContentImpl class models the textual or audio-visual
  * materials which are the source and content of Documents.
  * The AnnotationSetImpl class supplies annotations on Documents.
  * 
  * <P>
  * Abbreviations:
  * 
  * <UL>
  * <LI>
  * DC = DocumentContent
  * <LI>
  * D = Document
  * <LI>
  * AS = AnnotationSet
  * </UL>
  * 
  * <P>
  * We add an edit method to each of these classes; for DC and AS
  * the methods are package private; D has the public method.
  * 
  * <PRE>
  *   void edit(Long start, Long end, DocumentContent replacement)
  *   throws InvalidOffsetException;
  * </PRE>
  * 
  * <P>
  * D receives edit requests and forwards them to DC and AS.
  * On DC, this method makes a change to the content - e.g. replacing
  * a String range from start to end with replacement. (Deletions
  * are catered for by having replacement = null.) D then calls
  * AS.edit on each of its annotation sets.
  * 
  * <P>
  * On AS, edit calls replacement.size() (i.e. DC.size()) to
  * figure out how long the replacement is (0 for null). It then
  * considers annotations that terminate (start or end) in
  * the altered or deleted range as invalid; annotations that
  * terminate after the range have their offsets adjusted.
  * I.e.:
  * <UL>
  * <LI>
  * the nodes that pointed inside the old modified area are invalid now and
  * will be deleted along with the connected annotations;
  * <LI>
  * the nodes that are before the start of the modified area remain
  * untouched;
  * <LI>
  * the nodes that are after the end of the affected area will have the
  * offset changed according to the formula below.
  * </UL>
  * 
  * <P>
  * A note re. AS and annotations: annotations no longer have
  * offsets as in the old model, they now have nodes, and nodes
  * have offsets.
  * 
  * <P>
  * To implement AS.edit, we have several indices:
  * <PRE>
  *   HashMap annotsByStartNode, annotsByEndNode;
  * </PRE>
  * which map node ids to annotations;
  * <PRE>
  *   RBTreeMap nodesByOffset;
  * </PRE>
  * which maps offset to Nodes.
  * 
  * <P>
  * When we get an edit request, we traverse that part of the
  * nodesByOffset tree representing the altered or deleted
  * range of the DC. For each node found, we delete any annotations
  * that terminate on the node, and then delete the node itself.
  * We then traverse the rest of the tree, changing the offset
  * on all remaining nodes by:
  * <PRE>
  *   newOffset =
  *     oldOffset -
  *     (
  *       (end - start) -                                     // size of mod
  *       ( (replacement == null) ? 0 : replacement.size() )  // size of repl
  *     );
  * </PRE>
  * Note that we use the same convention as e.g. java.lang.String: start
  * offsets are inclusive; end offsets are exclusive. I.e. for string "abcd"
  * range 1-3 = "bc". Examples, for a node with offset 4:
  * <PRE>
  * edit(1, 3, "BC");
  * newOffset = 4 - ( (3 - 1) - 2 ) = 4
  *
  * edit(1, 3, null);
  * newOffset = 4 - ( (3 - 1) - 0 ) = 2
  *
  * edit(1, 3, "BBCC");
  * newOffset = 4 - ( (3 - 1) - 4 ) = 6
  * </PRE>
  */
public class DocumentImpl implements Document
{
  /** Default construction. Content left empty. */
  public DocumentImpl() {
    content = new DocumentContentImpl();
  } // default construction

  /** Construction from URL; content read over the net. */
  public DocumentImpl(URL u) { this(u, null, null); }

  /** Construction from URL and offsets. Both offsets must be
    * non-null, or they are both ignored. Content is read over the
    * net between the offsets.
    */
  public DocumentImpl(URL u, Long start, Long end) {
    // store the offsets if they're non-null
    if(start != null && end != null) {
      sourceURLOffsets = new Long[2];
      sourceURLOffsets[0] = start;
      sourceURLOffsets[1] = end;
    }

    // get content out of the URL
    content = new DocumentContentImpl(u, start, end);
  } // DocumentImpl(u,start,end)

  /** Construction from String representing URL */
  public DocumentImpl(String urlString) throws MalformedURLException {
    this(new URL(urlString), null, null);
  } // DocumentImpl(urlString)

  /** Documents are identified by URLs */
  public URL getSourceURL() { throw new LazyProgrammerException(); }

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document.
    */
  public Long[] getSourceURLOffsets() { throw new LazyProgrammerException(); }

  /** Get the data store the document lives in. */
  public DataStore getDataStore() { throw new LazyProgrammerException(); }

  /** The content of the document: a String for text; MPEG for video; etc. */
  public DocumentContent getContent() { throw new LazyProgrammerException(); }

  /** Get the default set of annotations. The set is created if it
    * doesn't exist yet.
    */
  public AnnotationSet getAnnotations() { throw new LazyProgrammerException(); }

  /** Get a named set of annotations. Creates a new set if one with this
    * name doesn't exist yet.
    */
  public AnnotationSet getAnnotations(String name) {
    throw new LazyProgrammerException();
  } // getAnnotations(name)

  /** Get the features associated with this document. */
  public FeatureMap getFeatures() { return features; }

  /** Propagate changes to the document content. */
  public void edit(Long start, Long end, DocumentContent replacement)
  throws InvalidOffsetException {
    throw new LazyProgrammerException();
  } // edit(start,end,replacement)

  /** Generate and return the next annotation ID */
  public Integer getNextAnnotationId() {
    return new Integer(nextAnnotationId++);
  } // getNextAnnotationId

  /** Check that an offset is valid */
  public boolean isValidOffset(Long offset) {
    if(offset == null)
      return false;

    long o = offset.longValue();
    long len = content.size().longValue();
    if(o > len || o < 0)
      return false;
      
    return true;
  } // isValidOffset

  /** Check that both start and end are valid offsets and that
    * they constitute a valid offset range
    */
  public boolean isValidOffsetRange(Long start, Long end) {
    return
      isValidOffset(start) && isValidOffset(end) &&
      start.longValue() <= end.longValue();
  } // isValidOffsetRange(start,end)

  /** Generate and return the next node ID */
  public Integer getNextNodeId() { return new Integer(nextNodeId++); }

  /** Ordering based on URL.toString() and the URL offsets (if any) */
  public int compareTo(Object o) throws ClassCastException {
    DocumentImpl other = (DocumentImpl) o;
    return getOrderingString().compareTo(other.getOrderingString());
  } // compareTo

  /** Utility method to produce a string for comparison in ordering.
    * String is based on the source URL and offsets.
    */
  String getOrderingString() { 
    StringBuffer orderingString = new StringBuffer(sourceURL.toString());
    if(sourceURLOffsets != null) {
      orderingString.append(sourceURLOffsets[0].toString());
      orderingString.append(sourceURLOffsets[1].toString());
    }
    return orderingString.toString();
  } // getOrderingString()

  /** The features associated with this document. */
  FeatureMap features;

  /** The id of the next new annotation */
  int nextAnnotationId = 0;

  /** The id of the next new node */
  int nextNodeId = 0;

  /** The source URL */
  URL sourceURL = null;

  /** The content of the document */
  DocumentContent content;

  /** The range that the content comes from at the source URL
    * (or null if none).
    */
  Long[] sourceURLOffsets = null;
} // class DocumentImpl
