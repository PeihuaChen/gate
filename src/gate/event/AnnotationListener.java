/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva 21/10/2001
 *
 *  $Id$
 */
package gate.event;

import java.util.EventListener;

/**
 * A listener for events fired by an {@link gate.AnnotationSet}
 * ({@link gate.event.AnnotationSetEvent})
 */
public interface AnnotationListener extends EventListener {

  /**Called when an {@link gate.Annotation} has been updated*/
  public void annotationUpdated(AnnotationEvent e);

}