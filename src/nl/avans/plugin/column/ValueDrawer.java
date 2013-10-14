package nl.avans.plugin.column;

import nl.avans.plugin.column.ColumnStep.State;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public interface ValueDrawer {
	public void paint(GC gc, State executionState, int x, int y, int width, int height);
}
