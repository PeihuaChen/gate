/*
 * LocaleHandler.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan, October 2000
 *
 * $Id$
 */
package guk.im;

/**
 * Title:        Gate Unicode Kit
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:
 * @author
 * @version
 */
import java.io.*;
import java.util.*;
import java.awt.event.*;

import guk.*;

/**
 * A Handler for a locale.
 * A locale handler is actually a finite state machine (FSM) that maps input events (presseed keys) to other input events(typed characters).
 *
 */
public class LocaleHandler{
  /**
   * Creates a locale handler for a given locale using the definitions from the file provided.
   *
   * @exception IOException
   * @param locale
   * @param fileName
   */
  public LocaleHandler(Locale locale, String fileName) throws IOException{
  //System.out.println("Loading " + fileName);
    this.locale = locale;
    InputStream is = GateIM.class.getResourceAsStream(GateIM.getIMBase() + fileName);
	  if (is==null) throw new IllegalArgumentException
	   ("Failed to retrieve resource '"+fileName+"'. Please reset classpath.");
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line = br.readLine();
    initialState = new State();

    String remains;
    String keyStr;
    String keycapStr;
    String keymapName;
    String number;
    String output;
    keycap = new HashMap();
    int start, end;
    while(line != null){
  //System.out.println(line);
      //skip comments and empty lines
      line = line.trim();
      if( line.startsWith("#") || line.startsWith("//" ) ||
          line.length() == 0 ){
        line = br.readLine();
        continue;
      }
      try{
        remains = line;
        keycapStr = null;
        keymapName = null;
        if(remains.startsWith("bind")){
          //bind declaration
          //skip the bind
          remains = remains.substring(4).trim();
          //get the key string
          keyStr = "";
          start = remains.indexOf('\"');
          for(end = start + 1 ; remains.charAt(end)!='\"'; end++){
            if(remains.charAt(end) == '\\') end++;
            keyStr += remains.charAt(end);
          }
          remains = remains.substring(end + 1).trim();
          if(remains.startsWith("digit")){
            //digit declaration
            //skip the "digit"
            remains = remains.substring(5).trim();
            //read the hex number(s)
            output = "";
            while(remains.startsWith("0x")){
              //read the hex number(s)
              number = remains.substring(2,6);
              output += (char)Integer.parseInt(number, 16);
              //do not trim so we can get out after the first number is read
              remains = remains.substring(6);
            }
            remains = remains.trim();

            //read the second number if it exists and ignore the first
            if(remains.length() > 0){
              output = "";
              while(remains.startsWith("0x")){
                //read the hex number(s)
                number = remains.substring(2,6);
                output += (char)Integer.parseInt(number, 16);
                //do not trim so we can get out after the first number is read
                remains = remains.substring(6);
              }
            }
            addAction(keyStr, output, output);
            //we're through with this line
          }else if(remains.startsWith("send")){
            //send declaration
            //skip the send
            remains = remains.substring(4).trim();
            //parse the text to be sent
            output = "";
            while(remains.startsWith("0x")){
              //read the hex number(s)
              number = remains.substring(2,6);
              output += (char)Integer.parseInt(number, 16);
              remains = remains.substring(6).trim();
            }
            //skip the keycap declaration
            if(remains.startsWith("keycap")){
              //skip "keycap"
              remains = remains.substring(6).trim();
              //skip all the numbers
              keycapStr = "";
              while(remains.startsWith("0x")){
                //read the hex number(s)
                number = remains.substring(2,6);
                keycapStr += (char)Integer.parseInt(number, 16);
                remains = remains.substring(6).trim();
              }
            }
            //is there a keymap declaration?
            if (remains.startsWith("keymap")){
              //skip "keymap"
              remains = remains.substring(6).trim();
  //XXXXXXXXXXXXXXXXXX//TO DO handle keymap declaration
            }else if(remains.length() == 0){
              //we're done with this line
              addAction(keyStr, output, keycapStr);
            } else System.err.println("[Gate Unicode input method loader]" +
                                     " Ignoring line: " + line);
          }else if(remains.startsWith("resetorsend")){
            //send declaration
            //skip the resetorsend
            remains = remains.substring(11).trim();
  //XXXXXXXXXXXXXXXXXX//TO DO handle resetorsend declaration
          }else System.err.println("[Gate Unicode input method loader]" +
                                 " Ignoring line: " + line);
        }else if(remains.startsWith("keymap")){
          //keymap declaration
        }else if(remains.startsWith("inputmethod")){
          //ignore
        }else if(remains.startsWith("option")){
          //ignore
        }else System.err.println("[Gate Unicode input method loader]" +
                                 " Ignoring line: " + line);
      }catch(StringIndexOutOfBoundsException siobe){
        System.err.println("[Gate Unicode input method loader]" +
                           " Ignoring line: " + line);
      }
      line = br.readLine();
    }//while(line != null)
  }//public LocaleHandler(String fileName)

  /**    *
   * @param keyDesc
   * @param textToAdd
   * @param keycapStr
   */
  protected State addAction(String keyDesc,
                            String textToAdd,
                            String keycapStr){
    //create the list of keys
    List keyList = new ArrayList(1);
    int modifiers = 0;
    char keyChar;
    int offset = 0;
    while(keyDesc.length() > 0){
  //System.out.println("A");
      modifiers = 0;
      offset = 0;
      if(keyDesc.startsWith("C-")){
        //CTRL + ?
        modifiers |= InputEvent.CTRL_MASK;
        offset = 2;
      }else if(keyDesc.startsWith("M-")){
        //ALT + ?
        modifiers |= InputEvent.ALT_MASK;
        offset = 2;
      }
      keyChar = keyDesc.charAt(offset);
      keyDesc = keyDesc.substring(offset + 1).trim();
      keyList.add(new Key(keyChar, modifiers));
    }//while(keyDesc.length() > 0)

    //add the keycap
    if(keycapStr != null && keyList.size() == 1){
      keycap.put(keyList.get(0), keycapStr);
//System.out.println("Added keycap: " + keycapStr);
    }

    //create the states and actions from the list of keys
    State nextState, currentState = initialState;
    Action currentAction = null;
    Iterator keyIter = keyList.iterator();
    Key currentKey;
    while(keyIter.hasNext()){
      currentKey = (Key)keyIter.next();
      currentAction = currentState.getNext(currentKey);
      if(currentAction == null){
        nextState = new State();
        currentAction = new Action(nextState);
        currentState.addAction(currentKey, currentAction);
        currentAction.setComposedText("" + currentKey.keyChar);
      }else nextState = currentAction.getNext();
      currentState = nextState;
    }//while(keyIter.hasNext())
    currentAction.setComposedText(textToAdd);
    currentState.setFinal(true);
    return currentState;
  }

  /**
   * The initial state of the FSM.
   *
   */
  public State getInitialState(){
    return initialState;
  }

  /**
   * Gets the map with the keycaps (the strings to be painted on virtual keys).
   *
   */
  public Map  getKeyCap(){
    return keycap;
  }

  //the initial state of the fsm
  /**
   * The initial state of the fsm.
   *
   */
  State initialState;
  /** maps from string (the English description of the key) to
   * string (the string to be displayed on the key)
   *
   */
  Map keycap;

  /**
   * The locale this handler handles.
   *
   */
  Locale locale;
}//class LocaleHandler
