/*
 * Created on Mar 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package gate.gui.docview;

import gate.Document;
import gate.creole.AbstractResource;
import gate.gui.Handle;

import java.awt.Component;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * @author valyt
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AnnotationSetsView extends AbstractResource 
															  implements DocumentView {
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
  /* (non-Javadoc)
   * @see gate.gui.ActionsPublisher#getActions()
   */
  public List getActions() {
    return null;
  }
  /* (non-Javadoc)
   * @see gate.VisualResource#setHandle(gate.gui.Handle)
   */
  public void setHandle(Handle handle) {
  }
  
  /* (non-Javadoc)
   * @see gate.VisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    // TODO Auto-generated method stub
  }
  
  protected void initGUI() {
    mainTable = new JTable();
    scroller = new JScrollPane(mainTable);
  }
    
	
  
  JTable mainTable;
  JScrollPane scroller;
  
  Document document;
  
}
