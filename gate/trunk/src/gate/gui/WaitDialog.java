package gate.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class WaitDialog extends JWindow implements Runnable{
  Box centerBox;

  public WaitDialog(Frame frame, String title, boolean modal) {
    super(frame);
    this.frame = frame;
    try  {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public void showDialog(String[] texts, boolean modal){
    centerBox.removeAll();
    for(int i =0; i < texts.length; i++){
      centerBox.add(new JLabel(texts[i]));
    }
    centerBox.validate();
    pack();
    stop = false;
    Thread thread = new Thread(this);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
    Point loc = frame.getLocation();
    loc.move(frame.getSize().width - getSize().width / 2 ,
             frame.getSize().height - getSize().height /2 );
    setLocation(loc);
    show();
  }

  public void showDialog(Component[] components, boolean modal){
    centerBox.removeAll();
    for(int i =0; i < components.length; i++){
      centerBox.add(components[i]);
    }
    centerBox.validate();
    pack();
    stop = false;
    Thread thread = new Thread(this);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
    Point loc = frame.getLocation();
    loc.move(frame.getSize().width - getSize().width / 2 ,
             frame.getSize().height - getSize().height /2 );
    setLocation(loc);
    show();
  }

  void jbInit() throws Exception {
    JPanel centerPanel = new JPanel();
    centerBox = Box.createVerticalBox();
    centerPanel.setLayout(borderLayout1);
    centerPanel.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
    centerBox.setBackground(Color.white);
    centerPanel.setBackground(Color.white);
    this.getContentPane().add(centerPanel);
    picture = new JLabel(new ImageIcon(ClassLoader.getSystemResource(
                    "muse/resources/wait.gif")));
    centerPanel.add(centerBox, BorderLayout.CENTER);
    centerPanel.add(picture, BorderLayout.WEST);


  }

  public void goAway(){
    stop = true;
  }
  public void run(){
    while(!stop){
      try{
        Thread.sleep(200);
        picture.paintImmediately(picture.getVisibleRect());
      }catch(InterruptedException ie){}
    }
    this.setVisible(false);
  }

  boolean stop = false;
  BorderLayout borderLayout1 = new BorderLayout();
  Frame frame;
  JLabel picture;
}
