/*  UserGroupEditor.java
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
import javax.swing.event.*;
import java.awt.*;
import gate.security.*;
import gate.*;
import java.awt.event.*;
import gate.util.Out;



public class UserGroupEditor extends JComponent {
  protected JPanel jPanel1 = new JPanel();
  protected JPanel jPanel2 = new JPanel();
  protected JList firstList = new JList();
  protected JList secondList = new JList();
  protected CardLayout cardLayout1 = new CardLayout();
  protected JRadioButton displayUsersFirst = new JRadioButton();
  protected JRadioButton displayGroupsFirst = new JRadioButton();

  protected Session session;
  protected AccessController controller;

  protected boolean usersFirst = true;
  protected JButton exitButton = new JButton();

  /** JDBC URL */
  private static final String JDBC_URL =
//            "jdbc:oracle:thin:GATEUSER/gate@192.168.128.207:1521:GATE03";
            "jdbc:oracle:thin:GATEUSER/gate2@hope.dcs.shef.ac.uk:1521:GateDB";

  public UserGroupEditor(AccessController ac, Session theSession) {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    this.session = theSession;
    this.controller = ac;

    readSecurityData();

  }

  public static void main(String[] args) throws Exception {
    Gate.init();

    AccessController ac = new AccessControllerImpl();
    ac.open(JDBC_URL);

    Session mySession = ac.login("kalina", "sesame",
                              ac.findGroup("English Language Group").getID());

    UserGroupEditor userGroupEditor1 = new UserGroupEditor(ac, mySession);

    JFrame frame = new JFrame();

    //INITIALISE THE FRAME, ETC.
    frame.setEnabled(true);
    frame.setTitle("GATE User/Group Administration Tool");
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    //Put the bean in a scroll pane.
    frame.getContentPane().add(userGroupEditor1, BorderLayout.CENTER);

    //DISPLAY FRAME
    frame.pack();
    frame.setSize(800, 600);
    frame.show();

  }

  private void jbInit() throws Exception {
    this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    jPanel2.setLayout(new BorderLayout(40, 40));

//    jPanel1.setSize(800, 100);
//    jPanel2.setSize(800, 500);

    displayUsersFirst.setText("Groups per user");
    displayUsersFirst.setToolTipText("");
    displayUsersFirst.setActionCommand("usersFirst");
    displayUsersFirst.setSelected(true);
    displayUsersFirst.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        displayUsersFirst_itemStateChanged(e);
      }
    });
    displayGroupsFirst.setText("Users per group");
    displayGroupsFirst.setActionCommand("groupsFirst");

    this.add(jPanel1, null);
    ButtonGroup group = new ButtonGroup();
    group.add(displayUsersFirst);
    group.add(displayGroupsFirst);
    this.add(jPanel1);
    jPanel1.add(displayUsersFirst);
    jPanel1.add(Box.createHorizontalStrut(50));
    jPanel1.add(displayGroupsFirst);

    this.add(jPanel2, null);
    jPanel2.add(firstList, BorderLayout.WEST);
    jPanel2.add(secondList, BorderLayout.EAST);
    firstList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    firstList.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          firstListItemSelected(e);
        }//
      }//the selection listener
    );

    this.add(exitButton);
    exitButton.setText("Exit");
    exitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
/*
        try {
          controller.close();
        } catch (gate.persist.PersistenceException ex) {
          Out.prln("Could not close the access controller connection. Exiting...");
        }
*/
        System.exit(0);
      } //actionPerformed
    });
    this.add(Box.createVerticalStrut(50));
  }

  private void showUsersFirst() {
  }

  private void showGroupsFirst() {
    Out.prln("Groups go first");
  }

  void displayUsersFirst_itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == e.DESELECTED) {
      if (!usersFirst)
        return;
      displayGroupsFirst.setSelected(true);
      if (usersFirst)  //if it used to be users first, we need to change
        showGroupsFirst();
      usersFirst = false;
    } else {
      if (usersFirst)
        return;
      displayGroupsFirst.setSelected(false);
      if (! usersFirst)
        showUsersFirst();
      usersFirst = true;
    }
  } //display users first (de-)selected

  //called when the selection changes in the first list
  void firstListItemSelected(ListSelectionEvent e) {
    int i = e.getFirstIndex();
    String name = (String) firstList.getModel().getElementAt(i);
    User user = null;
    try {
      user = controller.findUser(name);
    } catch (gate.persist.PersistenceException ex) {
      throw new gate.util.GateRuntimeException(
                  "Cannot locate the user with name: " + name
                );
    } catch (gate.security.SecurityException ex1) {
      throw new gate.util.GateRuntimeException(
                  ex1.getMessage()
                );
    }
    if (user == null)
      return;
    java.util.List myGroups = user.getGroups();
    if (myGroups == null)
      return;

      DefaultListModel secondListData = new DefaultListModel();

      for (int j = 0; j< myGroups.size(); j++) {
        try {
          Group myGroup = controller.findGroup((Long) myGroups.get(j));
          secondListData.addElement(myGroup.getName());
        } catch (Exception ex) {
          throw new gate.util.GateRuntimeException(
                  ex.getMessage()
                );
        }//catch
      }//for loop
      secondList.setModel(secondListData);


  }//firstListItemSelected


  private void readSecurityData() {
    //get the names of all users
    try {
      java.util.List users = controller.listUsers();
      DefaultListModel firstListData = new DefaultListModel();
      for (int i = 0; i < users.size(); i++)
        firstListData.addElement(users.get(i));
      firstList.setModel(firstListData);
    } catch (gate.persist.PersistenceException ex) {
      throw new gate.util.GateRuntimeException("Cannot read users!");
    }

  }//readSecurityData

}