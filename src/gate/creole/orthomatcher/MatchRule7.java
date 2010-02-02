package gate.creole.orthomatcher;

import gate.Annotation;

/**
 * RULE #6: if one name is the acronym of the other
 * e.g. "Imperial Chemical Industries" == "ICI"
 * Applied to: organisation annotations only
 */

public class MatchRule7 implements OrthoMatcherRule {

    OrthoMatcher orthmatcher;
	
	public MatchRule7(OrthoMatcher orthmatcher){
		this.orthmatcher=orthmatcher;
	}
	
	public boolean value(String s1, String s2) {
		 int i = 0;

		    //check and if the shorted string has a space in it, then it's not
		    //an acronym
		    if (s2.indexOf(" ") > 0)
		      return false;

		    // Abbreviations of one-word names are very rare and can lead to weird errors
		    if (orthmatcher.tokensLongAnnot.size() <= 1) {
		      return false;
		    }

		    //Out.prln("Acronym: Matching " + s1 + "and " + s2);
		    StringBuffer acronym_s1 = new StringBuffer("");
		    StringBuffer acronymDot_s1 = new StringBuffer("");

		    for ( ;i < orthmatcher.tokensLongAnnot.size(); i++ ) {
		      String toAppend = ( (String) ((Annotation) orthmatcher.tokensLongAnnot.get(i)
		      ).getFeatures().get(OrthoMatcher.TOKEN_STRING_FEATURE_NAME)).substring(0,1);
		      acronym_s1.append(toAppend);
		      acronymDot_s1.append(toAppend);
		      acronymDot_s1.append(".");
		    }

		    //Out.prln("Acronym dot: To Match " + acronymDot_s1 + "and " + s2);
		    //Out.prln("Result: " + matchRule1(acronymDot_s1.toString(),s2,caseSensitive));

		    if (OrthoMatcherHelper.straightCompare(acronym_s1.toString(),s2,orthmatcher.caseSensitive) ||
		    		OrthoMatcherHelper.straightCompare(acronymDot_s1.toString(),s2,orthmatcher.caseSensitive) )
		      return true;

		    return false;
	}
	
  public String getId(){
    return "MatchRule7";
  }
}
