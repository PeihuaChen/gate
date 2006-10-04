package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.gui.MainFrame;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DetailsTableCellRenderer extends DefaultTableCellRenderer {
  public DetailsTableCellRenderer(DetailsTableModel detailstablemodel) {
    detailsTableModel = detailstablemodel;
  }

  public Component getTableCellRendererComponent(JTable table, Object obj,
          boolean flag, boolean flag1, int i, int j) {
    Component component = super.getTableCellRendererComponent(table, "", flag,
            flag1, i, j);
    if(j == 0) {
      setText(null);
      if(obj == null) {
        setIcon(null);
      } else {
        Object obj1 = detailsTableModel.getValueAt(i, 1);
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
      } else if(obj instanceof TClass) {
        TClass tclass = (TClass)obj;
        setIcon(MainFrame.getIcon("ontology-class"));
        setFont(getFont().deriveFont(0));
        setText(tclass.getName());
        setToolTipText(tclass.getURI());
        setEnabled(true);
      } else if(obj instanceof OInstance) {
        OInstance oinstance = (OInstance)obj;
        setIcon(MainFrame.getIcon("ontology-instance"));
        setFont(getFont().deriveFont(0));
        setText(oinstance.getName());
        setToolTipText(oinstance.getURI());
        setEnabled(true);
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

  protected DetailsTableModel detailsTableModel;
}
