/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 28/01/2001
 *
 *  $Id$
 *
 */
package gate.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * A splash screen.
 * A splash screen is an image that appears on the screen while an application
 * initialises. The implementation uses a {@link java.awt.Window} (a Frame with
 * no decorations such as bar or buttons) and can either display a JComponent
 * as content or an image.
 */
public class Splash extends JWindow {

  /**
   * Constructor from owner and content.
   */
  public Splash(Window owner, JComponent content) {
    super(owner);
    getContentPane().setLayout(new BorderLayout());
    content.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    getContentPane().add(content, BorderLayout.CENTER);
    validate();
    pack();
  }

  /**
   * Contructor from image.
   */
  public Splash(String imageResourcePath) {
    this(null, imageResourcePath);
  }

  /**
   * Constructor from content.
   */
  public Splash(JComponent content) {
    this(null, content);
  }

  /**
   * Constructor from owner and image.
   */
  public Splash(Window owner, String imageResourcePath) {
    this(owner,
        new JLabel(new ImageIcon(Splash.class.getResource(imageResourcePath))));
  }

  /**
   * Displays the splash screen centered in the owner's space or centered on
   * the screen if no owner or owner not shown.
   */
  public void show(){
    Dimension ownerSize;
    Point ownerLocation;
    if(getOwner() == null){
      ownerSize = Toolkit.getDefaultToolkit().getScreenSize();
      ownerLocation = new Point(0, 0);
    }else{
      ownerSize = getOwner().getSize();
      ownerLocation = getOwner().getLocation();
      if(ownerSize.height == 0 ||
         ownerSize.width == 0 ||
         !getOwner().isVisible()){
        ownerSize = Toolkit.getDefaultToolkit().getScreenSize();
        ownerLocation = new Point(0, 0);
      }
    }
    //Center the window
    Dimension frameSize = getSize();
    if (frameSize.height > ownerSize.height)
      frameSize.height = ownerSize.height;
    if (frameSize.width > ownerSize.width)
      frameSize.width = ownerSize.width;
    setLocation(ownerLocation.x + (ownerSize.width - frameSize.width) / 2,
                ownerLocation.y + (ownerSize.height - frameSize.height) / 2);
    super.show();
  }
}