/*
 *  AbstractResource.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 15/Oct/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;

import gate.*;
import gate.util.*;


/** A convenience implementation of Resource with some default code.
  */
abstract public class AbstractResource
extends AbstractFeatureBearer implements Resource, Serializable
{
  static final long serialVersionUID = -9196293927841163321L;

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    return this;
  } // init()

  /** Sets the name of this resource*/
  public void setName(String name){
    FeatureMap fm = getFeatures();
    if(fm == null){
      fm = Factory.newFeatureMap();
      setFeatures(fm);
    }
    Gate.setName(fm, name);
  }

  /** Returns the name of this resource*/
  public String getName(){
    FeatureMap fm = getFeatures();
    if(fm == null) return null;
    else return Gate.getName(fm);
  }

} // class AbstractResource
