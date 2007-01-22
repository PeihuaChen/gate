package gate.util.web;

import java.util.HashMap;
import java.util.Iterator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagHighlighter {

    private HashMap tagColors;

    public TagHighlighter () {
        tagColors = new HashMap();
        tagColors.put("Person", "#FFA0FF");
        tagColors.put("Location", "#A0FFFF");
        tagColors.put("Organization", "#FFFFA0");
    }

    public void colorTag(String tag, String color) {
        tagColors.put(tag, color);
    }

    public String getColor(String tag) {
        return (String) tagColors.get(tag);
    }

    public String highlightText(String text) {
        Iterator tags = tagColors.keySet().iterator();
        while (tags.hasNext()) {
            String tag = (String) tags.next();
            String color = (String) tagColors.get(tag);
            Pattern pattern = Pattern.compile("(<" + tag + " .*?>)");
            String toAppend = "<B style=\"color:black;background-color:" + color + "\">";
            
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()){
              String group = matcher.group(1);
              text = text.replaceAll(group,toAppend + group);
            }
            
            String closing = "(</" + tag + ">)";
            String closingReplacement = "</" + tag + "></B>";
            text = text.replaceAll(closing,closingReplacement);
        }

        return text;
    }
}
