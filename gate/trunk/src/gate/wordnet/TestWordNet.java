/*
 *  TestWordnet.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 17/May/02
 *
 *  $Id$
 */

package gate.wordnet;

import java.io.*;
import java.util.*;

//import net.didion.jwnl.*;
//import net.didion.jwnl.dictionary.*;
//import net.didion.jwnl.data.*;
import junit.framework.*;

public class TestWordNet extends TestCase {

  private static final String propertiesFile = "D:/PRJ/jwnl/file_properties.xml";

  public TestWordNet(String dummy) {
    super(dummy);
  }

  public static void main(String[] args) {
    TestWordNet testWordNet1 = new TestWordNet("");

    try {
      testWordNet1.testWN_01();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public void testWN_01() throws Exception {

    IndexFileWordNetImpl wnMain = new IndexFileWordNetImpl();
    wnMain.setPropertyFile(new File(propertiesFile));
    wnMain.init();

    //get all synsets for "cup"
    List senseList = wnMain.lookupWord("cup",WordNet.POS_NOUN);
    Assert.assertTrue(senseList.size() == 8);

    Iterator itSenses = senseList.iterator();

    for (int i=0; i< senseList.size(); i++) {

      WordSense currSense = (WordSense)senseList.get(i);
      Synset currSynset = currSense.getSynset();
      Assert.assertNotNull(currSynset);

      switch(i+1) {

        case 1:
          checkSynset(currSynset,
                      "a small open container usually used for drinking; \"he put the cup back in the saucer\"; \"the handle of the cup was missing\"",
                      1);
          break;

        case 2:
          checkSynset(currSynset,
                      "the quantity a cup will hold; \"he drank a cup of coffee\"; \"he borrowed a cup of sugar\"",
                      2);
          break;

        case 3:
          checkSynset(currSynset,
                      "any cup-shaped concavity; \"bees filled the waxen cups with honey\"; \"he wore a jock strap with a metal cup\"; \"the cup of her bra\"",
                      1);
          break;

        case 4:
          checkSynset(currSynset,
                      "a United States liquid unit equal to 8 fluid ounces",
                      1);
          break;

        case 5:
          checkSynset(currSynset,
                      "cup-shaped plant organ",
                      1);
          break;

        case 6:
          checkSynset(currSynset,
                      "punch served in a pitcher instead of a punch bowl",
                      1);
          break;

        case 7:
          checkSynset(currSynset,
                      "the hole (or metal container in the hole) on a golf green; \"he swore as the ball rimmed the cup and rolled away\"; \"put the flag back in the cup\"",
                      1);
          break;

        case 8:
          checkSynset(currSynset,
                      "a large metal vessel with two handles that is awarded to the winner of a competition; \"the school kept the cups is a special glass case\"",
                      2);
          break;
      }
    }
  }

  private void checkSynset(Synset s, String gloss, int numWords) {

    Assert.assertEquals(s.getGloss(),gloss);

    List wordSenses = s.getWordSenses();
    Assert.assertTrue(wordSenses.size() == numWords);
  }

/*
  public void testWN_01() throws Exception {

    IndexFileWordNetImpl wnMain = new IndexFileWordNetImpl();
    wnMain.setPropertyFile(new File("D:/PRJ/jwnl/file_properties.xml"));
    wnMain.init();

    Dictionary dict = wnMain.getJWNLDictionary();
    Assert.assertNotNull(dict);

    IndexWordSet iSet = dict.lookupAllIndexWords("cup");
    IndexWord[] arr =  iSet.getIndexWordArray();
    for (int i=0; i< arr.length; i++) {
      IndexWord iw = arr[i];
      net.didion.jwnl.data.Synset[] synsets = iw.getSenses();
      for (int j=0; j< synsets.length; j++) {
        net.didion.jwnl.data.Synset s = synsets[j];
//System.out.println("synset: "+s.toString());
//net.didion.jwnl.data.Word firstWord = s.getWord(0);
//System.out.println("0th word index is " + firstWord.getIndex());
        Synset ss = new SynsetImpl(s,wnMain.getJWNLDictionary());
        List rel = ss.getSemanticRelations();
      }
    }


System.out.println(iSet.size());
System.out.println(iSet);
  }
*/

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestWordNet.class);
  } // suite

}