/*
 * UnusedPluginUnloader.java
 * 
 * Copyright (c) 1995-2014, The University of Sheffield. See the file
 * COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 * 
 * Johann Petrak 2014-05-22
 */
package gate.creole;

import gate.CreoleRegister;
import gate.Gate;
import gate.Resource;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleResource;
import gate.gui.ActionsPublisher;
import gate.resources.img.svg.Log4JALLIcon;
import gate.util.GateException;
import gate.util.GateRuntimeException;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A tool option that will try its best to unload just the plugins for which 
 * we do not have any known instances.
 * 
 * 
 * @author Johann Petrak
 */
@SuppressWarnings("serial")
@CreoleResource(
        tool = true, 
        isPrivate = true, 
        autoinstances = @AutoInstance, 
        name = "Unload unused plugins", 
        helpURL = "http://gate.ac.uk/userguide/sec:misc-creole:dev-tools", 
        comment = "Unloads all plugins for which we cannot find any loaded instances")
public class UnusedPluginUnloader extends AbstractResource implements ActionsPublisher {
  // the cached set of actions so we don't have to keep creating them
  private List<Action> actions;

  @Override
  public List<Action> getActions() {

    // if we have already built the action list then just return it
    if(actions != null) return actions;

    // create the empty actions list
    actions = new ArrayList<Action>();

    actions.add(new AbstractAction("Unload unused plugins") {

      @Override
      public void actionPerformed(ActionEvent e) {

        CreoleRegister reg = Gate.getCreoleRegister();
        List<Resource> allInstances;
        try {
          allInstances = reg.getAllInstances("gate.Resource");
        } catch (GateException ex) {
          System.err.println("Could not obtain the resource instances!");
          ex.printStackTrace(System.err);
          return;
        }
        Set<URL> allPlugins = new HashSet<URL>();
        for (ResourceData rd : reg.values()) {
          String uriString = rd.getXmlFileUrl().toString();
          if (uriString.startsWith("file:")) {
            uriString = uriString.replaceAll("creole.xml$", "");
            try {
              allPlugins.add(new URL(uriString));
            } catch (MalformedURLException ex) {
              // ignore this, in the worst case we won't unload this plugin ...
            }
          }
        }
        Set<URL> usedPlugins = new HashSet<URL>();
        for (Resource res : allInstances) {
          String clazz = res.getClass().getName();
          ResourceData rd = reg.get(clazz);
          if (rd == null) {
            // ignore ...
            //System.out.println("ODD: no resource data found for class " + clazz);            
          } else {
            String uriString = rd.getXmlFileUrl().toString();
            if (uriString.startsWith("file:")) {
              uriString = uriString.replaceAll("creole.xml$", "");
              try {
                usedPlugins.add(new URL(uriString));
              } catch (MalformedURLException ex) {
                // ignore: we may unload a plugin that is used, but this should never happen ...
              }
            }
          }
        }
        List<URL> pluginsToUnload = new ArrayList<URL>();
        for (URL plugin : allPlugins) {
          if (!usedPlugins.contains(plugin)) {
            pluginsToUnload.add(plugin);
          }
        }
        int n = 0;
        for (URL plugin : pluginsToUnload) {
          // The system logs plugins getting unloaded, so we do not have to do it
          // System.out.println("Unloading plugin: " + plugin);
          reg.removeDirectory(plugin);
          n++;
        }
        if(n==0) {
          System.out.println("No plugin unloaded");
        } else {
          System.out.println("Plugins unloaded: "+n);
        }
        System.out.println("\nPlugins still loaded:");
        for(URL plugin : usedPlugins) {
          System.out.println("  "+plugin);
        }
      }
    });

    //return the list of actions
    return actions;
  }
  
}
