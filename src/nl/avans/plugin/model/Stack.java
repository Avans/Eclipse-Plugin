package nl.avans.plugin.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;

public class Stack implements Iterable<StackFrame>{
	List<StackFrame> stackframes = new ArrayList<StackFrame>();

	
	public Stack(IStackFrame[] iStackFrames) throws DebugException {
		for(IStackFrame stackframe : iStackFrames) {
			stackframes.add(new StackFrame(stackframe));
		}
	}

	//TODO: Remove
	public void addStackFrame(StackFrame stackframe) {
		stackframes.add(stackframe);
	}
	
	@Override
	public Iterator<StackFrame> iterator() {
		return stackframes.iterator();
	}
}
