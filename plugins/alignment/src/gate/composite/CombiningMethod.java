package gate.composite;

import java.io.Serializable;
import java.util.Map;

import gate.compound.CompoundDocument;

/**
 * This interface declares a method that needs to be implemented by the
 * implementors of this interface. The purpose is to allow users to combine
 * various documents into a single document called Composite document.
 * 
 * @author niraj
 */
public interface CombiningMethod extends Serializable {

	/**
	 * This method takes an instance of compound document and returns an
	 * instance of composite document
	 * 
	 * @param compoundDocument
	 * @return
	 */
	public CompositeDocument combine(CompoundDocument compoundDocument,
			Map parameters) throws CombiningMethodException;

}
