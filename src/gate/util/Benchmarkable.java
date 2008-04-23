/*
 *  Benchmarkable.java
 *
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 */

package gate.util;

import org.apache.log4j.Logger;

/**
 * Resources that want to log their progress or results into a shared log
 * centrally maintained by GATE, should implement this interface and use the
 * java.util.Benchmark class to log their entries.
 * 
 * @author niraj
 */
public interface Benchmarkable {

  /**
   * Returns the benchmark ID of the parent of this resource.
   * 
   * @return
   */
  public String getParentBenchmarkId();

  /**
   * Returns the benchmark ID of this resource.
   * 
   * @return
   */
  public String getBenchmarkId();

  /**
   * Given an ID of the parent resource, this method is responsible for
   * producing the Benchmark ID, unique to this resource.
   * 
   * @param parentID
   */
  public void createBenchmarkId(String parentId);

  /**
   * This method sets the benchmarkID for this resource.
   * 
   * @param benchmarkID
   */
  public void setParentBenchmarkId(String benchmarkId);

  /**
   * Returns the logger object being used by this resource.
   * 
   * @return
   */
  public Logger getLogger();
}
