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


import com.sun.tools.javac.v8.util.*;
import com.sun.tools.javac.v8.comp.*;
import com.sun.tools.javac.v8.code.*;
import com.sun.tools.javac.v8.*;

import java.io.*;
import java.util.Map;
import java.util.ArrayList;

import gate.util.*;
import gate.*;

/**
 * This class copiles a set of java sources by accessing the java compiler
 * from tools.jar file in the jdk.
 * All processing is done without touching the disk.
 */
public class Javac{

  protected static class MemoryLog extends Log{
    MemoryLog(Map sources){
      super();
      this.sources = sources;
      errorsString = new StringBuffer();
    }

    public void error(int pos, String key, String arg0, String arg1,
                      String arg2, String arg3, String arg4, String arg5,
                      String arg6) {
      if(nerrors < MaxErrors){
        String msg = getText("compiler.err." + key, arg0, arg1, arg2, arg3,
                arg4, arg5, arg6);

        if (pos == Position.NOPOS) {
          errorsString.append(getText("compiler.err.error", null, null, null,
                                  null, null, null, null));
          errorsString.append(msg + Strings.getNl());
        }else{
          int line = Position.line(pos);
          int col = Position.column(pos);
          errorsString.append("Compilation error in " +
                              className + ":" + line + ": " + msg +
                              Strings.getNl() + Strings.getNl());

          String sourceCode = (String)sources.get(className);
          errorsString.append("The offending input was :" + Strings.getNl() +
                              (sourceCode == null || sourceCode.equals("") ?
                               "<not available>" :
                               Strings.addLineNumbers(sourceCode)) +
                              Strings.getNl());
        }
        prompt();
        nerrors++;
      }
    }

    //redirect automatic error reporting from System.err to memory
    public void print(String s) {
      errorsString.append(s);
    }//print
    Map sources;
    StringBuffer errorsString;
    String className;
  }

  protected static class GJC extends JavaCompiler{
    GJC(MemoryLog log, Symtab syms, Hashtable options, Map sources){
      super(log, syms, options);
      this.sources = sources;
      this.memLog = log;
    }

    /**
     * Overidden so that it reads the sources from the provided Map rather than
     * from the disk.
     * @param fileName the name of the file that should contain the source.
     * @return an input stream for the java source.
     */
    public InputStream openSource(String fileName) {
//Out.prln("Read request for: " + fileName);
      String className = fileName.substring(0, fileName.lastIndexOf(".java"));
      className = className.replace('/', '.');
      className = className.replace('\\', '.');
      String classSource = (String)sources.get(className);
      memLog.className = className;
//Out.prln("Source for: " + className + "\n" + classSource);
      return new ByteArrayInputStream(classSource.getBytes());
    }

    /**
     * Overidden so it loads the compiled class in the gate classloader rather
     * than writting it on the disk.
     * @param c the class symbol
     * @throws IOException
     */
    public void writeClass(com.sun.tools.javac.v8.code.Symbol.ClassSymbol c)
                throws IOException {
      //the compiler will try to write the class file too;
      //we'll just load the class instead
      ByteArrayOutputStream os = new ByteArrayOutputStream(2000);
      new ClassWriter(Hashtable.make()).writeClassFile(os, c);
      os.flush();
      byte[] bytes = os.toByteArray();
      String className = c.className();
      //replace the final '.' with '$' for inner classes
      if(c.isInner()){
        int loc = className.lastIndexOf('.');
        className = className.substring(0, loc) + "$" +
                    className.substring(loc + 1);
      }
//Out.pr(className + "[" + os.size() + " bytes]");
      Gate.getClassLoader().defineGateClass(className,
                                            bytes, 0, os.size());
    }

    Map sources;
    MemoryLog memLog;
  }

  /**
   * Compiles a set of java sources and loads the compiled classes in the gate
   * class loader.
   * @param sources a map from fully qualified classname to java source
   * @throws GateException in case of a compilation error or warning.
   * In the case of warnings the compiled classes are loaded before the error is
   * raised.
   */
  public static void loadClasses(Map sources)
    throws GateException{
    //build the compiler
    Hashtable options = Hashtable.make();
    MemoryLog log = new MemoryLog(sources);

    options.put("-classpath", System.getProperty("java.class.path"));


    JavaCompiler compiler = new GJC(log,
                                    new Symtab(new ClassReader(options),
                                               new ClassWriter(options)),
                                    options,
                                    sources);

    //we have the compiler, let's put it to work
    ArrayList classNames = new ArrayList(sources.keySet());
    for(int i = 0; i < classNames.size(); i++){
      String className = (String)classNames.get(i);
      String filename = className.replace('.',
                                          Strings.getFileSep().charAt(0));
      classNames.set(i, filename + ".java" );
    }

    try{
      compiler.compile(List.make(classNames.toArray()));
    }catch(Throwable t){
      throw new GateException(t);
    }

    //check for errors and warnings
    if(log.errorsString != null && log.errorsString.length() > 0){
      throw new GateException(log.errorsString.toString());
    }
  }

}