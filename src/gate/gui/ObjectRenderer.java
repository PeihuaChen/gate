package gate.gui;

import java.awt.Component;
import javax.swing.table.*;
import javax.swing.*;
import javax.swing.border.*;

  public class ObjectRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){
      //prepare the renderer
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                                          row, column);

      if(table.isCellEditable(row, column))
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      return this;
    }

    JButton button;
    JPanel textButtonBox;
  }//class ObjectRenderer extends DefaultTableCellRenderer
