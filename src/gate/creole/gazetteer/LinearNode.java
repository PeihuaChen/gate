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


/**Linear node specifies an entry of the type :
 * list:major:minor:language */
public class LinearNode {

  /** the gazetteer list from the node */
  private String list;
  /** the minor type from the node */
  private String minor;
  /** the major type from the node */
  private String major;
  /** the languages member from the node */
  private String language;

  /**
   * Constructs a linear node given its elements
   * @param aList the gazetteer list file name
   * @param aMajor the major type
   * @param aMinor the minor type
   * @param aLanguage the language(s)
   */
  public LinearNode(String aList,String aMajor,String aMinor, String aLanguage) {
    list = aList;
    minor = aMinor;
    major = aMajor;
    language = aLanguage;
  } // LinearNode construct

  /**
   * Parses and create a linear node from a string
   * @param node the linear node to be parsed
   * @throws InvalidFormatException
   */
  public LinearNode (String node) throws InvalidFormatException  {
    int firstColon = node.indexOf(':');
    int secondColon = node.indexOf(':', firstColon + 1);
    int thirdColon = node.indexOf(':', secondColon + 1);
    if(firstColon == -1){
      throw new InvalidFormatException("", "Line: " + node);
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

  /**Get the gazetteer list filename from the node
   * @return the gazetteer list filename */
  public String getList() {
    return list;
  }

  /**Sets the gazetteer list filename for the node
   * @param aList  the gazetteer list filename*/
  public void setList(String aList) {
    list = aList;
  }

  /** Gets the language of the node (the language is optional)
   *  @return the language of the node */
  public String getLanguage() {
    return language;
  }

  /** Sets the language of the node
   *  @param aLanguage the language of the node */
  public void setLanguage(String aLanguage) {
    language = aLanguage;
  }

  /** Gets the minor type
   *  @return the minor type  */
  public String getMinorType() {
    return minor;
  }

  /** Sets the minor type
   *  @return the minor type */
  public void setMinorType(String minorType) {
    minor = minorType;
  }

  /** Gets the major type
   *  @return the major type*/
  public String getMajorType() {
    return major;
  }

  /** Sets the major type
   *  @param majorType the major type */
  public void setMajorType(String majorType) {
    major = majorType;
  }

  /**
   * Gets the string representation of this node
   * @return the string representation of this node
   */
  public String toString() {
    String result = list+':'+major;

    if ( (null!=minor)  && (0 != minor.length()))
      result += ':'+minor;

    if ( (null!=language) && (0 != language.length())) {
      if ((null==minor) || (0 == minor.length()) )
        result +=':';
      result += ':'+language;
    }
    return result;
  }

  /**Checks this node vs another one for equality.
   * @param o another node
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