/*
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Valentin Tablan 09/07/2001
 *
 *  $Id$
 */
package gate.creole;

import gate.*;
import gate.util.*;

/**
 * Visual Resources that display and/or edit annotations.
 * This type of resources can be used to either display or edit existing
 * annotations or to create new annotations.
 */
public interface AnnotationVisualResource extends VisualResource {
  /**
   * Used when the viewer/editor has to display/edit an existing annotation
   * @param annSet the {@link AnnotationSet} to which the displayed annotation
   * belongs
   * @param ann the annotation to be displayed or edited
   */
  public void setAnnotation(AnnotationSet annSet, Annotation ann);

  /**
   * Used when the viewer has to create new annotations.
   * @param annSet the {@link AnnotationSet} to which the new annotation(s) will
   * belong
   * @param startOffset the start offset of the span covered by the new
   * annotation(s)
   */
  public void setSpan(AnnotationSet annSet, Long startOffset, Long endOffset);

  /**
   * Called by the GUI when the user has pressed the "OK" button. This should
   * trigger the saving of the newly created annotation(s)
   */
  public void okAction() throws GateException;

  /**
   * Checks whether this viewer/editor can handle a specific annotation type.
   */
  public boolean canDisplay(String annotationType);

}//public interface AnnotationVisualResource extends VisualResource