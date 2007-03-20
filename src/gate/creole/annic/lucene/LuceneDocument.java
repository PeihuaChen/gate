/*
 *  LuceneDocument.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: LuceneDocument.html,v 1.0 2007/03/19 16:22:01 niraj Exp $
 */
package gate.creole.annic.lucene;

import java.io.*;

import gate.creole.annic.Constants;
import gate.creole.annic.apache.lucene.document.Document;
import gate.creole.annic.apache.lucene.document.Field;
import java.util.ArrayList;
import gate.AnnotationSet;
import gate.util.Err;
import gate.util.OffsetComparator;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;

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
  public Document[] createDocument(String corpusPersistenceID,
          gate.Document gateDoc, String documentID, String annotSet,
          ArrayList featuresToExclude, String indexLocation,
          String baseTokenAnnotationType, String indexUnitAnnotationType) {

    // we need to generate the Token Stream here, and send it to the
    // GateLuceneReader
    AnnotationSet aSet = (annotSet == null || annotSet.trim().length() == 0)
            ? gateDoc.getAnnotations()
            : gateDoc.getAnnotations(annotSet);

    // check if these document has Tokens
    // otherwise throw an error
    if(aSet.get(baseTokenAnnotationType) == null) {
      Err.println("\nIgnoring the document : " + gateDoc.getName()
              + " since the document does not have annotations of type "
              + baseTokenAnnotationType);
      return null;
    }

    ArrayList[] tokenStreams = getTokens(gateDoc, aSet, featuresToExclude,
            baseTokenAnnotationType, indexUnitAnnotationType);

    if(tokenStreams == null) return null;

    Document[] toReturn = new Document[tokenStreams.length];
    for(int i = 0; i < tokenStreams.length; i++) {
      // make a new, empty document
      Document doc = new Document();

      // and then create the document
      LuceneReader reader = new LuceneReader(gateDoc, tokenStreams[i]);
      doc.add(Field.Keyword(Constants.DOCUMENT_ID, documentID + "-" + i));
      if(corpusPersistenceID != null)
        doc.add(Field.Keyword(Constants.CORPUS_ID, corpusPersistenceID));
      doc.add(Field.Text("contents", reader));
      // here we store token stream on the file system
      try {
        writeOnDisk(tokenStreams[i], documentID + "-" + i, indexLocation);
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
    return toReturn;
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
  private ArrayList[] getTokens(gate.Document document, AnnotationSet inputAs,
          ArrayList featuresToExclude, String baseTokenAnnotationType,
          String indexUnitAnnotationType) {

    HashSet<OffsetGroup> unitOffsetsSet = new HashSet<OffsetGroup>();
    AnnotationSet unitSet = inputAs.get(indexUnitAnnotationType);
    if(unitSet == null) {
      // the index Unit Annotation Type is not specified
      // therefore we consider the entire document as a single unit
      OffsetGroup group = new OffsetGroup();
      group.startOffset = new Long(0);
      group.endOffset = document.getContent().size();
      unitOffsetsSet.add(group); 
    }
    else {
      Iterator<Annotation> iter = unitSet.iterator();
      while(iter.hasNext()) {
        Annotation annotation = iter.next();
        OffsetGroup group = new OffsetGroup();
        group.startOffset = annotation.getStartNode().getOffset();
        group.endOffset = annotation.getEndNode().getOffset();
        unitOffsetsSet.add(group);
      }
    }

    ArrayList toReturn[] = new ArrayList[unitOffsetsSet.size()];
    Iterator<OffsetGroup> iter = unitOffsetsSet.iterator();
    int counter = 0;
    while(iter.hasNext()) {
      OffsetGroup group = iter.next();
      ArrayList<Token> newTokens = new ArrayList<Token>();
      ArrayList<Annotation> tokens = new ArrayList<Annotation>(inputAs
              .getContained(group.startOffset, group.endOffset));
      if(tokens == null) return null;

      Comparator offsetComparator = new OffsetComparator();
      Collections.sort(tokens, offsetComparator);

      int position = -1;
      for(int i = 0; i < tokens.size(); i++) {
        byte inc = 1;
        Annotation annot = tokens.get(i);
        String type = annot.getType();
        if(featuresToExclude.contains(type)) continue;

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
          if(annot.getStartNode().getOffset().longValue() == ((Annotation)tokens
                  .get(i - 1)).getStartNode().getOffset().longValue()) {
            token1.setPositionIncrement(0);
            inc = 0;
          }
        }

        position += inc;
        token1.setPosition(position);
        newTokens.add(token1);

        if((!type.equals(baseTokenAnnotationType))
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
          if(featuresToExclude.contains(type + "." + type1)) continue;

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
