/* 
 *  Jacl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 * 
 *  Hamish Cunningham, 14/03/00
 *
 *  $Id$
 */


package gate.util;

import java.util.*;
import tcl.lang.*;


/**
  * This class provides access to the Jacl Tcl interpreter, and
  * caters for loading any Tcl scripts that live in the GATE source.
  * It also serves as examples of how Tcl can be used from Java using
  * the Jacl library (which is my excuse for those cases where there was
  * an obvious easier way!).
  * <P>
  * Note that all GATE Tcl scripts should be in the namespace "GATE".
  */
public class Jacl
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The Tcl interpreter */
  private Interp interp;

  /** Construction */
  public Jacl() { interp = new Interp(); }

  /** Get the interpreter */
  public Interp getInterp() { return interp; }

  /** Local fashion for newlines */
  private String nl = Strings.getNl();

  /** Some Tcl code to get us into the gate/src directory (from gate
    * or a subdir).
    */
  String goToGateSrcScript =
    "set WD [pwd]                                                       "+nl+
    "if { ! [string match \"*gate*\" $WD] } {                          "+nl+
    "  error \"not in the gate directories\"                           "+nl+
    "}                                                                  "+nl+
    "while { [file tail $WD] != \"gate\" } { cd ..; set WD [pwd] }     "+nl+
    "cd src                                                             "+nl;

  /** Some Tcl code to find all the .tcl files under a directory. */
  private String findTclScript =
    "set tclFiles [list]                                                "+nl+
    "                                                                   "+nl+
    "proc filter { dir } {                                              "+nl+
    "  global tclFiles                                                  "+nl+
    "                                                                   "+nl+
    "  foreach f [glob -nocomplain ${dir}/*] {                          "+nl+
    "    if [file isdirectory $f] { filter $f }                         "+nl+
    "    if [string match {*.tcl} $f] {                                 "+nl+
    "      lappend tclFiles [string range $f 2 end]                     "+nl+
    "    }                                                              "+nl+
    "  }                                                                "+nl+
    "}                                                                  "+nl+
    "                                                                   "+nl+
    "filter {.}         ;# do the search                                "+nl+
    "return $tclFiles   ;# return the result to the interpreter         "+nl;

  /** Locate any files named .tcl in the directory hierarchy under .
    * and return a list of them.
    */
  public List findScripts()  throws TclException {
    return findScripts("");
  } // findScripts()

  /** Locate any files named .tcl in the directory hierarchy under .
    * and return a list of them. The prelimScript parameter should be
    * a non-null string containing Tcl code that will be evaluated before
    * the finder script runs (so it can be used to change directory,
    * for e.g.).
    */
  public List findScripts(String prelimScript) throws TclException {
    List scriptPaths = new ArrayList();

    String finderScript = prelimScript + findTclScript;

    // "return" in a script evaluated from Java works by throwing an
    // exception with completion code of TCL.RETURN (so using "set" to
    // return a value is easier where possible)
    try {
      interp.eval(finderScript);
    } catch(TclException e) {
      if(e.getCompletionCode() != TCL.RETURN) // wasn't a "return" exception
        throw(e);
    }

    TclObject resultObject = interp.getResult();
    TclObject pathsArray[] = TclList.getElements(interp, resultObject);
    for(int i = 0; i < pathsArray.length; i++)
      scriptPaths.add(pathsArray[i].toString());

    return scriptPaths;
  } // findScripts

  /** Copy scripts from the GATE source tree into the classes dir, so
    * that they will make it into gate.jar, and so that getResource
    * (used by Interp.evalResource) will find them.
    */
  void copyGateScripts(List scriptPaths) throws TclException {
    // tcl code to do the copy (move to GATE src dir first)
    String copyScript = goToGateSrcScript +
      "foreach f $scriptFilesToCopy {                                   "+nl+
      "  file copy -force $f ../classes/$f                              "+nl+
      "}                                                                "+nl;

    // set a variable containing the list of paths to the scripts
    TclObject tclPathsList = TclList.newInstance();
    ListIterator iter = scriptPaths.listIterator();
    while(iter.hasNext()) {
      TclObject path = TclString.newInstance((String) iter.next());
      TclList.append(interp, tclPathsList, path);
    }
    interp.setVar("scriptFilesToCopy", tclPathsList, TCL.GLOBAL_ONLY);

    // evaluate the copy script
    interp.eval(copyScript);
  } // copyGateScripts

  /** Load a list of Tcl scripts. The class loader is used to find the
    * scripts, so they must be on the class path, preferably in the same
    * code base as this class. Naming: each path in the list should be
    * the path to the script relative to the CLASSPATH. So, for e.g., if
    * you have MyJar.jar on the classpath, and it contains a script housed
    * in package x.y called z.tcl, the name should be x/y/z.tcl. (The class
    * loader can then be asked to retrieve /x/y/z.tcl and will find the
    * file in the jar.)
    */
  public void loadScripts(List scriptPaths) throws TclException {
    ListIterator iter = scriptPaths.listIterator();
    while(iter.hasNext()) {
      String path = (String) iter.next();
      String leadingSlash = ""; 

      // leading "/" on path needed by classloader
      if(! path.startsWith("/"))
        leadingSlash = "/";
      interp.evalResource(leadingSlash + path);
    }
  } // loadScripts(scriptPaths)

  /** Loads all the scripts in the GATE source. So to get a Tcl interpreter
    * that's fully initialised with all the GATE Tcl code do:
    * <PRE>
    * Jacl jacl = new Jacl();
    * jacl.loadScripts();
    * </PRE>
    */
  public void loadScripts() throws TclException {
    listGateScripts();
    loadScripts(gateScriptsList);
  } // loadScripts()

  /** Set up the gateScriptsList member. This uses the ScriptsList.tcl
    * script, which is built by "make tcl".
    */
  void listGateScripts() throws TclException {
    gateScriptsList = new ArrayList();

    interp.evalResource("/gate/util/ScriptsList.tcl");
    TclObject scriptsList = interp.getResult();

    TclObject pathsArray[] = TclList.getElements(interp, scriptsList);
    for(int i = 0; i < pathsArray.length; i++)
      gateScriptsList.add(pathsArray[i].toString());
  } // listGateScripts

  /** This is a list of all the .tcl files in the GATE source, used by
    * the loadScripts() method. 
    */
  private List gateScriptsList;

} // class Jacl






