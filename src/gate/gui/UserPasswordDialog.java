/*  UserPasswordDialog.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva,  03/October/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import javax.swing.*;
import java.awt.*;


public class UserPasswordDialog {

  String userName = "";
  String userPass = "";

  public UserPasswordDialog() {
  }

  public boolean showPasswordDialog(String message, Component parent) {

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel,BoxLayout.X_AXIS));

    JPanel panel1 = new JPanel();
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
    panel1.add(new JLabel("User name: "));
    panel1.add(new JLabel("Password: "));

    JPanel panel2 = new JPanel();
    panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS));
    JTextField usrField = new JTextField(30);
    panel2.add(usrField);
    JPasswordField pwdField = new JPasswordField(30);
    panel2.add(pwdField);

    listPanel.add(panel1);
    listPanel.add(Box.createHorizontalStrut(30));
    listPanel.add(panel2);

    if(OkCancelDialog.showDialog( parent,
                                  listPanel,
                                  message)){
      userName = usrField.getText();
      userPass = new String(pwdField.getPassword());
      return true;
    }

    return false;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return userPass;
  }

}