/*
 * BasicUnicodeButtonUI.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan, 21/09/2000
 *
 * $Id$
 */

package gate.gui;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

import guk.pcffont.*;
import guk.*;

public class BasicUnicodeButtonUI extends BasicButtonUI {

  /** Debug flag */
  private static final boolean DEBUG = false;

  protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    FontMetrics fm = g.getFontMetrics();
    int len = text.length();


    /* Draw the Text */
    PCFRenderString s = new PCFRenderString(ufont, text, 0, len,
                                            false, true);
    int x = textRect.x;
    int y = textRect.y + s.metrics.ascent;
    if(model.isEnabled()) {
      /*** paint the text normally */
      g.setColor(b.getForeground());
      if (s.direction == PCFRenderRun.RIGHT_TO_LEFT)
        ufont.drawString(g, s,
                         x + textRect.width - s.metrics.characterWidth, y);
      else
        ufont.drawString(g, s, x, y);
    }else {
      g.setColor(b.getForeground().brighter());
      if (s.direction == PCFRenderRun.RIGHT_TO_LEFT)
        ufont.drawString(g, s,
                         x + textRect.width - s.metrics.characterWidth, y);
      else
        ufont.drawString(g, s, x, y);
      g.setColor(b.getBackground().darker());
      if (s.direction == PCFRenderRun.RIGHT_TO_LEFT)
        ufont.drawString(g, s,
                         x + textRect.width - s.metrics.characterWidth - 1, y - 1);
      else
        ufont.drawString(g, s, x, y);
    }
    s.release();
  }//protected void paintText()

  private static PCFUnicodeFontSet ufont;

  static{
//    ufont = GUK.getFontSet();
  }

}