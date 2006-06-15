package com.ontotext.gate.vr.dialog;
/*EditClassURIDialog*/
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import gate.creole.ontology.*;
import gate.util.*;

import com.ontotext.gate.vr.*;
/**The dialog for renaming an ontology*/
public class EditClassURIDialog extends JDialog {

  /**reference to the ontology editor that invoked this*/
  private OntologyEditor editor;

  /** reference to the ontology class being edited*/
  private TClass clas;

  /** reference to the ontology being edited*/
  private Taxonomy ontology;

  protected GridBagLayout gridBagLayout1 = new GridBagLayout();
  protected JButton btnOk = new JButton();
  protected JButton btnCancel = new JButton();
  protected JLabel jLabel1 = new JLabel();
  protected JComboBox comboURI = new JComboBox();

  /**
   * @param e the editor
   * @param o the ontology being renamed   */
  public EditClassURIDialog(OntologyEditor e, TClass c) {
    if ( null == e )
      throw new GateRuntimeException("editor is null, on constructing EditClassURIDialog");

    if ( null == c )
      throw new GateRuntimeException("ontology class is null, on constructing EditClassURIDialog");

    ontology = c.getOntology();

    if ( null == ontology )
      throw new gate.util.GateRuntimeException("ontology is null, on constructing EditClassURIDialog");

    clas = c;
    editor = e;

    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }

  }// constructor(editor,ontology)

  private void jbInit() throws Exception {
    btnOk.setBorder(BorderFactory.createEtchedBorder());
    btnOk.setText("OK");
    this.getContentPane().setLayout(gridBagLayout1);
    this.setTitle("");



    this.setSize(new Dimension(389, 111));

    this.addKeyListener(new EnterEscListener());
    btnCancel.setBorder(BorderFactory.createEtchedBorder());
    btnCancel.setText("Cancel");

    jLabel1.setText("URI ");

    comboURI = new JComboBox(new Vector(editor.getAllURIs(ontology)));
    comboURI.setSelectedItem(clas.getURI());
    comboURI.setEditable(true);

    this.getContentPane().add(comboURI,                  new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 0, 14, 0), -1, 0));
    this.getContentPane().add(jLabel1,       new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0));
    this.getContentPane().add(btnCancel,   new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 19), 0, 0));
    this.getContentPane().add(btnOk,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 22, 0, 0), 27, 0));


    btnOk.addActionListener(new BtnListener());
    btnCancel.addActionListener(new BtnListener());
    this.addKeyListener(new EnterEscListener());

 } // jbInit();

  /**a button listener */
  private class BtnListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (btnCancel == e.getSource()) {
        EditClassURIDialog.this.setVisible(false);
      } // if cancel

      if (btnOk == e.getSource()) {

        if (null == editor)
            throw new GateRuntimeException("reference to the editor not set \n"
            +"in EditClassURIDialog");

        if (null == ontology)
            throw new GateRuntimeException("reference to the ontology not set \n"
            +"in EditClassURIDialog");

        clas.setURI(
          (String)EditClassURIDialog.this.comboURI.getModel().getSelectedItem());

        EditClassURIDialog.this.setVisible(false);
      } // if ok
    } // actionPerformed();
  }  // class btnListener


  /* An Enter and Esc Key Listener */
  private class EnterEscListener implements KeyListener {
    public void keyTyped(KeyEvent kev){};
    public void keyReleased(KeyEvent kev) {};
    public void keyPressed(KeyEvent kev) {
      if (kev.VK_ENTER == kev.getKeyCode()) {
        if (null == editor)
          throw new GateRuntimeException("reference to the editor not set \n"
          +"in EditClassURIDialog");

        if (null == ontology )
          throw new GateRuntimeException(
          "reference to the ontolgy to be renamed is null \n"
          +"in EditClassURIDialog");

          clas.setURI(
            (String)EditClassURIDialog.this.comboURI.getModel().getSelectedItem());

          EditClassURIDialog.this.setVisible(false);

      } // if enter
      else {
        if (kev.VK_ESCAPE == kev.getKeyCode()) {
          EditClassURIDialog.this.setVisible(false);
        } // if escape
      } // else
    } // keyReleased
  }


} // class EditClassURIDialog