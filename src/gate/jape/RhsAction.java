/*
 * RhsAction.java
 * Hamish, 30/7/98, $Id$
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

