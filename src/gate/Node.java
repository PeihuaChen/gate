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
  public Integer getId();

  /** Offset (will be null when the node is not anchored) */
  public Double getOffset();

  /** Does this node structurally precede n? */
  public boolean sPrecedes(Node n);

  /** Does this node temporally (i.e. by offset) precede n? */
  public boolean tPrecedes(Node n);

  /** Does this node precede n? */
  public boolean precedes(Node n);

} // interface Node
