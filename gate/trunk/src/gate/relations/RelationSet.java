/*
 *  RelationData.java
 *
 *  Copyright (c) 1995-2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 27 Feb 2012
 *
 *  $Id$
 */
package gate.relations;

import gate.AnnotationSet;
import gate.Document;
import gate.Gate;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

/**
 * Utility class for managing a set of GATE relations (usually each annotation
 * set of a document will have one set of associated relations).
 */
public class RelationSet implements Serializable {
  
  private static final long serialVersionUID = 8552798130184595465L;

  private static final Logger log = Logger.getLogger(RelationSet.class);
  
  /**
   * Annotation ID used when calling {@link #getRelations(int...)} for positions
   * with no restrictions.
   */
  public static final int ANY = -1;
  
  /**
   * Name used for a {@link Document} feature holding a {@link Map} from 
   * {@link String} name to {@link RelationSet}. By convention, the relation set
   * associated with an {@link AnnotationSet} used the annotation set name as a 
   * key.
   */
  public static final String RELATIONS_MAP_DOCUMENT_FEATURE_NAME = "gate.relations";
  
  /**
   * The list of all relations.
   */
  protected List<Relation> relations;
  
  /**
   * Index for relations by type.
   */
  protected Map<String, BitSet> indexByType;
  
  /**
   * Keeps the indexes (in {@link #relations}) for relations that have been 
   * deleted.
   */
  protected BitSet deleted;
  
  /**
   * Indexes for relations by member. Each element in the list refers to a given
   * position in the members array: the element at position zero refers to the 
   * first member of all relations.
   * 
   * The element at position <code>pos</code> is a map from annotation ID 
   * (representing a relation member) to a {@link BitSet} indicating which of
   * the relation indexes (in {@link #relations}) correspond to relations that 
   * contain the given annotation (i.e. member) on the position 
   * <code>pos</code>. 
   */
  protected List<Map<Integer, BitSet>> indexesByMember;
  
  /**
   * Factory method that gets a named {@link RelationSet} from a document. 
   * {@link RelationSet}s are stored inside a special document feature, which is
   * where this method retrieves them from. If no relation set is associated
   * with the given name, then a new one is created and returned.
   * 
   * To get the relation set associated with a given annotation set use 
   * {@link #getRelations(AnnotationSet)} instead.
   * 
   * @param document the document for which the relations are being requested.
   * @param name the name for the relation set. This can be any arbitrary 
   * {@link String} or even <code>null</code>. However, by convention, names of 
   * annotation sets (and <code>null</code> for the default annotation set) are
   * used to name the <i>principal</i> relation set for each annotation set.   
   * @return
   */
  public static RelationSet getRelations(Document document, String name) {
    Object relMapObj = document.getFeatures().get(
      RELATIONS_MAP_DOCUMENT_FEATURE_NAME);
    Map<String, RelationSet> relationsMap = null;
    if(relMapObj != null) {
      if(relMapObj instanceof Map) {
        relationsMap = (Map)relMapObj;
      } else {
        document.getFeatures().remove(RELATIONS_MAP_DOCUMENT_FEATURE_NAME);
        log.warn("Invalid value for feature \"" + 
            RELATIONS_MAP_DOCUMENT_FEATURE_NAME + "\" on document \"" + 
            document.getName() + "\" has been removed.");
      }      
    }
    if(relationsMap == null) {
      relationsMap = new HashMap<String, RelationSet>();
      document.getFeatures().put(RELATIONS_MAP_DOCUMENT_FEATURE_NAME, 
        relationsMap);
    }
    RelationSet relSet = relationsMap.get(name);
    if(relSet == null) {
      relSet = new RelationSet();
      relationsMap.put(name, relSet);
    }
    return relSet;
  }
  
  /**
   * Factory method that gets the {@link RelationSet} associated with an 
   * {@link AnnotationSet}.
   * @param annSet the annotation set for which the relations are being 
   * requested.
   * @return the requested relation set. If none exists, a new one is created
   * and returned.
   */
  public static RelationSet getRelations(AnnotationSet annSet) {
    return getRelations(annSet.getDocument(), annSet.getName());  
  }
  
  
  public RelationSet(String stringSerialisation) {
    this();
    if(stringSerialisation.startsWith("[") && 
        stringSerialisation.endsWith("]")) {
      stringSerialisation = stringSerialisation.substring(1, 
        stringSerialisation.length() - 1);
    } else {
      throw new IllegalArgumentException("Invalid string serialisation value" +
          "(not bounded by '[',']').");
    }
    String[] elems = stringSerialisation.split(";\\s+");
    for(String aRelStr : elems) {
      if(aRelStr.startsWith("(")) {
        // custom class
        int closeBracket = aRelStr.indexOf(')');
        if(closeBracket < 0){
          throw new IllegalArgumentException("Invalid string serialisation " +
          		"value (illegal Relation class declaration).");
        }
        String className = aRelStr.substring(1, closeBracket);
        aRelStr = aRelStr.substring(closeBracket);
        try {
          @SuppressWarnings("unchecked")
          Class<? extends Relation> clazz = (Class<? extends Relation>)
              Class.forName(className, true, Gate.getClassLoader());
          Constructor<? extends Relation> constructor = 
              clazz.getConstructor(String.class);
          addRelation(constructor.newInstance(aRelStr));
        } catch(Exception e) {
          throw new IllegalArgumentException(
            "Could not instantiate custom Relation implementation.", e);
        }
      } else {
        addRelation(new SimpleRelation(aRelStr));  
      }
      
    }
  }
  
  protected RelationSet() {
    relations = new ArrayList<Relation>();
    indexByType = new HashMap<String, BitSet>();
    indexesByMember = new ArrayList<Map<Integer,BitSet>>();
    deleted = new BitSet();
  }

  /**
   * Creates a new {@link Relation} and adds it to this set. Uses the default 
   * relation implementation at {@link SimpleRelation}.
   * @param type the type for the new relation.
   * @param members the annotation IDs for the annotations that are members in 
   * this relation.
   * @return the newly created {@link Relation} instance.
   */
  public Relation addRelation(String type, int... members) {
    Relation rel = new SimpleRelation(type, members);
    addRelation(rel);
    return rel;
  }
  
  
  /**
   * Adds an externally-created {@link Relation} instance.
   * @param rel the {@link Relation} to be added.
   */
  public void addRelation(Relation rel) {
    int relIdx = relations.size();
    relations.add(rel);
    BitSet sameType = indexByType.get(rel.getType());
    if(sameType == null) {
      sameType = new BitSet(relations.size());
      indexByType.put(rel.getType(), sameType);
    }
    sameType.set(relIdx);

    // widen the index by member list, if needed
    for(int i = indexesByMember.size(); i < rel.getMembers().length; i++) {
      indexesByMember.add(new HashMap<Integer, BitSet>());
    }
    for(int memeberPos = 0; memeberPos < rel.getMembers().length; memeberPos++) {
      int member = rel.getMembers()[memeberPos];
      Map<Integer, BitSet> indexByMember = indexesByMember.get(memeberPos);
      BitSet sameMember = indexByMember.get(member);
      if(sameMember == null) {
        sameMember = new BitSet(relations.size());
        indexByMember.put(member, sameMember);
      }
      sameMember.set(relIdx);
    }
  }
  
  /**
   * Returns the maximum arity for any relation in this {@link RelationSet}.
   * @return an int value.
   */
  public int getMaximumArity() {
    return indexesByMember.size();
  }
  
  /**
   * Finds relations based on their type.
   * @param type the type of relation being sought.
   * @return the list of all relations in this {@link RelationSet} that have the
   * required type.
   */
  public List<Relation> getRelations(String type) {
    List<Relation> res = new ArrayList<Relation>();
    BitSet rels = indexByType.get(type);
    if(rels != null) {
      rels.andNot(deleted);
      for(int relPos = 0; relPos < relations.size(); relPos++){
        if(rels.get(relPos)) res.add(relations.get(relPos));  
      }
    }
    return res;
  }
  
  /**
   * Finds relations based on their members.
   * @param members an array containing annotation IDs. If a constraint is not
   * required for a given member position, then the {@link #ANY}. value should 
   * be used.
   * @return all the relations that have the given annotation IDs (members)
   * on the specified positions.
   */
  public List<Relation> getRelations(int... members) {
    // get the lists of relations for each member
    BitSet[] postingLists = new BitSet[indexesByMember.size()];
    for(int  i = 0; i < postingLists.length; i++) {
      if(i < members.length && members[i] >= 0) {
        postingLists[i] = indexesByMember.get(i).get(members[i]);
      } else {
        postingLists[i] = null;
      }
    }
    return intersection(postingLists);
  }
  
  public List<Relation> getRelations(String type, int... members) {
    // get the lists of relations for each member
    BitSet[] postingLists = new BitSet[indexesByMember.size() + 1];
    for(int  i = 0; i < postingLists.length; i++) {
      if(i < members.length && members[i] >= 0) {
        postingLists[i] = indexesByMember.get(i).get(members[i]);
      } else {
        postingLists[i] = null;
      }
    }
    postingLists[postingLists.length - 1] = indexByType.get(type);
    return intersection(postingLists);
  }
  
  /**
   * Deletes the specified relation.
   * @param relation the relation to be deleted.
   * @return <code>true</code> if the given relation was deleted, or 
   * <code>false</code> if it was not found.
   */
  public boolean deleteRelation(Relation relation) {
    int relIdx = relations.indexOf(relation);
    if(relIdx >= 0){
      deleted.set(relIdx);
      relations.set(relIdx, null);
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Calculates the intersection of a set of lists containing relation indexes.
   * @param indexLists the list to be intersected.
   * @return
   */
  protected List<Relation> intersection(BitSet... indexLists) {
    BitSet relIds = new BitSet(relations.size());
    relIds.set(0, relations.size() - 1);
    relIds.andNot(deleted);
    for(BitSet aList : indexLists) {
      if(aList != null) {
        relIds.and(aList);
        if(relIds.isEmpty()) break;
      }
    }
    
    List<Relation> res = new ArrayList<Relation>();
    for(int relIdx = 0; relIdx < relations.size(); relIdx++){
      if(relIds.get(relIdx)) res.add(relations.get(relIdx));
    }
    return res;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("[");
    boolean first = true;
    for(int  i = 0; i < relations.size(); i++) {
      if(!deleted.get(i)) {
        if(first) {
          first = false;
        } else{ 
          str.append("; ");
        }
        String relStr = relations.get(i).toString();
        relStr = relStr.replaceAll(";", Matcher.quoteReplacement("\\;"));
        if(!relations.get(i).getClass().equals(SimpleRelation.class)) {
          relStr = "(" + relations.get(i).getClass().getName() + ")" + relStr;  
        }
        str.append(relStr);
      }
    }
    str.append("]");
    return str.toString();
  }
  
}
