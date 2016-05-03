/*
 *  DctParsingFormat.java
 *
 * Copyright (c) 2016, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 3, 29 June 2007.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  jstroetge, 14/3/2016 (jannik.stroetgen@gmail.com)
 *
 * For details on the configuration options, see the user guide:
 * http://gate.ac.uk/cgi-bin/userguide/sec:creole-model:config
 */

package de.mpii.nlp.gate.heideltime;

/**
 * Hardcoded DCT Format information for use with DCTParser.
 * 
 * @author Jannik Strötgen
 */
public enum DctParsingFormat {
	TIMEML ("timeml"),
	MANUALDATE ("manualdate"),
	;

	private String formatName;
	
	DctParsingFormat(String formatName) {
		this.formatName = formatName;
	}
	
	/*
	 * getter
	 */
	
	public final String getName() {
		return this.formatName;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}


