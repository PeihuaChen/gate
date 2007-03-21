package gate.learningLightWeight;

/** The labels for one feature vector (FV) (or one example) */

public class LabelsOfFV {
  /** Number of labels of the FV */
  public int num;

  /** Labels in string format */
  public int[] labels;

  /** The probability of each label for the FV */
  public float[] probs;

  public LabelsOfFV() {
  }

  public LabelsOfFV(int n) {
    this.num = n;
  }

  public LabelsOfFV(int n, int[] labels, float[] probs) {
    this.num = n;
    this.labels = labels;
    this.probs = probs;
  }

  public void setNum(int n) {
    this.num = n;
  }

  public int getNum() {
    return this.num;
  }

  public void setLabels(int[] labels) {
    this.labels = labels;
  }

  public int[] getLabels() {
    return this.labels;
  }

  public void setProbs(float[] probs) {
    this.probs = probs;
  }

  public float[] getProbs() {
    return this.probs;
  }

  public String toOneLine() {
    StringBuffer line = new StringBuffer();
    line.append(num);
    line.append(ConstantParameters.ITEMSEPARATOR);
    for(int i = 0; i < num; ++i) {
      line.append(labels[i]);
      line.append(ConstantParameters.ITEMSEPARATOR);
      line.append(probs[i]);
      line.append(ConstantParameters.ITEMSEPARATOR);
    }

    return line.substring(0, line.length()).toString();
  }
}
