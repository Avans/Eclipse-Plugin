package nl.avans.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.avans.plugin.column.ColumnStep;
import nl.avans.plugin.column.ColumnStep.State;
import nl.avans.plugin.column.LoopingSegment;
import nl.avans.plugin.debug.JavaDebuggerListener;
import nl.avans.plugin.debug.ProgramExecutionManager;
import nl.avans.plugin.debug.ProgramExecutionManager.ProgramExecutionListener;
import nl.avans.plugin.model.ProgramExecution;
import nl.avans.plugin.step.Step;
import nl.avans.plugin.ui.stepline.StepLineDisplayer;
import nl.avans.plugin.value.Value;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.AbstractRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;

public class AvansRulerColumn extends AbstractRulerColumn implements
		IContributedRulerColumn, MouseMoveListener, MouseListener,
		MouseTrackListener, IDocumentListener, ProgramExecutionListener {

	// Mandatory fields
	private RulerColumnDescriptor descriptor;
	private ITextEditor editor;

	/**
	 * An object which is used to display StepLine explanations in the source
	 * code editor
	 */
	private StepLineDisplayer stepLineDisplayer;

	/**
	 * All steps that are visible in the ruler. Including those that are in
	 * looping segments
	 */
	private List<ColumnStep> columnSteps = new ArrayList<ColumnStep>();

	/**
	 * Loops have separate layout and hovering rules. For this purpose we keep
	 * track of looping segments in the source code. These define while-loops,
	 * for-loops and do-while-loops. We only keep track of outerloops in
	 * methods. Loops within loops are not considered.
	 * 
	 * Steps are organized in loops, but are also in the columnSteps field.
	 */
	private Set<LoopingSegment> loops = new HashSet<LoopingSegment>();

	/**
	 * The active columnStep. The one that the user is hovering over and
	 */
	private ColumnStep activeColumnStep;

	public AvansRulerColumn() {
		setWidth(150);
	}

	/**
	 * Display a particular ProgramExecution. All steps in the execution are
	 * copied into special ColumnStep objects, which contain more fields with
	 * regard to layout.
	 * 
	 * Needs to be called from the UI thread
	 * 
	 * @param programExecution
	 */
	public void displayProgramExecution(ProgramExecution programExecution) {
		if (programExecution == null && columnSteps.size() == 0
				&& loops.size() == 0)
			return;

		// Empty previous steps
		loops = new HashSet<LoopingSegment>();
		columnSteps = new ArrayList<ColumnStep>();

		if (programExecution != null) {
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
		}

		// Find loops in the editor
		recreateLoopingSegments();

		layout();
		redraw();
	}

	@Override
	public void programExecutionChanged(
			final ProgramExecution newProgramExecution) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				displayProgramExecution(newProgramExecution);
			}
		});

	}

	@Override
	public void programExecutionRemoved() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				displayProgramExecution(null);
			}
		});
	}

	/**
	 * Sets the x and width property of all ColumnSteps
	 */
	public void layout() {
		// By default make them the size of the column itself
		for (ColumnStep columnStep : columnSteps) {
			columnStep.x = 0;
			columnStep.width = getWidth();
		}

		// Except those in ColumnSteps in looping segments
		for (LoopingSegment loop : loops) {
			loop.layout(getWidth());
		}
	}

	/**
	 * Paint a single line of the ruler. Calls the paint() method of all
	 * relevant columnSteps which can draw themselves.
	 */
	@Override
	protected void paintLine(GC gc, int modelLine, int widgetLine,
			int linePixel, int lineHeight) {
		super.paintLine(gc, modelLine, widgetLine, linePixel, lineHeight);

		// Draw alternating background
		if (widgetLine % 2 == 0) {
			gc.setBackground(new Color(null, 240, 240, 255));
			gc.fillRectangle(0, linePixel, getWidth(), lineHeight);
		}

		// Find a maximalValue, which is used for some data visualization in
		// numeric steps
		Value maximalValue = null;
		ColumnStep.DisplayMode displayMode = ColumnStep.DisplayMode.FULL;
		for (ColumnStep columnStep : columnSteps) {
			if (columnStep.line == widgetLine) {

				if (maximalValue == null
						|| columnStep.value.compareTo(maximalValue) > 0) {
					maximalValue = columnStep.value;
				}

				ColumnStep.DisplayMode preferredDisplayMode = columnStep
						.getDisplayMode(gc);
				
				
				if (preferredDisplayMode.priority < displayMode.priority) {
					displayMode = preferredDisplayMode;
				}
			}
		}

		// Draw each columnStep in this line
		for (ColumnStep columnStep : columnSteps) {
			if (columnStep.line == widgetLine) {
				State state;

				if (columnStep == activeColumnStep) {
					state = State.CURRENT;
				} else if (activeColumnStep != null) {
					if (columnStep.index < activeColumnStep.index) {
						state = State.EXECUTED;
					} else {
						state = State.NON_EXECUTED;
					}
				} else {
					state = State.EXECUTED; // By default display as executed
				}

				columnStep.paint(gc, displayMode, maximalValue, linePixel,
						lineHeight, state);
			}
		}
	}

	/**
	 * Creates all the LoopingSegment objects based on the current ColumnStep
	 * objects. The position of the loops in the editor are found by means of
	 * parsing the source with an ASTParser.
	 */
	private void recreateLoopingSegments() {
		loops = new HashSet<LoopingSegment>();

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
								.getStartPosition()) - 1; // Subtract 1 to make
															// 0-indexed
						int endLine = cu.getLineNumber(statement
								.getStartPosition() + statement.getLength()) - 1;
						LoopingSegment loop = new LoopingSegment(startLine,
								endLine - startLine + 1);
						loops.add(loop);
					}
				}
			}
		}

		// Fill the LoopingSegments with the appropriate steps
		for (ColumnStep columnStep : columnSteps) {
			for (LoopingSegment loop : loops) {
				loop.addColumnStepIfCorrectLine(columnStep);
			}
		}
	}

	/**
	 * Only known way to add mouse listeners to the column is by overwriting
	 * createControl()
	 */
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

	@Override
	public void setEditor(ITextEditor editor) {
		CompilationUnitEditor cueditor = (CompilationUnitEditor) editor;

		// Create a StepLineDisplayer. This object can display explanations
		// behind source code in the editor. Used to display StepLines like 'Set
		// variable x to 4'.
		if (stepLineDisplayer != null)
			stepLineDisplayer.removeAllStepLines();
		stepLineDisplayer = new StepLineDisplayer(cueditor);

		this.editor = editor;

		IDocument document = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());
		document.addDocumentListener(this);
	}

	@Override
	public ITextEditor getEditor() {
		return editor;
	}

	@Override
	public void columnCreated() {
		System.out.println("Column created");
		ProgramExecutionManager.getDefault().addProgramExecutionListener(this);

	}

	@Override
	public void columnRemoved() {
		System.out.println("Column removed");
		ProgramExecutionManager.getDefault().removeProgramExecutionListener(
				this);
	}

	/**
	 * Sets the active column step.
	 * 
	 * This step is displayed in red, indicating that we are 'this far' in the
	 * program. All previous steps will be drawn as 'executed' (dark gray), all
	 * next steps will be drawn as 'to be executed' (light gray)
	 * 
	 * If the step has some StepLine explanations associated with it they will
	 * be displayed in the editor.
	 * 
	 * Call this method with null to indicate that there is no currently active
	 * step.
	 * 
	 * @param columnStep
	 */
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
	 * Returns the step that should be selected when the user hovers at the
	 * specified coordinates
	 */
	private ColumnStep getColumnStepAtCoordinates(int x, int y) {
		int line = toDocumentLineNumber(y);
		if (line == -1)
			return null;

		// First, check the looping segments of the ruler.
		// The looping segments have different behavior when the user hovers
		// over them.
		// They only look at the x coordinate and then return the step that is
		// that far in the loop
		// Allowing for a scrubbing motion through the steps and getting an idea
		// for the loop behavior
		for (LoopingSegment loop : loops) {
			ColumnStep loopColumnStep = loop.getHoveringStep(line, x,
					getWidth());
			if (loopColumnStep != null)
				return loopColumnStep;
		}

		// Normal step hovering: Checks if the mouse is hovering over the
		// paintable area of the column step.
		for (ColumnStep columnStep : columnSteps) {
			if (columnStep.line == line && columnStep.isHovering(x)) {
				return columnStep;
			}
		}

		return null;
	}

	/**
	 * Listen to the mouse moving to keep the activeColumnStep field up to date
	 */
	@Override
	public void mouseMove(MouseEvent e) {
		ColumnStep columnStep = getColumnStepAtCoordinates(e.x, e.y);
		setActiveColumnStep(columnStep);
	}

	/**
	 * Listen to the mouse leaving the column area, so we can reset the
	 * activeColumnStep.
	 */
	@Override
	public void mouseExit(MouseEvent e) {
		setActiveColumnStep(null);
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// Not interesting
	}

	@Override
	public void mouseDown(MouseEvent e) {
		// Not interesting
	}

	@Override
	public void mouseUp(MouseEvent e) {
		// Not interesting
	}

	@Override
	public void mouseHover(MouseEvent e) {
		// Not interesting
	}

	@Override
	public void mouseEnter(MouseEvent me) {
		// Not interesting
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		// Not interesting
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		ProgramExecutionManager.getDefault().removeProgramExecution();
	}
}
