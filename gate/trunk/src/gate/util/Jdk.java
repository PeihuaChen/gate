/*
 *  Jdk.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 * 
 *  Hamish Cunningham, 04/05/00
 *
 *  $Id$
 *
 *  Developer notes:
 *
 *  It would be better to have a compile method that took two arrays
 *  of string for a set of classes, and to compile all in one go
 *
 *  Also, should change gate.jape.RHS to use the methods here for
 *  defining and instantiating classes.
 */

package gate.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import sun.toolsx.java.*;
import sun.toolsx.javac.*;


/** Jdk encapsulates some utilities for poking around in your Java
  * environment. 
  */
public class Jdk {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Anonymous construction. */
  public Jdk() {
  } // anon constructor

  /** main. */
  public static void main(String[] args) throws GateException {
    Jdk jdk = new Jdk();
    jdk.testMe();
  } // main

  /** Test method. Better to use TestJdk via the TestGate suite instead. */
  public void testMe() throws GateException {
    Out.println("Testing gate.util.Jdk");
    Out.println("getToolsHome(): " + getToolsHome());
  } // testMe

  /** Possible locations of the tools <CODE>bin</CODE> directory.
    * (relative to "java.home").
    */
  private String[] toolsLocations = {
    "/../bin",	  // jdk1.2 final gives java.home as the jre directory)
    "/bin",	  // (jdk1.1 and 1.2 betas give java.home as the jdk directory)
    "",	 	  // getting desperate!
    "/../../bin"  // e.g. we're running a JRE that's installed beside a JDK
  };

  /** Returns a File specifying the location of the JDK tools, i.e.
    * the location of programs like <CODE>java, javac, jar</CODE>. It
    * assumes that if it finds <CODE>javac</CODE> or <CODE>javac.exe</CODE>
    * then it found the tools home.
    */
  public File getToolsHome() throws GateException {
    File javaHome = new File(System.getProperty("java.home"));
    if(! javaHome.exists())
      throw new GateException(
        "directory " + javaHome.getPath() + " doesn't exist!"
      );

    // try the likely spots
    for(int i=0; i<toolsLocations.length; i++) {
      try {
        File javac = new
          File(javaHome.getCanonicalPath() + toolsLocations[i] + "/javac");
        if(javac.exists())
          return new File(javaHome.getCanonicalPath() + toolsLocations[i]);
        javac = new
          File(javaHome.getCanonicalPath() + toolsLocations[i] + "/javac.exe");
        if(javac.exists())
          return new File(javaHome.getCanonicalPath() + toolsLocations[i]);
      } catch(IOException e) {
      }
    }

    throw new GateException(
      "Found no javac or javac.exe in likely places relative to java.home"
    );
  } // getToolsHome

  /** Compile a class from its source code string.
    * @param className should have the package path to the source, e.g.
    * com/thing/MyClass.java.
    */
  public byte[] compile(String javaCode, String className)
  throws GateException {
    sun.toolsx.javac.Main compiler = new sun.toolsx.javac.Main(
      System.out, "gate.util.Jdk"
    );
    String argv[] = new String[5];
    argv[0] = "-classpath";
    argv[1] = System.getProperty("java.class.path");
    argv[2] = "-nodisk";
    argv[3] = className;
    argv[4] = javaCode;
    compiler.compile(argv);
    List compilerOutput = compiler.getCompilerOutput();

    Iterator iter = compilerOutput.iterator();
    while(iter.hasNext()) {
      byte[] classBytes = (byte[]) iter.next();

    if(classBytes == null || classBytes.length == 0)
    	throw new GateException("no bytes returned from compiler");

      // possibly this test is wrong - what about sources that contain
      // multiple classes or have inner classes? at any rate we currently
      // have no way to return them
    if(iter.hasNext())
    	throw
	      new GateException("only compiled one class but got multiple results");

      return classBytes;
    } // while

    throw new GateException("no compiler output");
  } // compile(String, String)


  /** Read the bytes for a class.
    * @param classFileName should have the path to the .class
    * file, e.g. com/thing/MyClass.class.
    */
  public byte[] readClass(String classFileName) throws GateException {
    byte[] classBytes = null;
    try {
      File f = new File(classFileName);
      FileInputStream fis = new FileInputStream(classFileName);
      classBytes = new byte[(int) f.length()];
      fis.read(classBytes, 0, (int) f.length());
      fis.close();
    } catch(IOException e) {
      throw(new GateException("couldn't read class bytes: " + e));
    }

    return classBytes;
  } // readClass

  /** Load a class.
    * @param classFileName is the path to the .class
    * file, e.g. com/thing/MyClass.class.
    */
  public Class loadActionClass(String classFileName) throws GateException {
    Class theClass = null;
    try {
      theClass = Gate.getClassLoader().loadClass(classFileName, true);
    } catch(Exception e) {
      e.printStackTrace();
      throw new GateException(
        "couldn't load " + classFileName + ": " + e.getMessage()
      );
    }

    return theClass;
  } // loadActionClass

  /** Define a class from its qualified name and
    * the byte array of its binary.
    * @param classQualified name should be e.g. com.thing.MyClass.
    * @param contains the bytes from a .class file.
    */
  public Class defineClass(String classQualifiedName, byte[] classBytes)
  throws GateException {
    Class theClass = null;
    try {
      theClass = Gate.getClassLoader().defineGateClass(
        classQualifiedName, classBytes, 0, classBytes.length
      );
    } catch(ClassFormatError e) {
      e.printStackTrace();
      throw new GateException(
        "couldn't define " + classQualifiedName + ": " + e
      );

    }
    Gate.getClassLoader().resolveGateClass(theClass);
    return theClass;
  } // defineClass

  /** Create an instance of a class. */
  public Object instantiateClass(Class theClass) throws GateException {
    Object theObject = null;
    try {
      theObject = theClass.newInstance();
    } catch(Exception e) {
      throw new GateException(
        "couldn't create instance of class " + theClass + ": " + e
      );
    }

    return theObject;
  } // instantiateClass

} // Jdk



