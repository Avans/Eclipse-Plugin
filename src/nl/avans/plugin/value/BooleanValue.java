package nl.avans.plugin.value;

import nl.avans.plugin.column.ColumnStep.State;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class BooleanValue extends Value {
	boolean value;
	
	public BooleanValue(boolean value) {
		this.value = value;
	}
	
	@Override
	public void paint(GC gc, State executionState, int x, int y, int width,
			int height) {
		String text = value ? "true" : "false";
		
		if(gc.stringExtent(text).x <= width) {
			paintText(text, gc, executionState, x, y, width, height);
		} else {
			paintText(value ? "T" : "F", gc, executionState, x, y, width, height);
		}
	}
}
