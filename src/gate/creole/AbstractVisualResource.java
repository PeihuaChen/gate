package gate.creole;

import javax.swing.JPanel;
import gate.*;

public abstract class AbstractVisualResource extends JPanel
                                             implements VisualResource{

  //no doc required: javadoc will copy it from the interface
  public FeatureMap getFeatures(){
    return features;
  }

  public void setFeatures(FeatureMap features){
    this.features = features;
  }

  public Resource init() throws ResourceInstantiationException {
    return this;
  }
  //properties
  protected FeatureMap features;

}