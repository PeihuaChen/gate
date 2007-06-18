/*
 *  EvaluationMeasuresComputation.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: EvaluationMeasuresComputation.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning;

import java.io.PrintWriter;

/**
 * Store the results for computing the F-measures for binary classification
 * problem (or one label of multi-class problems). The measures include
 * precision, recall and F1. It has exact results as well as lenient results
 * counting partial matches.
 */
public class EvaluationMeasuresComputation {
  /** Precision for exact matches. */
  public float precision;
  /** Recall for exact matches. */
  public float recall;
  /** F1 for exact matches. */
  public float f1;
  /** Precision for exact and partial matches. */
  public float precisionLenient;
  /** Recall for exact and partial matches. */
  public float recallLenient;
  /** F1 for exact and partial matches. */
  public float f1Lenient;
  /** Number of exact matches. */
  public int correct;
  /** Number of instances incorretly with predicted label. */
  public int spurious;
  /** Number of positive instances missed by the system. */
  public int missing;
  /** Number of only partial matches. */
  public int partialCor;
  /** Number of positive examples in key set. */
  private int keySize;
  /** Number of positive examples in the results. */
  private int resSize;

  /** Constructor. Set everything as zero. */
  public EvaluationMeasuresComputation() {
    precision = 0;
    recall = 0;
    f1 = 0;
    precisionLenient = 0;
    recallLenient = 0;
    f1Lenient = 0;
    correct = 0;
    spurious = 0;
    missing = 0;
    partialCor = 0;
  }

  /** Compute the F-measures from the numbers. */
  public void computeFmeasure() {
    keySize = correct + partialCor + spurious;
    resSize = correct + partialCor + missing;
    if((keySize) == 0)
      precision = 0;
    else precision = (float)correct / keySize;
    if((resSize) == 0)
      recall = 0;
    else recall = (float)correct / resSize;
    if((precision + recall) == 0)
      f1 = 0;
    else f1 = 2 * precision * recall / (precision + recall);
    return;
  }

  /** Compute the lenient F-measures from the numbers. */
  public void computeFmeasureLenient() {
    keySize = correct + partialCor + spurious;
    resSize = correct + partialCor + missing;
    if((keySize) == 0)
      precisionLenient = 0;
    else precisionLenient = (float)(correct + partialCor) / keySize;
    if((resSize) == 0)
      recallLenient = 0;
    else recallLenient = (float)(correct + partialCor) / resSize;
    if((precisionLenient + recallLenient) == 0)
      f1Lenient = 0;
    else f1Lenient = 2 * precisionLenient * recallLenient
      / (precisionLenient + recallLenient);
    return;
  }

  /** Accumulate the F-measure data for computing overall results. */
  public void add(EvaluationMeasuresComputation anotherMeasure) {
    this.correct += anotherMeasure.correct;
    this.partialCor += anotherMeasure.partialCor;
    this.missing += anotherMeasure.missing;
    this.spurious += anotherMeasure.spurious;
    this.precision += anotherMeasure.precision;
    this.recall += anotherMeasure.recall;
    this.f1 += anotherMeasure.f1;
    this.precisionLenient += anotherMeasure.precisionLenient;
    this.recallLenient += anotherMeasure.recallLenient;
    this.f1Lenient += anotherMeasure.f1Lenient;
    return;
  }

  /** Compute the macro averaged results. */
  public void macroAverage(int k) {
    if(k > 0) {
      this.correct /= k;
      this.partialCor /= k;
      this.missing /= k;
      this.spurious /= k;
      this.precision /= k;
      this.recall /= k;
      this.f1 /= k;
      this.precisionLenient /= k;
      this.recallLenient /= k;
      this.f1Lenient /= k;
    } else {
      System.out
        .println("!! The macro averaged F measure cannot be done because the number is less than 1 !!");
    }
    return;
  }

  /** Print out the results. */
  public String printResults() {
    StringBuffer logMessage= new StringBuffer();
    logMessage.append("  (correct, paritalCorrect, spurious, missing)= ("
      + new Integer(correct) + ", " + new Integer(partialCor) + ", "
      + new Integer(spurious) + ", " + new Integer(missing) + ");  ");
    logMessage.append("(precision, recall, F1)= (" + (new Float(precision))
      + ", " + (new Float(recall)) + ", " + new Float(f1) + ");  ");
    logMessage.append("Lenient: (" + (new Float(precisionLenient)) + ", "
      + (new Float(recallLenient)) + ", " + new Float(f1Lenient) + ")\n");
    return logMessage.toString();
  }

}
