/*
 *  DocumentImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
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
import gate.gui.*;
import gate.event.*;

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
public class DocumentImpl
extends AbstractLanguageResource implements Document, StatusReporter
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
        if(docFormat != null){
          docFormat.addStatusListener(new StatusListener(){
            public void statusChanged(String text){
              fireStatusChanged(text);
            }
          });
          docFormat.unpackMarkup(this);
        }
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
    fireAnnotationSetAdded(new DocumentEvent(this,
                                             DocumentEvent.ANNOTATION_SET_ADDED,
                                             null));
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
      DocumentEvent evt = new DocumentEvent(this,
                                            DocumentEvent.ANNOTATION_SET_ADDED,
                                            name);
      fireAnnotationSetAdded(evt);
      fireGateEvent(evt);
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

  /** Returns a GateXml document
    * @return a string representing a Gate Xml document
    */
  public String toXml(){
    StringBuffer xmlContent = new StringBuffer("");
    // Add xml header
    xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

    // Add the root element
    xmlContent.append("<GateDocument" +
        featuresToXml(this.getFeatures()) + ">\n");
    // Add plain text element
    xmlContent.append("<PlainText>");
    xmlContent.append("<![CDATA[");
    xmlContent.append(this.getContent().toString());
    xmlContent.append("]]>");
    xmlContent.append("</PlainText>\n");
    // Save the AnnotationSet element
    xmlContent.append(annotationSetToXml(this.getAnnotations()));
    // Add the end of GateDocument
    xmlContent.append("</GateDocument>");
    // return the XmlGateDocument
    return xmlContent.toString();
  }// toXml

  /** This method saves a FeatureMap as XML attributes.
    * @ param aFeatureMap the feature map that has to be saved as XML.
    * @ return a String like this: feat1="val1" feat2="val2" ...
    */
  private String featuresToXml(FeatureMap aFeatureMap){
    StringBuffer str = new StringBuffer("");

    if (aFeatureMap == null) return str.toString();

    Set keySet = aFeatureMap.keySet();
    Iterator keyIterator = keySet.iterator();
    while(keyIterator.hasNext()){
      Object key = keyIterator.next();
      Object value = aFeatureMap.get(key);
      str.append(" " + key + "=\"" + value + "\"");
    }// end While

    return str.toString();
  }//featuresToXml

  /** This method saves an AnnotationSet as XML.
    * @param anAnnotationSet The annotation set that has to be saved as XML.
    * @return a String like this: <AnnotationSet> <Annotation>....
    * </AnnotationSet>
    */
  private String annotationSetToXml(AnnotationSet anAnnotationSet){
    StringBuffer str = new StringBuffer("");

    str.append("<AnnotationSet>\n");
    if (anAnnotationSet == null){
      str.append("</AnnotationSet>\n");
      return str.toString();
    }// End if

    // Iterate through AnnotationSet and save each Annotation as XML
    Iterator iterator = anAnnotationSet.iterator();
    while (iterator.hasNext()){
      Annotation annot = (Annotation) iterator.next();
      str.append("<Annotation " + "Type=\"" + annot.getType() + "\" Start=\"" +
      annot.getStartNode().getOffset() + "\" End=\"" +
      annot.getEndNode().getOffset() + "\">\n");
      str.append("<Features" + featuresToXml(annot.getFeatures()) + "/>\n");
      str.append("</Annotation>\n");
    }// End while

    str.append("</AnnotationSet>\n");
    return str.toString();
  }// annotationSetToXml

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
  }
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }
  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
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
  private transient Vector statusListeners;
  private transient Vector documentListeners;
  private transient Vector gateListeners;
  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }

  /** Check: test 2 objects for equality */
  protected boolean check(Object a, Object b) {
    if( (a == null || b == null) )
      return a == b;

    return a.equals(b);
  } // check(a,b)

  /** Equals */
  public boolean equals(Object other) {
    DocumentImpl doc = (DocumentImpl) other;

//    if(! check(content, doc.content)) return false;
//    if(! check(defaultAnnots, doc.defaultAnnots)) return false;
    if(! check(encoding, doc.encoding)) return false;
//    if(! check(features, doc.features)) return false;
    if(markupAware != doc.markupAware) return false;
    if(! check(namedAnnotSets, doc.namedAnnotSets)) return false;
    if(nextAnnotationId != doc.nextAnnotationId) return false;
    if(nextNodeId != doc.nextNodeId) return false;
    if(! check(sourceUrl, doc.sourceUrl)) return false;
    if(! check(sourceUrlStartOffset, doc.sourceUrlStartOffset)) return false;
    if(! check(sourceUrlName, doc.sourceUrlName)) return false;
    if(! check(sourceUrlEndOffset, doc.sourceUrlEndOffset)) return false;

    return true;
  } // equals

  /** String respresentation */
  public String toString() {
    String n = Strings.getNl();
    StringBuffer s = new StringBuffer("DocumentImpl: " + n);
    s.append("  content:" + content + n);
    s.append("  defaultAnnots:" + defaultAnnots + n);
    s.append("  encoding:" + encoding + n);
    s.append("  features:" + features + n);
    s.append("  markupAware:" + markupAware + n);
    s.append("  namedAnnotSets:" + namedAnnotSets + n);
    s.append("  nextAnnotationId:" + nextAnnotationId + n);
    s.append("  nextNodeId:" + nextNodeId + n);
    s.append("  sourceUrl:" + sourceUrl + n);
    s.append("  sourceUrlStartOffset:" + sourceUrlStartOffset + n);
    s.append("  sourceUrlName:" + sourceUrlName + n);
    s.append("  sourceUrlEndOffset:" + sourceUrlEndOffset + n);
    s.append(n);

    return s.toString();
  } // toString

  public synchronized void removeDocumentListener(DocumentListener l) {
    if (documentListeners != null && documentListeners.contains(l)) {
      Vector v = (Vector) documentListeners.clone();
      v.removeElement(l);
      documentListeners = v;
    }
  }
  public synchronized void addDocumentListener(DocumentListener l) {
    Vector v = documentListeners == null ? new Vector(2) : (Vector) documentListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      documentListeners = v;
    }
  }
  protected void fireAnnotationSetAdded(DocumentEvent e) {
    if (documentListeners != null) {
      Vector listeners = documentListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((DocumentListener) listeners.elementAt(i)).annotationSetAdded(e);
      }
    }
  }
  protected void fireAnnotationSetRemoved(DocumentEvent e) {
    if (documentListeners != null) {
      Vector listeners = documentListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((DocumentListener) listeners.elementAt(i)).annotationSetRemoved(e);
      }
    }
  }
  public synchronized void removeGateListener(GateListener l) {
    if (gateListeners != null && gateListeners.contains(l)) {
      Vector v = (Vector) gateListeners.clone();
      v.removeElement(l);
      gateListeners = v;
    }
  }
  public synchronized void addGateListener(GateListener l) {
    Vector v = gateListeners == null ? new Vector(2) : (Vector) gateListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      gateListeners = v;
    }
  }
  protected void fireGateEvent(GateEvent e) {
    if (gateListeners != null) {
      Vector listeners = gateListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((GateListener) listeners.elementAt(i)).processGateEvent(e);
      }
    }
  }

} // class DocumentImpl
