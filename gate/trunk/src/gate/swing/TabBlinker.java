/*  TabBlinker.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 30/03/2001
 *
 *  $Id$
 *
 */
package gate.swing;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;

public class TabBlinker implements Runnable{
    public TabBlinker(JTabbedPane pane, Component comp, Color blinkColor){
      this.tPane = pane;
      this.tab = tPane.indexOfComponent(comp);
      this.blinkColor = blinkColor;
      thread = new Thread(Thread.currentThread().getThreadGroup(),
                          this);
      thread.setPriority(Thread.MIN_PRIORITY);
    }// TabBlinker(JTabbedPane pane, Component comp, Color blinkColor)

    public void run(){
      oldColor = tPane.getBackgroundAt(tab);
      synchronized(this){
        stopIt = false;
      }
      while(true){
        synchronized(this){
          if(tPane.getSelectedIndex() == tab) stopIt = true;
          if(stopIt){
            tPane.setBackgroundAt(tab, oldColor);
            return;
          }
        }
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            if(tPane.getBackgroundAt(tab).equals(oldColor)){
              tPane.setBackgroundAt(tab, blinkColor);
            }else{
              tPane.setBackgroundAt(tab, oldColor);
            }
          }// run()
        });
        try {
          Thread.sleep(400);
        } catch(InterruptedException ie){}
      }// while
    }//run()

    public void stopBlinking(){
      synchronized(this){
        if(thread.isAlive()){
          stopIt = true;
        }
      }
    }// void stopBlinking()

    public void startBlinking(){
      synchronized(this){
        if(!thread.isAlive()){
          thread = new Thread(Thread.currentThread().getThreadGroup(),
                              this);
          thread.setPriority(Thread.MIN_PRIORITY);
          thread.start();
        }
      }
    }// void startBlinking()

    boolean stopIt;
    JTabbedPane tPane;
    int tab;
    Color blinkColor;
    Color oldColor;
    Thread thread;
  }//class TabBlinker implements Runnable