package gate.creole;

import javax.swing.JPanel;
import gate.*;
import gate.util.*;

public abstract class AbstractVisualResource extends JPanel
                                             implements VisualResource{

  /**
   * Package access constructor to stop normal initialisation.
   * This kind of resources should only be created by the Factory class
   */
  public AbstractVisualResource(){
  }

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