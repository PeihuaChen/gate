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
  protected JPopupMenu userMenu = new JPopupMenu();
  protected JPopupMenu groupMenu = new JPopupMenu();

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

    showUsersFirst();

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
//    jPanel2.setLayout(new BorderLayout(40, 40));
    jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));


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
    jPanel2.add(new JScrollPane(firstList), BorderLayout.WEST);
    jPanel2.add(Box.createHorizontalStrut(50));
    jPanel2.add(new JScrollPane(secondList), BorderLayout.EAST);
    firstList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    firstList.setModel(new DefaultListModel());
    firstList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        listRightMouseClick(e);
      }//mouse clicked
    });
    firstList.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          firstListItemSelected(e);
        }//
      }//the selection listener
    );
    secondList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    secondList.setModel(new DefaultListModel());
    secondList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        listRightMouseClick(e);
      }//mouse clicked
    });

    this.add(Box.createVerticalGlue());

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
    DefaultListModel firstListData = (DefaultListModel) firstList.getModel();
    DefaultListModel secondListData = (DefaultListModel) secondList.getModel();
    firstListData.clear();
    secondListData.clear();

    readUsers(firstListData, firstList);
  }

  private void readUsers(DefaultListModel listModel, JList list) {
    //get the names of all users
    try {
      java.util.List users = controller.listUsers();
      for (int i = 0; i < users.size(); i++)
        listModel.addElement(users.get(i));
      list.setModel(listModel);
    } catch (gate.persist.PersistenceException ex) {
      throw new gate.util.GateRuntimeException("Cannot read users!");
    }

  }//readUsers

  private void showGroupsFirst() {
    DefaultListModel firstListData = (DefaultListModel) firstList.getModel();
    DefaultListModel secondListData = (DefaultListModel) secondList.getModel();
    firstListData.clear();
    secondListData.clear();

    readGroups(firstListData, firstList);
  }

  private void readGroups(DefaultListModel listModel, JList list) {
    //get the names of all groups
    try {
      java.util.List groups = controller.listGroups();
      for (int i = 0; i < groups.size(); i++)
        listModel.addElement(groups.get(i));
      list.setModel(listModel);
    } catch (gate.persist.PersistenceException ex) {
      throw new gate.util.GateRuntimeException("Cannot read groups!");
    }

  }//readGroups

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

  void listRightMouseClick(MouseEvent e) {
        //if it's not a right click, then return
        //coz we're not interested
        if (! SwingUtilities.isRightMouseButton(e))
          return;

        JList theList = (JList) e.getSource();
        //check if we have a selection and if not, try to force it
        if (theList.getSelectedIndex() == -1) {
          int index = theList.locationToIndex(e.getPoint());
          if (index == -1)
            return;
          else
            theList.setSelectedIndex(index);
        } else
            //if the right click is outside the currently selected item return
            if ( theList.locationToIndex(e.getPoint())
                 !=  theList.getSelectedIndex())
              return;


        if (theList.equals(firstList)) {
          if (usersFirst)
            showUsersMenu(theList,
                          (int) e.getPoint().getX(),
                          (int) e.getPoint().getY());
          else
            showGroupsMenu(theList,
                          (int) e.getPoint().getX(),
                          (int) e.getPoint().getY());

        } else {
          if (usersFirst)
            showGroupsMenu(theList,
                          (int) e.getPoint().getX(),
                          (int) e.getPoint().getY());
          else
            showUsersMenu(theList,
                          (int) e.getPoint().getX(),
                          (int) e.getPoint().getY());

        }

  }

  private void showUsersMenu(JList source, int x, int y) {
    //create the menu items first
    userMenu.removeAll();
    userMenu.add(new CreateUserAction(source));
    userMenu.add(new DeleteUserAction(source));
    userMenu.addSeparator();
    userMenu.add(new Add2GroupAction(source));
    userMenu.add(new RemoveFromGroupAction(source));
    userMenu.addSeparator();
    userMenu.add(new ChangePasswordAction(source));
    userMenu.add(new RenameUserAction(source));

    userMenu.show(source, x, y);

  }//create and show the menu for user manipulation

  private void showGroupsMenu(JList source, int x, int y) {
    //create the menu items first
    groupMenu.removeAll();
    groupMenu.add(new AddGroupAction(source));
    groupMenu.add(new DeleteGroupAction(source));
    groupMenu.addSeparator();
    groupMenu.add(new AddUserAction(source));
    groupMenu.add(new RemoveUserAction(source));
    groupMenu.addSeparator();
    groupMenu.add(new RenameGroupAction(source));

    groupMenu.show(source, x, y);

  }

  //called when the selection changes in the first list
  void firstListItemSelected(ListSelectionEvent e) {
    int i = firstList.getSelectedIndex();
    String name = (String) firstList.getModel().getElementAt(i);

    if (usersFirst)
      showGroupsForUser(name);
    else
      showUsersForGroup(name);
  } //firstListItemSelected

  protected void showGroupsForUser(String name) {
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

  }//showGroupsForUser


  protected void showUsersForGroup(String name) {
    Group group = null;
    try {
      group = controller.findGroup(name);
    } catch (gate.persist.PersistenceException ex) {
      throw new gate.util.GateRuntimeException(
                  "Cannot locate the group with name: " + name
                );
    } catch (gate.security.SecurityException ex1) {
      throw new gate.util.GateRuntimeException(
                  ex1.getMessage()
                );
    }
    if (group == null)
      return;
    java.util.List myUsers = group.getUsers();
    if (myUsers == null)
      return;

      DefaultListModel secondListData = new DefaultListModel();

      for (int j = 0; j< myUsers.size(); j++) {
        try {
          User myUser = controller.findUser((Long) myUsers.get(j));
          secondListData.addElement(myUser.getName());
        } catch (Exception ex) {
          throw new gate.util.GateRuntimeException(
                  ex.getMessage()
                );
        }//catch
      }//for loop
      secondList.setModel(secondListData);

  }//showGroupsForUser


  protected class CreateUserAction extends AbstractAction{
    private JList source;

    public CreateUserAction(JList source){
      super("Create new user");
      this.source = source;
    }

    public void actionPerformed(ActionEvent e){
      try {
        controller.createUser("myUser", "myPassword", session);
      } catch (gate.persist.PersistenceException ex) {
        throw new gate.util.GateRuntimeException(ex.getMessage());
      } catch (gate.security.SecurityException ex1) {
        throw new gate.util.GateRuntimeException(ex1.getMessage());
      }
      DefaultListModel model = (DefaultListModel) source.getModel();
      model.clear();
      readUsers(model, source);
    }//public void actionPerformed(ActionEvent e)
  } //CreateUserAction

  protected class DeleteUserAction extends AbstractAction{
    private JList source;

    public DeleteUserAction(JList source){
      super("Delete user");
      this.source = source;
    }

    public void actionPerformed(ActionEvent e){
      //first get the index of the selection
      int index = source.getSelectedIndex();
      if (index == -1) //return if no selection
        return;
      DefaultListModel model = (DefaultListModel) source.getModel();
      try {
      User user = controller.findUser((String) model.get(index) );
      controller.deleteUser(user, session);
      model.remove(index);
      } catch (gate.persist.PersistenceException ex) {
        throw new gate.util.GateRuntimeException(ex.getMessage());
      } catch (gate.security.SecurityException ex1) {
        throw new gate.util.GateRuntimeException(ex1.getMessage());
      }
    }//public void actionPerformed(ActionEvent e)
  } //DeleteUserAction

  protected class Add2GroupAction extends AbstractAction{
    private JList source;

    public Add2GroupAction(JList source){
      super("Add to group");
      this.source = source;
    }

    public void actionPerformed(ActionEvent e){
      Out.prln("I need to add the user to a group!");
    }//public void actionPerformed(ActionEvent e)
  } //Add2GroupAction

  protected class RemoveFromGroupAction extends AbstractAction{
    private JList source;

    public RemoveFromGroupAction(JList source){
      super("Remove from group");
      this.source = source;
    }//

    public void actionPerformed(ActionEvent e){
      Out.prln("I need to remove the user from a group!");
    }//public void actionPerformed(ActionEvent e)
  } //RemoveFromGroupAction


  protected class ChangePasswordAction extends AbstractAction{
    private JList source;

    public ChangePasswordAction(JList source){
      super("Change password");
      this.source = source;
    }//

    public void actionPerformed(ActionEvent e){
      Out.prln("I need to change the password!");
    }//public void actionPerformed(ActionEvent e)
  } //ChangePasswordAction


  protected class RenameUserAction extends AbstractAction{
    private JList source;

    public RenameUserAction(JList source){
      super("Rename user");
      this.source = source;
    }//

    public void actionPerformed(ActionEvent e){
      Out.prln("I need to change the user's name!");
    }//public void actionPerformed(ActionEvent e)
  } //RenameUserAction


  protected class AddGroupAction extends AbstractAction{
    private JList source;

    public AddGroupAction(JList source){
      super("Create new group");
      this.source = source;
    }//

    public void actionPerformed(ActionEvent e){
      Out.prln("I need to create a new group!");
    }//public void actionPerformed(ActionEvent e)
  } //AddGroupAction


  protected class DeleteGroupAction extends AbstractAction{
    private JList source;

    public DeleteGroupAction(JList source){
      super("Delete group");
      this.source = source;
    }//

    public void actionPerformed(ActionEvent e){
      Out.prln("I need to delete the group!");
    }//public void actionPerformed(ActionEvent e)
  } //DeleteGroupAction


  protected class AddUserAction extends AbstractAction{
    private JList source;

    public AddUserAction(JList source){
      super("Add user");
      this.source = source;
    }//

    public void actionPerformed(ActionEvent e){
      Out.prln("I need to add a user to the group!");
    }//public void actionPerformed(ActionEvent e)
  } //AddUserAction


  protected class RemoveUserAction extends AbstractAction{
    private JList source;

    public RemoveUserAction(JList source){
      super("Remove user");
      this.source = source;
    }//

    public void actionPerformed(ActionEvent e){
      Out.prln("I need to remove a user from the group!");
    }//public void actionPerformed(ActionEvent e)
  } //RemoveUserAction


  protected class RenameGroupAction extends AbstractAction{
    private JList source;

    public RenameGroupAction(JList source){
      super("Rename group");
      this.source = source;
    }//

    public void actionPerformed(ActionEvent e){
      Out.prln("I need to rename the group!");
    }//public void actionPerformed(ActionEvent e)
  } //RenameGroupAction

} //UserGroupEditor

