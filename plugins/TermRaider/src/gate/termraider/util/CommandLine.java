/*
 * Copyright (C) 2008--2012 by the University of Sheffield
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * $Id$
 */

package gate.termraider.util;

import gate.*;
import gate.termraider.bank.*;
import gate.util.ExtensionFileFilter;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;
import gnu.getopt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

import org.apache.commons.lang.StringUtils;



public class CommandLine {

  public static final double termSavingThreshold = 25.0;
  public static final double pmiSavingThreshold  =  0.0;
  
  private boolean useDatastore, recursive, dryRun, saveEverything, lucene;
  private File inputDirectory, outputDirectory, datastoreDirectory, applicationFile;
  private String encoding;
  private String[] filterExtensions;
  

  
  /**
   * This method just instantiates the working object with the
   * command-line arguments.
   */
  public static void main(String[] args) {
    CommandLine proc;
    try {
      proc = new CommandLine(args);
      proc.process();
    }
    catch(GateException e) {
      e.printStackTrace();
    }
  }


  public CommandLine(String[] args) throws GateException {
    if (args.length < 1) {
      printHelp();
      System.exit(0);
    }

    Utilities.startClock();
    processArgs(args);
    if (! testDirectories()) {
      System.exit(1);
    }
    Utilities.setGateHome();
    Gate.init();
  }


  
  /**
   * Process each corpus in a datastore through the pipeline, without
   * rearranging the corpus-document allocation.
   */
  private void process()  throws GateException {
    List<Corpus> corpora = new ArrayList<Corpus>();
    DataStore ds = null;
    String source = "";
    try { // BIG TRY
      if (useDatastore) {
        ds = Utilities.openDatastore(datastoreDirectory, lucene);
        source = datastoreDirectory.getName();
        corpora = Utilities.getCorpora(ds);
        int nbrOfDocuments = Utilities.getNbrOfDocuments(ds);
        Utilities.printNeatly("Nbr of documents found:", nbrOfDocuments);
      }
      else {
        Corpus corpus = loadFiles();
        source = inputDirectory.getName();
        corpora.add(corpus);
      }

      Utilities.printNeatly("Nbr of corpora found:", corpora.size());


      CorpusController pipeline = (CorpusController) PersistenceManager.loadObjectFromFile(applicationFile);
      System.out.println("loaded " + applicationFile.getAbsolutePath());

      for (Corpus corpus : corpora) {
        pipeline.setCorpus(corpus);
        if (! dryRun) {
          pipeline.execute();
          if (useDatastore) {
            corpus.sync();
          }
          String nameBase = source + '_' + Utilities.getTimestamp();
          saveBanks(source + '_' + corpus.getName(), nameBase, ds, this.outputDirectory, this.saveEverything);
        }
        pipeline.setCorpus(null);
        Utilities.printElapsedTime();
      }
    } // END BIG TRY
    catch(IOException e) {
      throw new GateException(e);
    }
    finally {
      if (useDatastore) {
        ds.close();
      }
    }
  }

  
  private static void saveBanks(String source, String nameBase, DataStore ds, File outputDirectory, 
          boolean saveEverything)
    throws GateException {
    // save all the Termbanks and Pairbanks we can find
    String bankClassname = gate.termraider.util.AbstractBank.class.getName();
    List<Resource> banks = Gate.getCreoleRegister().getAllInstances(bankClassname);
    int sequence = 0;
    for (Resource bank : banks) {
      saveBank((AbstractBank) bank, outputDirectory, nameBase, sequence, saveEverything);
      if (ds != null) {
        ds.adopt((LanguageResource) bank, null);
        ds.sync((LanguageResource) bank);
      }
      sequence++;
    }
  }


  /**
   * Save only the termbanks that include refCorpus in their input.
   * Intended for the GATE off-line component in Arcomem.
   * Return the RDF files so they can be uploaded from the off-line 
   * component.
   */
  public static Set<File> saveBanks(String nameBase, File outputDirectory, 
          boolean saveEverything, Corpus refCorpus)
    throws GateException {
    Set<File> filesSaved = new HashSet<File>();
    // save all the Termbanks and Pairbanks we can find
    String bankClassname = gate.termraider.util.AbstractBank.class.getName();
    List<Resource> banks = Gate.getCreoleRegister().getAllInstances(bankClassname);
    int sequence = 0;
    for (Resource bank : banks) {
      AbstractBank bank1 = (AbstractBank) bank;
      if (bank1.getCorpora().contains(refCorpus)) {
        File file = saveBank(bank1, outputDirectory, nameBase, sequence, saveEverything);
        sequence++;
        if (file != null) {
          filesSaved.add(file);
        }
      }
    }
    return filesSaved;
  }

  
  public static Set<AbstractBank> getRelevantBanks(Corpus refCorpus) throws GateException {
    Set<AbstractBank> relevantBanks = new HashSet<AbstractBank>();
    String bankClassname = gate.termraider.util.AbstractBank.class.getName();
    List<Resource> banks = Gate.getCreoleRegister().getAllInstances(bankClassname);
    for (Resource bank : banks) {
      AbstractBank bank1 = (AbstractBank) bank;
      if (bank1.getCorpora().contains(refCorpus)) {
        relevantBanks.add(bank1);
      }
    }
    return relevantBanks;
  }

  
  public static Set<AbstractTermbank> getRelevantTermbanks(Corpus refCorpus) throws GateException {
    Set<AbstractTermbank> relevantBanks = new HashSet<AbstractTermbank>();
    String termbankClassname = gate.termraider.bank.AbstractTermbank.class.getName();
    List<Resource> banks = Gate.getCreoleRegister().getAllInstances(termbankClassname);
    for (Resource bank : banks) {
      AbstractTermbank bank1 = (AbstractTermbank) bank;
      if (bank1.getCorpora().contains(refCorpus)) {
        relevantBanks.add(bank1);
      }
    }
    return relevantBanks;
  }

  
  
  private static File saveBank(AbstractBank bank, File outputDirectory, 
          String nameBase, int sequence, boolean saveEverything) throws GateException {
    String name = nameBase + "_" + Integer.toHexString(sequence)
    + "_" + bank.shortScoreDescription();
    
    File csvFile = new File(outputDirectory, name + ".csv");
    File rdfFile = new File(outputDirectory, name + ".rdf");
    if (saveEverything) {
      bank.saveAsCsv(csvFile);
      bank.saveAsRdfAndDeleteOntology(rdfFile);
    }
    else if (bank instanceof AbstractPairbank) {
      bank.saveAsCsv(pmiSavingThreshold, csvFile);
      bank.saveAsRdfAndDeleteOntology(pmiSavingThreshold, rdfFile);
    }
    else {
      bank.saveAsCsv(termSavingThreshold, csvFile);
      bank.saveAsRdfAndDeleteOntology(termSavingThreshold, rdfFile);
    }
  
    /* Only Termbanks have a real RDF option at present, but the 
     * PMIBank has a method implemented to print a warning.    */

    if (bank instanceof AbstractPairbank) {
      rdfFile = null;
    }
    
    return rdfFile;
  }
  

  private Corpus loadFiles() throws GateException {
    Corpus corpus = Factory.newCorpus("corpus");
    try {
      if (filterExtensions != null) {
        ExtensionFileFilter filter = new ExtensionFileFilter();
        for (String ext : filterExtensions) {
          filter.addExtension(ext);
        }
        corpus.populate(inputDirectory.toURI().toURL(), filter, encoding, recursive);
      }
      else {
        corpus.populate(inputDirectory.toURI().toURL(), null, encoding, recursive);
      }
    }
    catch (MalformedURLException e) {
      throw new GateException(e);
    } 
    catch(IOException e) {
      throw new GateException(e);
    }
    
    for (String docName : corpus.getDocumentNames()) {
      System.out.println("Input document (GATE name): " + docName);
    }
    
    return corpus;
  }
  
  
  /**
   * Process command line arguments
   */
  private void processArgs(String[] args)  {
    dryRun = false;
    lucene = false;
    recursive = false;
    saveEverything = false;
    String applicationFilename = "";
    Getopt g = new Getopt("DatastorePipelineProcessor", args, "hi:le:o:d:fra:T");
    int c;
    while ((c = g.getopt()) != -1) {
      switch (c) {
        case 'h':
          printHelp();
          System.exit(0);
        case 'i':
          inputDirectory = new File(g.getOptarg());
          break;
        case 'l':
          lucene = true;
          break;
        case 'a':
          applicationFilename = g.getOptarg();
          break;
        case 'e':
          encoding = g.getOptarg();
          break;
        case 'o':
          outputDirectory = new File(g.getOptarg());
          break;
        case 'd':  // use an existing datastore
          datastoreDirectory = new File(g.getOptarg());
          useDatastore = true;
          break;
        case 'f':
          filterExtensions = StringUtils.split(g.getOptarg(), ',');
          break;
        case 'r':
          recursive = true;
          break;
        case 'T':
          saveEverything = true;
          break;
      }
    }
    
    if (applicationFilename.isEmpty()) {
      applicationFilename = "termraider2-en.gapp";
      File applicationsDir = new File("applications");
      applicationFile = new File(applicationsDir, applicationFilename);
    }
    else {
      applicationFile = new File(applicationFilename);
    }

  }


  private boolean testDirectories() {
    boolean passed = true;

    if (useDatastore) {
      if (datastoreDirectory == null) {
        System.out.println("No datastore directory specified");
        passed = false;
      }
      else if (! datastoreDirectory.exists()) {
        System.out.println("Datastore directory does not exist: " + datastoreDirectory.getAbsolutePath());
        passed = false;
      }
      else if (! datastoreDirectory.isDirectory()) {
        System.out.println("Datastore directory is not a directory: " + datastoreDirectory.getAbsolutePath());
        passed = false;
      }
      else if (! datastoreDirectory.canRead()) {
        System.out.println("Datastore directory is not readable: " + datastoreDirectory.getAbsolutePath());
        passed = false;
      }
      else {
        System.out.println("Datastore directory OK: " + datastoreDirectory.getAbsolutePath());
      }
    }
    else {
      if (inputDirectory == null) {
        System.out.println("No input directory specified");
        passed = false;
      }
      else if (! inputDirectory.exists()) {
        System.out.println("Input directory does not exist: " + inputDirectory.getAbsolutePath());
        passed = false;
      }
      else if (! inputDirectory.isDirectory()) {
        System.out.println("Input directory is not a directory: " + inputDirectory.getAbsolutePath());
        passed = false;
      }
      else if (! inputDirectory.canRead()) {
        System.out.println("Input directory is not readable: " + inputDirectory.getAbsolutePath());
        passed = false;
      }
      else {
        System.out.println("Input directory OK: " + inputDirectory.getAbsolutePath());
      }
    }

    if (outputDirectory.exists()) {
      if (! outputDirectory.isDirectory()) {
        System.out.println("Output directory is not a directory: " + outputDirectory.getAbsolutePath());
        passed = false;
      }
      else if (! outputDirectory.canRead()) {
        System.out.println("Output directory is not readable: " + outputDirectory.getAbsolutePath());
        passed = false;
      }
      else {
        System.out.println("Output directory OK: " + outputDirectory.getAbsolutePath());
      }
    }
    else {
      boolean mkdirOK = outputDirectory.mkdirs();
      if (! mkdirOK) {
        System.out.println("Output directory cannot be created: " + outputDirectory.getAbsolutePath());
        passed = false;
      }
      else {
        System.out.println("Output directory OK: " + outputDirectory.getAbsolutePath());
      }
    }

    return passed;
  }


  /**
   * Print help about command line arguments
   */
  private void printHelp() {
    System.out.println("-h     : print this help & exit");
    System.out.println();
    System.out.println("-i DIR : input directory; populate the corpus from its documents");
    System.out.println();
    System.out.println("-d DIR : use this existing serial datastore;");
    System.out.println();
    System.out.println("Either -i or -d is required.");
    System.out.println();
    System.out.println("-a APP : path to application (gapp) file");
    System.out.println();
    System.out.println("-l     : datastore is lucene/indexed");
    System.out.println();
    System.out.println("-f STR : comma-separated list of input filename extensions (e.g., 'html,xml');");
    System.out.println("         if omitted, all documents in the directory will be used");
    System.out.println();
    System.out.println("-r     : process the input directory recursively (load files from subdirectories)");
    System.out.println();
    System.out.println("-T     : no minimum threshold (save everything)");
    System.out.println();
    System.out.println("-e STR : document encoding");
    System.out.println();
    System.out.println("-o DIR : output directory; will be created if necessary");
    System.out.println();
  }

}
