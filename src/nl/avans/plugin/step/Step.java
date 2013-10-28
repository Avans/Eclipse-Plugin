package nl.avans.plugin.step;

import java.util.List;

import nl.avans.plugin.debug.State;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.IntValue;
import nl.avans.plugin.value.Value;

import org.eclipse.jdt.core.ICompilationUnit;

public class Step {
	// The line that this step is on (0-indexed)
	public int line;
	
	public ICompilationUnit compilationUnit;

	public List<StepLine> stepLines;

	public Value value;

	public State state;

	public Value getValue() {
		return value;
	}

	public List<StepLine> getStepLines() {
		return stepLines;
	}
}
