/*
 *  LogArea.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 26/03/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import gate.util.*;

/**
  * This class is used to log all messages from gate. When an object of this
  * class is created, it redirects the output of gate.util.Out & gate.util.Err.
  * The output from Err is written with Red ans the one from Out is written
  * with Black.
  */
public class LogArea extends JTextPane {

  /** Field needed in inner classes*/
  protected LogArea thisLogArea = null;

  /** The popup menu with various actions*/
  protected JPopupMenu popup = null;

  /** This fields defines the Select all behaviour*/
  protected SelectAllAction selectAllAction = null;

  /** This fields defines the copy  behaviour*/
  protected CopyAction copyAction = null;

  /** Constructs a LogArea object and captures the output from Err and Out*/
  public LogArea(){
    thisLogArea = this;
    this.setEditable(false);

    LogAreaOutputStream err = new LogAreaOutputStream(true);
    LogAreaOutputStream out = new LogAreaOutputStream(false);

    // Redirect Err
    Err.setPrintWriter(new PrintWriter(err,true));
    // Redirect Out
    Out.setPrintWriter(new PrintWriter(out,true));
    // Corrupting System.out
    // For the moment this option is inactive.Comment out to activate it.
    //System.setOut(new PrintStream(out,true));
    // Corrupting System.err
    // For the moment this option is inactive.Comment out to activate it.
    //System.setErr(new PrintStream(err,true));

    popup = new JPopupMenu();
    selectAllAction = new SelectAllAction();
    copyAction = new CopyAction();

    popup.add(selectAllAction);
    popup.add(copyAction);

    initListeners();
  }// LogArea

  /** Init all listeners for this object*/
  public void initListeners(){
    this.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e){
        if(SwingUtilities.isRightMouseButton(e)){
          popup.show(thisLogArea, e.getPoint().x, e.getPoint().y);
        }//End if
      }// end mouseClicked()
    });// End addMouseListener();
  }// initListeners();

  /** Inner class that defines the behaviour of SelectAll action.*/
  protected class SelectAllAction extends AbstractAction{

    public SelectAllAction(){
      super("Select all");
    }// SelectAll

    public void actionPerformed(ActionEvent e){
      thisLogArea.selectAll();
    }// actionPerformed();
  }// End class SelectAllAction

  /** Inner class that defines the behaviour of copy action.*/
  protected class CopyAction extends AbstractAction{
    public CopyAction(){
      super("Copy");
    }// CopyAction

    public void actionPerformed(ActionEvent e){
      thisLogArea.copy();
    }// actionPerformed();
  }// End class CopyAction

  /** Inner class that defines the behaviour of a OutputStream that writes to
   *  the LogArea
   */
  class LogAreaOutputStream extends OutputStream{
    /** This field dictates the style on how to write */
    private boolean isErr = false;
    /** This is the styled Document form LogArea*/
    private StyledDocument styledDoc = null;
    /** Char style*/
    private Style style = null;

    /** Constructs an Out or Err LogAreaOutputStream*/
    public LogAreaOutputStream(boolean anIsErr){
      isErr = anIsErr;
      styledDoc = getStyledDocument();
      if (isErr){
        style = addStyle("error", getStyle("default"));
        StyleConstants.setForeground(style, Color.red);
      }else {
        style = addStyle("out",getStyle("default"));
        StyleConstants.setForeground(style, Color.black);
      }// End if
    }// LogAreaOutputStream

    /** Writes an int which must be a the code of a char, into the LogArea,
     *  using the style specified in constructor. The int is downcast to a byte.
     */
    public synchronized void write(int charCode){
      // charCode int must be a char. Let us be sure of that
      charCode &= 0x000000FF;
      // Convert the byte to a char before put it into the log area
      char c = (char)charCode;
      // Insert it in the log Area
      try{
          synchronized(styledDoc){
            styledDoc.insertString(0,String.valueOf(c),style);
          }// End synchronize
      } catch(BadLocationException e){
        e.printStackTrace(Err.getPrintWriter());
      }// End try
    }// write(int charCode)

    /** Writes an array of bytes into the LogArea,
     *  using the style specified in constructor.
     */
    public synchronized void write(byte[] data, int offset, int length){
      // Insert the string to the log area
      try{
          synchronized(styledDoc){
            styledDoc.insertString( 0,
                                    new String(data,offset,length),
                                    style);
          } // End synchronize
      } catch(BadLocationException e){
          e.printStackTrace(Err.getPrintWriter());
      }// End try
    }// write(byte[] data, int offset, int length)
  }//End class LogAreaOutputStream
}//End class LogArea