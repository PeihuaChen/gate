package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.gui.MainFrame;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class OntoTreeCellRenderer extends DefaultTreeCellRenderer {
  public OntoTreeCellRenderer() {
  }

  public Component getTreeCellRendererComponent(JTree jtree, Object obj,
          boolean flag, boolean flag1, boolean flag2, int i, boolean flag3) {
    if(obj != null && (obj instanceof DefaultMutableTreeNode)) {
      javax.swing.Icon icon = null;
      String s = null;
      Object obj1 = ((DefaultMutableTreeNode)obj).getUserObject();
      if(obj1 instanceof TClass) {
        icon = MainFrame.getIcon("ontology-class");
        s = ((TClass)obj1).getName();
        setToolTipText(((TClass)obj1).getURI());
      } else if(obj1 instanceof OInstance) {
        icon = MainFrame.getIcon("ontology-instance");
        s = ((OInstance)obj1).getName();
        setToolTipText(((OInstance)obj1).getURI());
      } else if(obj1 instanceof Property) {
        icon = MainFrame.getIcon("ontology-property");
        s = ((Property)obj1).getName();
        setToolTipText(((Property)obj1).getURI());
      }
      if(icon != null) {
        if(flag1)
          setOpenIcon(icon);
        else setClosedIcon(icon);
        if(flag2) setLeafIcon(icon);
      }
      super
              .getTreeCellRendererComponent(jtree, s, flag, flag1, flag2, i,
                      flag3);
    } else {
      super.getTreeCellRendererComponent(jtree, obj, flag, flag1, flag2, i,
              flag3);
    }
    return this;
  }
}
