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
import java.io.*;

/** An interface that defines what the action classes created
  * for RightHandSides look like.
  */
public interface RhsAction extends Serializable {

  /**
   * Fires the RHS action for a particular LHS match.
   * @param doc the document the RHS action will be run on
   * @param bindings A map containing the matc results from the LHS in the form
   * label(String) -> matched annotations (AnnotationSet)
   * @param annotations copy of the outputAS value provided for backward
   * compatibility
   * @param inputAS the input annotation set
   * @param outputAS the output annotation set
   * @throws JapeException
   */
  public void doit(Document doc, Map bindings, AnnotationSet annotations,
                   AnnotationSet inputAS, AnnotationSet outputAS)
              throws JapeException;

} // RhsAction
