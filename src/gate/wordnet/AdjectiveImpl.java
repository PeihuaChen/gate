/*
 *  AdjectiveImpl.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 20/May/2002
 *
 *  $Id$
 */

package gate.wordnet;

import net.didion.jwnl.dictionary.Dictionary;

/** Represents WordNet adjective
 *  implements interface Adjective
 */
public class AdjectiveImpl extends WordSenseImpl
                          implements Adjective {

  private int adjPosition;

  public AdjectiveImpl(Word _word,
                      Synset _synset,
                      int _senseNumber,
                      int _orderInSynset,
                      boolean _isSemcor,
                      int _adjPosition,
                      Dictionary _wnDict) {

    super(_word,_synset,_senseNumber,_orderInSynset,_isSemcor, _wnDict);
    this.adjPosition = _adjPosition;
  }

  /** returns the syntactic position of the adjective in relation to noun that it modifies */
  public int getAdjectivePosition() {
    return this.adjPosition;
  }
}