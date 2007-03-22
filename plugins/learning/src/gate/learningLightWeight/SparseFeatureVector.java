package gate.learningLightWeight;

public class SparseFeatureVector {
  int len; // length of feature vector (number of non-zero elements)

  public int[] indexes; // index of non-zero elements

  public float[] values; // value of non-zero elements

  public SparseFeatureVector() {
    len = 0;
    indexes = null;
    values = null;

  }

  public SparseFeatureVector(int num) {
    len = num;
    indexes = new int[num];
    values = new float[num];
  }

  public int getLen() {
    return len;
  }

  public int[] getIndexes() {
    return indexes;
  }

  public float[] getValues() {
    return values;
  }

}
