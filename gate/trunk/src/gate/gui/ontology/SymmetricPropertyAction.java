/*
 *  SymmetricPropertyAction.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: SymmetricPropertyAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.creole.ontology.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Action to create a new symmetric property in the ontology.
 * 
 * @author niraj
 * 
 */
public class SymmetricPropertyAction extends AbstractAction implements
                                                           TreeNodeSelectionListener {
  private static final long serialVersionUID = 3257286915891017008L;

  public SymmetricPropertyAction(String s, Icon icon) {
    super(s, icon);
    nameSpace = new JTextField(20);
    nsPanel = new JPanel(new FlowLayout(0));
    nsPanel.add(new JLabel("Name Space:"));
    nsPanel.add(nameSpace);
    propertyName = new JTextField(20);
    domainB = new JButton("Domain And Range");
    domainAction = new ValuesSelectionAction();
    domainB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        String as[] = new String[ontologyClassesURIs.size()];
        for(int i = 0; i < as.length; i++)
          as[i] = ((String)ontologyClassesURIs.get(i));
        ArrayList<String> arraylist = new ArrayList<String>();
        for(int j = 0; j < selectedNodes.size(); j++) {
          DefaultMutableTreeNode defaultmutabletreenode = selectedNodes.get(j);
          if(defaultmutabletreenode.getUserObject() instanceof OClass)
            arraylist.add(((OClass)defaultmutabletreenode.getUserObject())
                    .getURI().toString());
        }
        String as1[] = new String[arraylist.size()];
        for(int k = 0; k < as1.length; k++)
          as1[k] = arraylist.get(k);
        domainAction.showGUI("Domain And Range", as, as1, false);
      }

      final SymmetricPropertyAction this$0;
      {
        this$0 = SymmetricPropertyAction.this;
      }
    });
    propertyPanel = new JPanel(new FlowLayout(0));
    propertyPanel.add(new JLabel("Property Name:"));
    propertyPanel.add(propertyName);
    propertyPanel.add(domainB);
    panel = new JPanel(new GridLayout(2, 1));
    ((GridLayout)panel.getLayout()).setVgap(0);
    panel.add(propertyPanel);
    panel.add(nsPanel);
    subPropPanel = new JPanel(new FlowLayout(0));
    subPropertyCB = new JCheckBox("sub property of the selected nodes?");
    subPropPanel.add(subPropertyCB);
    // panel.add(subPropPanel);
  }

  public void actionPerformed(ActionEvent actionevent) {
    ArrayList<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>(
            this.selectedNodes);
    nameSpace.setText(ontology.getDefaultNameSpace());
    int i = JOptionPane.showOptionDialog(null, panel, "New Symmetric Property",
            2, 3, null, new String[] {"OK", "Cancel"}, "OK");
    if(i == 0) {
      String s = nameSpace.getText();
      if(!Utils.isValidNameSpace(s)) {
        JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                "Invalid NameSpace:").append(s).append(
                "\n example: http://gate.ac.uk/example#").toString());
        return;
      }
      if(!Utils.isValidOntologyResourceName(propertyName.getText())) {
        JOptionPane.showMessageDialog(null, "Invalid Property Name");
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
      String domainSelectedValues[] = domainAction.getSelectedValues();
      HashSet<OClass> domainSet = new HashSet<OClass>();
      for(int j = 0; j < domainSelectedValues.length; j++) {
        OClass oclass = (OClass)ontology
                .getOResourceFromMap(domainSelectedValues[j]);
        domainSet.add(oclass);
      }
      SymmetricProperty dp = ontology.addSymmetricProperty(new URI(nameSpace
              .getText()
              + propertyName.getText(), false), domainSet);
      if(subPropertyCB.isSelected()) {
        for(i = 0; i < selectedNodes.size(); i++) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)selectedNodes
                  .get(i);
          if(node.getUserObject() instanceof ObjectProperty) {
            ((ObjectProperty)node.getUserObject()).addSubProperty(dp);
            dp.addSubProperty((ObjectProperty)node.getUserObject());
          }
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

  public ArrayList<String> getOntologyClassesURIs() {
    return ontologyClassesURIs;
  }

  public void setOntologyClassesURIs(ArrayList<String> arraylist) {
    ontologyClassesURIs = arraylist;
  }

  protected JPanel nsPanel;

  protected JPanel propertyPanel;

  protected JPanel panel;

  protected JTextField nameSpace;

  protected JTextField propertyName;

  protected JButton domainB;

  protected ValuesSelectionAction domainAction;

  protected ArrayList<String> ontologyClassesURIs;

  protected ArrayList<DefaultMutableTreeNode> selectedNodes;

  protected Ontology ontology;

  protected JPanel subPropPanel;

  protected JCheckBox subPropertyCB;
}
