/*
 *	DocumentContent.java
 *
 *	Hamish Cunningham, 15/Feb/2000
 *
 *	$Id$
 */

package gate;

import java.util.*;
import gate.util.*;

/** The content of Documents.
  */
public interface DocumentContent
{
  /** The contents under a particular span. */
  public DocumentContent getContent(Long start, Long end)
    throws InvalidOffsetException;

  /** The size of this content (e.g. character length for textual
    * content).
    */
  public Long size();

} // interface DocumentContent
