/*
 *  Gate.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Hamish Cunningham, 31/07/98
 *
 *  $Id$
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
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

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

    // DERWENT
    try{
      // ask the web server from derwent if it's alive
      URL url = new URL("http://derwent.dcs.shef.ac.uk");
      url.getContent();
      // set the gateHomeReachable
      gateHomeReachable = true;

    } catch (MalformedURLException exception1){
      throw new GateException("The URL for derwent is malformed !");
    } catch (IOException exception2){
      gateHomeReachable = false;
    }

    //GATE.AC.UK
    try{
      // ask the web server from gate machine if it's alive
      URL url = new URL("http://www.gate.ac.uk");
      url.getContent();
      // set the gateHomeReachable
      gateAcUkReachable = true;

    } catch (MalformedURLException exception1){
      throw new GateException("The URL for gate.ac.uk is malformed !");
    } catch (IOException exception2){
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
