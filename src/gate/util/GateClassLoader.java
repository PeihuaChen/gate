/*
 * GateClassLoader.java
 * Kalina Bontcheva, 1998
 * Revised by Hamish for 1.2 style and URL/Jar loading, June 2000
 * $Id$
 */

package gate.util;

import java.io.*;
import java.net.*;

/** We need a class loader for loading certain types of CREOLE
  * modules, and for creating JAPE RHS action classes.
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
  } //

  /** Delegate loading to the super class (loadClass has protected
    * access there).
    */
  public synchronized Class loadClass(String name, boolean resolve)
  throws ClassNotFoundException {
    return super.loadClass(name, resolve);
  } // loadClass(name, resolve)

  /** Forward a call to super.defineClass, which is protected and final
    * in super. PROBABLY don't need this method, but old JAPE code uses
    * it.
    */
  public Class defineGateClass(String name, byte[] bytes, int offset, int len)
  {
    return super.defineClass(name, bytes, offset, len);
  } // defineGateClass(name, bytes, offset, len);

  /** Forward a call to super.resolveClass, which is protected and final
    * in super. PROBABLY don't need this method, but old JAPE code uses
    * it.
    */
  public void resolveGateClass(Class c) { super.resolveClass(c); }

} // GateClassLoader

