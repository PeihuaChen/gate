/*
 *  RDFPropertyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: RDFPropertyImpl.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology.owlim;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import gate.creole.ontology.AnnotationProperty;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.URI;
import gate.util.GateRuntimeException;

/**
 * Implementation of the RDFProperty
 * 
 * @author niraj
 * 
 */
public class RDFPropertyImpl extends OResourceImpl implements RDFProperty {
  /**
   * Constructor
   * 
   * @param aURI
   * @param repositoryID
   * @param owlimPort
   */
  public RDFPropertyImpl(URI aURI, Ontology ontology, String repositoryID,
          OWLIM owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#setSamePropertyAs(gate.creole.ontology.RDFProperty)
   */
  public void setEquivalentPropertyAs(RDFProperty theProperty) {
    try {
      if(this == theProperty) {
        Utils
                .warning("setEquivalentPropertyAs(RDFProperty) : The source and the argument properties refer to the same property and therefore cannot be set as equivalent");
        return;
      }

      owlim.setEquivalentPropertyAs(repositoryID, uri.toString(), theProperty
              .getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.EQUIVALENT_PROPERTY_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getSamePropertyAs()
   */
  public Set<RDFProperty> getEquivalentPropertyAs() {
    try {
      Property[] properties = owlim.getEquivalentPropertyAs(repositoryID, uri
              .toString());
      Set<RDFProperty> set = new HashSet<RDFProperty>();
      for(int i = 0; i < properties.length; i++) {
        set.add(Utils.createOProperty(this.repositoryID, this.ontology,
                this.owlim, properties[i].getUri(), properties[i].getType()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isSamePropertyAs(gate.creole.ontology.RDFProperty)
   */
  public boolean isEquivalentPropertyAs(RDFProperty theProperty) {
    try {
      return owlim.isEquivalentPropertyAs(this.repositoryID, uri.toString(),
              theProperty.getURI().toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getSuperProperties(byte)
   */
  public Set<RDFProperty> getSuperProperties(byte closure) {
    try {
      Property[] properties = owlim.getSuperProperties(repositoryID, uri
              .toString(), closure);
      Set<RDFProperty> set = new HashSet<RDFProperty>();
      for(int i = 0; i < properties.length; i++) {
        set.add(Utils.createOProperty(this.repositoryID, this.ontology,
                this.owlim, properties[i].getUri(), properties[i].getType()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isSuperPropertyOf(gate.creole.ontology.RDFProperty,
   *      byte)
   */
  public boolean isSuperPropertyOf(RDFProperty theProperty, byte closure) {
    try {
      return owlim.isSuperPropertyOf(this.repositoryID, uri.toString(),
              theProperty.getURI().toString(), closure);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#addSubProperty(gate.creole.ontology.RDFProperty)
   */
  public void addSubProperty(RDFProperty theProperty) {
    try {

      // lets first check if the current class is a subclass of the
      // subClass. If so,
      // we don't allow this.
      if(this == theProperty) {
        Utils
                .warning("addSubProperty(RDFProperty) : The super and sub properties are same.");
        return;
      }

      if(this.isSubPropertyOf(theProperty, OConstants.TRANSITIVE_CLOSURE)) {
        Utils.warning(theProperty.getURI().toString()
                + " is a super property of " + this.getURI().toString());
        return;
      }

      if(!(this.getClass().getName().equals(theProperty.getClass().getName()))) {
        Utils.warning(this.getURI().toString() + " and " + theProperty.getURI().toString()
                + " must be of the same property type " + this.getURI().toString());
        return;
      }
      
      owlim.addSubProperty(this.repositoryID, uri.toString(), theProperty
              .getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.SUB_PROPERTY_ADDED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#removeSubProperty(gate.creole.ontology.RDFProperty)
   */
  public void removeSubProperty(RDFProperty theProperty) {
    try {
      owlim.removeSubProperty(this.repositoryID, uri.toString(), theProperty
              .getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.SUB_PROPERTY_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getSubProperties(byte)
   */
  public Set<RDFProperty> getSubProperties(byte closure) {
    try {
      Property[] properties = owlim.getSubProperties(repositoryID, uri
              .toString(), closure);
      Set<RDFProperty> set = new HashSet<RDFProperty>();
      for(int i = 0; i < properties.length; i++) {
        set.add(Utils.createOProperty(this.repositoryID, this.ontology,
                this.owlim, properties[i].getUri(), properties[i].getType()));
      }
      return set;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isSubPropertyOf(gate.creole.ontology.RDFProperty,
   *      byte)
   */
  public boolean isSubPropertyOf(RDFProperty theProperty, byte closure) {
    try {
      return owlim.isSubPropertyOf(this.repositoryID, theProperty.getURI()
              .toString(), uri.toString(), closure);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isFunctional()
   */
  public boolean isFunctional() {
    try {
      return owlim.isFunctional(this.repositoryID, uri.toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#setFunctional(boolean)
   */
  public void setFunctional(boolean functional) {
    try {
      owlim.setFunctional(this.repositoryID, uri.toString(), functional);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isInverseFunctional()
   */
  public boolean isInverseFunctional() {
    try {
      return owlim.isInverseFunctional(this.repositoryID, uri.toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#setInverseFunctional(boolean)
   */
  public void setInverseFunctional(boolean inverseFunctional) {
    try {
      owlim.setInverseFunctional(this.repositoryID, uri.toString(),
              inverseFunctional);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isValidRange(gate.creole.ontology.OResource)
   */
  public boolean isValidRange(OResource aResource) {
    try {
      ResourceInfo[] listOfOResources = owlim.getRange(this.repositoryID, uri
              .toString());
      if(listOfOResources.length == 0) return true;
      // lets first make a easy move
      List<String> list = new ArrayList<String>();
      for(int i = 0; i < listOfOResources.length; i++) {
        list.add(listOfOResources[i].getUri());
      }
      if(list.contains(aResource.getURI().toString())) {
        return true;
      }
      if(aResource instanceof OInstance) {
        // lets find out all its super classes
        ResourceInfo[] oClasses = owlim.getClassesOfIndividual(
                this.repositoryID, aResource.getURI().toString(),
                OConstants.TRANSITIVE_CLOSURE);
        // if any of them is in listOfOResource, we return true, else
        // false
        List<String> oClassList = new ArrayList<String>();
        for(int i = 0; i < oClasses.length; i++) {
          oClassList.add(oClasses[i].getUri());
        }
        if(Collections.disjoint(oClassList, list))
          return false;
        else return true;
      }
      if(aResource instanceof OClass) {
        // lets find out all its super classes
        ResourceInfo[] oClasses = owlim.getSuperClasses(this.repositoryID,
                aResource.getURI().toString(), OConstants.TRANSITIVE_CLOSURE);
        // if any of them is in listOfOResource, we return true, else
        // false
        List<String> oClassList = new ArrayList<String>();
        for(int i = 0; i < oClasses.length; i++) {
          oClassList.add(oClasses[i].getUri());
        }
        if(Collections.disjoint(oClassList, list))
          return false;
        else return true;
      }
      if(aResource instanceof RDFProperty
              && !(aResource instanceof AnnotationProperty)) {
        Property[] oProps = owlim.getSuperProperties(this.repositoryID,
                aResource.getURI().toString(), OConstants.TRANSITIVE_CLOSURE);
        for(int i = 0; i < oProps.length; i++) {
          if(list.contains(oProps[i].getUri())) {
            return true;
          }
        }
      }
      return false;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isValidDomain(gate.creole.ontology.OResource)
   */
  public boolean isValidDomain(OResource aResource) {
    try {
      ResourceInfo[] listOfOResources = owlim.getDomain(this.repositoryID, uri
              .toString());
      if(listOfOResources.length == 0) return true;

      Set<String> list = new HashSet<String>();
      for(int i = 0; i < listOfOResources.length; i++) {
        list.add(listOfOResources[i].getUri().toString());
        OResource resource = ontology.getOResourceFromMap(listOfOResources[i]
                .getUri());
        if(resource != null && resource instanceof OClass) {
          Set<OClass> classes = ((OClass)resource)
                  .getSubClasses(OConstants.TRANSITIVE_CLOSURE);
          Iterator<OClass> iter = classes.iterator();
          while(iter.hasNext()) {
            list.add(iter.next().getURI().toString());
          }
        }
        else if(resource != null && resource instanceof RDFProperty
                && !(resource instanceof AnnotationProperty)) {
          Set<RDFProperty> props = ((RDFProperty)resource)
                  .getSubProperties(OConstants.TRANSITIVE_CLOSURE);
          Iterator<RDFProperty> iter = props.iterator();
          while(iter.hasNext()) {
            list.add(iter.next().getURI().toString());
          }
        }
      }

      if(list.contains(aResource.getURI().toString())) {
        return true;
      }
      if(aResource instanceof OInstance) {
        // lets find out all its super classes
        ResourceInfo[] oClasses = owlim.getClassesOfIndividual(
                this.repositoryID, aResource.getURI().toString(),
                OConstants.DIRECT_CLOSURE);
        // if any of them is in listOfOResource, we return true, else
        // false
        Set<String> oClassList = new HashSet<String>();
        for(int i = 0; i < oClasses.length; i++) {
          oClassList.add(oClasses[i].getUri());
        }
        return list.containsAll(oClassList);
      }

      if(aResource instanceof OClass) {
        return list.contains(aResource);
      }

      if(aResource instanceof RDFProperty
              && !(aResource instanceof AnnotationProperty)) {
        if(list.contains(aResource.getURI().toString())) {
          return true;
        }
      }

      return false;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getDomain()
   */
  public Set<OResource> getDomain() {
    try {
      ResourceInfo[] list = owlim.getDomain(this.repositoryID, uri.toString());
      Set<OResource> domain = new HashSet<OResource>();
      List<String> individuals = Arrays.asList(owlim
              .getIndividuals(this.repositoryID));
      // these resources can be anything - an instance, a property, or a
      // class
      for(int i = 0; i < list.length; i++) {
        // lets first search if it is available in ontology cache
        OResource resource = ontology.getOResourceFromMap(list[i].getUri());
        if(resource != null) {
          domain.add(resource);
          continue;
        }
        if(individuals.contains(list[i])) {
          domain.add(Utils.createOInstance(this.repositoryID, this.ontology,
                  this.owlim, list[i].getUri()));
          continue;
        }
        // otherwise we need to create it
        if(owlim.hasClass(this.repositoryID, list[i].getUri())) {
          // lets first check if this is a valid URI
          domain.add(Utils.createOClass(this.repositoryID, this.ontology,
                  this.owlim, list[i].getUri(), list[i].isAnonymous()));
          continue;
        }
        Property prop = owlim.getPropertyFromOntology(this.repositoryID,
                list[i].getUri());
        domain.add(Utils.createOProperty(this.repositoryID, this.ontology,
                this.owlim, prop.getUri(), prop.getType()));
      }
      return domain;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getRange()
   */
  public Set<OResource> getRange() {
    try {
      ResourceInfo[] list = owlim.getRange(this.repositoryID, uri.toString());
      Set<OResource> domain = new HashSet<OResource>();
      List<String> individuals = Arrays.asList(owlim
              .getIndividuals(this.repositoryID));
      // these resources can be anything - an instance, a property, or a
      // class
      for(int i = 0; i < list.length; i++) {
        // lets first search if it is available in ontology cache
        OResource resource = ontology.getOResourceFromMap(list[i].getUri());
        if(resource != null) {
          domain.add(resource);
          continue;
        }
        if(individuals.contains(list[i])) {
          domain.add(Utils.createOInstance(this.repositoryID, this.ontology,
                  this.owlim, list[i].getUri()));
          continue;
        }
        // otherwise we need to create it
        if(owlim.hasClass(this.repositoryID, list[i].getUri())) {
          domain.add(Utils.createOClass(this.repositoryID, this.ontology,
                  this.owlim, list[i].getUri(), list[i].isAnonymous()));
          continue;
        }
        Property prop = owlim.getPropertyFromOntology(this.repositoryID,
                list[i].getUri());
        domain.add(Utils.createOProperty(this.repositoryID, this.ontology,
                this.owlim, prop.getUri(), prop.getType()));
      }
      return domain;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }
}
