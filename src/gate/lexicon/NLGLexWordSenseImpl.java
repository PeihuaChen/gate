/*
 *  NLGLexWordSenseImpl.java
 *
 *  Copyright (c) 1998-2003, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 21/February/2003
 *
 *  $Id$
 */

package gate.lexicon;

import java.util.HashMap;
import java.io.Serializable;

public class NLGLexWordSenseImpl extends MutableLexKBWordSenseImpl
    implements NLGLexWordSense, Serializable {

  protected HashMap extraLexInfo;
  static final long serialVersionUID = -1049615572642010565L;

  public NLGLexWordSenseImpl(Word myWord, MutableLexKBSynset mySynset,
                                   int mySenseNumber, int myOrderInSynset) {
    super(myWord, mySynset, mySenseNumber, myOrderInSynset);
    extraLexInfo = new HashMap();
  }

  public void setExtraInfo(HashMap newInfo) {
    if (newInfo != null)
      extraLexInfo = newInfo;
    else
      extraLexInfo.clear();
  }

  public void addExtraInfo(String key, Object value) {
    extraLexInfo.put(key, value);
  }

  public Object getExtraInfo(String key) {
    return extraLexInfo.get(key);
  }

  public HashMap getExtraInfo() {
    return extraLexInfo;
  }
}