package gate.creole.tokeniser;

import gate.util.*;

/**The top level exception for all the exceptions fired by the tokeniser*/
public class TokeniserException extends GateException {
  public TokeniserException(String text){ super(text);}
} 
