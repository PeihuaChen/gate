package debugger.gui.actions.debugging;

import debugger.ClassRipper;
import debugger.JapeDebugger;
import gate.Gate;
import gate.creole.ExecutionException;
import gate.creole.ExecutionInterruptedException;
import gate.creole.SerialAnalyserController;
import gate.gui.SerialControllerEditor;

import javax.swing.*;
import java.util.Iterator;
import java.awt.event.ActionEvent;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin
 */
public class RunControllerAction extends AbstractAction {
    private static RunControllerAction ourInstance;

    public synchronized static RunControllerAction getInstance() {
        if (ourInstance == null) {
            ourInstance = new RunControllerAction();
        }
        return ourInstance;
    }

    private RunControllerAction() {
        super();
        putValue(Action.SHORT_DESCRIPTION, new String("Run controller"));
        putValue(Action.SMALL_ICON, new ImageIcon(JapeDebugger.class.getResource("gui/icons/controller")));
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent aEvt) {
        setEnabled(false);
        for (Iterator itr = Gate.getCreoleRegister().getVrInstances().iterator(); itr.hasNext();) {
            Object current = itr.next();
            if (current instanceof SerialControllerEditor) {
                SerialAnalyserController controller = null;
                try {
                    controller = (SerialAnalyserController) ClassRipper.getFieldValue(current, "controller");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (controller != null) {
                    final SerialAnalyserController controller1 = controller;
                    Runnable runnable = new Runnable() {
                        public void run() {
                            try {
                                Gate.setExecutable(controller1);
                                controller1.execute();
                            } catch (ExecutionInterruptedException eie) {
                                eie.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            } finally {
                                Gate.setExecutable(null);
                            }
                        }
                    };
                    Thread thread = new Thread(Thread.currentThread().getThreadGroup(), runnable, "ApplicationViewer1");
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.start();
                }
                break;
            }
        }
    }
}

