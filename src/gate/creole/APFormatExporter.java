/*
 *  APFormatExporter.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 26/Oct/2001
 *
 *  $Id$
 */

package gate.creole;

import gate.*;
import gate.creole.orthomatcher.*;
import gate.util.*;

import java.util.*;
import java.net.*;
import java.io.*;

/** This class implements a APF xml exporter. It works on documents or corpora
  * to export them in the APF format.
  */
public class APFormatExporter extends AbstractLanguageAnalyser{
  /** Debug flag */
  private static final boolean DEBUG = true;
  /** Constructor does nothing. This PR is bean like initialized*/
  public APFormatExporter() {}

  /** Run the resource and does the entire export process*/
  public void execute() throws ExecutionException{
    // Check if the thing can be run
    if(document == null)
      throw new ExecutionException("No document found to export in APF format!");
    if (exportedTypes == null)
      throw new ExecutionException("No export types found.");

    initDocId();
    if (docId == null)
      throw new ExecutionException("Couldn't detect the document's ID");
    if (DEBUG)
      Out.prln("Document id = "+ docId);

    String exportFilePathStr = null;
    if (exportFilePath == null)
      exportFilePathStr = new String(document.getSourceUrl().getFile() +
                                                                  ".apf.xml");
    else
      exportFilePathStr = exportFilePath.getFile();

    if (DEBUG)
      Out.prln("Export file path = "+ exportFilePathStr);
//*
    // Prepare to write into the xmlFile
    OutputStreamWriter writer = null;
    try{
      writer = new OutputStreamWriter(
              new FileOutputStream(new File(exportFilePathStr)));

      // Write (test the toXml() method)
      // This Action is added only when a gate.Document is created.
      // So, is Bor sure that the resource is a gate.Document
      serializeDocumentToAPF();
      writer.write(xmlDoc.toString());
      writer.flush();
      writer.close();
    }catch (Exception e){
      throw new ExecutionException(e);
    }// End try
//*/
  } // execute()


  /** Initialise this resource, and returns it. */
  public Resource init() throws ResourceInstantiationException {
    //document.getSourceUrl().getProtocol()
    xmlDoc = new StringBuffer();
    return this;
  } // init()

  /** Java bean style mutator for exportedTypes */
  public void setExportedTypes(List anExportedTypesList){
    exportedTypes = anExportedTypesList;
  }// setExportedTypes();

  /** Java bean style accesor for exportedTypes */
  public List getExportedTypes(){
    return exportedTypes;
  }// getExportedTypes()

  /** Java bean style mutator for dtdFileName */
  public void setDtdFileName(String aDtdFileName){
    dtdFileName = aDtdFileName;
  }// setDtdFileName();

  /** Java bean style accesor for DtdFileName */
  public String getDtdFileName(){
    return dtdFileName;
  }// getDtdFileName()

  /** Java bean style mutator for exportFilePath */
  public void setExportFilePath(URL anExportFilePath){
    exportFilePath = anExportFilePath;
  }// setExportFilePath();

  /** Java bean style accesor for exportFilePath */
  public URL getExportFilePath(){
    return exportFilePath;
  }// getDtdFileName()

  /** Initialises the docId with documents' file name without the complete path*/
  private void initDocId(){
    StringTokenizer filePathToken = new StringTokenizer(
                                      document.getSourceUrl().getFile(),"/");
    String fileName = new String("");
    while (filePathToken.hasMoreTokens()){
      fileName = filePathToken.nextToken();
    }// End while
    // File name contains now the last token
    if (DEBUG)
      Out.prln("From initDocId, fileName ="+ fileName);
    StringTokenizer fileNameTokenizer = new StringTokenizer(fileName,".");
    StringBuffer tmpDocId = new StringBuffer("");
    while(fileNameTokenizer.hasMoreTokens()){
      String token = (String)fileNameTokenizer.nextToken();
      // We don't want to append the last token
      if (fileNameTokenizer.hasMoreTokens())
        tmpDocId.append(token + ".");
    }// End while
    // if tokenization had place
    if (!"".equals(tmpDocId)){
      // Remove the last dot
      tmpDocId.replace(tmpDocId.length()-1,tmpDocId.length(),"");
      docId = tmpDocId.toString();
    }// End if
  }// initDocId()

  /** Returns the xml document conforming to APF dtd.*/
  protected void serializeDocumentToAPF(){
    xmlDoc.append("<?xml version=\"1.0\" ?>\n");
    xmlDoc.append("<!DOCTYPE source_file SYSTEM ");
    if (dtdFileName == null)
      xmlDoc.append("\"ace-pilot-ref.dtd\"");
    else
      xmlDoc.append("\""+dtdFileName+"\"");
    xmlDoc.append(">\n");
    xmlDoc.append("<source_file TYPE=\"text\" VERSION=\"1.2\" URI=\"");
    xmlDoc.append(docId);
    xmlDoc.append("-lf\">\n");
    xmlDoc.append("  <document DOCID=\"");
    xmlDoc.append(docId + "\">\n");
    serializeEntities();
    xmlDoc.append("  </document>\n");
    xmlDoc.append("</source_file>");
  }// serializeDocumentToAPF()

  /** Transforms all the entities from exportedTypes found in the GATE document
    * into their xml representation
    */
  protected void serializeEntities(){
    // If no types founded then simply return
    if (exportedTypes == null || exportedTypes.isEmpty()) return;

    FeatureMap entitiesMap = null;
    if ( document.getFeatures() == null ||
         document.getFeatures().get(OrthoMatcher.DOC_MATCHES_FEATURE)== null)
      entitiesMap = Factory.newFeatureMap();
    else
      entitiesMap = (FeatureMap)document.getFeatures().
                                        get(OrthoMatcher.DOC_MATCHES_FEATURE);
    // The entities map is a map from annotation sets names to list of lists
    // Each list element is composed from annotations refering the same entity
    // All the entities that are in the exportedTypes need to be serialized.
    Iterator exportedTypesIter = exportedTypes.iterator();
    while(exportedTypesIter.hasNext()){
      String entityType = (String)exportedTypesIter.next();
      // Serialize all entities of type
      // The keys in the entiitesMap are annotation sets names. The null key
      // designates the default annotation.
      //document.getNamedAnnotationSets();
      Set entitiesMapKeySet = entitiesMap.keySet();
      Iterator entitiesKeysIter = entitiesMapKeySet.iterator();
      while (entitiesKeysIter.hasNext()){
        Object annotSetName = entitiesKeysIter.next();
        // This list contains entities found in the annotSetName
        List entitiesList = (List) entitiesMap.get(annotSetName);
        // This annotation set will contain all annotations of "entityType"
        Set annotSet = null;
        if (annotSetName == null)
          // Get all annotations of type entityType from the default annot set
          annotSet = document.getAnnotations().get(entityType);
        else
          // Get all annotations of type entityType from the annotSetName
          annotSet = document.getAnnotations((String)annotSetName).get(entityType);
        if (annotSet == null) annotSet = new HashSet();
        // All annotations from annotSet will be serialized as entities unless
        // some of them are present in the entities map
        // Now we are searching for the entityType in the entitiesMap and
        // serialize it from there. After that, remove all annotations
        // entityType present in entitiesMap from annotSet and serialize the
        // remaining entities.
        //Iterate through the entitiesList in searching for entityType
        Iterator entitiesListIter = entitiesList.iterator();
        while (entitiesListIter.hasNext()){
          List entity = (List)entitiesListIter.next();
          // We want now to accesate an annotation from the entity list to get
          // its type and compare it with entityType
          String theEntityType = new String("");
          if (entity != null && !entity.isEmpty()){
            Annotation a = (Annotation)entity.get(0);
            if (a!= null) theEntityType = a.getType();
          }// End if
          // The the types are equal then serialize the entities
          if (theEntityType.equals(entityType)){
            serializeAnEntity(entity);
            // Remove all annotation from entity that apear in annotSet
            annotSet.removeAll(entity);
          }// End if
        }// End while(entitiesListIter.hasNext())
        // Serialize the remaining entities in annotSet
        Iterator annotSetIter = annotSet.iterator();
        while(annotSetIter.hasNext()){
          Annotation annotEntity = (Annotation) annotSetIter.next();
          List ent = new ArrayList();
          ent.add(annotEntity);
          serializeAnEntity(ent);
        }// End while(annotSetIter.hasNext())
      }// End while(entitiesKeysIter.hasNext())
    }// End while(exportedTypesIter.hasNext())
  }// serializeEntities()

  /** Writes an entity in the xmlDoc conforming to APF standards.
    * @param anEntity represents a list with annotations that refer the same
    * entity. Those annotations were detected and constructed by the
    * orthomatcher.
    */
  private void serializeAnEntity(List anEntity){
    if (anEntity == null || anEntity.isEmpty()) return;
    // Write the entities tags
    xmlDoc.append("  <entity ID=\"" + docId + "-" + getNextEntityId() + "\">\n");
    // We know for sure that the list is not empty (see above)
    Annotation a = (Annotation) anEntity.get(0);
    xmlDoc.append("    <entity_type>" + a.getType() + "</entity_type>\n");
    // Write the entities mentions
    Iterator anEntityIter = anEntity.iterator();
    while(anEntityIter.hasNext()){
      Annotation ann = (Annotation)anEntityIter.next();
      serializeAnEntityMention(ann);
    }// End while(anEntityIter.hasNext())
    // Write the entities attributes
    anEntityIter = anEntity.iterator();
    while(anEntityIter.hasNext()){
      Annotation ann = (Annotation)anEntityIter.next();
      serializeAnEntityAttributes(ann);
    }// End while(anEntityIter.hasNext())
    xmlDoc.append("  </entity>");
  }// End serializeAnEntity();

  /** This method serializes an entity mention from an Annotation*/
  private void serializeAnEntityMention(Annotation ann){
    if (ann == null) return;
    String entityMentionType = "NAME";
    FeatureMap fm = ann.getFeatures();
    if (fm != null && null != fm.get("ENTITY_MENTION_TYPE"))
      entityMentionType = (String) fm.get("ENTITY_MENTION_TYPE");
    xmlDoc.append("      <entity_mention TYPE=\""+entityMentionType+"\">\n");
    // extent
    xmlDoc.append("        <extent>\n");
    xmlDoc.append("          <charseq>\n");
    try{
      xmlDoc.append("          <!-- string = " +
            document.getContent().getContent(ann.getStartNode().getOffset(),
                                      ann.getEndNode().getOffset())+" -->\n");
    }catch (InvalidOffsetException ioe){
      Err.prln("APFormatExporter:Warning: Couldn't access text between"+
      " offsets:" + ann.getStartNode().getOffset() + " and "+
      ann.getEndNode().getOffset());
    }// End try
    xmlDoc.append("<start>"+ann.getStartNode().getOffset()+
        "</start><end>"+ann.getEndNode().getOffset()+"</end>\n");
    xmlDoc.append("          </charseq>\n");
    xmlDoc.append("        </extent>\n");
    // head
    xmlDoc.append("        <head>\n");
    xmlDoc.append("          <charseq>\n");
    try{
      xmlDoc.append("          <!-- string = " +
            document.getContent().getContent(ann.getStartNode().getOffset(),
                                      ann.getEndNode().getOffset())+" -->\n");
    }catch (InvalidOffsetException ioe){
      Err.prln("APFormatExporter:Warning: Couldn't access text between"+
      " offsets:" + ann.getStartNode().getOffset() + " and "+
      ann.getEndNode().getOffset());
    }// End try
    xmlDoc.append("<start>"+ann.getStartNode().getOffset()+
        "</start><end>"+ann.getEndNode().getOffset()+"</end>\n");
    xmlDoc.append("          </charseq>\n");
    xmlDoc.append("        </head>\n");
    xmlDoc.append("      </entity_mention>\n");
  }//serializeAnEntityMention();

  /** This method serializes an entity attribute from an Annotation*/
  private void serializeAnEntityAttributes(Annotation ann){
    if (ann == null) return;
    xmlDoc.append("      <entity_attributes>\n");
    // name
    xmlDoc.append("        <name>\n");
    xmlDoc.append("          <charseq>\n");
    try{
      xmlDoc.append("          <!-- string = " +
            document.getContent().getContent(ann.getStartNode().getOffset(),
                                      ann.getEndNode().getOffset())+" -->\n");
    }catch (InvalidOffsetException ioe){
      Err.prln("APFormatExporter:Warning: Couldn't access text between"+
      " offsets:" + ann.getStartNode().getOffset() + " and "+
      ann.getEndNode().getOffset());
    }// End try
    xmlDoc.append("<start>"+ann.getStartNode().getOffset()+
        "</start><end>"+ann.getEndNode().getOffset()+"</end>\n");
    xmlDoc.append("          </charseq>\n");
    xmlDoc.append("        </name>\n");
    xmlDoc.append("      </entity_attributes>\n");
  }//serializeAnEntityMention();

  /** Returns the next safe ID for an entity*/
  private int getNextEntityId(){
    return entityId ++;
  }// getNextEntityId()

  /** This list of strings represents the entities type that will be exported*/
  private List exportedTypes = null;
  /** This is the name of the dtd file. If it's not present no dtd would be
    * written in the APF file.
    */
  private String dtdFileName = null;
  /** This field represent the document id and it is used in generating the
    * entities IDs. It is the file name of the document, without the extension
    */
  private String docId = null;

  /** This field represent an unique entity ID generator*/
  private int entityId = 1;
  /** This is the xmlDoc that will be created*/
  private StringBuffer xmlDoc = null;

  private URL exportFilePath = null;

}// APFormatExporter