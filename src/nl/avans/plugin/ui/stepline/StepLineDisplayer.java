package nl.avans.plugin.ui.stepline;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
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
public class StepLineDisplayer {

	// The 'type' that all annotations displayed by this class will have
	final static String ANNOTATION_TYPE = "nl.avans.step-annotation";

	// The distance the explanation text will have from the left side of the
	// editor, if there is no overlap with source code.
	private final static int DEFAULT_TEXT_OFFSET = 200;

	// The minimal horizontal margin between the source code and the explanation
	// text
	private final static int MARGIN = 10;

	Set<StepLineAnnotation> activeAnnotations = new HashSet<StepLineAnnotation>();
	AnnotationPainter painter;
	private IAnnotationModel annotationModel;
	CompilationUnitEditor editor;

	/**
	 * Create a StepDisplayer, it takes an Eclipse editor which is the editor in
	 * which the step annotations will be displayed in.
	 * 
	 * The editor will be augmented to allow for custom painting
	 */
	public StepLineDisplayer(CompilationUnitEditor editor) {
		this.editor = editor;
		painter = getAnnotationPainterForEditor(editor);

		if (painter == null)
			return;

		// Register the custom painter for our custom annotations
		painter.setAnnotationTypeColor(ANNOTATION_TYPE,
				new Color(null, 0, 0, 0));
		painter.addAnnotationType(ANNOTATION_TYPE,
				StepLineAnnotationPainter.STRATEGY_ID);
		painter.addDrawingStrategy(StepLineAnnotationPainter.STRATEGY_ID,
				new StepLineAnnotationPainter());

		// Get a reference to the annotationModel, the thing we can use to add
		// and remove our Annotations.
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		annotationModel = documentProvider.getAnnotationModel(editor
				.getEditorInput());
	}

	/**
	 * Shows the step in the
	 * 
	 * @param step
	 */
	public void showStepLines(List<StepLine> stepLines) {
		removeAllStepLines();

		StyledText textWidget = editor.getViewer().getTextWidget();

		// The distance from the left side of the editor to draw the text from.
		// We want this to be be far enough out that the annotation doesn't
		// overlap with the source itself.
		int textOffset = DEFAULT_TEXT_OFFSET;

		for (StepLine stepLine : stepLines) {
			StepLineAnnotation stepLineAnnotation = new StepLineAnnotation(
					stepLine);
			annotationModel.addAnnotation(stepLineAnnotation, new Position(0));
			textWidget.addLineBackgroundListener(stepLineAnnotation);
			activeAnnotations.add(stepLineAnnotation);

			// Calculate if the textOffset needs to be bigger to compensate for
			// long lines of code
			int line = stepLineAnnotation.getLine();
			int lineStartOffset = textWidget.getOffsetAtLine(line);
			int lineEndOffset = lineStartOffset
					+ textWidget.getLine(line).length();
			Point p = textWidget.getLocationAtOffset(lineEndOffset);
			if (textOffset < p.x + MARGIN)
				textOffset = p.x + MARGIN;
		}

		// Set all annotations to the same text offset
		for (StepLineAnnotation stepLineAnnotation : activeAnnotations) {
			stepLineAnnotation.setTextOffset(textOffset);
		}

		painter.paint(AnnotationPainter.INTERNAL);
		textWidget.redraw();
	}

	/**
	 * Remove all annotations from the view.
	 */
	public void removeAllStepLines() {
		StyledText textWidget = editor.getViewer().getTextWidget();
		for (StepLineAnnotation stepLineAnnotation : activeAnnotations) {
			annotationModel.removeAnnotation(stepLineAnnotation);
			textWidget.removeLineBackgroundListener(stepLineAnnotation);
		}
		activeAnnotations.clear();

		painter.paint(AnnotationPainter.INTERNAL);
		textWidget.redraw();
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
	 * the text annotations that this class uses to annotate steps.
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
	 * that way. Old versions of http://github.com/scala-ide/scala-ide seems to
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
			// The arguments are not used by showAnnotations in any meaningful
			// way
			showAnnotations.invoke(support, null, false);

			// Pry the coveted AnnotationPainter from a private field in the
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
