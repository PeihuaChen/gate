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

import java.awt.*;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import gate.*;
import gate.creole.*;
import gate.swing.XJTable;
import gate.util.*;

/**
  * This class compare two annotation sets on annotation type given by the
  * AnnotationSchema object. It also deals with graphic representation of the
  * result.
  */
public class AnnotationDiff extends AbstractVisualResource
  implements  Scrollable{

  // number of pixels to be used as increment by scroller
  protected int maxUnitIncrement = 10;

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

  /** This field indicates wheter or not the annot diff should run int the text
   *  mode*/
  private boolean textMode = false;

  /**  Field designated to represent the max nr of annot types and coolors for
    *  each type
    **/
  public static final int MAX_TYPES = 5;
  /** A default type when all annotation are the same represented by White color*/
  public static final int DEFAULT_TYPE = 0;
  /** A correct type when all annotation are corect represented by Green color*/
  public static final int CORRECT_TYPE = 1;
  /** A partially correct type when all annotation are corect represented
   *  by Blue color*/
  public static final int PARTIALLY_CORRECT_TYPE = 2;
  /** A spurious type when annotations in response were not present in key.
   *  Represented by Red color*/
  public static final int SPURIOUS_TYPE = 3;
  /** A missing type when annotations in key were not present in response
   *  Represented by Yellow color*/
  public static final int MISSING_TYPE = 4;

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
  private final int NULL_TYPE = -1;
  /** Used in some setForeground() methods*/
  private  final Color BLACK = new Color(0,0,0);
  /** The array holding the colours according to the annotation types*/
  private Color colors[] = new Color[MAX_TYPES];

  /** A scroll for the AnnotDiff's table*/
  private JScrollPane scrollPane = null;

  /** Used to store the no. of annotations from response,identified as belonging
    * to one of the previous types.
    */
  private long typeCounter[] = new long[MAX_TYPES];


  private gate.util.AnnotationDiffer annotDiffer;

  /** Constructs a AnnotationDif*/
  public AnnotationDiff(){
    annotDiffer = new AnnotationDiffer();
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

  /**  Sets the annot diff to work in the text mode.This would not initiate the
    *  GUI part of annot diff but it would calculate precision etc
    */
  public void setTextMode(Boolean aTextMode){
    //it needs to be a Boolean and not boolean, because you cannot put
    //in the parameters hashmap a boolean, it needs an object
    textMode = aTextMode.booleanValue();
  }// End setTextMode();

  /** Gets the annot diff textmode.True means that the text mode is activated.*/
  public boolean isTextMode(){
    return textMode;
  }// End setTextMode();

  /** Returns a set with all annotations of a specific type*/
  public Set getAnnotationsOfType(int annotType){
    return annotDiffer.getAnnotationsOfType(annotType);
  }//getAnnotationsOfType


  ///////////////////////////////////////////////////
  // PRECISION methods
  ///////////////////////////////////////////////////

  /** @return the precisionStrict field*/
  public double getPrecisionStrict(){
    return annotDiffer.getPrecisionStrict();
  } // getPrecisionStrict

  /** @return the precisionLenient field*/
  public double getPrecisionLenient(){
    return annotDiffer.getPrecisionLenient();
  } // getPrecisionLenient

  /** @return the precisionAverage field*/
  public double getPrecisionAverage(){
    return annotDiffer.getPrecisionAverage();
  } // getPrecisionAverage

  /** @return the fMeasureStrict field*/
  public double getFMeasureStrict(){
    return annotDiffer.getFMeasureStrict(1);
  } // getFMeasureStrict

  /** @return the fMeasureLenient field*/
  public double getFMeasureLenient(){
    return annotDiffer.getFMeasureLenient(1);
  } // getFMeasureLenient

  /** @return the fMeasureAverage field*/
  public double getFMeasureAverage(){
    return annotDiffer.getFMeasureAverage(1);
  } // getFMeasureAverage

  ///////////////////////////////////////////////////
  // RECALL methods
  ///////////////////////////////////////////////////

  /** @return the recallStrict field*/
  public double getRecallStrict(){
    return annotDiffer.getRecallStrict();
  } // getRecallStrict

  /** @return the recallLenient field*/
  public double getRecallLenient(){
    return annotDiffer.getRecallLenient();
  } // getRecallLenient

  /** @return the recallAverage field*/
  public double getRecallAverage(){
    return annotDiffer.getRecallAverage();
  } // getRecallAverage

  ///////////////////////////////////////////////////
  // Missing, spurious, etc methods
  ///////////////////////////////////////////////////

  public long getCorrectCount() {
    return annotDiffer.getCorrectMatches();
  }

  public long getPartiallyCorrectCount() {
    return annotDiffer.getPartiallyCorrectMatches();
  }

  public long getSpuriousCount() {
    return annotDiffer.getSpurious();
  }

  public long getMissingCount() {
    return annotDiffer.getMissing();
  }

  ///////////////////////////////////////////////////
  // FALSE POSITIVE methods
  ///////////////////////////////////////////////////

  /** @return the falsePositiveStrict field*/
  public double getFalsePositiveStrict(){
    return annotDiffer.getFalsePositivesStrict();
  } // getFalsePositiveStrict

  /** @return the falsePositiveLenient field*/
  public double getFalsePositiveLenient(){
    return annotDiffer.getFalsePositivesLenient();
  } // getFalsePositiveLenient

  /** @return the falsePositiveAverage field*/
  public double getFalsePositiveAverage(){
    return (double)(((double)getFalsePositiveLenient() + getFalsePositiveStrict()) / (double)(2.0));
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

  public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
  }// public Dimension getPreferredScrollableViewportSize()

  public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
    return maxUnitIncrement;
  }// public int getScrollableUnitIncrement

  public int getScrollableBlockIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL)
        return visibleRect.width - maxUnitIncrement;
    else
        return visibleRect.height - maxUnitIncrement;
  }// public int getScrollableBlockIncrement

  public boolean getScrollableTracksViewportWidth() {
    return false;
  }// public boolean getScrollableTracksViewportWidth()

  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

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

    if(annotationSchema == null)
     throw new ResourceInstantiationException("No annotation schema defined !");

    if(keyDocument == null)
      throw new ResourceInstantiationException("No key document defined !");

    if(responseDocument == null)
      throw new ResourceInstantiationException("No response document defined !");

    if (keyAnnotationSetName == null)
      // Get the default key AnnotationSet from the keyDocument
      keyAnnotSet = keyDocument.getAnnotations().get(
                              annotationSchema.getAnnotationName());
    else
      keyAnnotSet = keyDocument.getAnnotations(keyAnnotationSetName).
                                    get(annotationSchema.getAnnotationName());


    if (responseAnnotationSetName == null)
      // Get the response AnnotationSet from the default set
      responseAnnotSet = responseDocument.getAnnotations().get(
                                          annotationSchema.getAnnotationName());
    else
      responseAnnotSet = responseDocument.getAnnotations(responseAnnotationSetName).
                                    get(annotationSchema.getAnnotationName());



    // Calculate the diff Set. This set will be used later with graphic
    // visualisation.
    ArrayList choices = (ArrayList) annotDiffer.calculateDiff(keyAnnotSet.get(), responseAnnotSet.get());
    diffSet = new HashSet();
    for(int i=0;i<choices.size();i++) {
      AnnotationDiffer.Choice choice = (AnnotationDiffer.Choice) choices.get(i);
      int type = choice.getType();
      int leftType = 0;
      int rightType = 0;
      if(type == 2) {
        leftType = CORRECT_TYPE;
        rightType = CORRECT_TYPE;
      } else if(type == 1) {
        leftType = PARTIALLY_CORRECT_TYPE;
        rightType = PARTIALLY_CORRECT_TYPE;
      } else {
        if(choice.getKey() == null) { leftType = SPURIOUS_TYPE; rightType = SPURIOUS_TYPE; }
        else { leftType = MISSING_TYPE; rightType = MISSING_TYPE; }
      }
      diffSet.add(new DiffSetElement(choice.getKey(),choice.getResponse(), leftType, rightType));
    }

    // If it runs under text mode just stop here.
    if (textMode) return this;

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
                                                annotDiffer.getMissing());
    jLabel.setForeground(BLACK);
    jLabel.setBackground(colors[MISSING_TYPE]);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    jLabel.setOpaque(true);
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(jLabel);

    // Add a space
    box.add(Box.createRigidArea(new Dimension(0,5)));

    jLabel = new JLabel("Correct (total match):  " + annotDiffer.getCorrectMatches());
    jLabel.setForeground(BLACK);
    jLabel.setBackground(colors[CORRECT_TYPE]);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    jLabel.setOpaque(true);
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(jLabel);

    // Add a space
    box.add(Box.createRigidArea(new Dimension(0,5)));

    jLabel =new JLabel("Partially correct (overlap in Key and Response):  "+
                                        annotDiffer.getPartiallyCorrectMatches());
    jLabel.setForeground(BLACK);
    jLabel.setBackground(colors[PARTIALLY_CORRECT_TYPE]);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    jLabel.setOpaque(true);
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(jLabel);

    // Add a space
    box.add(Box.createRigidArea(new Dimension(0,5)));

    jLabel = new JLabel("Spurious (present in Response but not in Key):  " +
                                        annotDiffer.getSpurious());
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
                                    formatter.format(getPrecisionStrict()));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("Precision average: " +
                                    formatter.format(getPrecisionAverage()));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("Precision lenient: " +
                                    formatter.format(getPrecisionLenient()));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    infoBox.add(box);
    // Add a space
    infoBox.add(Box.createRigidArea(new Dimension(40,0)));

    // RECALL measure
    //Lay out the JLabels from left to right.
    box = new Box(BoxLayout.Y_AXIS);

    jLabel = new JLabel("Recall strict: " + formatter.format(getRecallStrict()));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("Recall average: " + formatter.format(getRecallAverage()));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("Recall lenient: " + formatter.format(getRecallLenient()));
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
                                        formatter.format(getFMeasureStrict()));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("F-Measure average: " +
                                        formatter.format(getFMeasureAverage()));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("F-Measure lenient: " +
                                        formatter.format(getFMeasureLenient()));
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
                                        formatter.format(getFalsePositiveStrict()));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("False positive average: " +
                                        formatter.format(getFalsePositiveAverage()));
    jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));
    box.add(jLabel);

    jLabel = new JLabel("False positive lenient: " +
                                        formatter.format(getFalsePositiveLenient()));
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
        case 0: return "String - Key";
        case 1: return "Start - Key";
        case 2: return "End - Key";
        case 3: return "Features - Key";
        case 4: return "   ";
        case 5: return "String - Response";
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
//           return diffSetElement.getLeftAnnotation().getType();
           Annotation annot = diffSetElement.getLeftAnnotation();
           String theString = "";
           try {
             theString = keyDocument.getContent().getContent(
                    annot.getStartNode().getOffset(),
                    annot.getEndNode().getOffset()).toString();
           } catch (gate.util.InvalidOffsetException ex) {
             Err.prln(ex.getMessage());
           }
           return theString;
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
//           return diffSetElement.getRightAnnotation().getType();
           Annotation annot = diffSetElement.getRightAnnotation();
           String theString = "";
           try {
             theString = responseDocument.getContent().getContent(
                    annot.getStartNode().getOffset(),
                    annot.getEndNode().getOffset()).toString();
           } catch (gate.util.InvalidOffsetException ex) {
             Err.prln(ex.getMessage());
           }
           return theString;
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
        if (diffSetElement.getLeftAnnotation() != null) {
          background = colors[diffSetElement.getLeftType()];
        }
        else return new JPanel();
      }else{
        if (diffSetElement.getRightAnnotation() != null)
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

  /**
    * This class is used for internal purposes. It represents a row from the
    * table.
    */
  protected class DiffSetElement {
    /** This field represent a key annotation*/
    private Annotation leftAnnotation = null;
    /** This field represent a response annotation*/
    private Annotation rightAnnotation = null;
    /** Default initialization of the key type*/
    private int leftType = DEFAULT_TYPE;
    /** Default initialization of the response type*/
    private int rightType = DEFAULT_TYPE;

    /** Constructor for DiffSetlement*/
    public DiffSetElement() {}

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
