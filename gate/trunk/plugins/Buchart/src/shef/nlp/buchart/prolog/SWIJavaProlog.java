package shef.nlp.buchart.prolog;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import jpl.*;

public class SWIJavaProlog extends Prolog
{
	private File parserFile = null;

	public boolean init(File f)
	{
		parserFile = f;

		if (parserFile != null && parserFile.exists())
		{
			String[] args = new String[]{"pl","-x",parserFile.getAbsolutePath(),"-g","true"};

			JPL.init(args);

			return JPL.getActualInitArgs() != null;
		}

		return false;

	}

	public boolean parse(File in, File out)
	{
		String oFile = out.getAbsolutePath();
		String iFile = in.getAbsolutePath();

		if(System.getProperty("os.name").toLowerCase().startsWith("windows"))
		{
			oFile = oFile.replace('\\','/');
			iFile = iFile.replace('\\','/');
		}

		Query query = new Query("parse(['-o','"+oFile+"','"+iFile+"'])");

		return query.hasSolution();
	}
}