/*
 *  JarFileMerger.java
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
 *  Oana Hamza, 09/06/00
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

import java.lang.*;
import java.io.*;

/** This class is used to merge a set of Jar/Zip Files in a Jar File
  * It is ignored the manifest.
  */
public class JarFiles {

  /** Debug flag */
  private static final boolean DEBUG = false;

  private final static int BUFF_SIZE = 65000;

  private Set directorySet = null;

  private byte buffer[] = null;

  public JarFiles() {
    directorySet = new HashSet();
    buffer = new byte[BUFF_SIZE];
  }

  /** This method takes the content of all jar/zip files from the set
    * jarFileNames and put them in a file with the name outputFileName.
    * If the jar entry is manifest then this information isn't added.
    * @param jarFileNames is a set of names of files (jar/zip)
    * @param outputFileName is the name of the file which contains all the
    * classes of jarFilesNames
    */
  public void merge(Set jarFileNames, String destinationJarName)
                                                      throws GateException {
    String sourceJarName = null;
    JarOutputStream jarFileDestination = null;
    JarFile jarFileSource = null;

    try {
      // create the output jar file
      jarFileDestination =
        new JarOutputStream(new FileOutputStream(destinationJarName));

      // iterate through the Jar files set
      Iterator jarFileNamesIterator = jarFileNames.iterator();

      while (jarFileNamesIterator.hasNext()) {
        sourceJarName = (String) jarFileNamesIterator.next();

        // create the new input jar files based on the file name
        jarFileSource = new JarFile(sourceJarName);

        // Out.println("Adding " + sourceJarName + " to "
        // + destinationJarName);
        addJar(jarFileDestination, jarFileSource);

        jarFileSource.close();
      }

      jarFileDestination.close();

    } catch(IOException ioe) {
      ioe.printStackTrace(Err.getPrintWriter());
      //System.exit(1);
    }

  }// merge


  /**
    * This method adds all entries from sourceJar to destinationJar
    * NOTE: that manifest information is not added, method will throw
    * a gate Exception if a duplicate entry file is found.
    * @param destinationJar the jar that will collect all the entries
    * from source jar
    * @param sourceJar doesn't need any explanation ... DOES it?
   */
  private void addJar(JarOutputStream destinationJar, JarFile sourceJar)
                                                       throws GateException {
    try {

      // get an enumeration of all entries from the sourceJar
      Enumeration jarFileEntriesEnum = sourceJar.entries();

      JarEntry currentJarEntry = null;
      while (jarFileEntriesEnum.hasMoreElements()) {

        // get a JarEntry
        currentJarEntry = (JarEntry) jarFileEntriesEnum.nextElement();

        // if current entry is manifest then it is skipped
        if(currentJarEntry.getName().equalsIgnoreCase("META-INF/") ||
          currentJarEntry.getName().equalsIgnoreCase("META-INF/MANIFEST.MF"))
          continue;

        // if current entry is a directory that was previously added to the
        // destination JAR then it is skipped
        if( currentJarEntry.isDirectory() &&
            directorySet.contains(currentJarEntry.getName())
           ) continue;

        // otherwise the current entry is added to the final jar file
        try {
          // if the entry is directory then is added to the directorySet
          // NOTE: files entries are not added to this set
          if (currentJarEntry.isDirectory())
            directorySet.add(currentJarEntry.getName());

          // put the entry into the destination JAR
          destinationJar.putNextEntry(new JarEntry(currentJarEntry.getName()));

          // add the binary data from the entry
          // NOTE: if the entry is a directory there will be no binary data
          // get an input stream from the entry
          InputStream currentEntryStream =
            sourceJar.getInputStream(currentJarEntry);

          // write data to destinationJar
          int  bytesRead = 0;
          while((bytesRead = currentEntryStream.read(buffer,0,BUFF_SIZE)) != -1)
                destinationJar.write(buffer,0,bytesRead);

          // close the input stream
          currentEntryStream.close();

          // flush the destinationJar in order to be sure that
          // everything is there
          destinationJar.flush();

          // close the new added entry and  prepare to read and write
          // another one
          // NOTE: destinationJar.putNextEntry automaticaly closes any previous
          // opened entry
          destinationJar.closeEntry();

        } catch (java.util.zip.ZipException ze) {
          if(!currentJarEntry.isDirectory())
            throw new GateException("FATAL ERROR: Duplicate file entry " +
                                  currentJarEntry.getName() + " !");
        }
      }// while(jarFileEntriesEnum.hasMoreElements())
    } catch (java.io.IOException e) {
      e.printStackTrace(Err.getPrintWriter());
      // System.exit(1);
    }
  }// addJar

  /** args[0] is the final jar file and the other are the set of
    * jar file names
    * e.g. java gate.util.JarFiles libs.jar ../lib/*.jar ../lib/*.zip
    * will create a file calls libs.jar which will contain all
    * jar files and zip files
    */

  public static void main(String[] args) {
    if(args.length < 2) {
                   Err.println("USAGE : JarFiles arg0 arg1 ... argN" +
                                    "(must be at least 2 args)");
                   //System.exit(1);
    }else{
      JarFiles jarFiles = new JarFiles();
      Set filesToMerge = new HashSet();
      for (int i=1; i<args.length; i++) {
        filesToMerge.add(args[i]);
    }
    try {
      jarFiles.merge(filesToMerge, args[0]);
    } catch (GateException ge) {
      ge.printStackTrace(Err.getPrintWriter());
    }
    }// if
  }// main

}// class JarFiles