/*
 *  BumpyStack.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 10/Nov/2000
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import gate.*;

/** Stacks that allow you to bump an element to the front. */
public class BumpyStack extends Stack
{
  /** Bump an item to the front of the stack.
    * @param item the item to bump
    * @return true when the item was found, else false
    */
  public boolean bump(Object item) {
    int itemIndex = search(item);

    if(itemIndex == -1) // not a member of the stack
      return false;
    else if(itemIndex == 1) // at the front already
      return true;

    removeElementAt(itemIndex - 1);
    push(item);

    return true;
  } // bump

} // class BumpyStack
