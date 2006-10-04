// Decompiled by DJ v3.9.9.91 Copyright 2005 Atanas Neshkov  Date: 19/09/2006 10:19:55
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   PropertyDetailsTableCellRenderer.java
package gate.gui.ontology;

import gate.creole.ontology.Property;
import gate.gui.MainFrame;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

// Referenced classes of package gate.gui.ontology:
//            DetailsGroup, PropertyDetailsTableModel
public class PropertyDetailsTableCellRenderer extends DefaultTableCellRenderer {
  public PropertyDetailsTableCellRenderer(
          PropertyDetailsTableModel propertydetailstablemodel) {
    propertyDetailsTableModel = propertydetailstablemodel;
  }

  public Component getTableCellRendererComponent(JTable jtable, Object obj,
          boolean flag, boolean flag1, int i, int j) {
    Component component = super.getTableCellRendererComponent(jtable, "", flag,
            flag1, i, j);
    if(j == 0) {
      setText(null);
      if(obj == null) {
        setIcon(null);
      } else {
        Object obj1 = propertyDetailsTableModel.getValueAt(i, 1);
        setIcon(MainFrame.getIcon(((Boolean)obj).booleanValue()
                ? "expanded"
                : "closed"));
        setEnabled(((DetailsGroup)obj1).getSize() > 0);
      }
    } else if(j == 1)
      if(obj instanceof DetailsGroup) {
        DetailsGroup detailsgroup = (DetailsGroup)obj;
        setIcon(null);
        setFont(getFont().deriveFont(1));
        setText(detailsgroup.getName());
        setEnabled(detailsgroup.getSize() > 0);
      } else if(obj instanceof Property) {
        Property property = (Property)obj;
        setIcon(MainFrame.getIcon("ontology-property"));
        setFont(getFont().deriveFont(0));
        String s = (new StringBuilder()).append(property.getName()).append(
                " -> ").toString();
        java.util.Set set = property.getRange();
        s = (new StringBuilder()).append(s).append(set.toString()).toString();
        setText(s);
        setToolTipText((new StringBuilder()).append(
                "<HTML><b>Object Property</b><br>").append(property.getURI())
                .append("</html>").toString());
        setEnabled(true);
      } else {
        setIcon(null);
        setFont(getFont().deriveFont(0));
        setText(obj.toString());
        setEnabled(true);
      }
    return component;
  }

  protected PropertyDetailsTableModel propertyDetailsTableModel;
}
