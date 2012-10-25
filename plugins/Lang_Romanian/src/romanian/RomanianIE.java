package romanian;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.net.URL;
import java.util.List;

@CreoleResource(name = "Romanian IE System", autoinstances = @AutoInstance)
public class RomanianIE extends PackagedController {

  @Override
  @CreoleParameter(defaultValue = "resources/romanian.gapp")
  public void setPipelineURL(URL url) {
    this.url = url;
  }

  @Override
  @CreoleParameter(defaultValue = "Romanian")
  public void setMenu(List<String> menu) {
    super.setMenu(menu);
  }
}
