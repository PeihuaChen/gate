/*
 *	SortedTableComparator.java
 *
 *	Cristian URSU,  30/May/2000
 *
 *	$Id$
 */

package gate.gui;

import java.util.*;

public abstract class SortedTableComparator implements Comparator{
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

