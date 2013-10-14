package nl.avans.plugin.value;

import nl.avans.plugin.column.ColumnStep.State;

import org.eclipse.swt.graphics.GC;

public class StringValue extends Value {
	String value;
	
	public StringValue(String value) {
		this.value = value;
	}
	
	@Override
	public void paint(GC gc, Value maximalValue, State executionState, int x, int y, int width,
			int height) {
		paintText('"' + value + '"', gc, executionState, x, y, width, height);
	}
}
