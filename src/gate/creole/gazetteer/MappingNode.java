/*
 * MappingNode.java
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



/**represents a single node of the mapping definition*/
public class MappingNode{

  private String list;
  private String classID;
  private String ontologyID;

  /**create a new mapping node
   * @param node a node from the mapping definition
   * @throws InvalidFormatException
   */
  public MappingNode(String node) throws InvalidFormatException {
    int firstColumn = node.indexOf(':');
    int lastColumn = node.lastIndexOf(':');
    if (-1 == firstColumn || -1 == lastColumn ) {
      throw new InvalidFormatException();
    }
    list = node.substring(0,firstColumn);
    ontologyID = node.substring(firstColumn+1,lastColumn);
    classID = node.substring(lastColumn+1);
  }// MappingNode construct

  /**create a new mapping node
   * @param aList
   * @param anOntologyID
   * @param aClassID
   */
  public MappingNode(String aList, String anOntologyID,String aClassID) {
    list = aList;
    classID = aClassID;
    ontologyID = anOntologyID;
  }

  /**set list to the node
   * @param aList the list */
  public void setList(String aList) {
    list = aList;
  }

  /** get the list of the node
   *  @return the list */
  public String getList(){
    return list;
  }

  /** set the class ID {? maybe the class name }
   * @param theClassID  the class id */
  public void setClassID(String theClassID) {
    classID = theClassID;
  }

  /** get the class id
   *  @returm the class id  */
  public String getClassID(){
    return classID;
  }

  /** set the ontology id
   *  @param id the ontology id   */
  public void setOntologyID(String id) {
    ontologyID = id;
  }

  /** get the ontology id
   *  @return the ontology id  */
  public String getOntologyID(){
    return ontologyID;
  }

  public String toString() {
    return list + ":" + ontologyID + ":" + classID;
  }
} // class MappingNode