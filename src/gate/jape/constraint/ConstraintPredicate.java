/*
 *  ConstraintPredicate - transducer class
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

import gate.Annotation;
import gate.jape.JapeException;

/**
 * A predicate defines a single boolean operation on an
 * {@link gate.Annotation} or some property of an annotation. These are
 * also referred to as attributes of a constraint.
 * <p>
 * Implementors will determine if a provided annotation matches the
 * predicate based on the intent of the operator (equals, not equals,
 * greater than, etc).
 *
 * @version $Revision$
 * @author esword
 */
public interface ConstraintPredicate {

  // Standard operators. Note that this was purposefully not done as an
  // enum so that additional operators could be added dynamically for other
  // parsers
  public String EQUAL = "==";

  public String GREATER = ">";

  public String LESSER = "<";

  public String GREATER_OR_EQUAL = ">=";

  public String LESSER_OR_EQUAL = "<=";

  public String REGEXP = "=~";

  // Note that the != and !~ operators are syntactic shortcuts and not
  // encapsulated in distinct predicates. The parser translates these
  // to be negated versions of == and =~
  // public String NOT_EQUAL = "!=";
  // public String NOT_REGEXP = "!~";

  /**
   * The accessor associated with this predicate.
   *
   * @return
   */
  public AnnotationAccessor getAccessor();

  /**
   * Set the accessor associated with this predicate.
   */
  public void setAccessor(AnnotationAccessor accessor);

  /**
   * The value used in comparisons against passed in data in
   * {@link #matches(Annotation)}.
   *
   * @return
   */
  public Object getValue();

  /**
   * Set the value used in comparisons against passed in data in
   * {@link #matches(Annotation)}.
   *
   * @param value
   */
  public void setValue(Object value);

  /**
   * String representation of the logic operator that the predicate
   * implements.
   */
  public String getOperator();

  /**
   * Evaluates if the provided annotation meets the requirements of the
   * predicate.
   *
   * @param annot
   * @param context
   * @return
   * @throws JapeException
   */
  public boolean matches(Annotation annot, Object context) throws JapeException;
}
