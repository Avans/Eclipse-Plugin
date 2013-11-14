package nl.avans.plugin.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StackFrame implements Iterable<Variable> {
	String methodName;
	List<Variable> variables = new ArrayList<Variable>();
	
	public void addVariable(Variable foo) {
		variables.add(foo);
	}

	@Override
	public Iterator<Variable> iterator() {
		return variables.iterator();
	}
}
