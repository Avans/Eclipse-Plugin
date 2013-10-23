package nl.avans.plugin.value;

import nl.avans.plugin.column.ColumnStep;
import nl.avans.plugin.column.ColumnStep.DisplayMode;
import nl.avans.plugin.column.ColumnStep.State;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class BooleanValue extends Value {
	boolean value;
	
	public BooleanValue(boolean value) {
		this.value = value;
	}
	
	@Override
	public void paint(GC gc, ColumnStep.DisplayMode displayMode, Value maximalValue, State executionState, int x, int y, int width,
			int height) {
		if(displayMode == ColumnStep.DisplayMode.FULL) {
			paintText(value ? "true" : "false", gc, executionState, x, y, width, height);			
		} else if(displayMode == ColumnStep.DisplayMode.TRUNCATED) {
			paintText(value ? "T" : "F", gc, executionState, x, y, width, height);
		} else {
			paintDefault(gc, executionState, x, y, width, height);
		}
	}

	@Override
	public DisplayMode getPreferredDisplayMode(GC gc, int width) {
		if(gc.stringExtent(value ? "true" : "false").x <= width) {
			return ColumnStep.DisplayMode.FULL;
		} else if(gc.stringExtent(value ? "T" : "F").x <= width) {
			return ColumnStep.DisplayMode.TRUNCATED;
		} else {
			return ColumnStep.DisplayMode.DOT;
		}
	}
}
