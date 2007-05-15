/*
 *  SubClassAction.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: SubClassAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
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
 * Action to create a new subclass.
 * 
 * @author niraj
 * 
 */
public class SubClassAction extends AbstractAction implements
                                                  TreeNodeSelectionListener {
  private static final long serialVersionUID = 3258409543049359926L;

  public SubClassAction(String s, Icon icon) {
    super(s, icon);
    nameSpace = new JTextField(20);
    className = new JTextField(20);
    labelPanel = new JPanel(new GridLayout(2, 1));
    textFieldsPanel = new JPanel(new GridLayout(2, 1));
    panel = new JPanel(new FlowLayout(0));
    panel.add(labelPanel);
    panel.add(textFieldsPanel);
    labelPanel.add(new JLabel("Name Space :"));
    textFieldsPanel.add(nameSpace);
    labelPanel.add(new JLabel("Sub Class Name :"));
    textFieldsPanel.add(className);
  }

  public void actionPerformed(ActionEvent actionevent) {
    ArrayList<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>(
            this.selectedNodes);
    if(selectedNodes.size() == 0) {
      JOptionPane
              .showMessageDialog(MainFrame.getInstance(),
                      "Please select a class for which you want to create a new subclass");
      return;
    }
    OResource selectedNode = ((OResourceNode)selectedNodes.get(0).getUserObject()).getResource();
    nameSpace.setText(selectedNode.getURI().getNameSpace());
    ArrayList<OClass> arraylist = new ArrayList<OClass>();
    for(int i = 0; i < selectedNodes.size(); i++) {
      Object obj = ((OResourceNode)((DefaultMutableTreeNode)selectedNodes.get(i)).getUserObject()).getResource();
      if(obj instanceof OClass) arraylist.add((OClass)obj);
    }

    int j = JOptionPane.showOptionDialog(MainFrame.getInstance(), panel,
            "New Sub Class: ", 2, 3, null, new String[] {"OK", "Cancel"}, "OK");
    if(j == 0) {
      String s = nameSpace.getText();
      if(!Utils.isValidNameSpace(s)) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                (new StringBuilder()).append("Invalid NameSpace:").append(s)
                        .append("\n example: http://gate.ac.uk/example#")
                        .toString());
        return;
      }
      if(!Utils.isValidOntologyResourceName(className.getText())) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                "Invalid Classname");
        return;
      }

      if(ontology.getOResourceFromMap(s + className.getText()) != null) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                (new StringBuilder()).append("Class :").append(
                        className.getText()).append(" already exists")
                        .toString());
        return;
      }

      OClass oclassimpl = ontology.addOClass(new URI(s + className.getText(),
              false));
      for(int k = 0; k < arraylist.size(); k++) {
        ((OClass)arraylist.get(k)).addSubClass(oclassimpl);
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

  protected JTextField nameSpace;

  protected JTextField className;

  protected JPanel labelPanel;

  protected JPanel textFieldsPanel;

  protected JPanel panel;

  protected ArrayList<DefaultMutableTreeNode> selectedNodes;

  protected Ontology ontology;
}
