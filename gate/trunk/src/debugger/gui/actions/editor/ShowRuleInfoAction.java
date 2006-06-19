/**
 * @author Andrey Shafirin, Oleg Mishchenko
 *
 */
package debugger.gui.actions.editor;

import debugger.resources.pr.RuleModel;
import debugger.resources.ResourcesFactory;
import debugger.resources.PhaseController;
import debugger.resources.JapeFile;
import debugger.gui.GuiFactory;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.awt.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import gate.AnnotationSet;
import gate.jape.SinglePhaseTransducer;
import gate.util.Files;

public class ShowRuleInfoAction
{
    private static ShowRuleInfoAction ourInstance;

    public synchronized static ShowRuleInfoAction getInstance()
    {
        if(ourInstance == null)
        {
            ourInstance = new ShowRuleInfoAction();
        }
        return ourInstance;
    }

    private ShowRuleInfoAction()
    {
    }

    public void actionPerformed(RuleModel ruleModel)
    {
//        this.currentRuleModel = currentRuleModel;
//        DebugController.getInstance().getPrController().setCurrentPR((PRModelImpl) this.currentRuleModel.getParent().getParent());
        //debug

        int startSelection = 0;
        int endSelection = 0;
        Iterator it = ruleModel.getBindings().keySet().iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            if (s.startsWith("(")) {
                AnnotationSet as = (AnnotationSet) ruleModel.getBindings().get(s);
                if (as != null)
                    try {
                        startSelection = ((AnnotationSet) ruleModel.getBindings().get(s)).firstNode().getOffset().intValue();
                        endSelection = ((AnnotationSet) ruleModel.getBindings().get(s)).lastNode().getOffset().intValue();
//System.out.println("rule "+currentRuleModel.getName()+" checked! "+startSelection+" "+endSelection);
//                        startSelection = startSelection - getSlashNAmount(startSelection, as);
//                        endSelection = endSelection - getSlashNAmount(endSelection, as);
//                        DebugController.getInstance().getLRController().setSelected(startSelection, endSelection);
                        GuiFactory.getDocumentEditor().setTextSelection(Color.cyan, startSelection, endSelection);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                else
                    GuiFactory.getDocumentEditor().setTextSelection(Color.cyan, 0, 0);
//                    DebugController.getInstance().getLRController().setSelected(0, 0);
            }
        }
        ResourcesFactory.setCurrentJapeFile(getJapeFile(ruleModel));
        ResourcesFactory.setCurrentJapeText(getJapeText());
        /////////////////////////////////////
        GuiFactory.getDebugPanel().getRulePanel().ruleSelected();
        GuiFactory.getDebugPanel().getJapeSourcePanel().upgradeTextPane();
        /////////////////////////////////////
        GuiFactory.getDebugPanel().getTraceHistoryPanel().setCurrentRule(ruleModel);
        GuiFactory.getDebugPanel().getTraceHistoryPanel().updateRulePanel(ruleModel, null);
//        this.ruleView.ruleSelected();
//        getJapeSourceView().upgradeTextPane();
//        ((JapeSourceView)((JapePanel)((MainPanel)(((DebugView)DebugController.getInstance().getDebugView()).getMainPanel())).getJapePanel()).getJapeSourceTab()).upgradeTextPane();
//        ((JapeSourceView) this.japeSourceView).upgradeTextPane();
        //ruleModel.getRuleText();
    }

    private int getSlashNAmount(int offset, AnnotationSet as) {
        try {
            String temp = as.getDocument().getContent().getContent(new Long(0), new Long(offset)).toString();
            if (temp.equals("")) return 0;
            StringTokenizer st = new StringTokenizer(temp, "\n");
            if (temp.endsWith("\n")) {
                return st.countTokens();
            } else {
                return st.countTokens() - 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //private File getJapeFile(RuleModel ruleModel)
    private JapeFile getJapeFile(RuleModel ruleModel)
    {
        SinglePhaseTransducer spt = ruleModel.getParentPhase().getSPT();
        //File result;
        JapeFile result;
        if (spt == null)
        {
            return null;
        }
        URL japeURL = spt.getBaseURL();
        if(!japeURL.toString().startsWith("file"))
        {
           //japeURL = PhaseController.class.getResource(Files.getResourcePath() + japeURL.getPath());
          japeURL = Files.getGateResource(japeURL.getPath());
        }
        if(japeURL == null)
        {
           System.out.println("Not valid SPT BaseURL (" + spt.getBaseURL()+")");
           return null;
        }
        //result = new File(japeURL.getFile());
        result = new JapeFile(japeURL);
        return result;
    }

    public String getJapeText()
    {
        //File f = ResourcesFactory.getCurrentJapeFile();
        JapeFile f = ResourcesFactory.getCurrentJapeFile();
        if (f == null) return "";
//        if (lastPhase4Editor.equals(((PhaseModelIfc) currentRuleModel.getParent()).getName())) return lastJapeText;
        StringBuffer result = new StringBuffer("");
        String chunk = "";
        try {
            //BufferedReader br = new BufferedReader(new FileReader(f));
            BufferedReader br = new BufferedReader(f.getReader());
            while ((chunk = br.readLine()) != null) {
                result.append(chunk).append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("result = " + result);
//        lastPhase4Editor = ((PhaseModelIfc) currentRuleModel.getParent()).getName();
//        lastJapeText = result.toString();
        return result.toString();

    }
}

