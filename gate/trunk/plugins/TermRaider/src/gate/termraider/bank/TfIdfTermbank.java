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

import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.*;
import gate.gui.ActionsPublisher;
import gate.*;
import gate.termraider.bank.modes.IdfCalculation;
import gate.termraider.bank.modes.TfCalculation;
import gate.termraider.util.*;

import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;



@CreoleResource(name = "TfIdfTermbank",
        icon = "termbank-lr.png",
        comment = "TermRaider Termbank derived from vectors in document features")

public class TfIdfTermbank extends AbstractTermbank
    implements ActionsPublisher  {

  private static final long serialVersionUID = -8275690376233755995L;
  
  /* EXTRA CREOLE PARAMETERS */
  private TfCalculation tfCalculation;
  private IdfCalculation idfCalculation;
  private DocumentFrequencyBank docFreqSource;
  
  /* EXTRA DATA */
  private int documentCount;
  
  
  protected void processDocument(Document document) {
    documentCount++;
    String documentSource = Utilities.sourceOrName(document);
    AnnotationSet candidates = document.getAnnotations(inputASName).get(inputAnnotationTypes);

    for (Annotation candidate : candidates) {
      Term term = makeTerm(candidate, document);
      incrementTermFreq(term, 1);
      
      if (termDocuments.containsKey(term)) {
        termDocuments.get(term).add(documentSource);
      }
      else {
        Set<String> docNames = new HashSet<String>();
        docNames.add(documentSource);
        termDocuments.put(term, docNames);
      }
    }
  }

  
  protected void calculateScores() {
    for (Term term : termFrequencies.keySet()) {
      int tf = termFrequencies.get(term);
      int df = docFreqSource.getDocFrequency(term);
      int n = docFreqSource.getTotalDocs();
      double score = TfCalculation.calculate(tfCalculation, tf) * IdfCalculation.calculate(idfCalculation, df, n);
      rawTermScores.put(term, Double.valueOf(score));
      termScores.put(term, Utilities.normalizeScore(score));
    }
    
    termsByDescendingScore = new ArrayList<Term>(termScores.keySet());
    Collections.sort(termsByDescendingScore, new TermComparatorByDescendingScore(termScores));
    
    if (debugMode) {
      System.out.println("Termbank: nbr of terms = " + termsByDescendingScore.size());
    }
  }
  
  
  protected void resetScores() {
    termDocuments    = new HashMap<Term, Set<String>>();
    termScores       = new HashMap<Term, Double>();
    rawTermScores    = new HashMap<Term, Double>();
    termsByDescendingScore      = new ArrayList<Term>();
    termsByDescendingFrequency = new ArrayList<Term>();
    termsByDescendingDocFrequency = new ArrayList<Term>();
    termFrequencies = new HashMap<Term, Integer>();
    docFrequencies = new HashMap<Term, Integer>();
    documentCount = 0;
  }


  public int getDocCount() {
    return this.documentCount;
  }
  
  /***** CREOLE PARAMETERS *****/
  
  @CreoleParameter(comment = "document frequency bank (unset = create from these corpora)")
  public void setDocFreqSource(DocumentFrequencyBank dfb) {
    this.docFreqSource = dfb;
  }
  
  public DocumentFrequencyBank getDocFreqSource() {
    return this.docFreqSource;
  }
  

  @CreoleParameter(comment = "term frequency calculation",
          defaultValue = "Logarithmic")
  public void setTfCalculation(TfCalculation mode) {
    this.tfCalculation = mode;
  }
  
  public TfCalculation getTfCalculation() {
    return this.tfCalculation;
  }
          

          
  @CreoleParameter(comment = "inverted document frequency calculation",
          defaultValue = "Logarithmic")
  public void setIdfCalculation(IdfCalculation mode) {
    this.idfCalculation = mode;
  }
  
  public IdfCalculation getIdfCalculation() {
    return this.idfCalculation;
  }

  
  /* override default value from AbstractTermbank   */
  @CreoleParameter(defaultValue = "tfIdf")
  public void setScoreProperty(String name) {
    super.setScoreProperty(name);
  }


  public String getCsvHeader() {
    StringBuilder sb = new StringBuilder();
    sb.append(StringEscapeUtils.escapeCsv("Term"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Lang"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Type"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("ScoreType"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Score"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Document_Count"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Ref_Doc_Frequency"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Term_Frequency"));
    return sb.toString();
  }


  public String getCsvLine(Term term) {
      StringBuilder sb = new StringBuilder();
      sb.append(StringEscapeUtils.escapeCsv(term.getTermString()));
      sb.append(',');
      sb.append(StringEscapeUtils.escapeCsv(term.getLanguageCode()));
      sb.append(',');
      sb.append(StringEscapeUtils.escapeCsv(term.getType()));
      sb.append(',');
      sb.append(StringEscapeUtils.escapeCsv(this.getScoreProperty()));
      sb.append(',');
      sb.append(StringEscapeUtils.escapeCsv(this.getScore(term).toString()));
      sb.append(',');
      sb.append(StringEscapeUtils.escapeCsv(Integer.toString(this.getDocFrequency(term))));
      sb.append(',');
      sb.append(StringEscapeUtils.escapeCsv(Integer.toString(this.docFreqSource.getDocFrequency(term))));
      sb.append(',');
      sb.append(StringEscapeUtils.escapeCsv(Integer.toString(this.getTermFrequency(term))));
      return sb.toString();
  }


  protected void prepare() throws ResourceInstantiationException {
    if ( (corpora == null) || (corpora.size() == 0) ) {
      throw new ResourceInstantiationException("No corpora given");
    }
    
    // If no DFB is specified, we create one from the given corpora
    if (this.docFreqSource == null) {
      FeatureMap dfbParameters = Factory.newFeatureMap();
      dfbParameters.put("inputASName", this.inputASName);
      dfbParameters.put("languageFeature", this.languageFeature);
      dfbParameters.put("inputAnnotationFeature", this.inputAnnotationFeature);
      dfbParameters.put("corpora", this.corpora);
      dfbParameters.put("debugMode", this.debugMode);

      DocumentFrequencyBank dfb = (DocumentFrequencyBank) Factory.createResource(DocumentFrequencyBank.class.getName(), dfbParameters);
      this.setDocFreqSource(dfb);
    }
  }

}
