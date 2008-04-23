/*
 *  Ontology2Map.java
 *
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 */
package gate.clone.ql.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gate.clone.ql.query.serql.SerqlUtils;
import gate.creole.ontology.Ontology;

/**
 * 
 * @author Danica Damljanovic
 * 
 * This class serves to cache all data we retrieve from the ontology and is
 * singlenton.
 */
public class Ontology2Map {
  /* reference to the only instance of this class*/
  static private Ontology2Map myInstance;
  
  // read ontology and store data
 
  /* String is instanceURI, and Set is the list i.e. set of classURIs */
  static protected Map<String, Set<String>> instanceTypes =
    new HashMap<String, Set<String>>();
  
  static protected String listOfClasses;
  
  static private String listOfInstances;
  
  static private String listOfProperties;
  
  static private String classURIs;
  
  static private String propertyURIs;

  private Ontology2Map() {
  }

  public void reInit(Ontology o) {
    SerqlUtils.init(o);
    // for OntoRoot Gazetteer
    instanceTypes = StringUtil.fromStringToMap(SerqlUtils.getInstanceTypes());
    listOfClasses = SerqlUtils.getClasses();
    listOfProperties = SerqlUtils.getPropertiesOfProperties();
    listOfInstances = SerqlUtils.getInstances();
    classURIs = SerqlUtils.getClassURIs();
    propertyURIs = SerqlUtils.getPropertyURIs();
  }

  public static Ontology2Map getInstance() {
    if(myInstance == null) myInstance = new Ontology2Map();
    return myInstance;
  }

  public Map<String, Set<String>> getInstanceTypes() {
    return instanceTypes;
  }

  public void setInstanceTypes(Map<String, Set<String>> instanceTypes) {
    Ontology2Map.instanceTypes = instanceTypes;
  }

  public String getListOfClasses() {
    return listOfClasses;
  }

  public String getListOfProperties() {
    return listOfProperties;
  }

  public String getClassURIs() {
    return classURIs;
  }

  public String getPropertyURIs() {
    return propertyURIs;
  }

  public Ontology2Map getMyInstance() {
    return myInstance;
  }

  public void setMyInstance(Ontology2Map myInstance) {
    Ontology2Map.myInstance = myInstance;
  }

  public String getListOfInstances() {
    return listOfInstances;
  }

}
