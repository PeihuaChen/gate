
/*
 *  Word.java
 *
 *  Copyright (c) 1998-2003, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 28/January/2003
 *
 *  $Id$
 */
package gate.lexicon;

import java.util.*;

public interface Word {

  /** returns the senses of this word */
  public List getWordSenses();

  /** returns the lemma of this word */
  public String getLemma();

  /** returns the number of senses of this word (not necessarily loading them from storage) */
  public int getSenseCount();

}