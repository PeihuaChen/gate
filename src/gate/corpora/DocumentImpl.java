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
  * 
  * <H2>Editing</H2>
  * 
  * <P>
  * The DocumentContent class models the textual or audio-visual
  * materials which are the source and content of Documents.
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
  * On DC, this method actually makes the change - e.g. replacing
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
  *       (end - start) +
  *       ( (replacement == null) ? 0 : replacement.size() )
  *     );
  * </PRE>
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
