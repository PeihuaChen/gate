/*
*	ExtensionFileFilter.java
*
*	Valentin Tablan, 22/May/2000
*
*	$Id$
*/
package gate.gui;


import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;

/**
* This class 
*/
public class ExtensionFileFilter extends javax.swing.filechooser.FileFilter{

  public ExtensionFileFilter() {
  }

  public boolean accept(File f){
    String name = f.getName();
    if(f.isDirectory()) return true;
    boolean res = false;
    if(name.indexOf('.') != -1){
      String extension = name.substring(name.indexOf('.')+1,name.length());
      Iterator extIter = acceptedExtensions.iterator();
      while(extIter.hasNext()){
        if(((String)extIter.next()).equalsIgnoreCase(extension)) res=true;
      }
    };
    return res;
  }

  public String getDescription(){
    return description;
  }

  public void addExtension(String ext){
    acceptedExtensions.add(ext);
  }

  public void setDescription(String desc){
    description = desc;
  }

  private Set acceptedExtensions = new HashSet();
  private String description;

}