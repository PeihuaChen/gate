/*
 *  AnnotationPropertyAction.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: AnnotationPropertyAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Action to create a new annotation property.
 * 
 * @author niraj
 * 
 */
public class AnnotationPropertyAction extends AbstractAction implements
                                                            TreeNodeSelectionListener {
  private static final long serialVersionUID = 3546358452780544048L;

  /**
   * Constructor
   * 
   * @param s - Label assigned to the Button
   * @param icon - Icon assigned to the Button
   */
  public AnnotationPropertyAction(String s, Icon icon) {
    super(s, icon);
    nameSpace = new JTextField(20);
    nsPanel = new JPanel(new FlowLayout(0));
    nsPanel.add(new JLabel("Name Space:"));
    nsPanel.add(nameSpace);
    propertyName = new JTextField(20);
    propertyPanel = new JPanel(new FlowLayout(0));
    propertyPanel.add(new JLabel("Property Name:"));
    propertyPanel.add(propertyName);
    panel = new JPanel(new GridLayout(2, 1));
    ((GridLayout)panel.getLayout()).setVgap(0);
    panel.add(propertyPanel);
    panel.add(nsPanel);
  }

  /**
   * This is invovked whenever user click on the button
   */
  public void actionPerformed(ActionEvent actionevent) {
    ArrayList<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>(
            this.selectedNodes);
    nameSpace.setText(ontology.getDefaultNameSpace());
    int i = JOptionPane.showOptionDialog(null, panel,
            "New Annotation Property", 2, 3, null,
            new String[] {"OK", "Cancel"}, "OK");
    if(i == 0) {
      String s = nameSpace.getText();
      if(!Utils.isValidNameSpace(s)) {
        JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                "Invalid NameSpace:").append(s).append(
                "\n example: http://gate.ac.uk/example#").toString());
        return;
      }
      if(!Utils.isValidOntologyResourceName(propertyName.getText())) {
        JOptionPane.showMessageDialog(null, "Invalid Annotation Property Name");
        return;
      }
      if(ontology.getOResourceFromMap(nameSpace.getText()
              + propertyName.getText()) != null) {
        JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                "A Resource with name \"").append(
                nameSpace.getText() + propertyName.getText()).append(
                "\" already exists").toString());
        return;
      }
      ontology.addAnnotationProperty(new URI(nameSpace.getText()
              + propertyName.getText(), false));
    }
  }

  /**
   * Returns the associated ontology
   * 
   * @return
   */
  public Ontology getOntology() {
    return ontology;
  }

  /**
   * Specifies the ontology that should be used to add/remove resource
   * to/from.
   * 
   * @param ontology1
   */
  public void setOntology(Ontology ontology1) {
    ontology = ontology1;
  }

  /**
   * This method is invoked by the ontology editor whenever the
   * selection in ontology tree changes to reset the selected nodes.
   */
  public void selectionChanged(ArrayList<DefaultMutableTreeNode> arraylist) {
    selectedNodes = arraylist;
  }

  protected JPanel nsPanel;

  protected JPanel propertyPanel;

  protected JPanel propPanel;

  protected JPanel symTransPanel;

  protected JPanel commentPanel;

  protected JPanel panel;

  protected JTextField nameSpace;

  protected JTextField propertyName;

  protected ArrayList<DefaultMutableTreeNode> selectedNodes;

  protected Ontology ontology;
}
