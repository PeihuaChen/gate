package gate.util;


import com.sun.tools.javac.v8.util.*;
import com.sun.tools.javac.v8.comp.*;
import com.sun.tools.javac.v8.code.*;
import com.sun.tools.javac.v8.*;
//import com.sun.tools.javac.v8.code.Symbol.*;

import java.io.*;
import java.util.Map;
import java.util.ArrayList;

import gate.util.*;
import gate.*;


public class Javac{
  public static void loadClasses(final Map classes)
    throws GateException{
//Out.prln("Compiling " + classes.size() + " classes");
    //build the compiler
    Hashtable options = Hashtable.make();
    Log log = new Log(){
      //disable automatic error reporting to System/err
      public void print(String s) {
        String message = s;
        int colon = message.indexOf(':');
        String className = null;
        if(colon != -1) className = message.substring(0, colon);
        if(className == null || className.length() == 0){
          //we could not find a class name; it's probably some other problem
          logString += message;
        }else{
          className = className.substring(0, className.indexOf(".java"));
          className = className.replace('/', '.');
          className = className.replace('\\', '.');
          String sourceCode = (String)classes.get(className);
          logString +=
            "Could not compile class " + className + Strings.getNl() +
            "The problem was: " + message + Strings.getNl() +
            "The offending source code was:" + Strings.getNl() +
            (sourceCode == null || sourceCode.equals("") ?
             "<not available>" :
             Strings.addLineNumbers(sourceCode)) + Strings.getNl();
        }
      }//print
    };

    options.put("-classpath", System.getProperty("java.class.path"));


    JavaCompiler compiler = new JavaCompiler(
                                  log,
                                  new Symtab(new ClassReader(options),
                                             new ClassWriter(options)),
                                  options){
      public InputStream openSource(String fileName) {
//Out.prln("Read request for: " + fileName);
        String className = fileName.substring(0, fileName.lastIndexOf(".java"));
        className = className.replace(Strings.getFileSep().charAt(0),
                                            '.');
        String classSource = (String)classes.get(className);
//Out.prln("Source for: " + className + "\n" + classSource);
        return new ByteArrayInputStream(classSource.getBytes());
      }

      public void writeClass(com.sun.tools.javac.v8.code.Symbol.ClassSymbol c)
                  throws IOException {
        //the compiler will try to write the class file too;
        //we'll just load the class instead
        ByteArrayOutputStream os = new ByteArrayOutputStream(2000);
         new ClassWriter(Hashtable.make()).writeClassFile(os, c);
        os.flush();
        byte[] bytes = os.toByteArray();
        String className = c.className();
        if(c.isInner()){
          int loc = className.lastIndexOf('.');
          className = className.substring(0, loc) + "$" +
                      className.substring(loc + 1);
        }
//Out.pr(className + "[" + os.size() + " bytes]");
        Gate.getClassLoader().defineGateClass(className,
                                              bytes, 0, os.size());
      }
    };

    //we have the compiler, let's put it to work
    ArrayList classNames = new ArrayList(classes.keySet());
    for(int i = 0; i < classNames.size(); i++){
      String className = (String)classNames.get(i);
      String filename = className.replace('.',
                                          Strings.getFileSep().charAt(0));
      classNames.set(i, filename + ".java" );
    }
    logString = "";
    try{
      compiler.compile(List.make(classNames.toArray()));
    }catch(Throwable t){
      throw new GateException(t);
    }

    //check for errors and warnings
    if(logString == null || logString.length() == 0){
    }else{
      throw new GateException(logString);
    }
  }

  private static String logString;
}