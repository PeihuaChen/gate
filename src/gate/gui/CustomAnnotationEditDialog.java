/*  CustomAnnotationEditDialog.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  29/March/2001
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
  * It does this without using an @see gate.creole.AnnotationSchema.
  * The user can manipulate annotation and features at his own will.
  * Is his responsability.Although for annotation that have a schema present
  * into the system, this class will not be
  * used.@see gate.gui.AnnotationEditDialog is highly prioritar in this case.
  */
public class CustomAnnotationEditDialog extends JDialog {

  // Local data
  private final static int OK = 1;
  private final static int CANCEL = 2;

  // Internal data used in initLocalData() method
  private Annotation annot = null;
  private MyCustomFeatureBearer data = null;
  private Set annotationSchemaSet = null;

  int buttonPressed = CANCEL;

  // Gui Components
  JLabel annotTypeLabel = null;
  JTextField annotTypeTextField = null;

  JLabel featuresLabel = null;
  FeaturesEditor  featuresEditor = null;

  JButton okButton = null;
  JButton cancelButton = null;

  /** Constructs a CustomAnnotationEditDialog
    * @param aFram the parent frame of this dialog. It can be <b>null</b>.
    * @param aModal wheter or not this dialog is modal.
    * @param anAnnotationSchemaSet is used to veryfy the type of the annotation
    * being eddited. If the annotation has an AnnotationSchema then this
    * annotation will not be eddited by <b>this</b> object.
    */
  public CustomAnnotationEditDialog(  Frame aFrame,
                                      boolean aModal,
                                      Set anAnnotationSchemaSet){
    super(aFrame,aModal);
    this.setLocationRelativeTo(aFrame);
    this.setTitle("Custom Annotation Editor");
    data = new MyCustomFeatureBearer(null);
    annotationSchemaSet = anAnnotationSchemaSet;
  }//CustomAnnotationEditDialog

  /** Constructs a CustomAnnotationEditDialog.The parent frame is null and the
    * dialog is modal.
    * @param anAnnotationSchemaSet is used to veryfy the type of the annotation
    * being eddited. If the annotation has an AnnotationSchema then this
    * annotation will not be eddited by <b>this</b> object.
    */
  public CustomAnnotationEditDialog(Set anAnnotationSchemaSet){
    this(null, true, anAnnotationSchemaSet);
  }// End CustomAnnotationEditDialog

  /** Init local data*/
  protected void initLocalData(){
    data = new MyCustomFeatureBearer(annot);
  }// initLocalData();

  /** Init GUI components with values taken from local data*/
  protected void initGuiComponents(){
    this.getContentPane().setLayout(new BoxLayout( this.getContentPane(),
                                                   BoxLayout.Y_AXIS));
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

    // Add the Ok and Cancel buttons
    Box cancelOkBox = Box.createHorizontalBox();
    okButton = new JButton("Ok");
    cancelButton = new JButton("Cancel");

    cancelOkBox.add(Box.createHorizontalGlue());
    cancelOkBox.add(okButton);
    cancelOkBox.add(Box.createHorizontalStrut(25));
    cancelOkBox.add(cancelButton);
    cancelOkBox.add(Box.createHorizontalGlue());

    componentsBox.add(cancelOkBox);

    this.getContentPane().add(componentsBox);
    this.getContentPane().add(Box.createVerticalStrut(10));
    this.setSize(350,350);
  }//initGuiComponents()

  /** Init all the listeners*/
  protected void initListeners(){

    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          doOk();
      }// actionPerformed();
    });// addActionListener();

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        requestFocus();
        doCancel();
      }// actionPerformed();
    });// addActionListener();

  }//initListeners()

  /** Returns annot type edited with this tool*/
  public String getAnnotType(){ return data.getAnnotType();}

  /** Returns the features edited with this tool*/
  public FeatureMap getFeatures(){ return data.getFeatures();}

  /** This method is called when the user press the OK button */
  private void doOk(){
    buttonPressed = OK;

    if ("".equals(annotTypeTextField.getText())){
      JOptionPane.showMessageDialog(this,
                                    "You need to provide an annotation type!",
                                    "Gate", JOptionPane.ERROR_MESSAGE);
      return;
    }// End if
    if (annotationSchemaSet != null){
      Iterator iter = annotationSchemaSet.iterator();
      while (iter.hasNext()){
        AnnotationSchema schema = (AnnotationSchema) iter.next();
        if ((schema.getAnnotationName()).equals(annotTypeTextField.getText())){
          JOptionPane.showMessageDialog(this,
                               "Choose another annotation type!\n" +
                               "This type already exists in predefined types.",
                               "Gate", JOptionPane.ERROR_MESSAGE);
          return;
        }// End if
      }// End while
    }// End if
    data.setAnnotType(annotTypeTextField.getText());
    this.hide();
  }//doOk();

  /** This method is called when the user press the CANCEL button*/
  private void doCancel(){
    buttonPressed = CANCEL;
    this.hide();
  }//doCancel();

  /**  This method displays the AnnotationEditDialog in creating mode
    *  If one wants to create a new annotation then show() must be called with
    *  <b>null</b> as a param.
    *  @param anAnnot is the annotation that one wants to edit. If is <b>null</b>
    *  then an annotation will be created.
    *  @return JFileChooser.CANCEL_OPTION or JFileChooser.APPROVE_OPTION
    *  depending on what one choosed.
    */
  public int show(Annotation anAnnot){
    annot = anAnnot;
    initLocalData();
    initGuiComponents();
    initListeners();
    super.show();
    if (buttonPressed == CANCEL){
      doCancel();
      return JFileChooser.CANCEL_OPTION;
    }else{
      doOk();
      return JFileChooser.APPROVE_OPTION;
    }// End if
  }// show()

  // INNER CLASS
  /** This class implements a feature bearer. It is used as internal data.
    * The FeatureEditor will use an object belonging to this class.
    */
  class MyCustomFeatureBearer implements FeatureBearer{

    // Members
    private FeatureMap features = null;
    private String annotType = null;

    /** Constructs a custom feature bearer. If annot is null then it creates
      * empty annotType and fetures.
      */
    public MyCustomFeatureBearer(Annotation anAnnot){
      if (anAnnot != null){
        features = anAnnot.getFeatures();
        annotType = anAnnot.getType();
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

/*
  // Code used in development process
  public static void main(String[] args){

    try {
      Gate.init();
      Document doc = Factory.newDocument(new URL("http://www"));

      CustomAnnotationEditDialog caed = new CustomAnnotationEditDialog();
      //aed.show(annotSchema);
      caed.show(null);

    } catch (Exception e){
      e.printStackTrace(System.err);
    }
  }// main
*/
}//End class CustomAnnotationEditDialog