package gate.groovy.gui;

import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.event.ProgressListener;
import gate.groovy.ScriptPR;
import gate.gui.MainFrame;
import gate.util.Files;
import gate.util.GateRuntimeException;
import groovy.ui.ConsoleTextEditor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@CreoleResource(name = "Script Editor", comment = "Editor for the Groovy script behind this PR", guiType = GuiType.LARGE, mainViewer = true, resourceDisplayed = "gate.groovy.ScriptPR")
public class ScriptPREditor extends AbstractVisualResource implements
                                                          ProgressListener,
                                                          DocumentListener {

  private ConsoleTextEditor editor;

  private ScriptPR pr;

  private File file;

  private JButton btnSave, btnRevert;

  public Resource init() {
    initGuiComponents();

    return this;
  }

  protected void initGuiComponents() {
    setLayout(new BorderLayout());
    editor = new ConsoleTextEditor();
    editor.getTextEditor().getDocument().addDocumentListener(this);
    add(editor, BorderLayout.CENTER);

    btnSave = new JButton("Save and Reinitialize", MainFrame.getIcon("crystal-clear-app-download-manager"));
    btnSave.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        try {
          FileWriter out = new FileWriter(file);
          out.write(editor.getTextEditor().getText());
          out.flush();
          out.close();
          pr.reInit();
          btnRevert.setEnabled(false);
          btnSave.setEnabled(false);
        } catch(Exception ioe) {
          ioe.printStackTrace();
        }
      }
    });

    btnRevert = new JButton("Revert Changes", MainFrame.getIcon("crystal-clear-action-reload"));
    btnRevert.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editor.getTextEditor().setText(pr.getGroovySrc());
        btnRevert.setEnabled(false);
        btnSave.setEnabled(false);
      }
    });

    JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    toolbar.add(btnSave);
    toolbar.add(Box.createHorizontalStrut(5));
    toolbar.add(btnRevert);

    add(toolbar, BorderLayout.NORTH);
  }

  public void setTarget(Object target) {

    if(target == null) return;
    if(!(target instanceof ScriptPR)) { throw new GateRuntimeException(this
            .getClass().getName()
            + " can only be used to display "
            + ScriptPR.class.getName()
            + "\n"
            + target.getClass().getName()
            + " is not a " + ScriptPR.class.getName() + "!"); }

    if(pr != null) {
      pr.removeProgressListener(this);
    }

    pr = (ScriptPR)target;

    try {
      file = Files.fileFromURL(pr.getScriptURL());
    } catch(Exception e) {
      file = null;
    }

    editor.getTextEditor().setText(pr.getGroovySrc());

    editor.getTextEditor().setEditable(file != null);
    btnSave.setEnabled(false);
    btnRevert.setEnabled(false);

    pr.addProgressListener(this);
  }

  public void progressChanged(int i) {
    // do nothing and wait until the progress has finished
  }

  public void processFinished() {
    setTarget(pr);
  }

  public void changedUpdate(DocumentEvent e) {
    // ignore these

  }

  public void insertUpdate(DocumentEvent e) {
    btnRevert.setEnabled(true);
    btnSave.setEnabled(file != null);
  }

  public void removeUpdate(DocumentEvent e) {
    btnRevert.setEnabled(true);
    btnSave.setEnabled(file != null);
  }
}
