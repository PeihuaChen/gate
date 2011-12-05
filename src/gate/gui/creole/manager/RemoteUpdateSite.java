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

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.XStream;

public class RemoteUpdateSite {

  protected URI uri;

  protected String name;

  protected Boolean enabled = false;

  protected transient Boolean valid = null;

  protected transient List<CreolePlugin> plugins = null;

  public RemoteUpdateSite(String name, URI uri, boolean enabled) {
    this.name = name;
    this.uri = uri;
    this.enabled = enabled;
  }

  @SuppressWarnings("unchecked")
  public List<CreolePlugin> getCreolePlugins() {
    if(plugins == null) {
      valid = true;
      try {
        XStream xs = new XStream();
        xs.setClassLoader(RemoteUpdateSite.class.getClassLoader());
        xs.alias("UpdateSite", List.class);
        xs.alias("CreolePlugin", CreolePlugin.class);
        xs.useAttributeFor(CreolePlugin.class, "url");
        
        URLConnection conn =
                (new URL(uri.toURL(), "site.xml")).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        plugins = (List<CreolePlugin>)xs.fromXML(conn.getInputStream());
      } catch(Exception e) {
        valid = false;
        return Collections.EMPTY_LIST;
      }
    } else {
      for(CreolePlugin p : plugins) {
        p.reset();
      }
    }

    return plugins;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((uri == null) ? 0 : uri.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null) return false;
    if(getClass() != obj.getClass()) return false;
    RemoteUpdateSite other = (RemoteUpdateSite)obj;
    if(uri == null) {
      if(other.uri != null) return false;
    } else if(!uri.equals(other.uri)) return false;
    return true;
  }
}
