package nl.avans.plugin.value;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import nl.avans.plugin.column.ValueDrawer;
import nl.avans.plugin.column.ColumnStep.State;

public class Value implements ValueDrawer {

	protected static final Font FONT = new Font(null, "Arial", 12, SWT.NORMAL);

	@Override
	public void paint(GC gc, State executionState, int x, int y, int width,
			int height) {
		paintDefault(gc, executionState, x, y, width, height);
	}
	
	protected void paintDefault(GC gc, State executionState, int x, int y, int width, int height) {
		gc.setBackground(executionState.color);
		int center_x;
		int center_y;
		int radius;
		
		if(width > height) {
			radius = height / 2;
			center_y = radius;
			center_x = radius;
		} else {
			radius = width / 2;
			center_y = height / 2;
			center_x = radius;
		}

		if (executionState == State.CURRENT)
			radius++;

		gc.fillOval(x + center_x - radius, y + center_y - radius, radius * 2, radius * 2);
	}

	protected void paintText(String text, GC gc, State executionState, int x,
			int y, int width, int height) {

		gc.setFont(FONT);
		gc.setForeground(executionState.color);

		if (gc.stringExtent(text).x > width) {
			// Text doesn't fit, such a shame.
			// Default to the circle drawing
			paintDefault(gc, executionState, x, y, width, height);
		} else {
			gc.setClipping(x, y, width, height);
			gc.drawText(text, x, y);
			gc.setClipping((Rectangle) null);
		}

	}

}
