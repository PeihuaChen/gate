package com.ontotext.gate.vr.dialog;
/*CloseOKListener*/
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


import com.ontotext.gate.vr.dialog.*;
/** Listeners like this one are used to be set to the
 *  OK button of the MultipleSelectionDialog. Thus,
 *  different actions could be performed on OK while the
 *  MultipleSelectionDialog stays flexible
 *  note: could be moved to MultipleSelectionDialog */
public class CloseOKListener implements ActionListener {
  MultipleSelectionDialog dialog;

  /**@param the dialog that this listener has been/shall be
   * associated with*/
  public CloseOKListener(MultipleSelectionDialog dialog) {
    if ( null == dialog )
      throw new gate.util.GateRuntimeException("dialog not set (is null)");
    this.dialog = dialog;
  }// constructor

  public void actionPerformed(ActionEvent e) {
    try {
      if ( dialog.okBtn == e.getSource()) {
        Object[] oarr = dialog.guiList.getSelectedValues();

        Vector selection = new Vector(oarr.length);
        for ( int i = 0 ; i < oarr.length ; i++ ) {
           selection.add(oarr[i]);
        }
        dialog.editor.closeOntologies(selection);
        dialog.dispose();
      } // if ok
    } catch (gate.creole.ResourceInstantiationException x) {
      x.printStackTrace(gate.util.Err.getPrintWriter());
    }

  } // actionPerformed
}// class CloseOKListener
