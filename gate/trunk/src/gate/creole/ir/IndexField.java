/*
 *  IndexField.java
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

public class IndexField{

  private String fieldName;
  private PropertyReader propReader;
  private boolean isPreseved;

  public IndexField(String name, PropertyReader rdr, boolean preseved) {
    this.fieldName = name;
    this.propReader = rdr;
    this.isPreseved = preseved;
  }

  public String getName(){
    return fieldName;
  }

  public PropertyReader getReader(){
    return propReader;
  }

  public boolean isPreseved(){
    return isPreseved;
  }

}