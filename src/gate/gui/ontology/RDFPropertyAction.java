/*
 *  RDFPropertyAction.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: RDFPropertyAction.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
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
 * Action to create an RDF Property
 * @author niraj
 */
public class RDFPropertyAction extends AbstractAction implements
                                                     TreeNodeSelectionListener {
  private static final long serialVersionUID = 4050487806914867506L;

  public RDFPropertyAction(String s, Icon icon) {
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
        ArrayList<String> arraylist = new ArrayList<String>();
        for(int j = 0; j < selectedNodes.size(); j++) {
          DefaultMutableTreeNode defaultmutabletreenode = selectedNodes.get(j);
          if(defaultmutabletreenode.getUserObject() instanceof OClass)
            arraylist.add(((OClass)defaultmutabletreenode.getUserObject())
                    .getURI().toString());
        }
        String as[] = new String[resoucesList.size()];
        for(int i = 0; i < as.length; i++)
          as[i] = resoucesList.get(i).getURI().toString();
        domainAction.showGUI("Domain", as, new String[0], false);
      }

      final RDFPropertyAction this$0;
      {
        this$0 = RDFPropertyAction.this;
      }
    });
    rangeB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        String as[] = new String[resoucesList.size()];
        for(int i = 0; i < as.length; i++)
          as[i] = resoucesList.get(i).getURI().toString();
        rangeAction.showGUI("Range", as, new String[0], false);
      }

      final RDFPropertyAction this$0;
      {
        this$0 = RDFPropertyAction.this;
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
    //panel.add(subPropPanel);
  }

  public void actionPerformed(ActionEvent actionevent) {
    ArrayList<DefaultMutableTreeNode> selectedNodes = new ArrayList<DefaultMutableTreeNode>(this.selectedNodes);
    nameSpace.setText(ontology.getDefaultNameSpace());
    int i = JOptionPane.showOptionDialog(null, panel, "New Property", 2, 3,
            null, new String[]{"OK", "Cancel"}, "OK");
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
      HashSet<OResource> domainSet = new HashSet<OResource>();
      for(int j = 0; j < domainSelectedValues.length; j++) {
        OResource oclass = ontology
                .getOResourceFromMap(domainSelectedValues[j]);
        domainSet.add(oclass);
      }
      String rangeSelectedValues[] = rangeAction.getSelectedValues();
      HashSet<OResource> rangeSet = new HashSet<OResource>();
      for(int j = 0; j < rangeSelectedValues.length; j++) {
        OResource oclass = ontology.getOResourceFromMap(rangeSelectedValues[j]);
        rangeSet.add(oclass);
      }
      RDFProperty dp = ontology.addRDFProperty(new URI(nameSpace.getText()
              + propertyName.getText(), false), domainSet, rangeSet);
      if(subPropertyCB.isSelected()) {
        for(i = 0; i < selectedNodes.size(); i++) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)selectedNodes
                  .get(i);
          if(node.getUserObject() instanceof RDFProperty
                  && !(node.getUserObject() instanceof ObjectProperty)
                  && !(node.getUserObject() instanceof AnnotationProperty)
                  && !(node.getUserObject() instanceof DatatypeProperty)) {
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

  protected JPanel nsPanel;

  protected JPanel propertyPanel;

  protected JPanel panel;

  protected JTextField nameSpace;

  protected JTextField propertyName;

  protected JButton domainB;

  protected JButton rangeB;

  protected ValuesSelectionAction domainAction;

  protected ValuesSelectionAction rangeAction;

  protected ArrayList<DefaultMutableTreeNode> selectedNodes;

  protected List<OResource> resoucesList;

  protected Ontology ontology;

  protected JPanel subPropPanel;

  protected JCheckBox subPropertyCB;

  public void setResoucesList(List<OResource> resoucesList) {
    this.resoucesList = resoucesList;
  }
}
