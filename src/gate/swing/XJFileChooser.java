/*
 * Copyright (c) 1998-2009, The University of Sheffield.
 * Copyright (c) 2009-2009, Ontotext, Bulgaria.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June 1991 (in the distribution as file licence.html,
 * and also available at http://gate.ac.uk/gate/licence.html).
 *
 * Thomas Heitz - 15/09/2009
 *
 * $Id:$
 *
 */

package gate.swing;

import gate.gui.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * Extends {@link javax.swing.JFileChooser} to make sure the shared
 * {@link MainFrame} instance is used as a parent.
 *
 * Remember the last path used for the resource when loading/saving.
 * The class of the resource must be given in the variable
 * <code>resourceClassName</code>.
 */
public class XJFileChooser extends JFileChooser {
  /** key used when saving the file location to be retrieved later */
  private String resourceClassName;
  /** file name used instead of the one saved in the preferences */
  private String fileName;
  private Preferences node = Preferences.userNodeForPackage(MainFrame.class)
    .node("filechooserlocations");

  /**
   * Use this to set directly resourceClassName.
   * @param parent see {@link JFileChooser#showOpenDialog}
   * @param resourceClassName class name of the resource to load
   * @return see {@link JFileChooser#showOpenDialog}
   * @throws HeadlessException see {@link JFileChooser#showOpenDialog}
   */
  public int showOpenDialog(Component parent, String resourceClassName)
    throws HeadlessException {
    setResourceClassName(resourceClassName);
    return showOpenDialog(parent);
  }

  /**
   * Use this to set directly resourceClassName.
   * @param parent see {@link JFileChooser#showOpenDialog}
   * @param resourceClassName class name of the resource to save
   * @return see {@link JFileChooser#showOpenDialog}
   * @throws HeadlessException see {@link JFileChooser#showOpenDialog}
   */
  public int showSaveDialog(Component parent, String resourceClassName)
    throws HeadlessException {
    setResourceClassName(resourceClassName);
    return showSaveDialog(parent);
  }

  /**
   * Overridden to make sure the shared MainFrame instance is used as
   * a parent when no parent is specified
   */
  public int showDialog(Component parent, String approveButtonText)
  throws HeadlessException {
    setSelectedFileFromPreferences();
    return super.showDialog((parent != null) ? parent :
      (MainFrame.getFileChooser() != null) ? MainFrame.getInstance() :
        null, approveButtonText);
  }

  /**
   * If possible, set the last directory/file used by the resource as
   * the current directory/file otherwise use the user home directory.
   */
  public void setSelectedFileFromPreferences() {
    String lastUsedPath = null;
    if (resourceClassName != null) {
      lastUsedPath = node.get(resourceClassName, null);
    }
    File file;
    if (lastUsedPath != null && fileName != null) {
      file = new File(lastUsedPath, fileName);
    } else if (lastUsedPath != null) {
      file = new File(lastUsedPath);
    } else if (fileName != null) {
      file = new File(System.getProperty("user.home"), fileName);
    } else {
      file = new File(System.getProperty("user.home"));
    }
    setSelectedFile(file);
    ensureFileIsVisible(file);
  }

  /**
   * Set the file name to be used instead of the one saved in the preferences.
   * @param fileName file name
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /** overriden to first save the location of the file chooser
   *  for the current resource. */
  public void approveSelection() {
    if (resourceClassName != null && getSelectedFile() != null) {
      try {
        String filePath = getSelectedFile().getCanonicalPath();
        node.put(resourceClassName, filePath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    super.approveSelection();
    resetChoosableFileFilters();
  }

  /**
   * Set the resource to search for the last path used before to call
   * {@link #showDialog}.
   * @param name resource to be selected in the dialog.
   */
  public void setResourceClassName(String name) {
    resourceClassName = (name == null) ? null :
      (name.length() <= Preferences.MAX_KEY_LENGTH) ? name :
        name.substring(name.length()-Preferences.MAX_KEY_LENGTH);
  }

  /** Overriden to test first if the file exists */
  public void ensureFileIsVisible(File f) {
    if(f != null && f.exists()) super.ensureFileIsVisible(f);
  }

  /** Overriden to test first if the file exists */
  public void setSelectedFile(File file) {
    if(file != null){
      if(file.exists() ||
         (file.getParentFile() != null && file.getParentFile().exists())){
        super.setSelectedFile(file);
      }
    }
  }

  /** overriden to add a filter only if not already present */
  public void addChoosableFileFilter(FileFilter filterToAdd) {
    for (FileFilter filter : getChoosableFileFilters()) {
      if (filter.getDescription().equals(filterToAdd.getDescription())) {
        setFileFilter(filter);
        return;
      }
    }
    super.addChoosableFileFilter(filterToAdd);
  }
}
