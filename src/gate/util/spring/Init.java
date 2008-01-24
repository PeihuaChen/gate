/*
 *  Init.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
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
 * <p>Helper class to support GATE initialisation via
 * <a href="http://www.springframework.org">Spring</a>.  The following is a
 * typical XML fragment to initialise GATE.</p>
 *
 * <pre>
 * &lt;beans xmlns="http://www.springframework.org/schema/beans"
 *        xmlns:gate="http://gate.ac.uk/ns/spring"
 *        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *        xsi:schemaLocation="
 *          http://www.springframework.org/schema/beans
 *          http://www.springframework.org/schema/beans/spring-beans.xsd
 *          http://gate.ac.uk/ns/spring
 *          http://gate.ac.uk/ns/spring.xsd"&gt;
 *
 *   &lt;gate:init gate-home="path/to/GATE"
 *              site-config-file="site/gate.xml"
 *              user-config-file="user/gate.xml"&gt;
 *     &lt;gate:preload-plugins&gt;
 *       &lt;value&gt;plugins/ANNIE&lt;/value&gt;
 *       &lt;value&gt;http://plugins.org/another/plugin&lt;/value&gt;
 *     &lt;/gate:preload-plugins&gt;
 *   &lt;/gate:init&gt;
 * </pre>
 *
 * <p>Valid attributes are <code>gate-home</code>, <code>plugins-home</code>,
 * <code>site-config-file</code>, <code>user-config-file</code> and
 * <code>builtin-creole-dir</code> - Spring <code>Resource</code>s
 * corresponding to the equivalent static set methods of {@link gate.Gate}.
 * Also, <code>preload-plugins</code> is a list of <code>Resource</code>s that
 * will be loaded as GATE plugins after GATE is initialised.</p>
 *
 * <p>The equivalent definition in "normal" Spring form (without the
 * <code>gate:</code> namespace) would be:</p>
 * <pre>
 * &lt;bean class="gate.util.spring.Init"
 *      init-method="init"&gt;
 *   &lt;property name="gateHome" value="path/to/GATE" /&gt;
 *   &lt;property name="siteConfigFile" value="site/gate.xml" /&gt;
 *   &lt;property name="userConfigFile" value="user/gate.xml" /&gt;
 *   &lt;property name="preloadPlugins"&gt;
 *     &lt;list&gt;
 *       &lt;value&gt;plugins/ANNIE&lt;/value&gt;
 *       &lt;value&gt;http://plugins.org/another/plugin&lt;/value&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <b>Note that when using this form the init-method="init" in the above
 * definition is vital.  GATE will not work if it is omitted.</b>
 */
public class Init {

  /**
   * An optional list of plugins to load after GATE initialisation.
   */
  private List<Resource> plugins;
  
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

  public void setPreloadPlugins(List<Resource> plugins) {
    this.plugins = plugins;
  }

  /**
   * Initialises GATE and loads any preloadPlugins that have been specified.
   */
  public void init() throws Exception {
    Gate.init();
    if(plugins != null && !plugins.isEmpty()) {
      for(Resource plugin : plugins) {
        File pluginFile = null;
        try {
          pluginFile = plugin.getFile();
        }
        catch(IOException e) {
          // no problem, try just as URL
        }

        if(pluginFile == null) {
          Gate.getCreoleRegister().registerDirectories(plugin.getURL());
        }
        else {
          Gate.getCreoleRegister().registerDirectories(pluginFile.toURI().toURL());
        }
      }
    }
  }
}
