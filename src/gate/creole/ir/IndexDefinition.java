/*
 *  IndexDefinition.java
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

import java.util.Iterator;
import java.io.Serializable;

public interface IndexDefinition extends Serializable{

  /** @return String  path of index store directory*/
  public String getIndexLocation();

  /**  @return Iterator of IndexFields, fileds for indexing. */
  public Iterator getIndexFields();

  /**  @return int index type*/
  public int getIndexType();

}