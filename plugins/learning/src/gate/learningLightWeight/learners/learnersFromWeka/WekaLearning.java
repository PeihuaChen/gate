package gate.learningLightWeight.learners.learnersFromWeka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import gate.learningLightWeight.learners.MultiClassLearning;
import gate.learningLightWeight.ConstantParameters;

import gate.learningLightWeight.DocFeatureVectors;
import gate.learningLightWeight.Label2Id;
import gate.learningLightWeight.LabelsOfFV;
import gate.learningLightWeight.LabelsOfFeatureVectorDoc;
import gate.learningLightWeight.NLPFeaturesList;
import gate.learningLightWeight.SparseFeatureVector;

public class WekaLearning{
  /**The data in the Weka object for training or application.*/
  public Instances instancesData;
  
  /**The labels in the form of instances of every doc.*/
  public LabelsOfFeatureVectorDoc []  labelsFVDoc= null;
  
  public final static short SPARSEFVDATA = 2;
  public final static short NLPFEATUREFVDATA = 1;
  
  public void train(WekaLearner wekaCl, File modelFile) {

    //Training.
    wekaCl.training(instancesData);
    
    //Write the learner class into the modelfile by class serialisation
    
    try {
      FileOutputStream modelOutFile = new FileOutputStream(modelFile);
      ObjectOutputStream modelOutputObjectFile = new ObjectOutputStream(modelOutFile);
      modelOutputObjectFile.writeObject(wekaCl);
      modelOutputObjectFile.flush();
      modelOutputObjectFile.close();
      
    } catch(FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
 
  }
  
  public void apply(WekaLearner wekaCl, File modelFile, boolean distributionOutput) {
    //Read the learner class from the modelfile by class serialisation
    try {
      FileInputStream modelInFile = new FileInputStream(modelFile);
      ObjectInputStream modelInputObjectFile = new ObjectInputStream(modelInFile);
      wekaCl = (WekaLearner)modelInputObjectFile.readObject();
      modelInputObjectFile.close();
      
    } catch(FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch(ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    //Apply the model to the data.
    wekaCl.applying(instancesData, labelsFVDoc, distributionOutput);
  }
  
  
  public void readSparseFVsFromFile(File dataFile, int numDocs, boolean trainingMode, int numLabels, boolean surroundMode) {
   int numFeats = 0;
   int numClasses = 0;
   
   labelsFVDoc = new LabelsOfFeatureVectorDoc[numDocs];
   
   //Read the sparse FVs by using the method in MultiClassLearning class 
   MultiClassLearning multiClassL = new  MultiClassLearning();
   multiClassL.getDataFromFile(numDocs, dataFile);
   
   //Create the attributes.
   numFeats = multiClassL.dataFVinDoc.getTotalNumFeatures();
   FastVector attributes = new FastVector(numFeats+1);
   for(int i=0; i<numFeats; ++i)
     attributes.addElement(new Attribute(new Integer(i+1).toString()));
   //Add class attribute.
   
   /*numClasses = multiClassL.class2NumberInstances.keySet().size() + 1; //count the null too, as value -1.
   FastVector classValues = new FastVector(numClasses);
   classValues.addElement("-1"); //The first class for null class
   for(Object obj:multiClassL.class2NumberInstances.keySet())
     classValues.addElement(obj.toString());
   attributes.addElement(new Attribute("Class", classValues));*/
   
   if(surroundMode)
     numClasses =  2* numLabels+ 1; //count the null too, as value -1.
   else numClasses = numLabels + 1;
   FastVector classValues = new FastVector(numClasses);
   classValues.addElement("-1"); //The first class for null class
   for(int i=1; i<numClasses; ++i)
     classValues.addElement(new Integer(i).toString());
   attributes.addElement(new Attribute("Class", classValues));
   
   //Create the dataset with capacity of all FVs (but actuall number of FVs
   //mabe be larger than the pre-specified, because possible multi-label) and set index of class
   instancesData = new Instances("SparseFVsData", attributes, multiClassL.dataFVinDoc.getNumTraining());
   instancesData.setClassIndex(instancesData.numAttributes()-1);
   
   //Copy the data into the instance;
   for(int iDoc=0; iDoc<multiClassL.dataFVinDoc.getNumTrainingDocs(); ++iDoc) {
     SparseFeatureVector [] fvs = multiClassL.dataFVinDoc.trainingFVinDoc[iDoc].getFvs();
     labelsFVDoc[iDoc] = new LabelsOfFeatureVectorDoc();
     labelsFVDoc[iDoc].multiLabels = multiClassL.dataFVinDoc.labelsFVDoc[iDoc].multiLabels;
     for(int i=0; i<fvs.length; ++i) {
       //Object valueO = fvs[i].getValues();
       double [] values = new double[fvs[i].getLen()];
       for(int j=0; j<fvs[i].getLen(); ++j)
         values[j] = (double)fvs[i].values[j];
       SparseInstance inst = new SparseInstance(1.0, values, fvs[i].getIndexes(), 50000);
       inst.setDataset(instancesData);
       if(trainingMode && labelsFVDoc[iDoc].multiLabels[i].num>0) 
         for(int j1=0; j1<labelsFVDoc[iDoc].multiLabels[i].num; ++j1) {
           inst.setClassValue((labelsFVDoc[iDoc].multiLabels[i].labels[j1])); //label >0
           instancesData.add(inst);
         }
       else {
         inst.setClassValue("-1"); //set label as -1 for null 
         instancesData.add(inst);
       }
     }
   }
   
   return;
    
  }
  public void readNLPFeaturesFromFile(File dataFile, int numDocs, 
          NLPFeaturesList nlpFeatList, boolean trainingMode, int numLabels, boolean surroundMode){
   
    labelsFVDoc = new LabelsOfFeatureVectorDoc[numDocs];
    
    try {
      BufferedReader inData;
      inData = new BufferedReader(new FileReader(dataFile));
      //Get the number of attributes in the data
      String [] items = inData.readLine().split(ConstantParameters.ITEMSEPARATOR);
      HashMap metaFeats = new HashMap();
      int numFeats = 0;
      //Create an attribute for each meta feature
      HashMap entityToPosition = new HashMap();
      String entityTerm = "";
      int numEntity = 0;
      //Not include the class attribute
      for(int i=1; i<items.length; ++i) {
        //Assume the name of NGRAM should end with "gram"!!
        if(!items[i].endsWith("gram")) {
          if(!metaFeats.containsKey(items[i])) {
            metaFeats.put(items[i], new HashSet());
            ++numFeats; //counted as a new attribute
          }
          String feat = items[i].substring(0, items[i].lastIndexOf("("));
          String featNum = items[i].substring(items[i].lastIndexOf("("));
       
          if(!feat.equals(entityTerm)) {
            numEntity = 0;
            entityTerm = feat;
          } else ++numEntity;
          entityToPosition.put(feat+"_"+numEntity, featNum);
       
          if(!metaFeats.containsKey(feat)) {
            metaFeats.put(feat, new HashSet());
            //just for collect the terms
          }
        } 
      }
      List allTerms = new ArrayList(nlpFeatList.featuresList.keySet());
      Collections.sort(allTerms);
      for(int i=0; i<allTerms.size(); ++i) {
        String feat = allTerms.get(i).toString();
        if(isNgramFeat(feat)) {
          ++numFeats;
        } else {
          feat = feat.substring(feat.indexOf("_")+1);
          String feat1 = feat.substring(0,feat.indexOf("_"));//Name of the entity
          String feat2 = feat.substring(feat.indexOf("_")+1);//Term itself
          ((HashSet)metaFeats.get(feat1)).add(feat2);
          //System.out.println("feat="+feat+", feat1="+feat1+", feat2="+feat2+".");
        }
      }
      numFeats += 1; //include the class feature
   
      // Create the attributes.
      HashMap featToAttr = new HashMap(); //feat to attribute index
      FastVector attributes = new FastVector(numFeats);
      //First for the meta feature attribute.
      List metaFeatTerms = new ArrayList(metaFeats.keySet());
      int numMetaFeats = 0;
      for(int i=0; i<metaFeatTerms.size(); ++i) {
        String featName = metaFeatTerms.get(i).toString();
        if(featName.endsWith(")")) {
          String featName0 = featName.substring(0, featName.lastIndexOf("("));
          HashSet metaF = (HashSet)metaFeats.get(featName0);
          FastVector featFV = new FastVector(metaF.size());
          for(Object obj:metaF) 
            featFV.addElement(obj.toString());
          attributes.addElement(new Attribute(featName, featFV));
          featToAttr.put(featName, new Integer(numMetaFeats));
          //System.out.println("meta, featName"+featName+", featName0"+featName0);
          ++numMetaFeats;
        }
      }
      //Then the terms from ngram features
      for(int i=0; i<allTerms.size(); ++i) {
        String feat = allTerms.get(i).toString();
        if(isNgramFeat(feat)) {
          FastVector featFV = new FastVector(1);
          featFV.addElement(feat);
          attributes.addElement(new Attribute(feat, featFV));//Nominal form
          featToAttr.put(feat, new Integer(i+numMetaFeats));
        }
      }
   
      // Add class attribute.
      /*BufferedReader inLabels;
      inLabels = new BufferedReader(new FileReader(dataLabelFile));
      int numClasses;
      HashSet allLabels = new HashSet();
      String line = inLabels.readLine();
      numClasses = Integer.parseInt(line.substring(0, line.indexOf(ConstantParameters.ITEMSEPARATOR)));
      for(int i=0; i<numClasses; ++i)
        allLabels.add(inLabels.readLine().toString());
      numClasses += 1;
      FastVector classValues = new FastVector(numClasses);
      classValues.addElement("-1"); //The first label for null class.
      for(Object obj:allLabels) {
        classValues.addElement(obj.toString());
      }
      attributes.addElement(new Attribute("Class", classValues));*/
      
      int numClasses;
      if(surroundMode)
        numClasses =  2* numLabels+ 1; //count the null too, as value -1.
      else numClasses = numLabels + 1;
      FastVector classValues = new FastVector(numClasses);
      classValues.addElement("-1"); //The first class for null class
      for(int i=1; i<numClasses; ++i)
        classValues.addElement(new Integer(i).toString());
      attributes.addElement(new Attribute("Class", classValues));
   
      // Create the dataset with capacity of all FVs, and set index of class
      instancesData = new Instances("NLPFeatureData", attributes, numDocs*10);
      instancesData.setClassIndex(attributes.size()-1); //the first attribute is for class.
   
      //Read data from file and copy the data into the instance;
      for(int iDoc=0; iDoc<numDocs; ++iDoc) { //For each document
        items = inData.readLine().split(ConstantParameters.ITEMSEPARATOR);
        int num = Integer.parseInt(items[2]); //the third item is for number of instances in the doc.
        labelsFVDoc[iDoc] = new LabelsOfFeatureVectorDoc();
        labelsFVDoc[iDoc].multiLabels = new LabelsOfFV[num];
        for(int i=0; i<num; ++i) { //For each instance
          items = inData.readLine().split(ConstantParameters.ITEMSEPARATOR);
          Instance inst = new Instance(numFeats);
          inst.setDataset(instancesData);
          int numLabel = Integer.parseInt(items[0]); //number of labels for the instance
          entityTerm = "";
          numEntity = 0;
          for(int j=numLabel+1; j<items.length; ++j) { //for each NLP feature term
            if(!allTerms.contains(items[j])) continue; //skip the feature if it is not in the list
            if(isNgramFeat(items[j])) {//if it's a ngram
              items[j] = items[j].substring(0, items[j].lastIndexOf(NLPFeaturesList.SYMBOLNGARM));
              inst.setValue(Integer.parseInt(featToAttr.get(items[j]).toString()), items[j]);
            } else {//if not a ngram
              if(!items[j].equals(ConstantParameters.NAMENONFEATURE)) { //For real features, not "_NA"
                items[j] = items[j].substring(items[j].indexOf("_")+1); //get the feature term
                String feat1 = items[j].substring(0,items[j].indexOf("_")); //entity name
                String feat2 = items[j].substring(items[j].indexOf("_")+1); //feature name
                if(!feat1.equals(entityTerm)) {
                  numEntity = 0;
                  entityTerm = feat1;
                } else ++numEntity;
                feat1 = feat1+entityToPosition.get(feat1+"_"+numEntity).toString();
                //System.out.println(iDoc+ ", (i,j)="+i+" "+j+", items="+items[j]+", feat1="+feat1+", feat2="+feat2+".");
                inst.setValue(Integer.parseInt(featToAttr.get(feat1).toString()), feat2);
               
              }
            }
          }
          if(trainingMode && numLabel>0) {
            labelsFVDoc[iDoc].multiLabels[i] = new LabelsOfFV(numLabel);
            for(int j=1; j<=numLabel; ++j) {
              inst.setClassValue(items[j]);
              instancesData.add(inst);
            }
          } else {
            labelsFVDoc[iDoc].multiLabels[i] = new LabelsOfFV(0);
            inst.setClassValue("-1"); // set as null class
            instancesData.add(inst);
          }
        }//end of the loop i
      }
   
    } catch(FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return;
    
  }
   
   private boolean isNgramFeat(String item) {
     if(item.contains(NLPFeaturesList.SYMBOLNGARM))
       return true;
     else return false;
   }
}
