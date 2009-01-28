/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 02/10/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import gate.*;
import gate.creole.*;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.event.*;
import gate.swing.*;
import gate.util.*;

@CreoleResource(name = "Serial Application Editor", guiType = GuiType.LARGE,
    resourceDisplayed = "gate.creole.SerialController", mainViewer = true)
public class SerialControllerEditor extends AbstractVisualResource
                               implements CreoleListener, ControllerListener,
                                          ActionsPublisher{

  public SerialControllerEditor() {

  }

  public void setTarget(Object target){
    if(!(target instanceof SerialController))
    throw new IllegalArgumentException(
      "gate.gui.ApplicationViewer can only be used for serial controllers\n" +
      target.getClass().toString() +
      " is not a gate.creole.SerialController!");
    if(controller != null) controller.removeControllerListener(this);
    this.controller = (SerialController)target;
    controller.addControllerListener(this);
    analyserMode = controller instanceof SerialAnalyserController ||
                   controller instanceof ConditionalSerialAnalyserController;
    conditionalMode = controller instanceof ConditionalController;
    
    initLocalData();
    initGuiComponents();
    initListeners();

    loadedPRsTableModel.fireTableDataChanged();
    memberPRsTableModel.fireTableDataChanged();

  }//setController


  public void setHandle(Handle handle) {
    this.handle = handle;

    //register the listeners
    if(handle instanceof StatusListener)
      addStatusListener((StatusListener)handle);
    if(handle instanceof ProgressListener)
      addProgressListener((ProgressListener)handle);
  }

  public Resource init() throws ResourceInstantiationException{
    super.init();
    return this;
  }

  protected void initLocalData() {
    actionList = new ArrayList<Action>();
    runAction = new RunAction();
    //add the items to the popup
    actionList.add(null);
    actionList.add(runAction);
    addPRAction = new AddPRAction();
    removePRAction = new RemovePRAction();
  }

  protected void initGuiComponents() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


    JPanel topBox = new JPanel();
    topBox.setLayout(new BoxLayout(topBox, BoxLayout.X_AXIS));
    topBox.setAlignmentX(Component.LEFT_ALIGNMENT);

    loadedPRsTableModel = new LoadedPRsTableModel();
    loadedPRsTable = new XJTable();
    loadedPRsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    loadedPRsTable.setSortable(false);
    loadedPRsTable.setModel(loadedPRsTableModel);
    loadedPRsTable.setDragEnabled(true);
    loadedPRsTable.setTransferHandler(new TransferHandler() {
      // minimal drag and drop that only call the removePRAction when importing
      String source = "";
      public int getSourceActions(JComponent c) {
        return MOVE;
      }
      protected Transferable createTransferable(JComponent c) {
        return new StringSelection("loadedPRsTable");
      }
      protected void exportDone(JComponent c, Transferable data, int action) {
      }
      public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for(DataFlavor flavor : flavors) {
          if(DataFlavor.stringFlavor.equals(flavor)) {
            return true;
          }
        }
        return false;
      }
      public boolean importData(JComponent c, Transferable t) {
        if (canImport(c, t.getTransferDataFlavors())) {
          try {
              source = (String)t.getTransferData(DataFlavor.stringFlavor);
              if (source.startsWith("memberPRsTable")) {
                removePRAction.actionPerformed(null);
                return true;
              } else {
                return false;
              }
          } catch (UnsupportedFlavorException ufe) { // just return false later
          } catch (IOException ioe) { // just return false later
          }
        }
        return false;
      }
    });
    loadedPRsTable.setDefaultRenderer(ProcessingResource.class,
                                      new ResourceRenderer());

    final int width1 = new JLabel("Loaded Processing resources").
                getPreferredSize().width + 30;
    JScrollPane scroller = new JScrollPane(){
      public Dimension getPreferredSize(){
        Dimension dim = super.getPreferredSize();
        dim.width = Math.max(dim.width, width1);
        return dim;
      }
      public Dimension getMinimumSize(){
        Dimension dim = super.getMinimumSize();
        dim.width = Math.max(dim.width, width1);
        return dim;
      }
    };
    scroller.getViewport().setView(loadedPRsTable);
    scroller.setBorder(BorderFactory.
                       createTitledBorder(BorderFactory.createEtchedBorder(),
                                          " Loaded Processing resources "));

    topBox.add(scroller);
    topBox.add(Box.createHorizontalGlue());

    addButton = new JButton(addPRAction);
    addButton.setText("");
    addButton.setEnabled(false);
    removeButton = new JButton(removePRAction);
    removeButton.setText("");
    removeButton.setEnabled(false);

    Box buttonsBox =Box.createVerticalBox();
    buttonsBox.add(Box.createVerticalGlue());
    buttonsBox.add(addButton);
    buttonsBox.add(Box.createVerticalStrut(5));
    buttonsBox.add(removeButton);
    buttonsBox.add(Box.createVerticalGlue());

    topBox.add(buttonsBox);
    topBox.add(Box.createHorizontalGlue());

    memberPRsTableModel = new MemberPRsTableModel();
    memberPRsTable = new XJTable();
    memberPRsTable.setSortable(false);
    memberPRsTable.setModel(memberPRsTableModel);
    memberPRsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    memberPRsTable.setDefaultRenderer(ProcessingResource.class,
                                      new ResourceRenderer());
    memberPRsTable.setDefaultRenderer(JLabel.class, new LabelRenderer());
    memberPRsTable.setDragEnabled(true);
    memberPRsTable.setTransferHandler(new TransferHandler() {
      // minimal drag and drop that only call the addPRAction when importing
      String source = "";
      public int getSourceActions(JComponent c) {
        return MOVE;
      }
      protected Transferable createTransferable(JComponent c) {
        int selectedRows[] = memberPRsTable.getSelectedRows();
        Arrays.sort(selectedRows);
        return new StringSelection("memberPRsTable"
          + Arrays.toString(selectedRows));
      }
      protected void exportDone(JComponent c, Transferable data, int action) {
      }
      public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for(DataFlavor flavor : flavors) {
          if(DataFlavor.stringFlavor.equals(flavor)) {
            return true;
          }
        }
        return false;
      }
      public boolean importData(JComponent c, Transferable t) {
        if (!canImport(c, t.getTransferDataFlavors())) {
          return false;
        }
        try {
          source = (String)t.getTransferData(DataFlavor.stringFlavor);
          if (source.startsWith("memberPRsTable")) {
            int insertion = memberPRsTable.getSelectedRow();
            int initialInsertion = insertion;
            List<ProcessingResource> prs = new ArrayList<ProcessingResource>();
            source = source.replaceFirst("^memberPRsTable\\[", "");
            source = source.replaceFirst("\\]$", "");
            String selectedRows[] = source.split(", ");
            if (Integer.valueOf(selectedRows[0]) < insertion) { insertion++; }
            // get the list of PRs selected when dragging started
            for(String row : selectedRows) {
              if (Integer.valueOf(row) == initialInsertion) {
                // the user draged the selected rows on themselves, do nothing
                return false;
              }
              prs.add((ProcessingResource) memberPRsTable.getValueAt(
                Integer.valueOf(row),
                memberPRsTable.convertColumnIndexToView(1)));
              if (Integer.valueOf(row) < initialInsertion) { insertion--; }
            }
            // remove the PRs selected when dragging started
            for (ProcessingResource pr : prs) {
              controller.remove(pr);
            }
            // add the PRs at the insertion point
            for (ProcessingResource pr : prs) {
              controller.add(insertion, pr);
              insertion++;
            }
            // select the moved PRs
            for (ProcessingResource pr : prs) {
              for (int row = 0; row < memberPRsTable.getRowCount(); row++) {
                if (memberPRsTable.getValueAt(row,
                      memberPRsTable.convertColumnIndexToView(1)) == pr) {
                  memberPRsTable.addRowSelectionInterval(row, row);
                }
              }
            }
            return true;
          } else if (source.equals("loadedPRsTable")) {
            addPRAction.actionPerformed(null);
            return true;
          } else {
            return false;
          }
        } catch (UnsupportedFlavorException ufe) {
          return false;
        } catch (IOException ioe) {
          return false;
        }
      }
    });

    final int width2 = new JLabel("Selected Processing resources").
                           getPreferredSize().width + 30;
    scroller = new JScrollPane(){
      public Dimension getPreferredSize(){
        Dimension dim = super.getPreferredSize();
        dim.width = Math.max(dim.width, width2);
        return dim;
      }
      public Dimension getMinimumSize(){
        Dimension dim = super.getMinimumSize();
        dim.width = Math.max(dim.width, width2);
        return dim;
      }      
    };
    scroller.getViewport().setView(memberPRsTable);
    scroller.setBorder(BorderFactory.
                       createTitledBorder(BorderFactory.createEtchedBorder(),
                                          " Selected Processing resources "));


    topBox.add(scroller);

    moveUpButton = new JButton(MainFrame.getIcon("up"));
    moveUpButton.setMnemonic(KeyEvent.VK_UP);
    moveUpButton.setToolTipText("Move the selected resources up.");
    moveUpButton.setEnabled(false);
    moveDownButton = new JButton(MainFrame.getIcon("down"));
    moveDownButton.setMnemonic(KeyEvent.VK_DOWN);
    moveDownButton.setToolTipText("Move the selected resources down.");
    moveDownButton.setEnabled(false);

    buttonsBox =Box.createVerticalBox();
    buttonsBox.add(Box.createVerticalGlue());
    buttonsBox.add(moveUpButton);
    buttonsBox.add(Box.createVerticalStrut(5));
    buttonsBox.add(moveDownButton);
    buttonsBox.add(Box.createVerticalGlue());

    topBox.add(buttonsBox);
    topBox.add(Box.createHorizontalGlue());

    add(topBox);

    if(conditionalMode){
      strategyPanel = new JPanel();
      strategyPanel.setLayout(new BoxLayout(strategyPanel, BoxLayout.X_AXIS));
      strategyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      runBtnGrp = new ButtonGroup();
      yes_RunRBtn = new JRadioButton("Yes", true);
      yes_RunRBtn.setHorizontalTextPosition(AbstractButton.LEFT);
      runBtnGrp.add(yes_RunRBtn);
      no_RunRBtn = new JRadioButton("No", false);
      no_RunRBtn.setHorizontalTextPosition(AbstractButton.LEFT);
      runBtnGrp.add(no_RunRBtn);
      conditional_RunRBtn = new JRadioButton("If value of feature", false);
      conditional_RunRBtn.setHorizontalTextPosition(AbstractButton.LEFT);
      runBtnGrp.add(conditional_RunRBtn);

      featureNameTextField = new JTextField("", 25);
      featureNameTextField.setMaximumSize(
                           new Dimension(Integer.MAX_VALUE,
                                         featureNameTextField.getPreferredSize().
                                         height));
      featureValueTextField = new JTextField("", 25);
      featureValueTextField.setMaximumSize(
                           new Dimension(Integer.MAX_VALUE,
                                         featureValueTextField.getPreferredSize().
                                         height));

      strategyPanel.add(new JLabel(MainFrame.getIcon("greenBall")));
      strategyPanel.add(yes_RunRBtn);
      strategyPanel.add(Box.createHorizontalStrut(5));

      strategyPanel.add(new JLabel(MainFrame.getIcon("redBall")));
      strategyPanel.add(no_RunRBtn);
      strategyPanel.add(Box.createHorizontalStrut(5));

      strategyPanel.add(new JLabel(MainFrame.getIcon("yellowBall")));
      strategyPanel.add(conditional_RunRBtn);
      strategyPanel.add(Box.createHorizontalStrut(5));

      strategyPanel.add(featureNameTextField);
      strategyPanel.add(Box.createHorizontalStrut(5));
      strategyPanel.add(new JLabel("is"));
      strategyPanel.add(Box.createHorizontalStrut(5));
      strategyPanel.add(featureValueTextField);
      strategyPanel.add(Box.createHorizontalStrut(5));
      strategyBorder = BorderFactory.createTitledBorder(
          BorderFactory.createEtchedBorder(),
          " No processing resource selected... ");
      strategyPanel.setBorder(strategyBorder);

      add(strategyPanel);
    }//if conditional mode
    if(analyserMode){
      //we need to add the corpus combo
      corpusCombo = new JComboBox(corpusComboModel = new CorporaComboModel());
      corpusCombo.setRenderer(new ResourceRenderer());
      corpusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                               corpusCombo.getPreferredSize().
                                               height));
      Corpus corpus = null;
      if(controller instanceof SerialAnalyserController){
        corpus = ((SerialAnalyserController)controller).getCorpus();
      }else if(controller instanceof ConditionalSerialAnalyserController){
        corpus = ((ConditionalSerialAnalyserController)controller).getCorpus();
      }else{
        throw new GateRuntimeException("Controller editor in analyser mode " +
                                       "but the target controller is not an " +
                                       "analyser!");
      }

      if(corpus != null){
        corpusCombo.setSelectedItem(corpus);
      }else{
        if(corpusCombo.getModel().getSize() > 1) corpusCombo.setSelectedIndex(1);
        else corpusCombo.setSelectedIndex(0);
      }
      JPanel horBox = new JPanel();
      horBox.setLayout(new BoxLayout(horBox, BoxLayout.X_AXIS));
      horBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      horBox.add(new JLabel("Corpus:"));
      horBox.add(Box.createHorizontalStrut(5));
      horBox.add(corpusCombo);
      horBox.add(Box.createHorizontalStrut(5));
      horBox.add(Box.createHorizontalGlue());
      add(horBox);
      JLabel warningLbl = new JLabel(
        "<HTML>The <b>corpus</b> and <b>document</b> parameters are not " +
        "available as they are automatically set by the controller!</HTML>");
      warningLbl.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
      add(warningLbl);
    }

    parametersPanel = new JPanel();
    parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
    parametersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    parametersBorder = BorderFactory.createTitledBorder(
                                      BorderFactory.createEtchedBorder(),
                                      " No selected processing resource ");
    parametersPanel.setBorder(parametersBorder);
    parametersEditor = new ResourceParametersEditor();
    parametersEditor.init(null, null);
    parametersPanel.add(new JScrollPane(parametersEditor));
    add(Box.createVerticalStrut(5));
    add(parametersPanel);


    add(Box.createVerticalStrut(5));
    add(Box.createVerticalGlue());
    JPanel horBox = new JPanel();
    horBox.setLayout(new BoxLayout(horBox, BoxLayout.X_AXIS));
    horBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    horBox.add(Box.createHorizontalGlue());
    horBox.add(new JButton(runAction));
    horBox.add(Box.createHorizontalGlue());
    add(horBox);
    add(Box.createVerticalGlue());
    add(Box.createVerticalStrut(5));

  }// initGuiComponents()

  protected void initListeners() {
    Gate.getCreoleRegister().addCreoleListener(this);

    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        processMouseEvent(e);
      }
      public void mouseReleased(MouseEvent e) {
        processMouseEvent(e);
      }
      protected void processMouseEvent(MouseEvent e) {
        if(e.isPopupTrigger()) {
          // context menu
          if(handle != null
          && handle.getPopup() != null) {
            handle.getPopup().show(SerialControllerEditor.this,
                                   e.getX(), e.getY());
          }
        }
      }
    });

    moveUpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = memberPRsTable.getSelectedRows();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Please select some components to be moved "+
              "from the list of used components!\n" ,
              "GATE", JOptionPane.ERROR_MESSAGE);
        } else {
          //we need to make sure the rows are sorted
          Arrays.sort(rows);
          //get the list of PRs
          for(int row : rows) {
            if(row > 0) {
              //move it up
              ProcessingResource value = controller.remove(row);
              controller.add(row - 1, value);
            }
          }
//          memberPRsTableModel.fireTableDataChanged();
          //restore selection
          for(int row : rows) {
            int newRow;
            if(row > 0) newRow = row - 1;
            else newRow = row;
            memberPRsTable.addRowSelectionInterval(newRow, newRow);
          }
          memberPRsTable.requestFocusInWindow();
        }

      }
    });


    moveDownButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = memberPRsTable.getSelectedRows();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Please select some components to be moved "+
              "from the list of used components!\n" ,
              "GATE", JOptionPane.ERROR_MESSAGE);
        } else {
          //we need to make sure the rows are sorted
          Arrays.sort(rows);
          //get the list of PRs
          for(int i = rows.length - 1; i >= 0; i--){
            int row = rows[i];
            if(row < controller.getPRs().size() -1){
              //move it down
              ProcessingResource value = controller.remove(row);
              controller.add(row + 1, value);
            }
          }
//          memberPRsTableModel.fireTableDataChanged();
          //restore selection
          for(int row : rows) {
            int newRow;
            if(row < controller.getPRs().size() - 1) newRow = row + 1;
            else newRow = row;
            memberPRsTable.addRowSelectionInterval(newRow, newRow);
          }
          memberPRsTable.requestFocusInWindow();
        }

      }
    });

    // mouse click edit the resource
    // mouse double click or context menu add the resource to the application
    loadedPRsTable.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(e.isPopupTrigger()) { processMouseEvent(e); }
      }
      public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger()) { processMouseEvent(e); }
      }
      public void mouseClicked(MouseEvent e) {
        processMouseEvent(e);
      }
      protected void processMouseEvent(MouseEvent e) {
        int row = loadedPRsTable.rowAtPoint(e.getPoint());
        if(row == -1) { return; }
        ProcessingResource pr = (ProcessingResource) loadedPRsTable
          .getValueAt(row, loadedPRsTable.convertColumnIndexToView(0));

        if(e.isPopupTrigger()) {
          // context menu
          if(!loadedPRsTable.isRowSelected(row)) {
            // if right click outside the selection then reset selection
            loadedPRsTable.getSelectionModel().setSelectionInterval(row, row);
          }
          JPopupMenu popup = new XJPopupMenu();
          popup.add(addPRAction);
          popup.show(loadedPRsTable, e.getPoint().x, e.getPoint().y);

        } else if(e.getID() == MouseEvent.MOUSE_CLICKED) {
          if (e.getClickCount() == 2) {
            //add selected modules on double click
            addPRAction.actionPerformed(null);

          } else if(e.getClickCount() == 1) {
            //edit parameters on one click
            showParamsEditor(pr);
          }
        }
      }
    });

    // mouse click edit the resource
    // mouse double click or context menu remove the resource from the
    // application
    memberPRsTable.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(e.isPopupTrigger()) { processMouseEvent(e); }
      }
      public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger()) { processMouseEvent(e); }
      }
      public void mouseClicked(MouseEvent e) {
        processMouseEvent(e);
      }
      protected void processMouseEvent(MouseEvent e) {
        int row = memberPRsTable.rowAtPoint(e.getPoint());
        if(row == -1) { return; }

        if(e.isPopupTrigger()) {
          // context menu
          if(!memberPRsTable.isRowSelected(row)) {
            // if right click outside the selection then reset selection
            memberPRsTable.getSelectionModel().setSelectionInterval(row, row);
          }
          JPopupMenu popup = new XJPopupMenu();
          popup.add(removePRAction);
          popup.show(memberPRsTable, e.getPoint().x, e.getPoint().y);

        } else if(e.getID() == MouseEvent.MOUSE_CLICKED) {
          if (e.getClickCount() == 2) {
            //remove selected modules on double click
            removePRAction.actionPerformed(null);

          } else if(e.getClickCount() == 1) {
            //edit parameters on one click
            selectPR(row);
          }
        }
      }
    });

    loadedPRsTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          // disable Add button if no selection
          addButton.setEnabled(loadedPRsTable.getSelectedRowCount() > 0);
        }
      });

    memberPRsTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          // disable Remove and Move buttons if no selection
          removeButton.setEnabled(memberPRsTable.getSelectedRowCount() > 0);
          moveUpButton.setEnabled(memberPRsTable.getSelectedRowCount() > 0
            && !memberPRsTable.isRowSelected(0));
          moveDownButton.setEnabled(memberPRsTable.getSelectedRowCount() > 0
            && !memberPRsTable.isRowSelected(memberPRsTable.getRowCount() - 1));
        }
      });

     loadedPRsTable.addKeyListener(new KeyAdapter() {
       public void keyPressed(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_SPACE) {
           ProcessingResource pr = (ProcessingResource) loadedPRsTable
             .getValueAt(loadedPRsTable.getSelectedRow(),
               loadedPRsTable.convertColumnIndexToView(0));
           showParamsEditor(pr);
         }
       }
     });

    memberPRsTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          selectPR(memberPRsTable.getSelectedRow());
        }
      }
    });

    if(conditionalMode){
      final ActionListener executionModeActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(selectedPRRunStrategy != null &&
             selectedPRRunStrategy instanceof AnalyserRunningStrategy){
            AnalyserRunningStrategy strategy =
              (AnalyserRunningStrategy)selectedPRRunStrategy;
            if(yes_RunRBtn.isSelected()){
              strategy.setRunMode(RunningStrategy.RUN_ALWAYS);
              featureNameTextField.setEditable(false);
              featureValueTextField.setEditable(false);
            }else if(no_RunRBtn.isSelected()){
              strategy.setRunMode(RunningStrategy.RUN_NEVER);
              featureNameTextField.setEditable(false);
              featureValueTextField.setEditable(false);
            }else if(conditional_RunRBtn.isSelected()){
              strategy.setRunMode(RunningStrategy.RUN_CONDITIONAL);
              featureNameTextField.setEditable(true);
              featureValueTextField.setEditable(true);

              String str = featureNameTextField.getText();
              strategy.setFeatureName(str == null || str.length()==0 ?
                                      null : str);
              str = featureValueTextField.getText();
              strategy.setFeatureValue(str == null || str.length()==0 ?
                                      null : str);
            }
          }
          memberPRsTable.repaint();
        }
      };

      yes_RunRBtn.addActionListener(executionModeActionListener);

      no_RunRBtn.addActionListener(executionModeActionListener);

      conditional_RunRBtn.addActionListener(executionModeActionListener);

      featureNameTextField.getDocument().addDocumentListener(
      new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        protected void changeOccured(javax.swing.event.DocumentEvent e){
          if(selectedPRRunStrategy != null &&
             selectedPRRunStrategy instanceof AnalyserRunningStrategy){
            AnalyserRunningStrategy strategy =
              (AnalyserRunningStrategy)selectedPRRunStrategy;
            strategy.setFeatureName(featureNameTextField.getText());
          }
        }
      });

      featureValueTextField.getDocument().addDocumentListener(
      new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured(e);
        }

        protected void changeOccured(javax.swing.event.DocumentEvent e){
          if(selectedPRRunStrategy != null &&
             selectedPRRunStrategy instanceof AnalyserRunningStrategy){
            AnalyserRunningStrategy strategy =
              (AnalyserRunningStrategy)selectedPRRunStrategy;
            strategy.setFeatureValue(featureValueTextField.getText());
          }
        }
      });
    }//if conditional
    if(analyserMode){
      corpusCombo.addPopupMenuListener(new PopupMenuListener() {
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                      corpusComboModel.fireDataChanged();
                    }

                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    }

                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                  });
    }

    // binds F3 key to the run action
    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
      .put(KeyStroke.getKeyStroke("F3"), "Run");
    getActionMap().put("Run", runAction);
  
  }//protected void initListeners()


  public List getActions(){
    return actionList;
  }

  /**
   * Cleans the internal data and prepares this object to be collected
   */
  public void cleanup(){
    Gate.getCreoleRegister().removeCreoleListener(this);
    controller.removeControllerListener(this);
    controller = null;
    progressListeners.clear();
    statusListeners.clear();
    parametersEditor.cleanup();
    handle = null;
  }

  /**
   * Called when a PR has been selected in the member PRs table;
   * @param index row index in {@link #memberPRsTable}
   */
  protected void selectPR(int index){
    ProcessingResource pr = (ProcessingResource)
                            ((java.util.List)controller.getPRs()).get(index);
    showParamsEditor(pr);
    selectedPR = pr;
    if(conditionalMode){
      strategyBorder.setTitle(" Run \"" + pr.getName() + "\"? ");
      //update the state of the run strategy buttons
      selectedPRRunStrategy = (RunningStrategy)
                                 ((List)((ConditionalController)controller).
                                          getRunningStrategies()).get(index);
      int runMode = selectedPRRunStrategy.getRunMode();

      if(selectedPRRunStrategy instanceof AnalyserRunningStrategy){
        yes_RunRBtn.setEnabled(true);
        no_RunRBtn.setEnabled(true);
        conditional_RunRBtn.setEnabled(true);

        featureNameTextField.setText(
              ((AnalyserRunningStrategy)selectedPRRunStrategy).
              getFeatureName());
        featureValueTextField.setText(
              ((AnalyserRunningStrategy)selectedPRRunStrategy).
              getFeatureValue());
      }else{
        yes_RunRBtn.setEnabled(false);
        no_RunRBtn.setEnabled(false);
        conditional_RunRBtn.setEnabled(false);

        featureNameTextField.setText("");
        featureValueTextField.setText("");
      }

      featureNameTextField.setEditable(false);
      featureValueTextField.setEditable(false);

      switch(selectedPRRunStrategy.getRunMode()){
        case RunningStrategy.RUN_ALWAYS:{
          yes_RunRBtn.setSelected(true);
          break;
        }

        case RunningStrategy.RUN_NEVER:{
          no_RunRBtn.setSelected(true);
          break;
        }

        case RunningStrategy.RUN_CONDITIONAL:{
          conditional_RunRBtn.setSelected(true);
          if(selectedPRRunStrategy instanceof AnalyserRunningStrategy){
            featureNameTextField.setEditable(true);
            featureValueTextField.setEditable(true);
          }
          break;
        }
      }//switch
    }
  }

  /**
   * Stops the current edits for parameters; sets the parameters for the
   * resource currently being edited and displays the editor for the new
   * resource
   * @param pr the new resource
   */
  protected void showParamsEditor(ProcessingResource pr){
    try{
      if(parametersEditor.getResource() != null) parametersEditor.setParameters();
    }catch(ResourceInstantiationException rie){
      JOptionPane.showMessageDialog(
          SerialControllerEditor.this,
          "Failed to set parameters for \"" + pr.getName() +"\"!\n" ,
          "GATE", JOptionPane.ERROR_MESSAGE);
      rie.printStackTrace(Err.getPrintWriter());
    }

    if(pr != null){
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                         get(pr.getClass().getName());

      parametersBorder.setTitle(" Parameters for the \"" + pr.getName() +
                                "\" " + rData.getName() + " ");

      //this is a list of lists
      List<List<Parameter>> parameters =
        rData.getParameterList().getRuntimeParameters();

      if(analyserMode){
        //remove corpus and document
        //create a new list so we don't change the one from CreoleReg.
        List<List<Parameter>> newParameters = new ArrayList<List<Parameter>>();
        for(List<Parameter> aDisjunction : parameters) {
          List<Parameter> newDisjunction = new ArrayList<Parameter>();
          for(Parameter parameter : aDisjunction) {
            if(!parameter.getName().equals("corpus")
            && !parameter.getName().equals("document")) {
              newDisjunction.add(parameter);
            }
          }
          if(!newDisjunction.isEmpty()) newParameters.add(newDisjunction);
        }
        parametersEditor.init(pr, newParameters);
      }else{
        parametersEditor.init(pr, parameters);
      }
    }else{
      parametersBorder.setTitle("No selected processing resource");
      parametersEditor.init(null, null);
    }
    SerialControllerEditor.this.validate();
    SerialControllerEditor.this.repaint(100);
  }

  //CreoleListener implementation
  public void resourceLoaded(CreoleEvent e) {
    if(Gate.getHiddenAttribute(e.getResource().getFeatures())) return;
    if(e.getResource() instanceof ProcessingResource){
      loadedPRsTableModel.fireTableDataChanged();
      memberPRsTableModel.fireTableDataChanged();
//      repaint(100);
    }else if(e.getResource() instanceof LanguageResource){
      if(e.getResource() instanceof Corpus && analyserMode){
        corpusComboModel.fireDataChanged();
      }
    }
  }

  public void resourceUnloaded(CreoleEvent e) {
    if(Gate.getHiddenAttribute(e.getResource().getFeatures())) return;
    if(e.getResource() instanceof ProcessingResource){
      ProcessingResource pr = (ProcessingResource)e.getResource();
      if(controller.getPRs().contains(pr)){
        controller.remove(pr);
      }
      loadedPRsTableModel.fireTableDataChanged();
      memberPRsTableModel.fireTableDataChanged();
//      repaint(100);
    }
    else if(e.getResource() instanceof LanguageResource) {
      if(e.getResource() instanceof Corpus && analyserMode) {
        Corpus c = (Corpus)e.getResource();
        if(controller instanceof CorpusController) {
          if(c == ((CorpusController)controller).getCorpus()) {
            // setCorpus(null) is also called in the controller's
            // resourceUnloaded(), but we can't be sure which handler is
            // called first...
            ((CorpusController)controller).setCorpus(null);
          }
        }
        else {
          throw new GateRuntimeException("Controller editor in analyser mode " +
                                         "but the target controller is not an " +
                                         "analyser!");
        }
        corpusComboModel.fireDataChanged();
      }
    }
  }

  public void resourceRenamed(Resource resource, String oldName,
                              String newName){
    if(Gate.getHiddenAttribute(resource.getFeatures())) return;
    if(resource instanceof ProcessingResource){
      repaint(100);
    }
  }

  public void datastoreOpened(CreoleEvent e) {
  }
  public void datastoreCreated(CreoleEvent e) {
  }
  public void datastoreClosed(CreoleEvent e) {
  }
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector<StatusListener> v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }
  
  /* (non-Javadoc)
   * @see gate.event.ControllerListener#resourceAdded(gate.event.ControllerEvent)
   */
  public void resourceAdded(ControllerEvent evt){
    loadedPRsTableModel.fireTableDataChanged();
    memberPRsTableModel.fireTableDataChanged();

  }
  
  /* (non-Javadoc)
   * @see gate.event.ControllerListener#resourceRemoved(gate.event.ControllerEvent)
   */
  public void resourceRemoved(ControllerEvent evt){
    loadedPRsTableModel.fireTableDataChanged();
    memberPRsTableModel.fireTableDataChanged();
  }
  
  
  
  public synchronized void addStatusListener(StatusListener l) {
    Vector<StatusListener> v = statusListeners == null ?
      new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }



  /**
   * Table model for all the loaded processing resources that are not part of
   * the controller.
   */
  class LoadedPRsTableModel extends AbstractTableModel{
    public int getRowCount(){
      List<ProcessingResource> loadedPRs = new ArrayList<ProcessingResource>(
        Gate.getCreoleRegister().getPrInstances());
      if(controller != null) loadedPRs.removeAll(controller.getPRs());
      Iterator prsIter = loadedPRs.iterator();
      while(prsIter.hasNext()){
        ProcessingResource aPR = (ProcessingResource)prsIter.next();
        if(Gate.getHiddenAttribute(aPR.getFeatures())) prsIter.remove();
      }

      return loadedPRs.size();
    }

    public Object getValueAt(int row, int column){
      List<ProcessingResource> loadedPRs = new ArrayList<ProcessingResource>(
        Gate.getCreoleRegister().getPrInstances());
      if(controller != null) loadedPRs.removeAll(controller.getPRs());
      Iterator prsIter = loadedPRs.iterator();
      while(prsIter.hasNext()){
        ProcessingResource aPR = (ProcessingResource)prsIter.next();
        if(Gate.getHiddenAttribute(aPR.getFeatures())) prsIter.remove();
      }

      Collections.sort(loadedPRs, nameComparator);
      ProcessingResource pr = loadedPRs.get(row);
      switch(column){
        case 0 : return pr;
        case 1 : {
          ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                    get(pr.getClass().getName());
          if(rData == null) return pr.getClass();
          else return rData.getName();
        }
        default: return null;
      }
    }

    public int getColumnCount(){
      return 2;
    }

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0 : return "Name";
        case 1 : return "Type";
        default: return "?";
      }
    }

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0 : return ProcessingResource.class;
        case 1 : return String.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return false;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
    }
    NameComparator nameComparator = new NameComparator();
  }//protected class LoadedPRsTableModel extends AbstractTableModel

  /**
   * A model for a combobox containing the loaded corpora in the system
   */
  protected class CorporaComboModel extends AbstractListModel
                                  implements ComboBoxModel{
    public int getSize(){
      //get all corpora regardless of their actual type
      java.util.List loadedCorpora = null;
      try{
        loadedCorpora = Gate.getCreoleRegister().
                               getAllInstances("gate.Corpus");
      }catch(GateException ge){
        ge.printStackTrace(Err.getPrintWriter());
      }

      return loadedCorpora == null ? 1 : loadedCorpora.size() + 1;
    }

    public Object getElementAt(int index){
      if(index == 0) return "<none>";
      else{
        //get all corpora regardless of their actual type
        java.util.List loadedCorpora = null;
        try{
          loadedCorpora = Gate.getCreoleRegister().
                                 getAllInstances("gate.Corpus");
        }catch(GateException ge){
          ge.printStackTrace(Err.getPrintWriter());
        }
        return loadedCorpora == null? "" : loadedCorpora.get(index - 1);
      }
    }

    //use the controller for data caching
    public void setSelectedItem(Object anItem){
      if(controller instanceof SerialAnalyserController)
      ((SerialAnalyserController)controller).
        setCorpus((Corpus)(anItem.equals("<none>") ? null : anItem));
      else if(controller instanceof ConditionalSerialAnalyserController)
      ((ConditionalSerialAnalyserController)controller).
        setCorpus((Corpus)(anItem.equals("<none>") ? null : anItem));
    }

    public Object getSelectedItem(){
      Corpus corpus = null;
      if(controller instanceof SerialAnalyserController){
        corpus = ((SerialAnalyserController)controller).getCorpus();
      }else if(controller instanceof ConditionalSerialAnalyserController){
        corpus = ((ConditionalSerialAnalyserController)controller).getCorpus();
      }else{
        throw new GateRuntimeException("Controller editor in analyser mode " +
                                       "but the target controller is not an " +
                                       "analyser!");
      }
      return (corpus == null ? (Object)"<none>" : (Object)corpus);
    }

    void fireDataChanged(){
      fireContentsChanged(this, 0, getSize());
    }
  }

  /**
   *  Renders JLabel by simply displaying them
   */
  class LabelRenderer implements TableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){
      return (JLabel) value;
    }
  }

  /**
   * Table model for all the processing resources in the controller.
   */
  class MemberPRsTableModel extends AbstractTableModel{
    MemberPRsTableModel(){
      green = new JLabel(MainFrame.getIcon("greenBall"));
      red = new JLabel(MainFrame.getIcon("redBall"));
      yellow = new JLabel(MainFrame.getIcon("yellowBall"));
    }
    public int getRowCount(){
      return controller == null ? 0 : controller.getPRs().size();
    }

    public Object getValueAt(int row, int column){
      ProcessingResource pr = (ProcessingResource)
                              ((List)controller.getPRs()).get(row);
      switch(column){
        case 0 : {
          if(conditionalMode){
            RunningStrategy strategy = (RunningStrategy)
                                 ((List)((ConditionalController)controller).
                                          getRunningStrategies()).get(row);
            switch(strategy.getRunMode()){
              case RunningStrategy.RUN_ALWAYS : return green;
              case RunningStrategy.RUN_NEVER : return red;
              case RunningStrategy.RUN_CONDITIONAL : return yellow;
            }
          }
          return green;
        }
        case 1 : return pr;
        case 2 : {
          ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                    get(pr.getClass().getName());
          if(rData == null) return pr.getClass();
          else return rData.getName();
        }
        default: return null;
      }
    }

    public int getColumnCount(){
      return 3;
    }

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0 : return "!";
        case 1 : return "Name";
//        case 1 : return "!";
        case 2 : return "Type";
        default: return "?";
      }
    }

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0 : return JLabel.class;
        case 1 : return ProcessingResource.class;
//        case 1 : return Boolean.class;
        case 2 : return String.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return false;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
    }

    protected JLabel green, red, yellow;
  }//protected class MemberPRsTableModel extends AbstractTableModel

  /** Adds a PR to the controller*/
  class AddPRAction extends AbstractAction {
    AddPRAction(){
      putValue(NAME, "Add selected resources to the application");
      putValue(SHORT_DESCRIPTION, "Add selected resources to the application");
      putValue(SMALL_ICON, MainFrame.getIcon("right-arrow"));
      putValue(MNEMONIC_KEY, KeyEvent.VK_RIGHT);
    }

    public void actionPerformed(ActionEvent e){
      List<ProcessingResource> prs = new ArrayList<ProcessingResource>();
      int selectedRows[] = loadedPRsTable.getSelectedRows();
      Arrays.sort(selectedRows);
      for (int row : selectedRows) {
        prs.add((ProcessingResource) loadedPRsTable
          .getValueAt(row, loadedPRsTable.convertColumnIndexToView(0)));
      }
      int insertion = memberPRsTable.getSelectedRow();
      if (insertion == -1) { insertion = memberPRsTable.getRowCount(); }
      for (ProcessingResource pr : prs) {
        controller.add(insertion, pr);
        insertion++;
      }
      // transfer the selection
      for (ProcessingResource pr : prs) {
        for (int row = 0; row < memberPRsTable.getRowCount(); row++) {
          if (memberPRsTable.getValueAt(row,
                memberPRsTable.convertColumnIndexToView(1)) == pr) {
            memberPRsTable.addRowSelectionInterval(row, row);
          }
        }
      }
      memberPRsTable.requestFocusInWindow();
    }
  }

  /** Removes a PR from the controller*/
  class RemovePRAction extends AbstractAction {
    RemovePRAction(){
      putValue(NAME, "Remove selected resouces from the application");
      putValue(SHORT_DESCRIPTION,
        "Remove selected resouces from the application");
      putValue(SMALL_ICON, MainFrame.getIcon("left-arrow"));
      putValue(MNEMONIC_KEY, KeyEvent.VK_LEFT);
    }

    public void actionPerformed(ActionEvent e){
      List<ProcessingResource> prs = new ArrayList<ProcessingResource>();
      for (int row : memberPRsTable.getSelectedRows()) {
        prs.add((ProcessingResource) memberPRsTable
          .getValueAt(row, memberPRsTable.convertColumnIndexToView(1)));
      }
      for (ProcessingResource pr : prs) {
        controller.remove(pr);
      }
      // transfer the selection
      for (ProcessingResource pr : prs) {
        for (int row = 0; row < loadedPRsTable.getRowCount(); row++) {
          if (loadedPRsTable.getValueAt(row,
                loadedPRsTable.convertColumnIndexToView(0)) == pr) {
            loadedPRsTable.addRowSelectionInterval(row, row);
          }
        }
      }
      loadedPRsTable.requestFocusInWindow();
      if (memberPRsTable.getSelectedRowCount() == 0) {
        parametersEditor.init(null, null);
        parametersBorder.setTitle("No selected processing resource");
      }
    }
  }


  /** Runs the Application*/
  class RunAction extends AbstractAction {
    RunAction(){
      super("Run this application");
      super.putValue(SHORT_DESCRIPTION, "<html>Run this application"
      +"&nbsp;&nbsp;<font color=#667799><small>F3"
      +"&nbsp;&nbsp;</small></font></html>");
    }

    public void actionPerformed(ActionEvent e){
      Runnable runnable = new Runnable(){
        public void run(){

          if (memberPRsTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(SerialControllerEditor.this,
              "Add at least one processing resource in the right table\n"
              +"that contains the resources of the application to be run.",
              "GATE", JOptionPane.ERROR_MESSAGE);
            return;
          }

          //stop editing the parameters
          try{
            parametersEditor.setParameters();
          }catch(ResourceInstantiationException rie){
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Could not set parameters for the \"" +
              parametersEditor.getResource().getName() +
              "\" processing resource:\nSee \"Messages\" tab for details!",
              "GATE", JOptionPane.ERROR_MESSAGE);
              rie.printStackTrace(Err.getPrintWriter());
              return;
          }

          if(analyserMode){
            //set the corpus
            Object value = corpusCombo.getSelectedItem();
            Corpus corpus = value.equals("<none>") ? null : (Corpus)value;
            if(corpus == null){
              JOptionPane.showMessageDialog(
                SerialControllerEditor.this,
                "No corpus provided!\n" +
                "Please select a corpus and try again!",
                "GATE", JOptionPane.ERROR_MESSAGE);
              corpusCombo.requestFocusInWindow();
              return;
            }
            if(controller instanceof CorpusController)
              ((CorpusController)controller).setCorpus(corpus);
          }
          //check the runtime parameters
          List badPRs;
          try{
            badPRs = controller.getOffendingPocessingResources();
          }catch(ResourceInstantiationException rie){
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Could not check runtime parameters for " +
              "the processing resources:\n" + rie.toString(),
              "GATE", JOptionPane.ERROR_MESSAGE);
            return;
          }
          if(badPRs != null && !badPRs.isEmpty()){
            String badPRsString = "";
            for (Object badPR : badPRs) {
              badPRsString += "- "
                + ((ProcessingResource) badPR).getName() + "\n";
            }
            //we know what PRs have problems so it would be nice to show
            //them in red or something
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Some required runtime parameters are not set\n"+
              "in the following resources:\n"+
              badPRsString,
              "GATE", JOptionPane.ERROR_MESSAGE);
            return;
          }

          //set the listeners
          StatusListener sListener = new InternalStatusListener();
          ProgressListener pListener = new InternalProgressListener();

          controller.addStatusListener(sListener);
          controller.addProgressListener(pListener);

          Gate.setExecutable(controller);

          MainFrame.lockGUI("Running " + controller.getName() + "...");
          //execute the thing
          long startTime = System.currentTimeMillis();
          fireStatusChanged("Running " +
                            controller.getName());
          fireProgressChanged(0);

          try {
            Benchmark.executeWithBenchmarking(controller, controller.getName(),
                    RunAction.this, null);
          }catch(ExecutionInterruptedException eie){
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Interrupted!\n" + eie.toString(),
              "GATE", JOptionPane.ERROR_MESSAGE);
          }catch(ExecutionException ee) {
            ee.printStackTrace(Err.getPrintWriter());
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(
              SerialControllerEditor.this,
              "Execution error while running \"" + controller.getName() +
              "\" :\nSee \"Messages\" tab for details!",
              "GATE", JOptionPane.ERROR_MESSAGE);
          }catch(Exception e){
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(SerialControllerEditor.this,
                                          "Unhandled execution error!\n " +
                                          "See \"Messages\" tab for details!",
                                          "GATE", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(Err.getPrintWriter());
          }finally{
            MainFrame.unlockGUI();
            Gate.setExecutable(null);
          }//catch

          //remove the listeners
          controller.removeStatusListener(sListener);
          controller.removeProgressListener(pListener);

          long endTime = System.currentTimeMillis();
          fireProcessFinished();
          fireStatusChanged(controller.getName() +
                            " run in " +
                            NumberFormat.getInstance().format(
                            (double)(endTime - startTime) / 1000) + " seconds");
        }
      };
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable,
                                 "ApplicationViewer1");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }//public void actionPerformed(ActionEvent e)
  }//class RunAction

  /**
   * A simple progress listener used to forward the events upstream.
   */
  protected class InternalProgressListener implements ProgressListener{
    public void progressChanged(int i){
      fireProgressChanged(i);
    }

    public void processFinished(){
      fireProcessFinished();
    }
  }//InternalProgressListener

  /**
   * A simple status listener used to forward the events upstream.
   */
  protected class InternalStatusListener implements StatusListener{
    public void statusChanged(String message){
      fireStatusChanged(message);
    }
  }//InternalStatusListener



  /** The controller this editor edits */
  protected SerialController controller;

  /** The {@link Handle} that created this view */
  protected Handle handle;

  /**
   * The list of actions provided by this editor
   */
  protected List<Action> actionList;
  /**
   * Contains all the PRs loaded in the sytem that are not already part of the
   * serial controller
   */
  protected XJTable loadedPRsTable;

  /**
   * model for the {@link #loadedPRsTable} JTable.
   */
  protected LoadedPRsTableModel loadedPRsTableModel;

  /**
   * Displays the PRs in the controller
   */
  protected XJTable memberPRsTable;

  /** model for {@link #memberPRsTable}*/
  protected MemberPRsTableModel memberPRsTableModel;

  /** Adds one or more PR(s) to the controller*/
  protected JButton addButton;

  /** Removes one or more PR(s) from the controller*/
  protected JButton removeButton;

  /** Moves the module up in the controller list*/
  protected JButton moveUpButton;

  /** Moves the module down in the controller list*/
  protected JButton moveDownButton;

  /** A component for editing the parameters of the currently selected PR*/
  protected ResourceParametersEditor parametersEditor;

  /** A JPanel containing the {@link #parametersEditor}*/
  protected JPanel parametersPanel;

  /** A border for the {@link #parametersPanel} */
  protected TitledBorder parametersBorder;


  /** A JPanel containing the running strategy options*/
  protected JPanel strategyPanel;

  /** A border for the running strategy options box */
  protected TitledBorder strategyBorder;

  /**
   * Button for run always.
   */
  protected JRadioButton yes_RunRBtn;

  /**
   * Button for never run.
   */
  protected JRadioButton no_RunRBtn;

  /**
   * Button for conditional run.
   */
  protected JRadioButton conditional_RunRBtn;

  /**
   * The group for run strategy buttons;
   */
  protected ButtonGroup runBtnGrp;

  /**
   * Text field for the feature name for conditional run.
   */
  protected JTextField featureNameTextField;

  /**
   * Text field for the feature value for conditional run.
   */
  protected JTextField featureValueTextField;

  /**
   * A combobox that allows selection of a corpus from the list of loaded
   * corpora.
   */
  protected JComboBox corpusCombo;

  protected CorporaComboModel corpusComboModel;

  /** Action that runs the application*/
  protected RunAction runAction;

  /**
   * Is the controller displayed an analyser controller?
   */
  protected boolean analyserMode = false;

  /**
   * Is the controller displayed conditional?
   */
  protected boolean conditionalMode = false;

  /**
   * The PR currently selected (having its parameters set)
   */
  protected ProcessingResource selectedPR = null;

  /**
   * The running strategy for the selected PR.
   */
  protected RunningStrategy selectedPRRunStrategy = null;

  private transient Vector<StatusListener> statusListeners;
  private transient Vector<ProgressListener> progressListeners;

  private AddPRAction addPRAction;
  private RemovePRAction removePRAction;


  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector<StatusListener> listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        listeners.elementAt(i).statusChanged(e);
      }
    }
  }
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector<ProgressListener> v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }
  public synchronized void addProgressListener(ProgressListener l) {
    Vector<ProgressListener> v = progressListeners == null ?
      new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }
  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector<ProgressListener> listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        listeners.elementAt(i).progressChanged(e);
      }
    }
  }
  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector<ProgressListener> listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        listeners.elementAt(i).processFinished();
      }
    }
  }

}
