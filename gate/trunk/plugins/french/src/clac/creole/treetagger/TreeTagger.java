/*
 *  TreeTagger.java
 *
 *
 *  Copyright (c) 2003 the CLaC group,
 *  http://www.cs.concordia.ca/CLAC/index.shtml
 *
 *  René Witte, 20.3.2003
 *
 *  $Id$
 *
 */

package clac.creole.treetagger;

import java.io.*;
import java.util.*;
import java.net.*;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.event.*;
import gate.gui.MainFrame;


/**
 * This class is the implementation of the resource TREETAGGER,
 * a wrapper for the language-independent POS tagger from
 * Universität Stuttgart, Germany
 */
public class TreeTagger extends AbstractLanguageAnalyser
  implements ProcessingResource {

  public static final String GATETEXTFILE = "/tmp/GATETOKENS";

  /** Feature name (slot) for a "category" */
  public static final String CATEGORY_FEATURE = "category";


  String taggerLibDir;

  /**
   * Get the TaggerLibDir value.
   * @return the TaggerLibDir value.
   */
  public String getTaggerLibDir() {
    return taggerLibDir;
  }

  /**
   * Set the TaggerLibDir value.
   * @param newTaggerLibDir The new TaggerLibDir value.
   */
  public void setTaggerLibDir(String newTaggerLibDir) {
    this.taggerLibDir = newTaggerLibDir;
  }


  URL treeTaggerBinary;

  /**
   * Get the TreeTaggerBinary value.
   * @return the TreeTaggerBinary value.
   */
 
  public URL getTreeTaggerBinary() {
    return treeTaggerBinary;
  }

  /**
   * Set the TreeTaggerBinary value.
   * @param newTreeTaggerBinary The new TreeTaggerBinary value.
   */
  public void setTreeTaggerBinary(URL newTreeTaggerBinary) {
    this.treeTaggerBinary = newTreeTaggerBinary;
  }

  String treeTaggerParFile;

  /**
   * Get the TreeTaggerParFile value.
   * @return the TreeTaggerParFile value.
   */
  public String getTreeTaggerParFile() {
    return treeTaggerParFile;
  }

  /**
   * Set the TreeTaggerParFile value.
   * @param newTreeTaggerParFile The new TreeTaggerParFile value.
   */
  public void setTreeTaggerParFile(String newTreeTaggerParFile) {
    this.treeTaggerParFile = newTreeTaggerParFile;
  }


  public void reInit()
    throws ResourceInstantiationException {}

  public boolean isInterrupted(){ return boolean0; }

  public void interrupt() {}


  protected boolean boolean0;

  private ArrayList saveGateTextTokens() {
    AnnotationSet    allSentences;

    // get sentences from document
    allSentences = getDocument().getAnnotations();

    // Get all "token" tokens
    List tokens = new ArrayList((Set)allSentences.get( TOKEN_ANNOTATION_TYPE ));

    // create an ArrayList to hold all tokens
    ArrayList allTokens = new ArrayList( tokens );

    // sort all tokens by start offset
    Collections.sort( allTokens, new gate.util.OffsetComparator() );

    // save token strings to file for TreeTagger
    ListIterator tokenIterator = allTokens.listIterator();
    try {
      String tokenString = null;
      File gateTextFile = new File( GATETEXTFILE );
      FileWriter fw = new FileWriter ( gateTextFile );
      PrintWriter pw = new PrintWriter( fw, true );
      while( tokenIterator.hasNext() ) {
	Annotation token = (Annotation) tokenIterator.next();
	tokenString = null;
	try {
	  tokenString = getDocument().getContent().getContent(
				 token.getStartNode().getOffset(),
				 token.getEndNode().getOffset()).toString();
	} catch (InvalidOffsetException ioe) {
	  throw new GateRuntimeException("Invalid offset of the annotation");
	}
	pw.println( tokenString );
      }
      fw.close();
    } catch (java.io.IOException except) {
      System.out.println( "File error: " + except );
    }

    return allTokens;
  }

  private void runTreeTagger(ArrayList allTokens, String cmdline) {
    AnnotationSet allSentences;
    Annotation token;
    FeatureMap tokenFm;
    String tokenString;

    allSentences = document.getAnnotations();

    ListIterator tokenIterator = allTokens.listIterator();

    //System.out.println("TreeTagger CMD=" + cmdline);
    // run TreeTagger and save output
    try {
      String line, word, tag, comment;
      Process p = Runtime.getRuntime().exec(cmdline);

                
      BufferedReader input =
	new BufferedReader
	(new InputStreamReader(p.getInputStream()));

      while ((line = input.readLine()) != null) {
	StringTokenizer st = new StringTokenizer(line);
	if( tokenIterator.hasNext() )
	  token = (Annotation) tokenIterator.next();
	else break;
	tokenFm = token.getFeatures();
	//System.out.println("Line='"+line+"'");
	while (st.hasMoreTokens()) {
	  tag = null; comment = null;
	  word = st.nextToken();
	  if( st.hasMoreTokens() )
	    tag = st.nextToken();
	  if( tag != null )
	    comment = st.nextToken();
	  tokenFm.put( "Comment", comment );
	  tokenFm.put( CATEGORY_FEATURE , tag );
	  try {
	    tokenString = getDocument().getContent().getContent(
				 token.getStartNode().getOffset(),
				 token.getEndNode().getOffset()).toString();
	   // if( !tokenString.equals( word ))
	      //System.out.println( "TREETAGGER ERROR: tokens out of sync!");
	    //System.out.println("Token=" + tokenString + ", Word="+word+", Tag="+tag+", Comment="+comment);
	  } catch (InvalidOffsetException ioe) {
	    throw new GateRuntimeException("Invalid offset of the annotation");
	  }
	}
      }
      input.close();

    }
    catch (Exception err) {
      System.out.println( "TreeTagger Exception: " + err );
    }

      
  }

  public void execute() throws ExecutionException {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.indexOf("linux") == -1)
       throw new GateRuntimeException ("The Tree Tagger cannot be run on any other operating systems except Linux.");
    
    if(document == null)
      throw new GateRuntimeException("No document to process!");

    ArrayList allTokens = saveGateTextTokens();

    //String treeTaggerCmd = CLaCMain.mapURL2LocalDir( treeTaggerBinary ) + " " + GATETEXTFILE;

    String treeTaggerCmd = treeTaggerBinary.getFile() + " " + GATETEXTFILE;

    runTreeTagger( allTokens, treeTaggerCmd );
  }
  
} // class TreeTagger
