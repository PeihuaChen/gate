/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;

import gate.*;
import gate.creole.AnnotationSchema;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.MainFrame;
import gate.util.GateException;
import gate.util.GateRuntimeException;


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
  
  protected static void initStaticData(){
    solAction = new StartOffsetLeftAction();
    sorAction = new StartOffsetRightAction();
    eolAction = new EndOffsetLeftAction();
    eorAction = new EndOffsetRightAction();
    delAction = new DeleteAnnotationAction();
    schemasByType = new HashMap();
    try{
	    java.util.List schemas = Gate.getCreoleRegister().
	    	getAllInstances("gate.creole.AnnotationSchema");
	    for(Iterator schIter = schemas.iterator(); 
	        schIter.hasNext();){
	      AnnotationSchema aSchema = (AnnotationSchema)schIter.next();
	      schemasByType.put(aSchema.getAnnotationName(), aSchema);
	    }
    }catch(GateException ge){
      throw new GateRuntimeException(ge);
    }
    
    CreoleListener creoleListener = new CreoleListener(){
      public void resourceLoaded(CreoleEvent e){
        Resource newResource =  e.getResource();
        if(newResource instanceof AnnotationSchema){
  	      AnnotationSchema aSchema = (AnnotationSchema)newResource;
  	      schemasByType.put(aSchema.getAnnotationName(), aSchema);
        }
      }
      
      public void resourceUnloaded(CreoleEvent e){
        Resource newResource =  e.getResource();
        if(newResource instanceof AnnotationSchema){
  	      AnnotationSchema aSchema = (AnnotationSchema)newResource;
  	      if(schemasByType.containsValue(aSchema)){
  	        schemasByType.remove(aSchema.getAnnotationName());
  	      }
        }
      }
      
      public void datastoreOpened(CreoleEvent e){
        
      }
      public void datastoreCreated(CreoleEvent e){
        
      }
      public void datastoreClosed(CreoleEvent e){
        
      }
      public void resourceRenamed(Resource resource,
                              String oldName,
                              String newName){
      }  
    };
    Gate.getCreoleRegister().addCreoleListener(creoleListener); 
  }
  
  protected static void initTopWindow(Window parent){
    topWindow = new JWindow(parent);

    JPanel pane = new JPanel();
    pane.setLayout(new GridBagLayout());
    pane.setBackground(UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
    
    GridBagConstraints constraints = new GridBagConstraints();
    
    
    typeCombo = new JComboBox();
    typeCombo.setEditable(true);
    typeCombo.setBackground(UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 5;
    constraints.ipadx = 0;
    pane.add(typeCombo, constraints);
    
    JButton btn = new JButton(solAction);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    Insets insets0 = new Insets(0, 0, 0, 0);
    btn.setMargin(insets0);
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridwidth = 1;
    constraints.gridy = 1;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.weightx = 0;
    constraints.weighty= 0;
    pane.add(btn, constraints);
    
    btn = new JButton(sorAction);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setMargin(insets0);
    pane.add(btn, constraints);
    
    btn = new JButton(delAction);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setMargin(insets0);
    constraints.weightx = 1;
    pane.add(btn, constraints);
    
    btn = new JButton(eolAction);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setMargin(insets0);
    constraints.insets = insets0;
    constraints.weightx = 0;
    pane.add(btn, constraints);
    
    btn = new JButton(eorAction);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setMargin(insets0);
    pane.add(btn, constraints);

    pane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
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
  
  protected static void initBottomWindow(Window parent){
  }

  protected void initGUI(){
    //initialise static windows if not already done
    if(! inited){
      initStaticData();
      initTopWindow(SwingUtilities.getWindowAncestor(textView.getGUI()));
      initBottomWindow(SwingUtilities.getWindowAncestor(textView.getGUI()));
      inited = true;
    }
  }
  
  public void setAnnotation(Annotation ann){
   this.ann = ann;
   //repopulate the types combo
   String annType = ann.getType();
   Set types = new HashSet(schemasByType.keySet());
   types.add(annType);
   java.util.List typeList = new ArrayList(types);
   Collections.sort(typeList);
   typeCombo.setModel(new DefaultComboBoxModel(typeList.toArray()));
   typeCombo.setSelectedItem(annType);
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

  protected static class StartOffsetLeftAction extends AnnotationAction{
    public StartOffsetLeftAction(){
      super("Extend", MainFrame.getIcon("extend-left.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      
    }
  }
  
  protected static class StartOffsetRightAction extends AnnotationAction{
    public StartOffsetRightAction(){
      super("Srink", MainFrame.getIcon("extend-right.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      
    }
  }

  protected static class EndOffsetLeftAction extends AnnotationAction{
    public EndOffsetLeftAction(){
      super("Srink", MainFrame.getIcon("extend-left.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      
    }
  }
  
  protected static class EndOffsetRightAction extends AnnotationAction{
    public EndOffsetRightAction(){
      super("Extend", MainFrame.getIcon("extend-right.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      
    }
  }
  
  
  protected static class DeleteAnnotationAction extends AnnotationAction{
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
  
  protected static boolean inited = false;
  
  /**
   * Stores the Annotation schema objects available in the system.
   * The annotation types are used as keys for the map.
   */
  protected static Map schemasByType;
  
  
  protected TextualDocumentView textView;
  protected AnnotationSetsView setsView;
  protected JEditorPane textPane;
  protected Annotation ann;
}
