package nl.avans.plugin.column;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import nl.avans.plugin.ui.stepline.StepLine;

public class ColumnStep {
	public enum State {
		EXECUTED(new Color(null, 170, 170, 170)),
		CURRENT(new Color(null, 250, 0, 0)),
		NON_EXECUTED(new Color(null, 200, 200, 200));
		
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

	List<StepLine> stepLines = new ArrayList<StepLine>();

	public void paint(GC gc, int linePixel, int lineHeight, State executionState) {
		gc.setBackground(executionState.color);
		int margin = 1;
		gc.fillOval(0, linePixel + margin, lineHeight - margin * 2,
				lineHeight - margin * 2);
	}
}
