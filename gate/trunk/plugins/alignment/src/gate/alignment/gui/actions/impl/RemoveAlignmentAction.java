package gate.alignment.gui.actions.impl;

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

public class RemoveAlignmentAction implements AlignmentAction {

  public void execute(AlignmentEditor editor, CompoundDocument document, Map<Document, Set<Annotation>> alignedAnnotations) throws AlignmentException {
    // we don't really need to do anything here
    if(alignedAnnotations == null) {
      throw new AlignmentException("alignedAnnotations cannot be null");
    }
    
    // alignment object
    Alignment alignment = document.getAlignmentInformation();
    
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
              alignment.unalign(srcAnnotation, srcDocument, tgtAnnotation, tgtDocument);
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

  public Icon getIcon() {
    return null;
  }

  public boolean invokeForAlignedAnnotation() {
    return true;
  }
  
  public boolean invokeForHighlightedUnalignedAnnotation() {
    return false;
  }
  
  public boolean invokeForUnhighlightedUnalignedAnnotation() {
    return false;
  }
  
  public void init(String [] args)  throws AlignmentActionInitializationException {
    // no parameters 
  }

  public void cleanup() {
    // do nothing
  }

}
