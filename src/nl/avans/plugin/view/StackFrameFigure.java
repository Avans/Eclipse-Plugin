package nl.avans.plugin.view;

import nl.avans.plugin.model.StackFrame;
import nl.avans.plugin.model.Variable;

import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;

public class StackFrameFigure extends RectangleFigure {

	public StackFrameFigure(StackFrame stackframe) {
		super();
		setBackgroundColor(new Color(null, 255, 0, 255));
		setLayoutManager(new ToolbarLayout(false));
		
		for(Variable variable : stackframe) {
			VariableFigure variableFigure = new VariableFigure(variable);
			add(variableFigure);
		}

	}
}
