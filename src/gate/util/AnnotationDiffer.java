/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 28/01/2003
 *
 *  $Id$
 *
 */
package gate.util;

import java.util.*;
import gate.*;
public class AnnotationDiffer {
  /**
   * Computes a diff between two collections of annotations.
   * @param key
   * @param response
   */
  public void calculateDiff(Collection key, Collection response){
    //initialise data structures
    keyList = new ArrayList(key);
    responseList = new ArrayList(response);

    keyChoices = new ArrayList(keyList.size());
    keyChoices.addAll(Collections.nCopies(keyList.size(), null));
    responseChoices = new ArrayList(responseList.size());
    responseChoices.addAll(Collections.nCopies(responseList.size(), null));

    possibleChoices = new ArrayList();

    //1) try all possible pairings
    for(int i = 0; i < keyList.size(); i++){
      for(int j =0; j < responseList.size(); j++){
        Annotation keyAnn = (Annotation)keyList.get(i);
        Annotation resAnn = (Annotation)responseList.get(j);
        Choice choice = null;
        if(significantFeaturesSet == null){
          //full comaptibility required
          if(keyAnn.isCompatible(resAnn)){
            choice = new Choice(i, j, CORRECT);
          }else if(keyAnn.isPartiallyCompatible(resAnn)){
            choice = new Choice(i, j, PARTIALLY_CORRECT);
          }
        }else{
          //compatibility tests restricted to a set of features
          if(keyAnn.isCompatible(resAnn, significantFeaturesSet)){
            choice = new Choice(i, j, CORRECT);
          }else if(keyAnn.isPartiallyCompatible(resAnn, significantFeaturesSet)){
            choice = new Choice(i, j, PARTIALLY_CORRECT);
          }
        }
        //add the new choice if any
        if (choice != null) {
          addChoice(choice, i, keyChoices);
          addChoice(choice, j, responseChoices);
          possibleChoices.add(choice);
        }
      }//for j
    }//for i

    //2) from all possible pairings, find the maximal set that also
    //maximises the total score
    Collections.sort(possibleChoices);
    Collections.reverse(possibleChoices);
    finalChoices = new ArrayList();
    correctMatches = 0;
    partiallyCorrectMatches = 0;

    while(!possibleChoices.isEmpty()){
      Choice bestChoice = (Choice)possibleChoices.remove(0);
      bestChoice.consume();
      finalChoices.add(bestChoice);
      switch(bestChoice.type){
        case CORRECT:{
          correctMatches++;
          break;
        }
        case PARTIALLY_CORRECT:{
          partiallyCorrectMatches++;
          break;
        }
      }
    }
  }

  public double getPrecisionStrict(){
    return (double)correctMatches / responseList.size();
  }

  public double getRecallStrict(){
    return (double)correctMatches / keyList.size();
  }

  public double getPrecisionLenient(){
    return (double)(correctMatches + partiallyCorrectMatches) / responseList.size();
  }

  public double getRecallLenient(){
    return (double)(correctMatches + partiallyCorrectMatches) / keyList.size();
  }

  public double getFMeasureStrict(double beta){
    double precision = getPrecisionStrict();
    double recall = getRecallStrict();
    double betaSq = beta * beta;
    return ((betaSq + 1) * precision * recall ) /
           (betaSq * precision + recall);
  }

  public double getFMeasureLenient(double beta){
    double precision = getPrecisionLenient();
    double recall = getRecallLenient();
    double betaSq = beta * beta;
    return ((betaSq + 1) * precision * recall ) /
           (betaSq * precision + recall);
  }

  public int getFalsePositivesStrict(){
    return responseList.size() - correctMatches;
  }

  public int getFalsePositivesLenient(){
    return responseList.size() - correctMatches - partiallyCorrectMatches;
  }

  public void printMissmatches(){
    //get the partial correct matches
    Iterator iter = finalChoices.iterator();
    while(iter.hasNext()){
      Choice aChoice = (Choice)iter.next();
      switch(aChoice.type){
        case PARTIALLY_CORRECT:{
          System.out.println("Missmatch (partially correct):");
          System.out.println("Key: " + keyList.get(aChoice.keyIndex).toString());
          System.out.println("Response: " + responseList.get(aChoice.responseIndex).toString());
          break;
        }
      }
    }

    //get the unmatched keys
    for(int i = 0; i < keyChoices.size(); i++){
      List aList = (List)keyChoices.get(i);
      if(aList == null || aList.isEmpty()){
        System.out.println("Unmatched Key: " + keyList.get(i).toString());
      }
    }

    //get the unmatched responses
    for(int i = 0; i < responseChoices.size(); i++){
      List aList = (List)responseChoices.get(i);
      if(aList == null || aList.isEmpty()){
        System.out.println("Unmatched Key: " + responseList.get(i).toString());
      }
    }

  }
  /**
   * Performs some basic checks over the internal data structures from the last
   * run.
   * @throws Exception
   */
  void sanityCheck()throws Exception{
    //all keys and responses should have at most one choice left
    Iterator iter =keyChoices.iterator();
    while(iter.hasNext()){
      List choices = (List)iter.next();
      if(choices != null){
        if(choices.size() > 1){
          throw new Exception("Multiple choices found!");
        }else if(!choices.isEmpty()){
          //size must be 1
          Choice aChoice = (Choice)choices.get(0);
          //the SAME choice should be found for the associated response
          List otherChoices = (List)responseChoices.get(aChoice.responseIndex);
          if(otherChoices == null ||
             otherChoices.size() != 1 ||
             otherChoices.get(0) != aChoice){
            throw new Exception("Reciprocity error!");
          }
        }
      }
    }

    iter =responseChoices.iterator();
    while(iter.hasNext()){
      List choices = (List)iter.next();
      if(choices != null){
        if(choices.size() > 1){
          throw new Exception("Multiple choices found!");
        }else if(!choices.isEmpty()){
          //size must be 1
          Choice aChoice = (Choice)choices.get(0);
          //the SAME choice should be found for the associated response
          List otherChoices = (List)keyChoices.get(aChoice.keyIndex);
          if(otherChoices == null){
            throw new Exception("Reciprocity error : null!");
          }else if(otherChoices.size() != 1){
            throw new Exception("Reciprocity error: not 1!");
          }else if(otherChoices.get(0) != aChoice){
            throw new Exception("Reciprocity error: different!");
          }
        }
      }
    }
  }
  /**
   *
   * @param choice the choice to be added
   * @param index the index in the list of choices
   * @param list the list of choices where the choice should be added
   */
  protected void addChoice(Choice choice, int index, List listOfChoices){
    List existingChoices = (List)listOfChoices.get(index);
    if(existingChoices == null){
      existingChoices = new ArrayList();
      listOfChoices.set(index, existingChoices);
    }
    existingChoices.add(choice);
  }

  public java.util.Set getSignificantFeaturesSet() {
    return significantFeaturesSet;
  }

  public void setSignificantFeaturesSet(java.util.Set significantFeaturesSet) {
    this.significantFeaturesSet = significantFeaturesSet;
  }

  /**
   * Represents a pairing of a key annotation with a response annotation and
   * the associated score for that pairing.
   */
  class Choice implements Comparable{
    Choice(int keyIndex, int responseIndex, int type) {
      this.keyIndex = keyIndex;
      this.responseIndex = responseIndex;
      this.type = type;
      scoreCalculated = false;
    }

    int getScore(){
      if(scoreCalculated) return score;
      else{
        calculateScore();
        return score;
      }
    }

    /**
     * Removes all mutually exclusive OTHER choices possible from
     * the data structures.
     * <tt>this</tt> gets removed from {@link #possibleChoices} as well.
     */
    public void consume(){
      possibleChoices.remove(this);
      List sameKeyChoices = (List)keyChoices.get(keyIndex);
      sameKeyChoices.remove(this);
      possibleChoices.removeAll(sameKeyChoices);

      List sameResponseChoices = (List)responseChoices.get(responseIndex);
      sameResponseChoices.remove(this);
      possibleChoices.removeAll(sameResponseChoices);

      Iterator iter = new ArrayList(sameKeyChoices).iterator();
      while(iter.hasNext()){
        ((Choice)iter.next()).remove();
      }
      iter = new ArrayList(sameResponseChoices).iterator();
      while(iter.hasNext()){
        ((Choice)iter.next()).remove();
      }
      sameKeyChoices.add(this);
      sameResponseChoices.add(this);
    }

    /**
     * Removes this choice from the two lists it belongs to
     */
    protected void remove(){
      List fromKey = (List)keyChoices.get(keyIndex);
      fromKey.remove(this);
      List fromResponse = (List)responseChoices.get(responseIndex);
      fromResponse.remove(this);
    }
    /**
     * Compares two choices:
     * the better score is preferred;
     * for the same score the better type is preferred (exact matches are
     * preffered to partial ones).
     * @param other
     * @return
     */
    public int compareTo(Object other){
      int res = getScore() - ((Choice)other).getScore();
      if(res == 0) res = type - ((Choice)other).type;
      return res;
    }

    /**
     * Calculates the score for this choice as:
     * type - sum of all the types of all OTHER mutually exclusive choices
     */
    void calculateScore(){
      //this needs to be a set so we don't count conflicts twice
      Set conflictSet = new HashSet();
      //add all the choices from the same response annotation
      conflictSet.addAll((List)responseChoices.get(responseIndex));
      //add all the choices from the same key annotation
      conflictSet.addAll((List)keyChoices.get(keyIndex));
      //remove this choice from the conflict set
      conflictSet.remove(this);
      score = type;
      Iterator conflictIter = conflictSet.iterator();
      while(conflictIter.hasNext()) score -= ((Choice)conflictIter.next()).type;
      scoreCalculated = true;
    }

    int keyIndex;
    int responseIndex;
    int type;
    int score;
    boolean scoreCalculated;
  }

  public static final int CORRECT = 2;
  public static final int PARTIALLY_CORRECT = 1;
  public static final int DIFFERENT = 0;

  private java.util.Set significantFeaturesSet;

  protected int correctMatches;
  protected int partiallyCorrectMatches;

  /**
   * A list with all the key annotations
   */
  protected List keyList;

  /**
   * A list with all the response annotations
   */
  protected List responseList;

  /**
   * A list of lists representing all possible choices for each key
   */
  protected List keyChoices;

  /**
   * A list of lists representing all possible choices for each response
   */
  protected List responseChoices;

  /**
   * All the posible choices are added to this list for easy iteration.
   */
  protected List possibleChoices;

  /**
   * A list with the choices selected for the best result.
   */
  protected List finalChoices;

}