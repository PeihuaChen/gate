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
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * ContingencyTable, given two annotation sets, a type and a feature,
 * compares the feature values. It finds matching annotations and treats
 * the feature values as classifications. Its purpose is to calculate the
 * extent of agreement between the feature values in the two annotation
 * sets.
 * 
 * ContingencyTable computes observed agreement and Kappa 
 * measures. This class contains some
 * code to handle more than two annotators; i.e. to calculate Scott's Pi
 * and Davies and Fleiss's extensions for more than two annotators.
 * However, the supporting methods required to make these implementations
 * readily usable were not present, and have not been added at this time.
 * Therefore, for simplicity, calculations for more than two annotators have
 * been deprecated, since as it stands they are not complete. Macro average
 * methods are also deprecated: calculating a macro average is more 
 * appropriately done in the calling class, since it requires a set of
 * contingency tables. This class is just a single contingency table.
 */
public class ClassificationMeasures {
  
  /* The public variables below are remaining public for the 
   * sake of the IAA plugin. They should not be used in general.*/
  
  /** Number of categories. */
  public int numCats;
  
  /** Number of annotators. */
  public int numJudges;
  
  /** Array of dimensions categories * categories. */
  public float[][] confusionMatrix;
  
  /** The observed agreement. */
  public float observedAgreement = 0;

  /** Indicate if the agreement is available or not. */
  public boolean isAgreementAvailable = false;
  
  /** Cohen's kappa. */
  public float kappaCohen = 0;
  
  /** Scott's pi or Siegel & Castellan's kappa */
  public float kappaPi = 0;
  
  /** Assignment matrix for computing the all way kappa. */
  float[][] assignmentMatrix;
  
  /** Davies and Fleiss's extension of Cohen's kappa. */
  public float kappaDF = 0;
  
  /** Extension of Scott's pi for more than two judges. */
  public float kappaSC = 0;
  
  /** Positive and negative specific agreement for each category. */
  public float[][] sAgreements;
  
  /** List of feature values that are the labels of the confusion matrix */
  private TreeSet<String> featureValues;
    
  /**
   * Pairwise case.
   * Used in IAA plugin because the calling code knows the number of categories.
   * We are moving away from having to know that in advance.
   * @deprecated
   */
  public ClassificationMeasures(int numCats)
  {
    this.numCats = numCats;
    confusionMatrix = new float[numCats][numCats];
    isAgreementAvailable = false;
  }

  /**
   * More than two annotators?
   * @deprecated
   */
  public ClassificationMeasures(int numCats, int numJ)
  {
    this.numCats = numCats;
    this.numJudges = numJ;
    assignmentMatrix = new float[numCats][numJ];
    isAgreementAvailable = false;
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
    computeKappaPairwise();
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
    computeKappaPairwise();
    return kappaPi;
  }
  
  public TreeSet getFeatureValues(){
    return featureValues;
  }
  
  public float[][] getConfusionMatrix(){
    return confusionMatrix;
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
        Out.prln("ContingencyTable: Same span annotations detected! Ignoring.");
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
        
    // Set numcats global so calculation methods will work
    numCats = featureValues.size();
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

    /* Set numcats global so calculation methods will work */
    numCats = newFeatureValues.size();
  }
  
  
  /** Compute Cohen's and Pi kappas for two annotators. 
   * Currently the kappa getters recalculate both kappas, which might
   * be slow. This is a candidate for later improvement.
   */
  public void computeKappaPairwise()
  {
    // Compute the agreement
    if(!isAgreementAvailable) computeObservedAgreement();
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
    sAgreements = new float[numCats][2];
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
  
  public float getAgreedTrials(){
    float sumAgreed = 0;
    for(int i = 0; i < numCats; ++i) {
      sumAgreed += confusionMatrix[i][i];
    }
    return sumAgreed;
  }
  
  public float getTotalTrials(){
    float sumTotal = 0;
    for(int i = 0; i < numCats; ++i) {
      for(int j = 0; j < numCats; ++j) {
        sumTotal += confusionMatrix[i][j];
      }
    }
    return sumTotal;
  }
  
  /** Compute the observed agreement. 
   * It's a simple ratio of right to wrong. This will be made private later.
   * It is left public for the sake of the IAA plugin.
   * @deprecated You should use {@link #getObservedAgreement()} instead.
   */
  public void computeObservedAgreement()
  {
    float sumAgreed = 0;
    float sumTotal = 0;
    for(int i = 0; i < numCats; ++i) {
      sumAgreed += confusionMatrix[i][i];
      for(int j = 0; j < numCats; ++j)
        sumTotal += confusionMatrix[i][j];
    }
    if(sumTotal > 0.0)
      observedAgreement = sumAgreed / sumTotal;
    else observedAgreement = 0;
    isAgreementAvailable = true;
  }

  /** Compute the all way kappa. 
   * This will calculate DF and SC kappas for more than two annotators.
   * @deprecated it may not be supported in the future.
   */
  public void computeAllwayKappa(long ySum, long numInstances,
    long numAgreements, long[] numJudgesCat, boolean isUsingNonlabel)
  {
    // Compute cohen's kappa using the extended formula.
    float[] pc = new float[numCats];
    for(int j = 0; j < numJudges; ++j) {
      for(int i = 0; i < numCats; ++i)
        pc[i] += assignmentMatrix[i][j];
      float sum = 0;
      for(int i = 0; i < numCats; ++i)
        sum += assignmentMatrix[i][j];
      if(sum > 0) for(int i = 0; i < numCats; ++i)
        assignmentMatrix[i][j] /= sum;
    }
    float sum = 0;
    for(int i = 0; i < numCats; ++i)
      sum += pc[i];
    if(sum > 0) for(int i = 0; i < numCats; ++i)
      pc[i] /= sum;
    float term1, term2;
    term1 = 0;
    for(int i = 0; i < numCats; ++i)
      term1 += pc[i] * (1 - pc[i]);
    term2 = 0;
    for(int i = 0; i < numCats; ++i)
      for(int j = 0; j < numJudges; ++j)
        term2 += (assignmentMatrix[i][j] - pc[i])
          * (assignmentMatrix[i][j] - pc[i]);
    if(numInstances > 0 && numJudges > 1 && (term1 > 0 || term2 > 0))
      kappaDF = 1 - (float)(numInstances * numJudges * numJudges - ySum)
        / (numInstances * (numJudges * (numJudges - 1) * term1 + term2));
    else kappaDF = 0;
    // Compute the observed agreement and the S&C kappa
    if(numInstances == 0 || numJudges == 0) {
      observedAgreement = 0;
      kappaSC = 0;
    }
    else {
      observedAgreement = (float)numAgreements / numInstances;
      // Compute the kappa of S&C
      float pE = 0;
      float dNum = numInstances * numJudges;
      for(int i = 0; i < numCats; ++i) {
        float s = numJudgesCat[i] / dNum;
        float sR = s;
        for(int j = 1; j < numJudges; ++j)
          sR *= s;
        pE += sR;
      }
      kappaSC = (observedAgreement - pE) / (1 - pE);
    }
  }

  /** Print out the results for two annotators.
   */
  public String printResultsPairwise()
  {
    return "Observed agreement: " + observedAgreement + "; " +
                "Cohen's kappa: " + kappaCohen + "; " +
                   "Scott's pi: " + kappaPi + "\n";
  }

  /**
   * @deprecated If you populated your ContingencyTable using the new
   * constructors, then you won't need to provide labels, and should use
   * {@link #printConfusionMatrix()}.
   * @param labelsArr String array of labels
   * @return the confusion matrix as a String
   */
  public String printConfusionMatrix(String[] labelsArr)
  {
    StringBuffer logMessage = new StringBuffer();
    // logMessage.append("----------------------------------------------\n");
    logMessage.append("\t\t|");
    int numL = labelsArr.length;
    for(int i = 0; i < numL; ++i) {
      logMessage.append("\t").append(labelsArr[i]).append("\t|");
    }
    logMessage.append("\t ").append(IaaCalculation.NONCAT).append(" \t|\n");
    // logMessage.append("----------------------------------------------\n");
    for(int i = 0; i < numL; ++i) {
      logMessage.append("\t").append(labelsArr[i]).append("\t|");
      for(int j = 0; j < numL; ++j)
        logMessage.append("\t").append(this.confusionMatrix[i][j])
          .append("\t|");
      logMessage.append("\t").append(this.confusionMatrix[i][numL])
        .append("\t|\n");
    }
    logMessage.append("\t").append(IaaCalculation.NONCAT).append("\t|");
    for(int j = 0; j < numL; ++j)
      logMessage.append("\t").append(this.confusionMatrix[numL][j])
        .append("\t|");
    logMessage.append("\t").append(this.confusionMatrix[numL][numL])
      .append("\t|\n");
    // logMessage.append("----------------------------------------------\n");
    return logMessage.toString();
  }

  /**
   * Print out the confusion matrix on the standard out stream.
   */
  public void printConfusionMatrix()
  {
    StringBuffer logMessage = new StringBuffer();

    int numL = this.featureValues.size();
    Iterator it = this.featureValues.iterator();
    int x = 0;
    while(it.hasNext()){
      Out.prln(x + ": " + it.next());
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

  /** Print out the results for more than two annotators.
   * @deprecated
   */
  public String printResultsAllway()
  {
    StringBuffer logMessage = new StringBuffer();
    logMessage.append("Observed agreement: " + observedAgreement + "; ");
    logMessage.append("Cohen's kappa extended for  " + numJudges
      + " annotators: " + kappaDF + "; ");
    logMessage.append("S&C kappa for " + numJudges + " annotators: " + kappaSC
      + "\n");
    return logMessage.toString();
  }

  /** Add the results. This can be used to calculate macro averages,
   * by adding the results of calculations then using macroAveraged()
   * method. However, this is not very safe. You are recommended to
   * implement your own macro average.
   * @deprecated
   */
  public void add(ClassificationMeasures another)
  {
    kappaCohen += another.kappaCohen;
    kappaDF += another.kappaDF;
    kappaPi += another.kappaPi;
    kappaSC += another.kappaSC;
    observedAgreement += another.observedAgreement;
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
    float[][] confusionMatrix =
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
          confusionMatrix[i][j] = count;
        } else {
          confusionMatrix[i][j] = 0;
        }
        j++;
      }
      i++;
    }    
    return confusionMatrix;
  }
  
  /** Compute the macro averaged results. 
   * This method assumes you have added all the kappa figures up using add(). 
   * It expects you to provide the divisor yourself.
   * @param num number of documents or other items to average across
   * @deprecated You should preferably calculate your own macro average and
   * not use this method.
   */
  public void macroAveraged(int num)
  {
    kappaCohen /= num;
    kappaDF /= num;
    kappaPi /= num;
    kappaSC /= num;
    observedAgreement /= num;
  }
  
}
