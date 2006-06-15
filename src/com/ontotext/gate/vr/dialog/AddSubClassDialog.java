/*AddSubClassDialog*/
package com.ontotext.gate.vr.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


import com.ontotext.gate.vr.*;

import gate.creole.ResourceInstantiationException;
import gate.util.*;
import gate.creole.ontology.*;

/**The dialog for adding a sub class*/
public class AddSubClassDialog extends JDialog {
  protected GridLayout gridLayout1 = new GridLayout();
  protected JLabel jLabel1 = new JLabel();
  protected JTextField nameField = new JTextField();
  protected JLabel jLabel2 = new JLabel();
  protected JTextField commentField = new JTextField();
  protected JButton btnOk = new JButton();
  protected JButton btnCancel = new JButton();

  /**reference to the ontology editor that invoked this*/
  private OntologyEditor editor;
  /**reference to the class node which is the root for the sub class being added*/
  private ClassNode root;

  public AddSubClassDialog() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace(gate.util.Err.getPrintWriter());
    }

  }
  private void jbInit() throws Exception {
    jLabel1.setText("Sub Class Name");
    gridLayout1.setColumns(2);
    gridLayout1.setRows(3);
    this.getContentPane().setLayout(gridLayout1);
    jLabel2.setText("Comment");
    btnOk.setBorder(BorderFactory.createEtchedBorder());
    btnOk.setMaximumSize(new Dimension(30, 20));
    btnOk.setMinimumSize(new Dimension(30, 20));
    btnOk.setPreferredSize(new Dimension(30, 20));
    btnOk.setMnemonic('0');
    btnOk.setText("OK");
    btnCancel.setBorder(BorderFactory.createEtchedBorder());
    btnCancel.setText("CANCEL");
    this.setTitle("");
    this.getContentPane().add(jLabel1, null);
    this.getContentPane().add(nameField, null);
    this.getContentPane().add(jLabel2, null);
    this.getContentPane().add(commentField, null);
    this.getContentPane().add(btnOk, null);
    this.getContentPane().add(btnCancel, null);

    this.setSize(new Dimension(300, 91));

    this.btnOk.addActionListener(new BtnListener());
    this.btnCancel.addActionListener(new BtnListener());
    this.addKeyListener(new EnterEscListener());
  }

  public void setInvokers(OntologyEditor oe, ClassNode node ) {
    root = node;
    editor = oe;
  }

  /**A button listener */
  private class BtnListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (btnCancel == e.getSource()) {
        AddSubClassDialog.this.setVisible(false);
      } // if cancel

      if (btnOk == e.getSource()) {

        if (null == editor)
          throw new GateRuntimeException("reference to the editor not set \n"
          +"in addSubClassDialog");

        if (null == root )
          throw new GateRuntimeException(
          "reference to the element to which to add the sub class is not set \n"
          +"in addSubClassDialog");

        editor.addSubClass(root,
            AddSubClassDialog.this.nameField.getText(),
            AddSubClassDialog.this.commentField.getText());
        AddSubClassDialog.this.setVisible(false);
      } // if ok
    } // actionPerformed();
  }  // class btnListener

  /**
   * KeyListener to monitor Enter and Esc.
   */
  private class EnterEscListener implements KeyListener {
    public void keyTyped(KeyEvent kev){};
    public void keyReleased(KeyEvent kev) {};
    public void keyPressed(KeyEvent kev) {
      if (kev.VK_ENTER == kev.getKeyCode()) {
        if (null == editor)
          throw new GateRuntimeException("reference to the editor not set \n"
          +"in addSubClassDialog");

        if (null == root )
          throw new GateRuntimeException(
          "reference to the element to which to add the sub class is not set \n"
          +"in addSubClassDialog");

          editor.addSubClass(root,
              AddSubClassDialog.this.nameField.getText(),
              AddSubClassDialog.this.commentField.getText());

          AddSubClassDialog.this.setVisible(false);

      } // if enter
      else {
        if (kev.VK_ESCAPE == kev.getKeyCode()) {
          AddSubClassDialog.this.setVisible(false);
        } // if escape
      } // else
    } // keyPressed
  }  // class enterEscListener



}//class AddSubClassDialog