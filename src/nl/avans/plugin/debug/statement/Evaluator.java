package nl.avans.plugin.debug.statement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;

import com.sun.jdi.InvocationException;
import com.sun.jdi.ObjectReference;

public abstract class Evaluator {
	public static final long TIMEOUT_TIME_MS = 1000;

	/**
	 * Extracts the errors messages out of a failing IEvaluationResult. Copied
	 * from JavaObjectValueEditor.evaluate.
	 * 
	 * @param result
	 *            A failing evaluation result.
	 * @return A string representing the failure.
	 */
	private static String getErrors(IEvaluationResult result) {
		StringBuilder sb = new StringBuilder();
		// buffer.append("Error on evaluation of: ").append(result.getSnippet()).append("\n");
		if (result.getException() == null) {
			String[] messages = result.getErrorMessages();
			for (int i = 0; i < messages.length; i++)
				sb.append(messages[i]).append("\n ");
		} else
			sb.append(getExceptionMessage(result.getException()));
		return sb.toString();
	}

	/**
	 * Gets the message of an exception. Inspired by
	 * org.eclipse.jdt.internal.debug
	 * .ui.actions.EvaluateAction.getExceptionMessage.
	 * 
	 * @param exception
	 *            The exception whose message we want to get.
	 * @return The message of the given exception.
	 */
	public static String getExceptionMessage(Throwable exception) {
		if (exception instanceof CoreException) {
			CoreException ce = (CoreException) exception;
			Throwable throwable = ce.getStatus().getException();
			if (throwable instanceof InvocationException) {
				ObjectReference ref = ((InvocationException) throwable)
						.exception();
				return "An exception occurred: " + ref.referenceType().name();
			} else if (throwable instanceof CoreException)
				return getExceptionMessage(throwable);
			return ce.getStatus().getMessage();
		}
		String message = "An exception occurred: " + exception.getClass();
		if (exception.getMessage() != null)
			message += " - " + exception.getMessage();
		return message;
	}

	/**
	 * Return the project associated with the given stack frame. (copied from
	 * JavaWatchExpressionDelegate) (copied from JavaObjectValueEditor)
	 * 
	 * @param javaStackFrame
	 *            The stack frame
	 * @return the project associate with the given stack frame.
	 */
	public static IJavaProject getProject(IJavaStackFrame javaStackFrame) {
		Object sourceElement = javaStackFrame.getLaunch().getSourceLocator()
				.getSourceElement(javaStackFrame);
		if (!(sourceElement instanceof IJavaElement)
				&& sourceElement instanceof IAdaptable)
			sourceElement = ((IAdaptable) sourceElement)
					.getAdapter(IJavaElement.class);
		assert sourceElement instanceof IJavaElement;
		return ((IJavaElement) sourceElement).getJavaProject();
	}

	/**
	 * Gets an AST evaluation engine.
	 * 
	 * @param stackFrame
	 *            The current stack frame.
	 * @return An AST evaluation engine.
	 */
	public static IAstEvaluationEngine getASTEvaluationEngine(
			IJavaStackFrame stackFrame) {
		return org.eclipse.jdt.debug.eval.EvaluationManager
				.newAstEvaluationEngine(getProject(stackFrame),
						(IJavaDebugTarget) stackFrame.getDebugTarget());
	}

	/**
	 * Evaluates the given snippet. Reports any errors to the user. TODO:
	 * Integrate with codehint.expreval code?
	 * 
	 * @param stringValue
	 *            the snippet to evaluate
	 * @param stack
	 *            The current stack frame.
	 * @return the value that was computed or <code>null</code> if any errors
	 *         occurred.
	 * @throws DebugException
	 */
	public static IJavaValue evaluate(String stringValue,
			final IJavaStackFrame stack) {
		try {
			IAstEvaluationEngine engine = getASTEvaluationEngine(stack);
			final IEvaluationResult[] results = new IEvaluationResult[1];
			IEvaluationListener listener = new IEvaluationListener() {
				@Override
				public void evaluationComplete(IEvaluationResult result) {
					synchronized (stack) {
						results[0] = result;
						stack.notifyAll();
					}
				}
			};
			synchronized (stack) {
				if (stack.isTerminated()) // If the stack is terminated the wait
											// below will hang forever, so abort
											// in
											// that case.
					return null;
				engine.evaluate(stringValue, stack, listener,
						DebugEvent.EVALUATION_IMPLICIT, false);
				try {
					stack.wait(TIMEOUT_TIME_MS); // Timeout the
													// execution.
				} catch (InterruptedException e) {
					if (results[0] == null)
						throw new RuntimeException(e);
				}
			}
			IEvaluationResult result = results[0];
			if (result == null) { // The evaluation timed out, so we need to
									// cancel
									// it and wait for it to finish. If we don't
									// wait, the thread will be in a bad state
									// and
									// error for future evaluations until it
									// finishes.
				IJavaThread thread = (IJavaThread) stack.getThread();
				thread.terminateEvaluation(); // Unfortunately, we cannot easily
												// tell when it actually
												// terminates
												// (this method just sets a flag
												// asking it to).
				try {
					for (int i = 0; thread.isPerformingEvaluation(); i++) {
						if (i == 20) // Eventually give up on the termination
										// and
										// abort.
							throw new Error("Unable to terminate evaluation.");
						Thread.sleep(TIMEOUT_TIME_MS / 10);
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				return null;
			}
			if (result.hasErrors()) {
				String msg = "The following errors were encountered during evaluation.\n\n"
						+ getErrors(result);
				// showError("Evaluation error", msg, null);
				throw new Error(msg);
			}
			return result.getValue();

		} catch (DebugException e) {
			e.printStackTrace();
			return null;
		}
	}

}
