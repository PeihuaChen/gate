/*
 *  ExtensionFileFilter.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 22/May/2000
 *
 *  $Id$
 */
package gate.gui;


import javax.swing.filechooser.*;
import java.io.*;
import java.util.*;

/**
 * This class is used by {@link javax.swing.JFileChooser} to filter the
 * displayed files by their extension.
 *
 */
public class ExtensionFileFilter extends javax.swing.filechooser.FileFilter{

  /** Debug flag
   */
  private static final boolean DEBUG = false;

  /**
   * Builds a new ExtensionFileFilter
   */
  public ExtensionFileFilter() {
  }

  /**
   * Checks a file for compliance with the requested extensions.
   *
   * @param f
   */
  public boolean accept(File f){
    String name = f.getName();
    if(f.isDirectory()) return true;
    boolean res = false;

    if(name.indexOf('.') != -1){
      String extension = name.substring(name.lastIndexOf('.')+1,name.length());
      Iterator extIter = acceptedExtensions.iterator();
      while(extIter.hasNext()){
        if(((String)extIter.next()).equalsIgnoreCase(extension)) res=true;
      }
    };
    return res;
  }

  /**
   * Returns the user-frielndly description for the files, e.g. "Text files"
   *
   */
  public String getDescription() {
    return description;
  }

  /**
   * Adds a new extension to the list of accepted extensions.
   *
   * @param ext
   */
  public void addExtension(String ext) {
    acceptedExtensions.add(ext);
  }

  /**
   * Sets the user friendly description for the accepted files.
   *
   * @param desc
   */
  public void setDescription(String desc) {
    description = desc;
  }

  /**
   * The set of accepted extensions
   *
   */
  private Set acceptedExtensions = new HashSet();

  /**
   * The desciption of the accepted files.
   *
   */
  private String description;

} // ExtensionFileFilter
