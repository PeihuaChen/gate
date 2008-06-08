package gate.alignment.gui.actions.impl;

import java.util.Map;
import java.util.Set;

import gate.Annotation;
import gate.Document;
import gate.alignment.Alignment;
import gate.alignment.AlignmentException;
import gate.alignment.gui.AlignmentEditor;
import gate.compound.CompoundDocument;

public class RemoveAlignmentAction extends AbstractAlignmentAction {

  public void execute(AlignmentEditor editor, CompoundDocument document,
          Map<Document, Set<Annotation>> alignedAnnotations, Annotation clickedAnnotation)
          throws AlignmentException {
    // we don't really need to do anything here
    if(alignedAnnotations == null) {
      throw new AlignmentException("alignedAnnotations cannot be null");
    }

    // alignment object
    Alignment alignment = document.getAlignmentInformation(editor
            .getAlignmentFeatureName());

    // now we remove alignment
    for(Document srcDocument : alignedAnnotations.keySet()) {

      Set<Annotation> srcAnnotations = alignedAnnotations.get(srcDocument);
      if(srcAnnotations == null || srcAnnotations.isEmpty()) continue;

      for(Document tgtDocument : alignedAnnotations.keySet()) {
        if(srcDocument == tgtDocument) {
          continue;
        }

        Set<Annotation> targetAnnotations = alignedAnnotations.get(tgtDocument);
        if(targetAnnotations == null || targetAnnotations.isEmpty()) continue;
        for(Annotation srcAnnotation : srcAnnotations) {
          for(Annotation tgtAnnotation : targetAnnotations) {
            if(alignment.areTheyAligned(srcAnnotation, tgtAnnotation)) {
              alignment.unalign(srcAnnotation, srcDocument, tgtAnnotation,
                      tgtDocument);
            }
          }
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
