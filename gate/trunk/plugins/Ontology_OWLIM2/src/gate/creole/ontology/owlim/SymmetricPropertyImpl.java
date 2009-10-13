/*
 *  SymmetricPropertyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id$
 */
package gate.creole.ontology.owlim;

import gate.creole.ontology.Ontology;
import gate.creole.ontology.SymmetricProperty;
import gate.creole.ontology.URI;

/**
 * Implementation of the SymmetricProperty
 * @author niraj
 *
 */
public class SymmetricPropertyImpl extends ObjectPropertyImpl implements
                                                             SymmetricProperty {
  /**
   * Constructor
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort
   */
  public SymmetricPropertyImpl(URI aURI, Ontology ontology,
          String repositoryID, OWLIM owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }
}
