/**
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: ContingencyTable.java 12125 2010-01-04 14:44:43Z ggorrell $
 */

package gate.util;

import gate.AnnotationSet;
import gate.Annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Given two annotation sets, a type and a feature,
 * compares the feature values. It finds matching annotations and treats
 * the feature values as classifications. Its purpose is to calculate the
 * extent of agreement between the feature values in the two annotation
 * sets. It computes observed agreement and Kappa measures.
 */
public class ClassificationMeasures {
  
  /** Array of dimensions categories * categories. */
  private float[][] confusionMatrix;
  
  /** Cohen's kappa. */
  private float kappaCohen = 0;
  
  /** Scott's pi or Siegel & Castellan's kappa */
  private float kappaPi = 0;
  
  private boolean isCalculatedKappas = false;
  
  /** List of feature values that are the labels of the confusion matrix */
  private TreeSet<String> featureValues;

  public ClassificationMeasures() {
    // empty constructor
  }

  /**
   * See {@link #createConfusionMatrix(gate.AnnotationSet, gate.AnnotationSet,
   *  String, String)}.
   */
  public ClassificationMeasures(AnnotationSet aS1, AnnotationSet aS2, String type,
                          String feature)
  {
    createConfusionMatrix(aS1, aS2, type, feature);
  }
  
  /**
   * See {@link #combineConfusionMatrices(java.util.ArrayList)}.
   */
  public ClassificationMeasures(ArrayList<ClassificationMeasures> tables)
  {
    combineConfusionMatrices(tables);
  }
  
  /**
   * Portion of the instances on which the annotators agree.
   * @return a number between 0 and 1. 1 means perfect agreements.
   */
  public float getObservedAgreement()
  {
    float agreed = this.getAgreedTrials();
    float total = this.getTotalTrials();
    if(total>0){
      return agreed/total;
    } else {
      return 0;
    }
  }

  /**
   * Kappa is defined as the observed agreements minus the agreement
   * expected by chance.
   * The Cohen’s Kappa is based on the individual distribution of each
   * annotator.
   * @return a number between -1 and 1. 1 means perfect agreements.
   */
  public float getKappaCohen()
  {
    if(!isCalculatedKappas){
      computeKappaPairwise();
      isCalculatedKappas = true;
    }
    return kappaCohen;
  }

  /**
   * Kappa is defined as the observed agreements minus the agreement
   * expected by chance.
   * The Siegel & Castellan’s Kappa is based on the assumption that all the
   * annotators have the same distribution.
   * @return a number between -1 and 1. 1 means perfect agreements.
   */
  public float getKappaPi()
  {
    if(!isCalculatedKappas){
      computeKappaPairwise();
      isCalculatedKappas = true;
    }
    return kappaPi;
  }
  
  /**
   * Return the confusion matrix describing how annotations in one
   * set are classified in the other and vice versa. To understand
   * exactly which types are being confused with which other types
   * you will need to view this array in conjunction with featureValues,
   * which gives the class labels (annotation types) in the correct
   * order.
   * @return
   */
  public float[][] getConfusionMatrix(){
      return confusionMatrix.clone();
  }
  
  /**
   * Return the list of annotation types (class labels) in the
   * order in which they appear in the confusion matrix. This is
   * necessary to make sense of the confusion matrix.
   * @return
   */
  public SortedSet<String> getFeatureValues(){
    return Collections.unmodifiableSortedSet(featureValues);
  }
  
  /**
   * Create a confusion matrix in which annotations of identical span
   * bearing the specified feature name are compared in terms of feature value.
   * Compiles list of classes (feature values) on the fly.
   *
   * @param aS1 annotation set to compare to the second
   * @param aS2 annotation set to compare to the first
   * @param type annotation type containing the features to compare
   * @param feature feature name whose values will be compared
   */
  public void createConfusionMatrix(AnnotationSet aS1, AnnotationSet aS2,
                                    String type, String feature)
  {   
    // We'll accumulate a list of the feature values (a.k.a. class labels)
    featureValues = new TreeSet<String>();
    
    // Make a hash of hashes for the counts.
    HashMap<String, HashMap<String, Float>> countMap =
      new HashMap<String, HashMap<String, Float>>();
    
    // Get all the annotations of the correct type containing
    // the correct feature
    HashSet<String> featureSet = new HashSet<String>();
    featureSet.add(feature);
    AnnotationSet relevantAnns1 = aS1.get(type, featureSet);
    AnnotationSet relevantAnns2 = aS2.get(type, featureSet);
    
    // For each annotation in aS1, find the match in aS2
    for (Annotation relevantAnn1 : relevantAnns1) {

      // First we need to check that this annotation is not identical in span
      // to anything else in the same set. Duplicates should be excluded.
      int dupecount = 0;
      for (Annotation aRelevantAnns1 : relevantAnns1) {
        if (aRelevantAnns1.getStartNode().getOffset().equals(
          relevantAnn1.getStartNode().getOffset())
          && aRelevantAnns1.getEndNode().getOffset().equals(
          relevantAnn1.getEndNode().getOffset())) {
          dupecount++;
        }
      }

      if (dupecount > 1) {
        Out.prln("ClassificationMeasures: Same span annotations detected! Ignoring.");
      } else {
        // Find the match in as2
        int howManyCoextensiveAnnotations = 0;
        Annotation doc2Match = null;
        for (Annotation relevantAnn2 : relevantAnns2) {
          if (relevantAnn2.getStartNode().getOffset().equals(
            relevantAnn1.getStartNode().getOffset())
            && relevantAnn2.getEndNode().getOffset().equals(
            relevantAnn1.getEndNode().getOffset())) {
            howManyCoextensiveAnnotations++;
            doc2Match = relevantAnn2;
          }
        }

        if (howManyCoextensiveAnnotations == 0) {
          Out.prln("ContingencyTable: Annotation with no counterpart" +
            " detected!");
        } else if (howManyCoextensiveAnnotations == 1) {

          // What are our feature values?
          String featVal1 = (String) relevantAnn1.getFeatures().get(feature);
          String featVal2 = (String) doc2Match.getFeatures().get(feature);

          // Make sure both are present in our feature value list
          featureValues.add(featVal1);
          featureValues.add(featVal2);

          // Update the matrix hash of hashes
          // Get the right hashmap for the as1 feature value
          HashMap<String, Float> subHash = countMap.get(featVal1);
          if (subHash == null) {
            // This is a new as1 feature value, since it has no subhash yet
            HashMap<String, Float> subHashForNewAS1FeatVal =
              new HashMap<String, Float>();

            // Since it is a new as1 feature value, there can be no existing
            // as2 feature values paired with it. So we make a new one for this
            // as2 feature value
            subHashForNewAS1FeatVal.put(featVal2, (float) 1);

            countMap.put(featVal1, subHashForNewAS1FeatVal);
          } else {
            // Increment the count
            Float count = subHash.get(featVal2);
            if (count == null) {
              subHash.put(featVal2, (float) 1);
            } else {
              subHash.put(featVal2, (float) count.intValue() + 1);
            }

          }
        } else if (howManyCoextensiveAnnotations > 1) {
          Out.prln("ContingencyTable: Same span annotations detected!" +
            " Ignoring.");
        }
      }
    }
    
    // Now we have this hash of hashes, but the calculation implementations
    // require an array of floats. So for now we can just translate it.
    confusionMatrix = convert2DHashTo2DFloatArray(countMap, featureValues);
  }
  
  /**
   * Given a list of ContingencyTables, this will combine to make
   * a megatable. Then you can use kappa getters to get micro average
   * figures for the entire set.
   * @param tables tables to combine
   */
  private void combineConfusionMatrices(ArrayList<ClassificationMeasures> tables)
  {
    /* A hash of hashes for the actual values.
     * This will later be converted to a 2D float array for
     * compatibility with the existing code. */
    HashMap<String, HashMap<String, Float>> countMap =
      new HashMap<String, HashMap<String, Float>>();
    
    /* Make a new feature values set which is a superset of all the others */
    TreeSet<String> newFeatureValues = new TreeSet<String>();
    
    /* Now we are going to add each new contingency table in turn */

    for (ClassificationMeasures table : tables) {
      int it1index = 0;
      for (String featureValue1 : table.featureValues) {
        newFeatureValues.add(featureValue1);
        int it2index = 0;
        for (String featureValue2 : table.featureValues) {

          /* So we have the labels of the count we want to add */
          /* What is the value we want to add? */
          Float valtoadd = table.confusionMatrix[it1index][it2index];

          HashMap<String, Float> subHash = countMap.get(featureValue1);
          if (subHash == null) {
            /* This is a new as1 feature value, since it has no subhash yet */
            HashMap<String, Float> subHashForNewAS1FeatVal =
              new HashMap<String, Float>();

            /* Since it is a new as1 feature value, there can be no existing
             *  as2 feature values paired with it. So we make a new one for this
             *  as2 feature value */
            subHashForNewAS1FeatVal.put(featureValue2, valtoadd);

            countMap.put(featureValue1, subHashForNewAS1FeatVal);
          } else {
            /* Increment the count */
            Float count = subHash.get(featureValue2);
            if (count == null) {
              subHash.put(featureValue2, valtoadd);
            } else {
              subHash.put(featureValue2, count.intValue() + valtoadd);
            }
          }
          it2index++;
        }
        it1index++;
      }
    }
    
    confusionMatrix = convert2DHashTo2DFloatArray(countMap, newFeatureValues);
    featureValues = newFeatureValues;
    isCalculatedKappas = false;
  }
  
  
  /** Compute Cohen's and Pi kappas for two annotators.
   */
  public void computeKappaPairwise()
  {
    // Compute the agreement
    float observedAgreement = getObservedAgreement();
    int numCats = featureValues.size();
    // compute the agreement by chance
    // Get the marginal sum for each annotator
    float[] marginalArrayC = new float[numCats];
    float[] marginalArrayR = new float[numCats];
    float totalSum = 0;
    for(int i = 0; i < numCats; ++i) {
      float sum = 0;
      for(int j = 0; j < numCats; ++j)
        sum += confusionMatrix[i][j];
      marginalArrayC[i] = sum;
      totalSum += sum;
      sum = 0;
      for(int j = 0; j < numCats; ++j)
        sum += confusionMatrix[j][i];
      marginalArrayR[i] = sum;
    }
    // Compute Cohen's p(E)
    float pE = 0;
    if(totalSum > 0) {
      float doubleSum = totalSum * totalSum;
      for(int i = 0; i < numCats; ++i)
        pE += (marginalArrayC[i] * marginalArrayR[i]) / doubleSum;
    }
    // Compute Cohen's Kappa
    if(totalSum > 0)
      kappaCohen = (observedAgreement - pE) / (1 - pE);
    else kappaCohen = 0;
    // Compute S&C's chance agreement
    pE = 0;
    if(totalSum > 0) {
      float doubleSum = 2 * totalSum;
      for(int i = 0; i < numCats; ++i) {
        float p = (marginalArrayC[i] + marginalArrayR[i]) / doubleSum;
        pE += p * p;
      }
    }
    if(totalSum > 0)
      kappaPi = (observedAgreement - pE) / (1 - pE);
    else kappaPi = 0;
    // Compute the specific agreement for each label using marginal sums
    float[][] sAgreements = new float[numCats][2];
    for(int i = 0; i < numCats; ++i) {
      if(marginalArrayC[i] + marginalArrayR[i]>0) 
        sAgreements[i][0] = (2 * confusionMatrix[i][i])
          / (marginalArrayC[i] + marginalArrayR[i]);
      else sAgreements[i][0] = 0.0f;
      if(2 * totalSum - marginalArrayC[i] - marginalArrayR[i]>0)
        sAgreements[i][1] = (2 * (totalSum - marginalArrayC[i]
          - marginalArrayR[i] + confusionMatrix[i][i]))
          / (2 * totalSum - marginalArrayC[i] - marginalArrayR[i]);
      else sAgreements[i][1] = 0.0f;
    }
  }
  
  /** Gets the number of annotations for which the two annotation sets
   * are in agreement with regards to the annotation type.
   * @return Number of agreed trials
   */
  public float getAgreedTrials(){
    float sumAgreed = 0;
    for(int i = 0; i < featureValues.size(); ++i) {
      sumAgreed += confusionMatrix[i][i];
    }
    return sumAgreed;
  }
  
  /** Gets the total number of annotations in the two sets.
   * Note that only matched annotations (identical span) are
   * considered.
   * @return Number of trials
   */
  public float getTotalTrials(){
    float sumTotal = 0;
    for(int i = 0; i < featureValues.size(); ++i) {
      for(int j = 0; j < featureValues.size(); ++j) {
        sumTotal += confusionMatrix[i][j];
      }
    }
    return sumTotal;
  }
  
  /**
   * Print out the confusion matrix on the standard out stream.
   */
  public void printConfusionMatrix()
  {
    StringBuffer logMessage = new StringBuffer();

    int numL = this.featureValues.size();
    int x = 0;
    for(String featureValue : featureValues){
      Out.prln(x + ": " + featureValue);
      x++;
    }
    
    logMessage.append("\t|");
    for(int i = 0; i < numL; i++) {
      logMessage.append("\t").append(i).append("\t|");
    }
    logMessage.append("\n");
    for(int i = 0; i < numL; i++) {
      logMessage.append(i).append("\t|");
      for(int j = 0; j < numL; j++){
        logMessage.append("\t").append(this.confusionMatrix[i][j])
          .append("\t|");
      }
      logMessage.append("\n");
    }
    Out.pr(logMessage);
  }
  
  /**
   * Convert between two formats of confusion matrix.
   * A hashmap of hashmaps is easier to populate but an array is better for
   * matrix computation.
   * @param countMap count for each label as in confusion matrix
   * @param featureValues sorted set of labels that will define the dimensions
   * @return converted confusion matrix as an 2D array
   */
  private float[][] convert2DHashTo2DFloatArray(
    HashMap<String, HashMap<String, Float>> countMap,
    TreeSet<String> featureValues)
  {
    int dimensionOfContingencyTable = featureValues.size();
    float[][] matrix =
      new float[dimensionOfContingencyTable][dimensionOfContingencyTable];
    int i=0;
    int j=0;
    for (String featureValue1 : featureValues) {
      HashMap<String, Float> hashForThisAS1FeatVal =
        countMap.get(featureValue1);
      j = 0;
      for (String featureValue2 : featureValues) {
        Float count = null;
        if (hashForThisAS1FeatVal != null) {
          count = hashForThisAS1FeatVal.get(featureValue2);
        }
        if (count != null) {
          matrix[i][j] = count;
        } else {
          matrix[i][j] = 0;
        }
        j++;
      }
      i++;
    }    
    return matrix;
  }
  
}
