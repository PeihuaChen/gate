/*
 *	TextualDocument.java
 *
 *	Hamish Cunningham, 20/Jan/2000
 *
 *	$Id$
 */

package gate;
import java.util.*;
import gate.util.*;

/** 
  * Documents whose content is text
  */
public interface TextualDocument extends Document
{
  /** The contents under a particular annotation span. */
  public String getContent(Annotation a);

} // interface TextualDocument
