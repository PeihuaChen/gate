/* 
	Gate.java

	Hamish Cunningham, 31/07/98

	$Id$
*/


package gate.util;


/**
  * The class is responsible for initialising the GATE libraries.
  */
public class Gate
{
  /** Class loader used e.g. for loading CREOLE modules, of compiling
    * JAPE rule RHSs.
    */
  private static GateClassLoader classLoader = new GateClassLoader();

  /** Get the GATE class loader. */
  public static GateClassLoader getClassLoader() { return classLoader; }

} // class Gate

