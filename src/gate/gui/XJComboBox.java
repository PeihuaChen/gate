package gate.gui;

import java.awt.*;
import javax.swing.event.*;
import javax.swing.*;
import java.util.Vector;

public class XJComboBox extends JComboBox {

private JList list;
  public XJComboBox(ComboBoxModel aModel){
    super(aModel);
    adjustSizes();
  }

  public XJComboBox(Object[] items){
    super(items);
    adjustSizes();
  }

  public XJComboBox(Vector items){
    super(items);
    adjustSizes();
  }

  public XJComboBox(){
    super();
    adjustSizes();
  }

  public void intervalAdded(ListDataEvent e){
    super.intervalAdded(e);
    adjustSizes();
  }

  public void intervalRemoved(ListDataEvent e){
    super.intervalRemoved(e);
    adjustSizes();
  }

  public void contentsChanged(ListDataEvent e){
    super.contentsChanged(e);
    adjustSizes();
  }

  protected void adjustSizes(){
    Dimension dim = getMinimumSize();
    setPreferredSize(dim);
    setMaximumSize(dim);
  }

}