/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 28 May 2002
 *
 *  $Id$
 */
package gate.ml;

import java.util.*;
import javax.xml.parsers.*;
import java.net.*;

import gate.*;
import gate.util.*;
import gate.creole.*;

import weka.core.*;
/**
 * Collects training data from a corpus.
 * It iterates through the offsets in the annotation set and uses an instance
 * detector to find instances and a set of attribute detectors to find the
 * associated attributes.
 */
public class DataCollector extends AbstractLanguageAnalyser {
  /**
   * Default constructor.
   */
  public DataCollector() {
    attributeDetectors = new ArrayList();
    inputTypes = new ArrayList();
  }

  /**
   * Gets the annotations that start at a given offset.
   * Returns null or an empty list if none found.
   * @param offset a Long value
   * @return a Set value.
   */
  public Set getStartingAnnotations(Long offset){
    AnnotationsLists existingAnnotations = (AnnotationsLists)
                                           annotationsByOffset.get(offset);
    if(existingAnnotations == null) return null;
    else return existingAnnotations.startingAnnotations;
  }

  /**
   * Gets the annotations that end at a given offset.
   * Returns null or an empty list if none found.
   * @param offset a Long value
   * @return a Set value.
   */
  public Set getEndingAnnotations(Long offset){
    AnnotationsLists existingAnnotations = (AnnotationsLists)
                                           annotationsByOffset.get(offset);
    if(existingAnnotations == null) return null;
    else return existingAnnotations.endingAnnotations;
  }

  /**
   * Gets the next offset for a given offset using the natural ordering.
   * @param offset a Long value
   * @return a Long value.
   */
  public Long nextOffset(Long offset){
    if(annotationsByOffset == null ||
       annotationsByOffset.isEmpty()) return null;
    SortedMap tailMap = annotationsByOffset.tailMap(
                        new Long(offset.longValue() + 1));
    return (Long)((tailMap == null || tailMap.isEmpty()) ? null :
                                                           tailMap.firstKey());
  }

  /**
   * Gets the next offset for a given offset using the natural ordering.
   * @param offset a Long value
   * @return a Long value.
   */
  public Long previousOffset(Long offset){
    if(annotationsByOffset == null ||
       annotationsByOffset.isEmpty()) return null;
    SortedMap headMap = annotationsByOffset.subMap(
                          annotationsByOffset.firstKey(), offset);
    return (Long)((headMap == null || headMap.isEmpty()) ? null :
                                                           headMap.lastKey());
  }

  public void execute() throws ExecutionException{
    //check the input
    if(document == null)
      throw new ExecutionException("No document to process!");
    if(annotationSetName == null ||
       annotationSetName.equals("")) annotationSet = document.getAnnotations();
    else annotationSet = document.getAnnotations(annotationSetName);


    fireStatusChanged("Extracting data from " + document.getName() + "...");

    //get all the relevant offsets
    annotationsByOffset = new TreeMap();

    Iterator annIter = annotationSet.iterator();
    while(annIter.hasNext()){
      Annotation annotation = (Annotation)annIter.next();
      Long startOffset = annotation.getStartNode().getOffset();
      AnnotationsLists existingAnnotations = (AnnotationsLists)
                                              annotationsByOffset.
                                              get(startOffset);
      if(existingAnnotations == null){
        existingAnnotations = new AnnotationsLists();
        annotationsByOffset.put(startOffset, existingAnnotations);
      }
      existingAnnotations.startingAnnotations.add(annotation);

      Long endOffset = annotation.getEndNode().getOffset();
      existingAnnotations = (AnnotationsLists)annotationsByOffset.
                                              get(endOffset);
      if(existingAnnotations == null){
        existingAnnotations = new AnnotationsLists();
        annotationsByOffset.put(endOffset, existingAnnotations);
      }
      existingAnnotations.endingAnnotations.add(annotation);
    }

    //parse through all the offsets
    Iterator offsetsIter = annotationsByOffset.keySet().iterator();
    while(offsetsIter.hasNext()){
      fireDataAdvance((Long) offsetsIter.next());
    }
  }//public void execute() throws ExecutionException{

  /**
   * Adds a new instance to the dataset being constructed.
   * @param instance the instance value to be added.
   */
  public void addInstance(Instance instance){
    dataSet.add(instance);
  }

  public static void main(String[] args) {
  }


  public void setConfigFileURL(URL configFileURL) {
    this.configFileURL = configFileURL;
  }

  public URL getConfigFileURL() {
    return configFileURL;
  }

  public Resource init(){
    readConfigFile();
    //prepare the dataset
    FastVector attributes = new FastVector();
    Iterator attIter = attributeDetectors.iterator();
    while(attIter.hasNext()){
      attributes.addElement(((AttributeDetector)attIter.next()).getAttribute());
    }
    //add the attribute for the class
    attributes.addElement(instanceDetector.getClassAttribute());
    dataSet = new Instances(getName() + " Dataset", attributes, 0);

    return this;
  }

  /**
   * Reads the configuration file and populates internal data with values.
   */
  protected void readConfigFile(){
    //hardcoded for now
    AnnotationDetector annotationDetector = new AnnotationDetector();
    annotationDetector.setAnnotationTypes("Date,Person,Location,Organization,Money");
    setInstanceDetector(annotationDetector);
    //Add attributes now

    //annotation length (in tokens)
    addAttributeDetector(new AnnotationLengthExtractor());
    //POS category and orthography for the first 7 tokens
    for(int i = 1; i <= 7; i++){
      POSCategoryExtractor posExtractor = new POSCategoryExtractor();
      posExtractor.setPosition(i);
      //look in the right context too
//      posExtractor.setIgnoreRightContext(false);
      addAttributeDetector(posExtractor);

      TokenOrthographyExtractor orthExtractor = new TokenOrthographyExtractor();
      orthExtractor.setPosition(i);
      addAttributeDetector(orthExtractor);
    }


    //POS category and orthography for 3 tokens left context
    for(int i = -1; i >= -3; i--){
      POSCategoryExtractor posExtractor = new POSCategoryExtractor();
      posExtractor.setPosition(i);
      addAttributeDetector(posExtractor);

      TokenOrthographyExtractor orthExtractor = new TokenOrthographyExtractor();
      orthExtractor.setPosition(i);
      addAttributeDetector(orthExtractor);
    }

    //Lookup type and position for the first 3 lookups
    LookupDetector lookupDetector = new LookupDetector();
    //type - 1
    addAttributeDetector(lookupDetector);
    //position - 1
    addAttributeDetector(lookupDetector);
    //type - 2
    addAttributeDetector(lookupDetector);
    //position - 2
    addAttributeDetector(lookupDetector);
    //type -3
    addAttributeDetector(lookupDetector);
    //position - 3
    addAttributeDetector(lookupDetector);
  }

  public void setState(int state) {
    this.state = state;
  }

  public int getState() {
    return state;
  }
  public synchronized void removeDataListener(DataListener l) {
    if (dataListeners != null && dataListeners.contains(l)) {
      Vector v = (Vector) dataListeners.clone();
      v.removeElement(l);
      dataListeners = v;
    }
  }
  public synchronized void addDataListener(DataListener l) {
    Vector v = dataListeners == null ? new Vector(2) : (Vector) dataListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      dataListeners = v;
      l.setDataCollector(this);
    }
  }

  /**
   * URL to the file containing the configuration.
   */
  protected URL configFileURL;

  /**
   * The types of annotation to be considered. Annotations of types not
   * contained here will be ignored.
   */
  List inputTypes;

  protected AnnotationSet annotationSet;
  public Instances getDataSet(){
    return dataSet;
  }

  protected Instances dataSet;

  protected InstanceDetector instanceDetector;

  /**
   * Stores the annotations from the input annotation set by offset (starting
   * and ending). Maps from Long (offset) to {@link AnnotationsLists}.
   */
  protected SortedMap annotationsByOffset;

  /**
   * A structure that stores the annotations relevant for an offset: a list of
   * annotations that start at the offset and a list of annotations that end at
   * the offset.
   */
  protected static class AnnotationsLists{
    public AnnotationsLists(){
      startingAnnotations = new HashSet();
      endingAnnotations = new HashSet();
    }

    public Set startingAnnotations;
    public Set endingAnnotations;
  }

  List attributeDetectors;

  /**
   * The state of the data collector. Can be one of {@link BEFORE},
   * {@link INSIDE} or {@link AFTER} according to the relation between the
   * current location in the document and the instance being constructed.
   * The value of the state is controlled by the instance detector.
   */
  protected int state;

  private transient Vector dataListeners;
  private String annotationSetName;

  protected void fireDataAdvance(Long e) {
    if (dataListeners != null) {
      Vector listeners = dataListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((DataListener) listeners.elementAt(i)).dataAdvance(e);
      }
    }
  }

  public void addAttributeDetector(AttributeDetector attrDetector){
    attributeDetectors.add(attrDetector);
    attrDetector.setDataCollector(this);
  }

  public List getAttributeDetectors(){
    return attributeDetectors;
  }

  public InstanceDetector getInstanceDetector() {
    return instanceDetector;
  }

  public void setInstanceDetector(InstanceDetector instanceDetector) {
    if(instanceDetector != null) removeDataListener(instanceDetector);
    this.instanceDetector = instanceDetector;
    addDataListener(instanceDetector);
  }
  public void setAnnotationSetName(String annotationSetName) {
    this.annotationSetName = annotationSetName;
  }
  public String getAnnotationSetName() {
    return annotationSetName;
  }
}