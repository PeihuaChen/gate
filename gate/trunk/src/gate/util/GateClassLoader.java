/*
 * GateClassLoader.java
 * Kalina Bontcheva, 1998
 * Revised by Hamish for 1.2 style and URL/Jar loading, June 2000
 * $Id$
 */

package gate.util;

import java.io.*;
import java.net.*;

/** GATE's class loader, which allows loading of classes over the net.
  * A list of URLs is searched, which should point at .jar files or
  * to directories containing class file hierarchies.
  * The class loader is unusual in supporting reloading of classes, which
  * is useful for CREOLE developers who want to recompile modules without
  * relaunching GATE.
  * The loader is also used for creating JAPE RHS action classes.
  */
public class GateClassLoader extends URLClassLoader {

  /** Default construction - use an empty URL list. */
  public GateClassLoader() { super(new URL[0]); }

  /** Chaining constructor. */
  public GateClassLoader(ClassLoader parent) { super(new URL[0], parent); }

  /** Default construction with URLs list. */
  public GateClassLoader(URL[] urls) { super(urls); }

  /** Chaining constructor with URLs list. */
  public GateClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  } // Chaining constructor with URLs list.

  /** Appends the specified URL to the list of URLs to search for classes
    * and resources.
    */
  public void addURL(URL url) { super.addURL(url); }

  /** Delegate loading to the super class (loadClass has protected
    * access there).
    */
  public synchronized Class loadClass(String name, boolean resolve)
  throws ClassNotFoundException {
    return super.loadClass(name, resolve);
  } // loadClass(name, resolve)

  /** Forward a call to super.defineClass, which is protected and final
    * in super. This is used by JAPE and the Jdk compiler class.
    */
  public Class defineGateClass(String name, byte[] bytes, int offset, int len)
  {
    return super.defineClass(name, bytes, offset, len);
  } // defineGateClass(name, bytes, offset, len);

  /** Forward a call to super.resolveClass, which is protected and final
    * in super. This is used by JAPE and the Jdk compiler class
    */
  public void resolveGateClass(Class c) { super.resolveClass(c); }

  /** Reload a class. This works on the assumption that all classes that
    * we are asked to reload will have been loaded by a GateClassLoader
    * and not the system class loader. If this is not the case, this
    * method will simply return the previously loaded class (because of
    * the delegation chaining model of class loaders in JDK1.2 and above).
    */
  public Class reloadClass(String name) throws ClassNotFoundException {
    Class theClass = null;

    // if the class isn't already present in this class loader
    // we can just load it
    theClass = findLoadedClass(name);
    if(theClass == null)
      return loadClass(name);

    // if there's a cached loader, try that
    if(cachedReloader != null) {

      // if the cached loader hasn't already loaded this file, then ask it to
      theClass = cachedReloader.findLoadedClass(name);
      if(theClass == null)
        return cachedReloader.loadClass(name);
    }

    // create a new reloader and cache it
    cachedReloader = new GateClassLoader(getURLs());

    // ask the new reloader to load the class
    return cachedReloader.loadClass(name, true);
    
  } // reloadClass(String name)

  /** A cache used by the reloadClass method to store the last new
    * loader that we created.
    */
  private static GateClassLoader cachedReloader = null;

} // GateClassLoader

