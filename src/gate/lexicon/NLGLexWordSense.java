/*
 *  NLGWordSense.java
 *
 *  Copyright (c) 1998-2003, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 28/January/2003
 *
 *  $Id$
 */


package gate.lexicon;

import java.util.*;

public interface NLGLexWordSense extends MutableLexKBWordSense {

  /** Sets the extra syntactic info, which is a hash map
   with keys strings (e.g., synt-category) and values
   any object that is serialisable */
  public void setExtraInfo(HashMap newInfo);

  /** Add extra info for the given key and value */
  public void addExtraInfo(String key, Object value);

  /** Returns the extra info associated with a key */
  public Object getExtraInfo(String key);

  /** Returns all extra info for this entry */
  public HashMap getExtraInfo();
}