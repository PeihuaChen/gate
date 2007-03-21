package gate.learningLightWeight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class NLPFeaturesList {
  public Hashtable featuresList = null; // the features with ids, maybe
                                        // accessed by multiple threads

  public Hashtable idfFeatures = null; // the document frequence of
                                        // each term, useful for
                                        // document or passage
                                        // classification

  int totalNumDocs;

  public final static String SYMBOLNGARM = "<>";

  public NLPFeaturesList() {
    featuresList = new Hashtable();
    idfFeatures = new Hashtable();
    totalNumDocs = 1;
  }

  public void loadFromFile(File parentDir, String filename) {

    File fileFeaturesList = new File(parentDir, filename);

    if(fileFeaturesList.exists()) {

      try {
        BufferedReader in = new BufferedReader(new FileReader(fileFeaturesList));
        // featuresList = new Hashtable();
        String line;
        if((line = in.readLine()) != null)
          totalNumDocs = (new Integer(line.substring(line.lastIndexOf("=") + 1)))
                  .intValue();

        while((line = in.readLine()) != null) {
          String[] st = line.split(" ");
          featuresList.put(st[0], st[1]);
          idfFeatures.put(st[0], st[2]);
        }
        in.close();

      }
      catch(IOException e) {
      }
    }
    else {
      System.out.println("No feature list file in initialisation phrase.");
    }
  }

  public void writeListIntoFile(File parentDir, String filename) {

    File fileFeaturesList = new File(parentDir, filename);
    System.out.println("Lengh of List = " + featuresList.size());
    try {
      PrintWriter out = new PrintWriter(new FileWriter(fileFeaturesList));
      // featuresList = new Hashtable()
      // for the total number of docs
      out.println("totalNumDocs=" + new Integer(totalNumDocs));

      List keys = new ArrayList(featuresList.keySet());
      Collections.sort(keys);

      // write the features list into the output file
      Iterator iterator = keys.iterator();
      while(iterator.hasNext()) {
        Object key = iterator.next();
        out.println(key + " " + featuresList.get(key) + " "
                + idfFeatures.get(key));
        // System.out.println(key+ " " + featuresList.get(key));
      }
      out.close();

    }
    catch(IOException e) {
    }
  }

  public void addFeaturesFromDoc(NLPFeaturesOfDoc fd) {

    long size = featuresList.size();
    for(int i = 0; i < fd.numInstances; ++i) {
      String[] features = fd.featuresInLine[i].toString().split(
              ConstantParameters.ITEMSEPARATOR);
      for(int j = 0; j < features.length; ++j) {
        String feat = features[j];
        if(feat.contains(SYMBOLNGARM))
          feat = feat.substring(0, feat.lastIndexOf(SYMBOLNGARM));
        if(!feat.equals(ConstantParameters.NAMENONFEATURE)) {
          if(size < ConstantParameters.MAXIMUMFEATURES) { // if the
                                                          // featureName
                                                          // is not in
                                                          // the feature
                                                          // list
            if(!featuresList.containsKey(feat)) {
              ++size;
              featuresList.put(feat, new Long(size)); // the index of
                                                      // features is
                                                      // from 1 (not
                                                      // zero), in the
                                                      // SVM-light
                                                      // format
              idfFeatures.put(feat, new Long(1));
            }
            else {
              idfFeatures.put(feat, new Long((new Long(idfFeatures.get(feat)
                      .toString())).longValue() + 1));
            }
          }
          else {
            System.out
                    .println("There are more NLP features from the training docuemnts");
            System.out.println(" than the pre-defined maximal number"
                    + new Long(ConstantParameters.MAXIMUMFEATURES));
            return;
          }
        }

      }
    }// end of the loop on the instances

    // update the total number of docs
    totalNumDocs += fd.numInstances;

  }

}
