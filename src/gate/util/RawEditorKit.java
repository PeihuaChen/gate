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
      Inserts content from the given stream, which will be
      treated as plain text.
      This insertion is done without checking \r or \r \n sequence.
      It takes the text from the Reader and place it into Document at position pos
   */
  public void read(Reader in, Document doc, int pos)
              throws IOException, BadLocationException {

    char[] buff = new char[65536];
    int charsRead = 0;

    while ((charsRead = in.read(buff, 0, buff.length)) != -1) {
          doc.insertString(pos, new String(buff, 0, charsRead), null);
          pos += charsRead;
	  }//while
  }//read
}//class

