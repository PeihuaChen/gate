/*
	JarFileMerger.java

	Oana Hamza, 09/06/00

	$Id$
*/

package gate.util;
import java.util.*;
import java.util.jar.*;
import java.lang.*;
import java.io.*;

/** This class is used to merge a set of Jar/Zip Files in a Jar File
  * It is ignored the manifest.
  */
public class JarFiles {

  /** This method takes the content of all jar/zip files from the set
    * jarFileNames and put them in a file with the name outputFileName.
    * If the jar entry is manifest then this information isn't added.
    * @param jarFileNames is a set of names of files (jar/zip)
    * @param outputFileName is the name of the file which contains all the
    * classes of jarFilesNames
    */
  public void merge(Set jarFileNames, String outputFileName) throws GateException{
    Iterator iter;
    String jarFileName;
    Enumeration enum;
    Enumeration en;
    FileOutputStream outStream = null;
    JarOutputStream  outJar = null;
    byte buffer[] = new byte[1000];

    //open the FileOutputStream file
    try{
      outStream = new FileOutputStream(outputFileName);
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
      System.exit(1);
    }

    // open the JarOutputStream file
    try{
      outJar = new JarOutputStream(outStream);
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
      System.exit(1);
    }
    iter=jarFileNames.iterator();
    while(iter.hasNext()) {
      jarFileName = (String)iter.next();
      JarFile jarFile;
      // open a Jar File
      try{
        jarFile = new JarFile(jarFileName);
        enum = jarFile.entries();
        JarEntry currentJarEntry;
        InputStream currentEntryStream;
        while (enum.hasMoreElements())
        {
          currentJarEntry = (JarEntry)enum.nextElement();
          // if current entry is manifest then it is skipped
          if(currentJarEntry.getName().equalsIgnoreCase("META-INF/") ||
             currentJarEntry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF"))
            continue;
          // current entry is added to the final jar file
          try{
            outJar.putNextEntry(new JarEntry(currentJarEntry.getName()));
          }catch(java.util.zip.ZipException ze){
            if(currentJarEntry.isDirectory())
              System.err.println("Warning: duplicate directory \"" +
                                 currentJarEntry.getName() + "\"!");
            else{
              throw new GateException("Error: duplicate file entry \"" +
                                 currentJarEntry.getName() + "\"!");
            }
          }
          //the data from jar files is added
          currentEntryStream = jarFile.getInputStream(currentJarEntry);
          int read = 0;
          do{
            read = currentEntryStream.read(buffer,0,1000);
            if(read != -1){
              outJar.write(buffer,0,read);
            }
          }while(read != -1);

          outJar.closeEntry();
          currentEntryStream.close();
        }
      try{
        jarFile.close();
      }catch(IOException ioe){
        ioe.printStackTrace(System.err);
      }
      }catch(IOException ioe){
        ioe.printStackTrace(System.err);
      }
    }
    //close the JarOutputStream outJar
    try{
      outJar.close();
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
    }
 }//merge

  /** args[0] is the final jar file and the other are the set of jar file names
    * e.g. java gate.util.JarFiles libs.jar ../lib/*.jar ../lib/*.zip
    * will create a file calls libs.jar which will contain all jar files and zip
    * files
    */

  public static void main(String[] args){
    if(args.length < 2){
    System.err.println("No input files");
    System.exit(1);
    }
    else
    {
      JarFiles jarFiles = new JarFiles();
      Set filesToMerge = new HashSet();
      for (int i=2;i<=args.length;i++){
        filesToMerge.add(args[i-1]);
    }
    try{
      jarFiles.merge(filesToMerge, args[0]);
    }catch(GateException ge){
      ge.printStackTrace(System.err);
    }
    }//if
  }//main

}//class JarFiles
