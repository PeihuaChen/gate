package gate.gui;

import gate.creole.*;

import javax.swing.*;
//import java.awt.bo

public class ApplicationViewer extends AbstractVisualResource {

  public ApplicationViewer() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }

  protected void initListeners(){
  }


}