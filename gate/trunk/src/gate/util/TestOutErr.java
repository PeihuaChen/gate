/*
 *	TestOutErr.java
 *
 *	Oana Hamza
 *
 *	$Id$
 */

package gate.util;

import java.util.*;
import java.io.*;
import junit.framework.*;
import java.lang.*;

/**
  */
public class TestOutErr extends TestCase
  {
  /** Construction */
   public TestOutErr(String name) { super(name); }

   /** Fixture set up */
   public void setUp() {
  } // setUp

  /** A test */
  public void testSomething() throws Exception {
    assert(true);
  } // testSomething()

  public static void main(String[] args) {
    String str1 = "It's true";
    boolean str2 = true;
    int str3 = 8;
    double str4 = 12.34;
    char str5 = ' ';
    char[] str6 = {'D','i','a','n','a'};

    OutErr out = OutErr.getMeAnOut();
    OutErr out1 = OutErr.getMePrintStream(System.out);
    out.pr(str1);
    out.pr(str5);
    out.pr(str2);
    out.pr(str5);
    out.pr(str3);
    out.pr(str5);
    out.prln(str4);
    out.prln(str6);
    out1.pr(str6);
    out1.pr(str5);
    out1.pr(str1);
  }// main

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestTemplate.class);
  } // suite

} // class TestTemplate
