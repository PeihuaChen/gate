/*
 *	LuckyException.java
 *
 *	Valentin Tablan 06/2000
 *
 *	$Id$
 */
package gate.util;

/**This exception is intende to be used in places where there definitely
  *shouldn't be any exceptions thrown but the API requires us to catch some,
  *eg: <code>
  * try{
  *   if( a != null){
  *     a.doSomething();
  *   }
  * }catch(NullPointerException npe){
  *   throw new LuckyException("I found a null pointer!");
  * }
  *</code>
  *Of course thew system will never require you to catch NullPOinterException as
  *it derives from RuntimeException, but I couldn't come with a better example.
  */
public class LuckyException extends RuntimeException {

  /**Default constructor, creates a new execption with the default message*/
  public LuckyException() {
    super(defaultMessage);
  }

  /**Creates a new exception with the provided message prepended to the default
    *one on a separate line.
    *@param message the uses message
    */
  public LuckyException(String message) {
    super(message + "\n" + defaultMessage);
  }

  /**The default message carried by this type of exceptions*/ 
  static String defaultMessage =
    "Congratulations, you found the ONLY bug in GATE!";
} 