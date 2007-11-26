/*
 *  LuceneDocument.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: LuceneDocument.html,v 1.0 2007/03/19 16:22:01 niraj Exp $
 */
package gate.creole.annic.lucene;

import java.io.*;

import gate.annotation.AnnotationSetImpl;
import gate.creole.annic.Constants;
import gate.creole.annic.apache.lucene.document.Document;
import gate.creole.annic.apache.lucene.document.Field;
import java.util.ArrayList;
import gate.AnnotationSet;
import gate.util.Err;
import gate.util.InvalidOffsetException;
import gate.util.OffsetComparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gate.Annotation;
import gate.FeatureMap;
import gate.creole.annic.apache.lucene.analysis.Token;
import java.util.Iterator;

/**
 * Given an instance of Gate Document, this class provides a method to
 * convert it into the format that lucene can understand and can store
 * in its indexes. This class also stores the tokenStream on the disk in
 * order to retrieve it at the time of searching
 * 
 * @author niraj
 * 
 */
public class LuceneDocument {

  /**
   * Separator used in serialized tokenstream files.
   */
  public final static String separator = "\0";

  /**
   * Given an instance of Gate Document, it converts it into the format
   * that lucene can understand and can store in its indexes. This
   * method also stores the tokenStream on the disk in order to retrieve
   * it at the time of searching
   * 
   * @param corpusPersistenceID
   * @param gateDoc
   * @param documentID
   * @param annotSet
   * @param featuresToExclude
   * @param indexLocation
   * @param baseTokenAnnotationType
   * @param indexUnitAnnotationType
   * @return
   */
  public List<Document> createDocuments(String corpusPersistenceID,
          gate.Document gateDoc, String documentID,
          ArrayList<String> annotSetsToInclude,
          ArrayList<String> annotSetsToExclude,
          ArrayList<String> featuresToInclude,
          ArrayList<String> featuresToExclude, String indexLocation,
          String baseTokenAnnotationType, String indexUnitAnnotationType) {

    ArrayList<Document> toReturnBack = new ArrayList<Document>();
    ArrayList<String> annotSetsToIndex = new ArrayList<String>();

    // if user has provided annotation sets to include, we don't bother about annotation sets to exclude
    if(annotSetsToInclude.size() > 0) {
      annotSetsToIndex = annotSetsToInclude;
    }
    else if(annotSetsToExclude.size() > 0) {
      // if there were no annotation sets to include, check if user has provided any annotation sets to exclude
      // if so, we need to index all annotation sets but provided in the annotationsetstoexclude list
      for(String setName : (Set<String>)gateDoc.getNamedAnnotationSets()
              .keySet()) {
        if(annotSetsToExclude.contains(setName)) continue;
        annotSetsToIndex.add(setName);
      }
      if(!annotSetsToExclude.contains(Constants.DEFAULT_ANNOTATION_SET_NAME)) {
        annotSetsToIndex.add(Constants.DEFAULT_ANNOTATION_SET_NAME);
      }
    } else {
      // if both annotation sets to include and annotation sets to exclude are empty
      // we need to index all annotation sets
      for(String setName : (Set<String>)gateDoc.getNamedAnnotationSets()
              .keySet()) {
        annotSetsToIndex.add(setName);
      }
      annotSetsToIndex.add(Constants.DEFAULT_ANNOTATION_SET_NAME);
    }
    
    
    // lets find out the annotation set that contains tokens in it
    AnnotationSet baseTokenAnnotationSet = null;
    
    int index = -1;
    if(baseTokenAnnotationType != null && baseTokenAnnotationType.trim().length() > 0)
      index = baseTokenAnnotationType.lastIndexOf('.');

    if(index >= 0) {
      
      String setName = baseTokenAnnotationType.substring(0, index);
      //System.out.println("BaseToken Annotation Type Set Name: "+setName);
      
      baseTokenAnnotationType = baseTokenAnnotationType.substring(index + 1,
              baseTokenAnnotationType.length());
      
      //System.out.println("BaseToken Annotation Type : "+baseTokenAnnotationType);
    
      // check if user has asked to take tokens from the default annotation set
      if(setName.equals(Constants.DEFAULT_ANNOTATION_SET_NAME))
        baseTokenAnnotationSet = gateDoc.getAnnotations().get(
                baseTokenAnnotationType);
      else baseTokenAnnotationSet = gateDoc.getAnnotations(setName).get(
              baseTokenAnnotationType);
      
      // here we check if the baseTokenAnnotationSet is null or its size is 0
      // if so, we ignore the document
      if(baseTokenAnnotationSet == null || baseTokenAnnotationSet.size() == 0) {
        System.err.println("\nIgnoring the document : " + gateDoc.getName()
                + " since the document does not have annotations of type "
                + baseTokenAnnotationType + " in "+ baseTokenAnnotationSet.getName());
        return null;
      }
    } else {
      // there is no annotation set name provided
      // we assume that we need to index all annotation sets except those which doesn't have token annotations in it
      // we don't need to do anything here but we know that the value of the
      // baseTokenAnnotationSet will be null
    }


    // lets find out the annotation set that contains indexUnitAnnotationType in it
    AnnotationSet indexUnitAnnotationSet = null;
    index = -1;
    if(indexUnitAnnotationType != null && indexUnitAnnotationType.trim().length() > 0)
      index = indexUnitAnnotationType.lastIndexOf('.');
    
    if(index >= 0) {
      String setName = indexUnitAnnotationType.substring(0, index);
      //System.out.println("Index Unit Type Set Name: "+setName);
      
      indexUnitAnnotationType = indexUnitAnnotationType.substring(index + 1,
              indexUnitAnnotationType.length());
      
      //System.out.println("Index Unit Type : "+indexUnitAnnotationType);
      
      if(setName.equals(Constants.DEFAULT_ANNOTATION_SET_NAME))
        indexUnitAnnotationSet = gateDoc.getAnnotations().get(
                indexUnitAnnotationType);
      else indexUnitAnnotationSet = gateDoc.getAnnotations(setName).get(
              indexUnitAnnotationType);
      
      // here we check if the indexUnitAnnotationSet is null or its size is 0
      // if so, we ignore the document
      if(indexUnitAnnotationSet == null || indexUnitAnnotationSet.size() == 0) {
        System.err.println("\nIgnoring the document : " + gateDoc.getName()
                + " since the document does not have annotations of type "
                + indexUnitAnnotationType + " in "+ indexUnitAnnotationSet.getName());
        return null;
      }
      
    } else {
      // there is no annotation set name provided
      // we assume that we need to index all annotation sets except those which doesn't have indexUnitAnnotationType annotions in it
      // we don't need to do anything here but we know that the value of the
      // indexUnitAnnotationSet will be null
    }

    
    int j=0;
    
    // we maintain an annotation set that contains all annotations from all the annotation sets to be indexed 
    // however it must not contain the baseTokens or indexUnitAnnotationType annotations
    AnnotationSet mergedSet = null;
    boolean mergedBaseTokenAnnotations = false;
    boolean mergedIndexUnitAnnotations = false;
    boolean useTempBaseTokenAnnotationSet = false;
    
    AnnotationSet tempBaseTokenAnnotationSet = baseTokenAnnotationSet;
    String tempBaseTokenAnnotationType = baseTokenAnnotationType;

    
    for(String annotSet : annotSetsToIndex) {
      
      // lets print the name of annotation set that we are indexing
      //System.out.println("\tIndexing :"+annotSet);
      
      AnnotationSet tempIndexUnitAnnotationSet = indexUnitAnnotationSet;

      // this is to avoid recreating tokens for the document
      // where user has asked to create tokens from the document
      if(!useTempBaseTokenAnnotationSet) {
        tempBaseTokenAnnotationSet = baseTokenAnnotationSet;
        tempBaseTokenAnnotationType = baseTokenAnnotationType;
      }
      
      // we need to generate the Token Stream here, and send it to the
      // GateLuceneReader
      AnnotationSet aSetToIndex = annotSet
              .equals(Constants.DEFAULT_ANNOTATION_SET_NAME) ? gateDoc
              .getAnnotations() : gateDoc.getAnnotations(annotSet);

      // if baseTokenAnnotationSet is null - it means that the user has asked us to
      // obtain tokens from each annotation set. so if the setToIndex doesn't have annotations of type baseTokenAnnotationType
      // we must throw an exception
      if(tempBaseTokenAnnotationSet == null) {
        // here we check if the baseTokenAnnotationType is null
        if(tempBaseTokenAnnotationType != null && !tempBaseTokenAnnotationType.equals(Constants.ANNIC_TOKEN)) {
          tempBaseTokenAnnotationSet = aSetToIndex.get(tempBaseTokenAnnotationType); 
          if(tempBaseTokenAnnotationSet == null || tempBaseTokenAnnotationSet.size() == 0) {
//            System.err.println("\nIgnoring the annotation set : " + aSetToIndex.getName()
//                  + " since it does not have annotations of type "
//                  + baseTokenAnnotationType);
            continue;
          } 
        } else {
          // we are asked to create tokens ourselves
          tempBaseTokenAnnotationSet = new AnnotationSetImpl(gateDoc);
          if(createTokens(gateDoc, tempBaseTokenAnnotationSet)) {
            tempBaseTokenAnnotationType = Constants.ANNIC_TOKEN;
            useTempBaseTokenAnnotationSet = true;
          } else {
            System.err.println("\nIgnoring the document as tokens could not be created for the document : " + gateDoc.getName());
              return null;
          }
        }
      } 
      
      // if indexUnitAnnotationSet is null - it means that the user has asked us to
      // obtain indexUnitAnnotations from each annotation set. so if the setToIndex doesn't have annotations of type indexUnitAnnotationType
      // we must throw an exception
      if(tempIndexUnitAnnotationSet == null) {
        if(indexUnitAnnotationType != null && indexUnitAnnotationType.trim().length() > 0) {
          tempIndexUnitAnnotationSet = aSetToIndex.get(indexUnitAnnotationType); 
          if(tempIndexUnitAnnotationSet == null || tempIndexUnitAnnotationSet.size() == 0) {
//            System.err.println("\nIgnoring the annotation set : " + aSetToIndex.getName()
//                  + " since it does not have annotations of type "
//                  + indexUnitAnnotationType);
            continue;
          } 
        }
      } 
      
      // tempBaseTokenAnnotationSet is not null
      ArrayList<Token>[] tokenStreams = getTokens(gateDoc, aSetToIndex,
              featuresToInclude, featuresToExclude, tempBaseTokenAnnotationType,
              tempBaseTokenAnnotationSet, indexUnitAnnotationType,
              tempIndexUnitAnnotationSet);

      // if there was some problem inside obtaining tokens
      // tokenStream is set to null
      if(tokenStreams == null) return null;
      
      if(mergedSet == null)
        mergedSet = new AnnotationSetImpl(gateDoc);
      
      // we need to merge all annotations but the baseTokenAnnotationType
      for(String aType : aSetToIndex.getAllTypes()) {

        if(aType.equals(tempBaseTokenAnnotationType)) {
          // first thing we need to check is if baseTokenAnnotationSet is not null
          if(tempBaseTokenAnnotationSet != null) {
            continue;
          }
          
          // baseTokenAnnotationSet is null
          // lets see if we've already added token annotations
          if(mergedBaseTokenAnnotations) {
            continue;
          } else {
            mergedBaseTokenAnnotations = true;
          }
        }

        if(aType.equals(indexUnitAnnotationType)) {
          // first thing we need to check is if indexUnitAnnotationSet is not null
          if(indexUnitAnnotationSet != null) {
            continue;
          }
          
          // indexUnitAnnotationSet is null
          // lets see if we've already added setnence annotations
          if(mergedIndexUnitAnnotations) {
            continue;
          } else {
            mergedIndexUnitAnnotations = true;
          }
        }
        
        mergedSet.addAll(aSetToIndex.get(aType));
      }
      
      Document[] toReturn = new Document[tokenStreams.length];

      for(int i = 0; i < tokenStreams.length; i++,j++) {
        // make a new, empty document
        Document doc = new Document();

        // and then create the document
        LuceneReader reader = new LuceneReader(gateDoc, tokenStreams[i]);
        doc.add(Field.Keyword(Constants.DOCUMENT_ID, documentID));
        doc.add(Field.Keyword(Constants.DOCUMENT_ID_FOR_SERIALIZED_FILE, documentID + "-" + j));
        if(corpusPersistenceID != null)
          doc.add(Field.Keyword(Constants.CORPUS_ID, corpusPersistenceID));
        doc.add(Field.Keyword(Constants.ANNOTATION_SET_ID, annotSet));

        doc.add(Field.Text("contents", reader));
        // here we store token stream on the file system
        try {
          writeOnDisk(tokenStreams[i], documentID + "-" + j, indexLocation);
        }
        catch(Exception e) {
          Err.println("\nIgnoring the document : " + gateDoc.getName()
                  + " since its token stream cannot be written on the disk");
          Err.println("Reason: " + e.getMessage());
          return null;
        }

        // return the document
        toReturn[i] = doc;
      }

      toReturnBack.addAll(Arrays.asList(toReturn));
    }

    
    // one again do an index with everything merged all together
    if(mergedSet != null) {

      // lets print the name of annotation set that we are indexing
//      System.out.println("\tIndexing :"+Constants.COMBINED_SET);
      
      ArrayList<Token>[] tokenStreams = getTokens(gateDoc, mergedSet,
              featuresToInclude, featuresToExclude, tempBaseTokenAnnotationType,
              tempBaseTokenAnnotationSet, indexUnitAnnotationType,
              indexUnitAnnotationSet);
      
      if(tokenStreams == null) return null;

      Document[] toReturn = new Document[tokenStreams.length];
      
      for(int i = 0; i < tokenStreams.length; i++,j++) {
        // make a new, empty document
        Document doc = new Document();

        // and then create the document
        LuceneReader reader = new LuceneReader(gateDoc, tokenStreams[i]);
        doc.add(Field.Keyword(Constants.DOCUMENT_ID, documentID));
        doc.add(Field.Keyword(Constants.DOCUMENT_ID_FOR_SERIALIZED_FILE, documentID + "-" + j));
        if(corpusPersistenceID != null)
          doc.add(Field.Keyword(Constants.CORPUS_ID, corpusPersistenceID));
        doc.add(Field.Keyword(Constants.ANNOTATION_SET_ID, Constants.COMBINED_SET));
        
        doc.add(Field.Text("contents", reader));
        // here we store token stream on the file system
        try {
          writeOnDisk(tokenStreams[i], documentID + "-" + j, indexLocation);
        }
        catch(Exception e) {
          Err.println("\nIgnoring the document : " + gateDoc.getName()
                  + " since its token stream cannot be written on the disk");
          Err.println("Reason: " + e.getMessage());
          return null;
        }

        // return the document
        toReturn[i] = doc;
      }

      toReturnBack.addAll(Arrays.asList(toReturn));
    }
    
    return toReturnBack;
  }

  
  private boolean createTokens(gate.Document gateDocument, AnnotationSet set) {
    String gateContent = gateDocument.getContent().toString();
    int start = -1;
    for(int i=0;i<gateContent.length();i++) {
      char c = gateContent.charAt(i);
      if(Character.isWhitespace(c)) {
        if(start != -1) {
          FeatureMap features = gate.Factory.newFeatureMap();
          features.put("string", gateContent.substring(start, i));
          try {
            set.add(new Long(start), new Long(i), Constants.ANNIC_TOKEN, features);
          } catch(InvalidOffsetException ioe) {
            ioe.printStackTrace();
            return false;
          }
          start = i+1;
        } 
      } else {
        if(start == -1)
          start = i;
      }
    }
    if(start < gateContent.length()) {
      FeatureMap features = gate.Factory.newFeatureMap();
      features.put("string", gateContent.substring(start, gateContent.length()));
      try {
        set.add(new Long(start), new Long(gateContent.length()), Constants.ANNIC_TOKEN, features);
      } catch(InvalidOffsetException ioe) {
        ioe.printStackTrace();
        return false;
      }
    }
    return true;
  }
  
  /**
   * Some file names are not compatible to the underlying file system.
   * This method replaces all those incompatible characters with '_'.
   * 
   * @param name
   * @return
   */
  private String getCompatibleName(String name) {
    return name.replaceAll("[\\/:\\*\\?\"<>|]", "_");
  }

  /**
   * This method, given a tokenstream and file name, writes the
   * tokenstream on the provided location.
   * 
   * @param tokenStream
   * @param fileName
   * @param location
   * @throws Exception
   */
  private void writeOnDisk(ArrayList tokenStream, String fileName,
          String location) throws Exception {

    // before we write it on a disk, we need to change its name to underlying file system name
    fileName = getCompatibleName(fileName);

    if(location.startsWith("file:/"))
      location = location.substring(6, location.length());

    if(location.charAt(1) != ':') location = "/" + location;
    File locationFile = new File(location);
    File folder = new File(locationFile, Constants.SERIALIZED_FOLDER_NAME);
    if(!folder.exists()) {
      boolean created = folder.mkdir();
    }

    if(!folder.exists()) {
      throw new IOException("Directory could not be created :"
              + folder.getAbsolutePath());
    }
    File outputFile = new File(folder, fileName + ".annic");
    ObjectOutput output = null;
    OutputStream file = new FileOutputStream(outputFile);
    OutputStream buffer = new BufferedOutputStream(file);
    output = new ObjectOutputStream(buffer);
    output.writeObject(tokenStream);
    if(output != null) {
      output.close();
    }
  }

  /**
   * Internal class used for storing the offsets of annotations.
   * 
   * @author niraj
   * 
   */
  private class OffsetGroup {
    Long startOffset;

    Long endOffset;
  }

  /**
   * This method given a GATE document and other required parameters,
   * for each annotation of type indexUnitAnnotationType creates a
   * separate list of baseTokens underlying in it.
   * 
   * @param document
   * @param inputAs
   * @param featuresToExclude
   * @param baseTokenAnnotationType
   * @param indexUnitAnnotationType
   * @return
   */
  private ArrayList<Token>[] getTokens(gate.Document document,
          AnnotationSet inputAs, ArrayList<String> featuresToInclude,
          ArrayList<String> featuresToExclude, String baseTokenAnnotationType,
          AnnotationSet baseTokenSet, String indexUnitAnnotationType,
          AnnotationSet indexUnitSet) {

    boolean excludeFeatures = false;
    boolean includeFeatures = false;
    
    // if include features are provided, we donot look at the exclude features
    if(!featuresToInclude.isEmpty()) {
      includeFeatures = true;
    } else if(!featuresToExclude.isEmpty()) {
      excludeFeatures = true;
    }

    HashSet<OffsetGroup> unitOffsetsSet = new HashSet<OffsetGroup>();
    if(indexUnitAnnotationType == null
            || indexUnitAnnotationType.trim().length() == 0
            || indexUnitSet == null || indexUnitSet.size() == 0) {
      // the index Unit Annotation Type is not specified
      // therefore we consider the entire document as a single unit
      OffsetGroup group = new OffsetGroup();
      group.startOffset = new Long(0);
      group.endOffset = document.getContent().size();
      unitOffsetsSet.add(group);
    }
    else {
      Iterator<Annotation> iter = indexUnitSet.iterator();
      while(iter.hasNext()) {
        Annotation annotation = iter.next();
        OffsetGroup group = new OffsetGroup();
        group.startOffset = annotation.getStartNode().getOffset();
        group.endOffset = annotation.getEndNode().getOffset();
        unitOffsetsSet.add(group);
      }
    }

    Set<String> allTypes = inputAs.getAllTypes();

    if(baseTokenSet != null && baseTokenSet.size() > 0) {
      allTypes.remove(baseTokenAnnotationType);
      //System.out.println("\t\tIndexing Type : Token = "+baseTokenSet.size());
    }
    
    if(indexUnitSet != null && indexUnitSet.size() > 0)
      allTypes.remove(indexUnitAnnotationType);
    
    AnnotationSet toUseSet = new AnnotationSetImpl(document);
    for(String type : allTypes) {
      //System.out.println("\t\tIndexing type :"+type+" size :"+inputAs.get(type).size());
      toUseSet.addAll(inputAs.get(type));
      
    }
    
    //System.out.println("\t\tSize:"+toUseSet.size());
    
    ArrayList<Token> toReturn[] = new ArrayList[unitOffsetsSet.size()];
    Iterator<OffsetGroup> iter = unitOffsetsSet.iterator();
    int counter = 0;
    while(iter.hasNext()) {
      OffsetGroup group = iter.next();
      ArrayList<Token> newTokens = new ArrayList<Token>();
      ArrayList<Annotation> tokens = new ArrayList<Annotation>(toUseSet
              .getContained(group.startOffset, group.endOffset));

      
      // add tokens from the baseTokenSet
      if(baseTokenSet != null && baseTokenSet.size() != 0) {
        tokens.addAll(baseTokenSet.getContained(group.startOffset,
                group.endOffset));
      }
      
      if(tokens == null || tokens.size() == 0) return null;

      Collections.sort(tokens, new OffsetComparator());

      int position = -1;
      for(int i = 0; i < tokens.size(); i++) {
        byte inc = 1;
        Annotation annot = tokens.get(i);
        String type = annot.getType();

        // if the feature is specified in featuresToExclude -exclude it
        if(excludeFeatures && featuresToExclude.contains(type)) continue;

        // if the feature is not sepcified in the include features -
        // exclude it
        if(includeFeatures && !featuresToInclude.contains(type)) continue;

        int startOffset = annot.getStartNode().getOffset().intValue();
        int endOffset = annot.getEndNode().getOffset().intValue();
        String text = document.getContent().toString().substring(startOffset,
                endOffset);
        if(text == null) {
          continue;
        }

        Token token1 = new Token(type, startOffset, endOffset, "*");

        // each token has four values
        // String, int, int, String
        // we add extra info of position
        if(i > 0) {
          if(annot.getStartNode().getOffset().longValue() == tokens.get(i - 1)
                  .getStartNode().getOffset().longValue()) {
            token1.setPositionIncrement(0);
            inc = 0;
          }
        }

        position += inc;
        token1.setPosition(position);
        newTokens.add(token1);

        if(!type.equals(baseTokenAnnotationType)
                || (annot.getFeatures().get("string") == null)) {
          // we need to create one string feature for this
          Token tk1 = new Token(text, startOffset, endOffset, type + ".string");
          tk1.setPositionIncrement(0);
          tk1.setPosition(position);
          newTokens.add(tk1);
        }

        // now find out the features and add them
        FeatureMap features = annot.getFeatures();
        Iterator fIter = features.keySet().iterator();
        while(fIter.hasNext()) {
          String type1 = (String)fIter.next();
          // if the feature is specified in featuresToExclude -exclude
          // it
          if(excludeFeatures && featuresToExclude.contains(type + "." + type1))
            continue;

          // if the feature is not sepcified in the include features -
          // exclude it
          if(includeFeatures && !featuresToInclude.contains(type + "." + type1))
            continue;

          Object tempText = features.get(type1);
          if(tempText == null) continue;

          String text1 = (String)tempText.toString();
          // we need to qualify the type names
          Token tempToken = new Token(text1, startOffset, endOffset, type + "."
                  + type1);
          tempToken.setPositionIncrement(0);
          tempToken.setPosition(position);
          newTokens.add(tempToken);
        }
      }
      toReturn[counter] = newTokens;
      counter++;
    }
    return toReturn;
  }
}
