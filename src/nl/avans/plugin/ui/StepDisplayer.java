package nl.avans.plugin.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import nl.avans.plugin.step.Step;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * Class that is responsible for showing a step in a graphical way inside the
 * Java Editor
 * 
 * A step is displayed by highlighting the relevant code lines in the editor And
 * annotating all lines with small text labels that explains the current state
 * and actions
 * 
 * For example, an if-statement will be annotated with 'because 5 < 6 we do the
 * following'
 * 
 * @author Paul Wagener
 * 
 */
public class StepDisplayer {
	

	// The 'type' that all annotations displayed by this class will have
	final static String ANNOTATION_TYPE = "nl.avans.step-annotation";
	
	private IAnnotationModel annotationModel;

	/**
	 * Create a StepDisplayer, it takes an Eclipse editor which is the editor in
	 * which the step annotations will be displayed in.
	 * 
	 * The editor will be augmented to allow for custom painting
	 */
	public StepDisplayer(CompilationUnitEditor editor) {
		// this.editor = editor;
		AnnotationPainter painter = getAnnotationPainterForEditor(editor);
		System.out.println("Gots me a painter: " + painter);
		
		if(painter == null)
			return;
		
		// Register the custom painter for our custom annotations
		painter.setAnnotationTypeColor(ANNOTATION_TYPE,
				new Color(null, 0, 0, 0));
		painter.addAnnotationType(ANNOTATION_TYPE, StepAnnotationPainter.STRATEGY_ID);
		painter.addDrawingStrategy(StepAnnotationPainter.STRATEGY_ID, new StepAnnotationPainter());
		
		// Get a reference to the annotationModel, the thing we can use to add and remove ours Annotations.
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		annotationModel = documentProvider
				.getAnnotationModel(editor.getEditorInput());
		
		// TEST: add an annotation
		StepLine stepLine = new StepLine("zet x op 0", 3);
		annotationModel.addAnnotation(new StepLineAnnotion(stepLine), new Position(0));
	}

	/**
	 * Shows the step in the
	 * 
	 * @param step
	 */
	public void showStep(Step step) {
		hideSteps();
	}

	/**
	 * Hide any active step in the editor.
	 */
	public void hideSteps() {

	}

	/**
	 * So... Yeah... And then there's this code...
	 * 
	 * It works right now, in Eclipse Kepler 4.3. But if you are using a newer
	 * version and wondering why the plugin doesn't display step annotations
	 * anymore: look no further than this method right here.
	 * 
	 * getAnnotationPainterForEditor does what it says, it gets the
	 * AnnotationPainter that is associated with the editor. An instance of this
	 * object is necessary to allow for custom annotation painting, specifically
	 * the colored backgrounds and text annotations that this class uses to
	 * annotate steps.
	 * 
	 * Unfortunately it's impossible to get a reference to this object the
	 * legitimate way, as it is hidden deep behind private internal variables.
	 * So we do the thing that goes against all logic and oo-principles, we use
	 * reflection to break into the private fields and force our way to the
	 * object. This will surely reserve me a special spot in programmer hell,
	 * but right now I just want it to work.
	 * 
	 * Possible future solutions:
	 * 
	 * - Paint on the editor without annotations at all (not sure if this is
	 * possible).
	 * 
	 * - Wait till there is a legit way to get the AnnotationPainter or add
	 * drawing strategies, then use that. A fix for bug
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=51498 would be an ideal
	 * solution. But that bug was filed in 2004 and has shown no signs of
	 * progress since.
	 * 
	 * - Somehow let the editor use a subclass of SourceViewerDecorationSupport,
	 * so that we can overwrite createAnnotationPainter() and get an instance
	 * that way. Old versions of http://github.com/scala-ide/scala-ide seems
	 * have references to this technique
	 * 
	 * @see http://www.mi.fu-berlin.de/wiki/pub/SE/
	 *      SoftwaretechnikProjektAgil2013ProductBacklog/TI-NiceCursor.txt
	 * 
	 */
	private static AnnotationPainter getAnnotationPainterForEditor(
			CompilationUnitEditor editor) {
		SourceViewer sourceViewer = (SourceViewer) editor.getViewer();

		// Pry the SourceViewerDecorationSupport from the editor,
		// the object that contains the AnnotationPainter instance
		try {
			Method getSourceViewerDecorationSupport = AbstractDecoratedTextEditor.class
					.getDeclaredMethod("getSourceViewerDecorationSupport",
							ISourceViewer.class);

			getSourceViewerDecorationSupport.setAccessible(true);
			SourceViewerDecorationSupport support = (SourceViewerDecorationSupport) getSourceViewerDecorationSupport
					.invoke(editor, sourceViewer);

			if (support == null)
				return null;

			// First call showAnnotations, a method which will force the
			// AnnotationPainter to instantiate in a private field,
			// if it wasn't instantiated there already.
			Method showAnnotations = SourceViewerDecorationSupport.class
					.getDeclaredMethod("showAnnotations", Object.class,
							boolean.class);
			showAnnotations.setAccessible(true);
			showAnnotations.invoke(support, null, false);

			// Pry the coveted from a private field in the
			// SourceViewerDecorationSupport
			Field f = SourceViewerDecorationSupport.class
					.getDeclaredField("fAnnotationPainter");
			f.setAccessible(true);
			AnnotationPainter annotationPainter = (AnnotationPainter) f
					.get(support);

			return annotationPainter;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
