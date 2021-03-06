package nl.avans.plugin.column;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.Value;

public class ColumnStep {
	public enum State {
		EXECUTED(new Color(null, 140, 140, 140)),
		CURRENT(new Color(null, 250, 140, 140)),
		NON_EXECUTED(new Color(null, 220, 220, 220));
		
		public final Color color;
		State(Color color) {
			this.color = color;
		}
	}
	
	public enum DisplayMode {
		DOT(0),
		TRUNCATED(1),
		FULL(2);
		
		public final int priority;
		DisplayMode(int priority) {
			this.priority = priority;
		}
	}
	
	public int index;
	
	// The line that this step is on (0-indexed)
	public int line;
	
	// Position and size on the line in the ruler
	public int x, width;
	
	public Value value;

	public List<StepLine> stepLines = new ArrayList<StepLine>();

	public void paint(GC gc, DisplayMode displayMode, Value maximalValue, int linePixel, int lineHeight, State executionState) {
		value.paint(gc, displayMode, maximalValue, executionState, x, linePixel, width, lineHeight);
	}

	public boolean isHovering(int x_coordinate) {
		return x <= x_coordinate && x_coordinate < x + width;
	}
	
	private DisplayMode cachedDisplayMode;
	
	public DisplayMode getDisplayMode(GC gc) {
		if(cachedDisplayMode == null) {
			cachedDisplayMode = this.value.getPreferredDisplayMode(gc, width); 
		}
		return cachedDisplayMode;
	}
}
