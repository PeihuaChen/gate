/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 22 March 2004
 *
 *  $Id$
 */

package gate.gui.docview;

import gate.*;
import gate.gui.*;

import java.awt.Component;

/**
 * A document viewer is composed out of several views (like the on showing the
 * text, the one showing the annotation sets, the on showing the annotations
 * table, etc). This is the base interface for all the document views.
 */

public interface DocumentView extends ActionsPublisher, VisualResource{

  /**
   * Returns the actual UI component this view represents.
   * @return a {@link Component} value.
   */
  public Component getGUI();

  /**
   * Returns the type of this view.
   * @return an int value
   * @see #CENTRAL
   * @see #HORIZONTAL
   * @see #VERTICAL
   */
  public int getType();
  
  /**
   * Constant for the CENTRAL type of the view inside the document editor. Views
   * of this type are placed in the center of the document editor.
   */
  public static final int CENTRAL = 0;

  /**
   * Constant for the VERTICAL type of the view inside the document editor.
   * Views of this type are placed as a vertical band on the right side of the
   * document editor.
   */
  public static final int VERTICAL = 1;

  /**
   * Constant for the HORIZONTAL type of the view inside the document editor.
   * Views of this type are placed as a horizontal band on the lower side of the
   * document editor.
   */
  public static final int HORIZONTAL = 2;
  

}