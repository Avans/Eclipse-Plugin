package nl.avans.plugin.debug;

import java.util.HashMap;

import nl.avans.plugin.TimeoutChecker;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

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
			System.out.println("Hit breakpoint " + breakpoint + " " + this);

			StepRecorderBreakpoint stepBreakpoint = (StepRecorderBreakpoint) breakpoint;
			stepBreakpoint.record(thread);
		}

		return DONT_SUSPEND;
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

	/*
	 * public static IJavaProject getProject(IJavaStackFrame javaStackFrame) {
	 * System.out.println(javaStackFrame.getLaunch());
	 * System.out.println(javaStackFrame.getLaunch().getSourceLocator()); Object
	 * sourceElement = javaStackFrame.getLaunch().getSourceLocator()
	 * .getSourceElement(javaStackFrame); if (!(sourceElement instanceof
	 * IJavaElement) && sourceElement instanceof IAdaptable) sourceElement =
	 * ((IAdaptable) sourceElement) .getAdapter(IJavaElement.class); assert
	 * sourceElement instanceof IJavaElement; return ((IJavaElement)
	 * sourceElement).getJavaProject(); }
	 * 
	 * public IAstEvaluationEngine getASTEvaluationEngine( IJavaStackFrame
	 * stackFrame) { return org.eclipse.jdt.debug.eval.EvaluationManager
	 * .newAstEvaluationEngine(myJavaProject, (IJavaDebugTarget)
	 * stackFrame.getDebugTarget()); }
	 * 
	 * private static String getErrors(IEvaluationResult result) { StringBuilder
	 * sb = new StringBuilder(); //
	 * buffer.append("Error on evaluation of: ").append
	 * (result.getSnippet()).append("\n"); if (result.getException() == null) {
	 * String[] messages = result.getErrorMessages(); for (int i = 0; i <
	 * messages.length; i++) sb.append(messages[i]).append("\n "); } else
	 * sb.append(getExceptionMessage(result.getException())); return
	 * sb.toString(); }
	 * 
	 * public static String getExceptionMessage(Throwable exception) { if
	 * (exception instanceof CoreException) { CoreException ce = (CoreException)
	 * exception; Throwable throwable = ce.getStatus().getException(); if
	 * (throwable instanceof InvocationException) { ObjectReference ref =
	 * ((InvocationException) throwable) .exception(); return
	 * "An exception occurred: " + ref.referenceType().name(); } else if
	 * (throwable instanceof CoreException) return
	 * getExceptionMessage(throwable); return ce.getStatus().getMessage(); }
	 * String message = "An exception occurred: " + exception.getClass(); if
	 * (exception.getMessage() != null) message += " - " +
	 * exception.getMessage(); return message; }
	 * 
	 * public IJavaValue evaluate(String stringValue, final IJavaStackFrame
	 * stack) throws DebugException { IAstEvaluationEngine engine =
	 * getASTEvaluationEngine(stack);
	 * 
	 * // Save variable values, to mitigate any side effects HashMap<IVariable,
	 * IValue> originalVariableValues = new HashMap<IVariable, IValue>(); for
	 * (IVariable variable : stack.getVariables()) {
	 * originalVariableValues.put(variable, variable.getValue()); }
	 * 
	 * final IEvaluationResult[] results = new IEvaluationResult[1];
	 * IEvaluationListener listener = new IEvaluationListener() {
	 * 
	 * @Override public void evaluationComplete(IEvaluationResult result) {
	 * synchronized (stack) { results[0] = result; stack.notifyAll(); } } };
	 * synchronized (stack) { if (stack.isTerminated()) // If the stack is
	 * terminated the wait // below will hang forever, so abort in // that case.
	 * return null; engine.evaluate(stringValue, stack, listener,
	 * DebugEvent.EVALUATION_IMPLICIT, false); try {
	 * stack.wait(TimeoutChecker.TIMEOUT_TIME_MS); // Timeout the // execution.
	 * } catch (InterruptedException e) { if (results[0] == null) throw new
	 * RuntimeException(e); } } IEvaluationResult result = results[0]; if
	 * (result == null) { // The evaluation timed out, so we need to cancel //
	 * it and wait for it to finish. If we don't // wait, the thread will be in
	 * a bad state and // error for future evaluations until it // finishes.
	 * IJavaThread thread = (IJavaThread) stack.getThread();
	 * thread.terminateEvaluation(); // Unfortunately, we cannot easily // tell
	 * when it actually terminates // (this method just sets a flag // asking it
	 * to). try { for (int i = 0; thread.isPerformingEvaluation(); i++) { if (i
	 * == 20) // Eventually give up on the termination and // abort.
	 * System.err.print("Unable to terminate evaluation."); // throw new
	 * Exception("Unable to terminate evaluation.");
	 * Thread.sleep(TimeoutChecker.TIMEOUT_TIME_MS / 10); } } catch
	 * (InterruptedException e) { throw new RuntimeException(e); } return null;
	 * } if (result.hasErrors()) { String msg =
	 * "The following errors were encountered during evaluation.\n\n" +
	 * getErrors(result); // showError("Evaluation error", msg, null);
	 * System.err.print(msg); // throw new Exception(msg); }
	 * 
	 * for (IVariable variable : stack.getVariables()) { if
	 * (originalVariableValues.containsKey(variable))
	 * variable.setValue(originalVariableValues.get(variable)); }
	 * 
	 * return result.getValue(); }
	 */
}
