/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 13 Sep 2007
 *
 *  $Id$
 */
package gate.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListDataListener;

/**
 * A GUI component intended to allow quick selection from a set of
 * options. When the number of choices is small (i.e less or equal to
 * {@link #maximumFastChoices}) then the options are represented as a
 * set of buttons in a flow layout. If more options are available, a
 * simple {@link JComboBox} is used instead.
 */
public class JChoice extends JPanel {

  /**
   * The default value for the {@link #maximumWidth} parameter.
   */
  public static final int DEFAULT_MAX_WIDTH = 500;

  /**
   * The default value for the {@link #maximumFastChoices} parameter.
   */
  public static final int DEFAULT_MAX_FAST_CHOICES = 10;

  
  /**
   * The maximum number of options for which the flow of buttons is used
   * instead of a combobox. By default this value is
   * {@link #DEFAULT_MAX_FAST_CHOICES}
   */
  private int maximumFastChoices;


  /**
   * Margin used for choice buttons. 
   */
  private Insets defaultButtonMargin;
  
  /**
   * The maximum width allowed for this component. This value is only
   * used when the component appears as a flow of buttons. By default
   * this value is {@link #DEFAULT_MAX_WIDTH}. This is used to force the flow 
   * layout do a multi-line layout, as by default it prefers to lay all 
   * components in a single very wide line.
   */
  private int maximumWidth;

  /**
   * The layout used by this container.
   */
  private FlowLayout layout;

  /**
   * The combobox used for a large number of choices. 
   */
  private JComboBox combo;
  
  /**
   * The button group used for a small number of choices.
   */
  private ButtonGroup buttonGroup;
  
  /**
   * Internal item listener for both the combo and the buttons, used to keep
   * the two in sync. 
   */
  private ItemListener sharedItemListener; 
  
  /**
   * The data model used for choices and selection.
   */
  private ComboBoxModel model;
  
  /**
   * Keeps a mapping between the button and the corresponding option from the
   * model.
   */
  private Map<AbstractButton, Object> buttonToValueMap;
  
  /**
   * Creates a FastChoice with a default empty data model.
   */
  public JChoice() {
    this(new DefaultComboBoxModel());
  }
  
  /**
   * A map from wrapped action listeners to listener
   */
  private Map<EventListener, ListenerWrapper> listenersMap;
  
  /**
   * Creates a FastChoice with the given data model.
   */
  public JChoice(ComboBoxModel model) {
    layout = new FlowLayout();
    layout.setHgap(0);
    layout.setVgap(0);
    layout.setAlignment(FlowLayout.LEFT);
    setLayout(layout);
    this.model = model;
    
    initLocalData();
    buildGui();
  }

  /**
   * Creates a FastChoice with a default data model populated from the provided
   * array of objects.
   */
  public JChoice(Object[] items) {
    this(new DefaultComboBoxModel(items));
  }
  
  
  /**
   * Initialises some local values.
   */
  private void initLocalData(){
    maximumFastChoices = DEFAULT_MAX_FAST_CHOICES;
    maximumWidth = DEFAULT_MAX_WIDTH;
    listenersMap = new HashMap<EventListener, ListenerWrapper>();
    buttonGroup = new ButtonGroup();
    combo = new JComboBox(model);
    buttonToValueMap = new HashMap<AbstractButton, Object>();
    sharedItemListener = new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        //we only care about SELECTED events
        if(e.getStateChange() == ItemEvent.SELECTED){
          if(e.getSource() == combo){
            //combo selection changed -> propagate to buttons
            for(AbstractButton aBtn : buttonToValueMap.keySet()){
              Object aValue = buttonToValueMap.get(aBtn);
              if(e.getItem().equals(aValue)){
                //we found the right button
                aBtn.setSelected(true);
                break;
              }
            }
          }else{
            //button selection changed -> propagate to combo
            Object value = buttonToValueMap.get(e.getSource());
            combo.setSelectedItem(value);
          }
        }
      }      
    };
    combo.addItemListener(sharedItemListener);
  }
  
  public static void main(String[] args){
    final JChoice fChioce = new JChoice(new String[]{
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"});
    fChioce.setMaximumFastChoices(20);
    fChioce.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        System.out.println("Action (" + e.getActionCommand() + ") :" + fChioce.getSelectedItem().toString() + " selected!");
      }
    });
    fChioce.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        System.out.println("Item " + e.getItem().toString() +
               (e.getStateChange() == ItemEvent.SELECTED ? " selected!" :
               " deselected!"));
      }
      
    });
    JFrame aFrame = new JFrame("Fast Chioce Test Frame");
    aFrame.getContentPane().add(fChioce);
    
    aFrame.pack();
    aFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    aFrame.setVisible(true);
  }
  
  /**
   * @param l
   * @see javax.swing.JComboBox#removeActionListener(java.awt.event.ActionListener)
   */
  public void removeActionListener(ActionListener l) {
    ListenerWrapper wrapper = listenersMap.remove(l);
    combo.removeActionListener(wrapper);
  }

  /**
   * @param listener
   * @see javax.swing.JComboBox#removeItemListener(java.awt.event.ItemListener)
   */
  public void removeItemListener(ItemListener listener) {
    ListenerWrapper wrapper = listenersMap.remove(listener);
    combo.removeActionListener(wrapper);
  }

  /**
   * @param l
   * @see javax.swing.JComboBox#addActionListener(java.awt.event.ActionListener)
   */
  public void addActionListener(ActionListener l) {
    combo.addActionListener(new ListenerWrapper(l));
  }

  /**
   * @param listener
   * @see javax.swing.JComboBox#addItemListener(java.awt.event.ItemListener)
   */
  public void addItemListener(ItemListener listener) {
    combo.addItemListener(new ListenerWrapper(listener));
  }

  /**
   * (Re)constructs the UI. This can be called many times, whenever a 
   * significant value (such as {@link #maximumFastChoices}, or the model)
   * has changed.
   */
  private void buildGui(){
    removeAll();
    if(model != null && model.getSize() > 0){
      if(model.getSize() > maximumFastChoices){
        //use combobox
        add(combo);
      }else{
        //use buttons
        //first clear the old buttons, if any exist
        if(buttonGroup != null && buttonGroup.getButtonCount() > 0){
          Enumeration<AbstractButton> btnEnum = buttonGroup.getElements();
          while(btnEnum.hasMoreElements()){
            AbstractButton aButton = btnEnum.nextElement();
            aButton.removeItemListener(sharedItemListener);
            buttonGroup.remove(aButton);
          }
        }
        //now create the new buttons
        buttonToValueMap.clear();
        for(int i = 0; i < model.getSize(); i++){
          Object aValue = model.getElementAt(i);
          JToggleButton aButton = new JToggleButton(aValue.toString());
          if(defaultButtonMargin != null) aButton.setMargin(defaultButtonMargin);
          aButton.addItemListener(sharedItemListener);
          buttonGroup.add(aButton);
          buttonToValueMap.put(aButton, aValue);
          add(aButton);
        }
      }
    }
    revalidate();
  }
  
  
  /**
   * @param l
   * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
   */
  public void addListDataListener(ListDataListener l) {
    model.addListDataListener(l);
  }

  /**
   * @param index
   * @return
   * @see javax.swing.ListModel#getElementAt(int)
   */
  public Object getElementAt(int index) {
    return model.getElementAt(index);
  }

  /**
   * @return
   * @see javax.swing.ComboBoxModel#getSelectedItem()
   */
  public Object getSelectedItem() {
    return model.getSelectedItem();
  }

  /**
   * @return
   * @see javax.swing.ListModel#getSize()
   */
  public int getItemCount() {
    return model.getSize();
  }

  /**
   * @param l
   * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
   */
  public void removeListDataListener(ListDataListener l) {
    model.removeListDataListener(l);
  }

  /**
   * @param anItem
   * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
   */
  public void setSelectedItem(Object anItem) {
    combo.setSelectedItem(anItem);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    Dimension size = super.getPreferredSize();
    if(getItemCount() <= maximumFastChoices && size.width > maximumWidth) {
      setSize(maximumWidth, Integer.MAX_VALUE);
      doLayout();
      int compCnt = getComponentCount();
      if(compCnt > 0) {
        Component lastComp = getComponent(compCnt - 1);
        Point compLoc = lastComp.getLocation();
        Dimension compSize = lastComp.getSize();
        size.width = maximumWidth;
        size.height = compLoc.y + compSize.height + getInsets().bottom;
      }
    }
    return size;
  }
  

  /**
   * @return the maximumFastChoices
   */
  public int getMaximumFastChoices() {
    return maximumFastChoices;
  }

  /**
   * @param maximumFastChoices the maximumFastChoices to set
   */
  public void setMaximumFastChoices(int maximumFastChoices) {
    this.maximumFastChoices = maximumFastChoices;
    buildGui();
  }

  
  /**
   * @return the model
   */
  public ComboBoxModel getModel() {
    return model;
  }

  /**
   * @param model the model to set
   */
  public void setModel(ComboBoxModel model) {
    this.model = model;
    combo.setModel(model);
    buildGui();
  }

  /**
   * @return the maximumWidth
   */
  public int getMaximumWidth() {
    return maximumWidth;
  }

  /**
   * @param maximumWidth the maximumWidth to set
   */
  public void setMaximumWidth(int maximumWidth) {
    this.maximumWidth = maximumWidth;
  }
  
  /**
   * An action listener that changes the source of events to be this object.
   */
  private class ListenerWrapper implements ActionListener, ItemListener{
    public ListenerWrapper(EventListener originalListener) {
      this.originalListener = originalListener;
      listenersMap.put(originalListener, this);
    }

    public void itemStateChanged(ItemEvent e) {
      e.setSource(JChoice.this);
      ((ItemListener)originalListener).itemStateChanged(e);
    }

    public void actionPerformed(ActionEvent e) {
      e.setSource(JChoice.this);
      ((ActionListener)originalListener).actionPerformed(e);
    }
    private EventListener originalListener;
  }

  /**
   * @return the defaultButtonMargin
   */
  public Insets getDefaultButtonMargin() {
    return defaultButtonMargin;
  }

  /**
   * @param defaultButtonMargin the defaultButtonMargin to set
   */
  public void setDefaultButtonMargin(Insets defaultButtonMargin) {
    this.defaultButtonMargin = defaultButtonMargin;
    buildGui();
  }
}
