/*
 *  ConfigDataProcessor.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 9/Nov/2000
 *
 *  $Id$
 */

package gate.config;

import java.util.*;
import java.net.*;
import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;

import gate.*;
import gate.util.*;


/** This class parses <TT>gate.xml</TT> configuration data files.
  */
public class ConfigDataProcessor
{
  /** Debug flag */
  protected static final boolean DEBUG = false;

  /** The parser for the CREOLE directory files */
  protected SAXParser parser = null;


  /** Default constructor. Sets up config files parser. */
  public ConfigDataProcessor() throws GateException {

    // construct a SAX parser for parsing the config files
    try {
      // Get a parser factory.
      // Set up the factory to create the appropriate type of parser:
      // non validating one
      // non namespace aware one
      // create the parser
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      saxParserFactory.setValidating(false);
      saxParserFactory.setNamespaceAware(true);
      parser = saxParserFactory.newSAXParser();


    } catch (SAXException e) {
      if(DEBUG) Out.println(e);
      throw(new GateException(e));
    } catch (ParserConfigurationException e) {
      if(DEBUG) Out.println(e);
      throw(new GateException(e));
    }

  } // default constructor

  /** Parse a config file (represented as an open stream).
    */
  public void parseConfigFile(InputStream configStream, URL configUrl)
  throws GateException
  {
    String nl = Strings.getNl();

    // create a handler for the config file and parse it
    String configString = null; // for debug messages
    try {
      if(DEBUG) {
        File configFile = new File(configUrl.getFile());
        if(configFile.exists())
          configString = Files.getString(configUrl.getFile());
        else
          configString = configUrl.toString();
      }
      DefaultHandler handler = new ConfigXmlHandler(configUrl);
      parser.parse(configStream, handler);
      if(DEBUG) {
        Out.prln(
          "done parsing " +
          ((configUrl == null) ? "null" : configUrl.toString())
        );
      }
    } catch (IOException e) {
      Out.prln("conf file:"+nl+configString+nl);
      throw(new GateException("Config data error 1 on "+configUrl+": "+nl+e));
    } catch (SAXException e) {
      Out.prln("conf file:"+nl+configString+nl);
      throw(new GateException("Config data error 2 on "+configUrl+": "+nl+e));
    }

  } // parseConfigFile

} // class ConfigDataProcessor
