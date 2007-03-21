package gate.learningLightWeight;

import gate.learningLightWeight.DocFeatureVectors.LongCompactor;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class LabelsOfFeatureVectorDoc {
  /** The labels in training data. */
  public int[] labels;

  /** The class for annotation, from label2Id object. */
  int[] classes;

  short[] typesClass;

  /**
   * The object label for more complicated label and for
   * post-processing.
   */
  public LabelsOfFV[] multiLabels = null;

  public LabelsOfFeatureVectorDoc() {
  }

  public void obtainLabelsFromNLPDoc(NLPFeaturesOfDoc nlpDoc, Label2Id label2Id) {
    String currentN;
    int num = nlpDoc.numInstances;
    labels = new int[num];
    for(int i = 0; i < num; ++i) {
      labels[i] = 0;
      currentN = nlpDoc.classNames[i];
      // if(currentN instanceof String) { //use the new labels in the
      // document as well
      if(currentN instanceof String && label2Id.label2Id.containsKey(currentN)) { // just
                                                                                  // use
                                                                                  // the
                                                                                  // labels
                                                                                  // in
                                                                                  // the
                                                                                  // LabelsList.save
                                                                                  // file
        labels[i] = Integer.valueOf(label2Id.label2Id.get(currentN).toString());
      }
    }

  }

  public void obtainMultiLabelsFromNLPDoc(NLPFeaturesOfDoc nlpDoc,
          Label2Id label2Id) {
    String currentN;
    int num = nlpDoc.numInstances;
    multiLabels = new LabelsOfFV[num];
    for(int i = 0; i < num; ++i) {
      if(nlpDoc.classNames[i] instanceof String) {
        String[] items = nlpDoc.classNames[i]
                .split(ConstantParameters.ITEMSEPARATOR);
        multiLabels[i] = new LabelsOfFV(items.length);
        multiLabels[i].labels = new int[items.length];
        for(int j = 0; j < items.length; ++j) {
          currentN = items[j];
          // if(currentN instanceof String) { //use the new labels in
          // the document as well
          if(currentN instanceof String
                  && label2Id.label2Id.containsKey(currentN)) { // just
                                                                // use
                                                                // the
                                                                // labels
                                                                // in
                                                                // the
                                                                // LabelsList.save
                                                                // file
            multiLabels[i].labels[j] = Integer.valueOf(label2Id.label2Id.get(
                    currentN).toString()); // Integer.valueOf(label2Id.label2Id.get(currentN).toString());
          }
          else {
            multiLabels[i].labels[j] = 0;
          }
        }
      }
      else {
        multiLabels[i] = new LabelsOfFV(0);
      }
    }

  }

  public void obtainMultiLabelsFromNLPDocSurround(NLPFeaturesOfDoc nlpDoc,
          Label2Id label2Id, boolean surroundMode) {
    String currentN;
    int num = nlpDoc.numInstances;
    multiLabels = new LabelsOfFV[num];
    if(!surroundMode) {// not the surroundMode
      for(int i = 0; i < num; ++i) {
        HashSet setLabels = new HashSet();
        if(nlpDoc.classNames[i] instanceof String) {
          String[] items = nlpDoc.classNames[i]
                  .split(ConstantParameters.ITEMSEPARATOR);
          // multiLabels[i] = new LabelsOfFV(items.length);
          // multiLabels[i].labels = new int[items.length];
          for(int j = 0; j < items.length; ++j) {
            currentN = items[j];
            if(currentN.endsWith(ConstantParameters.SUFFIXSTARTTOKEN))
              currentN = currentN.substring(0, currentN
                      .lastIndexOf(ConstantParameters.SUFFIXSTARTTOKEN));
            if(label2Id.label2Id.containsKey(currentN))
              // just use the labels in the LabelsList.save file
              setLabels.add(Integer.valueOf(label2Id.label2Id.get(currentN)
                      .toString())); // Integer.valueOf(label2Id.label2Id.get(currentN).toString());
          }
        }
        multiLabels[i] = new LabelsOfFV(setLabels.size());
        if(setLabels.size() > 0) {
          multiLabels[i].labels = new int[setLabels.size()];
          List indexes = new ArrayList(setLabels);
          LongCompactor c = new LongCompactor();
          Collections.sort(indexes, c);
          for(int j = 0; j < indexes.size(); ++j)
            multiLabels[i].labels[j] = Integer.valueOf(indexes.get(j)
                    .toString()); // Integer.valueOf(obj.toString());
        }
      }// end of the i loop
    }
    else // for the surrond mode
    for(int i = 0; i < num; ++i) {
      HashSet setLabels = new HashSet();
      if(nlpDoc.classNames[i] instanceof String) {
        String[] items = nlpDoc.classNames[i]
                .split(ConstantParameters.ITEMSEPARATOR);
        for(int j = 0; j < items.length; ++j) {
          currentN = items[j];
          if(currentN.endsWith(ConstantParameters.SUFFIXSTARTTOKEN)) {
            String label = currentN.substring(0, currentN
                    .lastIndexOf(ConstantParameters.SUFFIXSTARTTOKEN));
            if(label2Id.label2Id.containsKey(label)) { // just use the
                                                        // labels in the
                                                        // LabelsList.save
                                                        // file
              setLabels.add(Integer.valueOf(label2Id.label2Id.get(label)
                      .toString()) * 2 - 1);
              if(i + 1 == num
                      || !hasTheSameLabel(label, nlpDoc.classNames[i + 1]))
                // single token
                setLabels.add(Integer.valueOf(label2Id.label2Id.get(label)
                        .toString()) * 2);
            }
          }
          else { // no start token
            if(label2Id.label2Id.containsKey(currentN)) { // just use
                                                          // the labels
                                                          // in the
                                                          // LabelsList.save
                                                          // file
              if(i + 1 == num) {// the last token, hence the end token
                setLabels.add(Integer.valueOf(label2Id.label2Id.get(currentN)
                        .toString()) * 2);
              }
              else if(!hasTheSameLabel(currentN, nlpDoc.classNames[i + 1]))
                setLabels.add(Integer.valueOf(label2Id.label2Id.get(currentN)
                        .toString()) * 2);
            }
          }
        }
      }
      multiLabels[i] = new LabelsOfFV(setLabels.size());
      if(setLabels.size() > 0) {
        multiLabels[i].labels = new int[setLabels.size()];
        List indexes = new ArrayList(setLabels);
        LongCompactor c = new LongCompactor();
        Collections.sort(indexes, c);
        for(int j = 0; j < indexes.size(); ++j) {
          multiLabels[i].labels[j] = Integer.valueOf(indexes.get(j).toString()); // Integer.valueOf(obj.toString());
        }
      }
    }// end of the i loop

  }

  private boolean hasTheSameLabel(String label, String classNames) {
    if(classNames instanceof String) {
      String[] items = classNames.split(ConstantParameters.ITEMSEPARATOR);
      for(int i = 0; i < items.length; ++i) {
        String currentN = items[i];
        if(currentN.endsWith(ConstantParameters.SUFFIXSTARTTOKEN))
          currentN = currentN.substring(0, currentN
                  .lastIndexOf(ConstantParameters.SUFFIXSTARTTOKEN));
        if(currentN.equals(label)) return true;
      }
    }
    return false;
  }

  public void convert2SurroundLabel() {
    int num = labels.length;
    int[] surroundLabels;
    surroundLabels = new int[num];
    for(int i = 0; i < num; ++i) {
      surroundLabels[i] = 0;
      if(labels[i] > 0) {
        if(i - 1 >= 0) // if not the first token in the document
          if(labels[i - 1] != labels[i]) // if not the same as with the
                                          // previous one
            if(i + 1 < num) // if not the last token in the document
              if(labels[i + 1] != labels[i]) // if not the same as with
                                              // the following one
                surroundLabels[i] = 3 * labels[i];
              else surroundLabels[i] = 3 * labels[i] - 2;
            else surroundLabels[i] = 3 * labels[i];
          else // if the same as with the previous one
          if(i + 1 < num) {
            if(labels[i + 1] != labels[i])
              surroundLabels[i] = 3 * labels[i] - 1;
          }
          else surroundLabels[i] = 3 * labels[i] - 1;
        else // if the first token
        if(i + 1 < num)
          if(labels[i + 1] != labels[i])
            surroundLabels[i] = 3 * labels[i];
          else surroundLabels[i] = 3 * labels[i] - 2;
        else surroundLabels[i] = 3 * labels[i];
      }
    }
    for(int i = 0; i < num; ++i) {
      labels[i] = surroundLabels[i];
    }

  }

  public void convertMulti2SurroundLabel() {
    int num = multiLabels.length;
    LabelsOfFV[] surroundLabels = new LabelsOfFV[num];
    for(int i = 0; i < num; ++i) {
      HashSet setLabels = new HashSet();
      for(int j = 0; j < multiLabels[i].num; ++j) {
        // String currentLabelS = multiLabels[i].labels[j];
        // int currentLabel = Integer.valueOf(currentLabelS);
        int currentLabel = multiLabels[i].labels[j];
        if(currentLabel > 0) {
          if(i - 1 >= 0) {// if not the first token in the document
            if(!isTheSameWith(currentLabel, multiLabels[i - 1])) {// if
                                                                  // not
                                                                  // the
                                                                  // same
                                                                  // as
                                                                  // with
                                                                  // the
                                                                  // previous
                                                                  // one
              if(i + 1 < num) {// if not the last token in the document
                if(!isTheSameWith(currentLabel, multiLabels[i + 1])) {
                  // if not the same as with the following one, single
                  // token entity
                  setLabels.add(new Integer(2 * currentLabel - 1));
                  setLabels.add(new Integer(2 * currentLabel));
                }
                else setLabels.add(new Integer(2 * currentLabel - 1));
              }
              else { // if the last token, single token entity
                setLabels.add(new Integer(2 * currentLabel - 1));
                setLabels.add(new Integer(2 * currentLabel));
              }
            }
            else // if the same as with the previous one
            if(i + 1 == num || !isTheSameWith(currentLabel, multiLabels[i + 1])) // end
                                                                                  // token
              setLabels.add(new Integer(2 * currentLabel));
          }
          else {// if the first token
            if(i + 1 < num) {
              if(!isTheSameWith(currentLabel, multiLabels[i + 1])) {
                setLabels.add(new Integer(2 * currentLabel - 1));
                setLabels.add(new Integer(2 * currentLabel));
              }
              else setLabels.add(new Integer(2 * currentLabel - 1));
            }
            else {
              setLabels.add(new Integer(2 * currentLabel - 1));
              setLabels.add(new Integer(2 * currentLabel));
            }
          }
        }// for the currentLabel>0
      }
      surroundLabels[i] = new LabelsOfFV(setLabels.size());
      if(setLabels.size() > 0) {
        surroundLabels[i].labels = new int[setLabels.size()];
        List indexes = new ArrayList(setLabels);
        LongCompactor c = new LongCompactor();
        Collections.sort(indexes, c);
        for(int j = 0; j < indexes.size(); ++j) {
          surroundLabels[i].labels[j] = Integer.valueOf(indexes.get(j)
                  .toString()); // Integer.valueOf(obj.toString());
        }
      }
    }

    for(int i = 0; i < num; ++i) {
      multiLabels[i] = surroundLabels[i];
    }

  }

  private boolean isTheSameWith(int label, LabelsOfFV labelsFV) {
    for(int i = 0; i < labelsFV.num; ++i)
      if(label == Integer.valueOf(labelsFV.labels[i])) return true;
    return false;
  }

  public void obtainLabelsFromResultFile(BufferedReader in) {
    // read the label from the results file

    try {
      String line;
      line = in.readLine().trim();
      int n = new Integer(line.substring(line.indexOf(' ') + 1, line
              .lastIndexOf(' '))).intValue();
      labels = new int[n];
      for(int i = 0; i < n; ++i) {
        line = in.readLine().trim();
        labels[i] = new Integer(line.substring(line.indexOf(' ') + 1))
                .intValue();
      }
    }
    catch(IOException e) {
    }
  }

  public void obtainAnnotationFromLabel() {
    int n = labels.length;
    classes = new int[n];
    typesClass = new short[n];
    int prevIndex = -1;
    int label;
    for(int i = 0; i < n; ++i)
      if(labels[i] > 0) {
        label = (labels[i] - 1) / 3 + 1;
        if(labels[i] % 3 == 0) {
          classes[i] = label;
          typesClass[i] = 4;
          prevIndex = -1;
        }
        else if(labels[i] % 3 == 1) {
          classes[i] = label;
          typesClass[i] = 1;
          prevIndex = i;
        }
        else {
          classes[i] = label;
          typesClass[i] = 3;
          if(prevIndex != -1) {
            if(labels[i] == labels[prevIndex] + 1)
              for(int j = prevIndex + 1; j < i; ++j) {
                classes[j] = label;
                typesClass[j] = 2;
              }
            prevIndex = -1;
          }
        }
      }
  }// end of the method

}
