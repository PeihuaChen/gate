/*
 *  Synset.java
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

import gate.util.*;

class RelationImpl implements Relation {

  private int type;

  protected RelationImpl(int _type) {
    this.type = _type;
  }

  public int getType() {
    return this.type;
  }

  public String getLabel() {
    return WNHelper.int2PointerType(this.type).getLabel();
  }

  public int getInverseType() {

    switch(this.type) {

      case Relation.REL_ANTONYM:
        return Relation.REL_ANTONYM;

      case Relation.REL_HYPONYM:
        return Relation.REL_HYPERNYM;

      case Relation.REL_HYPERNYM:
        return Relation.REL_HYPONYM;

      case Relation.REL_MEMBER_HOLONYM:
        return Relation.REL_MEMBER_MERONYM;

      case Relation.REL_MEMBER_MERONYM:
        return Relation.REL_MEMBER_HOLONYM;

      case Relation.REL_SIMILAR_TO:
        return Relation.REL_SIMILAR_TO;

      case Relation.REL_ATTRIBUTE:
        return Relation.REL_ATTRIBUTE;

      case Relation.REL_VERB_GROUP:
        return Relation.REL_VERB_GROUP;

      default:
        return -1;
    }
  }


  public boolean isApplicableTo(int pos) {
    return WNHelper.int2PointerType(this.type).appliesTo(WNHelper.int2POS(pos));
  }

}