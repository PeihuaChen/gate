/* 
	PrioritisedRuleList.java - transducer class

	Hamish Cunningham, 27/07/98

	$Id$
*/


package gate.jape;

import java.util.*;
import com.objectspace.jgl.*;


/**
  * A list of rules ordered according to priority. May be used for ordering
  * non-matched rules (in which case the order is based on
  * priority/position), or matched rules (in which case the order is based
  * on matched lenght/priority/position). Note that position represents
  * the idea of order within an input file; it is assumed that this is the
  * same as the order of addition of the rules to the list, i.e. a rule
  * added 5th is assumed to occupy 5th place in the file (or other rule
  * source). This class is based on JGL's DList, which allows for fast
  * insertion of elements at any point. The highest priority rule is the
  * first in the list, which may be accessed by <CODE>front()</CODE>.
  */
public class PrioritisedRuleList extends DList implements java.io.Serializable
{

  /** Adds a rule in order. Used for non-matched rules. Implements the
    * ordering based on priority/position.
    */
  public synchronized void add(Rule newRule) {
    /* for each rule,
     *   if it is higher priority, continue;
     *   else if it is same priority
     *     if it is higher position, continue;
     *     else break
     *   else (it is lower priority) break
     * insert newRule before current position (which may be finish)
     */
    DListIterator iterator = begin();
    for(  ; ! iterator.atEnd(); iterator.advance()) {
      Rule rule	=	(Rule) iterator.get();
      int rulePriority =	rule.getPriority();
      int newRulePriority =	newRule.getPriority();
      int rulePosition =	rule.getPosition();
      int newRulePosition =	newRule.getPosition();

      if(rulePriority > newRulePriority)
	      continue;
      else if(rulePriority == newRulePriority) {
        if(rulePosition < newRulePosition)
          continue;
        else
          break;
      } else {
        break;
      }

    } // while not hit the end of the rules

    insert(iterator, newRule);
  } // add(Rule)

  /** Adds a rule in order. Used for matched rules. Implements the
    * ordering based on length/priority/position. Length is given as
    * a parameter.
    */
  public synchronized void add(Rule newRule, int newRuleLength) {
    /* for each rule,
     *   if it is longer than the new one, continue;
     *   else if it is the same length
     *     if it is higher priority, continue;
     *     else if it is same priority
     *       if it is higher position, continue;
     *       else break;
     *     else (it is lower priority) break;
     *   else (it is shorter) break;
     * insert newRule before current position (which may be finish)
     */
    DListIterator iterator = begin();
    for(  ; ! iterator.atEnd(); iterator.advance()) {
      Rule rule	=	(Rule) iterator.get();
      int rulePriority =	rule.getPriority();
      int newRulePriority =	newRule.getPriority();
      int rulePosition =	rule.getPosition();
      int newRulePosition =	newRule.getPosition();
      int ruleLength = rule.getEndPosition() - rule.getStartPosition();

      if(ruleLength > newRuleLength)
        continue;
      else if(ruleLength == newRuleLength) {
        if(rulePriority > newRulePriority)
          continue;
        else if(rulePriority == newRulePriority) {
          if(rulePosition < newRulePosition)
            continue;
          else
            break;
        } else {
          break;
        }
      } else {
        break;
      }

    } // while not hit the end of the rules

    insert(iterator, newRule);
  } // add(Rule,int)

} // class PrioritisedRuleList


// $Log$
// Revision 1.1  2000/02/23 13:46:10  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:02  hamish
// added gate2
//
// Revision 1.6  1998/10/01 16:06:34  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.5  1998/09/17 10:24:02  hamish
// added options support, and Appelt-style rule application
//
// Revision 1.4  1998/08/12 19:05:47  hamish
// fixed multi-part CG bug; set reset to real reset and fixed multi-doc bug
//
// Revision 1.3  1998/07/30 11:05:24  mks
// more jape
//
// Revision 1.2  1998/07/29 11:07:08  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives
