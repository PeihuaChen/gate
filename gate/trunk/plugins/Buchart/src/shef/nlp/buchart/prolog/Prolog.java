package shef.nlp.buchart.prolog;

import java.io.File;

public abstract class Prolog
{
	public boolean init(File f) { return true; }

	public abstract boolean parse(File in, File out);
}