/*
 *  PropertyDetailsTableModel.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: PropertyDetailsTableModel.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 * A DataModel that is created when a node is selected in the ontology property
 * tree. It contains information such as direct/all sub/super properties,
 * equivalent properties, domain/range of each property and property values and so on.
 * The information from this model is then shown in the right hand side
 * panel of the ontology editor.
 * 
 * @author niraj
 * 
 */
public class PropertyDetailsTableModel extends AbstractTableModel {
  public PropertyDetailsTableModel() {
    directSuperProps = new DetailsGroup("Direct Super Properties", true, null);
    allSuperProps = new DetailsGroup("All Super Properties", true, null);
    directSubProps = new DetailsGroup("Direct Sub Properties", true, null);
    allSubProps = new DetailsGroup("All Sub Properties", true, null);
    equivalentProps = new DetailsGroup("Equivalent Properties", true, null);
    domain = new DetailsGroup("Domain", true, null);
    range = new DetailsGroup("Range", true, null);
    detailGroups = new DetailsGroup[0];
    itemComparator = new OntologyItemComparator();
  }

  public int getColumnCount() {
    return 2;
  }

  public int getRowCount() {
    int i = detailGroups.length;
    for(int j = 0; j < detailGroups.length; j++)
      if(detailGroups[j].isExpanded()) i += detailGroups[j].getSize();
    return i;
  }

  public String getColumnName(int i) {
    switch(i){
      case 0: // '\0'
        return "";
      case 1: // '\001'
        return "";
    }
    return "";
  }

  public Class getColumnClass(int i) {
    switch(i){
      case 0:
        return Boolean.class;
      case 1:
        return Object.class;
    }
    return Object.class;
  }

  public boolean isCellEditable(int i, int j) {
    Object obj = getItemForRow(i);
    return j == 0 && (obj instanceof DetailsGroup);
  }

  public void setValueAt(Object obj, int i, int j) {
    Object obj1 = getItemForRow(i);
    if(j == 0 && (obj1 instanceof DetailsGroup)) {
      DetailsGroup detailsgroup = (DetailsGroup)obj1;
      detailsgroup.setExpanded(((Boolean)obj).booleanValue());
    }
    fireTableDataChanged();
  }

  protected Object getItemForRow(int i) {
    int j = 0;
    for(int k = 0; j <= i; k++) {
      if(j == i) return detailGroups[k];
      int l = 1 + (detailGroups[k].isExpanded() ? detailGroups[k].getSize() : 0);
      if(j + l > i) return detailGroups[k].getValueAt(i - j - 1);
      j += l;
    }
    return null;
  }

  public Object getValueAt(int i, int j) {
    Object obj = getItemForRow(i);
    switch(j){
      case 0:
        return (obj instanceof DetailsGroup) ? new Boolean(((DetailsGroup)obj)
                .isExpanded()) : null;
      case 1:
        return obj;
    }
    return null;
  }

  public void setItem(Object obj) {
    detailGroups = (new DetailsGroup[]{directSuperProps, allSuperProps,
        directSubProps, allSubProps, equivalentProps, domain, range});
    RDFProperty property = (RDFProperty)obj;
    directSuperProps.getValues().clear();
    allSuperProps.getValues().clear();
    directSubProps.getValues().clear();
    allSubProps.getValues().clear();
    equivalentProps.getValues().clear();
    domain.getValues().clear();
    range.getValues().clear();
    if(property instanceof AnnotationProperty) {
      fireTableDataChanged();
      return;
    }
    Set<RDFProperty> set = property
            .getSuperProperties(OConstants.DIRECT_CLOSURE);
    if(set != null) {
      directSuperProps.getValues().addAll(set);
      Collections.sort(directSuperProps.getValues(), itemComparator);
    }
    set = property.getSuperProperties(OConstants.TRANSITIVE_CLOSURE);
    if(set != null) {
      allSuperProps.getValues().addAll(set);
      Collections.sort(allSuperProps.getValues(), itemComparator);
    }
    set = property.getSubProperties(OConstants.DIRECT_CLOSURE);
    if(set != null) {
      directSubProps.getValues().addAll(set);
      Collections.sort(directSubProps.getValues(), itemComparator);
    }
    set = property.getSubProperties(OConstants.TRANSITIVE_CLOSURE);
    if(set != null) {
      allSubProps.getValues().addAll(set);
      Collections.sort(allSubProps.getValues(), itemComparator);
    }
    
    set = property.getEquivalentPropertyAs();
    if(set != null) {
      equivalentProps.getValues().addAll(set);
      Collections.sort(equivalentProps.getValues(), itemComparator);
    }
    
    Set set1 = property.getDomain();
    if(set1 != null) {
      Iterator iterator = set1.iterator();
      while(iterator.hasNext()) {
        OResource resource = (OResource)iterator.next();
        domain.getValues().add(resource);
      }
      Collections.sort(domain.getValues(), itemComparator);
    }
    
    if(property instanceof DatatypeProperty) {
        range.getValues().add(
              ((DatatypeProperty)property).getDataType().getXmlSchemaURI());
      fireTableDataChanged();
      return;
    }
    
    Set set2 = property.getRange();
    if(set2 != null) {
      Iterator iterator = set2.iterator();
      while(iterator.hasNext()) {
        OResource resource = (OResource)iterator.next();
        range.getValues().add(resource.toString());
      }
      Collections.sort(range.getValues(), itemComparator);
    }
    fireTableDataChanged();
  }

  protected DetailsGroup directSuperProps;

  protected DetailsGroup allSuperProps;

  protected DetailsGroup directSubProps;

  protected DetailsGroup equivalentProps;
  
  protected DetailsGroup allSubProps;

  protected DetailsGroup domain;

  protected DetailsGroup range;

  protected DetailsGroup detailGroups[];

  protected OntologyItemComparator itemComparator;

  public static final int COLUMN_COUNT = 2;

  public static final int EXPANDED_COLUMN = 0;

  public static final int LABEL_COLUMN = 1;
}
