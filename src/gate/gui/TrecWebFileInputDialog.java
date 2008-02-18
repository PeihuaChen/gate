/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Niraj Aswani, 14/Feb/2008
 *
 *  $Id: TrecWebFileInputDialog.java $
 */
package gate.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;

import gate.Gate;

/**
 * A simple component that allows the user to select a trec web file and encoding
 */

public class TrecWebFileInputDialog extends JPanel {

  public TrecWebFileInputDialog(){
    initGUIComponents();
    initListeners();
  }


  /**
   * Creates the UI
   */
  protected void initGUIComponents(){
    setLayout(new GridBagLayout());
    //first row
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 0;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = new Insets(0, 0, 0, 5);
    add(new JLabel("TrecWeb File URL:"), constraints);

    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 0;
    constraints.gridwidth = 5;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(0, 0, 0, 10);
    add(urlTextField = new JTextField(40), constraints);

    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    add(filerBtn = new JButton(MainFrame.getIcon("open-file")), constraints);

    //second row
    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 1;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = new Insets(0, 0, 0, 5);
    add(new JLabel("Encoding:"), constraints);


    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 1;
    constraints.gridwidth = 4;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    add(encodingTextField = new JTextField(15), constraints);
  }

  /**
   * Adds listeners for UI components
   */
  protected void initListeners(){
    filerBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser filer = MainFrame.getFileChooser();
        filer.setFileSelectionMode(JFileChooser.FILES_ONLY);
        filer.setDialogTitle("Select a file");

        filer.resetChoosableFileFilters();
        filer.setAcceptAllFileFilterUsed(true);
        filer.setFileFilter(filer.getAcceptAllFileFilter());
        int res = filer.showOpenDialog(TrecWebFileInputDialog.this);
        if(res == JFileChooser.APPROVE_OPTION){
          try {
            urlTextField.setText(filer.getSelectedFile().
                                 toURI().toURL().toExternalForm());
          } catch(IOException ioe){}
        }
      }
    });
  }

  /**
   * Sets the values for the URL string. This value is not cached so the set
   * will actually the text in the text field itself
   */
  public void setUrlString(String urlString) {
    urlTextField.setText(urlString);
  }

  /**
   * Gets the current text in the URL text field.
   */
  public String getUrlString() {
    return urlTextField.getText();
  }

  /**
   * Gets the encoding selected by the user.
   */
  public String getEncoding(){
    return encodingTextField.getText();
  }

  /**
   * Sets the initila value for the encoding field.
   */
  public void setEncoding(String enc){
    encodingTextField.setText(enc);
  }

  /**
   * Test code
   */
  static public void main(String[] args){
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      Gate.init();
    }catch(Exception e){
      e.printStackTrace();
    }
    JFrame frame = new JFrame("Foo");
    TrecWebFileInputDialog comp = new TrecWebFileInputDialog();
    frame.getContentPane().add(comp);
    frame.pack();
    frame.setResizable(false);
    frame.setVisible(true);
  }

  /**
   * The text field for the directory URL
   */
  JTextField urlTextField;

  /**
   * The buttons that opens the file chooser
   */
  JButton filerBtn;

  /**
   * The textField for the encoding
   */
  JTextField encodingTextField;
}