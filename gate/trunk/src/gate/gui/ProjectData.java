/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 22/01/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.io.*;
import java.util.*;

import javax.swing.tree.*;

import gate.util.*;


public class ProjectData {

  public ProjectData(File projectFile, MainFrame frame) {
    this.projectFile = projectFile;
    //this.projectFileName = projectFile.getAbsolutePath();
    this.frame = frame;
    appList = new ArrayList();
    dsList = new ArrayList();
    lrList = new ArrayList();
    prList = new ArrayList();
  }

  public String toString(){
    return projectFile.getName();
  }

  public void addApplication(ApplicationHandle newApp){
    appList.add(newApp);
  }
  public void remove(CustomResourceHandle handle){
    if(handle instanceof ApplicationHandle){
      appList.remove(handle);
//      frame.remove(handle);
    }else if(handle instanceof LRHandle){
      lrList.remove(handle);
//      frame.remove(handle);
    }else if(handle instanceof PRHandle){
      prList.remove(handle);
//      frame.remove(handle);
    }
  }

  List getApplicationsList(){
    return appList;
  }

  public void addLR(LRHandle newLR){
    lrList.add(newLR);
  }

  List getLRList(){
    List result = new ArrayList();
    result.addAll(Gate.getCreoleRegister().getLrInstances());
    return result;
  }

  public void addPR(PRHandle newPR){
    prList.add(newPR);
  }
  List getPRList(){
    List result = new ArrayList();
    result.addAll(Gate.getCreoleRegister().getPrInstances());
    return result;
  }

  transient File projectFile;
  String projectFileName;
  /**
   * list of ResourceHandler
   */
  List lrList;
  List prList;
  List appList;
  List dsList;
  MainFrame frame;
}