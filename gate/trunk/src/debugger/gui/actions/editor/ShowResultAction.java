package debugger.gui.actions.editor;

import debugger.gui.GuiFactory;
import debugger.gui.MainFrame;
import debugger.gui.actions.resources.LrResourceSelectedAction;
import debugger.resources.ResourcesFactory;
import gate.Document;
import gate.LanguageResource;
import gate.corpora.DocumentImpl;
import gate.util.InvalidOffsetException;

import javax.swing.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko
 */

public class ShowResultAction {
    private static ShowResultAction ourInstance;

    public synchronized static ShowResultAction getInstance() {
        if (ourInstance == null) {
            ourInstance = new ShowResultAction();
        }
        return ourInstance;
    }

    private ShowResultAction() {
    }

    public void actionPerformed(int startOffset, int endOffset) {
        LanguageResource lr = ResourcesFactory.getCurrentLrModel().getLr();
        if (lr instanceof Document) {
            //check if document content changed
            if(!ResourcesFactory.getCurrentLrModel().getStoredContent().equals(((Document)lr).getContent().toString())) {
                JOptionPane.showMessageDialog(
                        JOptionPane.getRootFrame(),
                        "Document content changed.\n" +
                        "Synchronization of rule matching history with\n" +
                        "changing document content not supported.\n" +
                        "You should rerun pipeline again.",
                        "Warning!",
                        JOptionPane.PLAIN_MESSAGE);
                ResourcesFactory.getCurrentLrModel().synchronize();
                LrResourceSelectedAction.getInstance().actionPerformed(ResourcesFactory.getCurrentLrModel());
                return;
            }

            int startSelection = startOffset + getSlashNAmount(startOffset, (DocumentImpl) lr);
            int endSelection = endOffset + getSlashNAmount(endOffset, (DocumentImpl) lr);
            GuiFactory.getDebugPanel().getTraceHistoryPanel().setText(startSelection, endSelection, (Document) lr);
            GuiFactory.getResourceView().getResourceTreeCellRenderer().setIndexes(startSelection, endSelection);
            GuiFactory.getResourceView().repaint();
//            try {
//                System.out.println(((DocumentImpl) lr).getContent().getContent(new Long(startSelection), new Long(endSelection)).toString());
//            } catch (InvalidOffsetException e) {
//                e.printStackTrace();
//            }
            GuiFactory.getResourceView().repaint(); //to highlight rules
        }
    }

    private int getSlashNAmount(int offset, DocumentImpl document) {
        int result = 0;
        try {
            String temp = document.getContent().getContent(new Long(0), new Long(offset)).toString();

            char[] charArray = temp.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (c == '\n') result++;
            }
//            if(temp.equals("")) return 0;
//            StringTokenizer st = new StringTokenizer(temp, "\n");
//            if(temp.endsWith("\n"))
//            {
//                return st.countTokens();
//            }
//            else
//            {
//                return st.countTokens() - 1;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

