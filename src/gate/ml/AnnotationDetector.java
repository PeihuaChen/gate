package gate.ml;

import java.util.*;

import weka.core.*;

import gate.*;
import gate.util.*;

/**
 * Detects the presence of annotations of given types.
 */
public class AnnotationDetector implements InstanceDetector {

  public AnnotationDetector() {
    annotationTypes = new ArrayList();
  }


  public void dataAdvance(Long offset) {
    //check the annotations that start at the offset for the required types.
    Set annotations = dataCollector.getStartingAnnotations(offset);
    if(annotations != null && !annotations.isEmpty()){
      Iterator annIter = annotations.iterator();
      while(annIter.hasNext()){
        Annotation annotation = (Annotation)annIter.next();
        if(annotationTypes.contains(annotation.getType())){
          //we found a new instance; generate the instance for it
          //all the atributes plus the class
          Instance instance = new Instance(
            dataCollector.getAttributeDetectors().size() + 1);
          instance.setDataset(dataCollector.getDataSet());
          Iterator attDetIter = dataCollector.getAttributeDetectors().
                                iterator();
          int currentAtt = 0;
          while(attDetIter.hasNext()){
            Object attValue = ((AttributeDetector)attDetIter.next()).
                              getAttributeValue(annotation);
            if(attValue != null){
              if(attValue instanceof Number){
                instance.setValue(currentAtt, ((Number)attValue).doubleValue());
              }else instance.setValue(currentAtt, attValue.toString());
            }else{
              instance.setMissing(currentAtt);
            }
            currentAtt ++;
          }
          //set the class
          instance.setValue(currentAtt, annotation.getType());
          dataCollector.addInstance(instance);
        }//if(annotationTypes.contains(annotation.getType()))
      }//while(annIter.hasNext()){
    }//if(annotations != null && !annotations.isEmpty()){
  }//public void dataAdvance(Long offset) {

  public void setDataCollector(DataCollector collector) {
    this.dataCollector = collector;
  }

  /**
   * Sets the annotation types that constitute instences this detector is
   * interested in.
   * The types are represented as a string containing the annotation types
   * separated by commas (e.g. &quot;Person,Organisation,Location&quot;.
   * @param typesList a String value.
   */
  public void setAnnotationTypes(String typesList){
    StringTokenizer strTok = new StringTokenizer(typesList, ",", false);
    annotationTypes.clear();
    while(strTok.hasMoreTokens())
      annotationTypes.add(strTok.nextToken().trim());
  }

  /**
   * Gets the definition for the attribute handled by this detector.
   * @return an Attribute object.
   */
  public Attribute getClassAttribute(){
    FastVector values = new FastVector(annotationTypes.size());
    Iterator typesIter = annotationTypes.iterator();
    while(typesIter.hasNext()) values.addElement(typesIter.next());
    return new Attribute("AnnotationType", values);
  }

  protected DataCollector dataCollector;

  protected List annotationTypes;
}