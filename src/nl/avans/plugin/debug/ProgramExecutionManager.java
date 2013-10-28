package nl.avans.plugin.debug;

import java.util.HashSet;
import java.util.Set;

import nl.avans.plugin.model.ProgramExecution;

public class ProgramExecutionManager {

	public interface ProgramExecutionListener {
		public void programExecutionChanged(ProgramExecution newProgramExecution);
		public void programExecutionRemoved();
	}
	
	public interface ProgramStateListener {
		public void programStateChanged(State newState);
		public void programStateRemoved();
	}
	
	/**
	 * Allow access to the manager from any context
	 */
	private static ProgramExecutionManager defaultManager = new ProgramExecutionManager();
	
	public static ProgramExecutionManager getDefault() {
		return defaultManager;
	}
	
	private ProgramExecution currentProgramExecution;
	private State currentProgramState;
	
	private Set<ProgramExecutionListener> listeners = new HashSet<ProgramExecutionListener>();
	private Set<ProgramStateListener> stateListeners = new HashSet<ProgramStateListener>();
	
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
	
	public void setProgramState(State state) {
		if(state != currentProgramState) {
			this.currentProgramState = state;
			
			for(ProgramStateListener listener : stateListeners) {
				if(state == null) {
					listener.programStateRemoved();
				} else {
					listener.programStateChanged(state);
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
	
	public ProgramExecution getProgramExecution() {
		return currentProgramExecution;
	}

	public void removeProgramState() {
		setProgramState(null);
		
	}

	public void removeProgramStateListener(ProgramStateListener listener) {
		stateListeners.remove(listener);
	}

	public void addProgramStateListener(ProgramStateListener listener) {
		stateListeners.add(listener);
	}
	
}
