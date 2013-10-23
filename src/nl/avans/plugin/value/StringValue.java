package nl.avans.plugin.value;

import java.awt.DisplayMode;

import nl.avans.plugin.column.ColumnStep;
import nl.avans.plugin.column.ColumnStep.State;

import org.eclipse.swt.graphics.GC;

public class StringValue extends Value {
	String value;
	
	public StringValue(String value) {
		this.value = value;
	}
	
	@Override
	protected String getText() {
		return "\"" + value + "\"";
	}
}
