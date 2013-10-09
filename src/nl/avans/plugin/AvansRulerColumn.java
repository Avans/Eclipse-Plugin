package nl.avans.plugin;

import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;

public class AvansRulerColumn extends org.eclipse.jface.text.source.AbstractRulerColumn implements org.eclipse.ui.texteditor.rulers.IContributedRulerColumn{

	RulerColumnDescriptor descriptor;
	ITextEditor editor;
	
	@Override
	protected void paintLine(GC gc, int modelLine, int widgetLine,
			int linePixel, int lineHeight) {
		super.paintLine(gc, modelLine, widgetLine, linePixel, lineHeight);

		gc.setForeground(new org.eclipse.swt.graphics.Color(null, 0, 255, 0));
		gc.drawRectangle(5, linePixel, 20, lineHeight-1);
	}
	
	public AvansRulerColumn() {
		setWidth(30);
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
}
