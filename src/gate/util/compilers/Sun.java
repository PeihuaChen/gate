/*
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 18/Feb/2002
 *  Modified by Ian Roberts, 11/Dec/2004
 *
 *  $Id$
 */
package gate.util.compilers;

import java.io.*;
import java.util.*;

import com.sun.tools.javac.Main;

import gate.util.*;
import gate.Gate;
import gate.GateConstants;
import gate.creole.ExecutionException;

/**
 * This class copiles a set of java sources by accessing the java
 * compiler from tools.jar file in the jdk. As such, it will not run on
 * a JRE alone, but requires a JDK.
 */
public class Sun extends gate.util.Javac {

  public Sun() throws ClassNotFoundException {
    // attempt to load the sun javac Main class. If this fails then the
    // compiler selection algorithm in gate.util.Javac will fall back on
    // the default Eclipse compiler.
    Class.forName("com.sun.tools.javac.Main");
  }

  /**
   * Compiles a set of java sources and loads the compiled classes in
   * the gate class loader.
   * 
   * @param sources a map from fully qualified classname to java source
   * @throws GateException in case of a compilation error or warning. In
   *           the case of warnings the compiled classes are loaded
   *           before the error is raised.
   */
  public void compile(Map sources) throws GateException {
    if(classLoader == null) classLoader = Gate.getClassLoader();
    File workDir;
    File srcDir;
    File classesDir;
    try {
      workDir = File.createTempFile("gate", "");
      if(!workDir.delete())
        throw new GateRuntimeException("Cannot delete a temporary file!");
      if(!workDir.mkdir())
        throw new GateRuntimeException("Cannot create a temporary directory!");
      srcDir = new File(workDir, "src");
      if(!srcDir.mkdir())
        throw new GateRuntimeException("Cannot create a temporary directory!");
      classesDir = new File(workDir, "classes");
      if(!classesDir.mkdir())
        throw new GateRuntimeException("Cannot create a temporary directory!");
    }
    catch(IOException ioe) {
      throw new ExecutionException(ioe);
    }

    List sourceFiles = new ArrayList();
    List sourceListings = new ArrayList();

    Iterator fileIter = sources.keySet().iterator();
    while(fileIter.hasNext()) {
      String className = (String)fileIter.next();
      List pathComponents = getPathComponents(className);
      String source = (String)sources.get(className);
      File directory = getDirectory(srcDir, pathComponents);
      String fileName = (String)pathComponents.get(pathComponents.size() - 1);
      File srcFile = new File(directory, fileName + ".java");
      try {
        // we need to use the same encoding for writing the files and
        // for
        // compiling them: UTF-8 sounds like a good choice
        Writer fw = new OutputStreamWriter(
                new FileOutputStream(srcFile, false), "UTF-8");
        fw.write(source);
        fw.flush();
        fw.close();
        sourceFiles.add(srcFile.getCanonicalPath());
        sourceListings.add(source);
      }
      catch(IOException ioe) {
        throw new GateException(ioe);
      }
    }
    // all source files have now been saved to disk
    // Prepare the arguments for the javac invocation
    List args = new ArrayList();
    args.add("-sourcepath");
    args.add(srcDir.getAbsolutePath());
    args.add("-encoding");
    args.add("UTF-8");
    args.add("-d");
    args.add(classesDir.getAbsolutePath());
    // make a copy of the arguments in case we need to call class by
    // class
    List argsSave = new ArrayList(args);
    args.addAll(sourceFiles);
    // save the Err stream
    PrintStream oldErr = System.err;
    // call the compiler for all the classes at once
    int res = -1;
    try {
      // steal the err stream to avoid repeating error messages.
      // if there are errors they will be shown when compiling classes
      // individually

      // an initial size of 10K should be plenty; it grows if required
      // anyway
      System.setErr(new PrintStream(new ByteArrayOutputStream(10 * 1024)));
      res = Main.compile((String[])args.toArray(new String[args.size()]));
    }
    catch(Throwable t) {
      // if this throws exceptions then there's nothing else we can do.
      // restore the err stream
      System.setErr(oldErr);
      throw new GateRuntimeException(t);
    }
    finally {
      // restore the err stream
      System.setErr(oldErr);
    }

    boolean errors = res != 0;
    if(errors) {
      // we got errors: call class by class
      args = argsSave;
      for(int i = 0; i < sourceFiles.size(); i++) {
        String aSourceFile = (String)sourceFiles.get(i);
        args.add(aSourceFile);
        // call the compiler
        res = Main.compile((String[])args.toArray(new String[args.size()]));
        if(res != 0) {
          // javac writes the error to System.err; let's print the
          // source as well
          Err.prln("\nThe offending input was:\n");
          String source = (String)sourceListings.get(i);
          source = Strings.addLineNumbers(source);
          Err.prln(source);
        }
        args.remove(args.size() - 1);
      }

    }

    // load the newly compiled classes
    // load all classes from the classes directory
    try {
      loadAllClasses(classesDir, null);
    }
    catch(IOException ioe) {
      throw new GateException(ioe);
    }

    // delete the work directory
    Files.rmdir(workDir);

    if(errors)
      throw new GateException("There were errors; see error log for details!");
  }

  /**
   * Breaks a class name into path components.
   * 
   * @param classname
   * @return a {@link List} of {@link String}s.
   */
  protected static List getPathComponents(String classname) {
    // break the classname into pieces
    StringTokenizer strTok = new StringTokenizer(classname, ".", false);
    List pathComponents = new ArrayList();
    while(strTok.hasMoreTokens()) {
      String pathComponent = strTok.nextToken();
      pathComponents.add(pathComponent);
    }
    return pathComponents;
  }

  /**
   * Gets a file inside a parent directory from a list of path
   * components.
   * 
   * @param workDir
   * @param pathComponents
   * @return a {@link File} value.
   */
  protected static File getDirectory(File workDir, List pathComponents) {
    File currentDir = workDir;
    for(int i = 0; i < pathComponents.size() - 1; i++) {
      String dirName = (String)pathComponents.get(i);
      // create a new dir in the current directory
      currentDir = new File(currentDir, dirName);
      if(currentDir.exists()) {
        if(currentDir.isDirectory()) {
          // nothing to do
        }
        else {
          throw new GateRuntimeException(
                  "Path exists but is not a directory ( "
                          + currentDir.toString() + ")!");
        }
      }
      else {
        if(!currentDir.mkdir())
          throw new GateRuntimeException("Cannot create a temporary directory!");
      }
    }
    return currentDir;
  }

  /**
   * Loads the entire hierarchy of classes found in a parent directory.
   * 
   * @param classesDirectory
   */
  protected static void loadAllClasses(File classesDirectory, String packageName)
          throws IOException {
    File[] files = classesDirectory.listFiles();
    // adjust the package name
    if(packageName == null) {
      // top level directory -> not a package name
      packageName = "";
    }
    else {
      // internal directory -> a package name
      packageName += packageName.length() == 0
              ? classesDirectory.getName()
              : "." + classesDirectory.getName();
    }

    for(int i = 0; i < files.length; i++) {
      if(files[i].isDirectory())
        loadAllClasses(files[i], packageName);
      else {
        String filename = files[i].getName();
        if(filename.endsWith(".class")) {
          String className = packageName + "."
                  + filename.substring(0, filename.length() - 6);
          // load the class from the file
          byte[] bytes = Files.getByteArray(files[i]);
          classLoader.defineGateClass(className, bytes, 0, bytes.length);
        }
      }
    }

  }

  protected static GateClassLoader classLoader;
}
