/*
	Jdk.java

	Hamish Cunningham, 04/05/00

	$Id$
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

  /** Compile a class from its source code string.
    * @param className should have the package path to the source, e.g.
    * com/thing/MyClass.java.
    */
  public byte[] compile(String javaCode, String className)
  throws GateException {
    sun.toolsx.javac.Main compiler = new sun.toolsx.javac.Main(
      System.out, "gate.util.Jdk"
    );
    String argv[] = new String[3];
    argv[0] = "-nodisk";
    argv[1] = className;
    argv[2] = javaCode;
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
        "couldn't create instance of class " + theClass + ": " + e
      );
    }

    return theObject;
  } // instantiateClass

} // Jdk



/** Sun compiler wrapper */
class SunCompiler extends /*BatchEnvironment*/ Main
implements ErrorConsumer {

  SunCompiler() {
    //for Main: super(System.out, "gate.util.Jdk.SunCompiler");
    super(System.out, "gate.util.Jdk.SunCompiler");
    //for BEnv: super(new ClassPath(System.getProperty("java.class.path")));
  }

  /** Compile a string and return the binary image of the resultant
    * class.
    */
  byte[] compile(String javaCode, String className) throws GateException {
    boolean status = false;
    ByteArrayOutputStream buf = new ByteArrayOutputStream(4069);

    // construct the compilation environment
    BatchEnvironment env = new BatchEnvironment(
      System.out,
      new ClassPath(System.getProperty("java.class.path")),
      this
    );

    // parse the code
    try {
      env.parseFile(new StringClassFile(javaCode, className));
    } catch(IOException e) {
      throw new GateException("Couldn't parse " + className + e);
    }

    // check the class
    Enumeration classes = env.getClasses();
    ClassDeclaration classDecl = (ClassDeclaration)classes.nextElement();
    SourceClass src = null;
    try {
      src = (SourceClass) classDecl.getClassDefinition(env);
      src.check(env);
    } catch(ClassNotFound e) {
      throw new GateException("Couldn't find class " + className + e);
    }
//pushError("thingFile", 315, "hello", "refText", "refTextPtr");
    if(src.getError())
      throw new GateException("Parse errors on " + className);

    // compile the class
    classes = env.getClasses();
    classDecl = (ClassDeclaration)classes.nextElement();
    src = null;
    try {
      src = (SourceClass) classDecl.getClassDefinition(env);
      src.compile(buf);
    } catch(ClassNotFound e) {
      throw new GateException("Couldn't find class " + className + e);
    } catch(InterruptedException e) {
      throw new GateException("Interrupted on class " + className + e);
    } catch(IOException e) {
      throw new GateException("IOException on class " + className + e);
    }
    if(src.getNestError())
      throw new GateException("Compile errors on " + className);


    // return the binary image of the class
    return buf.toByteArray();
} // compile(String javaCode)

  /** Consume errors from the compiler */
  public void pushError(
    String errorFileName, int line, String message,
    String referenceText, String referenceTextPointer
  ) {
    String nl = Strings.getNl();
    System.out.println(
      "Error compiling: " + errorFileName + " at line " + line + ":" + nl +
      message + nl + "referenceText: " + referenceText + nl +
      "referenceTextPointer: " + referenceTextPointer
    );
  } // pushError

} // SunCompiler


/** A wrapper for the Sun ClassFile class that adds the ability to
  * construct from a String containing the Java code for the class
  */
class StringClassFile extends sun.toolsx.java.ClassFile {
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





