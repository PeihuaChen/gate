/*
 *	InvalidTransactionOperationException.java
 *
 *	Valentin Tablan, 21 Feb 2000
 */

package gate.util;
/**Used to signal an attempt to connect to a database in an invalid format,
  *that is a database tha does not have the right structure (see Gate2
  *documentation for details on required database structure).
  */
public class InvalidTransactionOperationException extends GateException {

  public InvalidTransactionOperationException() {
  }

  public InvalidTransactionOperationException(String s) {
    super(s);
  }

}//InvalidDatabaseException


