/*
 *  Document.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.net.*;

import gate.util.*;
import gate.event.*;


/** Represents the commonalities between all sorts of documents.
 */
public interface Document extends LanguageResource, Comparable {

  /** Documents are identified by URLs
   */
  public URL getSourceUrl();

  /** Set method for the document's URL
   */
  public void setSourceUrl(URL sourceUrl);

  /** Documents may be packed within files; in this case an optional pair of
   *  offsets refer to the location of the document.
   */
  public Long[] getSourceUrlOffsets();

  /** Documents may be packed within files; in this case an optional pair of
   *  offsets refer to the location of the document. This method gets the
   *  start offset.
   */
  public Long getSourceUrlStartOffset();

  /** Documents may be packed within files; in this case an optional pair of
   *  offsets refer to the location of the document. This method gets the
   *  end offset.
   */
  public Long getSourceUrlEndOffset();

  /** The content of the document: wraps e.g. String for text; MPEG for
   *  video; etc.
   */
  public DocumentContent getContent();

  /** Set method for the document content
   */
  public void setContent(DocumentContent newContent);

  /** Get the default set of annotations. The set is created if it
   *  doesn't exist yet.
   */
  public AnnotationSet getAnnotations();

  /** Get a named set of annotations. Creates a new set if one with this
   *  name doesn't exist yet.
   */
  public AnnotationSet getAnnotations(String name);

  /** Returns a map with the named annotation sets
    */
  public Map getNamedAnnotationSets();

  /**
   * Removes one of the named annotation sets.
   * Note that the default annotation set cannot be removed.
   * @param name the name of the annotation set to be removed
   */
  public void removeAnnotationSet(String name);

  /** Make the document markup-aware. This will trigger the creation
   *  of a DocumentFormat object at Document initialisation time; the
   *  DocumentFormat object will unpack the markup in the Document and
   *  add it as annotations. Documents are <B>not</B> markup-aware by default.
   *
   *  @param b markup awareness status.
   */
  public void setMarkupAware(Boolean b);

  /** Get the markup awareness status of the Document.
   *
   *  @return whether the Document is markup aware.
   */
  public Boolean getMarkupAware();

  /** Returns a GateXml document. This document is actually a serialization of
   *  a Gate Document in XML.
    * @return a string representing a Gate Xml document
    */
  public String toXml();

  /** Returns an XML document aming to preserve the original markups(
    * the original markup will be in the same place and format as it was
    * before processing the document) and include (if possible)
    * the annotations specified in the aSourceAnnotationSet.
    * <b>Warning:</b> Annotations from the aSourceAnnotationSet will be lost
    * if they will cause a crosed over situation.
    * @param aSourceAnnotationSet is an annotation set containing all the
    * annotations that will be combined with the original marup set.
    * @return a string representing an XML document containing the original
    * markup + dumped annotations form the aSourceAnnotationSet
    */
  public String toXml(Set aSourceAnnotationSet);

  /** Make changes to the content.
   */
  public void edit(Long start, Long end, DocumentContent replacement)
    throws InvalidOffsetException;

  /**
   * Adds a {@link gate.event.DocumentListener} to this document.
   * All the registered listeners will be notified of changes occured to the
   * document.
   */
  public void addDocumentListener(DocumentListener l);

  /**
   * Removes one of the previously registered document listeners.
   */
  public void removeDocumentListener(DocumentListener l);


  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method sets the
    * end offset.
    */
  public void setSourceUrlEndOffset(Long sourceUrlEndOffset);


  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method sets the
    * start offset.
    */
  public void setSourceUrlStartOffset(Long sourceUrlStartOffset);

} // interface Document

