/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 20 Feb 2003
 *
 *  $Id$
 */

package gate.swing;


import javax.swing.*;
import java.awt.*;

/**
 * A modified version of JPopupMenu that uses {@link MenuLayout} as its layout.
 */
public class XJPopupMenu extends JPopupMenu {
  public XJPopupMenu() {
    super();
    setLayout(new MenuLayout());
  }

  public XJPopupMenu(String label){
    super(label);
    setLayout(new MenuLayout());
  }

  /**
   * Force separators to be the same width as the JPopupMenu.
   * This is because the MenuLayout make separators invisible contrary
   * to the default JPopupMenu layout manager.
   * @param aFlag true if the popupmenu is visible
   */
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    for (Component component : getComponents()) {
      if (component instanceof JSeparator) {
        JSeparator separator = (JSeparator) component;
        // use the popupmenu width to set the separators width
        separator.setPreferredSize(new Dimension(
          (int) getLayout().preferredLayoutSize(this).getWidth()
          - getInsets().left - getInsets().right,
          separator.getHeight()));
      }
    }
    revalidate();
  }
}