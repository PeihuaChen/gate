/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 27/02/2002
 *
 *  $Id$
 *
 */
package gate.gui;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;

import gate.*;
import gate.util.*;
import gate.creole.*;


public class PRViewer extends AbstractVisualResource {

  public PRViewer() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    setLayout(new BorderLayout());
    editor = new ResourceParametersEditor();
    editor.setEditable(false);
    JScrollPane scroller = new JScrollPane(editor);
    scroller.setAlignmentX(Component.LEFT_ALIGNMENT);
    scroller.setAlignmentY(Component.TOP_ALIGNMENT);
    add(scroller, BorderLayout.CENTER);
  }

  protected void initListeners(){
  }

  public void setTarget(Object target){
    if(!(target instanceof ProcessingResource)){
      throw new GateRuntimeException(this.getClass().getName() +
                                     " can only be used to display " +
                                     ProcessingResource.class.getName() +
                                     "\n" + target.getClass().getName() +
                                     " is not a " +
                                     ProcessingResource.class.getName() + "!");
    }
    if(target != null){
      ProcessingResource pr = (ProcessingResource)target;
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                              get(pr.getClass().getName());
      if(rData != null){
        editor.init(null, rData.getParameterList().getInitimeParameters());
      }else{
        editor.init(null, null);
      }
    }else{
      editor.init(null, null);
    }
  }

  ResourceParametersEditor editor;
}