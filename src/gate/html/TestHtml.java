/*
 *	TestHtml.java
 *
 *	Cristian URSU,  8/May/2000
 *
 *	$Id$
 */

package gate.html;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;
import org.w3c.www.mime.*;
import gate.util.*;
import gate.gui.*;
import gate.*;
import javax.swing.*;

/** Test class for XML facilities
  *
  */
public class TestHtml extends TestCase
{
  /** Construction */
  public TestHtml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp


  public static void main(String args[]){
    TestHtml app = new TestHtml("TestHtml");
    try{
      app.testSomething ();
    }catch (Exception e){
      e.printStackTrace (System.err);
    }
  }


  /** A test */
  public void testSomething() throws Exception{
    assert(true);

    // create the markupElementsMap map
    Map markupElementsMap = null;
    /*
    markupElementsMap = new HashMap();
    // populate it
    markupElementsMap.put ("S","Sentence");
    markupElementsMap.put ("s","Sentence");
    markupElementsMap.put ("W","Word");
    markupElementsMap.put ("w","Word");
    markupElementsMap.put ("p","Paragraph");
    markupElementsMap.put ("h1","Header 1");
    markupElementsMap.put ("H1","Header 1");
    markupElementsMap.put ("A","link");
    markupElementsMap.put ("a","link");
    */
    // create a new gate document
    gate.Document doc = gate.Transients.newDocument(
             // new URL("http://www.funideas.com/visual_gallery.htm")
            //new URL ("http://www.dcs.shef.ac.uk/~hamish/GateIntro.html")
            new URL ("http://www.w3.org/TR/REC-xml")
            //new URL ("http://www.dcs.shef.ac.uk/~cursu")
            //new URL ("http://www.webhelp.com/home.html")
            //new URL ("http://big2.hotyellow98.com/sys/signup.cgi")
            //new URL ("http://www.epilot.com/SearchResults.asp?keyword=costume+baie&page=&source=&TokenID=82C7BE897D9643EDB3CB8A28E398A488")
    );
    // get the docFormat that deals with it.
    // the parameter MimeType doesn't affect right now the behaviour
    /*
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
        new MimeType("text","html")
    );
    */
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
      doc.getSourceURL()
    );
    //*
    if (docFormat != null){
      // set's the map
      docFormat.setMarkupElementsMap(markupElementsMap);
      /*
      // register a progress listener with it
      docFormat.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            System.out.println(text);
          }
          public void processFinished(){
          }
      });
      */
      // timing the operation
      Date startTime = new Date();
        docFormat.unpackMarkup (doc,"DocumentContent");
      Date endTime = new Date();
      long  time1 = endTime.getTime () - startTime.getTime ();
      int docSize = doc.getContent().size().intValue();
      System.out.println("unpacMarkup() time for " + doc.getSourceURL () + "(" +
        docSize/1024 + "." + docSize % 1024 + " K)" + "=" + time1 / 1000 + "." +
        time1 % 1000 + " sec," + " processing rate = " + docSize/time1*1000/1024 +
        "." + (docSize/time1*1000)%1024 + " K/second");
    }
    //*/

    // graphic visualisation
    //*
    if (docFormat != null){
        docFormat.unpackMarkup (doc);
        gate.jape.gui.JapeGUI japeGUI = new gate.jape.gui.JapeGUI();
        gate.Corpus corpus = gate.Transients.newCorpus("HTML Test");
        corpus.add(doc);
        japeGUI.setCorpus(corpus);
    }
    //*/
  } // testSomething()

  class AnnotationSetTableModel extends gate.gui.SortedTableModel{
    public gate.Document currentDoc = null;

    public AnnotationSetTableModel(AnnotationSet as, gate.Document doc){
      setData (as, new AnnotationSetComparator());
      currentDoc = doc;
    }

    public int getColumnCount(){
      return 5;
    }

    public String getColumnName(int column){
      switch(column){
        case 0:{
          return "Start" + addSortOrderString(0);
        }
        case 1:{
          return "End" + addSortOrderString(1);
        }
        case 2:{
          return "Type" + addSortOrderString(2);
        }
        case 3:{
          return "Features" + addSortOrderString(3);
        }
        case 4:{
          return "Text" + addSortOrderString(4);
        }
      }
      return null;
    }
    
    public Object getMaxValue(int column){
      Collection data = new TreeSet();
      switch(column){
        case 0:
                 for (int i = 0 ; i < getRowCount(); i++)
                    data.add( ((gate.Annotation) m_data.get(i)).getStartNode().getOffset());
                 return data.toArray()[data.size()];

        case 1:
                  for (int i = 0 ; i < getRowCount(); i++)
                    data.add( ((gate.Annotation) m_data.get(i)).getEndNode().getOffset());
                  return data.toArray()[data.size()];

        case 2:
                 return new String ("rrrrrrrrrrrrrrrrrrr");

        case 3:
                  return new String ("rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");

        case 4:
                 return new String ("rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");

     }
     return null;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return false;
    }

    public Object getValueAt(int row, int column){
      gate.Annotation currentAnn = (gate.Annotation) m_data.get(row);
      switch(column){
        case 0:{
          return currentAnn.getStartNode().getOffset();
        }
        case 1:{
          return currentAnn.getEndNode().getOffset();
        }
        case 2:{
          return currentAnn.getType();
        }
        case 3:{
          return currentAnn.getFeatures();
        }
        case 4:{
          return currentDoc.getContent().toString().substring(
              currentAnn.getStartNode().getOffset().intValue(),
              currentAnn.getEndNode().getOffset().intValue());
        }
      }
      return null;
    }

    class AnnotationSetComparator extends gate.gui.SortedTableComparator{

      public AnnotationSetComparator(){
      }
      public int compare(Object o1, Object o2){
        if ( !(o1 instanceof gate.Annotation) ||
             !(o2 instanceof gate.Annotation)) return 0;

        gate.Annotation a1 = (gate.Annotation) o1;
        gate.Annotation a2 = (gate.Annotation) o2;
        int result = 0;

        switch(this.getSortCol()){
          case 0: // Start
          {
            Long l1 = a1.getStartNode().getOffset();
            Long l2 = a2.getStartNode().getOffset();
            result = l1.compareTo(l2);
          }break;
          case 1: // End
          {
            Long l1 = a1.getEndNode().getOffset();
            Long l2 = a2.getEndNode().getOffset();
            result  = l1.compareTo(l2);
          }break;
          case 2: // Type
          {
            String s1 = a1.getType();
            String s2 = a2.getType();
            result = s1.compareTo(s2);
          }break;
          case 3: // Features
          {
            String fm1 = a1.getFeatures().toString();
            String fm2 = a2.getFeatures().toString();
            result = fm1.compareTo(fm2);
          }break;
          case 4: // Text
          {
            String text1 = currentDoc.getContent().toString().substring(
              a1.getStartNode().getOffset().intValue(),
              a1.getEndNode().getOffset().intValue());
            String text2 = currentDoc.getContent().toString().substring(
              a2.getStartNode().getOffset().intValue(),
              a2.getEndNode().getOffset().intValue());
            result = text1.compareTo(text2);
          }break;
        }// switch
        if (!this.getSortOrder()) result = -result;
        return result;
      }//compare
    }
  }




  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestHtml.class);
  } // suite

} // class TestXml
