/*
 *  EventAwareCorpus.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 06/Mar/2002
 *
 *
 *  $Id$
 */


package gate.corpora;

import java.util.*;


public interface EventAwareCorpus extends EventAwareLanguageResource {

  public List getLoadedDocuments();

  public List getRemovedDocuments();

  public List getAddedDocuments();

}