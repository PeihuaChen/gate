/*
 *  AnnonymousClassImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: AnnonymousClassImpl.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology.owlim;

import gate.creole.ontology.AnonymousClass;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.URI;

/**
 * Implementation of the AnonymousClass
 * @author niraj
 */
public class AnonymousClassImpl extends OClassImpl implements AnonymousClass {
  /**
   * Constructor
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort
   */
  public AnonymousClassImpl(URI aURI, Ontology ontology, String repositoryID,
          OWLIMServiceImpl owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }
}
