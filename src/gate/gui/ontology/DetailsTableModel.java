package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.util.Err;
import java.util.*;
import javax.swing.table.AbstractTableModel;

public class DetailsTableModel extends AbstractTableModel {

	public DetailsTableModel() {
		ontologyMode = false;
		directSuperClasses = new DetailsGroup("Direct Super Classes", true,
				null);
		allSuperClasses = new DetailsGroup("All Super Classes", true, null);
		directSubClasses = new DetailsGroup("Direct Sub Classes", true, null);
		allSubClasses = new DetailsGroup("All Sub Classes", true, null);
		instances = new DetailsGroup("Instances", true, null);
		properties = new DetailsGroup("Properties", true, null);
		directTypes = new DetailsGroup("Direct Types", true, null);
		allTypes = new DetailsGroup("All Types", true, null);
		detailGroups = new DetailsGroup[0];
		itemComparator = new OntologyItemComparator();
	}


	public int getColumnCount() {
		return 2;
	}


	public int getRowCount() {
		int i = detailGroups.length;
		for (int j = 0; j < detailGroups.length; j++)
			if (detailGroups[j].isExpanded())
				i += detailGroups[j].getSize();

		return i;
	}


	public String getColumnName(int i) {
		switch (i) {
		case 0: 
			return "";

		case 1: 
			return "";
		}
		return "";
	}


	public Class getColumnClass(int i) {
		switch (i) {
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
		if (j == 0 && (obj1 instanceof DetailsGroup)) {
			DetailsGroup detailsgroup = (DetailsGroup) obj1;
			detailsgroup.setExpanded(((Boolean) obj).booleanValue());
		}
		fireTableDataChanged();
	}


	protected Object getItemForRow(int i) {
		int j = 0;
		for (int k = 0; j <= i; k++) {
			if (j == i)
				return detailGroups[k];
			int l = 1 + (detailGroups[k].isExpanded() ? detailGroups[k]
					.getSize() : 0);
			if (j + l > i)
				return detailGroups[k].getValueAt(i - j - 1);
			j += l;
		}

		return null;
	}


	public Object getValueAt(int i, int j) {
		Object obj = getItemForRow(i);
		switch (j) {
		case 0: 
			return (obj instanceof DetailsGroup) ? new Boolean(
					((DetailsGroup) obj).isExpanded()) : null;

		case 1: 
			return obj;
		}
		return null;
	}


	public void setItem(Object obj) {
		if (obj instanceof TClass) {
			detailGroups = ontologyMode ? (new DetailsGroup[] {
					directSuperClasses, allSuperClasses, directSubClasses,
					allSubClasses, properties, instances })
					: (new DetailsGroup[] { directSuperClasses,
							allSuperClasses, directSubClasses, allSubClasses });
			TClass tclass = (TClass) obj;
			Set set = tclass.getSuperClasses((byte) 0);
			directSuperClasses.getValues().clear();
			if (set != null) {
				directSuperClasses.getValues().addAll(set);
				Collections
						.sort(directSuperClasses.getValues(), itemComparator);
			}
			set = tclass.getSuperClasses((byte) 1);
			allSuperClasses.getValues().clear();
			if (set != null) {
				allSuperClasses.getValues().addAll(set);
				Collections.sort(allSuperClasses.getValues(), itemComparator);
			}
			set = tclass.getSubClasses((byte) 0);
			directSubClasses.getValues().clear();
			if (set != null) {
				directSubClasses.getValues().addAll(set);
				Collections.sort(directSubClasses.getValues(), itemComparator);
			}
			set = tclass.getSubClasses((byte) 1);
			allSubClasses.getValues().clear();
			if (set != null) {
				allSubClasses.getValues().addAll(set);
				Collections.sort(allSubClasses.getValues(), itemComparator);
			}
			if (ontologyMode) {
				properties.getValues().clear();
				Iterator iterator = (new HashSet(ontology
						.getPropertyDefinitions())).iterator();
				OInstanceImpl oinstanceimpl = new OInstanceImpl("", "",
						(OClass) tclass, ontology);
				do {
					if (!iterator.hasNext())
						break;
					Property property = (Property) iterator.next();
					if (property.isValidDomain(oinstanceimpl))
						properties.getValues().add(property);
				} while (true);
				Set set3 = tclass.getSetPropertiesNames();
				if (set3 != null) {
					Iterator iterator3 = set3.iterator();
					do {
						if (!iterator3.hasNext())
							break;
						String s1 = (String) iterator3.next();
						List list1 = tclass.getPropertyValues(s1);
						if (list1 != null) {
							Iterator iterator5 = list1.iterator();
							while (iterator5.hasNext()) {
								StringBuffer stringbuffer1 = new StringBuffer(
										s1);
								stringbuffer1.append("(");
								Object obj2 = iterator5.next();
								if (obj2 != null)
									stringbuffer1
											.append((obj2 instanceof OInstance) ? ((OInstance) obj2)
													.getName()
													: obj2.toString());
								stringbuffer1.append(")");
								properties.getValues().add(
										stringbuffer1.toString());
							}
						}
					} while (true);
				}
				Collections.sort(properties.getValues(), itemComparator);
				if (ontologyMode) {
					Set set4 = ontology.getDirectInstances((OClass) tclass);
					instances.getValues().clear();
					if (set4 != null) {
						instances.getValues().addAll(set4);
						Collections.sort(instances.getValues(), itemComparator);
					}
				}
			}
		} else if (obj instanceof OInstance) {
			OInstance oinstance = (OInstance) obj;
			detailGroups = (new DetailsGroup[] { directTypes, allTypes,
					properties });
			Set set1 = oinstance.getOClasses();
			directTypes.getValues().clear();
			if (set1 != null) {
				directTypes.getValues().addAll(set1);
				Collections.sort(directTypes.getValues(), itemComparator);
			}
			HashSet hashset = new HashSet();
			set1 = oinstance.getOClasses();
			hashset.addAll(set1);
			OClass oclass;
			for (Iterator iterator1 = set1.iterator(); iterator1.hasNext(); hashset
					.addAll(oclass.getSuperClasses((byte) 1)))
				oclass = (OClass) iterator1.next();

			allTypes.getValues().clear();
			if (hashset != null) {
				allTypes.getValues().addAll(hashset);
				Collections.sort(allTypes.getValues(), itemComparator);
			}
			properties.getValues().clear();
			Set set2 = oinstance.getSetPropertiesNames();
			if (set2 != null) {
				Iterator iterator2 = set2.iterator();
				do {
					if (!iterator2.hasNext())
						break;
					String s = (String) iterator2.next();
					List list = oinstance.getPropertyValues(s);
					if (list != null) {
						Iterator iterator4 = list.iterator();
						while (iterator4.hasNext()) {
							StringBuffer stringbuffer = new StringBuffer(s);
							stringbuffer.append("(");
							Object obj1 = iterator4.next();
							if (obj1 != null)
								stringbuffer
										.append((obj1 instanceof OInstance) ? ((OInstance) obj1)
												.getName()
												: obj1.toString());
							stringbuffer.append(")");
							properties.getValues().add(stringbuffer.toString());
						}
					}
				} while (true);
				Collections.sort(properties.getValues());
			}
		}
		fireTableDataChanged();
	}


	protected boolean mightPropertyApplyToClass(Property property, OClass oclass) {
		HashSet hashset = new HashSet(property.getDomain());
		for (Iterator iterator = property.getSuperProperties((byte) 1)
				.iterator(); iterator.hasNext();) {
			Property property1 = (Property) iterator.next();
			if (property1 == null)
				Err.prln((new StringBuilder()).append("Null superProp for ")
						.append(property.getName()).toString());
			if (property1.getDomain() == null)
				Err.prln((new StringBuilder()).append("Null domain for ")
						.append(property1.getName()).toString());
			else
				hashset.addAll(property1.getDomain());
		}

		return hashset.contains(oclass);
	}


	public Ontology getOntology() {
		return ontology;
	}


	public void setOntology(Ontology ontology1) {
		ontology = ontology1;
	}


	public boolean isOntologyMode() {
		return ontologyMode;
	}


	public void setOntologyMode(boolean flag) {
		ontologyMode = flag;
	}

	protected DetailsGroup directSuperClasses;

	protected DetailsGroup allSuperClasses;

	protected DetailsGroup directSubClasses;

	protected DetailsGroup allSubClasses;

	protected DetailsGroup instances;

	protected DetailsGroup properties;

	protected DetailsGroup directTypes;

	protected DetailsGroup allTypes;

	protected DetailsGroup detailGroups[];

	protected Ontology ontology;

	protected boolean ontologyMode;

	public static final int COLUMN_COUNT = 2;

	public static final int EXPANDED_COLUMN = 0;

	public static final int LABEL_COLUMN = 1;

	protected OntologyItemComparator itemComparator;
}
