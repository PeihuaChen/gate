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
import gate.annotation.*;
import gate.*;
import gate.gui.*;
import gate.swing.*;
import gate.creole.*;

/**
  * This class compare two annotation sets on annotation type given by the
  * AnnotationSchema object. It also deals with graphic representation of the
  * result.
  */
public class AnnotationDiff extends AbstractVisualResource{

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** This document contains the key annotation set which is taken as reference
   *  in comparison*/
  private Document keyDocument = null;

  /** The name of the annotation set. If is null then the default annotation set
    * will be considered.
    */
  private String keyAnnotationSetName = null;

  /** This document contains the response annotation set which is being
    * compared against the key annotation set.
    */
  private Document responseDocument = null;

  /** The name of the annotation set. If is null then the default annotation set
    * will be considered.
    */
  private String responseAnnotationSetName = null;

  /** The name of the annotation set considered in calculating FalsePozitive.
    * If is null then the default annotation set will be considered.
    */
  private String responseAnnotationSetNameFalsePoz = null;

  /** The annotation schema object used to get the annotation name*/
  private AnnotationSchema annotationSchema = null;

  /** A set of feature names bellonging to annotations from keyAnnotList
    * used in isCompatible() and isPartiallyCompatible() methods
    */
  private Set keyFeatureNamesSet = null;

  /** The precision strict value (see NLP Information Extraction)*/
  private double precisionStrict = 0.0;
  /** The precision lenient value (see NLP Information Extraction)*/
  private double precisionLenient = 0.0;
  /** The precision average value (see NLP Information Extraction)*/
  private double precisionAverage = 0.0;

  /** The Recall strict value (see NLP Information Extraction)*/
  private double recallStrict = 0.0;
  /** The Recall lenient value (see NLP Information Extraction)*/
  private double recallLenient = 0.0;
  /** The Recall average value (see NLP Information Extraction)*/
  private double recallAverage = 0.0;

  /** The False positive strict (see NLP Information Extraction)*/
  private double falsePositiveStrict = 0.0;
  /** The False positive lenient (see NLP Information Extraction)*/
  private double falsePositiveLenient = 0.0;
  /** The False positive average (see NLP Information Extraction)*/
  private double falsePositiveAverage = 0.0;

  /** The F-measure strict (see NLP Information Extraction)*/
  private double fMeasureStrict = 0.0;
  /** The F-measure lenient (see NLP Information Extraction)*/
  private double fMeasureLenient = 0.0;
  /** The F-measure average (see NLP Information Extraction)*/
  private double fMeasureAverage = 0.0;
  /** The weight used in F-measure (see NLP Information Extraction)*/
  public  static double weight = 0.5;

  /**  This string represents the type of annotations used to play the roll of
    *  total number of words needed to calculate the False Positive.
    */
  private String annotationTypeForFalsePositive = null;

  /** A number formater for displaying precision and recall*/
  protected static NumberFormat formatter = NumberFormat.getInstance();

  /** The components that will stay into diffPanel*/
  private XJTable diffTable = null;

  /** Used to represent the result of diff. See DiffSetElement class.*/
  private Set diffSet = null;

  /** This field is used in doDiff() and detectKeyType() methods and holds all
   *  partially correct keys */
  private Set keyPartiallySet = null;
  /** This field is used in doDiff() and detectResponseType() methods*/
  private Set responsePartiallySet = null;

  /** This list is created from keyAnnotationSet at init() time*/
  private java.util.List keyAnnotList = null;
  /** This list is created from responseAnnotationSet at init() time*/
  private java.util.List responseAnnotList = null;

  /**  Field designated to represent the max nr of annot types and coolors for
    *  each type
    **/
  public final int MAX_TYPES = 5;
  /** A default type when all annotation are the same represented by White color*/
  public final int DEFAULT_TYPE = 0;
  /** A correct type when all annotation are corect represented by Green color*/
  public final int CORRECT_TYPE = 1;
  /** A partially correct type when all annotation are corect represented
   *  by Blue color*/
  public final int PARTIALLY_CORRECT_TYPE = 2;
  /** A spurious type when annotations in response were not present in key.
   *  Represented by Red color*/
  public final int SPURIOUS_TYPE = 3;
  /** A missing type when annotations in key were not present in response
   *  Represented by Yellow color*/
  public final int MISSING_TYPE = 4;

  /** Red used for SPURIOUS_TYPE*/
  private  final Color RED = new Color(255,173,181);
  /** Green used for CORRECT_TYPE*/
  private  final Color GREEN = new Color(173,255,214);
  /** White used for DEFAULT_TYPE*/
  private  final Color WHITE = new Color(255,255,255);
  /** Blue used for PARTIALLY_CORRECT_TYPE*/
  private  final Color BLUE = new Color(173,215,255);
  /** Yellow used for MISSING_TYPE*/
  private  final Color YELLOW = new Color(255,231,173);

  /** Used in DiffSetElement to represent an empty raw in the table*/
  public final int NULL_TYPE = -1;
  /** Used in some setForeground() methods*/
  private  final Color BLACK = new Color(0,0,0);
  /** The array holding the colours according to the annotation types*/
  private Color colors[] = new Color[MAX_TYPES];

  /** A scroll for the AnnotDiff's table*/
  private JScrollPane scrollPane = null;

  /** Used to store the no. of annotations from response,identified as belonging
    * to one of the previous types.
    */
  private int typeCounter[] = new int[MAX_TYPES];

  /** Constructs a AnnotationDif*/
  public AnnotationDiff(){
  } //AnnotationDiff

  /** Sets the annotation type needed to calculate the falsePossitive measure
    * @param anAnnotType is the annotation type needed to calculate a special
    *  mesure called falsePossitive. Usualy the value is "token", but it can be
    *  any other string with the same semantic.
    */
  public void setAnnotationTypeForFalsePositive(String anAnnotType){
    annotationTypeForFalsePositive = anAnnotType;
  } // setAnnotationTypeForFalsePositive

  /** Gets the annotation type needed to calculate the falsePossitive measure
    * @return annotation type needed to calculate a special
    * mesure called falsePossitive.
    */
  public String getAnnotationTypeForFalsePositive(){
    return annotationTypeForFalsePositive;
  } // getAnnotationTypeForFalsePositive

  /** Sets the keyDocument in AnnotDiff
    * @param aKeyDocument The GATE document used as a key in annotation diff.
    */
  public void setKeyDocument(Document aKeyDocument) {
    keyDocument = aKeyDocument;
  } // setKeyDocument

  /** @return the keyDocument used in AnnotDiff process */
  public Document getKeyDocument(){
    return keyDocument;
  } // getKeyDocument

  /** Sets the keyAnnotationSetName in AnnotDiff
    * @param aKeyAnnotationSetName The name of the annotation set from the
    * keyDocument.If aKeyAnnotationSetName is null then the default annotation
    * set will be used.
    */
  public void setKeyAnnotationSetName(String aKeyAnnotationSetName){
    keyAnnotationSetName = aKeyAnnotationSetName;
  } // setKeyAnnotationSetName();

  /** Gets the keyAnnotationSetName.
    * @return The name of the keyAnnotationSet used in AnnotationDiff. If
    * returns null then the the default annotation set will be used.
    */
  public String getKeyAnnotationSetName(){
    return keyAnnotationSetName;
  } // getKeyAnnotationSetName()

  /** Sets the keyFeatureNamesSet in AnnotDiff.
    * @param aKeyFeatureNamesSet a set containing the feature names from key
    * that will be used in isPartiallyCompatible()
    */
  public void setKeyFeatureNamesSet(Set aKeyFeatureNamesSet){
    keyFeatureNamesSet = aKeyFeatureNamesSet;
  }//setKeyFeatureNamesSet();

  /** Gets the keyFeatureNamesSet in AnnotDiff.
    * @return A set containing the feature names from key
    * that will be used in isPartiallyCompatible()
    */
  public Set getKeyFeatureNamesSet(){
    return keyFeatureNamesSet;
  }//getKeyFeatureNamesSet();

  /** Sets the responseAnnotationSetName in AnnotDiff
    * @param aResponseAnnotationSetName The name of the annotation set from the
    * responseDocument.If aResponseAnnotationSetName is null then the default
    * annotation set will be used.
    */
  public void setResponseAnnotationSetName(String aResponseAnnotationSetName){
    responseAnnotationSetName = aResponseAnnotationSetName;
  } // setResponseAnnotationSetName();

  /** gets the responseAnnotationSetName.
    * @return The name of the responseAnnotationSet used in AnnotationDiff. If
    * returns null then the the default annotation set will be used.
    */
  public String getResponseAnnotationSetName(){
    return responseAnnotationSetName;
  } // getResponseAnnotationSetName()

  /** Sets the responseAnnotationSetNameFalsePoz in AnnotDiff
    * @param aResponseAnnotationSetNameFalsePoz The name of the annotation set
    * from the responseDocument.If aResponseAnnotationSetName is null
    * then the default annotation set will be used.
    */
  public void setResponseAnnotationSetNameFalsePoz(
                                    String aResponseAnnotationSetNameFalsePoz){
    responseAnnotationSetNameFalsePoz = aResponseAnnotationSetNameFalsePoz;
  } // setResponseAnnotationSetNameFalsePoz();

  /** gets the responseAnnotationSetNameFalsePoz.
    * @return The name of the responseAnnotationSetFalsePoz used in
    * AnnotationDiff. If returns null then the the default annotation
    * set will be used.
    */
  public String getResponseAnnotationSetNameFalsePoz(){
    return responseAnnotationSetNameFalsePoz;
  } // getResponseAnnotationSetNamefalsePoz()


    //Prameters utility methods
  /**
   * Gets the value of a parameter of this resource.
   * @param paramaterName the name of the parameter
   * @return the current value of the parameter
   */
  public Object getParameterValue(String paramaterName)
                throws ResourceInstantiationException{
    return AbstractResource.getParameterValue(this, paramaterName);
  }

  /**
   * Sets the value for a specified parameter.
   *
   * @param paramaterName the name for the parameteer
   * @param parameterValue the value the parameter will receive
   */
  public void setParameterValue(String paramaterName, Object parameterValue)
              throws ResourceInstantiationException{
    AbstractResource.setParameterValue(this, paramaterName, parameterValue);
  }

  /**
   * Sets the values for more parameters in one step.
   *
   * @param parameters a feature map that has paramete names as keys and
   * parameter values as values.
   */
  public void setParameterValues(FeatureMap parameters)
              throws ResourceInstantiationException{
    AbstractResource.setParameterValues(this, parameters);
  }



  ///////////////////////////////////////////////////
  // PRECISION methods
  ///////////////////////////////////////////////////

  /** @return the precisionStrict field*/
  public double getPrecisionStrict(){
    return precisionStrict;
  } // getPrecisionStrict

  /** @return the precisionLenient field*/
  public double getPrecisionLenient(){
    return precisionLenient;
  } // getPrecisionLenient

  /** @return the precisionAverage field*/
  public double getPrecisionAverage(){
    return precisionAverage;
  } // getPrecisionAverage

  /** @return the fMeasureStrict field*/
  public double getFMeasureStrict(){
    return fMeasureStrict;
  } // getFMeasureStrict

  /** @return the fMeasureLenient field*/
  public double getFMeasureLenient(){
    return fMeasureLenient;
  } // getFMeasureLenient

  /** @return the fMeasureAverage field*/
  public double getFMeasureAverage(){
    return fMeasureAverage;
  } // getFMeasureAverage

  ///////////////////////////////////////////////////
  // RECALL methods
  ///////////////////////////////////////////////////

  /** @return the recallStrict field*/
  public double getRecallStrict(){
    return recallStrict;
  } // getRecallStrict

  /** @return the recallLenient field*/
  public double getRecallLenient(){
    return recallLenient;
  } // getRecallLenient

  /** @return the recallAverage field*/
  public double getRecallAverage(){
    return recallAverage;
  } // getRecallAverage

  ///////////////////////////////////////////////////
  // FALSE POSITIVE methods
  ///////////////////////////////////////////////////

  /** @return the falsePositiveStrict field*/
  public double getFalsePositiveStrict(){
    return falsePositiveStrict;
  } // getFalsePositiveStrict

  /** @return the falsePositiveLenient field*/
  public double getFalsePositiveLenient(){
    return falsePositiveLenient;
  } // getFalsePositiveLenient

  /** @return the falsePositiveAverage field*/
  public double getFalsePositiveAverage(){
    return falsePositiveAverage;
  } // getFalsePositive

  /**
    * @param aResponseDocument the GATE response Document
    * containing the annotation Set being compared against the annotation from
    * the keyDocument.
    */
  public void setResponseDocument(Document aResponseDocument) {
    responseDocument = aResponseDocument;
  } //setResponseDocument

  /**
    * @param anAnnotationSchema the annotation type being compared.
    * This type is found in annotationSchema object as field
    * {@link gate.creole.AnnotationSchema#getAnnotationName()}. If is <b>null<b>
    * then AnnotDiff will throw an exception when it comes to do the diff.
    */
  public void setAnnotationSchema(AnnotationSchema anAnnotationSchema) {
    annotationSchema = anAnnotationSchema;
  } // setAnnotationType

  /** @return the annotation schema object used in annotation diff process */
  public AnnotationSchema getAnnotationSchema(){
    return annotationSchema;
  } // AnnotationSchema

  /**
    * This method does the diff, Precision,Recall,FalsePositive
    * calculation and so on.
    */
  public Resource init() throws ResourceInstantiationException {
    colors[DEFAULT_TYPE] = WHITE;
    colors[CORRECT_TYPE] = GREEN;
    colors[SPURIOUS_TYPE] = RED;
    colors[PARTIALLY_CORRECT_TYPE] = BLUE;
    colors[MISSING_TYPE] = YELLOW;

    // Initialize the partially sets...
    keyPartiallySet = new HashSet();
    responsePartiallySet = new HashSet();

    // Do the diff, P&R calculation and so on
    AnnotationSet keyAnnotSet = null;
    AnnotationSet responseAnnotSet = null;

    if (annotationSchema == null)
     throw new ResourceInstantiationException("No annotation schema defined !");

    if (keyAnnotationSetName == null)
      // Get the default key AnnotationSet from the keyDocument
      keyAnnotSet = keyDocument.getAnnotations().get(
                              annotationSchema.getAnnotationName());
    else
      keyAnnotSet = keyDocument.getAnnotations(keyAnnotationSetName).
                                    get(annotationSchema.getAnnotationName());

    if (keyAnnotSet == null){
      throw new ResourceInstantiationException("No <" +
                              annotationSchema.getAnnotationName() +
                              "> annotations found in the KeyDocument");

    } //if (keyAnnotSet == null)

    // The alghoritm will modify this annotation set. It is better to make a
    // separate copy of them.
    keyAnnotList = new LinkedList(keyAnnotSet);

    if (responseAnnotationSetName == null)
      // Get the response AnnotationSet from the default set
      responseAnnotSet = responseDocument.getAnnotations().get(
                                          annotationSchema.getAnnotationName());
    else
      responseAnnotSet = responseDocument.getAnnotations(responseAnnotationSetName).
                                    get(annotationSchema.getAnnotationName());

    if (responseAnnotSet == null){
      throw new ResourceInstantiationException("No <" +
                              annotationSchema.getAnnotationName() +
                              "> annotations found in the ResponseDocument");

    } //if (responseAnnotSet == null)

    // The same thing applies here.
    responseAnnotList = new LinkedList(responseAnnotSet);

    // Sort them ascending on Start offset (the comparator does that)
    AnnotationSetComparator asComparator = new AnnotationSetComparator();
    Collections.sort(keyAnnotList, asComparator);
    Collections.sort(responseAnnotList, asComparator);

    for (int type=0; type < MAX_TYPES; type++)
      typeCounter[type] = 0;

    // Calculate the diff Set. This set will be used later with graphic
    // visualisation.
    doDiff(keyAnnotList, responseAnnotList);

    //Show it
    // Configuring the formatter object. It will be used later to format
    // precision and recall
    formatter.setMaximumIntegerDigits(1);
    formatter.setMinimumFractionDigits(4);
    formatter.setMinimumFractionDigits(4);

    // Create an Annotation diff table model
    AnnotationDiffTableModel diffModel = new AnnotationDiffTableModel(diffSet);
    // Create a XJTable based on this model
    diffTable = new XJTable(diffModel);
    diffTable.setAlignmentX(Component.LEFT_ALIGNMENT);
    // Set the cell renderer for this table.
    AnnotationDiffCellRenderer cellRenderer = new AnnotationDiffCellRenderer();
    diffTable.setDefaultRenderer(java.lang.String.class,cellRenderer);
    diffTable.setDefaultRenderer(java.lang.Long.class,cellRenderer);
    // Put the table into a JScroll
//    scrollPane = new JScrollPane(diffTable);
//    scrollPane.getViewport().
//                    putClientProperty("EnableWindowBlit", new Boolean(true));

    // Arange all components on a this JPanel
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        arangeAllComponents();
      }
    });

    if (DEBUG)
      printStructure(diffSet);

    return this;
  } //init()

  /** This method creates the graphic components and aranges them on
    * <b>this</b> JPanel
    */
  protected void arangeAllComponents(){
    this.removeAll();
    // Setting the box layout for diffpanel
    BoxLayout boxLayout = new BoxLayout(this,BoxLayout.Y_AXIS);
    this.setLayout(boxLayout);

    JTableHeader tableHeader = diffTable.getTableHeader();
    tableHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
    this.add(tableHeader);
    diffTable.setAlignmentX(Component.LEFT_ALIGNMENT);
    // Add the tableScroll to the diffPanel
    this.add(diffTable);

    // ADD the LEGEND
    //Lay out the JLabels from left to right.
    //Box infoBox = new Box(BoxLayout.X_AXIS);
    JPanel infoBox = new  JPanel();
    infoBox.setLayout(new BoxLayout(infoBox,BoxLayout.X_AXIS));
    infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    // Keep the components together
    //box.add(Box.createHorizontalGlue());

    Box box = new Box(BoxLayout.Y_AXIS);
    JLabel jLabel = new JLabel("LEGEND");
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    jLabel.setOpaque(true);
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(jLabel);

    jLabel = new JLabel("Missing (present in Key but not in Response):  " +
                                                typeCounter[MISSING_TYPE]);
    jLabel.setForeground(BLACK);
    jLabel.setBackground(colors[MISSING_TYPE]);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    jLabel.setOpaque(true);
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(jLabel);

    // Add a space
    box.add(Box.createRigidArea(new Dimension(0,5)));

    jLabel = new JLabel("Correct (total match):  " + typeCounter[CORRECT_TYPE]);
    jLabel.setForeground(BLACK);
    jLabel.setBackground(colors[CORRECT_TYPE]);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    jLabel.setOpaque(true);
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(jLabel);

    // Add a space
    box.add(Box.createRigidArea(new Dimension(0,5)));

    jLabel =new JLabel("Partially correct (overlap in Key and Response):  "+
                                        typeCounter[PARTIALLY_CORRECT_TYPE]);
    jLabel.setForeground(BLACK);
    jLabel.setBackground(colors[PARTIALLY_CORRECT_TYPE]);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    jLabel.setOpaque(true);
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(jLabel);

    // Add a space
    box.add(Box.createRigidArea(new Dimension(0,5)));

    jLabel = new JLabel("Spurious (present in Response but not in Key):  " +
                                        typeCounter[SPURIOUS_TYPE]);
    jLabel.setForeground(BLACK);
    jLabel.setBackground(colors[SPURIOUS_TYPE]);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    jLabel.setOpaque(true);
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(jLabel);

    infoBox.add(box);
    // Add a space
    infoBox.add(Box.createRigidArea(new Dimension(40,0)));

    // Precision measure
    //Lay out the JLabels from left to right.
    box = new Box(BoxLayout.Y_AXIS);

    jLabel = new JLabel("Precision strict: " +
                                    formatter.format(precisionStrict));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("Precision average: " +
                                    formatter.format(precisionAverage));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("Precision lenient: " +
                                    formatter.format(precisionLenient));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    infoBox.add(box);
    // Add a space
    infoBox.add(Box.createRigidArea(new Dimension(40,0)));

    // RECALL measure
    //Lay out the JLabels from left to right.
    box = new Box(BoxLayout.Y_AXIS);

    jLabel = new JLabel("Recall strict: " + formatter.format(recallStrict));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("Recall average: " + formatter.format(recallAverage));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("Recall lenient: " + formatter.format(recallLenient));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    infoBox.add(box);
    // Add a space
    infoBox.add(Box.createRigidArea(new Dimension(40,0)));

    // F-Measure
    //Lay out the JLabels from left to right.
    box = new Box(BoxLayout.Y_AXIS);

    jLabel = new JLabel("F-Measure strict: " +
                                        formatter.format(fMeasureStrict));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("F-Measure average: " +
                                        formatter.format(fMeasureAverage));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("F-Measure lenient: " +
                                        formatter.format(fMeasureLenient));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);
    infoBox.add(box);

    // Add a space
    infoBox.add(Box.createRigidArea(new Dimension(40,0)));

    // FALSE POZITIVE measure
    //Lay out the JLabels from left to right.
    box = new Box(BoxLayout.Y_AXIS);

    jLabel = new JLabel("False positive strict: " +
                                        formatter.format(falsePositiveStrict));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("False positive average: " +
                                        formatter.format(falsePositiveAverage));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("False positive lenient: " +
                                        formatter.format(falsePositiveLenient));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);
    infoBox.add(box);

    // Add a space
    infoBox.add(Box.createRigidArea(new Dimension(10,0)));

    this.add(infoBox);
  } //arangeAllComponents

  /** Used internally for debugging */
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
    } // end while
  } // printStructure

  /** This method is the brain of the AnnotationSet diff and creates a set with
    * diffSetElement objects.
    * @param aKeyAnnotList a list containing the annotations from key. If this
    * param is <b>null</b> then the method will simply return and will not do a
    * thing.
    * @param aResponseAnnotList a list containing the annotation from response.
    * If this param is <b>null</b> the method will return.
    */
  protected void doDiff(java.util.List aKeyAnnotList,
                        java.util.List aResponseAnnotList){

    // If one of the annotation sets is null then is no point in doing the diff.
    if (aKeyAnnotList == null || aResponseAnnotList == null)
      return;

    int responseSize = aResponseAnnotList.size();

    diffSet = new HashSet();
    // Iterate throught all elements from keyList and find those in the response
    // list which satisfies isCompatible() and isPartiallyCompatible() relations
    Iterator keyIterator = aKeyAnnotList.iterator();
    while(keyIterator.hasNext()){
      Annotation keyAnnot = (Annotation) keyIterator.next();
      Iterator responseIterator = aResponseAnnotList.iterator();

      DiffSetElement diffElement = null;
      while(responseIterator.hasNext()){
        Annotation responseAnnot = (Annotation) responseIterator.next();

        if(keyAnnot.isPartiallyCompatible(responseAnnot,keyFeatureNamesSet)){
          keyPartiallySet.add(keyAnnot);
          responsePartiallySet.add(responseAnnot);
          if (keyAnnot.coextensive(responseAnnot)){
            // Found two compatible annotations
            // Create a new DiffSetElement and add it to the diffSet
            diffElement = new DiffSetElement( keyAnnot,
                                              responseAnnot,
                                              DEFAULT_TYPE,
                                              CORRECT_TYPE);

            // Add this element to the DiffSet
            addToDiffset(diffElement);
          } // End if (keyAnnot.coextensive(responseAnnot))
        }else if (keyAnnot.coextensive(responseAnnot)){
          // Found two aligned annotations. We have to find out if the response
          // is partialy compatible with another key annotation.
          // Create a new DiffSetElement and add it to the diffSet
          diffElement = new DiffSetElement( keyAnnot,
                                            responseAnnot,
                                            detectKeyType(keyAnnot),
                                            detectResponseType(responseAnnot));
          // Add this element to the DiffSet
          addToDiffset(diffElement);
        } // End if (keyAnnot.coextensive(responseAnnot)){

        if (diffElement != null){
          // Eliminate the response annotation from the list.
          responseIterator.remove();
          break;
        } // End if
      } // end while responseIterator

      // If diffElement != null it means that break was used
      if (diffElement == null){
        if (keyPartiallySet.contains(keyAnnot))
          diffElement = new DiffSetElement( keyAnnot,
                                            null,
                                            DEFAULT_TYPE,
                                            NULL_TYPE);
        else{
          // If keyAnnot is not in keyPartiallySet then it has to be checked
          // agains all annotations in DiffSet to see if there is
          // a previous annotation from response set which is partially
          // compatible with the keyAnnot
          Iterator respParIter = diffSet.iterator();
          while (respParIter.hasNext()){
            DiffSetElement diffElem = (DiffSetElement) respParIter.next();
            Annotation respAnnot = diffElem.getRightAnnotation();
            if (respAnnot != null && keyAnnot.isPartiallyCompatible(respAnnot,
                                                          keyFeatureNamesSet)){
                diffElement = new DiffSetElement( keyAnnot,
                                                  null,
                                                  DEFAULT_TYPE,
                                                  NULL_TYPE);
                break;
            } // End if
          } // End while
          // If is still nul then it means that the key annotation is missing
          if (diffElement == null)
            diffElement = new DiffSetElement( keyAnnot,
                                              null,
                                              MISSING_TYPE,
                                              NULL_TYPE);
        } // End if
        addToDiffset(diffElement);
      } // End if

      keyIterator.remove();
    } // end while keyIterator

    DiffSetElement diffElem = null;
    Iterator responseIter = aResponseAnnotList.iterator();
    while (responseIter.hasNext()){
      Annotation respAnnot = (Annotation) responseIter.next();
      if (responsePartiallySet.contains(respAnnot))
        diffElem = new DiffSetElement( null,
                                       respAnnot,
                                       NULL_TYPE,
                                       PARTIALLY_CORRECT_TYPE);
      else
        diffElem = new DiffSetElement( null,
                                       respAnnot,
                                       NULL_TYPE,
                                       SPURIOUS_TYPE);
      addToDiffset(diffElem);
      responseIter.remove();
    } // End while

    // CALCULATE ALL (NLP) MEASURES like:
    // Precistion, Recall, FalsePositive and F-Measure
    int possible =  typeCounter[CORRECT_TYPE] +  // this comes from Key or Resp
                    typeCounter[PARTIALLY_CORRECT_TYPE] + // this comes from Resp
                    typeCounter[MISSING_TYPE]; // this comes from Key

    int actual =  typeCounter[CORRECT_TYPE] +  // this comes from Key or Resp
                  typeCounter[PARTIALLY_CORRECT_TYPE] + // this comes from Resp
                  typeCounter[SPURIOUS_TYPE]; // this comes from Resp

    if (actual != responseSize)
      Err.prln("AnnotDiff warning: The response size(" + responseSize +
      ") is not the same as the computed value of" +
    " actual(Correct[resp or key]+Partial[resp]+Spurious[resp]=" + actual +")");

    if (actual != 0){
      precisionStrict =  ((double)typeCounter[CORRECT_TYPE])/((double)actual);
      precisionLenient = ((double)(typeCounter[CORRECT_TYPE] +
                         typeCounter[PARTIALLY_CORRECT_TYPE]))/((double)actual);
      precisionAverage = ((double)(precisionStrict + precisionLenient)) /
                                                                  ((double) 2);
    } // End if
    if (possible != 0){
      recallStrict = ((double)typeCounter[CORRECT_TYPE])/((double)possible);
      recallLenient = ((double)(typeCounter[CORRECT_TYPE] +
                       typeCounter[PARTIALLY_CORRECT_TYPE]))/((double)possible);
      recallAverage = ((double)(recallStrict + recallLenient)) / ((double)2);
    } // End if


    int no = 0;
    // If an annotation type for false poz was selected calculate the number of
    // Annotations
    if (annotationTypeForFalsePositive != null)
     // Was it the default set ?
     if (responseAnnotationSetNameFalsePoz == null)
          no = responseDocument.getAnnotations().get(
                                      annotationTypeForFalsePositive).size();
     else
      no = responseDocument.getAnnotations(responseAnnotationSetNameFalsePoz).get(
                                        annotationTypeForFalsePositive).size();
    if (no != 0){
      // No error here: the formula is the opposite to recall or precission
     falsePositiveStrict = ((double)(typeCounter[SPURIOUS_TYPE] +
                             typeCounter[PARTIALLY_CORRECT_TYPE])) /((double)no);
     falsePositiveLenient = ((double)typeCounter[SPURIOUS_TYPE]) /((double) no);
     falsePositiveAverage = ((double)(falsePositiveStrict +
                                           falsePositiveLenient))/((double)2) ;
    } // End if

    // Calculate F-Measure Strict
    double denominator = weight * (precisionStrict + recallStrict);
    if (denominator != 0)
      fMeasureStrict = (precisionStrict * recallStrict) / denominator ;
    else fMeasureStrict = 0.0;
    // Calculate F-Measure Lenient
    denominator = weight * (precisionLenient + recallLenient);
    if (denominator != 0)
      fMeasureLenient = (precisionLenient * recallLenient) / denominator ;
    else fMeasureLenient = 0.0;
    // Calculate F-Measure Average
    fMeasureAverage = (fMeasureStrict + fMeasureLenient) / (double)2;

  } // doDiff

  /** Decide what type is the keyAnnotation (DEFAULT_TYPE, MISSING or NULL_TYPE)
   *  This method must be applied only on annotation from key set.
   *  @param anAnnot is an annotation from the key set.
   *  @return three possible value(DEFAULT_TYPE, MISSING or NULL_TYPE)
   */
  private int detectKeyType(Annotation anAnnot){
    if (anAnnot == null) return NULL_TYPE;

    if (keyPartiallySet.contains(anAnnot)) return DEFAULT_TYPE;
    Iterator iter = responsePartiallySet.iterator();
    while(iter.hasNext()){
      Annotation a = (Annotation) iter.next();
      if (anAnnot.isPartiallyCompatible(a,keyFeatureNamesSet))
        return DEFAULT_TYPE;
    } // End while

    iter = responseAnnotList.iterator();
    while(iter.hasNext()){
      Annotation a = (Annotation) iter.next();
      if (anAnnot.isPartiallyCompatible(a,keyFeatureNamesSet)){
         responsePartiallySet.add(a);
         keyPartiallySet.add(anAnnot);
         return DEFAULT_TYPE;
      } // End if
    } // End while
    return MISSING_TYPE;
  } //detectKeyType

  /**  Decide what type is the responseAnnotation
    *  (PARTIALLY_CORRECT_TYPE, SPURIOUS or NULL_TYPE)
    *  This method must be applied only on annotation from response set.
    *  @param anAnnot is an annotation from the key set.
    *  @return three possible value(PARTIALLY_CORRECT_TYPE, SPURIOUS or NULL_TYPE)
    */
  private int detectResponseType(Annotation anAnnot){
    if (anAnnot == null) return NULL_TYPE;

    if (responsePartiallySet.contains(anAnnot)) return PARTIALLY_CORRECT_TYPE;
    Iterator iter = keyPartiallySet.iterator();
    while(iter.hasNext()){
      Annotation a = (Annotation) iter.next();
      if (a.isPartiallyCompatible(anAnnot,keyFeatureNamesSet))
        return PARTIALLY_CORRECT_TYPE;
    } // End while

    iter = keyAnnotList.iterator();
    while(iter.hasNext()){
      Annotation a = (Annotation) iter.next();
      if (a.isPartiallyCompatible(anAnnot,keyFeatureNamesSet)){
         responsePartiallySet.add(anAnnot);
         keyPartiallySet.add(a);
         return PARTIALLY_CORRECT_TYPE;
      } // End if
    } // End while
    return SPURIOUS_TYPE;
  } //detectResponseType

  /** This method add an DiffsetElement to the DiffSet and also counts the
    * number of compatible, partialCompatible, Incorect and Missing annotations.
    */
  private void addToDiffset(DiffSetElement aDiffSetElement){
    if (aDiffSetElement == null) return;

    diffSet.add(aDiffSetElement);
    // For the Right side (response) the type can be one of the following:
    // PC, I, C
    if (NULL_TYPE != aDiffSetElement.getRightType())
      typeCounter[aDiffSetElement.getRightType()]++;
    // For the left side (key) the type can be : D or M
    if (NULL_TYPE != aDiffSetElement.getLeftType() &&
        CORRECT_TYPE != aDiffSetElement.getLeftType())
      typeCounter[aDiffSetElement.getLeftType()]++;
  } // addToDiffset

  /* ********************************************************************
   * INNER CLASS
   * ********************************************************************/

  /**
    * A custom table model used to render a table containing the two annotation
    * sets. The columns will be:
    * (KEY) Type, Start, End, Features, empty column,(Response) Type,Start, End, Features
    */
  protected class AnnotationDiffTableModel extends AbstractTableModel{

    /** Constructs an AnnotationDiffTableModel given a data Collection */
    public AnnotationDiffTableModel(Collection data){
      modelData = new ArrayList();
      modelData.addAll(data);
    } // AnnotationDiffTableModel

    /** Constructs an AnnotationDiffTableModel */
    public AnnotationDiffTableModel(){
      modelData = new ArrayList();
    } // AnnotationDiffTableModel

    /** Return the size of data.*/
    public int getRowCount(){
      return modelData.size();
    } //getRowCount

    /** Return the number of columns.*/
    public int getColumnCount(){
      return 9;
    } //getColumnCount

    /** Returns the name of each column in the model*/
    public String getColumnName(int column){
      switch(column){
        case 0: return "Type - Key";
        case 1: return "Start - Key";
        case 2: return "End - Key";
        case 3: return "Features - Key";
        case 4: return "   ";
        case 5: return "Type - Response";
        case 6: return "Start - Response";
        case 7: return "End -Response";
        case 8: return "Features - Response";
        default:return "?";
      }
    } //getColumnName

    /** Return the class type for each column. */
    public Class getColumnClass(int column){
      switch(column){
        case 0: return String.class;
        case 1: return Long.class;
        case 2: return Long.class;
        case 3: return String.class;
        case 4: return String.class;
        case 5: return String.class;
        case 6: return Long.class;
        case 7: return Long.class;
        case 8: return String.class;
        default:return Object.class;
      }
    } //getColumnClass

    /**Returns a value from the table model */
    public Object getValueAt(int row, int column){
      DiffSetElement diffSetElement = (DiffSetElement) modelData.get(row);
      if (diffSetElement == null) return null;
      switch(column){
        // Left Side (Key)
        //Type - Key
        case 0:{
           if (diffSetElement.getLeftAnnotation() == null) return null;
           return diffSetElement.getLeftAnnotation().getType();
        }
        // Start - Key
        case 1:{
           if (diffSetElement.getLeftAnnotation() == null) return null;
           return diffSetElement.getLeftAnnotation().getStartNode().getOffset();
        }
        // End - Key
        case 2:{
           if (diffSetElement.getLeftAnnotation() == null) return null;
           return diffSetElement.getLeftAnnotation().getEndNode().getOffset();
        }
        // Features - Key
        case 3:{
           if (diffSetElement.getLeftAnnotation() == null) return null;
           if (diffSetElement.getLeftAnnotation().getFeatures() == null)
             return null;
           return diffSetElement.getLeftAnnotation().getFeatures().toString();
        }
        // Empty column
        case 4:{
          return "   ";
        }
        // Right Side (Response)
        //Type - Response
        case 5:{
           if (diffSetElement.getRightAnnotation() == null) return null;
           return diffSetElement.getRightAnnotation().getType();
        }
        // Start - Response
        case 6:{
           if (diffSetElement.getRightAnnotation() == null) return null;
          return diffSetElement.getRightAnnotation().getStartNode().getOffset();
        }
        // End - Response
        case 7:{
           if (diffSetElement.getRightAnnotation() == null) return null;
           return diffSetElement.getRightAnnotation().getEndNode().getOffset();
        }
        // Features - resonse
        case 8:{
           if (diffSetElement.getRightAnnotation() == null) return null;
           return diffSetElement.getRightAnnotation().getFeatures().toString();
        }
        // The hidden column
        case 9:{
          return diffSetElement;
        }
        default:{return null;}
      } // End switch
    } //getValueAt

    public Object getRawObject(int row){
      return modelData.get(row);
    } //getRawObject

    /** Holds the data for TableDiff*/
    private java.util.List modelData = null;

  } //Inner class AnnotationDiffTableModel

  /* ********************************************************************
   * INNER CLASS
   * ********************************************************************/
  /**
    * This class defines a Cell renderer for the AnnotationDiff table
    */
  public class AnnotationDiffCellRenderer extends DefaultTableCellRenderer{

    /** Constructs a randerer with a table model*/
    public AnnotationDiffCellRenderer() { }  //AnnotationDiffCellRenderer

    private Color background = WHITE;

    private Color foreground = BLACK;

    /** This method is called by JTable*/

    public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column
    ) {
      JComponent defaultComp = null;
      defaultComp = (JComponent) super.getTableCellRendererComponent(
  table, value, isSelected, hasFocus, row, column
      );

      // The column number four will be randered using a blank component
      if (column == 4 || value == null)
        return new JPanel();

      if (!(table.getModel().getValueAt(row,9) instanceof DiffSetElement))
        return defaultComp;

      DiffSetElement diffSetElement =
                        (DiffSetElement) table.getModel().getValueAt(row,9);

      if (diffSetElement == null)
        return defaultComp;

      if (column < 4){
        if (NULL_TYPE != diffSetElement.getLeftType())
          background = colors[diffSetElement.getLeftType()];
        else return new JPanel();
      }else{
        if (NULL_TYPE != diffSetElement.getRightType())
          background = colors[diffSetElement.getRightType()];
        else return new JPanel();
      }

      defaultComp.setBackground(background);
      defaultComp.setForeground(BLACK);

      defaultComp.setOpaque(true);
      return defaultComp;
    } //getTableCellRendererComponent

  } // class AnnotationDiffCellRenderer

  /* ********************************************************************
   * INNER CLASS
   * ********************************************************************/
   class AnnotationSetComparator implements java.util.Comparator {

      public AnnotationSetComparator(){}

      public int compare(Object o1, Object o2) {
        if ( !(o1 instanceof gate.Annotation) ||
             !(o2 instanceof gate.Annotation)) return 0;

        gate.Annotation a1 = (gate.Annotation) o1;
        gate.Annotation a2 = (gate.Annotation) o2;

        Long l1 = a1.getStartNode().getOffset();
        Long l2 = a2.getStartNode().getOffset();
        if (l1 != null)
          return l1.compareTo(l2);
        else
          return -1;
      } //compare
    } // class AnnotationSetComparator

  /* ********************************************************************
   * INNER CLASS
   * ********************************************************************/

  /**
    * This class is used for internal purposes. It represents a row from the
    * table.
    */
  protected class DiffSetElement {

    private Annotation leftAnnotation = null;
    private Annotation rightAnnotation = null;
    private int leftType = DEFAULT_TYPE;
    private int rightType = DEFAULT_TYPE;

    /** Constructor for DiffSetlement*/
    public DiffSetElement() { }

    /** Constructor for DiffSetlement*/
    public DiffSetElement( Annotation aLeftAnnotation,
                           Annotation aRightAnnotation,
                           int aLeftType,
                           int aRightType){
      leftAnnotation = aLeftAnnotation;
      rightAnnotation = aRightAnnotation;
      leftType = aLeftType;
      rightType = aRightType;
    } // DiffSetElement

    /** Sets the left annotation*/
    public void setLeftAnnotation(Annotation aLeftAnnotation){
      leftAnnotation = aLeftAnnotation;
    } // setLeftAnnot

    /** Gets the left annotation*/
    public Annotation getLeftAnnotation(){
      return leftAnnotation;
    } // getLeftAnnotation

    /** Sets the right annotation*/
    public void setRightAnnotation(Annotation aRightAnnotation){
      rightAnnotation = aRightAnnotation;
    } // setRightAnnot

    /** Gets the right annotation*/
    public Annotation getRightAnnotation(){
      return rightAnnotation;
    } // getRightAnnotation

    /** Sets the left type*/
    public void setLeftType(int aLeftType){
      leftType = aLeftType;
    } // setLeftType

    /** Get the left type */
    public int getLeftType() {
      return leftType;
    } // getLeftType

    /** Sets the right type*/
    public void setRightType(int aRightType) {
      rightType = aRightType;
    } // setRightType

    /** Get the right type*/
    public int getRightType() {
      return rightType;
    } // getRightType
  } // classs DiffSetElement
} // class AnnotationDiff
