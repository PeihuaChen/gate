/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gate.creole.ontology.impl.sesame;

import gate.creole.ontology.Literal;
import gate.creole.ontology.ONodeID;
import gate.creole.ontology.OURI;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.OntologyTripleStore;
import gate.creole.ontology.OntologyTripleStoreListener;
import gate.util.GateRuntimeException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of an OntologyTripleStore for the Sesame ontology backend.
 * 
 * @author Johann Petrak
 */
public class OntologyTripleStoreImpl implements OntologyTripleStore {

  protected Ontology ontology;
  protected OntologyServiceImplSesame ontologyService;
  protected List<OntologyTripleStoreListener> listeners = 
    new ArrayList<OntologyTripleStoreListener>();

  // disallow default constructor
  private OntologyTripleStoreImpl() {} 
  OntologyTripleStoreImpl(Ontology onto, OntologyServiceImplSesame os) {
    ontology = onto;
    ontologyService = os;
  }
  
  public void addTriple(ONodeID subject, OURI predicate, ONodeID object) {
    ontologyService.addTriple(subject, predicate, object);
    for(OntologyTripleStoreListener listener : listeners) {
      listener.tripleAdded(subject, predicate, object);
    }
  }

  public void addTriple(ONodeID subject, OURI predicate, Literal object) {
    ontologyService.addTriple(subject, predicate, object);
    for(OntologyTripleStoreListener listener : listeners) {
      listener.tripleAdded(subject, predicate, object);
    }
  }

  public void removeTriple(ONodeID subject, OURI predicate, ONodeID object) {
    ontologyService.removeTriple(subject, predicate, object);
    for(OntologyTripleStoreListener listener : listeners) {
      listener.tripleRemoved(subject, predicate, object);
    }
  }

  public void removeTriple(ONodeID subject, OURI predicate, Literal object) {
    ontologyService.removeTriple(subject, predicate, object);
    for(OntologyTripleStoreListener listener : listeners) {
      listener.tripleRemoved(subject, predicate, object);
    }
  }

  public synchronized void addOntologyTripleStoreListener(OntologyTripleStoreListener listener) {
    if(listener == null) {
      throw new GateRuntimeException("Listener object must not be null");
    }
    listeners.add(listener);
  }

  public synchronized void removeOntologyTripleStoreListener(OntologyTripleStoreListener listener) {
    if(listener == null) {
      throw new GateRuntimeException("Listener object must not be null");
    }
    listeners.remove(listener);
  }

}
