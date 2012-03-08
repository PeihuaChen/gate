/*
 *  SimpleRelation.java
 *
 *  Copyright (c) 1995-2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 27 Feb 2012
 *
 *  $Id$
 */
package gate.relations;

import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;

/**
 * A simple implementation for the {@link Relation} interface.
 */
public class SimpleRelation implements Relation {
  
  private static final long serialVersionUID = 6866132107461267866L;

  protected String type;
  
  protected int[] members;
  
  protected Serializable userData;
  
  public SimpleRelation(String type, int[] members) {
    super();
    this.type = type;
    this.members = members;
  }
  
  /**
   * De-serialises a value previously produced by calling {@link #toString()}.
   * @param stringSerialisation a {@link String} representation of a 
   * {@link SimpleRelation} value.
   */
  public SimpleRelation(String stringSerialisation) {
    // split at round brackets (unless escaped)
    String[] elems = stringSerialisation.split("(?<!\\\\)(?:\\(|\\))");
    if(elems.length != 2 && elems.length != 3) {
      throw new IllegalArgumentException("Invalid string serialisation (\"" + 
          stringSerialisation +  "\")!");
    }
    this.type = elems[0];
    
    String memStr[] = elems[1].split(",\\s+");
    members = new int[memStr.length];
    for(int  i = 0; i < memStr.length; i++) {
      try{
        members[i] = Integer.parseInt(memStr[i]);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Could not parse input string (\"" +
            memStr[i] + "\" is not a number).");  
      }
    }
    
    if(members.length == 3) {
      // we have user data
      //TODO
    } else {
      userData = null;
    }
  }

  /* (non-Javadoc)
   * @see gate.relations.Relation#getType()
   */
  @Override
  public String getType() {
    return type;
  }

  /* (non-Javadoc)
   * @see gate.relations.Relation#getMembers()
   */
  @Override
  public int[] getMembers() {
    return members;
  }

  /* (non-Javadoc)
   * @see gate.relations.Relation#getUserData()
   */
  @Override
  public Serializable getUserData() {
    return userData;
  }

  /* (non-Javadoc)
   * @see gate.relations.Relation#setUserData(java.io.Serializable)
   */
  @Override
  public void setUserData(Serializable data) {
    this.userData = data;
  }

  /**
   * This implementation is used when dumping relation values to GATE XML. The
   * {@link String} value produced by this method can be parsed by the 
   * {@link #SimpleRelation(String)} constructor to produce an equivalent 
   * instance.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    String typeOut = type.replaceAll("\\(", 
        Matcher.quoteReplacement("\\(")).replaceAll("\\)", 
        Matcher.quoteReplacement("\\)"));
    str.append(typeOut).append("(");
    for(int i = 0; i < members.length; i++) {
      if(i > 0) str.append(", ");
      str.append(members[i]);
    }
    str.append(")");
    if(userData != null) {
      str.append("#");
      //TODO: convert user data to Base64
      str.append("#");
    }
    return str.toString();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(members);
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((userData == null) ? 0 : userData.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null) return false;
    if(!(obj instanceof SimpleRelation)) return false;
    SimpleRelation other = (SimpleRelation)obj;
    if(!Arrays.equals(members, other.members)) return false;
    if(type == null) {
      if(other.type != null) return false;
    } else if(!type.equals(other.type)) return false;
    if(userData == null) {
      if(other.userData != null) return false;
    } else if(!userData.equals(other.userData)) return false;
    return true;
  }
  
  
}
