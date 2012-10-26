package german;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.net.URL;
import java.util.List;

@CreoleResource(name = "German IE System", autoinstances = @AutoInstance)
public class GermanIE extends PackagedController {

  @Override
  @CreoleParameter(defaultValue = "resources/german.gapp")
  public void setPipelineURL(URL url) {
    this.url = url;
  }

  @Override
  @CreoleParameter(defaultValue = "German")
  public void setMenu(List<String> menu) {
    super.setMenu(menu);
  }
}
