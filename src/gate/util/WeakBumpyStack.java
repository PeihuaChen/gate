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
 *  Valentin Tablan, 10/Oct/2001
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.lang.ref.*;

import gate.*;

/**
 * Weak stack that allow you to bump an element to the front.
 * Objects that are only referenced by this stack will be candidates for
 * garbage collection and wil be removed from the stack as soon as the garbage
 * collector marks them for collection.
 */
public class WeakBumpyStack extends AbstractList
{

  /**
   * Creates a new empty stack.
   */
  public WeakBumpyStack(){
    supportStack = new Stack();
    refQueue = new ReferenceQueue();
  }

  /**
   * Pushes an item onto the top of this stack. This has exactly
   * the same effect as:
   * <blockquote><pre>
   * addElement(item)</pre></blockquote>
   *
   * @param   item   the item to be pushed onto this stack.
   * @return  the <code>item</code> argument.
   */
  public Object push(Object item){
    supportStack.push(new WeakReference(item,refQueue));
    return item;
  }

  /**
   * Removes the object at the top of this stack and returns that
   * object as the value of this function.
   *
   * @return     The object at the top of this stack.
   * @exception  EmptyStackException  if this stack is empty.
   */
  public synchronized Object pop(){
    processQueue();
    //we need to check for null in case the top reference has just been cleared
    Object res = null;
    while(res == null){
      res = ((WeakReference)supportStack.pop()).get();
    }
    return res;
  }


  /**
   * Looks at the object at the top of this stack without removing it
   * from the stack.
   *
   * @return     the object at the top of this stack.
   * @exception  EmptyStackException  if this stack is empty.
   */
  public synchronized Object peek(){
    processQueue();
    //we need to check for null in case the top reference has just been cleared
    Object res = null;
    while(res == null){
      res = ((WeakReference)supportStack.peek()).get();
    }
    return res;
  }

  /**
   * Tests if this stack is empty.
   *
   * @return  <code>true</code> if and only if this stack contains
   *          no items; <code>false</code> otherwise.
   */
  public boolean empty() {
    processQueue();
    return supportStack.empty();
  }


  /** Bump an item to the front of the stack.
    * @param item the item to bump
    * @return true when the item was found, else false
    */
  public boolean bump(Object item) {
    processQueue();

    int itemIndex = search(item);

    if(itemIndex == -1) // not a member of the stack
      return false;
    else if(itemIndex == 1) // at the front already
      return true;

    WeakReference wr = (WeakReference)supportStack.remove(itemIndex - 1);
    supportStack.push(wr);
    return true;
  } // bump

  /**
   * Returns the 1-based position where an object is on this stack.
   * If the object <tt>o</tt> occurs as an item in this stack, this
   * method returns the distance from the top of the stack of the
   * occurrence nearest the top of the stack; the topmost item on the
   * stack is considered to be at distance <tt>1</tt>. The <tt>equals</tt>
   * method is used to compare <tt>o</tt> to the
   * items in this stack.
   *
   * @param   o   the desired object.
   * @return  the 1-based position from the top of the stack where
   *          the object is located; the return value <code>-1</code>
   *          indicates that the object is not on the stack.
   */
  public synchronized int search(Object o) {
    processQueue();
    int i = supportStack.size() - 1;
    while(i >= 0 &&
          !((WeakReference)supportStack.get(i)).get().equals(o)) i--;
    if (i >= 0) {
      return supportStack.size() - i;
    }
    return -1;
  }


  /**
   * Checks the queue for any new weak references that have been cleared and
   * queued and removes them from the underlying stack.
   *
   * This method should be called by every public method before relising its
   * internal logic.
   */
  protected void processQueue(){
    WeakReference wr;
    while ((wr = (WeakReference)refQueue.poll()) != null) {
      supportStack.remove(wr);
    }
  }

  /**
   * Returns the element at the specified position in this list.
   *
   * @param  index index of element to return.
   * @return the element at the specified position in this list.
   * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
   * 		  &lt; 0 || index &gt;= size())</tt>.
   */
  public Object get(int index) {
    processQueue();
    //we need to check for null in case the top reference has just been cleared
    Object res = null;
    while(res == null){
      res = ((WeakReference)supportStack.get(index)).get();
    }
    return res;
  }

  /**
   * Returns the number of elements in this list.
   *
   * @return  the number of elements in this list.
   */
  public int size() {
    processQueue();
    return supportStack.size();
  }

  /**
   * Replaces the element at the specified position in this list with
   * the specified element.
   *
   * @param index index of element to replace.
   * @param element element to be stored at the specified position.
   * @return the element previously at the specified position.
   * @throws    IndexOutOfBoundsException if index out of range
   *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
   */
  public Object set(int index, Object element) {
    processQueue();
    WeakReference ref = (WeakReference)
                        supportStack.set(index,
                                         new WeakReference(element, refQueue));
    return ref.get();
  }

  /**
   * Inserts the specified element at the specified position in this
   * list. Shifts the element currently at that position (if any) and
   * any subsequent elements to the right (adds one to their indices).
   *
   * @param index index at which the specified element is to be inserted.
   * @param element element to be inserted.
   * @throws    IndexOutOfBoundsException if index is out of range
   *		  <tt>(index &lt; 0 || index &gt; size())</tt>.
   */
  public void add(int index, Object element) {
    processQueue();
    supportStack.add(index, new WeakReference(element, refQueue));
  }

  /**
   * Removes the element at the specified position in this list.
   * Shifts any subsequent elements to the left (subtracts one from their
   * indices).
   *
   * @param index the index of the element to removed.
   * @return the element that was removed from the list.
   * @throws    IndexOutOfBoundsException if index out of range <tt>(index
   * 		  &lt; 0 || index &gt;= size())</tt>.
   */
  public Object remove(int index) {
    processQueue();
    //we need to check for null in case the top reference has just been cleared
    Object res = null;
    while(res == null){
      res = ((WeakReference)supportStack.remove(index)).get();
    }
    return res;
  }

  ReferenceQueue refQueue;

  /**
   * This is the underlying stack object for this weak stack. It holds weak
   * references to the objects that are the actual contents of this stack.
   */
  Stack supportStack;
} // class BumpyStack
