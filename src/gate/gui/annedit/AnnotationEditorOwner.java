/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 17 Sep 2007
 *
 *  $Id$
 */
package gate.gui.annedit;

import javax.swing.text.JTextComponent;

import gate.*;

/**
 * Objects of this type control the interaction with an 
 * {@link AnnotationEditor}.
 */
public interface AnnotationEditorOwner {

  /**
   * Gets the document currently being edited.
   * @return a {@link Document} object.
   */
  public Document getDocument();
  
  /**
   * Gets the UI component used to display the document text. This is used by 
   * the annotation editor for obtaining positioning information. 
   * @return a {@link JTextComponent} object.
   */
  public JTextComponent getTextComponent();
  
  /**
   * Called by the annotation editor when the type of an annotation has been 
   * changed.
   * @param ann the annotation modified (after the modification occurred).
   * @param oldType the old type of the annotation. 
   * @param newType the new type of the annotation.
   */
  public void annotationTypeChanged(Annotation ann, AnnotationSet set, 
          String oldType, 
          String newType);
  
  /**
   * Called by the editor for obtaining the next annotation to be edited.
   * @return an {@link Annotation} value.
   */
  public Annotation getNextAnnotation();
  
  /**
   * Called by the editor for obtaining the previous annotation to be edited.
   * @return an {@link Annotation} value.
   */
  public Annotation getPreviousAnnotation();  
}
