package gate;

import gate.creole.ontology.Ontology;
/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Danica Damljanovic 06/02/2008
 *
 *
 */

/**
 * A special type of interface to allow manipulating with {@link Ontology}
 * as a parameter
 */
public interface OntologyAwareProcessingResource extends ProcessingResource {

  /** Set the ontology property for this analyser. */
  public void setOntology(Ontology ontology);

  /** Get the ontology property for this analyser. */
  public Ontology getOntology();
;
}