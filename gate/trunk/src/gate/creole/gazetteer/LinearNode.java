/*
 * LinearNode.java
 *
 * Copyright (c) 2002, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * borislav popov 02/2002
 *
 */
package gate.creole.gazetteer;

import gate.creole.gazetteer.*;


/**specifies an entry of the type :
 * list:major:minor:language */
public class LinearNode {

  private String list;
  private String minor;
  private String major;
  private String language;

  /**
   * construct a linear node
   * @param aList
   * @param aMajor
   * @param aMinor
   * @param aLanguage
   */
  public LinearNode(String aList,String aMajor,String aMinor, String aLanguage) {
    list = aList;
    minor = aMinor;
    major = aMajor;
    language = aLanguage;
  } // LinearNode construct

  /**
   * parse and create  a linear node from a string
   * @param node
   * @throws InvalidFormatException
   */
  public LinearNode (String node) throws InvalidFormatException  {
    int firstColon = node.indexOf(':');
    int secondColon = node.indexOf(':', firstColon + 1);
    int thirdColon = node.indexOf(':', secondColon + 1);
    if(firstColon == -1){
      throw new InvalidFormatException();
    }
    list = node.substring(0, firstColon);

    if(secondColon == -1){
      major = node.substring(firstColon + 1);
      minor = null;
      language = null;
    } else {
      major = node.substring(firstColon + 1, secondColon);
      if(thirdColon == -1) {
        minor = node.substring(secondColon + 1);
        language = null;
      } else {
        minor = node.substring(secondColon + 1, thirdColon);
        language = node.substring(thirdColon + 1);
      }
    } // else
  } // LinearNode concstruct

  /**get the list of the node
   * @return the list of the node   */
  public String getList() {
    return list;
  }

  /**set the list of the node
   * @param aList the list of the node   */
  public void setList(String aList) {
    list = aList;
  }

  /** get the language of the node (optional)
   *  @return the language of the node */
  public String getLanguage() {
    return language;
  }

  /** set the language of the node
   *  @param aLanguage the language of the node */
  public void setLanguage(String aLanguage) {
    language = aLanguage;
  }

  /** get the minor type
   *  @return the minor type  */
  public String getMinorType() {
    return minor;
  }

  /** set the minor type
   *  @return the minor type */
  public void setMinorType(String minorType) {
    minor = minorType;
  }

  /** get the major type
   *  @return the major type*/
  public String getMajorType() {
    return major;
  }

  /** set the major type
   *  @param majorType the major type */
  public void setMajorType(String majorType) {
    major = majorType;
  }

  public String toString() {
    return list+':'+minor+':'+major;
  }

  /**@param o another node
   * @return true if languages,list,major type and minor type match.*/
  public boolean equals(Object o) {
     boolean result = false;
     if ( o instanceof LinearNode ) {
      LinearNode node = (LinearNode) o;
      result = true;

      if (null != this.getLanguage())
        result &= this.getLanguage().equals(node.getLanguage());

      if ( null != this.getList())
        result &= this.getList().equals(node.getList());

      if ( null!=this.getMajorType())
        result &= this.getMajorType().equals(node.getMajorType());

      if ( null!= this.getMinorType())
        result &= this.getMinorType().equals(node.getMinorType());
     }
     return result;
  }

} // class LinearNode