/*
 *	InvalidRuleException.java
 *
 *	Valentin Tablan, 21.06.2000
 *
 *	$Id$
 */

package gate.creole.tokeniser;

/**Fired when an invalid tokeniser rule is found
  */
public class InvalidRuleException extends TokeniserException {

  public InvalidRuleException(String s) {
    super(s);
  }

}
