/*
 * OInstanceImpl.java
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
 * Kalina Bontcheva 03/2003
 *
 *
 *  $Id$
 */

package gate.creole.ontology;

import java.util.*;

public class OInstanceImpl implements OInstance {

  protected Object userData;
  protected OClass instanceClass;
  protected String instanceName;
  protected HashMap instanceProperties;

  public OInstanceImpl(String aName, OClass aClass) {
      instanceName = aName;
      instanceClass = aClass;
      instanceProperties = new HashMap();
    }


  public OClass getOClass() {
    return instanceClass;
  }

  public String getName() {
    return instanceName;
  }

  
  public String toString(){
    return getName() + "(" + getOClass().getName() + ")";
  }


  /** Sets the user data of this instance. To be used to
   * store arbitrary data on instances.
   */
  public void setUserData(Object theUserData){
    userData = theUserData;
  }

  /** Gets the user data of this instance.
   *  @return the object which is user data
   */
  public Object getUserData(){
    return userData;
  }

  public void setDifferentFrom(OInstance theIndividual){
    System.out.println("setDifferentFrom not supported yet");
  }

  public Set getDifferentFrom(){
    System.out.println("getDifferentFrom not supported yet");
    return null;
  }

  
  public boolean addPropertyValue(String propertyName, Object theValue){
    //this means that we look for a property with the same name 
    //in the class. If such cannot be found, i.e. the propSet is 
    //is empty, then we just return without adding the value
    Set propSet = instanceClass.getPropertiesByName(propertyName);
    if (propSet == null || propSet.isEmpty()) return false;
    
    List values = (List)instanceProperties.get(propertyName);
    if(values == null){
      values = new ArrayList();
      instanceProperties.put(propertyName, values);
    }
    values.add(theValue);
    return true;
  }


  public List getPropertyValues(String propertyName){
    return (List)instanceProperties.get(propertyName);
  }
  
  public boolean removePropertyValue(String propertyName, Object theValue){
    List values = (List)instanceProperties.get(propertyName);
    if(values != null){
      return values.remove(theValue);
    }else return false;
  }


  public void removePropertyValues(String propertyName){
    instanceProperties.remove(propertyName);
  }

  public void setPropertyValue(String propertyName, Object theValue){
    if (propertyName == null || instanceClass == null)
      
      return;
    //this means that we look for a property with the same name 
    //in the class. If such cannot be found, i.e. the propSet is 
    //is empty, then we just return without adding the value
    Set propSet = instanceClass.getPropertiesByName(propertyName);
    if (propSet == null || propSet.isEmpty())
      return;
    this.instanceProperties.put(propertyName, theValue);
  }

  public Object getPropertyValue(String propertyName){
    if (instanceProperties == null || instanceProperties.isEmpty())
      return null;
    return instanceProperties.get(propertyName);
  }

  public void setSameIndividualAs(OInstance theIndividual){
    System.out.println("setSameIndividualAs not supported yet");
  }

  public Set getSameIndividualAs(){
    System.out.println("getSameIndividualAs not supported yet");
    return null;
  }


}