package gate.alignment.gui.actions.impl;

import java.util.Set;
import gate.Annotation;
import gate.Document;
import gate.alignment.Alignment;
import gate.alignment.AlignmentException;
import gate.alignment.gui.AlignmentEditor;
import gate.compound.CompoundDocument;

public class AlignAction extends AbstractAlignmentAction {

  public void execute(AlignmentEditor editor, CompoundDocument document,
          Document srcDocument, String srcAS,
          Set<Annotation> srcAlignedAnnotations, Document tgtDocument,
          String tgtAS, Set<Annotation> tgtAlignedAnnotations,
          Annotation clickedAnnotation) throws AlignmentException {

    // alignment object
    Alignment alignment = document.getAlignmentInformation(editor
            .getAlignmentFeatureName());

    // so first of all clear the latestSelection
    editor.clearLatestAnnotationsSelection();

    if(srcAlignedAnnotations == null || srcAlignedAnnotations.isEmpty())
      return;
    if(tgtAlignedAnnotations == null || tgtAlignedAnnotations.isEmpty())
      return;
    for(Annotation srcAnnotation : srcAlignedAnnotations) {
      for(Annotation tgtAnnotation : tgtAlignedAnnotations) {
        if(!alignment.areTheyAligned(srcAnnotation, tgtAnnotation)) {
          alignment.align(srcAnnotation, srcAS, srcDocument, tgtAnnotation,
                  tgtAS, tgtDocument);
        }
      }
    }
  }

  public String getCaption() {
    return "Align";
  }

  public boolean invokeForAlignedAnnotation() {
    return false;
  }

  public boolean invokeForUnhighlightedUnalignedAnnotation() {
    return false;
  }

  public String getToolTip() {
    return "Aligns the selected source and target annotations";
  }
}
