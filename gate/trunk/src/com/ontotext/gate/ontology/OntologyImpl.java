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
    if(instancesByName.containsKey(name))
      return (OInstance)instancesByName.get(name);
    OInstance newInstance = new OInstanceImpl(name, null, theClass, this);
    instancesByName.put(name, newInstance);
    instances.add(newInstance);
    setModified(true);
    fireOntologyResourceAdded(newInstance);
    return newInstance;
  }

  public OInstance addInstance(OInstance theInstance) {
    if(instancesByName.containsKey(theInstance.getName())) return theInstance;
    instancesByName.put(theInstance.getName(), theInstance);
    instances.add(theInstance);
    setModified(true);
    fireOntologyResourceAdded(theInstance);
    return theInstance;
  }

  public void removeInstance(OInstance theInstance) {
    if(!instancesByName.containsKey(theInstance.getName())) return;
    instancesByName.remove(theInstance.getName());
    instances.remove(theInstance);
    setModified(true);
    fireOntologyResourceRemoved(theInstance);
  }

  /**
   * To Remove an existing property definition
   */
  public void removePropertyDefinition(Property property) {
    if(getPropertyDefinitions().remove(property)) {
      setModified(true);
      fireOntologyResourceRemoved(property);
      return;
    }
  }

  public Set getInstances() {
    return instances;
  }

  public Set getInstances(OClass aClass) {
    Set theInstances = getDirectInstances(aClass);
    Iterator classIter = aClass.getSuperClasses(OClass.TRANSITIVE_CLOSURE)
            .iterator();
    while(classIter.hasNext())
      theInstances.addAll(getDirectInstances((OClass)classIter.next()));
    return theInstances;
  }

  public Set getDirectInstances(OClass aClass) {
    Set theInstances = new HashSet();
    // iterate through all instances and only include those
    // that either have the same class or their class is a subclass
    // of the given class; not an efficient implementation but fine for
    // now
    Iterator instIter = instances.iterator();
    while(instIter.hasNext()) {
      OInstance anInstance = (OInstance)instIter.next();
      if(anInstance.getOClasses().contains(aClass))
        theInstances.add(anInstance);
    }// for
    return theInstances;
  }

  public OInstance getInstanceByName(String aName) {
    return (OInstance)instancesByName.get(aName);
  }

  public TClass createClass(String aName, String aComment) {
    TClass theClass = new OClassImpl(Long.toString(++lastGeneratedId), aName,
            aComment, this);
    addClass(theClass);
    nullBuffers = true;
    return theClass;
  }

  public DatatypeProperty addDatatypeProperty(String name, String comment,
          Set domain, Class range) {
    DatatypeProperty theProperty = new DatatypePropertyImpl(name, comment,
            domain, range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public DatatypeProperty addDatatypeProperty(String name, String comment,
          OClass domain, Class range) {
    DatatypeProperty theProperty = new DatatypePropertyImpl(name, comment,
            domain, range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public DatatypeProperty addDatatypeProperty(DatatypeProperty property) {
    addPropertyDefinition(property);
    return property;
  }

  public Property addProperty(String name, String comment, Set domain, Set range) {
    Property theProperty = new PropertyImpl(name, comment, domain, range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public Property addProperty(String name, String comment, OClass domain,
          Class range) {
    Property theProperty = new PropertyImpl(name, comment, domain, range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public Property addProperty(Property property) {
    addPropertyDefinition(property);
    return property;
  }

  public ObjectProperty addObjectProperty(String name, String comment,
          Set domain, Set range) {
    ObjectProperty theProperty = new ObjectPropertyImpl(name, comment, domain,
            range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public ObjectProperty addObjectProperty(String name, String comment,
          OClass domain, OClass range) {
    ObjectProperty theProperty = new ObjectPropertyImpl(name, comment, domain,
            range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public ObjectProperty addObjectProperty(ObjectProperty property) {
    addPropertyDefinition(property);
    return property;
  }

  public SymmetricProperty addSymmetricProperty(String name, String comment,
          Set domain, Set range) {
    SymmetricProperty theProperty = new SymmetricPropertyImpl(name, comment,
            domain, range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public SymmetricProperty addSymmetricProperty(String name, String comment,
          OClass domain, OClass range) {
    SymmetricProperty theProperty = new SymmetricPropertyImpl(name, comment,
            domain, range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public SymmetricProperty addSymmetricProperty(SymmetricProperty property) {
    addPropertyDefinition(property);
    return property;
  }

  public TransitiveProperty addTransitiveProperty(String name, String comment,
          Set domain, Set range) {
    TransitiveProperty theProperty = new TransitivePropertyImpl(name, comment,
            domain, range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public TransitiveProperty addTransitiveProperty(String name, String comment,
          OClass domain, OClass range) {
    TransitiveProperty theProperty = new TransitivePropertyImpl(name, comment,
            domain, range, this);
    theProperty.setURI(getDefaultNameSpace() + name);
    addPropertyDefinition(theProperty);
    return theProperty;
  }

  public TransitiveProperty addTransitiveProperty(TransitiveProperty property) {
    addPropertyDefinition(property);
    return property;
  }

  protected void addPropertyDefinition(gate.creole.ontology.Property theProperty) {
    this.propertyDefinitionSet.add(theProperty);
    setModified(true);
    fireOntologyResourceAdded(theProperty);
  }

  public Set getPropertyDefinitions() {
    return this.propertyDefinitionSet;
  }

  public gate.creole.ontology.Property getPropertyDefinitionByName(String name) {
    if(name == null) return null;
    Iterator iter = this.propertyDefinitionSet.iterator();
    while(iter.hasNext()) {
      gate.creole.ontology.Property theProperty = (gate.creole.ontology.Property)iter
              .next();
      if(name.equals(theProperty.getName())) return theProperty;
    }
    return null;
  }

  /**
   * Eliminates the more general classes from a set, keeping only the most
   * specific ones. The changes are made to the set provided as a parameter.
   * This is a utility method for ontologies.
   * 
   * @param classSet
   *          a set of {@link OClass} objects.
   */
  public static void reduceToMostSpecificClasses(Set classSet) {
    Map superClassesForClass = new HashMap();
    for(Iterator classIter = classSet.iterator(); classIter.hasNext();) {
      Object aGateClassValue = classIter.next();
      if(!(aGateClassValue instanceof OClass)) continue;
      OClass aGateClass = (OClass)aGateClassValue;
      superClassesForClass.put(aGateClass, aGateClass
              .getSuperClasses(OClass.TRANSITIVE_CLOSURE));
    }
    Set classesToRemove = new HashSet();
    List resultList = new ArrayList(classSet);
    for(int i = 0; i < resultList.size() - 1; i++)
      for(int j = i + 1; j < resultList.size(); j++) {
        OClass aClass = (OClass)resultList.get(i);
        OClass anotherClass = (OClass)resultList.get(j);
        if(((Set)superClassesForClass.get(aClass)).contains(anotherClass))
          classesToRemove.add(anotherClass);
        else if(((Set)superClassesForClass.get(anotherClass)).contains(aClass))
          classesToRemove.add(aClass);
      }
    classSet.removeAll(classesToRemove);
  }

  /**
   * This method is invoked whenever a resource in ontology is modified
   */
  public void ontologyModified(OntologyModificationEvent ome) {
    // first we call the super ontology Modified method
    super.ontologyModified(ome);
    if(ome.getEventType() == OntologyModificationEvent.ONTOLOGY_RESOURCE_REMOVED) {
      Ontology ontology = (Ontology)ome.getSource();
      // if the deleted resource is an instanceof OClass
      if(ome.getResource() instanceof OClass) {
        OClass deleted = (OClass)ome.getResource();
        // delete all disjoint entries
        Set disjointClasses = deleted.getDisjointClasses();
        if(disjointClasses != null) {
          Iterator iter = disjointClasses.iterator();
          while(iter.hasNext()) {
            OClass disjointClass = (OClass)iter.next();
            disjointClass.getDisjointClasses().remove(deleted);
          }
        }
        // delete all sameas entries
        Set sameAsClasses = deleted.getSameClasses();
        if(sameAsClasses != null) {
          Iterator iter = sameAsClasses.iterator();
          while(iter.hasNext()) {
            OClass sameAsClass = (OClass)iter.next();
            sameAsClass.getSameClasses().remove(deleted);
          }
        }
        // delete entry of the current class from all its super classes
        // done in super.ontologyModified Method
        // delete all its subclasses
        // done in super.ontologyModified method
        // delete all its instances
        Set instances = ontology.getInstances(deleted);
        if(instances != null) {
          Iterator iter = instances.iterator();
          while(iter.hasNext()) {
            OInstance instance = (OInstance)iter.next();
            ontology.removeInstance(instance);
          }
        }
        // we need to go through all properties check their domain and range
        // if properties have this class registered as one of them
        // the properties should be deleted
        Set properties = ontology.getPropertyDefinitions();
        if(properties != null) {
          ArrayList toDelete = new ArrayList();
          Iterator iter = properties.iterator();
          while(iter.hasNext()) {
            Property p = (Property)iter.next();
            Set domain = p.getDomain();
            if(domain != null) {
              if(domain.contains(deleted)) {
                toDelete.add(p);
                continue;
              }
            }
            if(p instanceof ObjectProperty) {
              Set range = p.getRange();
              if(range.contains(deleted)) {
                toDelete.add(p);
                continue;
              }
            }
          }
          for(int i = 0; i < toDelete.size(); i++) {
            Property p = (Property)toDelete.get(i);
            ontology.removePropertyDefinition(p);
          }
        }
      } else if(ome.getResource() instanceof OInstance) {
        // we need to go though all ontology resources of the ontology
        // check if they have property with value the current resource
        // we need to delete it
        // get classes = done in super.ontologyModified
        // get instances = done below
        Set allInstances = ontology.getInstances();
        if(allInstances != null) {
          Iterator iter = allInstances.iterator();
          while(iter.hasNext()) {
            OInstance anInstance = (OInstance)iter.next();
            Set setPropertyNames = anInstance.getSetPropertiesNames();
            if(setPropertyNames != null) {
              Iterator subIter = setPropertyNames.iterator();
              while(subIter.hasNext()) {
                String property = (String)subIter.next();
                anInstance.removePropertyValue(property, ome.getResource());
              }
            }
          }
        }
      } else if(ome.getResource() instanceof Property) {
        // need to remove the deleted property from all instances
        Property deleted = (Property)ome.getResource();
        Set allInstances = ontology.getInstances();
        if(allInstances != null) {
          Iterator iter = allInstances.iterator();
          while(iter.hasNext()) {
            OInstance anInstance = (OInstance)iter.next();
            anInstance.removePropertyValues(deleted.getName());
          }
        }
        Set sameAsProperties = deleted.getSamePropertyAs();
        if(sameAsProperties != null) {
          Iterator iter = sameAsProperties.iterator();
          while(iter.hasNext()) {
            Property sameAsProperty = (Property)iter.next();
            sameAsProperty.getSamePropertyAs().remove(deleted);
          }
        }
        Set superProperties = deleted
                .getSuperProperties(Property.DIRECT_CLOSURE);
        if(superProperties != null) {
          Iterator iter = superProperties.iterator();
          while(iter.hasNext()) {
            Property superProperty = (Property)iter.next();
            superProperty.removeSubProperty(deleted);
          }
        }
        Set subProperties = deleted.getSubProperties(Property.DIRECT_CLOSURE);
        if(subProperties != null) {
          Iterator iter = subProperties.iterator();
          while(iter.hasNext()) {
            Property subProperty = (Property)iter.next();
            ontology.removePropertyDefinition(subProperty);
          }
        }
      }
    }
  }
}
