/*
 *  RelationSetView.java
 *
 *  Copyright (c) 1995-2013, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Mark A. Greenwood, 15th December 2013
 */
package gate.gui.docview;

import gate.Document;
import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.gui.MainFrame;
import gate.relations.RelationSet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

@CreoleResource(name = "Relation Viewer", guiType = GuiType.LARGE, resourceDisplayed = "gate.Document")
public class RelationSetView extends AbstractVisualResource {

  private static final long serialVersionUID = 2976754146115707386L;

  private JTextPane text = new JTextPane();
  
  private Document doc = null;

  @Override
  public Resource init() {
    setLayout(new BorderLayout());

    text.setEditable(false);

    add(text, BorderLayout.CENTER);
    
    JButton btnRefresh = new JButton("Refresh",MainFrame.getIcon("Refresh"));
    btnRefresh.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent arg0) {
        refresh();
      }
    });
    
    JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    
    toolbar.add(btnRefresh);
    toolbar.addSeparator();
    toolbar.add(new JLabel("Currently this view is not automatically updated"));
    
    add(toolbar, BorderLayout.NORTH);

    return this;
  }
  
  private void refresh() {
    StringBuilder builder = new StringBuilder();
    
    RelationSet relations = doc.getAnnotations().getRelations();
    if(relations.size() > 0) {
      builder.append(relations).append("\n\n");
    }
    
    for(String name : doc.getAnnotationSetNames()) {
      relations = doc.getAnnotations(name).getRelations();
      if(relations.size() > 0) {
        builder.append(name).append(":\n").append(relations).append("\n\n");
      }
    }

    text.setText(builder.toString());
  }

  @Override
  public void setTarget(Object target) {    
    doc = (Document)target;
    refresh();
  }
}
