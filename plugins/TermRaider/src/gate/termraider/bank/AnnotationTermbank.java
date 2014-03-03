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
import gate.termraider.modes.*;

import java.util.*;


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
  private ScoreType termFrequencyST, localDocFrequencyST;
  
  
  protected void processDocument(Document document) {
    documentCount++;
    String documentSource = Utilities.sourceOrName(document);
    AnnotationSet candidates = document.getAnnotations(inputASName).get(inputAnnotationTypes);

    for (Annotation candidate : candidates) {
      Term term = makeTerm(candidate, document);
      FeatureMap fm = candidate.getFeatures();
      if (fm.containsKey(inputScoreFeature)) {
        Utilities.incrementScoreTermValue(scores, termFrequencyST, term, 1);
        
        double score = ((Number) fm.get(inputScoreFeature)).doubleValue();
        Utilities.addToMapSet(termDocuments, term, documentSource);
        
        if (termIndividualScores.containsKey(term)) {
          List<Double> scoreList = termIndividualScores.get(term);
          scoreList.add(score);
        }
        else {
          List<Double> scoreList = new ArrayList<Double>();
          scoreList.add(score);
          termIndividualScores.put(term, scoreList);
        }
      }
    }
  }


  public void calculateScores() {
    for (Term term : termDocuments.keySet()) {
      languages.add(term.getLanguageCode());
      types.add(term.getType());
      
      Double score = MergingMode.calculate(mergingMode, termIndividualScores.get(term));
      Utilities.setScoreTermValue(scores, getDefaultScoreType(), term, score);
      int localDF = termDocuments.get(term).size();
      Utilities.setScoreTermValue(scores, localDocFrequencyST, term, localDF);
    }
    
    if (debugMode) {
      System.out.println("Termbank: nbr of terms = " + termDocuments.size());
    }
  }

  
  protected void resetScores() {
    scores = new HashMap<ScoreType, Map<Term,Number>>();
    for (ScoreType st : scoreTypes) {
      scores.put(st, new HashMap<Term, Number>());
    }
    termIndividualScores = new HashMap<Term, List<Double>>();
    termDocuments        = new HashMap<Term, Set<String>>();
    languages = new HashSet<String>();
    types = new HashSet<String>();
  }

  
  protected void initializeScoreTypes() {
    this.scoreTypes = new ArrayList<ScoreType>();
    this.scoreTypes.add(new ScoreType(scoreProperty));
    this.termFrequencyST = new ScoreType("termFrequency");
    this.scoreTypes.add(termFrequencyST);
    this.localDocFrequencyST = new ScoreType("localDocFrequency");
    this.scoreTypes.add(localDocFrequencyST);
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


  @Override
  public Map<String, String> getMiscDataForGui() {
    Map<String, String> result = new HashMap<String, String>();
    result.put("nbr of local documents", String.valueOf(this.documentCount));
    result.put("nbr of terms", String.valueOf(this.getDefaultScores().size()));
    return result;
  }

}
