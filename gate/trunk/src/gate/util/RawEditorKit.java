/*
 *	RawEditorKit.java
 *
 *	Valentin Tablan, Nov/1999
 *
 *	$Id$
 */

package gate.util;

import javax.swing.text.*;
import java.io.Reader;
import java.io.IOException;
/** This class provides an editor kit that does not change \n\r to \n but
  * instead it leaves the original text as is.
  * Needed for GUI components
  */
public class RawEditorKit extends StyledEditorKit {
      /**
     * Inserts content from the given stream, which will be
     * treated as plain text. Modified StyledEditorKit so it does not ignore
     * \r's in \r\n.
     *
     * @param in  The stream to read from
     * @param doc The destination for the insertion.
     * @param pos The location in the document to place the
     *   content >= 0.
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *   location within the document.
     */
    public void read(Reader in, Document doc, int pos)
        throws IOException, BadLocationException {

        char[] buff = new char[4096];
        int nch;
	boolean lastWasCR = false;
	boolean isCRLF = false;
	boolean isCR = false;
	int last;
	boolean wasEmpty = (doc.getLength() == 0);

	// Read in a block at a time, mapping single
        // \r's to \n's. If a \r\n is encountered, \r\n will be set as the
        // newline string for the document, if \r is encountered it will
        // be set as the newline character, otherwise the newline property
        // for the document will be removed.
        while ((nch = in.read(buff, 0, buff.length)) != -1) {
	    last = 0;
	    for(int counter = 0; counter < nch; counter++) {
		switch(buff[counter]) {
		case '\r':
		    if (lastWasCR) {
			isCR = true;
			if (counter == 0) {
			    doc.insertString(pos, "\n", null);
			    pos++;
			}
			else {
			    buff[counter - 1] = '\n';
			}
		    }
		    else {
			lastWasCR = true;
		    }
		    break;
		case '\n':
		    if (lastWasCR) {
			if (counter > (last + 1)) {
			    doc.insertString(pos, new String(buff, last,
					    counter - last - 1), null);
			    pos += (counter - last - 1);
			}
			// else nothing to do, can skip \r, next write will
			// write \n
			lastWasCR = false;
			last = counter-1;
			isCRLF = true;
		    }
		    break;
		default:
		    if (lastWasCR) {
			isCR = true;
			if (counter == 0) {
			    doc.insertString(pos, "\n", null);
			    pos++;
			}
			else {
			    buff[counter - 1] = '\n';
			}
			lastWasCR = false;
		    }
		    break;
		}
	    }
	    if (last < nch) {
		if(lastWasCR) {
		    if (last < (nch - 1)) {
			doc.insertString(pos, new String(buff, last,
					 nch - last - 1), null);
			pos += (nch - last - 1);
		    }
		}
		else {
		    doc.insertString(pos, new String(buff, last,
				     nch - last), null);
		    pos += (nch - last);
		}
	    }
        }
	if (lastWasCR) {
	    doc.insertString(pos, "\n", null);
	    isCR = true;
	}
	if (wasEmpty) {
	    if (isCRLF) {
		doc.putProperty(EndOfLineStringProperty, "\r\n");
	    }
	    else if (isCR) {
		doc.putProperty(EndOfLineStringProperty, "\r");
	    }
	    else {
		doc.putProperty(EndOfLineStringProperty, "\n");
	    }
	}
    }

}//class

