/*
 *  CreoleRegisterImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 1/Sept/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.net.*;
import java.io.*;

import org.xml.sax.*;
import javax.xml.parsers.*;

import gate.*;
import gate.util.*;


/** This class implements the CREOLE register interface. DO NOT
  * construct objects of this class unless your name is gate.util.Gate
  * (in which case please go back to the source code repository and stop
  * looking at other class's code).
  * @see gate.CreoleRegister
  */
public class CreoleRegisterImpl extends HashMap implements CreoleRegister
{
  /** Debug flag */
  protected static final boolean DEBUG = false;

  /** The set of CREOLE directories (URLs). */
  protected Set directories = new HashSet();

  /** The parser for the CREOLE directory files */
  protected SAXParser parser = null;

  /** Default constructor. Sets up directory files parser. */
  public CreoleRegisterImpl() throws GateException {

    // construct a SAX parser for parsing the CREOLE directory files
    try {
      // Get a parser factory.
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

      // Set up the factory to create the appropriate type of parser:
      // non validating one
      saxParserFactory.setValidating(false);
      // non namespace aware one
      saxParserFactory.setNamespaceAware(false);

      // create the parser
      parser = saxParserFactory.newSAXParser();

    } catch (SAXException e) {
      if(DEBUG) Out.println(e);
      throw(new GateException(e));
    } catch (ParserConfigurationException e) {
      if(DEBUG) Out.println(e);
      throw(new GateException(e));
    }

  } // default constructor

  /** Removes all resources and forgets all directories. */
  public void clear() { directories.clear(); super.clear(); }

  /** Add a CREOLE directory URL to the register and to the GATE classloader.
    * The directory is <B>not</B> registered.
    */
  public void addDirectory(URL directoryUrl) {
    directories.add(directoryUrl);
  } // addDirectory

  /** Get the list of CREOLE directory URLs. */
  public Set getDirectories() {
    return directories;
  } // getDirectories

  /** Register all the CREOLE directories that we know of.
    * The <CODE>creole.xml</CODE> files
    * at the URLs are parsed, and <CODE>ResourceData</CODE> objects added
    * to the register.
    * URLs for resource JAR files are added to the GATE class loader.
    */
  public void registerDirectories() throws GateException {
    Iterator iter = directories.iterator();

    while(iter.hasNext()) {
      URL directoryUrl = (URL) iter.next();
      registerDirectories(directoryUrl);
    }
  } // registerDirectories

  /** Register a single CREOLE directory. The <CODE>creole.xml</CODE>
    * file at the URL is parsed, and <CODE>CreoleData</CODE> objects added
    * to the register. If the directory URL has not yet been added it
    * is now added.
    * URLs for resource JAR files are added to the GATE class loader.
    */
  public void registerDirectories(URL directoryUrl) throws GateException {

    // add the URL (may overwrite an existing one; who cares)
    directories.add(directoryUrl);

    // directory URLs shouldn't include "creole.xml"
    String urlName = directoryUrl.toExternalForm().toLowerCase();
    if(urlName.endsWith("creole.xml")) {
      throw new GateException(
        "CREOLE directory URLs should point to the parent location of " +
        "the creole.xml file, not the file itself; bad URL was: " + urlName
      );
    }

    // create a URL for the creole.xml file, based on the directory URL
    URL directoryXmlFileUrl = directoryUrl;
    String separator = "/";
    if(urlName.endsWith(separator)) separator = "";
    try {
      directoryXmlFileUrl = new URL(urlName + separator + "creole.xml");
    } catch(MalformedURLException e) {
      throw(new GateException("bad creole.xml URL, based on " + urlName));
    }

    // parse the directory file
    try {
      parseDirectory(directoryXmlFileUrl.openStream(), directoryUrl);
    } catch(IOException e) {
      throw(new GateException("couldn't open creole.xml: " + e.toString()));
    }
  } // registerDirectories(URL)

  /** Parse a directory file (represented as an open stream), adding
    * resource data objects to the CREOLE register as they occur.
    * If the resource is from a URL then that location is passed (otherwise
    * null).
    */
  protected void parseDirectory(InputStream directoryStream, URL directoryUrl)
  throws GateException
  {
    // create a handler for the directory file and parse it;
    // this will create ResourceData entries in the register
    try {
      HandlerBase handler = new CreoleXmlHandler(this, directoryUrl);
      parser.parse(directoryStream, handler);
      if(DEBUG) {
        Out.prln(
          "done parsing " +
          ((directoryUrl == null) ? "null" : directoryUrl.toString())
        );
      }
    } catch (IOException e) {
      throw(new GateException(e));
    } catch (SAXException e) {
      if(DEBUG) Out.println(e.toString());
      throw(new GateException(e));
    }

  } // parseDirectory

  /** Register resources that are built in to the GATE distribution.
    * These resources are described by the <TT>creole.xml</TT> file in
    * <TT>resources/creole</TT>.
    */
  public void registerBuiltins() throws GateException {
    try {
      parseDirectory(
        Files.getGateResourceAsStream("creole/creole.xml"),
        Gate.getClassLoader().getResource("gate/resources/creole/")
// Gate.getUrl("creole/")
      );
// Out.prln(Gate.getClassLoader().getResource("gate/resources/creole/"));
    } catch(IOException e) {
      if(DEBUG) System.out.println(e);
      throw(new GateException(e));
    }
  } // registerBuiltins()

  /** This is a utility method for creating CREOLE directory files
    * (typically called <CODE>creole.xml</CODE>) from a list of Jar
    * files that contain resources. The method concatenates the
    * <CODE>creole.xml</CODE> files that the Jars contain.
    * <P>
    * If Java allowed class methods in interfaces this would be static.
    */
  public File createCreoleDirectoryFile(File directoryFile, Set jarFileNames)
  {
    ////////////////////
    // dump xml header and comment header and <CREOLE-DIRECTORY> into dirfile
    // for each jar file pick out creole.xml
    // strip xml header
    // dump into dirfile
    // put </CREOLE-DIRECTORY> into dirfile
    throw new LazyProgrammerException();
  } // createCreoleDirectoryFile

} // class CreoleRegisterImpl
