/*
 * OntologyImpl.java
 * Copyright:    Copyright (c) 2001, OntoText Lab.
 * Company:      OntoText Lab.
 * borislav popov 02/2002 */
package com.ontotext.gate.ontology;

import gate.creole.ontology.*;
import java.util.*;

/**
 * An Ontology Implementation Class
 * 
 * @author borislav popov
 * @author Kalina Bontcheva
 * @author Valentin Tablan
 */
public class OntologyImpl extends TaxonomyImpl implements Ontology {

  private static final boolean DEBUG = false;

  protected Map instancesByName = new HashMap();
  protected Set instances = new HashSet();
  protected Set propertyDefinitionSet = new HashSet();

  public OInstance addInstance(String name, OClass theClass) {
    if (instancesByName.containsKey(name))
      return (OInstance) instancesByName.get(name);
    OInstance newInstance = new OInstanceImpl(name, null, theClass, this);
    instancesByName.put(name, newInstance);
    instances.add(newInstance);
    setModified(true);
    fireObjectModificationEvent(this);
    return newInstance;
  }

  public void addInstance(OInstance theInstance) {
    if (instancesByName.containsKey(theInstance.getName()))
      return;
    instancesByName.put(theInstance.getName(), theInstance);
    instances.add(theInstance);
    setModified(true);
    fireObjectModificationEvent(this);
  }

  public void removeInstance(OInstance theInstance) {
    if (! instancesByName.containsKey(theInstance.getName()))
      return;
    instancesByName.remove(theInstance.getName());
    instances.remove(theInstance);
  }

  public Set getInstances() {
    return instances;
  }

  public Set getInstances(OClass aClass) {
    Set theInstances = getDirectInstances(aClass);
    Iterator classIter = aClass.
      getSuperClasses(OClass.TRANSITIVE_CLOSURE).iterator();
    while(classIter.hasNext())
      theInstances.addAll(getDirectInstances((OClass)classIter.next()));
    return theInstances;
  }

  public Set getDirectInstances(OClass aClass) {
    Set theInstances = new HashSet();

    //iterate through all instances and only include those
    //that either have the same class or their class is a subclass
    //of the given class; not an efficient implementation but fine for now
    Iterator instIter = instances.iterator();
    while(instIter.hasNext()) {
      OInstance anInstance = (OInstance)instIter.next(); 
      if(anInstance.getOClasses().contains(aClass)) 
        theInstances.add(anInstance);
    }//for
    return theInstances;
  }

  public OInstance getInstanceByName(String aName) {
    return (OInstance) instancesByName.get(aName);
  }


  public TClass createClass(String aName, String aComment) {
    this.modified = true;
    TClass theClass
      = new OClassImpl(Long.toString(++lastGeneratedId),aName,aComment,this);
    addClass(theClass);
    nullBuffers = true;
    fireObjectModificationEvent(this);
    return theClass;
  }

  public DatatypeProperty addDatatypeProperty(String name, String comment, 
          Set domain, Class range) {
    DatatypeProperty theProperty = new DatatypePropertyImpl(name, 
            comment, domain, this);
    addPropertyDefinition(theProperty);
    return theProperty;
  }
    
  public DatatypeProperty addDatatypeProperty(String name, String comment, 
          OClass domain, Class range){
    DatatypeProperty theProperty = new DatatypePropertyImpl(name, 
            comment, domain, this);
    addPropertyDefinition(theProperty);
    return theProperty;
  }


  public ObjectProperty addObjectProperty(String name, String comment, 
          Set domain, Set range) {
    ObjectProperty theProperty = new ObjectPropertyImpl(name, comment, domain, 
            range, this);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public ObjectProperty addObjectProperty(String name, String comment, 
          OClass domain, OClass range){
    ObjectProperty theProperty = new ObjectPropertyImpl(name, comment, domain, 
            range, this);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public SymmetricProperty addSymmetricProperty(String name, String comment, 
          Set domain, Set range) {
    SymmetricProperty theProperty = new SymmetricPropertyImpl(name, comment, 
            domain, range, this);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public SymmetricProperty addSymmetricProperty(String name, String comment, 
          OClass domain, OClass range){
    SymmetricProperty theProperty = new SymmetricPropertyImpl(name, comment, 
            domain, range, this);
    addPropertyDefinition(theProperty);
    return theProperty;
  }  

  public TransitiveProperty addTransitiveProperty(String name, String comment, 
          Set domain, Set range) {
    TransitiveProperty theProperty = new TransitivePropertyImpl(name, comment, 
            domain, range, this);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public TransitiveProperty addTransitiveProperty(String name, String comment, 
          OClass domain, OClass range){
    TransitiveProperty theProperty = new TransitivePropertyImpl(name, comment, 
            domain, range, this);
    addPropertyDefinition(theProperty);
    return theProperty;
  }  
  
  protected void addPropertyDefinition(gate.creole.ontology.Property theProperty) {
    this.propertyDefinitionSet.add(theProperty);
    setModified(true);
  }

  public Set getPropertyDefinitions() {
    return this.propertyDefinitionSet;
  }

  public gate.creole.ontology.Property getPropertyDefinitionByName(String name){
    if (name == null)
      return null;
    Iterator iter = this.propertyDefinitionSet.iterator();
    while (iter.hasNext()) {
      gate.creole.ontology.Property theProperty = (gate.creole.ontology.Property) iter.next();
      if (name.equals(theProperty.getName()))
        return theProperty;
    }
    return null;
  }

}