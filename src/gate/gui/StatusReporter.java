package gate.gui;

public interface StatusReporter {
  public void addStatusListener(StatusListener listener);
  public void removeStatusListener(StatusListener listener);
}
