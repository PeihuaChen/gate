/*
 *  Files.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */

package gate.util;
import java.io.*;
import java.util.*;
import java.lang.*;
import gnu.regexp.*;

/** Some utilities for use with Files and with resources.
  * <P>
  * <B>Note</B> that there is a terminology conflict between the use
  * of "resources" here and <TT>gate.Resource</TT> and its inheritors.
  * <P>
  * Java "resources" are files that live on the CLASSPATH or in a Jar
  * file that are <I>not</I> <TT>.class</TT> files. For example: a
  * <TT>.gif</TT> file that is used by a GUI, or one of the XML files
  * used for testing GATE's document format facilities. This class
  * allows you to access these files in various ways (as streams, as
  * byte arrays, etc.).
  * <P>
  * GATE resources are components (Java Beans) that provide all of the
  * natural language processing capabilities of a GATE-based system, and
  * the language data that such systems analsyse and produce. For
  * example: parsers, lexicons, generators, corpora.
  * <P>
  * Where we say "resource" in this class we mean Java resource; elsewhere
  * in the system we almost always mean GATE resource.
  */
public class Files {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Used to generate temporary resources names*/
  static long resourceIndex = 0;

  /**Where on the classpath the gate resources are to be found*/
  protected static String resourcePath = "/gate/resources";

  /**Gets the path for the gate resources within the classpath*/
  public static String getResourcePath(){
    return resourcePath;
  }
  /** Get a string representing the contents of a text file. */
  public static String getString(String fileName) throws IOException {
    return getString(new File(fileName));
  } // getString(fileName)

  /** Get a string representing the contents of a text file. */
  public static String getString(File textFile) throws IOException {
    FileInputStream fis = new FileInputStream(textFile);
    int len = (int) textFile.length();
    byte[] textBytes = new byte[len];
    fis.read(textBytes, 0, len);
    fis.close();
    return new String(textBytes);
  } // getString(File)

  /** Get a byte array representing the contents of a binary file. */
  public static byte[] getByteArray(File binaryFile) throws IOException {
    FileInputStream fis = new FileInputStream(binaryFile);
    int len = (int) binaryFile.length();
    byte[] bytes = new byte[len];
    fis.read(bytes, 0, len);
    fis.close();
    return bytes;
  } // getByteArray(File)

  /** Get a resource from the classpath as a String.
    */
  public static String getResourceAsString(String resourceName)
  throws IOException {
    InputStream resourceStream = getResourceAsStream(resourceName);
    BufferedReader resourceReader =
      new BufferedReader(new InputStreamReader(resourceStream));
    StringBuffer resourceBuffer = new StringBuffer();

    int i;

    int charsRead = 0;
    final int size = 1024;
    char[] charArray = new char[size];

    while( (charsRead = resourceReader.read(charArray,0,size)) != -1 )
      resourceBuffer.append (charArray,0,charsRead);

    while( (i = resourceReader.read()) != -1 )
      resourceBuffer.append((char) i);

    resourceReader.close();
    return resourceBuffer.toString();
  } // getResourceAsString(String)

  /** Get a resource from the GATE resources directory as a String.
    * The resource name should be relative to {@link #resourcePath} which
    * is equal with <TT>gate/resources</TT>; e.g.
    * for a resource stored as <TT>gate/resources/jape/Test11.jape</TT>,
    * this method should be passed the name <TT>jape/Test11.jape</TT>.
    */
  public static String getGateResourceAsString(String resourceName)
    throws IOException {

    InputStream resourceStream = getGateResourceAsStream(resourceName);
    BufferedReader resourceReader =
      new BufferedReader(new InputStreamReader(resourceStream));
    StringBuffer resourceBuffer = new StringBuffer();

    int i;

    int charsRead = 0;
    final int size = 1024;
    char[] charArray = new char[size];

    while( (charsRead = resourceReader.read(charArray,0,size)) != -1 )
      resourceBuffer.append (charArray,0,charsRead);

    while( (i = resourceReader.read()) != -1 )
      resourceBuffer.append((char) i);

    resourceReader.close();
    return resourceBuffer.toString();
  } // getGateResourceAsString(String)

  /**
    * Writes a temporary file into the default temporary directory,
    * form an InputStream a unique ID is generated and associated automaticaly
    * with the file name...
    */
  public static File writeTempFile(InputStream contentStream)
    throws IOException {

    File resourceFile  = null;
    FileOutputStream resourceFileOutputStream = null;

    // create a temporary file name
    resourceFile = File.createTempFile ("gateResource", ".tmp");
    resourceFileOutputStream = new FileOutputStream(resourceFile);
    resourceFile.deleteOnExit ();
    //resourceReader = new BufferedReader(new InputStreamReader(contentStream));

    int bytesRead = 0;
    final int readSize = 1024;
    byte[] bytes = new byte[readSize];
    while( (bytesRead = contentStream.read(bytes,0,readSize) ) != -1 )
      resourceFileOutputStream.write(bytes,0, bytesRead);

    resourceFileOutputStream.close();
    contentStream.close ();
    return resourceFile;
  }



  /** Get a resource from the classpath as a byte array.
    */
  public static byte[] getResourceAsByteArray(String resourceName)
    throws IOException, IndexOutOfBoundsException, ArrayStoreException {

    InputStream resourceInputStream = getResourceAsStream(resourceName);
    BufferedInputStream resourceStream =
      new BufferedInputStream(resourceInputStream);
    byte b;
    final int bufSize = 1024;
    byte[] buf = new byte[bufSize];
    int i = 0;

    // get the whole resource into buf (expanding the array as needed)
    while( (b = (byte) resourceStream.read()) != -1 ) {
      if(i == buf.length) {
        byte[] newBuf = new byte[buf.length * 2];
        System.arraycopy (buf,0,newBuf,0,i);
        buf = newBuf;
      }
      buf[i++] = b;
    }

    // close the resource stream
    resourceStream.close();

    // copy the contents of buf to an array of the correct size
    byte[] bytes = new byte[i];
    // copy from buf to bytes
    System.arraycopy (buf,0,bytes,0,i);
    return bytes;
  } // getResourceAsByteArray(String)

  /** Get a resource from the GATE resources directory as a byte array.
    * The resource name should be relative to{@link #resourcePath} which
    * is equal with <TT>gate/resources</TT>; e.g.
    * for a resource stored as <TT>gate/resources/jape/Test11.jape</TT>,
    * this method should be passed the name <TT>jape/Test11.jape</TT>.
    */
  public static byte[] getGateResourceAsByteArray(String resourceName)
    throws IOException, IndexOutOfBoundsException, ArrayStoreException {

    InputStream resourceInputStream = getGateResourceAsStream(resourceName);
    BufferedInputStream resourceStream =
      new BufferedInputStream(resourceInputStream);
    byte b;
    final int bufSize = 1024;
    byte[] buf = new byte[bufSize];
    int i = 0;

    // get the whole resource into buf (expanding the array as needed)
    while( (b = (byte) resourceStream.read()) != -1 ) {
      if(i == buf.length) {
        byte[] newBuf = new byte[buf.length * 2];
        System.arraycopy (buf,0,newBuf,0,i);
        buf = newBuf;
      }
      buf[i++] = b;
    }

    // close the resource stream
    resourceStream.close();

    // copy the contents of buf to an array of the correct size
    byte[] bytes = new byte[i];

    // copy from buf to bytes
    System.arraycopy (buf,0,bytes,0,i);
    return bytes;
  } // getResourceGateAsByteArray(String)


  /** Get a resource from the classpath as an InputStream.
    */
  public static InputStream getResourceAsStream(String resourceName)
    throws IOException {

    return  Files.class.getResourceAsStream(resourceName);
    //return  ClassLoader.getSystemResourceAsStream(resourceName);
  } // getResourceAsStream(String)

  /** Get a resource from the GATE resources directory as an InputStream.
    * The resource name should be relative to {@link #resourcePath} which
    * is equal with <TT>gate/resources</TT>; e.g.
    * for a resource stored as <TT>gate/resources/jape/Test11.jape</TT>,
    * this method should be passed the name <TT>jape/Test11.jape</TT>.
    */
  public static InputStream getGateResourceAsStream(String resourceName)
    throws IOException {

    if(resourceName.startsWith("/") || resourceName.startsWith("\\") )
      return getResourceAsStream(resourcePath + resourceName);
    else return getResourceAsStream(resourcePath + "/" + resourceName);
  } // getResourceAsStream(String)


  /** This method takes a regular expression and a directory name and returns
    * the set of Files that match the pattern under that directory.
    */
  public static Set Find(String regex, String pathFile) {
    Set regexfinal = new HashSet();
    String[] tab;
    File file = null;
    PrintStream printstr = null;
    Object obj = new Object();
    //open a file
    try {
      file = new File(pathFile);
    } catch(NullPointerException npe) {
      npe.printStackTrace(Err.getPrintWriter());
      //System.exit(1);
    }
    //generate a regular expression
    try {
      RE regexp = new RE("^"+regex);
      if (file.isDirectory()){
        tab = file.list();
        for (int i=0;i<=tab.length-1;i++){
          String finalPath = pathFile+"/"+tab[i];
          REMatch m1 = regexp.getMatch(finalPath);
          if (regexp.getMatch(finalPath) != null){
            regexfinal.add(finalPath);
          }
        }
      }
      else {
        if (file.isFile()){
          if (regexp.getMatch(pathFile) != null) {
            regexfinal.add(file.getAbsolutePath());
        }
      }
    }
    } catch(REException ree) {
      ree.printStackTrace(Err.getPrintWriter());
      //System.exit(1);
    }
    return regexfinal;
  }//find


} // class Files
