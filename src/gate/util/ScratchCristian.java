/*
	ScratchCristian.java

	Cristian URSU, 03/Jul/2000

	$Id$
*/



package gate.util;

import org.w3c.www.mime.*;

import java.io.*;
// xml DOM import
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;


/**
  * A scratch pad for experimenting.
  */
public class ScratchCristian
{

/*
  public org.w3c.dom.Document parseXml (String xmlURI){
    // XML DOM
    DocumentBuilder domParser = null;
    org.w3c.dom.Document dom = null;
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
      dom = domParser.parse(xmlURI);
    } catch (ParserConfigurationException e){
      e.printStackTrace(System.err);
    } catch (org.xml.sax.SAXException e){
      // this exception is raised because DOM uses a SAX parser in order to load
      // its structure
      e.printStackTrace(System.err);
    } catch (IOException e){
     e.printStackTrace(System.err);
    }
    return dom;
  } // parseXML

  public org.w3c.dom.Document getEmptyDom() {
    DocumentBuilder domParser = null;
    org.w3c.dom.Document dom = null;
    try{
      // Get a parser factory.
      DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
	    // Set up the factory to create the appropriate type of parser
      // non validating one
      domBuilderFactory.setValidating(false);
      // a non namesapace aware one
      domBuilderFactory.setNamespaceAware(false);
      // create a new DOM parser
      domParser = domBuilderFactory.newDocumentBuilder();
      //create an empty DOM
      dom =  domParser.newDocument();
    }catch (javax.xml.parsers.ParserConfigurationException e){
      e.printStackTrace(System.err);
    }
    return dom;
  }// getEmptyDom

  public static void main(String args[]) {
    ScratchCristian app = new ScratchCristian();
    // create a DOM from the following URL
    org.w3c.dom.Document dom = app.parseXml("http://www.dcs.shef.ac.uk/~cursu/xml/input/xces/xces.xml");
    // create an empty DOM
    org.w3c.dom.Document newDom = app.getEmptyDom();

    // create the root element inside the newDom based on the source root element
    Element root = newDom.createElement(dom.getDocumentElement().getTagName());

    // copy the entire DOM into the new one using the method importNodes()
    NodeList nodeList = dom.getDocumentElement().getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++){
      org.w3c.dom.Node node = nodeList.item(i);
      app.importNodes(node,root,newDom);
    }
    // verify if everything is OK.
    app.ShowAllDetails(root);
  } // main

  // imports the structure under sourceNode (together with sourceNode) under destNode of destDom
  public void importNodes (org.w3c.dom.Node sourceNode, org.w3c.dom.Node destNode, org.w3c.dom.Document destDom){
    // create a new element
    if (sourceNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
     Element e = destDom.createElement(sourceNode.getNodeName());
     // add all the attributes
      NamedNodeMap namedNodeMap = sourceNode.getAttributes();
      for (int attIdx = 0 ; attIdx < namedNodeMap.getLength(); attIdx ++){
        org.w3c.dom.Node attNode = namedNodeMap.item(attIdx);
        e.setAttribute(attNode.getNodeName(),attNode.getNodeValue());
      }
     // put the element into DOM
     destNode.appendChild(e);
     // for all the childNodes of sourceNode import them all
     NodeList nodeList =sourceNode.getChildNodes();
     for (int i = 0; i < nodeList.getLength(); i++)
        importNodes(nodeList.item(i), e, destDom);
    }
    if (sourceNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE){
      Text t = destDom.createTextNode(sourceNode.getNodeValue());
      destNode.appendChild(t);
    }
  }

  public  void ShowAllDetails(org.w3c.dom.Node node){
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
*/

// WORKING with LAX



  public static void main (String[] args){
  /*
    HandlerObject ho = new HandlerObject();
    TemplateLaxErrorHandler errHandler= new TemplateLaxErrorHandler();
    Lax lax = new Lax(ho,errHandler);
    File xmlFile = null;
     try{
      // load the xml resource
      xmlFile = Files.writeTempFile(Files.getResourceAsStream("creole/creole.xml"));
    } catch (Exception e){
      e.printStackTrace (System.err);
    }
    lax.parseXmlDocument(xmlFile);
   */
   MimeType type = null;
   try{
    type = new MimeType("text/xml");
   } catch (Exception e){
    e.printStackTrace(System.err);
   }
   System.out.println(type.getType() + ":" + type.getSubtype());
  }
} // class ScratchCristian


class HandlerObject {
  public HandlerObject(){
  }

  public void startresource(AttributeList alAttrs){
    String className = alAttrs.getValue("class");
    // do something with this class name;
    System.out.println("Attribue :" + className);
  }
  public void textOfresource(String txt){
    /// do something with it
  }
}

// modify the class name the way you want
class MyLaxErrorHandler extends LaxErrorHandler {
/**
 * TemplateLaxErrorHandler constructor comment.
 */
public MyLaxErrorHandler() {super();}
/**
 * error method comment.
 */
public void error(SAXParseException ex) throws SAXException{
  // do something with the error
	File fInput = new File (ex.getSystemId());
	System.err.println("e: " + fInput.getPath() + ": line " + ex.getLineNumber() + ": " + ex);
}
/**
 * fatalError method comment.
 */
public void fatalError(SAXParseException ex) throws SAXException{
  // do something with the fatalError
	File fInput = new File(ex.getSystemId());
	System.err.println("E: " + fInput.getName() + ": line " + ex.getLineNumber() + ": " + ex);
}
/**
 * warning method comment.
 */
public void warning(SAXParseException ex) throws SAXException {
  // do something with the warning.
	File fInput = new File(ex.getSystemId());
	System.err.println("w: " + fInput.getName() + ": line " + ex.getLineNumber() + ": " + ex);
}

}// TemplateLaxErrorHandler
