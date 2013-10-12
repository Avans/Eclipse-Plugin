package nl.avans.plugin.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class StepAnnotationPainter implements IDrawingStrategy,
		LineBackgroundListener {

	// The strategy id that we use to register ourselves in the
	// AnnotationPainter
	static final String STRATEGY_ID = "nl.avans.step-annotation-strategy";

	static final Color BACKGROUND_COLOR = new Color(Display.getCurrent(), 255,
			255, 180);
	private static final Color TEXT_COLOR = new Color(Display.getCurrent(), 50,
			50, 50);
	LineBackgroundListener backgroundClearer = new LineBackgroundListener() {
		@Override
		public void lineGetBackground(LineBackgroundEvent event) {
			event.lineBackground = null;
		}
	};

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget,
			int offset, int length, Color color) {

		StepLineAnnotation stepLineAnnotation = (StepLineAnnotation) annotation;

		int lineStartOffset = textWidget.getOffsetAtLine(stepLineAnnotation
				.getLine());
		int lengthToNextLineStartOffset = textWidget.getOffsetAtLine(stepLineAnnotation
				.getLine() + 1) - lineStartOffset;
		
		if (gc != null) {

			textWidget.addLineBackgroundListener(stepLineAnnotation);
			textWidget.redrawRange(lineStartOffset, lengthToNextLineStartOffset, true); // Draw background

			int y = textWidget.getLocationAtOffset(lineStartOffset).y;

			gc.setFont(new Font(gc.getDevice(), "Arial", 12, SWT.BOLD));
			gc.setBackground(BACKGROUND_COLOR);
			gc.setForeground(TEXT_COLOR);
			gc.drawText(stepLineAnnotation.getText(),
					stepLineAnnotation.getDrawOffset(), y);
		} else {
			textWidget.removeLineBackgroundListener(stepLineAnnotation);
			textWidget.addLineBackgroundListener(backgroundClearer);
			textWidget.redrawRange(lineStartOffset, lengthToNextLineStartOffset, true);
		}

	}

	@Override
	public void lineGetBackground(LineBackgroundEvent event) {
		event.lineBackground = BACKGROUND_COLOR;
	}

}
