package gate.learningLightWeight;

import java.io.PrintWriter;

public class EvaluationMeasuresComputation {
  public float precision;

  public float recall;

  public float f1;

  public float precisionLenient;

  public float recallLenient;

  public float f1Lenient;

  public int correct;

  public int spurious;

  public int missing;

  public int partialCor;

  private int keySize;

  private int resSize;

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

    }
    else {
      System.out
              .println("!! The macro averaged F measure cannot be done because the number is less than 1 !!");
    }
    return;
  }

  public void printResults() {
    // System.out.println("correct="+new Integer(correct)+ ",
    // paritalCorrect="+new Integer(partialCor)+ ", spurious="+new
    // Integer(spurious)+", missing="+new Integer(missing)+".");
    System.out.print("  (correct, paritalCorrect, spurious, missing)= ("
            + new Integer(correct) + ", " + new Integer(partialCor) + ", "
            + new Integer(spurious) + ", " + new Integer(missing) + ");  ");
    // System.out.println("precision="+(new Float(precision))+ ",
    // recall="+(new Float(recall))+ ", F1="+new Float(f1)+".");
    System.out.print("(precision, recall, F1)= (" + (new Float(precision))
            + ", " + (new Float(recall)) + ", " + new Float(f1) + ");  ");
    // System.out.println("precisionLenient="+(new
    // Float(precisionLenient))+
    // ", recallLenient="+(new Float(recallLenient))+ ", F1Lenient="+new
    // Float(f1Lenient)+".");
    System.out.print("Lenient: (" + (new Float(precisionLenient)) + ", "
            + (new Float(recallLenient)) + ", " + new Float(f1Lenient) + ")\n");
  }

  public void printResults(PrintWriter logFileIn) {
    logFileIn.print("  (correct, paritalCorrect, spurious, missing)= ("
            + new Integer(correct) + ", " + new Integer(partialCor) + ", "
            + new Integer(spurious) + ", " + new Integer(missing) + ");  ");
    // System.out.println("precision="+(new Float(precision))+ ",
    // recall="+(new Float(recall))+ ", F1="+new Float(f1)+".");
    logFileIn.print("(precision, recall, F1)= (" + (new Float(precision))
            + ", " + (new Float(recall)) + ", " + new Float(f1) + ");  ");
    // System.out.println("precisionLenient="+(new
    // Float(precisionLenient))+
    // ", recallLenient="+(new Float(recallLenient))+ ", F1Lenient="+new
    // Float(f1Lenient)+".");
    logFileIn.print("Lenient: (" + (new Float(precisionLenient)) + ", "
            + (new Float(recallLenient)) + ", " + new Float(f1Lenient) + ")\n");
  }

}
