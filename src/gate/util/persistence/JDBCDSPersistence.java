/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 26/10/2001
 *
 *  $Id$
 *
 */
package gate.util.persistence;

import gate.*;
import gate.creole.*;
import gate.security.*;
import gate.util.*;
import gate.gui.OkCancelDialog;
import gate.persist.JDBCDataStore;
import gate.persist.PersistenceException;

import java.util.*;

import junit.framework.Assert;
import javax.swing.*;
/**
 * Adds security data storage to the DS persistence
 */
public class JDBCDSPersistence extends DSPersistence {

  /**
   * Populates this Persistence with the data that needs to be stored from the
   * original source object.
   */
  public void extractDataFromSource(Object source)throws PersistenceException{
    //check input
    if(! (source instanceof JDBCDataStore)){
      throw new UnsupportedOperationException(
                getClass().getName() + " can only be used for " +
                JDBCDataStore.class.getName() +
                " objects!\n" + source.getClass().getName() +
                " is not a " + JDBCDataStore.class.getName());
    }

    super.extractDataFromSource(source);

    JDBCDataStore ds = (JDBCDataStore)source;
    Map securityData = Gate.getDataStoreRegister().getSecurityData(ds);
    userName = ((User)securityData.get("user")).getName();
    userGroup = ((Group)securityData.get("group")).getName();
  }


  /**
   * Creates a new object from the data contained. This new object is supposed
   * to be a copy for the original object used as source for data extraction.
   */
  public Object createObject()throws PersistenceException,
                                     ResourceInstantiationException{

    AccessController ac = null;
    JDBCDataStore ds = null;
    User usr = null;
    Group grp = null;

    DataStoreRegister reg = Gate.getDataStoreRegister();
    boolean securityOK = false;
    Session mySession = null;
    //1. login the user;
    securityLoop: do{
      try{
        String userPass;
        ac = new AccessControllerImpl(storageUrlString);
        ac = Factory.createAccessController(storageUrlString);
        Assert.assertNotNull(ac);
        ac.open();

        try {
          Box listBox = Box.createHorizontalBox();

          Box vBox = Box.createVerticalBox();
          vBox.add(new JLabel("User name: "));
          vBox.add(new JLabel("Password: "));
          vBox.add(new JLabel("Group: "));
          listBox.add(vBox);
          listBox.add(Box.createHorizontalStrut(20));

          JPanel panel2 = new JPanel();
          panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS));
          vBox = Box.createVerticalBox();

          JTextField usrField = new JTextField(30);
          usrField.setText(userName);
          vBox.add(usrField);
          JPasswordField pwdField = new JPasswordField(30);
          vBox.add(pwdField);
          JTextField grpField = new JTextField(30);
          grpField.setText(userGroup);
          vBox.add(grpField);

          listBox.add(vBox);

          if(OkCancelDialog.showDialog(null, listBox,
                                       "Please re-enter login details")){
            userName = usrField.getText();
            userPass = new String(pwdField.getPassword());
            userGroup = grpField.getText();
            if (userName.equals("") || userPass.equals("") || userGroup.equals("")) {
              JOptionPane.showMessageDialog(
                null,
                "You must provide non-empty user name, password and group!",
                "Login error",
                JOptionPane.ERROR_MESSAGE
                );
              securityOK = false;
              continue securityLoop;
            }
          }else{
            //user cancelled
            try {
              if (ac != null)
                ac.close();
              if (ds != null)
                ds.close();
            } catch (gate.persist.PersistenceException ex) {
              JOptionPane.showMessageDialog(null, "Persistence error!\n " +
                                            ex.toString(),
                                            "Gate", JOptionPane.ERROR_MESSAGE);
            }
            throw new PersistenceException("User cancelled!");
          }

          grp = ac.findGroup(userGroup);
          usr = ac.findUser(userName);
          mySession = ac.login(userName, userPass, grp.getID());
        } catch (gate.security.SecurityException ex) {
            JOptionPane.showMessageDialog(
              null,
              "Authentication failed! Incorrect details entred.",
              "Login error",
              JOptionPane.ERROR_MESSAGE
              );
          securityOK = false;
          continue securityLoop;
        }

        if (! ac.isValidSession(mySession)){
          JOptionPane.showMessageDialog(
            null,
            "Incorrect session obtained. "
              + "Probably there is a problem with the database!",
            "Login error",
            JOptionPane.ERROR_MESSAGE
            );
          securityOK = false;
          continue securityLoop;
        }
      }catch(gate.security.SecurityException se) {
        JOptionPane.showMessageDialog(null, "User identification error!\n " +
                                      se.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
        securityOK = false;
        continue securityLoop;
      }
      securityOK = true;
    } while(!securityOK);

    try {

      //2. open the oracle datastore
      ds = (JDBCDataStore)super.createObject();
      try {
        ds.setSession(mySession);
      } catch(gate.security.SecurityException ex1) {
        throw new PersistenceException(ex1.getMessage());
      }

      //3. add the security data for this datastore
      //this saves the user and group information, so it can
      //be used later when resources are created with certain rights
      FeatureMap securityData = Factory.newFeatureMap();
      securityData.put("user", usr);
      securityData.put("group", grp);
      reg.addSecurityData(ds, securityData);

    } catch(PersistenceException pe) {
      JOptionPane.showMessageDialog(null, "Datastore open error!\n " +
                                    pe.toString(),
                                    "Gate", JOptionPane.ERROR_MESSAGE);
    }

    return ds;
  }

  protected String userName;
  protected String userGroup;
}