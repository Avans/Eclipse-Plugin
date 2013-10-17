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
		Step step1 = new Step();
		step1.line = 3;
		List<StepLine> list2 = new ArrayList<StepLine>();
		list2.add(new StepLine("Zet variabele 'x' op 0", 3, true));
		step1.stepLines = list2;
		step1.value = new IntValue(0);
		steps.add(step1);

		int iteration = 5;
		int x = 0;
		while (x <= iteration) {
			Step condition_step = new Step();
			condition_step.line = 5;
			condition_step.value = new BooleanValue(x == iteration ? false
					: true);

			List<StepLine> list = new ArrayList<StepLine>();
			if (x == iteration) {
				list.add(new StepLine("Omdat " + x + " < " + iteration
						+ " niet waar is...", 5, true));
				list.add(new StepLine("...stoppen we met loopen", 8, false));
			} else {
				list.add(new StepLine("Omdat " + x + " < " + iteration + "...",
						5, true));
				list.add(new StepLine("...doen we dit", 6, false));
				list.add(new StepLine("...en dit", 7, false));
				list.add(new StepLine("...en proberen we opnieuw", 8, false));
			}
			condition_step.stepLines = list;
			steps.add(condition_step);

			if (x < iteration) {

				Step print_step = new Step();
				print_step.line = 6;
				print_step.value = new nl.avans.plugin.value.StringValue(x + "");
				list = new ArrayList<StepLine>();
				list.add(new StepLine("Print \"" + x + "\"", 6, true));
				print_step.stepLines = list;
				steps.add(print_step);

				Step increment_step = new Step();
				increment_step.line = 7;
				increment_step.value = new IntValue(x + 1);
				list = new ArrayList<StepLine>();
				list.add(new StepLine("Zet variabele 'x' op " + (x + 1), 7,
						true));
				increment_step.stepLines = list;
				steps.add(increment_step);
			}

			x++;

		}
	}
	
	public List<Step> getSteps() {
		return steps;
	}
}
