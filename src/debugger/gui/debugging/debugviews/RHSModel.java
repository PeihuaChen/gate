package debugger.gui.debugging.debugviews;

import debugger.resources.ResourcesFactory;
import gate.AnnotationSet;
import gate.Document;
import gate.LanguageResource;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

//TODO: move this functionality to actions

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko, Vladimir Karasev
 */

public class RHSModel implements TableModel {

    public int getRowCount() {
//        if (DebugController.getInstance().getRuleController().getCreatedAnnotations() == null) return 0;
//        return DebugController.getInstance().getRuleController().getCreatedAnnotations().size();
        debugger.resources.lr.LrModel lrModel = ResourcesFactory.getCurrentLrModel();
        if (null == lrModel) {
            return 0;
        }
        LanguageResource lr = lrModel.getLr();
        if (lr instanceof Document) {
            if (null == ResourcesFactory.getCurrentRuleModel() ||
                    ResourcesFactory.getCurrentRuleModel().getAnnotationHistory() == null) {
                return 0;
            }
            AnnotationSet annSet = ResourcesFactory.getCurrentRuleModel().getAnnotationHistory().getAnnotationSet((Document) lr);
            if (null != annSet) {
                return annSet.size();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int columnIndex) {
//        if (columnIndex == 0) return "Exec Count";
//        if (columnIndex == 1) return "Rule Name";
//        if (columnIndex == 2) return "Annotation Type";
//        if (columnIndex == 3) return "Features";
        if (columnIndex == 0) return "Ann ID";
        if (columnIndex == 1) return "Ann Type";
        if (columnIndex == 2) return "Features";
        return null;
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        LanguageResource lr = ResourcesFactory.getCurrentLrModel().getLr();
        if (lr instanceof Document) {
            if (ResourcesFactory.getCurrentRuleModel().getAnnotationHistory() == null) return null;
            gate.Annotation ann = (gate.Annotation) ResourcesFactory.getCurrentRuleModel().getAnnotationHistory().getAnnotationSet((Document) lr).toArray()[rowIndex];
            if (null == ann) {
                return null;
            }
            if (columnIndex == 0) return ann.getId();
            if (columnIndex == 1) return ann.getType();
            if (columnIndex == 2) return ann.getFeatures().toString();
            return null;
        } else {
            return null;
        }
//        gate.Annotation ann = (gate.Annotation) DebugController.getInstance().getRuleController().getCreatedAnnotations().toArray()[rowIndex];
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }
}
