/*
 * KeyboardMap.java
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.LabelUI;

import guk.*;

/**
 * A virtual keyboard map. It uses its own thread do udate the display.
 *
 */
public class KeyboardMap implements Runnable{

  /**
   * Builds the keyboard map. Uses a window provided by the context of the input
   * method.
   *
   * @param im the input method
   * @param handler the active Locale handler
   * @param state the state of the handler.
   */
  public KeyboardMap(GateIM im, LocaleHandler handler, State state){
    this.im = im;
    this.handler = handler;
    this.state = state;
    jobs = Collections.synchronizedList(new ArrayList());
    myThread = new Thread(this);
    myThread.start();
  }

  /**
   * The run method for the thread responsible for updating the display.
   */
  public void run(){
    //do all the initialisations
    this.window = im.getContext().createInputMethodWindow(null, true);
    if(window instanceof Frame) ((Frame)window).setTitle(
                      handler.locale.getDisplayName() +
                      " keyboard map");
    window.setVisible(false);
    window.addWindowListener(new WindowAdapter(){

    });
    window.setLayout(new GridLayout(1,1));
    GridBagLayout layout = new GridBagLayout();
    contentPane = new JPanel(layout);
    contentPane.setDoubleBuffered(true);
    window.add(contentPane, BorderLayout.CENTER);

    labelForKey = new HashMap();
    GUIforString = new HashMap();

    double [] wheights = new double[45];
    for(int i = 0; i < wheights.length; i++) wheights[i] = 0.001;
    layout.columnWeights = wheights;
    wheights = new double[5];
    for(int i = 0; i < wheights.length; i++) wheights[i] = 0.001;
    layout.rowWeights = wheights;

    //read keycaps
    Map keyCap = handler.getKeyCap();
    Iterator keyIter = keyCap.keySet().iterator();
    Key currentKey;
    JLabel currentLabel;
    //LabelUI uLabelUI = new BasicUnicodeLabelUI(GateIM.getFontSet());
    while(keyIter.hasNext()){
      currentKey = (Key)keyIter.next();
      currentLabel = new JLabel();
      //currentLabel.setUI(uLabelUI);
      currentLabel.setFont(GateIM.getKeyboardFont());
      currentLabel.setForeground(Color.black);
      currentLabel.setText((String)keyCap.get(currentKey));
      labelForKey.put(currentKey, currentLabel);
    }
    //build the guis
    GUIforString = new HashMap();
    KeyGUI currentGui;
    int currentModifiers = 0;
    if(ctrl) currentModifiers |= InputEvent.CTRL_MASK;
    if(alt) currentModifiers |= InputEvent.ALT_MASK;
    char ch;
    String sKey;
    for(ch = 'a'; ch <= 'z'; ch++){
      sKey = "" + ch;
      if(shift) currentKey = new Key(Character.toUpperCase(ch), currentModifiers);
      else currentKey = new Key(ch, currentModifiers);
      currentGui = new KeyGUI(sKey, ch, Character.toUpperCase(ch),
                             (JLabel)labelForKey.get(currentKey));
      GUIforString.put(sKey, currentGui);
    }
    if(shift) ch = '!'; else ch = '1';
    sKey = "1";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '1', '!',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '\"'; else ch = '2';
    sKey = "2";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '2', '\"',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '£'; else ch = '3';
    sKey = "3";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '3', '£',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '$'; else ch = '4';
    sKey = "4";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '4', '$',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '%'; else ch = '5';
    sKey = "5";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '5', '%',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '^'; else ch = '6';
    sKey = "6";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '6', '^',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '&'; else ch = '7';
    sKey = "7";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '7', '&',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '*'; else ch = '8';
    sKey = "8";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '8', '*',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '('; else ch = '9';
    sKey = "9";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '9', '(',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = ')'; else ch = '0';
    sKey = "0";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '0', ')',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '¬'; else ch = '`';
    sKey = "`";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '`', '¬',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '_'; else ch = '-';
    sKey = "-";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '-', '_',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '+'; else ch = '=';
    sKey = "=";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '=', '+',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '{'; else ch = '[';
    sKey = "[";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '[', '{',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '}'; else ch = ']';
    sKey = "]";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, ']', '}',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = ':'; else ch = ';';
    sKey = ";";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, ';', ':',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '@'; else ch = '\'';
    sKey = "'";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '\'', '@',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '~'; else ch = '#';
    sKey = "#";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '#', '~',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '|'; else ch = '\\';
    sKey = "\\";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '\\', '|',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '<'; else ch = ',';
    sKey = ",";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, ',', '<',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '>'; else ch = '.';
    sKey = ".";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '.', '>',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);
    if(shift) ch = '?'; else ch = '/';
    sKey = "/";
    currentKey = new Key(ch, currentModifiers);
    currentGui = new KeyGUI(sKey, '/', '?',(JLabel)labelForKey.get(currentKey));
    GUIforString.put(sKey, currentGui);

    GUIforString.put("BACK_SPACE", new KeyGUI("BACK_SPACE", (char)0,(char)0,new JLabel("BackSpace")));
    GUIforString.put("TAB", new KeyGUI("TAB", (char)0,(char)0,new JLabel("Tab")));
    GUIforString.put("CAPS_LOCK", new KeyGUI("CAPS_LOCK", (char)0,(char)0,new JLabel("Caps Lock")));
    GUIforString.put("ENTER", new KeyGUI("ENTER", (char)0, (char)0,new JLabel("Enter")));
    GUIforString.put("LSHIFT", new KeyGUI("LSHIFT", (char)0,(char)0,new JLabel("Shift")));
    GUIforString.put("RSHIFT", new KeyGUI("RSHIFT", (char)0,(char)0,new JLabel("Shift")));
    GUIforString.put("LCTRL", new KeyGUI("LCTRL", (char)0,(char)0,new JLabel("Ctrl")));
    GUIforString.put("RCTRL", new KeyGUI("RCTRL", (char)0,(char)0,new JLabel("Ctrl")));
    GUIforString.put("LALT", new KeyGUI("LALT", (char)0,(char)0,new JLabel("Alt")));
    GUIforString.put("RALT", new KeyGUI("RALT", (char)0,(char)0,new JLabel("Alt")));
    GUIforString.put("SPACE", new KeyGUI("SPACE", ' ', ' ',new JLabel(" ")));

    //add the components to the window
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = constraints.BOTH;
    constraints.gridwidth = 3;
    contentPane.add((Component)GUIforString.get("`"), constraints);
    contentPane.add((Component)GUIforString.get("1"), constraints);
    contentPane.add((Component)GUIforString.get("2"), constraints);
    contentPane.add((Component)GUIforString.get("3"), constraints);
    contentPane.add((Component)GUIforString.get("4"), constraints);
    contentPane.add((Component)GUIforString.get("5"), constraints);
    contentPane.add((Component)GUIforString.get("6"), constraints);
    contentPane.add((Component)GUIforString.get("7"), constraints);
    contentPane.add((Component)GUIforString.get("8"), constraints);
    contentPane.add((Component)GUIforString.get("9"), constraints);
    contentPane.add((Component)GUIforString.get("0"), constraints);
    contentPane.add((Component)GUIforString.get("-"), constraints);
    contentPane.add((Component)GUIforString.get("="), constraints);
    constraints.gridwidth = 6;
    contentPane.add((Component)GUIforString.get("BACK_SPACE"), constraints);
    //second line
    constraints.gridy = 1;
    constraints.gridwidth = 5;
    contentPane.add((Component)GUIforString.get("TAB"), constraints);
    constraints.gridwidth = 3;
    contentPane.add((Component)GUIforString.get(""+'q'), constraints);
    contentPane.add((Component)GUIforString.get(""+'w'), constraints);
    contentPane.add((Component)GUIforString.get(""+'e'), constraints);
    contentPane.add((Component)GUIforString.get(""+'r'), constraints);
    contentPane.add((Component)GUIforString.get(""+'t'), constraints);
    contentPane.add((Component)GUIforString.get(""+'y'), constraints);
    contentPane.add((Component)GUIforString.get(""+'u'), constraints);
    contentPane.add((Component)GUIforString.get(""+'i'), constraints);
    contentPane.add((Component)GUIforString.get(""+'o'), constraints);
    contentPane.add((Component)GUIforString.get(""+'p'), constraints);
    contentPane.add((Component)GUIforString.get(""+'['), constraints);
    contentPane.add((Component)GUIforString.get(""+']'), constraints);
    constraints.gridwidth = 4;
    contentPane.add((Component)GUIforString.get(""+'\\'), constraints);
    //line 3
    constraints.gridy = 2;
    constraints.gridwidth = 6;
    contentPane.add((Component)GUIforString.get("CAPS_LOCK"), constraints);
    constraints.gridwidth = 3;
    contentPane.add((Component)GUIforString.get("a"), constraints);
    contentPane.add((Component)GUIforString.get(""+'s'), constraints);
    contentPane.add((Component)GUIforString.get(""+'d'), constraints);
    contentPane.add((Component)GUIforString.get(""+'f'), constraints);
    contentPane.add((Component)GUIforString.get(""+'g'), constraints);
    contentPane.add((Component)GUIforString.get(""+'h'), constraints);
    contentPane.add((Component)GUIforString.get(""+'j'), constraints);
    contentPane.add((Component)GUIforString.get(""+'k'), constraints);
    contentPane.add((Component)GUIforString.get(""+'l'), constraints);
    contentPane.add((Component)GUIforString.get(""+';'), constraints);
    contentPane.add((Component)GUIforString.get("'"), constraints);
    constraints.gridwidth = 6;
    contentPane.add((Component)GUIforString.get("ENTER"), constraints);
    //line 4
    constraints.gridy = 3;
    constraints.gridwidth = 5;
    contentPane.add((Component)GUIforString.get("LSHIFT"), constraints);
    constraints.gridwidth = 3;
    contentPane.add((Component)GUIforString.get(""+'z'), constraints);
    contentPane.add((Component)GUIforString.get(""+'x'), constraints);
    contentPane.add((Component)GUIforString.get(""+'c'), constraints);
    contentPane.add((Component)GUIforString.get(""+'v'), constraints);
    contentPane.add((Component)GUIforString.get(""+'b'), constraints);
    contentPane.add((Component)GUIforString.get(""+'n'), constraints);
    contentPane.add((Component)GUIforString.get(""+'m'), constraints);
    contentPane.add((Component)GUIforString.get(""+','), constraints);
    contentPane.add((Component)GUIforString.get(""+'.'), constraints);
    contentPane.add((Component)GUIforString.get(""+'/'), constraints);
    contentPane.add((Component)GUIforString.get(""+'#'), constraints);
    constraints.gridwidth = 8;
    contentPane.add((Component)GUIforString.get("RSHIFT"), constraints);
    //line 5
    constraints.gridy = 4;
    constraints.gridwidth = 5;
    contentPane.add((Component)GUIforString.get("LCTRL"), constraints);
    contentPane.add((Component)GUIforString.get("LALT"), constraints);
    constraints.gridwidth = 25;
    contentPane.add((Component)GUIforString.get("SPACE"), constraints);
    constraints.gridwidth = 5;
    contentPane.add((Component)GUIforString.get("RALT"), constraints);
    contentPane.add((Component)GUIforString.get("RCTRL"), constraints);
    window.pack();

    if(im.mapVisible) window.setVisible(true);

    //initialisations done
    //wait for jobs to do
    infinite:while(true){
      synchronized(jobs){
        try{
          if(!jobs.isEmpty()){
            //do the jobs in the job list
            while(!jobs.isEmpty()){
              Object job = jobs.remove(0);
              //do job
              if(job instanceof String){
                //job is a command
                String sJob = (String)job;
                //should we die? :(
                if(sJob.equalsIgnoreCase("DIE")) break infinite;
                else if(sJob.equalsIgnoreCase("HIDE") && window.isShowing()){
                  window.setVisible(false);
                }else if(sJob.equalsIgnoreCase("SHOW") && (!window.isShowing())){
                  window.setVisible(true);
                }else if(sJob.equalsIgnoreCase("UPDATE")) update();
              }else if(job instanceof KeyEvent){
                //job is an key event
                int code;
                KeyEvent kEvent = (KeyEvent)job;
                KeyGUI someGui;
                if(kEvent.getID() == KeyEvent.KEY_PRESSED){
                  code = kEvent.getKeyCode();
                  switch(code){
                    case KeyEvent.VK_SHIFT: {
                      if(!shift) {
                        shift = true;
                        ((KeyGUI)GUIforString.get("LSHIFT")).pressKey();
                        ((KeyGUI)GUIforString.get("RSHIFT")).pressKey();
                        updateLabels();
                      }
                      break;
                    }
                    case KeyEvent.VK_CONTROL:{
                      if(!ctrl){
                        ctrl = true;
                        ((KeyGUI)GUIforString.get("LCTRL")).pressKey();
                        ((KeyGUI)GUIforString.get("RCTRL")).pressKey();
                      updateLabels();
                      }
                      break;
                    }
                    case KeyEvent.VK_ALT:{
                      if(!alt){
                        alt = true;
                        ((KeyGUI)GUIforString.get("LALT")).pressKey();
                        ((KeyGUI)GUIforString.get("RALT")).pressKey();
                        updateLabels();
                      }
                      break;
                    }
                    case KeyEvent.VK_CAPS_LOCK:{
                      someGui = (KeyGUI)GUIforString.get("CAPS_LOCK");
                      someGui.pressKey();
                      break;
                    }
                    case KeyEvent.VK_BACK_SPACE:{
                      someGui = (KeyGUI)GUIforString.get("BACK_SPACE");
                      someGui.pressKey();
                      break;
                    }
                    case KeyEvent.VK_ENTER:{
                      someGui = (KeyGUI)GUIforString.get("ENTER");
                      someGui.pressKey();
                      break;
                    }
                    case KeyEvent.VK_TAB:{
                      someGui = (KeyGUI)GUIforString.get("TAB");
                      someGui.pressKey();
                      break;
                    }
                    case KeyEvent.VK_SPACE:{
                      someGui = (KeyGUI)GUIforString.get("SPACE");
                      someGui.pressKey();
                      break;
                    }
                    default:{
                      String key = "";
                      char keyCh = kEvent.getKeyChar();
                      if('a' <= keyCh && keyCh <= 'z'){
                        key += keyCh;
                      }else if('A' <= keyCh && keyCh <= 'Z'){
                        key += Character.toLowerCase(keyCh);
                      }else{
                        switch(keyCh){
                          case '`':{
                            key += keyCh;
                            break;
                          }
                          case '1':{
                            key += keyCh;
                            break;
                          }
                          case '2':{
                            key += keyCh;
                            break;
                          }
                          case '3':{
                            key += keyCh;
                            break;
                          }
                          case '4':{
                            key += keyCh;
                            break;
                          }
                          case '5':{
                            key += keyCh;
                            break;
                          }
                          case '6':{
                            key += keyCh;
                            break;
                          }
                          case '7':{
                            key += keyCh;
                            break;
                          }
                          case '8':{
                            key += keyCh;
                            break;
                          }
                          case '9':{
                            key += keyCh;
                            break;
                          }
                          case '0':{
                            key += keyCh;
                            break;
                          }
                          case '-':{
                            key += keyCh;
                            break;
                          }
                          case '=':{
                            key += keyCh;
                            break;
                          }
                          case '[':{
                            key += keyCh;
                            break;
                          }
                          case ']':{
                            key += keyCh;
                            break;
                          }
                          case ';':{
                            key += keyCh;
                            break;
                          }
                          case '\'':{
                            key += keyCh;
                            break;
                          }
                          case '#':{
                            key += keyCh;
                            break;
                          }
                          case '\\':{
                            key += keyCh;
                            break;
                          }
                          case ',':{
                            key += keyCh;
                            break;
                          }
                          case '.':{
                            key += keyCh;
                            break;
                          }
                          case '/':{
                            key += keyCh;
                            break;
                          }


                          case '¬':{
                            key += '`';
                            break;
                          }
                          case '!':{
                            key += '1';
                            break;
                          }
                          case '\"':{
                            key += '2';
                            break;
                          }
                          case '£':{
                            key += '3';
                            break;
                          }
                          case '$':{
                            key += '4';
                            break;
                          }
                          case '%':{
                            key += '5';
                            break;
                          }
                          case '^':{
                            key += '6';
                            break;
                          }
                          case '&':{
                            key += '7';
                            break;
                          }
                          case '*':{
                            key += '8';
                            break;
                          }
                          case '(':{
                            key += '9';
                            break;
                          }
                          case ')':{
                            key += '0';
                            break;
                          }
                          case '_':{
                            key += '-';
                            break;
                          }
                          case '+':{
                            key += '=';
                            break;
                          }
                          case '{':{
                            key += '[';
                            break;
                          }
                          case '}':{
                            key += ']';
                            break;
                          }
                          case ':':{
                            key += ';';
                            break;
                          }
                          case '@':{
                            key += '\'';
                            break;
                          }
                          case '~':{
                            key += '#';
                            break;
                          }
                          case '|':{
                            key += '\\';
                            break;
                          }
                          case '<':{
                            key += ',';
                            break;
                          }
                          case '>':{
                            key += '.';
                            break;
                          }
                          case '?':{
                            key += '/';
                            break;
                          }
                        }//switch
                      }
                      someGui = (KeyGUI)GUIforString.get(key);
                      if(someGui != null){
                        someGui.pressKey();
                      }
                    }//default
                  }//switch
                }else if(kEvent.getID() == KeyEvent.KEY_RELEASED){
                  code = kEvent.getKeyCode();
                  switch(code){
                    case KeyEvent.VK_SHIFT: {
                      if(shift) {
                        shift = false;
                        ((KeyGUI)GUIforString.get("LSHIFT")).releaseKey();
                        ((KeyGUI)GUIforString.get("RSHIFT")).releaseKey();
                        updateLabels();
                      }
                      break;
                    }
                    case KeyEvent.VK_CONTROL:{
                      if(ctrl){
                        ctrl = false;
                        ((KeyGUI)GUIforString.get("LCTRL")).releaseKey();
                        ((KeyGUI)GUIforString.get("RCTRL")).releaseKey();
                        updateLabels();
                      }
                      break;
                    }
                    case KeyEvent.VK_ALT:{
                      if(alt){
                        alt = false;
                        ((KeyGUI)GUIforString.get("LALT")).releaseKey();
                        ((KeyGUI)GUIforString.get("RALT")).releaseKey();
                        updateLabels();
                      }
                      break;
                    }
                    case KeyEvent.VK_CAPS_LOCK:{
                      someGui = (KeyGUI)GUIforString.get("CAPS_LOCK");
                      someGui.releaseKey();
                      break;
                    }
                    case KeyEvent.VK_BACK_SPACE:{
                      someGui = (KeyGUI)GUIforString.get("BACK_SPACE");
                      someGui.releaseKey();
                      break;
                    }
                    case KeyEvent.VK_ENTER:{
                      someGui = (KeyGUI)GUIforString.get("ENTER");
                      someGui.releaseKey();
                      break;
                    }
                    case KeyEvent.VK_TAB:{
                      someGui = (KeyGUI)GUIforString.get("TAB");
                      someGui.releaseKey();
                      break;
                    }
                    case KeyEvent.VK_SPACE:{
                      someGui = (KeyGUI)GUIforString.get("SPACE");
                      someGui.releaseKey();
                      break;
                    }
                    default:{
                      String key = "";
                      char keyCh = kEvent.getKeyChar();
                      if('a' <= keyCh && keyCh <= 'z'){
                        key += keyCh;
                      }else if('A' <= keyCh && keyCh <= 'Z'){
                        key += Character.toLowerCase(keyCh);
                      }else{
                        switch(keyCh){
                          case '`':{
                            key += keyCh;
                            break;
                          }
                          case '1':{
                            key += keyCh;
                            break;
                          }
                          case '2':{
                            key += keyCh;
                            break;
                          }
                          case '3':{
                            key += keyCh;
                            break;
                          }
                          case '4':{
                            key += keyCh;
                            break;
                          }
                          case '5':{
                            key += keyCh;
                            break;
                          }
                          case '6':{
                            key += keyCh;
                            break;
                          }
                          case '7':{
                            key += keyCh;
                            break;
                          }
                          case '8':{
                            key += keyCh;
                            break;
                          }
                          case '9':{
                            key += keyCh;
                            break;
                          }
                          case '0':{
                            key += keyCh;
                            break;
                          }
                          case '-':{
                            key += keyCh;
                            break;
                          }
                          case '=':{
                            key += keyCh;
                            break;
                          }
                          case '[':{
                            key += keyCh;
                            break;
                          }
                          case ']':{
                            key += keyCh;
                            break;
                          }
                          case ';':{
                            key += keyCh;
                            break;
                          }
                          case '\'':{
                            key += keyCh;
                            break;
                          }
                          case '#':{
                            key += keyCh;
                            break;
                          }
                          case '\\':{
                            key += keyCh;
                            break;
                          }
                          case ',':{
                            key += keyCh;
                            break;
                          }
                          case '.':{
                            key += keyCh;
                            break;
                          }
                          case '/':{
                            key += keyCh;
                            break;
                          }


                          case '¬':{
                            key += '`';
                            break;
                          }
                          case '!':{
                            key += '1';
                            break;
                          }
                          case '\"':{
                            key += '2';
                            break;
                          }
                          case '£':{
                            key += '3';
                            break;
                          }
                          case '$':{
                            key += '4';
                            break;
                          }
                          case '%':{
                            key += '5';
                            break;
                          }
                          case '^':{
                            key += '6';
                            break;
                          }
                          case '&':{
                            key += '7';
                            break;
                          }
                          case '*':{
                            key += '8';
                            break;
                          }
                          case '(':{
                            key += '9';
                            break;
                          }
                          case ')':{
                            key += '0';
                            break;
                          }
                          case '_':{
                            key += '-';
                            break;
                          }
                          case '+':{
                            key += '=';
                            break;
                          }
                          case '{':{
                            key += '[';
                            break;
                          }
                          case '}':{
                            key += ']';
                            break;
                          }
                          case ':':{
                            key += ';';
                            break;
                          }
                          case '@':{
                            key += '\'';
                            break;
                          }
                          case '~':{
                            key += '#';
                            break;
                          }
                          case '|':{
                            key += '\\';
                            break;
                          }
                          case '<':{
                            key += ',';
                            break;
                          }
                          case '>':{
                            key += '.';
                            break;
                          }
                          case '?':{
                            key += '/';
                            break;
                          }
                        }//switch
                      }
                      someGui = (KeyGUI)GUIforString.get(key);
                      if(someGui != null){
                        someGui.releaseKey();
                      }
                    }//default
                  }//switch
                }
                //update the state so the rebuildGui will update the highlights
//                state = im.currentState;
//                update();
              }
            }
          }
        }catch(Exception e){}
        jobs.notifyAll();
      }//synchronized(jobs);
      //no more jobs, take a nap :)
      try{
        myThread.sleep(150);
      }catch(InterruptedException ie){
        ie.printStackTrace();
      }
    }//infinite: while(true)

  }

  /**
   * Adds a job to the job list of the thread.
   * A job is either a {@link java.lang.String} or an {@link java.awt.event.InputEvent}
   * The string can be one of
   * <ul>
   * <li>SHOW: shows the keyboard map window
   * <li>UPDATE updates the keyboard map window
   * <li>HIDE: hides the keyboard map window
   * <li>DIE: releases all the memory and terminates the thread
   * </ul>
   *
   * The input events refer to pressed keys and are treated accordingly.
   *
   * @param job
   */
  public void addJob(Object job){
    synchronized(jobs){
      jobs.add(job);
      jobs.notifyAll();
    }
  }

  /**
   * Updates the keyboard map for a new Locale or a new state of the current locale handler.
   * Currently the state changes are ignored.
   * This method delegates its job to the thread which wil call {@link #update()}
   *
   * @param newHandler
   * @param newState
   */
  public void update(LocaleHandler newHandler, State newState){
    //did anything changed?
    if(newHandler == handler && newState == state) return;
    this.newHandler = newHandler;
    this.newState = newState;
    addJob("UPDATE");
  }

  /**
   * Does th actual update.
   */
  protected void update(){
    //did the locale changed?
    if(newHandler != handler){
      handler = newHandler;
      state = newState;
      if(window instanceof Frame) ((Frame)window).setTitle(
                              handler.locale.getDisplayLanguage() + " (" +
                              handler.locale.getVariant() + ") keyboard map");
      //read keycaps
      labelForKey.clear();
      Map keyCap = handler.getKeyCap();
      Iterator keyIter = keyCap.keySet().iterator();
      Key currentKey;
      JLabel currentLabel;
      //LabelUI uLabelUI = new BasicUnicodeLabelUI(GateIM.getFontSet());
      while(keyIter.hasNext()){
        currentKey = (Key)keyIter.next();
        currentLabel = new JLabel();
        currentLabel.setFont(GateIM.getKeyboardFont());
        //currentLabel.setUI(uLabelUI);
        currentLabel.setText((String)keyCap.get(currentKey));
        labelForKey.put(currentKey, currentLabel);
      }
      updateLabels();
    }
    //did the state changed?
    if(newState != state){
      //highlight the allowed keys
      state = newState;
      //un-highlight the highlighted keys
      Iterator keysIter = highlightedKeys.iterator();
      while(keysIter.hasNext()) ((KeyGUI)keysIter.next()).unHighlight();
      highlightedKeys.clear();

      //if not initial state highlight the allowed keys
      if(state != handler.getInitialState()){
        keysIter = state.transitionFunction.keySet().iterator();
        KeyGUI someGui;
        while(keysIter.hasNext()){
          someGui = guiForKey((Key)keysIter.next());
          if(someGui != null){
            someGui.highlight();
            highlightedKeys.add(someGui);
          }
        }
      }
    }
  }

  /**
   * Updates the virtual keyboard to reflect the current state.
   */
  protected void updateLabels(){
    //update the labels
    Component[] components = contentPane.getComponents();
    for(int i = 0; i <components.length; i++){
      if(components[i] instanceof KeyGUI) ((KeyGUI)components[i]).updateLabel();
    }
    fixShape();
  }

  /**    */
  protected void fixShape(){
    //get the current sizes
    int [][] sizes = ((GridBagLayout)contentPane.getLayout()).getLayoutDimensions();
    //override the minimum sizes
    ((GridBagLayout)contentPane.getLayout()).columnWidths = sizes[0];
    ((GridBagLayout)contentPane.getLayout()).rowHeights = sizes[1];
    window.pack();
    contentPane.repaint(100);
  }
  /**
   * Gets the gui that corresponds to a Key object.
   *
   * @param key
   */
  protected KeyGUI guiForKey(Key key){
    char ch = key.keyChar;
    boolean shiftOn = false;
    if(Character.isUpperCase(ch)){
      ch = Character.toLowerCase(ch);
      shiftOn = true;
    }
    boolean ctrlOn = (key.modifiers & KeyEvent.CTRL_MASK) > 0;
    boolean altOn = (key.modifiers & KeyEvent.ALT_MASK) > 0;
    if(shift == shiftOn &&
       ctrl == ctrlOn &&
       alt == altOn){
      return (KeyGUI)GUIforString.get("" + ch);
    }
    return null;
  }
  /**
   * Is the Shift key pressed?
   *
   * @param shift
   */
  public void setShift(boolean shift){
    this.shift = shift;
  }

  /**
   * Is the Alt key pressed?
   *
   * @param alt
   */
  public void setAlt(boolean alt){
    this.alt = alt;
  }

  /**
   * Is the Ctrl key pressed?
   *
   * @param ctrl
   */
  public void setCtrl(boolean ctrl){
    this.ctrl = ctrl;
  }

//variables

  //the current LocaleHandler
  /**
   * the active locale handler
   */
  LocaleHandler handler;
  //thenew handler that will become current on the next update
  /**
   * The new active locale handler. This member is useful during the period when the active locale handler has changed but the keyboard map is not updated yet. The keyboard map will be updated as soon as the tasks that were added to the job list before the locale change are consumed.
   */
  LocaleHandler newHandler;

  //the current state of the current locale handler
  /**
   * The current state of the current locale handler.
   */
  State state;
  //the new state that will become current on the next update
  /**
   * The current state of the new current locale handler.
   *
   * @see #newHandler
   */
  State newState;
  /**
   * The window used for displaying the keyboard map
   */
  Window window;
  /**
   * The content pane that holds all the KeyGUIs.
   *
   * @see guk.im.KeyboardMap.KeyGUI
   */
  JPanel contentPane;

  /**
   * The keys curently highlighted
   */
  java.util.List highlightedKeys = new ArrayList();

  /**    */
  boolean shift = false, ctrl = false, alt = false, capslock = false;
  /** maps from String(the English lowercase representation of the key) to
   * KeyGUI
   */
  Map GUIforString;
  //maps from Key to JLabel for the key that have keyCap defined
  /**
   * Maps from Key to JLabel for the keys that have keyCap defined
   * .
   */
  Map labelForKey;

  /**
   * The input method.
   */
  GateIM im;

  /**
   * The thread that does the updating.
   */
  Thread myThread;

  /**
   * The job list.
   */
  java.util.List jobs;
//classes
  public class KeyGUI extends JPanel {
    /**      */
    Box leftBox;
    /**      */
    JLabel leftUpLabel = new JLabel();
    /**      */
    JLabel leftDownLabel = new JLabel();
    /**      */
    Component centerLabel;
    /**      */
    char low, up;
    /**      */
    Border normalBorder, highlightedBorder;

    /**
     * Constructs a new KeyGUI.
     *
     * @param key the String key used in the
     * {@link guk.im.KeyboardMap#GUIforString}
     * map
     * @param englishLow the English char on the key
     * @param englishUp the English char on the key when used with the Shift
     *     key.
     * @param center the center label (the Unicode character on the key)
     */
    public KeyGUI(String key, char englishLow, char englishUp,
                  JLabel center) {
      this.setBackground(Color.lightGray);
      low = englishLow;
      up = englishUp;
      leftBox = Box.createVerticalBox();
      Dimension dim;
      if(englishUp > (char)0){
        leftUpLabel.setFont(leftUpLabel.getFont().deriveFont((float)10));
        leftUpLabel.setText("" + englishUp);
        leftBox.add(leftUpLabel);
      }else{
        leftBox.add(placeHolder);
      }
      if(englishLow > (char)0){
        leftDownLabel.setFont(leftDownLabel.getFont().deriveFont((float)10));
        leftDownLabel.setText("" + englishLow);
        leftBox.add(leftDownLabel);
      }else{
        leftBox.add(placeHolder);
      }
      leftBox.add(Box.createVerticalGlue());
      if(center == null) centerLabel = placeHolder;
      else centerLabel = center;
      this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
      this.add(leftBox);
      this.add(Box.createHorizontalGlue());
      this.add(centerLabel);
      normalBorder = new CompoundBorder(new BevelBorder(BevelBorder.RAISED),
                                        new EmptyBorder(2,3,2,3));
      highlightedBorder = new CompoundBorder(new BevelBorder(BevelBorder.RAISED),
                                        new MatteBorder(2,3,2,3, Color.green));

      this.setBorder(normalBorder);
      addMouseListener(new MouseAdapter(){
        /**          *
         * @param e
         */
        public void mouseClicked(MouseEvent e){
          int modifiers = 0;
          if(ctrl) modifiers |= KeyEvent.CTRL_MASK;
          if(alt) modifiers |= KeyEvent.ALT_MASK;
          char ch;
          if(shift) ch = up;
          else ch = low;
          if(ch != 0){
            im.dispatchEvent(new KeyEvent(window, KeyEvent.KEY_TYPED,
                                          System.currentTimeMillis(),
                                          modifiers, KeyEvent.VK_UNDEFINED, ch));
          }
        }
        /**          */
        public void mousePressed(MouseEvent e){
          char ch;
          if(shift) ch = up;
          else ch = low;
          if(ch != 0){
            pressKey();
            //repaint(100);
          }
        }
        /**          */
        public void mouseReleased(MouseEvent e){
          char ch;
          if(shift) ch = up;
          else ch = low;
          if(ch != 0){
            releaseKey();
            //repaint(100);
          }
        }
      });
    }

    /**      */
    public void updateLabel(){
      if(low == (char)0 || up == (char)0)return;
      remove(centerLabel);
      Key key;
      int modifiers = 0;
      if(ctrl) modifiers |= InputEvent.CTRL_MASK;
      if(alt) modifiers |= InputEvent.ALT_MASK;
      if(shift) key = new Key(up, modifiers);
      else key = new Key(low, modifiers);
      centerLabel = (JLabel)labelForKey.get(key);
      if(centerLabel == null) centerLabel = placeHolder;
      this.add(centerLabel);
//      this.invalidate();
    }

    /**
     * Displays this key as pressed
     */
    public void pressKey(){
      this.setBackground(Color.darkGray);
    }

    /**
     * Displays ths key as released.
     */
    public void releaseKey(){
      this.setBackground(Color.lightGray);
    }

    /**
     * Renders this KeyGUI as highlighted
     */
    public void highlight(){
      setBorder(highlightedBorder);
    }

    /**
     * Renders this KeyGUI normaly (not highlighted)
     */
    public void unHighlight(){
      setBorder(normalBorder);
    }

  }//public class KeyGUI extends JPanel



  /**
   * Empty component used for the key that are not bound to a Unicode character.
   */
  static Component placeHolder = Box.createRigidArea(new Dimension(12, 12));
}//class KeyboardMap
