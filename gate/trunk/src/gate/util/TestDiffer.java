/*
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 26/Feb/2002
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.io.*;
import java.net.*;

import junit.framework.*;
import java.lang.reflect.*;

import gate.*;
import gate.util.*;

public class TestDiffer extends TestCase{
  /** Construction */
  public TestDiffer(String name) { super(name); }

  /** Fixture set up */
   public void setUp() {
   } // setUp

   /** Put things back as they should be after running tests.
     */
   public void tearDown() throws Exception {
   } // tearDown

   /** Test suite routine for the test runner */
   public static Test suite() {
     return new TestSuite(TestDiffer.class);
   } // suite

  /** Jdk compiler */
   public void testDiffer() throws Exception {
     Document doc = Factory.newDocument(
         new URL(gate.corpora.TestDocument.getTestServerName() +
                 "tests/ft-bt-03-aug-2001.html"),
         "windows-1252"
         );
     AnnotationSet annSet = doc.getAnnotations();
     //create 100 annotations
     FeatureMap features = Factory.newFeatureMap();
     features.put("type", "BAR");
     for (int i = 0; i < 100; i++) {
       annSet.add(new Long(i * 10), new Long( (i + 1) * 10), "Foo", features);
     }
     List keySet = new ArrayList(annSet);
     List responseSet = new ArrayList(annSet);

     //check 100% Precision and recall
     AnnotationDiffer differ = new AnnotationDiffer();
     differ.setSignificantFeaturesSet(null);
     differ.calculateDiff(keySet, responseSet);
     differ.sanityCheck();
     double value = differ.getPrecisionStrict();
     Assert.assertEquals("Precision Strict: " + value + " instead of 1!",
                         1, value, 0);
     value = differ.getRecallStrict();
     Assert.assertEquals("Recall Strict: " + value + " instead of 1!",
                         1, value, 0);
     value = differ.getPrecisionLenient();
     Assert.assertEquals("Precision Lenient: " + value + " instead of 1!",
                         1, value, 0);
     value = differ.getRecallLenient();
     Assert.assertEquals("Recall Lenient: " + value + " instead of 1!",
                         1, value, 0);

     //check low precision
     Integer id = annSet.add(new Long(2), new Long(4), "Foo", features);
     Annotation falsePositive = annSet.get(id);
     responseSet.add(falsePositive);
     differ.calculateDiff(keySet, responseSet);
     differ.sanityCheck();
     value = differ.getPrecisionStrict();
     Assert.assertEquals("Precision Strict: " + value + " instead of .99!",
                         .99, value, .001);
     //recall should still be 100%
     value = differ.getRecallStrict();
     Assert.assertEquals("Recall Strict: " + value + " instead of 1!",
                         1, value, 0);
     value = differ.getRecallLenient();
     Assert.assertEquals("Recall Lenient: " + value + " instead of 1!",
                         1, value, 0);


     //check low recall
     responseSet.remove(falsePositive);
     keySet.add(falsePositive);
     differ.calculateDiff(keySet, responseSet);
     differ.sanityCheck();
     value = differ.getRecallStrict();
     Assert.assertEquals("Recall Strict: " + value + " instead of .99!",
                         .99, value, .001);
     //precision should still be 100%
     value = differ.getPrecisionStrict();
     Assert.assertEquals("Precision Strict: " + value + " instead of 1!",
                         1, value, 0);
     value = differ.getPrecisionLenient();
     Assert.assertEquals("Precision Lenient: " + value + " instead of 1!",
                         1, value, 0);
   }

}