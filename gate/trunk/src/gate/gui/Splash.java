package gate.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class Splash extends JWindow {

  public Splash(Window owner, JComponent content) {
    super(owner);
    getContentPane().setLayout(new BorderLayout());
    content.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    getContentPane().add(content, BorderLayout.CENTER);
    validate();
    pack();
  }

  public Splash(String imageResourcePath) {
    this(null, imageResourcePath);
  }

  public Splash(JComponent content) {
    this(null, content);
  }

  public Splash(Window owner, String imageResourcePath) {
    this(owner,
         new JLabel(new ImageIcon(Splash.class.getResource(imageResourcePath))));
  }

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