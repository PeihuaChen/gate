/*
 *  RhsAction.java
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
