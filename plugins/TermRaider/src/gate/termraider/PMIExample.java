package gate.termraider;

import gate.creole.PackagedController;
import gate.creole.metadata.*;

@CreoleResource(name = "PMI Example (English)",
    icon = "TermRaiderApp",
    autoinstances = @AutoInstance(parameters = {
        @AutoInstanceParam(name="pipelineURL", value="applications/pmi-example.gapp"),
        @AutoInstanceParam(name="menu", value="TermRaider")}))
public class PMIExample extends PackagedController {
  private static final long serialVersionUID = -4725697168124226331L;
}
