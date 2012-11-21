// Warning: this has to alter the inputAS; the outputAS is ignored.
// $Id$

Set<String> termTypes = new HashSet<String>();
termTypes.add("Entity");
termTypes.add("Verb");
termTypes.add("VG");
termTypes.add("SingleWord");
termTypes.add("MultiWord");

String defaultLanguage = null;
if ((scriptParams != null) && scriptParams.containsKey("defaultLanguage")) {
    defaultLanguage = scriptParams.get("defaultLanguage").toString();
}

AnnotationSet candidates = inputAS.get(termTypes);
for (Annotation candidate : candidates) {
  AnnotationSet sentences = gate.Utils.getCoveringAnnotations(inputAS, candidate, "Sentence");
  if (sentences != null) {
    for (Annotation sentence : sentences) {
      if (sentence.getFeatures().containsKey("lang")) {
        String language = sentence.getFeatures().get("lang").toString();
        candidate.getFeatures().put("lang", language);
      }
      else if (defaultLanguage != null) {
        candidate.getFeatures().put("lang", defaultLanguage);
      }
    }
  }
}
