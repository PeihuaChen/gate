/*
	JarFileMerger.java

	Oana Hamza, 09/06/00

	$Id$
*/

package gate.util;
import java.util.*;
import java.util.jar.*;
import java.io.*;
/**
  *This class is used to merge a set of Jar/Zip Files in a Jar File
  *It is ignored the manifest.
    */
public class JarFilesMerger {

  /**
    * This method takes the content of all jar/zip files from the set
    *jarFileNames and put them in a file with the name outputFileName.
    *If the jar entry is manifest then this information isn't added.
    *@param jarFileNames is a set of names of files (jar/zip)
    *@param outputFileName is the name of the file which contains all the
    *classes of jarFilesNames
    */
  public void merge(Set jarFileNames, String outputFileName) {
  Iterator iter;
  String jarFileName;
  Enumeration enum;
  FileOutputStream outStream = null;
  JarOutputStream  outJar = null;
  byte buffer[] = new byte[1000];
  try{
    outStream = new FileOutputStream(outputFileName);
  }catch(FileNotFoundException fnfe){
    fnfe.printStackTrace(System.err);
    System.exit(1);
  }
  try{
    outJar = new JarOutputStream(outStream);
  }catch(IOException ioe){
    ioe.printStackTrace(System.err);
    System.exit(1);
  }
    iter=jarFileNames.iterator();
    while(iter.hasNext()) {
      jarFileName = (String) iter.next();
      JarFile jarFile;
      try{
        jarFile = new JarFile(jarFileName);
        enum = jarFile.entries();
        JarEntry currentJarEntry;
        InputStream currentEntryStream;
        while (enum.hasMoreElements())
        {
          currentJarEntry = (JarEntry)enum.nextElement();
          if(currentJarEntry.getName().equalsIgnoreCase("META-INF/") ||
             currentJarEntry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF"))
            continue;
          try{
            outJar.putNextEntry(new JarEntry(currentJarEntry.getName()));
          }catch(java.util.zip.ZipException ze){
            if(currentJarEntry.isDirectory())
              System.err.println("Warning: duplicate directory \"" +
                                 currentJarEntry.getName() + "\"!");
            else{
              System.err.println("Error: duplicate file entry \"" +
                                 currentJarEntry.getName() + "\"! Exiting...");
              System.exit(1);
            }
          }
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
      }catch(IOException ioe){
        ioe.printStackTrace(System.err);
      }
    }
    try{
      outJar.close();
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
    }
 }

  /**
    *args[0] is the final jar file and the other are the set of jar file names
    *e.g. java gate.util.JarFileMerger libs.jar ../lib/*.jar ../lib/*.zip
    *will create a file calls libs.jar which will contain all jar files and zip
    *files
    */
  public static void main(String[] args){
   if(args.length < 2){
    System.err.println("No input files");
    System.exit(1);
   }
   {
     JarFilesMerger jarFiles = new JarFilesMerger();
     Set filesToMerge = new HashSet();
     for (int i=2;i<=args.length;i++)
     {
       filesToMerge.add(args[i-1]);
     }
     jarFiles.merge(filesToMerge, args[0]);
   }
  }
}