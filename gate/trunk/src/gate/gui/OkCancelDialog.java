/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 16/07/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A simple modal dialog that displays a component provided by the user along
 * with two buttons ("OK" and "Cancel").
 */
public class OkCancelDialog extends JDialog {

  protected OkCancelDialog(Frame owner, String title, Component contents){
    super(owner, title);
    init(contents);
  }

  protected OkCancelDialog(Dialog owner, String title, Component contents){
    super(owner, title);
    init(contents);
  }

  protected void init(Component contents){
    //fill in the contents
    JPanel vBox = new JPanel();
    vBox.setLayout(new BoxLayout(vBox, BoxLayout.Y_AXIS));

    JPanel contentsPanel = new JPanel();
    contentsPanel.add(contents);
    contentsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    vBox.add(contentsPanel);

    JPanel buttonsBox = new JPanel();
    buttonsBox.setLayout(new BoxLayout(buttonsBox, BoxLayout.X_AXIS));
    buttonsBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    okButton = new JButton("OK");
    cancelButton = new JButton("Cancel");
    buttonsBox.add(Box.createHorizontalGlue());
    buttonsBox.add(okButton);
    buttonsBox.add(Box.createHorizontalStrut(20));
    buttonsBox.add(cancelButton);
    buttonsBox.add(Box.createHorizontalGlue());

    vBox.add(buttonsBox);
    vBox.add(Box.createVerticalStrut(10));

    getContentPane().add(vBox, BorderLayout.CENTER);


    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        userHasPressedOK = true;
        hide();
      }
    });

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        userHasPressedCancel = true;
        hide();
      }
    });
  }


  public void show(){
    setModal(true);
    userHasPressedOK = false;
    userHasPressedCancel = false;
    super.show();
  }

  /**
   * @returns true if the user has selected the "OK" button.
   */
  public static boolean showDialog(Component parentComponent,
                                   Component contents,
                                   String title){
    //construct the dialog
    Window parent = SwingUtilities.getWindowAncestor(parentComponent);
    OkCancelDialog dialog;
    if(parent instanceof Frame){
      dialog = new OkCancelDialog((Frame)parent, title, contents);
    } else{
      dialog = new OkCancelDialog((Dialog)parent, title, contents);
    }

    //position the dialog
    dialog.pack();
    dialog.setLocationRelativeTo(parentComponent);

    //kalina: make it fit the screen
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension dialogSize = dialog.getSize();
    if (dialogSize.height > screenSize.height)
      dialogSize.height = screenSize.height;
    if (dialogSize.width > screenSize.width)
      dialogSize.width = screenSize.width;
    dialog.setSize(dialogSize);
    //end kalina

    //show the dialog
    dialog.show();
    return dialog.userHasPressedOK;
  }

  protected JButton okButton;
  protected JButton cancelButton;
  protected boolean userHasPressedOK;
  protected static boolean userHasPressedCancel;
}