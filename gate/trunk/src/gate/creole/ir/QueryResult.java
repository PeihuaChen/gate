/*
 *  QueryResult.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Rosen Marinov, 19/Apr/2002
 *
 */

package gate.creole.ir;

import java.util.*;
import gate.Document;

public class QueryResult{

  private Long docID;
  private float relevace;
  private List fieldValues;

  public QueryResult(Document doc,float relevance, List fieldNames){
  }

  public Long getDocumentID(){
    return docID;
  }

  public float getScore(){
    return relevace;
  }

  public List getFields(){
    return fieldValues;
  }

}