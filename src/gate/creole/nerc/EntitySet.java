/*
 *  EntitySet.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, July/2000
 *
 *  $Id$
 */

package gate.creole.nerc;

import gate.*;

import java.util.*;
import java.io.Serializable;

/** Representing a set of entities found in a single text file.
  * Each member a the set is an EntityDescriptor
  */
public class EntitySet extends AbstractSet implements Set, Serializable {

  /** Constructs an entity set from a Gate annotation set*/
  public EntitySet(String fileName, Document document,
                   AnnotationSet annotationSet) {
    this.fileName = fileName;
    myEntities = new HashSet();
    if(annotationSet != null){
      Iterator annIter = annotationSet.iterator();
      while(annIter.hasNext()){
        myEntities.add(new EntityDescriptor(document,
                                                  (Annotation)annIter.next()));
      }
    }
  }

  /** Returns the name of the file where the entities in this set
    *  were discovered
    */
  public String getTextFileName() {
    return fileName;
  }

  /** Returns a string giving the file name on one line (preceded by
    * &quot;==== FILE : &quot; followed by each entity descriptor's string
    * representation, one-per-line.
    */
  public String toString() {
    String res = "==== FILE: " + fileName + "\n";
    Iterator entIter = myEntities.iterator();
    while(entIter.hasNext()){
      res += entIter.next().toString() + "\n";
    }
    return res;
  }

  public int size(){ return myEntities.size();}

  public Iterator iterator() {return myEntities.iterator();}

  String fileName;
  Set myEntities;
}