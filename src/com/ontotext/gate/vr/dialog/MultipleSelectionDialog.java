package com.ontotext.gate.vr.dialog;

import com.ontotext.gate.vr.*;
import gate.creole.ontology.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/** Multiple Selection Dialog. This dialog allows the selection of
 *  multiple items and performing
 *  a desired operation over them.  */
public class MultipleSelectionDialog extends JDialog {

  /**
   * the size of the dialog (x)
   */
  private final static int SIZE_X = 320;

  /**
   * the size of the dialog (y)
   */
  private final static int SIZE_Y = 385;


  /** reference to te ontology editor */
  protected OntologyEditor editor;

  /** the list of items */
  private Vector list;

  /** the text to be displayed */
  private String text;

  /**the title of the dialog*/
  private String title;

  protected GridBagLayout gridBagLayout1 = new GridBagLayout();
  protected JList guiList = new JList();
  protected JLabel guiText = new JLabel();
  protected JPanel buttonsPanel = new JPanel();
  protected JButton cancelBtn = new JButton();
  public JButton okBtn = new JButton();
  protected JButton noneBtn = new JButton();
  protected JButton allBtn = new JButton();

  /**
   * @param editor reference to the ontology editor
   * @param list the list of items in the dialog
   * @param text the text of the dialog
   * @param title the title of the dialog
   */
  public MultipleSelectionDialog(OntologyEditor editor, Vector list,
    String text, String title) {
    if ( null == editor )
      throw new gate.util.GateRuntimeException("the ontology editor reference is null");

    if ( null == list )
      throw new gate.util.GateRuntimeException("the items list of this dialog is null");

    this.editor = editor;
    this.list  = list;
    this.text = text;
    this.title = title;

    try {
      jbInit();
      this.setSize(SIZE_X,SIZE_Y);
    }
    catch(Exception e) {
      e.printStackTrace();
    }


  } // MultipleSelectionDialog

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(gridBagLayout1);
    guiList.setBorder(BorderFactory.createLoweredBevelBorder());
    this.setResizable(false);
    guiText.setAlignmentY((float) 0.0);
    guiText.setHorizontalAlignment(SwingConstants.CENTER);
    guiText.setHorizontalTextPosition(SwingConstants.CENTER);
    cancelBtn.setText("Cancel");
    okBtn.setText("OK");
    noneBtn.setText("None");
    allBtn.setText("All");
    this.getContentPane().add(guiList,                        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 238, 249));
    this.getContentPane().add(guiText,                        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 6, 0), 257, 21));
    this.getContentPane().add(buttonsPanel,   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 6));
    buttonsPanel.add(allBtn, null);
    buttonsPanel.add(noneBtn, null);
    buttonsPanel.add(okBtn, null);
    buttonsPanel.add(cancelBtn, null);

    /*end of automatic code */

    this.setTitle(title);
    this.guiText.setText(text);
    this.guiList.setListData(list);

    this.setSize(new Dimension(436, 331));

    allBtn.addActionListener(new BtnListener());
    cancelBtn.addActionListener(new BtnListener());
    noneBtn.addActionListener(new BtnListener());
    okBtn.addActionListener(new BtnListener());

    this.addKeyListener(new EscListener());
  } // jbInit();

  /**Button Listener*/
  private class BtnListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        if (allBtn == e.getSource()) {
          MultipleSelectionDialog.this.guiList.setSelectionInterval(0,list.size()-1);
        } // if all

        if (noneBtn == e.getSource() ) {
          guiList.removeSelectionInterval(0,list.size());
        } // if none

        if ( cancelBtn == e.getSource()) {
          MultipleSelectionDialog.this.dispose();
        } //if cancel
    } //actionPerformed
  } // class BtnListener

  /** lsitens for Esc */
  private class EscListener implements KeyListener {
    public void keyTyped(KeyEvent kev){};
    public void keyReleased(KeyEvent kev) {};
    public void keyPressed(KeyEvent kev) {
      if (kev.VK_ESCAPE == kev.getKeyCode()) {
        MultipleSelectionDialog.this.setVisible(false);
      } // if escape
    } // keyPressed
  }  // class EscListener

} // class MultipleSelectionDialog