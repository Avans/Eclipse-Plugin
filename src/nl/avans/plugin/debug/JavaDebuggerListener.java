package nl.avans.plugin.debug;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;

/**
 * Singleton class that mindlessly listens to debugger events.
 * 
 * We only ever add one of these to
 * DebugPlugin.getDefault().addDebugEventSetListener(), as for some reason
 * removing them is problematic.
 * 
 * Specifically listens for termination and breakpoint hits of
 * StepRecorderBreakpoint
 * 
 */
public class JavaDebuggerListener implements IJavaBreakpointListener,
		IDebugEventSetListener {

	public interface TerminatorListener {
		public void debugTerminated();
	}

	/**
	 * Only provide static access to a debugger listener instance
	 */
	private static JavaDebuggerListener instance = null;

	public static JavaDebuggerListener getDefault() {
		if (instance == null) {
			instance = new JavaDebuggerListener();

		}
		return instance;
	}

	/**
	 * Create and register the DebuggerListener.
	 */
	private JavaDebuggerListener() {
		DebugPlugin.getDefault().addDebugEventListener(this);
		JDIDebugPlugin.getDefault().addJavaBreakpointListener(this);
	}

	TerminatorListener listener;
	private boolean neverSuspend = false;

	public void setTerminatorListener(TerminatorListener listener) {
		this.listener = listener;
	}

	/**
	 * Install the breakpoint if it is one of those special breakpoints that we
	 * are interested in.
	 */
	@Override
	public int installingBreakpoint(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint, IJavaType type) {
		if (breakpoint instanceof StepRecorderBreakpoint)
			return INSTALL;

		return DONT_CARE;
	}

	/**
	 * Listen for breakpoint hit events. If it is one of our own breakpoints we
	 * give control to it to somehow record the stackframe.
	 */
	@Override
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		if (breakpoint instanceof StepRecorderBreakpoint) {
			
			StepRecorderBreakpoint stepBreakpoint = (StepRecorderBreakpoint) breakpoint;
			try {
				stepBreakpoint.record((IJavaStackFrame) thread.getStackFrames()[0]);
			} catch (DebugException e) {
				e.printStackTrace();
			}
		}

		return neverSuspend ? DONT_SUSPEND : DONT_CARE;
	}

	/**
	 * Set whether to never suspend during debugging. This is used to ignore
	 * normal breakpoints set by the user. Obviously these breakpoints should
	 * work normally when we aren't doing our special debug run.
	 */
	public void setNeverSuspend(boolean neverSuspend) {
		this.neverSuspend = neverSuspend;
	}

	/**
	 * Listen for termination events and notify the listener if any occurs.
	 */
	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			if (event.getKind() == DebugEvent.TERMINATE
					&& event.getSource() instanceof IDebugTarget) {

				listener.debugTerminated();
			}
		}
	}

	@Override
	public void breakpointRemoved(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
		// Not interesting
	}

	@Override
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint,
			DebugException exception) {
		// Not interesting
	}

	@Override
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint,
			Message[] errors) {
		// Not interesting
	}

	@Override
	public void addingBreakpoint(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
		// Not interesting
	}

	@Override
	public void breakpointInstalled(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
		// Not interesting
	}
}
