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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.BadLocationException;

import gate.*;
import gate.creole.AnnotationSchema;
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
    textPane = (JEditorPane)((JScrollPane)textView.getGUI())
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
  
  protected void initTopWindow(Window parent){
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

    btn = new JButton(new DismissAction());
    btn.setContentAreaFilled(false);
    constraints.insets = new Insets(0, 2, 2, 2);
    constraints.gridheight = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    btn.setMargin(insets0);
    btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    constraints.gridy = 0;
    pane.add(btn, constraints);
    
    btn = new JButton(new ApplyAction());
    btn.setContentAreaFilled(false);
    btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    btn.setMargin(insets0);
    constraints.gridy = 1;
//    pane.add(btn, constraints);
    
    pane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    topWindow.getContentPane().add(pane);
  }
  
  protected void initBottomWindow(Window parent){
    bottomWindow = new JWindow(parent){
      
    };
    bottomWindow.getContentPane().setLayout(new BorderLayout());
    bottomWindow.getContentPane().setBackground(
            UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
//    bottomWindow.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    
//    GridBagConstraints constraints = new GridBagConstraints();
//    constraints.fill = GridBagConstraints.BOTH;
//    constraints.weightx = 1;
//    constraints.weighty = 1;
    featuresEditor = new FeaturesSchemaEditor();
    featuresEditor.setBackground(UIManager.getLookAndFeelDefaults().
            getColor("ToolTip.background"));
//    featuresEditor.getTableHeader().setBackground(UIManager.getLookAndFeelDefaults().
//            getColor("ToolTip.background"));
    JScrollPane scroller = new JScrollPane(featuresEditor){
//      public Dimension getPreferredSize(){
//        Dimension viewSize = ((Scrollable)getViewport().getView()).
//          getPreferredScrollableViewportSize();
//        int width = viewSize.width + 
//          (verticalScrollBar.isVisible() ? 
//           verticalScrollBar.getSize().width : 0) +
//           (rowHeader != null && rowHeader.isVisible() ? rowHeader.getSize().width : 0);
//        int height = viewSize.height + 
//          (horizontalScrollBar.isVisible() ? 
//           horizontalScrollBar.getSize().height : 0) +
//          (columnHeader != null && columnHeader.isVisible() ? columnHeader.getSize().height : 0);
//        return new Dimension(width, height);
//      }
    };
    bottomWindow.getContentPane().add(scroller, BorderLayout.CENTER);
//    bottomWindow.getContentPane().add(featuresEditor, constraints);
  }

  protected void initListeners(){
    MouseListener windowMouseListener = new MouseAdapter(){
      public void mouseEntered(MouseEvent evt){
        hideTimer.stop();
      }
    };
    topWindow.getRootPane().addMouseListener(windowMouseListener);
    bottomWindow.addMouseListener(windowMouseListener);
    featuresEditor.addMouseListener(windowMouseListener);
    
    typeCombo.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String newType = typeCombo.getSelectedItem().toString();
        if(ann != null && ann.getType().equals(newType)) return;
        if(ann == null){
          //annotation creation
        }else{
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
          }catch(InvalidOffsetException ioe){
            throw new GateRuntimeException(ioe);
          }
        }
      }
    });
    
//    featuresEditor.getModel().addTableModelListener(new TableModelListener(){
//      public void tableChanged(TableModelEvent e){
//        sizeWindows();
//        placeWindows();
//      }
//    });
    featuresEditor.addComponentListener(new ComponentAdapter(){
      public void componentResized(ComponentEvent e){
        sizeWindows();
        placeWindows();
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
    initTopWindow(SwingUtilities.getWindowAncestor(textView.getGUI()));
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
   java.util.List typeList = new ArrayList(types);
   Collections.sort(typeList);
   typeCombo.setModel(new DefaultComboBoxModel(typeList.toArray()));
   typeCombo.setSelectedItem(annType);
   
   featuresEditor.setSchema((AnnotationSchema)schemasByType.get(annType));
   featuresEditor.setFeatures(ann.getFeatures());
  }
  
  public boolean isShowing(){
    return topWindow.isShowing();
  }

  protected void sizeWindows(){
    topWindow.pack();
    bottomWindow.pack();
    Dimension topSize = topWindow.getPreferredSize();
    Dimension bottomSize = bottomWindow.getPreferredSize();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int width = Math.max(topSize.width, bottomSize.width);
    width = Math.min(width, screenSize.width /2);
    topWindow.setSize(width, topSize.height);
    bottomWindow.setSize(width, bottomSize.height);
  }
  /**
   * Shows the UI(s) involved in annotation editing.
   *
   */
  public void show(boolean autohide){
    sizeWindows();
    placeWindows();
    topWindow.setVisible(true);
    bottomWindow.setVisible(true);
    if(autohide) hideTimer.restart();
  }
  
  protected void placeWindows(){
    //calculate position
    int x, yTop, yBottom;
    try{
		  Rectangle startRect = textPane.modelToView(ann.getStartNode().
		    getOffset().intValue());
		  x = startRect.x;
		  yTop = startRect.y;
		  Rectangle endRect = textPane.modelToView(ann.getEndNode().
				    getOffset().intValue());
		  yBottom = endRect.y + endRect.height;
    }catch(BadLocationException ble){
      //this should never occur
      throw new GateRuntimeException(ble);
    }
    Point topLeft = textPane.getLocationOnScreen();
    topWindow.setLocation(x + topLeft.x, 
            yTop + topLeft.y - topWindow.getSize().height); 
    bottomWindow.setLocation(x + topLeft.x, yBottom + topLeft.y); 
  }
  
  public void hide(){
    topWindow.setVisible(false);
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
              MainFrame.getIcon("extend-left.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      Annotation oldAnn = ann;
      Integer oldID = ann.getId();
      Long newStartOffset = ann.getStartNode().getOffset();
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      long newValue = newStartOffset.longValue() - increment;
      if(newValue < 0) newValue = 0;
      newStartOffset = new Long(newValue);
      try{
        set.remove(oldAnn);
	      set.add(oldID, newStartOffset, ann.getEndNode().getOffset(),
	              ann.getType(), ann.getFeatures());
	      setAnnotation(set.get(oldID), set);
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class StartOffsetRightAction extends AnnotationAction{
    public StartOffsetRightAction(){
      super("<html><b>Shrink</b><br><small>SHIFT = 5 characters, " +
            "CTRL-SHIFT = 10 characters</small></html>", 
            MainFrame.getIcon("extend-right.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      Annotation oldAnn = ann;
      Integer oldID = ann.getId();
      long endOffset = ann.getEndNode().getOffset().longValue(); 
      Long newStartOffset = ann.getStartNode().getOffset();
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      
      long newValue = newStartOffset.longValue()  + increment;
      if(newValue > endOffset) newValue = endOffset;
      newStartOffset = new Long(newValue);
      try{
        set.remove(oldAnn);
	      set.add(oldID, newStartOffset, ann.getEndNode().getOffset(),
	              ann.getType(), ann.getFeatures());
	      setAnnotation(set.get(oldID), set);
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }

  protected class EndOffsetLeftAction extends AnnotationAction{
    public EndOffsetLeftAction(){
      super("<html><b>Shrink</b><br><small>SHIFT = 5 characters, " +
            "CTRL-SHIFT = 10 characters</small></html>",
            MainFrame.getIcon("extend-left.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      Annotation oldAnn = ann;
      Integer oldID = ann.getId();
      long startOffset = ann.getStartNode().getOffset().longValue(); 
      Long newEndOffset = ann.getEndNode().getOffset();
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment =CTRL_SHIFT_INCREMENT;
        }
      }
      
      long newValue = newEndOffset.longValue()  - increment;
      if(newValue < startOffset) newValue = startOffset;
      newEndOffset = new Long(newValue);
      try{
        set.remove(oldAnn);
	      set.add(oldID, ann.getStartNode().getOffset(), newEndOffset,
	              ann.getType(), ann.getFeatures());
	      setAnnotation(set.get(oldID), set);
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  protected class EndOffsetRightAction extends AnnotationAction{
    public EndOffsetRightAction(){
      super("<html><b>Extend</b><br><small>SHIFT = 5 characters, " +
            "CTRL-SHIFT = 10 characters</small></html>", 
            MainFrame.getIcon("extend-right.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      Annotation oldAnn = ann;
      Integer oldID = ann.getId();
      long maxOffset = textView.getDocument().
      		getContent().size().longValue() -1; 
      Long newEndOffset = ann.getEndNode().getOffset();
      int increment = 1;
      if((evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0){
        //CTRL pressed -> use tokens for advancing
        increment = SHIFT_INCREMENT;
        if((evt.getModifiers() & ActionEvent.CTRL_MASK) > 0){
          increment = CTRL_SHIFT_INCREMENT;
        }
      }
      long newValue = newEndOffset.longValue() + increment;
      if(newValue > maxOffset) newValue = maxOffset;
      newEndOffset = new Long(newValue);
      try{
        set.remove(oldAnn);
	      set.add(oldID, ann.getStartNode().getOffset(), newEndOffset,
	              ann.getType(), ann.getFeatures());
	      setAnnotation(set.get(oldID), set);
      }catch(InvalidOffsetException ioe){
        throw new GateRuntimeException(ioe);
      }
    }
  }
  
  
  protected class DeleteAnnotationAction extends AnnotationAction{
    public DeleteAnnotationAction(){
      super("Delete", MainFrame.getIcon("delete.gif"));
    }
    
    public void actionPerformed(ActionEvent evt){
      set.remove(ann);
      hide();
    }
  }
  
  protected class DismissAction extends AbstractAction{
    public DismissAction(){
      super("Dismiss");
//      putValue(SHORT_DESCRIPTION, "Dismiss");
      
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
  
  
  protected JWindow topWindow;
  protected JWindow bottomWindow;

  protected JComboBox typeCombo;
  protected FeaturesSchemaEditor featuresEditor;
  
  protected StartOffsetLeftAction solAction;
  protected StartOffsetRightAction sorAction;
  protected EndOffsetLeftAction eolAction;
  protected EndOffsetRightAction eorAction;
  
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
  protected JEditorPane textPane;
  protected Annotation ann;
  protected AnnotationSet set; 
}
