/*
 *  TestFiles.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 10/June/00
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.io.*;
import junit.framework.*;
import java.net.*;

/** Files test class.
  */
public class TestFiles extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestFiles(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Test the getResourceAs... methods. */
  public void testGetResources() throws Exception {
    assert(true);
    String japeResName = "jape/combined/testloc.jape";
    String firstLine = "// testloc.jape";

    InputStreamReader resReader =
      new InputStreamReader(Files.getGateResourceAsStream(japeResName));
    BufferedReader bufResReader = new BufferedReader(resReader);
    assert(bufResReader.readLine().equals(firstLine));
    resReader.close();

    String resString = Files.getGateResourceAsString(japeResName);
    assert(resString.startsWith(firstLine));

    byte[] resBytes = Files.getGateResourceAsByteArray(japeResName);

    /*
    Out.println(new String(resBytes));
    Out.println(resBytes.length);
    Out.println(resString);
    Out.println(resString.length());
    */

    char resChars[] = new char[firstLine.length()];
    for(int i=0; i<resChars.length; i++) resChars[i] = (char)resBytes[i];
    resString = new String(resChars);
    assert(resString, resString.equals(firstLine));

  } // testGetResources()

  /** Test the writeTempFile... method. */
  public void testWriteTempFile() throws Exception {
    assert(true);
    String japeResName = "jape/combined/testloc.jape";
    String firstLine = "// testloc.jape";

    File f = Files.writeTempFile(Files.getGateResourceAsStream(japeResName));
    BufferedReader bfr = new BufferedReader(new FileReader(f));

    String firstLn = bfr.readLine();
    assert("first line from jape/combined/testloc.jape doesn't match",
      firstLine.equals(firstLn));

    f.delete ();
  } // testWriteTempFile()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestFiles.class);
  } // suite

  public static void main(String args[]){
    TestFiles app = new TestFiles("TestFiles");
    try {
      app.testJarFiles ();
      app.testGetResources();
    } catch (Exception e) {
      e.printStackTrace (Err.getPrintWriter());
    }
  } // main

  /** Test JarFiles methods */
  public void testJarFiles() throws Exception {

    JarFiles jarFiles = new JarFiles();
    Set filesToMerge = new HashSet();
    String jarFilePathFirst = "jartest/ajartest.jar";
    String jarFilePathSecond ="jartest/bjartest.jar";
    String jarPathFirst = null;;
    String jarPathSecond = null;
    String jarPathFinal = null;
    File resourceFile  = null;
    File f1 = null;
    File f2 = null;
    FileInputStream fileStreamFirst = null;
    FileInputStream fileStreamSecond = null;

    //open first jar file in a temporal file
    // Out.println(Files.getResourceAsStream(jarFilePathFirst));
    f1 = Files.writeTempFile(Files.getGateResourceAsStream(jarFilePathFirst));

    //open second jar file in a temporal file
    f2 =Files.writeTempFile(Files.getGateResourceAsStream(jarFilePathSecond));


    //create a temporal file in order to put the classes of jar files
    resourceFile = File.createTempFile("jarfinal", ".tmp");
    resourceFile.deleteOnExit();

    //determin the paths of the temporal files
    jarPathFirst = f1.getAbsolutePath();
    jarPathSecond = f2.getAbsolutePath();
    f1.deleteOnExit();
    f2.deleteOnExit();
    jarPathFinal = resourceFile.getAbsolutePath();
    filesToMerge.add(jarPathFirst);
    filesToMerge.add(jarPathSecond);

    //close the temporal files
    fileStreamFirst = new FileInputStream(f1);

    fileStreamSecond = new FileInputStream(f2);

    fileStreamFirst.close();

    fileStreamSecond.close();

    jarFiles.merge(filesToMerge,jarPathFinal);

  } // testJarFiles

  public void testFind(){
    String regex = "z:/gate2/doc/.*.html";
    String filePath = "z:/gate2/doc";
    Iterator iter;
    Files files = new Files();
    Set regfind = new HashSet();

    regfind = files.Find(regex,filePath);
    iter = regfind.iterator();
    if (iter.hasNext()){
      while (iter.hasNext()){
        String verif = iter.next().toString();
        //Out.println(verif);
      }
    }
  } // testFind

} // class TestFiles
