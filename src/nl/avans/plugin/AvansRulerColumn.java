package nl.avans.plugin;

import java.util.ArrayList;
import java.util.List;

import nl.avans.plugin.column.ColumnStep;
import nl.avans.plugin.column.ColumnStep.State;
import nl.avans.plugin.ui.stepline.StepLineDisplayer;
import nl.avans.plugin.ui.stepline.StepLine;
import nl.avans.plugin.value.BooleanValue;
import nl.avans.plugin.value.IntValue;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
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

				columnStep.paint(gc, linePixel, lineHeight, state);
			}
		}
	}

	public AvansRulerColumn() {
		setWidth(100);
		

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

		
		int iteration = 3;
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
				
				ColumnStep increment_step = new ColumnStep();
				increment_step.index = index++;
				increment_step.line = 7;
				increment_step.width = WIDTH;
				increment_step.x = x * WIDTH;
				increment_step.value = new IntValue(x + 1);
				list = new ArrayList<StepLine>();
				list.add(new StepLine("Zet variabele 'x' op " + (x+1), 7, true));
				increment_step.stepLines = list;
				columnSteps.add(increment_step);
			}

			x++;

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
	public void mouseEnter(MouseEvent e) {
	}

	@Override
	public void mouseExit(MouseEvent e) {
		setActiveColumnStep(null);

	}

	@Override
	public void mouseHover(MouseEvent e) {

	}
}
