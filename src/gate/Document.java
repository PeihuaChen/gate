/*
 *	Document.java
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
 *  Hamish Cunningham, 19/Jan/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.net.*;

import gate.util.*;


/** Represents the commonalities between all sorts of documents.
  */
public interface Document extends LanguageResource, Comparable
{
  /** Documents are identified by URLs */
  public URL getSourceURL();

  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document.
    */
  public Long[] getSourceURLOffsets();

  /** The content of the document: wraps e.g. String for text; MPEG for
    * video; etc.
    */
  public DocumentContent getContent();

  /** Mutator method
    */
  public void setContent(DocumentContent newContent);

  /** Get the default set of annotations. The set is created if it
    * doesn't exist yet.
    */
  public AnnotationSet getAnnotations();

  /** Get a named set of annotations. Creates a new set if one with this
    * name doesn't exist yet.
    */
  public AnnotationSet getAnnotations(String name);


  /** Make changes to the content. */
  public void edit(Long start, Long end, DocumentContent replacement)
    throws InvalidOffsetException;

} // interface Document