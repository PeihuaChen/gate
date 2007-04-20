/*
 *  DatatypePropertyAction.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: DatatypePropertyAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
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
 * Action to create a new datatype property.
 * 
 * @author niraj
 * 
 */
public class DatatypePropertyAction extends AbstractAction implements
                                                          TreeNodeSelectionListener {
  private static final long serialVersionUID = 3257852073457235252L;

  public DatatypePropertyAction(String s, Icon icon) {
    super(s, icon);
    nameSpace = new JTextField(20);
    nsPanel = new JPanel(new FlowLayout(0));
    nsPanel.add(new JLabel("Name Space:"));
    nsPanel.add(nameSpace);
    propertyName = new JTextField(20);
    domainB = new JButton("Domain");
    domainAction = new ValuesSelectionAction();
    datatypes = new JComboBox(new DefaultComboBoxModel(new String[]{
        "http://www.w3.org/2001/XMLSchema#boolean",
        "http://www.w3.org/2001/XMLSchema#byte",
        "http://www.w3.org/2001/XMLSchema#date",
        "http://www.w3.org/2001/XMLSchema#decimal",
        "http://www.w3.org/2001/XMLSchema#double",
        "http://www.w3.org/2001/XMLSchema#duration",
        "http://www.w3.org/2001/XMLSchema#float",
        "http://www.w3.org/2001/XMLSchema#int",
        "http://www.w3.org/2001/XMLSchema#integer",
        "http://www.w3.org/2001/XMLSchema#long",
        "http://www.w3.org/2001/XMLSchema#negativeInteger",
        "http://www.w3.org/2001/XMLSchema#nonNegativeInteger",
        "http://www.w3.org/2001/XMLSchema#nonPositiveInteger",
        "http://www.w3.org/2001/XMLSchema#positiveInteger",
        "http://www.w3.org/2001/XMLSchema#short",
        "http://www.w3.org/2001/XMLSchema#string",
        "http://www.w3.org/2001/XMLSchema#time",
        "http://www.w3.org/2001/XMLSchema#unsignedByte",
        "http://www.w3.org/2001/XMLSchema#unsignedInt",
        "http://www.w3.org/2001/XMLSchema#unsignedLong",
        "http://www.w3.org/2001/XMLSchema#unsignedShort"}));
    domainB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        String as[] = new String[ontologyClassesURIs.size()];
        for(int i = 0; i < as.length; i++)
          as[i] = ((String)ontologyClassesURIs.get(i));
        ArrayList arraylist = new ArrayList();
        for(int j = 0; j < selectedNodes.size(); j++) {
          DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode)selectedNodes
                  .get(j);
          if(defaultmutabletreenode.getUserObject() instanceof OClass)
            arraylist.add(((OClass)defaultmutabletreenode.getUserObject())
                    .getURI().toString());
        }
        String as1[] = new String[arraylist.size()];
        for(int k = 0; k < as1.length; k++)
          as1[k] = (String)arraylist.get(k);
        domainAction.showGUI("Domain", as, as1, false);
      }

      final DatatypePropertyAction this$0;
      {
        this$0 = DatatypePropertyAction.this;
      }
    });
    propertyPanel = new JPanel(new FlowLayout(0));
    propertyPanel.add(new JLabel("Property Name:"));
    propertyPanel.add(propertyName);
    propertyPanel.add(domainB);
    dtPanel = new JPanel(new FlowLayout(0));
    dtPanel.add(new JLabel("Data Type :"));
    dtPanel.add(datatypes);
    panel = new JPanel(new GridLayout(3, 1));
    ((GridLayout)panel.getLayout()).setVgap(0);
    panel.add(propertyPanel);
    panel.add(nsPanel);
    panel.add(dtPanel);
    subPropPanel = new JPanel(new FlowLayout(0));
    subPropertyCB = new JCheckBox("sub property of the selected nodes?");
    subPropPanel.add(subPropertyCB);
    //panel.add(subPropPanel);
  }

  public void actionPerformed(ActionEvent actionevent) {
    ArrayList<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>(this.selectedNodes);
    nameSpace.setText(ontology.getDefaultNameSpace());
    int i = JOptionPane.showOptionDialog(null, panel, "New Datatype Property",
            2, 3, null, new String[]{"OK", "Cancel"}, "OK");
    if(i == 0) {
      String s = nameSpace.getText();
      if(!gate.gui.ontology.Utils.isValidNameSpace(s)) {
        JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                "Invalid NameSpace:").append(s).append(
                "\n example: http://gate.ac.uk/example#").toString());
        return;
      }
      if(!gate.gui.ontology.Utils.isValidOntologyResourceName(propertyName
              .getText())) {
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
      String as[] = domainAction.getSelectedValues();
      HashSet<OClass> hashset = new HashSet<OClass>();
      for(int j = 0; j < as.length; j++) {
        OClass oclass = (OClass)ontology.getOResourceFromMap(as[j]);
        hashset.add(oclass);
      }
      DataType dt = OntologyUtilities.getDataType((String)datatypes.getSelectedItem());
      DatatypeProperty dp = ontology.addDatatypeProperty(new URI(nameSpace
              .getText()
              + propertyName.getText(), false), hashset, dt);
      if(subPropertyCB.isSelected()) {
        for(i = 0; i < selectedNodes.size(); i++) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)selectedNodes
                  .get(i);
          if(node.getUserObject() instanceof DatatypeProperty) {
            ((DatatypeProperty)node.getUserObject()).addSubProperty(dp);
            dp.addSubProperty((DatatypeProperty)node.getUserObject());
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

  public ArrayList getOntologyClassesURIs() {
    return ontologyClassesURIs;
  }

  public void setOntologyClassesURIs(ArrayList<String> arraylist) {
    ontologyClassesURIs = arraylist;
  }

  protected JPanel nsPanel;

  protected JPanel propertyPanel;

  protected JPanel panel;

  protected JPanel dtPanel;

  protected JPanel subPropPanel;

  protected JCheckBox subPropertyCB;

  protected JComboBox datatypes;

  protected JTextField nameSpace;

  protected JTextField propertyName;

  protected JButton domainB;

  protected ValuesSelectionAction domainAction;

  protected ArrayList<String> ontologyClassesURIs;

  protected ArrayList<DefaultMutableTreeNode> selectedNodes;

  protected Ontology ontology;
}
