package cebuano;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.net.URL;
import java.util.List;

@CreoleResource(name = "Cebuano IE System", autoinstances = @AutoInstance)
public class CebuanoIE extends PackagedController {

  @Override
  @CreoleParameter(defaultValue = "resources/cebuano.gapp")
  public void setPipelineURL(URL url) {
    this.url = url;
  }

  @Override
  @CreoleParameter(defaultValue = "Cebuano")
  public void setMenu(List<String> menu) {
    super.setMenu(menu);
  }
}
