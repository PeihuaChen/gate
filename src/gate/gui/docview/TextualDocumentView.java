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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.*;
import javax.swing.text.Highlighter;


import gate.Annotation;
import gate.util.GateRuntimeException;


/**
 * This class provides a central view for a textual document.
 */

public class TextualDocumentView extends AbstractDocumentView {

  public TextualDocumentView(){
    hgTagForAnn = new HashMap();
  }
  
  public Object addHighlight(Annotation ann, Color colour){
    Highlighter highlighter = textView.getHighlighter();
    try{
	    return highlighter.addHighlight(
	            ann.getStartNode().getOffset().intValue(),
	            ann.getEndNode().getOffset().intValue(),
	            new DefaultHighlighter.DefaultHighlightPainter(colour));
    }catch(BadLocationException ble){
      //the offsets should always be OK as they come from an annotation
      throw new GateRuntimeException(ble.toString());
    }
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
    textView = new JEditorPane(){
//      public boolean getScrollableTracksViewportWidth(){
//        return true;
//      }
    };
    textView.setContentType("text/plain");
    textView.setEditorKit(new StyledEditorKit());
    textView.setAutoscrolls(false);
    scroller = new JScrollPane(textView);

    textView.setText(document.getContent().toString());
    scroller.getViewport().setViewPosition(new Point(0, 0));
    initListeners();

  }

  protected void initListeners(){
  }
  
  public void removeHighlight(Object tag){
    Highlighter highlighter = textView.getHighlighter();
    highlighter.removeHighlight(tag);
  }


  
  /**
   * Stores the highlighter tags for all the highlighted annotations;
   */
  protected Map hgTagForAnn; 
  protected JScrollPane scroller;


  protected JEditorPane textView;
}
