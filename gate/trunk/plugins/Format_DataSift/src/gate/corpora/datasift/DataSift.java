package gate.corpora.datasift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSift {
  private Interaction interaction;
  
  public Interaction getInteraction() {
    return interaction;
  }
  
  public void setInteraction(Interaction interaction) {
    this.interaction = interaction;
  }
}
