package debugger;

import debugger.gui.MainFrame;

import java.awt.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * Contains helper static methods to access private class fields
 * @author Oleg Mishchenko
 */

public class JapeDebugger {
    private static final boolean DEBUG = false;
	
	static MainFrame frame;

    public JapeDebugger() {
        JapeDebugger.main(null);
    }

    public static void main(String[] args) {
    	if (DEBUG)
    		System.out.println("Before creating MainFrame");
    	
        frame = new MainFrame();
        Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        int screenWidth = toolkit.getScreenSize().width;
        int screenHeight = toolkit.getScreenSize().height;

        frame.setSize(screenWidth - 100, screenHeight - 100);

        int framePositionX = (screenWidth - frame.getWidth()) / 2;
        int framePositionY = (screenHeight - frame.getHeight()) / 2;

        frame.setLocation(framePositionX, framePositionY);
        frame.setVisible(true);
    }

    public static MainFrame getMainFrame() {
        return frame;
    }
}