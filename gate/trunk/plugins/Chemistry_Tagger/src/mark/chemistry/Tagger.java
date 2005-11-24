/* **********************************************************************
 *         Chemistry Tagger - A GATE Processing Resource                *
 *         Copyright (C) 2004 The University of Sheffield               *
 *       Developed by Mark Greenwood <m.greenwood@dcs.shef.ac.uk>       *
 *       Modifications by Ian Roberts <i.roberts@dcs.shef.ac.uk>        *
 *                                                                      *
 * This program is free software; you can redistribute it and/or modify *
 * it under the terms of the GNU Lesser General Public License as       *
 * published by the Free Software Foundation; either version 2.1 of the *
 * License, or (at your option) any later version.                      *
 *                                                                      *
 * This program is distributed in the hope that it will be useful,      *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of       *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
 * GNU General Public License for more details.                         *
 *                                                                      *
 * You should have received a copy of the GNU Lesser General Public     *
 * License along with this program; if not, write to the Free Software  *
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.            *
 ************************************************************************/

package mark.chemistry;

import java.io.*;
import java.net.*;

import gate.*;
import gate.creole.*;

/**
 * A tagger for chemical elements and compounds.
 */
public class Tagger extends AbstractLanguageAnalyser implements ProcessingResource, Serializable
{
  private LanguageAnalyser gazc = null;
  private LanguageAnalyser gazo = null;
  private LanguageAnalyser net = null;

  //// Init parameters ////
  
  /**
   * The URL of the gazetteer lists definition for spotting elements as part of
   * compounds.
   */
  private URL compoundListsURL;

  public void setCompoundListsURL(URL newValue) {
    compoundListsURL = newValue;
  }

  public URL getCompoundListsURL() {
    return compoundListsURL;
  }

  /**
   * The URL of the gazetteer lists definition for spotting elements on their
   * own.
   */
  private URL elementListsURL;

  public void setElementListsURL(URL newValue) {
    elementListsURL = newValue;
  }

  public URL getElementListsURL() {
    return elementListsURL;
  }

  /**
   * URL of the JAPE grammar.
   */
  private URL transducerGrammarURL;

  public void setTransducerGrammarURL(URL newValue) {
    transducerGrammarURL = newValue;
  }
  
  public URL getTransducerGrammarURL() {
    return transducerGrammarURL;
  }

  /**
   * Create the tagger by creating the various gazetteers and JAPE transducers
   * it uses.
   */
  public Resource init() throws ResourceInstantiationException
  {
    // sanity check parameters
    if(compoundListsURL == null) {
      throw new ResourceInstantiationException(
          "Compound lists URL must be specified");
    }
    
    if(elementListsURL == null) {
      throw new ResourceInstantiationException(
          "Element lists URL must be specified");
    }

    if(transducerGrammarURL == null) {
      throw new ResourceInstantiationException(
          "Transducer grammar URL must be specified");
    }

    FeatureMap hidden = Factory.newFeatureMap();
    Gate.setHiddenAttribute(hidden, true);

    FeatureMap params = Factory.newFeatureMap();
    params.put("listsURL", compoundListsURL);
    params.put("wholeWordsOnly",Boolean.FALSE);
    gazc = (LanguageAnalyser)Factory.createResource("gate.creole.gazetteer.DefaultGazetteer",params,hidden);

    params = Factory.newFeatureMap();
    params.put("listsURL", elementListsURL);
    gazo = (LanguageAnalyser)Factory.createResource("gate.creole.gazetteer.DefaultGazetteer",params,hidden);

    params = Factory.newFeatureMap();
    params.put("grammarURL", transducerGrammarURL);
    net = (LanguageAnalyser)Factory.createResource("gate.creole.Transducer",params,hidden);

    return this;
  }

  public void execute() throws ExecutionException
  {
    Document doc = getDocument();

    gazc.setDocument(doc);
    gazo.setDocument(doc);
    net.setDocument(doc);

    try {
      gazc.execute();
      gazo.execute();
      net.execute();

      //This lot used to be in the clean.jape file but it was slowing things
      //down a lot as what I really wanted would have required the brill style
      //to do what it is meant to do.
      FeatureMap params = Factory.newFeatureMap();
      AnnotationSet temp = doc.getAnnotations().get("NotACompound",params);
      if (temp != null) doc.getAnnotations().removeAll(temp);

      params.put("majorType","CTelement");
      temp = doc.getAnnotations().get("Lookup",params);
      if (temp != null) doc.getAnnotations().removeAll(temp);

      params.put("majorType","chemTaggerSymbols");
      temp = doc.getAnnotations().get("Lookup",params);
      if (temp != null) doc.getAnnotations().removeAll(temp);
    }
    finally {
      // make sure document references are released after use
      gazc.setDocument(null);
      gazo.setDocument(null);
      net.setDocument(null);
    }
  }
}
