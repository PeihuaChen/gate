/*
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 26/Feb/2002
 *
 *  $Id$
 */

package gate.util;

import java.io.*;
import java.net.*;

import junit.framework.*;

import gate.*;
import gate.util.*;


public class TestReload extends TestCase{
  /** Construction */
  public TestReload(String name) { super(name); }

 /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestReload.class);
  } // suite

 /** Reload */
  public void testReload() throws Exception {
    ReloadingClassLoader loader = new ReloadingClassLoader();
    //load first version
    URL url = Gate.class.getResource(Files.getResourcePath() +
                                     "/gate.ac.uk/tests/first.jar");
    loader.load(url);
    //try the class
    Class c = loader.loadClass("loader.Scratch", true);
    String firstResult = c.newInstance().toString();

    //unload first version
    loader.unload(url);

    //try to get an error
    try{
      c = loader.loadClass("loader.Scratch", true);
      Assert.assertTrue("Class was found after being unloaded!", false);
    }catch(ClassNotFoundException cnfe){
      if(DEBUG) System.out.println("OK: got exception");
    }

    //load second version
    url = Gate.class.getResource(Files.getResourcePath() +
                                     "/gate.ac.uk/tests/second.jar");
    loader.load(url);

    //try the class
    c = loader.loadClass("loader.Scratch", true);
    String secondResult = c.newInstance().toString();

    //check the results are different
    Assert.assertTrue("Got same result from different versions of the class",
                      !firstResult.equals(secondResult));
  }

  public void testUnload() throws Exception {
    ReloadingClassLoader loader = new ReloadingClassLoader();
    //load first version
    URL url = Gate.class.getResource(Files.getResourcePath() +
                                     "/gate.ac.uk/tests/first.jar");
    loader.load(url);
    //try the class
    Class c = loader.loadClass("loader.Scratch", true);
    String firstResult = c.newInstance().toString();

    //unload first version
    loader.unload(url);

    //try to get an error
    try{
      c = loader.loadClass("loader.Scratch", true);
      Assert.assertTrue("Class was found after being unloaded!", false);
    }catch(ClassNotFoundException cnfe){
      if(DEBUG) System.out.println("OK: got exception");
    }
  }

  /** Debug flag */
  private static final boolean DEBUG = false;
}