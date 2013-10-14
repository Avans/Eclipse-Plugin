package nl.avans.plugin.value;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import nl.avans.plugin.column.ValueDrawer;
import nl.avans.plugin.column.ColumnStep.State;

public class Value implements ValueDrawer, Comparable<Value> {

	protected static final Font FONT = new Font(null, "Arial", 12, SWT.NORMAL);

	@Override
	public void paint(GC gc, Value maximalValue, State executionState, int x, int y, int width,
			int height) {
		paintDefault(gc, executionState, x, y, width, height);
	}

	protected void paintDefault(GC gc, State executionState, int x, int y,
			int width, int height) {
		gc.setBackground(executionState.color);
		if (width <= 3) {
			double HEIGHT = 0.2;
			gc.fillRectangle(x + 0, y + (int) (height * 0.5 - HEIGHT / 2),
					width, (int) (height * HEIGHT));
		} else {
			double center_x;
			double center_y;
			double radius;

			if (width > height) {
				radius = height / 2.0;
				center_y = radius;
				center_x = radius;
			} else {
				radius = width / 2;
				center_y = height / 2.0;
				center_x = radius;
			}

			if (executionState == State.CURRENT)
				radius++;

			gc.fillOval((int) (x + center_x - radius),
					(int) (y + center_y - radius), (int) (radius * 2),
					(int) (radius * 2));
		}
	}

	protected void paintText(String text, GC gc, State executionState, int x,
			int y, int width, int height) {
		gc.setFont(FONT);
		gc.setForeground(executionState.color);

		if (gc.stringExtent(text).x <= width) {
			gc.drawText(text, x, y);
		} else {
			// Text doesn't fit, such a shame.
			// Default to the circle drawing
			paintDefault(gc, executionState, x, y, width, height);
		}

	}

	@Override
	public int compareTo(Value o) {
		return 0;
	}

}
