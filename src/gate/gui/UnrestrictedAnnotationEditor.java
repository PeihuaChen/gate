/*  UnrestrictedAnnotationEditor.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  13/July/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.util.*;
import java.lang.reflect.*;
import java.net.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;
import gate.creole.*;

/** This class visually adds/edits features and annot type of an annotation
  * It does this without using an {@link gate.creole.AnnotationSchema}.
  * The user can manipulate annotation and features at his own will.
  * It's his responsability.
  */
public class UnrestrictedAnnotationEditor extends AbstractVisualResource
                                          implements AnnotationVisualResource{

  /** Default constructor*/
  public UnrestrictedAnnotationEditor() {}

  // Methods required by AnnotationVisualResource

  /**
    * Called by the GUI when this viewer/editor has to initialise itself for a
    * specific annotation or text span.
    * @param target the object which will always be a {@link gate.AnnotationSet}
    */
  public void setTarget(Object target){
    currentAnnotSet = (AnnotationSet) target;
  }// setTarget();

  /**
    * Used when the viewer/editor has to display/edit an existing annotation
    * @param ann the annotation to be displayed or edited. If ann is null then
    * the method simply returns
    */
  public void setAnnotation(Annotation ann){
    // If ann is null, then simply return.
    if (ann == null) return;
    currentAnnot = ann;
    currentStartOffset = currentAnnot.getStartNode().getOffset();
    currentEndOffset = currentAnnot.getEndNode().getOffset();

    initLocalData();
    initGuiComponents();

  }// setAnnotation();

  /**
    * Used when the viewer has to create new annotations.
    * @param startOffset the start offset of the span covered by the new
    * annotation(s). If is <b>null</b> the method will simply return.
    * @param endOffset the end offset of the span covered by the new
    * annotation(s). If is <b>null</b> the method will simply return.
    */
  public void setSpan(Long startOffset, Long endOffset){
    // If one of them is null, then simply return.
    if (startOffset == null || endOffset == null) return;

    currentStartOffset = startOffset;
    currentEndOffset = endOffset;
    currentAnnot = null;

    initLocalData();
    initGuiComponents();
  }// setSpan();

  /**
   * Called by the GUI when the user has pressed the "OK" button. This should
   * trigger the saving of the newly created annotation(s)
   */
  public void okAction() throws GateException {
    if (annotTypeTextField.getText().equals("")){
      throw new GateException("An annotation type must be specified !");
    }// End if
    // This code must be uncomented if the desired behaviour for
    // UnrestrictedAnnoatationEditor is not to allow annotation types
    // which have a schema present in the system.
/*
    CreoleRegister creoleReg = Gate.getCreoleRegister();
    List currentAnnotationSchemaList =
                      creoleReg.getLrInstances("gate.creole.AnnotationSchema");
    Iterator iter = currentAnnotationSchemaList.iterator();
    while (iter.hasNext()){
      AnnotationSchema annotSchema = (AnnotationSchema) iter.next();
      if (annotTypeTextField.getText().equals(annotSchema.getAnnotationName()))
        throw new GAteException("There is a schema type for this annotation");
    }// End while
*/
    data.setAnnotType(annotTypeTextField.getText());
    if (currentAnnot == null){
      currentAnnotSet.add( currentStartOffset,
                           currentEndOffset,
                           this.getAnnotType(),
                           this.getCurrentAnnotationFeatures());
    }else{
      if (currentAnnot.getType().equals(this.getAnnotType())){
        currentAnnot.setFeatures(this.getCurrentAnnotationFeatures());
      }else{
        currentAnnotSet.remove(currentAnnot);
        currentAnnotSet.add( currentStartOffset,
                             currentEndOffset,
                             this.getAnnotType(),
                             this.getCurrentAnnotationFeatures());
      }// End if
    }// End if
  }//okAction();

  /**
    * Checks whether this viewer/editor can handle a specific annotation type.
    * @param annotationType represents the annotation type being questioned.If
    * it is <b>null</b> then the method will return false.
    * @return true if the SchemaAnnotationEditor can handle the annotationType
    * or false otherwise.
    */
  public boolean canDisplayAnnotationType(String annotationType){
    return true;
  }// canDisplayAnnotationType();

  // The Unrestricted Editor functionality
  // Local data

  /** The curent annotation set used by the editor*/
  AnnotationSet currentAnnotSet = null;
  /** The curent annotation used by the editor*/
  Annotation currentAnnot = null;
  /** The start offset of the span covered by the currentAnnot*/
  Long currentStartOffset = null;
  /** The end offset of the span covered by the currentAnnot*/
  Long currentEndOffset = null;

  // Local data
  private MyCustomFeatureBearer data = null;

  // Gui Components
  JLabel annotTypeLabel = null;
  JTextField annotTypeTextField = null;

  JLabel featuresLabel = null;
  FeaturesEditor  featuresEditor = null;

  /** Init local data*/
  protected void initLocalData(){
    data = new MyCustomFeatureBearer(currentAnnot);
  }// initLocalData();

  /** Init GUI components with values taken from local data*/
  protected void initGuiComponents(){
    this.setLayout(new BoxLayout( this, BoxLayout.Y_AXIS));
    //create the main box
    Box componentsBox = Box.createVerticalBox();

    componentsBox.add(Box.createVerticalStrut(10));

    // Add the Annot Type
    Box box = Box.createVerticalBox();
    Box box1 = Box.createHorizontalBox();
    annotTypeLabel = new JLabel("Annotation type");
    annotTypeLabel.setToolTipText("The type of the annotation you are" +
                                                    " creating or editing");
    annotTypeLabel.setOpaque(true);

    box1.add(annotTypeLabel);
    box1.add(Box.createHorizontalGlue());
    box.add(box1);

    annotTypeTextField = new JTextField(data.getAnnotType());
    annotTypeTextField.setColumns(80);
    annotTypeTextField.setPreferredSize(
                                  annotTypeTextField.getPreferredSize());
    annotTypeTextField.setMinimumSize(
                                  annotTypeTextField.getPreferredSize());
    annotTypeTextField.setMaximumSize(
                                  annotTypeTextField.getPreferredSize());


    box1 = Box.createHorizontalBox();
    box1.add(annotTypeTextField);
    box1.add(Box.createHorizontalGlue());
    box.add(box1);
    box.add(Box.createVerticalStrut(10));

    componentsBox.add(box);
    // add the features editor
    box = Box.createVerticalBox();

    featuresLabel = new JLabel("Features");
    featuresLabel.setToolTipText("The features of the annotation you are" +
                                                    " creating or editing");
    featuresLabel.setOpaque(true);

    box1 = Box.createHorizontalBox();
    box1.add(featuresLabel);
    box1.add(Box.createHorizontalGlue());
    box.add(box1);
    box.add(Box.createVerticalStrut(5));

    featuresEditor = new FeaturesEditor();
    featuresEditor.setFeatureBearer(data);

    box.add(featuresEditor);
    box.add(Box.createVerticalStrut(10));

    componentsBox.add(box);
    componentsBox.add(Box.createVerticalStrut(10));

    this.add(componentsBox);
    this.add(Box.createVerticalStrut(10));
  }//initGuiComponents()

  /** Init all the listeners*/
  protected void initListeners(){
  }//initListeners()

  /** Returns annot type edited with this tool*/
  public String getAnnotType(){ return data.getAnnotType();}

  /** Returns the features edited with this tool*/
  protected FeatureMap getCurrentAnnotationFeatures(){ return data.getFeatures();}

  // INNER CLASS
  /** This class implements a feature bearer. It is used as internal data.
    * The FeatureEditor will use an object belonging to this class.
    */
  class MyCustomFeatureBearer extends AbstractFeatureBearer
                                                    implements FeatureBearer{

    // Members
    private FeatureMap features = null;
    private String annotType = null;

    /** Constructs a custom feature bearer. If annot is null then it creates
      * empty annotType and fetures.
      */
    public MyCustomFeatureBearer(Annotation anAnnot){
      if (anAnnot != null){
        features = Factory.newFeatureMap();
        features.putAll(anAnnot.getFeatures());
        annotType = new String(anAnnot.getType());
      }else{
        features = Factory.newFeatureMap();
        annotType = new String("");
      }// End if
    }//MyCustomFeatureBearer

    // Mutators and accesors
    public void setFeatures(FeatureMap aFeatureMap){
      features = aFeatureMap;
    }// setFeatures();

    public FeatureMap getFeatures(){
      return features;
    }// getFeatures()

    public void setAnnotType(String anAnnotType){
      annotType = anAnnotType;
    }// setAnnotType();

    public String getAnnotType(){
      return annotType;
    }//getAnnotType()
  }// End class MyCustomFeatureBearer
}// End class UnrestrictedAnnotationEditor