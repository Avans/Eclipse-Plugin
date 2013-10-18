package nl.avans.plugin.debug;

import java.util.HashSet;
import java.util.Set;

import nl.avans.plugin.model.ProgramExecution;

public class ProgramExecutionManager {

	public interface ProgramExecutionListener {
		public void programExecutionChanged(ProgramExecution newProgramExecution);
		public void programExecutionRemoved();
	}
	
	/**
	 * Allow access to the manager from any context
	 */
	private static ProgramExecutionManager defaultManager = new ProgramExecutionManager();
	
	public static ProgramExecutionManager getDefault() {
		return defaultManager;
	}
	
	private ProgramExecution currentProgramExecution;
	
	private Set<ProgramExecutionListener> listeners = new HashSet<ProgramExecutionListener>();
	
	
	
	public void setProgramExecution(ProgramExecution programExecution) {
		if(programExecution != currentProgramExecution) {
			this.currentProgramExecution = programExecution;
			
			for(ProgramExecutionListener listener : listeners) {
				if(programExecution == null) {
					listener.programExecutionRemoved();
				} else {
					listener.programExecutionChanged(programExecution);
				}
			}
		}
	}
	
	public void removeProgramExecution() {
		setProgramExecution(null);
	}

	public void addProgramExecutionListener(ProgramExecutionListener listener) {
		listeners.add(listener);
	}

	public void removeProgramExecutionListener(ProgramExecutionListener listener) {
		listeners.remove(listener);
		
	}
	
	
}
