/*
 *  SimpleArraySet.java
 *
 *  Copyright (c) 2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *   D.Ognyanoff, 5/Nov/2001
 *
 *  $Id$
 */


package gate.util;

import java.util.*;

/**
 * A specific *partial* implementation of the Set interface used for
 * high performance and memory reduction on small sets. Used in
 * gate.fsm.State, for example
 */
public class SimpleArraySet
{
  /**
   * The array storing the elements
   */
  Object[] theArray = null;

  public boolean add(Object tr)
  {
    if (theArray == null)
    {
      theArray = new Object[1];
      theArray[0] = tr;
    } else {
      int newsz = theArray.length+1;
      int index = java.util.Arrays.binarySearch(theArray, tr);
      if (index < 0)
      {
        index = ~index;
        Object[] temp = new Object[newsz];
        int i;
        for (i = 0; i < index; i++)
        {
          temp[i] = theArray[i]; theArray[i] = null;
        }
        for (i = index+1; i<newsz; i++)
        {
          temp[i] = theArray[i-1]; theArray[i-1] = null;
        }
        temp[index] = tr;
        theArray = temp;
      } else {
        theArray[index] = tr;
      }
    } // if initially empty
    return true;
  } // add

  /**
   * iterator
   */
  public java.util.Iterator iterator()
  {
    if (theArray == null)
      return new java.util.Iterator()
        {
          public boolean hasNext() {return false;}
          public Object next() { return null; }
          public void remove() {}
        };
    else
      return new java.util.Iterator()
        {
          int count = 0;
          public boolean hasNext()
          {
            if (theArray == null)
              throw new RuntimeException("");
            return count < theArray.length;
          }
          public Object next() {
            if (theArray == null)
              throw new RuntimeException("");
            return theArray[count++];
          }
          public void remove() {}
        }; // anonymous iterator
  } // iterator

} // SimpleArraySet
