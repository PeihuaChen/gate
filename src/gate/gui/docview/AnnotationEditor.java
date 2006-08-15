/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
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
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;

import gate.*;
import gate.creole.AnnotationSchema;
import gate.creole.ResourceInstantiationException;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.FeaturesSchemaEditor;
import gate.gui.MainFrame;
import gate.util.*;
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
    textPane = (JTextArea)((JScrollPane)textView.getGUI())
    			.getViewport().getView();
    this.setsView = setsView;
    initGUI();
  }
  
  protected void initData(){
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
  
  protected void initBottomWindow(Window parent){
    bottomWindow = new JWindow(parent);
    JPanel pane = new JPanel();
    pane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    pane.setLayout(new GridBagLayout());
    pane.setBackground(UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
    bottomWindow.setContentPane(pane);

    Insets insets0 = new Insets(0, 0, 0, 0);
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridwidth = 1;
    constraints.gridy = 0;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.weightx = 0;
    constraints.weighty= 0;
    constraints.insets = insets0;

    JButton btn = new JButton(solAction);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setMargin(insets0);
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
    constraints.insets = new Insets(0, 20, 0, 20);
    pane.add(btn, constraints);
    constraints.insets = insets0;
    
    btn = new JButton(eolAction);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setMargin(insets0);
    pane.add(btn, constraints);
    
    btn = new JButton(eorAction);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setMargin(insets0);
    pane.add(btn, constraints);
    
    dismissAction = new DismissAction(); 
    btn = new JButton(dismissAction);
    constraints.insets = new Insets(0, 10, 0, 0);
    constraints.anchor = GridBagConstraints.NORTHEAST;
    constraints.weightx = 1;
    btn.setBorder(null);
    pane.add(btn, constraints);
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.insets = insets0;

    
    typeCombo = new JComboBox();
    typeCombo.setEditable(true);
    typeCombo.setBackground(UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridy = 1;
    constraints.gridwidth = 6;
    constraints.weightx = 1;
    constraints.insets = new Insets(3, 2, 2, 2);
    pane.add(typeCombo, constraints);
    
    featuresEditor = new FeaturesSchemaEditor();
    featuresEditor.setBackground(UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
    try{
      featuresEditor.init();
    }catch(ResourceInstantiationException rie){
      throw new GateRuntimeException(rie);
    }
    scroller = new JScrollPane(featuresEditor.getTable());
    
    constraints.gridy = 2;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;
    pane.add(scroller, constraints);
  }
  

  protected void initListeners(){
    MouseListener windowMouseListener = new MouseAdapter(){
      public void mouseEntered(MouseEvent evt){
        hideTimer.stop();
      }
    };

    bottomWindow.getRootPane().addMouseListener(windowMouseListener);
//    featuresEditor.addMouseListener(windowMouseListener);
    
    ((JComponent)bottomWindow.getContentPane()).
    		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
    		put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "dismiss");
    ((JComponent)bottomWindow.getContentPane()).
    		getActionMap().put("dismiss", dismissAction);
    
    typeCombo.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String newType = typeCombo.getSelectedItem().toString();
        if(ann != null && ann.getType().equals(newType)) return;
        //annotation editing
        Integer oldId = ann.getId();
        Annotation oldAnn = ann;
        set.remove(ann);
        try{
          set.add(oldId, oldAnn.getStartNode().getOffset(), 
                  oldAnn.getEndNode().getOffset(), 
                  newType, oldAnn.getFeatures());
          setAnnotation(set.get(oldId), set);
          
          setsView.setTypeSelected(set.getName(), newType, true);
          setsView.setLastAnnotationType(newType);
        }catch(InvalidOffsetException ioe){
          throw new GateRuntimeException(ioe);
        }
      }
    });
  }
  
  protected void initGUI(){
    solAction = new StartOffsetLeftAction();
    sorAction = new StartOffsetRightAction();
    eolAction = new EndOffsetLeftAction();
    eorAction = new EndOffsetRightAction();
    delAction = new DeleteAnnotationAction();
    
    initData();
    initBottomWindow(SwingUtilities.getWindowAncestor(textView.getGUI()));
    initListeners();
    
    hideTimer = new Timer(HIDE_DELAY, new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        hide();
      }
    });
    hideTimer.setRepeats(false);
    
  }
  
  public void setAnnotation(Annotation ann, AnnotationSet set){
   this.ann = ann;
   this.set = set;
   //repopulate the types combo
   String annType = ann.getType();
   Set types = new HashSet(schemasByType.keySet());
   types.add(annType);
   types.addAll(set.getAllTypes());
   java.util.List typeList = new ArrayList(types);
   Collections.sort(typeList);
   typeCombo.setModel(new DefaultComboBoxModel(typeList.toArray()));
   typeCombo.setSelectedItem(annType);
   
   featuresEditor.setSchema((AnnotationSchema)schemasByType.get(annType));
   featuresEditor.setTargetFeatures(ann.getFeatures());
   bottomWindow.doLayout();
  }
  
  public boolean isShowing(){
    return bottomWindow.isShowing();
  }
  
  /**
   * Shows the UI(s) involved in annotation editing.
   *
   */
  public void show(boolean autohide){
    placeWindows();
    bottomWindow.setVisible(true);
    if(autohide) hideTimer.restart();
  }
  
  protected void placeWindows(){
    //calculate position
    try{
		  Rectangle startRect = textPane.modelToView(ann.getStartNode().
		    getOffset().intValue());
		  Rectangle endRect = textPane.modelToView(ann.getEndNode().
				    getOffset().intValue());
      Point topLeft = textPane.getLocationOnScreen();
      int x = topLeft.x + startRect.x;
      int y = topLeft.y + endRect.y + endRect.height;

      //make sure the window doesn't start lower 
      //than the end of the visible rectangle
      Rectangle visRect = textPane.getVisibleRect();
      int maxY = topLeft.y + visRect.y + visRect.height;      
      
      //make sure window doesn't get off-screen
      bottomWindow.pack();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      boolean revalidate = false;
      if(bottomWindow.getSize().width > screenSize.width){
        bottomWindow.setSize(screenSize.width, bottomWindow.getSize().height);
        revalidate = true;
      }
      if(bottomWindow.getSize().height > screenSize.height){
        bottomWindow.setSize(bottomWindow.getSize().width, screenSize.height);
        revalidate = true;
      }
      
      if(revalidate) bottomWindow.validate();
      //calculate max X
      int maxX = screenSize.width - bottomWindow.getSize().width;
      //calculate max Y
      if(maxY + bottomWindow.getSize().height > screenSize.height){
        maxY = screenSize.height - bottomWindow.getSize().height;
      }
      
      //correct position
      if(y > maxY) y = maxY;
      if(x > maxX) x = maxX;
      bottomWindow.setLocation(x, y);
      
    }catch(BadLocationException ble){
      //this should never occur
      throw new GateRuntimeException(ble);
    }
  }
  
  /**
   * Changes the span of an existing annotation by creating a new annotation 
   * with the same ID, type and features but with the new start and end offsets.
   * @param set the annotation set 
   * @param oldAnnotation the annotation to be moved
   * @param newStartOffset the new start offset
   * @param newEndOffset the new end offset
   */
  protected void moveAnnotation(AnnotationSet set, Annotation oldAnnotation, 
          Long newStartOffset, Long newEndOffset) throws InvalidOffsetException{
    //Moving is done by deleting the old annotation and creating a new one.
    //If this was the last one of one type it would mess up the gui which 
    //"forgets" about this type and then it recreates it (with a different 
    //colour and not visible
    //We need to store the metadata about this type so we can recreate it if 
    //needed
    AnnotationSetsView.TypeHandler oldHandler = setsView.getTypeHandler(
            set.getName(), oldAnnotation.getType());
    
    Integer oldID = oldAnnotation.getId();
    set.remove(oldAnnotation);
    set.add(oldID, newStartOffset, newEndOffset,
            oldAnnotation.getType(), oldAnnotation.getFeatures());
    setAnnotation(set.get(oldID), set);
    AnnotationSetsView.TypeHandler newHandler = setsView.getTypeHandler(
            set.getName(), oldAnnotation.getType());
    
    if(newHandler != oldHandler){
      //hide all highlights (if any) so we can show them in the right colour
      newHandler.setSelected(false);
      newHandler.colour = oldHandler.colour;
      newHandler.setSelected(oldHandler.isSelected());
    }
  }
  
  public void hide(){
//    topWindow.setVisible(false);
    bottomWindow.setVisible(false);
  }
  
  /**
   * Base class for actions on annotations.
   */
  protected abstract class AnnotationAction extends AbstractAction{
    public AnnotationAction(String name, Icon icon){
      super("", icon);
      putValue(SHORT_DESCRIPTION, name);
      
    }
  }

  protected class StartOffsetLeftAction extends AnnotationAction{
    public StartOffsetLeftAction(){
      super("<html><b>Extend</b><br><small>SHIFT = 5 characters, CTRL-SHIFT = 10 characters</small></html>", 
              MainFrame.getIcon("extend-left"));
    }
    
    public void actionPerformed(ActionEvent evt){
      Annotation oldAnn = ann;
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      long newValue = ann.getStartNode().getOffset().longValue() - increment;
      if(newValue < 0) newValue = 0;
      try{
        moveAnnotation(set, ann, new Long(newValue), 
                ann.getEndNode().getOffset());
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class StartOffsetRightAction extends AnnotationAction{
    public StartOffsetRightAction(){
      super("<html><b>Shrink</b><br><small>SHIFT = 5 characters, " +
            "CTRL-SHIFT = 10 characters</small></html>", 
            MainFrame.getIcon("extend-right"));
    }
    
    public void actionPerformed(ActionEvent evt){
      long endOffset = ann.getEndNode().getOffset().longValue(); 
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      
      long newValue = ann.getStartNode().getOffset().longValue()  + increment;
      if(newValue > endOffset) newValue = endOffset;
      try{
        moveAnnotation(set, ann, new Long(newValue), 
                ann.getEndNode().getOffset());
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }

  protected class EndOffsetLeftAction extends AnnotationAction{
    public EndOffsetLeftAction(){
      super("<html><b>Shrink</b><br><small>SHIFT = 5 characters, " +
            "CTRL-SHIFT = 10 characters</small></html>",
            MainFrame.getIcon("extend-left"));
    }
    
    public void actionPerformed(ActionEvent evt){
      long startOffset = ann.getStartNode().getOffset().longValue(); 
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment =CTRL_SHIFT_INCREMENT;
        }
      }
      
      long newValue = ann.getEndNode().getOffset().longValue()  - increment;
      if(newValue < startOffset) newValue = startOffset;
      try{
        moveAnnotation(set, ann, ann.getStartNode().getOffset(), 
                new Long(newValue));
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class EndOffsetRightAction extends AnnotationAction{
    public EndOffsetRightAction(){
      super("<html><b>Extend</b><br><small>SHIFT = 5 characters, " +
            "CTRL-SHIFT = 10 characters</small></html>", 
            MainFrame.getIcon("extend-right"));
    }
    
    public void actionPerformed(ActionEvent evt){
      long maxOffset = textView.getDocument().
      		getContent().size().longValue() -1; 
//      Long newEndOffset = ann.getEndNode().getOffset();
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      long newValue = ann.getEndNode().getOffset().longValue() + increment;
      if(newValue > maxOffset) newValue = maxOffset;
      try{
        moveAnnotation(set, ann, ann.getStartNode().getOffset(),
                new Long(newValue));
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  
  protected class DeleteAnnotationAction extends AnnotationAction{
    public DeleteAnnotationAction(){
      super("Delete", MainFrame.getIcon("remove-annotation"));
    }
    
    public void actionPerformed(ActionEvent evt){
      set.remove(ann);
      hide();
    }
  }
  
  protected class DismissAction extends AbstractAction{
    public DismissAction(){
      super("");
      Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
      if(icon == null) icon = MainFrame.getIcon("exit");
      putValue(SMALL_ICON, icon);
      putValue(SHORT_DESCRIPTION, "Dismiss");
    }
    
    public void actionPerformed(ActionEvent evt){
      hide();
    }
  }
  
  protected class ApplyAction extends AbstractAction{
    public ApplyAction(){
      super("Apply");
//      putValue(SHORT_DESCRIPTION, "Apply");
    }
    
    public void actionPerformed(ActionEvent evt){
      hide();
    }
  }
  
  protected JWindow bottomWindow;

  protected JComboBox typeCombo;
  protected FeaturesSchemaEditor featuresEditor;
  protected JScrollPane scroller;
  
  protected StartOffsetLeftAction solAction;
  protected StartOffsetRightAction sorAction;
  protected EndOffsetLeftAction eolAction;
  protected EndOffsetRightAction eorAction;
  protected DismissAction dismissAction;
  
  protected DeleteAnnotationAction delAction;
  protected Timer hideTimer;
  protected static final int HIDE_DELAY = 1500;
  protected static final int SHIFT_INCREMENT = 5;
  protected static final int CTRL_SHIFT_INCREMENT = 10;
    
  protected Object highlight;
  
  /**
   * Stores the Annotation schema objects available in the system.
   * The annotation types are used as keys for the map.
   */
  protected Map schemasByType;
  
  
  protected TextualDocumentView textView;
  protected AnnotationSetsView setsView;
  protected JTextArea textPane;
  protected Annotation ann;
  protected AnnotationSet set;
}
