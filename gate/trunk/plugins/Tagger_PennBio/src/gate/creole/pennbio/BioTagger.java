package gate.creole.pennbio;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.net.URL;
import java.util.List;

@CreoleResource(name = "Penn BioTagger", autoinstances = @AutoInstance, icon="bio")
public class BioTagger extends PackagedController {

  @Override
  @CreoleParameter(defaultValue = "resources/biotagger.xgapp")
  public void setPipelineURL(URL url) {
    this.url = url;
  }

  @Override
  @CreoleParameter(defaultValue = "Biomedical")
  public void setMenu(List<String> menu) {
    super.setMenu(menu);
  }
}
