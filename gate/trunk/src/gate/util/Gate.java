/*
 *  Gate.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Hamish Cunningham, 31/07/98
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.creole.*;

/** The class is responsible for initialising the GATE libraries, and
  * providing access to singleton utility objects, such as the GATE class
  * loader, CREOLE register and so on.
  */
public class Gate
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The list of builtin URLs to search for CREOLE resources. */
  private static String builtinCreoleDirectoryUrls[] = {
    // "http://derwent.dcs.shef.ac.uk/gate.ac.uk/creole/"

    // this has been moved to initCreoleRegister and made relative to
    // the base URL returned by getUrl()
    // "http://gate.ac.uk/creole/"
  };

  /** Initialisation - must be called by all clients before using
    * any other parts of the library. Also initialises the CREOLE
    * register.
    * @see #initCreoleRegister
    */
  public static void init() throws GateException {
    if(classLoader == null)
      classLoader = new GateClassLoader();

    if(creoleRegister == null)
      creoleRegister = new CreoleRegisterImpl();

    initCreoleRegister();
  } // init()

  /** Get a URL that points to either an HTTP server or a file system
    * that contains GATE files (such as test cases). The following locations
    * are tried in sequence:
    * <UL>
    * <LI>
    * <TT>http://derwent.dcs.shef.ac.uk/gate.ac.uk/</TT>, a Sheffield-internal
    * development server (the gate.ac.uk affix is a copy of the file system
    * present on GATE's main public server - see next item);
    * <LI>
    * <TT>http://gate.ac.uk/</TT>, GATE's main public server;
    * <LI>
    * <TT>http://localhost/gate.ac.uk/</TT>, a Web server running on the
    * local machine;
    * <LI>
    * the local file system where the binaries for the
    * current invocation of GATE are stored.
    * </UL>
    * In each case we assume that a Web server will be running on port 80,
    * and that if we can open a socket to that port then the server is
    * running. (This is a bit of a strong assumption, but this URL is used
    * largely by the test suite, so we're not betting anything too critical
    * on it.)
    * <P>
    * Note that the value returned will only be calculated when the existing
    * value recorded by this class is null (which will be the case when
    * neither setUrlBase nor getUrlBase have been called, or if
    * setUrlBase(null) has been called).
    */
  public static URL getUrl() throws GateException {
    if(urlBase != null) return urlBase;

    try {
       // if we're assuming a net connection, try network servers
      if(isNetConnected()) {
        if(
          tryNetServer("derwent.dcs.shef.ac.uk", 80, "/gate.ac.uk/") ||
          tryNetServer("gate.ac.uk", 80, "/")
        ) {
            if(DEBUG) Out.prln("getUrlBase() returned " + urlBase);
            return urlBase;
        }
      } // if isNetConnected() ...

      // no network servers; try for a local host web server.
      // we use InetAddress to get host name instead of using "localhost" coz
      // badly configured Windoze IP sometimes doesn't resolve the latter
      if(
        isLocalWebServer() &&
        tryNetServer(
          InetAddress.getLocalHost().getHostName(), 80, "/gate.ac.uk/"
        )
      ) {
        if(DEBUG) Out.prln("getUrlBase() returned " + urlBase);
        return urlBase;
      }

      // try the local file system
      tryFileSystem();

    } catch(MalformedURLException e) {
      throw new GateException("Bad URL, getUrlBase(): " + urlBase + ": " + e);
    } catch(UnknownHostException e) {
      throw new GateException("No host, getUrlBase(): " + urlBase + ": " + e);
    }

    // return value will be based on the file system, or null
    if(DEBUG) Out.prln("getUrlBase() returned " + urlBase);
    return urlBase;
  } // getUrl()

  /** Get a URL that points to either an HTTP server or a file system
    * that contains GATE files (such as test cases).
    * Calls <TT>getUrl()</TT> then adds the <TT>path</TT> parameter to
    * the result.
    * @param path a path to add to the base URL.
    * @see #getUrl()
    */
  public static URL getUrl(String path) throws GateException {
    getUrl();
    if(urlBase == null)
      return null;

    URL newUrl = null;
    try {
      newUrl = new URL(urlBase, path);
    } catch(MalformedURLException e) {
      throw new GateException("Bad URL, getUrl( " + path + "): " + e);
    }

    if(DEBUG) Out.prln("getUrl(" + path + ") returned " + newUrl);
    return newUrl;
  } // getUrl(path)

  /** Flag controlling whether we should try to access the net, e.g. when
    * setting up a base URL.
    */
  private static boolean netConnected = true;

  /** Should we assume we're connected to the net? */
  public static boolean isNetConnected() { return netConnected; }

  /** Tell GATE whether to assume we're connected to the net. */
  public static void setNetConnected(boolean b) { netConnected = b; }

  /** Flag controlling whether we should try to access a web server on
    * localhost, e.g. when setting up a base URL.
    */
  private static boolean localWebServer = true;

  /** Should we assume there's a local web server? */
  public static boolean isLocalWebServer() { return localWebServer; }

  /** Tell GATE whether to assume there's a local web server. */
  public static void setLocalWebServer(boolean b) { localWebServer = b; }

  /** Try to contact a network server. When sucessfull sets urlBase to an HTTP
    * URL for the server.
    * @param hostName the name of the host to try and connect to
    * @param serverPort the port to try and connect to
    * @param path a path to append to the URL when we make a successfull
    * connection. E.g. for host xyz, port 80, path /thing, the resultant URL
    * would be <TT>http://xyz:80/thing</TT>.
    * @see #urlBase
    */
  public static boolean tryNetServer(
    String hostName, int serverPort, String path
  ) throws MalformedURLException {
    Socket socket = null;
    if(DEBUG)
      Out.prln(
        "tryNetServer(hostName=" + hostName + ", serverPort=" + serverPort +
        ", path=" + path +")"
      );

    // is the host listening at the port?
    try{ socket = new Socket(hostName, serverPort); } catch (IOException e){ }
    if(socket != null) {
      urlBase = new URL("http", hostName, serverPort, path);
      return true;
    }

    return false;
  } // tryNetServer()

  /** Try to find GATE files in the local file system */
  private static boolean tryFileSystem() throws MalformedURLException {
    String aGateResourceName = "gate/resources/creole/creole.xml";
    urlBase = Gate.getClassLoader().getResource(aGateResourceName);
    StringBuffer basePath = new StringBuffer(urlBase.toExternalForm());
    String urlBaseName =
      basePath.substring(0, basePath.length() - aGateResourceName.length());
    if(DEBUG) Out.prln("tryFileSystem: " + urlBaseName);

    urlBase = new URL(urlBaseName + "gate/resources/gate.ac.uk/");
    return urlBase == null;
  } // tryFileSystem()

  /** Set the URL base for GATE files, e.g. <TT>http://gate.ac.uk/</TT>. */
  public static void setUrlBase(URL urlBase) { Gate.urlBase = urlBase; }

  /** The URL base for GATE files, e.g. <TT>http://gate.ac.uk/</TT>. */
  private static URL urlBase = null;

  /** Initialise the CREOLE register. */
  public static void initCreoleRegister() throws GateException {

    // register the builtin CREOLE directories
    for(int i=0; i<builtinCreoleDirectoryUrls.length; i++)
      try {
        creoleRegister.addDirectory(
          new URL(builtinCreoleDirectoryUrls[i])
        );
      } catch(MalformedURLException e) {
        throw new GateException(e);
      }

    // add the GATE base URL creole directory
    creoleRegister.addDirectory(Gate.getUrl("creole/"));
    creoleRegister.registerDirectories();

    // register the resources that are actually in gate.jar
    creoleRegister.registerBuiltins();
  } // initCreoleRegister

  /** Class loader used e.g. for loading CREOLE modules, of compiling
    * JAPE rule RHSs.
    */
  private static GateClassLoader classLoader = null;

  /** Get the GATE class loader. */
  public static GateClassLoader getClassLoader() { return classLoader; }

  /** The CREOLE register. */
  private static CreoleRegister creoleRegister = null;

  /** Get the CREOLE register. */
  public static CreoleRegister getCreoleRegister() { return creoleRegister; }

} // class Gate
