package gate.swing;

import javax.swing.*;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.Dialog;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import javax.swing.plaf.FontUIResource;

public class JFontChooser extends JPanel {

  public JFontChooser(){
    this(UIManager.getFont("Button.font"));
  }

  public JFontChooser(Font initialFont){
    initLocalData();
    initGuiComponents();
    initListeners();
    setFontValue(initialFont);
  }

  public static Font showDialog(Component parent, String title,
                                Font initialfont){

    Window windowParent;
    if(parent instanceof Window) windowParent = (Window)parent;
    else windowParent = SwingUtilities.getWindowAncestor(parent);
    if(windowParent == null) throw new IllegalArgumentException(
      "The supplied parent component has no window ancestor");
    JDialog dialog;
    if(windowParent instanceof Frame) dialog = new JDialog((Frame)windowParent,
                                                           title, true);
    else dialog = new JDialog((Dialog)windowParent, title, true);

    dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(),
                                                    BoxLayout.Y_AXIS));

    JFontChooser fontChooser = new JFontChooser(initialfont);
    dialog.getContentPane().add(fontChooser);

    JButton okBtn = new JButton("OK");
    JButton cancelBtn = new JButton("Cancel");
    JPanel buttonsBox = new JPanel();
    buttonsBox.setLayout(new BoxLayout(buttonsBox, BoxLayout.X_AXIS));
    buttonsBox.add(Box.createHorizontalGlue());
    buttonsBox.add(okBtn);
    buttonsBox.add(Box.createHorizontalStrut(30));
    buttonsBox.add(cancelBtn);
    buttonsBox.add(Box.createHorizontalGlue());
    dialog.getContentPane().add(buttonsBox);
    dialog.pack();
    dialog.show();
    return fontChooser.getFontValue();
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    familyCombo = new JComboBox(
                        GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getAvailableFontFamilyNames()
                      );
    familyCombo.setSelectedItem(UIManager.getFont("Label.font").getFamily());

    sizeCombo = new JComboBox(new String[]{"6", "8", "10", "12", "14", "16",
                                              "18", "20", "22", "24", "26"});
    sizeCombo.setSelectedItem(new Integer(
                        UIManager.getFont("Label.font").getSize()).toString());

    italicChk = new JCheckBox("<html><i>Italic</i></html>", false);
    boldChk = new JCheckBox("<html><i=b>Bold</b></html>", false);

    JPanel fontBox = new JPanel();
    fontBox.setLayout(new BoxLayout(fontBox, BoxLayout.X_AXIS));
    fontBox.add(familyCombo);
    fontBox.add(sizeCombo);
    fontBox.setBorder(BorderFactory.createTitledBorder("Font"));
    add(fontBox);
    add(Box.createVerticalStrut(10));

    JPanel effectsBox = new JPanel();
    effectsBox.setLayout(new BoxLayout(effectsBox, BoxLayout.X_AXIS));
    effectsBox.add(italicChk);
    effectsBox.add(boldChk);
    effectsBox.setBorder(BorderFactory.createTitledBorder("Effects"));
    add(effectsBox);
    add(Box.createVerticalStrut(10));

    sampleTextArea = new JTextArea("Type your sample here...");
    JPanel samplePanel = new JPanel();
    samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.X_AXIS));
    samplePanel.add(new JScrollPane(sampleTextArea));
    samplePanel.setBorder(BorderFactory.createTitledBorder("Sample"));
    add(samplePanel);
    add(Box.createVerticalStrut(10));
  }

  protected void initListeners(){
  }

  public static void main(String args[]){
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }catch(Exception e){
      e.printStackTrace();
    }
    JFrame frame = new JFrame("Foo frame");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setSize(new Dimension(300, 300));
    frame.setVisible(true);
    showDialog(frame, "Fonter", UIManager.getFont("Button.font"));
  }

  public void setFontValue(java.awt.Font newFontValue) {
    fontValue = newFontValue;
  }

  public java.awt.Font getFontValue() {
    return fontValue;
  }

  JComboBox familyCombo;
  JCheckBox italicChk;
  JCheckBox boldChk;
  JComboBox sizeCombo;
  JTextArea sampleTextArea;
  private java.awt.Font fontValue;
}