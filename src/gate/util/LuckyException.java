package gate.util;

public class LuckyException extends RuntimeException {

  public LuckyException() {
    super(defaultMessage);
  }

  public LuckyException(String message) {
    super(message + "\n" + defaultMessage);
  }

  static String defaultMessage =
    "Congratulations, you found the ONLY bug in GATE!";
} 