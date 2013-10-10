package nl.avans.plugin;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class ExplainLineAnnotation extends Annotation implements IDrawingStrategy{

	

	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget,
			int offset, int length, Color color) {
		gc.setForeground(new Color(null, 255, 0, 0));
		
		gc.drawRectangle(new Rectangle(0, 0, 100, 100));
		
	}

}
