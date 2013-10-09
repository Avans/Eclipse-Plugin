package nl.avans.plugin;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;

public class AvansRulerColumn extends org.eclipse.jface.text.source.AbstractRulerColumn implements org.eclipse.ui.texteditor.rulers.IContributedRulerColumn{

	RulerColumnDescriptor descriptor;
	ITextEditor editor;
	
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
