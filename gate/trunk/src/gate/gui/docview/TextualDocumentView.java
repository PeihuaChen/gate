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

import java.awt.Component;
import java.awt.Point;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import gate.Document;


/**
 * This class provides a central view for a textual document.
 */

public class TextualDocumentView extends AbstractDocumentView {

  public TextualDocumentView(){
    textView = new JEditorPane();
    scroller = new JScrollPane(textView);
    initListeners();
  }

  protected void initListeners(){
  }


  public Component getGUI() {
    return scroller;
  }

  public int getType() {
    return CENTRAL;
  }


  protected JEditorPane textView;
  protected JScrollPane scroller;
  protected Document document;


  /* (non-Javadoc)
   * @see gate.VisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    this.document = (Document)target;
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#initGUI()
   */
  protected void initGUI() {
    textView.setText(document.getContent().toString());
    scroller.getViewport().setViewPosition(new Point(0, 0));
////    textView.setSize(textView.getPreferredSize());
////    scroller.setSize(scroller.getPreferredSize());
//    try{
//      Rectangle rect = textView.modelToView(0);
//Out.prln(rect);      
//      textView.scrollRectToVisible(rect);
//    }catch(BadLocationException ble){
//      //ignore
//      ble.printStackTrace();
//    }
//    scroller.getViewport().setViewPosition(new Point(0,0));
//    scroller.getViewport().scrollRectToVisible(new Rectangle(0,0,1,1));
//      JComponent comp;
  }
}
