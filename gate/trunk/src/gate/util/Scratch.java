/*
	Scratch.java

	Hamish Cunningham, 22/03/00

	$Id$
*/


package gate.util;

import java.util.*;
import java.awt.datatransfer.*;

import gate.*;
import gate.jape.*;
import org.w3c.www.mime.*;
/*
import java.io.IOException;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.w3c.tidy.Tidy;
*/

import java.io.*;
// xml DOM import

import javax.xml.parsers.*;
import org.w3c.dom.*;


/**
  * A scratch pad for experimenting.
  */
public class Scratch
{

  //*
  public static void main(String args[]) {
   // Hamish scratch
  /*
    FlavorMap sysFlavors = SystemFlavorMap.getDefaultFlavorMap();
    System.out.println(sysFlavors);

    Map sysFlavorsMap = sysFlavors.getNativesForFlavors(null);
    Iterator iter = sysFlavorsMap.entrySet().iterator();
    while(iter.hasNext()) {
      Object flavor = iter.next();
      System.out.println(flavor);
    }

    sysFlavorsMap = sysFlavors.getFlavorsForNatives(null);
    iter = sysFlavorsMap.entrySet().iterator();
    while(iter.hasNext()) {
      Object flavor = iter.next();
      System.out.println(flavor);
    }
    System.exit(0);
   */

   // Cristian scratch
   // XML DOM
  File xmlFile = null;
  org.w3c.dom.Document dom = null;
  try{
    // load the xml resource
    xmlFile = Files.writeTempFile(Files.getResourceAsStream("creole/creole.xml"));
  } catch (Exception e){
    e.printStackTrace (System.err);
  }
  DocumentBuilder domParser = null;
  try{
    // Get a parser factory.
    DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
	  // Set up the factory to create the appropriate type of parser
    // non validating one
    domBuilderFactory.setValidating(false);
    // a non namesapace aware one
    domBuilderFactory.setNamespaceAware(false);
    // create it
    domParser = domBuilderFactory.newDocumentBuilder();
    // we have the DOM parser and we will use it to parse the xmlFile and
    // construct the DOM model

    dom = domParser.parse(xmlFile);
    xmlFile.delete ();
  } catch (ParserConfigurationException e){
    e.printStackTrace(System.err);
  } catch (org.xml.sax.SAXException e){
    // this exception is raised because DOM uses a SAX parser in order to load
    // its structure
    e.printStackTrace(System.err);
  } catch (IOException e){
     e.printStackTrace(System.err);
  }

  // now we have the dom and we have to query it in order to access our data
  // to get help on working with DOM : http://java.sun.com/xml/docs/api/index.html
  /*
  NodeList nodeList = dom.getElementsByTagName("resource");
  for (int i = 0; i < nodeList.getLength(); i++){
    org.w3c.dom.Node node = nodeList.item(i);
    System.out.println("ELEMENT: " + node.getNodeName());
    // take its attributes
    System.out.print("ATTRIBUTES: ");
    NamedNodeMap namedNodeMap = node.getAttributes();
    for (int attIdx = 0 ; attIdx < namedNodeMap.getLength(); attIdx ++){
      org.w3c.dom.Node attNode = namedNodeMap.item(attIdx);
      System.out.print(attNode.getNodeName() + "=\"" + attNode.getNodeValue() + "\"");
    }
    System.out.println();
    // deal with its other children like another Elements or TEXT nodes
    NodeList elemNodeList = node.getChildNodes();
    for (int j = 0; j < elemNodeList.getLength(); j++){
      org.w3c.dom.Node elemNode = elemNodeList.item(j);
      if (elemNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
        // do something with the element contained into a resource element
      if (elemNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE )
        System.out.println("TEXT: " + elemNode.getNodeValue() );
    }

    System.out.println("---------------------------------------------------");
    */
  org.w3c.dom.Document newDom = domParser.newDocument();
  Element root = newDom.createElement(dom.getDocumentElement().getTagName());
  DocumentFragment df = newDom.createDocumentFragment();

  NodeList nodeList = dom.getDocumentElement().getChildNodes();
  for (int i = 0; i < nodeList.getLength(); i++){
    org.w3c.dom.Node node = nodeList.item(i);

    //ShowAllDetails(node);
    //System.out.println("-----------------------------------------------------");
    df.appendChild(node);

  }

  NodeList nodeList1 = newDom.getDocumentElement().getChildNodes();
  for (int i = 0; i < nodeList1.getLength(); i++){
    org.w3c.dom.Node node = nodeList.item(i);
    ShowAllDetails(node);
    System.out.println("-----------------------------------------------------");
  }

  /*

        Test16 t1 = new Test16("url", "outXMlFile", "errorFile", true);
        Test16 t2 = new Test16(args[3], args[4], args[5], false);
        Thread th1 = new Thread(t1);
        Thread th2 = new Thread(t2);

        th1.start();
        th2.start();

  */
  /*
   Map map = new HashMap();

   ExtendedMimeType mime = new ExtendedMimeType("text","xml");
   map.put(mime,"XML handler");
   map.put(new ExtendedMimeType("text","html"),"HTML handler");

   System.out.println(map.get(new ExtendedMimeType("text","xml")));
  */
  } // main

  public static void ShowAllDetails(org.w3c.dom.Node node){
    if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
      System.out.println("ELEMENT: " + node.getNodeName());
          // take its attributes
      System.out.print("ATTRIBUTES: ");
      NamedNodeMap namedNodeMap = node.getAttributes();
      for (int attIdx = 0 ; attIdx < namedNodeMap.getLength(); attIdx ++){
        org.w3c.dom.Node attNode = namedNodeMap.item(attIdx);
        System.out.print(attNode.getNodeName() + "=\"" + attNode.getNodeValue() + "\"");
      }
      System.out.println();
      NodeList elemNodeList = node.getChildNodes();
      for (int j = 0; j < elemNodeList.getLength(); j++)
        ShowAllDetails(elemNodeList.item(j));
    }
    if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE )
        System.out.println("TEXT: " + node.getNodeValue() );
  }


  //public int i;

} // class Scratch

/*
class ExtendedMimeType extends MimeType{
  public ExtendedMimeType(String type, String subtype){super(type,subtype);}

  public boolean equals(ExtendedMimeType obj){
    if (this.toString().equals(obj.toString()))
          return true;
    return false;
  }

  public int hashCode(){
    System.out.println(this.toString () + " HASH code = " + this.toString ().hashCode());
    return this.toString ().hashCode();
  }
}
*/

/*
public class Test16 implements Runnable {

    private String url;
    private String outFileName;
    private String errOutFileName;
    private boolean xmlOut;

    public Test16(String url, String outFileName,
                  String errOutFileName, boolean xmlOut)
    {
        this.url = url;
        this.outFileName = outFileName;
        this.errOutFileName = errOutFileName;
        this.xmlOut = xmlOut;
    }

    public void run()
    {
        URL u;
        BufferedInputStream in;
        FileOutputStream out;
        Tidy tidy = new Tidy();

        tidy.setXmlOut(xmlOut);
        try {
            tidy.setErrout(new PrintWriter(new FileWriter(errOutFileName), true));
            u = new URL(url);
            in = new BufferedInputStream(u.openStream());
            out = new FileOutputStream(outFileName);
            tidy.parse(in, out);
        }
        catch ( IOException e ) {
            System.out.println( this.toString() + e.toString() );
        }
    }
}
*/

