package gate.gui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.awt.event.*;
import javax.swing.event.*;
import java.beans.*;

import gate.swing.*;
import gate.event.*;

/**
 * A frame used by Gate to display Help information.
 * It is a basic HTML browser.
 */
public class HelpFrame extends JFrame implements StatusListener {

  public HelpFrame(){
    super();
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    getContentPane().setLayout(new BorderLayout());
    textPane = new XJEditorPane();
    textPane.setEditable(false);
    getContentPane().add(new JScrollPane(textPane), BorderLayout.CENTER);

    toolBar = new JToolBar();
    toolBar.add(textPane.getBackAction());
    toolBar.add(textPane.getForwardAction());

    getContentPane().add(toolBar, BorderLayout.NORTH);

    Box southBox = Box.createHorizontalBox();
    southBox.add(new JLabel(" "));
    status = new JLabel();
    southBox.add(status);
    getContentPane().add(southBox, BorderLayout.SOUTH);

  }

  protected void initListeners(){
    textPane.addPropertyChangeListener(new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent e) {
        if(e.getPropertyName().equals("document")){
          String title = (String)textPane.getDocument().
                                          getProperty("title");
          setTitle((title == null) ?
                   "Gate help browser" :
                   title + " - Gate help browser");
        }
      }
    });

    textPane.addStatusListener(this);
  }

  public void setPage(URL newPage) throws IOException{
    textPane.setPage(newPage);
    String title = (String)textPane.getDocument().
                                    getProperty(Document.TitleProperty);
    setTitle((title == null) ?
             "Gate help browser" :
             title + " - Gate help browser");
  }

  XJEditorPane textPane;
  JToolBar toolBar;
  JLabel status;
  public void statusChanged(String e) {
    status.setText(e);
  }
}