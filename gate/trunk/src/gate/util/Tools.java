/*
 *	Tools.java
 *
 *	Valentin Tablan, Jan/2000
 *
 *	$Id$
 */

package gate.util;

import java.util.*;

public class Tools {

  public Tools() {
  }
  static long sym=0;

  /*Returns a Long wich is unique during the current run.
  * Maybe we should use serializaton in order to save the state on System.exit...
  */
  static public synchronized Long gensym(){
    return new Long(sym++);
  }

  static public synchronized Long genTime(){

    return new Long(new Date().getTime());
  }


  /**Specifies whether Gate should or shouldn't know about Unicode*/
  static public void setUnicodeEnabled(boolean value){
    unicodeEnabled = value;
  }

  /**Checks wheter Gate is Unicode enabled*/
  static public boolean isUnicodeEnabled(){
    return unicodeEnabled;
  }

  /**Does Gate know about Unicode?*/
  static private boolean unicodeEnabled = false;
}

