package nl.avans.plugin.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

import nl.avans.plugin.value.IntValue;
import nl.avans.plugin.value.Value;

public class Variable {
	private String name;
	private Value value;
	
	public Variable(String name, Value value) {
		this.name = name;
		this.value = value;
	}

	public Variable(IVariable variable) throws DebugException {
		this.name = variable.getName();
		this.value = new IntValue(7);//variable.getValue();
	}

	public String getName() {
		return this.name;
	}
	
	public Value getValue() {
		return value;
	}

}
