/*
 *	ExtensionFileFilter.java
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
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

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