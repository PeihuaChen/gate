/*
 *	TestSgml.java
 *
 *	Cristian URSU,  8/May/2000
 *
 *	$Id$
 */

package gate.sgml;

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
public class TestSgml extends TestCase
{
  /** Construction */
  public TestSgml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp


  public static void main(String args[]){
    TestSgml app = new TestSgml("TestHtml");
    try{
      app.testSgmlLoading ();
    }catch (Exception e){
      e.printStackTrace (System.err);
    }
  }



  public void testSgmlLoading() throws Exception {
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
            new URL ("http://www.dcs.shef.ac.uk/~cursu/sgml/HDS")
           //new URL ("file:///d:/tmp/Hds.SGML")
    );
    // get the docFormat that deals with it.
    // the parameter MimeType doesn't affect right now the behaviour
    
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
        new MimeType("text","sgml")
    );

    /*
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
      doc.getSourceURL()
    );
    */
    //*
    if (docFormat != null){
      // set's the map
      docFormat.setMarkupElementsMap(markupElementsMap);
      // register a progress listener with it
      docFormat.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            System.out.println(text);
          }
          public void processFinished(){
          }
      });
      // timing the operation
      Date startTime = new Date();
        docFormat.unpackMarkup (doc,"DocumentContent");
      Date endTime = new Date();
      // get the size of the doc
      long  time1 = endTime.getTime () - startTime.getTime ();
      int docSize = doc.getContent().size().intValue();
      System.out.println("unpacMarkup() time for " + doc.getSourceURL () + "(" +
        docSize/1024 + "." + docSize % 1024 + " K)" + "=" + time1 / 1000 + "." +
        time1 % 1000 + " sec," + " processing rate = " + docSize/time1*1000/1024 +
        "." + (docSize/time1*1000)%1024 + " K/second");
    }
    else
      System.out.println("Couldn't figure out the type for this document");
    //*/
    // graphic visualisation

    if (docFormat != null){
     /*
      JFrame jFrame = new JFrame();
      JScrollPane tableViewScroll = new JScrollPane();
            //create the table
      SortedTable tableView = new SortedTable();
      tableView.setTableModel(new AnnotationSetTableModel(doc.getAnnotations(),doc));
      tableViewScroll.getViewport().add(tableView, null);
      jFrame.getContentPane().add(tableViewScroll);
      jFrame.setSize(800,600);
      jFrame.setVisible(true);
     */
     /*
        gate.jape.gui.JapeGUI japeGUI = new gate.jape.gui.JapeGUI();
        gate.Corpus corpus = gate.Transients.newCorpus("SGML Test");
        corpus.add(doc);
        japeGUI.setCorpus(corpus);
      */
    }
  }// testSgml

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
      String maxValue = null;
      int maxValueLength = 0;

      switch(column){
        case 0:
                 for (int i = 0 ; i < getRowCount(); i++){
                   String strValue = ((gate.Annotation) m_data.get(i)).getStartNode().getOffset().toString();
                   int length = strValue.length();
                   if (length > maxValueLength){
                      maxValueLength = length;
                      maxValue = strValue;
                   }
                 }
                 return maxValue;

        case 1:
                 for (int i = 0 ; i < getRowCount(); i++){
                   String strValue = ((gate.Annotation) m_data.get(i)).getEndNode().getOffset().toString();
                   int length = strValue.length();
                   if (length > maxValueLength){
                      maxValueLength = length;
                      maxValue = strValue;
                   }
                 }
                 return maxValue;

        case 2:
                 for (int i = 0 ; i < getRowCount(); i++){
                   String strValue = ((gate.Annotation) m_data.get(i)).getType();
                   int length = strValue.length();
                   if (length > maxValueLength){
                      maxValueLength = length;
                      maxValue = strValue;
                   }
                 }
                 return maxValue;

        case 3:
                 for (int i = 0 ; i < getRowCount(); i++){
                   String strValue = ((gate.Annotation) m_data.get(i)).getFeatures().toString();
                   int length = strValue.length();
                   if (length > maxValueLength){
                      maxValueLength = length;
                      maxValue = strValue;
                   }
                 }
                 return maxValue;
        case 4:
        //*
                 for (int i = 0 ; i < getRowCount(); i++){
                   String strValue = currentDoc.getContent().toString().substring(
                                     ((gate.Annotation) m_data.get(i)).getStartNode().getOffset().intValue(),
                                     ((gate.Annotation) m_data.get(i)).getEndNode().getOffset().intValue());
                   int length = strValue.length();
                   if (length > maxValueLength){
                      maxValueLength = length;
                      maxValue = strValue;
                   }
                 }
                 return maxValue;
          //*/
     }
     return null;
    }// getMaxValue()

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
    return new TestSuite(TestSgml.class);
  } // suite

} // class TestXml
