package nl.avans.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.avans.plugin.column.ColumnStep;
import nl.avans.plugin.column.ColumnStep.State;
import nl.avans.plugin.column.LoopStepContainer;
import nl.avans.plugin.model.ProgramExecution;
import nl.avans.plugin.step.Step;
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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;
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
import org.eclipse.debug.internal.ui.sourcelookup.DownAction;

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
	}

	public Set<LoopStepContainer> loops = new HashSet<LoopStepContainer>();

	public void displayProgramExecution(ProgramExecution programExecution) {
		// Empty previous steps
		loops = new HashSet<LoopStepContainer>();
		columnSteps = new ArrayList<ColumnStep>();

		// Create steps to be displayed in the ruler
		int index = 0;
		for (Step step : programExecution.getSteps()) {
			ColumnStep columnStep = new ColumnStep();
			columnStep.line = step.line;
			columnStep.index = index++;
			columnStep.value = step.getValue();
			columnStep.stepLines = step.getStepLines();
			columnSteps.add(columnStep);
		}

		// Find loops in the editor
		createLoops();

		layout();
		redraw();
	}

	public void layout() {
		for (ColumnStep columnStep : columnSteps) {
			columnStep.x = 0;
			columnStep.width = getWidth();
		}
		
		for(LoopStepContainer loop : loops) {
			loop.layout();
		}

	}

	private void createLoops() {
		loops = new HashSet<LoopStepContainer>();
		
		String source = getEditor().getDocumentProvider()
				.getDocument(getEditor().getEditorInput()).get();
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray()); // set source
		parser.setResolveBindings(true); // we need bindings later on
		CompilationUnit cu = (CompilationUnit) parser
				.createAST(null /* IProgressMonitor */); // parse

		// For all types in the file
		for (Object objectType : cu.types()) {
			TypeDeclaration type = (TypeDeclaration) objectType;

			// For all methods in the type
			for (MethodDeclaration method : type.getMethods()) {

				// For all while-statements in the method
				for (Object statementObject : method.getBody().statements()) {
					Statement statement = (Statement) statementObject;
					if (statement.getNodeType() == ASTNode.WHILE_STATEMENT) {

						// Create a loop object
						int startLine = cu.getLineNumber(statement
								.getStartPosition()) - 1; // Subtract 1 to make 0-indexed
						int endLine = cu.getLineNumber(statement
								.getStartPosition() + statement.getLength()) - 1;
						LoopStepContainer loopStepContainer = new LoopStepContainer(
								startLine, endLine - startLine + 1, getWidth());
						loops.add(loopStepContainer);
					}
				}
			}
		}

		// Fill the containers with the appropriate steps
		for (ColumnStep columnStep : columnSteps) {
			for (LoopStepContainer loopStepContainer : loops) {
				loopStepContainer.addIfAppropriate(columnStep);
			}
		}
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

		displayProgramExecution(new ProgramExecution());
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

		// ITypeRoot typeRoot = EditorUtility.getEditorInputJavaElement(
		// getEditor(), true);
		// System.out.println("Document changed" + typeRoot);
		/*
		 * if (typeRoot == null) { throw new
		 * CoreException(getErrorStatus("Editor not showing a CU or class file",
		 * null)); //$NON-NLS-1$ } fTypeRoot= typeRoot;
		 */
	}

	private ColumnStep activeColumnStep;

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

		for(LoopStepContainer loop : loops) {
			ColumnStep loopColumnStep = loop.getHoveringStep(line, x);
			if (loopColumnStep != null)
				return loopColumnStep;	
		}
		

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

		if (true)
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
				IVMRunner vmRunner = vmInstall
						.getVMRunner(ILaunchManager.DEBUG_MODE);
				if (vmRunner != null) {
					String[] classPath = null;
					try {
						classPath = JavaRuntime
								.computeDefaultRuntimeClassPath(myJavaProject);
					} catch (CoreException e) {
					}
					if (classPath != null) {
						VMRunnerConfiguration vmConfig = new VMRunnerConfiguration(
								"TienTeller", classPath);

						ILaunchManager manager = DebugPlugin.getDefault()
								.getLaunchManager();
						JavaSourceLookupDirector sourceLocator = new JavaSourceLookupDirector();
						sourceLocator.initializeDefaults(DebugPlugin
								.getDefault().getLaunchManager()
								.getLaunchConfigurations()[0]);

						ILaunch launch = new Launch(null,
								ILaunchManager.DEBUG_MODE, sourceLocator);

						IType tienTeller = myJavaProject.findType("TienTeller");
						IMethod main = tienTeller.getMethods()[0];
						String signature = main.getSignature();

						System.out.println(tienTeller + " " + main + " "
								+ signature);

						// launch.

						JDIDebugModel.addJavaBreakpointListener(new MyListener(
								myJavaProject));
						int linenumber = 6;
						JDIDebugModel.createLineBreakpoint(
								tienTeller.getResource(),
								tienTeller.getFullyQualifiedName(), linenumber,
								-1, -1, 0, true, null);

						// JDIDebugModel.createMethodEntryBreakpoint(myProject,
						// "TienTeller", "main", signature, -1, -1, -1, -0,
						// true, null);
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
