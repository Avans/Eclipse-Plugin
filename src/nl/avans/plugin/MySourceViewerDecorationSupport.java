package nl.avans.plugin;

import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class MySourceViewerDecorationSupport extends SourceViewerDecorationSupport {
	public MySourceViewerDecorationSupport(ISourceViewer sourceViewer,
			IOverviewRuler overviewRuler, IAnnotationAccess annotationAccess,
			ISharedTextColors sharedTextColors) {
		super(sourceViewer, overviewRuler, annotationAccess, sharedTextColors);
	}

	@Override
	protected AnnotationPainter createAnnotationPainter() {
		System.out.println("Annotation painter yay!");
		return super.createAnnotationPainter();
	}

}
