package gate.swing;

import javax.swing.JTabbedPane;

import java.awt.*;

public class XJTabbedPane extends JTabbedPane {

  public XJTabbedPane(int tabPlacement){
    super(tabPlacement);
  }

  public int getIndexAt(Point p){
    for(int i = 0; i < getTabCount(); i++){
      if(getBoundsAt(i).contains(p)) return i;
    }
    return -1;
  }
}