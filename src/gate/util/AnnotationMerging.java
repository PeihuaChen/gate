/**
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: IaaCalculation.java 9050 2007-09-04 10:42:12Z yaoyongli $
 */

package gate.util;

import gate.Annotation;
import gate.AnnotationSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
/**
 * Merging the annotations from different annotators. The input
 * is the array containing the annotation set for merging. The
 * output is a map, the key of which is the merged annotations
 * and the values of which represent those annotators who agree
 * on the merged annotation. The program also selects only one 
 * annotation for one example (namely if there are more than one
 * merged annotations with the same span, the program selects only
 * one annotation from them with the maximal number of annotators 
 * on it.
 */
public class AnnotationMerging {

  /**
   * Merge all annotationset from an array. If one annotation is in at least
   * numK annotation sets, then put it into the merging annotation set.
   */
  public static void mergeAnnogation(AnnotationSet[] annsArr, String nameFeat,
    HashMap<Annotation, String> mergeAnns, int numMaxK, boolean isTheSameInstances) {
    int numA = annsArr.length;
    // First copy the annotatioin sets into a temp array
    HashSet<Annotation>[] annsArrTemp = new HashSet[numA];
    for(int i = 0; i < numA; ++i) {
      if(annsArr[i] != null) {
        annsArrTemp[i] = new HashSet<Annotation>();
        for(Annotation ann : annsArr[i])
          annsArrTemp[i].add(ann);
      }
    }
    HashSet<String> featSet = new HashSet<String>();
    if(nameFeat != null) featSet.add(nameFeat);
    for(int iA = 0; iA < numA - numMaxK + 1; ++iA) {
      if(annsArrTemp[iA] != null) {
        for(Annotation ann : annsArrTemp[iA]) {
          int numContained = 1;
          StringBuffer featAdd = new StringBuffer();
          featAdd.append(iA);
          for(int i = iA + 1; i < numA; ++i) {
            if(annsArrTemp[i] != null) {
              Annotation annT = null;
              for(Annotation ann0 : annsArrTemp[i]) {
                if(ann0.isCompatible(ann, featSet)) {
                  ++numContained;
                  featAdd.append("-" + i);
                  annT = ann0;
                  break;
                }
              }
              annsArrTemp[i].remove(annT);
            }
          }
          if(numContained >= numMaxK) {
            mergeAnns.put(ann, featAdd.toString());
          } else if(isTheSameInstances && nameFeat != null) {
           ann.getFeatures().remove(nameFeat);
           mergeAnns.put(ann, featAdd.toString());
        }
        }
      }
    }
    //Remove the annotation in the same place
    removeDuplicate(mergeAnns);
    return;
  }

  /**
   * Merge all annotationset from an array. If one annotation is agreed by
   * the majority of the annotators, then put it into the merging annotation set.
   */
  public static void mergeAnnogationMajority(AnnotationSet[] annsArr, String nameFeat,
    HashMap<Annotation, String> mergeAnns) {
    int numA = annsArr.length;
    // First copy the annotatioin sets into a temp array
    HashSet<Annotation>[] annsArrTemp = new HashSet[numA];
    for(int i = 0; i < numA; ++i) {
      if(annsArr[i] != null) {
        annsArrTemp[i] = new HashSet<Annotation>();
        for(Annotation ann : annsArr[i])
          annsArrTemp[i].add(ann);
      }
    }
    HashSet<String> featSet = new HashSet<String>();
    if(nameFeat != null) featSet.add(nameFeat);
    for(int iA = 0; iA < numA; ++iA) {
      if(annsArrTemp[iA] != null) {
        for(Annotation ann : annsArrTemp[iA]) {
          int numAgreed=0;
          int numDisagreed=0;
          //Already the iA annotators don't agree the annotation
          if(iA>0) numDisagreed = iA;
          //The current annotator agree the annotation of course.
          ++numAgreed;
          StringBuffer featAdd = new StringBuffer();
          featAdd.append(iA);
          for(int i = iA + 1; i < numA; ++i) {
            boolean isContained = false;
            if(annsArrTemp[i] != null) {
              Annotation annT = null;
              for(Annotation ann0 : annsArrTemp[i]) {
                  if(ann0.isCompatible(ann, featSet)) {
                    ++numAgreed;
                    featAdd.append("-" + i);
                    annT = ann0;
                    isContained = true;
                    break;
                  }
              }
              if(isContained) 
                annsArrTemp[i].remove(annT);
            }
            if(!isContained) 
              ++numDisagreed; 
          }
          if(numAgreed >= numDisagreed) {
            mergeAnns.put(ann, featAdd.toString());
          }
        }
      }
    }
    //remove the annotation in the same place
    removeDuplicate(mergeAnns);
    return;
  }
  /** Remove the duplicate annotations from the merged annotations. */
  private static void removeDuplicate(HashMap<Annotation, String> mergeAnns) {
//  first copy the annotations into a tempory
    HashMap <Annotation, Integer> mergeAnnsNum = new HashMap<Annotation, Integer>();
    for(Annotation ann:mergeAnns.keySet()) {
      String str = mergeAnns.get(ann);
      int num=1;
      while(str.contains("-")) {
        ++num;
        str = str.substring(str.indexOf('-')+1);
      }
      mergeAnnsNum.put(ann, new Integer(num));
    }
    //remove the annotaitons having the same places
    for(Annotation ann:mergeAnnsNum.keySet()) {
      Annotation annT=null;
      int num0=-1;
      Vector<Annotation>sameAnns= new Vector<Annotation>();
      for(Annotation ann1:mergeAnnsNum.keySet()) {
        if(ann.coextensive(ann1)) {
          sameAnns.add(ann1);
          int num = mergeAnnsNum.get(ann1).intValue();
          if(num>num0) {
            annT = ann1;
            num0 = num;
          }
        }
      } //end the inner loop for merged annotations
      //Keep the one which most annotators agree on.
      sameAnns.remove(annT);
      //Remove all others 
      for(int i=0; i<sameAnns.size(); ++i)
        mergeAnns.remove(sameAnns.elementAt(i));
    }
  } 
  
}
