/*
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 18/Feb/2002
 *
 *  $Id$
 */
package gate.util;

import java.io.*;
import java.util.*;

import gate.Gate;
import gate.GateConstants;

/**
 * This class compiles a set of java sources using the user's preferred Java
 * compiler.  The default compiler used is the Eclipse JDT compiler, but this
 * can be overridden by the user via an option in gate.xml.
 */
public abstract class Javac implements GateConstants {

  /**
   * Compiles a set of java sources and loads the compiled classes in the gate
   * class loader.
   * @param sources a map from fully qualified classname to java source
   * @throws GateException in case of a compilation error or warning.
   * In the case of warnings the compiled classes are loaded before the error is
   * raised.
   */
  public static void loadClasses(Map sources) throws GateException {
    if(compiler == null) {
      setCompilerTypeFromUserConfig();
    }

    compiler.compile(sources);
  }

  /**
   * Sets the type of compiler to be used, based on the user's configuration.
   * The default is to use the Eclipse compiler unless the user requests
   * otherwise.
   */
  private static void setCompilerTypeFromUserConfig() throws GateException {
    if(classLoader == null) classLoader = Gate.getClassLoader();
    // see if the user has expressed a preference
    String compilerType = Gate.getUserConfig().getString(COMPILER_TYPE_KEY);
    // if not, use the default
    if(compilerType == null) {
      compilerType = DEFAULT_COMPILER;
    }

    // We try and load the compiler class first by treating the given name as a
    // fully qualified class name.  If this fails, we prepend
    // "gate.util.compilers." (so the user can say just "Sun" rather than
    // "gate.util.compilers.Sun").  If that fails, we try the default value
    // DEFAULT_COMPILER.  If that fails, we give up.
    Class compilerClass = null;
    try {
      // first treat the compiler type as a fully qualified class name
      compilerClass = classLoader.loadClass(compilerType, true);
    }
    catch(ClassNotFoundException cnfe) {
      // not a problem
    }

    if(compilerClass == null 
        || !Javac.class.isAssignableFrom(compilerClass)) {
      // failed to find the class as a FQN, so try relative to
      // gate.util.compilers
      compilerType = "gate.util.compilers." + compilerType;
      try {
        compilerClass = classLoader.loadClass(compilerType, true);
      }
      catch(ClassNotFoundException cnfe2) {
        // still not a problem
      }
    }

    if(compilerClass == null
        || !Javac.class.isAssignableFrom(compilerClass)) {
      Err.prln("Unable to load compiler class " + compilerType 
          + ", falling back to default of " + DEFAULT_COMPILER);
      compilerType = DEFAULT_COMPILER;
      try {
        compilerClass = classLoader.loadClass(compilerType, true);
      }
      catch(ClassNotFoundException cnfe3) {
        // a problem
      }
    }

    if(compilerClass == null
        || !Javac.class.isAssignableFrom(compilerClass)) {
      throw new GateException("Unable to load a Java compiler class");
    }
    
    // At this point we have successfully loaded a compiler class.
    // Now create an instance using a no-argument constructor.
    try {
      compiler = (Javac)compilerClass.newInstance();
    }
    catch(IllegalAccessException iae) {
      Err.prln("Cannot access Java compiler class " + compilerType);
      throw new GateException(iae);
    }
    catch(InstantiationException ie) {
      Err.prln("Cannot instantiate Java compiler class " + compilerType);
      throw new GateException(ie);
    }
  }

  /**
   * Compile a set of Java sources, and load the resulting classes into the
   * GATE class loader.
   *
   * @param sources a map from fully qualified classname to java source
   * @throws GateException in case of a compilation error or warning.
   * In the case of warnings, the compiled classes are loaded before the
   * exception is thrown.
   */
  public abstract void compile(Map sources) throws GateException;

  /**
   * The compiler to use.
   */
  private static Javac compiler = null;

  /**
   * The GATE class loader.
   */
  private static GateClassLoader classLoader = null;
  
  /**
   * The default compiler to use.
   */
  public static final String DEFAULT_COMPILER = "gate.util.compilers.Eclipse";
}
