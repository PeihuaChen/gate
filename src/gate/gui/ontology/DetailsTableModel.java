/*
 *  DetailsTableModel.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: DetailsTableModel.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;

import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 * A DataModel that is created when a node is selected in the ontology
 * tree. It contains information such as direct/all sub/super classes,
 * equivalent classes, instances, properties/ property values and so on.
 * The information from this model is then shown in the right hand side
 * panel of the ontology editor.
 * 
 * @author niraj
 * 
 */
public class DetailsTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 3834870286880618035L;

  public DetailsTableModel() {
    ontologyMode = false;
    resourceInfo = new DetailsGroup("Resource Information",true,null);
    directSuperClasses = new DetailsGroup("Direct Super Classes", true, null);
    allSuperClasses = new DetailsGroup("All Super Classes", true, null);
    directSubClasses = new DetailsGroup("Direct Sub Classes", true, null);
    allSubClasses = new DetailsGroup("All Sub Classes", true, null);
    equivalentClasses = new DetailsGroup("Equivalent Classes", true, null);
    sameAsInstances = new DetailsGroup("Same Instances", true, null);
    instances = new DetailsGroup("Instances", true, null);
    propertyTypes = new DetailsGroup("Property Types", true, null);
    propertyValues = new DetailsGroup("Property Values", true, null);
    directTypes = new DetailsGroup("Direct Types", true, null);
    allTypes = new DetailsGroup("All Types", true, null);
    detailGroups = new DetailsGroup[0];
    itemComparator = new OntologyItemComparator();
  }

  public int getColumnCount() {
    return COLUMN_COUNT;
  }

  public int getRowCount() {
    int i = detailGroups.length;
    for(int j = 0; j < detailGroups.length; j++)
      if(detailGroups[j].isExpanded()) {
        i += detailGroups[j].getSize();
      }
    return i;
  }

  public String getColumnName(int i) {
    switch(i) {
      case EXPANDED_COLUMN:
        return "";
      case LABEL_COLUMN:
        return "";
      case VALUE_COLUMN:
        return "Value";
      case DELETE_COLUMN:
        return "Delete";
    }
    return "";
  }

  public Class getColumnClass(int i) {
    switch(i) {
      case EXPANDED_COLUMN:
        return Boolean.class;
      case LABEL_COLUMN:
        return Object.class;
      case VALUE_COLUMN:
        return Object.class;
      case DELETE_COLUMN:
        return Object.class;
      
    }
    return Object.class;
  }

  public boolean isCellEditable(int i, int j) {
    Object obj = getItemForRow(i);
    //if(obj instanceof PropertyValue && j==2) return true;
    return false;
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
    switch(j) {
      case EXPANDED_COLUMN:
        return (obj instanceof DetailsGroup) ? new Boolean(((DetailsGroup)obj)
                .isExpanded()) : null;
      case LABEL_COLUMN:
        return obj;
      case VALUE_COLUMN:
        return obj;
      case DELETE_COLUMN:
        return obj;
    }
    return null;
  }

  public void setItem(Object obj) {
    if(obj instanceof OClass) {
      detailGroups = new DetailsGroup[] {resourceInfo, directSuperClasses, allSuperClasses,
          directSubClasses, allSubClasses, equivalentClasses, propertyTypes,
          propertyValues, instances};
      OClass tclass = (OClass)obj;
      resourceInfo.getValues().clear();
      if(tclass instanceof Restriction) {
          resourceInfo.getValues().addAll(Utils.getDetailsToAdd(tclass));
      } else {
        resourceInfo.getValues().add(tclass);
        resourceInfo.getValues().add(new KeyValuePair(tclass, "URI", tclass.getURI().toString(), false));
        resourceInfo.getValues().add(new KeyValuePair(tclass, "TYPE", "Ontology Class", false));
      }
      
      Set<OClass> set = tclass.getSuperClasses(OConstants.DIRECT_CLOSURE);
      directSuperClasses.getValues().clear();
      directSuperClasses.getValues().addAll(set);
      Collections.sort(directSuperClasses.getValues(), itemComparator);
      
//      if(set != null) {
//        for(OClass aClass : set) {
//          directSuperClasses.getValues().addAll(Utils.getDetailsToAdd(aClass));
//        }
//      }

      Set<OClass> set1 = tclass.getSuperClasses(OConstants.TRANSITIVE_CLOSURE);
      allSuperClasses.getValues().clear();
      allSuperClasses.getValues().addAll(set1);
      Collections.sort(allSuperClasses.getValues(), itemComparator);
      
//      if(set1 != null) {
//        for(OClass aClass : set1) {
//          allSuperClasses.getValues().addAll(Utils.getDetailsToAdd(aClass));
//        }
//      }

      Set<OClass> set2 = tclass.getSubClasses(OConstants.DIRECT_CLOSURE);
      directSubClasses.getValues().clear();
      directSubClasses.getValues().addAll(set2);
      Collections.sort(directSubClasses.getValues(), itemComparator);
      
//      if(set2 != null) {
//        for(OClass aClass : set2) {
//          directSubClasses.getValues().addAll(Utils.getDetailsToAdd(aClass));
//        }
//      }
      
      
      Set<OClass> set3 = tclass.getSubClasses(OConstants.TRANSITIVE_CLOSURE);
      allSubClasses.getValues().clear();
      allSubClasses.getValues().addAll(set3);
      Collections.sort(allSubClasses.getValues(), itemComparator);
      
//      if(set3 != null) {
//        for(OClass aClass : set3) {
//          allSubClasses.getValues().addAll(Utils.getDetailsToAdd(aClass));
//        }
//      }

      Set<OClass> set4 = tclass.getEquivalentClasses();
      equivalentClasses.getValues().clear();
      equivalentClasses.getValues().addAll(set4);
      Collections.sort(equivalentClasses.getValues(), itemComparator);
      
//      if(set4 != null) {
//        for(OClass aClass : set4) {
//          equivalentClasses.getValues().addAll(Utils.getDetailsToAdd(aClass));
//        }
//      }

      propertyTypes.getValues().clear();
      Set<RDFProperty> dprops = tclass.getPropertiesWithResourceAsDomain();
      propertyTypes.getValues().addAll(dprops);
      Collections.sort(propertyTypes.getValues(), itemComparator);
      
//      for(RDFProperty prop : dprops) {
//        propertyTypes.getValues().addAll(Utils.getDetailsToAdd(prop));
//      }

      propertyValues.getValues().clear();
      Set<AnnotationProperty> props = tclass.getSetAnnotationProperties();
      if(props != null) {
        Iterator<AnnotationProperty> apIter = props.iterator();
        while(apIter.hasNext()) {
          AnnotationProperty ap = apIter.next();
          List<Literal> literals = tclass.getAnnotationPropertyValues(ap);
          for(int i = 0; i < literals.size(); i++) {
            PropertyValue pv = new PropertyValue(ap, literals.get(i));
            propertyValues.getValues().add(pv);
          }
        }
      }

      Set<OInstance> set5 = ontology.getOInstances(tclass,
              OConstants.DIRECT_CLOSURE);
      instances.getValues().clear();
      if(set5 != null) {
        instances.getValues().addAll(set5);
        Collections.sort(instances.getValues(), itemComparator);
      }
    }
    else if(obj instanceof OInstance) {
      OInstance oinstance = (OInstance)obj;
      detailGroups = (new DetailsGroup[] {resourceInfo, directTypes, allTypes,
          sameAsInstances, propertyTypes, propertyValues});

      resourceInfo.getValues().clear();
      resourceInfo.getValues().add(oinstance);
      resourceInfo.getValues().add(new KeyValuePair(oinstance, "URI", oinstance.getURI().toString(), false));
      resourceInfo.getValues().add(new KeyValuePair(oinstance, "TYPE", "Ontology Instance", false));
      
      Set<OClass> set1 = oinstance.getOClasses(OConstants.DIRECT_CLOSURE);
      directTypes.getValues().clear();
      if(set1 != null) {
        for(OClass aClass : set1) {
          directTypes.getValues().addAll(Utils.getDetailsToAdd(aClass));
        }
      }

      Set<OClass> set2 = oinstance.getOClasses(OConstants.TRANSITIVE_CLOSURE);
      allTypes.getValues().clear();
      if(set2 != null) {
        for(OClass aClass : set2) {
          allTypes.getValues().addAll(Utils.getDetailsToAdd(aClass));
        }
      }

      Set<OInstance> set3 = oinstance.getSameInstance();
      sameAsInstances.getValues().clear();
      if(set3 != null) {
        sameAsInstances.getValues().addAll(set3);
        Collections.sort(sameAsInstances.getValues(), itemComparator);
      }

      propertyTypes.getValues().clear();
      Set<RDFProperty> dprops = oinstance.getPropertiesWithResourceAsDomain();
      propertyTypes.getValues().addAll(dprops);
//      for(RDFProperty prop : dprops) {
//        propertyTypes.getValues().addAll(Utils.getDetailsToAdd(prop));
//      }
      
      propertyValues.getValues().clear();
      Set<AnnotationProperty> apProps = oinstance.getSetAnnotationProperties();
      Set<DatatypeProperty> dtProps = oinstance.getSetDatatypeProperties();
      Set<ObjectProperty> obProps = oinstance.getSetObjectProperties();
      Set<RDFProperty> rdfProp = oinstance.getSetRDFProperties();

      for(AnnotationProperty ap : apProps) {
        List<Literal> literals = oinstance.getAnnotationPropertyValues(ap);
        for(int i = 0; i < literals.size(); i++) {
          PropertyValue pv = new PropertyValue(ap, literals.get(i));
          propertyValues.getValues().add(pv);
        }
      }

      for(DatatypeProperty dt : dtProps) {
        List<Literal> literals = oinstance.getDatatypePropertyValues(dt);
        for(int i = 0; i < literals.size(); i++) {
          PropertyValue pv = new PropertyValue(dt, literals.get(i));
          propertyValues.getValues().add(pv);
        }
      }

      for(ObjectProperty ob : obProps) {
        List<OInstance> oinstances = oinstance.getObjectPropertyValues(ob);
        for(int i = 0; i < oinstances.size(); i++) {
          PropertyValue pv = new PropertyValue(ob, oinstances.get(i));
          propertyValues.getValues().add(pv);
        }
      }

      for(RDFProperty rd : rdfProp) {
        List<OResource> oinstances = oinstance.getRDFPropertyValues(rd);
        for(int i = 0; i < oinstances.size(); i++) {
          PropertyValue pv = new PropertyValue(rd, oinstances.get(i));
          propertyValues.getValues().add(pv);
        }
      }
    }
    fireTableDataChanged();
  }

  public Ontology getOntology() {
    return ontology;
  }

  public void setOntology(Ontology ontology1) {
    ontology = ontology1;
  }

  protected DetailsGroup resourceInfo;
  
  protected DetailsGroup directSuperClasses;
  
  
  protected DetailsGroup allSuperClasses;

  protected DetailsGroup directSubClasses;

  protected DetailsGroup allSubClasses;

  protected DetailsGroup equivalentClasses;

  protected DetailsGroup sameAsInstances;

  protected DetailsGroup instances;

  protected DetailsGroup propertyTypes;

  protected DetailsGroup propertyValues;

  protected DetailsGroup directTypes;

  protected DetailsGroup allTypes;

  protected DetailsGroup detailGroups[];

  protected Ontology ontology;

  protected boolean ontologyMode;

  public static final int COLUMN_COUNT = 4;

  public static final int EXPANDED_COLUMN = 0;

  public static final int LABEL_COLUMN = 1;

  public static final int VALUE_COLUMN = 2;
  
  public static final int DELETE_COLUMN = 3;

  protected OntologyItemComparator itemComparator;
}
