/*
 * Created on Mar 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package gate.gui.docview;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import gate.Document;

/**
 * @author valyt
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AnnotationSetsView extends AbstractDocumentView {

  /* (non-Javadoc)
   * @see gate.gui.docview.DocumentView#getGUI()
   */
  public Component getGUI() {
    return scroller;
  }
  /* (non-Javadoc)
   * @see gate.gui.docview.DocumentView#getType()
   */
  public int getType() {
    return VERTICAL;
  }
  
  protected void initGUI() {
    mainTable = new JTable();
    scroller = new JScrollPane(mainTable);
  }
    
	
  
  JTable mainTable;
  JScrollPane scroller;
  
  Document document;
  
}
