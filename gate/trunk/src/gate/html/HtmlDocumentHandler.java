/**
 *	HtmlDocumentHandler.java
 *
 *	Cristian URSU,  12/June/2000
 *  $Id$
 */

package gate.html;

import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.html.HTMLEditorKit.*;
import javax.swing.text.*;
import java.util.*;

import gate.corpora.*;
import gate.util.*;
import gate.*;
import gate.gui.*;


/**
  * Implements the behaviour of the HTML reader (kind of SAX)
  */
public class HtmlDocumentHandler extends ParserCallback{

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

  // listeners for status report
  protected List myStatusListeners = new LinkedList();

  // the size of the document
  private int documentSize = 0;

  /**
    * Constructor
    */
  public HtmlDocumentHandler(gate.Document doc, java.util.Map markupElementsMap){
    // init stack, tmpDocContent, doc
    stack = new java.util.Stack();
    tmpDocContent = new String("");
    this.doc = doc ;
    this.markupElementsMap = markupElementsMap;
    basicAS = doc.getAnnotations ();
    documentSize = doc.getContent().size().intValue();
    if (documentSize == 0) documentSize = 1;
  }

  /**
    * this method is called when the HTML parser encounts the beginning of a tag
    * that means that the tag is paired by an end tag
    */
  public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos){
    // inform the progress listener about that
    fireStatusChangedEvent("Processing:" + t);
    // construct a feature map from the attributes list
    FeatureMap fm = new SimpleFeatureMapImpl();
    // take all the attributes an put them into the feature map

    //out.println("START TAG:" + t + " @:" + pos);
    if (0 != a.getAttributeCount ()){
       // System.out.println("HAS  attributes = " + a.getAttributeCount ());
        Enumeration enum = a.getAttributeNames ();
        while (enum.hasMoreElements ()){
          Object attribute = enum.nextElement ();
          //out.print ( attribute + " = " + a.getAttribute (attribute) + ",");
          fm.put ( attribute.toString(), (a.getAttribute (attribute)).toString());
        }
        //out.println();
    }
    // create the start index of the annotation
    Long startIndex = new Long(tmpDocContent.length ());
    // initialy the start index is equal with the End index
    MyCustomObject obj = new MyCustomObject(t.toString(),fm,startIndex, startIndex);
    // put it into the stack
    stack.push (obj);

       //if ( t == HTML.Tag.A )
  } //handleStartTag

  /**
    * this method is called when the HTML parser encounts the end of a tag
    * that means that the tag is paired by a beginning tag
    */
  public void handleEndTag(HTML.Tag t, int pos){
 // System.out.println(t);
    // obj is for internal use
    MyCustomObject obj = null;
    // if the stack is not empty we check to see if the object from the top of the
    // stack is a simple tag.
    // if is a simple tag, then we create  annotations (one with the simple tag )
    // and with the pair of the this End tag
    // every simple tag from the stack is one that contains text.
    if (!stack.isEmpty ())
      obj = (MyCustomObject) stack.pop ();
      try{
        if (markupElementsMap == null)
          basicAS.add(obj.getStart(),obj.getEnd(),obj.getElemName(),obj.getFM());
        else {
          String annotationType = (String) markupElementsMap.get(obj.getElemName());
          if (annotationType != null)
                basicAS.add(obj.getStart(),obj.getEnd(),annotationType,obj.getFM());
        }
      } catch (InvalidOffsetException e){
            e.printStackTrace (System.err);
      }
    if (stack.isEmpty ()){
      //out.println (tmpDocContent);
      //out.flush ();
      doc.setContent (new DocumentContentImpl(tmpDocContent));
    }
  }//handleEndTag

  /**
    * this method is called when the HTML parser encounts only the beginning of a tag
    */
  public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos){
     // inform the progress listener about that
    fireStatusChangedEvent("Processing:" + t);
    // construct a feature map from the attributes list
    // these are empty elements
    FeatureMap fm = new SimpleFeatureMapImpl();
    // take all the attributes an put them into the feature map

    //out.println("START TAG:" + t + " @:" + pos);
    if (0 != a.getAttributeCount ()){
       // System.out.println("HAS  attributes = " + a.getAttributeCount ());
        Enumeration enum = a.getAttributeNames ();
        while (enum.hasMoreElements ()){
          Object attribute = enum.nextElement ();
          //out.print ( attribute + " = " + a.getAttribute (attribute) + ",");
          fm.put ( attribute.toString(), (a.getAttribute (attribute)).toString());
        }
        //out.println();
    }
    // create the start index of the annotation
    Long startIndex = new Long(tmpDocContent.length ());
    try{
      if (markupElementsMap == null)
        basicAS.add(startIndex,startIndex,t.toString(),fm);
      else {
        String annotationType = (String) markupElementsMap.get(t.toString());
        if (annotationType != null)
          basicAS.add(startIndex, startIndex,annotationType, fm);
      }
    } catch (InvalidOffsetException e){
      e.printStackTrace (System.err);
    }
    if (HTML.Tag.BR == t)
      tmpDocContent += "\n";
  }//handleSimpleTag

  /**
    * this method is called when the HTML parser encounts text (PCDATA)
    */
  public void handleText(char[] text, int pos){
    String content = new String(text);
    MyCustomObject obj = null;
    Long end = new Long(tmpDocContent.length() + content.length());

    Iterator iterator = stack.iterator ();
    while (iterator.hasNext()){
      obj = (MyCustomObject) iterator.next();
      if (null != obj){
        obj.setEnd (end);
      }
    }
    // update the document content
    tmpDocContent += content;
  }

  /**
    * this method is called when the HTML parser encounts an error
    * it depends on the programmer if he wants to deal with that error
    */
  public void handleError(String errorMsg, int pos){
    //System.out.println ("ERROR CALLED : " + errorMsg);
  }

  /**
    * this method is called once, when the HTML parser reaches the end of its input
    * streamin order to notify the parserCallback that there is nothing more to
    * parse.
    */
  public void flush() throws BadLocationException{
    //System.out.println("Flush called!!!!!!!!!!!");
  }
  /*
  public void flush() throws BadLocationException{
    System.out.println("Flush called.");
    doc.setContent (new DocumentContentImpl(tmpDocContent));
    out.println ("DOC content:");
    out.println ("===========================================================");
    out.println (tmpDocContent);
    out.flush ();
    out.close();
  }
  */
  /**
    * this method is called when the HTML parser encounts a comment
    */
  public void handleComment(char[] text, int pos){
  //  String s = new String(text);
  //  System.out.println (s);
  }

  /**
    * here we check if the HTML tag is a empty one
    * we have to distingusih between P and IMG for example
    */
  private boolean isEmpty(HTML.Tag t){
    //  return false;
    //System.out.println (t);
    if (HTML.Tag.A.equals (t)) return true;
    if (HTML.Tag.BR.equals(t)) return true;
    if (HTML.Tag.IMG.equals(t)) return true;
    return false;
 }

  //StatusReporter Implementation
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }
  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }
  protected void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }

} //CustomDocumentHandler


/*
 * The objects belonging to this class are used inside the stack
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
  public FeatureMap getFM(){
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
