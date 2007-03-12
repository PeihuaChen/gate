/*
 *  InstanceAction.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: InstanceAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.gui.MainFrame;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Action to create a new Instance in the ontology
 * @author niraj
 *
 */
public class InstanceAction extends AbstractAction implements
                                                  TreeNodeSelectionListener {
  private static final long serialVersionUID = 3257844402729529651L;

  public InstanceAction(String caption, Icon icon) {
    super(caption, icon);
    nameSpace = new JTextField(20);
    instanceName = new JTextField(20);
    labelPanel = new JPanel(new GridLayout(2, 1));
    textFieldsPanel = new JPanel(new GridLayout(2, 1));
    panel = new JPanel(new FlowLayout(0));
    panel.add(labelPanel);
    panel.add(textFieldsPanel);
    labelPanel.add(new JLabel("Name Space :"));
    textFieldsPanel.add(nameSpace);
    labelPanel.add(new JLabel("Instance Name :"));
    textFieldsPanel.add(instanceName);
  }

  public void actionPerformed(ActionEvent actionevent) {
    ArrayList<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>(this.selectedNodes);
    nameSpace.setText(ontology.getDefaultNameSpace());
    int j = JOptionPane.showOptionDialog(MainFrame.getInstance(), panel, "New Instance: ", 2, 3,
            null, new String[]{"OK", "Cancel"}, "OK");
    if(j == 0) {
      String s = nameSpace.getText();
      if(!Utils.isValidNameSpace(s)) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), (new StringBuilder()).append(
                "Invalid NameSpace:").append(s).append(
                "\n example: http://gate.ac.uk/example#").toString());
        return;
      }
      if(!Utils.isValidOntologyResourceName(instanceName.getText())) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid Instance Name");
        return;
      }
      
      if(ontology.getOResourceFromMap(nameSpace.getText()+instanceName.getText()) != null) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), (new StringBuilder()).append(
                "An instance with name \"").append(nameSpace.getText()+instanceName.getText()).append(
                "\" already exists").toString());
        return;
      }

      for(int i = 0; i < selectedNodes.size(); i++) {
        Object obj = selectedNodes.get(i).getUserObject();
        if(obj instanceof OClass) {
          OInstance instance = ontology.addOInstance(new URI(nameSpace.getText()+instanceName.getText(), false),
                  (OClass)obj);
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

  public void selectionChanged(ArrayList<DefaultMutableTreeNode> arraylist) {
    selectedNodes = arraylist;
  }

  JTextField nameSpace;

  JTextField instanceName;

  JPanel labelPanel;

  JPanel textFieldsPanel;

  JPanel panel;

  Ontology ontology;

  ArrayList<DefaultMutableTreeNode> selectedNodes;
}