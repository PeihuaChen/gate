/*
 *  ParameterList.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 15/Nov/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import gate.*;
import gate.util.*;

/** Models resource parameters lists as described in their
  * <TT>creole.xml</TT> metadata. Parameters are stored as lists
  * of disjunctions (<TT>OR'd</TT> sets in the metadata).
  */
public class ParameterList
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The runtime parameters */
  protected List runtimeParameters = new ArrayList();

  /** Get the list of runtime parameters
    * (as a list of parameter disjunctions).
    */
  public List getRuntimeParameters() {
    return runtimeParameters;
  } // getRuntimeParameters()

  /** The initialisation time parameters */
  protected List initimeParameters = new ArrayList();

  /** Get the list of initialisation-time parameters
    * (as a list of parameter disjunctions).
    */
  public List getInitimeParameters() {
    return initimeParameters;
  } // getInitimeParameters()

  /** Add a parameter disjunction.
    * It is assumed that the parameters in a disjunction are all runtime
    * or all init-time, never a mix of the two.
    * @exception NoSuchElementException disjunction has no more elements.
    */
  public boolean add(List disjunction) {
    boolean status = false;
    Iterator iter = disjunction.iterator();
    Parameter param = (Parameter) iter.next();

    if(param.isRuntime()) {
      status = runtimeParameters.add(disjunction);
    } else {
      status = initimeParameters.add(disjunction);
    }

    return status;
  } // add(param)

  /** Add all the members of a parameter list (as individual disjunctions) */
  public boolean addAll(List c) {
    boolean status = false;
    Iterator iter = c.iterator();
    while(iter.hasNext()) {
      List disj = new ArrayList();
      Parameter param = (Parameter) iter.next();
      disj.add(param);
      status = add(disj);
    }

    return status;
  } // addAll(Collection)

  /** Get default runtime parameter value set.
    * Calls <TT>getDefaults(List)</TT>.
    * @see #getDefaults(List)
    */
  public FeatureMap getRuntimeDefaults() throws ParameterException {
    return getDefaults(runtimeParameters);
  } // getRuntimeDefaults()

  /** Get default initime parameter value set.
    * Calls <TT>getDefaults(List)</TT>.
    * @see #getDefaults(List)
    */
  public FeatureMap getInitimeDefaults() throws ParameterException {
    return getDefaults(initimeParameters);
  } // getInitimeDefaults()

  /** Get default parameter value set. Where more than one default
    * is possible amongst disjunctive parameters, only the first will be set.
    * To check if the default set is comprehensive,
    * use <TT>isFullyDefaulted()</TT>.
    * @see #isFullyDefaulted()
    */
  public FeatureMap getDefaults(List parameters) throws ParameterException {
    FeatureMap defaults = Factory.newFeatureMap();

    // each element of the parameters list is a list of (disjunctive) params
    Iterator disjIter = parameters.iterator();

    // for each parameter disjunction in parameters
    disjIterLoop:
    while(disjIter.hasNext()) {
      boolean optional = false; // were any of this disj optional?

      // get an iterator for this disjunction of parameters
      List paramDisj = (List) disjIter.next();
      Iterator paramsIter = paramDisj.iterator();

      // for each parameter in the disjunction
      while(paramsIter.hasNext()) {
        Parameter param = (Parameter) paramsIter.next();
        if(DEBUG) Out.prln("Examining " + param);
        if(!optional)
          optional = param.isOptional();

        // try and find a default value
        Object defaultValue = param.calculateDefaultValue();

        // no default found
        if(defaultValue == null) {
          // if none of this disj were optional, and we're the last, then
          // we've got at least one non-optional param unset
          if(!optional && !paramsIter.hasNext()) {
            fullyDefaulted = false;
          }

        // valid default found - set it and continue with the next disj
        } else {
          defaults.put(param.getName(), defaultValue);
          continue disjIterLoop;
        }
      } // paramsIter

    } // disjIter

    return defaults;
  } // getDefaults()


// this stuff is for if we decide to do more thorough checking
// of parameterisation in Factory.createResource... but at present
// the gui checks pretty well so...
//
//  /** Analyse a parameter-value feature map and return a list
//    * of init time parameter disjunctions that are unset.
//    */
//  public List getUnsetInitimeParameters(FeatureMap paramValues) {
//    List unsetDisj = new ArrayList();
//
//    // for each init disj,
//    //   for each param,
//    //     if there's no value in paramValues
//    //       add this disj to rtnValue,
//    //       continue disjLoop
//  } // getUnsetInitimeParameters(paramValues)
//
//  /** Analyse a parameter-value feature map and return a list
//    * of runtime parameter disjunctions that are unset.
//    */
//  public List getUnsetRuntimeParameters(FeatureMap paramValues) {
//  } // getUnsetRuntimeParameters(paramValues)
//
//  public List getUnsetInitimeParameters(
//    FeatureMap paramValues, boolean includeOptional
//  ) {
//  } // getUnsetInitimeParameters(paramValues, includeOptional)
//
//  public List getUnsetRuntimeParameters(
//    FeatureMap paramValues, boolean includeOptional
//  ) {
//  } // getUnsetRuntimeParameters(paramValues, includeOptional)
//

  /** Status of the last run of <TT>getDefaults(List)</TT>. */
  protected boolean fullyDefaulted = false;

  /** Get the status of the last run of <TT>getDefaults(List)</TT>.
    * If the last run managed to find a default for all parameters
    * that are part of a disjunction of which none are optional, then
    * this status is true; else it is false.
    * @see #getDefaults(List)
    */
  public boolean isFullyDefaulted() { return fullyDefaulted; }


  /** String representation */
  public String toString() {
    StringBuffer s = new StringBuffer(Strings.getNl() + "  ParameterList:");

    Iterator iter = getRuntimeParameters().iterator();
    if(iter.hasNext()) s.append(Strings.getNl() + "  runtime params=");
    while(iter.hasNext()) {
      s.append(Strings.getNl() + "    ");
      List paramDisj = (List) iter.next();
      Iterator iter2 = paramDisj.iterator();

      while(iter2.hasNext())
        s.append( (Parameter) iter2.next() + Strings.getNl() + "    " );
    }

    iter = getInitimeParameters().iterator();
    if(iter.hasNext()) s.append(Strings.getNl() + "  initime params=");
    while(iter.hasNext()) {
      s.append(Strings.getNl() + "    ");
      List paramDisj = (List) iter.next();
      Iterator iter2 = paramDisj.iterator();

      while(iter2.hasNext())
        s.append( (Parameter) iter2.next() + Strings.getNl() + "    " );
    }

    return s.toString();
  } // toString()

} // class ParameterList


