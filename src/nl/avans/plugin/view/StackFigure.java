package nl.avans.plugin.view;

import nl.avans.plugin.model.Stack;
import nl.avans.plugin.model.StackFrame;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;

public class StackFigure extends RectangleFigure {

	public StackFigure(Stack stack) {
		super();
		
		setBackgroundColor(new Color(null, 255, 255, 255));
		setOutline(false);
		
		FlowLayout layout = new FlowLayout(false);
		layout.setMajorSpacing(10);
		layout.setStretchMinorAxis(true);
		setLayoutManager(layout);
		
		// Add children of the stack as stackframefigures
		for(StackFrame stackframe : stack) {
			StackFrameFigure stf = new StackFrameFigure(stackframe);
			add(stf);
		}
	}
}
