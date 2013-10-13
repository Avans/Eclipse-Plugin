package nl.avans.plugin.ui.stepline;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

public class StepAnnotationPainter implements IDrawingStrategy {

	// The strategy id that we use to register ourselves in the
	// AnnotationPainter
	static final String STRATEGY_ID = "nl.avans.step-annotation-strategy";

	static final Color BACKGROUND_COLOR = new Color(Display.getCurrent(), 255,
			255, 180);
	static final Color TEXT_COLOR = new Color(Display.getCurrent(), 50, 50, 50);

	/**
	 * Paints the explanation text in the editor. This method just paints the
	 * text, see StepLineDisplayer for the background for the entire line.
	 */
	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget,
			int offset, int length, Color color) {
		StepLineAnnotation stepLineAnnotation = (StepLineAnnotation) annotation;

		if (gc != null) {
			int lineStartOffset = textWidget.getOffsetAtLine(stepLineAnnotation
					.getLine());
			int y = textWidget.getLocationAtOffset(lineStartOffset).y;

			gc.setFont(new Font(gc.getDevice(), "Arial", 12, stepLineAnnotation.isBold() ? SWT.BOLD : SWT.NORMAL));
			gc.setBackground(BACKGROUND_COLOR);
			gc.setForeground(TEXT_COLOR);
			gc.drawText(stepLineAnnotation.getText(),
					stepLineAnnotation.getTextOffset(), y);
		} else {
			textWidget.redrawRange(offset, length, true);
		}
	}
}
