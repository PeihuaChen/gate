package com.ontotext.gate.vr.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

import java.net.*;
import java.io.*;


import com.ontotext.gate.vr.*;

import gate.util.*;
import gate.creole.ontology.*;


/**New Ontology Dialog. Creates a new ontology.
 */
public class NewOntologyDialog extends JDialog {
  /**reference to the ontology editor which invokde this dialog */
  protected OntologyEditor editor;

  protected GridBagLayout gridBagLayout1 = new GridBagLayout();
  protected JTextField name = new JTextField();
  protected JLabel jLabel1 = new JLabel();
  protected JLabel jLabel2 = new JLabel();
  protected JLabel jLabel3 = new JLabel();
  protected JTextField sourceURI = new JTextField();
  protected JTextField url = new JTextField();
  protected JLabel jLabel4 = new JLabel();
  protected JTextField comment = new JTextField();
  protected JButton btnOk = new JButton();
  protected JButton btnCancel = new JButton();
  protected TitledBorder titledBorder1;

  public NewOntologyDialog(OntologyEditor e) {
    if ( null == e ) {
      throw new GateRuntimeException(
      "null ontology editor passed to the constructor of NewOntologyDialog");
    }
    editor = e;
    try {
      jbInit();
      initListeners();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder("");
    jLabel1.setText("Name");
    this.getContentPane().setLayout(gridBagLayout1);
    jLabel2.setText("Source URI");
    jLabel3.setText("URL");
    jLabel4.setText("Comment");
    btnOk.setBorder(BorderFactory.createEtchedBorder());
    btnOk.setNextFocusableComponent(btnCancel);
    btnOk.setText("OK");
    btnCancel.setBorder(BorderFactory.createEtchedBorder());
    btnCancel.setNextFocusableComponent(name);
    btnCancel.setText("Cancel");
    if (!name.requestDefaultFocus())
      name.requestFocus();
    name.setNextFocusableComponent(sourceURI);
    sourceURI.setNextFocusableComponent(url);
    url.setNextFocusableComponent(comment);
    comment.setNextFocusableComponent(btnOk);
    this.setResizable(false);
    this.getContentPane().add(jLabel1,                       new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(13, 0, 0, 0), 33, 0));
    this.getContentPane().add(jLabel2,                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 6, 0));
    this.getContentPane().add(jLabel3,                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 41, 0));
    this.getContentPane().add(sourceURI,                     new GridBagConstraints(1, 1, 4, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 7, 7, 0), 186, 0));
    this.getContentPane().add(jLabel4,             new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 10, 0));
    this.getContentPane().add(comment,                              new GridBagConstraints(1, 3, 6, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 8, 8, 0), 186, 0));
    this.getContentPane().add(name,                         new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 7, 8, 0), 130, 0));
    this.getContentPane().add(url,                   new GridBagConstraints(1, 2, 5, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(9, 7, 8, 0), 186, 0));
    this.getContentPane().add(btnOk,             new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 8, 0), 29, 0));
    this.getContentPane().add(btnCancel,     new GridBagConstraints(2, 4, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 0, 11, 7), 44, 0));

  }

  /**
   * initializes the listeners of the dialog.
   */
  private void initListeners() {
    this.btnOk.addActionListener(new BtnListener());
    this.btnCancel.addActionListener(new BtnListener());
    this.addKeyListener(new EnterEscListener());

    this.setSize(300,245);
  } // init Listeners

  /**a button listener */
  private class BtnListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      try {
        if (btnCancel == e.getSource()) {
          NewOntologyDialog.this.setVisible(false);
        } // if cancel

        if (btnOk == e.getSource()) {

          if (null == editor)
            throw new GateRuntimeException("reference to the editor not set \n"
            +"in addSubClassDialog");

          try {
            java.net.URL u = new URL(url.getText());
            String pth = u.getPath();
            if (-1 != u.getProtocol().indexOf("gate")) {
              u = gate.util.protocols.gate.Handler.class.getResource(
                            Files.getResourcePath()
                          );
            } // if gate:path url

            File f;
            if (u.getPath().equals(pth)) {
              f = new File(pth);
            } else {
              f = new File(u.getPath()+pth);
            }
            if (!f.exists()) {
              f.createNewFile();
            } // if file doesn't exist

            editor.createOntology(
              name.getText(),
              sourceURI.getText(),
              url.getText(),
              comment.getText());

            NewOntologyDialog.this.setVisible(false);

          } catch (java.net.MalformedURLException ux) {
            JOptionPane.showMessageDialog(null,
              "This is not a valid URL:\n"+
              url.getText(),"Create Ontology Failure",
              JOptionPane.ERROR_MESSAGE);
          }//catch
           catch (IOException ioe) {
            JOptionPane.showMessageDialog(null,
              "Cannot Create New Ontology \n Due to :"+
              ioe.getMessage(),"Create Ontology Failure",
              JOptionPane.ERROR_MESSAGE);
          }//catch
        } // if ok
      } catch (gate.creole.ResourceInstantiationException x) {
        x.printStackTrace(gate.util.Err.getPrintWriter());
      }
    } // actionPerformed();
  }  // class btnListener

  /**key listener class */
  private class EnterEscListener implements KeyListener {
    public void keyTyped(KeyEvent kev){};
    public void keyReleased(KeyEvent kev) {};
    public void keyPressed(KeyEvent kev) {
      try {
        if (kev.VK_ENTER == kev.getKeyCode()) {

          if (null == editor)
            throw new GateRuntimeException("reference to the editor not set \n"
            +"in addSubClassDialog");

          try {
            java.net.URL u = new URL(url.getText());
            String pth = u.getPath();
            if (-1 != u.getProtocol().indexOf("gate")) {
              u = gate.util.protocols.gate.Handler.class.getResource(
                            Files.getResourcePath()
                          );
            } // if gate:path url

            File f;
            if (u.getPath().equals(pth)) {
              f = new File(pth);
            } else {
              f = new File(u.getPath()+pth);
            }
            if (!f.exists()) {
              f.createNewFile();
            } // if file doesn't exist

            editor.createOntology(
              name.getText(),
              sourceURI.getText(),
              url.getText(),
              comment.getText());

            NewOntologyDialog.this.setVisible(false);

          } catch (java.net.MalformedURLException ux) {
            JOptionPane.showMessageDialog(null,
              "This is not a valid URL:\n"+
              url.getText(),"Create Ontology Failure",
              JOptionPane.ERROR_MESSAGE);
          }//catch
           catch (IOException ioe) {
            JOptionPane.showMessageDialog(null,
              "Cannot Create New Ontology \n Due to :"+
              ioe.getMessage(),"Create Ontology Failure",
              JOptionPane.ERROR_MESSAGE);
          }//catch
        } // if enter
        else {
          if (kev.VK_ESCAPE == kev.getKeyCode()) {
            NewOntologyDialog.this.setVisible(false);
          } // if escape
        } // else
      } catch (gate.creole.ResourceInstantiationException x) {
        x.printStackTrace(gate.util.Err.getPrintWriter());
      }

    } // keyPressed();
  }  // class enterEscListener


} // class NewOntologyDialog