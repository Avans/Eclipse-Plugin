package nl.avans.plugin.debug.statement;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public class InFixEvaluatableExpression extends EvaluatableExpression {

	EvaluatableExpression left_hand;
	String operator;
	EvaluatableExpression right_hand;

	public InFixEvaluatableExpression(InfixExpression expression) {
		super(expression);
		left_hand = getEvaluatableExpressionForExpression(expression
				.getLeftOperand());
		right_hand = getEvaluatableExpressionForExpression(expression
				.getRightOperand());
		operator = expression.getOperator().toString();
	}

	@Override
	public String toEvaluatableString() {
		return left_hand.toEvaluatableString() + " " + operator + " "
				+ right_hand.toEvaluatableString();
	}

	@Override
	public String evaluateForPresentableString(IJavaStackFrame stackframe) throws DebugException {
		return left_hand.evaluateForPresentableString(stackframe) + " "
				+ operator + " "
				+ right_hand.evaluateForPresentableString(stackframe);
	}
}
