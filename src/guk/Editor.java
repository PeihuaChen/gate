/*
 * Editor.java
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
package guk;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.beans.*;
import java.io.*;
import java.util.Locale;
import java.util.*;

import guk.im.GateIM;
import guk.im.GateIMDescriptor;

/**
 * A simple text editor included here to demonstrate the capabilities of the GUK
 * package.
 *
 * @author             <a href="http://www.gate.ac.uk/people/">The Gate Team</a>
 * @version            1.0
 */
public class Editor extends JFrame {
  JPanel contentPane;
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenu jMenuEdit = new JMenu();
  JMenu jMenuHelp = new JMenu();
  JMenu jMenuIM = null;
  JMenuItem jMenuHelpAbout = new JMenuItem();
  JToolBar jToolBar = new JToolBar();
  JTextPane textPane = new JTextPane();
  JMenu jMenuOptions = new JMenu();
  JComboBox fontsComboBox;
  JComboBox sizeComboBox;
  JCheckBoxMenuItem jCheckBoxMenuItemKeyboardMap = new JCheckBoxMenuItem();
  Action openAction, saveAction, saveAsAction, closeAction,
         exitAction, undoAction, redoAction, cutAction, copyAction,
         pasteAction, attributesChangedAction;
  /**
   * The current open file
   */
  File file = null;
  /**
   * The file chooser used in all operations requiring the user to select a file
   */
  JFileChooser filer = new JFileChooser();
  /**
   * The main frame
   */
  JFrame frame;
  UndoManager undoManager = new UndoManager();
  /**
   * has the current document changed since the last save?
   */
  boolean docChanged = false;

  /**
   * Construct the frame
   */
  public Editor() {
    frame = this;
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    frame.validate();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    setLocation((screenSize.width - frameSize.width) / 2,
              (screenSize.height - frameSize.height) / 2);
    setVisible(true);
  }// public Editor()

  /**
   * Component initialization
   */
  private void jbInit() throws Exception {
    Locale locale = new Locale("en", "GB");
    Locale[] availableLocales = new GateIMDescriptor().getAvailableLocales();
    Arrays.sort(availableLocales, new Comparator(){
      public int compare(Object o1, Object o2){
        return ((Locale)o1).getDisplayName().compareTo(((Locale)o2).getDisplayName());
      }
    });
    JMenuItem item;
    if(availableLocales != null && availableLocales.length > 1) {
      jMenuIM = new JMenu("Input methods");
      ButtonGroup bg = new ButtonGroup();
      for(int i = 0; i < availableLocales.length; i++){
        locale = availableLocales[i];
        item = new LocaleSelectorMenuItem(locale, frame);
        jMenuIM.add(item);
        bg.add(item);
      }
    }// if

    undoManager.setLimit(1000);
    //OPEN ACTION
    openAction = new AbstractAction("Open", new ImageIcon(
            guk.Editor.class.getResource("img/openFile.gif"))){
      public void actionPerformed(ActionEvent e){
        int res = JOptionPane.OK_OPTION;
        if(docChanged){
          res = JOptionPane.showConfirmDialog(
                frame,
                "Close unsaved file " +
                (file== null?"Untitled":file.getName()) + "?",
                "Gate",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        }
        if(res == JOptionPane.OK_OPTION){
          filer.setMultiSelectionEnabled(false);
          filer.setDialogTitle("Select file to open...");
          filer.setSelectedFile(null);
          filer.setFileFilter(filer.getAcceptAllFileFilter());
          int res1 = filer.showOpenDialog(frame);
          if(res1 == filer.APPROVE_OPTION){
            //we have the file, what's the encoding?
            Object[] encodings = { "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16",
                                   "ISO-8859-1", "US-ASCII" };
            Object encoding = JOptionPane.showInputDialog(
                      frame,
                      "Please select the encoding for the chosen file",
                      "Gate",
                      JOptionPane.INFORMATION_MESSAGE,
                      null,
                      encodings,
                      encodings[0]
                      );
            if(encoding == null) return;
            file = filer.getSelectedFile();
            try {
              InputStreamReader reader = new InputStreamReader(
                new BufferedInputStream(new FileInputStream(file)),
                (String)encoding);
              textPane.selectAll();
              textPane.replaceSelection("");
              textPane.read(reader, null);
              reader.close();
            } catch(FileNotFoundException fnfe) {
              JOptionPane.showMessageDialog(frame,
                                            "Cannot find the file specified!",
                                            "Gate",
                                            JOptionPane.ERROR_MESSAGE);
              file = null;
              docChanged = false;
              updateTitle();
            } catch(UnsupportedEncodingException usee) {
              JOptionPane.showMessageDialog(frame,
                                            "Unsupported encoding!\n" +
                                            "Please choose another.",
                                            "Gate",
                                            JOptionPane.ERROR_MESSAGE);
              file = null;
              docChanged = false;
              updateTitle();
            } catch(IOException ioe) {
              JOptionPane.showMessageDialog(
                                  frame,
                                  "Input/Output error! (wrong encoding?)\n" +
                                  "Please try again.",
                                  "Gate",
                                  JOptionPane.ERROR_MESSAGE);
              file = null;
              docChanged = false;
              updateTitle();
            }
            docChanged = false;
            updateTitle();
          }
        }
      }// actionPerformed(ActionEvent e)
    };
    openAction.putValue(Action.SHORT_DESCRIPTION, "Open file...");


    //SAVE ACTION
    saveAction = new AbstractAction("Save", new ImageIcon(
            guk.Editor.class.getResource("img/saveFile.gif"))) {
      public void actionPerformed(ActionEvent e){
        if(docChanged){
          if(file == null) saveAsAction.actionPerformed(null);
          else {
            //get the encoding
            Object[] encodings = { "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16",
                                   "ISO-8859-1", "US-ASCII" };
            Object encoding = JOptionPane.showInputDialog(
                      frame,
                      "Please select the encoding for the chosen file",
                      "Gate",
                      JOptionPane.INFORMATION_MESSAGE,
                      null,
                      encodings,
                      encodings[0]
                      );
            if(encoding == null) return;
            try {
              OutputStreamWriter writer = new OutputStreamWriter(
                  new FileOutputStream(file), (String)encoding);
              writer.write(textPane.getText());
              writer.flush();
              writer.close();
              docChanged = false;
              updateTitle();
            } catch(UnsupportedEncodingException usee) {
              JOptionPane.showMessageDialog(frame,
                                            "Unsupported encoding!\n" +
                                            "Please choose another.",
                                            "Gate",
                                            JOptionPane.ERROR_MESSAGE);
              docChanged = true;
              updateTitle();
            } catch(IOException ioe) {
              JOptionPane.showMessageDialog(frame,
                                            "Input/Output error!\n" +
                                            "Please try again.",
                                            "Gate",
                                            JOptionPane.ERROR_MESSAGE);
              docChanged = true;
              updateTitle();
            }
          }// else
        }// if
      }// actionPerformed(ActionEvent e)
    };
    saveAction.putValue(Action.SHORT_DESCRIPTION, "Save...");

    //SAVE AS ACTION
    saveAsAction = new AbstractAction("Save as...", new ImageIcon(
            guk.Editor.class.getResource("img/saveFile.gif"))){
      public void actionPerformed(ActionEvent e) {
          filer.setMultiSelectionEnabled(false);
          filer.setDialogTitle("Select file to save to...");
          filer.setSelectedFile(null);
          filer.setFileFilter(filer.getAcceptAllFileFilter());
          int res = filer.showSaveDialog(frame);
          if(res == filer.APPROVE_OPTION){
            File newFile = filer.getSelectedFile();
            if(newFile == null) return;
            int res1 = JOptionPane.OK_OPTION;
            if(newFile.exists()){
              res1 = JOptionPane.showConfirmDialog(
                      frame,
                      "Overwrite existing file " + newFile.getName() + "?",
                      "Gate",
                      JOptionPane.OK_CANCEL_OPTION,
                      JOptionPane.WARNING_MESSAGE);
            }
            if(res1 == JOptionPane.OK_OPTION){
              file = newFile;
              docChanged = true;
              saveAction.actionPerformed(null);
            }
          }
      }// actionPerformed(ActionEvent e)
    };
    saveAsAction.putValue(Action.SHORT_DESCRIPTION, "Save as...");

    //CLOSE ACTION
    closeAction = new AbstractAction("Close", new ImageIcon(
            guk.Editor.class.getResource("img/closeFile.gif"))){
      public void actionPerformed(ActionEvent e){
        int res = JOptionPane.OK_OPTION;
        if(docChanged){
          res = JOptionPane.showConfirmDialog(
                frame,
                "Close unsaved file " +
                (file== null?"Untitled":file.getName()) + "?",
                "Gate",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        }
        if(res == JOptionPane.OK_OPTION){
          textPane.selectAll();
          textPane.replaceSelection("");
          docChanged = false;
          file = null;
          updateTitle();
        }
      }// actionPerformed(ActionEvent e)
    };
    closeAction.putValue(Action.SHORT_DESCRIPTION, "Close...");


    //EXIT ACTION
    exitAction = new AbstractAction("Exit", new ImageIcon(
            guk.Editor.class.getResource("img/exit.gif"))){
      public void actionPerformed(ActionEvent e){
        int res = JOptionPane.OK_OPTION;
        if(docChanged){
          res = JOptionPane.showConfirmDialog(
                frame,
                "Close unsaved file " +
                (file== null?"Untitled":file.getName()) + "?",
                "Gate",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        }
        if(res == JOptionPane.OK_OPTION){
          frame.setVisible(false);
          frame.dispose();
        }
      }// actionPerformed(ActionEvent e)
    };
    exitAction.putValue(Action.SHORT_DESCRIPTION, "Exit...");

    //UNDO ACTION
    undoAction = new AbstractAction("Undo", new ImageIcon(
            guk.Editor.class.getResource("img/undo.gif"))){
      public void actionPerformed(ActionEvent e){
        if(undoManager.canUndo()) undoManager.undo();
      }
    };
     undoAction.setEnabled(undoManager.canUndo());
     undoAction.putValue(Action.SHORT_DESCRIPTION, "Undo...");

    //REDO ACTION
    redoAction = new AbstractAction("Redo", new ImageIcon(
            guk.Editor.class.getResource("img/redo.gif"))){
      public void actionPerformed(ActionEvent e){
        if(undoManager.canRedo()) undoManager.redo();
      }
    };
    redoAction.setEnabled(undoManager.canRedo());
    redoAction.putValue(Action.SHORT_DESCRIPTION, "Redo...");

    //COPY ACTION
    copyAction = new AbstractAction("Copy", new ImageIcon(
            guk.Editor.class.getResource("img/copy.gif"))){
      public void actionPerformed(ActionEvent e){
        textPane.copy();
      }
    };
    copyAction.putValue(Action.SHORT_DESCRIPTION, "Copy...");

    //CUT ACTION
    cutAction = new AbstractAction("Cut", new ImageIcon(
            guk.Editor.class.getResource("img/cut.gif"))){
      public void actionPerformed(ActionEvent e){
        textPane.cut();
      }
    };
    cutAction.putValue(Action.SHORT_DESCRIPTION, "Cut...");

    //PASTE ACTION
    pasteAction = new AbstractAction("Paste", new ImageIcon(
            guk.Editor.class.getResource("img/paste.gif"))){
      public void actionPerformed(ActionEvent e){
        textPane.paste();
      }
    };
    pasteAction.putValue(Action.SHORT_DESCRIPTION, "Paste...");

    //attributesChangedAction
    attributesChangedAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        //change the selection
        MutableAttributeSet as = textPane.getInputAttributes();
        StyleConstants.setFontFamily(as,
                                    (String)fontsComboBox.getSelectedItem());
        StyleConstants.setFontSize(as,
                                   Integer.parseInt(
                                   (String)sizeComboBox.getSelectedItem()));
        textPane.setCharacterAttributes(as, false);
        //restore selection
        textPane.setCaretPosition(start);
        textPane.moveCaretPosition(end);
      }// actionPerformed(ActionEvent e)
    };

    textPane.addPropertyChangeListener("document", new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
        undoAction.setEnabled(undoManager.canUndo());
        redoAction.setEnabled(undoManager.canRedo());
        //add the document listener
        textPane.getDocument().addDocumentListener(new DocumentListener(){
          public void insertUpdate(DocumentEvent e){
            changeOccured();
          }
          public void removeUpdate(DocumentEvent e){
            changeOccured();
          }
          public void changedUpdate(DocumentEvent e){
            changeOccured();
          }
          protected void changeOccured(){
            undoAction.setEnabled(undoManager.canUndo());
            undoAction.putValue(Action.SHORT_DESCRIPTION,
                                undoManager.getUndoPresentationName());
            redoAction.setEnabled(undoManager.canRedo());
            redoAction.putValue(Action.SHORT_DESCRIPTION,
                                undoManager.getRedoPresentationName());
            if(docChanged) return;
            else{
              docChanged = true;
              updateTitle();
            }
          }// changeOccured()
        });
        //add the document UNDO listener
        undoManager.discardAllEdits();
        textPane.getDocument().addUndoableEditListener(undoManager);
      }// propertyChange(PropertyChangeEvent evt)
    });

    fontsComboBox = new JComboBox(
                        GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getAvailableFontFamilyNames()
                        );
    fontsComboBox.setEditable(false);
    fontsComboBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        attributesChangedAction.actionPerformed(null);
      }// actionPerformed(ActionEvent e)
    });


    sizeComboBox = new JComboBox(new Object[]{"6", "8", "10", "12", "14", "16",
                                              "18", "20", "22", "24", "26"});
    sizeComboBox.setEditable(true);
    sizeComboBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        try {
          Integer.parseInt((String)sizeComboBox.getSelectedItem());
          //fire the action
          attributesChangedAction.actionPerformed(null);
        } catch(NumberFormatException nfe){
          //invalid input, go to default
          sizeComboBox.setSelectedIndex(3);
        }
      }//actionPerformed(ActionEvent e)
    });

    //initialisation for the fonts and size combos
    fontsComboBox.setSelectedItem(StyleConstants.getFontFamily(
                                  textPane.getInputAttributes()));
    sizeComboBox.setSelectedItem(String.valueOf(StyleConstants.getFontSize(
                                  textPane.getInputAttributes())));
    //keep them updated
    textPane.addCaretListener(new CaretListener(){
      public void caretUpdate(CaretEvent e) {
        if(e.getDot() == e.getMark()){
          fontsComboBox.setSelectedItem(StyleConstants.getFontFamily(
                                        textPane.getCharacterAttributes()));
          sizeComboBox.setSelectedItem(String.valueOf(StyleConstants.getFontSize(
                                        textPane.getCharacterAttributes())));
        }
      }//caretUpdate(CaretEvent e)
    });

    fontsComboBox.setMaximumSize(new Dimension(150,25));
    //fontsComboBox.setMinimumSize(new Dimension(150,25));
    fontsComboBox.setPreferredSize(new Dimension(150,25));
    //fontsComboBox.setSize(new Dimension(150,25));
    sizeComboBox.setMaximumSize(new Dimension(50,25));
    //sizeComboBox.setMinimumSize(new Dimension(30,25));
    sizeComboBox.setPreferredSize(new Dimension(50,25));
    //sizeComboBox.setSize(new Dimension(30,25));
    sizeComboBox.enableInputMethods(false);
    //setIconImage(Toolkit.getDefaultToolkit().createImage(EditorFrame.class.getResource("[Your Icon]")));
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(new BorderLayout());
    this.setSize(new Dimension(800, 600));
    updateTitle();
    jMenuFile.setText("File");
    jMenuEdit.setText("Edit");
    jMenuHelp.setText("Help");
    jMenuHelpAbout.setText("About");
    jMenuHelpAbout.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        jMenuHelpAbout_actionPerformed(e);
      }
    });
    jMenuOptions.setText("Options");
    jCheckBoxMenuItemKeyboardMap.setText("Keyboard Map");
    jCheckBoxMenuItemKeyboardMap.setSelected(false);
    jCheckBoxMenuItemKeyboardMap.setMnemonic('0');
    jCheckBoxMenuItemKeyboardMap.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        jCheckBoxMenuItemKeyboardMap_stateChanged(e);
      }
    });
    jToolBar.add(openAction);
    jToolBar.add(saveAction);
    jToolBar.add(closeAction);
    jToolBar.addSeparator();
    jToolBar.add(undoAction);
    jToolBar.add(redoAction);
    jToolBar.addSeparator();
    jToolBar.add(cutAction);
    jToolBar.add(copyAction);
    jToolBar.add(pasteAction);
    jToolBar.addSeparator();
    jToolBar.add(fontsComboBox);
    jToolBar.addSeparator();
    jToolBar.add(sizeComboBox);

    jToolBar.add(Box.createHorizontalGlue());

    jMenuFile.add(openAction);
    jMenuFile.add(saveAction);
    jMenuFile.add(saveAsAction);
    jMenuFile.add(closeAction);
    jMenuFile.addSeparator();
    jMenuFile.add(exitAction);

    jMenuEdit.add(cutAction);
    jMenuEdit.add(copyAction);
    jMenuEdit.add(pasteAction);
    jMenuEdit.addSeparator();
    jMenuEdit.add(undoAction);
    jMenuEdit.add(redoAction);

    jMenuOptions.add(jCheckBoxMenuItemKeyboardMap);
    if(jMenuIM != null) jMenuOptions.add(jMenuIM);

    jMenuHelp.add(jMenuHelpAbout);

    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuEdit);
    jMenuBar1.add(jMenuOptions);
    jMenuBar1.add(jMenuHelp);

//    textPane.setEditorKit(new UnicodeStyledEditorKit(GUK.getFontSet()));
    textPane.setEditorKit(new StyledEditorKit());
    textPane.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));
    this.setJMenuBar(jMenuBar1);
    contentPane.add(jToolBar, BorderLayout.NORTH);
    contentPane.add(new JScrollPane(textPane), BorderLayout.CENTER);
  }// jbInit()

  protected void updateTitle(){
    String title = "Gate Unicode Editor - ";
    if(file != null) title += file.getName();
    else title += "Untitled";
    if(docChanged) title += "*";
    frame.setTitle(title);
  }// updateTitle()

  /**
   * Main method
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    /*
    Object[] ffs = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for(int i = 0; i < ffs.length; i++) System.out.println(ffs[i]);
    */
    new Editor();
  }// main

  /**
   * Help | About action performed
   */
  public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
    Editor_AboutBox dlg = new Editor_AboutBox(this);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
                    (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.show();
  }// jMenuHelpAbout_actionPerformed(ActionEvent e)

  /**
   * Overridden so we can exit when window is closed
   */
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      exitAction.actionPerformed(null);
    } else {
      super.processWindowEvent(e);
    }
  }// processWindowEvent(WindowEvent e)

  void jCheckBoxMenuItemKeyboardMap_stateChanged(ActionEvent e) {
    Object imObject = getInputContext().getInputMethodControlObject();
    if(imObject != null && imObject instanceof GateIM){
      ((GateIM)imObject).setMapVisible(jCheckBoxMenuItemKeyboardMap.getState());
    }else jCheckBoxMenuItemKeyboardMap.setState(false);
  }// void jCheckBoxMenuItemKeyboardMap_stateChanged(ActionEvent e)
}// class Editor extends JFrame

class LocaleSelectorMenuItem extends JRadioButtonMenuItem {
  public LocaleSelectorMenuItem(Locale locale, Frame pframe){
    super(locale.getDisplayName());
    this.frame = pframe;
    me = this;
    myLocale = locale;
    this.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        me.setSelected(frame.getInputContext().selectInputMethod(myLocale));
      }
    });
  }// LocaleSelectorMenuItem(Locale locale, Frame pframe)
  Locale myLocale;
  JRadioButtonMenuItem me;
  Frame frame;
}// class LocaleSelectorMenuItem extends JRadioButtonMenuItem