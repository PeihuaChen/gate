/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 21/11/2002
 *
 *  $Id$
 *
 */
package gate.creole.ml.weka;

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.util.zip.*;

import org.jdom.Element;

import weka.core.*;
import weka.classifiers.*;

import gate.creole.ml.*;
import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.gui.*;

/**
 * Wrapper class for the WEKA Machine Learning Engine.
 * {@ see http://www.cs.waikato.ac.nz/ml/weka/}
 */

public class Wrapper implements MLEngine, ActionsPublisher {

  public Wrapper() {
    actionsList = new ArrayList();
    actionsList.add(new LoadModelAction());
    actionsList.add(new SaveModelAction());
    actionsList.add(new SaveDatasetAsArffAction());
  }

  public void setOptions(Element optionsElem) {
    this.optionsElement = optionsElem;
  }

  public void addTrainingInstance(List attributeValues)
              throws ExecutionException{
    Instance instance = buildInstance(attributeValues);
    dataset.add(instance);
    if(classifier != null){
      if(classifier instanceof UpdateableClassifier){
        //the classifier can learn on the fly; we need to update it
        try{
          ((UpdateableClassifier)classifier).updateClassifier(instance);
        }catch(Exception e){
          throw new GateRuntimeException(
            "Could not update updateable classifier! Problem was:\n" +
            e.toString());
        }
      }else{
        //the classifier is not updatebale; we need to mark the dataset as changed
        datasetChanged = true;
      }
    }
  }

  /**
   * Constructs an instance valid for the current dataset from a list of
   * attribute values.
   * @param attributeValues the values for the attributes.
   * @return an {@link weka.core.Instance} value.
   */
  protected Instance buildInstance(List attributeValues)
            throws ExecutionException{
    //sanity check
    if(attributeValues.size() != datasetDefinition.getAttributes().size()){
      throw new ExecutionException(
        "The number of attributes provided is wrong for this dataset!");
    }

    double[] values = new double[datasetDefinition.getAttributes().size()];
    int index = 0;
    Iterator attrIter = datasetDefinition.getAttributes().iterator();
    Iterator valuesIter = attributeValues.iterator();

    Instance instance = new Instance(attributeValues.size());
    instance.setDataset(dataset);

    while(attrIter.hasNext()){
      gate.creole.ml.Attribute attr = (gate.creole.ml.Attribute)attrIter.next();
      String value = (String)valuesIter.next();
      if(value == null){
        instance.setMissing(index);
      }else{
        if(attr.getFeature() == null){
          //boolean attribute ->the value should already be true/false
          instance.setValue(index, value);
        }else{
          //nominal or numeric attribute
          if(attr.getValues() != null && !attr.getValues().isEmpty()){
            //nominal attribute
            if(attr.getValues().contains(value)){
              instance.setValue(index, value);
            }else{
              Out.prln("Warning: invalid value: \"" + value +
                       "\" for attribute " + attr.getName() + " was ignored!");
              instance.setMissing(index);
            }
          }else{
            //numeric attribute
            try{
              double db = Double.parseDouble(value);
              instance.setValue(index, db);
            }catch(Exception e){
              Out.prln("Warning: invalid numeric value: \"" + value +
                       "\" for attribute " + attr.getName() + " was ignored!");
              instance.setMissing(index);
            }
          }
        }
      }
      index ++;
    }
    return instance;
  }

  public void setDatasetDefinition(DatasetDefintion definition) {
    this.datasetDefinition = definition;
  }

  public Object classifyInstance(List attributeValues)
         throws ExecutionException {
    Instance instance = buildInstance(attributeValues);
//    double result;

    try{
      if(classifier instanceof UpdateableClassifier){
        return convertAttributeValue(classifier.classifyInstance(instance));
      }else{
        if(datasetChanged){
          classifier.buildClassifier(dataset);
          datasetChanged = false;
        }

        if(confidenceThreshold > 0 &&
           dataset.classAttribute().type() == weka.core.Attribute.NOMINAL){
          //confidence set; use probability distribution

          double[] distribution = null;
          distribution = ((DistributionClassifier)classifier).
                                  distributionForInstance(instance);

          List res = new ArrayList();
          for(int i = 0; i < distribution.length; i++){
            if(distribution[i] >= confidenceThreshold){
              res.add(dataset.classAttribute().value(i));
            }
          }
          return res;

        }else{
          //confidence not set; use simple classification
          return convertAttributeValue(classifier.classifyInstance(instance));
        }
      }
    }catch(Exception e){
      throw new ExecutionException(e);
    }
  }

  protected Object convertAttributeValue(double value){
    gate.creole.ml.Attribute classAttr = datasetDefinition.getClassAttribute();
    List classValues = classAttr.getValues();
    if(classValues != null && !classValues.isEmpty()){
      //nominal attribute
      return dataset.attribute(datasetDefinition.getClassIndex()).
                     value((int)value);
    }else{
      if(classAttr.getFeature() == null){
        //boolean attribute
        return dataset.attribute(datasetDefinition.getClassIndex()).
                       value((int)value);
      }else{
        //numeric attribute
        return new Double(value);
      }
    }
  }
  /**
   * Initialises the classifier and prepares for running.
   * @throws GateException
   */
  public void init() throws GateException{
    //find the classifier to be used
    Element classifierElem = optionsElement.getChild("CLASSIFIER");
    if(classifierElem == null){
      Out.prln("Warning (WEKA ML engine): no classifier selected;" +
               " dataset collection only!");
      classifier = null;
    }else{
      String classifierClassName = classifierElem.getTextTrim();


      //get the options for the classiffier
      String[] options;
      Element classifierOptionsElem = optionsElement.getChild("CLASSIFIER-OPTIONS");
      if(classifierOptionsElem == null){
        options = new String[]{};
      }else{
        List optionsList = new ArrayList();
        StringTokenizer strTok =
          new StringTokenizer(classifierOptionsElem.getTextTrim() , " ", false);
        while(strTok.hasMoreTokens()){
          optionsList.add(strTok.nextToken());
        }
        options = (String[])optionsList.toArray(new String[optionsList.size()]);
      }

      try{
        classifier = Classifier.forName(classifierClassName, options);
      }catch(Exception e){
        throw new GateException(e);
      }
      Element anElement = optionsElement.getChild("CONFIDENCE-THRESHOLD");
      if(anElement != null){
        try{
          confidenceThreshold = Double.parseDouble(anElement.getTextTrim());
        }catch(Exception e){
          throw new GateException(
            "Could not parse confidence threshold value: " +
            anElement.getTextTrim() + "!");
        }
        if(!(classifier instanceof DistributionClassifier)){
          throw new GateException(
            "Cannot use confidence threshold with classifier: " +
            classifier.getClass().getName() + "!");
        }
      }

    }

    //initialise the dataset
    FastVector attributes = new FastVector();
    weka.core.Attribute classAttribute;
    Iterator attIter = datasetDefinition.getAttributes().iterator();
    while(attIter.hasNext()){
      gate.creole.ml.Attribute aGateAttr =
        (gate.creole.ml.Attribute)attIter.next();
      weka.core.Attribute aWekaAttribute = null;
      if(aGateAttr.getValues() != null && !aGateAttr.getValues().isEmpty()){
        //nominal attribute
        FastVector attrValues = new FastVector(aGateAttr.getValues().size());
        Iterator valIter = aGateAttr.getValues().iterator();
        while(valIter.hasNext()){
          attrValues.addElement(valIter.next());
        }
        aWekaAttribute = new weka.core.Attribute(aGateAttr.getName(),
                                                 attrValues);
      }else{
        if(aGateAttr.getFeature() == null){
          //boolean attribute ([lack of] presence of an annotation)
          FastVector attrValues = new FastVector(2);
          attrValues.addElement("true");
          attrValues.addElement("false");
          aWekaAttribute = new weka.core.Attribute(aGateAttr.getName(),
                                                   attrValues);
        }else{
          //feature is not null but no values provided -> numeric attribute
          aWekaAttribute = new weka.core.Attribute(aGateAttr.getName());
        }
      }
      if(aGateAttr.isClass()) classAttribute = aWekaAttribute;
      attributes.addElement(aWekaAttribute);
    }

    dataset = new Instances("Weka ML Engine Dataset", attributes, 0);
    dataset.setClassIndex(datasetDefinition.getClassIndex());

    if(classifier != null && classifier instanceof UpdateableClassifier){
      try{
        classifier.buildClassifier(dataset);
      }catch(Exception e){
        throw new ResourceInstantiationException(e);
      }
    }
  }


  /**
   * Loads the state of this engine from previously saved data.
   * @param is
   */
  protected void load(InputStream is) throws IOException{
    ObjectInputStream ois = new ObjectInputStream(is);
    try{
      classifier = (Classifier)ois.readObject();
      dataset = (Instances)ois.readObject();
      datasetDefinition = (DatasetDefintion)ois.readObject();
    }catch(ClassNotFoundException cnfe){
      throw new GateRuntimeException(cnfe.toString());
    }
    ois.close();
  }

  /**
   * Saves the state of the engine for reuse at a later time.
   * @param os
   */
  protected void save(OutputStream os) throws IOException{
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(classifier);
    oos.writeObject(dataset);
    oos.writeObject(datasetDefinition);
    oos.flush();
    oos.close();
  }

  /**
   * Gets the list of actions that can be performed on this resource.
   * @return a List of Action objects (or null values)
   */
  public List getActions(){
    return actionsList;
  }

  protected class SaveDatasetAsArffAction extends javax.swing.AbstractAction{
    public SaveDatasetAsArffAction(){
      super("Save dataset as ARFF");
      putValue(SHORT_DESCRIPTION, "Saves the ML model to a file in ARFF format");
    }

    public void actionPerformed(java.awt.event.ActionEvent evt){
      JFileChooser fileChooser = MainFrame.getFileChooser();
      fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
      fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);
      fileChooser.setMultiSelectionEnabled(false);
      if(fileChooser.showSaveDialog(null) == fileChooser.APPROVE_OPTION){
        File file = fileChooser.getSelectedFile();
        try{
          FileWriter fw = new FileWriter(file, false);
          fw.write(dataset.toString());
          fw.flush();
          fw.close();
        }catch(IOException ioe){
          JOptionPane.showMessageDialog(null,
                          "Error!\n"+
                           ioe.toString(),
                           "Gate", JOptionPane.ERROR_MESSAGE);
          ioe.printStackTrace(Err.getPrintWriter());
        }
      }
    }
  }


  protected class SaveModelAction extends javax.swing.AbstractAction{
    public SaveModelAction(){
      super("Save model");
      putValue(SHORT_DESCRIPTION, "Saves the ML model to a file");
    }

    public void actionPerformed(java.awt.event.ActionEvent evt){
      JFileChooser fileChooser = MainFrame.getFileChooser();
      fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
      fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);
      fileChooser.setMultiSelectionEnabled(false);
      if(fileChooser.showSaveDialog(null) == fileChooser.APPROVE_OPTION){
        File file = fileChooser.getSelectedFile();
        try{
          save(new GZIPOutputStream(new FileOutputStream(file)));
        }catch(IOException ioe){
          JOptionPane.showMessageDialog(null,
                          "Error!\n"+
                           ioe.toString(),
                           "Gate", JOptionPane.ERROR_MESSAGE);
          ioe.printStackTrace(Err.getPrintWriter());
        }
      }
    }
  }

  protected class LoadModelAction extends javax.swing.AbstractAction{
    public LoadModelAction(){
      super("Load model");
      putValue(SHORT_DESCRIPTION, "Loads a ML model from a file");
    }

    public void actionPerformed(java.awt.event.ActionEvent evt){
    }
  }

  protected DatasetDefintion datasetDefinition;

  double confidenceThreshold = 0;

  /**
   * The WEKA classifier used by this wrapper
   */
  protected Classifier classifier;

  /**
   * The dataset used for training
   */
  protected Instances dataset;

  /**
   * The JDom element contaning the options fro this wrapper.
   */
  protected Element optionsElement;

  /**
   * Marks whether the dataset was changed since the last time the classifier
   * was built.
   */
  protected boolean datasetChanged = false;

  protected List actionsList;
}