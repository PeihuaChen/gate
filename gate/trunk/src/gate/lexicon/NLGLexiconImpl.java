/*
 *  NLGLexiconImpl.java
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

import java.io.Serializable;

public class NLGLexiconImpl extends MutableLexicalKnowledgeBaseImpl
    implements Serializable {

  static final long serialVersionUID = -2543190013851016324L;

  public NLGLexiconImpl() {
    super();
    this.setLexiconId("MIAKT NLG Lexicon");
  }

  public MutableWord addWord(String lemma) {
    if (words.containsKey(lemma))
      return (MutableWord) words.get(lemma);

    MutableWordImpl newWord = new NLGLexWordImpl(lemma);
    words.put(lemma, newWord);
    return newWord;
  }
}