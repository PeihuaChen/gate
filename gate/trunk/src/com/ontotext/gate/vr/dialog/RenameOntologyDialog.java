package com.ontotext.gate.vr.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import gate.creole.ontology.*;
import gate.util.*;

import com.ontotext.gate.vr.*;



/**Rename Ontology Dialog*/
public class RenameOntologyDialog extends JDialog {
  protected GridLayout gridLayout1 = new GridLayout();
  protected JLabel jLabel1 = new JLabel();
  public JTextField nameField = new JTextField();
  protected JLabel jLabel2 = new JLabel();
  public JTextField commentField = new JTextField();
  protected JButton btnOk = new JButton();
  protected JButton btnCancel = new JButton();

  /**reference to the ontology editor that invoked this*/
  private OntologyEditor editor;
  /** reference to the ontology being renamed */
  private Ontology ontology;

  /**construct the dialog
   * @param e the editor
   * @param o the ontology being renamed   */
  public RenameOntologyDialog(OntologyEditor e, Ontology o) {
    if ( null == e )
      throw new GateRuntimeException("editor is null, on constructing RenameOntologyDialog");

    if ( null == o )
      throw new GateRuntimeException("ontology is null, on constructing RenameOntologyDialog");

    ontology = o;
    editor = e;

    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }

  }// constructor(editor,ontology)

  private void jbInit() throws Exception {
    jLabel1.setText("Name");
    gridLayout1.setColumns(2);
    gridLayout1.setRows(3);
    this.getContentPane().setLayout(gridLayout1);
    jLabel2.setText("Comment");
    btnOk.setMaximumSize(new Dimension(30, 20));
    btnOk.setMinimumSize(new Dimension(30, 20));
    btnOk.setPreferredSize(new Dimension(30, 20));
    btnOk.setMnemonic('0');
    btnOk.setText("OK");
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

  /**a button listener */
  private class BtnListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (btnCancel == e.getSource()) {
        RenameOntologyDialog.this.setVisible(false);
      } // if cancel

      if (btnOk == e.getSource()) {

        if (null == editor)
            throw new GateRuntimeException("reference to the editor not set \n"
            +"in RenameOntologyDialog");

        if (null == ontology)
            throw new GateRuntimeException("reference to the ontology not set \n"
            +"in RenameOntologyDialog");

        ontology.setName(RenameOntologyDialog.this.nameField.getText());
        ontology.setComment(RenameOntologyDialog.this.commentField.getText());

        gate.CreoleRegister reg = gate.Gate.getCreoleRegister();
        reg.setResourceName(ontology,RenameOntologyDialog.this.nameField.getText());

        RenameOntologyDialog.this.setVisible(false);
      } // if ok
    } // actionPerformed();
  }  // class btnListener


  /** an Enter and Esc KeyListener */
  private class EnterEscListener implements KeyListener {
    public void keyTyped(KeyEvent kev){};
    public void keyReleased(KeyEvent kev) {};
    public void keyPressed(KeyEvent kev) {
      if (kev.VK_ENTER == kev.getKeyCode()) {
        if (null == editor)
          throw new GateRuntimeException("reference to the editor not set \n"
          +"in RenameOntologyDialog");

        if (null == ontology )
          throw new GateRuntimeException(
          "reference to the ontolgy to be renamed is null \n"
          +"in RenameOntologyDialog");

          ontology.setName(RenameOntologyDialog.this.nameField.getText());

          ontology.setComment(RenameOntologyDialog.this.commentField.getText());

          gate.CreoleRegister reg = gate.Gate.getCreoleRegister();
          reg.setResourceName(ontology,RenameOntologyDialog.this.nameField.getText());


          RenameOntologyDialog.this.setVisible(false);

      } // if enter
      else {
        if (kev.VK_ESCAPE == kev.getKeyCode()) {
          RenameOntologyDialog.this.setVisible(false);
        } // if escape
      } // else
    } // keyReleased
  }  // class enterEscListener



}//class RenameOntologyDialog