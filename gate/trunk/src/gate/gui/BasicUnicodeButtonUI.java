package gate.gui;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;


import pcffont.*;



public class BasicUnicodeButtonUI extends BasicButtonUI {


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




  private static String fontBase = "file:///z:/ue/jmutt/fonts/";

  private static String imBase =  "file:///z:/ue/jmutt/im/";
  private static PCFUnicodeFontSet ufont;

  static {
    URL fonturl = null;
    //
    // Set up the character info package.
    //
    try {
        UCData.ucdata_load(new URL(fontBase),
                           UCData.UCDATA_CTYPE|UCData.UCDATA_RECOMP);
    } catch (MalformedURLException malfu) {}

    //
    // Load the font.
    //
    try {
        fonturl = new URL(fontBase + "basic.fst");
    } catch (MalformedURLException mue) {
        System.err.println("Unable to load: "+fontBase+"basic.fst");
        System.exit(1);
    }

    try {
        ufont = new PCFUnicodeFontSet(fonturl.openStream(), fontBase);
    } catch (IOException ie) {
        System.err.println("Unable to load the font: " + fonturl);
//        System.exit(1);
    }
  }
}
