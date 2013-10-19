package nl.avans.plugin.debug.statement;

import nl.avans.plugin.step.Step;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.IntValue;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

public class AssignmentStepStatement extends StepStatement {

	private String variableName;
	private EvaluatableExpression expression;
	
	public AssignmentStepStatement(VariableDeclarationStatement statement, IType type) {
		super(statement, type);
		int y = 6;
		int x = y = 6;
		
		x = 6;
		// TODO Auto-generated constructor stub
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		variableName = fragment.getName().toString();
		expression = EvaluatableExpression.getEvaluatableExpressionForExpression(fragment.getInitializer());
		
		
	}
	
	@Override
	public Step createStepFromThread(IJavaStackFrame stackframe)
			throws DebugException {
		// TODO Auto-generated method stub
		Step step = super.createStepFromThread(stackframe);
		step.value = expression.evaluateForValue(stackframe);
		step.stepLines.add(new StepLine("Zet variabele " + variableName + " op " + expression.evaluateForPresentableString(stackframe), line, true));
		return step;
	}

}
