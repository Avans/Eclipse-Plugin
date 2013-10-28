package nl.avans.plugin.column;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * LoopingSegments represent a portion of a ruler column that are next to a
 * while loop. They are used for separate behaviour in the column with respect
 * to ColumnStep layout and ColumnStep hovering.
 * 
 * LoopingSegments only define outer-loops. Loops within loops don't need
 * separate layout and behaviour.
 */
public class LoopingSegment {

	// The columnSteps within this loop
	private List<ColumnStep> columnSteps = new ArrayList<ColumnStep>();

	// The start line of the looping segment (0-indexed)
	private int line;

	// The number of lines of source code the loop spans.
	private int linecount;

	public LoopingSegment(int line, int linecount) {
		this.line = line;
		this.linecount = linecount;
	}

	public void addColumnStep(ColumnStep columnStep) {
		columnSteps.add(columnStep);
	}

	public void addColumnStepIfCorrectLine(ColumnStep columnStep) {
		if (columnStep.step.line >= line && columnStep.step.line < line + linecount) {
			addColumnStep(columnStep);
		}
	}

	/**
	 * Sets the x and width property of all ColumnSteps in the loop. We try to
	 * layout them in this fashion:
	 * 
	 * <pre>
	 * O  O 
	 *  O  O
	 *   O  O
	 * </pre>
	 * 
	 * Where the left to right order of the steps is the order of execution.
	 * 
	 * @param width
	 *            The width of the ruler column
	 */
	public void layout(int width) {
		double stepWidth = width / (double) columnSteps.size();

		// Set all the x-positions and widths of the columnSteps
		HashMap<Integer, Integer> remainingWidth = new HashMap<Integer, Integer>();
		for (int i = columnSteps.size() - 1; i >= 0; i--) {
			ColumnStep columnStep = columnSteps.get(i);
			columnStep.x = (int) (i * stepWidth);

			if (!remainingWidth.containsKey(columnStep.getLine())) {
				remainingWidth.put(columnStep.getLine(), width);
			}
			columnStep.width = remainingWidth.get(columnStep.getLine())
					- columnStep.x;
			remainingWidth.put(columnStep.getLine(), columnStep.x);
		}
	}

	/**
	 * Find the step that the mouse is hovering over within a looping segment
	 * 
	 * In contrast with normal steps steps are not selected when the mouse is
	 * hovering over the drawable area. Instead steps are selected based purely
	 * on the x-position of the mouse. Allowing for a scrubbing motion over the
	 * looping segment over any line, with all steps still highlighted in the
	 * correct order.
	 * 
	 * @param line
	 * @param x
	 * @param width
	 * @return
	 */
	public ColumnStep getHoveringStep(int line, int x, int width) {
		// Check if relevant line
		if (line < this.line || line >= this.line + this.linecount)
			return null;

		// Find step based on solely x position
		int index = x * columnSteps.size() / width;
		if (index >= columnSteps.size())
			return null;

		return columnSteps.get(index);
	}
}
