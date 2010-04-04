package gate.groovy;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import gate.Gate;
import gate.GateConstants;
import gate.Resource;
import gate.creole.AbstractResource;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleResource;
import gate.gui.ActionsPublisher;
import gate.gui.MainFrame;
import gate.util.GateException;
import gate.util.GateRuntimeException;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

@CreoleResource(name = "Groovy support for GATE", isPrivate = true, tool = true,
        autoinstances = @AutoInstance)
public class GroovySupport extends AbstractResource implements ActionsPublisher {
  /**
   * Standard list of import statements that are available to any groovy script
   * or console in GATE. These are the same imports available by default on the
   * right-hand-side of JAPE rules.
   */
  public static final String STANDARD_IMPORTS =
      "import gate.*;\n" +
      "import gate.jape.*;\n" +
      "import gate.creole.ontology.*;\n" +
      "import gate.annotation.*;\n" +
      "import gate.util.*;\n";

  public Resource init() {
    // mix-in gate.Utils
    Class<gate.Utils> utilsClass = gate.Utils.class;
    // find the set of types into which it needs to be mixed.
    // this means the types of the first argument of each
    // static method in the class.
    Set<Class<?>> typesToMixInto = new HashSet<Class<?>>();
    for(Method method : utilsClass.getMethods()) {
      if(Modifier.isStatic(method.getModifiers())
              && method.getParameterTypes().length > 0) {
        typesToMixInto.add(method.getParameterTypes()[0]);
      }
    }
    for(Class<?> clazz : typesToMixInto) {
      DefaultGroovyMethods.mixin(clazz, utilsClass);
    }
    
    return this;
  }
  
  private List<Action> actions;
  
  public List getActions() {
    if(actions == null) {
      actions = new ArrayList<Action>();
      actions.add(new AbstractAction("Groovy Console", MainFrame.getIcon("groovyConsole")) {
        {
          putValue(SHORT_DESCRIPTION, "Console for Groovy scripting");
          putValue(GateConstants.MENU_PATH_KEY, new String[] {"Groovy Tools"});
        }
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent evt) {
          groovy.ui.Console console = new groovy.ui.Console(Gate.getClassLoader()) {

            /**
             * Overridden to (a) install a Binding that knows about the
             * corpora, docs, prs and apps variables, and (b) set up the
             * standard imports each time a script is run.
             */
            @Override
            public void newScript(ClassLoader parent, Binding binding) {
              // TODO Auto-generated method stub
              Binding realBinding = new GateBinding(binding);
              setShell(new GroovyShell(parent, realBinding) {
                public Object run(final String scriptText,
                        final String fileName, String[] args) {
                  return super.run(scriptText + "\n\n\n" + STANDARD_IMPORTS,
                          fileName, args);
                }
              });
            }
          };
          console.run();
        }
      });
    }
    return actions;
  }
  
  /**
   * Special Binding subclass that handles requests for the variables
   * corpora, docs, prs and apps by delegating to the creole register.
   */
  static class GateBinding extends Binding {
    GateBinding(Binding delegateBinding) {
      super(delegateBinding.getVariables());
    }
    
    public Object getVariable(String name) {
      if("corpora".equals(name)) {
        return Gate.getCreoleRegister()
          .getLrInstances("gate.corpora.CorpusImpl");
      }
      else if("docs".equals(name)) {
        return Gate.getCreoleRegister()
          .getLrInstances("gate.corpora.DocumentImpl");
      }
      else if("prs".equals(name)) {
        return Gate.getCreoleRegister().getPrInstances();
      }
      else if("apps".equals(name)) {
        try {
          return Gate.getCreoleRegister().getAllInstances(
            "gate.creole.AbstractController");
        }
        catch(GateException e) {
          throw new GateRuntimeException(e);
        }
      }
      else {
        return super.getVariable(name);
      }
    }
  }

}
