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
    }

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
          }
        });
        try{
          Thread.sleep(400);
        }catch(InterruptedException ie){}
      }
    }

    public void stopBlinking(){
      synchronized(this){
        if(thread.isAlive()){
          stopIt = true;
        }
      }
    }

    public void startBlinking(){
      synchronized(this){
        if(!thread.isAlive()){
          thread = new Thread(Thread.currentThread().getThreadGroup(),
                              this);
          thread.setPriority(Thread.MIN_PRIORITY);
          thread.start();
        }
      }
    }

    boolean stopIt;
    JTabbedPane tPane;
    int tab;
    Color blinkColor;
    Color oldColor;
    Thread thread;
  }//class TabBlinker implements Runnable