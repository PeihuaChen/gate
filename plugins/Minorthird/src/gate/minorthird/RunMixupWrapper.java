package gate.minorthird;

import java.net.URL;
import java.util.Iterator;
import java.io.*;
import java.lang.*;

import gate.*;
import gate.creole.*;
import gate.util.GateRuntimeException;

import edu.cmu.minorthird.util.gui.*;
import edu.cmu.minorthird.ui.*;
import edu.cmu.minorthird.text.*;
import edu.cmu.minorthird.text.mixup.*;
import edu.cmu.minorthird.util.*;


 /******************************************************************
 * This class is a wrapper for Minorthird RunMixup.                *
 * It passes GATE documents and a mixup program.                   *
 * Results of RunMixup are used to annotate the GATE documents. *
 *                                                                 *
 ******************************************************************/
 
 
 
 public class RunMixupWrapper extends AbstractLanguageAnalyser implements ProcessingResource {
 
 //Document to be processed, must be provided at Runtime.
 private gate.Document document;
 
 // File with contains the Mixup program, must be provided at Runtime.
 private URL mixupFile;
 
 //instance of RunMixup class
 private RunMixup runMixup;
 private MonotonicTextLabels labels=null;
 
 //default constructor
 public RunMixupWrapper(){}
 
 public Resource init() throws ResourceInstantiationException{
     return this;
 }
 
 
 public void execute() throws ExecutionException{
 
   //if no document provided to process
   if (document==null)
     throw new GateRuntimeException("No document to process!");
     
   // if no mixup program is specified
   if (mixupFile ==null)
     throw new GateRuntimeException("Cannot proceed unless a valid mixup program is specified.");

   //RunMixup cannot work on a single document. It works on a directory containing document(s).
   //the problem is solved by copying the source document into a temporary directory.
   //after running RunMixup, and delete the temporary directory and the file in it.
   
        
   URL sourceUrl = document.getSourceUrl();
   String sourceFile = sourceUrl.getFile();
   File source = new File(sourceFile);
  
   String currentdir = System.getProperty("user.dir");     
   File tmpdir = new File(currentdir+"/tmpmixupdir");
      
     
   //create a temporary directory
   boolean success = tmpdir.mkdir();
   if(!success){
        System.out.println("cannot create directory");
   }
   
   File tmpfile = new File(tmpdir.getAbsolutePath()+"/tmpfile");
   

   //copy the document to the temporary directory
   try{
       copySource(source, tmpfile);
   } catch (java.lang.Exception ex){
          System.out.println("cannot copy the source:" +ex);
   }
   
    //run the mixup file  
   String[] argu = {"-labels", tmpdir.getAbsolutePath(), "-mixup", mixupFile.getFile()};  
   runMixup = new RunMixup();
   runMixup.callMain(argu);
   
   //delete the temporary working directory and the document in it   
   tmpfile.delete();
   tmpdir.delete();
  
   //get the results
   labels=runMixup.annotatedLabels;
   
   //annotate source documents using the result of runmixup
   annotate();    
      
 }
 
 
 public void copySource(File in, File out) throws Exception{
    FileInputStream fis = new FileInputStream(in);
    FileOutputStream fos = new FileOutputStream(out);
    byte[] buf = new byte[1024];
    int i=0;
    while ((i=fis.read(buf))!=-1){
        fos.write(buf,0,i);
    }
    fis.close();
    fos.close();
 }
 
 
 //annotate the document using results returned by RunMixup
 public void annotate(){
   AnnotationSet aSet = document.getAnnotations();
   int startoffset, endoffset, start;
   String sContent = document.getContent().toString();
    
   for (Iterator i=labels.getTypes().iterator(); i.hasNext(); ){
       String type = (String) i.next();
       for (Span.Looper j=labels.instanceIterator(type); j.hasNext(); ){
           Span s=j.nextSpan();
           FeatureMap map=Factory.newFeatureMap();
           map.put("String", s.asString());         
           
           //RunMixup is run on the original document rather than the gate document
           //index of the span need to be adjusted
           //index in the original document
           start = s.documentSpanStartIndex();
           
           //index in the gate document
           startoffset = sContent.indexOf(s.asString(), start);          
           endoffset = startoffset + s.asString().length();
           
           try{
           aSet.add(new Long(startoffset), new Long(endoffset), type, map);       
           } catch (gate.util.InvalidOffsetException ioe) {
                  throw new GateRuntimeException("invalid offset of the annotation");
           }
       }
   }
   
   for (Iterator i = labels.getSpanProperties().iterator(); i.hasNext(); ) {
       String prop = (String) i.next();
       for (Span.Looper j=labels.getSpansWithProperty(prop); j.hasNext(); ){
           Span s=j.nextSpan();
           String val = labels.getProperty(s,prop);
           FeatureMap map = Factory.newFeatureMap();
           map.put ("String", s.asString());
           
           start = s.documentSpanStartIndex();
           
           startoffset = sContent.indexOf(s.asString(), start);
           endoffset = startoffset + s.asString().length();
           
           
           try{
           aSet.add(new Long(startoffset), new Long(endoffset), prop + " "+val, map);
           } catch (gate.util.InvalidOffsetException ioe){
                   throw new GateRuntimeException("invalid offset of the annotation");
           }
       }
   }
 }
 

 // getter and setter methods
 
  //set the source document
 public void setDocument(gate.Document document){
      this.document = document;
 }
 
 //return the source documents
 public gate.Document getDocument(){
      return this.document;
 }
 
 //set the mixup file
 public void setMixupFile(URL mixupFile){
       this.mixupFile = mixupFile; 
 }
 
 //return the mixup file
 public URL getMixupFile(){
     return this.mixupFile;
 }
 
 }