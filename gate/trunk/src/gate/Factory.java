/*
 *	Factory.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *  
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *  
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *  
 *	Hamish Cunningham, 25/May/2000
 *
 *	$Id$
 */

package gate;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.corpora.*;
import gate.util.*;
import gate.annotation.*;

/** Provides static methods for the creation of Resources. 
  * <B>NOTE:</B> these methods should be abstract (or, even better, this 
  * should be an interface). The method implementations given here make
  * no sense. Unfortunately, Java has no way to implement abstract
  * static methods; interface methods can't be static; static class methods
  * can't be abstract.
  */
public abstract class Factory
{
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** Create a new LanguageResource. */
  public static LanguageResource newLR(Class LRClass, Object[] args) {
    throw new RuntimeException();
  }

  /** Create a new Corpus. */
  public static Corpus newCorpus(String name) {
    throw new RuntimeException();
  }

  /** Create a new Document from a URL. */
  public static Document newDocument(URL u) throws IOException { 
    throw new RuntimeException();
  }

  /** Create a new Document from a String. */
  public static Document newDocument(String s) throws IOException { 
    throw new RuntimeException();
  }

  /** Create a new FeatureMap. */
  public static FeatureMap newFeatureMap() { 
    throw new RuntimeException();
  }

} // abstract Factory