/*
 *  LexicalRelation.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 16/May/2002
 *
 *  $Id$
 */

package gate.wordnet;

import junit.framework.*;

/** Represents WordNet lexical relation.
 *  implrments LexicalRelation
 */

public class LexicalRelationImpl extends RelationImpl
                                  implements LexicalRelation {

  /** relation source */
  private WordSense source;
  /** relation target */
  private WordSense target;

  public LexicalRelationImpl(int _type, WordSense _src, WordSense _target) {

    super(_type);

    Assert.assertNotNull(_src);
    Assert.assertNotNull(_target);
    Assert.assertTrue(WNHelper.isValidLexicalPointer(_type));

    this.source = _src;
    this.target = _target;
  }


  /** returns the source (WordSense) of this lexical relation */
  public WordSense getSource() {
    return this.source;
  }


  /** returns the target (WordSense) of this lexical relation */
  public WordSense getTarget() {
    return this.target;
  }

}