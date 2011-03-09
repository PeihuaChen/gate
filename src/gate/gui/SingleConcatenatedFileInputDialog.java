/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Niraj Aswani, 14/Feb/2008
 *
 *  $Id: SingleConcatenatedFileInputDialog.java $
 */
package gate.gui;

import gate.Gate;
import gate.corpora.DocType;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * A simple component that allows the user to select a trec web file and
 * encoding
 */

public class SingleConcatenatedFileInputDialog extends JPanel {

  public SingleConcatenatedFileInputDialog() {
    initGUIComponents();
    initListeners();
  }

  /**
   * Creates the UI
   */
  protected void initGUIComponents() {
    setLayout(new GridBagLayout());
    // first row
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 0;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = new Insets(0, 0, 0, 5);
    add(new JLabel("File URL:"), constraints);

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

    // second row
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

    // third row
    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 2;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = new Insets(0, 0, 0, 5);
    add(new JLabel("Root Element:"), constraints);

    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 2;
    constraints.gridwidth = 4;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    add(documentRootElementTextField = new JTextField("DOC", 15), constraints);

    // fourth row
    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 3;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = new Insets(0, 0, 0, 5);
    add(new JLabel("Document type:"), constraints);

    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 3;
    constraints.gridwidth = 4;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    documentTypeComboBox = new JComboBox(DocType.values());
    documentTypeComboBox.setEditable(false);
    add(documentTypeComboBox, constraints);

    // fifth row
    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 4;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = new Insets(0, 0, 0, 5);
    add(new JLabel("No. of Docs:"), constraints);

    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 4;
    constraints.gridwidth = 4;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    add(numOfDocumentsToFetchTextField = new JTextField("-1", 15), constraints);

    // sixth row
    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 5;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = new Insets(0, 0, 0, 5);
    add(new JLabel("Prefix for documents:"), constraints);

    constraints = new GridBagConstraints();
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 5;
    constraints.gridwidth = 4;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    add(documentNamePrefixTextField = new JTextField("Document", 15),
            constraints);
  }

  /**
   * Adds listeners for UI components
   */
  protected void initListeners() {
    filerBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser filer = MainFrame.getFileChooser();
        filer.setFileSelectionMode(JFileChooser.FILES_ONLY);
        filer.setDialogTitle("Select a file");

        filer.resetChoosableFileFilters();
        filer.setAcceptAllFileFilterUsed(true);
        filer.setFileFilter(filer.getAcceptAllFileFilter());
        int res = filer.showOpenDialog(SingleConcatenatedFileInputDialog.this);
        if(res == JFileChooser.APPROVE_OPTION) {
          try {
            urlTextField.setText(filer.getSelectedFile().toURI().toURL()
                    .toExternalForm());
          }
          catch(IOException ioe) {
          }
        }
      }
    });
  }

  /**
   * Sets the values for the URL string. This value is not cached so the
   * set will actually the text in the text field itself
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
  public String getEncoding() {
    return encodingTextField.getText();
  }

  /**
   * Sets the initila value for the encoding field.
   */
  public void setEncoding(String enc) {
    encodingTextField.setText(enc);
  }

  /**
   * Gets the document root element set by user
   * 
   * @return
   */
  public String getDocumentRootElement() {
    return documentRootElementTextField.getText();
  }

  /**
   * Sets the value for documentRootElement field
   * 
   * @param documentRootElement
   */
  public void setDocumentRootElement(String documentRootElement) {
    this.documentRootElementTextField.setText(documentRootElement);
  }

  /**
   * Gets the document name prefix set by user
   * 
   * @return
   */
  public String getDocumentNamePrefix() {
    return documentNamePrefixTextField.getText();
  }

  /**
   * Sets the value for document name prefix
   * 
   * @param documentNamePrefix
   */
  public void setDocumentNamePrefix(String documentNamePrefix) {
    this.documentNamePrefixTextField.setText(documentNamePrefix);
  }

  /**
   * Gets the selected document type.
   * 
   * @return
   */
  public DocType getDocumentType() {
    return (DocType)this.documentTypeComboBox.getSelectedItem();
  }

  /**
   * Sets the document type
   * 
   * @param documentType
   */
  public void setDocumentType(DocType documentType) {
    this.documentTypeComboBox.setSelectedItem(documentType);
  }

  /**
   * Returns the number of documents to fetch
   * 
   * @return
   */
  public int getNumOfDocumentsToFetch() {
    if(this.numOfDocumentsToFetchTextField.getText().trim().length() == 0) {
      return -1;
    }
    else {
      return Integer.parseInt(this.numOfDocumentsToFetchTextField.getText()
              .trim());
    }
  }

  /**
   * Sets the number of documents to fetch
   * 
   * @param numOfDocumentsToFetch
   */
  public void setNumOfDocumentsToFetch(int numOfDocumentsToFetch) {
    this.numOfDocumentsToFetchTextField.setText("" + numOfDocumentsToFetch);
  }

  /**
   * Test code
   */
  static public void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      Gate.init();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    JFrame frame = new JFrame("Foo");
    SingleConcatenatedFileInputDialog comp = new SingleConcatenatedFileInputDialog();
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

  /**
   * The textField for the document root element
   */
  JTextField documentRootElementTextField;

  /**
   * The textField for the document name prefix
   */
  JTextField documentNamePrefixTextField;

  /**
   * Dropdown box with available document types
   */
  JComboBox documentTypeComboBox;

  /**
   * Number of documents to extract from the big document
   */
  JTextField numOfDocumentsToFetchTextField;

}
