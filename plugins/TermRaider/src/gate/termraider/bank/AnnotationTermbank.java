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
import gate.termraider.bank.modes.*;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;



@CreoleResource(name = "AnnotationTermbank",
    icon = "termbank-lr.png",
    comment = "TermRaider Termbank derived from document annotations")
public class AnnotationTermbank extends AbstractTermbank
    implements ActionsPublisher  {
  private static final long serialVersionUID = 5433955185886301874L;
  
  /* EXTRA CREOLE PARAMETERS */
  protected String inputScoreFeature;
  private MergingMode mergingMode;

  /* EXTRA DATA FOR ANALYSIS */
  private Map<Term, List<Double>>  termIndividualScores;

  
  
  protected void processDocument(Document document) {
    String documentSource = Utilities.sourceOrName(document);
    AnnotationSet candidates = document.getAnnotations(inputASName).get(inputAnnotationTypes);

    for (Annotation candidate : candidates) {
      Term term = makeTerm(candidate, document);
      FeatureMap fm = candidate.getFeatures();
      if (fm.containsKey(inputScoreFeature)) {
        incrementTermFreq(term, 1);
        
        double score = ((Number) fm.get(inputScoreFeature)).doubleValue();
        if (termIndividualScores.containsKey(term)) {
          List<Double> scoreList = termIndividualScores.get(term);
          scoreList.add(score);
          termDocuments.get(term).add(documentSource);
        }
        else {
          List<Double> scoreList = new ArrayList<Double>();
          scoreList.add(score);
          Set<String> docNames = new HashSet<String>();
          docNames.add(documentSource);
          termDocuments.put(term, docNames);
          termIndividualScores.put(term, scoreList);
        }
      }
    }
  }


  public void calculateScores() {
      double score;
        
      for (Term term : termIndividualScores.keySet()) {
        if (mergingMode == MergingMode.MAXIMUM) {
          score = Collections.max(termIndividualScores.get(term));
        }
        else if (mergingMode == MergingMode.MINIMUM) {
          score = Collections.min(termIndividualScores.get(term));
        }
        else { // must be MEAN
          score = Utilities.meanDoubleList(termIndividualScores.get(term));
        }

        rawTermScores.put(term, score);
        termScores.put(term, Utilities.normalizeScore(score));
      }
      
      termsByDescendingScore = new ArrayList<Term>(termScores.keySet());
      Collections.sort(termsByDescendingScore, new TermComparatorByDescendingScore(termScores));

    if (debugMode) {
      System.out.println("Termbank: nbr of terms = " + termsByDescendingScore.size());
    }
  }

  
  protected void resetScores() {
    termIndividualScores = new HashMap<Term, List<Double>>();
    termDocuments    = new HashMap<Term, Set<String>>();
    termScores       = new HashMap<Term, Double>();
    rawTermScores = new HashMap<Term, Double>();
    termsByDescendingScore      = new ArrayList<Term>();
    termsByDescendingScore     = new ArrayList<Term>();
    termsByDescendingFrequency = new ArrayList<Term>();
    termsByDescendingDocFrequency = new ArrayList<Term>();
    termFrequencies = new HashMap<Term, Integer>();
    docFrequencies = new HashMap<Term, Integer>();
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

  @CreoleParameter(comment = "annotation feature containing the score to index",
          defaultValue = "localAugTfIdf")
  public void setInputScoreFeature(String annScoreFeature) {
    this.inputScoreFeature = annScoreFeature;    
  }
  
  public String getInputScoreFeature() {
    return this.inputScoreFeature;
  }
  
  @CreoleParameter(comment = "method for aggregating local scores",
          defaultValue = "MAXIMUM")
  public void setMergingMode(MergingMode mode) {
    this.mergingMode = mode;
  }
  
  public MergingMode getMergingMode() {
    return this.mergingMode;
  }
  
  /* override default value from AbstractTermbank   */
  @CreoleParameter(defaultValue = "tfIdfAug")
  public void setScoreProperty(String name) {
    super.setScoreProperty(name);
  }


}
