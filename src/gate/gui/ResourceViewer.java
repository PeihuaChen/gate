/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 23/01/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import gate.*;

import javax.swing.*;
import javax.swing.table.*;

import gate.creole.AbstractVisualResource;

public class ResourceViewer extends AbstractVisualResource {

  public ResourceViewer() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }


  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    table = new XJTable();

  }

  protected void initListeners(){
  }
  public void setResource(gate.Resource newResource) {
    resource = newResource;
  }
  public gate.Resource getResource() {
    return resource;
  }

  JTable table;
  private gate.Resource resource;

  class FeaturesTableModel extends DefaultTableModel{
    public int getColumnCount(){return 2;}

    public Class getColumnClass(int columnIndex){ return String.class;}

    public String getColumnName(int columnIndex){
      switch(columnIndex){
         case 0: return "Feature";
        case 1: return "Value";
        default: return "?";
      }
    }//public String getColumnName(int columnIndex)

  }///class FeaturesTableModel extends DefaultTableModel
}