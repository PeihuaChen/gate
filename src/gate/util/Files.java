//Title:        Files.java
//Version:      $Id$
//Author:       Hamish Cunningham

package gate.util;
import java.io.*;

/** Some utilities for use with Files. */
public class Files {

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


} // class Files

