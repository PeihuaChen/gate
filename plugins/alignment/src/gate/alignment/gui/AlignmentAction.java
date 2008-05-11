package gate.alignment.gui;

import java.util.Map;
import java.util.Set;

import gate.Annotation;
import gate.Document;
import gate.alignment.AlignmentActionInitializationException;
import gate.alignment.AlignmentException;
import gate.compound.CompoundDocument;

import javax.swing.Icon;

public interface AlignmentAction {

  public void execute(AlignmentEditor editor, CompoundDocument document,
          Map<Document, Set<Annotation>> alignedAnnotations)
          throws AlignmentException;

  public String getCaption();

  public Icon getIcon();

  public boolean invokeForAlignedAnnotation();

  public boolean invokeForHighlightedUnalignedAnnotation();

  public boolean invokeForUnhighlightedUnalignedAnnotation();

  public void init(String[] args) throws AlignmentActionInitializationException;

  public void cleanup();
}
