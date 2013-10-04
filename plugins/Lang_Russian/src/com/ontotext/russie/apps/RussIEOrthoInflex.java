package com.ontotext.russie.apps;

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.AutoInstanceParam;
import gate.creole.metadata.CreoleResource;

@CreoleResource(name = "RussIE + Inflectional Gazetteer & OrthoMatcher", icon = "Russian", autoinstances = @AutoInstance(parameters = {
  @AutoInstanceParam(name = "pipelineURL", value = "resources/RussIE_ortho_inflex.xgapp"),
  @AutoInstanceParam(name = "menu", value = "Russian")}))
public class RussIEOrthoInflex extends PackagedController {

}