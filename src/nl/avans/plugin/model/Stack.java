package nl.avans.plugin.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Stack implements Iterable<StackFrame>{
	List<StackFrame> stackframes = new ArrayList<StackFrame>();
	
	public void addStackFrame(StackFrame stackframe) {
		stackframes.add(stackframe);
	}
	
	@Override
	public Iterator<StackFrame> iterator() {
		return stackframes.iterator();
	}
}
