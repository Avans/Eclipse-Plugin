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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;

public class StepRecorderBreakpoint extends JavaLineBreakpoint {

	private ProgramExecution programExecution;

	public StepRecorderBreakpoint(IType type, int charStart, int charEnd)
			throws CoreException {
		super(type.getResource(), type.getFullyQualifiedName(),
				getLineForPosition(type, charStart), charStart, charEnd, -1,
				false, new HashMap<String, Object>());

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

	private static int getLineForPosition(IType type, int charStart)
			throws JavaModelException {
		// Calculate 1-indexed line from charStart and charEnd
		String source = type.getCompilationUnit().getSource();
		int line = source.substring(0, charStart).split("\n").length;
		System.out.println("Line number: " + line);

		return line;
	}

	private int getZeroIndexedLineNumber() {
		try {
			return getLineNumber() - 1;
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	public void record(IJavaThread thread) {
		try {
			IJavaStackFrame stack = (IJavaStackFrame) thread.getStackFrames()[0];

			Step step = new Step();
			step.line = getZeroIndexedLineNumber();
			step.value = new BooleanValue(true);
			List<StepLine> stepLines = new ArrayList<StepLine>();
			stepLines.add(new StepLine("Omdat dit waar is...",
					getZeroIndexedLineNumber(), true));
			step.stepLines = stepLines;
			programExecution.addStep(step);

		} catch (DebugException e) {
			e.printStackTrace();
		}

	}
}
