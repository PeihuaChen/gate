package gate.creole;

import gate.Resource;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleResource;
import gate.gui.ActionsPublisher;
import gate.gui.MainFrame;
import gate.resources.img.svg.HeapDumpIcon;
import gate.swing.XJFileChooser;
import gate.util.ExtensionFileFilter;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
@CreoleResource(tool = true, isPrivate = true, autoinstances = @AutoInstance, name = "Java Heap Dumper", helpURL = "http://gate.ac.uk/userguide/sec:misc-creole:dev-tools", comment = "Dumps the Java heap to the specified file")
public class HeapDumper extends AbstractResource implements ActionsPublisher {

  private List<Action> actions = null;

  private static volatile Object hotspot = null;

  private static volatile Method dumper = null;

  @Override
  public List<Action> getActions() {

    if(actions == null) {
      actions = new ArrayList<Action>();

      if(isHotSpotAvailable()) {

        actions.add(new AbstractAction("Dump Java Heap...", new HeapDumpIcon(
            24, 24)) {

          @Override
          public void actionPerformed(ActionEvent e) {
            XJFileChooser fileChooser = MainFrame.getFileChooser();
            ExtensionFileFilter filter =
                new ExtensionFileFilter("Java Heap Dump (*.hprof)", "hprof");
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Java Heap Dump Generator");

            if(fileChooser.showSaveDialog(MainFrame.getInstance()) != JFileChooser.APPROVE_OPTION)
              return;

            final File selectedFile = fileChooser.getSelectedFile();
            if(selectedFile == null) return;

            if(selectedFile.exists() && !selectedFile.delete()) {
              System.out.println("file exists but can't be deleted");
            }

            

            Runnable runableAction = new Runnable() {

              @Override
              public void run() {

                MainFrame.lockGUI("Dumping Heap...");
                
                try {
                  // MainFrame.lockGUI("Dumping Heap...");
                  dumpHeap(selectedFile);
                } catch(Exception ex) {
                  ex.printStackTrace();
                } finally {
                  SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                      // TODO Auto-generated method stub
                      MainFrame.unlockGUI();
                    }
                  });
                }
              }
            };

            Thread thread = new Thread(runableAction, "Heap Dumper");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
          }

        });
      }
    }

    return actions;
  }

  /**
   * Call this method from your application whenever you want to dump the heap
   * snapshot into a file.
   */
  public static void dumpHeap(File file) throws IOException {

    if(!isHotSpotAvailable())
      throw new IOException("Unable to access HotSpot to dump heap");

    try {
      dumper.invoke(hotspot, file.getAbsolutePath(), false);
    } catch(Exception e) {
      throw new IOException("Unable to dump heap", e);
    }
  }

  @Override
  public Resource init() throws ResourceInstantiationException {

    if(dumper == null) {
      synchronized(HeapDumper.class) {
        try {
          MBeanServer server = ManagementFactory.getPlatformMBeanServer();
          hotspot =
              ManagementFactory.newPlatformMXBeanProxy(
                  server,
                  "com.sun.management:type=HotSpotDiagnostic",
                  HeapDumper.class.getClassLoader().loadClass(
                      "com.sun.management.HotSpotDiagnosticMXBean"));
          dumper =
              hotspot.getClass().getMethod("dumpHeap", String.class,
                  boolean.class);

        } catch(Exception e) {
          // for now we just swallow any problems and don't add the menu item
          // e.printStackTrace();
        }
      }
    }

    return this;
  }

  public static boolean isHotSpotAvailable() {
    return dumper != null;
  }
}
