/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 08/03/2001
 *
 *  $Id$
 */
package gate.event;

import java.util.EventListener;

/**
 * A listener for document events ({@link gate.event.DocumentEvent}).
 */
public interface DocumentListener extends EventListener {
  /**Called when a new {@link gate.AnnotationSet} has been added*/
  public void annotationSetAdded(DocumentEvent e);

  /**Called when an {@link gate.AnnotationSet} has been removed*/
  public void annotationSetRemoved(DocumentEvent e);

}