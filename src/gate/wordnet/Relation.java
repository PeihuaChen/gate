/*
 *  Relation.java
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


/** Represents WordNet relation.
 */
public interface Relation {

  public static final int REL_ANTONYM = 10001;
  public static final int REL_HYPERNYM = 10002;
  public static final int REL_HYPONYM = 10003;
  public static final int REL_MEMBER_HOLONYM = 10004;
  public static final int REL_SUBSTANCE_HOLONYM = 10005;
  public static final int REL_PART_HOLONYM = 10006;
  public static final int REL_MEMBER_MERONYM = 10007;
  public static final int REL_SUBSTANCE_MERONYM = 10008;
  public static final int REL_PART_MERONYM = 10009;
  public static final int REL_ATTRIBUTE = 10010;
  public static final int REL_ENTAILMENT = 10011;
  public static final int REL_CAUSE = 10012;
  public static final int REL_SEE_ALSO = 10013;
  public static final int REL_VERB_GROUP = 10014;
  public static final int REL_PARTICIPLE_OF_VERB = 10015;
  public static final int REL_SIMILAR_TO = 10016;
  public static final int REL_PERTAINYM = 10017;
  public static final int REL_DERIVED_FROM_ADJECTIVE = 10018;

  public int getType();

  public int getInverseType();

  public String getLabel();

  public String getSymbol();

  public boolean isApplicableTo(int pos);

}

