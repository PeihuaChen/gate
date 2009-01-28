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
 * Accessor that returns the annotation itself
 *
 * @version $Revision$
 * @author esword
 */
public class SimpleAnnotationAccessor extends MetaPropertyAccessor {

  public Object getValue(Annotation annot, AnnotationSet context) {
    return annot;
  }

  @Override
  public Object getKey() {
    return null;
  }
}
