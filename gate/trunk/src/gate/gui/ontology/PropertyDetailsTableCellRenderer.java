/*
 *  PropertyDetailsTableCellRenderer.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: PropertyDetailsTableCellRenderer.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.AnnotationProperty;
import gate.creole.ontology.DatatypeProperty;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.SymmetricProperty;
import gate.creole.ontology.TransitiveProperty;
import gate.gui.MainFrame;
import java.awt.Component;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A Class that specifies how each node in the details panel should look
 * like.
 * 
 * @author niraj
 * 
 */
public class PropertyDetailsTableCellRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 3835153969470124336L; 

  public PropertyDetailsTableCellRenderer(
          PropertyDetailsTableModel propertydetailstablemodel) {
    propertyDetailsTableModel = propertydetailstablemodel;
  }

  public Component getTableCellRendererComponent(JTable jtable, Object obj,
          boolean flag, boolean flag1, int i, int j) {
    Component component = super.getTableCellRendererComponent(jtable, "", flag,
            flag1, i, j);
    try {
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
        } else if(obj instanceof RDFProperty) {
          RDFProperty property = (RDFProperty)obj;
          String propertyType = "RDF";
          if(property instanceof SymmetricProperty) {
            setIcon(MainFrame.getIcon("ontology-symmetric-property"));
            propertyType = "Symmetric";
          } else if(property instanceof AnnotationProperty) {
            setIcon(MainFrame.getIcon("ontology-annotation-property"));
            propertyType = "Annotation";
          } else if(property instanceof TransitiveProperty) {
            setIcon(MainFrame.getIcon("ontology-transitive-property"));
            propertyType = "Transitive";
          } else if(property instanceof ObjectProperty) {
            setIcon(MainFrame.getIcon("ontology-object-property"));
            propertyType = "Object";
          } else if(property instanceof DatatypeProperty) {
            setIcon(MainFrame.getIcon("ontology-datatype-property"));
            propertyType = "Datatype";
          } else setIcon(MainFrame.getIcon("ontology-rdf-property"));
          setFont(getFont().deriveFont(0));
          String s = (new StringBuilder()).append(property.getName()).append(
                  " -> ").toString();
          if(property instanceof DatatypeProperty) {
            s = (new StringBuilder()).append(s).append(
                    ((DatatypeProperty)property).getDataType()
                            .getXmlSchemaURI()).toString();
          } else if(property instanceof AnnotationProperty) {
            s = property.getName();
          } else {
            Set<OResource> set = property.getRange();
            s = (new StringBuilder()).append(s).append(set.toString())
                    .toString();
          }
          setText(s);
          setToolTipText((new StringBuilder()).append(
                  "<HTML><b>" + propertyType + " Property</b><br>").append(
                  property.getURI()).append("</html>").toString());
          setEnabled(true);
        } else {
          setIcon(null);
          setFont(getFont().deriveFont(0));
          setText(obj.toString());
          setEnabled(true);
        }
    } catch(Exception e) {
      // just ignore it
      // we might be refreshing the tree
    }
    return component;
  }

  protected PropertyDetailsTableModel propertyDetailsTableModel;
}
