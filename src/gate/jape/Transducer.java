/* 
	Transducer.java - transducer class

	Hamish Cunningham, 24/07/98

	$Id$
*/


package gate.jape;

import java.util.*;
import com.objectspace.jgl.*;
import gate.annotation.*;
import gate.util.*;
import gate.*;


/**
  * Represents a single or multiphase transducer.
  */
public abstract class Transducer implements java.io.Serializable
{
  /** Name of this transducer. */
  protected String name;

  /** Get the phase name of this transducer */
  public String getName() { return name; }

  /** Transduce a document.  */
  public abstract void transduce(Document doc) throws JapeException;

  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  public abstract void finish();

  /** Clean up (delete action class files, for e.g.). */
  public abstract void cleanUp();

  /** Create a string representation of the object with padding. */
  public abstract String toString(String pad);

} // interface Transducer



// $Log$
// Revision 1.1  2000/02/23 13:46:13  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:03  hamish
// added gate2
//
// Revision 1.11  1998/11/01 21:21:42  hamish
// use Java arrays in transduction where possible
//
// Revision 1.10  1998/10/29 12:09:09  hamish
// added serializable
//
// Revision 1.9  1998/09/18 13:36:03  hamish
// made Transducer a class
//
// Revision 1.8  1998/08/19 20:21:47  hamish
// new RHS assignment expression stuff added
//
// Revision 1.7  1998/08/12 15:39:46  hamish
// added padding toString methods
//
// Revision 1.6  1998/08/10 14:16:43  hamish
// fixed consumeblock bug and added batch.java
//
// Revision 1.5  1998/08/07 16:18:49  hamish
// parser pretty complete, with backend link done
