package gate.alignment.gui.actions.impl;

import java.util.Set;
import gate.Annotation;
import gate.Document;
import gate.alignment.AlignmentException;
import gate.alignment.gui.AlignmentEditor;
import gate.compound.CompoundDocument;

public class ResetAction extends AbstractAlignmentAction {

  public void execute(AlignmentEditor editor, CompoundDocument document,
          Document srcDocument, String srcAS,
          Set<Annotation> srcAlignedAnnotations, Document tgtDocument,
          String tgtAS, Set<Annotation> tgtAlignedAnnotations,
          Annotation clickedAnnotation) throws AlignmentException {

    editor.clearLatestAnnotationsSelection();
  }

  public String getCaption() {
    return "Reset Selection";
  }

  public boolean invokeForAlignedAnnotation() {
    return false;
  }

  public boolean invokeForUnhighlightedUnalignedAnnotation() {
    return false;
  }

  public String getToolTip() {
    return "Dehighlight selected annotations";
  }

}
