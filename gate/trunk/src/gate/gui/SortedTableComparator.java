/*
 *	SortedTableComparator.java
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

public abstract class SortedTableComparator implements Comparator{

    /** Debug flag */
    private static final boolean DEBUG = false;

    // members area
    private int m_sortCol;
    private boolean m_sortAsc;

    public SortedTableComparator(){
      m_sortCol = 0;
      m_sortAsc = true;
    }

    // mutator
    public void setSortCol(int sortCol){
      m_sortCol = sortCol;
    }
    public void setSortOrder(boolean sortAsc){
      m_sortAsc = sortAsc;
    }

    // accessor
    public int getSortCol(){
      return m_sortCol;
    }
    public boolean getSortOrder(){
      return m_sortAsc;
    }
}
