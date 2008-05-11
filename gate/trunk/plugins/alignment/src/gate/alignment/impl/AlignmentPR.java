package gate.alignment.impl;

import java.util.HashMap;

import gate.ProcessingResource;
import gate.Resource;
import gate.alignment.AlignmentException;
import gate.alignment.AlignmentMethod;
import gate.compound.CompoundDocument;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;

/**
 * This PR deletes a member document from the compound document.
 * 
 * @author niraj
 */
public class AlignmentPR extends AbstractLanguageAnalyser implements
                                                         ProcessingResource {

  /**
   * Name of the combining method class that is used for combining the
   * documents.
   */
  protected String alignmentMethod;

  /**
   * Other accompanying parameters used for the alignment method. It
   * must follow the following format. param1=value;param2=value;
   */
  protected String parameters;

  /**
   * name of the document feature used to refer to the alignment
   */
  protected String alignmentFeatureName;

  /**
   * Instance of the alignment method
   */
  protected AlignmentMethod alignmentMethodInst;

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    try {
      alignmentMethodInst = (AlignmentMethod)Class.forName(alignmentMethod)
              .newInstance();
      return this;
    }
    catch(Exception e) {
      throw new ResourceInstantiationException(e);
    }
  }

  /* this method is called to reinitialize the resource */
  public void reInit() throws ResourceInstantiationException {
    // reinitialization code
    init();
  }

  /**
   * Execute method
   */
  public void execute() throws ExecutionException {
    if(document == null) {
      throw new ExecutionException("Document is null!");
    }

    if(!(document instanceof CompoundDocument))
      throw new ExecutionException(
              "Document has to be an instance of Compound Document");

    HashMap<String, String> params = new HashMap<String, String>();
    String[] prms = parameters.split(";");
    if(prms != null) {
      for(int i = 0; i < prms.length; i++) {
        int index = prms[i].indexOf("=");
        if(index < 0) throw new ExecutionException("Invalid parameters!");

        String[] keyValue = new String[2];
        keyValue[0] = prms[i].substring(0, index);
        keyValue[1] = prms[i].substring(index + 1, prms[i].length());
        params.put(keyValue[0], keyValue[1]);
      }
    }

    try {
      alignmentMethodInst.execute((CompoundDocument)document,
              alignmentFeatureName, params);
    }
    catch(AlignmentException ex) {
      throw new ExecutionException(ex);
    }

  }

  public String getAlingmentFeatureName() {
    return alignmentFeatureName;
  }

  public void setAlingmentFeatureName(String alingmentFeatureName) {
    this.alignmentFeatureName = alingmentFeatureName;
  }

}
