package gate.gui.ontology;

import gate.creole.ontology.Literal;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.RDFProperty;
import gate.gui.MainFrame;

import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;

public class SearchAction extends AbstractAction {

  public SearchAction(String s, Icon icon, OntologyEditor editor) {
    super(s, icon);
    this.ontologyEditor = editor;
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel1.add(new JLabel("Find what: "));

    domainBox = new JComboBox();
    domainBox.setPrototypeDisplayValue("this is just an example, not a value. OK?");
    domainBox.setEditable(true);
    domainBox.setEditable(true);
    domainBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent keyevent) {
        String s = ((JTextComponent)domainBox.getEditor().getEditorComponent())
                .getText();
        if(s != null) {
          if(keyevent.getKeyCode() != KeyEvent.VK_ENTER) {
            HashSet<OResource> set = new HashSet<OResource>();
            for(int i = 0; i < resourcesArray.length; i++) {
              String s1 = resourcesArray[i].getName();
              if(s1.toLowerCase().startsWith(s.toLowerCase())) {
                set.add(resourcesArray[i]);
              }
            }

            if(searchInPropertyValues.isSelected()) {
              RDFProperty aProp = (RDFProperty)properties.getSelectedItem();
              List<OResource> toAdd = new ArrayList<OResource>();
              if(aProp instanceof ObjectProperty) {
                OResource res = ontologyEditor.ontology.getOResourceByName(s);
                if(res != null) {
                  toAdd = ontologyEditor.ontology.getOResourcesWith(aProp, res);
                }
              }
              else {
                toAdd = ontologyEditor.ontology.getOResourcesWith(aProp,
                        new Literal(s));
              }
              set.addAll(toAdd);
            }
            List<OResource> setList = new ArrayList<OResource>(set);
            Collections.sort(setList, new OntologyItemComparator());

            
            DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(
                    setList.toArray());

            domainBox.setModel(defaultcomboboxmodel);

            try {
              if(!setList.isEmpty()) domainBox.showPopup();
            }
            catch(Exception exception) {
            }
          }
          ((JTextComponent)domainBox.getEditor().getEditorComponent())
                  .setText(s);
        }
      }
    });
    panel1.add(domainBox);
    properties = new JComboBox();
    properties.setEditable(true);
    properties.setPrototypeDisplayValue("this is just an example, not a value. OK?");
    properties.getEditor().getEditorComponent().addKeyListener(
            new KeyAdapter() {
              public void keyReleased(KeyEvent keyevent) {
                String s = ((JTextComponent)properties.getEditor()
                        .getEditorComponent()).getText();
                if(s != null) {
                  if(keyevent.getKeyCode() != KeyEvent.VK_ENTER) {
                    ArrayList<OResource> arraylist = new ArrayList<OResource>();
                    for(int i = 0; i < propertiesArray.length; i++) {
                      String s1 = propertiesArray[i].getName();
                      if(s1.toLowerCase().startsWith(s.toLowerCase())) {
                        arraylist.add(propertiesArray[i]);
                      }
                    }
                    Collections.sort(arraylist, new OntologyItemComparator());
                    DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(
                            arraylist.toArray());
                    properties.setModel(defaultcomboboxmodel);
                    
                    try {
                      if(!arraylist.isEmpty()) properties.showPopup();
                    }
                    catch(Exception exception) {
                    }
                  }
                  ((JTextComponent)properties.getEditor().getEditorComponent())
                          .setText(s);
                }
              }
            });
    
    searchInPropertyValues = new JCheckBox("In the values of: ");
    searchInPropertyValues.setSelected(false);
    panel2.add(searchInPropertyValues);
    panel2.add(properties);
    
    panel.add(panel1);
    panel.add(panel2);
  }

  public void actionPerformed(ActionEvent ae) {
    List<OResource> resources = ontologyEditor.ontology.getAllResources();
    Collections.sort(resources, new OntologyItemComparator());
    
    resourcesArray = new OResource[resources.size()];
    resourcesArray = resources.toArray(resourcesArray);
    DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(
            resources.toArray());
    domainBox.setModel(defaultcomboboxmodel);
    
    Set<RDFProperty> props = ontologyEditor.ontology.getRDFProperties();
    props.addAll(ontologyEditor.ontology.getAnnotationProperties());
    props.addAll(ontologyEditor.ontology.getObjectProperties());
    List<RDFProperty> propsList = new ArrayList<RDFProperty>(props);
    Collections.sort(propsList, new OntologyItemComparator());
    
    propertiesArray = new RDFProperty[props.size()];
    propertiesArray = props.toArray(propertiesArray);
    DefaultComboBoxModel defaultcomboboxmodel1 = new DefaultComboBoxModel(
            propsList.toArray());
    properties.setModel(defaultcomboboxmodel1);
    
    
    resources = null;
    props = null;
    propsList = null;
    
    int returnValue = JOptionPane.showOptionDialog(MainFrame.getInstance(),
            panel, "Find Ontology Resource", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
            MainFrame.getIcon("search"), new String[] {"Find", "Cancel"}, "Find");
    if(returnValue == JOptionPane.OK_OPTION) {
      OResource selectedR = (OResource)domainBox.getSelectedItem();
      if(selectedR instanceof RDFProperty) {
        ontologyEditor.propertyTree.setSelectionPath(new TreePath(
                ontologyEditor.uri2TreeNodesListMap.get(
                        selectedR.getURI().toString()).get(0).getPath()));
        ontologyEditor.propertyTree
                .scrollPathToVisible(ontologyEditor.propertyTree
                        .getSelectionPath());
        ontologyEditor.tabbedPane
                .setSelectedComponent(ontologyEditor.propertyScroller);
      }
      else {
        ontologyEditor.tree.setSelectionPath(new TreePath(
                ontologyEditor.uri2TreeNodesListMap.get(
                        selectedR.getURI().toString()).get(0).getPath()));
        ontologyEditor.tree.scrollPathToVisible(ontologyEditor.tree
                .getSelectionPath());
        ontologyEditor.tabbedPane.setSelectedComponent(ontologyEditor.scroller);
      }
    }
  }

  protected JComboBox domainBox;

  protected JPanel panel;

  protected OntologyEditor ontologyEditor;

  protected OResource[] resourcesArray = new OResource[0];

  protected RDFProperty[] propertiesArray = new RDFProperty[0];

  protected JComboBox properties;

  protected JCheckBox searchInPropertyValues;
}
