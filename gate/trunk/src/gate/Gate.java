/*
 *  Gate.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 31/07/98
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.util.*;
import gate.creole.*;
import gate.config.*;
import gate.event.*;

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

  /** Minimum version of JDK we support */
  protected static final String MIN_JDK_VERSION = "1.3";

  /** Get the minimum supported version of the JDK */
  public static String getMinJdkVersion() { return MIN_JDK_VERSION; }

  /** Initialisation - must be called by all clients before using
    * any other parts of the library. Also initialises the CREOLE
    * register and reads config data (<TT>gate.xml</TT> files).
    * @see #initCreoleRegister
    */
  public static void init() throws GateException {

    // register the URL handler  for the "gate://" URLs
    System.setProperty(
      "java.protocol.handler.pkgs",
      System.getProperty("java.protocol.handler.pkgs")
        + "|" + "gate.util.protocols"
    );

    // create class loader and creole register if they're null
    if(classLoader == null)
      classLoader = new GateClassLoader();
    if(creoleRegister == null)
      creoleRegister = new CreoleRegisterImpl();

    // init the creole register
    initCreoleRegister();

    // init the data store register
    initDataStoreRegister();

    // read gate.xml files; this must come before creole register
    // initialisation in order for the CREOLE-DIR elements to have and effect
    initConfigData();

    // the creoleRegister acts as a proxy for datastore related events
    dataStoreRegister.addCreoleListener(creoleRegister);

    // some of the events are actually fired by the {@link gate.Factory}
    Factory.addCreoleListener(creoleRegister);

    // check we have a useable JDK
    if(System.getProperty("java.version").compareTo(MIN_JDK_VERSION) < 0) {
      throw new GateException(
        "GATE requires JDK " + MIN_JDK_VERSION + " or newer"
      );
    }
  } // init()

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

/*
We'll have to think about this. Right now it points to the creole inside the
jar/classpath so it's the same as registerBuiltins
*/
//    // add the GATE base URL creole directory
//    creoleRegister.addDirectory(Gate.getUrl("creole/"));
//    creoleRegister.registerDirectories();

    // register the resources that are actually in gate.jar
    creoleRegister.registerBuiltins();
  } // initCreoleRegister

  /** Initialise the DataStore register. */
  public static void initDataStoreRegister() {
    dataStoreRegister = new DataStoreRegister();
  } // initDataStoreRegister()

  /** Reads config data (<TT>gate.xml</TT> files). */
  public static void initConfigData() throws GateException {
    ConfigDataProcessor configProcessor = new ConfigDataProcessor();

    // url of the builtin config data (for error messages)
    URL configUrl =
      Gate.getClassLoader().getResource("gate/resources/gate.xml");

    // open a stream to the builtin config data file
    InputStream configStream = null;
    try {
      configStream = Files.getGateResourceAsStream("gate.xml");
    } catch(IOException e) {
      throw new GateException(
        "Couldn't open builtin config data file: " + configUrl + " " + e
      );
    }
    configProcessor.parseConfigFile(configStream, configUrl);
  } // initConfigData()

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
            if(DEBUG) Out.prln("getUrl() returned " + urlBase);
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

  /**
   * Tell GATE whether to assume we're connected to the net. Has to be
   * called <B>before</B> {@link #init()}.
   */
  public static void setNetConnected(boolean b) { netConnected = b; }

  /**
   * Flag controlling whether we should try to access a web server on
   * localhost, e.g. when setting up a base URL. Has to be
   * called <B>before</B> {@link #init()}.
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
    try{
      URL url = new URL("http://" + hostName + ":" + serverPort + "/");
      URLConnection uConn =  url.openConnection();
      HttpURLConnection huConn = null;
      if(uConn instanceof HttpURLConnection)
        huConn = (HttpURLConnection)uConn;
      if(huConn.getResponseCode() == -1) return false;
    } catch (IOException e){
      return false;
    }

//    if(socket != null) {
      urlBase = new URL("http", hostName, serverPort, path);
      return true;
//    }

//    return false;
  } // tryNetServer()

  /** Try to find GATE files in the local file system */
  protected static boolean tryFileSystem() throws MalformedURLException {
    String urlBaseName = locateGateFiles();
    if(DEBUG) Out.prln("tryFileSystem: " + urlBaseName);

    urlBase = new URL(urlBaseName + "gate/resources/gate.ac.uk/");
    return urlBase == null;
  } // tryFileSystem()

  /**
   * Find the location of the GATE binaries (and resources) in the
   * local file system.
   */
  public static String locateGateFiles() {
    String aGateResourceName = "gate/resources/creole/creole.xml";
    URL resourcesUrl = Gate.getClassLoader().getResource(aGateResourceName);

    StringBuffer basePath = new StringBuffer(resourcesUrl.toExternalForm());
    String urlBaseName =
      basePath.substring(0, basePath.length() - aGateResourceName.length());

    return urlBaseName;
  } // locateGateFiles

  /** Returns the value for the HIDDEN attribute of a feature map */
  static public boolean getHiddenAttribute(FeatureMap fm){
    if(fm == null) return false;
    Object value = fm.get("gate.HIDDEN");
    return value != null &&
           value instanceof String &&
           ((String)value).equals("true");
  }

  /** Sets the value for the HIDDEN attribute of a feature map */
  static public void setHiddenAttribute(FeatureMap fm, boolean hidden){
    if(hidden){
      fm.put("gate.HIDDEN", "true");
    }else{
      fm.remove("gate.HIDDEN");
    }
  }

  /** Returns the value for the APPLICATION attribute of a feature map */
  static public boolean getApplicationAttribute(FeatureMap fm){
    if(fm == null) return false;
    Object value = fm.get("gate.APPLICATION");
    return value != null &&
           value instanceof String &&
           ((String)value).equalsIgnoreCase("true");
  }

  /** Sets the value for the APPLICATION attribute of a feature map */
  static public void setApplicationAttribute(FeatureMap fm,
                                             boolean isApplication){
    if(isApplication){
      fm.put("gate.APPLICATION", "true");
    }else{
      fm.remove("gate.APPLICATION");
    }
  }

  /** Gets the NAME attribute feature map.*/
  static public String getName(FeatureMap fm){
    Object value = fm.get("gate.NAME");
    if(value != null && value instanceof String){
      return (String)value;
    }
    return null;
  }

  /** Sets the NAME attribute in a feature map. */
  static public void setName(FeatureMap fm, String name){
    fm.put("gate.NAME", name);
  }


  /** Registers a {@link gate.event.CreoleListener} with the Gate system
    */
  public static synchronized void addCreoleListener(CreoleListener l){
    creoleRegister.addCreoleListener(l);
  } // addCreoleListener

  /** Set the URL base for GATE files, e.g. <TT>http://gate.ac.uk/</TT>. */
  public static void setUrlBase(URL urlBase) { Gate.urlBase = urlBase; }

  /** The URL base for GATE files, e.g. <TT>http://gate.ac.uk/</TT>. */
  private static URL urlBase = null;

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

  /** The DataStore register */
  private static DataStoreRegister dataStoreRegister = null;

  /** Get the DataStore register. */
  public static DataStoreRegister getDataStoreRegister() {
    return dataStoreRegister;
  } // getDataStoreRegister

} // class Gate