/*
 *  ConllDocumentFormat.java
 *
 *  Copyright (c) 1995-2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  $Id$
 */

package gate.corpora;

import java.util.*;
import gate.*;
import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleResource;
import gate.util.DocumentFormatException;
import gate.util.InvalidOffsetException;


/** Document format for handling CoNLL/IOB documents:
  * He PRP B-NP
  * accepted VBD B-VP
  * the DT B-NP
  * position NN I-NP
  * ...
  */
@CreoleResource(name = "GATE CoNLL Document Format", isPrivate = true,
    autoinstances = {@AutoInstance(hidden = true)})
public class ConllDocumentFormat extends TextualDocumentFormat {
  
  private static final long serialVersionUID = 5756433194230855515L;
  
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction */
  public ConllDocumentFormat() { super();}


  public void unpackMarkup(gate.Document doc) throws DocumentFormatException{
    if ( (doc == null) || (doc.getSourceUrl() == null && doc.getContent() == null) ) {
      throw new DocumentFormatException("GATE document is null or no content found. Nothing to parse!");
    }

    setNewLineProperty(doc);

    String[] lines = doc.getContent().toString().split("[\\n\\r]+");
    StringBuilder newContent = new StringBuilder();
    
    // Items of data to be turned into Original markups annotations
    List<Annotandum> annotanda = new ArrayList<Annotandum>();
    
    // Currently open tags: created by "B-FOO", extended by "I-FOO", closed
    // by "O" or end of sentence.
    Map<String, Annotandum> inProgress = new HashMap<String, Annotandum>();

    // TODO: close "FOO" annotation when we run out of "I-FOO" tags, rather
    // than waiting for the end of the sentence or the next "B-FOO".
    long oldEnd = 0L;
    long start = 0L;
    long end = 0L;

    for (String line : lines) {
      oldEnd = end;
      start = newContent.length();
      String[] items = line.split("\\s+");
      
      // blank line: stick a newline in the document content and close
      // any annotations in progress
      if (items.length == 0) {
        newContent.append("\n");
        end = newContent.length();
        finishAllTags(inProgress, annotanda, oldEnd);
      }
      
      else {
        String token = items[0];
        // TODO: handle spacing better  
        newContent.append(token);
        end = newContent.length();
        newContent.append(' ');
        

        for (int i=1 ; i < items.length ; i++) {
          // O means close all annotations in progress
          if (items[i].equals("O")) {
            finishAllTags(inProgress, annotanda, oldEnd);
          }
          
          // "B-FOO": start a new "FOO" annotation
          // after closing any "FOO" already in progress
          else if ( (items[i].length() > 2) &&
              items[i].startsWith("B-") ) {
            String type = items[i].substring(2);
            finishTag(type, inProgress, annotanda, oldEnd);
            inProgress.put(type, new Annotandum(type, start, end));
          }
          
          // "I-FOO": extend current "FOO" annotation
          else if ( (items[i].length() > 2) &&
              items[i].startsWith("I-") ) {
            String type = items[i].substring(2);
            
            if (inProgress.containsKey(type)) {
              // good I-FOO, so update the end offset
              inProgress.get(type).endOffset = end;
            }
            else {
              // bad data, containing I-FOO without a B-FOO, so treat as if B-FOO
              inProgress.put(type, new Annotandum(type, start, end));
            }
          }
          
          // "FOO": treat as single-token annotation (such as POS tag)
          else { 
            Annotandum tag = new Annotandum(items[1], start, end);
            annotanda.add(tag);
          }
        }
      }
      
    }
    // end of input: close any remaining annotations
    finishAllTags(inProgress, annotanda, end);

    
    // set new content & create Original markups annotations
    try {
      DocumentContent newContentImpl = new DocumentContentImpl(newContent.toString());
      doc.edit(0L, doc.getContent().size(), newContentImpl);
      long newSize = doc.getContent().size();
      
      AnnotationSet originalMarkups = doc.getAnnotations(GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME);
      for (Annotandum ann : annotanda) {
        if (DEBUG) {
          String string = Utils.stringFor(doc, ann.startOffset, 
              (ann.endOffset <= newSize) ? ann.endOffset : newSize);
          System.out.format("%d  %d  %s  %s\n", ann.startOffset, ann.endOffset, ann.type, string);
        }
        originalMarkups.add(ann.startOffset, ann.endOffset, ann.type, ann.features);
      }
    }
    catch (InvalidOffsetException e) {
      throw new DocumentFormatException(e);
    }
  }

  
  /* Close any open annotations (typically at the end of a sentence).  Leave the existing
   * end offset on an annotation that has one, but chop it off if it's still unspecified.
   */
  private void finishAllTags(Map<String, Annotandum> annsUnderway, List<Annotandum> annsFinished, long cutoff) {
    for (String key : annsUnderway.keySet()) {
      Annotandum ann = annsUnderway.get(key);
      if (ann.endOffset == null) {
        ann.endOffset = cutoff;
      }
      annsFinished.add(ann);
    }
    annsUnderway.clear();
  }
  
  
  /* If there is an annotation in progress of this type, close it;
   * if not, do nothing.    */
  private void finishTag(String type, Map<String, Annotandum> annsUnderway, List<Annotandum> annsFinished, long cutoff) {
    Annotandum ann = annsUnderway.remove(type);
    if (ann != null) {
      if (ann.endOffset == null) {
        ann.endOffset = cutoff;
      }
      annsFinished.add(ann);
    }
  }
  
  
  
  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    // Register ad hoc MIME-type
    MimeType mime = new MimeType("text","x-conll");
    // Register the class handler for this MIME-type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(), this);
    // Register the mime type with string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file suffixes for this mime type
    suffixes2mimeTypeMap.put("conll",mime);
    suffixes2mimeTypeMap.put("iob",mime);
    // Register magic numbers for this mime type
    //magic2mimeTypeMap.put("Subject:",mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  }
  
}


/** Wrapper around data to be turned into an "Original markups" annotation.
 */
class Annotandum {
  protected Long startOffset, endOffset;
  protected String type;
  protected FeatureMap features;
  
  protected Annotandum(String type, Long startOffset, Long endOffset) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.type = type;
    this.features = Factory.newFeatureMap();
  }
  
  protected Annotandum(String type, Long startOffset, Long endOffset, String tag) {
    this.features = Factory.newFeatureMap();
    this.features.put(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME, tag);
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.type = type;
  }

  protected Annotandum(String type, Long startOffset, Long endOffset, FeatureMap features) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.type = type;
    this.features = features;
  }

  
  
}