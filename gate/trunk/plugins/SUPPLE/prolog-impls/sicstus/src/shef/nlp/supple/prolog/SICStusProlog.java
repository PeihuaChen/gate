package shef.nlp.supple.prolog;

import java.io.File;
import java.io.IOException;

import gate.creole.ExecutionException;

import shef.nlp.supple.utils.ProcessManager;

/**
 * Prolog wrapper for SUPPLE running on SICStus prolog (http://www.sics.se).
 */
public class SICStusProlog extends Prolog
{
  private File parserFile = null;

  /**
   * ProcessManager to handle running external sicstus processes.
   */
  private ProcessManager processManager = new ProcessManager();

  /**
   * Initialise this Prolog wrapper, passing the path to the saved state.
   *
   * @return <code>true</code> if the supplied file exists, <code>false</code>
   * otherwise.
   */
  public boolean init(File f) {
    parserFile = f;

    return (parserFile != null && parserFile.exists());
  }

  /**
   * Run the parser, taking input from and sending output to the specified
   * temporary files.
   *
   * @param in temporary file containing input for the parser
   * @param out temporary file to receive output from the parser
   * @param debugMode should we write debugging information to the console?
   */
  public void parse(File in, File out, boolean debugMode)
                  throws ExecutionException {
    boolean windows = false;
    if(System.getProperty("os.name").toLowerCase().startsWith("windows"))
    {
      windows = true;
    }

    String[] commandArgs = new String[8];

    // find the prolog executable.  The default value varies by platform
    if(windows) {
      commandArgs[0] = System.getProperty("supple.sicstus.executable", "sicstus.exe");
    }
    else {
      commandArgs[0] = System.getProperty("supple.sicstus.executable", "sicstus");
    }

    commandArgs[1] = "-m";
    commandArgs[2] = "-r";
    commandArgs[3] = parserFile.getAbsolutePath();
    commandArgs[4] = "-a";
    commandArgs[5] = "-o";
    commandArgs[6] = out.getAbsolutePath();
    commandArgs[7] = in.getAbsolutePath();

    if(debugMode) {
      System.err.println("Executing SICStus prolog with the command line:");
      System.err.println();
      for(int i = 0; i < commandArgs.length; i++) {
        System.err.println(commandArgs[i]);
      }
      System.err.println();
    }

    try {
      int exitCode = processManager.runProcess(commandArgs, debugMode);

      if(exitCode != 0) {
        String message = null;
        if(debugMode) {
          throw new ExecutionException("SICStus Prolog exit code: " + exitCode);
        }
        else {
          throw new ExecutionException("SICStus Prolog exited with error code "
              + exitCode + ".  Rerun with debug = true to see the console "
              + "output, which may help show the reasons for this error.");
        }
      }
    }
    catch(IOException e) {
      ExecutionException ee = new ExecutionException("I/O error executing "
          + commandArgs[0] + ".  If this is not the correct path to SICStus "
          + "prolog on your machine, please set the supple.sicstus.executable "
          + "system property (see the user guide).");
      ee.initCause(e);
      throw ee;
    }
  }
}
