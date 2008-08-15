/*
 *  Constraint Predicate implementation
 *
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Ian Roberts, 03/09/08
 *
 *  $Id$
 */
package gate.jape.constraint;

import gate.jape.JapeException;

public class NotEqualPredicate extends EqualPredicate {

  public String getOperator() {
    return NOT_EQUAL;
  }

  public boolean doMatch(Object annotValue, Object context) throws JapeException {
    return !super.doMatch(annotValue, context);
  }

}
