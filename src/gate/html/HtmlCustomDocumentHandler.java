/**
 *	HtmlCustomDocumentHandler.java
 *
 *	Cristian URSU,  12/June/2000
 *  $Id$
 */

package gate.html;

import gate.corpora.*;
import gate.util.*;
import gate.*;

import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.html.HTMLEditorKit.*;
import javax.swing.text.*;
import java.util.*;

/**
  * Implements the behaviour of the XML reader
  */
public class HtmlCustomDocumentHandler extends ParserCallback{

  // member data

  // the markupElementsMap
  private java.util.Map markupElementsMap = null;


  // the content of the HTML document, without any tag
  // for internal use
  private String tmpDocContent = null;

  // a stack used to remember elements
  private java.util.Stack stack = null;

  private java.io.PrintWriter out = null;

  /** a gate document */
  protected gate.Document doc = null;

  /** an annotation set */
  protected gate.AnnotationSet basicAS;


  /**
    * Constructor
    */
  public HtmlCustomDocumentHandler(gate.Document doc, java.util.Map markupElementsMap){
    // init stack, tmpDocContent, doc
    stack = new java.util.Stack();
    tmpDocContent = new String("");
    this.doc = doc ;
    this.markupElementsMap = markupElementsMap;
    try{
      out = new java.io.PrintWriter(new java.io.FileOutputStream("d:\\cursu\\Results.txt"));
    } catch (Exception e){
      System.out.println (e);
    }
  }

  /**
    * this method is called when the HTML parser encounts the beginning of a tag
    *
    */
  public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos){
    //if ( t == HTML.Tag.A )
    out.println("START TAG:" + t + " @:" + pos);
    if (0 != a.getAttributeCount ()){
       // System.out.println("HAS  attributes = " + a.getAttributeCount ());
        Enumeration enum = a.getAttributeNames ();
        while (enum.hasMoreElements ()){
          Object attribute = enum.nextElement ();
          out.print ( attribute + " = " + a.getAttribute (attribute) + ",");
          //out.print ( attribute + ",");
        }
        out.println();
      }
  }
} //CustomDocumentHandler


/*
 * The objects belonging to thsi class are used inside the stack
 */
class  MyCustomObject{

  // data fields
  private String elemName = null;
  private FeatureMap fm = null;
  private Long start = null;
  private Long end  = null;

  // constructor
  public MyCustomObject(String elemName, FeatureMap fm,
                         Long start, Long end){
    this.elemName = elemName;
    this.fm = fm;
    this.start = start;
    this.end = end;
  }

  // accesor
  public String getElemName(){
    return elemName;
  }
  public FeatureMap GetFM(){
    return fm;
  }
  public Long getStart(){
    return start;
  }
  public Long getEnd(){
    return end;
  }


  // mutator
  public void setElemName(String elemName){
    this.elemName = elemName;
  }
  public void setFM(FeatureMap fm){
    this.fm = fm;
  }
  public void setStart(Long start){
    this.start = start;
  }
  public void setEnd(Long end){
    this.end = end;
  }

}// MyCustomObject
