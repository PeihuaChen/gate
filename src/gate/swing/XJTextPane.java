/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 06/03/2001
 *
 *  $Id$
 *
 */

package gate.swing;

import javax.swing.*;
import javax.swing.text.*;
import java.beans.*;
import java.awt.*;

/**
 * A custom JTextPane that reinitialises the default font style when th UI
 * changes. This is needed by applications that want to be able to change the
 * font in the entire application by changing the UI defaults table.
 */
public class XJTextPane extends JTextPane {

  public XJTextPane() {
    super();
    initListeners();
    updateStyle();
  }

  public XJTextPane(StyledDocument doc) {
    super(doc);
    initListeners();
    updateStyle();
  }

  protected void initListeners(){
    addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if(e.getPropertyName().equals("UI")){
          updateStyle();
        }
      }
    });
  }

  protected void updateStyle(){
    Font newFont = UIManager.getFont("TextPane.font");
    Style defaultStyle = getStyle("default");
    StyleConstants.setFontFamily(defaultStyle, newFont.getFamily());
    StyleConstants.setFontSize(defaultStyle, newFont.getSize());
    StyleConstants.setItalic(defaultStyle, newFont.isItalic());
    StyleConstants.setBold(defaultStyle, newFont.isBold());
  }
}