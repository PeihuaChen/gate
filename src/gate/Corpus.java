/*
 *  Corpus.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/2000
 *
 *  $Id$
 */

package gate;
import java.util.*;
import java.net.URL;
import java.io.FileFilter;
import java.io.IOException;

import gate.util.*;
import gate.event.*;
import gate.creole.ResourceInstantiationException;

/** Corpora are lists of Document. TIPSTER equivalent: Collection.
  */
public interface Corpus extends LanguageResource, List, NameBearer {

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
   * Unloads the document from memory. Only needed if memory
   * preservation is an issue. Only supported for Corpus which is
   * stored in a Datastore. To get this document back in memory,
   * use get() on Corpus or if you have its persistent ID, request it
   * from the Factory.
   * <P>
   * Transient Corpus objects do nothing,
   * because there would be no way to get the document back
   * again afterwards.
   * @param Document to be unloaded from memory.
   * @return void.
   */
  public void unloadDocument(Document doc);

  /**
   * Fills this corpus with documents created on the fly from selected files in
   * a directory. Uses a link {@FileFilter} to select which files will be used
   * and which will be ignored.
   * A simple file filter based on extensions is provided in the Gate
   * distribution ({@link gate.util.ExtensionFileFilter}).
   * @param directory the directory from which the files will be picked. This
   * parameter is an URL for uniformity. It needs to be a URL of type file
   * otherwise an InvalidArgumentException will be thrown.
   * An implementation for this method is provided as a static method at
   * {@link gate.corpora.CorpusImpl#populate(Corpus,URL,FileFilter,boolean)}.
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
   * This method returns true when the document is already loaded in memory.
   * The transient corpora will always return true as they can only contain
   * documents that are present in the memory.
   */
  public boolean isDocumentLoaded(int index);


  /**
   * Removes one of the listeners registered with this corpus.
   * @param l the listener to be removed.
   */
  public void removeCorpusListener(CorpusListener l);

  /**
   * Registers a new {@link CorpusListener} with this corpus.
   * @param l the listener to be added.
   */
  public void addCorpusListener(CorpusListener l);

} // interface Corpus
