/*
 *  Copyright (c) 2009, Valentin Tablan.
 *
 *  Predicate.java
 *
 *  Valentin Tablan, 4 Aug 2009
 *
 *  $Id$
 */

package gate.jape.plus;

import java.util.regex.Pattern;

/**
 * An atomic predicate is a single test (that cannot be broken into sub-tests)
 */
public class Predicate{

  public static enum PredicateType{
    EQ,
    NOT_EQ,
    LT,
    GT,
    LE,
    GE,
    REGEX_FIND,
    REGEX_MATCH,
    REGEX_NOT_FIND,
    REGEX_NOT_MATCH,
    CONTAINS,
    WITHIN
  }

  
  /**
   * The annotation feature this predicate refers to.
   */
  protected String featureName;
  
  /**
   * The desired value for the feature. The only allowed types are 
   * {@link String}, {@link Long}, {@link Double}, or {@link Pattern}!
   */
  protected Object featureValue;
  
  /**
   * The type of this predicate (i.e. which test it refers to).
   */
  protected PredicateType type;

  
  /**
   * A set of predicates (for the same annotation type) that are also true
   * if this predicate is true.
   */
  protected int[] alsoTrue;
  
  /**
   * A set of predicates (for the same annotation type) that are also false 
   * if this predicate is false.
   */
  protected int[] alsoFalse;
  
  /**
   * A set of predicates (for the same annotation type) that are true 
   * when this predicate is false.
   */    
  protected int[] converselyTrue;
  
  /**
   * A set of predicates (for the same annotation type) that are false 
   * when this predicate is true.
   */
  protected int[] converselyFalse;

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
      prime * result + ((featureName == null) ? 0 : featureName.hashCode());
    result =
      prime * result + ((featureValue == null) ? 0 : featureValue.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    Predicate other = (Predicate)obj;
    if(featureName == null) {
      if(other.featureName != null) return false;
    } else if(!featureName.equals(other.featureName)) return false;
    if(featureValue == null) {
      if(other.featureValue != null) return false;
    } else if(!featureValue.equals(other.featureValue)) return false;
    if(type == null) {
      if(other.type != null) return false;
    } else if(!type.equals(other.type)) return false;
    return true;
  }
}
