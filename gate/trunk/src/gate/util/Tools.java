/*
 *  Tools.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, Jan/2000
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.net.URL;
import java.io.File;
import java.net.JarURLConnection;
import java.util.jar.*;
import java.util.zip.*;

import gate.*;

public class Tools {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public Tools() {
  }
  static long sym=0;

  /** Returns a Long wich is unique during the current run.
    * Maybe we should use serializaton in order to save the state on
    * System.exit...
    */
  static public synchronized Long gensym(){
    return new Long(sym++);
  }

  static public synchronized Long genTime(){

    return new Long(new Date().getTime());
  }


  /** Specifies whether Gate should or shouldn't know about Unicode */
  static public void setUnicodeEnabled(boolean value){
    unicodeEnabled = value;
  }

  /** Checks wheter Gate is Unicode enabled */
  static public boolean isUnicodeEnabled(){
    return unicodeEnabled;
  }

  /** Does Gate know about Unicode? */
  static private boolean unicodeEnabled = false;


  /**
   * Finds all subclasses of a given class or interface. It will only search
   * within the loaded packages and not the entire classpath.
   * @param parent the class for which subclasses are sought
   * @return a list of {@link Class} objects.
   */
  static public List findSubclasses(Class parentClass){
    Package[] packages = Package.getPackages();
    List result = new ArrayList();
    for(int i = 0; i < packages.length; i++){
      String packageDir = packages[i].getName();
      //look in the file system
      if(!packageDir.startsWith("/")) packageDir = "/" + packageDir;
      packageDir.replace('.', Strings.getPathSep().charAt(0));
      URL packageURL = Gate.getClassLoader().getResource(packageDir);
      if(packageURL != null){
        File directory = new File(packageURL.getFile());
        if(directory.exists()){
          String [] files = directory.list();
          for (int j=0; j < files.length; j++){
            // we are only interested in .class files
            if(files[j].endsWith(".class")){
              // removes the .class extension
              String classname = files[j].substring(0, files[j].length() - 6);
              try {
                // Try to create an instance of the object
                Class aClass = Class.forName(packages[i] + "." + classname);
                if(parentClass.isAssignableFrom(aClass)) result.add(aClass);
              }catch(ClassNotFoundException cnfex){}
            }
          }
        }else{
          //look in jar files
          try{
            JarURLConnection conn = (JarURLConnection)packageURL.openConnection();
            String starts = conn.getEntryName();
            JarFile jFile = conn.getJarFile();
            Enumeration e = jFile.entries();
            while (e.hasMoreElements()){
              String entryname = ((ZipEntry)e.nextElement()).getName();
              if (entryname.startsWith(starts) &&
                  //not sub dir
                  (entryname.lastIndexOf('/')<=starts.length()) &&
                  entryname.endsWith(".class")){
                String classname = entryname.substring(0, entryname.length() - 6);
                if (classname.startsWith("/")) classname = classname.substring(1);
                classname = classname.replace('/','.');
                try {
                  // Try to create an instance of the object
                  Class aClass = Class.forName(packages[i] + "." + classname);
                  if(parentClass.isAssignableFrom(aClass)) result.add(aClass);
                }catch(ClassNotFoundException cnfex){}
              }
            }
          }catch(java.io.IOException ioe){}
        }
      }
    }
    return result;
  }
} // class Tools
