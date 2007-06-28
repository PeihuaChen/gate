/**
 * 
 */
package gate.creole.ontology.owlim;

import gate.creole.ontology.HasValueRestriction;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OResource;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.URI;

/**
 * @author niraj
 *
 */
public class HasValueRestrictionImpl extends OClassImpl implements
                                                       HasValueRestriction {

  /**
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort
   */
  public HasValueRestrictionImpl(URI aURI, Ontology ontology,
          String repositoryID, OWLIM owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.HasValuesFromRestriction#getHasValue()
   */
  public OResource getHasValue() {
      ResourceInfo resource = owlim.getRestrictionValue(this.repositoryID,
              this.uri.toString(), OConstants.HAS_VALUE_RESTRICTION);
      return Utils.createOClass(this.repositoryID, this.ontology, this.owlim,
              resource.getUri(), resource.getClassType());
  }
  
  /**
   * Sets the resource as a restricted value.
   * 
   * @param resource
   */
  public void setHasValue(OResource resource) {
    owlim.setRestrictionValue(this.repositoryID, this.uri.toString(),
            OConstants.HAS_VALUE_RESTRICTION, resource.getURI()
                    .toString());
  }
  

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Restriction#getOnPropertyValue()
   */
  public RDFProperty getOnPropertyValue() {
      Property property = owlim.getOnPropertyValue(this.repositoryID, this.uri
              .toString());
      return Utils.createOProperty(repositoryID, ontology, owlim, property
              .getUri(), property.getType());
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Restriction#setOnPropertyValue(gate.creole.ontology.RDFProperty)
   */
  public void setOnPropertyValue(RDFProperty property) {
      owlim.setOnPropertyValue(this.repositoryID, this.uri.toString(), property
              .getURI().toString());
      ontology.fireOntologyModificationEvent(this, OConstants.RESTRICTION_MODIFICATION_EVENT);
  }
}
