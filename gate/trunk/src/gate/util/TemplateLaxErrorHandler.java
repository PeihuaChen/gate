/*
 *	TemplateLaxErrorHandler.java
 *
 *	Cristian URSU, 07/July/2000
 *
 *	$Id$
 */

// modify this according with your package
package gate.util;


/**
 * TemplateLaxErrorHandler
 */
import java.io.*;
import org.xml.sax.*;

// this import is for the abstract class LaxErrorHandler located in gate.util


// modify the class name the way you want
public class TemplateLaxErrorHandler extends LaxErrorHandler {
/**
 * TemplateLaxErrorHandler constructor comment.
 */
public TemplateLaxErrorHandler() {super();}
/**
 * error method comment.
 */
public void error(SAXParseException ex) throws SAXException{
  // do something with the error
	File fInput = new File (ex.getSystemId());
	System.err.println("e: " + fInput.getPath() + ": line " + ex.getLineNumber() + ": " + ex);
}
/**
 * fatalError method comment.
 */
public void fatalError(SAXParseException ex) throws SAXException{
  // do something with the fatalError
	File fInput = new File(ex.getSystemId());
	System.err.println("E: " + fInput.getName() + ": line " + ex.getLineNumber() + ": " + ex);
}
/**
 * warning method comment.
 */
public void warning(SAXParseException ex) throws SAXException {
  // do something with the warning.
	File fInput = new File(ex.getSystemId());
	System.err.println("w: " + fInput.getName() + ": line " + ex.getLineNumber() + ": " + ex);
}

}// TemplateLaxErrorHandler
