/*
 *  DatabaseCorpusImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 05/Nov/2001
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;

import gate.*;
import gate.persist.*;
import gate.annotation.*;
import gate.creole.*;
import gate.event.*;
import gate.util.*;


public class DatabaseCorpusImpl extends CorpusImpl {

  public DatabaseCorpusImpl() {

    super();
  }

  public boolean add(Object o){

    //accept only documents
    if (false == o instanceof Document) {
      throw new IllegalArgumentException("");
    }

    LanguageResource lr = (LanguageResource)o;
    Long lrID = (Long)lr.getLRPersistenceId();

    //assert docs are either transient or from the same datastore
    if (null == this.getDataStore()) {
      //accept only transient doc
      if (null == lrID) {
        //ok, accept it
        return super.add(o);
      }
    }
    else {
      //accept transient docs + these from our DS

      if (null == lrID || lr.getDataStore().equals(this.getDataStore())) {
        //ok, accept it
        return super.add(o);
      }
    }

    return false;
  }


  public boolean addAll(Collection c){

    throw new MethodNotImplementedException();
//    return supportList.addAll(c);
  }

  public boolean addAll(int index, Collection c){
    throw new MethodNotImplementedException();
//    return supportList.addAll(index, c);
  }

}