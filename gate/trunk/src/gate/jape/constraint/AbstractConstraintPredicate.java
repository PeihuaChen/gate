/*
 *  Constraint Predicate implementation
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
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

import gate.*;
import gate.jape.JapeException;

/**
 * Base class for most {@link ConstraintPredicate}s. Contains standard
 * getters/setters and other routines.
 *
 * @version $Revision$
 * @author esword
 */
public abstract class AbstractConstraintPredicate implements
                                                 ConstraintPredicate {
  protected AnnotationAccessor accessor;
  protected Object value;

  public AbstractConstraintPredicate() {
  }

  public AbstractConstraintPredicate(AnnotationAccessor accessor, Object value) {
    setAccessor(accessor);
    setValue(value);
  }

  public int hashCode() {
    int hashCode = getOperator().hashCode();
    hashCode = 37 * hashCode + ((accessor != null) ? accessor.hashCode() : 0);
    hashCode = 37 * hashCode + ((value != null) ? value.hashCode() : 0);
    return hashCode;
  }

  public boolean equals(Object obj) {
    if(obj == null) return false;
    if(obj == this) return true;
    if(!(this.getClass().equals(obj.getClass()))) return false;

    ConstraintPredicate a = (ConstraintPredicate)obj;

    if(accessor != a.getAccessor() && accessor != null
            && !accessor.equals(a.getAccessor())) return false;

    if(value != a.getValue() && value != null && !value.equals(a.getValue()))
      return false;

    return true;
  }

  public String toString() {
    // If value is a String, quote it. Otherwise (for things like
    // Numbers), don't.
    Object val = getValue();
    if(val instanceof String) val = "\"" + val + "\"";
    return accessor + " " + getOperator() + " " + val;
  }

  public boolean matches(Annotation annot, AnnotationSet context) throws JapeException {
    //get the appropriate value using the accessor and then have
    //concrete subclasses do the eval
    return doMatch(accessor.getValue(annot, context), context);
  }

  protected abstract boolean doMatch(Object value, AnnotationSet context)
          throws JapeException;



  public void setAccessor(AnnotationAccessor accessor) {
    this.accessor = accessor;
  }

  public AnnotationAccessor getAccessor() {
    return accessor;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return value;
  }
}
