/*
 *  Copyright (c) 2010--2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */
package gate.termraider.output;

import gate.*;
import gate.creole.*;
import gate.creole.ontology.*;
import gate.util.GateException;
import java.net.*;
import java.util.*;
import gate.termraider.bank.*;
import gate.termraider.util.*;


public class OntologyGenerator {
  
  String xmlBaseFilePath      = "gate/resources/base_ontology.rdf";
  
  public static final String rdfUri   = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  public static final String rdfsUri  = "http://www.w3.org/2000/01/rdf-schema#";
  public static final String xsdUri   = "http://www.w3.org/2001/XMLSchema#";
  public static final String arcoUri  = "http://www.gate.ac.uk/ns/ontologies/arcomem-data-model.rdf#";
  public static final String randomNsBase = "http://www.gate.ac.uk/ns/arcomem/";
  public static final String ontologyClass = "gate.creole.ontology.impl.sesame.OWLIMOntology";
  
  private String instanceNamespaceUri;
  
  private AnnotationProperty labelProperty;
  private DatatypeProperty scoreProperty, freqProperty, languageProperty;
  private ObjectProperty isRealizedByProperty;
  private OClass termClass, textClass;
  private boolean debugMode;
  private AbstractTermbank termbank;
  private String scorePropertyName, freqPropertyName, languagePropertyName;
  
  
  public OntologyGenerator(AbstractTermbank termbank) {
    this.termbank = termbank;
    this.debugMode = termbank.getDebugMode();
    this.scorePropertyName = termbank.getScoreProperty();
    this.freqPropertyName = termbank.getFreqProperty();
    this.languagePropertyName = "hasLanguage";
  }
  
  
  public static String generateInstanceNamespace() {
    return Utilities.generateID(randomNsBase, "#");
  }
  
  
  public Ontology generateOntology(double threshold, String ontologyName,
          String specifiedInstanceNamespace) throws ResourceInstantiationException  {
    Ontology ontology = loadBaseOntology();
    initializeOntology(ontology, ontologyName, specifiedInstanceNamespace);
    try {
      populateOntology(ontology, threshold);
    } 
    catch(InvalidValueException e) {
      throw new ResourceInstantiationException(e);
    }
    return ontology;
  }
  
  
  private Ontology loadBaseOntology() throws ResourceInstantiationException {
    FeatureMap parameters = Factory.newFeatureMap();
    URL baseUrl = Utilities.getUrlInJar(termbank, xmlBaseFilePath);
    parameters.put("rdfXmlURL", baseUrl);
    Resource ontology = Factory.createResource(ontologyClass, parameters);
    return (Ontology) ontology;
  }
  
  
  private void populateOntology(Ontology ontology, double threshold) throws InvalidValueException {
    Map<Term, Double> termScores = termbank.getTermScores();
    Map<Term, Set<String>> termDocuments = termbank.getTermDocuments();
    List<Term> sortedTerms = termbank.getTermsByDescendingScore();

    int written = 0;
    
    for (Term term : sortedTerms) {
      Double score = termScores.get(term);
      if (score >= threshold) {
        Set<String> documents = termDocuments.get(term);
        makeTermInstance(ontology, term, score, documents);
        written++;
      }
      else {  // the rest must be lower
        break;
      }
    }
  }
  
  
  private OInstance makeTermInstance(Ontology ontology, Term term,  
          Double score, Set<String> documents) throws InvalidValueException {
    String flattenedTermName  = Utilities.veryCleanString(term.getTermString());
    OURI termUri = ontology.createOURI(instanceNamespaceUri + flattenedTermName);
    OInstance termInstance = ontology.addOInstance(termUri, this.termClass);
    termInstance.addAnnotationPropertyValue(labelProperty, new Literal(term.getTermString(), DataType.getStringDataType()));
    termInstance.addDatatypePropertyValue(scoreProperty, new Literal(score.toString(), DataType.getDoubleDataType()));
    termInstance.addDatatypePropertyValue(languageProperty, new Literal(term.getLanguageCode(), DataType.getStringDataType()));

    for (String document: documents) {
      OURI textInstanceUri = ontology.createOURI(instanceNamespaceUri + Utilities.generateID("Text_", ""));
      OInstance textInstance = ontology.addOInstance(textInstanceUri, this.textClass);
      textInstance.addAnnotationPropertyValue(labelProperty, new Literal(document, DataType.getStringDataType()));
      termInstance.addObjectPropertyValue(isRealizedByProperty, textInstance);
    }
    
    if (freqProperty != null) {
      int frequency = this.termbank.getTermFrequency(term);
      termInstance.addDatatypePropertyValue(freqProperty, new Literal(Integer.toString(frequency), DataType.getIntDataType()));
    }

    return termInstance;
  }
  
  
  private void initializeOntology(Ontology ontology, String ontologyName, 
          String specifiedInstanceNamespace) {
    if ( (specifiedInstanceNamespace == null) || specifiedInstanceNamespace.isEmpty() ) {
      this.instanceNamespaceUri = Utilities.generateID(randomNsBase, "#");
    }
    else {
      this.instanceNamespaceUri = specifiedInstanceNamespace;
    }

    OURI termUri = ontology.createOURI(arcoUri + "Term");
    OURI textUri = ontology.createOURI(arcoUri + "Text");
    OURI labelUri = ontology.createOURI(rdfsUri + "label");
    OURI scoreUri = ontology.createOURI(arcoUri + scorePropertyName);
    OURI languageUri = ontology.createOURI(arcoUri + languagePropertyName);
    OURI isRealizedByUri = ontology.createOURI(arcoUri + "isRealizedBy");

    this.termClass = ontology.getOClass(termUri);
    Set<OClass> termClassSet = Collections.singleton(this.termClass);

    this.textClass = ontology.getOClass(textUri);
    Set<OClass> textClassSet = Collections.singleton(this.textClass);

    if ( (freqPropertyName != null) && (! freqPropertyName.isEmpty()) ) {
      OURI freqUri = ontology.createOURI(arcoUri + freqPropertyName);
      this.freqProperty = ontology.addDatatypeProperty(freqUri, termClassSet, DataType.getIntDataType());
    }
    else {
      this.freqProperty = null;
    }

    
    this.labelProperty = ontology.getAnnotationProperty(labelUri);
    this.languageProperty = ontology.addDatatypeProperty(languageUri, termClassSet, DataType.getStringDataType());
    this.scoreProperty = ontology.addDatatypeProperty(scoreUri, termClassSet, DataType.getDoubleDataType());
    this.isRealizedByProperty = ontology.addObjectProperty(isRealizedByUri, termClassSet, textClassSet);
    ontology.setName(ontologyName);

    if (debugMode) {
      System.out.println("Termbank -> Ontology: instances  " + this.instanceNamespaceUri);
    }
  }
  


  protected void setInstanceNamespace(String uri) throws GateException  {
    try {
      // make sure it's valid
      new java.net.URI(uri);
    }
    catch(URISyntaxException e) {
      throw new GateException(e);
    }
    instanceNamespaceUri = uri;
  }

  
}
