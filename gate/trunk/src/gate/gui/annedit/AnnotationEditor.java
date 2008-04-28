/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, Sep 11, 2007
 *
 *  $Id$
 */
package gate.gui.annedit;

import gate.*;

/**
 * Interface for all annotation editor components
 */
public interface AnnotationEditor extends VisualResource{
  /**
   * Changes the annotation currently being edited.
   * @param ann the new annotation.
   * @param set the set to which the new annotation belongs. 
   */
  public void editAnnotation(Annotation ann, AnnotationSet set);
  
  /**
   * Checks whether the annotation currently being edited can be considered
   * complete.
   * @return <tt>true</tt> iff the editor has finished editing the current 
   * annotation. This might return <tt>false</tt> for instance when the current 
   * annotation does not yet comply with the schema and the editor 
   * implementation is designed to enforce schemas. 
   */
  public boolean editingFinished();
  
  /**
   * Finds the best location for the editor dialog for a given span of text
   */
  public void placeDialog(int start, int end);

  /**
   * Sets the owner (i.e. controller) for this editor.
   * @param owner
   */
  public void setOwner(AnnotationEditorOwner owner);

  /**
   * @return owner The owner (i.e. controller) for this editor.
   */
  public AnnotationEditorOwner getOwner();

  /**
   * @return the annotation currently edited
   */
  public Annotation getAnnotationCurrentlyEdited();

  /**
   * @return the annotation set currently edited
   */
  public AnnotationSet getAnnotationSetCurrentlyEdited();

  /**
   * @param pinned true if the window should not move
   * when an annotation is selected.
   */
  public void setPinnedMode(boolean pinned);
}
