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
public class Gate implements GateConstants
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /**
   *  The default StringBuffer size, it seems that we need longer string
   *  than the StringBuffer class default because of the high number of
   *  buffer expansions
   *  */
  public static final int STRINGBUFFER_SIZE = 1024;

  /**
   *  The default size to be used for Hashtable, HashMap and HashSet.
   *  The defualt is 11 and it leads to big memory usage. Having a default
   *  load factor of 0.75, table of size 4 can take 3 elements before being
   *  re-hashed - a values that seems to be optimal for most of the cases.
   *  */
  public static final int HASH_STH_SIZE = 4;


  /**
   *  The database schema owner (GATEADMIN is default)
   *  this one should not be hardcoded but set in the
   *  XML initialization files
   *
   *  */
  public static final String DB_OWNER = "gateadmin";


  /** The list of builtin URLs to search for CREOLE resources. */
  private static String builtinCreoleDirectoryUrls[] = {
    // "http://derwent.dcs.shef.ac.uk/gate.ac.uk/creole/"

    // this has been moved to initCreoleRegister and made relative to
    // the base URL returned by getUrl()
    // "http://gate.ac.uk/creole/"
  };


  /** The GATE URI used to interpret custom GATE tags*/
  public static final String URI = "http://www.gate.ac.uk";

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

    System.setProperty("javax.xml.parsers.SAXParserFactory",
                             "org.apache.xerces.jaxp.SAXParserFactoryImpl");

    //initialise the symbols generator
    lastSym = 0;

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

    //register Lucene as a IR search engine
    try{
      registerIREngine("gate.creole.ir.lucene.LuceneIREngine");
    }catch(ClassNotFoundException cnfe){
      throw new GateRuntimeException(cnfe.toString());
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

  /**
   * Reads config data (<TT>gate.xml</TT> files). There are three
   * sorts of these files:
   * <UL>
   * <LI>
   * The builtin file from GATE's resources - this is read first.
   * <LI>
   * A site-wide init file given as a command-line argument or as a
   * <TT>gate.config</TT> property - this is read second.
   * <LI>
   * The user's file from their home directory - this is read last.
   * </UL>
   * Settings from files read after some settings have already been
   * made will simply overwrite the previous settings.
   */
  public static void initConfigData() throws GateException {
    ConfigDataProcessor configProcessor = new ConfigDataProcessor();

    // url of the builtin config data (for error messages)
    URL configUrl =
      Gate.getClassLoader().getResource("gate/resources/" + GATE_DOT_XML);

    // open a stream to the builtin config data file and parse it
    InputStream configStream = null;
    try {
      configStream = Files.getGateResourceAsStream(GATE_DOT_XML);
    } catch(IOException e) {
      throw new GateException(
        "Couldn't open builtin config data file: " + configUrl + " " + e
      );
    }
    configProcessor.parseConfigFile(configStream, configUrl);

    // parse any command-line initialisation file
    File siteConfigFile = Gate.getSiteConfigFile();
    if(siteConfigFile != null) {
      try {
        configUrl = siteConfigFile.toURL();
        configStream = new FileInputStream(Gate.getSiteConfigFile());
      } catch(IOException e) {
        throw new GateException(
          "Couldn't open site config data file: " + configUrl + " " + e
        );
      }
      configProcessor.parseConfigFile(configStream, configUrl);
    }

    // parse the user's config file (if it exists)
    String userConfigName = getUserConfigFileName();
    File userConfigFile = null;
    URL userConfigUrl = null;
    if(DEBUG) { Out.prln("loading user config from " + userConfigName); }
    configStream = null;
    boolean userConfigExists = true;
    try {
      userConfigFile = new File(userConfigName);
      configStream = new FileInputStream(userConfigFile);
      userConfigUrl = userConfigFile.toURL();
    } catch(IOException e) {
      userConfigExists = false;
    }
    if(userConfigExists)
      configProcessor.parseConfigFile(configStream, userConfigUrl);

    // remember the init-time config options
    originalUserConfig.putAll(userConfig);

    if(DEBUG) {
      Out.prln(
        "user config loaded; DBCONFIG=" + DataStoreRegister.getConfigData()
      );
    }
  } // initConfigData()

  /**
   * Attempts to guess the Unicode font for the platform.
   */
  public static String guessUnicodeFont(){
    //guess the Unicode font for the platform
    String[] fontNames = java.awt.GraphicsEnvironment.
                         getLocalGraphicsEnvironment().
                         getAvailableFontFamilyNames();
    String unicodeFontName = null;
    for(int i = 0; i < fontNames.length; i++){
      if(fontNames[i].equalsIgnoreCase("Arial Unicode MS")){
        unicodeFontName = fontNames[i];
        break;
      }
      if(fontNames[i].toLowerCase().indexOf("unicode") != -1){
        unicodeFontName = fontNames[i];
      }
    }//for(int i = 0; i < fontNames.length; i++)
    return unicodeFontName;
  }

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
          tryNetServer("gate-internal.dcs.shef.ac.uk", 80, "/") ||
   //       tryNetServer("derwent.dcs.shef.ac.uk", 80, "/gate.ac.uk/") ||
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

  private static int lastSym;

  /**
   * A list of names of classes that implement {@link gate.creole.ir.IREngine}
   * that will be used as information retrieval engines.
   */
  private static Set registeredIREngines = new HashSet();

  /**
   * Registers a new IR engine. The class named should implement
   * {@link gate.creole.ir.IREngine}.
   * @param className the fully qualified name of the class to be registered
   * @throws GateException if the class does not implement the
   * {@link gate.creole.ir.IREngine} interface.
   * @throws ClassNotFoundException if the named class cannot be found.
   */
  public static void registerIREngine(String className)
    throws GateException, ClassNotFoundException{
    Class aClass = Class.forName(className);
    if(gate.creole.ir.IREngine.class.isAssignableFrom(aClass)){
      registeredIREngines.add(className);
    }else{
      throw new GateException(className + " does not implement the " +
                              gate.creole.ir.IREngine.class.getName() +
                              " interface!");
    }
  }

  /**
   * Unregisters a previously registered IR engine.
   * @param className the name of the class to be removed from the list of
   * registered IR engines.
   * @return true if the class was found and removed.
   */
  public static boolean unregisterIREngine(String className){
    return registeredIREngines.remove(className);
  }

  /**
   * Gets the set of registered IR engines.
   * @return an unmodifiable {@link java.util.Set} value.
   */
  public static Set getRegisteredIREngines(){
    return Collections.unmodifiableSet(registeredIREngines);
  }

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

  /**
   * Checks whether a particular class is a Gate defined type
   */
  public static boolean isGateType(String classname){
    boolean res = getCreoleRegister().containsKey(classname);
    if(!res){
      try{
        Class aClass = Class.forName(classname);
        res = Resource.class.isAssignableFrom(aClass) ||
              Controller.class.isAssignableFrom(aClass) ||
              DataStore.class.isAssignableFrom(aClass);
      }catch(ClassNotFoundException cnfe){}
    }
    return res;
  }

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

  /**
   * The current executable under execution.
   */
  private static gate.Executable currentExecutable;

  /** Get the DataStore register. */
  public static DataStoreRegister getDataStoreRegister() {
    return dataStoreRegister;
  } // getDataStoreRegister

  /**
   * Sets the {@link Executable} currently under execution.
   * At a given time there can be only one executable set. After the executable
   * has finished its execution this value should be set back to null.
   * An attempt to set the executable while this value is not null will result
   * in the method call waiting until the old executable is set to null.
   */
  public synchronized static void setExecutable(gate.Executable executable) {
    if(executable == null) currentExecutable = executable;
    else{
      while(getExecutable() != null){
        try{
          Thread.currentThread().sleep(200);
        }catch(InterruptedException ie){
          throw new LuckyException(ie.toString());
        }
      }
      currentExecutable = executable;
    }
  } // setExecutable

  /**
   * Returns the curently set executable.
   * {@see setExecutable()}
   */
  public synchronized static gate.Executable getExecutable() {
    return currentExecutable;
  } // getExecutable


  /**
   * Returns a new unique string
   */
  public synchronized static String genSym() {
    StringBuffer buff = new StringBuffer(Integer.toHexString(lastSym++).
                                         toUpperCase());
    for(int i = buff.length(); i <= 4; i++) buff.insert(0, '0');
    return buff.toString();
  } // genSym

  /** GATE development environment configuration data (stored in gate.xml). */
  private static OptionsMap userConfig = new OptionsMap();

  /**
   * This map stores the init-time config data in case we need it later.
   * GATE development environment configuration data (stored in gate.xml).
   */
  private static OptionsMap originalUserConfig = new OptionsMap();

  /** Name of the XML element for GATE development environment config data. */
  private static String userConfigElement = "GATECONFIG";

  /**
   * Gate the name of the XML element for GATE development environment
   * config data.
   */
  public static String getUserConfigElement() { return userConfigElement; }

  /**
   * Get the site config file (generally set during command-line processing
   * or as a <TT>gate.config</TT> property).
   * If the config is null, this method checks the <TT>gate.config</TT>
   * property and uses it if non-null.
   */
  public static File getSiteConfigFile() {
    if(siteConfigFile == null) {
      String gateConfigProperty = System.getProperty(GATE_CONFIG_PROPERTY);
      if(gateConfigProperty != null)
        siteConfigFile = new File(gateConfigProperty);
    }
    return siteConfigFile;
  } // getSiteConfigFile

  /** Set the site config file (e.g. during command-line processing). */
  public static void setSiteConfigFile(File siteConfigFile) {
    Gate.siteConfigFile = siteConfigFile;
  } // setSiteConfigFile

  /** Site config file */
  private static File siteConfigFile;

  /** Shorthand for local newline */
  private static String nl = Strings.getNl();

  /** An empty config data file. */
  private static String emptyConfigFile =
    "<?xml version=\"1.0\"?>" + nl +
    "<!-- " + GATE_DOT_XML + ": GATE configuration data -->" + nl +
    "<GATE>" + nl +
    "" + nl +
    "<!-- NOTE: the next element may be overwritten by the GUI!!! -->" + nl +
    "<" + userConfigElement + "/>" + nl +
    "" + nl +
    "</GATE>" + nl;

  /**
   * Get an empty config file. <B>NOTE:</B> this method is intended only
   * for use by the test suite.
   */
  public static String getEmptyConfigFile() { return emptyConfigFile; }

  /**
   * Get the GATE development environment configuration data
   * (initialised from <TT>gate.xml</TT>).
   */
  public static OptionsMap getUserConfig() { return userConfig; }

  /**
   * Get the original, initialisation-time,
   * GATE development environment configuration data
   * (initialised from <TT>gate.xml</TT>).
   */
  public static OptionsMap getOriginalUserConfig() {
    return originalUserConfig;
  } // getOriginalUserConfig

  /**
   * Update the GATE development environment configuration data in the
   * user's <TT>gate.xml</TT> file (create one if it doesn't exist).
   */
  public static void writeUserConfig() throws GateException {
    // the user's config file
    String configFileName = getUserConfigFileName();
    File configFile = new File(configFileName);

    // create if not there, then update
    try {
      // if the file doesn't exist, create one with an empty GATECONFIG
      if(! configFile.exists()) {
        FileWriter writer = new FileWriter(configFile);
        writer.write(emptyConfigFile);
        writer.close();
      }

      // update the config element of the file
      Files.updateXmlElement(
        new File(configFileName), userConfigElement, userConfig
      );

    } catch(IOException e) {
      throw new GateException(
        "problem writing user " + GATE_DOT_XML + ": " + nl + e.toString()
      );
    }
  } // writeUserConfig

  /**
   * Get the name of the user's <TT>gate.xml</TT> config file (this
   * doesn't guarantee that file exists!).
   */
  public static String getUserConfigFileName() {
    String filePrefix = "";
    if(runningOnUnix()) filePrefix = ".";

    String userConfigName =
      System.getProperty("user.home") + Strings.getFileSep() +
      filePrefix + GATE_DOT_XML;
    return userConfigName;
  } // getUserConfigFileName

  /**
   * Get the name of the user's <TT>gate.ser</TT> session state file (this
   * doesn't guarantee that file exists!).
   */
  public static String getUserSessionFileName() {
    String filePrefix = "";
    if(runningOnUnix()) filePrefix = ".";

    String userSessionName =
      System.getProperty("user.home") + Strings.getFileSep() +
      filePrefix + GATE_DOT_SER;
    return userSessionName;
  } // getUserSessionFileName

  /**
   * This method tries to guess if we are on a UNIX system. It does this
   * by checking the value of <TT>System.getProperty("file.separator")</TT>;
   * if this is "/" it concludes we are on UNIX. <B>This is obviously not
   * a very good idea in the general case, so nothing much should be made
   * to depend on this method (e.g. just naming of config file
   * <TT>.gate.xml</TT> as opposed to <TT>gate.xml</TT>)</B>.
   */
  public static boolean runningOnUnix() {
    return Strings.getFileSep().equals("/");
  } // runningOnUnix

  /** Flag for SLUG GUI start instead of standart GATE GUI. */
  private static boolean slugGui = false;

  /** Should we start SLUG GUI. */
  public static boolean isSlugGui() { return slugGui; }

  /** Tell GATE whether to start SLUG GUI. */
  public static void setSlugGui(boolean b) { slugGui = b; }

} // class Gate
