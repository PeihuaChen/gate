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

import gate.*;

/**
 * Accessor that returns the underlying string of an annotation in a document.
 * The string is cleaned up a bit as follows:
 * <code>
 * cleanString = string.replaceAll("\\s+", " ").trim();
 * </code>
 *
 * @version $Revision$
 * @author esword
 */
public class CleanStringAccessor extends StringAccessor {
  /**
   * Return the cleaned up underlying string for the annotation. Context
   * must be a {@link Document} or an {@link AnnotationSet} which points
   * to the document.
   */
  public Object getValue(Annotation annot, Object context) {
    String retVal = (String)super.getValue(annot, context);
    if (retVal != null)
      retVal = retVal.replaceAll("\\s+", " ").trim();

    return retVal;
  }

  /**
   * Always returns "cleanString", the name of the meta-property which this
   * accessor provides.
   */
  @Override
  public Object getKey() {
    return "cleanString";
  }

}
