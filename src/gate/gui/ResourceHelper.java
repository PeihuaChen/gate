/*
 * ResourceHelper.java
 * 
 * Copyright (c) 2013, The University of Sheffield. See the file COPYRIGHT.txt
 * in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 * 
 * Mark A. Greenwood, 01/08/2013
 */

package gate.gui;

import gate.Gate;
import gate.Resource;
import gate.creole.AbstractResource;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

public abstract class ResourceHelper extends AbstractResource implements
                                                             CreoleListener {

  private static final long serialVersionUID = 1657147709821774423L;

  // We cache the actions so we don't keep recreating them every time. If you
  // don't want this behaviour (i.e. you want completely dynamic menus) then
  // you'll need to override the getActions method
  Map<Object, List<Action>> actions = new HashMap<Object, List<Action>>();

  @Override
  public Resource init() {
    // we need to listen for unload events so we register ourselves with the
    // creole register
    Gate.getCreoleRegister().addCreoleListener(this);

    // nothing else to do so just return ourself
    return this;
  }

  /**
   * Get the right-click menu items that this tool will add to the specified
   * resource. Note that this implementation uses a cache so that the same items
   * will be returned each time the menu is displayed. For a fully dynamic
   * approach a subclass should override this method to simply call @{link
   * {@link #buildActions(NameBearerHandle)}.
   * 
   * @param handle
   *          the {@link gate.gui.NameBearerHandle} instance we are wanting to add new menu
   *          items to
   * @return a list of {@link javax.swing.Action} instances which will be added
   *         to the right click menu of the specified handle
   */
  public List<Action> getActions(NameBearerHandle handle) {

    if(!actions.containsKey(handle.getTarget())) {
      // if we haven't seen this resource before then build the actions
      actions.put(handle.getTarget(), buildActions(handle));
    }

    // return the actions from the cache
    return actions.get(handle.getTarget());
  }

  /**
   * Build the {@link javax.swing.Action} instances that should be used to
   * enhance the right-click menu of the specified {@link gate.gui.NameBearerHandle}.
   * 
   * @param resource
   *          the {@link gate.gui.NameBearerHandle} instance we are adding to
   * @return a list of {@link javax.swing.Action} instances that will be added
   *         to the right-click menu of the resource.
   */
  protected abstract List<Action> buildActions(NameBearerHandle handle);

  @Override
  public void cleanup() {
    // do the normal cleanup
    super.cleanup();

    // stop listening for creole events so that we can get fully unloaded
    Gate.getCreoleRegister().removeCreoleListener(this);
  }

  @Override
  public void resourceUnloaded(CreoleEvent e) {
    // remove any cached menus for the resource that is being unloaded to help
    // with memory consumption etc.
    actions.remove(e.getResource());
  }

  @Override
  public void resourceLoaded(CreoleEvent e) {
    // we don't care about this event
  }

  @Override
  public void datastoreOpened(CreoleEvent e) {
    // we don't care about this event
  }

  @Override
  public void datastoreCreated(CreoleEvent e) {
    // we don't care about this event
  }

  @Override
  public void datastoreClosed(CreoleEvent e) {
    // we don't care about this event
  }

  @Override
  public void resourceRenamed(Resource resource, String oldName, String newName) {
    // we don't care about this event
  }
}
