/*
 *	TestFiles.java
 *
 *	Hamish Cunningham, 10/June/00
 *
 *	$Id$
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
      new InputStreamReader(Files.getResourceAsStream(japeResName));
    BufferedReader bufResReader = new BufferedReader(resReader);
    assert(bufResReader.readLine().equals(firstLine));
    resReader.close();

    String resString = Files.getResourceAsString(japeResName);
    assert(resString.startsWith(firstLine));

    byte[] resBytes = Files.getResourceAsByteArray(japeResName);

    /*
    System.out.println(new String(resBytes));
    System.out.println(resBytes.length);
    System.out.println(resString);
    System.out.println(resString.length());
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

    File f = Files.writeTempFile(Files.getResourceAsStream(japeResName));
    BufferedReader bfr = new BufferedReader(new FileReader(f));

    String firstLn = bfr.readLine();
    assert("first line from jape/combined/testloc.jape doesn't match", firstLine.equals(firstLn));

    f.delete ();
  } // testWriteTempFile()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestFiles.class);
  } // suite

  public static void main(String args[]){
    TestFiles app = new TestFiles("TestFiles");
    try{
      app.testJarFiles ();
    }catch (Exception e){
      e.printStackTrace (System.err);
    }
  }

  /** Test JarFiles methods */
  public void testJarFiles(){
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
    try{
//      System.out.println(Files.getResourceAsStream(jarFilePathFirst));
      f1 = Files.writeTempFile(Files.getResourceAsStream(jarFilePathFirst));
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
      System.exit(1);
    }
    //open second jar file in a temporal file
    try{
      f2 =Files.writeTempFile(Files.getResourceAsStream(jarFilePathSecond));
    }catch (IOException ioe){
      ioe.printStackTrace(System.err);
      System.exit(1);
    }
    //create a temporal file in order to put the classes of jar files
    try{
    resourceFile = File.createTempFile("jarfinal", ".tmp");
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
      System.exit(1);
    }
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
    try{
      fileStreamFirst = new FileInputStream(f1);
    }catch(FileNotFoundException fnfe){
      fnfe.printStackTrace(System.err);
    }
    try{
      fileStreamSecond = new FileInputStream(f2);
    }catch(FileNotFoundException fnfe){
      fnfe.printStackTrace(System.err);
    }
    try{
      fileStreamFirst.close();
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
    }
    try{
      fileStreamSecond.close();
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
    }
    //create the final jar file
    try{
      jarFiles.merge(filesToMerge,jarPathFinal);
    }catch(GateException ge){
      ge.printStackTrace(System.err);
    }
  }// testJarFiles

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
        System.out.println(verif);
      }
    }
  }//testFind

} // class TestFiles
