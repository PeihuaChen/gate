package shef.nlp.supple.prolog;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class SWIProlog extends Prolog
{
	private File parserFile = null;

	public boolean init(File f)
	{
		parserFile = f;

		return (parserFile != null && parserFile.exists());
	}

	public boolean parse(File in, File out)
	{
		boolean windows = false;
		if(System.getProperty("os.name").toLowerCase().startsWith("windows"))
		{
			windows = true;
		}

		String[] commandArgs;
		commandArgs = new String[7];

		if(windows) {
			commandArgs[0] = System.getProperty("supple.swi.executable", "plcon.exe");
		}
		else {
			commandArgs[0] = System.getProperty("supple.swi.executable", "swipl");
		}
		commandArgs[1] = "-x";
		commandArgs[2] = parserFile.getAbsolutePath();
		commandArgs[3] = "--";

		// add command arguments
		commandArgs[4] = "-o";
		commandArgs[5] = out.getAbsolutePath();
		commandArgs[6] = in.getAbsolutePath();

		Runtime javaRuntime = Runtime.getRuntime();
		Process swiProcess = null;
		try
		{
			swiProcess = javaRuntime.exec(commandArgs);

			BufferedReader bin = new BufferedReader(new InputStreamReader(swiProcess.getErrorStream()));

			String line = bin.readLine();

			while (line != null)
			{
				line = bin.readLine();
			}

			return true;
		}
		catch(Exception e) {}

		return false;

	}
}
