/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 22 March 2004
 *
 *  $Id$
 */
package gate.gui.docview;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import weka.classifiers.trees.adtree.Splitter;

import gate.*;
import gate.creole.*;
import gate.gui.ActionsPublisher;
import gate.swing.VerticalTextIcon;
import gate.util.GateRuntimeException;

/**
 * This is the GATE Document viewer/editor. This class is only the shell of the
 * main document VR, which gets populated with views (objects that implement
 * the {@link DocumentView} interface.
 */

public class DocumentEditor extends AbstractVisualResource
                            implements ActionsPublisher {

  public List getActions() {
    return new ArrayList();
  }


  /* (non-Javadoc)
   * @see gate.Resource#init()
   */
  public Resource init() throws ResourceInstantiationException {
    addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent e) {
      }
      public void componentMoved(ComponentEvent e) {
      }
      public void componentResized(ComponentEvent e) {
      }
      //lazily build the GUI only when needed
      public void componentShown(ComponentEvent e) {
        initViews();
//System.out.println("Docedit shown");        
      }
    });

    return this;
  }
  
  
  protected void initViews(){
    //start building the UI
    setLayout(new BorderLayout());
    JProgressBar progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, progressBar.getPreferredSize().height));
    add(progressBar, BorderLayout.CENTER);

    progressBar.setString("Building views");
    progressBar.setValue(10);

    //create the skeleton UI
    topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, null, null);
    topSplit.setResizeWeight(0.3);
    bottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, null);
    bottomSplit.setResizeWeight(0.7);
    horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomSplit, null);
    horizontalSplit.setResizeWeight(0.7);

    //create the bars
    topBar = new JToolBar(JToolBar.HORIZONTAL);
    topBar.setFloatable(false);
    add(topBar, BorderLayout.NORTH);

    bottomBar = new JToolBar(JToolBar.HORIZONTAL);
    bottomBar.setFloatable(false);
    add(bottomBar, BorderLayout.SOUTH);

    leftBar = new JToolBar(JToolBar.VERTICAL);
    leftBar.setFloatable(false);
    add(leftBar, BorderLayout.WEST);

    rightBar = new JToolBar(JToolBar.VERTICAL);
    rightBar.setFloatable(false);
    add(rightBar, BorderLayout.EAST);

    progressBar.setValue(40);
    //parse all Creole resources and look for document views
    Set vrSet = Gate.getCreoleRegister().getVrTypes();
    Iterator vrIter = vrSet.iterator();
    centralViews = new ArrayList();
    verticalViews = new ArrayList();
    horizontalViews = new ArrayList();

    while(vrIter.hasNext()){
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                           get(vrIter.next());
      try{
        if(DocumentView.class.isAssignableFrom(rData.getResourceClass())){
          //create the resource
          DocumentView aView = (DocumentView)Factory.
                               createResource(rData.getClassName());
          aView.setTarget(document);
          //add the view
          addView(aView, rData.getName());
        }
      }catch(ClassNotFoundException cnfe){
        cnfe.printStackTrace();
      }catch(ResourceInstantiationException rie){
            rie.printStackTrace();
      }
    }
    //select the main central view only
    if(centralViews.size() > 0) setCentralView(0);
    
    //populate the main VIEW
    remove(progressBar);
    add(horizontalSplit, BorderLayout.CENTER);
    validate();
  }
  

  /**
   * Registers a new view by adding it to the right list and creating the 
   * activation button for it.
   * @param view
   */
  protected void addView(DocumentView view, String name){
    switch(view.getType()){
      case DocumentView.CENTRAL :
        centralViews.add(view);
      	leftBar.add(new ViewButton(view, name));
        break;
      case DocumentView.VERTICAL :
        verticalViews.add(view);
      	rightBar.add(new ViewButton(view, name));
        break;
      case DocumentView.HORIZONTAL :
        horizontalViews.add(view);
      	topBar.add(new ViewButton(view, name));
      	bottomBar.add(new ViewButton(view, name));
      default :
        throw new GateRuntimeException(getClass().getName() +  ": Invalid view type");
    }
  }
  
  
  /**
   * Gets the currently showing top view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getTopView(){
    if(topViewIdx == -1) return null;
    else return(DocumentView)horizontalViews.get(topViewIdx);
  }
  
  /**
   * Shows a new top view based on an index in the {@link #horizontalViews}
   * list.
   * @param index the index in {@link #horizontalViews} list for the new
   * view to be shown.
   */
  protected void setTopView(int index){
    //deactivate current view
    DocumentView oldView = getTopView();
    if(oldView != null){
      oldView.setActive(false);
    }
    topViewIdx = index;
    if(topViewIdx == -1) setTopView(null);
    else{
	    DocumentView newView = (DocumentView)horizontalViews.get(topViewIdx);
	    //hide if shown at the bottom
	    if(bottomViewIdx == topViewIdx){
	      setBottomView(null);
	      bottomViewIdx  = -1;
	    }
	    //activate if necessary
	    if(!newView.isActive()){
	      newView.setActive(true);
	    }
	    //show the new view
	    setTopView(newView);
    }
  }

  /**
   * Sets a new UI component in the top location. This method is intended to 
   * only be called from {@link #setTopView(int)}.
   * @param view the new view to be shown.
   */
  protected void setTopView(DocumentView view){
    topSplit.setTopComponent(view == null ? null : view.getGUI());
    topSplit.resetToPreferredSizes();
    updateBar(topBar);
    validate();
  }

  /**
   * Gets the currently showing central view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getCentralView(){
    if(centralViewIdx == -1) return null;
    else return(DocumentView)centralViews.get(centralViewIdx);
  }
  
  /**
   * Shows a new central view based on an index in the {@link #centralViews}
   * list.
   * @param index the index in {@link #centralViews} list for the new
   * view to be shown.
   */
  protected void setCentralView(int index){
    //deactivate current view
    DocumentView oldView = getCentralView();
    if(oldView != null){
      oldView.setActive(false);
    }
    centralViewIdx = index;
    if(centralViewIdx == -1) setCentralView(null);
    else{
	    DocumentView newView = (DocumentView)centralViews.get(centralViewIdx);
	    //activate if necessary
	    if(!newView.isActive()){
	      newView.setActive(true);
	    }
	    //show the new view
	    setCentralView(newView);
    }
  }

  /**
   * Sets a new UI component in the central location. This method is intended to 
   * only be called from {@link #setCentralView(int)}.
   * @param view the new view to be shown.
   */
  protected void setCentralView(DocumentView view){
    topSplit.setBottomComponent(view == null ? null : view.getGUI());
    topSplit.resetToPreferredSizes();
    updateBar(leftBar);
    validate();
  }
  
  
  /**
   * Gets the currently showing bottom view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getBottomView(){
    if(bottomViewIdx == -1) return null;
    else return(DocumentView)horizontalViews.get(bottomViewIdx);
  }
  
  /**
   * Shows a new bottom view based on an index in the {@link #horizontalViews}
   * list.
   * @param index the index in {@link #horizontalViews} list for the new
   * view to be shown.
   */
  protected void setBottomView(int index){
    //deactivate current view
    DocumentView oldView = getBottomView();
    if(oldView != null){
      oldView.setActive(false);
    }
    bottomViewIdx = index;
    if(bottomViewIdx == -1){
      setBottomView(null);
    }else{
	    DocumentView newView = (DocumentView)horizontalViews.get(bottomViewIdx);
	    //hide if shown at the top
	    if(topViewIdx == bottomViewIdx){
	      setTopView(null);
	      topViewIdx  = -1;
	    }
	    //activate if necessary
	    if(!newView.isActive()){
	      newView.setActive(true);
	    }
	    //show the new view
	    setBottomView(newView);
    }
  }

  /**
   * Sets a new UI component in the top location. This method is intended to 
   * only be called from {@link #setBottomView(int)}.
   * @param view the new view to be shown.
   */
  protected void setBottomView(DocumentView view){
    bottomSplit.setBottomComponent(view == null ? null : view.getGUI());
    bottomSplit.resetToPreferredSizes();
    updateBar(bottomBar);
    validate();
  }
  
  
  /**
   * Gets the currently showing right view
   * @return a {@link DocumentView} object.
   */
  protected DocumentView getRightView(){
    if(rightViewIdx == -1) return null;
    else return(DocumentView)verticalViews.get(rightViewIdx);
  }
  
  /**
   * Shows a new right view based on an index in the {@link #verticalViews}
   * list.
   * @param index the index in {@link #verticalViews} list for the new
   * view to be shown.
   */
  protected void setRightView(int index){
    //deactivate current view
    DocumentView oldView = getRightView();
    if(oldView != null){
      oldView.setActive(false);
    }
    rightViewIdx = index;
    if(rightViewIdx == -1) setRightView(null);
    else{
	    DocumentView newView = (DocumentView)verticalViews.get(rightViewIdx);
	    //activate if necessary
	    if(!newView.isActive()){
	      newView.setActive(true);
	    }
	    //show the new view
	    setRightView(newView);
    }
  }

  /**
   * Sets a new UI component in the right hand side location. This method is 
   * intended to only be called from {@link #setRightView(int)}.
   * @param view the new view to be shown.
   */
  protected void setRightView(DocumentView view){
    horizontalSplit.setRightComponent(view == null ? null : view.getGUI());
Component left = horizontalSplit.getLeftComponent();
Component right = horizontalSplit.getRightComponent();
System.out.println(left == null ? "null" : left.getPreferredSize().toString());
System.out.println(right == null ? "null" : right.getPreferredSize().toString());
    horizontalSplit.resetToPreferredSizes();
//		updateSplitLocation(horizontalSplit);
    
    updateBar(rightBar);
    validate();
  }  
  

  protected void updateSplitLocation(JSplitPane split){
    Component left = split.getLeftComponent();
    Component right = split.getRightComponent();
    if(left == null){
      split.setDividerLocation(0);
      return;
    }
    if(right == null){ 
      split.setDividerLocation(1);
      return;
    }
    Dimension leftPS = left.getPreferredSize();
    Dimension rightPS = right.getPreferredSize();
    double location = split.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? 
      (double)leftPS.width / (leftPS.width + rightPS.width) :
      (double)leftPS.height / (leftPS.height + rightPS.height);
    split.setDividerLocation(location);
  }
  
  /* (non-Javadoc)
   * @see gate.VisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    this.document = (Document)target;
  }

  /**
   * Updates the selected state of the buttons on one of the toolbars. 
   * @param toolbar
   */
  protected void updateBar(JToolBar toolbar){
    Component btns[] = toolbar.getComponents();
    if(btns != null){
      for(int i = 0; i < btns.length; i++){
        ((ViewButton)btns[i]).updateSelected();
      }
    }
  }

  protected class ViewButton extends JToggleButton{
    public ViewButton(DocumentView aView, String name){
      super();
      setSelected(false);
      setBorder(null);
      this.view = aView;
      if(aView.getType() == DocumentView.HORIZONTAL){
        setText(name);
      }else if(aView.getType() == DocumentView.CENTRAL){
        setIcon(new VerticalTextIcon(this, name, VerticalTextIcon.ROTATE_LEFT));
      }else if(aView.getType() == DocumentView.VERTICAL){
        setIcon(new VerticalTextIcon(this, name, 
                										 VerticalTextIcon.ROTATE_RIGHT));
      }
      
      addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          if(isSelected()){
            //show this new view
            switch(view.getType()){
              case DocumentView.CENTRAL:
                setCentralView(centralViews.indexOf(view));
                break;
              case DocumentView.VERTICAL:
                setRightView(verticalViews.indexOf(view));
                break;
              case DocumentView.HORIZONTAL:
                if(ViewButton.this.getParent() == topBar){
                  setTopView(horizontalViews.indexOf(view));
                }else{
                  setBottomView(horizontalViews.indexOf(view));
                }
                break;
            }
          }else{
            //hide this view
            switch(view.getType()){
              case DocumentView.CENTRAL:
                setCentralView(-1);
                break;
              case DocumentView.VERTICAL:
                setRightView(-1);
                break;
              case DocumentView.HORIZONTAL:
                if(ViewButton.this.getParent() == topBar){
                  setTopView(-1);
                }else{
                  setBottomView(-1);
                }
                break;
            }
          }
        }
      });
    }
    
    public void updateSelected(){
      switch(view.getType()){
        case DocumentView.CENTRAL:
          setSelected(getCentralView() == view);
          break;
        case DocumentView.VERTICAL:
          setSelected(getRightView() == view);
          break;
        case DocumentView.HORIZONTAL:
          if(ViewButton.this.getParent() == topBar){
            setSelected(getTopView() == view);
          }else{
            setSelected(getBottomView() == view);
          }
          break;
      }
    }
    DocumentView view;
  }

  protected JSplitPane horizontalSplit;
  protected JSplitPane topSplit;
  protected JSplitPane bottomSplit;

  protected JToolBar topBar;
  protected JToolBar rightBar;
  protected JToolBar leftBar;
  protected JToolBar bottomBar;

  protected Document document;


  /**
   * A list of {@link DocumentView} objects of type {@link DocumentView#CENTRAL}
   */
  protected List centralViews;
  
  /**
   * A list of {@link DocumentView} objects of type 
   * {@link DocumentView#VERTICAL}
   */
  protected List verticalViews;

  /**
   * A list of {@link DocumentView} objects of type 
   * {@link DocumentView#HORIZONTAL}
   */
  protected List horizontalViews;

  /**
   * The index in {@link #centralViews} of the currently active central view.
   * <code>-1</code> if none is active.
   */
  protected int centralViewIdx = -1;

  /**
   * The index in {@link #verticalViews} of the currently active right view.
   * <code>-1</code> if none is active.
   */
  protected int rightViewIdx = -1;
  
  /**
   * The index in {@link #horizontalViews} of the currently active top view.
   * <code>-1</code> if none is active.
   */
  protected int topViewIdx = -1;
  
  /**
   * The index in {@link #horizontalViews} of the currently active bottom view.
   * <code>-1</code> if none is active.
   */
  protected int bottomViewIdx = -1;

}