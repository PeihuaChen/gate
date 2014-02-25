/*
 *  Copyright (c) 2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */
package gate.termraider.bank.modes;

public enum IdfCalculation {
  Natural,
  Logarithmic;
  
  /* These calculations are from Manning & Schütze, Foundations of
   * Statistical NLP, section 15.2 (p.544).
   */
  
  public static double calculate(IdfCalculation mode, int rawDF, int corpusSize) {
    double df = (double) rawDF;
    double n = (double) corpusSize;
    
    if (mode == Logarithmic) {
      return 1.0 + logarithm(n / (df + 1.0));
    }
    
    // TODO: review the df calculation modes; they must always return 
    // something > 0.
    
    // must be Natural
    return 1.0 / (df + 1.0);
  }

  public static final double logBase = 2.0;
  private static double conversion;
  
  static {
    conversion = Math.log10(logBase);
  }
  
  public static double logarithm(double input) {
    return Math.log10(input) / conversion;
  }
  
}
