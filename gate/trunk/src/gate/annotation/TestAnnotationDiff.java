/*
 *  TestAnnotationDiff.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  Cristian URSU, 06/Nov/2000
 *
 *  $Id$
 */

package gate.annotation;

import java.util.*;
import java.io.*;
import java.net.*;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;

import gate.util.*;
import gate.gui.*;
import gate.creole.*;
import gate.*;


/**
  */
public class TestAnnotationDiff extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The Precision value (see NLP Information Extraction)*/
  private Double precision = null;

  /** The Recall value (see NLP Information Extraction)*/
  private Double recall = null;


  /** Construction */
  public TestAnnotationDiff(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {

  } // setUp

  /** A test */
  public void testDiff() throws Exception {
    // Create a AnnotationSchema object from URL.
    ResourceData resData = (ResourceData)
      Gate.getCreoleRegister().get("gate.creole.AnnotationSchema");

    FeatureMap parameters = Factory.newFeatureMap();
    parameters.put("xmlFileUrl", resData.getXmlFileUrl());

    AnnotationSchema annotationSchema = (AnnotationSchema)
      Factory.createResource("gate.creole.AnnotationSchema", parameters);

    URL url = Gate.getUrl("tests/xml/POSSchema.xml");
    annotationSchema.fromXSchema(url);

    // Load the xml Key Document and unpack it
    gate.Document keyDocument =
       gate.Factory.newDocument(
          Gate.getUrl("tests/annotDiff/KeyDocument.xml")
       );
    gate.DocumentFormat keyDocFormat = gate.DocumentFormat.getDocumentFormat(
      keyDocument, keyDocument.getSourceUrl()
    );
    keyDocFormat.unpackMarkup (keyDocument,"DocumentContent");
    // Load the xml Response Document and unpack it
    gate.Document responseDocument =
        gate.Factory.newDocument(
            Gate.getUrl("tests/annotDiff/ResponseDocument.xml")
        );
    gate.DocumentFormat responseDocFormat =
        gate.DocumentFormat.getDocumentFormat(
                responseDocument, responseDocument.getSourceUrl()
        );
    responseDocFormat.unpackMarkup (responseDocument,"DocumentContent");

    AnnotationSet keyAnnotSet = null;
    AnnotationSet responseAnnotSet = null;
    Set diffSet  = null;
    // Get the key AnnotationSet from the keyDocument
    keyAnnotSet = keyDocument.getAnnotations().get(
                              annotationSchema.getAnnotationName());
    // Get the response AnnotationSet from the resonseDocument
    responseAnnotSet = responseDocument.getAnnotations().get(
                                        annotationSchema.getAnnotationName());
    diffSet = doDiff(keyAnnotSet, responseAnnotSet);

/*
    // Create an AnnotationDiff object.
    // Creole.xml must contain a entry for AnnotationDiff.
    // If not, you will get an exception (couldn't configure resource metadata)

    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",keyDocument);
    parameters.put("responseDocument",responseDocument);
    parameters.put("annotationSchema",annotSchema);

    // Create Annotation Diff visual resource
    AnnotationDiff annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);
*/

    assert("Diffset is NULL. It shouldn't be NULL",diffSet!= null);
    assert("Precision changed.That's because of the key/response document or" +
            " code implementation!",
            new Double(0.6666666666666666).equals(precision));
    assert("Recall changed.That's because of the key/response document or" +
            " code implementation!",
            new Double(0.7272727272727273).equals(recall));

    // Display the component
/*
    JFrame jFrame = new JFrame("AnnotationDiff GUI");
    jFrame.getContentPane().add(annotDiff, BorderLayout.CENTER);
    jFrame.pack();
    jFrame.setVisible(true);
/*/

  } // testDiff()

  public void testAnnotationAreEquals() throws Exception {
    Node node1 = new NodeImpl(new Integer(1),new Long(10));
    Node node2 = new NodeImpl(new Integer(2),new Long(20));
    Node node3 = new NodeImpl(new Integer(3),new Long(15));
    Node node4 = new NodeImpl(new Integer(4),new Long(15));
    Node node5 = new NodeImpl(new Integer(5),new Long(20));
    Node node6 = new NodeImpl(new Integer(6),new Long(30));

    FeatureMap fm1 = new SimpleFeatureMapImpl();
    fm1.put("color","red");
    fm1.put("Age",new Long(25));
    fm1.put(new Long(23), "Cristian");

    FeatureMap fm2 = new SimpleFeatureMapImpl();
    fm2.put("color","red");
    fm2.put("Age",new Long(25));
    fm2.put(new Long(23), "Cristian");

    FeatureMap fm4 = new SimpleFeatureMapImpl();
    fm4.put("color","red");
    fm4.put("Age",new Long(26));
    fm4.put(new Long(23), "Cristian");

    FeatureMap fm3 = new SimpleFeatureMapImpl();
    fm3.put("color","red");
    fm3.put("Age",new Long(25));
    fm3.put(new Long(23), "Cristian");
    fm3.put("best",new Boolean(true));

    Annotation annot1 = new AnnotationImpl(new Integer(1),
                                           node1,
                                           node2,
                                           "word",
                                           null);
    Annotation annot2 = new AnnotationImpl (new Integer(2),
                                            node2,
                                            node6,
                                            "sentence",
                                            null);
    Annotation annot3 = new AnnotationImpl (new Integer(3),
                                            node5,
                                            node6,
                                            "sentence",
                                            null);

   // types and offsets not equals
   assert("Those annotations must not be equal!",
                                !areEqual(annot1,annot2));
   // Those two must be equals
   assert("Those annotations must be equal!",areEqual(annot2,annot3));

   annot2.setFeatures(fm1);
   annot3.setFeatures(fm2);
   assert("Those annotations must be equal!",areEqual(annot2,annot3));

   annot2.setFeatures(fm1);
   annot3.setFeatures(fm3);
   assert("Those annotations must NOT be equal!",
                          !areEqual(annot2,annot3));

   annot3.setFeatures(null);
   assert("Those annotations must NOT be equal!",
                      !areEqual(annot2,annot3));

    annot3.setFeatures(fm4);
    assert("Those annotations must not be equal!",
                                    !areEqual(annot2,annot3));
  }// testAnnotationEquals

  /** This method does the AnnotationSet diff and creates a set with
    * diffSetElement objects.
    */
  protected Set doDiff( AnnotationSet aKeyAnnotSet,
                        AnnotationSet aResponseAnnotSet){

    // If one of the annotation sets is null then is no point in doing the diff.
    if (aKeyAnnotSet == null || aResponseAnnotSet == null)
      return null;

    Set diffSet = new HashSet();
    Set keyBackupSet = new HashSet();
    Set responseBackupSet = new HashSet();

    int correctItems = 0;
    int totalItemsRetrived = aResponseAnnotSet.size();
    int totalCorrectItems = aKeyAnnotSet.size();

    // Take all annotations from KeySet and detect all from RespnseSet that are
    // equals.
    Iterator keyIterator = aKeyAnnotSet.iterator();
    boolean stopLoop = false;
    while(keyIterator.hasNext() && !stopLoop){
      Annotation keyAnnot = (Annotation) keyIterator.next();
      Iterator responseIterator = aResponseAnnotSet.iterator();
      // There are no elements in responseSet, then quit this loop.
      if (!responseIterator.hasNext()){
        stopLoop = true;
        continue;
      } // end if
      DiffSetElement diffElement = null;
      while(responseIterator.hasNext()){
        Annotation responseAnnot = (Annotation) responseIterator.next();
        if(areEqual(keyAnnot,responseAnnot)){
          // Create a new DiffSetElement and ass it to the diffSet
          diffElement = new DiffSetElement(keyAnnot,responseAnnot);
          responseIterator.remove();
          // We need to be able to keep intact the responseAnnotSet.
          // That's why the annotation is saved temporary here.
          responseBackupSet.add(responseAnnot);
          // Calculate the number of correct items retrieved
          correctItems ++;
          // This break is here because we are iterating through a set and
          // once we found one element equal to the key element, it means there
          // is no other element and it's no point to search the nexts elements.
          break;
        } // end if
      }// end while responseIterator
      if (diffElement == null)
        diffElement = new DiffSetElement(keyAnnot,null);
      diffSet.add(diffElement);
      keyIterator.remove();
      // We need to be able to keep intact the keyAnnotSet.
      // That's why the annotation is saved temporary here.
      keyBackupSet.add(keyAnnot);
    }// end while keyIterator

    while(keyIterator.hasNext()){
      Annotation keyAnnot = (Annotation) keyIterator.next();
      DiffSetElement diffElement = new DiffSetElement(keyAnnot,null);
      diffSet.add(diffElement);
    }// end while
    Iterator responseIterator = aResponseAnnotSet.iterator();
    while(responseIterator.hasNext()){
      Annotation responseAnnot = (Annotation) responseIterator.next();
      DiffSetElement diffElement = new DiffSetElement(null, responseAnnot);
      diffSet.add(diffElement);
    }// end while

    // Rebuild the original annotation Sets
    aKeyAnnotSet.addAll(keyBackupSet);
    aResponseAnnotSet.addAll(responseBackupSet);

    // Calculate Precision. The formula is:
    // Precision = No of correct items retrieved / total no. of items retrieved
    if (totalItemsRetrived == 0)
      precision = new Double(0);
    else
      precision = new Double((double) correctItems/totalItemsRetrived);

    // Calculate Recall. The formula is:
    // Recall = no. of correct items retrieved / total no. of correct items
    if (totalCorrectItems == 0)
      recall = new Double(0);
    else
      recall = new Double((double) correctItems/totalCorrectItems);

    return diffSet;
  }// doDiff

  /**  Returns true if two annotation are Equals.
    *  Two Annotation are equals if their offsets, types and features are the
    *  same.
    */
  protected boolean areEqual(   Annotation anAnnot,
                                Annotation otherAnnot){
    if(anAnnot == null || otherAnnot == null)
      return false;

    // If their types are not equals then return false
    if((anAnnot.getType() == null) ^ (otherAnnot.getType() == null))
      return false;
    if( anAnnot.getType() != null &&
        (!anAnnot.getType().equals(otherAnnot.getType())))
      return false;

    // If their start offset is not the same then return false
    if((anAnnot.getStartNode() == null) ^ (otherAnnot.getStartNode() == null))
      return false;
    if(anAnnot.getStartNode() != null){
      if((anAnnot.getStartNode().getOffset() == null) ^
         (otherAnnot.getStartNode().getOffset() == null))
        return false;
      if(anAnnot.getStartNode().getOffset() != null &&
        (!anAnnot.getStartNode().getOffset().equals(
                            otherAnnot.getStartNode().getOffset())))
        return false;
    }

  // If their end offset is not the same then return false
    if((anAnnot.getEndNode() == null) ^ (otherAnnot.getEndNode() == null))
      return false;
    if(anAnnot.getEndNode() != null){
      if((anAnnot.getEndNode().getOffset() == null) ^
         (otherAnnot.getEndNode().getOffset() == null))
        return false;
      if(anAnnot.getEndNode().getOffset() != null &&
        (!anAnnot.getEndNode().getOffset().equals(
              otherAnnot.getEndNode().getOffset())))
        return false;
    }

    // If their featureMaps are not equals then return false
    if((anAnnot.getFeatures() == null) ^ (otherAnnot.getFeatures() == null))
      return false;
    if(anAnnot.getFeatures() != null && (!anAnnot.getFeatures().equals(
                                      otherAnnot.getFeatures())))
      return false;
    return true;
  }// areEqual

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestAnnotationDiff.class);
  } // suite

  public static void main(String[] args) {
    try{
      Gate.init();
      TestAnnotationDiff testAnnotDiff = new TestAnnotationDiff("");
      testAnnotDiff.testDiff();
      testAnnotDiff.testAnnotationAreEquals();
    }catch(Exception e){
      e.printStackTrace();
    }
  }// main

  /**
    * This class is used for internal purposes. It represents a data row
    */
  protected class DiffSetElement{

    private Annotation leftAnnotation = null;

    private Annotation rightAnnotation = null;

    public DiffSetElement(){
    }// DiffSetElement

    /** Constructor for DiffSetlement*/
    public DiffSetElement( Annotation aLeftAnnotation,
                           Annotation aRightAnnotation){
      leftAnnotation = aLeftAnnotation;
      rightAnnotation = aRightAnnotation;
    }// DiffSetElement

    /** Sets the left annotation*/
    public void setLeftAnnotation(Annotation aLeftAnnotation){
      leftAnnotation = aLeftAnnotation;
    }// setLeftAnnot

    /** Gets the left annotation*/
    public Annotation getLeftAnnotation(){
      return leftAnnotation;
    }// getLeftAnnotation

    /** Sets the right annotation*/
    public void setRightAnnotation(Annotation aRightAnnotation){
      rightAnnotation = aRightAnnotation;
    }// setRightAnnot

    /** Gets the right annotation*/
    public Annotation getRightAnnotation(){
      return rightAnnotation;
    }// getRightAnnotation
  }// classs DiffSetElement
} // class TestAnnotationDiff
