/* 
	Gate.java

	Hamish Cunningham, 31/07/98

	$Id$
*/


package gate.util;

import java.util.*;
import java.net.*;

import gate.*;
import gate.creole.*;

/**
  * The class is responsible for initialising the GATE libraries.
  */
public class Gate
{
  /** The list of builtin URLs to search for CREOLE resources. */
  private static String builtinCreoleDirectoryUrls[] = {
    "http://gate.ac.uk/creole/creole.xml"
  };

  /** Initialisation - must be called by all clients before using
    * any other parts of the library.
    */
  public static void init() throws GateException {
    if(classLoader == null)
      classLoader = new GateClassLoader();

    if(creoleRegister == null)
      creoleRegister = new CreoleRegisterImpl();
  } // init()

  /** Initialise the CREOLE register. */
  public static void initCreoleRegister() throws GateException {

    // register the builtin CREOLE directories
    for(int i=0; i<builtinCreoleDirectoryUrls.length; i++)
      try {
        creoleRegister.addDirectory(
          new URL(builtinCreoleDirectoryUrls[i])
        );
      } catch(MalformedURLException e) {
        throw new GateException(e);
      }
    creoleRegister.registerDirectories();
  } // initCreoleRegister

  /** Class loader used e.g. for loading CREOLE modules, of compiling
    * JAPE rule RHSs.
    */
  private static GateClassLoader classLoader = null;

  /** Get the GATE class loader. */
  public static GateClassLoader getClassLoader() { return classLoader; }

  /** The CREOLE register. */
  private static CreoleRegister creoleRegister = null;

  /** Get the CREOLE register. */
  public static CreoleRegister getCreoleRegister() { return creoleRegister; }


} // class Gate

