
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
import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.arp.*;

import gate.*;


public class RDFFormatExporter extends AbstractLanguageAnalyser {

  private static final int DAML_EXPORT = 0;
  private static final int RDF_EXPORT = 1;

  private static final String[] EXPORT_FORMATS = {"DAML+OIL","RDF"};
  private static final String[] EXPORT_EXTS = {"daml","rdf"};

  private static final String ONTOGAZ_CLASS_FEATURE = "class";
  private static final String ONTOGAZ_ONTOLOGY_FEATURE = "ontology";

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
  public gate.Resource init() throws ResourceInstantiationException {
    return this;
  } // init()

  /** Run the resource and does the entire export process*/
  public void execute() throws ExecutionException{
System.out.println("execute called...");
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

//    StringBuffer rdfDoc = new StringBuffer(10*(document.getContent().size().intValue()));

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
System.out.println("export path:" +exportFilePathStr);
    // Prepare to write into the xmlFile
    FileWriter  writer = null;
    try{
      writer = new FileWriter(exportFilePathStr,false);
      annotations2ontology(writer);
      writer.flush();
      writer.close();
    }catch (Exception e){
      throw new ExecutionException(e);
    }// End try

  } // execute()

  private void annotations2ontology(Writer output) throws ExecutionException {
System.out.println("a2o called...");
    DAMLModel ontologyModel, instanceModel;
    HashMap ontologies = new HashMap();

    try {
      ontologyModel = new DAMLModelImpl();
      instanceModel = new DAMLModelImpl();

      Assert.assertNotNull(ontologyModel);
      Assert.assertNotNull(instanceModel);

      Assert.assertNotNull(this.ontologyLocation);
      ontologyModel.read(this.ontologyLocation.toString());
System.out.println("model read...");
      //get a mapping: class name to DAML class
      HashMap ontologyMap = ontology2hashmap(ontologyModel);
      Assert.assertNotNull(ontologyMap);

      //add the mapping to the ontologies hashmap
      //key is ontology URL as generated by the OntoGaz
      ontologies.put(this.ontologyLocation.toString(),ontologyMap);

      if (null == ontologyModel) {
        throw new ExecutionException("cannot read ontology");
      }

      Iterator itClasses = ontologyModel.listDAMLClasses();

/*
      Set setClasses = new HashSet();
      while (itClasses.hasNext()) {
        DAMLClass cls = (DAMLClass)itClasses.next();
        String className = cls.getLocalName();
        setClasses.add(className.toLowerCase());
System.out.println(className);
      }
*/
System.out.println("exporting: "+this.exportedTypes);
      Iterator itTypes = this.exportedTypes.iterator();
      while (itTypes.hasNext()) {

        String type = (String)itTypes.next();
        AnnotationSet as = this.document.getAnnotations().get(type);

        if (null == as || true == as.isEmpty()) {
          continue;
        }

        Iterator itAnnotations = as.iterator();
        while (itAnnotations.hasNext()) {

          Annotation ann = (Annotation)itAnnotations.next();
          Assert.assertTrue(ann.getType().equals(type));

          FeatureMap features = ann.getFeatures();
          String annClass = (String)features.get(ONTOGAZ_CLASS_FEATURE);
          String annOntology = (String)features.get(ONTOGAZ_ONTOLOGY_FEATURE);

          if (null == annClass) {
gate.util.Err.prln("skipping annotation - class unknown ["+ann+"]");
            continue;
          }
gate.util.Err.prln("EXPORTING annotation - class : ["+annClass+"]");
          //is this a new ontology?
          if (false == ontologies.containsKey(annOntology)) {
            //oops, new ontology:
            //1. create model for it
            //2. create class name 2 daml class mapping
            //3. add it to hashmap

            //1.
            DAMLModel model = new DAMLModelImpl();
            model.read(annOntology);

            //2.
            //create mapping between class names and DAML classes
            HashMap name2class = ontology2hashmap(model);
            Assert.assertNotNull(name2class);

            //3.
            ontologies.put(annOntology,model);
          }

          //get the class of the annotation
          DAMLClass damlClass = (DAMLClass)((HashMap)ontologies.get(annOntology)).get(annClass);
          Assert.assertNotNull(damlClass);

          DAMLClass annInstance = instanceModel.createDAMLClass(annClass);
          annInstance.prop_subClassOf().add(damlClass);
        }//while
      }//while


      //print the model into file
      instanceModel.write(output,"RDF/XML-ABBREV");

    }
    catch (Exception e) {
      throw new ExecutionException(e);
    }

  }

  private HashMap ontology2hashmap(DAMLModel ontology) throws ExecutionException {

    HashMap result = null;

    //0.
    Assert.assertNotNull(ontology);

    try {
      result = new HashMap((int)ontology.size()/3);

      //1.Iterate classes
      Iterator itClasses = ontology.listDAMLClasses();
      while (itClasses.hasNext()) {
        DAMLClass clazz = (DAMLClass)itClasses.next();
        Assert.assertNotNull(clazz.getLocalName());
        result.put(clazz.getLocalName(),clazz);
      }
    }
    catch(RDFException rdfe) {
      throw new ExecutionException(rdfe);
    }

    return result;
  }

}