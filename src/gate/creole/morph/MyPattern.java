package gate.creole.morph;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Niraj Aswani
 * @version 1.0
 */

public class MyPattern implements Comparable {

	private String methodName;
	private Matcher matcher;
	private String[] parameters;
	
	public MyPattern(Pattern pattern, String function) {
		this.matcher = pattern.matcher("");
		methodName = getMethodName(function).intern();
		parameters = getParameterValues(function);
	}

	/**
	 * This method is used to find the method definition But it can recognize
	 * only String, boolean and int types for Example: stem(2,"ed","d") ==>
	 * stem(int,java.lang.String,java.lang.String);
	 * 
	 * @param method
	 * @return the definition of the method
	 */
	private String getMethodName(String method) {
		// find the first index of '('
		int index = method.indexOf('(');
		String methodName = method.substring(0, index) + "(";

		// now get the parameter types
		String[] parameters = method.substring(index + 1, method.length() - 1)
				.split(",");

		// find the approapriate type
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].startsWith("\"") && parameters[i].endsWith("\"")) {
				methodName = methodName + "java.lang.String";
			} else if (ParsingFunctions.isBoolean(parameters[i])) {
				methodName = methodName + "boolean";
			} else if (ParsingFunctions.isInteger(parameters[i])) {
				methodName = methodName + "int";
			}
			if ((i + 1) < parameters.length) {
				methodName = methodName + ",";
			}
		}
		methodName = methodName + ")";
		return methodName;
	}

	/**
	 * This method finds the actual parameter values
	 * 
	 * @param method
	 *            from which parameters are required to be found
	 * @return parameter values
	 */
	private String[] getParameterValues(String method) {
		// now first find the name of the method
		// their parameters and their types
		int index = method.indexOf("(");

		// now get the parameters
		String[] parameters = method.substring(index + 1, method.length() - 1)
				.split(",");

		// process each parameter
		for (int i = 0; i < parameters.length; i++) {
			// we need to remove " from String
			if (parameters[i].startsWith("\"") && parameters[i].endsWith("\"")) {
				parameters[i] = parameters[i].substring(1, parameters[i]
						.length() - 1).intern();
				continue;
			}
		}
		return parameters;
	}

	public int compareTo(Object obj) {
		// this is always to be added at the end
		return 1;
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}
}