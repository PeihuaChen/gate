/*
 *  DocumentImpl.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;
import gate.creole.*;

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
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction. Content left empty. */
  public DocumentImpl() {
    content = new DocumentContentImpl();
  } // default construction

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {

    // set up the source URL and create the content
    if(sourceUrl == null && sourceUrlName != null)
      try {
        sourceUrl = new URL(sourceUrlName);
        content = new DocumentContentImpl(
          sourceUrl, encoding, sourceUrlStartOffset, sourceUrlEndOffset
        );
      } catch(IOException e) {
        throw new ResourceInstantiationException("DocumentImpl.init: " + e);
      }

    // record the source URL name in case we only got the URL itself
    if(sourceUrlName == null && sourceUrl != null)
      sourceUrlName = sourceUrl.toExternalForm();

    // set up a DocumentFormat if markup unpacking required
    if(isMarkupAware()) {
      DocumentFormat docFormat =
        DocumentFormat.getDocumentFormat(this, sourceUrl);
      try {
        if(docFormat != null) docFormat.unpackMarkup(this);
      } catch(DocumentFormatException e) {
        throw new ResourceInstantiationException(
          "Couldn't unpack markup in document " + sourceUrlName + e
        );
      }
    } // if markup aware

    return this;
  } // init()

  /** Documents are identified by URLs */
  public URL getSourceUrl() { return sourceUrl; }

  /** Set method for the document's URL */
  public void setSourceUrl(URL sourceUrl) {
    this.sourceUrl = sourceUrl;
  } // setSourceUrl

  /** Get method for the document's URL name (i.e. the string that
    * describes the URL).
    */
  public String getSourceUrlName() { return sourceUrlName; }

  /** Set method for the document's URL name (i.e. the string that
    * describes the URL).
    */
  public void setSourceUrlName(String sourceUrlName) {
    this.sourceUrlName = sourceUrlName;
  } // setSourceUrlName

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document.
    */
  public Long[] getSourceUrlOffsets() {
    Long[] sourceUrlOffsets = new Long[2];
    sourceUrlOffsets[0] = sourceUrlStartOffset;
    sourceUrlOffsets[1] = sourceUrlEndOffset;
    return sourceUrlOffsets;
  } // getSourceUrlOffsets

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method gets the
    * start offset.
    */
  public Long getSourceUrlStartOffset() { return sourceUrlStartOffset; }

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method sets the
    * start offset.
    */
  public void setSourceUrlStartOffset(Long sourceUrlStartOffset) {
    this.sourceUrlStartOffset = sourceUrlStartOffset;
  } // setSourceUrlStartOffset

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method gets the
    * end offset.
    */
  public Long getSourceUrlEndOffset() { return sourceUrlEndOffset; }

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method sets the
    * end offset.
    */
  public void setSourceUrlEndOffset(Long sourceUrlEndOffset) {
    this.sourceUrlEndOffset = sourceUrlEndOffset;
  } // setSourceUrlStartOffset

  /** Get the data store the document lives in. */
  public DataStore getDataStore() {
    return null;
  } // getDataStore

  /** The content of the document: a String for text; MPEG for video; etc. */
  public DocumentContent getContent() { return content; }

  /** Set method for the document content */
  public void setContent(DocumentContent content) { this.content = content; }

  /** Get the encoding of the document content source */
  public String getEncoding() { return encoding; }

  /** Set the encoding of the document content source */
  public void setEncoding(String encoding) { encoding = encoding; }

  /** Get the default set of annotations. The set is created if it
    * doesn't exist yet.
    */
  public AnnotationSet getAnnotations() {
    if(defaultAnnots == null)
      defaultAnnots = new AnnotationSetImpl(this);
    return defaultAnnots;
  } // getAnnotations()

  /** Get a named set of annotations. Creates a new set if one with this
    * name doesn't exist yet.
    */
  public AnnotationSet getAnnotations(String name) {
    if(namedAnnotSets == null)
      namedAnnotSets = new HashMap();
    AnnotationSet namedSet = (AnnotationSet) namedAnnotSets.get(name);

    if(namedSet == null) {
      namedSet = new AnnotationSetImpl(this, name);
      namedAnnotSets.put(name, namedSet);
    }
    return namedSet;
  } // getAnnotations(name)

  /** Is the document markup-aware? */
  protected boolean markupAware = false;

  /** Make the document markup-aware. This will trigger the creation
   *  of a DocumentFormat object at Document initialisation time; the
   *  DocumentFormat object will unpack the markup in the Document and
   *  add it as annotations. Documents are <B>not</B> markup-aware by default.
   *
   *  @param b markup awareness status.
   */
  public void setMarkupAware(boolean b) { this.markupAware = b; }

  /** Get the markup awareness status of the Document.
   *  <B>Documents are markup-aware by default.</B>
   *  @return whether the Document is markup aware.
   */
  public boolean isMarkupAware() { return markupAware; }

  /** Returns a map with the named annotation sets
    */
  // This was needed by the constructor on DocumentWrapper that
  // takes a DocumentImpl.
  public Map getNamedAnnotationSets(){
    return namedAnnotSets;
  } // getNamedAnnotationSets

  /** Get the features associated with this document. */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; }

  /** Propagate edit changes to the document content and annotations. */
  public void edit(Long start, Long end, DocumentContent replacement)
    throws InvalidOffsetException
  {
    if(! isValidOffsetRange(start, end))
      throw new InvalidOffsetException();

    if(content != null)
      ((DocumentContentImpl) content).edit(start, end, replacement);

    if(defaultAnnots != null)
      ((AnnotationSetImpl) defaultAnnots).edit(start, end, replacement);

    if(namedAnnotSets != null) {
      Iterator iter = namedAnnotSets.values().iterator();
      while(iter.hasNext())
        ((AnnotationSetImpl) iter.next()).edit(start, end, replacement);
    }

  } // edit(start,end,replacement)

  /** Check that an offset is valid, i.e. it is non-null, greater than
    * or equal to 0 and less than the size of the document content.
    */
  public boolean isValidOffset(Long offset) {
    if(offset == null)
      return false;

    long o = offset.longValue();
    if(o > content.size().longValue() || o < 0)
      return false;

    return true;
  } // isValidOffset

  /** Check that both start and end are valid offsets and that
    * they constitute a valid offset range, i.e. start is greater
    * than or equal to long.
    */
  public boolean isValidOffsetRange(Long start, Long end) {
    return
      isValidOffset(start) && isValidOffset(end) &&
      start.longValue() <= end.longValue();
  } // isValidOffsetRange(start,end)

  /** Generate and return the next annotation ID */
  public Integer getNextAnnotationId() {
    return new Integer(nextAnnotationId++);
  } // getNextAnnotationId

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
  protected String getOrderingString() {
    if(sourceUrl == null) return toString();

    StringBuffer orderingString = new StringBuffer(sourceUrl.toString());
    if(sourceUrlStartOffset != null && sourceUrlEndOffset != null) {
      orderingString.append(sourceUrlStartOffset.toString());
      orderingString.append(sourceUrlEndOffset.toString());
    }

    return orderingString.toString();
  } // getOrderingString()

  /** The features associated with this document. */
  protected FeatureMap features;

  /** The id of the next new annotation */
  protected int nextAnnotationId = 0;

  /** The id of the next new node */
  protected int nextNodeId = 0;
  /** The source URL */
  protected URL sourceUrl;

  /** The document's URL name. */
  protected String sourceUrlName;

  /** The content of the document */
  protected DocumentContent content;

  /** The encoding of the source of the document content */
  protected String encoding;

  /** The range that the content comes from at the source URL
    * (or null if none).
    */
  //protected Long[] sourceUrlOffsets;

  /** The start of the range that the content comes from at the source URL
    * (or null if none).
    */
  protected Long sourceUrlStartOffset;

  /** The end of the range that the content comes from at the source URL
    * (or null if none).
    */
  protected Long sourceUrlEndOffset;

  /** The default annotation set */
  protected AnnotationSet defaultAnnots;

  /** Named sets of annotations */
  protected Map namedAnnotSets;

} // class DocumentImpl
