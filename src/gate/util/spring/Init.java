/*
 *  Init.java
 *
 *  Copyright (c) 1998-2006, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Ian Roberts, 07/Oct/2006
 *
 *  $Id$
 */

package gate.util.spring;

import gate.Gate;
import org.springframework.core.io.Resource;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

/**
 * Helper class to support GATE initialisation via
 * <a href="http://www.springframework.org">Spring</a>.  The following is a
 * typical XML fragment to initialise GATE.
 *
 * <pre>
 * &lt;bean id="init-gate" class="gate.util.spring.Init"
 *      singleton="true" init-method="init"&gt;
 *   &lt;property name="gateHome" value="path/to/GATE" /&gt;
 *   &lt;property name="siteConfigFile" value="site/gate.xml" /&gt;
 *   &lt;property name="userConfigFile" value="user/gate.xml" /&gt;
 *   &lt;property name="preloadPlugins"&gt;
 *     &lt;list&gt;
 *       &lt;value type="org.springframework.core.io.Resource"&gt;plugins/ANNIE&lt;/value&gt;
 *       &lt;value&gt;http://plugins.org/another/plugin&lt;/value&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * Valid properties are: <code>gateHome</code>, <code>pluginsHome</code>,
 * <code>siteConfigFile</code>, <code>userConfigFile</code>,
 * <code>builtinCreoleDir</code> - Spring <code>Resource</code>s corresponding
 * to the equivalent static set methods of {@link gate.Gate}.
 *
 * Also, <code>preloadPlugins</code>, a List containing URLs, Spring Resources
 * or Files, giving plugins that will be loaded after GATE has been
 * initialised.
 *
 * <b>Note that the init-method="init" in the above definition is vital.  GATE
 * will not work if it is omitted.</b>
 */
public class Init {

  /**
   * An optional list of plugins to load after GATE initialisation.
   */
  private List plugins;
  
  public void setGateHome(Resource gateHome) throws IOException {
    Gate.setGateHome(gateHome.getFile());
  }

  public void setPluginsHome(Resource pluginsHome) throws IOException {
    Gate.setPluginsHome(pluginsHome.getFile());
  }

  public void setSiteConfigFile(Resource siteConfigFile) throws IOException {
    Gate.setSiteConfigFile(siteConfigFile.getFile());
  }

  public void setUserConfigFile(Resource userConfigFile) throws IOException {
    Gate.setUserConfigFile(userConfigFile.getFile());
  }

  public void setBuiltinCreoleDir(Resource builtinCreoleDir) throws IOException {
    Gate.setBuiltinCreoleDir(builtinCreoleDir.getURL());
  }

  public void setPreloadPlugins(List plugins) {
    this.plugins = plugins;
  }

  /**
   * Initialises GATE and loads any preloadPlugins that have been specified.
   */
  public void init() throws Exception {
    Gate.init();
    if(plugins != null && !plugins.isEmpty()) {
      Iterator pluginsIt = plugins.iterator();
      while(pluginsIt.hasNext()) {
        Object plugin = pluginsIt.next();
        URL pluginURL;
        if(plugin instanceof URL) {
          pluginURL = (URL)plugin;
        }
        else if(plugin instanceof Resource) {
          pluginURL = ((Resource)plugin).getURL();
        }
        else if(plugin instanceof String) {
          pluginURL = new URL((String)plugin);
        }
        else if(plugin instanceof File) {
          pluginURL = ((File)plugin).toURL();
        }
        else {
          throw new IllegalArgumentException(
              "Found a " + plugin.getClass().getName() + " in preloadPlugins, "
              + "but it must contain only "
              + "URL, org.springframework.core.io.Resource, File or String");
        }
        Gate.getCreoleRegister().registerDirectories(pluginURL);
      }
    }
  }
}
