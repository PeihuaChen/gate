/*
 * GateClassLoader.java
 * Kalina Bontcheva, 1998
 * $Id$
 */

package gate.util;

import java.io.*;

/** We need a class loader for loading certain types of CREOLE
  * modules, and for creating JAPE RHS action classes.
  */
public class GateClassLoader extends ClassLoader {

  /** Load a class given the path name of the .class file. */
	public synchronized Class  loadClass(String name, boolean resolve)
  	throws ClassNotFoundException
  {
    /*Debug.pr(
      this, "GCL.loadClass, name(" + name + "), resolve(" + resolve + ")"
    );*/

    // bare class name
   	String className = (new File(name)).getName();

    // qualified classname
    String fullClassName = name.replace(File.separatorChar, '.');

    // strip the .class because the class checking functions
    // moan otherwise
    if (className.endsWith(".class")) {
     	className =
        className.substring(0, className.length() - ".class".length());
    	fullClassName =
        fullClassName.substring(0, fullClassName.length() - ".class".length());
    }

    try {
    	Class newClass = findLoadedClass(className);
      if (newClass == null) { //not yet defined
    		try {                	// check if system class
        	newClass = findSystemClass(className);
          if (newClass != null)
          	return newClass;
        } catch (ClassNotFoundException e) {
        	; //keep on looking
        }

	      //class not found -- need to load it
  	    byte[] buf = bytesForClass(name);
        // System.out.println("Loaded " + className + " class from: " + name);

    	  newClass = defineClass(fullClassName, buf, 0, buf.length);
      }

      if (resolve)
      	resolveClass(newClass);

      return newClass;
    } catch (IOException e) {
    	throw new ClassNotFoundException(e.toString());
    }
  } /** loadClass filename, resolve

  /** Get the bytes from a .class file. */
  protected byte[] bytesForClass(String name)
  	throws IOException, ClassNotFoundException
  {
    /*Debug.pr(this, "GCL.bytesForClass, name(" + name + ")");*/
  	FileInputStream in;
  	try {
    	String fileName = (new File(name)).getAbsolutePath();

      if (fileName.endsWith(".class") )
      	in = new FileInputStream(name);
	    else
      	in = new FileInputStream(name + ".class");

    } catch (FileNotFoundException e) {
    	throw new ClassNotFoundException(name);
    }

    int length = in.available(); //get byte count

    if (length == 0)
    	throw new ClassNotFoundException(name);

    byte[] buf = new byte[length];
    in.read(buf);

    return buf;

  } // bytesForClass

  /** Call the real class loader's define class method. */
  public Class defineGateClass(
    String name, byte[] bytes, int offset, int length
  ) {
    return super.defineClass(name, bytes, offset, length);
  } // defineGateClass


  /** Call the real class loader's resolve method. */
  public void resolveGateClass(Class c) {
    super.resolveClass(c);
  } // resolve

} // GateClassLoader

