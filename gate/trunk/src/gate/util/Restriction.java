/*
 *  Restriction.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Rosen Marinov, 10/Dec/2001
 *
 *  $Id$
 */

package gate.util;

public class Restriction {

  /* Type of operator for cmarision in query*/
  public static final int OPERATOR_EQUATION = 100;
  public static final int OPERATOR_LESS = 101;
  public static final int OPERATOR_BIGGER = 102;
  public static final int OPERATOR_EQUATION_OR_BIGGER = 103;
  public static final int OPERATOR_EQUATION_OR_LESS = 104;

  private Object value;
  private String key;
  private int    operator_;

  /** --- */
  public Restriction(String key, Object value, int operator_){
    this.key = key;
    this.value = value;
    this.operator_ = operator_;
  }

  /** --- */
  public Object getValue(){
    return value;
  }

  /** --- */
  public String getStringValue(){
    return value.toString();
  }

  /** --- */
  public String getKey(){
    return key;
  }

  /** --- */
  public int getOperator(){
    return operator_;
  }
}