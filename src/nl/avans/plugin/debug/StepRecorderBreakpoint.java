package nl.avans.plugin.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;

public class StepRecorderBreakpoint extends JavaLineBreakpoint {

	private ProgramExecution programExecution;

	public StepRecorderBreakpoint(ProgramExecution programExecution,
			IType type, int charStart, int charEnd) throws DebugException {
		super(type.getResource(), type.getFullyQualifiedName(), 6, -1, -1, -1,
				false, new HashMap<String, Object>());
		this.programExecution = programExecution;
	}

	@Override
	public int getLineNumber() throws CoreException {
		return 6;
	}

	/**
	 * For pete's sake, do not persist this breakpoint It is meant as a
	 * super-temporary breakpoint and should be removed at the first possible
	 * instance.
	 */
	@Override
	public boolean isPersisted() throws CoreException {
		return false;
	}

	public void record(IJavaThread thread) {
		try {
			IJavaStackFrame stack = (IJavaStackFrame) thread.getStackFrames()[0];

			Step step = new Step();
			step.line = 5;
			step.value = new BooleanValue(true);
			List<StepLine> stepLines = new ArrayList<StepLine>();
			stepLines.add(new StepLine("Omdat dit waar is...", 5, true));
			step.stepLines = stepLines;
			programExecution.addStep(step);

		} catch (DebugException e) {
			e.printStackTrace();
		}

	}
}
