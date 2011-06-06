package com.ontotext.jape.automaton;

/**
 * This class provides a basic functionality used to compute hash codes in
 * closed hashes.
 * 
 * @author petar.mitankin
 * 
 */
public class CodeInt {
	public static int code(int number, int code, int hashLength) {
		code = (code * Constants.hashBase + (number & 0x000000FF)) % hashLength;
		code = (code * Constants.hashBase + ((number & 0x0000FF00) >>> 8))
				% hashLength;
		code = (code * Constants.hashBase + ((number & 0x00FF0000) >>> 16))
				% hashLength;
		code = (code * Constants.hashBase + ((number & 0xFF000000) >>> 24))
				% hashLength;
		return (code);
	}
}
