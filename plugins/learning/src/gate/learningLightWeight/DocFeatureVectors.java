package gate.learningLightWeight;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class DocFeatureVectors {
  String docId = null;

  int numInstances;

  SparseFeatureVector[] fvs;

  final static float DEFAULTVALUE = 1.0f;

  public DocFeatureVectors() {
    numInstances = 0;
  }

  public void obtainFVsFromNLPFeatures(NLPFeaturesOfDoc nlpDoc,
          NLPFeaturesList featList, int[] featurePosition, int maxNegPosition,
          int numDocs) {
    numInstances = nlpDoc.numInstances;
    docId = new String(nlpDoc.getDocId());
    fvs = new SparseFeatureVector[numInstances];

    for(int i = 0; i < numInstances; ++i) {
      // fvs[i] = new SparseFeatureVector(nlpDoc.featuresCounted[i]);
      Hashtable indexValues = new Hashtable();
      // List indexes = new ArrayList();
      int n = 0;
      String[] feat = nlpDoc.featuresInLine[i].toString().split(
              ConstantParameters.ITEMSEPARATOR);

      // System.out.println("i=" + new Integer(i) + " feature length="+
      // new Integer(feat.length));

      for(int j = 0; j < feat.length; ++j) {
        String featCur = feat[j];
        String featVal = null;
        int kk = -3;
        if(featCur.contains(NLPFeaturesList.SYMBOLNGARM)) {
          kk = feat[j].lastIndexOf(NLPFeaturesList.SYMBOLNGARM);
          featCur = feat[j].substring(0, kk);
          featVal = feat[j].substring(kk + 2);
        }
        if(featCur.length() > 0) { // if there is any feature

          if(featList.featuresList.containsKey(featCur)) {
            if(kk > -3) {
              // for only the presence of Ngram in the sentence
              // indexValues.put(featList.featuresList.get(featCur),
              // "1");
              // for tf representation
              // indexValues.put(featList.featuresList.get(featCur),
              // featVal);
              // for tf*idf representation
              double val = ((new Long(featVal)).doubleValue() + 1)
                      * Math.log((double)numDocs
                              / (new Long(featList.idfFeatures.get(featCur)
                                      .toString())).doubleValue());
              indexValues.put(featList.featuresList.get(featCur),
                      new Float(val));
            }
            else if(kk == -3) {
              if(featurePosition[j] == 0)
                indexValues.put(featList.featuresList.get(feat[j]), "1");
              else if(featurePosition[j] < 0)
                indexValues.put(new Long((Long.parseLong(featList.featuresList
                        .get(feat[j]).toString()) - featurePosition[j]
                        * ConstantParameters.MAXIMUMFEATURES)), new Float(-1.0
                        / (double)featurePosition[j]));
              else indexValues
                      .put(
                              new Long(
                                      (Long.parseLong(featList.featuresList
                                              .get(feat[j]).toString()) + (featurePosition[j] + maxNegPosition)
                                              * ConstantParameters.MAXIMUMFEATURES)),
                              new Float(1.0 / (double)featurePosition[j]));
            }
            ++n;
            // System.out.println(new Integer(n) +" feat=*"+feat +"*
            // index=*" + featList.featuresList.get(feat)+"*");
          }
        }
      } // end of the loop on the types
      if(n != nlpDoc.featuresCounted[i]) {
        // System.out.println("Error: the number of features (" + new
        // Integer(n)+
        // ") is not the same as the number recorded (" + new
        // Integer(nlpDoc.featuresCounted[i]) + ")in document "+ docId);
      }
      // for(int j=0; j<n; ++j) {
      // System.out.println("before " + new Integer(j) + " " +
      // indexes.get(j));
      // /}
      // System.out.println("before " + indexes.toString());
      // sort the indexes in ascending order
      List indexes = new ArrayList(indexValues.keySet());
      Collections.sort(indexes, new LongCompactor());

      // Iterator iterator = indexes.iterator();
      // n = 0;
      // while(iterator.hasNext()) {
      // Object key = iterator.next();
      // fvs[i].indexes[n] = ((Long)key).longValue();
      fvs[i] = new SparseFeatureVector(indexes.size());
      // for(int j=0; j<n; ++j) {
      for(int j = 0; j < indexes.size(); ++j) {
        // System.out.println(new Integer(j) +" index=*"+
        // indexes.get(j)+"*");
        fvs[i].indexes[j] = Integer.parseInt(indexes.get(j).toString());
        // for the constant value 1
        // fvs[i].values[j] = DEFAULTVALUE;
        // for the tf or tf*idf value
        fvs[i].values[j] = Float.parseFloat(indexValues.get(indexes.get(j))
                .toString());
      }

    } // end of the loop on the instances
  }

  public static class LongCompactor implements java.util.Comparator {

    public int compare(Object l1, Object l2) {
      // return (new Long((new Long(l1.toString()).longValue()- new
      // Long(l2.toString()).longValue()))).intValue();
      return (int)(Long.parseLong(l1.toString()) - Long
              .parseLong(l2.toString()));
    }
  }

  /** Read the feature vectors of a document from a file. */
  public void readDocFVFromFile(BufferedReader dataFile, int num,
          LabelsOfFeatureVectorDoc labelsDoc) {
    numInstances = num;
    fvs = new SparseFeatureVector[numInstances];
    labelsDoc.multiLabels = new LabelsOfFV[numInstances];
    labelsDoc.labels = new int[numInstances];
    try {
      String line;
      for(int i = 0; i < num; ++i) {
        line = dataFile.readLine();
        // System.out.println("i="+i+"line="+line);
        String[] items = line.split(ConstantParameters.ITEMSEPARATOR);
        // get the label from the line
        int iEndLabel;
        // get the simple label
        /*
         * iEndLabel = obtainSimpleLabels(items, labelsDoc, i);
         * //convert the label of 1-3 into 1-2 or multi-label
         * if(labelsDoc.labels[i]>0) { if(labelsDoc.labels[i]%3==0) {
         * int lenL = 2; labelsDoc.multiLabels[i] = new
         * LabelsOfFV(lenL); labelsDoc.multiLabels[i].labels = new
         * String[lenL]; labelsDoc.multiLabels[i].labels[0] = new
         * Integer((labelsDoc.labels[i]/3)*2-2).toString();
         * labelsDoc.multiLabels[i].labels[1] = new
         * Integer((labelsDoc.labels[i]/3)*2-1).toString(); } else { int
         * lenL = 1; labelsDoc.multiLabels[i] = new LabelsOfFV(lenL);
         * labelsDoc.multiLabels[i].labels = new String[lenL];
         * labelsDoc.multiLabels[i].labels[0] = new Integer(
         * (labelsDoc.labels[i]/3)*2+labelsDoc.labels[i]%3-1).toString(); }
         *  } else { labelsDoc.multiLabels[i] = new LabelsOfFV(0); }
         */
        // get the multilabel directly
        iEndLabel = obtainMultiLabels(items, labelsDoc.multiLabels, i);
        // get the feature vector
        int len = items.length - iEndLabel;
        fvs[i] = new SparseFeatureVector(len);
        obtainFVs(items, iEndLabel, len, fvs[i]);
      }
    }
    catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return;
  }

  /** Get the simple (one) label(s) from one line of feature vector. */
  private int obtainSimpleLabels(String[] items,
          LabelsOfFeatureVectorDoc labelsDoc, int ifv) {
    int kk = 1;// because the first item is the index of this instance
    labelsDoc.labels[ifv] = (new Integer(items[kk])).intValue();
    ++kk;
    return kk;
  }

  /** Get the multi label(s) from one line of feature vector. */
  private int obtainMultiLabels(String[] items, LabelsOfFV[] multiLabels, int i) {
    int num;
    int kk = 1;
    num = Integer.valueOf(items[kk++]);
    multiLabels[i] = new LabelsOfFV(num);
    if(num > 0) {
      multiLabels[i].labels = new int[num];
      for(int j = 0; j < num; ++j)
        multiLabels[i].labels[j] = Integer.valueOf(items[kk++]);
    }
    return kk;
  }

  /** Get the feature vector in parse format. */
  private void obtainFVs(String[] items, int iEndLabel, int len,
          SparseFeatureVector fv) {
    String[] indexValue;
    for(int i = 0; i < len; ++i) {
      indexValue = items[i + iEndLabel]
              .split(ConstantParameters.INDEXVALUESEPARATOR);
      if(indexValue.length <= 1) {
        System.out.println("i=" + i + " item=" + items[i + iEndLabel]);
      }
      fv.indexes[i] = (new Integer(indexValue[0])).intValue();
      fv.values[i] = (new Float(indexValue[1])).floatValue();
    }
    return;
  }

  /** Get number of instances in the document. */
  public int getNumInstances() {
    return numInstances;
  }

  /** Get the fv array. */
  public SparseFeatureVector[] getFvs() {
    return fvs;
  }

}
