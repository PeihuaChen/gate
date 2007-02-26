package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.gui.MainFrame;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class SubClassAction extends AbstractAction implements
                                                  TreeNodeSelectionListener {
  public SubClassAction(String s, Icon icon) {
    super(s, icon);
    nameSpace = new JTextField(20);
    className = new JTextField(20);
    comment = new JTextField(20);
    labelPanel = new JPanel(new GridLayout(3, 1));
    textFieldsPanel = new JPanel(new GridLayout(3, 1));
    panel = new JPanel(new FlowLayout(0));
    panel.add(labelPanel);
    panel.add(textFieldsPanel);
    labelPanel.add(new JLabel("Name Space :"));
    textFieldsPanel.add(nameSpace);
    labelPanel.add(new JLabel("Sub Class Name :"));
    textFieldsPanel.add(className);
    labelPanel.add(new JLabel("Comment :"));
    textFieldsPanel.add(comment);
  }

  public void actionPerformed(ActionEvent actionevent) {
    ArrayList arraylist = new ArrayList();
    for(int i = 0; i < selectedNodes.size(); i++) {
      Object obj = ((DefaultMutableTreeNode)selectedNodes.get(i))
              .getUserObject();
      if(obj instanceof TClass) arraylist.add(obj);
    }
    nameSpace.setText(ontology.getDefaultNameSpace());
    int j = JOptionPane.showOptionDialog(MainFrame.getInstance(), panel, "Sub Class Action: ", 2,
            3, null, new String[]{"OK", "Cancel"}, "OK");
    if(j == 0) {
      String s = nameSpace.getText();
      if(!Utils.isValidNameSpace(s)) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), (new StringBuilder()).append(
                "Invalid NameSpace:").append(s).append(
                "\n example: http://gate.ac.uk/example#").toString());
        return;
      }
      if(!Utils.isValidOntologyResourceName(className.getText())) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid Classname");
        return;
      }
      if(ontology.getClassByName(className.getText()) != null) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), (new StringBuilder()).append(
                "Class :").append(className.getText())
                .append(" already exists").toString());
        return;
      }
      
      OClass oclassimpl = ontology.createClass(className.getText(), comment.getText());
      for(int k = 0; k < arraylist.size(); k++) {
        oclassimpl.addSuperClass((OClass)arraylist.get(k));
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

  public void selectionChanged(ArrayList arraylist) {
    selectedNodes = arraylist;
  }

  protected JTextField nameSpace;

  protected JTextField className;

  protected JTextField comment;

  protected JPanel labelPanel;

  protected JPanel textFieldsPanel;

  protected JPanel panel;

  protected ArrayList selectedNodes;

  protected Ontology ontology;
}
