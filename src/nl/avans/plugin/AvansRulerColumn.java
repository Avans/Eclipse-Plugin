package nl.avans.plugin;

import java.util.ArrayList;
import java.util.List;

import nl.avans.plugin.column.ColumnStep;
import nl.avans.plugin.column.ColumnStep.State;
import nl.avans.plugin.column.LoopStepContainer;
import nl.avans.plugin.ui.stepline.StepLineDisplayer;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.BooleanValue;
import nl.avans.plugin.value.IntValue;
import nl.avans.plugin.value.Value;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.AbstractRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.text.source.VerticalRulerEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;

public class AvansRulerColumn extends AbstractRulerColumn implements
IContributedRulerColumn, IDocumentListener, MouseMoveListener,
MouseListener, MouseTrackListener {

	RulerColumnDescriptor descriptor;
	ITextEditor editor;

	List<ColumnStep> columnSteps = new ArrayList<ColumnStep>();

	@Override
	protected void paintLine(GC gc, int modelLine, int widgetLine,
			int linePixel, int lineHeight) {

		super.paintLine(gc, modelLine, widgetLine, linePixel, lineHeight);

		// Draw alternating background
		if (widgetLine % 2 == 0) {
			gc.setBackground(new Color(null, 240, 240, 255));
			gc.fillRectangle(0, linePixel, getWidth(), lineHeight);
		}

		Value maximalValue = null;

		for (ColumnStep columnStep : columnSteps) {
			if (columnStep.line == widgetLine
					&& (maximalValue == null || columnStep.value
					.compareTo(maximalValue) > 0)) {
				maximalValue = columnStep.value;
			}
		}

		for (ColumnStep columnStep : columnSteps) {
			if (columnStep.line == widgetLine) {
				State state = State.EXECUTED; // By default display as executed

				if (activeColumnStep != null) {
					if (columnStep == activeColumnStep) {
						state = State.CURRENT;
					} else if (columnStep.index < activeColumnStep.index) {
						state = State.EXECUTED;
					} else {
						state = State.NON_EXECUTED;
					}

				}
				if (columnStep == activeColumnStep) {
					state = State.CURRENT;
				}

				columnStep
				.paint(gc, maximalValue, linePixel, lineHeight, state);
			}
		}
	}

	public AvansRulerColumn() {
		setWidth(150);

		int index = 0;
		ColumnStep step1 = new ColumnStep();
		step1.index = index++;
		step1.line = 3;
		step1.x = 0;
		step1.width = getWidth();
		List<StepLine> list2 = new ArrayList<StepLine>();
		list2.add(new StepLine("Zet variabele 'x' op 0", 3, true));
		step1.stepLines = list2;
		step1.value = new IntValue(0);
		columnSteps.add(step1);

		loop = new LoopStepContainer(5, 3, getWidth());
		
		int iteration = 5;
		int x = 0;
		int WIDTH = getWidth() / (iteration + 1);
		while (x <= iteration) {
			ColumnStep condition_step = new ColumnStep();
			condition_step.index = index++;
			condition_step.line = 5;
			condition_step.width = WIDTH;
			condition_step.x = x * WIDTH;
			condition_step.value = new BooleanValue(x == iteration ? false
					: true);

			List<StepLine> list = new ArrayList<StepLine>();
			if (x == iteration) {
				list.add(new StepLine("Omdat " + x + " < " + iteration
						+ " niet waar is...", 5, true));
				list.add(new StepLine("...stoppen we met loopen", 8, false));
			} else {
				list.add(new StepLine("Omdat " + x + " < " + iteration + "...",
						5, true));
				list.add(new StepLine("...doen we dit", 6, false));
				list.add(new StepLine("...en dit", 7, false));
				list.add(new StepLine("...en proberen we opnieuw", 8, false));
			}
			condition_step.stepLines = list;
			columnSteps.add(condition_step);
			loop.addColumnStep(condition_step);

			if (x < iteration) {

				ColumnStep print_step = new ColumnStep();
				print_step.index = index++;
				print_step.line = 6;
				print_step.width = WIDTH;
				print_step.x = x * WIDTH;
				print_step.value = new nl.avans.plugin.value.StringValue(x + "");
				list = new ArrayList<StepLine>();
				list.add(new StepLine("Print \"" + x + "\"", 6, true));
				print_step.stepLines = list;
				columnSteps.add(print_step);
				loop.addColumnStep(print_step);

				ColumnStep increment_step = new ColumnStep();
				increment_step.index = index++;
				increment_step.line = 7;
				increment_step.width = WIDTH;
				increment_step.x = x * WIDTH;
				increment_step.value = new IntValue(x + 1);
				list = new ArrayList<StepLine>();
				list.add(new StepLine("Zet variabele 'x' op " + (x + 1), 7,
						true));
				increment_step.stepLines = list;
				columnSteps.add(increment_step);
				loop.addColumnStep(increment_step);
			}

			x++;

		}
		loop.layout();
	}

	@Override
	public Control createControl(CompositeRuler parentRuler,
			Composite parentControl) {
		Control control = super.createControl(parentRuler, parentControl);
		control.addMouseMoveListener(this);
		control.addMouseTrackListener(this);
		return control;
	}

	@Override
	public RulerColumnDescriptor getDescriptor() {
		return this.descriptor;
	}

	@Override
	public void setDescriptor(RulerColumnDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	private StepLineDisplayer stepLineDisplayer;

	@Override
	public void setEditor(ITextEditor editor) {
		CompilationUnitEditor cueditor = (CompilationUnitEditor) editor;

		stepLineDisplayer = new StepLineDisplayer(cueditor);
		this.editor = editor;
	}

	@Override
	public ITextEditor getEditor() {
		return editor;
	}

	@Override
	public void columnCreated() {
		System.out.println("Column created");

	}

	@Override
	public void columnRemoved() {
		System.out.println("Column removed");
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		// Not interesting
	}

	@Override
	public void documentChanged(DocumentEvent event) {

		ITypeRoot typeRoot = EditorUtility.getEditorInputJavaElement(
				getEditor(), true);
		System.out.println("Document changed" + typeRoot);
		/*
		 * if (typeRoot == null) { throw new
		 * CoreException(getErrorStatus("Editor not showing a CU or class file",
		 * null)); //$NON-NLS-1$ } fTypeRoot= typeRoot;
		 */
	}

	private ColumnStep activeColumnStep;
	private LoopStepContainer loop;

	@Override
	public void mouseMove(MouseEvent e) {
		ColumnStep columnStep = getColumnStepAtCoordinates(e.x, e.y);

		setActiveColumnStep(columnStep);
	}

	public void setActiveColumnStep(ColumnStep columnStep) {
		if (columnStep != activeColumnStep) {
			activeColumnStep = columnStep;

			if (activeColumnStep != null) {
				stepLineDisplayer.showStepLines(activeColumnStep.stepLines);
			} else {
				stepLineDisplayer.removeAllStepLines();
			}
			redraw();
		}
	}

	/**
	 * Returns the step that is visible at the specified coordinates
	 */
	private ColumnStep getColumnStepAtCoordinates(int x, int y) {
		int line = toDocumentLineNumber(y);

		if (line == -1)
			return null;

		ColumnStep loopColumnStep = loop.getHoveringStep(line, x);
		if(loopColumnStep != null)
			return loopColumnStep;
		
		for (ColumnStep columnStep : columnSteps) {
			if (columnStep.line == line && columnStep.isHovering(x)) {
				return columnStep;
			}
		}

		return null;
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDown(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseUp(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEnter(MouseEvent me) {

		if(true)
			return;
		IJavaProject myJavaProject = null;
		IProject myProject = null;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {
			try {
				// only work on open projects with the Java nature
				if (project.isOpen()
						& project.isNatureEnabled(JavaCore.NATURE_ID)) {
					myProject = project;
					myJavaProject = JavaCore.create(project);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}


		IVMInstall vmInstall = null;
		try {
			vmInstall = JavaRuntime.getVMInstall(myJavaProject);
			if (vmInstall == null)
				vmInstall = JavaRuntime.getDefaultVMInstall();
			if (vmInstall != null) {
				IVMRunner vmRunner = vmInstall.getVMRunner(ILaunchManager.DEBUG_MODE);
				if (vmRunner != null) {
					String[] classPath = null;
					try {
						classPath = JavaRuntime.computeDefaultRuntimeClassPath(myJavaProject);
					} catch (CoreException e) { }
					if (classPath != null) {
						VMRunnerConfiguration vmConfig = 
								new VMRunnerConfiguration("TienTeller", classPath);
						
						ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
						JavaSourceLookupDirector sourceLocator = new JavaSourceLookupDirector();
						sourceLocator.initializeDefaults(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()[0]);
						
						ILaunch launch = new Launch(null, ILaunchManager.DEBUG_MODE, sourceLocator);
						
						IType tienTeller = myJavaProject.findType("TienTeller");
						IMethod main = tienTeller.getMethods()[0];
						String signature = main.getSignature();
						
						System.out.println(tienTeller + " " + main + " " + signature);
						
						//launch.
						
						JDIDebugModel.addJavaBreakpointListener(new MyListener(myJavaProject));
						int linenumber = 6;
						JDIDebugModel.createLineBreakpoint(tienTeller.getResource(), tienTeller.getFullyQualifiedName(), linenumber, -1, -1, 0, true, null);
						
						
						//JDIDebugModel.createMethodEntryBreakpoint(myProject, "TienTeller", "main", signature, -1, -1, -1, -0, true, null);
						vmRunner.run(vmConfig, launch, null);
					}
				}
			}
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	@Override
	public void mouseExit(MouseEvent e) {
		setActiveColumnStep(null);

	}

	@Override
	public void mouseHover(MouseEvent e) {

	}
}
