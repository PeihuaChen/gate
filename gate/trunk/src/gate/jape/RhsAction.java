/*
 * RhsAction.java
 * Hamish, 30/7/98, $Id$
 */

package gate.jape;
import gate.*;

/** An interface that defines what the action classes created
  * for RightHandSides look like.
  */
public interface RhsAction {

  public void doit(Document doc, LeftHandSide lhs) throws JapeException;

} // RhsAction

