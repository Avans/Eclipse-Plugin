package nl.avans.plugin.column;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import nl.avans.plugin.ui.stepline.StepLine;

public class ColumnStep {
	public enum State {
		EXECUTED(new Color(null, 170, 170, 170)),
		CURRENT(new Color(null, 250, 140, 140)),
		NON_EXECUTED(new Color(null, 220, 220, 220));
		
		public final Color color;
		State(Color color) {
			this.color = color;
		}
	}
	
	public int index;
	public ColumnStep afterColumnStep;
	
	// The line that this step is on
	public int line;
	
	// Position and size on the line
	public int x, width;

	public List<StepLine> stepLines = new ArrayList<StepLine>();

	public void paint(GC gc, int linePixel, int lineHeight, State executionState) {
		gc.setBackground(executionState.color);
		int margin = 2;
		if(executionState == State.CURRENT)
			margin = 0;
		
		gc.fillOval(margin, linePixel + margin, lineHeight - margin * 2,
				lineHeight - margin * 2);
	}

	public boolean isHovering(int x_coordinate) {
		return x < x_coordinate && x_coordinate < x + width;
	}
}
