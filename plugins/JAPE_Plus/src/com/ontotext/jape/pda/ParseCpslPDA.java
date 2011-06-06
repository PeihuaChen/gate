package com.ontotext.jape.pda;

import gate.jape.parser.ParseCpsl;
import gate.jape.parser.ParseCpslTokenManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;

public class ParseCpslPDA extends ParseCpsl {

	private static final long serialVersionUID = 1404057872006325930L;

	public ParseCpslPDA(InputStream stream) {
		super(stream);
	}

	public ParseCpslPDA(InputStream stream, String encoding) {
		super(stream, encoding);
	}

	public ParseCpslPDA(ParseCpslTokenManager tm) {
		super(tm);
	}

	public ParseCpslPDA(Reader stream) {
		super(stream);
	}

	public ParseCpslPDA(URL url, String encoding) throws IOException {
		super(url, encoding);
	}

	public ParseCpslPDA(Reader stream, HashMap existingMacros) {
		super(stream, existingMacros);
	}

	public ParseCpslPDA(URL url, String encoding, HashMap existingMacros) throws IOException {
		super(url, encoding, existingMacros);
	}

	@Override
	protected SinglePhaseTransducerPDA createSinglePhaseTransducer(String name) {
		return new SinglePhaseTransducerPDA(name);
	}

	@Override
	protected ParseCpslPDA spawn(URL sptURL) throws IOException {
		return new ParseCpslPDA(sptURL, encoding, macrosMap);
	}
}
