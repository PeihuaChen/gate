/*
	TestPR2.java 

	Hamish Cunningham, 4/Sept/2000

	$Id$
*/

package testpkg;

import java.util.*;

import gate.*;
import gate.util.*;


/** A simple ProcessingResource for testing purposes.
  */
public class TestPR2 implements ProcessingResource
{

  /** Default Construction */
  public TestPR2() {
    this(null);
  } // Default Construction

  /** Construction from name and features */
  public TestPR2(FeatureMap features) {
    this.features = features;
  } // Construction from name and features

  /** Get the features associated with this corpus. */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; } 

  /** Get the factory that created this resource. */
  public Factory getFactory() {
    throw new LazyProgrammerException();
  } // getFactory()

  /** The features associated with this resource. */
  protected FeatureMap features;
   
} // class TestPR2
