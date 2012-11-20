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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import gate.termraider.bank.*;
import gate.termraider.util.Utilities;

import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;


/**
 * Action class for saving RDF-XML from the GATE GUI.
 */
public class ActionSaveRdf
    extends AbstractAction {

  private static final long serialVersionUID = 5725028061478144129L;
  
  private AbstractTermbank termbank;

  public ActionSaveRdf(String label, AbstractTermbank termbank) {
    super(label);
    this.termbank = termbank;
  }

  public void actionPerformed(ActionEvent ae) {
    JDialog saveDialog = new JDialog(MainFrame.getInstance(), "Save Termbank as RDF-XML", true);
    MainFrame.getGuiRoots().add(saveDialog);
    saveDialog.setLayout(new BorderLayout());
    SliderPanel sliderPanel = new SliderPanel(termbank, "save", true, null);
    saveDialog.add(sliderPanel, BorderLayout.CENTER);

    JPanel chooserPanel = new JPanel();
    chooserPanel.setLayout(new BoxLayout(chooserPanel, BoxLayout.Y_AXIS));
    chooserPanel.add(new JSeparator());
    
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter("RDF-XML files", Utilities.EXTENSION_RDF, "xml");
    chooser.setFileFilter(filter);
    chooser.setApproveButtonText("Save");
    chooser.addActionListener(new RdfFileSelectionActionListener(chooser, termbank, sliderPanel, saveDialog));
    chooserPanel.add(chooser);
    saveDialog.add(chooserPanel, BorderLayout.SOUTH);
    saveDialog.pack();
    saveDialog.setLocationRelativeTo(saveDialog.getOwner());
    saveDialog.setVisible(true);
  }
}


class RdfFileSelectionActionListener implements ActionListener {

  private JFileChooser chooser;
  private AbstractTermbank termbank;
  private SliderPanel sliderPanel;
  private JDialog dialog;
  
  public RdfFileSelectionActionListener(JFileChooser chooser, AbstractTermbank termbank, SliderPanel sliderPanel, JDialog dialog) {
    this.chooser = chooser;
    this.termbank = termbank;
    this.sliderPanel = sliderPanel;
    this.dialog = dialog;
  }
  
  @Override
  public void actionPerformed(ActionEvent event) {
    if (event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
      File outputFile = Utilities.addExtensionIfNotExtended(chooser.getSelectedFile(),
              Utilities.EXTENSION_RDF);
      try {
        termbank.saveAsRdf(sliderPanel.getValues(), outputFile);
      }
      catch(GateException e) {
        e.printStackTrace();
      }
      finally {
        dialog.dispose();
      }
    }
    else if (event.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
      dialog.dispose();
    }
  }
}
