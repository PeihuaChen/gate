package gate.alignment.gui.actions.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gate.Annotation;
import gate.Document;
import gate.alignment.Alignment;
import gate.alignment.AlignmentActionInitializationException;
import gate.alignment.AlignmentException;
import gate.alignment.gui.AlignmentAction;
import gate.alignment.gui.AlignmentEditor;
import gate.compound.CompoundDocument;
import javax.swing.Icon;

public class AlignAction implements AlignmentAction {

  public void execute(AlignmentEditor editor, CompoundDocument document,
          Map<Document, Set<Annotation>> alignedAnnotations)
          throws AlignmentException {
    // we don't really need to do anything here
    if(alignedAnnotations == null) {
      throw new AlignmentException("alignedAnnotations cannot be null");
    }

    // alignment object
    Alignment alignment = document.getAlignmentInformation(editor
            .getAlignmentFeatureName());

    // so first of all clear the latestSelection
    editor.clearLatestAnnotationsSelection();

    List<Document> documents = new ArrayList<Document>(alignedAnnotations
            .keySet());
    // now we add alignment
    for(int i = 0; i < documents.size(); i++) {
      Document srcDocument = documents.get(i);
      Set<Annotation> srcAnnotations = alignedAnnotations.get(srcDocument);
      if(srcAnnotations == null || srcAnnotations.isEmpty()) continue;

      for(int j = 0; j < documents.size(); j++) {
        if(i == j) continue;
        Document tgtDocument = documents.get(j);

        Set<Annotation> targetAnnotations = alignedAnnotations.get(tgtDocument);
        if(targetAnnotations == null || targetAnnotations.isEmpty()) continue;
        for(Annotation srcAnnotation : srcAnnotations) {
          for(Annotation tgtAnnotation : targetAnnotations) {
            if(!alignment.areTheyAligned(srcAnnotation, tgtAnnotation))
              alignment.align(srcAnnotation, srcDocument, tgtAnnotation,
                      tgtDocument);
          }
        }
      }
    }
  }

  public String getCaption() {
    return "Align";
  }

  public Icon getIcon() {
    return null;
  }

  public boolean invokeForAlignedAnnotation() {
    return false;
  }

  public boolean invokeForHighlightedUnalignedAnnotation() {
    return true;
  }

  public boolean invokeForUnhighlightedUnalignedAnnotation() {
    return false;
  }

  public void init(String[] args) throws AlignmentActionInitializationException {
    // no parameters
  }

  public void cleanup() {
    // do nothing
  }

}
