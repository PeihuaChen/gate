/*
 * AbstractGazetteer.java
 *
 * Copyright (c) 2002, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * borislav popov 02/2002
 *
 */
package gate.creole.gazetteer;

import gate.*;
import gate.creole.*;
import gate.util.*;

import java.util.*;

/**AbstractGazetteer
 * This class implements the common-for-all methods of the Gazetteer interface*/
public abstract class AbstractGazetteer
  extends gate.creole.AbstractLanguageAnalyser implements Gazetteer {

  /** the set of gazetteer listeners */
  protected Set listeners = new HashSet();

  /** Used to store the annotation set currently being used for the newly
   * generated annotations*/
  protected String annotationSetName;

  /** A map of the features */
  protected FeatureMap features  = null;

  /** the encoding of the gazetteer */
  protected String encoding = "UTF-8";

  /**
   * The value of this property is the URL that will be used for reading the
   * lists dtaht define this Gazetteer
   */
  protected java.net.URL listsURL;

  /**
   * Should this gazetteer be case sensitive. The default value is true.
   */
  protected Boolean caseSensitive = new Boolean(true);

  /** the linear definition of the gazetteer */
  protected LinearDefinition definition;

  /** reference to mapping definiton info
   *  allows filling of Lookup.ontologyClass according to a list*/
  protected MappingDefinition mappingDefinition;

  /**
   * Sets the AnnotationSet that will be used at the next run for the newly
   * produced annotations.
   */
  public void setAnnotationSetName(String newAnnotationSetName) {
    annotationSetName = newAnnotationSetName;
  }

  /**
   * Gets the AnnotationSet that will be used at the next run for the newly
   * produced annotations.
   */
  public String getAnnotationSetName() {
    return annotationSetName;
  }

  public void setEncoding(String newEncoding) {
    encoding = newEncoding;
  }

  public String getEncoding() {
    return encoding;
  }

  public java.net.URL getListsURL() {
    return listsURL;
  }

  public void setListsURL(java.net.URL newListsURL) {
    listsURL = newListsURL;
  }

  public void setCaseSensitive(Boolean newCaseSensitive) {
    caseSensitive = newCaseSensitive;
  }

  public Boolean getCaseSensitive() {
    return caseSensitive;
  }

  public void setMappingDefinition(MappingDefinition mapping) {
    mappingDefinition = mapping;
  }

  public MappingDefinition getMappingDefinition(){
    return mappingDefinition;
  }

  /**Gets the linear definition of this gazetteer. there is no parallel
   * set method because the definition is loaded through the listsUrl
   * on init().
   * @return the linear definition of the gazetteer */
  public LinearDefinition getLinearDefinition() {
    return definition;
  }

  /**     */
  public FeatureMap getFeatures(){
    return features;
  } // getFeatures

  /**     */
  public void setFeatures(FeatureMap features){
    this.features = features;
  } // setFeatures

  public void reInit() throws ResourceInstantiationException {
    super.reInit();
    fireGazetteerEvent(new GazetteerEvent(this,GazetteerEvent.REINIT));
  }//reInit()

  /**
   * fires a Gazetteer Event
   * @param ge Gazetteer Event to be fired
   */
  public void fireGazetteerEvent(GazetteerEvent ge) {
    Iterator li = listeners.iterator();
    while ( li.hasNext()) {
      GazetteerListener gl = (GazetteerListener) li.next();
      gl.processGazetteerEvent(ge);
    }
  }

  /**
   * Registers a Gazetteer Listener
   * @param gl Gazetteer Listener to be registered
   */
  public void addGazetteerListener(GazetteerListener gl){
    if ( null!=gl )
      listeners.add(gl);
  }
}//class AbstractGazetteer