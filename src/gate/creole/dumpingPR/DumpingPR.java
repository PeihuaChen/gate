/*
 *  DumpingPR.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 19/10/2001
 *
 *  $Id$
 */

package gate.creole.dumpingPR;

import java.util.*;
import gate.*;
import gate.creole.*;
import gate.util.*;
import java.net.URL;
import java.io.*;

/**
 * This class implements a DumpingPR which exports a given set of annotation
 * types + the original markup, back into the document's native format.
 * The export might also include the GATE features of those annotations or
 * not (the default). One can also control whether the export files have a
 * new suffix (useSuffixForDumpFiles) and what this suffix is
 * (suffixForDumpFiles). By default, a suffix is used and it is .gate.
 */
public class DumpingPR extends AbstractLanguageAnalyser
  implements ProcessingResource {

  public static final String
    DPR_DOCUMENT_PARAMETER_NAME = "document";

  public static final String
    DPR_ANN_SET_PARAMETER_NAME = "annotationSetName";

  public static final String
    DPR_ANN_TYPES_PARAMETER_NAME = "annotationTypes";

  public static final String
    DPR_DUMP_TYPES_PARAMETER_NAME = "dumpTypes";

  public static final String
    DPR_OUTPUR_URL_PARAMETER_NAME = "outputFileUrl";

  public static final String
    DPR_INCLUDE_FEAT_PARAMETER_NAME = "includeFeatures";

  public static final String
    DPR_USE_SUFFIX_PARAMETER_NAME = "useSuffixForDumpFiles";

  public static final String
    DPR_FILE_SUFFIX_PARAMETER_NAME = "suffixForDumpFiles";

  private static final boolean DEBUG = false;

  /**
   * A list of annotation types, which are to be dumped into the output file
   */
  protected List annotationTypes;

  /**
   * A list of strings specifying new names to be used instead of the original
   * annotation types given in the annotationTypes parameter. For example, if
   * annotationTypes was set to [Location, Date], then if dumpTypes is set to
   * [Place, Date-expr], then the labels <Place> and <Date-expr> will be inserted
   * instead of <Location> and <Date>.
   */
  protected List dumpTypes;

  /**the name of the annotation set
   * from which to take the annotations for dumping
   */
  protected String annotationSetName;

  /**
   * Whether or not to include the annotation features during export
   */
  protected boolean includeFeatures = false;

  /**
   * What suffix to use for the dump files. .gate by default, but can be
   * changed via the set method.
   */
  protected String suffixForDumpFiles = ".gate";

  /**
   * Whether or not to use the special suffix fo the dump files. True by
   * default.
   */
  protected boolean useSuffixForDumpFiles = true;

  protected java.net.URL outputFileUrl;

  private static final String DUMPING_PR_SET = "DumpingPRTempSet";

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException
  {
    return super.init();
  } // init()

  /**
  * Reinitialises the processing resource. After calling this method the
  * resource should be in the state it is after calling init.
  * If the resource depends on external resources (such as rules files) then
  * the resource will re-read those resources. If the data used to create
  * the resource has changed since the resource has been created then the
  * resource will change too after calling reInit().
  */
  public void reInit() throws ResourceInstantiationException
  {
    init();
  } // reInit()

  /** Run the resource. */
  public void execute() throws ExecutionException {

    if(document == null)
      throw new GateRuntimeException("No document to process!");

    AnnotationSet allAnnots;
    // get the annotations from document
    if ((annotationSetName == null)|| (annotationSetName.equals("")))
      allAnnots = document.getAnnotations();
    else
      allAnnots = document.getAnnotations(annotationSetName);

    //if none found, print warning and exit
    if ((allAnnots == null) || allAnnots.isEmpty()) {
      Out.prln("DumpingPR Warning: No annotations found for export. "
               + "Including only those from the Original markups set.");
      write2File(null);
      return;
    }

    //first transfer the annotation types from a list to a set
    //don't I just hate this!
    Set types2Export = new HashSet();
    for(int i=0; i<annotationTypes.size(); i++)
      types2Export.add(annotationTypes.get(i));

    //then get the annotations for export
    AnnotationSet annots2Export = allAnnots.get(types2Export);

    //check whether we want the annotations to be renamed before
    //export (that's what dumpTypes is for)
    if (dumpTypes != null && !dumpTypes.isEmpty()) {
      HashMap renameMap = new HashMap();
      for(int i=0; i<dumpTypes.size() && i<annotationTypes.size(); i++) {
        //check if we have a corresponding annotationType and if yes,
        //then add to the hash map for renaming
        renameMap.put(annotationTypes.get(i), dumpTypes.get(i));
      }//for
      //if we have to rename annotations, then do so
      if(!renameMap.isEmpty() && annots2Export != null)
        annots2Export = renameAnnotations(annots2Export, renameMap);
    }//if

    write2File(annots2Export);
    document.removeAnnotationSet(this.DUMPING_PR_SET);

  } // execute()

  protected void write2File(AnnotationSet exportSet) {
    File outputFile;
    String source = (String) document.getFeatures().get("gate.SourceURL");
    try {
      URL sourceURL = new URL(source);
      StringBuffer tempBuff = new StringBuffer(sourceURL.getFile());
      //now append the special suffix if we want to use it
      if (useSuffixForDumpFiles)
        tempBuff.append(this.suffixForDumpFiles);
      String outputPath = tempBuff.toString();
      if (DEBUG)
        Out.prln(outputPath);
      outputFile = new File(outputPath);
    } catch (java.net.MalformedURLException ex) {
      if (outputFileUrl != null)
        outputFile = new File(outputFileUrl.getFile());
      else
        throw new GateRuntimeException("Cannot export GATE annotations because"
                     + "document does not have a valid source URL.");
    }

    try {
      // Prepare to write into the xmlFile using UTF-8 encoding
      OutputStreamWriter writer = new OutputStreamWriter(
                            new FileOutputStream(outputFile),"UTF-8");

      // Write (test the toXml() method)
      // This Action is added only when a gate.Document is created.
      // So, is for sure that the resource is a gate.Document
      writer.write(document.toXml(exportSet, includeFeatures));
      writer.flush();
      writer.close();
    } catch (IOException ex) {
      throw new GateRuntimeException("Dumping PR: Error writing document "
                                     + document.getName() + ": "
                                     + ex.getMessage());
    }


  }//write2File

  protected AnnotationSet renameAnnotations(AnnotationSet annots2Export,
                                   HashMap renameMap){
    Iterator iter = annots2Export.iterator();
    AnnotationSet as = document.getAnnotations(DUMPING_PR_SET);
    if (!as.isEmpty())
      as.clear();
    while(iter.hasNext()) {
      Annotation annot = (Annotation) iter.next();
      //first check whether this type needs to be renamed
      //if not, continue
      if (!renameMap.containsKey(annot.getType()))
        renameMap.put(annot.getType(), annot.getType());
      try{
        as.add(annot.getId(),
            annot.getStartNode().getOffset(),
            annot.getEndNode().getOffset(),
            (String) renameMap.get(annot.getType()),
            annot.getFeatures());
      } catch (InvalidOffsetException ex) {
        throw new GateRuntimeException("DumpingPR: " + ex.getMessage());
      }
    }//while
    return as;
  }//renameAnnotations


  /**get the name of the annotation set*/
  public String getAnnotationSetName() {
    return annotationSetName;
  }//getAnnotationSetName

  /** set the annotation set name*/
  public void setAnnotationSetName(String newAnnotationSetName) {
    annotationSetName = newAnnotationSetName;
  }//setAnnotationSetName

  public List getAnnotationTypes() {
    return this.annotationTypes;
  }

  public void setAnnotationTypes(List newTypes) {
    annotationTypes = newTypes;
  }

  public List getDumpTypes() {
    return this.dumpTypes;
  }

  public void setDumpTypes(List newTypes) {
    dumpTypes = newTypes;
  }

  public URL getOutputFileUrl() {
    return this.outputFileUrl;
  }

  public void setOutputFileUrl(URL file) {
    outputFileUrl = file;
  }

  public void setIncludeFeatures(Boolean inclFeatures) {
    if (inclFeatures != null)
      includeFeatures = inclFeatures.booleanValue();
  }

  public Boolean getIncludeFeatures() {
    return new Boolean(includeFeatures);
  }

  public String getSuffixForDumpFiles() {
    return suffixForDumpFiles;
  }

  public void setSuffixForDumpFiles(String newSuffix) {
    this.suffixForDumpFiles = newSuffix;
  }

  public Boolean getUseSuffixForDumpFiles() {
    return new Boolean(this.useSuffixForDumpFiles);
  }

  public void setUseSuffixForDumpFiles(Boolean useOrNot) {
    if (useOrNot != null)
      this.useSuffixForDumpFiles = useOrNot.booleanValue();
  }

} // class AnnotationSetTransfer
