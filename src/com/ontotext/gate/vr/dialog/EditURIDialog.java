package com.ontotext.gate.vr.dialog;
/*EditURIDialog*/
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import gate.creole.ontology.*;
import gate.util.GateRuntimeException;

import com.ontotext.gate.vr.*;

/**The dialog for renaming an ontology*/
public class EditURIDialog extends JDialog {

  /**reference to the ontology editor that invoked this*/
  private OntologyEditor editor;
  /** reference to the ontology being renamed */
  private Taxonomy ontology;
  protected GridBagLayout gridBagLayout1 = new GridBagLayout();
  protected JButton btnOk = new JButton();
  protected JButton btnCancel = new JButton();
  protected JLabel jLabel1 = new JLabel();
  protected JComboBox comboURI = new JComboBox();

  /**construct the dialog
   * @param e the editor
   * @param o the ontology being renamed   */
  public EditURIDialog(OntologyEditor e, Taxonomy o) {
    if ( null == e )
      throw new GateRuntimeException("editor is null, on constructing EditURIDialog");

    if ( null == o )
      throw new GateRuntimeException("ontology is null, on constructing EditURIDialog");

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
    btnOk.setBorder(BorderFactory.createEtchedBorder());
    btnOk.setText("OK");
    this.getContentPane().setLayout(gridBagLayout1);
    this.setTitle("");



    this.setSize(new Dimension(360, 101));

    this.addKeyListener(new EnterEscListener());
    btnCancel.setBorder(BorderFactory.createEtchedBorder());
    btnCancel.setText("Cancel");
    jLabel1.setText("URI ");

    comboURI = new JComboBox(new Vector(editor.getAllURIs()));
    comboURI.setSelectedItem(ontology.getSourceURI());
    comboURI.setEditable(true);

    this.getContentPane().add(jLabel1,                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 16, 6, 0), 11, 0));
    this.getContentPane().add(comboURI,       new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(19, 3, 9, 19), 24, 0));
    this.getContentPane().add(btnOk,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 178, 2, 0), 21, 0));
    this.getContentPane().add(btnCancel,   new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 9, 3, 10), 6, 0));


    btnOk.addActionListener(new BtnListener());
    btnCancel.addActionListener(new BtnListener());
    this.addKeyListener(new EnterEscListener());

 } // jbInit();

  /**a button listener */
  private class BtnListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (btnCancel == e.getSource()) {
        EditURIDialog.this.setVisible(false);
      } // if cancel

      if (btnOk == e.getSource()) {

        if (null == editor)
            throw new GateRuntimeException("reference to the editor not set \n"
            +"in EditURIDialog");

        if (null == ontology)
            throw new GateRuntimeException("reference to the ontology not set \n"
            +"in EditURIDialog");

        ontology.setSourceURI(
          (String)EditURIDialog.this.comboURI.getModel().getSelectedItem());

        EditURIDialog.this.setVisible(false);
      } // if ok
    } // actionPerformed();
  }  // class btnListener


  private class EnterEscListener implements KeyListener {
    public void keyTyped(KeyEvent kev){};
    public void keyReleased(KeyEvent kev) {};
    public void keyPressed(KeyEvent kev) {
      if (kev.VK_ENTER == kev.getKeyCode()) {
        if (null == editor)
          throw new GateRuntimeException("reference to the editor not set \n"
          +"in EditURIDialog");

        if (null == ontology )
          throw new GateRuntimeException(
          "reference to the ontolgy to be renamed is null \n"
          +"in EditURIDialog");

          ontology.setSourceURI(
            (String)EditURIDialog.this.comboURI.getModel().getSelectedItem());

          EditURIDialog.this.setVisible(false);

      } // if enter
      else {
        if (kev.VK_ESCAPE == kev.getKeyCode()) {
          EditURIDialog.this.setVisible(false);
        } // if escape
      } // else
    } // keyReleased
  }  // class enterEscListener

} // class EditURIDialog