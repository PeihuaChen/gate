package gate.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MenuLayoutTest
    extends JFrame {
  public MenuLayoutTest() {
    super("Displaying Long Menus");
    JMenuBar menuBar = new JMenuBar();
    this.setJMenuBar(menuBar);
    JMenu bigMenu = new JMenu("bigMenu");
    menuBar.add(bigMenu);

    // specify a layout manager for the menu
    // Uncomment the next two lines to use the layout manager
    MenuLayout vflayout = new MenuLayout();
    bigMenu.getPopupMenu().setLayout(vflayout);
    for (int i = 1; i < 200; i++) {
      JMenuItem bigMenuItem = new JMenuItem("bigMenu " + i);
      //uncomment below for crazy sizes
//      bigMenuItem.setFont(bigMenuItem.getFont().deriveFont(
//          12 + (float)Math.random() * 10));
      bigMenu.add(bigMenuItem);
    }
  }

  public static void main(String[] args) {
    MenuLayoutTest frame = new MenuLayoutTest();
    frame.setSize(250, 200);
    frame.setLocation(200, 300);
    frame.setVisible(true);
  }
}