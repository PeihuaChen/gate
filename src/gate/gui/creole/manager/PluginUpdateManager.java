/*
 * PluginUpdateManager.java
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

import gate.Gate;
import gate.gui.MainFrame;
import gate.resources.img.svg.AddIcon;
import gate.resources.img.svg.AdvancedIcon;
import gate.resources.img.svg.AvailableIcon;
import gate.resources.img.svg.DownloadIcon;
import gate.resources.img.svg.EditIcon;
import gate.resources.img.svg.GATEIcon;
import gate.resources.img.svg.GATEUpdateSiteIcon;
import gate.resources.img.svg.InvalidIcon;
import gate.resources.img.svg.OpenFileIcon;
import gate.resources.img.svg.RemoveIcon;
import gate.resources.img.svg.UpdateSiteIcon;
import gate.resources.img.svg.UpdatesIcon;
import gate.resources.img.svg.UserPluginIcon;
import gate.swing.CheckBoxTableCellRenderer;
import gate.swing.SpringUtilities;
import gate.swing.XJFileChooser;
import gate.swing.XJTable;
import gate.util.Files;
import gate.util.OptionsMap;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Expand;

/**
 * The CREOLE plugin manager which includes the ability to download and
 * install/update plugins from remote update sites.
 * 
 * @author Mark A. Greenwood
 */
@SuppressWarnings("serial")
public class PluginUpdateManager extends JDialog {
  private PluginTableModel availableModel = new PluginTableModel(3);

  private PluginTableModel updatesModel = new PluginTableModel(4);

  private UpdateSiteModel sitesModel = new UpdateSiteModel();

  private AvailablePlugins installed = new AvailablePlugins();

  private ProgressPanel progressPanel = new ProgressPanel();

  private JPanel panel = new JPanel(new BorderLayout());

  private JTabbedPane tabs = new JTabbedPane();

  private static File userPluginDir;

  private JFrame owner;

  private List<RemoteUpdateSite> updateSites =
          new ArrayList<RemoteUpdateSite>();

  private static final String GATE_USER_PLUGINS = "gate.user.plugins";

  private static final String GATE_UPDATE_SITES = "gate.update.sites";

  private static final String SUPPRESS_USER_PLUGINS = "suppress.user.plugins";

  private static final String SUPPRESS_UPDATE_INSTALLED =
          "suppress.update.install";

  public static File getUserPluginsHome() {
    // TODO move this into gate.util.OptionaMap as a getFile() method
    if(userPluginDir == null) {
      String upd = Gate.getUserConfig().getString(GATE_USER_PLUGINS);
      if(upd != null) {
        userPluginDir = new File(upd);
      }
    }
    return userPluginDir;
  }

  /**
   * Responsible for pushing some of the config date for the plugin manager into
   * the main user config. Note that this doesn't actually persist the data,
   * that is only done on a clean exit of the GUI by code hidden somewhere else.
   */
  private void saveConfig() {
    Map<String, String> sites = new HashMap<String, String>();
    for(RemoteUpdateSite rus : updateSites) {
      sites.put(rus.uri.toString(), (rus.enabled ? "1" : "0") + rus.name);
    }
    OptionsMap userConfig = Gate.getUserConfig();
    userConfig.put(GATE_UPDATE_SITES, sites);
    userConfig.put(GATE_USER_PLUGINS, userPluginDir.getAbsolutePath());
  }

  /**
   * Load all the data about available plugins/updates from the remote update
   * sites as well as checking what is installed in the user plugin directory
   */
  private void loadData() {
    // display the progress panel to stop user input and show progress
    progressPanel.messageChanged("Loading CREOLE Plugin Information...");
    progressPanel.rangeChanged(0, 0);

    if(getUserPluginsHome() == null) {
      // if the user plugin directory is not set then there is no point trying
      // to load any of the data, just disable the update/install tabs
      tabs.setEnabledAt(1, false);
      tabs.setEnabledAt(2, false);
      showProgressPanel(false);
      return;
    }

    // the assumption is that this code is run from the EDT so we need to run
    // the time consuming stuff in a different thread to stop things locking up
    new Thread() {
      @Override
      public void run() {
        installed.reInit();
        // reset the info ready for a reload
        availableModel.data.clear();
        updatesModel.data.clear();

        // go through all the known update sites and get all the plugins they
        // are making available, skipping those sites which are marked as
        // invalid for some reason
        for(RemoteUpdateSite rus : updateSites) {
          if(rus.enabled && (rus.valid == null || rus.valid)) {
            try {
              availableModel.data.addAll(rus.getCreolePlugins());
              rus.valid = true;
            } catch(Exception e) {
              e.printStackTrace();
              rus.valid = false;
            }
          }
        }

        // now work through the folders in the user plugin directory to see if
        // there are updates for any of the installed plugins
        if(userPluginDir.exists() && userPluginDir.isDirectory()) {
          File[] plugins = userPluginDir.listFiles();
          for(File f : plugins) {
            if(f.isDirectory()) {
              File pluginInfo = new File(f, "creole.xml");
              if(pluginInfo.exists()) {
                try {
                  CreolePlugin plugin =
                          CreolePlugin.load(pluginInfo.toURI().toURL());
                  if(plugin != null) {
                    int index = availableModel.data.indexOf(plugin);
                    if(index != -1) {
                      CreolePlugin ap = availableModel.data.remove(index);
                      if(ap.version > plugin.version) {
                        ap.installed = plugin.version;
                        ap.dir = f;
                        updatesModel.data.add(ap);
                      }
                    }
                  }

                  // add the plugin. most will already be known but this will
                  // catch any that have just been installed
                  Gate.addKnownPlugin(f.toURI().toURL());
                } catch(Exception e) {
                  e.printStackTrace();
                }
              }
            }
          }
        }

        SwingUtilities.invokeLater(new Thread() {
          @Override
          public void run() {
            // TODO probably not needed once the user dir is enumerated by the
            // main GATE methods
            installed.reInit();
            updatesModel.dataChanged();
            availableModel.dataChanged();

            // if we are going to load stuff then make the tabs available
            tabs.setEnabledAt(1, updatesModel.data.size() > 0);
            tabs.setEnabledAt(2, true);
            showProgressPanel(false);
          }
        });
      }
    }.start();
  }

  private void showProgressPanel(final boolean visible) {
    if(visible == getRootPane().getGlassPane().isVisible()) return;
    if(visible) {
      remove(panel);
      add(progressPanel, BorderLayout.CENTER);
    } else {
      remove(progressPanel);
      add(panel, BorderLayout.CENTER);
    }
    getRootPane().getGlassPane().setVisible(visible);
    validate();
  }

  private void applyChanges() {
    progressPanel.messageChanged("Updating CREOLE Plugin Configuration...");
    progressPanel.rangeChanged(0, updatesModel.data.size()
            + availableModel.data.size());
    showProgressPanel(true);

    // the assumption is that this code is run from the EDT so we need to run
    // the time consuming stuff in a different thread to stop things locking up
    new Thread() {
      @Override
      public void run() {
        if(getUserPluginsHome() != null) {

          // set up ANT ready to do the unzipping
          Expander expander = new Expander();
          expander.setOverwrite(true);
          expander.setDest(getUserPluginsHome());

          // store the list of failed plugins
          List<CreolePlugin> failed = new ArrayList<CreolePlugin>();

          // has the user been warned about installing updates (or have the
          // suppressed the warning)
          boolean hasBeenWarned =
                  Gate.getUserConfig().getBoolean(SUPPRESS_UPDATE_INSTALLED);

          // lets start by going through the updates that are available
          Iterator<CreolePlugin> it = updatesModel.data.iterator();
          while(it.hasNext()) {
            CreolePlugin p = it.next();
            if(p.install) {
              // if the user wants the update...
              if(!hasBeenWarned) {
                // warn them about the dangers of updating plugins if we haven't
                // done so yet
                if(JOptionPane
                        .showConfirmDialog(
                                PluginUpdateManager.this,
                                "<html><body style='width: 350px;'><b>UPDATE WARNING!</b><br><br>"
                                        + "Updating installed plugins will remove any customizations you may have made. "
                                        + "Are you sure you wish to continue?</body></html>",
                                "CREOLE Plugin Manager",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE, new DownloadIcon(
                                        48, 48)) == JOptionPane.OK_OPTION) {
                  hasBeenWarned = true;
                } else {
                  // if they want to stop then remove the progress panel
                  SwingUtilities.invokeLater(new Thread() {
                    @Override
                    public void run() {
                      showProgressPanel(false);
                    }
                  });
                  return;
                }
              }

              // report on which plugin we are updating
              progressPanel
                      .messageChanged("Updating CREOLE Plugin Configuration...<br>Currently Updating: "
                              + p.getName());
              try {

                // download the new version
                // TODO download this into the tmp directory
                File downloaded =
                        new File("pluigin-" + System.currentTimeMillis()
                                + ".zip");
                downloadFile(p.getName(), p.downloadURL, downloaded);

                // try to rename the existing plugin folder
                File renamed =
                        new File(getUserPluginsHome(), "renamed-"
                                + System.currentTimeMillis());

                if(!p.dir.renameTo(renamed)) {
                  // if we can't rename then just remember that we haven't
                  // updated this plugin
                  failed.add(p);
                } else {
                  // if we could rename then trash the old version
                  Files.rmdir(renamed);

                  // unzip the downloaded file
                  expander.setSrc(downloaded);
                  expander.execute();

                  // and delete the download
                  if(!downloaded.delete()) downloaded.deleteOnExit();
                }
              } catch(IOException ex) {
                // something went wrong so log the failed plugin
                ex.printStackTrace();
                failed.add(p);
              }
            }

            // move on to the next plugin
            progressPanel.valueIncrement();
          }

          // now lets work through the available plugins
          it = availableModel.data.iterator();
          while(it.hasNext()) {
            CreolePlugin p = it.next();
            if(p.install) {
              // if plugin is marked for install then...

              // update the progress panel
              progressPanel
                      .messageChanged("Updating CREOLE Plugin Configuration...<br>Currently Installing: "
                              + p.getName());
              try {
                // download the zip file
                File downloaded =
                        new File("pluigin-" + System.currentTimeMillis()
                                + ".zip");
                downloadFile(p.getName(), p.downloadURL, downloaded);

                // unpack it into the right place
                expander.setSrc(downloaded);
                expander.execute();

                // delete the download
                if(!downloaded.delete()) downloaded.deleteOnExit();
              } catch(IOException ex) {
                // something went wrong so log the failed plugin
                ex.printStackTrace();
                failed.add(p);
              }

              // move on to the next plugin
              progressPanel.valueIncrement();
            }
          }

          // explain that some plugins failed to install
          if(failed.size() > 0)
            JOptionPane
                    .showMessageDialog(
                            PluginUpdateManager.this,
                            "<html><body style='width: 350px;'><b>Installation of "
                                    + failed.size()
                                    + " plugins failed!</b><br><br>"
                                    + "Try unloading all plugins and then restarting GATE before trying to install or update plugins.</body></html>",
                            PluginUpdateManager.this.getTitle(),
                            JOptionPane.ERROR_MESSAGE);
        }

        // (un)load already installed plugins
        progressPanel.messageChanged("Updating CREOLE Plugin Configuration...");
        installed.updateAvailablePlugins();

        // refresh the tables to reflect what we have just done
        loadData();
      }
    }.start();
  }

  @Override
  public void dispose() {
    MainFrame.getGuiRoots().remove(this);
    super.dispose();
  }

  public PluginUpdateManager(JFrame owner) {
    super(owner, true);
    MainFrame.getGuiRoots().add(this);
    this.owner = owner;

    // get the list of remote update sites so we can fill in the GUI
    Map<String, String> sites = Gate.getUserConfig().getMap(GATE_UPDATE_SITES);
    for(Map.Entry<String, String> site : sites.entrySet()) {
      try {
        updateSites.add(new RemoteUpdateSite(site.getValue().substring(1),
                new URI(site.getKey()), site.getValue().charAt(0) == '1'));
      } catch(URISyntaxException e) {
        e.printStackTrace();
      }
    }

    if(updateSites.size() == 0) {
      /*
       * try { // TODO we need to change this to something more sensible
       * updateSites.add(new RemoteUpdateSite("Default Test Site", new URI(
       * "http://greenwoodma.servehttp.com/gate-plugins/"), true)); }
       * catch(URISyntaxException e) { // this can never happen! }
       */
    }

    // set up the main window
    setTitle("CREOLE Plugin Manager");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());
    setIconImages(Arrays.asList(new Image[]{new GATEIcon(64, 64).getImage(),
        new GATEIcon(48, 48).getImage(), new GATEIcon(32, 32).getImage(),
        new GATEIcon(22, 22).getImage(), new GATEIcon(16, 16).getImage()}));

    // set up the panel that displays the main GUI elements
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.add(tabs, BorderLayout.CENTER);

    // initialize all the different tabs
    tabs.addTab("Installed Plugins", new AvailableIcon(20, 20), installed);
    tabs.addTab("Available Updates", new UpdatesIcon(20, 20), buildUpdates());
    tabs.addTab("Available to Install", new DownloadIcon(20, 20),
            buildAvailable());
    tabs.addTab("Configuration", new AdvancedIcon(20, 20), buildConfig());
    tabs.setDisabledIconAt(
            1,
            new ImageIcon(GrayFilter.createDisabledImage((new UpdatesIcon(20,
                    20)).getImage())));
    tabs.setDisabledIconAt(
            2,
            new ImageIcon(GrayFilter.createDisabledImage((new DownloadIcon(20,
                    20)).getImage())));
    tabs.setEnabledAt(1, false);
    tabs.setEnabledAt(2, false);

    // setup the row of buttons at the bottom of the screen...
    JPanel pnlButtons = new JPanel();
    pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
    pnlButtons.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

    // ... the apply button
    JButton btnApply = new JButton("Apply All");
    getRootPane().setDefaultButton(btnApply);
    btnApply.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        PluginUpdateManager.this.applyChanges();
      }
    });

    // ... the close button
    Action cancelAction = new AbstractAction("Close") {
      @Override
      public void actionPerformed(ActionEvent e) {
        PluginUpdateManager.this.setVisible(false);
      }
    };
    JButton btnCancel = new JButton(cancelAction);

    // ... and the help button    
    Action helpAction = new AbstractAction("Help") {
      @Override
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showHelpFrame("sec:howto:plugins",
                "gate.gui.creole.PluginUpdateManager");
      }
    };
    JButton btnHelp = new JButton(helpAction);
    
    //add the buttons to the panel
    pnlButtons.add(btnHelp);
    pnlButtons.add(Box.createHorizontalGlue());
    pnlButtons.add(btnApply);
    pnlButtons.add(Box.createHorizontalStrut(5));
    pnlButtons.add(btnCancel);
    
    //and the panel to the main GUI
    panel.add(pnlButtons, BorderLayout.SOUTH);
    
    //make the main GUI the currently visisble dialog item
    add(panel, BorderLayout.CENTER);
    
    // define keystrokes action bindings at the level of the main window
    getRootPane().registerKeyboardAction(cancelAction,
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    getRootPane().registerKeyboardAction(helpAction,
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    
    //make sure the dialog is a reasonable size
    pack();
    Dimension screenSize = getGraphicsConfiguration().getBounds().getSize();
    Dimension dialogSize = getPreferredSize();
    int width =
            dialogSize.width > screenSize.width
                    ? screenSize.width * 3 / 4
                    : dialogSize.width;
    int height =
            dialogSize.height > screenSize.height
                    ? screenSize.height * 2 / 3
                    : dialogSize.height;
    setSize(width, height);
    validate();
    
    //TODO move this so we remember different folders for different actions
    MainFrame.getFileChooser().setResource(getClass().getName());
    
    //place the dialog somewhere sensible
    setLocationRelativeTo(owner);
  }

  private Component buildUpdates() {
    XJTable tblUpdates = new XJTable(updatesModel);
    tblUpdates.getColumnModel().getColumn(0)
            .setCellRenderer(new CheckBoxTableCellRenderer());
    tblUpdates.setSortable(false);
    tblUpdates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tblUpdates.getColumnModel().getColumn(0).setMaxWidth(100);
    tblUpdates.getColumnModel().getColumn(2).setMaxWidth(100);
    tblUpdates.getColumnModel().getColumn(3).setMaxWidth(100);
    JScrollPane scroller = new JScrollPane(tblUpdates);
    scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    return scroller;
  }

  private Component buildAvailable() {
    XJTable tblAvailable = new XJTable(availableModel);
    tblAvailable.getColumnModel().getColumn(0)
            .setCellRenderer(new CheckBoxTableCellRenderer());
    tblAvailable.setSortable(false);
    tblAvailable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tblAvailable.getColumnModel().getColumn(0).setMaxWidth(100);
    tblAvailable.getColumnModel().getColumn(2).setMaxWidth(100);
    JScrollPane scroller = new JScrollPane(tblAvailable);
    scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    return scroller;
  }

  private Component buildConfig() {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel pnlUpdateSites = new JPanel(new BorderLayout());
    pnlUpdateSites.setBorder(BorderFactory
            .createTitledBorder("Plugin Repositories:"));
    final XJTable tblSites = new XJTable(sitesModel);
    tblSites.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    pnlUpdateSites.add(new JScrollPane(tblSites), BorderLayout.CENTER);
    final JPanel pnlEdit = new JPanel(new SpringLayout());
    final JTextField txtName = new JTextField(20);
    final JTextField txtURL = new JTextField(20);
    pnlEdit.add(new JLabel("Name: "));
    pnlEdit.add(txtName);
    pnlEdit.add(new JLabel("URL: "));
    pnlEdit.add(txtURL);
    SpringUtilities.makeCompactGrid(pnlEdit, 2, 2, 6, 6, 6, 6);
    JButton btnAdd = new JButton(new AddIcon(24, 24));
    btnAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        txtName.setText("");
        txtURL.setText("");
        if(JOptionPane.showConfirmDialog(PluginUpdateManager.this, pnlEdit,
                "Plugin Repository Info", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, new UpdateSiteIcon(48, 48)) != JOptionPane.OK_OPTION)
          return;
        if(txtName.getText().trim().equals("")) return;
        if(txtURL.getText().trim().equals("")) return;
        try {
          updateSites.add(new RemoteUpdateSite(txtName.getText().trim(),
                  new URI(txtURL.getText().trim()), true));
          sitesModel.dataChanged();
          saveConfig();
          loadData();
        } catch(Exception ex) {
          ex.printStackTrace();
        }
      }
    });
    JButton btnRemove = new JButton(new RemoveIcon(24, 24));
    btnRemove.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int row = tblSites.getSelectedRow();
        if(row == -1) return;
        updateSites.remove(row);
        sitesModel.dataChanged();
        saveConfig();
        loadData();
      }
    });
    JButton btnEdit = new JButton(new EditIcon(24, 24));
    btnEdit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int row = tblSites.getSelectedRow();
        if(row == -1) return;
        RemoteUpdateSite site = updateSites.get(row);
        txtName.setText(site.name);
        txtURL.setText(site.uri.toString());
        if(JOptionPane.showConfirmDialog(PluginUpdateManager.this, pnlEdit,
                "Update Site Info", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, new UpdateSiteIcon(48, 48)) != JOptionPane.OK_OPTION)
          return;
        if(txtName.getText().trim().equals("")) return;
        if(txtURL.getText().trim().equals("")) return;
        try {
          URI url = new URI(txtURL.getText().trim());
          if(!url.equals(site.uri)) {
            site.uri = url;
            site.plugins = null;
          }
          site.name = txtName.getText().trim();
          site.valid = null;
          sitesModel.dataChanged();
          saveConfig();
          loadData();
        } catch(Exception ex) {
          ex.printStackTrace();
        }
      }
    });
    JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
    toolbar.setFloatable(false);
    toolbar.add(btnAdd);
    toolbar.add(btnRemove);
    toolbar.add(btnEdit);
    pnlUpdateSites.add(toolbar, BorderLayout.EAST);
    JToolBar pnlUserPlugins = new JToolBar(JToolBar.HORIZONTAL);
    pnlUserPlugins.setOpaque(false);
    pnlUserPlugins.setFloatable(false);
    pnlUserPlugins.setLayout(new BoxLayout(pnlUserPlugins, BoxLayout.X_AXIS));
    pnlUserPlugins.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    String userPluginsDir = (String)Gate.getUserConfig().get(GATE_USER_PLUGINS);
    final JTextField txtUserPlugins =
            new JTextField(userPluginsDir == null ? "" : userPluginsDir);
    txtUserPlugins.setEditable(false);
    JButton btnUserPlugins = new JButton(new OpenFileIcon(24, 24));
    btnUserPlugins.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        XJFileChooser fileChooser = MainFrame.getFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
        fileChooser.setResource("gate.CreoleRegister");
        int result = fileChooser.showOpenDialog(PluginUpdateManager.this);
        if(result == JFileChooser.APPROVE_OPTION) {
          userPluginDir = fileChooser.getSelectedFile();
          txtUserPlugins.setText(userPluginDir.getAbsolutePath());
          saveConfig();
          loadData();
        }
      }
    });
    pnlUserPlugins.setBorder(BorderFactory
            .createTitledBorder("User Plugin Directory: "));
    pnlUserPlugins.add(txtUserPlugins);
    pnlUserPlugins.add(btnUserPlugins);
    JPanel pnlSuppress = new JPanel();
    pnlSuppress.setLayout(new BoxLayout(pnlSuppress, BoxLayout.X_AXIS));
    pnlSuppress.setBorder(BorderFactory
            .createTitledBorder("Suppress Warning Messages:"));
    final JCheckBox chkUserPlugins =
            new JCheckBox("User Plugin Directory Not Set", Gate.getUserConfig()
                    .getBoolean(SUPPRESS_USER_PLUGINS));
    pnlSuppress.add(chkUserPlugins);
    pnlSuppress.add(Box.createHorizontalStrut(10));
    final JCheckBox chkUpdateInsatlled =
            new JCheckBox("Update Of Installed Plugin", Gate.getUserConfig()
                    .getBoolean(SUPPRESS_UPDATE_INSTALLED));
    pnlSuppress.add(chkUpdateInsatlled);
    ActionListener chkListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        Gate.getUserConfig().put(SUPPRESS_USER_PLUGINS,
                chkUserPlugins.isSelected());
        Gate.getUserConfig().put(SUPPRESS_UPDATE_INSTALLED,
                chkUpdateInsatlled.isSelected());
      }
    };
    chkUpdateInsatlled.addActionListener(chkListener);
    chkUserPlugins.addActionListener(chkListener);
    panel.add(pnlUpdateSites, BorderLayout.CENTER);
    panel.add(pnlUserPlugins, BorderLayout.NORTH);
    panel.add(pnlSuppress, BorderLayout.SOUTH);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    return panel;
  }

  private void downloadFile(String name, URL url, File file) throws IOException {
    InputStream in = null;
    FileOutputStream out = null;
    try {
      URLConnection conn = url.openConnection();
      in = conn.getInputStream();
      int expectedSize = conn.getContentLength();
      progressPanel.downloadStarting(name, expectedSize == -1);
      int downloaded = 0;
      byte[] buf = new byte[1024];
      int length;
      out = new FileOutputStream(file);
      while((in != null) && ((length = in.read(buf)) != -1)) {
        downloaded += length;
        out.write(buf, 0, length);
        if(expectedSize != -1)
          progressPanel.downloadProgress((downloaded * 100) / expectedSize);
      }
      out.flush();
    } finally {
      progressPanel.downloadFinished();
      if(out != null) out.close();
      if(in != null) in.close();
    }
  }

  @Override
  public void setVisible(boolean visible) {
    if(visible) {
      tabs.setSelectedIndex(0);
      installed.reInit();
      loadData();
      if(userPluginDir == null
              && !Gate.getUserConfig().getBoolean(SUPPRESS_USER_PLUGINS)) {
        JOptionPane
                .showMessageDialog(
                        owner,
                        "<html><body style='width: 350px;'><b>The user plugin folder has not yet been configured!</b><br><br>"
                                + "In order to install new CREOLE plugins you must choose a user plugins folder. "
                                + "This can be achieved from the Configuration tab of the CREOLE Plugin Manager.",
                        "CREOLE Plugin Manager",
                        JOptionPane.INFORMATION_MESSAGE, new UserPluginIcon(48,
                                48));
      }
    }
    super.setVisible(visible);
  }

  private static class PluginTableModel extends AbstractTableModel {
    private int columns;

    private List<CreolePlugin> data = new ArrayList<CreolePlugin>();

    public PluginTableModel(int columns) {
      this.columns = columns;
    }

    @Override
    public int getColumnCount() {
      return columns;
    }

    @Override
    public int getRowCount() {
      return data.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
      CreolePlugin plugin = data.get(row);
      switch(column){
        case 0:
          return plugin.install;
        case 1:
          return "<html><body>"
                  + plugin.getName()
                  + plugin.compatabilityInfo()
                  + (plugin.description != null
                          ? "<br><span style='font-size: 80%;'>"
                                  + plugin.description + "</span>"
                          : "") + "</body></html>";
        case 2:
          return plugin.version;
        case 3:
          return plugin.installed;
        default:
          return null;
      }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
      if(column != 0) return false;
      return data.get(row).compatible;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
      CreolePlugin plugin = data.get(row);
      plugin.install = (Boolean)value;
    }

    @Override
    public String getColumnName(int column) {
      switch(column){
        case 0:
          return "<html><body style='padding: 2px; text-align: center;'>Install</body></html>";
        case 1:
          return "<html><body style='padding: 2px; text-align: center;'>Plugin Name</body></html>";
        case 2:
          // TODO it would be nice to use "Version<br>Available" but for some
          // reason the header isn't expanding
          return "<html><body style='padding: 2px; text-align: center;'>Available</body></html>";
        case 3:
          return "<html><body style='padding: 2px; text-align: center;'>Installed</body></html>";
        default:
          return null;
      }
    }

    @Override
    public Class<?> getColumnClass(int column) {
      switch(column){
        case 0:
          return Boolean.class;
        case 1:
          return String.class;
        case 2:
          return Double.class;
        case 3:
          return Double.class;
        default:
          return null;
      }
    }

    public void dataChanged() {
      fireTableDataChanged();
    }
  }

  private static class Expander extends Expand {
    public Expander() {
      setProject(new Project());
      getProject().init();
      setTaskType("unzip");
      setTaskName("unzip");
      setOwningTarget(new Target());
    }
  }

  private class UpdateSiteModel extends AbstractTableModel {
    private transient Icon icoSite = new UpdateSiteIcon(32, 32);

    private transient Icon icoInvalid = new InvalidIcon(32, 32);

    private transient Icon icoGATE = new GATEUpdateSiteIcon(32, 32);

    @Override
    public String getColumnName(int column) {
      switch(column){
        case 0:
          return "";
        case 1:
          return "<html><body style='padding: 2px; text-align: center;'>Enabled</body></html>";
        case 2:
          return "<html><body style='padding: 2px; text-align: center;'>Repository Info</body></html>";
        default:
          return null;
      }
    }

    @Override
    public Class<?> getColumnClass(int column) {
      switch(column){
        case 0:
          return Icon.class;
        case 1:
          return Boolean.class;
        case 2:
          return String.class;
        default:
          return null;
      }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
      return column == 1;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
      RemoteUpdateSite site = updateSites.get(row);
      site.enabled = (Boolean)value;
      saveConfig();
    }

    public void dataChanged() {
      fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public int getRowCount() {
      return updateSites.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
      RemoteUpdateSite site = updateSites.get(row);
      switch(column){
        case 0:
          if(site.valid != null && !site.valid) return icoInvalid;
          if(site.uri.getHost().equals("gate.ac.uk")) return icoGATE;
          return icoSite;
        case 1:
          return site.enabled;
        case 2:
          return "<html><body>" + site.name
                  + "<br><span style='font-size: 80%;'>" + site.uri
                  + "</span></body></html>";
        default:
          return null;
      }
    }
  }
}