package nl.avans.plugin.ui.stepline;

/**
 * Model class for a bit of text that is to be displayed next to a line of
 * source code. Explaining the statement and state. Like 'set x to 2'.
 */
public class StepLine {
	// The text to be displayed next to the line
	private String explanation;
	
	// The line in the source code that is explained (0-indexed)
	private int line;
	
	// Whether it the text is bold or not
	private boolean primary;

	public StepLine(String explanation, int line, boolean primary) {
		this.explanation = explanation;
		this.line = line;
		this.primary = primary;
	}

	public String getExplanation() {
		return explanation;
	}

	public int getLine() {
		return line;
	}
	
	public boolean isPrimary() {
		return primary;
	}
}
