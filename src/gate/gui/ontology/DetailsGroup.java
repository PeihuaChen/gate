package gate.gui.ontology;

import java.util.*;

public class DetailsGroup {
  public DetailsGroup(String groupName, boolean flag, Collection collection) {
    name = groupName;
    expanded = flag;
    values = collection != null
            ? ((List)(new ArrayList(collection)))
            : ((List)(new ArrayList()));
  }

  public String getName() {
    return name;
  }

  public boolean isExpanded() {
    return expanded;
  }

  public void setExpanded(boolean flag) {
    expanded = flag;
  }

  public void setName(String s) {
    name = s;
  }

  public int getSize() {
    return values.size();
  }

  public Object getValueAt(int i) {
    return values.get(i);
  }

  public List getValues() {
    return values;
  }

  public void setValues(List list) {
    values = list;
  }

  boolean expanded;

  String name;

  List values;
}
