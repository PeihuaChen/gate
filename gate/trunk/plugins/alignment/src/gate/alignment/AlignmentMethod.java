package gate.alignment;

import java.util.Map;

import gate.compound.CompoundDocument;

public interface AlignmentMethod {

  public void execute(CompoundDocument compoundDocument,
          String alignmentFeatureName, Map parameters)
          throws AlignmentException;
}
