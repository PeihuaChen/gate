package com.ontotext.gate.ontology;

import gate.creole.ontology.*;
import java.util.List;
import java.net.URL;
import gate.creole.ResourceInstantiationException;
import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;
import gate.DataStore;
import gate.persist.PersistenceException;
import gate.security.SecurityException;
import gate.LanguageResource;
import gate.Resource;
import gate.FeatureMap;

import java.util.*;
import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.arp.*;


public class OntologyImpl extends TaxonomyImpl implements Ontology {

  private static final boolean DEBUG = false;

  private Map instancesByName = new HashMap();
  private List instances = new ArrayList();
  private Set propertyDefinitionSet = new HashSet();

  public OInstance addInstance(String name, OClass theClass) {
    if (instancesByName.containsKey(name))
      return (OInstance) instancesByName.get(name);
    OInstance newInstance = new OInstanceImpl(name, theClass);
    instancesByName.put(name, newInstance);
    instances.add(newInstance);
    fireObjectModificationEvent(this);
    return newInstance;
  }

  public void addInstance(OInstance theInstance) {
    if (instancesByName.containsKey(theInstance.getName()))
      return;
    instancesByName.put(theInstance.getName(), theInstance);
    instances.add(theInstance);
    fireObjectModificationEvent(this);
  }

  public void removeInstance(OInstance theInstance) {
    if (! instancesByName.containsKey(theInstance.getName()))
      return;
    instancesByName.remove(theInstance.getName());
    instances.remove(theInstance);
  }

  public List getInstances() {
    return instances;
  }

  public List getInstances(OClass aClass) {
    List theInstances = new ArrayList();
    Set subClasses;
    try {
      subClasses = aClass.getSubClasses(OClass.TRANSITIVE_CLOSURE);
    } catch (NoSuchClosureTypeException ex){
      subClasses = new HashSet();
    }

    //iterate through all instances and only include those
    //that either have the same class or their class is a subclass
    //of the given class; not an efficient implementation but fine for now
    for (int i=0; i< instances.size(); i++) {
      OClass theClass = ((OInstance)instances.get(i)).getOClass();
      if (theClass.equals(aClass) || subClasses.contains(theClass))
        theInstances.add(instances.get(i));
    }//for
    return theInstances;
  }

  public List getDirectInstances(OClass aClass) {
    List theInstances = new ArrayList();
    //iterate through all instances and only include those
    //that have the same class; not an efficient implementation but fine for now
    for (int i=0; i< instances.size(); i++) {
      OClass theClass = ((OInstance)instances.get(i)).getOClass();
      if (theClass.equals(aClass))
        theInstances.add(instances.get(i));
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

  public DatatypeProperty addDatatypeProperty(String name, OClass domain, String value){
    DatatypeProperty theProperty =
      new DatatypePropertyImpl(name, domain, value, this);
    ((OClassImpl)domain).addProperty(theProperty);
    return theProperty;
  }

  public DatatypeProperty addDatatypeProperty(String name, OClass domain, Number value){
    DatatypeProperty theProperty =
      new DatatypePropertyImpl(name, domain, value, this);
    ((OClassImpl)domain).addProperty(theProperty);
    return theProperty;
  }

  public FunctionalProperty addFunctionalProperty(String name, OClass domain, Object range){
    System.out.println("Functional properties not supported yet");
    return null;
  }

  public ObjectProperty addObjectProperty(String name, OClass domain, OClass range){
    ObjectProperty theProperty =
      new ObjectPropertyImpl(name, domain, range, this);
    ((OClassImpl)domain).addProperty(theProperty);
    return theProperty;
  }

  public SymmetricProperty addSymmetricProperty(String name, OClass domain, OClass range){
    System.out.println("Symmetric properties not supported yet");
    return null;
  }

  public TransitiveProperty addTransitiveProperty(OClass domain, OClass range){
    System.out.println("Transitive properties not supported yet");
    return null;
  }

  public void addPropertyDefinition(gate.creole.ontology.Property theProperty) {
    this.propertyDefinitionSet.add(theProperty);
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