package gate.learningLightWeight;

public class UsefulFunctions {

  /** The sigmoid fucntion. */
  public static double sigmoid(double x) {
    return 1.0 / (1 + Math.exp(-2.0 * x));
  }
}
