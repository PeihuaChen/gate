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

import gate.Document;
import gate.creole.AbstractResource;
import gate.gui.Handle;
import gate.util.Out;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;


/**
 * This class provides a central view for a textual document.
 */

public class TextualDocumentView extends AbstractResource implements DocumentView{

  public TextualDocumentView(){
    textView = new JEditorPane();
    scroller = new JScrollPane(textView);
    initListeners();
  }

  protected void initListeners(){
  }

  public void setDocument(Document doc) {
    this.document = doc;
  }

  public Component getGUI() {
    return scroller;
  }

  public int getType() {
    return CENTRAL;
  }

  public List getActions() {
    return new ArrayList();
  }


  protected JEditorPane textView;
  protected JScrollPane scroller;
  protected Document document;


  /* (non-Javadoc)
   * @see gate.VisualResource#setHandle(gate.gui.Handle)
   */
  public void setHandle(Handle handle) {
    // TODO Auto-generated method stub
  }
  /* (non-Javadoc)
   * @see gate.VisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    this.document = (Document)target;
    textView.setText(document.getContent().toString());
  }
}
