//Title:        Compiler.java - compile .jape files.
//Version:      $Id$
//Copyright:    Copyright (c) 1998
//Author:       Hamish Cunningham
//Company:      NLP Group, DCS, Univ. of Sheffield

package gate.jape;

import java.io.*;
import java.util.*;
import com.objectspace.jgl.*;

import gate.util.*;
import gate.annotation.*;
import gate.jape.parser.*;

/**
  * Compiler for JAPE files.
  */
public class Compiler {

  /** How much noise to make. */
  static private boolean verbose = false;

  /** Take a list of .jape files names and compile them to .ser.
    * Also recognises a -v option which makes it chatty.
    */
  static public void main(String[] args) {

    // process options
    int argsIndex = 0;
    while(args[argsIndex].toCharArray()[0] == '-')
      if(args[argsIndex++].equals("-v"))
        verbose = true;

    // construct list of the files
    Array fileNames = new Array();
    for( ; argsIndex<args.length; argsIndex++)
      fileNames.add(args[argsIndex]);

    // compile the files
    compile(fileNames);

    message("done");
  } // main

  /** The main compile method, taking a file name. */
  static public void compile(String japeFileName) {
    // parse
    message("parsing " + japeFileName);
    Transducer transducer = null;
    try {
      transducer = parseJape(japeFileName);
    } catch(JapeException e) {
      emessage("couldn't compile " + japeFileName + ": " + e);
      return;
    }

    // save
    message("saving " + japeFileName);
    try {
      saveJape(japeFileName, transducer);
    } catch (JapeException e) {
      emessage("couldn't save " + japeFileName + ": " + e);
    }

    message("finished " + japeFileName);
  } // compile(String japeFileName)

  /** The main compile method, taking a list of file names. */
  static public void compile(Array fileNames) {
    // for each file, compile and save
    for(ArrayIterator i = fileNames.begin(); ! i.atEnd(); i.advance())
      compile((String) i.get());
  } // compile

  /** Parse a .jape and return a transducer, or throw exception. */
  static public Transducer parseJape(String japeFileName)
  throws JapeException {
    Transducer transducer = null;

    try {
      ParseCpsl cpslParser = new ParseCpsl(japeFileName);
      transducer = cpslParser.MultiPhaseTransducer();
    } catch(gate.jape.parser.ParseException e) {
      throw(new JapeException(e.toString()));
    } catch(IOException e) {
      throw(new JapeException(e.toString()));
    }

    return transducer;
  } // parseJape

  /** Save a .jape, or throw exception. */
  static public void saveJape(String japeFileName, Transducer transducer)
  throws JapeException {
    String saveName = japeNameToSaveName(japeFileName);

    try {
      FileOutputStream fos = new FileOutputStream(saveName);
      ObjectOutputStream oos = new ObjectOutputStream (fos);
      oos.writeObject(transducer);
      oos.close();
    } catch (IOException e) {
      throw(new JapeException(e.toString()));
    }
  } // saveJape

  /** Convert a .jape file name to a .ser file name. */
  static String japeNameToSaveName(String japeFileName) {
    String base = japeFileName;
    if(japeFileName.endsWith(".jape") || japeFileName.endsWith(".JAPE"))
      base = japeFileName.substring(0, japeFileName.length() - 5);
    return base + ".ser";
  } // japeNameToSaveName

  /** Hello? Anybody there?? */
  public static void message(String mess) {
    if(verbose) System.out.println("JAPE compiler: " + mess);
  } // message

  /** Ooops. */
  public static void emessage(String mess) {
    System.err.println("JAPE compiler error: " + mess);
  } // emessage


} // class Compiler


// $Log$
// Revision 1.1  2000/02/23 13:46:04  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:01  hamish
// added gate2
//
// Revision 1.3  1998/10/29 12:07:27  hamish
// added compile method taking a file name
//
// Revision 1.2  1998/09/21 16:19:27  hamish
// don't catch *all* exceptions!
//
// Revision 1.1  1998/09/18 15:07:41  hamish
// a functioning compiler in two shakes of a rats tail

