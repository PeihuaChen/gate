package gate.alignment.gui.actions.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import gate.Annotation;
import gate.Document;
import gate.alignment.AlignmentActionInitializationException;
import gate.alignment.AlignmentException;
import gate.alignment.gui.AlignmentAction;
import gate.alignment.gui.AlignmentEditor;
import gate.compound.CompoundDocument;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ShowFeaturesAction implements AlignmentAction {

  JTable featuresTable;
  FeaturesModel model;

  public ShowFeaturesAction() {
    model = new FeaturesModel();
    featuresTable = new JTable(model);
  }

  public void execute(AlignmentEditor editor, CompoundDocument document,
          Map<Document, Set<Annotation>> alignedAnnotations,
          Annotation clickedAnnotation) throws AlignmentException {
    model.setAnnotation(clickedAnnotation);
    
    JOptionPane.showOptionDialog(editor, featuresTable, "Features",
            JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null,
            new String[] {"OK"}, "OK");
  }

  public class FeaturesModel extends DefaultTableModel {
    Annotation toShow;

    ArrayList<String> features;

    ArrayList<String> values;

    public FeaturesModel() {
      super(new String[]{"Feature","Value"},0);      
    }
    
    public void setAnnotation(Annotation annot) {
      features = new ArrayList<String>();
      values = new ArrayList<String>();
      for(Object key : annot.getFeatures().keySet()) {
        features.add(key.toString());
        values.add(annot.getFeatures().get(key).toString());
      }
      super.fireTableDataChanged();
    }

    public Class getColumnClass(int column) {
      return String.class;
    }

    public int getRowCount() {
      return values == null ? 0 : values.size();
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch(column) {
        case 0:
          return "Feature";
        default:
          return "Value";
      }
    }

    public Object getValueAt(int row, int column) {
      switch(column) {
        case 0:
          return features.get(row);
        default:
          return values.get(row);
      }
    }

  }

  public String getCaption() {
    return "Features";
  }

  public Icon getIcon() {
    return null;
  }

  public String getIconPath() {
    return null;
  }

  public boolean invokeForAlignedAnnotation() {
    return true;
  }

  public boolean invokeForHighlightedUnalignedAnnotation() {
    return true;
  }

  public boolean invokeForUnhighlightedUnalignedAnnotation() {
    return true;
  }

  public void init(String[] args) throws AlignmentActionInitializationException {
    // no parameters
  }

  public void cleanup() {
    // do nothing
  }

}