/*
 *  SimpleCorpus.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 23/Jul/2004
 *
 *  $Id$
 */

package gate;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import gate.creole.ResourceInstantiationException;
import java.io.FileFilter;

import gate.util.NameBearer;

/** Corpora are lists of Document. TIPSTER equivalent: Collection.
  */
public interface SimpleCorpus extends LanguageResource, List, NameBearer {

  public static final String CORPUS_NAME_PARAMETER_NAME = "name";
  public static final String CORPUS_DOCLIST_PARAMETER_NAME = "documentsList";

  /**
   * Gets the names of the documents in this corpus.
   * @return a {@link List} of Strings representing the names of the documents
   * in this corpus.
   */
  public List getDocumentNames();

  /**
   * Gets the name of a document in this corpus.
   * @param index the index of the document
   * @return a String value representing the name of the document at
   * <tt>index</tt> in this corpus.
   */
  public String getDocumentName(int index);

  /**
   * Fills this corpus with documents created on the fly from selected files in
   * a directory. Uses a {@link FileFilter} to select which files will be used
   * and which will be ignored.
   * A simple file filter based on extensions is provided in the Gate
   * distribution ({@link gate.util.ExtensionFileFilter}).
   * @param directory the directory from which the files will be picked. This
   * parameter is an URL for uniformity. It needs to be a URL of type file
   * otherwise an InvalidArgumentException will be thrown.
   * An implementation for this method is provided as a static method at
   * {@link gate.corpora.CorpusImpl#populate(Corpus, URL, FileFilter, String, boolean)}.
   * @param filter the file filter used to select files from the target
   * directory. If the filter is <tt>null</tt> all the files will be accepted.
   * @param encoding the encoding to be used for reading the documents
   * @param recurseDirectories should the directory be parsed recursively?. If
   * <tt>true</tt> all the files from the provided directory and all its
   * children directories (on as many levels as necessary) will be picked if
   * accepted by the filter otherwise the children directories will be ignored.
   */
  public void populate(URL directory, FileFilter filter,
                       String encoding, boolean recurseDirectories)
                       throws IOException, ResourceInstantiationException;

  /**
   * Fills the provided corpus with documents extracted from the provided trec file.
   * @param trecfile the trec file.
   * @param encoding the encoding of the trec file.
   */
  public void populate(URL trecFile, String encoding)
              throws IOException, ResourceInstantiationException;  

} // interface SimpleCorpus
