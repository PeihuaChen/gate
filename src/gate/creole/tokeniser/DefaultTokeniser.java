/**
 * Title:        GATE<p>
 * Description:  <p>
 * Copyright:    Copyright (c) 1999<p>
 * Company:      Univ Sheffield<p>
 * @author Hamish, Kalina, Christian, Valentin
 * @version
 */
package gate.creole.tokeniser;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import gate.*;
import gate.gui.*;
import gate.util.*;
import gate.fsm.TestFSM;
import EDU.auburn.VGJ.graph.ParseError;

/**
  *Implementation of a Unicode rule based tokeniser.
  *The tokeniser gets its rules from a file an {@link java.io.InputStream
  *InputStream} or a {@link java.io.Reader Reader} which should be sent to one
  *of the constructors.
  *The implementations is based on a finite state machine that is built based on
  *the set of rules.
  *A rule has two sides, the left hand side (LHS)and the right hand side (RHS)
  *that are separated by the &quot;&gt;&quot; character. The LHS represents a
  *regular expression that will be matched against the input while the RHS
  *describes a Gate2 annotation in terms of annotation type and attribute-value
  *pairs.
  *The matching is done using Unicode enumarated types as defined by the {@link
  *java.lang.Character Character} class. At the time of writing this class the
  *suported Unicode categories were:
  *<ul>
  *<li>UNASSIGNED
  *<li>UPPERCASE_LETTER
  *<li>LOWERCASE_LETTER
  *<li>TITLECASE_LETTER
  *<li>MODIFIER_LETTER
  *<li>OTHER_LETTER
  *<li>NON_SPACING_MARK
  *<li>ENCLOSING_MARK
  *<li>COMBINING_SPACING_MARK
  *<li>DECIMAL_DIGIT_NUMBER
  *<li>LETTER_NUMBER
  *<li>OTHER_NUMBER
  *<li>SPACE_SEPARATOR
  *<li>LINE_SEPARATOR
  *<li>PARAGRAPH_SEPARATOR
  *<li>CONTROL
  *<li>FORMAT
  *<li>PRIVATE_USE
  *<li>SURROGATE
  *<li>DASH_PUNCTUATION
  *<li>START_PUNCTUATION
  *<li>END_PUNCTUATION
  *<li>CONNECTOR_PUNCTUATION
  *<li>OTHER_PUNCTUATION
  *<li>MATH_SYMBOL
  *<li>CURRENCY_SYMBOL
  *<li>MODIFIER_SYMBOL
  *<li>OTHER_SYMBOL
  *</ul>
  *The accepted operators for the LHS are "+", "*" and "|" having the usual
  *interpretations of "1 to n occurences", "0 to n occurences" and "boolean OR".
  *For instance this is a valid LHS:
  *<br>"UPPERCASE_LETTER" "LOWERCASE_LETTER"+
  *<br>meaning an uppercase letter followed by one or more lowercase letters.
  *
  *The RHS describes an annotation that is to be created and inserted in the
  *annotation set provided in case of a match. The new annotation will span the
  *text that has been recognised. The RHS consists in the annotation type
  *followed by pairs of attributes and associated values.
  *E.g. for the LHS above a possible RHS can be:<br>
  *Token;kind=upperInitial;<br>
  *representing an annotation of type &quot;Token&quot; having one attribute
  *named &quot;kind&quot; with the value &quot;upperInitial&quot;<br>
  *The entire rule willbe:<br>
  *<pre>"UPPERCASE_LETTER" "LOWERCASE_LETTER"+ > Token;kind=upperInitial;</pre>
  *<br>
  *The tokeniser ignores all the empty lines or the ones that start with # or
  * //.
 */
public class DefaultTokeniser implements Runnable, ProcessingResource,
                                         ProcessProgressReporter,
                                         StatusReporter{
  protected DefaultTokeniser(){
  }

  /**Constructs a DefaultTokeniser from the file with the name specified by
    *ruleFile
    *@throws FileNotFoundException if the file cannot be found.
    *@throws IOException if an I/O error occurs during reading the rules
    *@throws TokeniserException in case of a malformed rule.
  */
  public DefaultTokeniser(String rulesFile) throws FileNotFoundException,
                                                   IOException,
                                                   TokeniserException {
    this(new FileReader(rulesFile));
  }

  /**Constructs a DefaultTokeniser from an {@link java.io.InputStream
    *InputStream}.
    *@throws IOException if an I/O error occurs during reading the rules
    *@throws TokeniserException in case of a malformed rule.
  */
  public DefaultTokeniser(InputStream rulesIOStr) throws IOException,
                                                   TokeniserException {
    this(new InputStreamReader(rulesIOStr));
  }

  /**Constructs a DefaultTokeniser from a {@link java.io.Reader Reader}.
    *@throws IOException if an I/O error occurs during reading the rules
    *@throws TokeniserException in case of a malformed rule.
    */
  public DefaultTokeniser(Reader rulesRdr) throws IOException,
                                                  TokeniserException {
    initialState = new FSMState(this);
    BufferedReader rulesReader = new BufferedReader(rulesRdr);
    String line = rulesReader.readLine();
    String toParse = "";
    while (line != null){
      if(line.endsWith("\\")){
        toParse += line.substring(0,line.length()-1);
      }else{
        toParse += line;
        parseRule(toParse);
        toParse = "";
      }
      line = rulesReader.readLine();
    }
//System.out.println("\n\n" + getFSMgml());
//    try{
//      gate.fsm.TestFSM.showGraph("Tokeniser graph (Non deterministic)", getFSMgml());
      eliminateVoidTransitions();
//System.out.println("\n\n" + getDFSMgml());
//      gate.fsm.TestFSM.showGraph("Tokeniser graph (deterministic)", getDFSMgml());
//    }catch(EDU.auburn.VGJ.graph.ParseError pe){pe.printStackTrace(System.err);}
  }

  /**Parses one input line containing a tokeniser rule.
    *This will create the necessary FSMState objects and the links between them.
    *@param line the string containing the rule
    */
  void parseRule(String line)throws TokeniserException{
    //ignore comments
    if(line.startsWith("#")) return;
    if(line.startsWith("//")) return;
    StringTokenizer st = new StringTokenizer(line, "()+*|\" \t\f>", true);
    FSMState newState = new FSMState(this);
    initialState.put(null, newState);
    FSMState finalState = parseLHS(newState, st, LHStoRHS);
    String rhs = "";
    if(st.hasMoreTokens()) rhs = st.nextToken("\f");
    if(rhs.length() > 0)finalState.setRhs(rhs);
  }

  /**Parses a part or the entire LHS.
    *@param startState a FSMState object representing the initial state for the
    *small FSM that will recognise the (part of) the rule parsed by this method.
    *@param st a {@link java.util.StringTokenizer StringTokenizer} that provides
    *the input
    *@param until the string that marks the end of the section to be recognised.
    *This method will first be called by {@link #parseRule(String)} with &quot;
    *&gt;&quot; in order to parse the entire LHS.
    *when necessary it will make itself another call to {@link #parseLHS
    *parseLHS} to parse a region of the LHS (e.g. a &quot;(&quot;,&quot;)&quot;
    *enclosed part.
    */
  FSMState parseLHS(FSMState startState, StringTokenizer st, String until)
       throws TokeniserException{
    FSMState currentState = startState;
    boolean orFound = false;
    List orList = new LinkedList();
    String token;
    token = skipIgnoreTokens(st);
    if(null == token) return currentState;
    FSMState newState;
    Integer typeId;
    UnicodeType uType;
    bigwhile: while(!token.equals(until)){
      if(token.equals("(")){//(..)
        newState = parseLHS(currentState, st,")");
      }else if(token.equals("\"")){//"unicode_type"
        String sType = parseQuotedString(st, "\"");
//System.out.println(sType);
        newState = new FSMState(this);
        typeId = (Integer)stringTypeIds.get(sType);
        if(null == typeId)
          throw new InvalidRuleException("Invalid type: \"" + sType + "\"");
        else uType = new UnicodeType(typeId.intValue());
        currentState.put(uType ,newState);
      }else{// a type with no quotes
        String sType = token;
//System.out.println(sType);
        newState = new FSMState(this);
        typeId = (Integer)stringTypeIds.get(sType);
        if(null == typeId)
          throw new InvalidRuleException("Invalid type: \"" + sType + "\"");
        else uType = new UnicodeType(typeId.intValue());
        currentState.put(uType ,newState);
      }
      //treat the operators
      token = skipIgnoreTokens(st);
      if(null == token) throw
                        new InvalidRuleException("Tokeniser rule ended too soon!");
      if(token.equals("|")){
        orFound = true;
        orList.add(newState);
        token = skipIgnoreTokens(st);
        if(null == token) throw
                          new InvalidRuleException("Tokeniser rule ended too soon!");
        continue bigwhile;
      }else if(orFound){//done parsing the "|"
        orFound = false;
        orList.add(newState);
        newState = new FSMState(this);
        Iterator orListIter = orList.iterator();
        while(orListIter.hasNext())
          ((FSMState)orListIter.next()).put(null, newState);
        orList.clear();
      }
      if(token.equals("+")){
        newState.put(null,currentState);
        currentState = newState;
        newState = new FSMState(this);
        currentState.put(null,newState);
        token = skipIgnoreTokens(st);
        if(null == token) throw
                          new InvalidRuleException("Tokeniser rule ended too soon!");
      }else if(token.equals("*")){
        currentState.put(null,newState);
        newState.put(null,currentState);
        currentState = newState;
        newState = new FSMState(this);
        currentState.put(null,newState);
        token = skipIgnoreTokens(st);
        if(null == token) throw
                          new InvalidRuleException("Tokeniser rule ended too soon!");
      }
      currentState = newState;
    }
    return currentState;
  }

  /**Parses a quoted string returning all the text up to a given delimiter.
    *@param st a {@link java.util.StringTokenizer StringTokenizer} that provides
    *the input
    *@param until a String representing the end delimiter.
    */
  String parseQuotedString(StringTokenizer st, String until)
    throws TokeniserException{
    String token;
    if(st.hasMoreElements()) token = st.nextToken();
    else return null;
    String type = "";
    while(!token.equals(until)){
      type += token;
      if(st.hasMoreElements())token = st.nextToken();
      else throw new InvalidRuleException("Tokeniser rule ended too soon!");
    }
    return type;
  }

  /**Skips the ignorables tokens from the input returning the first significant
    *token.
    *The ignorable tokens are defined by {@link #ignoreTokens a set}
    */
  public static String skipIgnoreTokens(StringTokenizer st){
    Iterator ignorables;
    boolean ignorableFound = false;
    String currentToken;
    while(true){
      if(st.hasMoreTokens()){
        currentToken = st.nextToken();
        ignorables = ignoreTokens.iterator();
        ignorableFound = false;
        while(!ignorableFound && ignorables.hasNext()){
          if(currentToken.equals((String)ignorables.next()))
            ignorableFound = true;
        }
        if(!ignorableFound) return currentToken;
      }else return null;
    }
  }

  void parseRHS(String rhs){
  }
  /*
  * Computes the lambda-closure (aka epsilon closure) of the given set of
  * states, that is the set of states that are accessible from any of the
  *states in the given set using only unrestricted transitions.
  *@return a set containing all the states accessible from this state via
  *transitions that bear no restrictions.
  */
  private AbstractSet lambdaClosure(Set s){
    //the stack/queue used by the algorithm
    LinkedList list = new LinkedList(s);
    //the set to be returned
    AbstractSet lambdaClosure = new HashSet(s);
    FSMState top;
    FSMState currentState;
    Set nextStates;
    Iterator statesIter;
    while(!list.isEmpty()){
      top = (FSMState)list.removeFirst();
      nextStates = top.nextSet(null);
      if(null != nextStates){
        statesIter = nextStates.iterator();
        while(statesIter.hasNext()){
          currentState = (FSMState)statesIter.next();
          if(!lambdaClosure.contains(currentState)){
            lambdaClosure.add(currentState);
            list.addFirst(currentState);
          }//if(!lambdaClosure.contains(currentState))
        }//while(statesIter.hasNext())
      }//if(null != nextStates)
    }
    return lambdaClosure;
  }

  /**
  * Converts the FSM from a non-deterministic to a deterministic one by
  * eliminating all the unrestricted transitions.
  */
  void eliminateVoidTransitions() throws TokeniserException{
    Map newStates = new HashMap();
    Set sdStates = new HashSet();
    LinkedList unmarkedDStates = new LinkedList();
    DFSMState dCurrentState = new DFSMState(this);
    Set sdCurrentState = new HashSet();
    sdCurrentState.add(initialState);
    sdCurrentState = lambdaClosure(sdCurrentState);
    newStates.put(sdCurrentState, dCurrentState);
    sdStates.add(sdCurrentState);
    //find out if the new state is a final one
    Iterator innerStatesIter = sdCurrentState.iterator();
    String rhs;
    FSMState currentInnerState;
    Set rhsClashSet = new HashSet();
    boolean newRhs = false;
    while(innerStatesIter.hasNext()){
      currentInnerState = (FSMState)innerStatesIter.next();
      if(currentInnerState.isFinal()){
        rhs = currentInnerState.getRhs();
        rhsClashSet.add(rhs);
        dCurrentState.rhs = rhs;
        newRhs = true;
      }
    }
    if(rhsClashSet.size() > 1){
      System.err.println("Warning, rule clash: " +  rhsClashSet +
                         "\nSelected last definition: " + dCurrentState.rhs);
    }
    if(newRhs)dCurrentState.buildTokenDesc();
    rhsClashSet.clear();
    unmarkedDStates.addFirst(sdCurrentState);
    dInitialState = dCurrentState;
    Set nextSet;
    while(!unmarkedDStates.isEmpty()){
//System.out.println("\n\n=====================" + unmarkedDStates.size());
      sdCurrentState = (Set)unmarkedDStates.removeFirst();
      for(int type = 0; type < maxTypeId; type++){
//System.out.print(type);
        nextSet = new HashSet();
        innerStatesIter = sdCurrentState.iterator();
        while(innerStatesIter.hasNext()){
          currentInnerState = (FSMState)innerStatesIter.next();
          Set tempSet = currentInnerState.nextSet(type);
          if(null != tempSet) nextSet.addAll(tempSet);
        }//while(innerStatesIter.hasNext())
        if(!nextSet.isEmpty()){
          nextSet = lambdaClosure(nextSet);
          dCurrentState = (DFSMState)newStates.get(nextSet);
          if(dCurrentState == null){
            //we have a new DFSMState
            dCurrentState = new DFSMState(this);
            sdStates.add(nextSet);
            unmarkedDStates.add(nextSet);
            //check to see whether the new state is a final one
            innerStatesIter = nextSet.iterator();
            newRhs =false;
            while(innerStatesIter.hasNext()){
              currentInnerState = (FSMState)innerStatesIter.next();
              if(currentInnerState.isFinal()){
                rhs = currentInnerState.getRhs();
                rhsClashSet.add(rhs);
                dCurrentState.rhs = rhs;
                newRhs = true;
              }
            }
            if(rhsClashSet.size() > 1){
              System.err.println("Warning, rule clash: " +  rhsClashSet +
                                 "\nSelected last definition: " + dCurrentState.rhs);
            }
            if(newRhs)dCurrentState.buildTokenDesc();
            rhsClashSet.clear();
            newStates.put(nextSet, dCurrentState);
          }
          ((DFSMState)newStates.get(sdCurrentState)).put(type,dCurrentState);
        }//if(!nextSet.isEmpty())
      }//for(byte type = 0; type < 256; type++)
    }//while(!unmarkedDStates.isEmpty())
  }

  /**Returns a string representation of the non-deterministic FSM graph using
    *GML.
    */
  public String getFSMgml(){
    String res = "graph[ \ndirected 1\n";
    String nodes = "", edges = "";
    Iterator fsmStatesIter = fsmStates.iterator();
    while (fsmStatesIter.hasNext()){
      FSMState currentState = (FSMState)fsmStatesIter.next();
      int stateIndex = currentState.getIndex();
      nodes += "node[ id " + stateIndex +
               " label \"" + stateIndex;
             if(currentState.isFinal()){
              nodes += ",F\\n" + currentState.getRhs();
             }
             nodes +=  "\"  ]\n";
      edges += currentState.getEdgesGML();
    }
    res += nodes + edges + "]\n";
    return res;
  }

  /**Returns a string representation of the deterministic FSM graph using
    *GML.
    */
  public String getDFSMgml(){
    String res = "graph[ \ndirected 1\n";
    String nodes = "", edges = "";
    Iterator dfsmStatesIter = dfsmStates.iterator();
    while (dfsmStatesIter.hasNext()){
      DFSMState currentState = (DFSMState)dfsmStatesIter.next();
      int stateIndex = currentState.getIndex();
      nodes += "node[ id " + stateIndex +
               " label \"" + stateIndex;
             if(currentState.isFinal()){
              nodes += ",F\\n" + currentState.getRhs();
             }
             nodes +=  "\"  ]\n";
      edges += currentState.getEdgesGML();
    }
    res += nodes + edges + "]\n";
    return res;
  }

//no doc required: javadoc will copy it from the interface
  public FeatureMap getFeatures(){
    return features;
  }

  public void setFeatures(FeatureMap features){
    this.features = features;
  }

  /** Tokenises the given document writting all the generated annotations in
    * the provided annotation set.
    * It is the user's responsability to make sure that the annotation set
    * provided belongs to the document as the tokeniser will not make any
    * checks.
    *@param doc the document to be tokenised
    *@param annotationSet the AnnotationSet where the new annotations will be
    *added
    *@param runInNewThread if <b>true</b> the tokeniser will spawn a new thread
    *for doing all the processing, if <b>false</b> all the priocessing will
    *take place in the current thread and this method will block until the
    *tokenisation is done.
    */
  public void tokenise(Document doc, AnnotationSet annotationSet,
                       boolean runInNewThread){
    this.doc =  doc;
    this.annotationSet = annotationSet;
    if(runInNewThread){
      Thread thread = new Thread(this);
      thread.start();
    }else run();
  }

  /** Tokenises the given document writting all the generated annotations in
    * the default annotation set.
    */
  public void tokenise(Document doc, boolean runInNewThread){
    tokenise(doc, doc.getAnnotations(), runInNewThread);
  }

  /**The method that does the actual tokenisation. This method should not be
    *explicitly called but the {@link #tokenise tokenise} method should be
    *used instead.
    */
  public void run(){
    fireStatusChangedEvent("Tokenising " + doc.getSourceURL().getFile() + "...");
    String content = doc.getContent().toString();
    int length = content.length();
    char currentChar;
    DFSMState graphPosition = dInitialState;
    //the index of the first character of the token trying to be recognised
    int tokenStart = 0;
    //the index of the last character of the last token recognised
    int lastMatch = -1;
    DFSMState lastMatchingState = null;
    DFSMState nextState;
    String tokenString;
    int charIdx = 0;
    int oldCharIdx = 0;
    FeatureMap newTokenFm;
    while(charIdx < length){
      currentChar = content.charAt(charIdx);
//System.out.println(currentChar + typesMnemonics[Character.getType(currentChar)+128]);
      nextState = graphPosition.next(((Integer)typeIds.get(
                  new Integer(Character.getType(currentChar)))).intValue());
      if(null != nextState){
        graphPosition = nextState;
        if(graphPosition.isFinal()){
          lastMatch = charIdx;
          lastMatchingState = graphPosition;
        }
        charIdx ++;
      }else{//we have a match!
        newTokenFm = Transients.newFeatureMap();
        if(null == lastMatchingState){
          tokenString = content.substring(tokenStart, tokenStart +1);
          newTokenFm.put("type","UNKNOWN");
          newTokenFm.put("string", tokenString);
          newTokenFm.put("length", Integer.toString(tokenString.length()));
          try{
            annotationSet.add(new Long(tokenStart),
                              new Long(tokenStart + 1),
                              "DEFAULT_TOKEN", newTokenFm);
          }catch(InvalidOffsetException ioe){
            //This REALLY shouldn't happen!
            ioe.printStackTrace(System.err);
          }
//          System.out.println("Default token: " + tokenStart +
//                             "->" + tokenStart + " :" + tokenString + ";");
          charIdx  = tokenStart + 1;
        }else{
          tokenString = content.substring(tokenStart, lastMatch + 1);
          newTokenFm.put("string", tokenString);
          newTokenFm.put("length", Integer.toString(tokenString.length()));
          for(int i = 1; i < lastMatchingState.getTokenDesc().length; i++){
            newTokenFm.put(lastMatchingState.getTokenDesc()[i][0],
                           lastMatchingState.getTokenDesc()[i][1]);
//System.out.println(lastMatchingState.getTokenDesc()[i][0] + "=" +
//                           lastMatchingState.getTokenDesc()[i][1]);
          }
          try{
            annotationSet.add(new Long(tokenStart),
                              new Long(lastMatch + 1),
                              lastMatchingState.getTokenDesc()[0][0], newTokenFm);
          }catch(InvalidOffsetException ioe){
            //This REALLY shouldn't happen!
            throw new LuckyException(ioe.toString());
          }
//          System.out.println(lastMatchingState.getTokenDesc()[0][0] +
//                             ": " + tokenStart + "->" + lastMatch +
//                             " :" + tokenString + ";");
          charIdx = lastMatch + 1;
        }
        lastMatchingState = null;
        graphPosition = dInitialState;
        tokenStart = charIdx;
      }
      if(charIdx - oldCharIdx > 256){
        fireProgressChangedEvent((100 * charIdx )/ length );
        oldCharIdx = charIdx;
      }
    }//while(charIdx < length)

    if(null != lastMatchingState){
      tokenString = content.substring(tokenStart, lastMatch + 1);
      newTokenFm = Transients.newFeatureMap();
      newTokenFm.put("string", tokenString);
      newTokenFm.put("length", Integer.toString(tokenString.length()));
      for(int i = 1; i < lastMatchingState.getTokenDesc().length; i++){
        newTokenFm.put(lastMatchingState.getTokenDesc()[i][0],
                       lastMatchingState.getTokenDesc()[i][1]);
      }
      try{
        annotationSet.add(new Long(tokenStart),
                          new Long(lastMatch + 1),
                          lastMatchingState.getTokenDesc()[0][0], newTokenFm);
      }catch(InvalidOffsetException ioe){
        //This REALLY shouldn't happen!
        ioe.printStackTrace(System.err);
      }
    }
    fireProcessFinishedEvent();
    fireStatusChangedEvent("Tokenisation complete!");
  }

  //StatusReporter Implementation
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }
  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }
  protected void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }

  //ProcessProgressReporter implementation
  public void addProcessProgressListener(ProgressListener listener){
    myProgressListeners.add(listener);
  }

  public void removeProcessProgressListener(ProgressListener listener){
    myProgressListeners.remove(listener);
  }

  protected void fireProgressChangedEvent(int i){
    Iterator listenersIter = myProgressListeners.iterator();
    while(listenersIter.hasNext())
      ((ProgressListener)listenersIter.next()).progressChanged(i);
  }

  protected void fireProcessFinishedEvent(){
    Iterator listenersIter = myProgressListeners.iterator();
    while(listenersIter.hasNext())
      ((ProgressListener)listenersIter.next()).processFinished();
  }
  //ProcessProgressReporter implementation ends here

  static public void main(String[] args){
    try{
      DefaultTokeniser dt = new DefaultTokeniser(Files.getResourceAsStream(
                            "creole/tokeniser/DefaultTokeniser.rules"));
      Document doc = Transients.newDocument("Germany England and France   are countries that use ... $$$.");
      dt.tokenise(doc, false);
    }catch(Exception ex){ex.printStackTrace(System.err);}
  }

  public Factory getFactory(){
    return new Transients();
  }

  protected FeatureMap features  = null;
  protected List myProgressListeners = new LinkedList();
  protected List myStatusListeners = new LinkedList();
  protected Document doc;
  protected AnnotationSet annotationSet;

  /**The initial state of the non deterministic machine*/
  protected FSMState initialState;

  /**A set containng all the states of the non deterministic machine*/
  protected Set fsmStates = new HashSet();

  /**The initial state of the deterministic machine*/
  protected DFSMState dInitialState;

  /**A set containng all the states of the deterministic machine*/
  protected Set dfsmStates = new HashSet();

  /**Maps from the String description to the int value representing Unicode
    *categories
    */
//  public static Map characterTypes;

  /**Maps from the int associated to the unicode category to its name*/
//  public static String[] typesMnemonics;

  /**The separator from LHS to RHS*/
  static String LHStoRHS = ">";

  /**A set of string representing tokens to be ignored (e.g. blanks)*/
  static Set ignoreTokens;


//======================
  /**maps from int (the static value on {@link java.lang.Character} to int
    *the internal value used by the tokeniser
    */
  public static Map typeIds;

  /**The maximum int value used internally as a type id*/
  public static int maxTypeId;

  /**Maps the internal type ids to the type names*/
  public static String[] typeMnemonics;

  /**Maps from type names to type internal ids*/
  public static Map stringTypeIds;
//========================
  static{
    Field[] characterClassFields;
    try{
      characterClassFields = Class.forName("java.lang.Character").getFields();
    }catch(ClassNotFoundException cnfe){
      throw new LuckyException("Could not find the java.lang.Character class!");
    }
    Collection staticFields = new LinkedList();
    for(int i = 0; i< characterClassFields.length; i++)
      if(Modifier.isStatic(characterClassFields[i].getModifiers()))
         staticFields.add(characterClassFields[i]);
    typeIds = new HashMap();
    maxTypeId = staticFields.size() -1;
    typeMnemonics = new String[maxTypeId + 1];
    stringTypeIds = new HashMap();
    Iterator staticFieldsIter = staticFields.iterator();
    Field currentField;
    int currentId = 0;
    String fieldName;
    try{
      while(staticFieldsIter.hasNext()){
        currentField = (Field)staticFieldsIter.next();
        if(currentField.getType().toString().equals("byte")){
          fieldName = currentField.getName();
          typeIds.put(new Integer(currentField.getInt(null)), new Integer(currentId));
          typeMnemonics[currentId] = fieldName;
          stringTypeIds.put(fieldName, new Integer(currentId));
          currentId++;
        }
      }
    }catch(Exception e){
      throw new LuckyException(e.toString());
    }
/*
    characterTypes = new HashMap();
    characterTypes.put("UPPERCASE_LETTER", new UnicodeType(Character.UPPERCASE_LETTER));
    characterTypes.put("LOWERCASE_LETTER", new UnicodeType(Character.LOWERCASE_LETTER));
    characterTypes.put("TITLECASE_LETTER", new UnicodeType(Character.TITLECASE_LETTER));
    characterTypes.put("MODIFIER_LETTER", new UnicodeType(Character.MODIFIER_LETTER));
    characterTypes.put("OTHER_LETTER", new UnicodeType(Character.OTHER_LETTER));
    characterTypes.put("NON_SPACING_MARK", new UnicodeType(Character.NON_SPACING_MARK));
    characterTypes.put("ENCLOSING_MARK", new UnicodeType(Character.ENCLOSING_MARK));
    characterTypes.put("COMBINING_SPACING_MARK", new UnicodeType(Character.COMBINING_SPACING_MARK));
    characterTypes.put("DECIMAL_DIGIT_NUMBER", new UnicodeType(Character.DECIMAL_DIGIT_NUMBER));
    characterTypes.put("LETTER_NUMBER", new UnicodeType(Character.LETTER_NUMBER));
    characterTypes.put("OTHER_NUMBER", new UnicodeType(Character.OTHER_NUMBER));
    characterTypes.put("SPACE_SEPARATOR", new UnicodeType(Character.SPACE_SEPARATOR));
    characterTypes.put("LINE_SEPARATOR", new UnicodeType(Character.LINE_SEPARATOR));
    characterTypes.put("PARAGRAPH_SEPARATOR", new UnicodeType(Character.PARAGRAPH_SEPARATOR));
    characterTypes.put("CONTROL", new UnicodeType(Character.CONTROL));
    characterTypes.put("FORMAT", new UnicodeType(Character.FORMAT));
    characterTypes.put("SURROGATE", new UnicodeType(Character.SURROGATE));
    characterTypes.put("DASH_PUNCTUATION", new UnicodeType(Character.DASH_PUNCTUATION));
    characterTypes.put("START_PUNCTUATION", new UnicodeType(Character.START_PUNCTUATION));
    characterTypes.put("END_PUNCTUATION", new UnicodeType(Character.END_PUNCTUATION));
    characterTypes.put("CONNECTOR_PUNCTUATION", new UnicodeType(Character.CONNECTOR_PUNCTUATION));
    characterTypes.put("OTHER_PUNCTUATION", new UnicodeType(Character.OTHER_PUNCTUATION));
    characterTypes.put("MATH_SYMBOL", new UnicodeType(Character.MATH_SYMBOL));
    characterTypes.put("CURRENCY_SYMBOL", new UnicodeType(Character.CURRENCY_SYMBOL));
    characterTypes.put("MODIFIER_SYMBOL", new UnicodeType(Character.MODIFIER_SYMBOL));
    characterTypes.put("OTHER_SYMBOL", new UnicodeType(Character.OTHER_SYMBOL));

    typesMnemonics = new String[257];
    typesMnemonics[Character.UPPERCASE_LETTER + 128] = "UPPERCASE_LETTER";
    typesMnemonics[Character.LOWERCASE_LETTER + 128] = "LOWERCASE_LETTER";
    typesMnemonics[Character.TITLECASE_LETTER + 128] = "TITLECASE_LETTER";
    typesMnemonics[Character.MODIFIER_LETTER + 128] = "MODIFIER_LETTER";
    typesMnemonics[Character.OTHER_LETTER + 128] = "OTHER_LETTER";
    typesMnemonics[Character.NON_SPACING_MARK + 128] = "NON_SPACING_MARK";
    typesMnemonics[Character.ENCLOSING_MARK + 128] = "ENCLOSING_MARK";
    typesMnemonics[Character.COMBINING_SPACING_MARK + 128] = "COMBINING_SPACING_MARK";
    typesMnemonics[Character.DECIMAL_DIGIT_NUMBER + 128] = "DECIMAL_DIGIT_NUMBER";
    typesMnemonics[Character.LETTER_NUMBER + 128] = "LETTER_NUMBER";
    typesMnemonics[Character.OTHER_NUMBER + 128] = "OTHER_NUMBER";
    typesMnemonics[Character.SPACE_SEPARATOR + 128] = "SPACE_SEPARATOR";
    typesMnemonics[Character.LINE_SEPARATOR + 128] = "LINE_SEPARATOR";
    typesMnemonics[Character.PARAGRAPH_SEPARATOR + 128] = "PARAGRAPH_SEPARATOR";
    typesMnemonics[Character.CONTROL + 128] = "CONTROL";
    typesMnemonics[Character.FORMAT + 128] = "FORMAT";
    typesMnemonics[Character.SURROGATE + 128] = "SURROGATE";
    typesMnemonics[Character.DASH_PUNCTUATION + 128] = "DASH_PUNCTUATION";
    typesMnemonics[Character.START_PUNCTUATION + 128] = "START_PUNCTUATION";
    typesMnemonics[Character.END_PUNCTUATION + 128] = "END_PUNCTUATION";
    typesMnemonics[Character.CONNECTOR_PUNCTUATION + 128] = "CONNECTOR_PUNCTUATION";
    typesMnemonics[Character.OTHER_PUNCTUATION + 128] = "OTHER_PUNCTUATION";
    typesMnemonics[Character.MATH_SYMBOL + 128] = "MATH_SYMBOL";
    typesMnemonics[Character.CURRENCY_SYMBOL + 128] = "CURRENCY_SYMBOL";
    typesMnemonics[Character.MODIFIER_SYMBOL + 128] = "MODIFIER_SYMBOL";
    typesMnemonics[Character.OTHER_SYMBOL + 128] = "OTHER_SYMBOL";
*/
    ignoreTokens = new HashSet();
    ignoreTokens.add(" ");
    ignoreTokens.add("\t");
    ignoreTokens.add("\f");
  }//static initializer


}//class DefaultTokeniser






