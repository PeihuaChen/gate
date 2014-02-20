/*
 *  Copyright (c) 2008-2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */
package gate.termraider.bank;

import java.io.File;
import java.util.*;
import javax.swing.Action;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.gui.ActionsPublisher;
import gate.termraider.gui.ActionSaveCsv;
import gate.termraider.util.*;
import gate.util.GateException;

@CreoleResource(name = "DocumentFrequencyBank",
icon = "termbank-lr.png",
comment = "Document frequency counter derived from corpora and other DFBs")
public class DocumentFrequencyBank extends AbstractBank
implements ActionsPublisher{
  
  private static final long serialVersionUID = 5149075094060830331L;
  
  
  private Set<DocumentFrequencyBank> inputBanks;
  private boolean debugMode;
  protected String inputASName;
  protected Set<String> inputAnnotationTypes;
  
  private int documentTotal;
  private Map<Term, Integer> documentFrequencies;
  private int minFrequency, maxFrequency;
  private Map<String, Set<Term>> stringLookupTable;

  // transient to allow serialization
  protected transient List<Action> actionsList;


  public Resource init() throws ResourceInstantiationException {
    prepare();
    processInputBanks();
    processCorpora();
    churnData();
    return this;
  }
  

  public void cleanup() {
    super.cleanup();
  }
  
  
  
  protected void prepare() throws ResourceInstantiationException {
    if (corpora == null) {
      corpora = new HashSet<Corpus>();
    }
    if (inputBanks == null) {
      inputBanks = new HashSet<DocumentFrequencyBank>();
    }
    
    documentTotal = 0;
    documentFrequencies = new HashMap<Term, Integer>();
    languages = new HashSet<String>();
    types = new HashSet<String>();
    stringLookupTable = new HashMap<String, Set<Term>>();
  }

  
  protected void createActions() {
    actionsList = new ArrayList<Action>();
    actionsList.add(new ActionSaveCsv("Save as CSV...", this));
  }
  
  
  protected void processCorpora() {
    for (Corpus corpus : corpora) {
      processCorpus(corpus);
      if (debugMode) {
        System.out.println("Termbank: added corpus " + corpus.getName() + " with " + corpus.size() + " documents");
      }
    }
  }
  
  
  protected void processInputBanks() {
    for (DocumentFrequencyBank bank : inputBanks) {
      this.documentTotal += bank.documentTotal;
      for (Term term : bank.getTerms()) {
        increment(term, bank.getFrequencyStrict(term));
      }
    }
  }
  
  
  protected void processCorpus(Corpus corpus) {
    for (int i=0 ; i < corpus.size() ; i++) {
      boolean wasLoaded = corpus.isDocumentLoaded(i);
      Document document = (Document) corpus.get(i);
      processDocument(document);
      // datastore safety
      if (! wasLoaded) {
        corpus.unloadDocument(document);
        Factory.deleteResource(document);
      }
    }
  }

  
  protected void processDocument(Document document) {
    documentTotal++;
    AnnotationSet candidates = document.getAnnotations(inputASName).get(inputAnnotationTypes);

    Set<Term> documentTerms = new HashSet<Term>();
    for (Annotation candidate : candidates) {
      documentTerms.add(makeTerm(candidate, document));
    }
    
    for (Term term : documentTerms) {
      increment(term, 1);
    }
  }

  
  private void churnData() {
    if (this.getTerms().size() > 0) {
      minFrequency = this.getFrequencyStrict(this.getTerms().iterator().next());
    }
    else {
      minFrequency = 0;
    }
    maxFrequency = 0;
    for (Term term : this.getTerms()) {
      int freq = this.getFrequencyStrict(term);
      maxFrequency = Math.max(maxFrequency, freq);
      minFrequency = Math.min(minFrequency, freq);
      this.types.add(term.getType());
      this.languages.add(term.getLanguageCode());
      storeStringLookup(term);
    }
  }
  
  
  public Set<Term> getTerms() {
    return documentFrequencies.keySet();
  }
  
  public int getFrequencyStrict(Term term) {
    if (documentFrequencies.containsKey(term)) {
      return documentFrequencies.get(term).intValue();
    }
    
    return 0;
  }
  
  
  public int getFrequencyLax(Term term) {
    // Try for an exact match first
    if (documentFrequencies.containsKey(term)) {
      return documentFrequencies.get(term).intValue();
    }
    
    // Now see if there's one with a blank language code
    String termString = term.getTermString();
    if (stringLookupTable.containsKey(termString)) {
      for (Term testTerm : stringLookupTable.get(termString)) {
        if (testTerm.closeMatch(term)) {
          return documentFrequencies.get(testTerm).intValue();
        }
      }
    }
    
    return 0;
  }
  
  
  
  @CreoleParameter(comment = "Other DFBs to compile into the new one")
  public void setInputBanks(Set<DocumentFrequencyBank> inputBanks) {
    this.inputBanks = inputBanks;
  }
  
  public Set<DocumentFrequencyBank> getInputBanks() {
    return this.inputBanks;
  }


  @Override
  public List<Action> getActions() {
    // lazy instantiation because actionsList is transient
    if (actionsList == null) {
      createActions();
    }
    
    return this.actionsList;
  }


  @Override
  public Double getMinScore() {
    return new Double(this.minFrequency);
  }


  @Override
  public Double getMaxScore() {
    return new Double(this.maxFrequency);
  }

  
  public int getMinFrequency() {
    return this.minFrequency;
  }
  
  public int getMaxFrequency() {
    return this.maxFrequency;
  }

  @Override
  public void saveAsCsv(double threshold, File file) throws GateException {
    System.out.println("CSV output has not yet been implemented.");
    // TODO Auto-generated method stub
  }


  @Override
  public void saveAsCsv(File file) throws GateException {
    saveAsCsv(0.0, file);
  }

  
  @CreoleParameter(comment = "print debugging information during initialization",
          defaultValue = "false")
  public void setDebugMode(Boolean debug) {
    this.debugMode = debug;
  }

  public Boolean getDebugMode() {
    return this.debugMode;
  }

  
  @CreoleParameter(comment = "input AS name",
          defaultValue = "")
  public void setInputASName(String name) {
    this.inputASName = name;
  }
  public String getInputASName() {
    return this.inputASName;
  }
  
  
  @CreoleParameter(comment = "input annotation types",
          defaultValue = "SingleWord;MultiWord")
  public void setInputAnnotationTypes(Set<String> names) {
    this.inputAnnotationTypes = names;
  }
  
  public Set<String> getInputAnnotationTypes() {
    return this.inputAnnotationTypes;
  }
  
  
  private void increment(Term term, int i) {
    int count = i;
    if (documentFrequencies.containsKey(term)) {
      count += documentFrequencies.get(term).intValue();
    }
    documentFrequencies.put(term, count);
  }
  
  
  private void storeStringLookup(Term term) {
    String termString = term.getTermString();
    Set<Term> terms;
    if (stringLookupTable.containsKey(termString)) {
      terms = stringLookupTable.get(termString);
    }
    else {
      terms = new HashSet<Term>();
    }
    terms.add(term);
    stringLookupTable.put(termString, terms);
  }
  
  
  public Map<Term, Integer> getDocFrequencies() {
    return this.documentFrequencies;
  }
  
  public int getTotalDocs() {
    return this.documentTotal;
  }
}
