package nl.avans.plugin.debug.statement;

import nl.avans.plugin.step.Step;
import nl.avans.plugin.ui.stepline.StepLine;

import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.WhileStatement;



public class WhileStepStatement extends StepStatement {

	// 0-indexed end line of the while loop
	private int endLine;
	public WhileStepStatement(WhileStatement statement, IType type) {
		super(statement, type);
		endLine = getLineForPosition(type, getCharEnd());
	}
	
	@Override
	public Step createStepFromThread(IThread thread) {
		Step step = super.createStepFromThread(thread);
		for(int line = this.line; line <= endLine; line++) {
			if(line == this.line)
				step.stepLines.add(new StepLine("Omdat <expression> waar is...", line, true));
			else if(line == endLine) {
				step.stepLines.add(new StepLine("...en proberen we opnieuw", line, false));
			} else if(line == this.line + 1){
				step.stepLines.add(new StepLine("...doen we dit", line, false));
			} else {
				step.stepLines.add(new StepLine("...en dit", line, false));
			}
			
		}
		return step;
	}

	

}
