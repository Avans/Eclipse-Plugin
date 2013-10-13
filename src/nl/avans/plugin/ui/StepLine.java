package nl.avans.plugin.ui;

/**
 * Model class for a bit of text that is to be displayed next to a line of
 * source code. Explaining the statement and state. Like 'set x to 2'.
 */
public class StepLine {
	// The text to be displayed next to the line
	private String explanation;
	
	// The line in the source code that is explained
	private int line;
	
	// Whether it the text is bold or not
	private boolean bold;

	public StepLine(String explanation, int line, boolean bold) {
		this.explanation = explanation;
		this.line = line;
		this.bold = bold;
	}

	public String getExplanation() {
		return explanation;
	}

	public int getLine() {
		return line;
	}
	
	public boolean isBold() {
		return bold;
	}
}
