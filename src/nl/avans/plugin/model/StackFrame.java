package nl.avans.plugin.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;

public class StackFrame implements Iterable<Variable> {
	String methodName;
	List<Variable> variables = new ArrayList<Variable>();
	
	public StackFrame(IStackFrame stackframe) throws DebugException {
		methodName = stackframe.getName();
		for(IVariable variable : stackframe.getVariables()) {
			variables.add(new Variable(variable));
		}
		// TODO Auto-generated constructor stub
	}

	public void addVariable(Variable foo) {
		variables.add(foo);
	}

	@Override
	public Iterator<Variable> iterator() {
		return variables.iterator();
	}
}
