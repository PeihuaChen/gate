/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 02/10/2001
 *
 *  $Id$
 *
 */
package gate.gui;

import gate.*;
import gate.creole.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.tree.*;

/**
 * Renders a {@link Resource} for tables, trees and lists.
 * It will use the icon info from the creole register, the name of the resource
 * as the rendered string and the type of the resource as the tooltip.
 */
public class ResourceRenderer extends JLabel
                              implements ListCellRenderer, TableCellRenderer,
                                         TreeCellRenderer {

  public ResourceRenderer(){
    setOpaque(true);
  }

  public Component getListCellRendererComponent(JList list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus){
    prepareRenderer(list, value, isSelected, hasFocus());
    return this;
  }


  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column){
    prepareRenderer(table, value, isSelected, hasFocus);
    return this;
  }

  public Component getTreeCellRendererComponent(JTree tree,
                                                Object value,
                                                boolean selected,
                                                boolean expanded,
                                                boolean leaf,
                                                int row,
                                                boolean hasFocus){
    prepareRenderer(tree, value, selected, hasFocus);
    return this;
  }

  private void prepareRenderer(JComponent ownerComponent, Object value,
                               boolean isSelected, boolean hasFocus){

    if (isSelected) {
      if(ownerComponent instanceof JTable){
        setForeground(((JTable)ownerComponent).getSelectionForeground());
        setBackground(((JTable)ownerComponent).getSelectionBackground());
      }else if(ownerComponent instanceof JTree){
        setForeground(UIManager.getColor("Tree.selectionForeground"));
        setBackground(UIManager.getColor("Tree.selectionBackground"));
      }else if(ownerComponent instanceof JList){
        setForeground(((JList)ownerComponent).getSelectionForeground());
        setBackground(((JList)ownerComponent).getSelectionBackground());
      }
    }else{
      setForeground(ownerComponent.getForeground());
      setBackground(ownerComponent.getBackground());
    }

    setFont(ownerComponent.getFont());

    if (hasFocus) {
      setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
    }else{
      setBorder(noFocusBorder);
    }

    String text;
    String toolTipText;
    Icon icon;
    ResourceData rData = null;
    if(value instanceof Resource){
      text = ((Resource)value).getName();

      rData = (ResourceData)Gate.getCreoleRegister().
                                 get(value.getClass().getName());
    }else{
      text = value.toString();
    }
    if(rData != null){
      toolTipText = "<HTML>Type: <b>" + rData.getName() + "</b></HTML>";
      String iconName = rData.getIcon();
      if(iconName == null){
        if(value instanceof LanguageResource) iconName = "lr.gif";
        else if(value instanceof ProcessingResource) iconName = "pr.gif";
        else if(value instanceof Controller) iconName = "controller.gif";
      }
      icon = (iconName == null) ? null : MainFrame.getIcon(iconName);
    }else{
      icon = null;
    }

    setText(text);
    setIcon(icon);
    setToolTipText(value.getClass().getName());
  }

  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
}