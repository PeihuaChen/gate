/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 05/10/2001
 *
 *  $Id$
 *
 */


package gate;

/**
 * A special type of {@link ProcessingResource} that processes {@link Document}s
 */
public interface LanguageAnalyser extends ProcessingResource {

  /** Set the document property for this analyser. */
  public void setDocument(Document document);

  /** Get the document property for this analyser. */
  public Document getDocument();

  /** Set the corpus property for this analyser. */
  public void setCorpus(Corpus corpus);

  /** Get the corpus property for this analyser. */
  public Corpus getCorpus();
}