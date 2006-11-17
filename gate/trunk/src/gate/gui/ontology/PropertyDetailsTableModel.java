package gate.gui.ontology;

import gate.creole.ontology.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;

public class PropertyDetailsTableModel extends AbstractTableModel {
  public PropertyDetailsTableModel() {
    directSuperProps = new DetailsGroup("Direct Super Properties", true, null);
    allSuperProps = new DetailsGroup("All Super Properties", true, null);
    directSubProps = new DetailsGroup("Direct Sub Properties", true, null);
    allSubProps = new DetailsGroup("All Sub Properties", true, null);
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
        directSubProps, allSubProps, domain, range});
    Property property = (Property)obj;
    Set set = property.getSuperProperties(Property.DIRECT_CLOSURE);
    directSuperProps.getValues().clear();
    if(set != null) {
      directSuperProps.getValues().addAll(set);
      Collections.sort(directSuperProps.getValues(), itemComparator);
    }
    set = property.getSuperProperties(Property.TRANSITIVE_CLOSURE);
    allSuperProps.getValues().clear();
    if(set != null) {
      allSuperProps.getValues().addAll(set);
      Collections.sort(allSuperProps.getValues(), itemComparator);
    }
    set = property.getSubProperties(Property.DIRECT_CLOSURE);
    directSubProps.getValues().clear();
    if(set != null) {
      directSubProps.getValues().addAll(set);
      Collections.sort(directSubProps.getValues(), itemComparator);
    }
    set = property.getSubProperties(Property.TRANSITIVE_CLOSURE);
    allSubProps.getValues().clear();
    if(set != null) {
      allSubProps.getValues().addAll(set);
      Collections.sort(allSubProps.getValues(), itemComparator);
    }
    Set set1 = property.getDomain();
    domain.getValues().clear();
    if(set1 != null) {
      OClass oclass;
      for(Iterator iterator = set1.iterator(); iterator.hasNext(); domain
              .getValues().add(oclass.getName()))
        oclass = (OClass)iterator.next();
      Collections.sort(domain.getValues(), itemComparator);
    }
    Set set2 = property.getRange();
    range.getValues().clear();
    if(set2 != null) {
      Iterator iterator1 = set2.iterator();
      while(iterator1.hasNext()) {
        if(property instanceof ObjectProperty) {
          OClass oclass1 = (OClass)iterator1.next();
          range.getValues().add(oclass1.getName());
        } else if(property instanceof DatatypeProperty) {
          Class class1 = (Class)iterator1.next();
          range.getValues().add(class1.getName());
        }
      }
    }
    fireTableDataChanged();
  }

  protected DetailsGroup directSuperProps;

  protected DetailsGroup allSuperProps;

  protected DetailsGroup directSubProps;

  protected DetailsGroup allSubProps;

  protected DetailsGroup domain;

  protected DetailsGroup range;

  protected DetailsGroup detailGroups[];

  protected OntologyItemComparator itemComparator;

  public static final int COLUMN_COUNT = 2;

  public static final int EXPANDED_COLUMN = 0;

  public static final int LABEL_COLUMN = 1;
}
