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
import java.awt.Component;
import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.*;
import javax.swing.text.Highlighter;


import gate.Annotation;
import gate.AnnotationSet;
import gate.util.GateRuntimeException;


/**
 * This class provides a central view for a textual document.
 */

public class TextualDocumentView extends AbstractDocumentView {

  public TextualDocumentView(){
    hgTagForAnn = new HashMap();
  }
  
  public Object addHighlight(Annotation ann, AnnotationSet set, Color colour){
    Highlighter highlighter = textView.getHighlighter();
    try{
      Object tag = highlighter.addHighlight(
	            ann.getStartNode().getOffset().intValue(),
	            ann.getEndNode().getOffset().intValue(),
	            new DefaultHighlighter.DefaultHighlightPainter(colour));
      annotationListView.addAnnotation(tag, ann, set);
      return tag;
    }catch(BadLocationException ble){
      //the offsets should always be OK as they come from an annotation
      throw new GateRuntimeException(ble.toString());
    }
  }

  public void removeHighlight(Object tag){
    Highlighter highlighter = textView.getHighlighter();
    highlighter.removeHighlight(tag);
    annotationListView.removeAnnotation(tag);
  }

  /**
   * Ads several highlights in one go. This method does not assume that it was 
   * called from the UI thread.
   * @param annotations
   * @param set
   * @param colour
   * @return
   */
  public List addHighlights(Collection annotations, 
          AnnotationSet set, Color colour){
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        scroller.getViewport().setView(new JLabel("Updating"));
      }
    });
    
    Highlighter highlighter = textView.getHighlighter();
    
    Iterator annIter = annotations.iterator();
    List tagsList = new ArrayList(annotations.size());
    while(annIter.hasNext()){
      Annotation ann = (Annotation)annIter.next();
      try{
        Object tag = highlighter.addHighlight(
                ann.getStartNode().getOffset().intValue(),
                ann.getEndNode().getOffset().intValue(),
                new DefaultHighlighter.DefaultHighlightPainter(colour));
        tagsList.add(tag);
      }catch(BadLocationException ble){
        //the offsets should always be OK as they come from an annotation
        throw new GateRuntimeException(ble.toString());
      }
    }
    annotationListView.addAnnotations(tagsList, annotations, set);
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        scroller.getViewport().setView(textView);
      }
    });
    return tagsList;
  }
  
  
  public void removeHighlights(Collection tags){
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        scroller.getViewport().setView(new JLabel("Updating"));
      }
    });
    
    Highlighter highlighter = textView.getHighlighter();
    
    Iterator tagIter = tags.iterator();
    while(tagIter.hasNext()){
      highlighter.removeHighlight(tagIter.next());
    }
    annotationListView.removeAnnotations(tags);
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        scroller.getViewport().setView(textView);
      }
    });
  }


  public Component getGUI() {
    return scroller;
  }

  public int getType() {
    return CENTRAL;
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#initGUI()
   */
  protected void initGUI() {
    textView = new JEditorPane();
    textView.setContentType("text/plain");
    textView.setEditorKit(new StyledEditorKit());
    textView.setAutoscrolls(false);
    scroller = new JScrollPane(textView);

    textView.setText(document.getContent().toString());
    scroller.getViewport().setViewPosition(new Point(0, 0));
    
    //get a pointer to the annotation list view used to display
    //the highlighted annotations 
    Iterator horizViewsIter = owner.getHorizontalViews().iterator();
    while(annotationListView == null && horizViewsIter.hasNext()){
      DocumentView aView = (DocumentView)horizViewsIter.next();
      if(aView instanceof AnnotationListView) 
        annotationListView = (AnnotationListView)aView;
    }

    initListeners();
  }

  protected void unregisterHooks(){}
  protected void registerHooks(){}
  
  protected void initListeners(){
  }
  
  
  /**
   * Stores the highlighter tags for all the highlighted annotations;
   */
  protected Map hgTagForAnn; 
  protected JScrollPane scroller;
  protected AnnotationListView annotationListView;


  protected JEditorPane textView;
}
