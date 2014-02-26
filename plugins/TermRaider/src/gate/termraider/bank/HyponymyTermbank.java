/*
 *  Copyright (c) 2008--2014, The University of Sheffield. See the file
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

import gate.creole.metadata.*;
import gate.gui.ActionsPublisher;
import gate.*;
import gate.termraider.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import java.util.*;



@CreoleResource(name = "HyponymyTermbank",
        icon = "termbank-lr.png",
        comment = "TermRaider Termbank derived from head/string hyponymy")

public class HyponymyTermbank extends AbstractTermbank
    implements ActionsPublisher  {
  private static final long serialVersionUID = 6846311108437600048L;
  

  
  /* EXTRA CREOLE PARAMETERS */
  protected List<String> inputHeadFeatures;

  
  /* EXTRA DATA FOR ANALYSIS */
  private Map<Term, Set<String>> termHeads;
  private Map<Term, Set<String>> termHyponyms;

  
  /* Methods for the debugging GUI to get the data   */
  public Map<Term, Set<String>> getTermHeads() {
    return this.termHeads;
  }

  public Map<Term, Set<String>> getTermHyponyms() {
    return this.termHyponyms;
  }

  
  
  private double calculateOneRawScore(Term term) {
    double docFreq = (double) getSetFromMap(termDocuments, term).size();
    double hyponyms = (double) getSetFromMap(termHyponyms, term).size();
    return docFreq * (1.0F + hyponyms);
  }

  
  protected void processDocument(Document document) {
    String documentSource = Utilities.sourceOrName(document);
    AnnotationSet candidates = document.getAnnotations(inputASName).get(inputAnnotationTypes);
    
    for (Annotation candidate : candidates) {
      Term term = makeTerm(candidate, document);

      FeatureMap features = candidate.getFeatures();
      String head = Utils.stringFor(document, candidate);
      
      for (String key : inputHeadFeatures) {
        if (features.containsKey(key)) {
          head = features.get(key).toString();
          break;
        }
      }
      
      addToMapSet(termDocuments, term, documentSource);
      addToMapSet(termHeads, term, head);
      incrementTermFreq(term, 1);
    }
  }

  
  private void addToMapSet(Map<Term, Set<String>> map, Term key, String value) {
    Set<String> valueSet;
    if (map.containsKey(key)) {
      valueSet = map.get(key);
    }
    else {
      valueSet = new HashSet<String>();
    }
    
    valueSet.add(value);
    map.put(key, valueSet);
  }
  
  private Set<String> getSetFromMap(Map<Term, Set<String>> map, Term key) {
    if (map.containsKey(key)) {
      return map.get(key);
    }
    
    //implied else
    Set<String> valueSet = new HashSet<String>();
    map.put(key, valueSet);
    return valueSet;
  }
  
  

  public void calculateScores() {
    Set<Term> terms = termHeads.keySet();
    Set<String> headsI, headsJ;
    
    for (Term termI : terms) {
      headsI = termHeads.get(termI);
      
      for (Term termJ : terms) {
        if (termJ.getTermString().contains(termI.getTermString())
                && (! termI.equals(termJ))) {
          headsJ = termHeads.get(termJ);
          
          hyponymLoop:
            for (String headI : headsI) {
              for (String headJ : headsJ) {
                if (headI.endsWith(headJ)) {
                  addToMapSet(termHyponyms, termI, termJ.getTermString());
                  break hyponymLoop;
                }
              }
            }
        }
      }
    }
    
    for (Term term : terms) {
      double rawScore = calculateOneRawScore(term);
      rawTermScores.put(term, rawScore);
      double score = Utilities.normalizeScore(rawScore);
      termScores.put(term, score);
    }
    
    termsByDescendingScore = new ArrayList<Term>(termScores.keySet());
    Collections.sort(termsByDescendingScore, new TermComparatorByDescendingScore(termScores));
    
    termsByDescendingFrequency = new ArrayList<Term>(termScores.keySet());
    Collections.sort(termsByDescendingFrequency, new TermComparatorByDescendingScore(termFrequencies));
    
    termsByDescendingDocFrequency = new ArrayList<Term>(termScores.keySet());
    Collections.sort(termsByDescendingFrequency, new TermComparatorByDescendingScore(docFrequencies));
    
    if (debugMode) {
      System.out.println("Termbank: nbr of terms = " + termsByDescendingScore.size());
    }
  }
  
  
  protected void resetScores() {
    termHeads       = new HashMap<Term, Set<String>>();
    termHyponyms    = new HashMap<Term, Set<String>>();
    termDocuments   = new HashMap<Term, Set<String>>();
    termScores      = new HashMap<Term, Double>();
    rawTermScores   = new HashMap<Term, Double>();
    termsByDescendingScore     = new ArrayList<Term>();
    termsByDescendingFrequency = new ArrayList<Term>();
    termsByDescendingDocFrequency = new ArrayList<Term>();
    termFrequencies = new HashMap<Term, Integer>();
    docFrequencies = new HashMap<Term, Integer>();
  }

  
  protected void initializeScoreTypes() {
    this.scoreTypes = new ArrayList<ScoreType>();
    this.scoreTypes.add(new ScoreType(scoreProperty));
    // TODO this TB needs a whole different kettle of fish
    //this.termFrequencyST = new ScoreType("termFrequency");
    //this.scoreTypes.add(termFrequencyST);
    //this.localDocFrequencyST = new ScoreType("localDocFrequency");
    //this.scoreTypes.add(localDocFrequencyST);
    //this.refDocFrequencyST = new ScoreType("refDocFrequency");
    //this.scoreTypes.add(refDocFrequencyST);
  }

  
  
  public String getCsvHeader() {
    StringBuilder sb = new StringBuilder();
    sb.append(StringEscapeUtils.escapeCsv("Term"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Lang"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Type"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("ScoreType"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Score"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Document_Count"));
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
      sb.append(StringEscapeUtils.escapeCsv(Integer.toString(this.getTermFrequency(term))));
      return sb.toString();
  }

  /***** CREOLE PARAMETERS *****/

  @CreoleParameter(comment = "Annotation features (in order) to be scanned as terms' heads")
  public void setInputHeadFeatures(List<String> list) {
    this.inputHeadFeatures = list;
  }
  
  public List<String> getInputHeadFeatures() {
    return this.inputHeadFeatures;
  }
  
  
  /* override default value from AbstractTermbank   */
  @CreoleParameter(defaultValue = "kyotoDomainRelevance")
  public void setScoreProperty(String name) {
    super.setScoreProperty(name);
  }


}
