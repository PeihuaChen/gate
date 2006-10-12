package gate.gui.ontology;

import gate.creole.ontology.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class InstanceAction extends AbstractAction implements
                                                  TreeNodeSelectionListener {
  public InstanceAction(String caption, Icon icon) {
    super(caption, icon);
    nameSpace = new JTextField(20);
    instanceName = new JTextField(20);
    comment = new JTextField(20);
    labelPanel = new JPanel(new GridLayout(3, 1));
    textFieldsPanel = new JPanel(new GridLayout(3, 1));
    panel = new JPanel(new FlowLayout(0));
    panel.add(labelPanel);
    panel.add(textFieldsPanel);
    labelPanel.add(new JLabel("Name Space :"));
    textFieldsPanel.add(nameSpace);
    labelPanel.add(new JLabel("Instance Name :"));
    textFieldsPanel.add(instanceName);
    labelPanel.add(new JLabel("Comment :"));
    textFieldsPanel.add(comment);
  }

  public void actionPerformed(ActionEvent actionevent) {
    nameSpace.setText(ontology.getDefaultNameSpace());
    int j = JOptionPane.showOptionDialog(null, panel, "New Instance: ", 2, 3,
            null, new String[]{"OK", "Cancel"}, "OK");
    if(j == 0) {
      String s = nameSpace.getText();
      if(!Utils.isValidNameSpace(s)) {
        JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                "Invalid NameSpace:").append(s).append(
                "\n example: http://gate.ac.uk/example#").toString());
        return;
      }
      if(!Utils.isValidOntologyResourceName(instanceName.getText())) {
        JOptionPane.showMessageDialog(null, "Invalid Instance Name");
        return;
      }
      if(ontology.getInstanceByName(instanceName.getText()) != null) {
        JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                "Instance :").append(instanceName.getText()).append(
                " already exists").toString());
        return;
      }

      for(int i = 0; i < selectedNodes.size(); i++) {
        Object obj = ((DefaultMutableTreeNode)selectedNodes.get(i))
                .getUserObject();
        if(obj instanceof OClass) {
          OInstance instance = ontology.addInstance(instanceName.getText(),
                  (OClass)obj);
          instance.setComment(comment.getText());
        }
      }
    }
  }

  public Ontology getOntology() {
    return ontology;
  }

  public void setOntology(Ontology ontology1) {
    ontology = ontology1;
  }

  public void selectionChanged(ArrayList arraylist) {
    selectedNodes = arraylist;
  }

  JTextField nameSpace;

  JTextField instanceName;

  JTextField comment;

  JPanel labelPanel;

  JPanel textFieldsPanel;

  JPanel panel;

  Ontology ontology;

  ArrayList selectedNodes;
}
