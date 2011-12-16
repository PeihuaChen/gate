/*
 * Copyright (c) 2011, The University of Sheffield.
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * Licensed under the GNU Library General Public License, Version 3, June 2007
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 */

package gate.creole.measurements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.TestCase;

/**
 * @author Mark A. Greenwood
 */
public class MeasurementsTest extends TestCase {

  private static MeasurementsParser parser;
  
  @Override
  public void setUp() throws MalformedURLException, IOException  {
    parser = new MeasurementsParser((new File("resources/units.dat")).toURI().toURL(), new File("resources/common_words.txt").toURI().toURL());    
  }

  @Override
  public void tearDown() {
    parser = null;
  }

  public void test6Feet() {
    Measurement m = parser.parse(6d, "feet");
    assertNotNull(m);
    
    assertEquals("m", m.getNormalizedUnit());
    assertEquals(6d, m.getValue());
  }
}
