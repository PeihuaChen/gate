/*
 *  DocumentData.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 05/Mar/2002
 *
 *  $Id$
 */

package gate.corpora;

import java.io.*;

public class DocumentData implements Serializable {

  //fix the ID for serialisation
  static final long serialVersionUID = 4192762901421847525L;

  public DocumentData(String name, Object ID){
    docName = name;
    persistentID = ID;
  }

  public String getDocumentName() {
    return docName;
  }

  public Object getPersistentID() {
    return persistentID;
  }

  public void setPersistentID(Object newID) {
    persistentID = newID;
  }

  public String toString() {
    return new String("DocumentData: " + docName + ", " + persistentID);
  }

  String docName;
  Object persistentID;
}

