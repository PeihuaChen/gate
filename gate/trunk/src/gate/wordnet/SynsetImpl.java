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

//import net.didion.jwnl.JWNL;
//import net.didion.jwnl.dictionary.Dictionary;


import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.persist.PersistenceException;


public class SynsetImpl implements Synset {

  private ArrayList wordSenses;
  private ArrayList semRelations;
  private String gloss;
  private int POS;

  public SynsetImpl(int _POS,
                    String _gloss,
                    List _wordSenses,
                    List _semRelations) {
  }

  public int getPOS(){
    return this.POS;
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

  public List getSemanticRealtions(){
    return this.semRelations;
  }

  public List getSemanticRealtions(int type){
    List result = new ArrayList(1);

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

}