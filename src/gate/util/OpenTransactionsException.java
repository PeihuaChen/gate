/*
 *	OpenTransactionsException.java
 *
 *	Valentin Tablan, 21 Feb 2000
 */

package gate.util;
/**Used to signal an attempt to close all connections to a database while there
  *are still connections in use by the clients of that database.
  */
public class OpenTransactionsException extends GateException {

  public OpenTransactionsException() {
  }

  public OpenTransactionsException(String s) {
    super(s);
  }

}//OpenTransactionsException


