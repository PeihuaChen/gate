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

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.*;
import java.net.URL;
import java.io.IOException;
import java.text.*;

import gate.*;
import gate.util.*;
import gate.swing.*;
import gate.creole.*;

public class NewResourceDialog extends JDialog {

  public NewResourceDialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    initLocalData();
    initGuiComponents();
    initListeners();

  }// public NewResourceDialog(Frame frame, String title, boolean modal)

  protected void initLocalData(){
    listeners = new HashMap();
    if(getParent() instanceof gate.event.ProgressListener)
      listeners.put("gate.event.ProgressListener", getParent());
    if(getParent() instanceof gate.event.StatusListener)
      listeners.put("gate.event.StatusListener", getParent());
  }// protected void initLocalData()

  protected void initGuiComponents(){
    this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),
                                                  BoxLayout.Y_AXIS));

    //name field
    Box nameBox = Box.createHorizontalBox();
    nameBox.add(Box.createHorizontalStrut(5));
    nameBox.add(new JLabel("Name: "));
    nameBox.add(Box.createHorizontalStrut(5));
    nameField = new JTextField(30);
    nameField.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));
    nameField.setRequestFocusEnabled(true);
    nameField.setVerifyInputWhenFocusTarget(false);
    nameBox.add(nameField);
    nameBox.add(Box.createHorizontalStrut(5));
    nameBox.add(Box.createHorizontalGlue());
    this.getContentPane().add(nameBox);
    this.getContentPane().add(Box.createVerticalStrut(5));

    //parameters table
    parametersEditor = new ResourceParametersEditor();
    tableScroll = new JScrollPane(parametersEditor);
    this.getContentPane().add(tableScroll);
    this.getContentPane().add(Box.createVerticalStrut(5));
    this.getContentPane().add(Box.createVerticalGlue());

    //buttons box
    JPanel buttonsBox = new JPanel();
    buttonsBox.setLayout(new BoxLayout(buttonsBox, BoxLayout.X_AXIS));
    //buttonsBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonsBox.add(Box.createHorizontalStrut(10));
    buttonsBox.add(okBtn = new JButton("OK"));
    buttonsBox.add(Box.createHorizontalStrut(10));
    buttonsBox.add(cancelBtn = new JButton("Cancel"));
    buttonsBox.add(Box.createHorizontalStrut(10));
    this.getContentPane().add(buttonsBox);
    this.getContentPane().add(Box.createVerticalStrut(5));
    setSize(400, 300);
    nameField.setNextFocusableComponent(parametersEditor);
    parametersEditor.setNextFocusableComponent(okBtn);
    okBtn.setNextFocusableComponent(cancelBtn);
    cancelBtn.setNextFocusableComponent(nameField);
    getRootPane().setDefaultButton(okBtn);
  }// protected void initGuiComponents()


  protected void initListeners(){
    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        userCanceled = false;
//        String name = nameField.getText();
//        if(name == null || name.length() == 0){
//          JOptionPane.showMessageDialog(getOwner(),
//                                        "Please give a name for the new resource!\n",
//                                        "Gate", JOptionPane.ERROR_MESSAGE);
//        }else{
        hide();
//        }
      }//public void actionPerformed(ActionEvent e)
    });

    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        userCanceled = true;
        hide();
      }//public void actionPerformed(ActionEvent e)
    });
  }//protected void initListeners()

  JButton okBtn, cancelBtn;
  JTextField nameField;
  ResourceParametersEditor parametersEditor;
  JScrollPane tableScroll;
  ResourceData resourceData;
  Resource resource;

  boolean userCanceled;
  Map listeners;

  public synchronized void show(ResourceData rData) {
    this.resourceData = rData;
    setLocationRelativeTo(getParent());
    nameField.setText("");
    parametersEditor.init(null,
                          rData.getParameterList().getInitimeParameters());

    validate();
    pack();

    requestFocus();
    nameField.requestFocus();
    userCanceled = true;
    setModal(true);
    super.show();
    if(userCanceled) return;
    else{
      Runnable runnable = new Runnable(){
        public void run(){
          //create the new resource
          FeatureMap params = parametersEditor.getParameterValues();

          Resource res;
          gate.event.StatusListener sListener = (gate.event.StatusListener)
                                      listeners.get("gate.event.StatusListener");
          if(sListener != null) sListener.statusChanged("Loading " +
                                                        nameField.getText() +
                                                        "...");

          gate.event.ProgressListener pListener = (gate.event.ProgressListener)
                                      listeners.get("gate.event.ProgressListener");
          if(pListener != null){
            pListener.progressChanged(0);
          }

          try {
            long startTime = System.currentTimeMillis();
            FeatureMap features = Factory.newFeatureMap();
            String name = nameField.getText();
            if(name == null || name.length() == 0) name = null;
            res = Factory.createResource(resourceData.getClassName(), params,
                                         features, listeners,
                                         name);
            long endTime = System.currentTimeMillis();
            if(sListener != null) sListener.statusChanged(
                nameField.getText() + " loaded in " +
                NumberFormat.getInstance().format(
                (double)(endTime - startTime) / 1000) + " seconds");
            if(pListener != null) pListener.processFinished();
          } catch(ResourceInstantiationException rie){
            JOptionPane.showMessageDialog(getOwner(),
                                          "Resource could not be created!\n" +
                                          rie.toString(),
                                          "Gate", JOptionPane.ERROR_MESSAGE);
            rie.printStackTrace(Err.getPrintWriter());
            Exception innerEx = rie.getException();
            if(innerEx != null){
              Err.prln("From: ===>");
              innerEx.printStackTrace(Err.getPrintWriter());
            }
            res = null;
            if(sListener != null) sListener.statusChanged("Error loading " +
                                                          nameField.getText() +
                                                          "!");
            if(pListener != null) pListener.processFinished();
          }
        }//public void run()
      };
      Thread thread = new Thread(runnable, "");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }// public synchronized Resource show(ResourceData rData)

}//class NewResourceDialog