package debugger.gui.debugging.debugviews;

import debugger.resources.ResourcesFactory;
import debugger.resources.pr.RuleModel;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;

//TODO: move this functionality to actions

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko, Vladimir Karasev
 */

public class LHSModel implements TableModel {

    public int getRowCount() {
        if (getMatchedRuleTable(ResourcesFactory.getCurrentRuleModel()) == null) return 0;
        return getMatchedRuleTable(ResourcesFactory.getCurrentRuleModel()).size();
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) return "Rule LHS";
        if (columnIndex == 1) return "Matching";
        return null;
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        if (columnIndex == 0) result = getMatchedRuleTable(ResourcesFactory.getCurrentRuleModel()).get(rowIndex);
        if (columnIndex == 1) result = getMatchedText(ResourcesFactory.getCurrentRuleModel()).get(rowIndex);
        return result;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }

    private ArrayList getMatchedRuleTable(RuleModel ruleModel) {
        if (ruleModel == null) return null;
        ArrayList al = ruleModel.getMatshedRuleTable();
        for (int i = 0; i < al.size(); i++) {
            String s = (String) al.get(i);
            if (s.endsWith("*")) {
                al.remove(i);
                s = s.substring(0, s.indexOf("*"));
                al.add(i, s);
            }
        }
        return al;
    }

    private ArrayList getMatchedText(RuleModel ruleModel) {
        if (ruleModel == null) return null;
        return ruleModel.getMatchedText();
    }
}
