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
import java.io.*;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.persist.PersistenceException;


public class SynsetImpl implements Synset {

  private ArrayList wordSenses;
  private ArrayList semRelations;
  private String gloss;
  private int synsetPOS;
  Dictionary wnDictionary;
  private long synsetOffset;

  public SynsetImpl(net.didion.jwnl.data.Synset jwSynset, Dictionary _wnDictionary) throws Exception {

    //0.
    Assert.assertNotNull(jwSynset);

    //dictionary
    this.wnDictionary = _wnDictionary;

    //offset
    this.synsetOffset = jwSynset.getOffset();

    //pos
    this.synsetPOS = WNHelper.POS2int(jwSynset.getPOS());
/*
    if (jwSynset.getPOS().equals(POS.ADJECTIVE)) {
      this.synsetPOS = WordNet.POS_ADJECTIVE;
    }
    else if (jwSynset.getPOS().equals(POS.ADVERB)) {
      this.synsetPOS = WordNet.POS_ADVERB;
    }
    else if (jwSynset.getPOS().equals(POS.NOUN)) {
      this.synsetPOS = WordNet.POS_NOUN;
    }
    else if (jwSynset.getPOS().equals(POS.VERB)) {
      this.synsetPOS = WordNet.POS_VERB;
    }
    else {
      Assert.fail();
    }
*/
    //gloss
    this.gloss = jwSynset.getGloss();

    //word senses
    net.didion.jwnl.data.Word[] synsetWords = jwSynset.getWords();
    this.wordSenses = new ArrayList(synsetWords.length);

    for (int i= 0; i< synsetWords.length; i++) {

      net.didion.jwnl.data.Word jwWord = synsetWords[i];
      IndexWord jwIndexWord = this.wnDictionary.lookupIndexWord(jwWord.getPOS(),jwWord.getLemma());

      Word gateWord = new WordImpl(jwWord.getLemma(),
                                   jwIndexWord.getSenseCount(),
                                   _wnDictionary);

      WordSense gateWordSense = new WordSenseImpl(gateWord,
                                                  this,
                                                  0,
                                                  jwWord.getIndex(),
                                                  false);
      this.wordSenses.add(gateWordSense);
    }

  }

  public int getPOS(){
    return this.synsetPOS;
  }

  public boolean isUniqueBeginner(){
    throw new MethodNotImplementedException();
  }

  public String getGloss(){
    return this.gloss;
  }

  public List getWordSenses(){
    return this.wordSenses;
  }

  public WordSense getWordSense(int offset){
    return (WordSense)this.wordSenses.get(offset);
  }

  public List getSemanticRealtions() throws WordNetException{

    if (null == this.semRelations) {
      _loadSemanticRealtions();
    }

    return this.semRelations;
  }

  public List getSemanticRealtions(int type) throws WordNetException{

    List result = new ArrayList(1);

    if (null == this.semRelations) {
      _loadSemanticRealtions();
    }

    Iterator it = this.semRelations.iterator();
    while (it.hasNext()) {
      SemanticRelation sRel = (SemanticRelation)it.next();
      Assert.assertNotNull(sRel);
      if (type == sRel.getType()) {
        result.add(sRel);
      }
    }

    return result;
  }


  private void _loadSemanticRealtions() throws WordNetException{

    POS jwPOS = null;
    jwPOS = WNHelper.int2POS(this.synsetPOS);

    try {
      net.didion.jwnl.data.Synset jwSynset = this.wnDictionary.getSynsetAt(jwPOS,this.synsetOffset);
      Pointer[] jwPointers = jwSynset.getPointers();

      for (int i= 0; i< jwPointers.length; i++) {
        Pointer currPointer = jwPointers[i];
        PointerType currType = currPointer.getType();
//        currType.
      }
    }
    catch(JWNLException e) {
      throw new WordNetException(e);
    }


    throw new MethodNotImplementedException();
  }

}