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

import gate.*;
import gate.annotation.*;
import gate.util.*;
import gate.creole.*;

/** This class visually adds/edits features and annot type of an annotation*/
public class CustomAnnotationEditDialog extends JDialog {

  // Local data
  private final static int OK = 1;
  private final static int CANCEL = 2;

  // internal data

  private Annotation annot = null;
  private MyCustomFeatureBearer data = null;

  int buttonPressed = CANCEL;

  // Gui Components
  JLabel annotTypeLabel = null;
  JTextField annotTypeTextField = null;

  JLabel featuresLabel = null;
  FeaturesEditor  featuresEditor = null;

  JButton okButton = null;
  JButton cancelButton = null;

  /** Constructs a CustomAnnotationEditDialog
    * @param aFram the parent frame of this dialog
    * @param aModal (wheter or not this dialog is modal)
    */
  public CustomAnnotationEditDialog(Frame aFrame, boolean aModal) {

    super(aFrame,aModal);
    this.setLocationRelativeTo(aFrame);

    initLocalData();
    initGuiComponents();
    initListeners();
  }//CustomAnnotationEditDialog

  public CustomAnnotationEditDialog() {
    this(null, true);
  }// End CustomAnnotationEditDialog

  /** Init local data*/
  protected void initLocalData(){
    if (annot != null)
      data = new MyCustomFeatureBearer(annot);
    else
      data = new MyCustomFeatureBearer();
  }// initLocalData();

  /** Init GUI components with values taken from local data*/
  protected void initGuiComponents(){
    this.getContentPane().setLayout(new BoxLayout( this.getContentPane(),
                                                   BoxLayout.Y_AXIS));
    //create the main box
    Box componentsBox = Box.createVerticalBox();

    componentsBox.add(Box.createVerticalStrut(10));

    Box box = Box.createVerticalBox();
    annotTypeLabel = new JLabel("Annotation type");
    box.add(annotTypeLabel);
    box.add(Box.createVerticalStrut(5));
    annotTypeTextField = new JTextField(data.getAnnotType());
    box.add(annotTypeTextField);
    box.add(Box.createVerticalStrut(10));

    componentsBox.add(box);

    // add the features editor
    box = Box.createVerticalBox();
    featuresLabel = new JLabel("Features");
    box.add(featuresLabel);
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

    cancelOkBox.add(okButton);
    cancelOkBox.add(Box.createHorizontalStrut(25));
    cancelOkBox.add(cancelButton);

    componentsBox.add(cancelOkBox);

    this.getContentPane().add(componentsBox);
    this.getContentPane().add(Box.createVerticalStrut(10));

    setSize(500,350);
  }//initGuiComponents()

  /** Init all the listeners*/
  protected void initListeners(){

    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        featuresEditor.stopCellEditing();
        doOk();
      }
    });

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        requestFocus();
        doCancel();
      }
    });

  }//initListeners()

  /** This method is called when the user press the OK button*/
  private void doOk(){
    buttonPressed = OK;
    this.hide();

  }//doOk();

  /** This method is called when the user press the CANCEL button*/
  private void doCancel(){
    buttonPressed = CANCEL;
    this.hide();
  }//doCancel();

  /** This method displays the AnnotationEditDialog in creating mode*/
  public int show(Annotation anAnnot){
    this.setTitle("Custom Annotation Editor");
    initLocalData();
    initGuiComponents();
    super.show();
    if (buttonPressed == CANCEL)
      return JFileChooser.CANCEL_OPTION;
    else{
      return JFileChooser.APPROVE_OPTION;
      // modify the structure of annot if not nulll
      // otherwise export feature map and annot type

    }
  }// show()

  /** This method displays the AnnotationEditDialog in edit mode*/
/*  public int show(){
    return show(null);
  }// show()
*/

  public static void main(String[] args){

    try {
      Gate.init();
      FeatureMap parameters = Factory.newFeatureMap();
      parameters.put("xmlFileUrl", AnnotationEditDialog.class.getResource(
                              "/gate/resources/creole/schema/PosSchema.xml"));

      AnnotationSchema annotSchema = (AnnotationSchema)
         Factory.createResource("gate.creole.AnnotationSchema", parameters);

      FeatureMap fm = Factory.newFeatureMap();
      fm.put("time",new Integer(10));

      fm.put("cat","V");
      fm.put("match", new Vector(3));

      AnnotationEditDialog aed = new AnnotationEditDialog(null,true);
      //aed.show(annotSchema);
      aed.show(fm,annotSchema);

  /*
      // Create an annoatationSchema from a URL.
      URL url =
      annotSchema.fromXSchema(url);
  */
    } catch (Exception e){
      e.printStackTrace(System.err);
    }
  }// main

  class MyCustomFeatureBearer implements FeatureBearer{

    private FeatureMap features = null;
    private String annotType = null;

    public MyCustomFeatureBearer(Annotation anAnnot){
      if (anAnnot != null){
        features = anAnnot.getFeatures();
        annotType = anAnnot.getType();
      }// End if
    }//MyCustomFeatureBearer

    public MyCustomFeatureBearer(){}

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
    }//getAlignmentY()
  }// End MyCustomFeatureBearer
}//CustomAnnotationEditDialog