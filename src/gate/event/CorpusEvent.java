/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 13/07/2001
 *
 *  $Id$
 */

package gate.event;

import gate.Corpus;
import gate.Document;

/**
 * Models events fired by corpora when documents are added or removed.
 */
public class CorpusEvent extends GateEvent {

  /**
   * Event type that is fired when a new document is added to a corpus
   */
  public final static int DOCUMENT_ADDED = 401;

  /**
   * Event type that is fired when a document is removed from a corpus
   */
  public final static int DOCUMENT_REMOVED = 402;

  /**
   * Creates a new CorpusEvent.
   * @param source the corpus that fires the event
   * @param doc the document this event refers to
   * @param type the type of event ({@link #DOCUMENT_ADDED} or
   * {@link #DOCUMENT_REMOVED}).
   */
  public CorpusEvent(Corpus source, Document doc, int index, int type){
    super(source, type);
    this.document = doc;
    this.documentIndex = index;
  }

  /**
   * Gets the dcument this event refers to
   */
  public gate.Document getDocument() {
    return document;
  }

  /**
   * Gets the index of the document this event refers to
   */
  public int getDocumentIndex() {
    return this.documentIndex;
  }

  /**
   * The document that has been added/removed.
   */
  private gate.Document document;
  /**
   * The index of the document which has been removed. Needed because
   * the document itself might not have been loaded in memory, so the
   * index could be used instead.
   */
  private int documentIndex;
}

