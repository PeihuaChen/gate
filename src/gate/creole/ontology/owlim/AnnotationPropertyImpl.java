/*
 *  AnnotationPropertyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: AnnotationPropertyImpl.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology.owlim;

import java.util.Set;
import gate.creole.ontology.AnnotationProperty;
import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.URI;
import gate.util.GateRuntimeException;

/**
 * Provides an implementation of the AnnotationProperty interface.
 * @author niraj
 */
public class AnnotationPropertyImpl extends RDFPropertyImpl implements
                                                         AnnotationProperty {
  /**
   * Constructor
   * 
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort
   */
  public AnnotationPropertyImpl(URI aURI, Ontology ontology,
          String repositoryID, OWLIMServiceImpl owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#setEquivalentPropertyAs(gate.creole.ontology.RDFProperty)
   */
  public void setEquivalentPropertyAs(RDFProperty theProperty) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getEquivalentPropertyAs()
   */
  public Set<RDFProperty> getEquivalentPropertyAs() {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isEquivalentPropertyAs(gate.creole.ontology.RDFProperty)
   */
  public boolean isEquivalentPropertyAs(RDFProperty theProperty) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#addSuperProperty(gate.creole.ontology.RDFProperty)
   */
  public void addSuperProperty(RDFProperty property) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#removeSuperProperty(gate.creole.ontology.RDFProperty)
   */
  public void removeSuperProperty(RDFProperty property) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getSuperProperties(byte)
   */
  public Set<RDFProperty> getSuperProperties(byte closure) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isSuperPropertyOf(gate.creole.ontology.RDFProperty,
   *      byte)
   */
  public boolean isSuperPropertyOf(RDFProperty theProperty, byte closure) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#addSubProperty(gate.creole.ontology.RDFProperty)
   */
  public void addSubProperty(RDFProperty property) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#removeSubProperty(gate.creole.ontology.RDFProperty)
   */
  public void removeSubProperty(RDFProperty property) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getSubProperties(byte)
   */
  public Set<RDFProperty> getSubProperties(byte closure) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isSubPropertyOf(gate.creole.ontology.RDFProperty,
   *      byte)
   */
  public boolean isSubPropertyOf(RDFProperty theProperty, byte closure) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isFunctional()
   */
  public boolean isFunctional() {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#setFunctional(boolean)
   */
  public void setFunctional(boolean functional) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isInverseFunctional()
   */
  public boolean isInverseFunctional() {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#setInverseFunctional(boolean)
   */
  public void setInverseFunctional(boolean inverseFunctional) {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isValidRange(gate.creole.ontology.OResource)
   */
  public boolean isValidRange(OResource aResource) {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isValidDomain(gate.creole.ontology.OResource)
   */
  public boolean isValidDomain(OResource aResource) {
    return true;
  }

  /*
   * (non-Javadoc)
   *  
   * @see gate.creole.ontology.RDFProperty#getDomain()
   */
  public Set<OResource> getDomain() {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getRange()
   */
  public Set<OResource> getRange() {
    throw new GateRuntimeException(
            "This operation is not valid for AnnotationProperties.");
  }
}
