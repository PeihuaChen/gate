/*
  DocumentContentImpl.java 

  Hamish Cunningham, 11/Feb/2000

  $Id$
*/

package gate.corpora;

import java.util.*;
import java.net.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;

/** Represents the commonalities between all sorts of document contents.
  */
public class DocumentContentImpl implements DocumentContent
{
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
    throw new LazyProgrammerException();
  } // size()

} // class DocumentContentImpl
