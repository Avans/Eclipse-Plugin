package nl.avans.plugin.debug.statement;

import java.util.List;

import nl.avans.plugin.step.Step;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.BooleanValue;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public class IfStepStatement extends StepStatement {

	private boolean hasThen = false;
	private boolean hasElse = false;

	private int thenEndLine;

	private int elseBeginLine;
	private int elseEndLine;

	EvaluatableExpression expression;

	public IfStepStatement(IfStatement statement, IType type) {
		super(statement, type);
		Statement then = statement.getThenStatement();
		Statement elseStmnt = statement.getElseStatement();

		if (then != null) {
			hasThen = true;
			thenEndLine = getLineForPosition(type, then.getStartPosition()
					+ then.getLength());
		}
		if (elseStmnt != null) {
			hasElse = true;
			elseBeginLine = getLineForPosition(type,
					elseStmnt.getStartPosition());
			elseEndLine = getLineForPosition(type, elseStmnt.getStartPosition()
					+ elseStmnt.getLength());
		}

		expression = EvaluatableExpression
				.getEvaluatableExpressionForExpression(statement
						.getExpression());
	}

	@Override
	public Step createStepFromThread(IJavaStackFrame stackframe)
			throws DebugException {
		Step step = super.createStepFromThread(stackframe);

		boolean evaluated = expression.evaluateForTruth(stackframe);
		step.value = new BooleanValue(evaluated);

		if (evaluated) {
			if (hasThen) {
				step.stepLines.add(new StepLine("Omdat "
						+ expression.evaluateForPresentableString(stackframe)
						+ " waar is...", line, true));
				addWeDoThis(step.stepLines, line + 1, thenEndLine-1);
			}

			if (hasElse) {
				addWeDontDoThis(step.stepLines, elseBeginLine+1, elseEndLine-1);
			}
		} else {
			if(hasThen) {
				step.stepLines.add(new StepLine("Omdat "
						+ expression.evaluateForPresentableString(stackframe)
						+ " niet waar is...", line, true));
				addWeDontDoThis(step.stepLines, line + 1, thenEndLine-1);
			}
			if(hasElse) {
				addWeDoThis(step.stepLines, elseBeginLine+1, elseEndLine-1);
			}
		}

		return step;
	}

	private static void addWeDoThis(List<StepLine> stepLines, int startLine,
			int endLine) {
		for (int line = startLine; line <= endLine; line++) {
			if (line == startLine) {
				stepLines.add(new StepLine("...doen we dit", line, false));
			} else {
				stepLines.add(new StepLine("...en dit", line, false));
			}
		}
	}
	
	private static void addWeDontDoThis(List<StepLine> stepLines, int startLine,
			int endLine) {
		for (int line = startLine; line <= endLine; line++) {
			if (line == startLine) {
				stepLines.add(new StepLine("...doen we niet dit", line, false));
			} else {
				stepLines.add(new StepLine("...en ook niet dit", line, false));
			}
		}
	}

}
