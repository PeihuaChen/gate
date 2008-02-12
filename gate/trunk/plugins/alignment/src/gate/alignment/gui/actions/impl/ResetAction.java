package gate.alignment.gui.actions.impl;

import java.util.Map;
import java.util.Set;

import gate.Annotation;
import gate.Document;
import gate.alignment.AlignmentActionInitializationException;
import gate.alignment.AlignmentException;
import gate.alignment.gui.AlignmentAction;
import gate.alignment.gui.AlignmentEditor;
import gate.compound.CompoundDocument;
import javax.swing.Icon;

public class ResetAction implements AlignmentAction {

  public void execute(AlignmentEditor editor, CompoundDocument document, Map<Document, Set<Annotation>> alignedAnnotations) throws AlignmentException {
    editor.clearLatestAnnotationsSelection();
  }

  public String getCaption() {
    return "Reset Selection";
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
  
  public void init(String [] args)  throws AlignmentActionInitializationException {
    // no parameters 
  }
  
  public void cleanup() {
    // do nothing
  }
}
