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
    try{
      //build the compiler
      Hashtable options = Hashtable.make();
      options.put("-classpath", System.getProperty("java.class.path"));
      JavaCompiler compiler = new JavaCompiler(
                                    new Log(),
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
  //Out.pr(c.className() + "[" + os.size() + " bytes]");
          Gate.getClassLoader().defineGateClass(c.className(),
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

      compiler.compile(List.make(classNames.toArray()));
    }catch(Throwable t){
      throw new GateException(t);
    }
  }
}