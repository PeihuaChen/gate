package shef.nlp.buchart.prolog;

import java.io.File;
import java.io.BufferedReader;
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
		boolean win9x = false;
		if(System.getProperty("os.name").toLowerCase().startsWith("windows"))
		{
			windows = true;

			if (System.getProperty("os.name").toLowerCase().indexOf("9") != -1)
				win9x = true;
		}

		String[] commandArgs = new String[3];
		String command;
		if(windows)
		{
			if (win9x)
				commandArgs[0] = "command";
			else
				commandArgs[0] = "cmd";

			commandArgs[1] = "/c";
			command = "\"plcon.exe -x ";
		}
		else
		{
			commandArgs[0] = "/bin/sh";
			commandArgs[1] = "-c";
			command = "plcon -x ";
		}

		command += parserFile+" -- ";
		command += " -o ";
		command += out.getAbsolutePath()+ " ";
		command += in.getAbsolutePath();

		if (windows)
			command += " \"";

		commandArgs[2]=command;

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