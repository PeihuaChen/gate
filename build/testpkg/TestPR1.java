/*
	TestPR1.java 

	Hamish Cunningham, 4/Sept/2000

	$Id$
*/

package testpkg;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.creole.*;


/** A simple ProcessingResource for testing purposes.
  */
public class TestPR1 extends AbstractResource implements ProcessingResource
{

  /** Default Construction */
  public TestPR1() {
    this(null);
  } // Default Construction

  /** Construction from name and features */
  public TestPR1(FeatureMap features) {
    this.features = features;
  } // Construction from name and features

  /** Get the features associated with this corpus. */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; } 

  /** The features associated with this resource. */
  protected FeatureMap features;

  /** Run the thing. */
  public void run() {
    features = Factory.newFeatureMap();
    features.put("I", "have been run, thankyou");
  } // run

  /** Initialisation */
  public Resource init() {
    return this;
  } // init
   
} // class TestPR1
