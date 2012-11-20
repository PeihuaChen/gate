/*
 *  Copyright (c) 2008--2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */
package gate.termraider.gui;

import gate.gui.MainFrame;
import gate.util.GateException;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import gate.termraider.bank.*;
import gate.termraider.output.*;


/**
 * Action class for saving an ontology from the GATE GUI.
 */
public class ActionMakeOntology
    extends AbstractAction {

  
  private static final long serialVersionUID = -1135800259696959339L;

  private AbstractTermbank termbank;

  public ActionMakeOntology(String label, AbstractTermbank termbank) {
    super(label);
    this.termbank = termbank;
  }

  public void actionPerformed(ActionEvent ae) {
    JDialog ontologyDialog = new JDialog(MainFrame.getInstance(), "Generate an ontology from this Termbank (EXPERIMENTAL)", true);
    MainFrame.getGuiRoots().add(ontologyDialog);
    ontologyDialog.setLayout(new BorderLayout());
    SliderPanel sliderPanel = new SliderPanel(termbank, "populate the ontology", true, null);
    ontologyDialog.add(sliderPanel, BorderLayout.CENTER);
    
    JPanel underPanel = new JPanel();
    underPanel.setLayout(new BoxLayout(underPanel, BoxLayout.Y_AXIS));
    underPanel.add(new JSeparator());
    underPanel.add(Box.createVerticalStrut(5));
    underPanel.add(new JLabel("The following parameters can be edited (default values are provided):"));
    underPanel.add(Box.createVerticalStrut(5));
    
    JPanel stringsPanel = new JPanel(new GridBagLayout());
    Insets insets = new Insets(0, 0, 0, 0);
    
    JLabel ontologyNameLabel = new JLabel("Ontology name");
    stringsPanel.add(ontologyNameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 1, 1));
    JTextField nameField = new JTextField("", 50);
    String defaultOntologyName = termbank.getName() + "_ontology";
    nameField.setText(defaultOntologyName);
    nameField.setToolTipText("name of the GATE Ontology LR to be created");
    stringsPanel.add(nameField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 1, 1));
    
    JLabel instanceNamespaceLabel = new JLabel("Namespace for instances"); 
    stringsPanel.add(instanceNamespaceLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 1, 1));

    JTextField instanceNamespaceField = new JTextField("", 50);
    if ( (termbank.getNamespaceBase() == null) || termbank.getNamespaceBase().isEmpty()) {
      instanceNamespaceField.setText(OntologyGenerator.generateInstanceNamespace());
    }
    else {
      instanceNamespaceField.setText(termbank.getNamespaceBase());
    }
    instanceNamespaceField.setToolTipText("Ontology namespaces for instances (terms)");
    stringsPanel.add(instanceNamespaceField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 1, 1));
    
    underPanel.add(stringsPanel);
    underPanel.add(Box.createVerticalStrut(5));
    
    Box buttonBox = Box.createHorizontalBox();
    buttonBox.add(Box.createHorizontalGlue());
    JButton okButton = new JButton(OntologyActionListener.okAction);
    buttonBox.add(okButton);
    buttonBox.add(Box.createHorizontalGlue());
    JButton cancelButton = new JButton(OntologyActionListener.cancelAction);
    buttonBox.add(cancelButton);
    buttonBox.add(Box.createHorizontalGlue());
    underPanel.add(buttonBox);
    
    OntologyActionListener listener = new OntologyActionListener(termbank, sliderPanel, ontologyDialog,
            nameField, instanceNamespaceField);
    okButton.addActionListener(listener);
    cancelButton.addActionListener(listener);

    ontologyDialog.add(underPanel, BorderLayout.SOUTH);
    ontologyDialog.pack();
    ontologyDialog.setLocationRelativeTo(ontologyDialog.getOwner());
    ontologyDialog.setVisible(true);
  }
}


class OntologyActionListener implements ActionListener {

  private AbstractTermbank termbank;
  private SliderPanel sliderPanel;
  private JDialog dialog;
  private JTextField nameField, instanceNamespaceField;
  public static String okAction = "OK";
  public static String cancelAction = "Cancel";
  
  public OntologyActionListener(AbstractTermbank termbank, SliderPanel sliderPanel, JDialog dialog, 
          JTextField nameField, JTextField instanceNamespaceField) {
    this.termbank = termbank;
    this.sliderPanel = sliderPanel;
    this.dialog = dialog;
    this.nameField = nameField;
    this.instanceNamespaceField = instanceNamespaceField;
  }
  
  @Override
  public void actionPerformed(ActionEvent event) {
    if (event.getActionCommand().equals(okAction)) {
      try {
        String oName = nameField.getText();
        if (oName.isEmpty()) {
          oName = nameField.getToolTipText();
        }
        termbank.makeOntology(sliderPanel.getValues(), oName, instanceNamespaceField.getText());
      }
      catch(GateException e) {
        e.printStackTrace();
      }
      finally {
        dialog.dispose();
      }
    }
    else if (event.getActionCommand().equals(cancelAction)) {
      dialog.dispose();
    }
  }
}
