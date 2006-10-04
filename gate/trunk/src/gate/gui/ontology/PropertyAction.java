// Decompiled by DJ v3.9.9.91 Copyright 2005 Atanas Neshkov  Date: 19/09/2006 10:13:51
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   PropertyAction.java
package gate.gui.ontology;

import gate.creole.ontology.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

// Referenced classes of package gate.gui.ontology:
//            ValuesSelectionAction, TreeNodeSelectionListener, Utils
public class PropertyAction extends AbstractAction implements
                                                  TreeNodeSelectionListener {
  public PropertyAction(String s, Icon icon) {
    super(s, icon);
    dataTypeRange = "java.lang.String";
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
        String as[] = new String[ontologyClassesNames.size()];
        for(int i = 0; i < as.length; i++)
          as[i] = ((String)ontologyClassesNames.get(i));
        ArrayList arraylist = new ArrayList();
        for(int j = 0; j < selectedNodes.size(); j++) {
          DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode)selectedNodes
                  .get(j);
          if(defaultmutabletreenode.getUserObject() instanceof TClass)
            arraylist.add(((TClass)defaultmutabletreenode.getUserObject())
                    .getName());
        }
        String as1[] = new String[arraylist.size()];
        for(int k = 0; k < as1.length; k++)
          as1[k] = (String)arraylist.get(k);
        domainAction.showGUI("New Property", as, as1);
      }

      final PropertyAction this$0;
      {
        this$0 = PropertyAction.this;
      }
    });
    rangeB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        if(objectProperty.isSelected()) {
          String as[] = new String[ontologyClassesNames.size()];
          for(int i = 0; i < as.length; i++)
            as[i] = ((String)ontologyClassesNames.get(i));
          rangeAction.showGUI("New Property", as, new String[0]);
        } else {
          dataTypeRange = JOptionPane.showInputDialog(null, dataTypeRange,
                  "range Class", 3);
        }
      }

      final PropertyAction this$0;
      {
        this$0 = PropertyAction.this;
      }
    });
    propertyPanel = new JPanel(new FlowLayout(0));
    propertyPanel.add(new JLabel("Property Name:"));
    propertyPanel.add(propertyName);
    propertyPanel.add(domainB);
    propertyPanel.add(rangeB);
    objectProperty = new JRadioButton("Object Property");
    datatypeProperty = new JRadioButton("Datatype Property");
    ButtonGroup buttongroup = new ButtonGroup();
    buttongroup.add(objectProperty);
    buttongroup.add(datatypeProperty);
    propPanel = new JPanel(new FlowLayout(0));
    propPanel.add(new JLabel("Property Type:"));
    propPanel.add(objectProperty);
    propPanel.add(datatypeProperty);
    transitiveProperty = new JRadioButton("Transitive Property");
    symmetricProperty = new JRadioButton("Symmetric Property");
    none = new JRadioButton("none of them");
    ButtonGroup buttongroup1 = new ButtonGroup();
    buttongroup1.add(transitiveProperty);
    buttongroup1.add(symmetricProperty);
    buttongroup1.add(none);
    symTransPanel = new JPanel(new FlowLayout(0));
    symTransPanel.add(transitiveProperty);
    symTransPanel.add(symmetricProperty);
    symTransPanel.add(none);
    none.setSelected(true);
    objectProperty.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        transitiveProperty.setEnabled(true);
        symmetricProperty.setEnabled(true);
        none.setEnabled(true);
      }

      final PropertyAction this$0;
      {
        this$0 = PropertyAction.this;
      }
    });
    datatypeProperty.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        transitiveProperty.setEnabled(false);
        symmetricProperty.setEnabled(false);
        none.setEnabled(false);
      }

      final PropertyAction this$0;
      {
        this$0 = PropertyAction.this;
      }
    });
    objectProperty.setSelected(true);
    comment = new JTextField(20);
    commentPanel = new JPanel(new FlowLayout(0));
    commentPanel.add(new JLabel("Comment :"));
    commentPanel.add(comment);
    panel = new JPanel(new GridLayout(5, 1));
    ((GridLayout)panel.getLayout()).setVgap(0);
    panel.add(propertyPanel);
    panel.add(nsPanel);
    panel.add(propPanel);
    panel.add(symTransPanel);
    panel.add(commentPanel);
  }

  public void actionPerformed(ActionEvent actionevent) {
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
      if(ontology.getPropertyDefinitionByName(propertyName.getText()) != null) {
        JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                "Property :").append(propertyName.getText()).append(
                " already exists").toString());
        return;
      }
      String as[] = domainAction.getSelectedValues();
      HashSet hashset = new HashSet();
      if(as == null || as.length == 0) {
        JOptionPane.showMessageDialog(null, "Invalid Domain");
        return;
      }
      for(int j = 0; j < as.length; j++) {
        OClass oclass = (OClass)ontology.getClassByName((String)as[j]);
        if(oclass == null) {
          JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                  "Invalid Domain :").append((String)as[j]).toString());
          return;
        }
        hashset.add(oclass);
      }
      if(datatypeProperty.isSelected()) {
        Class class1 = null;
        try {
          class1 = Class.forName(dataTypeRange.trim());
        } catch(Exception exception) {
          JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                  "Invalid Range :").append(dataTypeRange).toString());
          return;
        }
        DatatypePropertyImpl datatypepropertyimpl = new DatatypePropertyImpl(
                propertyName.getText(), comment.getText(), hashset, class1,
                ontology);
        ontology.addDatatypeProperty(datatypepropertyimpl);
      } else {
        String as1[] = rangeAction.getSelectedValues();
        HashSet hashset1 = new HashSet();
        if(as1 == null || as1.length == 0) {
          JOptionPane.showMessageDialog(null, "Invalid Range");
          return;
        }
        for(int k = 0; k < as1.length; k++) {
          OClass oclass1 = (OClass)ontology.getClassByName((String)as1[k]);
          if(oclass1 == null) {
            JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                    "Invalid Range :").append((String)as1[k]).toString());
            return;
          }
          hashset1.add(oclass1);
        }
        if(none.isSelected()) {
          ObjectPropertyImpl objectpropertyimpl = new ObjectPropertyImpl(
                  propertyName.getText(), comment.getText(), hashset, hashset1,
                  ontology);
          ontology.addObjectProperty(objectpropertyimpl);
        } else if(transitiveProperty.isSelected()) {
          TransitivePropertyImpl transitivepropertyimpl = new TransitivePropertyImpl(
                  propertyName.getText(), comment.getText(), hashset, hashset1,
                  ontology);
          ontology.addTransitiveProperty(transitivepropertyimpl);
        } else {
          SymmetricPropertyImpl symmetricpropertyimpl = new SymmetricPropertyImpl(
                  propertyName.getText(), comment.getText(), hashset, hashset1,
                  ontology);
          ontology.addSymmetricProperty(symmetricpropertyimpl);
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

  public ArrayList getOntologyClassesNames() {
    return ontologyClassesNames;
  }

  public void setOntologyClassesNames(ArrayList arraylist) {
    ontologyClassesNames = arraylist;
  }

  protected JPanel nsPanel;

  protected JPanel propertyPanel;

  protected JPanel propPanel;

  protected JPanel symTransPanel;

  protected JPanel commentPanel;

  protected JPanel panel;

  protected JTextField nameSpace;

  protected JTextField propertyName;

  protected JTextField comment;

  protected JButton domainB;

  protected JButton rangeB;

  protected ValuesSelectionAction domainAction;

  protected ValuesSelectionAction rangeAction;

  protected String dataTypeRange;

  protected JRadioButton objectProperty;

  protected JRadioButton datatypeProperty;

  protected JRadioButton symmetricProperty;

  protected JRadioButton transitiveProperty;

  protected JRadioButton none;

  protected ArrayList ontologyClassesNames;

  protected ArrayList selectedNodes;

  protected Ontology ontology;
}
