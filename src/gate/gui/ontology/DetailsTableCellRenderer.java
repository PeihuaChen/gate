/*
 *  DetailsTableCellRenderer.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: DetailsTableCellRenderer.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
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
public class DetailsTableCellRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 3257572784619337525L;

  public DetailsTableCellRenderer(DetailsTableModel detailstablemodel) {
    detailsTableModel = detailstablemodel;
  }

  public Component getTableCellRendererComponent(JTable table, Object obj,
          boolean flag, boolean flag1, int i, int j) {
    Component component = super.getTableCellRendererComponent(table, "", flag,
            flag1, i, j);
    try {
      if(j == 0) {
        setText(null);
        if(obj == null) {
          setIcon(null);
        }
        else {
          Object obj1 = detailsTableModel.getValueAt(i, 1);
          setIcon(MainFrame.getIcon(((Boolean)obj).booleanValue()
                  ? "expanded"
                  : "closed"));
          setEnabled(((DetailsGroup)obj1).getSize() > 0);
        }
      }
      else if(j == 1)
        if(obj instanceof DetailsGroup) {
          DetailsGroup detailsgroup = (DetailsGroup)obj;
          setIcon(null);
          setFont(getFont().deriveFont(1));
          setText(detailsgroup.getName());
          setEnabled(detailsgroup.getSize() > 0);
        }
        else if(obj instanceof OClass) {
          OClass tclass = (OClass)obj;
          setIcon(MainFrame.getIcon("ontology-class"));
          setFont(getFont().deriveFont(0));
          setText(tclass.getName());
          setToolTipText(tclass.getURI().toString());
          setEnabled(true);
        }
        else if(obj instanceof OInstance) {
          OInstance oinstance = (OInstance)obj;
          setIcon(MainFrame.getIcon("ontology-instance"));
          setFont(getFont().deriveFont(0));
          setText(oinstance.getName());
          setToolTipText(oinstance.getURI().toString());
          setEnabled(true);
        }
        else if(obj instanceof RDFProperty) {
          RDFProperty property = (RDFProperty)obj;
          String propertyType = "RDF";
          if(property instanceof SymmetricProperty) {
            setIcon(MainFrame.getIcon("ontology-symmetric-property"));
            propertyType = "Symmetric";
          }
          else if(property instanceof AnnotationProperty) {
            setIcon(MainFrame.getIcon("ontology-annotation-property"));
            propertyType = "Annotation";
          }
          else if(property instanceof TransitiveProperty) {
            setIcon(MainFrame.getIcon("ontology-transitive-property"));
            propertyType = "Transitive";
          }
          else if(property instanceof ObjectProperty) {
            setIcon(MainFrame.getIcon("ontology-object-property"));
            propertyType = "Object";
          }
          else if(property instanceof DatatypeProperty) {
            setIcon(MainFrame.getIcon("ontology-datatype-property"));
            propertyType = "Datatype";
          }
          else setIcon(MainFrame.getIcon("ontology-rdf-property"));
          setFont(getFont().deriveFont(0));
          String s = (new StringBuilder()).append(property.getName()).append(
                  " -> ").toString();
          if(property instanceof DatatypeProperty) {
            s = (new StringBuilder()).append(s).append(
                    ((DatatypeProperty)property).getDataType()
                            .getXmlSchemaURI()).toString();
          }
          else if(property instanceof AnnotationProperty) {
            s = property.getName();
          }
          else {
            Set<OResource> set = property.getRange();
            s = (new StringBuilder()).append(s).append(set.toString())
                    .toString();
          }
          setText(s);
          setToolTipText((new StringBuilder()).append(
                  "<HTML><b>" + propertyType + " Property</b><br>").append(
                  property.getURI()).append("</html>").toString());
          setEnabled(true);
        }
        else if(obj instanceof PropertyValue) {
          PropertyValue property = (PropertyValue)obj;
          String propertyType = "RDF";
          if(property.getProperty() instanceof SymmetricProperty) {
            setIcon(MainFrame.getIcon("ontology-symmetric-property"));
            propertyType = "Symmetric";
          }
          else if(property.getProperty() instanceof AnnotationProperty) {
            setIcon(MainFrame.getIcon("ontology-annotation-property"));
            propertyType = "Annotation";
          }
          else if(property.getProperty() instanceof TransitiveProperty) {
            setIcon(MainFrame.getIcon("ontology-transitive-property"));
            propertyType = "Transitive";
          }
          else if(property.getProperty() instanceof ObjectProperty) {
            setIcon(MainFrame.getIcon("ontology-object-property"));
            propertyType = "Object";
          }
          else if(property.getProperty() instanceof DatatypeProperty) {
            setIcon(MainFrame.getIcon("ontology-datatype-property"));
            propertyType = "Datatype";
          }
          else setIcon(MainFrame.getIcon("ontology-rdf-property"));
          setFont(getFont().deriveFont(0));
          String s = property.toString();
          setText(s);
          setToolTipText((new StringBuilder()).append(
                  "<HTML><b>" + propertyType + " Property Value</b><br>")
                  .append(property.getProperty().getURI()).append("</html>")
                  .toString());
          setEnabled(true);
        }
        else {
          setIcon(null);
          setFont(getFont().deriveFont(0));
          setText(obj.toString());
          setEnabled(true);
        }
    }
    catch(Exception e) {
      // just ignore this as we might be making some changes to the tree
    }
    return component;
  }

  protected DetailsTableModel detailsTableModel;
}
