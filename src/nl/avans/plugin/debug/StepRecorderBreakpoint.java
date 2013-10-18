package nl.avans.plugin.debug;

import java.util.HashMap;
import java.util.Map;

import nl.avans.plugin.model.ProgramExecution;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;

public class StepRecorderBreakpoint extends JavaLineBreakpoint {
	private ProgramExecution programExecution;

	public StepRecorderBreakpoint(ProgramExecution programExecution, IType type, int charStart, int charEnd) throws DebugException {
		/*
		 * (IResource resource, String typeName, int lineNumber, int charStart,
		 * int charEnd, int hitCount, boolean add, Map<String, Object>
		 * attributes)
		 */
		super(type.getResource(), type.getFullyQualifiedName(), 6, -1, -1, -1,
				false, new HashMap<String, Object>());
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
}
