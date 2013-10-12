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

public class StepAnnotationPainter implements IDrawingStrategy,
		LineBackgroundListener {

	// The strategy id that we use to register ourselves in the
	// AnnotationPainter
	static final String STRATEGY_ID = "nl.avans.step-annotation-strategy";

	static final Color BACKGROUND_COLOR = new Color(null, 255, 255, 180);
	private static final Color TEXT_COLOR = new Color(null, 50, 50, 50);

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget,
			int offset, int length, Color color) {

		StepLineAnnotion stepLineAnnotation = (StepLineAnnotion) annotation;

		if (gc != null) {
			textWidget.addLineBackgroundListener(stepLineAnnotation);

			int lineStartOffset = textWidget.getOffsetAtLine(stepLineAnnotation
					.getLine());
			int y = textWidget.getLocationAtOffset(lineStartOffset).y;

			gc.setFont(new Font(gc.getDevice(), "Arial", 12, SWT.BOLD));
			gc.setBackground(BACKGROUND_COLOR);
			gc.setForeground(TEXT_COLOR);
			gc.drawText(stepLineAnnotation.getText(),
					stepLineAnnotation.getDrawOffset(), y);
		} else {
			textWidget.removeLineBackgroundListener(stepLineAnnotation);
			textWidget.redrawRange(offset, length, true);
		}

	}

	@Override
	public void lineGetBackground(LineBackgroundEvent event) {
		event.lineBackground = BACKGROUND_COLOR;
	}

}
