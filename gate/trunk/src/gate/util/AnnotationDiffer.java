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

import gate.Annotation;

public class AnnotationDiffer {


  public static interface Pairing{
    public Annotation getKey();
    public Annotation getResponse();
    public int getType();
  }

  /**
   * Computes a diff between two collections of annotations.
   * @param key
   * @param response
   * @return a list of {@link Pairing} objects representing the pairing set
   * that results in the best score.
   */
  public List calculateDiff(Collection key, Collection response){
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
        PairingImpl choice = null;
        if(significantFeaturesSet == null){
          //full comaptibility required
          if(keyAnn.isCompatible(resAnn)){
            choice = new PairingImpl(i, j, CORRECT);
          }else if(keyAnn.isPartiallyCompatible(resAnn)){
            choice = new PairingImpl(i, j, PARTIALLY_CORRECT);
          }
        }else{
          //compatibility tests restricted to a set of features
          if(keyAnn.isCompatible(resAnn, significantFeaturesSet)){
            choice = new PairingImpl(i, j, CORRECT);
          }else if(keyAnn.isPartiallyCompatible(resAnn, significantFeaturesSet)){
            choice = new PairingImpl(i, j, PARTIALLY_CORRECT);
          }
        }
        //add the new choice if any
        if (choice != null) {
          addPairing(choice, i, keyChoices);
          addPairing(choice, j, responseChoices);
          possibleChoices.add(choice);
        }
      }//for j
    }//for i

    //2) from all possible pairings, find the maximal set that also
    //maximises the total score
    Collections.sort(possibleChoices, new PairingScoreComparator());
    Collections.reverse(possibleChoices);
    finalChoices = new ArrayList();
    correctMatches = 0;
    partiallyCorrectMatches = 0;
    missing = 0;
    spurious = 0;

    while(!possibleChoices.isEmpty()){
      PairingImpl bestChoice = (PairingImpl)possibleChoices.remove(0);
      bestChoice.consume();
      finalChoices.add(bestChoice);
      switch(bestChoice.type){
        case CORRECT:{
          if(correctAnnotations == null) correctAnnotations = new HashSet();
          correctAnnotations.add(bestChoice.getResponse());
          correctMatches++;
          break;
        }
        case PARTIALLY_CORRECT:{
          if(partiallyCorrectAnnotations == null) partiallyCorrectAnnotations = new HashSet();
          partiallyCorrectAnnotations.add(bestChoice.getResponse());
          partiallyCorrectMatches++;
          break;
        }
      }
    }
    //add choices for the incorrect matches (MISSED, SPURIOUS)
    //get the unmatched keys
    for(int i = 0; i < keyChoices.size(); i++){
      List aList = (List)keyChoices.get(i);
      if(aList == null || aList.isEmpty()){
        if(missingAnnotations == null) missingAnnotations = new HashSet();
        missingAnnotations.add((Annotation)(keyList.get(i)));
        finalChoices.add(new PairingImpl(i, -1, WRONG));
        missing ++;
      }
    }

    //get the unmatched responses
    for(int i = 0; i < responseChoices.size(); i++){
      List aList = (List)responseChoices.get(i);
      if(aList == null || aList.isEmpty()){
        if(spuriousAnnotations == null) spuriousAnnotations = new HashSet();
        spuriousAnnotations.add((Annotation)(responseList.get(i)));
        finalChoices.add(new PairingImpl(-1, i, WRONG));
        spurious ++;
      }
    }

    return finalChoices;
  }

  public double getPrecisionStrict(){
    return (double)((double)correctMatches / (double)responseList.size());
  }

  public double getRecallStrict(){
    return (double)((double)correctMatches / (double)keyList.size());
  }

  public double getPrecisionLenient(){
    return (double)((double)(correctMatches + partiallyCorrectMatches) / (double)responseList.size());
  }

  public double getPrecisionAverage() {
    return (double)((double)(getPrecisionLenient() + getPrecisionStrict()) / (double)(2.0));
  }

  public double getRecallLenient(){
    return (double)((double)(correctMatches + partiallyCorrectMatches) / (double)keyList.size());
  }

  public double getRecallAverage() {
    return (double)((double)(getRecallLenient() + getRecallStrict()) / (double)(2.0));
  }

  public double getFMeasureStrict(double beta){
    double precision = getPrecisionStrict();
    double recall = getRecallStrict();
    double betaSq = beta * beta;
    return (double)(((betaSq + 1) * precision * recall ) /
           (double)(betaSq * precision + recall));
  }

  public double getFMeasureLenient(double beta){
    double precision = getPrecisionLenient();
    double recall = getRecallLenient();
    double betaSq = beta * beta;
    return (double)(((betaSq + 1) * precision * recall ) /
           (double)(betaSq * precision + recall));
  }

  public double getFMeasureAverage(double beta) {
    return (double)(((double)(getFMeasureLenient(beta) + getFMeasureStrict(beta)) / (double)(2.0)));
  }

  public int getCorrectMatches(){
    return correctMatches;
  }

  public int getPartiallyCorrectMatches(){
    return partiallyCorrectMatches;
  }

  public int getMissing(){
    return missing;
  }

  public int getSpurious(){
    return spurious;
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
      PairingImpl aChoice = (PairingImpl)iter.next();
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
        System.out.println("Missed Key: " + keyList.get(i).toString());
      }
    }

    //get the unmatched responses
    for(int i = 0; i < responseChoices.size(); i++){
      List aList = (List)responseChoices.get(i);
      if(aList == null || aList.isEmpty()){
        System.out.println("Spurious Response: " + responseList.get(i).toString());
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
          PairingImpl aChoice = (PairingImpl)choices.get(0);
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
          PairingImpl aChoice = (PairingImpl)choices.get(0);
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
   * @param pairing the pairing to be added
   * @param index the index in the list of pairings
   * @param listOfPairings the list of {@link Pairing}s where the
   * pairing should be added
   */
  protected void addPairing(PairingImpl pairing, int index, List listOfPairings){
    List existingChoices = (List)listOfPairings.get(index);
    if(existingChoices == null){
      existingChoices = new ArrayList();
      listOfPairings.set(index, existingChoices);
    }
    existingChoices.add(pairing);
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
   public class PairingImpl implements Pairing{
    PairingImpl(int keyIndex, int responseIndex, int type) {
      this.keyIndex = keyIndex;
      this.responseIndex = responseIndex;
      this.type = type;
      scoreCalculated = false;
	    }

    public int getScore(){
      if(scoreCalculated) return score;
      else{
        calculateScore();
        return score;
      }
    }

    public Annotation getKey(){
      return keyIndex == -1 ? null : (Annotation)keyList.get(keyIndex);
    }

    public Annotation getResponse(){
      return responseIndex == -1 ? null :
        (Annotation)responseList.get(responseIndex);
    }

    public int getType(){
      return type;
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
        ((PairingImpl)iter.next()).remove();
      }
      iter = new ArrayList(sameResponseChoices).iterator();
      while(iter.hasNext()){
        ((PairingImpl)iter.next()).remove();
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
      while(conflictIter.hasNext()) score -= ((PairingImpl)conflictIter.next()).type;
      scoreCalculated = true;
    }

    int keyIndex;
    int responseIndex;
    int type;
    int score;
    boolean scoreCalculated;
  }

	protected static class PairingScoreComparator implements Comparator{
    /**
     * Compares two choices:
     * the better score is preferred;
     * for the same score the better type is preferred (exact matches are
     * preffered to partial ones).
     * @return a positive value if the first pairing is better than the second,
     * zero if they score the same or negative otherwise.
     */

	  public int compare(Object o1, Object o2){
	    PairingImpl first = (PairingImpl)o1;
	    PairingImpl second = (PairingImpl)o2;
      int res = first.getScore() - second.getScore();
      if(res == 0) res = first.getType() - second.getType();
      return res;
	  }
	}


	public static class PairingOffsetComparator implements Comparator{
    /**
     * Compares two choices based on start offset of key (or response
     * if key not present) and type if offsets are equal.
     */
	  public int compare(Object o1, Object o2){
	    Pairing first = (Pairing)o1;
	    Pairing second = (Pairing)o2;
	    Annotation key1 = first.getKey();
	    Annotation key2 = second.getKey();
	    Annotation res1 = first.getResponse();
	    Annotation res2 = second.getResponse();
	    Long start1 = key1 == null ? null : key1.getStartNode().getOffset();
	    if(start1 == null) start1 = res1.getStartNode().getOffset();
	    Long start2 = key2 == null ? null : key2.getStartNode().getOffset();
	    if(start2 == null) start2 = res2.getStartNode().getOffset();
	    int res = start1.compareTo(start2);
	    if(res == 0){
	      //compare by type
	      res = second.getType() - first.getType();
	    }

//
//
//
//	    //choices with keys are smaller than ones without
//	    if(key1 == null && key2 != null) return 1;
//	    if(key1 != null && key2 == null) return -1;
//	    if(key1 == null){
//	      //both keys are null
//	      res = res1.getStartNode().getOffset().
//	      		compareTo(res2.getStartNode().getOffset());
//	      if(res == 0) res = res1.getEndNode().getOffset().
//      				compareTo(res2.getEndNode().getOffset());
//	      if(res == 0) res = second.getType() - first.getType();
//	    }else{
//	      //both keys are present
//	      res = key1.getStartNode().getOffset().compareTo(
//	          key2.getStartNode().getOffset());
//
//	      if(res == 0){
//		      //choices with responses are smaller than ones without
//		      if(res1 == null && res2 != null) return 1;
//		      if(res1 != null && res2 == null) return -1;
//		      if(res1 != null){
//			      res = res1.getStartNode().getOffset().
//    						compareTo(res2.getStartNode().getOffset());
//		      }
//		      if(res == 0)res = key1.getEndNode().getOffset().compareTo(
//		              key2.getEndNode().getOffset());
//		      if(res == 0 && res1 != null){
//				      res = res1.getEndNode().getOffset().
//	    						compareTo(res2.getEndNode().getOffset());
//		      }
//		      if(res == 0) res = second.getType() - first.getType();
//	      }
//	    }
      return res;
	  }

	}

  /**
   * A method that returns specific type of annotations
   * @param type
   * @return a {@link Set} of {@link Pairing}s.
   */
  public Set getAnnotationsOfType(int type) {
    switch(type) {
      case CORRECT_TYPE:
        return (correctAnnotations == null)? new HashSet() : correctAnnotations;
      case PARTIALLY_CORRECT_TYPE:
        return (partiallyCorrectAnnotations == null) ? new HashSet() : partiallyCorrectAnnotations;
      case SPURIOUS_TYPE:
        return (spuriousAnnotations == null) ? new HashSet() : spuriousAnnotations;
      case MISSING_TYPE:
        return (missingAnnotations == null) ? new HashSet() : missingAnnotations;
      default:
        return new HashSet();
    }
  }


  public HashSet correctAnnotations, partiallyCorrectAnnotations, missingAnnotations, spuriousAnnotations;


  /** A correct type when all annotation are corect represented by Green color*/
  public static final int CORRECT_TYPE = 1;
  /** A partially correct type when all annotation are corect represented
   *  by Blue color*/
  public static final int PARTIALLY_CORRECT_TYPE = 2;
  /** A spurious type when annotations in response were not present in key.
   *  Represented by Red color*/
  public static final int SPURIOUS_TYPE = 3;
  /** A missing type when annotations in key were not present in response
   *  Represented by Yellow color*/
  public static final int MISSING_TYPE = 4;


  public static final int CORRECT = 2;
  public static final int PARTIALLY_CORRECT = 1;
  public static final int WRONG = 0;

  private java.util.Set significantFeaturesSet;

  protected int correctMatches;
  protected int partiallyCorrectMatches;
  protected int missing;
  protected int spurious;

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