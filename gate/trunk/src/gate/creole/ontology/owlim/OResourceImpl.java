/*
 *  OResourceImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OResourceImpl.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology.owlim;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openrdf.vocabulary.RDFS;
import gate.creole.ontology.AnnotationProperty;
import gate.creole.ontology.DatatypeProperty;
import gate.creole.ontology.Literal;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.URI;
import gate.creole.ontology.owlim.PropertyValue;
import gate.util.GateRuntimeException;

/**
 * Constructor
 * 
 * @author niraj
 * 
 */
public class OResourceImpl implements OResource {
  /**
   * ID of the repository
   */
  protected String repositoryID;

  /**
   * instance of the OWLIMServices
   */
  protected OWLIMServiceImpl owlim;

  /**
   * URI of the resource
   */
  protected URI uri;

  /**
   * The ontology the current resource belongs to
   */
  protected Ontology ontology;

  /**
   * Constructor
   * 
   * @param aURI
   * @param repositoryID
   * @param owlimPort
   */
  public OResourceImpl(URI aURI, Ontology ontology, String repositoryID,
          OWLIMServiceImpl owlimPort) {
    this.uri = aURI;
    this.repositoryID = repositoryID;
    this.owlim = owlimPort;
    this.ontology = ontology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#getURI()
   */
  public URI getURI() {
    return this.uri;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#getURI()
   */
  public void setURI(URI uri) {
    throw new GateRuntimeException(
            "This operation is not allowed in this version!");
  }

  /**
   * This method returns a set of labels specified on this resource.
   * 
   * @return
   */
  public Set<Literal> getLabels() {
    try {
      PropertyValue[] pvalues = owlim.getAnnotationPropertyValues(
              this.repositoryID, this.uri.toString(), RDFS.LABEL);

      Set<Literal> toReturn = new HashSet<Literal>();
      for(PropertyValue pv : pvalues) {
        toReturn.add(new Literal(pv.getValue(), pv.getDatatype()));
      }

      return toReturn;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }

  }

  /**
   * This method returns a set of comments specified on this resource.
   * 
   * @return
   */
  public Set<Literal> getComments() {
    try {
      PropertyValue[] pvalues = owlim.getAnnotationPropertyValues(
              this.repositoryID, this.uri.toString(), RDFS.COMMENT);

      Set<Literal> toReturn = new HashSet<Literal>();
      for(PropertyValue pv : pvalues) {
        toReturn.add(new Literal(pv.getValue(), pv.getDatatype()));
      }

      return toReturn;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#getComment(java.lang.String)
   */
  public String getComment(String language) {
    try {
      return owlim.getAnnotationPropertyValue(this.repositoryID, this.uri
              .toString(), RDFS.COMMENT, language);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#setComment(java.lang.String,
   *      java.lang.String)
   */
  public void setComment(String aComment, String language) {
    try {
      owlim.addAnnotationPropertyValue(this.repositoryID, this.uri.toString(),
              RDFS.COMMENT, aComment, language);
      ontology.fireOntologyModificationEvent(this,
              OConstants.COMMENT_CHANGED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#getLabel(java.lang.String)
   */
  public String getLabel(String language) {
    try {
      return owlim.getAnnotationPropertyValue(this.repositoryID, this.uri
              .toString(), RDFS.LABEL, language);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#setLabel(java.lang.String,
   *      java.lang.String)
   */
  public void setLabel(String aLabel, String language) {
    try {
      owlim.addAnnotationPropertyValue(this.repositoryID, this.uri.toString(),
              RDFS.LABEL, aLabel, language);
      ontology.fireOntologyModificationEvent(this,
              OConstants.LABEL_CHANGED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#getName()
   */
  public String getName() {
    return this.uri.getResourceName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#getOntology()
   */
  public Ontology getOntology() {
    return this.ontology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#addAnnotationPropertyValue(gate.creole.ontology.AnnotationProperty,
   *      gate.creole.ontology.Literal)
   */
  public void addAnnotationPropertyValue(
          AnnotationProperty theAnnotationProperty, Literal literal) {
    try {
      owlim.addAnnotationPropertyValue(this.repositoryID, this.uri.toString(),
              theAnnotationProperty.getURI().toString(), literal.getValue(),
              literal.getLanguage());
      ontology.fireOntologyModificationEvent(this,
              OConstants.ANNOTATION_PROPERTY_VALUE_ADDED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#getAnnotationPropertyValues(gate.creole.ontology.AnnotationProperty)
   */
  public List<Literal> getAnnotationPropertyValues(
          AnnotationProperty theAnnotationProperty) {
    try {
      PropertyValue[] propValues = owlim.getAnnotationPropertyValues(
              this.repositoryID, this.uri.toString(), theAnnotationProperty
                      .getURI().toString());
      List<Literal> list = new ArrayList<Literal>();
      for(int i = 0; i < propValues.length; i++) {
        Literal l = new Literal(propValues[i].getValue(), propValues[i]
                .getDatatype());
        list.add(l);
      }
      return list;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#removeAnnotationPropertyValue(gate.creole.ontology.AnnotationProperty,
   *      gate.creole.ontology.Literal)
   */
  public void removeAnnotationPropertyValue(
          AnnotationProperty theAnnotationProperty, Literal literal) {
    try {
      owlim.removeAnnotationPropertyValue(this.repositoryID, this.uri
              .toString(), theAnnotationProperty.getURI().toString(), literal
              .getValue(), literal.getLanguage());
      ontology.fireOntologyModificationEvent(this,
              OConstants.ANNOTATION_PROPERTY_VALUE_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.OResource#removeAnnotationPropertyValues(gate.creole.ontology.AnnotationProperty)
   */
  public void removeAnnotationPropertyValues(
          AnnotationProperty theAnnotationProperty) {
    try {
      owlim.removeAnnotationPropertyValues(this.repositoryID, this.uri
              .toString(), theAnnotationProperty.getURI().toString());
      ontology.fireOntologyModificationEvent(this,
              OConstants.ANNOTATION_PROPERTY_VALUE_REMOVED_EVENT);
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /**
   * This method returns the annotation properties set on this resource.
   * 
   * @return
   */
  public Set<AnnotationProperty> getSetAnnotationProperties() {
    try {
      Property[] properties = owlim.getAnnotationProperties(this.repositoryID,
              this.uri.toString());
      Set<AnnotationProperty> annotProps = new HashSet<AnnotationProperty>();
      for(int i = 0; i < properties.length; i++) {
        if(properties[i].getType() != OConstants.ANNOTATION_PROPERTY) {
          throw new GateRuntimeException(
                  "The property :"
                          + properties[i].getUri()
                          + " returned from the repository is not an AnnotationProperty");
        }
        String propUri = properties[i].getUri();
        OResource resource = ontology.getOResourceFromMap(propUri);
        if(resource == null) {
          resource = new AnnotationPropertyImpl(new URI(propUri, false),
                  this.ontology, this.repositoryID, owlim);
          ontology.addOResourceToMap(propUri, resource);
        }
        annotProps.add((AnnotationProperty)resource);
      }
      return annotProps;
    }
    catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /**
   * Checks if the resource has the provided annotation property set on
   * it with the specified value.
   * 
   * @param aProperty
   * @param aValue
   * @return
   */
  public boolean hasAnnotationPropertyWithValue(AnnotationProperty aProperty,
          Literal aValue) {
    List<Literal> literals = getAnnotationPropertyValues(aProperty);
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

  /**
   * This method returns all the set properties set on this resource.
   * 
   * @return
   */
  public Set<RDFProperty> getAllSetProperties() {
    Set<RDFProperty> toReturn = new HashSet<RDFProperty>();
    toReturn.addAll(getSetAnnotationProperties());
    return toReturn;
  }

  /**
   * This method returns a set of all applicable properties on this
   * resource. Please note that this method is different from the
   * getAllSetProperties() method which returns a set of properties set
   * on the resource. For each property in the ontology, this method
   * checks if the current resource is valid domain. If so, the property
   * is said to be applicable, and otherwise not.
   * 
   * @return
   */
  public Set<RDFProperty> getProperties() {
    Set<RDFProperty> toReturn = new HashSet<RDFProperty>();

    // obtain all property definitions
    Set<RDFProperty> rdfProps = ontology.getPropertyDefinitions();
    if(rdfProps != null) {
      outer: for(RDFProperty property : rdfProps) {
        if(property instanceof AnnotationProperty) {
          toReturn.add(property);
          continue;
        }

        if(property instanceof ObjectProperty
                || property instanceof DatatypeProperty) {
          if(this instanceof OClass) {
            Set<OResource> domain = property.getDomain();
            if(domain.size() == 0) {
              toReturn.add(property);
              continue;
            }

            for(OResource r : domain) {
              OClass c = (OClass)r;
              if(c.equals(this)
                      || ((OClass)this).isSubClassOf(c,
                              OConstants.TRANSITIVE_CLOSURE)) {
                toReturn.add(property);
                continue outer;
              }
            }
          }
          else if(this instanceof OInstance) {
            if(property.isValidDomain((OInstance)this)) {
              toReturn.add(property);
              continue;
            }
          }
        }
        else {
          if(property.isValidDomain(this)) {
            toReturn.add(property);
            continue;
          }
        }
      }
    }
    return toReturn;
  }

  /**
   * String representation of the resource: its name and not the URI.
   */
  public String toString() {
    return this.getName();
  }

  /**
   * HashCode for this resource.
   */
  public int hashcode() {
    return this.getURI().toString().hashCode();
  }

  /**
   * equals method overriden tocompare URIs
   */
  public boolean equals(Object a) {
    if(a instanceof OResource) {
      return ((OResource)a).getURI().toString()
              .equals(this.getURI().toString());
    }
    return false;
  }
}
