/*
	RightHandSide.java - transducer class

	Hamish Cunningham, 24/07/98

	$Id$
*/


package gate.jape;

import java.util.Enumeration;
import java.util.Map;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.objectspace.jgl.*;

import gate.annotation.*;
import gate.util.*;
import gate.*;


/**
  * The RHS of a CPSL rule. The action part. Contains an inner class
  * created from the code in the grammar RHS.
  */
public class RightHandSide implements JapeConstants, java.io.Serializable
{
  /** The "action class" we create to implement the action. Has a static
    * method that performs the action of the RHS.
    */
  transient private Class theActionClass;

  /** An instance of theActionClass. */
  transient private Object theActionObject;

  /** The string we use to create the action class. */
  private StringBuffer actionClassString;

  /** The bytes of the compiled action class. */
  private byte[] actionClassBytes;

  /** The name of the action class. */
  private String actionClassName;

  /** Package name for action classes. It's called a "dir name" because
    * we used to dump the action classes to disk and compile them there.
    */
  static private String actionsDirName = "japeactionclasses";

  /** The qualified name of the action class. */
  private String actionClassQualifiedName;

  /** Name of the .java file for the action class. */
  private String actionClassJavaFileName;

  /** Name of the .class file for the action class. */
  private String actionClassClassFileName;

  /** Cardinality of the action class set. Used for ensuring class name
    * uniqueness.
    */
  private static int actionClassNumber = 0;

  /** Allow setting of the initial action class number. Used for ensuring
    * class name uniqueness when running more than one transducer. The
    * long-term solution is to have separate class loaders for each
    * transducer.
    */
  public static void setActionClassNumber(int n) { actionClassNumber = n; }

  /** The set of block names.
    * Used to ensure we only get their annotations once in the action class.
    */
  private HashSet blockNames;

  /** For debugging. */
  public String getActionClassString() { return actionClassString.toString(); }

  /** The LHS of our rule, where we get bindings from. */
  private LeftHandSide lhs;

  /** A list of the files and directories we create. */
  static private Array tempFiles = new Array();

  /** Local fashion for newlines. */
  private final String nl = Strings.getNl();

  /** Debug flag. */
  static final boolean debug = false;

  /** Construction from the transducer name, rule name and the LHS. */
  public RightHandSide(
    String transducerName, String ruleName, LeftHandSide lhs
  ) {
    // debug = true;
    this.lhs = lhs;
    actionClassName = new String(
      transducerName + ruleName + "ActionClass" + actionClassNumber++
    );
    blockNames = new HashSet();

    // initialise the class action string
    actionClassString = new StringBuffer(
      "// " + actionClassName + nl +
      "package " + actionsDirName + "; " + nl +
      "import java.io.*;" + nl +
      "import java.util.*;" + nl +
      "import gate.*;" + nl +
      "import gate.jape.*;" + nl +
      "import gate.annotation.*;" + nl +
      "import gate.util.*;" + nl + nl +
      "public class " + actionClassName + nl +
      "implements java.io.Serializable, RhsAction { " + nl +
      "  public void doit(Document doc, java.util.Map bindings) { " + nl
    );

    // initialise various names
    actionClassJavaFileName =
      actionsDirName +  File.separator +
      actionClassName.replace('.', File.separatorChar) + ".java";
    actionClassQualifiedName =
      actionsDirName.
      replace(File.separatorChar, '.').replace('/', '.').replace('\\', '.') +
      "." + actionClassName;
    actionClassClassFileName =
      actionClassQualifiedName.replace('.', File.separatorChar) + ".class";
  } // Construction from lhs

  /** Add an anonymous block to the action class */
  public void addBlock(String anonymousBlock) {
    actionClassString.append(anonymousBlock);
    actionClassString.append(nl);
  } // addBlock(anon)

  /** Add a named block to the action class */
  public void addBlock(String name, String namedBlock) {
    // is it really a named block?
    // (dealing with null name cuts code in the parser...)
    if(name == null) {
      addBlock(namedBlock);
      return;
    }

    if(blockNames.add(name) == null) // it wasn't already a member
      actionClassString.append(
        "    AnnotationSet " + name + "Annots = (AnnotationSet)bindings.get(\"" +
        name + "\"); " + nl
      );

    actionClassString.append(
      "    if(" + name + "Annots != null && " + name +
      "Annots.size() != 0) { " + nl + "      " + namedBlock +
      nl + "    }" + nl
    );
  } // addBlock(name, block)

  /** Create the action class and an instance of it. */
  public void createActionClass() throws JapeException {
    // terminate the class string
    actionClassString.append("  }" + nl + "}" + nl);

    // create class:
    // contruct a file name from the class name
    // write the action class string out into the file
    // call javac on the class
    // call the gate class loader to load the resultant class
    // read in the bytes if the compiled file for later serialisation
    // create an instance of the class
/*    writeActionClass();
    compileActionClass();
    loadActionClass();
    readActionClass();
    instantiateActionClass();
*/
    //System.out.println(actionClassString);
    try {
      actionClassBytes = new Jdk().compile(
        actionClassString.toString(),
        actionClassJavaFileName
      );
    } catch(GateException e) {
      throw new JapeException("Couldn't create action class: " + e);
    }
    defineActionClass();
    instantiateActionClass();

    //Debug.pr(this, "RightHandSide: action class loaded ok");
  } // createActionClass

  /** Write out the action class file. */
  public void writeActionClass() throws JapeException {

    File actionClassJavaFile = new File(actionClassJavaFileName);
    try {
      FileWriter writer = new FileWriter(actionClassJavaFile);
      writer.write(actionClassString.toString());
      writer.close();
    } catch(IOException e) {
      throw new JapeException(
        "problem writing to " + actionClassJavaFileName + ": " + e.getMessage()
      );
    }
  } // writeActionClass

  /** Compile the action class. First tries to use the sun.tools.javac
    * class directly via reflection. If that fails, tries to exec javac
    * as an external process.
    */
  public void compileActionClass() throws JapeException {
    if(debug) System.out.println("RightHandSide: trying to compile action");

    // see if we can find the sun compiler class
    Class sunCompilerClass = null;
    try {
      sunCompilerClass = Class.forName("sun.tools.javac.Main");
    } catch(ClassNotFoundException e) {
      sunCompilerClass = null;
    }

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
        toBeCompiled[0] = actionClassJavaFileName;
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
      if(debug) System.out.println("RightHandSide: action compiled ok");
      if(compiledOk.booleanValue())
        return;
    }

    // no sun compiler: try execing javac as an external process
    Runtime runtime = Runtime.getRuntime();
    try {
      String actionCompileCommand = new String(
        "javac -classpath " +
        System.getProperty("java.class.path") +
        " " + actionClassJavaFileName
      );
      if(debug) System.out.println("doing " + actionCompileCommand);

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
      if(debug) System.out.println("process complete");

    } catch(Exception e) {
      throw new JapeException(
        "couldn't compile " + actionClassJavaFileName + ": " + e.toString()
      );
    }
  } // compileActionClass

  /** Read action class bytes, for storing during serialisation. */
  public void readActionClass() throws JapeException {
    try {
      File f = new File(actionClassClassFileName);
      FileInputStream fis = new FileInputStream(actionClassClassFileName);
      actionClassBytes = new byte[(int) f.length()];
      fis.read(actionClassBytes, 0, (int) f.length());
      fis.close();
    } catch(IOException e) {
      throw(new JapeException("couldn't read action class bytes: " + e));
    }
  } // readActionClass

  /** Load the action class. */
  public void loadActionClass() throws JapeException {
    //Debug.pr(this, "RightHandSide: trying to load the action class");
    try {
      theActionClass =
        Gate.getClassLoader().loadClass(actionClassClassFileName, true);
    } catch(Exception e) {
      e.printStackTrace();
      throw new JapeException(
        "couldn't load " + actionClassClassFileName + ": " + e.getMessage()
      );
    }
  } // loadActionClass

  /** Define the action class (after deserialisation). */
  public void defineActionClass() throws JapeException {
    //Debug.pr(this, "RightHandSide: trying to define the action class");
    try {
      theActionClass = Gate.getClassLoader().defineGateClass(
        actionClassQualifiedName, actionClassBytes, 0, actionClassBytes.length
      );
    } catch(ClassFormatError e) {
      e.printStackTrace();
      throw new JapeException(
        "couldn't define " + actionClassName + ": " + e
      );

    }
    Gate.getClassLoader().resolveGateClass(theActionClass);
  } // defineActionClass

  /** Create an instance of the action class. */
  public void instantiateActionClass() throws JapeException {

    try {
      theActionObject = theActionClass.newInstance();
    } catch(Exception e) {
      throw new JapeException(
        "couldn't create instance of action class " + actionClassName + ": "
        + e.getMessage()
      );
    }
  } // instantiateActionClass

  /** Remove class files created for actions. */
  public static void cleanUp() {
    if(tempFiles.size() == 0) return;

    // traverse the list in reverse order, coz any directories we
    // created were done first
    for(ArrayIterator i = tempFiles.end(); ! i.atBegin(); ) {
      i.retreat();
      File tempFile = (File) i.get();
      tempFile.delete();
    } // for each tempFile

    tempFiles.clear();
  } // cleanUp


  /** Makes changes to the document, using LHS bindings. */
  public void transduce(Document doc, java.util.Map bindings) throws JapeException {
    if(theActionObject == null) {
      defineActionClass();
      instantiateActionClass();
    }

    ((RhsAction) theActionObject).doit(doc, bindings);
  } // transduce

  /** Create a string representation of the object. */
  public String toString() { return toString(""); }

  /** Create a string representation of the object. */
  public String toString(String pad) {
    String nl = Strings.getNl();
    StringBuffer buf = new StringBuffer(
      pad + "RHS: actionClassName(" + actionClassName + "); "
    );
    //buf.append("actionClassString(" + nl + actionClassString + nl);
    buf.append(
      "actionClassClassFileName(" + nl + actionClassClassFileName + nl
    );
    buf.append("actionClassJavaFileName(" + nl + actionClassJavaFileName + nl);
    buf.append(
      "actionClassQualifiedName(" + nl + actionClassQualifiedName + nl
    );

    buf.append("blockNames(" + blockNames.toString() + "); ");

    buf.append(nl + pad + ") RHS." + nl);

    return buf.toString();
  } // toString

  /** Create a string representation of the object. */
  public String shortDesc() {
    String res = "" + actionClassName;
    return res;
  } // toString

} // class RightHandSide


// $Log$
// Revision 1.9  2000/06/12 13:33:27  hamish
// removed japeactionclasse create code (static init block
//
// Revision 1.8  2000/05/16 10:38:25  hamish
// removed printout
//
// Revision 1.7  2000/05/16 10:30:33  hamish
// uses new gate.util.Jdk compiler
//
// Revision 1.6  2000/05/05 12:51:12  valyt
// Got rid of deprecation warnings
//
// Revision 1.5  2000/05/05 10:14:09  hamish
// added more to toString
//
// Revision 1.4  2000/05/02 16:54:47  hamish
// porting to new annotation API
//
// Revision 1.3  2000/04/20 13:26:42  valyt
// Added the graph_drawing library.
// Creating of the NFSM and DFSM now works.
//
// Revision 1.2  2000/02/24 17:28:48  hamish
// more porting to new API
//
// Revision 1.1  2000/02/23 13:46:11  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:02  hamish
// added gate2
//
// Revision 1.21  1998/11/13 17:25:10  hamish
// stop it using sun.tools... when in 1.2
//
// Revision 1.20  1998/10/30 15:31:07  kalina
// Made small changes to make compile under 1.2 and 1.1.x
//
// Revision 1.19  1998/10/29 12:17:12  hamish
// use reflection when using sun compiler classes, so can compile without them
//
// Revision 1.18  1998/10/01 16:06:36  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.17  1998/09/18 16:54:17  hamish
// save/restore works except for attribute seq
//
// Revision 1.16  1998/09/18 13:35:44  hamish
// refactored to split up createActionClass
//
// Revision 1.15  1998/09/18 12:15:40  hamish
// bugs fixed: anon block null ptr; no error for some non-existant labelled blocks
//
// Revision 1.14  1998/08/19 20:21:41  hamish
// new RHS assignment expression stuff added
//
// Revision 1.13  1998/08/17 10:43:29  hamish
// action classes have unique names so can be reloaded
//
// Revision 1.12  1998/08/12 15:39:42  hamish
// added padding toString methods
//
// Revision 1.11  1998/08/10 14:16:38  hamish
// fixed consumeblock bug and added batch.java
//
// Revision 1.10  1998/08/07 12:01:46  hamish
// parser works; adding link to backend
//
// Revision 1.9  1998/08/05 21:58:07  hamish
// backend works on simple test
//
// Revision 1.8  1998/08/04 12:42:56  hamish
// fixed annots null check bug
//
// Revision 1.7  1998/08/03 21:44:57  hamish
// moved parser classes to gate.jape.parser
//
// Revision 1.6  1998/08/03 19:51:26  hamish
// rollback added
//
// Revision 1.5  1998/07/31 16:50:18  hamish
// RHS compilation works; it runs - and falls over...
//
// Revision 1.4  1998/07/31 13:12:25  hamish
// done RHS stuff, not tested
//
// Revision 1.3  1998/07/30 11:05:24  hamish
// more jape
//
// Revision 1.2  1998/07/29 11:07:10  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives
