/*
 *  HtmlLinkExtractor.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  16/Nov/2001
 *
 *  $Id$
 */

package gate.util;

import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.html.HTMLEditorKit.*;
import javax.swing.text.*;
import java.util.*;
import java.io.*;

/** 
 * This class extracts links from HTML files.
 * Implements the behaviour of the HTML reader.
 * Methods of an object of this class are called by the HTML parser when
 * events will appear.
 */
public class HtmlLinksExtractor extends ParserCallback {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The tag currently being processed */
  private HTML.Tag currentTag = null;

  /** This method is called when the HTML parser encounts the beginning
    * of a tag that means that the tag is paired by an end tag and it's
    * not an empty one.
    */
  public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
    currentTag = t;

    if (HTML.Tag.A == t){
      Out.prln("<"+t);
      // Construct a feature map from the attributes list
      Map fm = new HashMap();
      // Take all the attributes an put them into the feature map
      if (0 != a.getAttributeCount()){
        Enumeration enum = a.getAttributeNames();
        while (enum.hasMoreElements()){
          Object attribute = enum.nextElement();
          fm.put(attribute.toString(),(a.getAttribute(attribute)).toString());
          Out.prln(" "+ attribute.toString() + "=\"" +
                                    a.getAttribute(attribute).toString()+"\"");

        }// while
        Out.prln(">");
    }// if
  }// End if
  }//handleStartTag

   /** This method is called when the HTML parser encounts the end of a tag
     * that means that the tag is paired by a beginning tag
     */
  public void handleEndTag(HTML.Tag t, int pos){
    currentTag = null;
    if (HTML.Tag.A == t)
      Out.prln("</"+t+">\n");
  }//handleEndTag

  /** This method is called when the HTML parser encounts an empty tag
    */
  public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos){
    if (HTML.Tag.A == t){
      Out.prln("<"+t);
      // construct a feature map from the attributes list
      // these are empty elements
      Map fm = new HashMap();
      // take all the attributes an put them into the feature map
      if (0 != a.getAttributeCount ()){
          Enumeration enum = a.getAttributeNames();
          while (enum.hasMoreElements ()){
            Object attribute = enum.nextElement ();
            fm.put ( attribute.toString(),(a.getAttribute(attribute)).toString());
            Out.prln(" "+ attribute.toString() + "=\"" +
                                    a.getAttribute(attribute).toString()+"\"");
          }//while
      }
      Out.prln("/>\n");
    }// End if
  } // handleSimpleTag

  /** This method is called when the HTML parser encounts text (PCDATA)
    */
  public void handleText(char[] text, int pos){
    if(HTML.Tag.A == currentTag){
      //text of tag A
      String tagText = new String(text);
      Out.prln(tagText);
    }// End if
  }// end handleText();

  /**
    * This method is called when the HTML parser encounts an error
    * it depends on the programmer if he wants to deal with that error
    */
  public void handleError(String errorMsg, int pos) {
    //Out.println ("ERROR CALLED : " + errorMsg);
  }

  /** This method is called once, when the HTML parser reaches the end
    * of its input streamin order to notify the parserCallback that there
    * is nothing more to parse.
    */
  public void flush() throws BadLocationException{
  }// flush

  /** This method is called when the HTML parser encounts a comment
    */
  public void handleComment(char[] text, int pos) {
  }

  /**
   * Given a certain folder it lists recursively all the files contained
   * in that folder. It returns a list of strings representing the file
   * names
   */
  private static List listAllFiles(File aFile){
    java.util.List sgmlFileNames = new ArrayList();
    java.util.List foldersToExplore = new ArrayList();
    if (!aFile.isDirectory()){
      // add the file to the file list
      sgmlFileNames.add(aFile.getPath());
      return sgmlFileNames;
    }// End if
    listFilesRec(aFile,sgmlFileNames,foldersToExplore);
    return sgmlFileNames;
  } // listAllFiles();

  /** Helper method for listAllFiles */
  private static void listFilesRec(File aFile, java.util.List fileNames,
                                             java.util.List foldersToExplore){

    String[] fileList = aFile.list();
    for (int i=0; i< fileList.length; i++){
      File tmpFile = new File(aFile.getPath()+"\\"+fileList[i]);
      if (tmpFile.isDirectory())
        foldersToExplore.add(tmpFile);
      else
        fileNames.add(tmpFile.getPath());
    }// End for

    while(!foldersToExplore.isEmpty()){
      File folder = (File)foldersToExplore.get(0);
      foldersToExplore.remove(0);
      listFilesRec(folder,fileNames,foldersToExplore);
    }//End while

  } // listFilesRec();

  /** Extract links from all .html files below a directory */
  public static void main(String[] args){
    HTMLEditorKit.Parser  parser = new ParserDelegator();
    // create a new Htmldocument handler
    HtmlLinksExtractor htmlDocHandler = new HtmlLinksExtractor();

    if (args.length != 1){
      Out.prln(
        "Eg: java HtmlLinksExtractor g:\\tmp\\relative > results.txt"
      );
      return;
    }
    // Create a folder file File
    File htmlFolder = new File(args[0]);
    List htmlFileNames = listAllFiles(htmlFolder);
    while (!htmlFileNames.isEmpty()){
      try{
        String htmlFileName = (String) htmlFileNames.get(0);
        htmlFileNames.remove(0);

	// only process .html files
	if(
	  (! htmlFileName.toLowerCase().endsWith(".html") ) &&
	  (! htmlFileName.toLowerCase().endsWith(".htm") )
	)
	  continue;

        Reader reader = new FileReader(htmlFileName);
        // parse the HTML document
        parser.parse(reader, htmlDocHandler, true);
      } catch (IOException e){
        e.printStackTrace(System.out);
      }// End try
    }// End while
    System.err.println("done.");
  }// main

}//End class HtmlLinksExtractor



