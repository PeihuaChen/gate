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

  /** JDBC URL */
  private static final String JDBC_URL =
            "jdbc:oracle:thin:GATEUSER/gate@192.168.128.207:1521:GATE03";
//"jdbc:oracle:thin:GATEUSER/gate2@hope.dcs.shef.ac.uk:1521:GateDB"

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
    ac.open(JDBC_URL);

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

    //1. open security factory
    AccessController ac = new AccessControllerImpl();
    ac.open(JDBC_URL);

    //2. log into the securoty factory
    Session adminSession = ac.login("kalina", "sesame",
                              ac.findGroup("English Language Group").getID());
    //check session
    Assert.assertNotNull(adminSession);
    //is session valid?
    Assert.assert(true == ac.isValidSession(adminSession));

    //3. create a new user and group
    User myUser = ac.createUser("myUser", "myPassword");
    //is the user aded to the security factory?
    Assert.assertNotNull(ac.findUser("myUser"));
    //is the user in the security factory equal() to what we put there?
    Assert.assertEquals(myUser,ac.findUser("myUser"));
    //is the key correct?
    Assert.assertEquals(myUser.getName(),ac.findUser("myUser").getName());



    Group myGroup = ac.createGroup("myGroup");
    //is the group aded to the security factory?
    Assert.assertNotNull(ac.findGroup("myGroup"));
    //is the group in the security factory equal() to what we put there?
    Assert.assertEquals(myGroup,ac.findGroup("myGroup"));
    //is the key correct?
    Assert.assertEquals(myGroup.getName(), "myGroup");



    //4. add user to group
    myGroup.addUser(myUser, adminSession);
    //is the user added to the group?
    Assert.assert(myGroup.getUsers().contains(myUser));

    //5. change group name
    myGroup.setName("my new group", adminSession);
    //is the name changed?
    Assert.assertEquals("my new group",myGroup.getName());


    //6. get users
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

    //7. change name again
    myGroup.setName("my new group again", adminSession);
    //is the name changed?
    Assert.assertEquals("my new group again",myGroup.getName());

    //8. try to log the user in
    Session mySession = ac.login("myUser", "myPassword",
                              ac.findGroup("my new group again").getID());
    //check session
    Assert.assertNotNull(mySession);
    //is valid session?
    Assert.assert(true == ac.isValidSession(mySession));

    //9. logout
    ac.logout(mySession);
    //is session invalidated?
    Assert.assert(false == ac.isValidSession(mySession));

    //10. try to perform an operation with invalid session
    boolean exceptionThrown = false;
    try {
      myGroup.removeUser(myUser,mySession);
    }
    catch(SecurityException ex) {
      exceptionThrown = true;
      Err.prln("++++ OK, got exception ["+ex.getMessage()+"]");
    }
    Assert.assert(true == exceptionThrown);

    //11. try to delete group
    ac.deleteGroup(myGroup, adminSession);
    //is the group deleted?
    Assert.assertNull(ac.findGroup(myGroup.getName()));

    //12. check that the sessions are invalidated ig the
    //group/user in the session is deleted

    //13. check objectModification events

    //14.

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
