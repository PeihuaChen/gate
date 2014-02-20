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
import gate.gui.ActionsPublisher;
import gate.termraider.gui.ActionSaveCsv;
import gate.termraider.util.*;
import gate.util.GateException;

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
        increment(term, bank.getFrequency(term));
      }
    }
  }
  
  
  protected void processCorpus(Corpus corpus) {
    for (int i=0 ; i < corpus.size() ; i++) {
      boolean wasLoaded = corpus.isDocumentLoaded(i);
      Document document = (Document) corpus.get(i);
      addData(document);
      // datastore safety
      if (! wasLoaded) {
        corpus.unloadDocument(document);
        Factory.deleteResource(document);
      }
    }
  }

  
  protected void addData(Document document) {
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
    minFrequency = this.getFrequency(this.getTerms().iterator().next());
    maxFrequency = 0;
    for (Term term : this.getTerms()) {
      int freq = this.getFrequency(term);
      maxFrequency = Math.max(maxFrequency, freq);
      minFrequency = Math.min(minFrequency, freq);
      this.types.add(term.getType());
      this.languages.add(term.getLanguageCode());
    }
  }
  
  
  public Set<Term> getTerms() {
    return documentFrequencies.keySet();
  }
  
  public int getFrequency(Term term) {
    if (documentFrequencies.containsKey(term)) {
      return documentFrequencies.get(term).intValue();
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


  @Override
  public void saveAsCsv(double threshold, File file) throws GateException {
    // TODO Auto-generated method stub
    
  }


  @Override
  public void saveAsCsv(File file) throws GateException {
    // TODO Auto-generated method stub
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
  
}
