/*
 *  WordSense.java
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

import net.didion.jwnl.data.POS;

final class WNHelper {

  private WNHelper() {
  }

  public static POS int2POS(int pos) {

    POS result = null;

    switch(pos) {

      case WordNet.POS_ADJECTIVE:
        result = POS.ADJECTIVE;
        break;

      case WordNet.POS_ADVERB:
        result = POS.ADVERB;
        break;

      case WordNet.POS_NOUN:
        result = POS.NOUN;
        break;

      case WordNet.POS_VERB:
        result = POS.VERB;
        break;

      default:
        throw new IllegalArgumentException();
    }

    return result;
  }

  public static int POS2int(POS pos) {

    int result;

    //pos
    if (pos.equals(POS.ADJECTIVE)) {
      result = WordNet.POS_ADJECTIVE;
    }
    else if (pos.equals(POS.ADVERB)) {
      result = WordNet.POS_ADVERB;
    }
    else if (pos.equals(POS.NOUN)) {
      result = WordNet.POS_NOUN;
    }
    else if (pos.equals(POS.VERB)) {
      result = WordNet.POS_VERB;
    }
    else {
      throw new IllegalArgumentException();
    }

    return result;
  }

}