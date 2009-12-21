/**
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */

package gate.util;

import gate.AnnotationSet;
import gate.FeatureMap;
import gate.Factory;
import gate.Document;
import gate.Annotation;
import gate.Gate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
public class ContingencyTable {
  
  /* The public variables below are remaining public for the 
   * sake of the IAA plugin. They should not be used in general.*/
  
  /** Number of categories. */
  public int numCats;
  
  /** Number of annotators. */
  public int numJudges;
  
  /** Confusion matrix. */
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
    
  /** Constructor for pairwise case. 
   * Used in IAA plugin because the calling code knows the number of categories.
   * We are moving away from having to know that in advance.
   * @deprecated
   */
  public ContingencyTable(int numC)
  {
    this.numCats = numC;
    this.confusionMatrix = new float[numC][numC];
    isAgreementAvailable = false;
  }

  /** Constructor for more than two annotators?
   * @deprecated
   */
  public ContingencyTable(int numC, int numJ)
  {
    this.numCats = numC;
    this.numJudges = numJ;
    this.assignmentMatrix = new float[numC][numJ];
    isAgreementAvailable = false;
  }

  /** Constructor that takes 2 annotation sets,
   * one type and one feature. Compiles list of categories
   * on the fly. Creates the contingency table. 
   */
  public ContingencyTable(AnnotationSet aS1, AnnotationSet aS2, String type, String feature)
  {
    this.init(aS1, aS2, type, feature);
  }
  
  /** Constructor that takes a list of existing contingency tables
   * and makes a megatable.
   * @param tables
   */
  public ContingencyTable(ArrayList<ContingencyTable> tables)
  {
    this.init(tables);
  }
  
  /**
   * Get the observed agreement.
   * The global variable is remaining public for now but it is safer to use this method.
   * @return
   */
  public float getObservedAgreement()
  {
    this.computeObservedAgreement();
    return this.observedAgreement;
  }

  /**
   * Get Cohen's Kappa.
   * The global variable is remaining public for now but it is safer to use this method.
   * @return
   */
  public float getKappaCohen()
  {
    this.computeKappaPairwise();
    return this.kappaCohen;
  }

  /**
   * Get Scott's Pi.
   * The global variable is remaining public for now but it is safer to use this method.
   * @return
   */
  public float getKappaPi()
  {
    this.computeKappaPairwise();
    return this.kappaPi;
  }
    
  /**
   * Given 2 annotation sets, a type and a feature, this will create a confusion matrix
   * in which annotations of identical span bearing the specified feature are
   * compared in terms of feature value.
   * @param aS1
   * @param aS2
   * @param type
   * @param feature
   * @return
   */
  public void init(AnnotationSet aS1, AnnotationSet aS2, String type, String feature)
  {   
    /* We'll accumulate a list of the feature values (a.k.a. class labels) */
    this.featureValues = new TreeSet<String>();
    
    /* Make a hash of hashes for the counts. */
    HashMap<String, HashMap<String, Float>> countMap = new HashMap<String, HashMap<String, Float>>();
    
    /* Get all the annotations of the correct type containing the correct feature */
    HashSet<String> featureSet = new HashSet<String>();
    featureSet.add(feature);
    AnnotationSet relevantAnns1 = aS1.get(type, featureSet);
    AnnotationSet relevantAnns2 = aS2.get(type, featureSet);
    
    /* For each annotation in aS1, find the match in aS2 */
    Iterator<Annotation> as1it = relevantAnns1.iterator();
    while (as1it.hasNext()){
      Annotation thisAnn = as1it.next();
      
      /*First we need to check that this annotation is not identical in span
       * to anything else in the same set. Duplicates should be excluded. */
      Iterator<Annotation> dupeIterator = relevantAnns1.iterator();
      int dupecount = 0;
      while (dupeIterator.hasNext()){
        Annotation dupeCheckAnn = dupeIterator.next();
        if(dupeCheckAnn.getStartNode().getOffset()==thisAnn.getStartNode().getOffset()
                && dupeCheckAnn.getEndNode().getOffset()==thisAnn.getEndNode().getOffset()){
          dupecount++;
        }
      }
      
      if(dupecount>1){
        System.out.println("ContingencyTable: Same span annotations detected! Ignoring.");
      } else {
        /* Find the match in as2 */
        Iterator<Annotation> as2it = relevantAnns2.iterator();
        int howManyCoextensiveAnnotations = 0;
        Annotation doc2Match = null;
        while(as2it.hasNext()){
          Annotation doc2MatchCandidate = as2it.next();          
          if(doc2MatchCandidate.getStartNode().getOffset().equals(thisAnn.getStartNode().getOffset())
                  && doc2MatchCandidate.getEndNode().getOffset().equals(thisAnn.getEndNode().getOffset())){
            howManyCoextensiveAnnotations++;
            doc2Match = doc2MatchCandidate;
          }
        }
          
        if(howManyCoextensiveAnnotations==0){
          System.out.println("ContingencyTable: Annotation with no counterpart detected!");
        } else if(howManyCoextensiveAnnotations==1){
          
          /* What are our feature values? */
          String featVal1 = (String)thisAnn.getFeatures().get(feature);
          String featVal2 = (String)doc2Match.getFeatures().get(feature);
          
          /* Make sure both are present in our feature value list */
          featureValues.add(featVal1);
          featureValues.add(featVal2);
          
          /* Update the matrix hash of hashes*/
          /* Get the right hashmap for the as1 feature value */
          HashMap<String, Float> subHash = countMap.get(featVal1);
          if(subHash==null){
            /* This is a new as1 feature value, since it has no subhash yet */
            HashMap<String, Float> subHashForNewAS1FeatVal = new HashMap<String, Float>();
              
            /* Since it is a new as1 feature value, there can be no existing
             *  as2 feature values paired with it. So we make a new one for this
             *  as2 feature value */
            subHashForNewAS1FeatVal.put(featVal2, new Float(1));
              
            countMap.put(featVal1, subHashForNewAS1FeatVal);
          } else {
            /* Increment the count */
            Float count = subHash.get(featVal2);
            if(count==null){
              subHash.put(featVal2, new Float(1));
            } else {
              subHash.put(featVal2, new Float(count.intValue() + 1));
            }
            
          }
        } else if(howManyCoextensiveAnnotations>1){
          System.out.println("ContingencyTable: Same span annotations detected! Ignoring.");
        }
      }
    }
    
    /* Now we have this hash of hashes, but the calculation implementations
     * require an array of floats. So for now we can just translate it.
     */
    this.confusionMatrix = this.convert2DHashTo2DFloatArray(countMap, featureValues);
        
    /* Set numcats global so calculation methods will work */
    this.numCats = this.featureValues.size();
  }
  
  /**
   * Given a list of ContingencyTables, this will combine to make
   * a megatable. Then you can use kappa getters to get micro average
   * figures for the entire set.
   * @param tables
   */
  private void init(ArrayList<ContingencyTable> tables)
  {
    /* A hash of hashes for the actual values.
     * This will later be converted to a 2D float array for
     * compatibility with the existing code. */
    HashMap<String, HashMap<String, Float>> countMap = new HashMap<String, HashMap<String, Float>>();
    
    /* Make a new feature values set which is a superset of all the others */
    TreeSet<String> featureValues = new TreeSet<String>();
    
    
    /* Now we are going to add each new contingency table in turn */
    
    Iterator<ContingencyTable> tableIterator = tables.iterator();
    
    while(tableIterator.hasNext()){
      ContingencyTable thisTable = tableIterator.next();
        
      Iterator<String> it1 = thisTable.featureValues.iterator();
      int it1index = 0;
      while(it1.hasNext()){
        String featVal1 = it1.next();
        featureValues.add(featVal1);
        Iterator<String> it2 = thisTable.featureValues.iterator();
        int it2index = 0;
        while(it2.hasNext()){
          String featVal2 = it2.next();
          
          /* So we have the labels of the count we want to add */
          /* What is the value we want to add? */
          Float valtoadd = new Float((thisTable.confusionMatrix[it1index][it2index]));
          
          HashMap<String, Float> subHash = countMap.get(featVal1);
          if(subHash==null){
            /* This is a new as1 feature value, since it has no subhash yet */
            HashMap<String, Float> subHashForNewAS1FeatVal = new HashMap<String, Float>();
            
            /* Since it is a new as1 feature value, there can be no existing
             *  as2 feature values paired with it. So we make a new one for this
             *  as2 feature value */
            subHashForNewAS1FeatVal.put(featVal2, valtoadd);
              
            countMap.put(featVal1, subHashForNewAS1FeatVal);
          } else {
            /* Increment the count */
            Float count = subHash.get(featVal2);
            if(count==null){
              subHash.put(featVal2, new Float(valtoadd));
            } else {
              subHash.put(featVal2, new Float(count.intValue() + valtoadd));
            }
          }
          it2index++;
        }
        it1index++;
      }
    }
    
    this.confusionMatrix = this.convert2DHashTo2DFloatArray(countMap, featureValues);
    this.featureValues = featureValues;
    
    /* Set numcats global so calculation methods will work */
    this.numCats = this.featureValues.size();  
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

  /** Compute the observed agreement. 
   * It's a simple ratio of right to wrong. You should use getObservedAgreement() rather
   * than using this method directly. This will be made private later. It is left
   * public for the sake of the IAA plugin.
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
   * However it may not be supported in the future.
   * @deprecated
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
    StringBuffer logMessage = new StringBuffer();
    logMessage.append("Observed agreement: " + observedAgreement + ";  ");
    logMessage.append("Cohen's kappa: " + kappaCohen + "; ");
    logMessage.append("Scott's pi: " + kappaPi + "\n");
    return logMessage.toString();
  }

  /** Print out the confusion matrix.
   * This method requires a String array of labels. If you
   * populated your ContingencyTable using the new constructors, then you
   * won't need to provide labels, and should use printConfusionMatrix().
   * @deprecated
   * @param labelsArr
   * @return
   */
  public String printConfusionMatrix(String[] labelsArr)
  {
    StringBuffer logMessage = new StringBuffer();
    // logMessage.append("----------------------------------------------\n");
    logMessage.append("\t\t|");
    int numL = labelsArr.length;
    for(int i = 0; i < numL; ++i) {
      logMessage.append("\t" + labelsArr[i] + "\t|");
    }
    logMessage.append("\t " + IaaCalculation.NONCAT + " \t|\n");
    // logMessage.append("----------------------------------------------\n");
    for(int i = 0; i < numL; ++i) {
      logMessage.append("\t" + labelsArr[i] + "\t|");
      for(int j = 0; j < numL; ++j)
        logMessage.append("\t" + this.confusionMatrix[i][j] + "\t|");
      logMessage.append("\t" + this.confusionMatrix[i][numL] + "\t|\n");
    }
    logMessage.append("\t" + IaaCalculation.NONCAT + "\t|");
    for(int j = 0; j < numL; ++j)
      logMessage.append("\t" + this.confusionMatrix[numL][j] + "\t|");
    logMessage.append("\t" + this.confusionMatrix[numL][numL] + "\t|\n");
    // logMessage.append("----------------------------------------------\n");
    return logMessage.toString();
  }

  /** Print out the confusion matrix. 
   */
  public void printConfusionMatrix()
  {
    StringBuffer logMessage = new StringBuffer();

    int numL = this.featureValues.size();
    Iterator it = this.featureValues.iterator();
    int x = 0;
    while(it.hasNext()){
      System.out.println(x + ": " + it.next());
      x++;
    }
    
    logMessage.append("\t|");
    for(int i = 0; i < numL; i++) {
      logMessage.append("\t" + i + "\t|");
    }
    logMessage.append("\n");
    for(int i = 0; i < numL; i++) {
      logMessage.append(i + "\t|");
      for(int j = 0; j < numL; j++){
        logMessage.append("\t" + this.confusionMatrix[i][j] + "\t|");
      }
      logMessage.append("\n");
    }
    System.out.print(logMessage);
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
  public void add(ContingencyTable another)
  {
    this.kappaCohen += another.kappaCohen;
    this.kappaDF += another.kappaDF;
    this.kappaPi += another.kappaPi;
    this.kappaSC += another.kappaSC;
    this.observedAgreement += another.observedAgreement;
  }

  
  /** Private method to convert between two formats of confusion matrix.
   * A hashmap of hashmaps is easier to populate but older methods require
   * a 2D float array. So this method is used to convert. It requires a 
   * sorted set of labels, that will define the dimensions.
   * @param countMap
   * @param featureValues
   * @return
   */
  private float[][] convert2DHashTo2DFloatArray(HashMap<String, HashMap<String, Float>> countMap, 
          TreeSet<String> featureValues)
  {

    int dimensionOfContingencyTable = featureValues.size();
    
    float[][] confusionMatrix = new float[dimensionOfContingencyTable][dimensionOfContingencyTable];
    int i=0;
    int j=0;
    Iterator<String> it1 = featureValues.iterator();
    while(it1.hasNext()){
      String featval1 = it1.next();
      HashMap<String, Float> hashForThisAS1FeatVal = countMap.get(featval1);      
      Iterator<String> it2 = featureValues.iterator();
      j = 0;
      while(it2.hasNext()){
        String featval2 = it2.next();
        Float count = null;
        if(hashForThisAS1FeatVal!=null){
          count = hashForThisAS1FeatVal.get(featval2);
        }
        if(count != null){
          confusionMatrix[i][j] = count.floatValue();
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
   * It expects you to provide the divisor yourself. You should preferably 
   * calculate your own macro average and not use this method.
   * @deprecated
   */
  public void macroAveraged(int num)
  {
    this.kappaCohen /= num;
    this.kappaDF /= num;
    this.kappaPi /= num;
    this.kappaSC /= num;
    this.observedAgreement /= num;
  }
  
/*
  public static void main(String [] args)
  {
    //Testing
    String type = "Type1";
    String feature = "Feat1";
      
    Document doc1 = null;
    Document doc2 = null;
    Document doc3 = null;
      
    try {
      Gate.init();
        
      URI uri1 = new URI("file:////home/genevieve/Desktop/doc1.xml");
      URI uri2 = new URI("file:////home/genevieve/Desktop/doc2.xml");
      URI uri3 = new URI("file:////home/genevieve/Desktop/doc3.xml");
        
      doc1 = Factory.newDocument(uri1.toURL());
      doc2 = Factory.newDocument(uri2.toURL());
      doc3 = Factory.newDocument(uri3.toURL());
        
    } catch (Exception e) {
      e.printStackTrace();
    }
     if(doc1!=null && doc2!=null && doc3!=null){
       AnnotationSet as1 = doc1.getAnnotations();
       AnnotationSet as2 = doc2.getAnnotations();
       AnnotationSet as3 = doc3.getAnnotations();
       
       System.out.println("Docs 1 and 2\n");
       ContingencyTable myContingencyTable1 = new ContingencyTable(as1, as2, type, feature);
       myContingencyTable1.printConfusionMatrix();
       System.out.println("Observed agreement: " + myContingencyTable1.getObservedAgreement());
       System.out.println("Kappa Cohen: " + myContingencyTable1.getKappaCohen());
       System.out.println("Kappa Pi: " + myContingencyTable1.getKappaPi() + "\n");

       System.out.println("Docs 1 and 3\n");
       ContingencyTable myContingencyTable2 = new ContingencyTable(as1, as3, type, feature);
       myContingencyTable2.printConfusionMatrix();
       System.out.println("Observed agreement: " + myContingencyTable2.getObservedAgreement());
       System.out.println("Kappa Cohen: " + myContingencyTable2.getKappaCohen());
       System.out.println("Kappa Pi: " + myContingencyTable2.getKappaPi() + "\n");

       System.out.println("Docs 2 and 3\n");
       ContingencyTable myContingencyTable3 = new ContingencyTable(as2, as3, type, feature);
       myContingencyTable3.printConfusionMatrix();
       System.out.println("Observed agreement: " + myContingencyTable3.getObservedAgreement());
       System.out.println("Kappa Cohen: " + myContingencyTable3.getKappaCohen());
       System.out.println("Kappa Pi: " + myContingencyTable3.getKappaPi() + "\n");
       
       ArrayList<ContingencyTable> tablesList = new ArrayList<ContingencyTable>();
       tablesList.add(myContingencyTable1);
       tablesList.add(myContingencyTable2);    
       tablesList.add(myContingencyTable3);       
       ContingencyTable myNewContingencyTable = new ContingencyTable(tablesList);

       System.out.println("All 3 added\n");
       myNewContingencyTable.printConfusionMatrix();
       System.out.println("Observed agreement: " + myNewContingencyTable.getObservedAgreement());
       System.out.println("Kappa Cohen: " + myNewContingencyTable.getKappaCohen());
       System.out.println("Kappa Pi: " + myNewContingencyTable.getKappaPi());
       
     } else {
       System.out.println("Failed to create docs from URLs.");
     }
  }
  */
}
