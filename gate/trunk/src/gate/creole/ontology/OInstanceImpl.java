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

import java.util.Set;

public class OInstanceImpl implements OInstance {

  protected Object userData;
  protected OClass instanceClass;
  protected String instanceName;

  public OInstanceImpl(String aName, OClass aClass) {
      instanceName = aName;
      instanceClass = aClass;
    }


  public OClass getOClass() {
    return instanceClass;
  }

  public String getName() {
    return instanceName;
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

  public void setPropertyValue(Property theProperty, Object theValue){
    System.out.println("setPropertyValue not supported yet");
  }

  public Object getPropertyValue(Property theProperty){
    System.out.println("getPropertyValue not supported yet");
    return null;
  }

  public void setSameIndividualAs(OInstance theIndividual){
    System.out.println("setSameIndividualAs not supported yet");
  }

  public Set getSameIndividualAs(){
    System.out.println("getSameIndividualAs not supported yet");
    return null;
  }


}