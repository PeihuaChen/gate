package gate.swing;

import javax.swing.JMenuItem;
import javax.swing.*;
import java.awt.event.*;

import gate.event.*;

public class XJMenuItem extends JMenuItem {

  public XJMenuItem(Icon icon, String description, StatusListener listener){
    super(icon);
    this.description = description;
    this.listener = listener;
    initListeners();
  }

  public XJMenuItem(String text, String description, StatusListener listener){
    super(text);
    this.description = description;
    this.listener = listener;
    initListeners();
  }

  public XJMenuItem(Action a, StatusListener listener){
    super(a);
    this.description = (String)a.getValue(a.SHORT_DESCRIPTION);
    this.listener = listener;
    initListeners();
  }

  public XJMenuItem(String text, Icon icon,
                    String description, StatusListener listener){
    super(text, icon);
    this.description = description;
    this.listener = listener;
    initListeners();
  }

  public XJMenuItem(String text, int mnemonic,
                    String description, StatusListener listener){
    super(text, mnemonic);
    this.description = description;
    this.listener = listener;
    initListeners();
  }

  protected void initListeners(){
    this.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        listener.statusChanged(description);
      }

      public void mouseExited(MouseEvent e) {
        listener.statusChanged("");
      }
    });
  }

  private StatusListener listener;
  String description;
}