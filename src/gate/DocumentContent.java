/*
 *  DocumentContent.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 15/Feb/2000
 *
 *  $Id$
 */

package gate;

import java.io.Serializable;

import gate.util.InvalidOffsetException;

/** The content of Documents.
  */
public interface DocumentContent extends Serializable {

  /** The contents under a particular span. */
  public DocumentContent getContent(Long start, Long end)
    throws InvalidOffsetException;

  /** The size of this content (e.g. character length for textual
    * content).
    */
  public Long size();

} // interface DocumentContent
