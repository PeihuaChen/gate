/*
 *  Scratch.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 22/03/00
 *
 *  $Id$
 */


package gate.util;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.creole.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import org.w3c.www.mime.*;

/** A scratch pad for experimenting.
  */
public class Scratch
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  public static void main(String args[]) {
    try{
      Gate.init();
      doIt();
/*
      URL url = null;
      url = new URL("file:///d:/tmp/testXml.xml");

        // Load the xml Key Document and unpack it
        gate.Document keyDocument = null;
        keyDocument = gate.Factory.newDocument(url);

      gate.DocumentFormat keyDocFormat = null;
      keyDocFormat = gate.DocumentFormat.getDocumentFormat(
        keyDocument, keyDocument.getSourceUrl()
      );
      Out.prln(keyDocFormat);
      // Unpack the markup
      keyDocFormat.unpackMarkup(keyDocument);
      Out.prln(keyDocument.getContent().toString());
*/
    }catch (Exception e){
      e.printStackTrace(System.out);
    }

  } // main

  public static void doIt() throws Exception{
    String str = new String(
                 "<s><w>Salut</w> <w>Ba</w> <p/><w>Ce</w> <w>misto</w> </s>"
                 );
    gate.Document doc = Factory.newDocument(str);
    doc.setSourceUrl(null);
    gate.DocumentFormat keyDocFormat = null;
    keyDocFormat = gate.DocumentFormat.getDocumentFormat(
      doc, new MimeType("text/xml")
    );

    // Unpack the markup
    keyDocFormat.unpackMarkup(doc);


    // Export the Gate document called keyDocument as  XML, into a temp file,
    // using UTF-8 encoding
    File xmlFile = new File("d:/tmp/testXml.xml");

    // Prepare to write into the xmlFile using UTF-8 encoding
    OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(xmlFile),"UTF-8");
    // Write (test the toXml() method)
    writer.write(doc.toXml());
    writer.flush();
  } // doIt

  /** Generate a random integer for file naming. */
  protected static int random() {
    return randomiser.nextInt(9999);
  } // random

  /** Random number generator */
  protected static Random randomiser = new Random();

} // class Scratch

