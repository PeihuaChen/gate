/*
 * WaitDialog.java
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
 * Valentin Tablan, 12/07/2000
 *
 * $Id$
 */
 
package gate.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class WaitDialog extends JWindow implements Runnable{
    /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  Box centerBox;

  public WaitDialog(Frame frame, String title) {
    super(frame);
    this.icon = new ImageIcon(ClassLoader.getSystemResource(
                "gate/resources/img/wait.gif"));
    this.frame = frame;
    try  {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public synchronized void showDialog(String[] texts){
    centerBox.removeAll();
    for(int i =0; i < texts.length; i++){
      centerBox.add(new JLabel(texts[i]));
    }
    centerBox.validate();
    pack();
    Point loc = frame.getLocation();
    loc.move(frame.getSize().width - getSize().width / 2 ,
             frame.getSize().height - getSize().height /2 );
    setLocation(loc);
    stop = false;
    Thread thread = new Thread(this);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
    show();
  }

  public synchronized void showDialog(Component[] components){
    centerBox.removeAll();
    for(int i =0; i < components.length; i++){
      centerBox.add(components[i]);
    }
    centerBox.validate();
    pack();
    Point loc = frame.getLocation();
    setLocation(loc.x + (frame.getSize().width - getSize().width) / 2 ,
                loc.y + (frame.getSize().height - getSize().height) /2);
    stop = false;
    Thread thread = new Thread(this);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
    show();
  }

  void jbInit() throws Exception {
    JPanel centerPanel = new JPanel();
    centerBox = Box.createVerticalBox();
    centerPanel.setLayout(borderLayout1);
    centerPanel.setBorder(new LineBorder(Color.darkGray, 2));
    centerPanel.setBackground(Color.white);
    centerBox.setBackground(Color.white);
    picture = new JLabel(icon);
    centerPanel.add(centerBox, BorderLayout.CENTER);
    centerPanel.add(picture, BorderLayout.WEST);
    centerPanel.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
    centerPanel.add(Box.createVerticalStrut(5), BorderLayout.SOUTH);
    centerPanel.add(Box.createHorizontalStrut(8), BorderLayout.EAST);
    getContentPane().add(centerPanel, BorderLayout.CENTER);

  }

  public void goAway(){
    stop = true;
  }
  public void run(){
    while(!stop){
      try{
        Thread.sleep(300);
        centerBox.validate();
        pack();
        Point loc = frame.getLocation();
        setLocation(loc.x + (frame.getSize().width - getSize().width) / 2 ,
                    loc.y + (frame.getSize().height - getSize().height) /2);
        picture.paintImmediately(picture.getVisibleRect());
      }catch(InterruptedException ie){}
    }
    this.setVisible(false);
  }

  boolean stop = false;
  BorderLayout borderLayout1 = new BorderLayout();
  Frame frame;
  JLabel picture;
  Icon icon;
}