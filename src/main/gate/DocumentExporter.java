/*
 *  Copyright (c) 1995-2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Mark A. Greenwood 11/07/2014
 *
 */

package gate;

import gate.creole.AbstractResource;
import gate.util.ExtensionFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.IOUtils;

public abstract class DocumentExporter extends AbstractResource {

  private static final long serialVersionUID = -4810523902750051704L;

  protected String fileType, defaultExtension;
  protected FileFilter filter;
  
  public DocumentExporter(String fileType, String defaultExtension) {
    this.fileType = fileType;
    this.defaultExtension = defaultExtension;
    filter = new ExtensionFileFilter(fileType+" Files (*."+defaultExtension+")",defaultExtension);    
  }
  
  public String getFileType() {
    return fileType;
  }
  
  public String getDefaultExtension() {
    return defaultExtension;
  }
  
  public FileFilter getFileFilter() {
    return filter;
  }
    
  /**
   * Equivalent to {@link #export(Document,File, Map)} with an empty map
   * of options.
   */
  public void export(Document doc, File file) throws IOException {
    export(doc, file, Factory.newFeatureMap());
  }

  /**
   * Equivalent to {@link #export(Document,OutputStream, Map)} using a
   * FileOutputStream instance constructed from the File param.
   */
  public void export(Document doc, File file, FeatureMap options)
          throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      export(doc, new FileOutputStream(file), options);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  /**
   * Equivalent to {@link #export(Document,OutputStream)} with an empty
   * map of options.
   */
  public void export(Document doc, OutputStream out) throws IOException {
    export(doc, out, Factory.newFeatureMap());
  }

  /**
   * Exports the provided {@link Document} instance to the specified
   * {@link OutputStream} using the specified options.
   * 
   * @param doc the document to export
   * @param out the OutputStream to export the document to
   * @param options DocumentExporter specific options
   */
  public abstract void export(Document doc, OutputStream out, FeatureMap options) throws IOException;
}
