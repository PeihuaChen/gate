/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
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
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.*;


import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.corpora.DocumentContentImpl;
import gate.event.DocumentEvent;
import gate.event.DocumentListener;
import gate.util.*;


/**
 * This class provides a central view for a textual document.
 */

public class TextualDocumentView extends AbstractDocumentView {

  public TextualDocumentView(){
    blinkingTagsForAnnotations = new HashMap();
    gateDocListener = new GateDocumentListener();
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

  
  public void scrollAnnotationToVisible(Annotation ann){
    //if at least part of the blinking section is visible then we
    //need to do no scrolling
    //this is required for long annotations that span more than a 
    //screen
    Rectangle visibleView = scroller.getViewport().getViewRect();
    int viewStart = textView.viewToModel(visibleView.getLocation());
    Point endPoint = new Point(visibleView.getLocation());
    endPoint.translate(visibleView.width, visibleView.height);
    int viewEnd = textView.viewToModel(endPoint);
    int annStart = ann.getStartNode().getOffset().intValue();
    int annEnd = ann.getEndNode().getOffset().intValue();
    if(annEnd < viewStart || viewEnd < annStart){
      try{
        textView.scrollRectToVisible(textView.modelToView(annStart));
      }catch(BadLocationException ble){
        //this should never happen
        throw new GateRuntimeException(ble);
      }
    }
  }
  
  public void removeHighlight(Object tag){
    Highlighter highlighter = textView.getHighlighter();
    highlighter.removeHighlight(tag);
    annotationListView.removeAnnotation(tag);
  }

  /**
   * Gives access to the highliter's change highlight operation. Can be used to 
   * change the offset of an existing highlight.
   * @param tag the tag for the highlight
   * @param newStart new start offset.
   * @param newEnd new end offset.
   * @throws BadLocationException
   */
  public void moveHighlight(Object tag, int newStart, int newEnd) 
    throws BadLocationException{
    textView.getHighlighter().changeHighlight(tag, newStart, newEnd);
  }
  
  /**
   * Ads several highlights in one go. 
   * This method should <b>not</b> be called from within the UI thread.
   * @param annotations the collection of annotations for which highlights 
   * are to be added.
   * @param set the annotation set all the annotations belong to.
   * @param colour the colour for the highlights.
   * @return the list of tags for the added highlights. The order of the 
   * elements corresponds to the order defined by the iterator of the 
   * collection of annotations provided. 
   */
  public List addHighlights(Collection annotations, 
          AnnotationSet set, Color colour){
    //hide the text pane to speed up rendering.
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        textView.setVisible(false);
        scroller.getViewport().setView(new JLabel("Updating"));
      }
    });
    //wait for the textual view to be hidden
    while(textView.isVisible()) 
      try{
        Thread.sleep(30);
      }catch(InterruptedException ie){
        //ignore
      }
    
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
        textView.setVisible(true);
      }
    });
    return tagsList;
  }
  
  /**
   * Removes several highlights in one go. 
   * This method should <b>not</b> be called from within the UI thread.
   * @param tags the tags for the highlights to be removed
   */
  public void removeHighlights(Collection tags){
    //hide the text pane to speed up rendering.
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        textView.setVisible(false);
        scroller.getViewport().setView(new JLabel("Updating"));
      }
    });
    //wait for the textual view to be hidden.
    while(textView.isVisible()) 
      try{
        Thread.sleep(30);
      }catch(InterruptedException ie){
        //ignore
      }
    
    Highlighter highlighter = textView.getHighlighter();
    
    Iterator tagIter = tags.iterator();
    while(tagIter.hasNext()){
      highlighter.removeHighlight(tagIter.next());
    }
    annotationListView.removeAnnotations(tags);
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        scroller.getViewport().setView(textView);
        textView.setVisible(true);
      }
    });
  }

  
  public void addBlinkingHighlight(Annotation ann){
    synchronized(blinkingTagsForAnnotations){
      blinkingTagsForAnnotations.put(ann.getId(), new AnnotationTag(ann, null));
    }
  }
  
  public void removeBlinkingHighlight(Annotation ann){
    synchronized(blinkingTagsForAnnotations){
      AnnotationTag annTag = (AnnotationTag)blinkingTagsForAnnotations.
          remove(ann.getId()); 
      if(annTag != null && annTag.getTag() != null)
          textView.getHighlighter().removeHighlight(annTag.getTag());
    }
  }
  
  
  public void removeAllBlinkingHighlights(){
    synchronized(blinkingTagsForAnnotations){
      Iterator annIdIter = new ArrayList(blinkingTagsForAnnotations.keySet()).
        iterator();
      while(annIdIter.hasNext()){
        AnnotationTag annTag = (AnnotationTag)
            blinkingTagsForAnnotations.remove(annIdIter.next());
        Annotation ann = annTag.getAnnotation();
        Object tag = annTag.getTag();
        if(tag != null){
          Highlighter highlighter = textView.getHighlighter();
          highlighter.removeHighlight(tag);
        }
      }
    }
  }
  
  
  public int getType() {
    return CENTRAL;
  }

  /**
   * Stores the target (which should always be a {@link Document}) into the 
   * {@link #document} field.
   */
  public void setTarget(Object target) {
    if(document != null){
      //remove the old listener
      document.removeDocumentListener(gateDocListener);
    }
    super.setTarget(target);
    //register the new listener
    this.document.addDocumentListener(gateDocListener);
  }

  
  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#initGUI()
   */
  protected void initGUI() {
    textView = new JEditorPane();
    textView.setContentType("text/plain");
    textView.setEditorKit(new RawEditorKit());
    textView.setAutoscrolls(false);
    scroller = new JScrollPane(textView);

    textView.setText(document.getContent().toString());
    textView.getDocument().addDocumentListener(new SwingDocumentListener());
    scroller.getViewport().setViewPosition(new Point(0, 0));
    
    //get a pointer to the annotation list view used to display
    //the highlighted annotations 
    Iterator horizViewsIter = owner.getHorizontalViews().iterator();
    while(annotationListView == null && horizViewsIter.hasNext()){
      DocumentView aView = (DocumentView)horizViewsIter.next();
      if(aView instanceof AnnotationListView) 
        annotationListView = (AnnotationListView)aView;
    }
    blinker = new Timer(BLINK_DELAY, new BlinkAction());
    blinker.setRepeats(true);
    blinker.start();
    initListeners();
  }
  
  public Component getGUI(){
    return scroller;
  }
  
  protected void initListeners(){
    textView.addComponentListener(new ComponentAdapter(){
      public void componentResized(ComponentEvent e){
        try{
    	    scroller.getViewport().setViewPosition(
    	            textView.modelToView(0).getLocation());
    	    scroller.paintImmediately(textView.getBounds());
        }catch(BadLocationException ble){
          //ignore
        }
      }
    });
  }
  protected void unregisterHooks(){}
  protected void registerHooks(){}
  
  
  /**
   * Blinks the blinking highlights if any.
   */
  protected class BlinkAction extends AbstractAction{
    public void actionPerformed(ActionEvent evt){
      //this needs to either add or remove the highlights
      synchronized(blinkingTagsForAnnotations){
        //get out as quickly as possible if nothing to do
        if(blinkingTagsForAnnotations.isEmpty()) return;
        Iterator annIdIter = new ArrayList(blinkingTagsForAnnotations.keySet()).
          iterator();
        Highlighter highlighter = textView.getHighlighter();
        if(highlightsShown){
          //hide current highlights
          while(annIdIter.hasNext()){
            AnnotationTag annTag = (AnnotationTag)
              blinkingTagsForAnnotations.get(annIdIter.next());
            Annotation ann = annTag.getAnnotation();
            Object tag = annTag.getTag();
            if(tag != null) highlighter.removeHighlight(tag);
            annTag.setTag(null);
          }
          highlightsShown = false;
        }else{
          //show highlights
          while(annIdIter.hasNext()){
            AnnotationTag annTag = (AnnotationTag)
              blinkingTagsForAnnotations.get(annIdIter.next());
            Annotation ann = annTag.getAnnotation();
            try{
              Object tag = highlighter.addHighlight(
                      ann.getStartNode().getOffset().intValue(),
                      ann.getEndNode().getOffset().intValue(),
                      new DefaultHighlighter.DefaultHighlightPainter(
                              textView.getSelectionColor()));
              annTag.setTag(tag);
//              scrollAnnotationToVisible(ann);
            }catch(BadLocationException ble){
              //this should never happen
              throw new GateRuntimeException(ble);
            }
          }
          highlightsShown = true;
        }
      }
    }
    protected boolean highlightsShown = false;
  }
    
  class GateDocumentListener implements DocumentListener{

    public void annotationSetAdded(DocumentEvent e) {
    }

    public void annotationSetRemoved(DocumentEvent e) {
    }

    public void contentEdited(DocumentEvent e) {
      if(active){
        //reload the content.
        textView.setText(document.getContent().toString());
      }
    }
    
    public void setActive(boolean active){
      this.active = active;
    }
    private boolean active = true;
  }
  
  class SwingDocumentListener implements javax.swing.event.DocumentListener{
    public void insertUpdate(final javax.swing.event.DocumentEvent e) {
      //propagate the edit to the document
      try{
        //deactivate our own listener so we don't get cycles
        gateDocListener.setActive(false);
        document.edit(new Long(e.getOffset()), new Long(e.getOffset()),
                      new DocumentContentImpl(
                        e.getDocument().getText(e.getOffset(), e.getLength())));
      }catch(BadLocationException ble){
        ble.printStackTrace(Err.getPrintWriter());
      }catch(InvalidOffsetException ioe){
        ioe.printStackTrace(Err.getPrintWriter());
      }finally{
        //reactivate our listener
        gateDocListener.setActive(true);
      }
      //update the offsets in the list
      Component listView = annotationListView.getGUI();
      if(listView != null) listView.repaint();
    }

    public void removeUpdate(final javax.swing.event.DocumentEvent e) {
      //propagate the edit to the document
      try{
        //deactivate our own listener so we don't get cycles
        gateDocListener.setActive(false);        
        document.edit(new Long(e.getOffset()),
                      new Long(e.getOffset() + e.getLength()),
                      new DocumentContentImpl(""));
      }catch(InvalidOffsetException ioe){
        ioe.printStackTrace(Err.getPrintWriter());
      }finally{
        //reactivate our listener
        gateDocListener.setActive(true);
      }
      //update the offsets in the list
      Component listView = annotationListView.getGUI();
      if(listView != null) listView.repaint();
    }

    public void changedUpdate(javax.swing.event.DocumentEvent e) {
      //some attributes changed: we don't care about that
    }
  }//class SwingDocumentListener implements javax.swing.event.DocumentListener

  /**
   * A structure that holds an annotation and its tag.
   */
  class AnnotationTag{
    
    /**
     * @param tag The tag to set.
     */
    public void setTag(Object tag){
      this.tag = tag;
    }
    public AnnotationTag(Annotation annotation, Object tag){
      this.annotation = annotation;
      this.tag = tag;
    }
    
    /**
     * @return Returns the annotation.
     */
    public Annotation getAnnotation(){
      return annotation;
    }
    /**
     * @return Returns the tag.
     */
    public Object getTag(){
      return tag;
    }
    Annotation annotation;
    Object tag;
  }
  
  protected JScrollPane scroller;
  protected AnnotationListView annotationListView;
  
  protected GateDocumentListener gateDocListener;

  /**
   * The annotations used for blinking highlights and their tags. A map from 
   * {@link Annotation} ID to tag(i.e. {@link Object}).
   */
  protected Map blinkingTagsForAnnotations;
  
  protected Timer blinker;
  
  protected JEditorPane textView;
  
  /**
   * The delay used by the blinker.
   */
  protected final static int BLINK_DELAY = 400;
}
