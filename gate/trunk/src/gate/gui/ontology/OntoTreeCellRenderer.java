/*
 *  OntologyTreeCellRenderer.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OntologyTreeCellRenderer.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.gui.MainFrame;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * A Class that defines how each node in the ontology tree should look
 * like (e.g. Icon, Text and Tooltips etc).
 * 
 * @author niraj
 * 
 */
public class OntoTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final long serialVersionUID = 3256445798102610225L;

  public OntoTreeCellRenderer() {
  }

  public Component getTreeCellRendererComponent(JTree jtree, Object obj,
          boolean flag, boolean flag1, boolean flag2, int i, boolean flag3) {
    if(obj != null && (obj instanceof DefaultMutableTreeNode)) {
      javax.swing.Icon icon = null;
      String s = null;
      Object obj1 = ((DefaultMutableTreeNode)obj).getUserObject();
      if(obj1 instanceof OClass) {
        icon = MainFrame.getIcon("ontology-class");
        s = ((OClass)obj1).getName();
        if(((OClass)obj1).getURI().isAnonymousResource())
          setToolTipText(((OClass)obj1).getURI().toString()
                  + " is an annonymous class");
        else setToolTipText(((OClass)obj1).getURI().toString());
      }
      else if(obj1 instanceof OInstance) {
        icon = MainFrame.getIcon("ontology-instance");
        s = ((OInstance)obj1).getName();
        setToolTipText(((OInstance)obj1).getURI().toString());
      }
      else if(obj1 instanceof AnnotationProperty) {
        icon = MainFrame.getIcon("ontology-annotation-property");
        s = ((AnnotationProperty)obj1).getName();
        setToolTipText(((AnnotationProperty)obj1).getURI().toString());
      }
      else if(obj1 instanceof DatatypeProperty) {
        icon = MainFrame.getIcon("ontology-datatype-property");
        s = ((DatatypeProperty)obj1).getName();
        setToolTipText(((DatatypeProperty)obj1).getURI().toString());
      }
      else if(obj1 instanceof SymmetricProperty) {
        icon = MainFrame.getIcon("ontology-symmetric-property");
        s = ((SymmetricProperty)obj1).getName();
        setToolTipText(((SymmetricProperty)obj1).getURI().toString());
      }
      else if(obj1 instanceof TransitiveProperty) {
        icon = MainFrame.getIcon("ontology-transitive-property");
        s = ((TransitiveProperty)obj1).getName();
        setToolTipText(((TransitiveProperty)obj1).getURI().toString());
      }
      else if(obj1 instanceof ObjectProperty) {
        icon = MainFrame.getIcon("ontology-object-property");
        s = ((ObjectProperty)obj1).getName();
        setToolTipText(((ObjectProperty)obj1).getURI().toString());
      }
      else if(obj1 instanceof RDFProperty) {
        icon = MainFrame.getIcon("ontology-property");
        s = ((RDFProperty)obj1).getName();
        setToolTipText(((RDFProperty)obj1).getURI().toString());
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
    }
    else {
      super.getTreeCellRendererComponent(jtree, obj, flag, flag1, flag2, i,
              flag3);
    }
    return this;
  }
}
