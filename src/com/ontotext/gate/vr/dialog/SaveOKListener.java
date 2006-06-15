package com.ontotext.gate.vr.dialog;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;



/** Listeners like this one are used to be set to the
 *  OK button of the MultipleSelectionDialog. Thus,
 *  different actions could be performed on OK while the
 *  MultipleSelectionDialog stays flexible
 *  note: could be moved to MultipleSelectionDialog */
public class SaveOKListener implements ActionListener {
  MultipleSelectionDialog dialog;

  /**@param the dialog that this listener has been/shall be
   * associated with*/
  public SaveOKListener(MultipleSelectionDialog dialog) {
    if ( null == dialog )
      throw new gate.util.GateRuntimeException("dialog not set (is null)");
    this.dialog = dialog;
  }// constructor

  public void actionPerformed(ActionEvent e) {

    if ( dialog.okBtn == e.getSource()) {
      Object[] oarr = dialog.guiList.getSelectedValues();

      Vector selection = new Vector(oarr.length);
      for ( int i = 0 ; i < oarr.length ; i++ ) {
         selection.add(oarr[i]);
      }
      dialog.editor.saveOntologies(selection);
      dialog.dispose();
    } // if ok

  } // actionPerformed
}// class SaveOKListener
