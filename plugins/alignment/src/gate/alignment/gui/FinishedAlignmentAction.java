package gate.alignment.gui;

import java.util.Set;

import gate.Annotation;
import gate.Document;
import gate.alignment.AlignmentActionInitializationException;
import gate.alignment.AlignmentException;
import gate.compound.CompoundDocument;

/**
 * Implementers of these are called when user says that the alignment is finished.
 * 
 * @author niraj
 */
public interface FinishedAlignmentAction {

  /**
   * This method is called when user says that the alignment is finished.
   * 
   * @param editor - the editor from which this action is called.
   * @param document - the compoound document.
   * @throws AlignmentException
   */
  public void execute(AlignmentEditor editor, CompoundDocument document,
          Document srcDocument, String srcAS, Set<Annotation> srcAnnotations,
          Document tgtDocument, String tgtAS, Set<Annotation> tgtAnnotations)
          throws AlignmentException;

  public void init(String[] args) throws AlignmentActionInitializationException;

  public void cleanup();

}
