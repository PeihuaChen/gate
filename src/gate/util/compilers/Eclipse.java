/*
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  This class is based on code from the Jasper 2 JSP compiler from Jakarta
 *  Tomcat 5.5, produced by the Apache project.
 *
 *  Ian Roberts, 13/Dec/2004
 *
 *  $Id$
 */
package gate.util.compilers;

import java.io.*;
import java.util.*;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import gate.util.*;
import gate.Gate;
import gate.GateConstants;
import gate.creole.ExecutionException;

/**
 * This class copiles a set of java sources using the JDT compiler from the
 * Eclipse project.  Unlike the Sun compiler, this compiler can load
 * dependencies directly from the GATE class loader, which (a) makes it faster,
 * (b) means the compiler will work when GATE is loaded from a classloader
 * other than the system classpath (for example within a Tomcat web
 * application), and (c) allows it to compile code that depends on classes
 * defined in CREOLE plugins, as well as in the GATE core.  This is the default
 * compiler for GATE version 3.0.
 *
 * @author Ian Roberts
 */
public class Eclipse extends gate.util.Javac {

  /**
   * Compiles a set of java sources using the Eclipse Java compiler and loads
   * the compiled classes in the gate class loader.
   * 
   * @param sources a map from fully qualified classname to java source
   * @throws GateException in case of a compilation error or warning.
   * In the case of warnings the compiled classes are loaded before the error is
   * raised.
   */
  public void compile(final Map sources) throws GateException {
    if(classLoader == null) classLoader = Gate.getClassLoader();

    // store any problems that occur douring compilation
    final Map problems = new HashMap();

    // A class representing a file to be compiled.  An instance of this class
    // is returned by the name environment when one of the classes given in the
    // sources map is requested.
    class CompilationUnit implements ICompilationUnit {
      String className;

      CompilationUnit(String className) {
        this.className = className;
      }

      public char[] getFileName() {
        return className.toCharArray();
      }
      
      public char[] getContents() {
        return ((String)sources.get(className)).toCharArray();
      }
      
      /**
       * Returns the unqualified name of the class defined by this
       * compilation unit.
       */
      public char[] getMainTypeName() {
        int dot = className.lastIndexOf('.');
        if (dot > 0) {
          return className.substring(dot + 1).toCharArray();
        }
        return className.toCharArray();
      }
      
      /**
       * Returns the package name for the class defined by this compilation
       * unit.  For example, if this unit defines java.lang.String,
       * ["java".toCharArray(), "lang".toCharArray()] would be returned.
       */
      public char[][] getPackageName() {
        StringTokenizer izer = 
          new StringTokenizer(className, ".");
        char[][] result = new char[izer.countTokens()-1][];
        for (int i = 0; i < result.length; i++) {
          String tok = izer.nextToken();
          result[i] = tok.toCharArray();
        }
        return result;
      }
    }
    
    // Name enviroment - maps class names to eclipse objects.  If the class
    // name is one of those given in the sources map, the appropriate
    // CompilationUnit is created.  Otherwise, we try to load the requested
    // .class file from the GATE classloader and return a ClassFileReader for
    // that class.
    final INameEnvironment env = new INameEnvironment() {

      /**
       * Tries to find the class or source file defined by the given type
       * name.  We construct a string from the compound name (e.g. ["java",
       * "lang", "String"] becomes "java.lang.String") and search using that.
       */
      public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
        String result = "";
        String sep = "";
        for (int i = 0; i < compoundTypeName.length; i++) {
          result += sep;
          result += new String(compoundTypeName[i]);
          sep = ".";
        }
        return findType(result);
      }

      /**
       * Tries to find the class or source file defined by the given type
       * name.  We construct a string from the compound name (e.g. "String",
       * ["java", "lang"] becomes "java.lang.String") and search using that.
       */
      public NameEnvironmentAnswer findType(char[] typeName, 
                                            char[][] packageName) {
        String result = "";
        String sep = "";
        for (int i = 0; i < packageName.length; i++) {
          result += sep;
          result += new String(packageName[i]);
          sep = ".";
        }
        result += sep;
        result += new String(typeName);
        return findType(result);
      }
      
      /**
       * Find the type referenced by the given name.
       */
      private NameEnvironmentAnswer findType(String className) {
        try {
          if (sources.containsKey(className)) {
            // if it's one of the sources we were given to compile,
            // return that as a CompilationUnit.
            ICompilationUnit compilationUnit = new CompilationUnit(className);
            return new NameEnvironmentAnswer(compilationUnit);
          }

          // otherwise, try and load the class from the GATE classloader.
          String resourceName = className.replace('.', '/') + ".class";
          InputStream is = classLoader.getResourceAsStream(resourceName);
          if (is != null) {
            byte[] classBytes;
            byte[] buf = new byte[8192];
            ByteArrayOutputStream baos = 
              new ByteArrayOutputStream(buf.length);
            int count;
            while ((count = is.read(buf, 0, buf.length)) > 0) {
              baos.write(buf, 0, count);
            }
            baos.flush();
            classBytes = baos.toByteArray();
            char[] fileName = className.toCharArray();
            ClassFileReader classFileReader = 
              new ClassFileReader(classBytes, fileName, 
                                  true);
            return new NameEnvironmentAnswer(classFileReader);
          }
        }
        catch (IOException exc) {
          System.err.println("Compilation error");
          exc.printStackTrace();
        }
        catch (org.eclipse.jdt.internal.compiler
                    .classfmt.ClassFormatException exc) {
          System.err.println("Compilation error");
          exc.printStackTrace();
        }
        // if no class found by that name, either as a source of in the
        // GATE classloader, return null.  This will cause a compiler
        // error.
        return null;
      }

      /**
       * Is the requested name a package?  We assume yes if it's not a class.
       */
      private boolean isPackage(String result) {
        if (sources.containsKey(result)) {
          return false;
        }
        String resourceName = result.replace('.', '/') + ".class";
        InputStream is = classLoader.getResourceAsStream(resourceName);
        return is == null;
      }

      /**
       * Checks whether the given name refers to a package rather than a
       * class.
       */
      public boolean isPackage(char[][] parentPackageName, 
                               char[] packageName) {
        String result = "";
        String sep = "";
        if (parentPackageName != null) {
          for (int i = 0; i < parentPackageName.length; i++) {
            result += sep;
            String str = new String(parentPackageName[i]);
            result += str;
            sep = ".";
          }
        }
        String str = new String(packageName);
        if (Character.isUpperCase(str.charAt(0))) {
          if (!isPackage(result)) {
            return false;
          }
        }
        result += sep;
        result += str;
        return isPackage(result);
      }

      public void cleanup() {
      }

    };

    // Error handling policy - try the best we can
    final IErrorHandlingPolicy policy = 
        DefaultErrorHandlingPolicies.proceedWithAllProblems();

    final Map settings = new HashMap();
    settings.put(CompilerOptions.OPTION_LineNumberAttribute,
                 CompilerOptions.GENERATE);
    settings.put(CompilerOptions.OPTION_SourceFileAttribute,
                 CompilerOptions.GENERATE);
    settings.put(CompilerOptions.OPTION_ReportDeprecation,
                 CompilerOptions.IGNORE);
    // ignore unused imports - otherwise every JAPE action class would generate
    // warnings...
    settings.put(CompilerOptions.OPTION_ReportUnusedImport,
                 CompilerOptions.IGNORE);

    final IProblemFactory problemFactory = 
      new DefaultProblemFactory(Locale.getDefault());

    // CompilerRequestor defines what to do with the result of a compilation.
    final ICompilerRequestor requestor = new ICompilerRequestor() {
      public void acceptResult(CompilationResult result) {
        boolean errors = false;
        if (result.hasProblems()) {
          IProblem[] problems = result.getProblems();
          for (int i = 0; i < problems.length; i++) {
            // store all the errors and warnings from this result
            IProblem problem = problems[i];
            if (problem.isError()) {
              errors = true;
            }
            addProblem(problem);
          }
        }
        // if there were no errors (there may have been warnings), load the
        // compiled classes into the GATE classloader
        if (!errors) {
          ClassFile[] classFiles = result.getClassFiles();
          for (int i = 0; i < classFiles.length; i++) {
            ClassFile classFile = classFiles[i];
            char[][] compoundName = classFile.getCompoundName();
            String className = "";
            String sep = "";
            for (int j = 0; j < compoundName.length; j++) {
              className += sep;
              className += new String(compoundName[j]);
              sep = ".";
            }
            byte[] bytes = classFile.getBytes();
            classLoader.defineGateClass(className, bytes,
                                        0, bytes.length);
          }
        }
      }

      private void addProblem(IProblem problem) {
        String name = new String(problem.getOriginatingFileName());
        List problemsForName = (List)problems.get(name);
        if(problemsForName == null) {
          problemsForName = new ArrayList();
          problems.put(name, problemsForName);
        }
        problemsForName.add(problem);
      }
    };

    // Define the list of things to compile
    ICompilationUnit[] compilationUnits = new ICompilationUnit[sources.size()];
    int i = 0;
    Iterator sourcesIt = sources.keySet().iterator();
    while(sourcesIt.hasNext()) {
      compilationUnits[i++] =
        new CompilationUnit((String)sourcesIt.next());
    }

    // create the compiler
    Compiler compiler = new Compiler(env,
                                     policy,
                                     settings,
                                     requestor,
                                     problemFactory);

    // and compile the classes
    compiler.compile(compilationUnits);

    if(!problems.isEmpty()) {
      Iterator problemsIt = problems.entrySet().iterator();
      while(problemsIt.hasNext()) {
        Map.Entry prob = (Map.Entry)problemsIt.next();
        String name = (String)prob.getKey();
        List probsForName = (List)prob.getValue();
        Iterator probsForNameIt = probsForName.iterator();
        while(probsForNameIt.hasNext()) {
          IProblem problem = (IProblem)probsForNameIt.next();
          if(problem.isError()) {
            Err.pr("Error: ");
          }
          else if(problem.isWarning()) {
            Err.pr("Warning: ");
          }
          Err.prln(problem.getMessage()
                + " at line " 
                + problem.getSourceLineNumber() + " in " + name);
        }
        // print the source for this class, to help the user debug.
        Err.prln("\nThe offending input was:\n");
        Err.prln(Strings.addLineNumbers((String)sources.get(name)));
      }
      throw new GateException(
        "There were problems; see error log for details!");
    }
  }

  private static GateClassLoader classLoader;
}
