package gate.gui;

/** This interface describes objects that can register themselves as listeners
  * to ProcessProgressReporters.
  * They need to be able to handle progress change and process finished events.
  */
public interface ProgressListener {

  public void progressChanged(int i);

  public void processFinished();

}
