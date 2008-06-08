package gate.alignment.gui;

import java.util.HashMap;
import gate.Annotation;
import gate.alignment.AlignmentActionInitializationException;
import gate.alignment.AlignmentException;
import gate.compound.CompoundDocument;

/**
 * Implementers of these are called just before the pair is displayed.
 * @author niraj
 */
public interface PreDisplayAction {

  /**
   * This method is called just before the pair is displayed.
   * @param editor - the editor from which this action is called.
   * @param document - the compoound document.
   * @param docIDsAndAnnots - document id and parent of unit annotations (e.g. sentence)
   * @throws AlignmentException
   */
  public void execute(AlignmentEditor editor, CompoundDocument document,HashMap<String, Annotation> docIDsAndAnnots)
          throws AlignmentException;
  
  public void init(String[] args) throws AlignmentActionInitializationException;

  public void cleanup();
  
}
