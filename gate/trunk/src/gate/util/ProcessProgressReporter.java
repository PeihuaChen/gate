package gate.util;

/** This interface describes a processing resource that can report on the
  * progress of its processing activity.
  * It is useful for implementing progress bars and for waiting on precssing
  * resources that use theur own thread for processing purposes.
  */
public interface ProcessProgressReporter {
  public void addProcessProgressListener(ProgressListener listener);
  public void removeProcessProgressListener(ProgressListener listener);

}
