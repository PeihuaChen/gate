/*
 * NodePosition.java
 *
 * Copyright (c) 2004, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Niraj Aswani 02/2002
 *
 */

package gate.creole.gazetteer;

/**
 * <p>Title: NodePosition.java </p>
 * <p>Description: This class is used to store the information about the
 * changes in the text and the addition or the substraction of the spaces.
 * It is used by FlexibleGazetteer. </p>
 * @author Niraj Aswani
 * @version 1.0
 */

public class NodePosition {

  /** The original start offset before changes */
  private long oldStartNode;

  /** The original end offset before changes */
  private long oldEndNode;

  /** The new start offset after the changes */
  private long newStartNode;

  /** The new end offset after the changes */
  private long newEndNode;

  /** total deducted spaces due to change in the text before the start
   * offset in the document
   */
  private long deductedSpaces;

  /** Constructor */
  public NodePosition() {
  }

  /**
   * constructor
   * @param osn - old start offset
   * @param oen - old end offset
   * @param nsn - new start offset
   * @param nen - new end offset
   * @param space - total deducted spaces due to change in the text before
   * the start offset in the document
   */
  public NodePosition(long osn, long oen, long nsn, long nen, long space) {
    oldStartNode = osn;
    oldEndNode = oen;
    newStartNode = nsn;
    newEndNode = nen;
    deductedSpaces = space;
  }

  /**
   * Returns the old start offset
   * @return a <tt>long</tt> value.
   */
  public long getOldStartNode() {
    return oldStartNode;
  }

  /**
   * Returns the old end offset
   * @return a <tt>long</tt> value.
   */
  public long getOldEndNode() {
    return oldEndNode;
  }

  /**
   * Returns new start offset
   * @return  a <tt>long</tt> value.
   */
  public long getNewStartNode() {
    return newStartNode;
  }

  /**
   * Returns the new end offset
   * @return a <tt>long</tt> value.
   */
  public long getNewEndNode() {
    return newEndNode;
  }

  /**
   * Sets the old start offset
   * @param node
   */
  public void setOldStartNode(long node) {
    oldStartNode = node;
  }

  /**
   * Sets the old end offset
   * @param node
   */
  public void setOldEndNode(long node) {
    oldEndNode = node;
  }

  /**
   * sets the new start offset
   * @param node
   */
  public void setNewStartNode(long node) {
    newStartNode = node;
  }

  /**
   * Sets the new end offset
   * @param node
   */
  public void setNewEndNode(long node) {
    newEndNode = node;
  }

  /**
   * Sets the deducted spaces
   * @param space
   */
  public void setDeductedSpaces(long space) {
    deductedSpaces = space;
  }

  /**
   * Returns the total deducted spaces
   * @return a <tt>long</tt> value.
   */
  public long getDeductedSpaces() {
    return deductedSpaces;
  }
}