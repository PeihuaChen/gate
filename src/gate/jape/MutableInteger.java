/* 
	MutableInteger.java - A mutable wrapper for int, so you can return
                        integer values via a method parameter

	Hamish Cunningham, 24/07/98

	$Id$
*/


package gate.jape;


/**
  * A mutable wrapper for int, so you can return
  * integer values via a method parameter. If public data members bother you
  * I suggest you get a hobby, or have more sex or something.
  */
public class MutableInteger implements java.io.Serializable
{
	public int value = 0;

} // class MutableInteger


