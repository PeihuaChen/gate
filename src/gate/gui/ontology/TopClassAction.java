/*
 *  TopClassAction.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: TopClassAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
import gate.gui.MainFrame;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * Action to create a new Top Class
 * 
 * @author niraj
 * 
 */
public class TopClassAction extends AbstractAction {
  private static final long serialVersionUID = 3258409543049359926L;

  public TopClassAction(String s, Icon icon) {
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
    labelPanel.add(new JLabel("Top Class Name :"));
    textFieldsPanel.add(className);
  }

  public void actionPerformed(ActionEvent actionevent) {
    nameSpace.setText(ontology.getDefaultNameSpace());
    int i = JOptionPane.showOptionDialog(MainFrame.getInstance(), panel,
            "New Top Class", 2, 3, null, new String[] {"OK", "Cancel"}, "OK");
    if(i == 0) {
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
      OClass oclassimpl = ontology.addOClass(new URI(nameSpace.getText()
              + className.getText(), false), OConstants.OWL_CLASS);
    }
  }

  public Ontology getOntology() {
    return ontology;
  }

  public void setOntology(Ontology ontology1) {
    ontology = ontology1;
  }

  protected JTextField nameSpace;

  protected JTextField className;

  protected JPanel labelPanel;

  protected JPanel textFieldsPanel;

  protected JPanel panel;

  protected Ontology ontology;
}
