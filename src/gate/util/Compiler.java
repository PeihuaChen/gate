/*
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
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

import com.sun.tools.javac.Main;

import java.io.*;
import java.util.*;

import gate.util.*;
import gate.*;

/**
 * This class copiles a set of java sources by accessing the java compiler
 * from tools.jar file in the jdk.
 * All processing is done without touching the disk.
 */
public class Compiler implements GateConstants{

  /**
   * Compiles a set of java sources and loads the compiled classes in the gate
   * class loader.
   * @param sources a map from fully qualified classname to java source
   * @throws GateException in case of a compilation error or warning.
   * In the case of warnings the compiled classes are loaded before the error is
   * raised.
   */
  public static void loadClasses(Map sources)throws GateException{
    File workDir;
    try{
      workDir = File.createTempFile("gate", "");
      if(!workDir.delete()) throw new GateRuntimeException(
            "Cannot delete a temporary file!");
      if(! workDir.mkdir())throw new GateRuntimeException(
            "Cannot delete a temporary directory!");
    }catch(IOException ioe){
      throw new GateRuntimeException("Cannot create a temporary file!");
    }

    List sourceFiles = new ArrayList();

    Iterator fileIter = sources.keySet().iterator();
    while(fileIter.hasNext()){
      String className = (String)fileIter.next();
      List pathComponents = getPathComponents(className);
      String source = (String)sources.get(className);
      File directory = getDirectory(workDir, pathComponents);
      String fileName = (String) pathComponents.get(pathComponents.size() - 1);
      File srcFile = new File(directory, fileName + ".java");
      try{
        FileWriter fw = new FileWriter(srcFile);
        fw.write(source);
        fw.flush();fw.close();
        sourceFiles.add(srcFile.getCanonicalPath());
      }catch(IOException ioe){
        throw new GateException(ioe);
      }
    }
    //all source files have now been saved to disk
    //Prepare the arguments for the javac invocation
    List args = new ArrayList();
    args.add("-sourcepath");
    args.add(workDir.getAbsolutePath());
    args.addAll(sourceFiles);
//System.out.print(args);
    //call the compiler
    Main.compile((String[])args.toArray(new String[args.size()]));
    //load the newly compiled classes
    try{
      GateClassLoader classLoader = Gate.getClassLoader();
      classLoader.addURL(workDir.toURL());
      Iterator classIter = sources.keySet().iterator();
      while(classIter.hasNext())
        classLoader.loadClass((String)classIter.next());
    }catch(java.net.MalformedURLException mue){
      //this should never occur
      throw new GateException(mue);
    }catch(ClassNotFoundException cnfe){
      //this should never occur
      throw new GateException(cnfe);
    }
    //delete the work directory
    Files.rmdir(workDir);
  }

  protected static List getPathComponents(String classname){
    //break the classname into pieces
    StringTokenizer strTok = new StringTokenizer(classname, ".", false);
    List pathComponents = new ArrayList();
    while(strTok.hasMoreTokens()){
      String pathComponent = strTok.nextToken();
      pathComponents.add(pathComponent);
    }
    return pathComponents;
  }

  protected static File getDirectory(File workDir, List pathComponents){
    File currentDir = workDir;
    for(int i = 0; i < pathComponents.size() - 1; i++){
      String dirName = (String)pathComponents.get(i);
      //create a new dir in the current directory
      currentDir = new File(currentDir, dirName);
      if(currentDir.exists()){
        if(currentDir.isDirectory()){
          //nothing to do
        }else{
          throw new GateRuntimeException(
            "Path exists but is not a directory ( " +
            currentDir.toString() + ")!");
        }
      }else{
        if (!currentDir.mkdir())
          throw new GateRuntimeException(
              "Cannot create a temporary directory!");
      }
    }
    return currentDir;
  }

}