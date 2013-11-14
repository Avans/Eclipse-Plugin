package nl.avans.plugin.view;

import java.awt.Color;

import nl.avans.plugin.debug.ProgramExecutionManager;
import nl.avans.plugin.debug.ProgramExecutionManager.ProgramExecutionListener;
import nl.avans.plugin.debug.ProgramExecutionManager.ProgramStateListener;
import nl.avans.plugin.debug.State;
import nl.avans.plugin.model.Stack;
import nl.avans.plugin.model.StackFrame;
import nl.avans.plugin.model.Variable;
import nl.avans.plugin.value.IntValue;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphContainer;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

public class AvansView extends ViewPart implements ProgramStateListener {
	public static final String ID = "de.vogella.zest.first.view";
	private Graph graph;
	private int layout = 1;

	public static void main(String args[]) {

		
		
		// Boilerplate canvas code
		Shell shell = new Shell(new Display());
		shell.setSize(365, 280);
		shell.setLayout(new FillLayout());
		shell.setText("Avans Test View");
		Canvas canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		LightweightSystem lws = new LightweightSystem(canvas);

		IFigure rf = lws.getRootFigure();
		rf.setBackgroundColor(new org.eclipse.swt.graphics.Color(null, 0, 255, 0));

		
		
		LayoutManager layout = new BorderLayout();
		rf.setLayoutManager(layout);

		
		// Create a test model
		/*Stack stack = new Stack();
		StackFrame stackframe = new StackFrame();
		Variable foo = new Variable("foo", new IntValue(5));
		stackframe.addVariable(foo);
		stack.addStackFrame(stackframe);
		
		Figure stackFigure = new StackFigure(stack);
		rf.add(stackFigure, BorderLayout.LEFT);
		
		Figure heap = new RectangleFigure();
		heap.setBackgroundColor(new org.eclipse.swt.graphics.Color(null, 0, 0, 255));
		heap.setSize(50, 100);
		rf.add(heap, BorderLayout.CENTER);*/

		// AvansView avans = new AvansView();
		// avans.createPartControl(canvas);

		Display display = shell.getDisplay();
		shell.open();
		while (!shell.isDisposed()) {
			while (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();

		ProgramExecutionManager.getDefault().removeProgramStateListener(this);
	}

	IFigure root;
	public void createPartControl(Composite parent) {
		ProgramExecutionManager.getDefault().addProgramStateListener(this);
		
		// Create drawable surface
		Canvas canvas = new Canvas(parent, SWT.NONE);
		LightweightSystem lws = new LightweightSystem(canvas);
		root = lws.getRootFigure();
		root.setBackgroundColor(new org.eclipse.swt.graphics.Color(null, 255, 0, 0));
		root.setLayoutManager(new BorderLayout());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */

	public void setFocus() {
	}

	@Override
	public void programStateChanged(State state) {
		// Remove previous figures
		if(stackFigure != null) {
			root.remove(stackFigure);
		}
		
		stackFigure = new StackFigure(state.getStack());
		
		root.add(stackFigure, BorderLayout.LEFT);
		
		System.out.println("State: " + state);

	}
	
	StackFigure stackFigure;

	@Override
	public void programStateRemoved() {
		System.out.println("Removed state!");
		
		if(stackFigure != null) {
			root.remove(stackFigure);
			stackFigure = null;
		}

	}
}