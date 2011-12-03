/*
 * RemoteUpdateSite.java
 * 
 * Copyright (c) 2011, The University of Sheffield. See the file COPYRIGHT.txt
 * in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 * 
 * Mark A. Greenwood, 29/10/2011
 */

package gate.gui.creole.manager;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.thoughtworks.xstream.XStream;

public class RemoteUpdateSite {

  // TODO can we do this with URI instead so that we don't block on non-exist
  // URLs, or ones that are timing out etc.
  protected URI url;

  protected String name;
  
  protected Boolean enabled = false;

  protected transient Boolean valid = null;

  protected transient List<CreolePlugin> plugins = null;

  public RemoteUpdateSite(String name, URI url, boolean enabled) {
    this.name = name;
    this.url = url;
    this.enabled = enabled;
  }
  
  

  @SuppressWarnings("unchecked")
  public List<CreolePlugin> getCreolePlugins() throws IOException {
    if(plugins == null) {

      XStream xs = new XStream();
      xs.setClassLoader(RemoteUpdateSite.class.getClassLoader());
      xs.alias("UpdateSite", List.class);
      xs.alias("CreolePlugin", CreolePlugin.class);
      xs.useAttributeFor(CreolePlugin.class, "id");
      xs.useAttributeFor(CreolePlugin.class, "description");
      xs.useAttributeFor(CreolePlugin.class, "version");
      xs.useAttributeFor(CreolePlugin.class, "downloadURL");
      xs.useAttributeFor(CreolePlugin.class, "url");
      xs.useAttributeFor(CreolePlugin.class, "gateMin");
      xs.useAttributeFor(CreolePlugin.class, "gateMax");

      URLConnection conn = (new URL(url.toURL(), "site.xml")).openConnection();
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);

      plugins = (List<CreolePlugin>)xs.fromXML(conn.getInputStream());
    }

    for(CreolePlugin p : plugins) {
      p.reset();
    }

    return plugins;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    RemoteUpdateSite other = (RemoteUpdateSite)obj;
    if(name == null) {
      if(other.name != null) return false;
    } else if(!name.equals(other.name)) return false;
    if(url == null) {
      if(other.url != null) return false;
    } else if(!url.equals(other.url)) return false;
    return true;
  }
}
