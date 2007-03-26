/*
 *  LogService.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: LogService.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learningLightWeight;

import java.io.PrintWriter;
/**
 *  Write the learning information into a log file, 
 *  which name is specified in ConstantParameters.java.
 *  Also including a static variable debug determining the
 *  level of output of the ML Api.
 */
public class LogService {
  /** Determine if or not printing the information. 
   * 0 -- no debug information; 
   * 1 -- usual information output;
   * 2 -- all the information output, including some 
   *     warning information.
   */
  public static short debug = 0; 
  /** The PrintStream to write the messages to. */
  private static PrintWriter logFileIn = new PrintWriter(System.err);;
  /** The minimal verbosity level. Message below this level are ignored. */
  private static int minVerbosityLevel;
  /** The last printed message. */
  private static String lastMessage;
  /** Counts how often a message was repeated. */
  private static int equalMessageCount;
  // private static File logFile = null;
  // Public void LogService(String logFileName) {
  // logFileIn = logFileIn = new PrintWriter(new FileWriter(logFile,
  // true));
  // }
}
