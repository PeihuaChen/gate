/*
 *  StreamingCharFactory.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Angel Kirilov,  10 January 2002
 *
 *  $Id$
 */

package gate.xml;

import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.utils.ChunkyByteArray;
import org.apache.xerces.utils.StringPool;
import org.xml.sax.InputSource;
import org.apache.xerces.readers.*;

import java.io.*;
import java.net.URL;
import java.util.Stack;

/**
 * With this class the correct possition in the parsed XML document will be
 * reported in the characters() callback function during the SAX parsing.
 * You should set an instance of this class to the parser with the method
 * setReaderFactory().
 * <BR>
 * If you use default reader factory you will recieve zerro instead of correct
 * position in the file.
 */
public class StreamingCharFactory extends DefaultReaderFactory {


    public XMLEntityHandler.EntityReader createCharReader(XMLEntityHandler  entityHandler,
                                                          XMLErrorReporter errorReporter,
                                                          boolean sendCharDataAsCharArray,
                                                          Reader reader,
                                                          StringPool stringPool)
    throws Exception
    {
        return new org.apache.xerces.readers.StreamingCharReader(entityHandler,
                                                                 errorReporter,
//                                                                 sendCharDataAsCharArray,
                                                                 true,
                                                                 reader,
                                                                 stringPool);
    }

    public XMLEntityHandler.EntityReader createUTF8Reader(XMLEntityHandler entityHandler,
                                                          XMLErrorReporter errorReporter,
                                                          boolean sendCharDataAsCharArray,
                                                          InputStream data,StringPool stringPool)
    throws Exception
    {
        XMLEntityHandler.EntityReader reader;
        reader = new org.apache.xerces.readers.StreamingCharReader(entityHandler,
                                                                   errorReporter,
//                                                                 sendCharDataAsCharArray,
                                                                   true,
                                                                   new InputStreamReader(data, "UTF8"),
                                                                   stringPool);
        return reader;
    }
}