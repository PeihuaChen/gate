/*
	CreoleRegisterImpl.java

	Hamish Cunningham, 1/Sept/2000

	$Id$
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
  * looking at other class' code).
  * @see gate.CreoleRegister
  */
public class CreoleRegisterImpl extends HashMap implements CreoleRegister
{
  /** The set of CREOLE directories (URLs). */
  private Set directories = new HashSet();

  /** Are we debugging? */
  private static final boolean debug = true;

  /** Add a CREOLE directory URL. The directory is <B>not</B> registered. */
  public void addDirectory(URL directoryUrl) {
    directories.add(directoryUrl);
  } // addDirectory

  /** Get the list of CREOLE directory URLs. */
  public Set getDirectories() {
    return directories;
  } // getDirectories

  /** Register all the CREOLE directories that we know of.
    * The <CODE>creole.xml</CODE> files
    * at the URLs are parsed, and <CODE>CreoleData</CODE> objects added
    * to the register.
    */
  public void registerDirectories() {
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
    */
  public void registerDirectories(URL directoryUrl) {
    // if the URL ends in creole.xml, pass it directly to the parser;
    // else add creole.xml and pass it
    String urlName = directoryUrl.toExternalForm().toLowerCase();
    if(! urlName.endsWith("creole.xml")) {
      boolean needSlash = false;
      if(! urlName.endsWith("/")) needSlash = true;
      try {
        directoryUrl =
          new URL(urlName + ((needSlash) ? "/creole.xml" : "creole.xml"));
      } catch(MalformedURLException e) {
        if(debug) System.out.println(e);
//// process e
      }
    }

    // construct a parser for the directory file and parse it.
    // this will create ResourceData entries in this
    SAXParser parser = null;
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

      // use it
      HandlerBase handler = new CreoleXmlHandler(this);
      parser.parse(directoryUrl.openStream(), handler);

	  } catch (IOException e) {
      if(debug) System.out.println(e);
//// process e
	  } catch (SAXException e) {
      if(debug) System.out.println(e);
//// process e
	  } catch (ParserConfigurationException e) {
      if(debug) System.out.println(e);
//// process e
	  }

  } // registerDirectories

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
    return null;
  } // createCreoleDirectoryFile

} // class CreoleRegisterImpl
