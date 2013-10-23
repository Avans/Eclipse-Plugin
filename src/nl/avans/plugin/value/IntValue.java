package nl.avans.plugin.value;

import nl.avans.plugin.column.ColumnStep;
import nl.avans.plugin.column.ColumnStep.DisplayMode;
import nl.avans.plugin.column.ColumnStep.State;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class IntValue extends Value {

	int value;
	public IntValue(int value) {
		this.value = value;
	}

	@Override
	public void paint(GC gc, ColumnStep.DisplayMode displayMode, Value maximalValue, State executionState, int x, int y, int width,
			int height) {
		gc.setFont(FONT);
		
		if(displayMode == ColumnStep.DisplayMode.FULL) {
			paintText(value+"", gc, executionState, x, y, width, height);
		} else {
			if(maximalValue instanceof IntValue) {
				int maximum = ((IntValue)maximalValue).value;
				int barHeight = (int)(value / (double)maximum * height);
				if(barHeight < 1)
					barHeight = 1;
				
				gc.setBackground(executionState.color);
				gc.fillRectangle(x, y + height - barHeight, width, barHeight);
			} else {
				paintDefault(gc, executionState, x, y, width > 2 ? width - 1 : width, height);
			}
		}
	}
	
	@Override
	public DisplayMode getPreferredDisplayMode(GC gc, int width) {
		if(gc.stringExtent(value+"").x <= width) {
			return ColumnStep.DisplayMode.FULL;
		} else {
			return ColumnStep.DisplayMode.TRUNCATED;
		}
	}
	
	@Override
	public int compareTo(Value value) {
		if(value instanceof IntValue) {
			return this.value - ((IntValue)value).value;
		}
		return super.compareTo(value);
	}
}
