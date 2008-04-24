/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AnnotationList.java
 *
 *  Valentin Tablan, 23 Apr 2008
 *
 *  $Id$
 */

package gate.gui.docview;

import gate.gui.annedit.AnnotationData;

import javax.swing.ListSelectionModel;

/**
 * Interface for document views showing a list of annotations.
 */
public interface AnnotationList extends DocumentView {
  
  /**
   * Obtains the selection model used by this list view.
   * @return a {@link ListSelectionModel} object.
   */
  public ListSelectionModel getSelectionModel();
  
  /**
   * Provides the annotation 
   * @param row
   * @return
   */
  public AnnotationData getAnnotationAtRow(int row);

}
