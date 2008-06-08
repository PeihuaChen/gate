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
          Map<Document, Set<Annotation>> alignedAnnotations, Annotation clickedAnnotation)
          throws AlignmentException;

  /**
   * Keep this null in order to be called along with the default align action.
   * @return
   */
  public String getCaption();

  /**
   * Keep this null in order to be called along with the default align action.
   * @return
   */
  public Icon getIcon();

  public String getIconPath();
  
  public boolean invokeForAlignedAnnotation();

  public boolean invokeForHighlightedUnalignedAnnotation();

  public boolean invokeForUnhighlightedUnalignedAnnotation();

  public void init(String[] args) throws AlignmentActionInitializationException;

  public void cleanup();
  
  public boolean invokeWithAlignAction();
  
  public boolean invokeWithRemoveAction();
  
  public String getToolTip();
}
