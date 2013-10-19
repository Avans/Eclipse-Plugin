package nl.avans.plugin.debug.statement;

import org.eclipse.jdt.core.dom.Expression;

public class DefaultEvaluatableExpression extends EvaluatableExpression {

	String expressionString;
	
	public DefaultEvaluatableExpression(Expression expression) {
		super(expression);
		this.expressionString = expression.toString(); 
	}

	@Override
	public String toEvaluatableString() {
		return expressionString;
	}

}
