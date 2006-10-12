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
 * This class provides a GUI for creating a new sub property
 * 
 * @author niraj
 */
public class SubPropertyAction extends AbstractAction implements
                                                     TreeNodeSelectionListener {
  public SubPropertyAction(String s, Icon icon) {
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
        Object userObject = ((DefaultMutableTreeNode)selectedNodes.get(0))
                .getUserObject();
        Set domain = ((Property)userObject).getDomain();
        Iterator iter = domain.iterator();
        String as1[] = new String[domain.size()];
        for(int k = 0; k < as1.length; k++)
          as1[k] = ((TClass)iter.next()).getName();
        domainAction.showGUI("New Property", as, as1);
      }

      final SubPropertyAction this$0;
      {
        this$0 = SubPropertyAction.this;
      }
    });
    rangeB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        Object userObject = ((DefaultMutableTreeNode)selectedNodes.get(0))
                .getUserObject();
        if(userObject instanceof ObjectProperty) {
          String as[] = new String[ontologyClassesNames.size()];
          for(int i = 0; i < as.length; i++)
            as[i] = ((String)ontologyClassesNames.get(i));
          Set range = ((Property)userObject).getRange();
          Iterator iter = range.iterator();
          String as1[] = new String[range.size()];
          for(int k = 0; k < as1.length; k++)
            as1[k] = ((TClass)iter.next()).getName();
          rangeAction.showGUI("New Property", as, as1);
        } else {
          dataTypeRange = JOptionPane.showInputDialog(null, dataTypeRange,
                  "range Class", 3);
        }
      }

      final SubPropertyAction this$0;
      {
        this$0 = SubPropertyAction.this;
      }
    });
    propertyPanel = new JPanel(new FlowLayout(0));
    propertyPanel.add(new JLabel("Property Name:"));
    propertyPanel.add(propertyName);
    propertyPanel.add(domainB);
    propertyPanel.add(rangeB);
    comment = new JTextField(20);
    commentPanel = new JPanel(new FlowLayout(0));
    commentPanel.add(new JLabel("Comment :"));
    commentPanel.add(comment);
    panel = new JPanel(new GridLayout(3, 1));
    ((GridLayout)panel.getLayout()).setVgap(0);
    panel.add(propertyPanel);
    panel.add(nsPanel);
    panel.add(commentPanel);
  }

  private boolean validSelection(Set superDomain, Set subDomain) {
    Iterator iter = subDomain.iterator();
    outer: while(iter.hasNext()) {
      TClass tc = (TClass)iter.next();
      Iterator subIter = superDomain.iterator();
      while(subIter.hasNext()) {
        TClass stc = (TClass)subIter.next();
        if(stc == tc) continue outer;
        if(ontology.isSubClassOf(stc.getName(), tc.getName())) continue outer;
      }
      // if we are here
      return false;
    }
    return true;
  }

  public void actionPerformed(ActionEvent actionevent) {
    Object userObject = ((DefaultMutableTreeNode)selectedNodes.get(0))
            .getUserObject();
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
      // we need to check for its validity
      if(!validSelection(((Property)userObject).getDomain(), hashset)) {
        JOptionPane.showMessageDialog(null, (new StringBuilder())
                .append("Invalid Domain"));
        return;
      }
      if(userObject instanceof DatatypeProperty) {
        Class class1 = null;
        try {
          class1 = Class.forName(dataTypeRange.trim());
        } catch(Exception exception) {
          JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                  "Invalid Range :").append(dataTypeRange).toString());
          return;
        }

        DatatypeProperty datatypepropertyimpl = ontology.addDatatypeProperty(propertyName.getText(), comment.getText(), hashset, class1);
        ((DatatypeProperty)userObject).addSubProperty(datatypepropertyimpl);
        datatypepropertyimpl.addSuperProperty((DatatypeProperty)userObject);
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
        // we need to check for its validity
        if(!validSelection(((Property)userObject).getRange(), hashset1)) {
          JOptionPane.showMessageDialog(null, (new StringBuilder())
                  .append("Invalid Range"));
          return;
        }
        if(userObject instanceof TransitiveProperty) {
          TransitiveProperty transitivepropertyimpl = ontology.addTransitiveProperty(
                  propertyName.getText(), comment.getText(), hashset, hashset1);
          transitivepropertyimpl.addSuperProperty((Property)userObject);
          ((Property)userObject).addSubProperty(transitivepropertyimpl);
        } else if(userObject instanceof SymmetricProperty) {
          SymmetricProperty symmetricpropertyimpl = ontology.addSymmetricProperty(
                  propertyName.getText(), comment.getText(), hashset, hashset1);
          symmetricpropertyimpl.addSuperProperty((Property)userObject);
          ((Property)userObject).addSubProperty(symmetricpropertyimpl);
        } else {
          ObjectProperty objectpropertyimpl = ontology.addObjectProperty(
                  propertyName.getText(), comment.getText(), hashset, hashset1);
          objectpropertyimpl.addSuperProperty((Property)userObject);
          ((Property)userObject).addSubProperty(objectpropertyimpl);
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

  protected ArrayList ontologyClassesNames;

  protected ArrayList selectedNodes;

  protected Ontology ontology;
}
