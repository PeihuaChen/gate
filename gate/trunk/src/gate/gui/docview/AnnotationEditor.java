/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AnnotationEditor.java
 *
 *  Valentin Tablan, Apr 5, 2004
 *
 *  $Id$
 */

package gate.gui.docview;

import gate.Annotation;
import gate.gui.MainFrame;
import gate.util.GateRuntimeException;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;


/**
 * @author Valentin Tablan
 *
 */
public class AnnotationEditor{
  /**
   * 
   */
  public AnnotationEditor(TextualDocumentView textView,
                          AnnotationSetsView setsView){
    this.textView = textView;
    textPane = (JEditorPane)((JScrollPane)textView.getGUI())
    			.getViewport().getView();
    this.setsView = setsView;
    initGUI();
  }
  
  
  protected void initGUI(){
    //initialise static windows if not already done
    if(topWindow == null){
      solAction = new StartOffsetLeftAction();
      sorAction = new StartOffsetRightAction();
      eolAction = new EndOffsetLeftAction();
      eorAction = new EndOffsetRightAction();
      delAction = new DeleteAnnotationAction();
      
      topWindow = new JWindow(SwingUtilities.
              getWindowAncestor(textView.getGUI()));

      JPanel pane = new JPanel();
      pane.setLayout(new GridBagLayout());
      pane.setBackground(textPane.getBackground());
      pane.setBackground(textPane.getBackground());
      GridBagConstraints constraints = new GridBagConstraints();
      
      typeCombo = new JComboBox();
      constraints.fill = GridBagConstraints.BOTH;
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.gridwidth = 5;
      constraints.ipadx = 0;
      pane.add(typeCombo, constraints);
      
      JButton btn = new JButton(solAction);
      btn.setContentAreaFilled(false);
//      btn.setHorizontalAlignment(JButton.CENTER);
      Dimension btnSize = new Dimension(16, 16);
      btn.setPreferredSize(btnSize);
      constraints.fill = GridBagConstraints.NONE;
      constraints.gridwidth = 1;
      constraints.gridy = 1;
      constraints.gridx = GridBagConstraints.RELATIVE;
      pane.add(btn, constraints);
      
      btn = new JButton(sorAction);
      btn.setContentAreaFilled(false);
//      btn.setBackground(textPane.getBackground());
//      btn.setHorizontalAlignment(JButton.CENTER);
      btn.setPreferredSize(btnSize);
      pane.add(btn, constraints);
      
      btn = new JButton(delAction);
      btn.setContentAreaFilled(false);
//      btn.setBackground(textPane.getBackground());
//      btn.setHorizontalAlignment(JButton.CENTER);
//      btn.setIconTextGap(0);
      constraints.insets = new Insets(0, 10, 0, 10);
      btn.setPreferredSize(new Dimension(22, 22));
      pane.add(btn, constraints);
      
      btn = new JButton(eolAction);
      btn.setContentAreaFilled(false);
//      btn.setBackground(textPane.getBackground());
//      btn.setHorizontalAlignment(JButton.CENTER);
      constraints.insets = new Insets(0, 0, 0, 0);
      btn.setPreferredSize(btnSize);
      pane.add(btn, constraints);
      
      btn = new JButton(eorAction);
      btn.setContentAreaFilled(false);
//      btn.setBackground(textPane.getBackground());
//      btn.setHorizontalAlignment(JButton.CENTER);
      btn.setPreferredSize(btnSize);
      pane.add(btn, constraints);

      pane.setBorder(BorderFactory.createRaisedBevelBorder());
      topWindow.getContentPane().add(pane);
      hideTimer = new Timer(HIDE_DELAY, new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          topWindow.hide();
        }
      });
      hideTimer.setRepeats(false);
      topWindow.addMouseListener(new MouseAdapter(){
        public void mouseEntered(MouseEvent evt){
          hideTimer.stop();
        }
      });
    }
  }
  
  public void setAnnotation(Annotation ann){
   this.ann = ann; 
  }
  
  
  /**
   * Shows the UI(s) involved in annotation editing.
   *
   */
  public void show(){
    //calculate position
    int x, y;
    try{
		  Rectangle startRect = textPane.modelToView(ann.getStartNode().
		    getOffset().intValue());
		  x = startRect.x;
		  y = startRect.y;
    }catch(BadLocationException ble){
      //this should never occur
      throw new GateRuntimeException(ble);
    }
    Point topLeft = textPane.getLocationOnScreen();
    topWindow.pack();
    topWindow.setLocation(x + topLeft.x, 
                          y + topLeft.y - topWindow.getSize().height);
    topWindow.setVisible(true);
    topWindow.pack();
    hideTimer.restart();
  }
  
  /**
   * Base class for actions on annotations.
   */
  protected static abstract class AnnotationAction extends AbstractAction{
    public AnnotationAction(String name, Icon icon){
      super("", icon);
      putValue(SHORT_DESCRIPTION, name);
      
    }
    public void actionPerformed(ActionEvent evt){
    }
    
    public void setAnnotation(Annotation ann){
      this.ann = ann;
    }
    Annotation ann;
  }

  protected class StartOffsetLeftAction extends AnnotationAction{
    public StartOffsetLeftAction(){
      super("Extend", MainFrame.getIcon("extend-left.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      
    }
  }
  
  protected class StartOffsetRightAction extends AnnotationAction{
    public StartOffsetRightAction(){
      super("Srink", MainFrame.getIcon("extend-right.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      
    }
  }

  protected class EndOffsetLeftAction extends AnnotationAction{
    public EndOffsetLeftAction(){
      super("Srink", MainFrame.getIcon("extend-left.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      
    }
  }
  
  protected class EndOffsetRightAction extends AnnotationAction{
    public EndOffsetRightAction(){
      super("Extend", MainFrame.getIcon("extend-right.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      
    }
  }
  
  
  protected class DeleteAnnotationAction extends AnnotationAction{
    public DeleteAnnotationAction(){
      super("Delete", MainFrame.getIcon("delete.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      
    }
  }
  
  
  protected static JWindow topWindow;
  protected static JWindow bottomWindow;
  protected static JComboBox typeCombo;
  
  protected static StartOffsetLeftAction solAction;
  protected static StartOffsetRightAction sorAction;
  protected static EndOffsetLeftAction eolAction;
  protected static EndOffsetRightAction eorAction;
  
  protected static DeleteAnnotationAction delAction;
  protected static Timer hideTimer;
  protected static final int HIDE_DELAY = 1500;
  
  
  protected TextualDocumentView textView;
  protected AnnotationSetsView setsView;
  protected JEditorPane textPane;
  protected Annotation ann;
}
