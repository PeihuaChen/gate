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

import java.util.zip.*;
/** This class is used to merge a set of Jar/Zip Files in a Jar File
  * It is ignored the manifest.
  */
public class JarFiles {

  private final static int BUFF_SIZE = 65000;
  /** This method takes the content of all jar/zip files from the set
    * jarFileNames and put them in a file with the name outputFileName.
    * If the jar entry is manifest then this information isn't added.
    * @param jarFileNames is a set of names of files (jar/zip)
    * @param outputFileName is the name of the file which contains all the
    * classes of jarFilesNames
    */
  public void merge(Set jarFileNames, String outputFileName) throws GateException{
    String jarFileName;

    FileOutputStream outStream = null;
    JarOutputStream  outJar = null;
    byte buffer[] = new byte[BUFF_SIZE];
    // open the JarOutputStream file
    try{
      outJar = new JarOutputStream(new FileOutputStream(outputFileName));
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
      System.exit(1);
    }
    // iterate throught the Jar files set
    Iterator jarFileNamesIterator = jarFileNames.iterator();
    while(jarFileNamesIterator.hasNext()) {
      jarFileName = (String) jarFileNamesIterator.next();

      JarFile jarFile = null;
      // open a Jar File
      try{
        // create a new jarFile based on jarFileName
        jarFile = new JarFile(jarFileName);
        // get an enumeration of all entries
        Enumeration jarFileEntriesEnum = jarFile.entries();

        JarEntry currentJarEntry = null;
        while (jarFileEntriesEnum.hasMoreElements()){
          currentJarEntry = (JarEntry) jarFileEntriesEnum.nextElement();
          // if current entry is manifest then it is skipped
          if(currentJarEntry.getName().equalsIgnoreCase("META-INF/") ||
             currentJarEntry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF"))
            continue;
          // current entry is added to the final jar file
          try{
            outJar.putNextEntry(new JarEntry(currentJarEntry.getName()));
          }catch(java.util.zip.ZipException ze){

            if(!currentJarEntry.isDirectory())
              throw new GateException("Warning: duplicate file entry " +
                                 currentJarEntry.getName() + " !");
          }
          //the binary data from jar files is added
          // get an input stream
          InputStream currentEntryStream = null;
          currentEntryStream = jarFile.getInputStream(currentJarEntry);
          // write data to outJar
          int  bytesRead = 0;
          while((bytesRead = currentEntryStream.read(buffer,0,BUFF_SIZE)) != -1)
            outJar.write(buffer,0,bytesRead);

          outJar.flush();
          // close input stream
          currentEntryStream.close();
          //close the new added entry
          // prepare to write another one
          outJar.closeEntry();
        }//while(jarFileEntriesEnum.hasMoreElements())
        jarFile.close();
      }catch(IOException ioe){
        ioe.printStackTrace(System.err);
      }
    }//while(jarFileNamesIterator.hasNext())
    //close the JarOutputStream outJar
    try{

      outJar.flush();
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
    System.err.println("USAGE : JarFiles arg0 arg1 ... argN (must be at list 2 args)");
    System.exit(1);
    }
    else
    {
      JarFiles jarFiles = new JarFiles();
      Set filesToMerge = new HashSet();
      for (int i=1; i<args.length; i++){
        filesToMerge.add(args[i]);
    }
    try{
      jarFiles.merge(filesToMerge, args[0]);
    }catch(GateException ge){
      ge.printStackTrace(System.err);
    }
    }//if
  }//main

}//class JarFiles
