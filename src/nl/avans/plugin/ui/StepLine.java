package nl.avans.plugin.ui;

/**
 * Model class for a bit of text that is to be displayed next to a line of
 * source code. Explaining the statement and state. Like 'set x to 2'.
 */
public class StepLine {
	// The text to be displayed next to the line
	private String explanation;
	
	// The line
	private int line;

	public StepLine(String explanation, int line) {
		this.explanation = explanation;
		this.line = line;
	}

	public String getExplanation() {
		return explanation;
	}

	public int getLine() {
		return line;
	}
}
