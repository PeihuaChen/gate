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

  /** The contents of the document */
  public String getCurrentContent();

  /** The contents of a particular span */
  public String getContentOf(Annotation a);

} // interface TextualDocument
