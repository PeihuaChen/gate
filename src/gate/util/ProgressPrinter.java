package gate.util;

import java.io.*;

import gate.gui.*;


  public class ProgressPrinter implements ProgressListener{
    public ProgressPrinter(PrintStream out, int numberOfSteps){
      this.out = out;
      this.numberOfSteps = numberOfSteps;
    }

    public ProgressPrinter(PrintStream out){
      this.out = out;
    }

    public void processFinished(){
      for(int i = currentValue; i < numberOfSteps; i++){
        out.print("#");
      }
      out.println("]");
      currentValue = 0;
      started = false;
    }

    public void progressChanged(int newValue){
      if(!started){
        out.print("[");
        started = true;
      }
      newValue = newValue * numberOfSteps / 100;
      if(newValue > currentValue){
        for(int i = currentValue; i < newValue; i++){
          out.print("#");
        }
        currentValue = newValue;
      }
    }


    int currentValue = 0;
    int numberOfSteps = 70;
    PrintStream out;
    boolean started = false;
  }

