/*
 *  WordImpl.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 17/May/2002
 *
 *  $Id$
 */

package gate.wordnet;

import java.util.*;

import junit.framework.*;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerTarget;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.JWNLException;

import gate.util.*;


public class WordSenseImpl implements WordSense {

  private Word word;
  private Synset  synset;
  private int senseNumber;
  private int orderInSynset;
  private boolean isSemcor;
  private List lexRelations;
  private Dictionary wnDictionary;

  public WordSenseImpl(Word _word,
                      Synset _synset,
                      int _senseNumber,
                      int _orderInSynset,
                      boolean _isSemcor,
                      Dictionary _wnDict) {

    //0.
    Assert.assertNotNull(_word);
    Assert.assertNotNull(_synset);
    Assert.assertNotNull(_wnDict);

    this.word = _word;
    this.synset = _synset;
    this.senseNumber = _senseNumber;
    this.orderInSynset = _orderInSynset;
    this.isSemcor = _isSemcor;
    this.wnDictionary = _wnDict;
  }

  public Word getWord() {
    return this.word;
  }

  public int getPOS() {
    return this.synset.getPOS();
  }

  public Synset getSynset() {
    return this.synset;
  }

  public int getSenseNumber() {
    return this.senseNumber;
  }

  public int getOrderInSynset() {
    return this.orderInSynset;
  }

  public boolean isSemcor() {
    return this.isSemcor;
  }

  public List getLexicalRelations() throws WordNetException {

    if (null == this.lexRelations) {
      _loadLexicalRelations();
    }

    return this.lexRelations;
  }

  public List getLexicalRelations(int type) throws WordNetException {

    List result = new ArrayList(1);

    if (null == this.lexRelations) {
      _loadLexicalRelations();
    }

    Iterator it = this.lexRelations.iterator();
    while (it.hasNext()) {
      LexicalRelation lRel = (LexicalRelation)it.next();
      Assert.assertNotNull(lRel);
      if (type == lRel.getType()) {
        result.add(lRel);
      }
    }

    return result;
  }


  private void _loadLexicalRelations() throws WordNetException{

    POS jwPOS = null;
    jwPOS = WNHelper.int2POS(this.getPOS());

    try {
      net.didion.jwnl.data.Synset jwSynset = this.wnDictionary.getSynsetAt(jwPOS,this.synset.getOffset());
      Assert.assertNotNull(jwSynset);

      Pointer[] jwPointers = null;

      net.didion.jwnl.data.Word[] jwWords = jwSynset.getWords();
      for (int i=0; i< jwWords.length; i++) {
        net.didion.jwnl.data.Word currJwWord = jwWords[i];
        if (currJwWord.getLemma().equalsIgnoreCase(this.getWord().getLemma())) {
          jwPointers = currJwWord.getPointers();
          break;
        }
      }

      this.lexRelations = new ArrayList(jwPointers.length);

      for (int i= 0; i< jwPointers.length; i++) {

        Pointer currPointer = jwPointers[i];
        //skip semantic relations
        if (false == currPointer.isLexical()) {
          continue;
        }

        PointerType currType = currPointer.getType();
//        PointerTarget ptrSource = currPointer.getSource();
        PointerTarget ptrTarget = currPointer.getTarget();
        Assert.assertTrue(ptrTarget instanceof net.didion.jwnl.data.Word);

        net.didion.jwnl.data.Word jwTargetWord = (net.didion.jwnl.data.Word)ptrTarget;
        net.didion.jwnl.data.Synset jwTargetSynset = jwTargetWord.getSynset();
        IndexWord jwTargetIndexWord = this.wnDictionary.lookupIndexWord(jwTargetWord.getPOS(),
                                                                      jwTargetWord.getLemma());

        Synset gateSynset = new SynsetImpl(jwTargetSynset,this.wnDictionary);

        Word gateWord = new WordImpl(jwTargetWord.getLemma(),
                                      jwTargetIndexWord.getSenseCount(),
                                      this.wnDictionary);

        WordSense gateTargetWordSense = new WordSenseImpl(gateWord,
                                                          gateSynset,
                                                          0,
                                                          jwTargetWord.getIndex(),
                                                          false,
                                                          this.wnDictionary);

        LexicalRelation gateLexRel = new LexicalRelationImpl(WNHelper.PointerType2int(currType),
                                                            this,
                                                            gateTargetWordSense);
        //add to list of sem relations for this synset
        this.lexRelations.add(gateLexRel);
      }
    }
    catch(JWNLException e) {
      throw new WordNetException(e);
    }
  }
}