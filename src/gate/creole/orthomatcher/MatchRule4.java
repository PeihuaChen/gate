package gate.creole.orthomatcher;

import gate.Annotation;

import java.util.Iterator;

/**
 * RULE #4: Does the first non-punctuation token from the long string match
 * the first token from the short string?
 * e.g. "fred jones" == "fred"
 * Condition(s): case-insensitive match
 * Applied to: person annotations
 *
 * Modified by Andrew Borthwick, Spock Networks:  Disallow stop words
 */
public class MatchRule4 implements OrthoMatcherRule {
	
	OrthoMatcher orthmatcher;
	
	public MatchRule4(OrthoMatcher orthmatcher){
		this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		boolean allTokensMatch = true;
	    // Out.prln("MR4:  Matching" + s1 + " with " + s2);

	    Iterator tokensLongAnnotIter = orthmatcher.tokensLongAnnot.iterator();
	    Iterator tokensShortAnnotIter = orthmatcher.tokensShortAnnot.iterator();
	    while (tokensLongAnnotIter.hasNext() && tokensShortAnnotIter.hasNext()) {
	      Annotation token = (Annotation) tokensLongAnnotIter.next();
	      if (((String)token.getFeatures().get(orthmatcher.TOKEN_KIND_FEATURE_NAME)).equals(OrthoMatcher.PUNCTUATION_VALUE) ||
	              token.getFeatures().containsKey("ortho_stop"))
	        continue;
	      if (! ((String)(((Annotation) tokensShortAnnotIter.next()).
	              getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME))).equals(
	                      (String) token.getFeatures().get(orthmatcher.TOKEN_STRING_FEATURE_NAME))) {
	        allTokensMatch = false;
	        break;
	      } // if (!tokensLongAnnot.nextToken()
	    } // while
	//  if (allTokensMatch)
	//  Out.prln("rule4 fired. result is: " + allTokensMatch);
	    /*if (allTokensMatch && log.isDebugEnabled())
	      log.debug("rule4 matched " + s1 + "(id: " + OrthoMatcher.longAnnot.getId() + ") to " + s2+ "(id: " + OrthoMatcher.shortAnnot.getId() + ")");
	      */
	    return allTokensMatch;
	}
	
  public String getId(){
    return "MatchRule4";
  }
}
