/*
 *  Adjective.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 16/May/2002
 *
 *  $Id$
 */

package gate.wordnet;

import java.util.*;

import gate.*;
import gate.event.*;


/** Represents WordNet adj.
 */
public interface Adjective extends WordSense {

  public static final int ADJ_POS_ATTRIBUTIVE = 10001;

  public static final int ADJ_POS_IMMEDIATE_POSTNOMINAL  = 10002;

  public static final int ADJ_POS_PREDICATIVE  = 10003;

  public static final int ADJ_POS_NONE = 10004;

  public int getAdjectivePosition();

}

