/* 
	JapeConstants.java

	Hamish Cunningham, 09/07/98

	$Id$
*/


package gate.jape;

import gate.*;
import gate.annotation.*;
import gate.util.*;
import java.util.*;

/**
  * Constants interface for the JAPE package.
  */
public interface JapeConstants
{

  /** no Kleene operator */
  public int NO_KLEENE_OP		=  0;
  /** Kleene star (*) */
  public int KLEENE_STAR		=  1;
  /** Kleene plus (+) */
  public int KLEENE_PLUS		=  2;
  /** Kleene query (?) */
  public int KLEENE_QUERY		=  3;

  /** No binding on this element */
  public int NO_BINDING			=  1;
  public int MULTI_SPAN_BINDING		=  2;
  public int SINGLE_SPAN_BINDING	=  3;

  /** Brill-style rule application */
  public int BRILL_STYLE = 1;
  /** Appelt-style rule application */
  public int APPELT_STYLE = 2;

  /** The default priority of a rule. */
  public int DEFAULT_PRIORITY = -1;

  /** How far to increase indent when padding toString invocations. */
  public int INDENT_PADDING = 4;

} // JapeConstants





// $Log$
// Revision 1.1  2000/02/23 13:46:06  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:01  hamish
// added gate2
//
// Revision 1.5  1998/08/12 15:39:36  hamish
// added padding toString methods
//
// Revision 1.4  1998/07/31 13:12:18  mks
// done RHS stuff, not tested
//
// Revision 1.3  1998/07/30 11:05:17  mks
// more jape
//
// Revision 1.2  1998/07/29 11:06:58  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives
