/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
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

////////////////////////////////////////////////////////////////////
//////////// DEVELOPERS: SEE WARNING IN JAVADOC COMMENT FOR
//////////// THIS CLASS!!!!
////////////////////////////////////////////////////////////////////

package gate.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Weak stack that allow you to bump an element to the front.
 * Objects that are only referenced by this stack will be candidates for
 * garbage collection and wil be removed from the stack as soon as the garbage
 * collector marks them for collection.
 * <P>
 * <B>*** WARNING: ***</B> the test for this class,
 * <TT>TestBumpyStack.testSelfCleaning</TT> is not a proper test; it doesn't
 * fail even when it should, and only prints a warning when DEBUG is true.
 * This is because to test it properly you need to force garbage collection,
 * and that isn't possible. So, if you work on this class <B>you must
 * turn DEBUG on on TestBumpyStack</B> in order to run the tests in a
 * meaningfull way.
 */
public class WeakBumpyStack<T> extends AbstractList<T>
{

  /**
   * Creates a new empty stack.
   */
  public WeakBumpyStack(){
    supportStack = new Stack<WeakReference<T>>();
    refQueue = new ReferenceQueue<T>();
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
  public T push(T item){
    supportStack.push(new WeakReference<T>(item,refQueue));
    return item;
  }

  /**
   * Removes the object at the top of this stack and returns that
   * object as the value of this function.
   *
   * @return     The object at the top of this stack.
   * @exception  EmptyStackException  if this stack is empty.
   */
  public synchronized T pop(){
    processQueue();
    //we need to check for null in case the top reference has just been cleared
    T res = null;
    while(res == null){
      res = supportStack.pop().get();
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
  public synchronized T peek(){
    processQueue();
    //we need to check for null in case the top reference has just been cleared
    T res = null;
    while(res == null){
      res = supportStack.peek().get();
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

    WeakReference<T> wr = supportStack.remove(itemIndex - 1);
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
   * This method should be called by every public method before realising its
   * internal logic.
   */
  protected void processQueue(){
    Reference<? extends T> wr = null;
    while ((wr = refQueue.poll()) != null) {
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
  public T get(int index) {
    processQueue();
    //we need to check for null in case the top reference has just been cleared
    T res = null;
    while(res == null){
      res = supportStack.get(index).get();
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
  public T set(int index, T element) {
    processQueue();
    WeakReference<T> ref = supportStack.set(index,
                                         new WeakReference<T>(element, refQueue));
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
  public void add(int index, T element) {
    processQueue();
    supportStack.add(index, new WeakReference<T>(element, refQueue));
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
  public T remove(int index) {
    processQueue();
    //we need to check for null in case the top reference has just been cleared
    T res = null;
    while(res == null){
      res = supportStack.remove(index).get();
    }
    return res;
  }

  ReferenceQueue<T> refQueue;

  /**
   * This is the underlying stack object for this weak stack. It holds weak
   * references to the objects that are the actual contents of this stack.
   */
  Stack<WeakReference<T>> supportStack;
} // class BumpyStack
