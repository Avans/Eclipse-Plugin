package nl.avans.plugin.debug;

import java.util.HashSet;
import java.util.Set;

import nl.avans.plugin.model.Stack;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public class State {

	Set<String> variables = new HashSet<String>();
	
	Stack stack;

	public State(IStackFrame[] iStackFrames)
			throws DebugException {
		this.stack = new Stack(iStackFrames);
	}

	public Stack getStack() {
		return stack;
	}

}
