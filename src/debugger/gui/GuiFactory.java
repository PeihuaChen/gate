package debugger.gui;

import debugger.gui.debugging.MainDebugPanel;
import debugger.gui.editor.DocumentEditor;
import debugger.gui.resources.ResourceTree;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko
 */

public class GuiFactory {
	private static final boolean DEBUG = false;
	
    private static debugger.gui.editor.DocumentEditor documentEditor;
    private static MainDebugPanel debugPanel;
    private static ResourceTree resourceView;

    public static ResourceTree getResourceView() {
        if (resourceView == null) {
        	if (DEBUG)
        		System.out.println("Initializing resources...");

        	long i = System.currentTimeMillis();
            resourceView = new debugger.gui.resources.ResourceTree();
            long j = System.currentTimeMillis();
            if (DEBUG)
            	System.out.println("Time: " + (j - i));
        }
        return resourceView;
    }

    public static DocumentEditor getDocumentEditor() {
        if (documentEditor == null) {
        	if (DEBUG)
        		System.out.println("Initializing editor...");

        	long i = System.currentTimeMillis();
            documentEditor = new DocumentEditor();
            long j = System.currentTimeMillis();
            if (DEBUG)
            	System.out.println("Time: " + (j - i));
        }
        return documentEditor;
    }

    public static MainDebugPanel getDebugPanel() {
        if (debugPanel == null) {
        	if (DEBUG)
        		System.out.println("Initializing debugging...");
        	
            long i = System.currentTimeMillis();
            debugPanel = new MainDebugPanel();
            long j = System.currentTimeMillis();
            if (DEBUG)
            	System.out.println("Time: " + (j - i));
        }
        return debugPanel;
    }
}
