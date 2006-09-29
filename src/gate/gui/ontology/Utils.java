// Decompiled by DJ v3.9.9.91 Copyright 2005 Atanas Neshkov  Date: 19/09/2006 10:23:15
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Utils.java

package gate.gui.ontology;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public Utils() {
	}


	public static boolean isValidNameSpace(String s) {
		String s1 = new String(
				"[a-zA-Z]+(:)(/)+[a-zA-Z0-9]+((\\.)[a-zA-Z0-9]+)+((/)[a-zA-Z0-9]+)*(#)");
		Pattern pattern = Pattern.compile(s1);
		return pattern.matcher(s).matches();
	}


	public static boolean isValidOntologyResourceName(String s) {
		String s1 = new String("[a-zA-Z0-9_-]+");
		Pattern pattern = Pattern.compile(s1);
		return pattern.matcher(s).matches();
	}
}
