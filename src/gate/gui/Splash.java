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
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    setLocation((screenSize.width - frameSize.width) / 2,
                      (screenSize.height - frameSize.height) / 2);
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

}