package learnableprogramming;

import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;

public class ContributedRulerColumn1 extends org.eclipse.jface.text.source.AbstractRulerColumn implements org.eclipse.ui.texteditor.rulers.IContributedRulerColumn{

	public ContributedRulerColumn1() {
		// TODO Auto-generated constructor stub
		setWidth(30);
	}
	
	@Override
	public RulerColumnDescriptor getDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescriptor(RulerColumnDescriptor descriptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEditor(ITextEditor editor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ITextEditor getEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void columnCreated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void columnRemoved() {
		// TODO Auto-generated method stub
		
	}

	

}
