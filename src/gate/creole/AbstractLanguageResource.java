/*
 *  AbstractLanguageResource.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Hamish Cunningham, 24/Oct/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;

import gate.*;
import gate.util.*;


/** A convenience implementation of LanguageResource with some default code.
  */
abstract public class AbstractLanguageResource
extends AbstractResource implements LanguageResource
{
  /** Get the data store that this LR lives in. Null for transient LRs. */
  public DataStore getDataStore() { return dataStore; }

  /** The data store this LR lives in. */
  protected DataStore dataStore;

} // class AbstractLanguageResource
