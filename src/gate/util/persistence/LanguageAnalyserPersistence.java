/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 29/10/2001
 *
 *  $Id$
 *
 */
package gate.util.persistence;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.persist.PersistenceException;

import java.util.*;
/**
 * Provides a persistent equivalent for {@link LanguageAnalyser}s.
 * Adds handling of corpus and document members for PRPersistence.
 */
public class LanguageAnalyserPersistence extends PRPersistence {
  /**
   * Populates this Persistence with the data that needs to be stored from the
   * original source object.
   */
  public void extractDataFromSource(Object source)throws PersistenceException{
    if(! (source instanceof LanguageAnalyser)){
      throw new UnsupportedOperationException(
                getClass().getName() + " can only be used for " +
                LanguageAnalyser.class.getName() +
                " objects!\n" + source.getClass().getName() +
                " is not a " + LanguageAnalyser.class.getName());
    }

    super.extractDataFromSource(source);

    LanguageAnalyser la = (LanguageAnalyser)source;
    document = PersistenceManager.getPersistentRepresentation(la.getDocument());
    corpus = PersistenceManager.getPersistentRepresentation(la.getCorpus());
  }

  /**
   * Creates a new object from the data contained. This new object is supposed
   * to be a copy for the original object used as source for data extraction.
   */
  public Object createObject()throws PersistenceException,
                                     ResourceInstantiationException{
    LanguageAnalyser la = (LanguageAnalyser)super.createObject();
    la.setCorpus((Corpus)PersistenceManager.getTransientRepresentation(corpus));
    la.setDocument((Document)PersistenceManager.
                             getTransientRepresentation(document));
    return la;
  }


  protected Object corpus;
  protected Object document;
}