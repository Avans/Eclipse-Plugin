package nl.avans.plugin.model;

import java.util.ArrayList;
import java.util.List;

import nl.avans.plugin.step.Step;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.BooleanValue;
import nl.avans.plugin.value.IntValue;

public class ProgramExecution {
	private List<Step> steps = new ArrayList<Step>();

	public ProgramExecution() {
		
	}
	
	public List<Step> getSteps() {
		return steps;
	}

	public void addStep(Step step) {
		steps.add(step);
		
	}
}
