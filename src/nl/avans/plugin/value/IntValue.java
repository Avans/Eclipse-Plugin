package nl.avans.plugin.value;

import nl.avans.plugin.column.ColumnStep.State;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class IntValue extends Value {

	int value;
	public IntValue(int value) {
		this.value = value;
	}

	@Override
	public void paint(GC gc, State executionState, int x, int y, int width,
			int height) {
		paintText(value+"", gc, executionState, x, y, width, height);
	}
}
