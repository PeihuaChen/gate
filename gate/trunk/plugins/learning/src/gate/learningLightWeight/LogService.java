package gate.learningLightWeight;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class LogService {

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
