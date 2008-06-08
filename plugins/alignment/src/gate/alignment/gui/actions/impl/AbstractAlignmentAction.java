package gate.alignment.gui.actions.impl;

import javax.swing.Icon;
import gate.alignment.AlignmentActionInitializationException;
import gate.alignment.gui.AlignmentAction;

public abstract class AbstractAlignmentAction implements AlignmentAction {

  public void init(String[] args) throws AlignmentActionInitializationException {
    // nothing to do
  }

  public void cleanup() {
    // do nothing
  }

  public boolean invokeForAlignedAnnotation() {
    return true;
  }

  public boolean invokeForHighlightedUnalignedAnnotation() {
    return true;
  }

  public boolean invokeForUnhighlightedUnalignedAnnotation() {
    return true;
  }

  public boolean invokeWithAlignAction() {
    return false;
  }

  public boolean invokeWithRemoveAction() {
    return false;
  }

  public Icon getIcon() {
    return null;
  }

  public String getIconPath() {
    return null;
  }

}
