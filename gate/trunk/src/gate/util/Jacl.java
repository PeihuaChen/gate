/* 
 * Jacl.java
 *
 * Hamish Cunningham, 14/03/00
 *
 * $IdC$
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
  *
  * <P>
  * Warning: main, and the listGateScripts method,
  * do something really wierd - don't mess if you don't understand! (Ok, not
  * a good idea for a class to rewrite itself, in general, but this was
  * all I had to amuse myself with today apart from a bottle of
  * antibiotics....) 
  */
public class Jacl
{
  /** Main routine. */
  public static void main(String[] args) throws TclException {
    // construct a Tcl interpreter
    Jacl jacl = new Jacl();

    // refresh Jacl.java's list of GATE scripts
    System.out.println("Updating Jacl.java....");
    jacl.listGateScripts();

    // tell the world
    System.out.println("...done");
  } // main


  /** The Tcl interpreter */
  private Interp interp;

  /** Construction */
  public Jacl() { interp = new Interp(); }

  /** Get the interpreter */
  public Interp getInterp() { return interp; }

  /** Local fashion for newlines */
  private String nl = Strings.getNl();

  /** Some Tcl code to get us into the gate2/src directory (from gate2
    * or a subdir).
    */
  String goToGateSrcScript =
    "set WD [pwd]                                                       "+nl+
    "if { ! [string match \"*gate2*\" $WD] } {                          "+nl+
    "  error \"not in the gate2 directories\"                           "+nl+
    "}                                                                  "+nl+
    "while { [file tail $WD] != \"gate2\" } { cd ..; set WD [pwd] }     "+nl+
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
    * file in the jar.
    */
  public void loadScripts(List scriptPaths) throws TclException {
    ListIterator iter = scriptPaths.listIterator();
    while(iter.hasNext()) {
      String path = (String) iter.next();
      interp.evalResource("/" + path); // leading "/" needed by classloader
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
    loadScripts(Arrays.asList(gateScriptsList));
  } // loadScripts()

  /** Regenerate the gateScriptsList member. */
  void listGateScripts() throws TclException {
    List scriptPaths = findScripts(goToGateSrcScript);
    StringBuffer editJaclScript = new StringBuffer();

    editJaclScript.append(
      "cd gate/util                                                       "+nl+
      "set jaclFile [open Jacl.java r]                                    "+nl+
      "set newJaclFile [open NewJacl.java w]                              "+nl+
      "set scriptListFlagPattern "                                         +
      "  \"*// AUTOGENERATED GATE SCRIPTS LIST - DO NOT EDIT!!!\"         "+nl+
      "set scriptListEndFlagPattern "                                      +
      "  \"*// END OF AUTOGENERATED GATE SCRIPTS LIST - DO NOT EDIT!!!\"  "+nl+
      "                                                                   "+nl+
      "while { ! [eof $jaclFile] } {                                      "+nl+
      "  set line [gets $jaclFile]                                        "+nl+
      "                                                                   "+nl+
      "  puts $newJaclFile $line                                          "+nl+
      "                                                                   "+nl+
      "  if [string match $scriptListFlagPattern \"$line\"] {             "+nl
    );

    Iterator iter = scriptPaths.iterator();
    String commaOrNothing = "";
    while(iter.hasNext()) {
      String path = (String) iter.next();
      editJaclScript.append(
        "    puts $newJaclFile "  + "\"    " + commaOrNothing +
        "\\\"" + path + "\\\"\"" + nl
      );
      commaOrNothing = ", ";
    }

    editJaclScript.append(
      "                                                                   "+nl+
      "  # read past the existing list                                    "+nl+
      "  set line \"\"                                                    "+nl+
      "  while { ! [string match $scriptListEndFlagPattern \"$line\"] } { "+nl+
      "    set line [gets $jaclFile]                                      "+nl+
      "  }                                                                "+nl+
      "  puts $newJaclFile "                                               +
      "  \"    // END OF AUTOGENERATED GATE SCRIPTS LIST - DO NOT EDIT!!!\" "
      +nl+
      "                                                                   "+nl+
      "  }                                                                "+nl+
      "}                                                                  "+nl+
      "                                                                   "+nl+
      "close $jaclFile                                                    "+nl+
      "close $newJaclFile                                                 "+nl+
      "                                                                   "+nl+
      "# file rename on its own didn't work, so delete first...           "+nl+
      "file delete -force Jacl.java                                       "+nl+
      "file rename -force NewJacl.java Jacl.java                          "+nl
    );
    
    interp.eval(goToGateSrcScript + editJaclScript.toString());
  } // listGateScripts

  /** This is a list of all the .tcl files in the GATE source, used by
    * the loadScripts() method. The list is AUTOGENERATED, so DON'T
    * EDIT IT!!!! The generation is done by the listGateScripts method.
    */
  private String[] gateScriptsList = {
    // AUTOGENERATED GATE SCRIPTS LIST - DO NOT EDIT!!!
    "gate/util/FindScripts.tcl"
    // END OF AUTOGENERATED GATE SCRIPTS LIST - DO NOT EDIT!!!
  };

} // class Jacl







