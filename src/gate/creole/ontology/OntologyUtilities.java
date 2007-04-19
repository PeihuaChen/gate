/*
 *  OntologyUtilities.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OntologyUtilities.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology;

import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class provides various Utility methods that can be used to
 * perform some generic options. For more information see javadoc of
 * each individual static method.
 * 
 * @author niraj
 * 
 */
public class OntologyUtilities {

  /**
   * Checks the availability of an existing instance of the Ontology
   * with the given URL in the GATE's CreoleRegister. If found, returns
   * the first available one (doesn't guranttee in which order). If not
   * found, attempts to create one using OWLIM implementation with
   * RDF/XML as ontology type and if successful returns the newly
   * created instance of the ontology.
   * 
   * @param url
   * @return
   * @throws ResourceInstantiationException
   */
  public static Ontology getOntology(URL url)
          throws ResourceInstantiationException {
    java.util.List<Resource> loadedOntologies = null;
    Ontology ontology = null;
    try {
      loadedOntologies = Gate.getCreoleRegister().getAllInstances(
              Ontology.class.getName());
    }
    catch(GateException ge) {
      throw new ResourceInstantiationException("Cannot list loaded ontologies",
              ge);
    }

    Iterator<Resource> ontIter = loadedOntologies.iterator();
    while(ontology == null && ontIter.hasNext()) {
      Ontology anOntology = (Ontology)ontIter.next();
      if(anOntology.getURL().equals(url)) {
        ontology = anOntology;
        break;
      }
    }

    try {
      // if not found, load it
      if(ontology == null) {
        // hardcoded to use OWL as the ontology type
        FeatureMap params = Factory.newFeatureMap();
        params.put("persistLocation", File.createTempFile("abc", "abc")
                .getParentFile().toURL());
        params.put("rdfXmlURL", url);
        ontology = (Ontology)Factory.createResource(
                "gate.creole.ontology.owlim.OWLIMOntologyLR", params);
      }
    }
    catch(Exception e) {
      throw new ResourceInstantiationException(
              "Cannot create a new instance of ontology", e);
    }
    return ontology;
  }

  /**
   * Given a URI, this methord returns the name part
   * 
   * @param uri
   * @return
   */
  public static String getResourceName(String uri) {
    int index = uri.lastIndexOf('#');
    if(index < 0) {
      index = uri.lastIndexOf('/');
      if(index < 0) return uri;
      if(index + 2 > uri.length()) return uri;
    }
    return uri.substring(index + 1, uri.length());
  }

  /**
   * Map containing uri and respective instance of datatypes
   */
  private static HashMap<String, DataType> datatypeMap = new HashMap<String, DataType>();
  static {
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#boolean", DataType
            .getBooleanDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#byte", DataType
            .getByteDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#date", DataType
            .getDateDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#decimal", DataType
            .getDecimalDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#double", DataType
            .getDoubleDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#duration", DataType
            .getDurationDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#float", DataType
            .getFloatDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#int", DataType
            .getIntDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#integer", DataType
            .getIntegerDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#long", DataType
            .getLongDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#negativeInteger",
            DataType.getNegativeIntegerDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger",
            DataType.getNonNegativeIntegerDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger",
            DataType.getNonPositiveIntegerDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#positiveInteger",
            DataType.getPositiveIntegerDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#short", DataType
            .getShortDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#string", DataType
            .getStringDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#time", DataType
            .getTimeDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#unsignedByte", DataType
            .getUnsignedByteDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#unsignedInt", DataType
            .getUnsignedIntDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#unsignedLong", DataType
            .getUnsignedLongDataType());
    datatypeMap.put("http://www.w3.org/2001/XMLSchema#unsignedShort", DataType
            .getUnsignedShortDataType());
  }

  /**
   * Gets the respective datatype for the given datatype URI. If the URI
   * is invalid, the method returns null.
   * 
   * @param datatypeURI
   * @return
   */
  public static DataType getDataType(String datatypeURI) {
    return datatypeMap.get(datatypeURI);
  }

  /**
   * This method by using the default name space and the provided ontology resource name, creates a new instance of
   * URI.  If isAnonymousResource is set to true, an  anonymous URI only using the resource name is created.
   * @param ontology
   * @param aResourceName
   * @param isAnonymousResource
   * @return an instance of URI
   */
  public static URI createURI(Ontology ontology, String aResourceName, boolean isAnonymousResource) {
     if(isAnonymousResource) {
       return new URI(aResourceName, true);
     }
     
     String uri = ontology.getDefaultNameSpace() + aResourceName;
     return new URI(uri, false);
  }
}
