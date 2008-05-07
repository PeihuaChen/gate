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
 *  Eric Sword, 03/09/08
 *
 *  $Id$
 */
package gate.jape.constraint;

import gate.jape.JapeException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExpPredicate extends AbstractConstraintPredicate {

  public String getOperator() {
    return REGEXP;
  }

  @Override
  public String toString() {
    String val = ((Pattern)getValue()).pattern();
    return getAccessor() + getOperator() + "\"" + val + "\"";
  }

  @Override
  public void setValue(Object value) {
    if(value == null) value = "";
    try {
      super.setValue(Pattern.compile(value.toString()));
    }
    catch(PatternSyntaxException pse) {
      throw new IllegalArgumentException("Cannot compile pattern '" + value
              + "'");
    }
  }

  /**
   * Returns true if the given value matches the set pattern. If the
   * value is null it is treated as an empty string.
   */
  public boolean doMatch(Object annotValue, Object context)
          throws JapeException {

    if(annotValue == null) annotValue = "";

    if(annotValue instanceof String) {
      String annotValueString = (String)annotValue;
      Pattern constraintPattern = (Pattern)getValue();
      return constraintPattern.matcher(annotValueString).matches();
    }
    else {
      throw new JapeException("Cannot do pattern matching on attribute '"
              + getAccessor() + "'.  Are you sure the value is a string?");
    }
  }

}
