package gate.gui.ontology;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.text.JTextComponent;

public class ValuesSelectionAction {

	public ValuesSelectionAction() {
		list = null;
		domainBox = new JComboBox();
		domainBox.setEditable(true);
		list = new JList(new DefaultListModel());
		list.setVisibleRowCount(7);
		add = new JButton("Add");
		remove = new JButton("Remove");
		panel = new JPanel();
		BoxLayout boxlayout = new BoxLayout(panel, 1);
		panel.setLayout(boxlayout);
		panel.add(domainBox);
    domainBox.setEditable(true);
    
    domainBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent keyevent) {
        String s = ((JTextComponent) domainBox.getEditor().getEditorComponent()).getText();
        if (s != null) {
          ArrayList arraylist = new ArrayList();
          for (int i = 0; i < ontologyClasses.length; i++) {
            String s1 = ontologyClasses[i];
            if (s1.toLowerCase().startsWith(s.toLowerCase())) {
              arraylist.add(s1);
            }
          }

          Collections.sort(arraylist);
          DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(arraylist.toArray());
          domainBox.setModel(defaultcomboboxmodel);
          try {
            domainBox.showPopup();
          } catch (Exception exception) {
          }
          ((JTextComponent) domainBox.getEditor().getEditorComponent()).setText(s);
        }
      }
    });
    
		JPanel jpanel = new JPanel(new FlowLayout(1));
		jpanel.add(add);
		jpanel.add(remove);
		panel.add(jpanel);
		panel.add(new JScrollPane(list));
		add.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent) {
				String s = (String) domainBox.getSelectedItem();
				if (((DefaultListModel) list.getModel()).contains(s)) {
					JOptionPane.showMessageDialog(null, "Already added!");
					return;
				} else {
					((DefaultListModel) list.getModel()).addElement(s);
					return;
				}
			}

			final ValuesSelectionAction this$0;

			{
				this$0 = ValuesSelectionAction.this;
			}
		});
		remove.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent actionevent) {
				Object aobj[] = list.getSelectedValues();
				if (aobj != null || aobj.length > 0) {
					for (int i = 0; i < aobj.length; i++)
						((DefaultListModel) list.getModel())
								.removeElement(aobj[i]);

				}
			}

			final ValuesSelectionAction this$0;

			{
				this$0 = ValuesSelectionAction.this;
			}
		});
	}


	public void showGUI(String s, String as[], String as1[]) {
	  this.ontologyClasses = as;
    DefaultComboBoxModel defaultcomboboxmodel = new DefaultComboBoxModel(as);
		domainBox.setModel(defaultcomboboxmodel);
		DefaultListModel defaultlistmodel = new DefaultListModel();
		for (int i = 0; i < as1.length; i++)
			defaultlistmodel.addElement(as1[i]);

		list.setModel(defaultlistmodel);
		JOptionPane.showOptionDialog(null, panel, s, 0, 3, null,
				new String[] { "OK" }, "OK");
	}


	public String[] getSelectedValues() {
		DefaultListModel defaultlistmodel = (DefaultListModel) list.getModel();
		String as[] = new String[defaultlistmodel.getSize()];
		for (int i = 0; i < as.length; i++)
			as[i] = (String) defaultlistmodel.getElementAt(i);

		return as;
	}

	final protected JComboBox domainBox;

	protected JList list;

	protected JButton add;

	protected JButton remove;

	protected JPanel panel;
  
  protected String[] ontologyClasses;
}
