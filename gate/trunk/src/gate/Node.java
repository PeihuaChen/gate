/*
 *	Node.java
 *
 *	Hamish Cunningham, 19/Jan/2000
 *
 *	$Id$
 */

package gate;
import java.util.*;
import gate.util.*;

/** Nodes in AnnotationGraphs. Immutable.
  */
public interface Node
{
  /** Id */
  public Long getId();

  /** Offset (will be null when the node is not anchored) */
  public Long getOffset();

} // interface Node
