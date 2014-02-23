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

import org.apache.commons.lang.*;

import gate.termraider.bank.*;
import gate.termraider.util.*;

public class CsvGenerator {
  
  public static void generateAndSaveCsv(AbstractBank bank, 
          Number threshold, File outputFile) throws GateException {
    PrintWriter writer = initializeWriter(outputFile);
    
    if (bank instanceof AbstractTermbank) {
      String scorePropertyName = bank.getScoreProperty();
      generateTermbankCsv((AbstractTermbank) bank, writer, threshold.doubleValue(), scorePropertyName);
    }
    else if (bank instanceof DocumentFrequencyBank) {
      generateDFCsv((DocumentFrequencyBank) bank, writer, threshold.intValue());
    }
    
    writer.flush();
    writer.close();
    if (bank.getDebugMode()) {
      System.out.println("Saved CSV to " + outputFile.getAbsolutePath() +
              " from " + bank.getName() + " (" + bank.getClass().getName() + ")");
    }
  }
  
  
  private static void generateTermbankCsv(AbstractTermbank bank, PrintWriter writer, 
          double threshold, String scorePropertyName) {
    Map<Term, Double> termScores = bank.getTermScores();
    Map<Term, Set<String>> termDocuments = bank.getTermDocuments();
    Map<Term, Integer> termFrequencies = null;
    termFrequencies = bank.getTermFrequencies();
    addComment(bank, "threshold = " + threshold);
    List<Term> sortedTerms = bank.getTermsByDescendingScore();
    
    addComment(bank, "Unfiltered nbr of terms = " + sortedTerms.size());
    int written = 0;
    writeTermbankHeader(writer);
    
    for (Term term : sortedTerms) {
      Double score = termScores.get(term);
      if (score >= threshold) {
        Set<String> documents = termDocuments.get(term);
        Integer frequency = termFrequencies.get(term);
        writeTermBankContent(writer, term, score, documents, frequency, scorePropertyName);
        written++;
      }
      else {  // the rest must be lower
        break;
      }
    }
    addComment(bank, "Filtered nbr of terms = " + written);
  }

  
  private static void generateDFCsv(DocumentFrequencyBank bank, PrintWriter writer, int threshold) {
    Map<Term, Integer> frequencies = bank.getDocFrequencies();
    addComment(bank, "threshold = " + threshold);
    List<Term> sortedTerms = bank.getTermsByDescendingFreq();
    
    addComment(bank, "Unfiltered nbr of terms = " + sortedTerms.size());
    int written = 0;
    writeDFHeader(writer);
    writeDFContent(writer, "_TOTAL_DOCS_", bank.getTotalDocs());
    
    for (Term term : sortedTerms) {
      Integer freq = frequencies.get(term);
      if (freq >= threshold) {
        writeDFContent(writer, term, freq);
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
  
  
  private static void writeTermBankContent(PrintWriter writer, Term term, Double score,
          Set<String> documents, Integer frequency, String scorePropertyName) {
    StringBuilder sb = new StringBuilder();
    sb.append(StringEscapeUtils.escapeCsv(term.getTermString()));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(term.getLanguageCode()));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(term.getType()));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(scorePropertyName));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(score.toString()));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(Integer.toString(documents.size())));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(frequency.toString()));
    writer.println(sb.toString());
  }
  
  
  private static void writeTermbankHeader(PrintWriter writer) {
    StringBuilder sb = new StringBuilder();
    sb.append(StringEscapeUtils.escapeCsv("Term"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Lang"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Type"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("ScoreType"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Score"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Document_Count"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Term_Frequency"));
    writer.println(sb.toString());
  }


  private static void writeDFContent(PrintWriter writer, Term term, Integer frequency) {
    StringBuilder sb = new StringBuilder();
    sb.append(StringEscapeUtils.escapeCsv(term.getTermString()));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(term.getLanguageCode()));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(term.getType()));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(frequency.toString()));
    writer.println(sb.toString());
  }

  

  private static void writeDFContent(PrintWriter writer, String string, Integer frequency) {
    StringBuilder sb = new StringBuilder();
    sb.append(StringEscapeUtils.escapeCsv(string));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(""));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(""));
    sb.append(',');
    sb.append(StringEscapeUtils.escapeCsv(frequency.toString()));
    writer.println(sb.toString());
  }


  private static void writeDFHeader(PrintWriter writer) {
    StringBuilder sb = new StringBuilder();
    sb.append(StringEscapeUtils.escapeCsv("Term"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Lang"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("Type"));
    sb.append(',').append(StringEscapeUtils.escapeCsv("DocFrequency"));
    writer.println(sb.toString());
  }
  
}
