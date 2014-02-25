package gate.termraider;

import gate.creole.PackagedController;
import gate.creole.metadata.*;

@CreoleResource(name = "TermRaider English Term Extraction",
    icon = "TermRaiderApp",
    autoinstances = @AutoInstance(parameters = {
        @AutoInstanceParam(name="pipelineURL", value="applications/termraider-eng.gapp"),
        @AutoInstanceParam(name="menu", value="TermRaider")}))
public class TermRaiderEnglish extends PackagedController {
  private static final long serialVersionUID = -1599367292323903155L;
}
