/*
 *  AnnotationDiff.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 27/Oct/2000
 *
 *  $Id$
 */

package gate.annotation;

import java.util.*;
import java.awt.*;
import java.text.NumberFormat;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import gate.util.*;
import gate.*;
import gate.gui.*;
import gate.creole.*;

/**
  * This class compare two annotation sets on annotation type given by the
  * AnnotationSchema object. It also deals with graphic representation.
  */
public class AnnotationDiff implements VisualResource{

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** This document contains the key annotation set which is taken as reference
   *  in comparison*/
  private Document keyDocument = null;

  /** This document contains the response annotation set which is being
    * compared against the key annotation set.
    */
  private Document responseDocument = null;

  /** The annotation schema object used to get the annotation name
    */
  private AnnotationSchema annotationSchema = null;

  /** The Precision value (see NLP Information Extraction)*/
  private Double precision = null;

  /** The Recall value (see NLP Information Extraction)*/
  private Double recall = null;

  /** A number formater for displaying precision and recall*/
  protected static NumberFormat formatter = NumberFormat.getInstance();

  /** As Required by Resource Interface*/
  private FeatureMap featureMap = null;

  /** The viewer component */
  private JPanel diffPanel = new JPanel();

  /** The components that will stay into diffPanel*/
  private SortedTable diffTable = new SortedTable();

  /** Sets the key Document containing the annotation set taken as refference*/
  public void setKeyDocument(Document aKeyDocument) {
    keyDocument = aKeyDocument;
  }// setKeyDocument

  /** Sets the precision field*/
  public void setPrecision(Double aPrecision){
    precision = aPrecision;
  }// setPrecission

  /** Gets the precision field*/
  public Double getPrecision(){
    return precision;
  }// getPrecision

  /** Sets the Recall field*/
  public void setRecall(Double aRecall){
    recall = aRecall;
  }// setRecall

  /** Gets the recall*/
  public Double getRecall(){
    return recall;
  }// getRecall

  /** Gets the keyDocument */
  public Document getKeyDocument(){
    return keyDocument;
  }// getKeyDocument

  /**
    * Sets the response Document(containing the annotation Set being compared)
    */
  public void setResponseDocument(Document aResponseDocument) {
    responseDocument = aResponseDocument;
  }//setResponseDocument

  /**
    * Sets the annotation type being compared. This type is found in annotation
    * Schema object as parameter.
    */
  public void setAnnotationSchema(AnnotationSchema anAnnotationSchema) {
    annotationSchema = anAnnotationSchema;
  } // setAnnotationType

  /** Returns the annotation schema object */
  public AnnotationSchema getAnnotationSchema(){
    return annotationSchema;
  }// AnnotationSchema

  /** This method is required in VisualResource Interface*/
  public JComponent getGUI(){
    return diffTable;
  }// getViewer

  /**
    * This method does the diff, P&R calculation and so on.
    */
  public Resource init() {
    // do the diff, P&R calculation and so on
    AnnotationSet keyAnnotSet = null;
    AnnotationSet responseAnnotSet = null;
    Set diffSet  = null;
    // Get the key AnnotationSet from the keyDocument
    keyAnnotSet = keyDocument.getAnnotations().get(
                              annotationSchema.getAnnotationName());
    // Get the response AnnotationSet from the resonseDocument
    responseAnnotSet = responseDocument.getAnnotations().get(
                                        annotationSchema.getAnnotationName());
    // Calculate the diff Set. This set will be used later with graphic
    // visualisation.
    diffSet = doDiff(keyAnnotSet, responseAnnotSet);
    if (diffSet != null){
      //Show it
      // Configuring the formatter object. It will be used later to format
      // precision and recall
      formatter.setMaximumIntegerDigits(1);
      formatter.setMinimumFractionDigits(4);
      formatter.setMinimumFractionDigits(4);

      // Create an Annotation diff table model
      AnnotationDiffTableModel diffModel = new AnnotationDiffTableModel();
      // Set data for this table model
      diffModel.setData(diffSet);
      // Set the model for our table
      diffTable.setModel(diffModel);
      // Set the cell renderer.
      diffTable.setDefaultRenderer(java.lang.String.class,
                                  new AnnotationDiffCellRenderer(diffModel));
      // Setting the box layout for diffpanel
      BoxLayout boxLayout = new BoxLayout(diffPanel,BoxLayout.Y_AXIS);
      diffPanel.setLayout(boxLayout);
      // Put the table into a JScrollPanel
      JScrollPane tableScroll = new JScrollPane(diffTable);
      // Add the tableScroll to the diffPanel
      diffPanel.add(tableScroll);

      //Lay out the JLabels from left to right.
      JPanel jLabelPane = new JPanel();
      jLabelPane.setLayout(new BoxLayout(jLabelPane, BoxLayout.X_AXIS));
      // Keep the components together
      jLabelPane.add(Box.createHorizontalGlue());
      JLabel precisionLabel = new JLabel("Precision: " +
                                      formatter.format(precision));
      jLabelPane.add(precisionLabel);
      // This places a space between the two JLabel components
      jLabelPane.add(Box.createRigidArea(new Dimension(20, 0)));
      JLabel recallLabel = new JLabel("Recall: " + formatter.format(recall));
      jLabelPane.add(recallLabel);

      diffPanel.add(jLabelPane);
    }// End if(diffSet != null)
    if (DEBUG)
      printStructure(diffSet);

    return this;
  } // init()

  protected void printStructure(Set aDiffSet){
    Iterator iterator = aDiffSet.iterator();
    String leftAnnot = null;
    String rightAnnot = null;
    while(iterator.hasNext()){
      DiffSetElement diffElem = (DiffSetElement) iterator.next();
      if (diffElem.getLeftAnnotation() == null)
        leftAnnot = "NULL ";
      else
        leftAnnot = diffElem.getLeftAnnotation().toString();
      if (diffElem.getRightAnnotation() == null)
        rightAnnot = " NULL";
      else
        rightAnnot = diffElem.getRightAnnotation().toString();
      Out.prln( leftAnnot + "|" + rightAnnot);
    }// end while
    Out.prln("Precision = " + precision + " , Recall = " + recall);
  }// printStructure

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

  /** This method comes from Resource Interface*/
  public void setFeatures(FeatureMap aFeatureMap){
    featureMap = aFeatureMap;
  }// setFeatures

  /** This method comes from Resource Interface*/
  public FeatureMap getFeatures(){
    return featureMap;
  }// getFeatures

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

  /**
    * This class defines a Cell renderere for the AnnotationDiff table
    */
  public class AnnotationDiffCellRenderer extends DefaultTableCellRenderer{
    /** The model used to analyse data and get decision on how to render it.*/
    AnnotationDiffTableModel diffModel = null;

    /** Constructs a randerer with a table model*/
    public AnnotationDiffCellRenderer(AnnotationDiffTableModel aDiffModel){
      diffModel = aDiffModel;
    }//AnnotationDiffCellRenderer

    /** This method is called by JTable*/
    public Component getTableCellRendererComponent( JTable table,
                                                    Object value,
                                                    boolean isSelected,
                                                    boolean hasFocus,
                                                    int row,
                                                    int column){

      Component defaultComp = super.getTableCellRendererComponent(table,
                                                                  value,
                                                                  isSelected,
                                                                  hasFocus,
                                                                  row,
                                                                  column);
      // Nothing special to render if the model is null.
      if (diffModel == null)
        return defaultComp;

      if (!(diffModel.getRawObject(row) instanceof DiffSetElement))
        return defaultComp;

      // The column number four will be randered using a blank component
      if (column == 4)
        return new JPanel();

      DiffSetElement diffElement = (DiffSetElement) diffModel.getRawObject(row);
      if (diffElement != null){
        if (diffElement.getLeftAnnotation() == null ||
            diffElement.getRightAnnotation()== null ){
          // Background red and foreground white
          defaultComp.setBackground(new Color(255,0,0));
          defaultComp.setForeground(new Color(255,255,255));
        }else{
          // Background green and foreground black
          defaultComp.setBackground(new Color(0,255,0));
          defaultComp.setForeground(new Color(0,0,0));
        }// end if
      }// end if
      return defaultComp;
    }//getTableCellRendererComponent
  }// class AnnotationDiffCellRenderer
} // class AnnotationDiff
