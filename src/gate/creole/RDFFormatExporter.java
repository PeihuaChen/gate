
/*
 *  RDFFormatExporter.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 07/May/2002
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.net.*;
import java.io.*;

import junit.framework.*;

import gate.*;


public class RDFFormatExporter extends AbstractLanguageAnalyser {

  private static final int DAML_EXPORT = 0;
  private static final int RDF_EXPORT = 1;

  private static final String[] EXPORT_FORMATS = {"DAML+OIL","RDF"};
  private static final String[] EXPORT_EXTS = {"daml","rdf"};

  /** Debug flag */
  private static final boolean DEBUG = false;

  private int exportFormat;

  /** This list of strings represents the entities type that will be exported*/
  private List exportedTypes = null;

  private URL exportFilePath = null;

  private URL ontologyLocation = null;

  public RDFFormatExporter() {
  }

  /** Java bean style mutator for exportedTypes */
  public void setExportedTypes(List anExportedTypesList){
    exportedTypes = anExportedTypesList;
  }// setExportedTypes();


  /** Java bean style accesor for exportedTypes */
  public List getExportedTypes(){
    return exportedTypes;
  }// getExportedTypes()

  /** Java bean style mutator for exportedTypes */
  public void setExportFormat(String format){

    Assert.assertTrue(format.equalsIgnoreCase(EXPORT_FORMATS[DAML_EXPORT]) ||
                      format.equalsIgnoreCase(EXPORT_FORMATS[RDF_EXPORT]));

    if (format.equalsIgnoreCase(EXPORT_FORMATS[DAML_EXPORT])) {
      this.exportFormat = DAML_EXPORT;
    }
    else if (format.equalsIgnoreCase(EXPORT_FORMATS[RDF_EXPORT])) {
      this.exportFormat = RDF_EXPORT;
    }
    else {
      Assert.fail();
    }

  }// setExportedTypes();

  /** Java bean style mutator for exportedTypes */
  public String getExportFormat() {
    return EXPORT_FORMATS[this.exportFormat];
  }// setExportedTypes();

  /** Java bean style mutator for exportFilePath */
  public void setExportFilePath(URL anExportFilePath){
    exportFilePath = anExportFilePath;
  }// setExportFilePath();

  /** Java bean style accesor for exportFilePath */
  public URL getExportFilePath(){
    return exportFilePath;
  }// getDtdFileName()

  /** Java bean style mutator for exportFilePath */
  public void setOntology(URL _ontologyLocation){
    ontologyLocation = _ontologyLocation;
  }// setExportFilePath();

  /** Java bean style accesor for exportFilePath */
  public URL getOntology(){
    return ontologyLocation;
  }// getDtdFileName()

  /** Initialise this resource, and returns it. */
  public Resource init() throws ResourceInstantiationException {
    return this;
  } // init()

  /** Run the resource and does the entire export process*/
  public void execute() throws ExecutionException{
    // Check if the thing can be run
    if(document == null) {
      throw new ExecutionException("No document found to export in APF format!");
    }

    if (exportedTypes == null) {
      throw new ExecutionException("No export types found.");
    }

    if (exportedTypes == null) {
      throw new ExecutionException("No export types found.");
    }

    StringBuffer rdfDoc = new StringBuffer(10*(document.getContent().size().intValue()));

    String exportFilePathStr = null;

    if (exportFilePath == null) {
      exportFilePathStr = new String(document.getSourceUrl().getFile() +
                                    EXPORT_EXTS[this.exportFormat]);
    }
    else {
      exportFilePathStr = new String(exportFilePath.getPath()+
                                    "/" +
                                    document.getName() +
                                    EXPORT_EXTS[this.exportFormat]);
    }

    // Prepare to write into the xmlFile
    FileWriter  writer = null;
    try{
      writer = new FileWriter(exportFilePathStr,false);

      // Write (test the toXml() method)
      // This Action is added only when a gate.Document is created.
      // So, is Bor sure that the resource is a gate.Document
      annotations2ontology(rdfDoc);
      writer.write(rdfDoc.toString());
      writer.flush();
      writer.close();
    }catch (Exception e){
      throw new ExecutionException(e);
    }// End try
//*/
  } // execute()

  protected void annotations2ontology(StringBuffer buff){
  }

}