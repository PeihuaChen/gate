/*
 *  RhsAction.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish, 30/7/98
 *
 *  $Id$
 */

package gate.jape;
import gate.*;
import java.util.Map;

/** An interface that defines what the action classes created
  * for RightHandSides look like.
  */
public interface RhsAction {

  public void doit(Document doc, AnnotationSet annotations, Map bindings)
              throws JapeException;

} // RhsAction
