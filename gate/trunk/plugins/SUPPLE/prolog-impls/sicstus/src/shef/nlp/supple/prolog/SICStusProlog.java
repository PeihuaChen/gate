package shef.nlp.supple.prolog;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class SICStusProlog extends Prolog
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

		String[] commandArgs = new String[8];
		if(windows)
		{
			commandArgs[0] = System.getProperty("supple.sicstus.executable", "sicstus.exe");
		}
		else
		{
			commandArgs[0] = System.getProperty("supple.sicstus.executable", "sicstus");
		}

		commandArgs[1] = "-m";
		commandArgs[2] = "-r";
		commandArgs[3] = parserFile.getAbsolutePath();
		commandArgs[4] = "-a";
		commandArgs[5] = "-o";
		commandArgs[6] = out.getAbsolutePath();
		commandArgs[7] = in.getAbsolutePath();

		Runtime javaRuntime = Runtime.getRuntime();
		Process sicstusProcess = null;
		try
		{
			sicstusProcess = javaRuntime.exec(commandArgs);

			BufferedReader bin = new BufferedReader(new InputStreamReader(sicstusProcess.getErrorStream()));

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
