package nl.avans.plugin.model;

import nl.avans.plugin.value.Value;

public class Variable {
	private String name;
	private Value value;
	
	public Variable(String name, Value value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return this.name;
	}
	
	public Value getValue() {
		return value;
	}

}
