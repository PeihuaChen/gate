/*
 *  DefaultIndexDefinition.java
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

public class DefaultIndexDefinition implements IndexDefinition{

  private DocumentAnalyzer analyzer;
  private List fields;
  private String location;
  private int indexType;

  public DocumentAnalyzer getAnalyzer(){
    return analyzer;
  }

  public void setAnalyzer(DocumentAnalyzer analyzer){
    this.analyzer = analyzer;
  }

  public void setIndexLocation(String location){
    this.location = location;
  }

  public String getIndexLocation(){
    return location;
  }

  public int getIndexType(){
    return indexType;
  }

  public void setIndexType(int type){
    this.indexType = type;
  }

  public Iterator getIndexFields(){
    return fields.iterator();
  }

  public void addIndexField(IndexField fld){
    if (fields==null){
      fields = new Vector();
    }
    fields.add(fld);
  }

}