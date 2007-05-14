/*
 *  OInstanceImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OInstanceImpl.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology.owlim;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gate.creole.ontology.DataType;
import gate.creole.ontology.DatatypeProperty;
import gate.creole.ontology.InvalidValueException;
import gate.creole.ontology.Literal;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.OntologyUtilities;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.URI;
import gate.util.GateRuntimeException;

/**
 * Implementation of the OInstance
 * 
 * @author niraj
 * 
 */
public class OInstanceImpl extends OResourceImpl implements OInstance {
  /**
   * Constructor
   * 
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort
   */
  public OInstanceImpl(URI aURI, Ontology ontology, String repositoryID,
          OWLIM owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#getOClasses(byte)
   */
  public Set<OClass> getOClasses(byte closure) {
    try {
      ResourceInfo[] oClasses = owlim.getClassesOfIndividual(this.repositoryID,
              this.uri.toString(), closure);
      Set<OClass> set = new HashSet<OClass>();
      for(int i = 0; i < oClasses.length; i++) {
        set.add(Utils.createOClass(this.repositoryID, this.ontology,
                this.owlim, oClasses[i].getUri(), oClasses[i].isAnonymous()));
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
   * @see gate.creole.ontology.OInstance#isInstanceOf(gate.creole.ontology.OClass,
   *      byte)
   */
  public boolean isInstanceOf(OClass aClass, byte closure) {
    try {
      return owlim.hasIndividual(this.repositoryID, aClass.getURI().toString(),
              this.uri.toString(), closure);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#setDifferentFrom(gate.creole.ontology.OInstance)
   */
  public void setDifferentFrom(OInstance theInstance) {
    try {
      if(this == theInstance) {
        Utils
                .warning("setDifferentFrom(theInstance) : the source and the argument instances refer to the same instance and therefore cannot be set as different from each other");
        return;
      }

      owlim.setDifferentIndividualFrom(this.repositoryID, this.uri.toString(),
              theInstance.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.DIFFERENT_INSTANCE_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#getDifferentInstances()
   */
  public Set<OInstance> getDifferentInstances() {
    try {
      String[] oInsts = owlim.getDifferentIndividualFrom(this.repositoryID,
              this.uri.toString());
      Set<OInstance> set = new HashSet<OInstance>();
      for(int i = 0; i < oInsts.length; i++) {
        set.add(Utils.createOInstance(this.repositoryID, this.ontology,
                this.owlim, oInsts[i]));
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
   * @see gate.creole.ontology.OInstance#isDifferentFrom(gate.creole.ontology.OInstance)
   */
  public boolean isDifferentFrom(OInstance theInstance) {
    try {
      return owlim.isDifferentIndividualFrom(this.repositoryID, this.uri
              .toString(), theInstance.getURI().toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#setSameInstanceAs(gate.creole.ontology.OInstance)
   */
  public void setSameInstanceAs(OInstance theInstance) {
    try {
      if(this == theInstance) {
        Utils
                .warning("setDifferentFrom(theInstance) : the source and the argument instances refer to the same instance and therefore cannot be set as same");
        return;
      }

      owlim.setSameIndividualAs(this.repositoryID, this.uri.toString(),
              theInstance.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.SAME_INSTANCE_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#getSameInstance()
   */
  public Set<OInstance> getSameInstance() {
    try {
      String[] oInsts = owlim.getSameIndividualAs(this.repositoryID, this.uri
              .toString());
      Set<OInstance> set = new HashSet<OInstance>();
      for(int i = 0; i < oInsts.length; i++) {
        set.add(Utils.createOInstance(this.repositoryID, this.ontology,
                this.owlim, oInsts[i]));
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
   * @see gate.creole.ontology.OInstance#isSameInstanceAs(gate.creole.ontology.OInstance)
   */
  public boolean isSameInstanceAs(OInstance theInstance) {
    try {
      return owlim.isSameIndividualAs(this.repositoryID, this.uri.toString(),
              theInstance.getURI().toString());
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#addRDFPropertyValue(gate.creole.ontology.RDFProperty,
   *      gate.creole.ontology.OResource)
   */
  public void addRDFPropertyValue(RDFProperty aProperty, OResource value)
          throws InvalidValueException {
    try {

      // we need to check if the current instance is a valid domain for
      // the property
      if(!aProperty.isValidDomain(this)) {
        Utils.error(this.getURI().toString()
                + " is not a valid domain for the property "
                + aProperty.getURI().toString());
        return;
      }

      // we need to check if the current instance is a valid domain for
      // the property
      if(!aProperty.isValidRange(value)) {
        Utils.error(value.getURI().toString()
                + " is not a valid range for the property "
                + aProperty.getURI().toString());
        return;
      }

      owlim.addRDFPropertyValue(this.repositoryID, this.uri.toString(),
              aProperty.getURI().toString(), value.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.RDF_PROPERTY_VALUE_ADDED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#removeRDFPropertyValue(gate.creole.ontology.RDFProperty,
   *      gate.creole.ontology.OResource)
   */
  public void removeRDFPropertyValue(RDFProperty aProperty, OResource value) {
    try {
      owlim.removeRDFPropertyValue(this.repositoryID, this.uri.toString(),
              aProperty.getURI().toString(), value.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.RDF_PROPERTY_VALUE_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#getRDFPropertyValues(gate.creole.ontology.RDFProperty)
   */
  public List<OResource> getRDFPropertyValues(RDFProperty aProperty) {
    try {
      ResourceInfo[] list = owlim.getRDFPropertyValues(this.repositoryID, uri
              .toString(), aProperty.getURI().toString());
      List<OResource> values = new ArrayList<OResource>();
      List<String> individuals = Arrays.asList(owlim
              .getIndividuals(this.repositoryID));
      // these resources can be anything - an instance, a property, or a
      // class
      for(int i = 0; i < list.length; i++) {
        // lets first search if it is available in ontology cache
        OResource resource = ontology.getOResourceFromMap(list[i].getUri());
        if(resource != null) {
          values.add(resource);
          continue;
        }
        // is it an individual
        if(individuals.contains(list[i])) {
          values.add(Utils.createOInstance(this.repositoryID, this.ontology,
                  this.owlim, list[i].getUri()));
          continue;
        }
        // is it a class
        if(owlim.hasClass(this.repositoryID, list[i].getUri())) {
          values.add(Utils.createOClass(this.repositoryID, this.ontology,
                  this.owlim, list[i].getUri(), list[i].isAnonymous()));
          continue;
        }

        Property prop = owlim.getPropertyFromOntology(this.repositoryID,
                list[i].getUri());
        values.add(Utils.createOProperty(this.repositoryID, this.ontology,
                this.owlim, prop.getUri(), prop.getType()));
      }
      return values;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /**
   * This method returns the RDF properties set on this resource.
   * 
   * @return
   */
  public Set<RDFProperty> getSetRDFProperties() {
    try {
      Property[] properties = owlim.getRDFProperties(this.repositoryID,
              this.uri.toString());
      Set<RDFProperty> rdfProps = new HashSet<RDFProperty>();
      for(int i = 0; i < properties.length; i++) {
        if(properties[i].getType() != OConstants.RDF_PROPERTY) {
          throw new GateRuntimeException("The property :"
                  + properties[i].getUri()
                  + " returned from the repository is not an RDFProperty");
        }
        String propUri = properties[i].getUri();
        OResource resource = ontology.getOResourceFromMap(propUri);
        if(resource == null) {
          resource = new RDFPropertyImpl(new URI(propUri, false),
                  this.ontology, this.repositoryID, owlim);
          ontology.addOResourceToMap(propUri, resource);
        }
        rdfProps.add((RDFProperty)resource);
      }
      return rdfProps;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }

  }

  /**
   * Checks if the resource has the provided RDF property set on it with
   * the specified value.
   * 
   * @param aProperty
   * @param aResource
   * @return
   */
  public boolean hasRDFPropertyWithValue(RDFProperty aProperty,
          OResource aResource) {
    List<OResource> resources = getRDFPropertyValues(aProperty);
    for(OResource r : resources) {
      if(r.equals(aResource)) return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#removeRDFPropertyValues(gate.creole.ontology.RDFProperty)
   */
  public void removeRDFPropertyValues(RDFProperty aProperty) {
    try {
      owlim.removeRDFPropertyValues(this.repositoryID, this.uri.toString(),
              aProperty.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.RDF_PROPERTY_VALUE_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#addDatatypePropertyValue(gate.creole.ontology.DatatypeProperty,
   *      gate.creole.ontology.Literal)
   */
  public void addDatatypePropertyValue(DatatypeProperty aProperty, Literal value)
          throws InvalidValueException {
    try {
      // we need to check if the current instance is a valid domain for
      // the property
      if(!aProperty.isValidDomain(this)) {
        Utils.error(this.getURI().toString()
                + " is not a valid domain for the property "
                + aProperty.getURI().toString());
        return;
      }

      DataType type = aProperty.getDataType();
      if(value.getDataType() == null) {
        type = aProperty.getDataType();
      }
      else {
        if(!type.getXmlSchemaURI().toString().equals(
                value.getDataType().getXmlSchemaURI().toString()))
          throw new GateRuntimeException("Datatype :"
                  + value.getDataType().getXmlSchemaURI().toString()
                  + " doesn't match with the property's datatype :"
                  + type.getXmlSchemaURI().toString());
      }

      owlim.addDatatypePropertyValue(this.repositoryID, this.uri.toString(),
              aProperty.getURI().toString(), type.getXmlSchemaURI().toString(),
              value.getValue());
      ontology.fireOntologyModificationEvent(this,
              OConstants.DATATYPE_PROPERTY_VALUE_ADDED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#removeDatatypePropertyValue(gate.creole.ontology.DatatypeProperty,
   *      gate.creole.ontology.Literal)
   */
  public void removeDatatypePropertyValue(DatatypeProperty aProperty,
          Literal value) {
    try {
      owlim.removeDatatypePropertyValue(this.repositoryID, this.uri.toString(),
              aProperty.getURI().toString(), value.getDataType()
                      .getXmlSchemaURI().toString(), value.getValue());
      ontology.fireOntologyModificationEvent(this,
              OConstants.DATATYPE_PROPERTY_VALUE_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#getDatatypePropertyValues(gate.creole.ontology.DatatypeProperty)
   */
  public List<Literal> getDatatypePropertyValues(DatatypeProperty aProperty) {
    try {
      PropertyValue[] values = owlim.getDatatypePropertyValues(
              this.repositoryID, this.uri.toString(), aProperty.getURI()
                      .toString());
      List<Literal> list = new ArrayList<Literal>();
      for(int i = 0; i < values.length; i++) {
        list.add(new Literal(values[i].getValue(), OntologyUtilities
                .getDataType(values[i].getDatatype())));
      }
      return list;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
    catch(InvalidValueException ive) {
      throw new GateRuntimeException(ive);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#removeDatatypePropertyValues(gate.creole.ontology.DatatypeProperty)
   */
  public void removeDatatypePropertyValues(DatatypeProperty aProperty) {
    try {
      owlim.removeDatatypePropertyValues(this.repositoryID,
              this.uri.toString(), aProperty.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.DATATYPE_PROPERTY_VALUE_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /**
   * This method returns the datatype properties set on this resource.
   * 
   * @return
   */
  public Set<DatatypeProperty> getSetDatatypeProperties() {
    try {
      Property[] properties = owlim.getDatatypeProperties(this.repositoryID,
              this.uri.toString());
      Set<DatatypeProperty> dataProps = new HashSet<DatatypeProperty>();
      for(int i = 0; i < properties.length; i++) {
        if(properties[i].getType() != OConstants.DATATYPE_PROPERTY) {
          throw new GateRuntimeException("The property :"
                  + properties[i].getUri()
                  + " returned from the repository is not an DatatypeProperty");
        }
        String propUri = properties[i].getUri();
        OResource resource = ontology.getOResourceFromMap(propUri);
        if(resource == null) {
          resource = new DatatypePropertyImpl(new URI(propUri, false),
                  this.ontology, this.repositoryID, owlim);
          ontology.addOResourceToMap(propUri, resource);
        }
        dataProps.add((DatatypeProperty)resource);
      }
      return dataProps;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }

  }

  /**
   * Checks if the resource has the provided datatype property set on it
   * with the specified value.
   * 
   * @param aProperty
   * @param aValue
   * @return
   */
  public boolean hasDatatypePropertyWithValue(DatatypeProperty aProperty,
          Literal aValue) {

    List<Literal> literals = getDatatypePropertyValues(aProperty);
    for(Literal l : literals) {
      if(l.getValue().equals(aValue.getValue())) {
        if(l.getDataType() == null) {
          if(aValue.getDataType() != null) continue;
        }
        else {
          if(aValue.getDataType() == null)
            continue;
          else if(!aValue.getDataType().getXmlSchemaURI().equals(
                  l.getDataType().getXmlSchemaURI())) continue;
        }

        if(l.getLanguage() == null) {
          if(aValue.getLanguage() != null) continue;
        }
        else {
          if(aValue.getLanguage() == null)
            continue;
          else if(!aValue.getLanguage().equals(l.getLanguage())) continue;
        }
        return true;
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#addObjectPropertyValue(gate.creole.ontology.ObjectProperty,
   *      gate.creole.ontology.OInstance)
   */
  public void addObjectPropertyValue(ObjectProperty aProperty, OInstance value)
          throws InvalidValueException {
    try {

      // we need to check if the current instance is a valid domain for
      // the property
      if(!aProperty.isValidDomain(this)) {
        Utils.error(this.getURI().toString()
                + " is not a valid domain for the property "
                + aProperty.getURI().toString());
        return;
      }

      // we need to check if the current instance is a valid domain for
      // the property
      if(!aProperty.isValidRange(value)) {
        Utils.error(value.getURI().toString()
                + " is not a valid range for the property "
                + aProperty.getURI().toString());
        return;
      }

      owlim.addObjectPropertyValue(this.repositoryID, this.uri.toString(),
              aProperty.getURI().toString(), value.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.OBJECT_PROPERTY_VALUE_ADDED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#removeObjectPropertyValue(gate.creole.ontology.ObjectProperty,
   *      gate.creole.ontology.OInstance)
   */
  public void removeObjectPropertyValue(ObjectProperty aProperty,
          OInstance value) {
    try {
      owlim.removeObjectPropertyValue(this.repositoryID, this.uri.toString(),
              aProperty.getURI().toString(), value.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.OBJECT_PROPERTY_VALUE_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#getObjectPropertyValues(gate.creole.ontology.ObjectProperty)
   */
  public List<OInstance> getObjectPropertyValues(ObjectProperty aProperty) {
    try {
      String[] list = owlim.getObjectPropertyValues(this.repositoryID, uri
              .toString(), aProperty.getURI().toString());
      List<OInstance> values = new ArrayList<OInstance>();
      for(int i = 0; i < list.length; i++) {
        values.add(Utils.createOInstance(this.repositoryID, this.ontology,
                this.owlim, list[i]));
      }
      return values;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OInstance#removeObjectPropertyValues(gate.creole.ontology.ObjectProperty)
   */
  public void removeObjectPropertyValues(ObjectProperty aProperty) {
    try {
      owlim.removeObjectPropertyValues(this.repositoryID, this.uri.toString(),
              aProperty.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.OBJECT_PROPERTY_VALUE_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /**
   * This method returns the object properties set on this resource.
   * 
   * @return
   */
  public Set<ObjectProperty> getSetObjectProperties() {
    try {
      Property[] properties = owlim.getObjectProperties(this.repositoryID,
              this.uri.toString());
      Set<ObjectProperty> objectProps = new HashSet<ObjectProperty>();
      for(int i = 0; i < properties.length; i++) {
        if(properties[i].getType() != OConstants.OBJECT_PROPERTY) {
          throw new GateRuntimeException("The property :"
                  + properties[i].getUri()
                  + " returned from the repository is not an ObjectProperty");
        }
        String propUri = properties[i].getUri();
        OResource resource = ontology.getOResourceFromMap(propUri);
        if(resource == null) {
          resource = new ObjectPropertyImpl(new URI(propUri, false),
                  this.ontology, this.repositoryID, owlim);
          ontology.addOResourceToMap(propUri, resource);
        }
        objectProps.add((ObjectProperty)resource);
      }
      return objectProps;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }

  }

  /**
   * Checks if the resource has the provided object property set on it
   * with the specified value.
   * 
   * @param aProperty
   * @param aValue
   * @return
   */
  public boolean hasObjectPropertyWithValue(ObjectProperty aProperty,
          OInstance aValue) {
    List<OInstance> instances = getObjectPropertyValues(aProperty);
    for(OInstance i : instances) {
      if(i.equals(aValue)) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method returns all the set properties set on this resource.
   * 
   * @return
   */
  public Set<RDFProperty> getAllSetProperties() {
    Set<RDFProperty> toReturn = new HashSet<RDFProperty>();
    toReturn.addAll(getSetAnnotationProperties());
    toReturn.addAll(getSetDatatypeProperties());
    toReturn.addAll(getSetObjectProperties());
    toReturn.addAll(getSetRDFProperties());
    return toReturn;
  }
}
