/*
 * Key.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan, October 2000
 *
 * $Id$
 */

package guk.im;

/**
 * Title:        Gate Unicode Kit
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:
 * @author
 * @version
 */
import java.awt.event.*;

/** 
 * This calls describes a keyboard key.
 * A key is defined by one character and modifiers (CTRL or ALT or both).
 * 
 */
public class Key {
  /**    */
  public Key(char keyChar, int modifiers){
    this.keyChar = keyChar;
    this.modifiers = modifiers;
  }

  /**    */
  public int hashCode(){
    return (int)keyChar;
  }

  /**    */
  public boolean equals(Object o){
    if(o instanceof Key){
      Key other = (Key)o;
      return keyChar == other.keyChar &&
             (modifiers & KeyEvent.ALT_MASK) ==
                (other.modifiers & KeyEvent.ALT_MASK) &&
             (modifiers & KeyEvent.CTRL_MASK) ==
                (other.modifiers & KeyEvent.CTRL_MASK);
    }else return false;
  }

  /**    */
  char keyChar;
  /**    */
  int modifiers;
}//class Key
