/*
 *  Test___CLASSNAME___.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  ___AUTHOR___, ___DATE___
 *
 *  $Id$
 */

package template;

import java.util.*;
import junit.framework.*;
/** Tests for the MyClass class
  */
public class Test___CLASSNAME___ extends TestCase
{
  /** Construction */
  public Test___CLASSNAME___(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** MyClass creation and use of GATE scripts */
  public void test___CLASSNAME___OrOther() {
    assert(true);
  } // testMyClassOrOther()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(Test___CLASSNAME___.class);
  } // suite

} // class Test___CLASSNAME___
