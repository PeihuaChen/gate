/*
 *  TestEmail.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  7/Aug/2000
 *
 *  $Id$
 */

package gate.email;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.util.*;
import gate.gui.*;
import gate.email.*;

import junit.framework.*;
import org.w3c.www.mime.*;


/**
  * Test class for Email facilities
  */
public class TestEmail extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestEmail(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** A test */
  public void testUnpackMarkup() throws Exception{
    assert(true);

    // create the markupElementsMap map
    Map markupElementsMap = null;
    gate.Document doc = null;
    Gate.init();
    doc = gate.Factory.newDocument(Gate.getUrl("tests/email/test.eml"), "ISO-8859-1");

    // get a document format that deals with e-mails
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat(
      doc, doc.getSourceUrl()
    );
    assert( "Bad document Format was produced.EmailDocumentFormat was expected",
            docFormat instanceof gate.corpora.EmailDocumentFormat
          );

    docFormat.unpackMarkup (doc,"DocumentContent");
  } // testUnpackMarkup()

  public static void main(String[] args) {
    try{
      Gate.init();
      TestEmail testEmail = new TestEmail("");
      testEmail.testUnpackMarkup();

    }catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
    * final test
    */
  public void testEmail(){
    EmailDocumentHandler emailDocumentHandler = new EmailDocumentHandler();
    emailDocumentHandler.testSelf();
  }// testEmail

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestEmail.class);
  } // suite

} // class TestEmail
