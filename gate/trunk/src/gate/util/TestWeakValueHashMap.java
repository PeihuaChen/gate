package gate.util;

import java.util.*;
import java.io.*;
import junit.framework.*;
import java.net.*;

/**
 * Title:        Gate2
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      University Of Sheffield
 * @author
 * @version 1.0
 */

public class TestWeakValueHashMap extends TestCase {

  public TestWeakValueHashMap(String name) {
    super(name);
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestWeakValueHashMap.class);
  } // suite

  /** Fixture set up */
  public void setUp() {
  } // setUp


  public void testSelfCleaning(){
    //create the map
    WeakValueHashMap wvhm = new WeakValueHashMap();

    //populate the Map;
    //Don't use Strings as they are not collectable being interned
    Object value1 = new byte[100000];
    wvhm.put("v1", value1);
    wvhm.put("v2", new byte[100000]);
    wvhm.put("v3", new byte[100000]);

    //force gc
    System.gc();

    //the last two values should have dissappeared
    assertTrue("The weak hash map has not been cleaned:\n" +
           "expected size: 1; actual size: " + wvhm.size(), wvhm.size() == 1);
  }

  public void testContainsKey(){
    //create the map
    WeakValueHashMap wvhm = new WeakValueHashMap();

    //populate the Map;
    //Don't use Strings as they are not collectable being interned
    Object value1 = new byte[100000];
    wvhm.put("v1", value1);
    wvhm.put("v2", new byte[100000]);
    wvhm.put("v3", new byte[100000]);

    //force gc
    System.gc();

    assertTrue("Key misteriously dissappeared", wvhm.containsKey("v1"));
    assertTrue("Key misteriously preserved", !wvhm.containsKey("v2"));
    assertTrue("Key misteriously preserved", !wvhm.containsKey("v3"));
  }

  public void testContainsValue(){
    //create the map
    WeakValueHashMap wvhm = new WeakValueHashMap();

    //populate the Map;
    //Don't use Strings as they are not collectable being interned
    Object value1 = new byte[100000];
    wvhm.put("v1", value1);
    wvhm.put("v2", new byte[100000]);
    wvhm.put("v3", new byte[100000]);

    //force gc
    System.gc();

    //the last two values should have dissappeared
    assertTrue("Value misteriously dissappeared", wvhm.containsValue(value1));
  }


  public void testNullKey(){
    //create the map
    WeakValueHashMap wvhm = new WeakValueHashMap();

    //populate the Map;
    Object value1 = new byte[100000];
    wvhm.put(null, value1);

    assertEquals("The weak hash map does not support null keys!",
                 value1, wvhm.get(null));
  }

  public void testNullValue(){
    //create the map
    WeakValueHashMap wvhm = new WeakValueHashMap();

    //populate the Map;
    wvhm.put("null", null);

    assertTrue("The weak hash map does not support null values!",
           wvhm.get("null") == null);
  }

  static public void main(String[] args){
    TestWeakValueHashMap test = new TestWeakValueHashMap("");
    test.setUp();
    test.testSelfCleaning();
    test.testNullKey();
    test.testNullValue();
    test.testContainsKey();
    test.testContainsValue();
  }
}