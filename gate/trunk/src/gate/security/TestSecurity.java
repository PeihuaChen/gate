/*
 *  TestSecurity.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 01/Oct/01
 *
 *  $Id$
 */

package gate.security;

import java.util.*;
import java.io.*;
import java.net.*;
import java.beans.*;
import java.lang.reflect.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.corpora.*;
import gate.security.*;

/** Persistence test class
  */
public class TestSecurity extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestSecurity(String name) throws GateException { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
  } // setUp

  /** Put things back as they should be after running tests
    * (reinitialise the CREOLE register).
    */
  public void tearDown() throws Exception {
  } // tearDown


  public void testSecurityTables() throws Exception {
    AccessController ac = new AccessControllerImpl();
    ac.open("jdbc:oracle:thin:GATEUSER/gate2@hope.dcs.shef.ac.uk:1521:GateDB");

    User myUser = ac.findUser("kalina");
    Assert.assertNotNull(myUser);
    Assert.assertEquals(myUser.getName(), "kalina");

    List myGroups = myUser.getGroups();
    Assert.assertNotNull(myGroups);
    for (int i = 0; i< myGroups.size(); i++) {
      Group myGroup = ac.findGroup((Long) myGroups.get(i));
      if (i == 0)
        Assert.assertEquals(myGroup.getName(), "English Language Group");
      else if (i == 1)
        Assert.assertEquals(myGroup.getName(), "Suahili Group");
      else
        Assert.fail("Found more groups for user kalina than should have been!");
    }//for

    Session mySession = ac.login("kalina", "sesame",
                              ac.findGroup("English Language Group").getID());
    Assert.assertNotNull(mySession);
//    Assert.assert(ac.isValidSession(mySession));

  } // testSecurityTables

  public void testUserGroupManipulation() throws Exception {
    AccessController ac = new AccessControllerImpl();
    ac.open("jdbc:oracle:thin:GATEUSER/gate2@hope.dcs.shef.ac.uk:1521:GateDB");

    User myUser = ac.createUser("myUser", "myPassword");
    Group myGroup = ac.createGroup("myGroup");
    Session mySession = ac.login("myUser", "myPassword", myGroup.getID());

    myGroup.addUser(myUser, mySession);
    myGroup.setName("my new group", mySession);
    Assert.assertEquals(myGroup.getName(), "my new group");

    List myUsers = myGroup.getUsers();
    Assert.assertNotNull(myUsers);
    for (int i = 0; i< myUsers.size(); i++) {
      User myUser1 = ac.findUser((Long) myUsers.get(i));
      if (i == 0)
        Assert.assertEquals(myUser1.getName(), "myUser");
      else
        Assert.fail("Found more groups for user "
                            + myUser1 + "than should have been!");
      Out.prln("are equals? " + myUser1.equals(myUser));
    }//for

    ac.logout(mySession);
    myGroup.setName("my new group again", mySession);

    mySession = ac.login("myUser", "myPassword",
                              ac.findGroup("my new group").getID());
    ac.deleteGroup(myGroup, mySession);

    Out.prln(myGroup.getName());
  } // testUserGroupManipulation



  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestSecurity.class);
  } // suite

  public static void main(String[] args){
    try{
      Gate.setLocalWebServer(false);
      Gate.setNetConnected(false);
      Gate.init();
      TestSecurity test = new TestSecurity("");

      test.setUp();
      test.testSecurityTables();
      test.tearDown();

      test.setUp();
      test.testUserGroupManipulation();
      test.tearDown();

    }catch(Exception e){
      e.printStackTrace();
    }
  }
} // class TestPersist
