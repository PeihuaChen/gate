/* 
	Gate.java

	Hamish Cunningham, 31/07/98

	$Id$
*/


package gate.util;

import java.util.*;
import java.net.*;
import java.io.*;

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

    if(creoleLoader == null)
      creoleLoader = new CreoleLoaderImpl();

    // check net connection and set canReachGateHome
    // and canReachGateAcUk appropriately
    try{
      // determine an IP address for the given host name
      InetAddress inet = InetAddress.getByName("derwent.dcs.shef.ac.uk");

      // set the gateHomeReachable
      gateHomeReachable = true;

    } catch (UnknownHostException e) {
      // if no IP address for the host could be found then
      // set the gateHomeReachable on false
      gateHomeReachable = false;
    }

    try{
      // determine an IP address for www.gate.ac.uk
      InetAddress inet = InetAddress.getByName("www.gate.ac.uk");

      // set the gateAcUkReachable
      gateAcUkReachable = true;

    } catch (UnknownHostException e) {

      // if no IP address could be found
      // then set the gateAcUkReachable on false
      gateAcUkReachable = false;
    }

  } // init()

  /** Is access to the GATE internal server available? */
  private static boolean gateHomeReachable = false;

  /** Is access to gate.ac.uk available? */
  private static boolean gateAcUkReachable = false;

  /** Get reachability status of GATE internal server */
  public static boolean isGateHomeReachable() { return gateHomeReachable; }

  /** Get reachability status of GATE.ac.uk public server */
  public static boolean isGateAcUkReachable() { return gateAcUkReachable; }

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

  /** The CREOLE loader. */
  private static CreoleLoader creoleLoader = null;

  /** Get the CREOLE register. */
  public static CreoleLoader getCreoleLoader() { return creoleLoader; }


} // class Gate

