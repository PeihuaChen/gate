/*
 *  TransitivePropertyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: TransitivePropertyImpl.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology.owlim;

import service.client.OWLIM;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.TransitiveProperty;
import gate.creole.ontology.URI;

/**
 * Implementation of the TransitiveProperty
 * @author niraj
 *
 */
public class TransitivePropertyImpl extends ObjectPropertyImpl implements
                                                              TransitiveProperty {
  /**
   * Constructor
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort
   */
  public TransitivePropertyImpl(URI aURI, Ontology ontology,
          String repositoryID, OWLIM owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }
}
