package nl.avans.plugin.view;

import nl.avans.plugin.model.Variable;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;

public class VariableFigure extends RectangleFigure {
	
	public VariableFigure(Variable variable) {
		super();
		
		setBackgroundColor(new Color(null, 255, 255, 0));
		
		setLayoutManager(new ToolbarLayout());
		add(new Label(variable.getName()));
		
	}

}
