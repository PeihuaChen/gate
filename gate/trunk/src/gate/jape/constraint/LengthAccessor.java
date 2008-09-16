/*
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Eric Sword, 09/03/08
 *
 *  $Id$
 */
package gate.jape.constraint;

import gate.Annotation;

/**
 * Accessor that returns the length of the characters spanned by the annotation
 *
 * @version $Revision$
 * @author esword
 */
public class LengthAccessor extends MetaPropertyAccessor {

  /**
   * Return the length of the span of the annotation.
   */
  public Object getValue(Annotation annot, Object context) {
    if(annot == null) return 0;
    Long retVal = annot.getEndNode().getOffset()
            - annot.getStartNode().getOffset();
    return retVal;
  }

  /**
   * Always returns "length", the name of the meta-property which this
   * accessor provides.
   */
  @Override
  public Object getKey() {
    return "length";
  }
}
