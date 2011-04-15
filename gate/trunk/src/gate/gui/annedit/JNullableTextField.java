/*
 *  Copyright (c) 1995-2011, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).

 *  Valentin Tablan, 15 Apr 2011
 *
 *  $Id$
 */
package gate.gui.annedit;

import gate.gui.MainFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * An encapsulation of {@link JTextField} and a {@link JButton} that allows 
 * the text value to be set to null by pressing the button. Provides the minimal
 * API required for the needs of {@link SchemaFeaturesEditor}. 
 */
public class JNullableTextField extends JPanel {
  private static final long serialVersionUID = -1530694436281692216L;

  protected class NullifyTextAction extends AbstractAction {
    private static final long serialVersionUID = -7807829141939910776L;

    public NullifyTextAction() {
      super(null, MainFrame.getIcon("delete"));
      putValue(SHORT_DESCRIPTION, "Removes this feature completely");
    }

    public void actionPerformed(ActionEvent e) {
      textField.setText(null);
      text = null;
      fireInsertUpdate(null);
    }
  }
  
  protected JButton nullifyButton;
  
  protected JTextField textField;
  
  protected Color normalBgColor;
  
  protected Color nullBgColor = new Color(200, 250, 255);
  
  protected Set<DocumentListener> documentListeners;
  
  /**
   * The text value, which can be null
   */
  protected String text = null;
  
  public JNullableTextField() {
    initGui();
    initListeners();
  }

  public void setText(String text) {
    textField.setText(text);
    this.text = text;
    fireInsertUpdate(null);
  }
  
  public String getText() {
    return text;
  }

  public void setColumns(int cols) {
    textField.setColumns(cols);
  }
  
  protected void initGui() {
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    textField = new JTextField();
    add(textField);
    add(Box.createHorizontalStrut(2));
    nullifyButton = new JButton(new NullifyTextAction());
//    nullifyButton.setBorderPainted(true);
//    nullifyButton.setContentAreaFilled(false);
//    nullifyButton.setOpaque(false);
//    nullifyButton.setMargin(new Insets(2, 2, 2, 2));
    add(nullifyButton);

    normalBgColor = textField.getBackground();
    // borrow the LnF from the text field
//    setBackground(textField.getBackground());
//    Border border = textField.getBorder();
//    textField.setBorder(null);
//    setBorder(border);
  }
  
  protected void initListeners() {
    documentListeners = Collections.synchronizedSet(
            new HashSet<DocumentListener>());
    
    final DocumentListener tfDocumentListener = new DocumentListener() {
      public void removeUpdate(DocumentEvent e) {
        text = textField.getText();
        fireRemoveUpdate(e);
      }
      
      public void insertUpdate(DocumentEvent e) {
        text = textField.getText();
        fireInsertUpdate(e);
      }
      
      public void changedUpdate(DocumentEvent e) {
        fireChangedUpdate(e);
      }
    };
    
    textField.getDocument().addDocumentListener(tfDocumentListener);
    
    textField.addPropertyChangeListener("document", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        textField.getDocument().addDocumentListener(tfDocumentListener);
      }
    });
    
    // listen to our own events, and display null value
    addDocumentListener(new DocumentListener() {
      public void removeUpdate(DocumentEvent e) {
        valueChanged();
      }
      public void insertUpdate(DocumentEvent e) {
        valueChanged();
      }
      
      public void changedUpdate(DocumentEvent e) { }
      
      private void valueChanged() {
        if(getText() == null) {
          textField.setBackground(nullBgColor);
        } else {
          textField.setBackground(normalBgColor);
        }
      }
    });
    
  }

  public void addDocumentListener(DocumentListener listener) {
    documentListeners.add(listener);
  }

  public void removeDocumentListener(DocumentListener listener) {
    documentListeners.remove(listener);
  }
  
  protected void fireChangedUpdate(DocumentEvent e) {
    for(DocumentListener aListener : documentListeners) 
      aListener.changedUpdate(e);
  }
  
  protected void fireInsertUpdate(DocumentEvent e) {
    for(DocumentListener aListener : documentListeners) 
      aListener.insertUpdate(e);
  }
  
  protected void fireRemoveUpdate(DocumentEvent e) {
    for(DocumentListener aListener : documentListeners) 
      aListener.removeUpdate(e);
  }
  
  public static void main(String[] args) {
    
    JFrame frame = new JFrame(JNullableTextField.class.getName());
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    
    final JNullableTextField textField = new JNullableTextField();
    textField.setText("Test text");
    mainPanel.add(textField, BorderLayout.CENTER);

    final JLabel textLabel = new JLabel();
    JButton getTextButton = new JButton(new AbstractAction("Copy text") {
      public void actionPerformed(ActionEvent e) {
        String text = textField.getText();
        textLabel.setText(text == null ? "<null>" : text);
      }
    });
    
    Box box = new Box(BoxLayout.X_AXIS);
    box.add(getTextButton);
    box.add(textLabel);
    mainPanel.add(box, BorderLayout.SOUTH);
    
    frame.add(mainPanel);
    frame.setSize(400, 400);
    frame.pack();
    frame.setVisible(true);
    
    
  }
}
