/*
 *  LexKBWordSense.java
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

public interface LexKBWordSense {

  /** returns the Word of this WordSense */
  public Word getWord();

  /** part-of-speech for this sense (inherited from the containing synset) */
  public Object getPOS();

  /** synset of this sense */
  public LexKBSynset getSynset();

  /** order of this sense relative to the word - i.e. most important senses of the same word come first */
  public int getSenseNumber();

  /** order of this sense relative to the synset- i.e. most important senses of the same synset come first */
  public int getOrderInSynset();

}