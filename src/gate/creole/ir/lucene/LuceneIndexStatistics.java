/*
 *  LuceneIndexStatistics.java
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

package gate.creole.ir.lucene;

import gate.creole.ir.*;
import java.util.*;

public class LuceneIndexStatistics implements IndexStatistics {

  public LuceneIndexStatistics(){
  }

  public Long getTermCount(){
    //NOT IMPLEMENTED YET
    return null;
  }

  public Long getUniqueTermCount(){
    //NOT IMPLEMENTED YET
    return null;
  }

  public Long getExhaustivity(Long docID, String fieldName){
    //NOT IMPLEMENTED YET
    return null;
  }

  public Long getSpecificity(String term){
    //NOT IMPLEMENTED YET
    return null;
  }

  public HashMap getTermFrequency(Long docID, String fieldName){
    //NOT IMPLEMENTED YET
    return null;
  }

}