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

  /** Get a resource from the GATE resources directory as a String.
    * The resource name should be relative to <TT>gate/resources</TT>; e.g.
    * for a resource stored as <TT>gate/resources/jape/Test11.jape</TT>,
    * this method should be passed the name <TT>jape/Test11.jape</TT>.
    */
  public static String getResourceAsString(String resourceName)
  throws IOException {
    InputStream resourceStream = getResourceAsStream(resourceName);
    BufferedReader resourceReader =
      new BufferedReader(new InputStreamReader(resourceStream));
    StringBuffer resourceBuffer = new StringBuffer();
    int i;

    while( (i = resourceReader.read()) != -1 )
      resourceBuffer.append((char) i);

    resourceReader.close();
    return resourceBuffer.toString();
  } // getResourceAsString(String)

  /** Get a resource from the GATE resources directory as a byte array.
    * The resource name should be relative to <TT>gate/resources</TT>; e.g.
    * for a resource stored as <TT>gate/resources/jape/Test11.jape</TT>,
    * this method should be passed the name <TT>jape/Test11.jape</TT>.
    */
  public static byte[] getResourceAsByteArray(String resourceName)
  throws IOException {
    InputStream resourceInputStream = getResourceAsStream(resourceName);
    BufferedInputStream resourceStream =
      new BufferedInputStream(resourceInputStream);
    int len = resourceStream.available();
    byte[] bytes = new byte[len];
    resourceStream.read(bytes, 0, len);
    resourceStream.close();

    return bytes;
  } // getResourceAsByteArray(String)

  /** Get a resource from the GATE resources directory as an InputStream.
    * The resource name should be relative to <TT>gate/resources</TT>; e.g.
    * for a resource stored as <TT>gate/resources/jape/Test11.jape</TT>,
    * this method should be passed the name <TT>jape/Test11.jape</TT>.
    */
  public static InputStream getResourceAsStream(String resourceName)
  throws IOException {
    return Class.class.getResourceAsStream("/gate/resources/" + resourceName);
  } // getResourceAsStream(String)

} // class Files

