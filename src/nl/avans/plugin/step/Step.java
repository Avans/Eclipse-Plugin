package nl.avans.plugin.step;

import java.util.List;

import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.IntValue;
import nl.avans.plugin.value.Value;

import org.eclipse.jdt.core.ICompilationUnit;

public class Step {
	public int line;
	
	private ICompilationUnit compilationUnit;

	public List<StepLine> stepLines;

	public Value value;

	public Value getValue() {
		return value;
	}

	public List<StepLine> getStepLines() {
		return stepLines;
	}
}
