package gate.alignment.gui;

import gate.Annotation;
import gate.Document;
import gate.alignment.AlignmentActionInitializationException;
import gate.alignment.AlignmentException;
import gate.compound.CompoundDocument;

/**
 * Implementers of these are called just before the pair is displayed.
 * 
 * @author niraj
 */
public interface PreDisplayAction {

  /**
   * This method is called just before the pair is displayed.
   * 
   * @param editor - the editor from which this action is called.
   * @param document - the compoound document.
   * @throws AlignmentException
   */
  public void execute(AlignmentEditor editor, CompoundDocument document,
          Document srcDocument, String srcAS, Annotation srcAnnotation,
          Document tgtDocument, String tgtAS, Annotation tgtAnnotation)
          throws AlignmentException;

  public void init(String[] args) throws AlignmentActionInitializationException;

  public void cleanup();

}
