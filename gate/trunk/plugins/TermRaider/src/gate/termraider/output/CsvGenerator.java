/*
 *  Copyright (c) 2010--2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */
package gate.termraider.output;

import gate.util.GateException;

import java.io.*;
import java.util.*;
import gate.termraider.bank.*;
import gate.termraider.util.*;


public class CsvGenerator {
  
  public static void generateAndSaveCsv(AbstractTermbank bank, 
          Number threshold, File outputFile) throws GateException {
    PrintWriter writer = initializeWriter(outputFile);
    Map<Term, Double> termScores = bank.getTermScores();
    addComment(bank, "threshold = " + threshold);
    List<Term> sortedTerms = bank.getTermsByDescendingScore();
    
    addComment(bank, "Unfiltered nbr of terms = " + sortedTerms.size());
    int written = 0;
    writer.println(bank.getCsvHeader());
    
    for (Term term : sortedTerms) {
      Double score = termScores.get(term);
      if (score >= threshold.doubleValue()) {
        writer.println(bank.getCsvLine(term));
        written++;
      }
      else {  // the rest must be lower
        break;
      }
    }
    addComment(bank, "Filtered nbr of terms = " + written);
  }

  
  private static void addComment(AbstractBank termbank, String commentStr) {
    if (termbank.getDebugMode()) {
      System.out.println(commentStr);
    }
  }
  
  
  private static PrintWriter initializeWriter(File outputFile) throws GateException {
    try {
      return new PrintWriter(outputFile);
    } 
    catch(FileNotFoundException e) {
      throw new GateException(e);
    }
  }
  
}
