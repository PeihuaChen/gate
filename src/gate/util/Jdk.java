/*
	Jdk.java

	Hamish Cunningham, 04/05/00

	$Id$
*/

package gate.util;

import java.io.*;
import java.lang.reflect.*;

import sun.tools.java.*;
import sun.tools.javac.*;


/** Jdk encapsulates some utilities for poking around in your Java
  * environment. 
  */
public class Jdk {

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
    System.out.println("Testing gate.util.Jdk");
    System.out.println("getToolsHome(): " + getToolsHome());
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

  /** Compile a class.
    * @param javaSourceFile should have the package path to the source, e.g.
    * com/thing/MyClass.java.
    */
  public byte[] compile(File javaSourceFile) throws GateException {

    // see if we can find the sun compiler class
    Class sunCompilerClass = null;
    try {
      sunCompilerClass = Class.forName("sun.tools.javac.Main");
    } catch(ClassNotFoundException e) {
      sunCompilerClass = null;
    }
/*
    // if it's 1.2, we can't support the compiler class at present
    String jversion = System.getProperty("java.version");
    if(jversion == null || jversion.startsWith("1.2"))
      sunCompilerClass = null;


    // if we have the sun compiler class, try to use it directly
    if(sunCompilerClass != null) {
      // none-reflection version:
      // sun.tools.javac.Main compiler =
      //   new sun.tools.javac.Main(System.err, "RhsCompiler");
      // String toBeCompiled[] = new String[1];
      // toBeCompiled[0] = actionClassJavaFileName;
      // boolean compiledOk = compiler.compile(toBeCompiled);

      Boolean compiledOk = new Boolean(false);
      try {
        // get the compiler constructor
        Class[] consTypes = new Class[2];
        consTypes[0] = OutputStream.class;
        consTypes[1] = String.class;
        Constructor compilerCons = sunCompilerClass.getConstructor(consTypes);

        // get an instance of the compiler
        Object[] consArgs = new Object[2];
        consArgs[0] = System.err;
        consArgs[1] = "RhsCompiler";
        Object sunCompiler = compilerCons.newInstance(consArgs);

        // get the compile method
        Class[] compilerTypes = new Class[1];
        compilerTypes[0] = String[].class;
        Method compileMethod = sunCompilerClass.getDeclaredMethod(
          "compile", compilerTypes
        );

        // call the compiler
        String toBeCompiled[] = new String[1];
        toBeCompiled[0] = javaSourceFile.getPath();
        Object[] compilerArgs = new Object[1];
        compilerArgs[0] = toBeCompiled;
        compiledOk = (Boolean) compileMethod.invoke(sunCompiler, compilerArgs);

      // any exceptions mean the reflection stuff failed, as the compile
      // method doesn't throw any. so (apart from RuntimeExceptions) we just
      // print a warning and go on to try execing javac
      } catch(RuntimeException e) { // rethrow runtime exceptions as they are
        throw e;
      } catch(Exception e) { // print out other sorts, and try javac exec
        compiledOk = new Boolean(false);
        System.err.println(
          "Warning: RHS compiler error: " + e.toString()
        );
      }

      if(compiledOk.booleanValue())
        return null;
    }

    // no sun compiler: try execing javac as an external process
    Runtime runtime = Runtime.getRuntime();
    try {
      String actionCompileCommand = new String(
        "javac -classpath " +
        System.getProperty("java.class.path") +
        " " + javaSourceFile.getPath()
      );

      Process actionCompile = runtime.exec(actionCompileCommand);
      //InputStream stdout = actionCompile.getInputStream();
      //InputStream stderr = actionCompile.getErrorStream();
      actionCompile.waitFor();

      //System.out.flush();
      //while(stdout.available() > 0)
      //  System.out.print((char) stdout.read());
      //while(stderr.available() > 0)
      //  System.out.print((char) stderr.read());
      //System.out.flush();

    } catch(Exception e) {
      throw new GateException(
        "couldn't compile " + javaSourceFile + ": " + e.toString()
      );
    }
*/
    return null;
  } // compile(File)

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
      throw(new GateException("couldn't read action class bytes: " + e));
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
        "couldn't create instance of class " + theClass + ": "
        + e.getMessage()
      );
    }

    return theObject;
  } // instantiateClass

} // Jdk



/** Sun compiler wrapper */
class SunCompiler {

  SunCompiler() {
  }

  /** Compile a string and return the binary image of the resultant
    * class.
    */
  byte[] compile(String javaCode, String className) throws Exception /*****/ {
    boolean status = false;
    byte[] binary = null;
    ByteArrayOutputStream buf = new ByteArrayOutputStream(4069);

    // construct the compilation environment
    BatchEnvironment env = new BatchEnvironment(
      System.out,
      new ClassPath(System.getProperty("java.class.path"))
    );

    // parse the code
    env.parseFile(new StringClassFile(javaCode, className));

    // 

    // return the binary image of the class
    return binary;
  } // compile(String javaCode)

} // SunCompiler


/** A wrapper for the Sun ClassFile class that adds the ability to
  * construct from a String containing the Java code for the class
  */
class StringClassFile extends sun.tools.java.ClassFile {
  /** The source code. */
  private String javaCode = null;

  /** Construction from string. */
  StringClassFile(String javaCode, String className) {
    super(new File(className)); // the File will be no use, but the
                                // super class expects it to be non-null
    this.javaCode = javaCode;
  }

  /** We overide this method to return a stream based on the
    * source string.
    */
  public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(javaCode.getBytes());
  } // getInputStream

} // StringClassFile

