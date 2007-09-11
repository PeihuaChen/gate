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
public interface AnnotationEditor {
  public void editAnnotation(Annotation ann, AnnotationSet set);
}
