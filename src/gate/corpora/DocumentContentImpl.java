/*
  DocumentContentImpl.java 

  Hamish Cunningham, 11/Feb/2000

  $Id$
*/

package gate.corpora;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;

/** Represents the commonalities between all sorts of document contents.
  */
public class DocumentContentImpl implements DocumentContent
{
  /** Default construction */
  public DocumentContentImpl() {
    content = new String();
  } // default construction

  /** Contruction from URL and offsets. */
  public DocumentContentImpl(URL u, Long start, Long end) throws IOException
  {
    BufferedReader uReader = null;
    StringBuffer buf = new StringBuffer();
    char c;

    uReader = new BufferedReader(new InputStreamReader(u.openStream()));
    while( true ) {
      int i = uReader.read();
      if(i == -1) break;
      buf.append((char) i);
    }

      content = new String(buf);
  } // Contruction from URL and offsets */


  /** Propagate changes to the document content. */
  void edit(Long start, Long end, DocumentContent replacement)
  throws InvalidOffsetException {
    throw new LazyProgrammerException();
  } // edit(start,end,replacement)

  /** The contents under a particular span. */
  public DocumentContent getContent(Long start, Long end)
  throws InvalidOffsetException {
    throw new LazyProgrammerException();
  } // getContent(start, end)

  /** The size of this content (e.g. character length for textual
    * content).
    */
  public Long size() {
    return new Long(content.length());
  } // size()


  /** Just for now - later we have to cater for different types of
    * content.
    */
  String content;

  /** For ranges */
  DocumentContentImpl(String s) { content = s; }

} // class DocumentContentImpl
