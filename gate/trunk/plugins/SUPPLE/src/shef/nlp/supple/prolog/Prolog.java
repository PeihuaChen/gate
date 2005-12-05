package shef.nlp.supple.prolog;

import java.io.File;

/**
 * This abstract class allows for the easy changing of the
 * Prolog implementation being used to host the parser.
 * If you wish to use a different Prolog implementation
 * than those we support simply extend this class so as to
 * communicate with the Prolog system of your choice.
 **/
public abstract class Prolog
{
	/**
	 * Allows for the parser to be initialized from a file.
	 * This is called by GATE passing the user specified file.
	 * The file could by the compiled parser or a config file
	 * which you then use to find the parser code etc. Really
	 * the choice of how you init Prolog and load the parser
	 * is up to you.
	 * @param f the parser file used to init the parser.
	 * @return true of the parser is correctly initialised,
	 *         false otherwise
	 **/
	public boolean init(File f) { return true; }

	/**
	 * This method should simply pass the input file to the
	 * parser, parse the text storring the result in the
	 * output file.
	 * @param in the file containing the initial chart which
	 *        the parser will run over.
	 * @param out the file containing the output of the parser.
	 * @return true if the parser succedded (the out file should
	 *         be valid), false otherwise.
	 **/
	public abstract boolean parse(File in, File out);
}