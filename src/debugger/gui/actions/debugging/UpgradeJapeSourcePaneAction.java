package debugger.gui.actions.debugging;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko
 */

public class UpgradeJapeSourcePaneAction {
    private static UpgradeJapeSourcePaneAction ourInstance;

    public synchronized static UpgradeJapeSourcePaneAction getInstance() {
        if (ourInstance == null) {
            ourInstance = new UpgradeJapeSourcePaneAction();
        }
        return ourInstance;
    }

    private UpgradeJapeSourcePaneAction() {
    }
}

