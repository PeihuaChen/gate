/*
 *	DocumentContent.java
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
 *	Hamish Cunningham, 15/Feb/2000
 *
 *	$Id$
 */

package gate;

import java.util.*;
import gate.util.*;

/** The content of Documents.
  */
public interface DocumentContent
{
  /** The contents under a particular span. */
  public DocumentContent getContent(Long start, Long end)
    throws InvalidOffsetException;
 
  /** The size of this content (e.g. character length for textual
    * content).
    */
  public Long size();


} // interface DocumentContent