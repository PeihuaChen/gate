/*
 *  PropertyReader.java
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

import gate.Document;
import java.io.Serializable;


public interface PropertyReader extends Serializable{

  static final long serialVersionUID = 3632609241787241616L;

  /** @return String value of the requested field. */
  public String getRpopertyValue(Document doc);

}