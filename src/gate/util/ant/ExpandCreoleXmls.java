package gate.util.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import gate.Gate;
import gate.creole.CreoleAnnotationHandler;
import gate.creole.metadata.CreoleResource;
import gate.util.CreoleXmlUpperCaseFilter;
import gate.util.GateException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * Ant task to take a bunch of creole.xml files, process the
 * {@link CreoleResource} annotations on their resources, and write the
 * augmented XML to a target directory.  If the "classesOnly" attribute is
 * set to "true" (or "1", "yes" or "on" in the normal Ant way) then the task
 * performs only the first JAR scanning step, adding
 * <pre>
 * &lt;RESOURCE&gt;
 *   &lt;CLASS&gt;com.example.ClassName&lt;/CLASS&gt;
 * &lt;/RESOURCE&gt;
 * </pre>
 * for each annotated class in the JAR, but does not process the annotation
 * fully.  This limited expansion is useful in cases where you will be using
 * the plugin in a way that does not allow JAR scanning to take place at
 * runtime, for example if you will be loading the plugin directly from a
 * <code>jar:<code> URL (with the plugin's JAR files placed on your
 * application's classpath).
 */
public class ExpandCreoleXmls extends Task {

  private static boolean gateInited = false;
  
  private List<FileSet> srcFiles = new ArrayList<FileSet>();
  
  private File toDir;

  private boolean classesOnly = false;
  
  private SAXBuilder builder;
  
  private XMLOutputter outputter = new XMLOutputter();

  public ExpandCreoleXmls() {
    builder = new SAXBuilder(false);
    builder.setXMLFilter(new CreoleXmlUpperCaseFilter());
  }
  
  public void addFileset(FileSet fs) {
    srcFiles.add(fs);
  }
  
  public void setTodir(File toDir) {
    this.toDir = toDir;
  }

  public void setClassesOnly(boolean classesOnly) {
    this.classesOnly = classesOnly;
  }
  
  @Override
  public void execute() throws BuildException {
    if(toDir == null) {
      throw new BuildException("Please specify a destination directory using todir", getLocation());
    }
    if(toDir.isFile()) {
      throw new BuildException("Destination already exists and is not a directory", getLocation());
    }

    if(!gateInited) {
      try {
        Gate.init();
      }
      catch(GateException e) {
        throw new BuildException("Error initialising GATE", e, getLocation());
      }
    }
    for(FileSet fs : srcFiles) {
      DirectoryScanner ds = fs.getDirectoryScanner(getProject());
      for(String f : ds.getIncludedFiles()) {
        File creoleFile = new File(ds.getBasedir(), f);
        try {
          File plugin = creoleFile.getParentFile();
          File destFile = new File(toDir, f);
          File destPlugin = destFile.getParentFile();

          log("Expanding " + creoleFile + " to " + destFile, Project.MSG_VERBOSE);
          Gate.addKnownPlugin(plugin.toURI().toURL());
          CreoleAnnotationHandler annotationHandler = new CreoleAnnotationHandler(creoleFile.toURI().toURL());
          Document creoleDoc = builder.build(creoleFile);
          annotationHandler.createResourceElementsForDirInfo(creoleDoc);
          if(!classesOnly) {
            annotationHandler.addJarsToClassLoader(creoleDoc);
            annotationHandler.processAnnotations(creoleDoc);
          }
          
          destPlugin.mkdirs();
          FileOutputStream fos = new FileOutputStream(destFile);
          try {
            outputter.output(creoleDoc, fos);
          }
          finally {
            fos.close();
          }
        }
        catch(Exception e) {
          log("Error processing " + creoleFile + ", skipped", Project.MSG_WARN);
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          e.printStackTrace(pw);
          log(sw.toString(), Project.MSG_VERBOSE);
        }
      }
    }
  }

  public void setGateHome(File gateHome) {
    Gate.setGateHome(gateHome);
  }
  
  public void setPluginsHome(File pluginsHome) {
    Gate.setPluginsHome(pluginsHome);
  }

}
