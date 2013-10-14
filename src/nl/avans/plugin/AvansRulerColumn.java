package nl.avans.plugin;

import java.util.ArrayList;
import java.util.List;

import nl.avans.plugin.column.ColumnStep;
import nl.avans.plugin.column.ColumnStep.State;
import nl.avans.plugin.ui.stepline.StepLineDisplayer;
import nl.avans.plugin.ui.stepline.StepLine;

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
				
				if(activeColumnStep != null) {
					if(columnStep == activeColumnStep) {
						state = State.CURRENT;
					} else if(columnStep.index < activeColumnStep.index) {
						state = State.EXECUTED;
					} else {
						state = State.NON_EXECUTED;
					}
					
					
				}
				if(columnStep == activeColumnStep) {
					state = State.CURRENT;
				}

				columnStep.paint(gc, linePixel, lineHeight, state);
			}
		}
	}

	public AvansRulerColumn() {
		setWidth(60);

		ColumnStep step1 = new ColumnStep();
		step1.index = 0;
		step1.line = 3;
		step1.x = 0;
		step1.width = getWidth();
		List<StepLine> list2 = new ArrayList<StepLine>();
		list2.add(new StepLine("Zet variabele 'x' op 0", 3, true));
		step1.stepLines = list2;
		

		ColumnStep step2 = new ColumnStep();
		step2.index = 1;
		step2.line = 5;
		step2.x = 0;
		step2.width = getWidth();
		
		List<StepLine> list = new ArrayList<StepLine>();
		list.add(new StepLine("Omdat 0 < 5...", 5, true));
		list.add(new StepLine("...doen we dit", 6, false));
		list.add(new StepLine("...en dit", 7, false));
		list.add(new StepLine("...en proberen we opnieuw", 8, false));
		step2.stepLines = list;

		columnSteps.add(step1);
		columnSteps.add(step2);
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
			
			if(activeColumnStep != null) {
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
