/*
 * GateIM.java
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

import java.awt.im.spi.InputMethodContext;
import java.util.Locale;
import java.awt.AWTEvent;
import java.awt.Window;
import java.awt.Frame;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;
import java.awt.event.InputEvent;
import java.awt.Rectangle;
import java.awt.im.spi.InputMethod;
import java.lang.Character.Subset;
import java.util.*;
import java.io.*;
import java.text.*;

import guk.*;

/**
 * The Gate input method
 *
 */
public class GateIM implements InputMethod {

  /**
   * Constructs a new Gate input method
   *
   * @param supportedLocales
   */
  public GateIM(Map supportedLocales) {
    this.supportedLocales = supportedLocales;
    loadedLocales = new HashMap();
  }

  /**
   * Provides the input method with a context. This method is called by the
   * system after the input method is loaded and linked to a text component.
   *
   * @param context
   */
  public void setInputMethodContext(InputMethodContext context) {
    myContext = context;
    //we don't care about the client window state and position
    myContext.enableClientWindowNotification(this, false);
  }

  /**
   * Selects the active locale
   *
   * @param locale
   */
  public boolean setLocale(Locale locale) {
    endComposition();
    try{
      if(supportedLocales.containsKey(locale)){
        currentLocale = locale;
        loadLocale(locale);
        if(keyboardMap != null) keyboardMap.update(currentHandler,
                                                   currentState);
        return true;
      }
    }catch(IllegalArgumentException iae){
      iae.printStackTrace();
      return false;
    }
    return false;
  }

  /**
   * Gets the active locale
   *
   */
  public Locale getLocale() {
    return currentLocale;
  }

  /**
   * gets the descriptor class for this input method
   *
   */
  public GateIMDescriptor getDescriptor(){
    return new GateIMDescriptor();
  }

  /**
   * Restricts the character ranges valid for this input method output. This is
   * currently ignored by the input method.
   *
   * @param subsets
   */
  public void setCharacterSubsets(Subset[] subsets) {
  }

  /**
   * Enables this input method for composition
   *
   * @param enable
   */
  public void setCompositionEnabled(boolean enable) {
    enabled = enable;
  }

  /**
   * Is this input method enabled?
   *
   */
  public boolean isCompositionEnabled() {
    return enabled;
  }

  /**
   * Throws a UnsupportedOperationException as this input method does not
   * support recnversion.
   *
   */
  public void reconvert() {
    /**@todo: Implement this java.awt.im.spi.InputMethod method*/
    throw new java.lang.UnsupportedOperationException(
                        "Reconversion not supported!");
  }

  /**
   * Called by the system when an input event occures in a component that uses
   * this input method.
   * The input method then analyses the input event and sends an input method
   * event to the interested components
   * using the input context provided by the system.
   *
   * @param event
   */
  public void dispatchEvent(AWTEvent event) {
    if(event instanceof KeyEvent){
      KeyEvent kEvent = (KeyEvent) event;
      char ch = kEvent.getKeyChar();
      int keyCode = kEvent.getKeyCode();
      int modifiers = kEvent.getModifiers();
      int id = kEvent.getID();
      //process the CTRL+? events that do not generate key-typed events.
      if((id == KeyEvent.KEY_PRESSED || id == KeyEvent.KEY_RELEASED) &&
         (modifiers & KeyEvent.CTRL_MASK) > 0 &&
         keyCode != KeyEvent.VK_CONTROL){
        boolean shift = (modifiers & KeyEvent.SHIFT_MASK) > 0;
        if(ch == KeyEvent.CHAR_UNDEFINED ||
           Character.isISOControl(ch)){
          if((int)'0' <= keyCode && keyCode <= (int)'9'){
            if(!shift){
              ch = (char)keyCode;
            }else{
              //shifted versions for the digit keys
              switch((char)keyCode){
                case '0':{
                  ch = ')';
                  break;
                }
                case '1':{
                  ch = '!';
                  break;
                }
                case '2':{
                  ch = '\"';
                  break;
                }
                case '3':{
                  ch = '£';
                  break;
                }
                case '4':{
                  ch = '$';
                  break;
                }
                case '5':{
                  ch = '%';
                  break;
                }
                case '6':{
                  ch = '^';
                  break;
                }
                case '7':{
                  ch = '&';
                  break;
                }
                case '8':{
                  ch = '*';
                  break;
                }
                case '9':{
                  ch = '(';
                  break;
                }
              }//switch((char)keyCode)
            }
          }else if((int)'A' <= keyCode && keyCode <= (int)'Z'){
            ch = (char)keyCode;
            if(!shift){
              ch = Character.toLowerCase(ch);
            }
          }else{
            switch(keyCode){
              case KeyEvent.VK_MINUS:{
                ch = shift?'_':'-';
                break;
              }
              case KeyEvent.VK_EQUALS:{
                ch = shift?'+':'=';
                break;
              }
              case KeyEvent.VK_OPEN_BRACKET:{
                ch = shift?'{':'[';
                break;
              }
              case KeyEvent.VK_CLOSE_BRACKET:{
                ch = shift?'}':']';
                break;
              }
              case KeyEvent.VK_SEMICOLON:{
                ch = shift?':':';';
                break;
              }
              case KeyEvent.VK_BACK_QUOTE:{
                ch = shift?'@':'\'';
                break;
              }
              case KeyEvent.VK_QUOTE:{
                ch = shift?'~':'#';
                break;
              }
              case KeyEvent.VK_BACK_SLASH:{
                ch = shift?'|':'\\';
                break;
              }
              case KeyEvent.VK_COMMA:{
                ch = shift?'<':',';
                break;
              }
              case KeyEvent.VK_STOP:{
                ch = shift?'>':'.';
                break;
              }
              case KeyEvent.VK_SLASH:{
                ch = shift?'?':'/';
                break;
              }
            }
          }
        }//if(ch = KeyEvent.CHAR_UNDEFINED)
        //modify the event
        if(id == KeyEvent.KEY_PRESSED) id = KeyEvent.KEY_TYPED;
      }

      //now send it to the virtual keyboard
      ((KeyEvent)event).setKeyChar(ch);
      if(keyboardMap != null) keyboardMap.addJob(event);

      //now process it for input
      if( id == KeyEvent.KEY_TYPED &&
          ( ch == ' ' ||
            !Character.isISOControl(ch)
          )
        )
        {
        //it's a key typed event
        Key key = new Key(ch, modifiers);
        Action action = currentState.getNext(key);
        if(action == null){
          //we can't go further, commit if in final state cancel otherwise
          if(currentState.isFinal()){
            myContext.dispatchInputMethodEvent(
                  InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                  (new AttributedString(composedText)).getIterator(),
                  composedText.length(), null, null);
          }
          //move to the initial state
          composedText = "";
          currentState = currentHandler.getInitialState();
          action = currentState.getNext(key);
        }
        if(action ==null){
          //undefined state, remain in initial state, cancel composed text
          //send the key char
          composedText = "";
        }else{
          //advance and compose new text
          currentState = action.getNext();
          composedText = action.getComposedText();
        }
        ((KeyEvent)event).consume();
        //fire the event to the client
        boolean commit = !currentState.hasNext();
        myContext.dispatchInputMethodEvent(
          InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
          (new AttributedString(composedText)).getIterator(),
          commit?composedText.length():0, null,null);
        if(commit) composedText = "";
        if(keyboardMap != null) keyboardMap.update(currentHandler, currentState);
      }
    }
  }

  /**
   * Called by the system when the client window has changed size or position.
   * This event is ignored by the input method.
   *
   * @param bounds
   */
  public void notifyClientWindowChange(Rectangle bounds) {
    //we don't care about that as we don't display any composition windows
    //do nothing
  }

  /**
   * Activates this input method
   *
   */
  public void activate() {
    enabled = true;
    if(currentLocale == null) setLocale(defaultLocale);
    if(mapVisible){
      if(keyboardMap == null) keyboardMap = new KeyboardMap(this,
                                                            currentHandler,
                                                            currentState);
      keyboardMap.addJob("SHOW");
    }
  }

  /**
   * Deactivates this input method
   *
   * @param isTemporary
   */
  public void deactivate(boolean isTemporary) {
    endComposition();
    enabled = false;
//    if(mapVisible) keyboardMap.addJob("HIDE");
  }

  /**
   * Hides all the windows displayed by the input method. Currently this only
   * includes the virtual keyboard map.
   *
   */
  public void hideWindows() {
    if(mapVisible) keyboardMap.addJob("HIDE");
  }

  /**
   * Called by the system when a client unregisters to this input method. This
   * event is currently ignored by the input method.
   *
   */
  public void removeNotify() {
    //so what! :)
  }

  /**
   * Ends the curent composition.
   *
   */
  public void endComposition() {
//System.out.println("GateIM endComposition()!");
    if(composedText.length() > 0 && currentState.isFinal()){
      myContext.dispatchInputMethodEvent(
            InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
            (new AttributedString(composedText)).getIterator(),
            composedText.length(), null, null);
    }
    composedText = "";
  }

  /**
   * Disposes this input method releasing all the memory.
   *
   */
  public void dispose() {
    endComposition();
    if(keyboardMap != null){
      keyboardMap.addJob("DIE");
      keyboardMap = null;
    }
    currentLocale = null;
    currentHandler = null;
    currentState = null;
    myContext = null;
    supportedLocales.clear();
    supportedLocales = null;
    loadedLocales.clear();
    loadedLocales = null;
  }

  /**
   * Gives the clients a chance to control the bevaviour of this input method
   * by returning a handle to itself.
   *
   * @return a reference to this input method
   */
  public Object getControlObject() {
    return this;
  }

  /**
   * Should the virtual keyboard map be visible?
   *
   * @param mapvis
   */
  public void setMapVisible(boolean mapvis){
    if(mapvis){
      mapVisible = true;
      if(keyboardMap == null) keyboardMap = new KeyboardMap(this,
                                                            currentHandler,
                                                            currentState);
      keyboardMap.addJob("SHOW");
    }else{
      mapVisible = false;
      if(keyboardMap != null) keyboardMap.addJob("HIDE");
    }
  }

  /**
   * Loads a new locale if it's not already loaded.
   *
   * @param locale
   */
  protected void loadLocale(Locale locale){
    String fileName = (String)supportedLocales.get(locale);
    if(fileName == null) throw new IllegalArgumentException(
                                   "Unknown locale: " + locale);
    currentHandler = (LocaleHandler)loadedLocales.get(locale);
    if(currentHandler == null){
      try{
        currentHandler = new LocaleHandler(locale, fileName);
        loadedLocales.put(locale, currentHandler);
        currentState = currentHandler.getInitialState();
      }catch(IOException ioe){
        throw new IllegalArgumentException("Cannot load locale: " + locale);
      }
    }
  }

  /**
   * Returns theinput context for this input method.
   *
   */
  public InputMethodContext getContext(){
    return myContext;
  }

  //--------- variables
  /**
   * The active locale
   *
   */
  Locale currentLocale;
  /**
   * The default locale to be used when this method is loaded and no locale is
   * specified.
   *
   */
  Locale defaultLocale = new Locale("en",  "", "ASCII");
  /**
   * The current locale handler.
   *
   */
  LocaleHandler currentHandler;
  /**
   * The input context
   *
   */
  InputMethodContext myContext;
  //maps from Loacle to String (the file name)
  /**
   * The available locales (the locales for which a definition file exists).
   *
   */
  Map supportedLocales;
  //maps from Locale to LocaleHandler
  /**
   * The locales that have been loaded already. Maps from Loacle to
   * {@link LocaleHandler}.
   *
   */
  Map loadedLocales;
  /**
   * Is this inpuit method enabled?
   *
   */
  boolean enabled;
  /**
   * The composed text;
   *
   */
  String composedText = "";
  /**
   * The current state of the current LocaleHandler.
   *
   */
  State currentState;
  /**
   * Not used
   *
   */
  Map additionalKeymaps;
  /**
   * The current virtual keyboard map.
   *
   */
  KeyboardMap keyboardMap;
  /**
   * Should the keyboard map be visible?
   *
   */
  boolean mapVisible = true;

  /** The resource path to the input methods director
   */
  static private String imBase =  "/guk/im/data/";

  /** Sets the default path to be used when looking for input methods.
   * This should be  a resource path (a path inside the class path).
   * By default the path is &quot;guk/im/data/&quot;
   *
   * @param path
   */
  static public void setIMBase(String path){
    imBase = path;
  }

  /** Gets the path inside the classpath where the input methods should be found
   */
  public static String getIMBase(){return imBase;}


  static private Font keyboardFont = new Font("Arial Unicode MS", Font.PLAIN, 12);
  public static Font getKeyboardFont(){
    return keyboardFont;
  }
}
