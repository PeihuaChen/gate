
/*
 *  RDFFormatExporter.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
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

import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.util.*;

import junit.framework.Assert;

import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.DAMLModelImpl;
import com.hp.hpl.mesa.rdf.jena.common.RDFWriterFImpl;
import com.hp.hpl.mesa.rdf.jena.model.RDFWriter;

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

  private String annotationSetName = null;

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

  /** Java bean style accessor for annotationSetName */
  public String getAnnotationSetName() {
    return annotationSetName;
  } //getAnnotationSetName


  /** Java bean style mutator for annotaionSetName */
  public void setAnnotationSetName(String annotationSetName) {
    this.annotationSetName = annotationSetName;
  }

  /** Initialise this resource, and returns it. */
  public gate.Resource init() throws ResourceInstantiationException {
    return this;
  } // init()


  /** Run the resource and does the entire export process*/
  public void execute() throws ExecutionException{

    // Check if the thing can be run
    if(document == null) {
      throw new ExecutionException("No document found to export in APF format!");
    }

    /* Commented by Niraj to include support for annotationSetName where all the
     * annotations should be exported incase exportedTypes is null
    if (exportedTypes == null) {
      throw new ExecutionException("No export types found.");
    }*/

//    StringBuffer rdfDoc = new StringBuffer(10*(document.getContent().size().intValue()));

    String exportFilePathStr = null;

    if (exportFilePath == null) {
      exportFilePathStr = new String(document.getSourceUrl().getFile() + "." +
                                    EXPORT_EXTS[this.exportFormat]);
    }
    else {
      exportFilePathStr = new String(exportFilePath.getPath()+
                                    "/" +
                                    document.getName() + "." +
                                    EXPORT_EXTS[this.exportFormat]);
    }
//System.out.println("export path:" +exportFilePathStr);
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

  private void annotations2ontology(Writer output) throws Exception {

    DAMLModel ontologyModel, instanceModel;
    HashMap ontologies = new HashMap();
    HashMap instanceMatches = new HashMap();
    HashSet instanceNames = new HashSet();

      ontologyModel = new DAMLModelImpl();
      instanceModel = new DAMLModelImpl();

      Assert.assertNotNull(ontologyModel);
      Assert.assertNotNull(instanceModel);

      //final settings of the model
      DAMLOntology onto = instanceModel.createDAMLOntology("");
      onto.prop_comment().addValue("autogenerated from GATE RDFFormatExporter");
      onto.prop_versionInfo().addValue("1.0");

      Assert.assertNotNull(this.ontologyLocation);
      ontologyModel.read(this.ontologyLocation.toString());

      //get a mapping: class name to DAML class
      HashMap ontologyMap = ontology2hashmap(ontologyModel);
      Assert.assertNotNull(ontologyMap);

      //add the mapping to the ontologies hashmap
      //key is ontology URL as generated by the OntoGaz
      ontologies.put(this.ontologyLocation.toString(),ontologyMap);

      if (null == ontologyModel) {
        throw new ExecutionException("cannot read ontology");
      }

      HashMap defaultClasses = new HashMap((int)ontologyModel.size()/5);
      Iterator itClasses = ontologyModel.listDAMLClasses();
      while (itClasses.hasNext()) {
        DAMLClass cls = (DAMLClass)itClasses.next();
        String className = cls.getLocalName();
        if (null != className) {
          defaultClasses.put(className.toLowerCase(),cls);
        }
      }

      //* Addition by Niraj to include AnnotationSet Support */
      AnnotationSet inputAs = (annotationSetName == null ||
                               annotationSetName.length() == 0) ?
                               document.getAnnotations() :
                               document.getAnnotations(annotationSetName);

      // see if exportedTypes is null
      Iterator itTypes = (exportedTypes == null || exportedTypes.size() == 0) ?
                       inputAs.getAllTypes().iterator() : exportedTypes.iterator();

      //Iterator itTypes = this.exportedTypes.iterator();
      // End of addition

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
          DAMLClass damlClass = null;

          if (null == annClass) {
            //no ontological info
            //try to get proper class from the default ontology
            if (defaultClasses.containsKey(ann.getType().toLowerCase())) {
              //bingo
              //we have a class with the name of the annotation's type
              damlClass = (DAMLClass)defaultClasses.get(ann.getType().toLowerCase());
              Assert.assertNotNull(damlClass);
            }
            else {
              continue;
            }
          }
          else {
            //ontological info available
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
            damlClass = (DAMLClass)((HashMap)ontologies.get(annOntology)).get(annClass);
            Assert.assertNotNull(damlClass);
          }

          String instanceName = this.document.getContent().getContent(
                                                                  ann.getStartNode().getOffset(),
                                                                  ann.getEndNode().getOffset())
                                .toString();
          Assert.assertNotNull(instanceName);

          //create instance of proper type only if new
          if (instanceNames.contains(instanceName)) {
            continue;
          }

          DAMLInstance annInstance = instanceModel.createDAMLInstance(damlClass,instanceName);
          instanceNames.add(instanceName);

          //check orhtographic matches
          List matches = (List)ann.getFeatures().get("matches");
          if (null != matches) {
            //try to get equiv instance
            if (instanceMatches.containsKey(matches)) {
              DAMLInstance equivInstance = (DAMLInstance)instanceMatches.get(matches);

              //make sure we don't have duplicated name
              annInstance.prop_sameIndividualAs().add(equivInstance);
            }
            else {
              //first entry of the coref chain
              instanceMatches.put(matches,annInstance);
            }
          }


        }//while
      }//while

      //print the model into file
      RDFWriter rdfWriter = new RDFWriterFImpl().getWriter("RDF/XML-ABBREV");
      rdfWriter.setNsPrefix("gate",this.ontologyLocation.toString()+"#");
      rdfWriter.write(instanceModel,output,null);
  }

  private HashMap ontology2hashmap(DAMLModel ontology) throws Exception {

    HashMap result = null;

    //0.
    Assert.assertNotNull(ontology);


    result = new HashMap((int)ontology.size()/5);

    //1.Iterate classes
    Iterator itClasses = ontology.listDAMLClasses();
    while (itClasses.hasNext()) {
      DAMLClass clazz = (DAMLClass)itClasses.next();
      //Assert.assertNotNull(clazz.getLocalName());
      if (null != clazz.getLocalName()) {
        result.put(clazz.getLocalName(),clazz);
      }

    }

    return result;
  }
}