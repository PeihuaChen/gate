/*
 *  Minipar.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Niraj Aswani
 *
 *  $Id$
 */

package minipar;

import java.io.*;
import java.util.*;
import java.net.*;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.event.*;
import gate.gui.MainFrame;


/**
 * This class is the implementation of the resource Minipar
 */
public class Minipar extends AbstractLanguageAnalyser
  implements ProcessingResource {

  public static final String GATETEXTFILE = "GATESentences";
  private URL miniparDataDir;
  private URL miniparBinary;
  private String annotationTypeName;
  private String annotationSetName;
  private gate.Document document;

  /**
   * Get the MiniparDataDir value.
   */
  public URL getMiniparDataDir() {
    return miniparDataDir;
  }

  /**
   * Get the Document to process.
   */
  public gate.Document getDocument() {
    return this.document;
  }

  /**
   * Set the Document to process
   */
  public void setDocument(gate.Document document) {
    this.document = document;
  }

 /**
 * Get the AnnotationTypeName, new annotations are created with this name
 */
  public String getAnnotationTypeName() {
    return this.annotationTypeName;
  }

  /**
  * Set the AnnotationTypeName, new annotations are created with this name
  */
  public void setAnnotationTypeName(String aTypeName) {
    this.annotationTypeName = aTypeName;
  }

  /**
  * Get the AnnotationSetName, source of the annotations to be taken from and to work on
  */
  public String getAnnotationSetName() {
    return this.annotationSetName;
  }

  /**
  * Set the AnnotationSetName, source of the annotations to be taken from and to work on
  */
  public void setAnnotationSetName(String aSetName) {
    this.annotationSetName = aSetName;
  }


  /**
  * Set the MiniparDataDirectory.. This is the directory that Minipar uses to collect the data for
  * its internal processing. Default location is minipar_home/data
  */
  public void setMiniparDataDir(URL newMiniparDataDir) {
    this.miniparDataDir = newMiniparDataDir;
  }

  /**
   * This is the url of MiniparBinary
   * It should be somewhere located on the drive where the user has execution rights
   */
  public URL getMiniparBinary() {
    return miniparBinary;
  }

  /**
   * This is the url of MiniparBinary
   * It should be somewhere located on the drive where the user has execution rights
   */
  public void setMiniparBinary(URL newMiniparBinary) {
    this.miniparBinary = newMiniparBinary;
  }


  public Resource init() throws ResourceInstantiationException {
    // we need to check the operating system
    //And to detect the underlying Operating system
    String osName = System.getProperty("os.name").toLowerCase();
    System.out.println(osName);
    // Detecting Linux
    if(osName.toLowerCase().indexOf("linux") == -1) {
      throw new ResourceInstantiationException("This PR can only be instantiated on Linux Machine");
    }

    return super.init();
  }

  public void reInit()
    throws ResourceInstantiationException {}


/**
 * Minipar Binary file takes a file as an argument, which has one sentence written
 * on one line.  It takes one sentence at a time and parses them one by one.
 * @return The list containing annotations of type *Sentence*
 * @throws ExecutionException
 */
  private ArrayList saveGateSentences() throws ExecutionException {

    AnnotationSet allAnnotations;

    // get sentences from document
    allAnnotations = (annotationSetName == null || annotationSetName.equals("")) ?
        document.getAnnotations() :
        document.getAnnotations(annotationSetName);

    if(allAnnotations == null || allAnnotations.size() == 0) {
      throw new ExecutionException("Document doesn't have sentence annotations. please run tokenizer, sentence splitter and then Minipar");
    }

    // Get all "sentences"
    List sentences = new ArrayList((Set)allAnnotations.get(SENTENCE_ANNOTATION_TYPE));

    // create an ArrayList to hold all sentences
    ArrayList allSentences = new ArrayList( sentences );

    // sort all sentences by start offset
    Collections.sort( allSentences, new gate.util.OffsetComparator() );

    // save sentence strings to file for Minipar
    ListIterator sentenceIterator = allSentences.listIterator();
    try {
      String sentenceString = null;
      File gateTextFile = new File( GATETEXTFILE );
      FileWriter fw = new FileWriter ( gateTextFile );
      PrintWriter pw = new PrintWriter( fw, true );
      while( sentenceIterator.hasNext() ) {
        Annotation sentence = (Annotation) sentenceIterator.next();
        sentenceString = null;
        try {
          sentenceString = getDocument().getContent().getContent(
                                 sentence.getStartNode().getOffset(),
                                 sentence.getEndNode().getOffset()).toString();
        } catch (InvalidOffsetException ioe) {
          throw new GateRuntimeException("Invalid offset of the annotation");
        }
        pw.println( sentenceString );
      }
      fw.close();
    } catch (java.io.IOException except) {
      System.out.println( "File error: " + except );
    }

    return allSentences;
  }

  // run Minipar
  // cmdline consists of the file
  private void runMinipar(ArrayList allSentences) {

    // this should be the miniparBinary + "-p " + getMiniparDataDir + GATETEXTFILE
    File gateTextFile = new File( GATETEXTFILE );
    // each file url starts with file:
    // we need to take this out
    String binary = getMiniparBinary().toString();
    int indexL = binary.indexOf("file:");
    if(indexL != -1) {
      binary = binary.substring(indexL+5, binary.length());
    }

    String dataFile = getMiniparDataDir().toString();
    indexL = dataFile.indexOf("file:");
    if(indexL != -1) {
      dataFile = dataFile.substring(indexL+5, dataFile.length());
    }
    String cmdline = binary + " -p " + dataFile + " -file " + gateTextFile.getAbsolutePath();

    // run minipar and save output
    try {
      String line;
      Process p = Runtime.getRuntime().exec(cmdline);
      BufferedReader input =
        new BufferedReader
        (new InputStreamReader(p.getInputStream()));

      // this has ArrayList as its each element
      // this element consists of all annotations for that particular sentence
      ArrayList sentenceTokens = new ArrayList();

      // this will have an annotation for each line begining with a number
      ArrayList tokens = new ArrayList();

      outer:while ((line = input.readLine()) != null) {
        WordToken wt = new WordToken();
        // so here whatever we get in line
        // is of our interest only if it begins with any number
        // each line is deliminated with a tab sign
        String [] output = line.split("\t");
        for(int i=0;i<output.length;i++) {
          // we ignore case 2 and 3 and 6 and after.. because we don't want that information
          switch(i) {
            case 0:
              // this is a word number
              try {
                int number = Integer.parseInt(output[i].trim());
                // yes this is correct line
                // we need to check if the line number is 1
                // it may be the begining of new sentence
                if(number == 1 && tokens.size() > 0) {
                  // we need to add tokens to the sentenceTokens
                  sentenceTokens.add(tokens);
                  tokens = new ArrayList();
                }
              } catch(NumberFormatException infe) {
                // if we are here, there is something wrong with number
                // ignore this line and continue with next line
                continue outer;
              }
              break;
            case 1:
              // this is the actual word which
              wt.word = output[i];
              break;
            case 4:
              // this should be the number and if it is not
              // then we leave it and do not add any head
              try {
                int head = Integer.parseInt(output[i].trim());
                // yes this is the correct head number
                wt.headNumber = head;
              } catch(NumberFormatException nfe) {
                // if we are here, there is something wrong with number
                // ignore this and make headNumber -1 letter on to
                // remember that we don't want headnumber to be inserted as a
                // feature
                wt.headNumber = -1;
              }
              break;
            case 5:
              // this is the relation between head and the current node
              wt.relationWithHead = output[i];
              break;
            default:
              break;
          }
        }

        // here we have parsed the one line and thus now we should add it to the
        // tokens for letter use
        tokens.add(wt);
      }
      if(tokens.size() > 0) {
        sentenceTokens.add(tokens);
      }
      input.close();

      // ok so here we have all the information we need from the minipar in local variables
      // ok so first we would create annotation for each word Token

      AnnotationSet annotSet = (annotationSetName == null || annotationSetName.equals("")) ?
          document.getAnnotations() :
          document.getAnnotations(annotationSetName);

      // size of the sentenceTokens and the allSentences would be always same
      for(int i=0;i<sentenceTokens.size();i++) {
        tokens = (ArrayList) sentenceTokens.get(i);

        // we need this to generate the generate the offsets
        Annotation sentence = (Annotation) allSentences.get(i);
        int startOffset = sentence.getStartNode().getOffset().intValue();
        String sentenceString = document.getContent().getContent(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset()).toString();
        // this will hold the position from where it should start searching for the token text
        int index = 0;
        for(int j=0;j<tokens.size();j++) {
          // each item here is a separate word token
          WordToken wt = (WordToken) tokens.get(j);
          // ok so find out the offsets
          int stOffset = sentenceString.toLowerCase().indexOf(wt.word.toLowerCase(),index) + startOffset;
          int enOffset = stOffset + wt.word.length();
          Integer id = annotSet.add(new Long(stOffset), new Long(enOffset), annotationTypeName, Factory.newFeatureMap());
          wt.annotation = annotSet.get(id);
          index = enOffset - startOffset;
        }
      }

      // now we need to create the children nodes
      for(int i=0;i<sentenceTokens.size();i++) {
        tokens = (ArrayList) sentenceTokens.get(i);

        for(int j=0;j<tokens.size();j++) {
          WordToken wt = (WordToken) tokens.get(j);
          // read the head node
          // find out the respective word token for that head node
          // and add the current node as its child
          if(wt.headNumber > 0) {
            WordToken headToken = (WordToken) tokens.get(wt.headNumber - 1);
            headToken.children.add(wt.annotation.getId());
          }
        }
      }

      // and finally we need to add features to the annotations
      // now we need to create the children nodes
      for(int i=0;i<sentenceTokens.size();i++) {
        tokens = (ArrayList) sentenceTokens.get(i);

        for(int j=0;j<tokens.size();j++) {
          WordToken wt = (WordToken) tokens.get(j);
          FeatureMap map = wt.annotation.getFeatures();
          if(wt.headNumber > 0) {
            Integer head_id = ((WordToken) tokens.get(wt.headNumber - 1)).annotation.getId();
            map.put("word",wt.word);
            map.put("head_id",head_id);
            map.put("rel_with_head", wt.relationWithHead);
          }

          if(wt.children.size() > 0) {
            map.put("child_id", wt.children);
          }
        }
      }

      // and finally make the sentenceTokens and tokens to null
      tokens = null;
      sentenceTokens = null;
    }
    catch (Exception err) {
      err.printStackTrace();
    }
  }

  public void execute() throws ExecutionException {
    if(document == null)
      throw new GateRuntimeException("No document to process!");
    if(getMiniparBinary() == null)
      throw new GateRuntimeException("Please provide the URL for Minipar Binary");
    if(getMiniparDataDir() == null)
      throw new GateRuntimeException("Minipar requires the location of its data directory (By default it is %Minipar_Home%/data");

    ArrayList allSentences = saveGateSentences();
    runMinipar(allSentences);
  }

  /**
   * Sub class we use to store the annotation before defining its relation with other wordtoken
   */
  private class WordToken {
    String word;
    int headNumber;
    String relationWithHead;
    ArrayList children = new ArrayList();
    gate.Annotation annotation;
  }

}

