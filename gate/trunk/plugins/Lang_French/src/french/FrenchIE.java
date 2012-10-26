package french;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.net.URL;
import java.util.List;

@CreoleResource(name = "French IE System", autoinstances = @AutoInstance)
public class FrenchIE extends PackagedController {

  @Override
  @CreoleParameter(defaultValue = "french.gapp")
  public void setPipelineURL(URL url) {
    this.url = url;
  }

  @Override
  @CreoleParameter(defaultValue = "French")
  public void setMenu(List<String> menu) {
    super.setMenu(menu);
  }
}
