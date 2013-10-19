package nl.avans.plugin.debug.statement;

import nl.avans.plugin.step.Step;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.BooleanValue;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public class WhileStepStatement extends StepStatement {

	EvaluatableExpression expression;

	// 0-indexed end line of the while loop
	private int endLine;

	public WhileStepStatement(WhileStatement statement, IType type) {
		super(statement, type);
		endLine = getLineForPosition(type, getCharEnd());
		this.expression = EvaluatableExpression
				.getEvaluatableExpressionForExpression(statement
						.getExpression());
	}

	@Override
	public Step createStepFromThread(IJavaStackFrame stackframe) throws DebugException {
		Step step = super.createStepFromThread(stackframe);
		boolean evaluated = expression.evaluateForTruth(stackframe);
		step.value = new BooleanValue(evaluated);

		if (evaluated) {
			for (int line = this.line; line <= endLine; line++) {
				if (line == this.line)
					step.stepLines.add(new StepLine("Omdat "
							+ expression
									.evaluateForPresentableString(stackframe)
							+ " waar is...", line, true));
				else if (line == endLine) {
					step.stepLines.add(new StepLine(
							"...en proberen we opnieuw", line, false));
				} else if (line == this.line + 1) {
					step.stepLines.add(new StepLine("...doen we dit", line,
							false));
				} else {
					step.stepLines.add(new StepLine("...en dit", line, false));
				}
			}
		} else {
			step.stepLines.add(new StepLine("Omdat "
					+ expression.evaluateForPresentableString(stackframe)
					+ " niet waar is", line, true));
			step.stepLines.add(new StepLine("Stoppen we met loopen", endLine,
					false));
		}
		return step;
	}
}
