package gate.gui.ontology;

import gate.creole.ontology.*;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Calendar;
import javax.swing.*;

public class TopClassAction extends AbstractAction {
  public TopClassAction(String s, Icon icon) {
    super(s, icon);
    nameSpace = new JTextField(20);
    className = new JTextField(20);
    comment = new JTextField(20);
    labelPanel = new JPanel(new GridLayout(3, 1));
    textFieldsPanel = new JPanel(new GridLayout(3, 1));
    panel = new JPanel(new FlowLayout(0));
    panel.add(labelPanel);
    panel.add(textFieldsPanel);
    labelPanel.add(new JLabel("Name Space :"));
    textFieldsPanel.add(nameSpace);
    labelPanel.add(new JLabel("Top Class Name :"));
    textFieldsPanel.add(className);
    labelPanel.add(new JLabel("Comment :"));
    textFieldsPanel.add(comment);
  }

  public void actionPerformed(ActionEvent actionevent) {
    nameSpace.setText(ontology.getDefaultNameSpace());
    int i = JOptionPane.showOptionDialog(null, panel, "New Top Class", 2, 3,
            null, new String[]{"OK", "Cancel"}, "OK");
    if(i == 0) {
      String s = nameSpace.getText();
      if(!Utils.isValidNameSpace(s)) {
        JOptionPane.showMessageDialog(null, (new StringBuilder()).append(
                "Invalid NameSpace:").append(s).append(
                "\n example: http://gate.ac.uk/example#").toString());
        return;
      }
      if(!Utils.isValidOntologyResourceName(className.getText())) {
        JOptionPane.showMessageDialog(null, "Invalid Classname");
        return;
      }
      OClassImpl oclassimpl = new OClassImpl(Calendar.getInstance().getTime()
              .toString(), className.getText(), comment.getText(), ontology);
      oclassimpl.setURI((new StringBuilder()).append(nameSpace.getText())
              .append(oclassimpl.getName()).toString());
      TClass topClass = ontology.getClassByName("Thing");
      if(topClass != null && topClass.isTopClass()) {
        topClass.addSubClass(oclassimpl);
        oclassimpl.addSuperClass(topClass);
        ontology.addClass(oclassimpl);
      } else {
        ontology.addClass(oclassimpl);
      }
    }
  }

  public Ontology getOntology() {
    return ontology;
  }

  public void setOntology(Ontology ontology1) {
    ontology = ontology1;
  }

  protected JTextField nameSpace;

  protected JTextField className;

  protected JTextField comment;

  protected JPanel labelPanel;

  protected JPanel textFieldsPanel;

  protected JPanel panel;

  protected Ontology ontology;
}
