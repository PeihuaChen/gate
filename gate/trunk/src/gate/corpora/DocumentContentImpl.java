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
    long s = 0, e = Long.MAX_VALUE, counter = 0;
    if(start != null && end != null) {
      s = start.longValue();
      e = end.longValue();
    }

    uReader = new BufferedReader(new InputStreamReader(u.openStream()));
    while( true ) {
      int i = uReader.read();
      if(i == -1) break;

      if(counter >= s && counter < e)
        buf.append((char) i);
      counter++;
    }

    content = new String(buf);
  } // Contruction from URL and offsets */

  /** Propagate changes to the document content. */
  void edit(Long start, Long end, DocumentContent replacement)
  {
    int s = start.intValue(), e = end.intValue();
    String repl = ((DocumentContentImpl) replacement).content;
    StringBuffer newContent = new StringBuffer(content);
    newContent.replace(s, e, repl);
  } // edit(start,end,replacement)

  /** The contents under a particular span. */
  public DocumentContent getContent(Long start, Long end)
    throws InvalidOffsetException
  {
    if(! isValidOffsetRange(start, end))
      throw new InvalidOffsetException();

    return new DocumentContentImpl(
      content.substring(start.intValue(), end.intValue())
    );
  } // getContent(start, end)

/** Returns the String representing the content in case of a textual document.
  * NOTE: this is a temporary solution until we have a more generic one.
  */
  public String toString(){
    return content;
  }

  /** The size of this content (e.g. character length for textual
    * content).
    */
  public Long size() {
    return new Long(content.length());
  } // size()

  /** Check that an offset is valid */
  boolean isValidOffset(Long offset) {
    if(offset == null)
      return false;

    long o = offset.longValue();
    long len = content.length();
    if(o > len || o < 0)
      return false;

    return true;
  } // isValidOffset

  /** Check that both start and end are valid offsets and that
    * they constitute a valid offset range
    */
  boolean isValidOffsetRange(Long start, Long end) {
    return
      isValidOffset(start) && isValidOffset(end) &&
      start.longValue() <= end.longValue();
  } // isValidOffsetRange(start,end)


  /** Just for now - later we have to cater for different types of
    * content.
    */
  String content;

  /** For ranges */
  DocumentContentImpl(String s) { content = s; }

} // class DocumentContentImpl
