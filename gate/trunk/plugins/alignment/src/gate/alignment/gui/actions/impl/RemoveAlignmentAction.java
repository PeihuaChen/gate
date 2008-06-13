package gate.alignment.gui.actions.impl;


import java.util.Set;
import gate.Annotation;
import gate.Document;
import gate.alignment.Alignment;
import gate.alignment.AlignmentException;
import gate.alignment.gui.AlignmentEditor;
import gate.compound.CompoundDocument;

public class RemoveAlignmentAction extends AbstractAlignmentAction {

  public void execute(AlignmentEditor editor, CompoundDocument document,
          Document srcDocument, String srcAS,
          Set<Annotation> srcAlignedAnnotations, Document tgtDocument,
          String tgtAS, Set<Annotation> tgtAlignedAnnotations,
          Annotation clickedAnnotation) throws AlignmentException {

    // alignment object
    Alignment alignment = document.getAlignmentInformation(editor
            .getAlignmentFeatureName());
    if(srcAlignedAnnotations == null || srcAlignedAnnotations.isEmpty())
      return;
    if(tgtAlignedAnnotations == null || tgtAlignedAnnotations.isEmpty())
      return;
    for(Annotation srcAnnotation : srcAlignedAnnotations) {
      for(Annotation tgtAnnotation : tgtAlignedAnnotations) {
        if(alignment.areTheyAligned(srcAnnotation, tgtAnnotation)) {
          alignment.unalign(srcAnnotation, srcAS, srcDocument, tgtAnnotation,
                  tgtAS, tgtDocument);
        }
      }
    }
    editor.clearLatestAnnotationsSelection();
  }

  public String getCaption() {
    return "Remove Alignment";
  }

  public boolean invokeForHighlightedUnalignedAnnotation() {
    return false;
  }

  public boolean invokeForUnhighlightedUnalignedAnnotation() {
    return false;
  }

  public String getToolTip() {
    return "Removes the alignment for selected annotations";
  }

}
