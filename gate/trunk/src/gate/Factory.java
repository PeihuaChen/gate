/*
 *	Factory.java
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
