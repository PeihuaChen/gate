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
abstract public class TextualDocument implements Document
{

  /** The contents of the document */
  public String getContents() { return ""; }

  /** The contents of a particular span */
  public String getContent(Annotation a) { return ""; }

  /** Main routine. */
  public static void main(String[] args) {
	  
  } // main

} // class TextualDocument
