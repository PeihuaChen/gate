/*
 *	InvalidRuleException.java
 *
 *	Valentin Tablan, 21.06.2000
 *
 *	$Id$
 */

package gate.creole.tokeniser;

/** Used by resources that can load rules from outside sources (such as a
  * definition file) when finding an invalid rule.
  */
public class InvalidRuleException extends TokeniserException {

  public InvalidRuleException(String s) {
    super(s);
  }

}
