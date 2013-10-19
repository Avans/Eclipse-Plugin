package nl.avans.plugin.debug.statement;

import java.util.ArrayList;
import java.util.List;

import nl.avans.plugin.step.Step;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.BooleanValue;

import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Statement;

public class StepStatement {

	// 0-indexed starting line of the statement
	protected int line;

	private int charStart;
	private int charEnd;

	public StepStatement(Statement statement, IType type) {
		this.charStart = statement.getStartPosition();
		this.charEnd = statement.getStartPosition() + statement.getLength();
		this.line = getLineForPosition(type, charStart);
	}

	/**
	 * Calculate 0-indexed line for character position
	 */
	protected static int getLineForPosition(IType type, int charStart) {

		String source = "";
		try {
			source = type.getCompilationUnit().getSource();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		int line = source.substring(0, charStart).split("\n").length - 1;
		
		return line;
	}
	
	

	public Step createStepFromThread(IThread thread) {
		Step step = new Step();
		step.line = line;
		step.value = new BooleanValue(true);
		step.stepLines = new ArrayList<StepLine>();
		return step;
	}

	public int getCharEnd() {
		return charEnd;
	}

	public int getOneIndexedLineNumber() {
		return line + 1;
	}

	public int getCharStart() {
		return charEnd;
	}

}
