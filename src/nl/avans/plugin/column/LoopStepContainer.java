package nl.avans.plugin.column;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class LoopStepContainer {

	List<ColumnStep> columnSteps = new ArrayList<ColumnStep>();
	
	int line;
	int linecount;
	int width;
	
	public LoopStepContainer(int line, int linecount, int width) {
		this.line = line;
		this.linecount = linecount;
		this.width = width;
	}
	
	public void addColumnStep(ColumnStep columnStep) {
		columnSteps.add(columnStep);
	}
	
	public void layout() {
		double stepWidth = width / (double)columnSteps.size();
		
		// Set all the x-positions of the columnSteps
		HashMap<Integer, Integer> remainingWidth = new HashMap<Integer, Integer>(); 
		for(int i = columnSteps.size() - 1; i >= 0 ; i--) {
			ColumnStep columnStep = columnSteps.get(i);
			columnStep.x = (int)(i * stepWidth);
			
			if(!remainingWidth.containsKey(columnStep.line)) {
				remainingWidth.put(columnStep.line, width);
			}
			columnStep.width = remainingWidth.get(columnStep.line) - columnStep.x;
			remainingWidth.put(columnStep.line, columnStep.x);
		}
	}
	
	public ColumnStep getHoveringStep(int line, int x) {
		if(line < this.line || line >= this.line + this.linecount)
			return null;
		
		int index = x * columnSteps.size() / width;
		return columnSteps.get(index);
	}
}
