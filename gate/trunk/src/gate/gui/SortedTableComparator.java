/*
 *  SortedTableComparator.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *	Cristian URSU,  30/May/2000
 *
 *	$Id$
 */

package gate.gui;

import java.util.*;

/** This class has to be implemented and used with the implementation of a
  * SortedTableModel.
  * @deprecated  As of 21 Jan 2001, replaced by {@link XJTable}
  */
public abstract class SortedTableComparator implements Comparator {

    /** Debug flag */
    private static final boolean DEBUG = false;

    // Members area
    // The index of the column being sorted
    private int sortCol;
    //The type of sorting (asc/desc)
    private boolean sortAsc;

    /** Constructor sets the default column index as being the first column and
     *  the default sorting type as being ascending.
     */
    public SortedTableComparator() {
      sortCol = 0;
      sortAsc = true;
    }// SortedTableComparator

    /** Set method for sorting column */
    public void setSortCol(int aSortCol) {
      sortCol = aSortCol;
    }//setSortCol

    /** Set method for sorting type (asc/desc)*/
    public void setSortOrder(boolean aSortAsc) {
      sortAsc = aSortAsc;
    }// setSortOrder

    /** Get method for sorting column */
    public int getSortCol() {
      return sortCol;
    }// getSortCol

    /** Get method for sorting order*/
    public boolean getSortOrder(){
      return sortAsc;
    }// getSortOrder
} // SortedTableComparator
