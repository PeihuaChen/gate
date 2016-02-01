package danish;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.AutoInstanceParam;
import gate.creole.metadata.CreoleResource;

@CreoleResource(name = "Danish IE System",
    comment = "Ready-made Danish IE application",
    autoinstances = @AutoInstance(parameters = {
	@AutoInstanceParam(name="pipelineURL", value="resources/dkie.xgapp"),
	@AutoInstanceParam(name="menu", value="Danish")}))
public class DanishIE extends PackagedController {

  private static final long serialVersionUID = -6224055187863557225L;

}
