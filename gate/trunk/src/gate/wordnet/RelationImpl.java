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
  }

  public int getType() {
    return this.type;
  }

  public int getInverseType() {
    throw new MethodNotImplementedException();
  }

  public String getLabel() {
    throw new MethodNotImplementedException();
  }

  public boolean isApplicableTo(int pos) {
    throw new MethodNotImplementedException();
  }

}