package nl.avans.plugin.debug.statement;

import nl.avans.plugin.value.Value;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;

public abstract class EvaluatableExpression {

	public EvaluatableExpression(Expression expression) {

	}

	public Value evaluateForValue(IJavaStackFrame stackframe) {
		return new nl.avans.plugin.value.StringValue("Hoi!");
	}

	public String evaluateForPresentableString(IJavaStackFrame stackframe)
			throws DebugException {
		return Evaluator.evaluate(toEvaluatableString(), stackframe)
				.getValueString();
	}

	public boolean evaluateForTruth(IJavaStackFrame stackframe)
			throws DebugException {
		IJavaValue value = Evaluator
				.evaluate(toEvaluatableString(), stackframe);
		return value.getValueString().equals("true");
	}

	public abstract String toEvaluatableString();

	public static EvaluatableExpression getEvaluatableExpressionForExpression(
			Expression expression) {
		if (expression.getNodeType() == Expression.INFIX_EXPRESSION) {
			return new InFixEvaluatableExpression((InfixExpression) expression);
		} else {
			return new DefaultEvaluatableExpression(expression);
		}
	}
}
