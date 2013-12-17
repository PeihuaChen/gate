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

import gate.Annotation;
import gate.AnnotationSet;
import gate.corpora.DocumentImpl;
import gate.event.AnnotationSetEvent;
import gate.event.AnnotationSetListener;
import gate.event.RelationSetEvent;
import gate.event.RelationSetListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

/**
 * Utility class for managing a set of GATE relations (usually each
 * annotation set of a document will have one set of associated
 * relations).
 */
public class RelationSet implements Serializable, AnnotationSetListener {

  private static final long serialVersionUID = 8552798130184595465L;

  private static final Logger log = Logger.getLogger(RelationSet.class);

  /**
   * Annotation ID used when calling {@link #getRelations(int...)} for
   * positions with no restrictions.
   */
  public static final int ANY = -1;

  /**
   * The list of all relations.
   */
  protected List<Relation> relations;

  /**
   * Index for relations by type.
   */
  protected Map<String, BitSet> indexByType;

  /**
   * Index for relations by id.
   */
  protected Map<Integer, Relation> indexById;

  /**
   * Keeps the indexes (in {@link #relations}) for relations that have
   * been deleted.
   */
  protected BitSet deleted;

  /**
   * Indexes for relations by member. Each element in the list refers to
   * a given position in the members array: the element at position zero
   * refers to the first member of all relations.
   * 
   * The element at position <code>pos</code> is a map from annotation
   * ID (representing a relation member) to a {@link BitSet} indicating
   * which of the relation indexes (in {@link #relations}) correspond to
   * relations that contain the given annotation (i.e. member) on the
   * position <code>pos</code>.
   */
  protected List<Map<Integer, BitSet>> indexesByMember;

  /**
   * The {@link AnnotationSet} this set of relations relates to. The
   * assumption (which is not currently enforced) is that all members of
   * this RelationSet will be either {@link Annotation} instances from
   * this {@link AnnotationSet} or other {@link Relation} instances
   * within this set.
   */
  protected AnnotationSet annSet;

  private Vector<RelationSetListener> listeners = null;

  private int maxID = 0;

  public AnnotationSet getAnnotationSet() {
    return annSet;
  }

  public Collection<Relation> get() {
    return indexById.values();
  }

  /**
   * You should never create a RelationSet directly, instead get if via
   * the AnnotationSet
   */
  public RelationSet(AnnotationSet annSet) {
    this.annSet = annSet;
    relations = new ArrayList<Relation>();
    indexByType = new HashMap<String, BitSet>();
    indexesByMember = new ArrayList<Map<Integer, BitSet>>();
    indexById = new HashMap<Integer, Relation>();
    deleted = new BitSet();

    annSet.addAnnotationSetListener(this);
  }

  /**
   * Empties the relation set
   */
  public void clear() {
    relations.clear();
    indexByType.clear();
    indexesByMember.clear();
    indexById.clear();
    deleted.clear();
  }

  /**
   * The number of relations in this set.
   * 
   * @return the number of relations in this set.
   */
  public int size() {
    // return the size of the by ID index as this is the only place that
    // actually tracks the size accurately once a relation has been
    // deleted from the set
    return indexById.size();
  }

  /**
   * Creates a new {@link Relation} and adds it to this set. Uses the
   * default relation implementation at {@link SimpleRelation}.
   * 
   * @param type the type for the new relation.
   * @param members the annotation IDs for the annotations that are
   *          members in this relation.
   * @return the newly created {@link Relation} instance.
   */
  public Relation addRelation(String type, int... members)
          throws IllegalArgumentException {

    if(members.length == 0)
      throw new IllegalArgumentException("A relation can't have zero members");

    for(int member : members) {
      if(!indexById.containsKey(member) && annSet.get(member) == null)
        throw new IllegalArgumentException(
                "Member must be from within this annotation set");
    }

    Relation rel =
            new SimpleRelation(
                    ((DocumentImpl)annSet.getDocument()).getNextAnnotationId(),
                    type, members);
    addRelation(rel);
    return rel;
  }

  /**
   * Adds an externally-created {@link Relation} instance.
   * 
   * @param rel the {@link Relation} to be added.
   */
  public void addRelation(Relation rel) {
    maxID = Math.max(maxID, rel.getId());

    int relIdx = relations.size();
    relations.add(rel);

    /** index by ID **/
    indexById.put(rel.getId(), rel);

    /** index by type **/
    BitSet sameType = indexByType.get(rel.getType());
    if(sameType == null) {
      sameType = new BitSet(rel.getId());
      indexByType.put(rel.getType(), sameType);
    }
    sameType.set(rel.getId());

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

    fireRelationAdded(new RelationSetEvent(this,
            RelationSetEvent.RELATION_ADDED, rel));
  }

  /**
   * Returns the maximum arity for any relation in this
   * {@link RelationSet}.
   * 
   * @return an int value.
   */
  public int getMaximumArity() {
    return indexesByMember.size();
  }

  /**
   * Finds relations based on their type.
   * 
   * @param type the type of relation being sought.
   * @return the list of all relations in this {@link RelationSet} that
   *         have the required type.
   */
  public List<Relation> getRelations(String type) {
    List<Relation> res = new ArrayList<Relation>();
    BitSet rels = indexByType.get(type);
    if(rels != null) {
      for(int relPos = 0; relPos < maxID; relPos++) {
        if(rels.get(relPos)) res.add(indexById.get(relPos));
      }
    }
    return res;
  }

  public Relation get(Integer id) {
    return indexById.get(id);
  }

  /**
   * Finds relations based on their members.
   * 
   * @param members an array containing annotation IDs. If a constraint
   *          is not required for a given member position, then the
   *          {@link #ANY}. value should be used.
   * @return all the relations that have the given annotation IDs
   *         (members) on the specified positions.
   */
  public List<Relation> getRelations(int... members) {
    // get the lists of relations for each member
    BitSet[] postingLists = new BitSet[indexesByMember.size()];
    for(int i = 0; i < postingLists.length; i++) {
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
    for(int i = 0; i < postingLists.length; i++) {
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
   * 
   * @param relation the relation to be deleted.
   * @return <code>true</code> if the given relation was deleted, or
   *         <code>false</code> if it was not found.
   */
  public boolean deleteRelation(Relation relation) {
    int relIdx = relations.indexOf(relation);
    if(relIdx >= 0) {

      // delete this relation from the type index
      indexByType.get(relation.getType()).clear(relation.getId());

      deleted.set(relIdx);
      relations.set(relIdx, null);
      indexById.remove(relation.getId());
      fireRelationRemoved(new RelationSetEvent(this,
              RelationSetEvent.RELATION_REMOVED, relation));
      return true;
    } else {
      return false;
    }
  }

  /**
   * Calculates the intersection of a set of lists containing relation
   * indexes.
   * 
   * @param indexLists the list to be intersected.
   * @return the list of relations contained in all the supplied index
   *         lists.
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
    for(int relIdx = 0; relIdx < relations.size(); relIdx++) {
      if(relIds.get(relIdx)) res.add(relations.get(relIdx));
    }
    return res;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("[");
    boolean first = true;
    for(int i = 0; i < relations.size(); i++) {
      if(!deleted.get(i)) {
        if(first) {
          first = false;
        } else {
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

  @Override
  public void annotationAdded(AnnotationSetEvent e) {
    // we don't care about annotations being added so we do nothing
  }

  @Override
  public void annotationRemoved(AnnotationSetEvent e) {

    Annotation a = e.getAnnotation();

    // find all relations which include the annotation and remove them

    // may need to be an iterative method as we may remove relations
    // which are themselves in a relation
  }

  public synchronized void removeRelationSetListener(RelationSetListener l) {
    if(listeners != null && listeners.contains(l)) {
      @SuppressWarnings("unchecked")
      Vector<RelationSetListener> v =
              (Vector<RelationSetListener>)listeners.clone();
      v.removeElement(l);
      listeners = v;
    }
  }

  public synchronized void addAnnotationSetListener(RelationSetListener l) {
    @SuppressWarnings("unchecked")
    Vector<RelationSetListener> v =
            listeners == null
                    ? new Vector<RelationSetListener>(2)
                    : (Vector<RelationSetListener>)listeners.clone();
    if(!v.contains(l)) {
      v.addElement(l);
      listeners = v;
    }
  }

  protected void fireRelationAdded(RelationSetEvent e) {
    if(listeners != null) {
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        listeners.elementAt(i).relationAdded(e);
      }
    }
  }

  protected void fireRelationRemoved(RelationSetEvent e) {
    if(listeners != null) {
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        listeners.elementAt(i).relationRemoved(e);
      }
    }
  }
}
