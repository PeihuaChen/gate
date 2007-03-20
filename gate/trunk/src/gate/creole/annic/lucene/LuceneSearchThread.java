/*
 *  LuceneSearchThread.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: LuceneSearchThread.html,v 1.0 2007/03/19 16:22:01 niraj Exp $
 */
package gate.creole.annic.lucene;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import gate.creole.annic.Pattern;
import gate.creole.annic.PatternAnnotation;
import gate.creole.annic.Constants;
import gate.creole.annic.SearchException;
import gate.creole.annic.apache.lucene.search.Hits;
import gate.creole.annic.apache.lucene.search.Query;

/**
 * Given a boolean query, it is translated into one or more AND
 * normalized queries. For example: (A|B)C is translated into AC and BC.
 * For each such query an instance of LuceneSearchThread is created.
 * Here, each query is issued separately and results are submitted to
 * main instance of LuceneSearch.
 * 
 * @author niraj
 */
public class LuceneSearchThread {

  /**
   * Debug variable
   */
  private static boolean DEBUG = false;

  /**
   * Number of base token annotations to be used in context.
   */
  private int contextWindow;

  /**
   * The location of index.
   */
  private String indexLocation;

  /**
   * Instance of a QueryParser.
   */
  private QueryParser queryParser;

  /**
   * BaseTokenAnnotationType.
   */
  private String baseTokenAnnotationType;

  /**
   * Instance of the LuceneSearcher.
   */
  private LuceneSearcher luceneSearcher;

  /**
   * Indicates if searching process is finished.
   */
  public boolean finished = false;

  /**
   * Index of the documentID we are currently searching for.
   */
  private int documentIDIndex = 0;

  /**
   * QueryItemIndex
   */
  private int queryItemIndex = 0;

  /**
   * List of document IDs retrieved from lucene index.
   */
  private ArrayList documentIDsList = new ArrayList();

  /**
   * A Map that holds information about search results.
   */
  private HashMap searchResultInfoMap = new HashMap();

  /**
   * First term position index.
   */
  private int ftpIndex = 0;

  /**
   * Indicates if the query was success.
   */
  private boolean success = false;

  /**
   * Indicates if we've reached the end of search results.
   */
  private boolean fwdIterationEnded = false;

  /**
   * We keep track of what was the last document ID visited. This is
   * used for optimization reasons
   */
  private String documentIDInUse = null;

  /**
   * This is where we store the tokenStreamInUse
   */
  private ArrayList tokenStreamInUse = null;

  /**
   * Query
   */
  private String query = null;

  /**
   * Given a file name, it replaces the all invalid characters with '_'.
   * 
   * @param name
   * @return
   */
  private String getCompatibleName(String name) {
    return name.replaceAll("[\\/:\\*\\?\"<>|]", "_");
  }

  /**
   * This method collects the necessary information from lucene and uses
   * it when the next method is called
   * 
   * @param limit limit indicates the number of patterns to retrieve
   * @param query query supplied by the user
   * @param patternWindow number of tokens to refer on left and right
   *          context
   * @param indexLocation location of the index the searcher should
   *          search in
   * @param luceneSearcher an instance of lucene search from where the
   *          instance of SearchThread is invoked
   * @return true iff search was successful false otherwise
   */
  public boolean search(String query, int patternWindow, String indexLocation,
          String corpusToSearchIn, LuceneSearcher luceneSearcher)
          throws SearchException {

    this.query = query;
    this.contextWindow = patternWindow;
    this.indexLocation = indexLocation;
    this.queryParser = new QueryParser();
    this.luceneSearcher = luceneSearcher;

    /*
     * reset all parameters that keep track of where we are in our
     * searching. These parameters are used mostly to keep track of
     * where to start fetching the next results from
     */
    searchResultInfoMap = new HashMap();
    documentIDIndex = 0;
    queryItemIndex = 0;
    documentIDsList = new ArrayList();
    ftpIndex = -1;
    success = false;
    fwdIterationEnded = false;

    try {
      // first find out the location of Index
      String temp = "";
      for(int i = 0; i < indexLocation.length(); i++) {
        if(indexLocation.charAt(i) == '\\') {
          temp += "/";
        }
        else {
          temp += indexLocation.charAt(i);
        }
      }
      indexLocation = temp;

      /*
       * for each different location there can be different
       * baseTokenAnnotationType each index will have their index
       * Definition file stored under the index directory so first see
       * if given location is a valid directory
       */
      File locationFile = new File(indexLocation);
      if(!locationFile.isDirectory()) {
        System.out.println("Skipping the invalid Index Location :"
                + indexLocation);
        return false;
      }

      if(!indexLocation.endsWith("/")) {
        indexLocation += "/";
      }

      // otherwise let us read the index definition file
      locationFile = new File(indexLocation + "LuceneIndexDefinition.xml");

      // check if this file is available
      if(!locationFile.exists()) {
        System.out
                .println("Index Definition file not found - Skipping the invalid Index Location :"
                        + indexLocation + "LuceneIndexDefinition.xml");
        return false;
      }

      java.io.FileReader fileReader = new java.io.FileReader(indexLocation
              + "LuceneIndexDefinition.xml");

      // other wise read this file
      com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream(
              new com.thoughtworks.xstream.io.xml.StaxDriver());

      // Saving was accomplished by using XML serialization of the map.
      HashMap indexInformation = (HashMap)xstream.fromXML(fileReader);

      // find out the baseTokenAnnotationType name
      baseTokenAnnotationType = (String)indexInformation
              .get(Constants.BASE_TOKEN_ANNOTATION_TYPE);

      // find out if the current index was indexed by annicIndexPR
      String indexedWithANNICIndexPR = (String)indexInformation
              .get(Constants.CORPUS_INDEX_FEATURE);

      if(indexedWithANNICIndexPR == null
              || !indexedWithANNICIndexPR
                      .equals(Constants.CORPUS_INDEX_FEATURE_VALUE)) {
        System.out
                .println("This corpus was not indexed by Annic Index PR - Skipping the invalid Index");
        return false;
      }

      // create various Queries from the user's query
      Query[] luceneQueries = queryParser.parse("contents", query,
              baseTokenAnnotationType, corpusToSearchIn);
      if(queryParser.needValidation()) {
        if(DEBUG) System.out.println("Validation enabled!");
      }
      else {
        if(DEBUG) System.out.println("Validation disabled!");
      }

      // create an instance of Index Searcher
      LuceneIndexSearcher searcher = new LuceneIndexSearcher(indexLocation);

      // we need to iterate through one query at a time
      for(int luceneQueryIndex = 0; luceneQueryIndex < luceneQueries.length; luceneQueryIndex++) {

        /*
         * this call reinitializes the first Term positions arraylists
         * which are being used to store the results
         */
        searcher.initializeTermPositions();

        /*
         * and now execute the query result of which will be stored in
         * hits
         */
        Hits hits = searcher.search(luceneQueries[luceneQueryIndex]);

        /*
         * and so now find out the positions of the first terms in the
         * returned results. first term position is the position of the
         * first term in the found pattern
         */
        ArrayList[] firstTermPositions = searcher.getFirstTermPositions();

        // if no result available, set null to our scores
        if(firstTermPositions[0].size() == 0) {
          // do nothing
          continue;
        }

        // iterate through each result and collect necessary information
        for(int hitIndex = 0; hitIndex < hits.length(); hitIndex++) {
          int index = firstTermPositions[0].indexOf(new Integer(hits
                  .id(hitIndex)));

          // we fetch all the first term positions for the query
          // issued
          ArrayList ftp = (ArrayList)firstTermPositions[1].get(index);

          /*
           * pattern length (in terms of total number of annotations
           * following one other)
           */
          int patLen = ((Integer)firstTermPositions[2].get(index)).intValue();

          /*
           * and the type of query (if it has only one annotation in it,
           * or multiple terms following them)
           */
          int qType = ((Integer)firstTermPositions[3].get(index)).intValue();

          // find out the documentID
          String documentID = hits.doc(hitIndex).get(Constants.DOCUMENT_ID);

          QueryItem queryItem = new QueryItem();
          queryItem.id = hits.id(hitIndex);
          queryItem.ftp = ftp;
          queryItem.patLen = patLen;
          queryItem.qType = qType;
          queryItem.query = luceneQueries[luceneQueryIndex];
          queryItem.queryString = queryParser.getQueryString(luceneQueryIndex);

          /*
           * all these information go in the top level arrayList. we
           * create separate arrayList for each individual document
           * where each element in the arrayList provides information
           * about different query issued over it
           */
          ArrayList queryItemsList = (ArrayList)searchResultInfoMap
                  .get(documentID);
          if(queryItemsList == null) {
            queryItemsList = new ArrayList();
            queryItemsList.add(queryItem);
            searchResultInfoMap.put(documentID, queryItemsList);
            documentIDsList.add(documentID);
          }
          else {
            // before inserting we check if it is already added
            if(!doesAlreadyExist(queryItem, queryItemsList)) {
              queryItemsList.add(queryItem);
              searchResultInfoMap.put(documentID, queryItemsList);
            }
          }
        }
      }
      searcher.close();

      // if any result possible, return true
      if(searchResultInfoMap.size() > 0)
        success = true;
      else success = false;
    }
    catch(Exception e) {
      throw new SearchException(e);
    }

    return success;
  }

  /**
   * First term positions.
   */
  private ArrayList ftp;

  /**
   * This method returns an array containing instances of QueryResult
   * 
   * @param numberOfResults the number of results to fetch
   * @return a list of QueryResult
   * @throws Exception
   */
  public ArrayList next(int numberOfResults) throws Exception {

    /*
     * We check here, if there were no results found, we return null
     */
    if(!success) {
      return null;
    }

    if(fwdIterationEnded) {
      return null;
    }

    int noOfResultsToFetch = numberOfResults;
    ArrayList toReturn = new ArrayList();

    // iterator over one document ID
    for(; documentIDIndex < documentIDsList.size(); documentIDIndex++, queryItemIndex = 0, this.ftp = null) {

      // deal with one document at a time
      String documentID = (String)documentIDsList.get(documentIDIndex);

      // obtain the information about all queries
      ArrayList queryItemsList = (ArrayList)searchResultInfoMap.get(documentID);

      if(documentIDInUse == null || !documentIDInUse.equals(documentID)
              || tokenStreamInUse == null) {
        documentIDInUse = documentID;
        try {
          // this is the first and last time we want this tokenStream
          // to hold information about the current document
          tokenStreamInUse = getTokenStreamFromDisk(indexLocation,
                  getCompatibleName(documentID));
        }
        catch(Exception e) {
          continue;
        }
      }

      // deal with one query at a time
      for(; queryItemIndex < queryItemsList.size(); queryItemIndex++, ftpIndex = -1, this.ftp = null) {
        QueryItem queryItem = (QueryItem)queryItemsList.get(queryItemIndex);

        /*
         * we've found the tokenStream and now we need to convert it
         * into the format we had at the time of creating index.. the
         * method getTokenStream(...) returns an array of arraylists
         * where the first object is GateAnnotations of that pattern
         * only second object is the position of the first token of the
         * actual pattern third object is the lenght of the actual
         * pattern
         */
        int qType = queryItem.qType;
        int patLen = queryItem.patLen;
        if(this.ftp == null) {
          this.ftp = queryItem.ftp;
        }
        else {
          qType = 1;
          patLen = 1;
        }
        PatternResult patternResult = getTokenStream(tokenStreamInUse, patLen,
                qType, contextWindow, queryItem.queryString,
                baseTokenAnnotationType, noOfResultsToFetch);

        /*
         * if none of the found patterns is valid continue with the next
         * query
         */
        if(patternResult == null || patternResult.numberOfPatterns == 0)
          continue;

        /*
         * We've found some patterns so give its effect to
         * noOfResultsToFetch
         */
        if(noOfResultsToFetch != -1)
          noOfResultsToFetch -= patternResult.numberOfPatterns;

        ArrayList annicPatterns = createAnnicPatterns(new LuceneQueryResult(
                removeUnitNumber(documentID), patternResult.firstTermPositions,
                patternResult.patternLegths, queryItem.qType,
                patternResult.gateAnnotations, queryItem.queryString));
        toReturn.addAll(annicPatterns);

        /*
         * If noOfResultsToFetch is 0, it means the search should
         * terminate unless and otherwise user has asked to return all
         * (-1)
         */
        if(numberOfResults != -1 && noOfResultsToFetch == 0) {
          return toReturn;
        }
      }
    }

    /*
     * if we are out of the loop set success to false such that this
     * thread is closed
     */
    fwdIterationEnded = true;
    return toReturn;
  }

  /**
   * Given an object of luceneQueryResult this method for each found
   * pattern, converts it into the annic pattern. In other words, for
   * each pattern it collects the information such as annotations in
   * context and so on.
   * 
   * @param aResult
   * @return
   */
  private ArrayList createAnnicPatterns(LuceneQueryResult aResult) {
    // get the result from search engine
    ArrayList annicPatterns = new ArrayList();
    ArrayList firstTermPositions = aResult.getFirstTermPositions();
    if(firstTermPositions != null && firstTermPositions.size() > 0) {
      ArrayList patternLength = aResult.patternLength();
      // locate Pattern
      ArrayList pats = locatePatterns((String)aResult.getDocumentID(), aResult
              .getGateAnnotations(), firstTermPositions, patternLength, aResult
              .getQuery());
      if(pats != null) {
        annicPatterns.addAll(pats);
      }
    }
    return annicPatterns;
  }

  /**
   * Locates the valid patterns in token stream and discards the invalid
   * first term positions returned by the lucene searcher.
   * 
   * @param docID
   * @param gateAnnotations
   * @param firstTermPositions
   * @param patternLength
   * @param queryString
   * @return
   */
  private ArrayList locatePatterns(String docID, ArrayList gateAnnotations,
          ArrayList firstTermPositions, ArrayList patternLength,
          String queryString) {

    // patterns
    ArrayList pat = new ArrayList();
    outer: for(int i = 0; i < gateAnnotations.size(); i++) {

      // each element in the tokens stream is a pattern
      ArrayList annotations = (ArrayList)gateAnnotations.get(i);
      if(annotations.size() == 0) {
        continue;
      }
      // from this annotations we need to create a text string
      // so lets find out the smallest and the highest offsets
      int smallest = Integer.MAX_VALUE;
      int highest = -1;
      for(int j = 0; j < annotations.size(); j++) {
        // each annotation is an instance of GateAnnotation
        gate.creole.annic.PatternAnnotation ga = (gate.creole.annic.PatternAnnotation)annotations
                .get(j);
        if(ga.getStartOffset() < smallest) {
          smallest = ga.getStartOffset();
        }

        if(ga.getEndOffset() > highest) {
          highest = ga.getEndOffset();
        }
      }

      // we have smallest and highest offsets
      char[] patternText = new char[highest - smallest];

      for(int j = 0; j < patternText.length; j++) {
        patternText[j] = ' ';
      }

      // and now place the text
      for(int j = 0; j < annotations.size(); j++) {
        // each annotation is an instance of GateAnnotation
        gate.creole.annic.PatternAnnotation ga = (gate.creole.annic.PatternAnnotation)annotations
                .get(j);
        if(ga.getText() == null) {
          // this is to avoid annotations such as split
          continue;
        }

        for(int k = ga.getStartOffset() - smallest, m = 0; m < ga.getText()
                .length()
                && k < patternText.length; m++, k++) {
          patternText[k] = ga.getText().charAt(m);
        }

        // we will initiate the annotTypes as well
        if(luceneSearcher.annotationTypesMap.keySet().contains(ga.getType())) {
          ArrayList aFeatures = (ArrayList)luceneSearcher.annotationTypesMap
                  .get(ga.getType());
          HashMap features = ga.getFeatures();
          if(features != null) {
            Iterator fSet = features.keySet().iterator();
            while(fSet.hasNext()) {
              String feature = (String)fSet.next();
              if(!aFeatures.contains(feature)) {
                aFeatures.add(feature);
              }
            }
          }
          luceneSearcher.annotationTypesMap.put(ga.getType(), aFeatures);
        }
        else {
          HashMap features = ga.getFeatures();
          ArrayList aFeatures = new ArrayList();
          aFeatures.add("All");
          if(features != null) {
            aFeatures.addAll(features.keySet());
          }
          luceneSearcher.annotationTypesMap.put(ga.getType(), aFeatures);
        }
        // end of initializing annotationTypes for the comboBox
      }

      // we have the text
      // smallest is the textStOffset
      // highest is the textEndOffset
      // how to find the patternStartOffset
      int stPos = ((Integer)firstTermPositions.get(i)).intValue();
      int endOffset = ((Integer)patternLength.get(i)).intValue();
      int patStart = Integer.MAX_VALUE;

      for(int j = 0; j < annotations.size(); j++) {
        // each annotation is an instance of GateAnnotation
        gate.creole.annic.PatternAnnotation ga = (gate.creole.annic.PatternAnnotation)annotations
                .get(j);
        if(ga.getPosition() == stPos) {
          if(ga.getStartOffset() < patStart) {
            patStart = ga.getStartOffset();
          }
        }
      }

      if(patStart == Integer.MAX_VALUE) {
        continue;
      }

      if(patStart < smallest || endOffset > highest) {
        continue;
      }

      // now create the pattern for this
      Pattern ap = new Pattern(docID, new String(patternText), patStart,
              endOffset, smallest, highest, annotations, queryString);
      pat.add(ap);
    }
    return pat;
  }

  /**
   * Each index unit is first converted into a separate lucene document.
   * And a new ID with documentName and a unit number is assined to it.
   * But when we return results, we take the unit number out.
   * 
   * @param documentID
   * @return
   */
  private String removeUnitNumber(String documentID) {
    int index = documentID.lastIndexOf("-");
    if(index == -1) return documentID;
    return documentID.substring(0, index);
  }

  /**
   * This method looks on the disk to find the tokenStream
   * 
   * @param location String
   * @throws Exception
   * @return ArrayList
   */
  private ArrayList getTokenStreamFromDisk(String indexDirectory,
          String documentID) throws Exception {
    if(indexDirectory.startsWith("file:/"))
      indexDirectory = indexDirectory.substring(6, indexDirectory.length());

    // use buffering
    File fileToLoad = new File(new File(indexDirectory,
            Constants.SERIALIZED_FOLDER_NAME), documentID + ".annic");
    InputStream file = new FileInputStream(fileToLoad);
    InputStream buffer = new BufferedInputStream(file);
    ObjectInput input = new ObjectInputStream(buffer);

    // deserialize the List
    ArrayList recoveredTokenStream = (ArrayList)input.readObject();
    if(input != null) {
      // close "input" and its underlying streams
      input.close();
    }
    return recoveredTokenStream;
  }

  /**
   * this method takes the tokenStream as a text, the first term
   * positions, pattern length, queryType and patternWindow and returns
   * the GateAnnotations as an array for each pattern with left and
   * right context
   * 
   * @param subTokens
   * @param ftp
   * @param patLen
   * @param qType
   * @param patWindow
   * @param query
   * @param baseTokenAnnotationType
   * @return
   */
  private PatternResult getTokenStream(ArrayList subTokens, int patLen,
          int qType, int patWindow, String query,
          String baseTokenAnnotationType, int numberOfResultsToFetch) {

    /*
     * ok so we first see what kind of query is that two possibilities
     * (Phrase query or Term query) Term query is what contains only one
     * word to seach and Phrase query contains more than one word 1
     * indicates the PhraseQuery
     */
    if(qType == 1) {
      return getTokenStream(subTokens, patLen, patWindow, query,
              baseTokenAnnotationType, numberOfResultsToFetch);
    }
    else {
      /*
       * where the query is Term In term query it is possible that user
       * is searching for the particular annotation type (say: "Token"
       * or may be for text (say: "Hello") query parser converts the
       * annotation type query into Token == "*" and the latter to
       * Token.string == "Hello"
       */

      /*
       * the first element is text. the second element is type
       */
      String annotText = (String)ftp.get(0);
      String annotType = (String)ftp.get(1);

      // so here we search through subTokens and find out the positions
      ArrayList positions = new ArrayList();
      for(int j = 0; j < subTokens.size(); j++) {
        gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                .get(j);
        String type = token.termText();
        String text = token.type();

        // if annotType == "*", the query was {AnnotType}
        if(annotType.equals("*")) {
          if(type.equals(annotText) && annotType.equals(text)) {
            positions.add(new Integer(token.getPosition()));
          }
        }
        // the query is Token == "string"
        else {
          if(annotText.equals(type) && annotType.equals(text)) {
            positions.add(new Integer(token.getPosition()));
          }
        }
      }

      this.ftp = positions;
      // we have positions here
      return getTokenStream(subTokens, 1, patWindow, query,
              baseTokenAnnotationType, numberOfResultsToFetch);
    }
  }

  /**
   * This method returns the valid patterns back and the respective
   * GateAnnotations
   * 
   * @param subTokens ArrayList
   * @param ftp ArrayList
   * @param patLen int
   * @param patWindow int
   * @param query String
   * @return PatternResult
   */
  private PatternResult getTokenStream(ArrayList subTokens, int patLen,
          int patWindow, String query, String baseTokenAnnotationType,
          int noOfResultsToFetch) {

    ArrayList tokens = new ArrayList();
    ArrayList patLens = new ArrayList();
    ftpIndex++;

    // Phrase Query
    // consider only one pattern at a time
    int ftpIndexATB = ftpIndex;
    mainForLoop: for(; ftpIndex < ftp.size()
            && (noOfResultsToFetch == -1 || noOfResultsToFetch > 0); ftpIndex++) {

      // find out the position of the first term
      int pos = ((Integer)ftp.get(ftpIndex)).intValue();

      // find out the token with pos
      int j = 0;
      for(; j < subTokens.size(); j++) {
        gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                .get(j);
        if(token.getPosition() == pos) {
          break;
        }
      }

      int counter = 0;
      int leftstart = -1;
      /*
       * ok so we need to go back to find out the first token of the
       * left context
       */
      int k = j - 1;
      for(; k >= 0; k--) {
        gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                .get(k);
        if(token.getPosition() < pos
                && token.termText().equals(baseTokenAnnotationType)
                && token.type().equals("*")) {
          counter++;
          leftstart = token.startOffset();
          j = k;
        }
        if(counter == patWindow) {
          break;
        }
      }

      // j holds the start of the left context

      // now we want to search for the end of left context
      pos--;
      k = j;

      if(leftstart > -1) {

        boolean breakNow = false;
        for(; k < subTokens.size(); k++) {
          gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                  .get(k);
          if(token.getPosition() == pos) {
            breakNow = true;
          }
          else {
            if(breakNow) {
              break;
            }
          }
        }
      }
      // now k holds the begining of the pattern

      // leftEnd holds the position of the last token in left context
      int leftEnd = leftstart == -1 ? -1 : k - 1;

      /*
       * we need to validate this pattern as a result of query, we get
       * the positions of the first term we need to locate the full
       * pattern along with all its other annotations this is done by
       * using the ValidatePattern class this class provides a method,
       * which takes as arguments the query Tokens the position in the
       * tokenStream from where to start searching and returns the end
       * offset of the last annotation in the found pattern we then
       * search for this endoffset in our current tokenStream to
       * retrieve the wanted annotations
       */
      int upto = -1;
      int tempPos = 0;
      if(this.queryParser.needValidation()) {

        try {

          ArrayList queryTokens = luceneSearcher.getQueryTokens(query);
          if(queryTokens == null) {
            queryTokens = new QueryParser().findTokens(query);
            luceneSearcher.addQueryTokens(query, queryTokens);
          }

          /*
           * validate method returns the endoffset of the last token of
           * the middle pattern returns -1 if pattern could not be
           * located at that location
           */
          PatternValidator vp = new PatternValidator();

          // here k is the position where the first token should occur

          upto = vp.validate(queryTokens, subTokens, k, new QueryParser());
          if(upto == -1) {
            /*
             * if the validatePAttern class could not find the valid
             * pattern it returns -1 and therefore we should remove the
             * position of the invalid pattern
             */
            ftp.remove(ftpIndex);
            ftpIndex--;
            continue mainForLoop;
          }
          else {
            /*
             * now we need to locate the token whose endPosition is upto
             */
            int jj = leftEnd + 1;
            boolean breaknow = false;
            tempPos = ((gate.creole.annic.apache.lucene.analysis.Token)subTokens
                    .get(jj)).getPosition();
            for(; jj < subTokens.size(); jj++) {
              gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                      .get(jj);
              if(token.endOffset() == upto) {
                tempPos = token.getPosition();
                breaknow = true;
              }
              else if(breaknow) {
                break;
              }
            }
            // we send the endoffset to our GUI class
            patLens.add(new Integer(upto));

            /*
             * k holds the position of the first token in right context
             */
            k = jj;
          }
        }
        catch(Exception e) {
          e.printStackTrace();
        }
      }
      else {
        /*
         * the query contains all tokens, which is already validated at
         * the time of creating query the pointer k points to the
         * begining of our patern we need to travel patLen into the
         * right direction to obtain the pattern
         */
        for(counter = 0; counter < patLen && k < subTokens.size(); k++) {
          gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                  .get(k);
          if(token.termText().equals(baseTokenAnnotationType)
                  && token.type().equals("*")) {
            counter++;
            upto = token.endOffset();
            tempPos = token.getPosition();
          }
        }
        patLens.add(new Integer(upto));
        k++;
      }
      int maxEndOffset = upto;

      /*
       * so now search for the token with the position == tempPos + 1 in
       * other words search for the first term of the right context
       */
      for(; k < subTokens.size(); k++) {
        gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                .get(k);
        if(token.getPosition() == tempPos + 1) {
          break;
        }
      }

      // and now we need to locate the right context pattern
      counter = 0;
      for(; k < subTokens.size(); k++) {
        gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                .get(k);
        if(token.startOffset() >= upto
                && token.termText().equals(baseTokenAnnotationType)
                && token.type().equals("*")) {
          counter++;
          maxEndOffset = token.endOffset();
        }
        if(counter == patWindow) {
          break;
        }
      }

      // if there are any sub-tokens left
      if(k < subTokens.size()) {
        /*
         * now we would search for the position untill we see it having
         * the same position
         */
        tempPos = ((gate.creole.annic.apache.lucene.analysis.Token)subTokens
                .get(k)).getPosition();

        for(; k < subTokens.size(); k++) {
          gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                  .get(k);
          if(token.getPosition() != tempPos) {
            break;
          }
        }
      }

      if(k >= subTokens.size()) {
        // we used all sub-tokens - set k to maximum size
        k = subTokens.size() - 1;
      }

      /*
       * so k is the position til where we need to search for each
       * annotation and every feature in it at the time of creating
       * index were converted into separate tokens we need to convert
       * them back into annotations
       */
      ArrayList patternGateAnnotations = new ArrayList();
      PatternAnnotation ga = null;
      for(int m = j; m <= k; m++) {
        gate.creole.annic.apache.lucene.analysis.Token token = (gate.creole.annic.apache.lucene.analysis.Token)subTokens
                .get(m);
        String text = token.termText();
        int st = token.startOffset();
        int end = token.endOffset();
        String type = token.type();
        int position = token.getPosition();

        // if this is a new annotation Type
        if(type.equals("*")) {
          ga = new PatternAnnotation();
          ga.setType(text);
          ga.setStOffset(st);
          ga.setEnOffset(end);
          ga.setPosition(position);
          if(ga.getEndOffset() <= maxEndOffset) {
            patternGateAnnotations.add(ga);
          }
          continue;
        }

        // and from here all are the features
        int index = type.indexOf(".");
        String feature = type.substring(index + 1, type.length());
        /*
         * we need to compare the type1 each annotation has string
         * feature in index so text will be definitely going to be
         * initialized
         */
        if(feature.equals("string")) {
          ga.setText(text);
        }
        ga.addFeature(feature, text);
      }
      tokens.add(patternGateAnnotations);
      if(noOfResultsToFetch != -1) noOfResultsToFetch--;
    }

    if(noOfResultsToFetch == 0 && ftpIndex < ftp.size()) ftpIndex--;

    // finally create an instance of PatternResult
    PatternResult pr = new PatternResult();
    pr.gateAnnotations = tokens;
    pr.firstTermPositions = new ArrayList();
    for(int i = 0; i < pr.gateAnnotations.size(); i++) {
      pr.firstTermPositions.add(ftp.get(i + ftpIndexATB));
    }
    pr.patternLegths = patLens;
    pr.numberOfPatterns = pr.gateAnnotations.size();
    return pr;
  }

  /**
   * Inner class to store pattern results.
   * 
   * @author niraj
   */
  private class PatternResult {
    int numberOfPatterns;

    ArrayList gateAnnotations;

    ArrayList firstTermPositions;

    ArrayList patternLegths;
  }

  /**
   * Inner class to store query Item.
   * 
   * @author niraj
   * 
   */
  private class QueryItem {
    float score;

    int id;

    ArrayList ftp;

    int patLen;

    int qType;

    Query query;

    String queryString;

    public boolean equals(Object m) {
      if(m instanceof QueryItem) {
        QueryItem n = (QueryItem)m;
        return n.score == score && n.id == id && n.patLen == patLen
                && n.qType == qType && n.queryString.equals(queryString)
                && areTheyEqual(n.ftp, ftp, qType);
      }
      return false;
    }
  }

  /**
   * Checks if the QueryItem already exists.
   * 
   * @param n
   * @param top
   * @return
   */
  private boolean doesAlreadyExist(QueryItem n, ArrayList top) {

    for(int i = 0; i < top.size(); i++) {
      QueryItem m = (QueryItem)top.get(i);
      if(m.equals(n)) return true;
    }
    return false;
  }

  /**
   * Checks if two first term positions are identical. 
   * @param ftp
   * @param ftp1
   * @param qType
   * @return
   */
  private boolean areTheyEqual(ArrayList ftp, ArrayList ftp1, int qType) {
    if(qType == 1) {
      if(ftp.size() == ftp1.size()) {
        for(int i = 0; i < ftp.size(); i++) {
          int pos = ((Integer)ftp.get(i)).intValue();
          int pos1 = ((Integer)ftp1.get(i)).intValue();
          if(pos != pos1) return false;
        }
        return true;
      }
      else {
        return false;
      }
    }
    else {
      String annotText = (String)ftp.get(0);
      String annotType = (String)ftp.get(1);
      String annotText1 = (String)ftp1.get(0);
      String annotType1 = (String)ftp1.get(1);
      return annotText1.equals(annotText) && annotType1.equals(annotType);
    }
  }

  /**
   * Gets the query.
   * @return
   */
  public String getQuery() {
    return query;
  }

}
