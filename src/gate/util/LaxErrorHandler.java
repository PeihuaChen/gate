/*
 *	LaxErrorHandler.java
 *
 *	Cristian URSU,  7/July/2000
 *
 *	$Id$
 */
package gate.util;

/**
 * LaxErrorHandler
 */
import java.io.*;
import org.xml.sax.*;

public abstract class LaxErrorHandler implements ErrorHandler {
/**
 * LaxErrorHandler constructor comment.
 */
public LaxErrorHandler() {super();}
/**
 * error method comment.
 */
public abstract void error(SAXParseException ex) throws SAXException;
/**
 * fatalError method comment.
 */
public abstract void fatalError(SAXParseException ex) throws SAXException ;
/**
 * warning method comment.
 */
public abstract void warning(SAXParseException ex) throws SAXException ;
}
