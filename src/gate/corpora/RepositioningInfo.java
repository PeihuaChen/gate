/*
 *  RepositioningInfo.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Angel Kirilov, 04/January/2002
 *
 *  $Id$
 */

package gate.corpora;

import java.util.ArrayList;

/**
 * RepositioningInfo keep information about correspondence of positions
 * between the original and extracted document content. With this information
 * this class could be used for computing of this correspondence in the strict
 * way (return -1 where is no correspondence)
 * or in "flow" way (return near computable position)
 */

public class RepositioningInfo extends ArrayList {

  /**
   * Just information keeper inner class. No significant functionality.
   */
  public class PositionInfo {

    /** Data members for one peace of text information */
    private long m_origPos, m_origLength, m_currPos, m_currLength;

    /** The only constructor. We haven't set methods for data members. */
    public PositionInfo(long orig, long origLen, long curr, long currLen) {
      m_origPos = orig;
      m_origLength = origLen;
      m_currPos = curr;
      m_currLength = currLen;
    } // PositionInfo

    /** Position in the extracted (and probably changed) content */
    public long getCurrentPosition() {
      return m_currPos;
    } // getCurrentPosition

    /** Position in the original content */
    public long getOriginalPosition() {
      return m_origPos;
    } // getOriginalPosition

    /** Length of peace of text in the original content */
    public long getOriginalLength() {
      return m_origLength;
    } // getOriginalLength

    /** Length of peace of text in the extracted content */
    public long getCurrentLength() {
      return m_currLength;
    } // getCurrentLength

    /** For debug purposes */
    public String toString() {
      return "("+m_origPos+","+m_origLength+","
                +m_currPos+","+m_currLength+")";
    } // toString
  } // class PositionInfo

  /** Default constructor */
  public RepositioningInfo() {
    super();
  } // RepositioningInfo

  /** Create a new position information record. */
  public void addPositionInfo(long origPos, long origLength,
                              long currPos, long currLength) {
    // sorted add of new position
    int insertPos = 0;
    PositionInfo lastPI;

    for(int i = size(); i>0; i--) {
      lastPI = (PositionInfo) get(i-1);
      if(lastPI.getOriginalPosition() < origPos) {
        insertPos = i;
        break;
      } // if - sort key
    } // for

    add(insertPos, new PositionInfo(origPos, origLength, currPos, currLength));
  } // addPositionInfo

  /** Compute position in extracted content by position in the original content.
   *  If there is no correspondence return -1.
   */
  public long getExtractedPos(long absPos) {
    long result = absPos;
    PositionInfo currPI = null;
    int size = size();

    if(size != 0) {
      long origPos, origLen;
      boolean found = false;

      for(int i=0; i<size; ++i) {
        currPI = (PositionInfo) get(i);
        origPos = currPI.getOriginalPosition();
        origLen = currPI.getOriginalLength();

        if(absPos <= origPos+origLen) {
          if(absPos < origPos) {
            // outside the range of information
            result = -1;
          }
          else {
            // current position + offset in this PositionInfo record
            result = currPI.getCurrentPosition() + absPos - origPos;
          } // if
          found = true;
          break;
        } // if
      } // for

      if(!found) {
        // after the last repositioning info
        result = -1;
      } // if - !found
    } // if

    return result;
  } // getExtractedPos

  /** Compute position in original content by position in the extracted content.
   *  If there is no correspondence return -1.
   */
  public long getOriginalPos(long relPos) {
    long result = relPos;
    PositionInfo currPI = null;
    int size = size();

    if(size != 0) {
      long currPos, currLen;
      boolean found = false;

      for(int i=0; i<size; ++i) {
        currPI = (PositionInfo) get(i);
        currPos = currPI.getCurrentPosition();
        currLen = currPI.getCurrentLength();

        if(relPos <= currPos+currLen) {
          if(relPos < currPos) {
            // outside the range of information
            result = -1;
          }
          else {
            // current position + offset in this PositionInfo record
            result = currPI.getOriginalPosition() + relPos - currPos;
          } // if
          found = true;
          break;
        } // if
      } // for

      if(!found) {
        // after the last repositioning info
        result = -1;
      } // if - !found
    } // if

    return result;
  } // getOriginalPos

  /** Not finished yet */
  public long getExtractedPosFlow(long absPos) {
    long result = -1;
    return result;
  } // getExtractedPosFlow

  /** Not finished yet */
  public long getOriginalPosFlow(long relPos) {
    long result = -1;
    return result;
  } // getOriginalPosFlow
} // class RepositioningInfo