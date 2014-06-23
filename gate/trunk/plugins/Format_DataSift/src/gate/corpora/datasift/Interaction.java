package gate.corpora.datasift;

import gate.Factory;
import gate.FeatureMap;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Interaction {
  private String content, type, link, id, created;
  
  private Map<String,Object> author;
  
  public String getContent() {
    return content;
  }
  
  public void setContent(String content) {
    this.content = content;
  }
  
  @JsonProperty("id")
  public String getID() {
    return id;
  }
  
  public void setID(String id) {
    this.id = id;
  }
    
  @JsonProperty("created_at")
  public String getCreatedAt() {
    return created;
  }
  
  public void setCreatedAt(String created) {
    this.created = created;
  }
    
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public Map<String,Object> getAuthor() {
    return author;
  }
  
  public void setAuthor(Map<String,Object> author) {
    this.author = author;
  }
  
  public String getLink() {
    return link;
  }
  
  public void setLink(String link) {
    this.link = link;
  }
  
  public FeatureMap asFeatureMap() {
    FeatureMap features = Factory.newFeatureMap();
    
    if (type != null && !type.trim().equals("")) features.put("type", type);
    if (link != null && !link.trim().equals("")) features.put("link", link);
    if (id != null && !id.trim().equals("")) features.put("id", id);
    if (created != null && !created.trim().equals("")) features.put("created_at", created);
    
    if (author != null) {
      for(Map.Entry<String,Object> data : author.entrySet()) {
        features.put("author_"+data.getKey(), data.getValue());
      }
    }
    
    return features;
  }
}

