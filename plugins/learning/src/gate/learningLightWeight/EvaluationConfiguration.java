/*
 * Created on 2005-5-25
 * EvaluationConfiguration.java 
 */
package gate.learningLightWeight;

import java.io.Serializable;
import org.jdom.Element;

/***********************************************************************
 * This object is used by the LearningEngine and specifies how the
 * Evaluation is done. It just stores the appropriate values which are
 * used internally by the LearningEngine.
 **********************************************************************/
public class EvaluationConfiguration implements Serializable {
  public static final int kfold = 1;

  public static final int split = 2;

  public int mode = EvaluationConfiguration.split;

  public double ratio = 0.66d;

  public int k = 10;

  public int kk = 1;

  /** Creates an EvaluationConfiguration with the default values * */
  public EvaluationConfiguration() {
  }

  /** Uses K fold cross validation with a random seed. * */
  public EvaluationConfiguration(int k) {
    mode = EvaluationConfiguration.kfold;
    this.k = k;
  }

  /**
   * Does a simple hold-out evaluation. Ratio has a value between 0 and
   * 1 and indicates the ratio of instances to be used for training. A
   * typical value is 0.66, indicating that one third of the instances
   * are used for testing and not for training.
   */
  public EvaluationConfiguration(double ratio, int kk) {
    this.mode = EvaluationConfiguration.split;
    this.ratio = ratio;
    this.kk = kk;
  }

  public EvaluationConfiguration(double ratio) {
    this.mode = EvaluationConfiguration.split;
    this.ratio = ratio;
    this.kk = 1;
  }

  // ex : <EVALUATION method="holdout">0.7</EVALUATION>
  public String toXML() {
    StringBuffer sb = new StringBuffer();
    sb.append("<EVALUATION method=\"");
    if(mode == kfold)
      sb.append("kfold");
    else sb.append("split");
    sb.append("\">");
    if(mode == kfold)
      sb.append(this.k);
    else sb.append(this.ratio);
    sb.append("</EVALUATION>");
    return sb.toString();
  }

  // ex : <EVALUATION method="holdout">0.7</EVALUATION>
  public static EvaluationConfiguration fromXML(Element domElement) {
    String method = domElement.getAttributeValue("method");
    String kk = domElement.getAttributeValue("runs");
    String value = domElement.getAttributeValue("ratio");
    boolean kfold = method.equalsIgnoreCase("kfold");
    if(kfold) {
      return new EvaluationConfiguration(Integer.parseInt(kk));
    }
    if(kk == null) {
      return new EvaluationConfiguration(Double.parseDouble(value));
    }
    else {
      return new EvaluationConfiguration(Double.parseDouble(value), Integer
              .parseInt(kk));
    }
  }
}
