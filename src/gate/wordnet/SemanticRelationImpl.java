/*
 *  SemanticRelation.java
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

public class SemanticRelationImpl extends RelationImpl
                                  implements SemanticRelation {

  private Synset source;
  private Synset target;

  public SemanticRelationImpl(int _type, Synset _src, Synset _target) {

    super(_type);

    Assert.assertNotNull(_src);
    Assert.assertNotNull(_target);
    Assert.assertTrue(WNHelper.isValidSemanticPointer(_type));

    this.source = _src;
    this.target = _target;
  }

  public Synset getSource() {
    return this.source;
  }

  public Synset getTarget() {
    return this.target;
  }

}