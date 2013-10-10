package nl.avans.plugin;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;

public class AvansRulerColumn extends org.eclipse.jface.text.source.AbstractRulerColumn implements org.eclipse.ui.texteditor.rulers.IContributedRulerColumn, IDocumentListener{

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
		
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		IDocument document = documentProvider.getDocument(editor.getEditorInput());
		
		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());
		
		annotationModel.addAnnotation(new Annotation("com.ibm.example.myannotation", false, "hallo wereld!"), new Position(10,  10));
		
		//SourceViewer sv = (SourceViewer) ((CompilationUnitEditor) editor).getViewer();
		//CompilationUnitEditor cueditor = ((CompilationUnitEditor) editor);
		//cueditor.get
		//sv.getAnnotationModel().addAnnotation(annotation, position);
		//sv.addp
		
		//document.addDocumentListener(this);
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
		
		ITypeRoot typeRoot= EditorUtility.getEditorInputJavaElement(getEditor(), true);
		System.out.println("Document changed" + typeRoot);
		/*
		if (typeRoot == null) {
			throw new CoreException(getErrorStatus("Editor not showing a CU or class file", null)); //$NON-NLS-1$
		}
		fTypeRoot= typeRoot;
		*/
	}
}
