/*
 *  ObjectPropertyAction.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: ObjectPropertyAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
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
 * Action to create a new ObjectProperty in the ontology
 * 
 * @author niraj
 * 
 */
public class ObjectPropertyAction extends AbstractAction implements
                                                        TreeNodeSelectionListener {
  private static final long serialVersionUID = 3689632475823551285L;

  public ObjectPropertyAction(String s, Icon icon) {
    super(s, icon);
    nameSpace = new JTextField(20);
    nsPanel = new JPanel(new FlowLayout(0));
    nsPanel.add(new JLabel("Name Space:"));
    nsPanel.add(nameSpace);
    propertyName = new JTextField(20);
    domainB = new JButton("Domain");
    rangeB = new JButton("Range");
    domainAction = new ValuesSelectionAction();
    rangeAction = new ValuesSelectionAction();
    domainB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        String as[] = new String[ontologyClassesURIs.size()];
        for(int i = 0; i < as.length; i++)
          as[i] = ((String)ontologyClassesURIs.get(i));
        ArrayList<String> arraylist = new ArrayList<String>();
        for(int j = 0; j < selectedNodes.size(); j++) {
          DefaultMutableTreeNode defaultmutabletreenode = selectedNodes.get(j);
          if(((OResourceNode)defaultmutabletreenode.getUserObject())
                  .getResource() instanceof OClass)
            arraylist.add(((OClass)((OResourceNode)defaultmutabletreenode
                    .getUserObject()).getResource()).getURI().toString());
        }
        String as1[] = new String[arraylist.size()];
        for(int k = 0; k < as1.length; k++)
          as1[k] = arraylist.get(k);
        domainAction.showGUI("Domain", as, as1, false);
      }

      final ObjectPropertyAction this$0;
      {
        this$0 = ObjectPropertyAction.this;
      }
    });
    rangeB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        String as[] = new String[ontologyClassesURIs.size()];
        for(int i = 0; i < as.length; i++)
          as[i] = ((String)ontologyClassesURIs.get(i));
        rangeAction.showGUI("Range", as, new String[0], false);
      }

      final ObjectPropertyAction this$0;
      {
        this$0 = ObjectPropertyAction.this;
      }
    });
    propertyPanel = new JPanel(new FlowLayout(0));
    propertyPanel.add(new JLabel("Property Name:"));
    propertyPanel.add(propertyName);
    propertyPanel.add(domainB);
    propertyPanel.add(rangeB);
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
    int i = JOptionPane.showOptionDialog(null, panel, "New Property", 2, 3,
            null, new String[] {"OK", "Cancel"}, "OK");
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
      String rangeSelectedValues[] = rangeAction.getSelectedValues();
      HashSet<OClass> rangeSet = new HashSet<OClass>();
      for(int j = 0; j < rangeSelectedValues.length; j++) {
        OClass oclass = (OClass)ontology
                .getOResourceFromMap(rangeSelectedValues[j]);
        rangeSet.add(oclass);
      }
      ObjectProperty dp = ontology.addObjectProperty(new URI(nameSpace
              .getText()
              + propertyName.getText(), false), domainSet, rangeSet);
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

  protected JButton rangeB;

  protected ValuesSelectionAction domainAction;

  protected ValuesSelectionAction rangeAction;

  protected ArrayList<String> ontologyClassesURIs;

  protected ArrayList<DefaultMutableTreeNode> selectedNodes;

  protected Ontology ontology;

  protected JPanel subPropPanel;

  protected JCheckBox subPropertyCB;
}
