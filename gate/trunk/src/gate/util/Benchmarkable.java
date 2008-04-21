package gate.util;

import org.apache.log4j.Logger;

/**
 * Resources that want to log their progress or results into a shared log
 * centrally maintained by GATE, should implement this interface and use
 * the java.util.Benchmark class to log their entries.
 * @author niraj
 *
 */
public interface Benchmarkable {

  /**
   * Returns the benchmark ID of the parent of this resource.
   * @return
   */
  public String getParentBenchmarkID();
  
  /**
   * Returns the benchmark ID of this resource.
   * @return
   */
  public String getBenchmarkID();
  
  /**
   * Given an ID of the parent resource, this method is responsible for producing the Benchmark ID, unique to this resource.
   * @param parentID
   */
  public void createBenchmarkID(String parentID);
  
  /**
   * This method sets the benchmarkID for this resource.
   * @param benchmarkID
   */
  public void setParentBenchmarkID(String benchmarkID);
  
  /**
   * Returns the logger object being used by this resource.
   * @return
   */
  public Logger getLogger();

}
