package nl.avans.plugin.debug;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public class State {

	Set<String> variables = new HashSet<String>();

	public State(IJavaStackFrame stackframe, State previousState)
			throws DebugException {
		// Dummy recording, just record some variables names
		for (IVariable variable : stackframe.getVariables()) {
			variables.add(variable.getName());
		}
	}

}
