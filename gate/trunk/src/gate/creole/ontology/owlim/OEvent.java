package gate.creole.ontology.owlim;

import gate.util.GateRuntimeException;

public class OEvent {
  public OEvent(String subject, String predicate, String object, boolean toAdd) {
    this.subject = subject == null ? "*" : subject;
    this.predicate = predicate == null ? "*" : predicate;
    this.object = object == null ? "*" : object;
    this.toAdd = toAdd;
  }

  public OEvent(String subject, String predicate, String object,
          String datatype, boolean toAdd) {
    this(subject, predicate, object, toAdd);
    this.datatype = datatype == null ? "*" : datatype;
  }

  public static OEvent parseEvent(String eventDesc) {
    // the first character is either - or +
    char c = eventDesc.charAt(0);
    boolean add = c == '-' ? false : true;
    String neventDesc = eventDesc.substring(3, eventDesc.length()-1);
    // each string is delimited with "> <"
    String parts[] = neventDesc.split("> <");

    if(parts.length == 3) {
      return new OEvent(removeEscapeChar(parts[0]), removeEscapeChar(parts[1]),
              removeEscapeChar(parts[2]), add);
    }
    else if(parts.length == 4) {
      return new OEvent(removeEscapeChar(parts[0]), removeEscapeChar(parts[1]),
              removeEscapeChar(parts[2]), removeEscapeChar(parts[3]), add);
    }
    else {
//      System.out.println("subject :"+parts[0]);
//      System.out.println("predicate :"+parts[1]);
//      System.out.println("object :"+parts[2]);
//      if(parts.length == 4) {
//        System.out.println("datatype : "+parts[3]);
//      }
      throw new GateRuntimeException("Invalid event description " + eventDesc);
    }
  }

  private static String removeEscapeChar(String string) {
    String toReturn = "";
    if(string.equals("*")) return string;
    string = string.substring(0, string.length());
    for(int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if(c == '\\') {
        // check if the next character is ", < or >
        if(i + 1 < string.length()) {
          char ch1 = string.charAt(i + 1);
          if(ch1 == '"' || ch1 == '<' || ch1 == '>') {
            toReturn += ch1;
            i++;
            continue;
          }
        }
      }
      toReturn += c;
    }
    return toReturn;
  }

  public String toString() {
    // lets replace any " with \", new line with space
    String subject1 = "<" + getEscapedString(subject) + ">";
    String predicate1 = "<" + getEscapedString(predicate) + ">";
    String object1 = "<" + getEscapedString(object) + ">";
    String datatype1 = datatype == null ? null : "<"
            + getEscapedString(datatype) + ">";
    
    return (toAdd ? "+" : "-") + " " + subject1 + " " + predicate1 + " " + object1
            + (datatype1 == null ? "" : " " + datatype1);
  }

  private String getEscapedString(String string) {
    String toReturn = "";
    for(char c : string.toCharArray()) {
      if(c == '"') {
        toReturn += "\\\"";
      }
      else if(c == '<') {
        toReturn += "\\<";
      }
      else if(c == '>') {
        toReturn += "\\>";
      }
      else if(c == '\n') {
        toReturn += " ";
      }
      else {
        toReturn += c + "";
      }
    }
    return toReturn;
  }

  private String subject;

  private String predicate;

  private String object;

  private String datatype;

  private boolean toAdd;

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getPredicate() {
    return predicate;
  }

  public void setPredicate(String predicate) {
    this.predicate = predicate;
  }

  public String getObject() {
    return object;
  }

  public void setObject(String object) {
    this.object = object;
  }

  public String getDatatype() {
    return datatype;
  }

  public void setDatatype(String datatype) {
    this.datatype = datatype;
  }

  public boolean getToAdd() {
    return toAdd;
  }

  public void setToAdd(boolean toAdd) {
    this.toAdd = toAdd;
  }

}
