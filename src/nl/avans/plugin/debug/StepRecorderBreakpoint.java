package nl.avans.plugin.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import nl.avans.plugin.debug.statement.StepStatement;
import nl.avans.plugin.model.ProgramExecution;
import nl.avans.plugin.step.Step;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.BooleanValue;
import nl.avans.plugin.value.IntValue;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;

public class StepRecorderBreakpoint extends JavaLineBreakpoint {

	private ProgramExecution programExecution;
	private StepStatement statement;

	public StepRecorderBreakpoint(IType type, StepStatement statement)
			throws CoreException {
		super(type.getResource(), type.getFullyQualifiedName(), statement
				.getOneIndexedLineNumber(), statement.getCharStart(), statement
				.getCharEnd(), -1, false, new HashMap<String, Object>());
		
		this.statement = statement;

		System.out.println("Breakpoint set at " + statement.getOneIndexedLineNumber());
		/**
		 * For pete's sake, do not persist this breakpoint. It is meant as a
		 * super-temporary breakpoint and should be removed at the first
		 * possible instance.
		 */
		setPersisted(false);
	}

	public void setProgramExecution(ProgramExecution programExecution) {
		this.programExecution = programExecution;
	}

	public void record(IJavaThread thread) {
		Step step = statement.createStepFromThread(thread);
		System.out.println(step);
		programExecution.addStep(step);
	}
}
