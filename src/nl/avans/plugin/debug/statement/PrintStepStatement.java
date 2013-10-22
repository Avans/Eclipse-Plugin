package nl.avans.plugin.debug.statement;

import nl.avans.plugin.step.Step;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.StringValue;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.debug.core.IJavaStackFrame;


public class PrintStepStatement extends StepStatement {

	String expression;
	
	public PrintStepStatement(Statement statement, IType type, MethodInvocation printExpression) {
		super(statement, type);
		
		if(printExpression.arguments().size() > 0) {
			expression = printExpression.arguments().get(0).toString();
		}
	}
	
	@Override
	public Step createStepFromThread(IJavaStackFrame stackframe)
			throws DebugException {
		if(expression == null)
			return null;

		String evaluated = Evaluator.evaluate(expression, stackframe).toString();
		evaluated = evaluated.replaceAll("\"$|^\"", ""); // Trim " characters
		
		Step step = super.createStepFromThread(stackframe);
		step.value = new StringValue(evaluated);
		step.stepLines.add(new StepLine("Print \"" + evaluated + "\" naar de Console", line, true));
		return step;
	}

	
}
