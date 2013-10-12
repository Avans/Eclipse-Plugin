package nl.avans.plugin;

import nl.avans.plugin.ui.StepDisplayer;
import nl.avans.plugin.ui.StepLine;

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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;

public class AvansRulerColumn extends AbstractRulerColumn implements
		IContributedRulerColumn, IDocumentListener, MouseMoveListener, MouseListener, MouseTrackListener {

	RulerColumnDescriptor descriptor;
	ITextEditor editor;

	@Override
	protected void paintLine(GC gc, int modelLine, int widgetLine,
			int linePixel, int lineHeight) {

		super.paintLine(gc, modelLine, widgetLine, linePixel, lineHeight);

		gc.setForeground(new org.eclipse.swt.graphics.Color(null, 0, 255, 0));
		gc.drawRectangle(5, linePixel, 20, lineHeight - 1);
	}

	public AvansRulerColumn() {
		setWidth(30);
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
	
	private StepDisplayer stepLineDisplayer;

	@Override
	public void setEditor(ITextEditor editor) {
		CompilationUnitEditor cueditor = (CompilationUnitEditor) editor;

		stepLineDisplayer = new StepDisplayer(cueditor);
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

	@Override
	public void mouseMove(MouseEvent e) {
		//System.out.println(e.x + ", " + e.y);
		
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
		System.out.println("Enter");
		stepLineDisplayer.addStepLine(new StepLine("Hallo wereld", 5));
	}

	@Override
	public void mouseExit(MouseEvent e) {
		System.out.println("Exit");
		stepLineDisplayer.removeAllStepLines();
		
	}

	@Override
	public void mouseHover(MouseEvent e) {
		
		
	}
}
