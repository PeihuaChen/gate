/*
 * AbstractOntoGazetteer.java
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

import java.util.*;
/**AbstratOntoGazetteer*/
public abstract class AbstractOntoGazetteer
extends AbstractGazetteer implements OntoGazetteer {

  /** the url of the mapping definition */
  protected java.net.URL mappingURL;

  /** class name of the linear gazetteer to be called */
  protected String gazetteerName;

  /** class of the linear gazetteer */
  protected Gazetteer gaz;

  public void setGazetteerName(String name) {
    gazetteerName = name;
  }

  public String getGazetteerName() {
    return gazetteerName;
  }

  public void setMappingURL(java.net.URL url) {
    mappingURL = url;
  }

  public java.net.URL getMappingURL() {
    return mappingURL;
  }

  public Gazetteer getGazetteer(){
    return gaz;
  }

  public void setGazetteer(Gazetteer gaze) {
    gaz = gaze;
  }
} // class AbstractOntoGazetteer