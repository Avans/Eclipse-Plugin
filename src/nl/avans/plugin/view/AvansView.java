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
		Stack stack = new Stack();
		StackFrame stackframe = new StackFrame();
		Variable foo = new Variable("foo", new IntValue(5));
		stackframe.addVariable(foo);
		stack.addStackFrame(stackframe);
		
		Figure stackFigure = new StackFigure(stack);
		rf.add(stackFigure, BorderLayout.LEFT);
		
		Figure heap = new RectangleFigure();
		heap.setBackgroundColor(new org.eclipse.swt.graphics.Color(null, 0, 0, 255));
		heap.setSize(50, 100);
		rf.add(heap, BorderLayout.CENTER);

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

	public void createPartControl(Composite parent) {
		ProgramExecutionManager.getDefault().addProgramStateListener(this);

		// Graph will hold all other objects
		graph = new Graph(parent, SWT.NONE);
		// now a few nodes
		GraphNode node1 = new GraphNode(graph, SWT.NONE, "Jim");
		GraphNode node2 = new GraphNode(graph, SWT.NONE, "Jack");
		GraphNode node3 = new GraphNode(graph, SWT.NONE, "Joe");
		GraphNode node4 = new GraphNode(graph, SWT.NONE, "Bill");

		node1.setSize(100, 100);

		GraphContainer container = new GraphContainer(graph, ZestStyles.NONE);

		GraphNode node5 = new GraphNode(container, SWT.NONE, "Paul");

		// Lets have a directed connection
		new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, node1,
				node2);
		// Lets have a dotted graph connection
		new GraphConnection(graph, ZestStyles.CONNECTIONS_DOT, node2, node3);
		// Standard connection
		new GraphConnection(graph, SWT.NONE, node3, node1);
		// Change line color and line width
		GraphConnection graphConnection = new GraphConnection(graph, SWT.NONE,
				node1, node4);
		graphConnection.changeLineColor(parent.getDisplay().getSystemColor(
				SWT.COLOR_GREEN));
		// Also set a text
		graphConnection.setText("This is a text");
		graphConnection.setHighlightColor(parent.getDisplay().getSystemColor(
				SWT.COLOR_RED));
		graphConnection.setLineWidth(3);

		graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		graph.setLayoutAlgorithm(new SpringLayoutAlgorithm() {

		}, true);
		graph.getLayoutAlgorithm();
		// Selection listener on graphConnect or GraphNode is not supported
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=236528
		graph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(e);
			}

		});
	}

	public void setLayoutManager() {
		switch (layout) {
		case 1:
			graph.setLayoutAlgorithm(new TreeLayoutAlgorithm(
					LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
			layout++;
			break;
		case 2:
			graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(
					LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
			layout = 1;
			break;

		}

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */

	public void setFocus() {
	}

	@Override
	public void programStateChanged(State newState) {

		System.out.println("State: " + newState);

	}

	@Override
	public void programStateRemoved() {
		System.out.println("Removed state!");

	}
}